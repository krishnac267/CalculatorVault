package com.calculator.vault.e2e

import android.content.Intent
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.calculator.vault.MainActivity
import com.calculator.vault.data.testing.ResetAppForTestingUseCase
import com.calculator.vault.domain.repository.SecurityRepository
import com.calculator.vault.domain.usecase.SetupVaultUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object E2ETestBase {
    /**
     * Restarts MainActivity without restoring navigation state. `recreate()` keeps the
     * back stack on the setup screen even after vault data is seeded programmatically.
     */
    private fun relaunchMainActivity(
        composeRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
    ) {
        composeRule.activityRule.scenario.onActivity { activity ->
            val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)!!
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
            activity.finish()
        }
        composeRule.waitForIdle()
    }

    fun resetApp(
        composeRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
        resetAppForTestingUseCase: ResetAppForTestingUseCase,
    ) {
        runBlocking(Dispatchers.Default) {
            resetAppForTestingUseCase()
        }
        relaunchMainActivity(composeRule)
        composeRule.waitForSetup()
    }

    suspend fun seedVault(
        setupVaultUseCase: SetupVaultUseCase,
        securityRepository: SecurityRepository,
        pin: String = "1234",
        fakePin: String? = null,
        biometricEnabled: Boolean = false,
    ) {
        setupVaultUseCase(
            pin = pin,
            fakePin = fakePin,
            securityQuestion = "Security question?",
            securityAnswer = "Answer",
            biometricEnabled = biometricEnabled,
        )
        securityRepository.updateSettings(
            securityRepository.getSettings().copy(intruderCaptureEnabled = false),
        )
    }

    /** Clears state, seeds vault data, relaunches, and waits for the calculator. */
    fun resetAndOpenCalculator(
        composeRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
        resetAppForTestingUseCase: ResetAppForTestingUseCase,
        setupVaultUseCase: SetupVaultUseCase,
        securityRepository: SecurityRepository,
        pin: String = "1234",
        fakePin: String? = null,
        biometricEnabled: Boolean = false,
    ) {
        runBlocking(Dispatchers.Default) {
            resetAppForTestingUseCase()
            seedVault(setupVaultUseCase, securityRepository, pin, fakePin, biometricEnabled)
        }
        relaunchMainActivity(composeRule)
        composeRule.waitForCalculator()
    }
}
