package com.calculator.vault.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.calculator.vault.MainActivity
import com.calculator.vault.data.testing.ResetAppForTestingUseCase
import com.calculator.vault.domain.repository.SecurityRepository
import com.calculator.vault.domain.usecase.SetupVaultUseCase
import com.calculator.vault.presentation.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BiometricE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule = E2ETestHelpers.grantRuntimePermissions()

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var setupVaultUseCase: SetupVaultUseCase

    @Inject
    lateinit var securityRepository: SecurityRepository

    @Inject
    lateinit var resetAppForTestingUseCase: ResetAppForTestingUseCase

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun biometricButton_shownWhenEnabled() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
            biometricEnabled = true,
        )
        composeRule.onNodeWithTag(TestTags.CALC_BIOMETRIC).assertIsDisplayed()
    }

    @Test
    fun biometricUnlock_opensRealVault() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
            biometricEnabled = true,
        )

        E2ETestHelpers.syncEmulatorFingerprint()
        composeRule.waitForIdle()
        val activity = E2ETestHelpers.getResumedActivity()
        assumeTrue("Activity not available", activity != null)
        assumeTrue(
            "No enrolled fingerprint on this emulator",
            E2ETestHelpers.isBiometricAuthAvailable(activity!!),
        )

        composeRule.onNodeWithTag(TestTags.CALC_BIOMETRIC).performClick()
        composeRule.waitForIdle()
        assumeTrue(
            "Virtual fingerprint sensor unavailable",
            E2ETestHelpers.simulateFingerprintTouch(),
        )
        composeRule.waitForVault(realVault = true)
        composeRule.onNodeWithTag(TestTags.VAULT_TITLE).assertIsDisplayed()
    }
}
