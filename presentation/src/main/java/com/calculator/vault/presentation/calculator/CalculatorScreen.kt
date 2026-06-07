package com.calculator.vault.presentation.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.presentation.theme.CalcAccent
import com.calculator.vault.presentation.theme.CalcBackground
import com.calculator.vault.presentation.theme.CalcButtonFunction
import com.calculator.vault.presentation.theme.CalcButtonMemory
import com.calculator.vault.presentation.theme.CalcButtonNumber
import com.calculator.vault.presentation.theme.CalcButtonOperator
import com.calculator.vault.presentation.theme.CalcButtonText
import com.calculator.vault.presentation.theme.CalcDisplayBackground
import com.calculator.vault.presentation.theme.CalcDisplayText
import com.calculator.vault.presentation.theme.PinSuccessGlow
import com.calculator.vault.presentation.testing.TestTags

@Composable
fun CalculatorScreen(
    onNavigateToSetup: () -> Unit,
    onNavigateToVault: (isFakeVault: Boolean) -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? FragmentActivity

    LaunchedEffect(uiState.isCheckingSetup, uiState.isSetupComplete) {
        if (!uiState.isCheckingSetup && !uiState.isSetupComplete) {
            onNavigateToSetup()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CalculatorEvent.NavigateToSetup -> onNavigateToSetup()
                CalculatorEvent.NavigateToRealVault -> onNavigateToVault(false)
                CalculatorEvent.NavigateToFakeVault -> onNavigateToVault(true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalcBackground)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CalcDisplay(
                display = uiState.display,
                showBiometric = uiState.biometricEnabled && uiState.isSetupComplete,
                onBiometricClick = {
                    activity?.let { viewModel.requestBiometricUnlock(it) }
                },
            )

            CalcButtonRow(
                buttons = listOf(
                    CalcButtonSpec("MC", CalcButtonMemory) { viewModel.onInput(CalculatorAction.MemoryClear) },
                    CalcButtonSpec("MR", CalcButtonMemory) { viewModel.onInput(CalculatorAction.MemoryRecall) },
                    CalcButtonSpec("M+", CalcButtonMemory) { viewModel.onInput(CalculatorAction.MemoryAdd) },
                    CalcButtonSpec("M-", CalcButtonMemory) { viewModel.onInput(CalculatorAction.MemorySubtract) },
                    CalcButtonSpec("MS", CalcButtonMemory) { viewModel.onInput(CalculatorAction.MemoryStore) },
                ),
            )

            CalcButtonRow(
                buttons = listOf(
                    CalcButtonSpec("C", CalcButtonFunction) { viewModel.onInput(CalculatorAction.Clear) },
                    CalcButtonSpec("CE", CalcButtonFunction) { viewModel.onInput(CalculatorAction.ClearEntry) },
                    CalcButtonSpec("⌫", CalcButtonFunction) { viewModel.onInput(CalculatorAction.Backspace) },
                    CalcButtonSpec("√", CalcButtonFunction) { viewModel.onInput(CalculatorAction.SquareRoot) },
                    CalcButtonSpec("^", CalcButtonFunction) { viewModel.onInput(CalculatorAction.Power) },
                ),
            )

            CalcButtonRow(
                buttons = listOf(
                    CalcButtonSpec("7") { viewModel.onInput(CalculatorAction.Digit("7")) },
                    CalcButtonSpec("8") { viewModel.onInput(CalculatorAction.Digit("8")) },
                    CalcButtonSpec("9") { viewModel.onInput(CalculatorAction.Digit("9")) },
                    CalcButtonSpec("÷", CalcButtonOperator) { viewModel.onInput(CalculatorAction.Operator("÷")) },
                    CalcButtonSpec("%", CalcButtonOperator) { viewModel.onInput(CalculatorAction.Percent) },
                ),
            )

            CalcButtonRow(
                buttons = listOf(
                    CalcButtonSpec("4") { viewModel.onInput(CalculatorAction.Digit("4")) },
                    CalcButtonSpec("5") { viewModel.onInput(CalculatorAction.Digit("5")) },
                    CalcButtonSpec("6") { viewModel.onInput(CalculatorAction.Digit("6")) },
                    CalcButtonSpec("×", CalcButtonOperator) { viewModel.onInput(CalculatorAction.Operator("×")) },
                    CalcButtonSpec("±", CalcButtonFunction) {
                        viewModel.onInput(CalculatorAction.Operator("-"))
                    },
                ),
            )

            CalcButtonRow(
                buttons = listOf(
                    CalcButtonSpec("1") { viewModel.onInput(CalculatorAction.Digit("1")) },
                    CalcButtonSpec("2") { viewModel.onInput(CalculatorAction.Digit("2")) },
                    CalcButtonSpec("3") { viewModel.onInput(CalculatorAction.Digit("3")) },
                    CalcButtonSpec("-", CalcButtonOperator) { viewModel.onInput(CalculatorAction.Operator("-")) },
                    CalcButtonSpec("=", CalcAccent) { viewModel.onInput(CalculatorAction.Equals) },
                ),
            )

            CalcButtonRow(
                buttons = listOf(
                    CalcButtonSpec("0", spanWeight = 2f) { viewModel.onInput(CalculatorAction.Digit("0")) },
                    CalcButtonSpec(".") { viewModel.onInput(CalculatorAction.Decimal) },
                    CalcButtonSpec("+", CalcButtonOperator) { viewModel.onInput(CalculatorAction.Operator("+")) },
                ),
            )
        }

        AnimatedVisibility(
            visible = uiState.showPinSuccessAnimation,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.6f, animationSpec = tween(300)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PinSuccessGlow,
                    modifier = Modifier.height(72.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Access granted",
                    color = CalcDisplayText,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun CalcDisplay(
    display: String,
    showBiometric: Boolean = false,
    onBiometricClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CalcDisplayBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (showBiometric) {
            IconButton(
                onClick = onBiometricClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag(TestTags.CALC_BIOMETRIC),
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric unlock",
                    tint = PinSuccessGlow,
                )
            }
        }
        Text(
            text = display,
            color = CalcDisplayText,
            fontSize = 42.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.CALC_DISPLAY),
        )
    }
}

private data class CalcButtonSpec(
    val label: String,
    val backgroundColor: androidx.compose.ui.graphics.Color = CalcButtonNumber,
    val spanWeight: Float = 1f,
    val onClick: () -> Unit,
)

@Composable
private fun CalcButtonRow(buttons: List<CalcButtonSpec>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        buttons.forEach { spec ->
            CalcButton(
                label = spec.label,
                backgroundColor = spec.backgroundColor,
                modifier = Modifier.weight(spec.spanWeight),
                onClick = spec.onClick,
            )
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .testTag(TestTags.calcKey(label)),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = CalcButtonText,
        ),
    ) {
        Text(text = label, fontSize = 20.sp)
    }
}
