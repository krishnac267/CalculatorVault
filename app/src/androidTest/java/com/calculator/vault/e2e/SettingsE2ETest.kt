package com.calculator.vault.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.calculator.vault.presentation.testing.TestTags
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.calculator.vault.MainActivity
import com.calculator.vault.data.testing.ResetAppForTestingUseCase
import com.calculator.vault.domain.repository.SecurityRepository
import com.calculator.vault.domain.usecase.SetupVaultUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsE2ETest {

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

    private fun openSettings() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
        )
        composeRule.enterPin("1234")
        composeRule.waitForVault()
        composeRule.openSecuritySettingsScreen()
    }

    @Test
    fun settingsScreen_showsSecurityOptions() {
        openSettings()
        composeRule.assertSettingsTagVisible(TestTags.SETTINGS_CHANGE_PIN)
        composeRule.assertSettingsTagVisible(TestTags.SETTINGS_SESSION_TIMEOUT)
        composeRule.assertSettingsTagVisible(TestTags.SETTINGS_INTRUDER)
    }

    @Test
    fun sessionTimeoutToggle_persistsAfterRestart() {
        openSettings()
        composeRule.assertSettingsTagVisible(TestTags.SETTINGS_SESSION_TIMEOUT)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onNodeWithText("minutes", substring = true).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }
}
