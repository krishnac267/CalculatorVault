package com.calculator.vault.e2e

import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class CalculatorE2ETest {

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

    private fun prepareCalculator() {
        E2ETestBase.resetAndOpenCalculator(
            composeRule,
            resetAppForTestingUseCase,
            setupVaultUseCase,
            securityRepository,
        )
    }

    @Test
    fun addition_2_plus_2_equals_4() {
        prepareCalculator()
        composeRule.tapCalc("2")
        composeRule.tapCalc("+")
        composeRule.tapCalc("2")
        composeRule.tapCalc("=")
        composeRule.assertDisplay("4")
    }

    @Test
    fun subtraction_9_minus_4_equals_5() {
        prepareCalculator()
        composeRule.tapCalc("9")
        composeRule.tapCalc("-")
        composeRule.tapCalc("4")
        composeRule.tapCalc("=")
        composeRule.assertDisplay("5")
    }

    @Test
    fun multiplication_6_times_7_equals_42() {
        prepareCalculator()
        composeRule.tapCalc("6")
        composeRule.tapCalc("×")
        composeRule.tapCalc("7")
        composeRule.tapCalc("=")
        composeRule.assertDisplay("42")
    }

    @Test
    fun division_100_div_5_equals_20() {
        prepareCalculator()
        composeRule.tapCalc("1")
        composeRule.tapCalc("0")
        composeRule.tapCalc("0")
        composeRule.tapCalc("÷")
        composeRule.tapCalc("5")
        composeRule.tapCalc("=")
        composeRule.assertDisplay("20")
    }

    @Test
    fun division_by_zero_shows_error() {
        prepareCalculator()
        composeRule.tapCalc("5")
        composeRule.tapCalc("÷")
        composeRule.tapCalc("0")
        composeRule.tapCalc("=")
        composeRule.assertDisplay("Error")
    }

    @Test
    fun clear_resets_display() {
        prepareCalculator()
        composeRule.tapCalc("9")
        composeRule.tapCalc("C")
        composeRule.assertDisplay("0")
    }

    @Test
    fun backspace_removes_last_digit() {
        prepareCalculator()
        composeRule.tapCalc("1")
        composeRule.tapCalc("2")
        composeRule.tapCalc("3")
        composeRule.tapCalc("⌫")
        composeRule.assertDisplay("12")
    }

    @Test
    fun decimal_addition() {
        prepareCalculator()
        composeRule.tapCalc("1")
        composeRule.tapCalc(".")
        composeRule.tapCalc("5")
        composeRule.tapCalc("+")
        composeRule.tapCalc("2")
        composeRule.tapCalc(".")
        composeRule.tapCalc("5")
        composeRule.tapCalc("=")
        composeRule.assertDisplay("4")
    }

    @Test
    fun large_multiplication() {
        prepareCalculator()
        repeat(6) { composeRule.tapCalc("9") }
        composeRule.tapCalc("×")
        repeat(6) { composeRule.tapCalc("9") }
        composeRule.tapCalc("=")
        composeRule.assertDisplay("999998000001")
    }

    @Test
    fun pin_entry_after_calculation_does_not_concatenate() {
        prepareCalculator()
        composeRule.tapCalc("2")
        composeRule.tapCalc("+")
        composeRule.tapCalc("2")
        composeRule.tapCalc("=")
        composeRule.assertDisplay("4")
        composeRule.enterPin("1234")
        composeRule.waitForVault(realVault = true)
    }

    @Test
    fun square_root_of_9_equals_3() {
        prepareCalculator()
        composeRule.tapCalc("9")
        composeRule.tapCalc("√")
        composeRule.assertDisplay("3")
    }

    @Test
    fun percent_of_50() {
        prepareCalculator()
        composeRule.tapCalc("5")
        composeRule.tapCalc("0")
        composeRule.tapCalc("%")
        composeRule.assertDisplay("0.5")
    }
}
