package com.calculator.vault

import android.Manifest
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
import com.calculator.vault.presentation.navigation.VaultNavHost
import com.calculator.vault.presentation.navigation.VaultNavigationManager
import com.calculator.vault.presentation.theme.CalculatorVaultTheme
import com.calculator.vault.security.detection.IntruderCaptureCoordinator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var checkSessionExpiredUseCase: CheckSessionExpiredUseCase
    @Inject lateinit var lockSessionUseCase: LockSessionUseCase
    @Inject lateinit var vaultNavigationManager: VaultNavigationManager
    @Inject lateinit var intruderCaptureCoordinator: IntruderCaptureCoordinator

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* result handled on next capture attempt */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                lockSessionUseCase()
                vaultNavigationManager.requestLockVault()
            }
        }
    }
}
