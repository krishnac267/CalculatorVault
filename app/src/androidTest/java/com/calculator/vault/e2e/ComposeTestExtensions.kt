package com.calculator.vault.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.calculator.vault.MainActivity
import com.calculator.vault.presentation.testing.TestTags

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.waitForCalculator() {
    waitUntil(timeoutMillis = 30_000) {
        try {
            onNodeWithTag(TestTags.CALC_DISPLAY).assertExists()
            true
        } catch (_: AssertionError) {
            false
        }
    }
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.waitForSetup() {
    waitUntil(timeoutMillis = 30_000) {
        try {
            onNodeWithTag(TestTags.SETUP_TITLE).assertExists()
            true
        } catch (_: AssertionError) {
            false
        }
    }
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.completeSetup(
    pin: String = "1234",
    fakePin: String? = null,
    enableBiometric: Boolean = false,
) {
    waitUntil(timeoutMillis = 15_000) {
        try {
            onNodeWithTag(TestTags.SETUP_TITLE).assertExists()
            true
        } catch (_: AssertionError) {
            false
        }
    }

    onNodeWithTag(TestTags.SETUP_PIN).performClick()
    onNodeWithTag(TestTags.SETUP_PIN).performTextInput(pin)
    onNodeWithTag(TestTags.SETUP_NEXT).performClick()

    onNodeWithTag(TestTags.SETUP_CONFIRM_PIN).performClick()
    onNodeWithTag(TestTags.SETUP_CONFIRM_PIN).performTextInput(pin)
    onNodeWithTag(TestTags.SETUP_NEXT).performClick()

    onNodeWithTag(TestTags.SETUP_QUESTION).performClick()
    onNodeWithTag(TestTags.SETUP_QUESTION).performTextInput("Pet name?")
    onNodeWithTag(TestTags.SETUP_ANSWER).performClick()
    onNodeWithTag(TestTags.SETUP_ANSWER).performTextInput("Fluffy")
    onNodeWithTag(TestTags.SETUP_NEXT).performClick()

    if (fakePin != null) {
        onNodeWithText("Enable decoy vault (fake PIN)", substring = true).performClick()
    }
    onNodeWithTag(TestTags.SETUP_NEXT).performClick()

    if (fakePin != null) {
        onNodeWithTag(TestTags.SETUP_FAKE_PIN).performClick()
        onNodeWithTag(TestTags.SETUP_FAKE_PIN).performTextInput(fakePin)
        onNodeWithTag(TestTags.SETUP_NEXT).performClick()
    }

    if (enableBiometric) {
        onNodeWithText("Enable biometric unlock", substring = true).performClick()
    }
    onNodeWithTag(TestTags.SETUP_FINISH).performClick()

    waitForCalculator()
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.tapCalc(label: String) {
    onNodeWithTag(TestTags.calcKey(label)).performClick()
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.assertDisplay(expected: String) {
    onNodeWithTag(TestTags.CALC_DISPLAY).assertIsDisplayed()
    waitUntil(timeoutMillis = 5_000) {
        try {
            onNodeWithTag(TestTags.CALC_DISPLAY).assertTextEquals(expected)
            true
        } catch (_: AssertionError) {
            false
        }
    }
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.enterPin(pin: String) {
    pin.forEach { digit -> tapCalc(digit.toString()) }
    tapCalc("=")
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.waitForVault(realVault: Boolean = true) {
    val expectedTitle = if (realVault) "Vault" else "My Apps"
    waitUntil(timeoutMillis = 15_000) {
        try {
            onNodeWithText(expectedTitle).assertIsDisplayed()
            true
        } catch (_: AssertionError) {
            false
        }
    }
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.openVaultSettingsTab() {
    waitUntil(timeoutMillis = 15_000) {
        try {
            onNodeWithTag(TestTags.VAULT_TAB_SETTINGS).assertExists()
            true
        } catch (_: AssertionError) {
            false
        }
    }
    onNodeWithTag(TestTags.VAULT_TAB_SETTINGS).performClick()
    waitForIdle()
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.openSecuritySettingsScreen() {
    openVaultSettingsTab()
    onNodeWithTag(TestTags.VAULT_SECURITY_SETTINGS).performClick()
    waitUntil(timeoutMillis = 15_000) {
        try {
            onNodeWithTag(TestTags.SETTINGS_CHANGE_PIN).assertExists()
            true
        } catch (_: AssertionError) {
            false
        }
    }
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.assertSettingsTagVisible(
    tag: String,
) {
    onNodeWithTag(tag).performScrollTo()
    onNodeWithTag(tag).assertIsDisplayed()
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.waitForVaultTabEmpty() {
    waitUntil(timeoutMillis = 15_000) {
        try {
            onNodeWithTag(TestTags.VAULT_EMPTY).assertExists()
            true
        } catch (_: AssertionError) {
            false
        }
    }
}
