package com.calculator.vault.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class FakeVaultE2ETest {

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
    fun realPin_opensRealVault() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
            fakePin = "5678",
        )
        composeRule.enterPin("1234")
        composeRule.waitForVault(realVault = true)
        composeRule.onNodeWithText("Vault").assertIsDisplayed()
        composeRule.onNodeWithText("Favorites").assertIsDisplayed()
    }

    @Test
    fun fakePin_opensDecoyVault() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
            fakePin = "5678",
        )
        composeRule.enterPin("5678")
        composeRule.waitForVault(realVault = false)
        composeRule.onNodeWithText("My Apps").assertIsDisplayed()
        composeRule.onNodeWithText("Favorites").assertDoesNotExist()
    }

    @Test
    fun wrongPin_doesNotOpenVault() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
            fakePin = "5678",
        )
        composeRule.enterPin("0000")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Vault").assertDoesNotExist()
        composeRule.onNodeWithText("My Apps").assertDoesNotExist()
    }

    @Test
    fun fakeVault_settingsTab_blocksRealSettings() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
            fakePin = "5678",
        )
        composeRule.enterPin("5678")
        composeRule.waitForVault(realVault = false)
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Add apps to vault").assertIsDisplayed()
        composeRule.onNodeWithText("Security settings").assertIsDisplayed()
    }
}
