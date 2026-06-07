package com.calculator.vault.e2e

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.calculator.vault.VaultLauncherActivity
import com.calculator.vault.data.testing.ResetAppForTestingUseCase
import com.calculator.vault.domain.model.VaultApp
import com.calculator.vault.domain.repository.SecurityRepository
import com.calculator.vault.domain.usecase.AddAppToVaultUseCase
import com.calculator.vault.domain.usecase.GetInstalledAppsUseCase
import com.calculator.vault.domain.usecase.SetupVaultUseCase
import com.calculator.vault.presentation.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LauncherModeE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule = E2ETestHelpers.grantRuntimePermissions()

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<VaultLauncherActivity>()

    @Inject
    lateinit var setupVaultUseCase: SetupVaultUseCase

    @Inject
    lateinit var securityRepository: SecurityRepository

    @Inject
    lateinit var resetAppForTestingUseCase: ResetAppForTestingUseCase

    @Inject
    lateinit var getInstalledAppsUseCase: GetInstalledAppsUseCase

    @Inject
    lateinit var addAppToVaultUseCase: AddAppToVaultUseCase

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private data class LauncherSeed(
        val hiddenPackage: String,
        val visiblePackage: String,
    )

    private fun seedLauncherWithHiddenApp(): LauncherSeed = runBlocking(Dispatchers.Default) {
        resetAppForTestingUseCase()
        E2ETestBase.seedVault(setupVaultUseCase, securityRepository)
        securityRepository.updateSettings(
            securityRepository.getSettings().copy(launcherModeEnabled = true),
        )
        val ownPackage = composeRule.activity.packageName
        val candidates = getInstalledAppsUseCase().filter { it.packageName != ownPackage }
        require(candidates.size >= 2) { "Need at least two launchable apps on device" }
        val hidden = candidates.first()
        val visible = candidates[1]
        addAppToVaultUseCase(
            VaultApp(packageName = hidden.packageName, appName = hidden.appName),
            freeLimit = 100,
        )
        LauncherSeed(hidden.packageName, visible.packageName)
    }

    private fun refreshLauncherAfterSeed() {
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }
        composeRule.waitForIdle()
        composeRule.waitForLauncherGrid()
    }

    @Test
    fun hiddenVaultApp_doesNotAppearInLauncherGrid() {
        val seed = seedLauncherWithHiddenApp()
        refreshLauncherAfterSeed()

        composeRule.onNodeWithTag(TestTags.launcherApp(seed.hiddenPackage)).assertDoesNotExist()
        composeRule.onNodeWithTag(TestTags.launcherApp(seed.visiblePackage)).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.LAUNCHER_HIDDEN_BANNER).assertIsDisplayed()
        composeRule.onNodeWithText("1 app hidden in Calculator Vault").assertIsDisplayed()
    }
}

private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<
    androidx.test.ext.junit.rules.ActivityScenarioRule<VaultLauncherActivity>,
    VaultLauncherActivity,
    >.waitForLauncherGrid() {
    waitUntil(timeoutMillis = 30_000) {
        try {
            onNodeWithTag(TestTags.LAUNCHER_APP_GRID).assertExists()
            true
        } catch (_: AssertionError) {
            false
        }
    }
}
