package com.calculator.vault.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.calculator.vault.presentation.testing.TestTags
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class HiddenAppsE2ETest {

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
    fun addAppsScreen_loadsInstalledApps() {
        openVault()
        composeRule.onNodeWithTag(TestTags.VAULT_TAB_SETTINGS).performClick()
        composeRule.onNodeWithTag(TestTags.VAULT_ADD_APPS).performClick()
        composeRule.onNodeWithText("Add Apps").assertIsDisplayed()
        composeRule.onNodeWithText("Search installed apps").assertIsDisplayed()
    }

    @Test
    fun addAppsSearch_filtersResults() {
        openVault()
        composeRule.onNodeWithTag(TestTags.VAULT_TAB_SETTINGS).performClick()
        composeRule.onNodeWithTag(TestTags.VAULT_ADD_APPS).performClick()
        composeRule.onNodeWithText("Search installed apps").performClick()
        composeRule.onNodeWithText("Search installed apps").performTextInput("zzz_no_match_xyz")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Add Apps").assertIsDisplayed()
    }

    @Test
    fun hiddenTab_showsEmptyState() {
        openVault()
        composeRule.waitForVaultTabEmpty()
    }
}
