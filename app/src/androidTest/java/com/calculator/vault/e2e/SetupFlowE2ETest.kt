package com.calculator.vault.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.calculator.vault.MainActivity
import com.calculator.vault.data.testing.ResetAppForTestingUseCase
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
class SetupFlowE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule = E2ETestHelpers.grantRuntimePermissions()

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var resetAppForTestingUseCase: ResetAppForTestingUseCase

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun completeSetupViaUi_reachesCalculator() {
        composeRule.waitForSetup()
        composeRule.onNodeWithTag(TestTags.SETUP_PIN).assertExists()
        composeRule.onNodeWithTag(TestTags.SETUP_PIN).performClick()
        composeRule.onNodeWithTag(TestTags.SETUP_PIN).performTextInput("1234")
        composeRule.onNodeWithTag(TestTags.SETUP_NEXT).performClick()

        composeRule.onNodeWithTag(TestTags.SETUP_CONFIRM_PIN).performClick()
        composeRule.onNodeWithTag(TestTags.SETUP_CONFIRM_PIN).performTextInput("1234")
        composeRule.onNodeWithTag(TestTags.SETUP_NEXT).performClick()

        composeRule.onNodeWithTag(TestTags.SETUP_QUESTION).performClick()
        composeRule.onNodeWithTag(TestTags.SETUP_QUESTION).performTextInput("Pet?")
        composeRule.onNodeWithTag(TestTags.SETUP_ANSWER).performClick()
        composeRule.onNodeWithTag(TestTags.SETUP_ANSWER).performTextInput("Dog")
        composeRule.onNodeWithTag(TestTags.SETUP_NEXT).performClick()

        composeRule.onNodeWithTag(TestTags.SETUP_NEXT).performClick()
        composeRule.onNodeWithTag(TestTags.SETUP_FINISH).performClick()

        composeRule.waitForCalculator()
        composeRule.onNodeWithTag(TestTags.CALC_DISPLAY).assertIsDisplayed()
    }
}
