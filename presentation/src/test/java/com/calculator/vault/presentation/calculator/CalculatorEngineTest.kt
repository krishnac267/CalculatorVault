package com.calculator.vault.presentation.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine

    @Before
    fun setup() {
        engine = CalculatorEngine()
    }

    @Test
    fun addition() {
        engine.inputDigit("2")
        engine.inputOperator("+")
        engine.inputDigit("2")
        assertEquals("4", engine.evaluate())
    }

    @Test
    fun multiplication() {
        listOf("5", "×", "8").forEach {
            if (it.length == 1 && it[0].isDigit()) engine.inputDigit(it) else engine.inputOperator(it)
        }
        assertEquals("40", engine.evaluate())
    }

    @Test
    fun division() {
        "100÷5".forEach { c ->
            when (c) {
                in '0'..'9' -> engine.inputDigit(c.toString())
                '÷' -> engine.inputOperator("÷")
            }
        }
        assertEquals("20", engine.evaluate())
    }

    @Test
    fun divisionByZero() {
        "0÷5".forEach { c ->
            when (c) {
                in '0'..'9' -> engine.inputDigit(c.toString())
                '÷' -> engine.inputOperator("÷")
            }
        }
        assertEquals("0", engine.evaluate())
    }

    @Test
    fun divisionByZeroError() {
        "5÷0".forEach { c ->
            when (c) {
                in '0'..'9' -> engine.inputDigit(c.toString())
                '÷' -> engine.inputOperator("÷")
            }
        }
        assertEquals("Error", engine.evaluate())
    }

    @Test
    fun largeMultiplication() {
        "999999×999999".forEach { c ->
            when (c) {
                in '0'..'9' -> engine.inputDigit(c.toString())
                '×' -> engine.inputOperator("×")
            }
        }
        assertEquals("999998000001", engine.evaluate())
    }

    @Test
    fun pinAttemptDetected() {
        "1234".forEach { engine.inputDigit(it.toString()) }
        assertEquals(true, engine.isPinAttempt())
        assertNull(engine.evaluate())
    }

    @Test
    fun pinNotDetectedWithOperator() {
        "1+234".forEach { c ->
            when (c) {
                in '0'..'9' -> engine.inputDigit(c.toString())
                '+' -> engine.inputOperator("+")
            }
        }
        assertEquals(false, engine.isPinAttempt())
    }

    @Test
    fun backspaceClearsError() {
        "5÷0".forEach { c ->
            when (c) {
                in '0'..'9' -> engine.inputDigit(c.toString())
                '÷' -> engine.inputOperator("÷")
            }
        }
        engine.evaluate()
        engine.backspace()
        assertEquals("0", engine.getDisplay())
    }

    @Test
    fun subtraction() {
        "9-4".forEach { c ->
            when (c) {
                in '0'..'9' -> engine.inputDigit(c.toString())
                '-' -> engine.inputOperator("-")
            }
        }
        assertEquals("5", engine.evaluate())
    }

    @Test
    fun decimalAddition() {
        engine.inputDigit("1")
        engine.inputDecimal()
        engine.inputDigit("5")
        engine.inputOperator("+")
        engine.inputDigit("2")
        engine.inputDecimal()
        engine.inputDigit("5")
        assertEquals("4", engine.evaluate())
    }

    @Test
    fun memoryStoreAndRecall() {
        engine.inputDigit("7")
        engine.memoryStore()
        engine.clear()
        engine.memoryRecall()
        assertEquals("7", engine.getDisplay())
    }

    @Test
    fun stateRestoredAfterSave() {
        engine.inputDigit("2")
        engine.inputOperator("+")
        engine.inputDigit("3")
        val state = engine.saveState()
        val engine2 = CalculatorEngine()
        engine2.restoreState(state)
        assertEquals("2+3", engine2.getDisplay())
        assertEquals("5", engine2.evaluate())
    }

    @Test
    fun emptyPinNotDetected() {
        assertEquals(false, engine.isPinAttempt())
    }

    @Test
    fun longPinNotDetected() {
        "123456789".forEach { engine.inputDigit(it.toString()) }
        assertEquals(false, engine.isPinAttempt())
    }

    @Test
    fun trailingOperatorIsError() {
        engine.inputDigit("5")
        engine.inputOperator("×")
        assertEquals("Error", engine.evaluate())
    }

    @Test
    fun pinAfterCalculation_startsFreshExpression() {
        engine.inputDigit("2")
        engine.inputOperator("+")
        engine.inputDigit("2")
        assertEquals("4", engine.evaluate())
        "1234".forEach { engine.inputDigit(it.toString()) }
        assertEquals(true, engine.isPinAttempt())
        assertEquals("1234", engine.getDisplay())
    }

    @Test
    fun clearEntry_keepsOperator() {
        engine.inputDigit("1")
        engine.inputDigit("2")
        engine.inputOperator("+")
        engine.inputDigit("3")
        engine.clearEntry()
        assertEquals("12+", engine.getDisplay())
    }

    @Test
    fun percent_halvesValue() {
        engine.inputDigit("5")
        engine.inputDigit("0")
        engine.inputPercent()
        assertEquals("0.5", engine.getDisplay())
    }

    @Test
    fun squareRoot() {
        engine.inputDigit("9")
        engine.inputSquareRoot()
        assertEquals("3", engine.getDisplay())
    }
}
