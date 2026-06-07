package com.calculator.vault.presentation.calculator

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Full-featured calculator engine supporting basic and advanced operations.
 * Uses expression evaluation with operator precedence.
 */
class CalculatorEngine {

    private var expression = ""
    private var memory = 0.0
    private var lastResult = 0.0
    private var pinCandidate = ""
    private var justEvaluated = false

    fun getDisplay(): String = when {
        expression == "Error" -> "Error"
        expression.isNotEmpty() -> expression
        else -> "0"
    }

    fun getPinCandidate(): String = pinCandidate

    fun inputDigit(digit: String) {
        if (expression == "Error") clear()
        if (justEvaluated) {
            clear()
            justEvaluated = false
        }
        if (expression.length >= MAX_EXPRESSION_LENGTH) return
        pinCandidate += digit
        expression += digit
    }

    fun inputDecimal() {
        if (expression == "Error") clear()
        if (justEvaluated) {
            clear()
            justEvaluated = false
        }
        pinCandidate = ""
        val parts = expression.split(Regex("[+\\-×÷%^]"))
        val current = parts.lastOrNull() ?: ""
        if (!current.contains('.') && expression.length < MAX_EXPRESSION_LENGTH) {
            expression = if (expression.isEmpty()) "0." else expression + "."
        }
    }

    fun inputOperator(op: String) {
        justEvaluated = false
        pinCandidate = ""
        if (expression == "Error") {
            if (op == "-") expression = "-"
            return
        }
        if (expression.isEmpty()) {
            if (op == "-") expression = "-"
            return
        }
        val last = expression.last()
        if (last in OPERATORS) {
            expression = expression.dropLast(1) + op
        } else {
            expression += op
        }
    }

    fun inputPercent() {
        pinCandidate = ""
        if (expression.isEmpty()) return
        try {
            val value = evaluateExpression(expression) / 100.0
            expression = formatResult(value)
        } catch (_: Exception) {
            expression = "Error"
        }
    }

    fun inputSquareRoot() {
        pinCandidate = ""
        try {
            val value = if (expression.isEmpty()) lastResult else evaluateExpression(expression)
            expression = formatResult(sqrt(value))
        } catch (_: Exception) {
            expression = "Error"
        }
    }

    fun inputPower() {
        pinCandidate = ""
        if (expression.isNotEmpty() && expression.last() !in OPERATORS) {
            expression += "^"
        }
    }

    fun clear() {
        expression = ""
        pinCandidate = ""
        justEvaluated = false
    }

    fun clearEntry() {
        if (expression == "Error") {
            clear()
            return
        }
        val lastOpIndex = expression.indexOfLast { it in OPERATORS }
        if (lastOpIndex >= 0) {
            expression = expression.substring(0, lastOpIndex + 1)
        } else {
            expression = ""
        }
        syncPinCandidateFromExpression()
    }

