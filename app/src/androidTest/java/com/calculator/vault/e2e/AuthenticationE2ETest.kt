package com.calculator.vault.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.calculator.vault.MainActivity
import com.calculator.vault.data.testing.ResetAppForTestingUseCase
import com.calculator.vault.domain.repository.SecurityRepository
import com.calculator.vault.domain.usecase.SetupVaultUseCase
import com.calculator.vault.presentation.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthenticationE2ETest {

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
    fun firstLaunch_showsSetupScreen() {
        composeRule.waitForSetup()
        composeRule.onNodeWithTag(TestTags.SETUP_TITLE).assertIsDisplayed()
    }

    @Test
    fun validPin_opensRealVault() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
        )
        composeRule.enterPin("1234")
        composeRule.waitForVault(realVault = true)
        composeRule.onNodeWithTag(TestTags.VAULT_TITLE).assertIsDisplayed()
    }

    @Test
    fun invalidPin_staysOnCalculator() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
        )
        composeRule.enterPin("9999")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.CALC_DISPLAY).assertIsDisplayed()
    }

    @Test
    fun rapidPinEntry_doesNotCrash() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
        )
        repeat(5) {
            composeRule.enterPin("9999")
            composeRule.waitForIdle()
            composeRule.tapCalc("C")
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag(TestTags.CALC_DISPLAY).assertIsDisplayed()
    }

    @Test
    fun lockVault_returnsToCalculator() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
        )
        composeRule.enterPin("1234")
        composeRule.waitForVault()
        composeRule.onNodeWithTag(TestTags.VAULT_LOCK).performClick()
        composeRule.waitForCalculator()
    }
}
