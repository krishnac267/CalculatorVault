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
class VaultDashboardE2ETest {

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

    private fun openVault() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
        )
        composeRule.enterPin("1234")
        composeRule.waitForVault()
    }

    @Test
    fun allTabs_areVisible() {
        openVault()
        composeRule.onNodeWithTag(TestTags.VAULT_TAB_HIDDEN).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.VAULT_TAB_FAVORITES).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.VAULT_TAB_RECENT).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.VAULT_TAB_SETTINGS).assertIsDisplayed()
    }

    @Test
    fun tabNavigation_works() {
        openVault()
        composeRule.onNodeWithTag(TestTags.VAULT_TAB_FAVORITES).performClick()
        composeRule.waitForVaultTabEmpty()
        composeRule.onNodeWithTag(TestTags.VAULT_TAB_RECENT).performClick()
        composeRule.waitForVaultTabEmpty()
        composeRule.onNodeWithTag(TestTags.VAULT_TAB_SETTINGS).performClick()
        composeRule.onNodeWithTag(TestTags.VAULT_ADD_APPS).assertIsDisplayed()
    }

    @Test
    fun settingsNavigation_opensSecuritySettings() {
        openVault()
        composeRule.openSecuritySettingsScreen()
        composeRule.assertSettingsTagVisible(TestTags.SETTINGS_CHANGE_PIN)
        composeRule.assertSettingsTagVisible(TestTags.SETTINGS_SESSION_TIMEOUT)
    }

    @Test
    fun addAppsScreen_opens() {
        openVault()
        composeRule.openVaultSettingsTab()
        composeRule.onNodeWithTag(TestTags.VAULT_ADD_APPS).performClick()
        composeRule.onNodeWithText("Add Apps").assertIsDisplayed()
    }
}