    fun backspace() {
        if (expression == "Error") {
            clear()
            return
        }
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            syncPinCandidateFromExpression()
        }
    }

    fun memoryClear() { memory = 0.0 }

    fun memoryRecall() {
        expression = formatResult(memory)
        syncPinCandidateFromExpression()
    }

    fun memoryAdd() {
        safeEvaluate()?.let { memory += it }
    }

    fun memorySubtract() {
        safeEvaluate()?.let { memory -= it }
    }

    fun memoryStore() {
        safeEvaluate()?.let { memory = it }
    }

    /** Returns calculated result, or null if input looks like a PIN attempt. */
    fun evaluate(): String? {
        if (isPinAttempt()) return null
        return try {
            validateExpression(expression.ifEmpty { "0" })
            val result = evaluateExpression(expression.ifEmpty { "0" })
            lastResult = result
            val formatted = formatResult(result)
            expression = formatted
            pinCandidate = ""
            justEvaluated = true
            formatted
        } catch (_: Exception) {
            expression = "Error"
            pinCandidate = ""
            "Error"
        }
    }

    fun isPinAttempt(): Boolean = expression.matches(PIN_REGEX)

    fun getPinForValidation(): String = expression

    fun saveState(): CalculatorState = CalculatorState(
        expression = expression,
        memory = memory,
        lastResult = lastResult,
    )

    fun restoreState(state: CalculatorState) {
        expression = state.expression
        memory = state.memory
        lastResult = state.lastResult
        syncPinCandidateFromExpression()
    }

    data class CalculatorState(
        val expression: String = "",
        val memory: Double = 0.0,
        val lastResult: Double = 0.0,
    )

    private fun syncPinCandidateFromExpression() {
        val trailing = expression.takeLastWhile { it.isDigit() }
        pinCandidate = if (trailing.isNotEmpty() && !hasOperators(expression.dropLast(trailing.length))) {
            trailing
        } else {
            ""
        }
    }

    private fun hasOperators(value: String): Boolean =
        value.any { it in OPERATORS || it == '%' || it == '^' || it == '.' }

    private fun safeEvaluate(): Double? = try {
        validateExpression(expression.ifEmpty { "0" })
        evaluateExpression(expression.ifEmpty { "0" })
    } catch (_: Exception) {
        null
    }

    private fun validateExpression(expr: String) {
        if (expr.isEmpty()) return
        if (expr.last() in OPERATORS) throw IllegalStateException("Trailing operator")
    }

    private fun evaluateExpression(expr: String): Double {
        var normalized = expr.replace("×", "*").replace("÷", "/")
        normalized = normalized.replace("%", "/100")
        return evaluateWithPrecedence(normalized)
    }

    private fun evaluateWithPrecedence(expr: String): Double {
        val tokens = tokenize(expr)
        if (tokens.isEmpty()) return 0.0

        val values = ArrayDeque<Double>()
        val ops = ArrayDeque<String>()

        fun precedence(op: String) = when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            "^" -> 3
            else -> 0
        }

        fun shouldPopStackTop(currentOp: String): Boolean {
            if (ops.isEmpty()) return false
            val top = ops.last()
            return when {
                currentOp == "^" -> precedence(top) > precedence(currentOp)
                else -> precedence(top) >= precedence(currentOp)
            }
        }

        fun applyOp() {
            if (values.size < 2 || ops.isEmpty()) return
            val b = values.removeLast()
            val a = values.removeLast()
            val op = ops.removeLast()
            values.addLast(
                when (op) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> if (b == 0.0) throw ArithmeticException() else a / b
                    "^" -> a.pow(b)
                    else -> throw IllegalArgumentException()
                },
            )
        }

        var i = 0
        while (i < tokens.size) {
            when (val token = tokens[i]) {
                is Token.Number -> values.addLast(token.value)
                is Token.Operator -> {
                    while (shouldPopStackTop(token.symbol)) {
                        applyOp()
                    }
                    ops.addLast(token.symbol)
                }
            }
            i++
        }
        while (ops.isNotEmpty()) applyOp()
        if (values.size != 1) throw IllegalStateException("Invalid expression")
        return values.last()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < expr.length) {
            when {
                expr[i].isDigit() || expr[i] == '.' -> {
                    val start = i
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                    tokens.add(Token.Number(expr.substring(start, i).toDouble()))
                }
                expr[i] in charArrayOf('+', '-', '*', '/', '^') -> {
                    if (expr[i] == '-' && (tokens.isEmpty() || tokens.last() is Token.Operator)) {
                        val start = i
                        i++
                        while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                        tokens.add(Token.Number(expr.substring(start, i).toDouble()))
                    } else {
                        tokens.add(Token.Operator(expr[i].toString()))
                        i++
                    }
                }
                else -> i++
            }
        }
        return tokens
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.8f", value).trimEnd('0').trimEnd('.')
        }
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val symbol: String) : Token()
    }

    companion object {
        private val OPERATORS = setOf('+', '-', '×', '÷', '^')
        private val PIN_REGEX = Regex("^\\d{4,8}$")
        private const val MAX_EXPRESSION_LENGTH = 24
    }
}
