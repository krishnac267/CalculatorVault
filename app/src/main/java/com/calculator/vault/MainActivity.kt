package com.calculator.vault

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.calculator.vault.domain.usecase.CheckSessionExpiredUseCase
import com.calculator.vault.domain.usecase.LockSessionUseCase
import com.calculator.vault.monetization.BillingManager
import com.calculator.vault.presentation.navigation.VaultNavHost
import com.calculator.vault.presentation.navigation.VaultNavigationManager
import com.calculator.vault.presentation.theme.CalculatorVaultTheme
import com.calculator.vault.security.detection.IntruderCaptureCoordinator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    companion object {
        const val EXTRA_FROM_LAUNCHER = "extra_from_launcher"
    }

    @Inject lateinit var checkSessionExpiredUseCase: CheckSessionExpiredUseCase
    @Inject lateinit var lockSessionUseCase: LockSessionUseCase
    @Inject lateinit var vaultNavigationManager: VaultNavigationManager
    @Inject lateinit var intruderCaptureCoordinator: IntruderCaptureCoordinator
    @Inject lateinit var billingManager: BillingManager

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* result handled on next capture attempt */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        billingManager.startConnection()
        intruderCaptureCoordinator.lifecycleOwner = this
        intruderCaptureCoordinator.hasCameraPermission = {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        }

        setContent {
            CalculatorVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VaultNavHost()
                }
            }
        }

        if (intent.getBooleanExtra(EXTRA_FROM_LAUNCHER, false)) {
            lockVaultImmediately()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_FROM_LAUNCHER, false)) {
            lockVaultImmediately()
        }
    }

    override fun onResume() {
        super.onResume()
        intruderCaptureCoordinator.lifecycleOwner = this
        maybeRequestCameraPermission()
        enforceSessionLockIfExpired()
    }

    override fun onPause() {
        super.onPause()
        enforceSessionLockIfExpired()
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            lockVaultImmediately()
        }
    }

    private fun maybeRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun enforceSessionLockIfExpired() {
        lifecycleScope.launch {
            if (checkSessionExpiredUseCase()) {
                lockVaultImmediately()
            }
        }
    }

    private fun lockVaultImmediately() {
        lifecycleScope.launch {
            lockSessionUseCase()
            vaultNavigationManager.requestLockVault()
        }
    }
}
