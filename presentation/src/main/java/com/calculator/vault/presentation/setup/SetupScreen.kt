package com.calculator.vault.presentation.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.presentation.components.GlassCard
import com.calculator.vault.presentation.components.SecureScreenEffect
import com.calculator.vault.presentation.theme.VaultTextSecondary
import com.calculator.vault.presentation.testing.TestTags

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SecureScreenEffect()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SetupEvent.SetupComplete -> onSetupComplete()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Vault Setup",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag(TestTags.SETUP_TITLE),
        )
        Text(
            text = stepTitle(uiState.currentStep),
            style = MaterialTheme.typography.titleMedium,
            color = VaultTextSecondary,
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            when (uiState.currentStep) {
                SetupStep.EnterPin -> PinField(
                    label = "Create PIN (4–8 digits)",
                    value = uiState.pin,
                    onValueChange = viewModel::onPinChange,
                    testTag = TestTags.SETUP_PIN,
                )
                SetupStep.ConfirmPin -> PinField(
                    label = "Confirm PIN",
                    value = uiState.confirmPin,
                    onValueChange = viewModel::onConfirmPinChange,
                    testTag = TestTags.SETUP_CONFIRM_PIN,
                )
                SetupStep.SecurityQuestion -> {
                    OutlinedTextField(
                        value = uiState.securityQuestion,
                        onValueChange = viewModel::onSecurityQuestionChange,
                        label = { Text("Security question") },
                        modifier = Modifier.fillMaxWidth().testTag(TestTags.SETUP_QUESTION),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.securityAnswer,
                        onValueChange = viewModel::onSecurityAnswerChange,
                        label = { Text("Answer") },
                        modifier = Modifier.fillMaxWidth().testTag(TestTags.SETUP_ANSWER),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ToggleRow(
                        label = "Enable decoy vault (fake PIN)",
                        checked = uiState.useFakePin,
                        onCheckedChange = viewModel::onUseFakePinChange,
                    )
                }
                SetupStep.FakePin -> {
                    ToggleRow(
                        label = "Enable decoy vault (fake PIN)",
                        checked = uiState.useFakePin,
                        onCheckedChange = viewModel::onUseFakePinChange,
                    )
                    if (uiState.useFakePin) {
                        Spacer(modifier = Modifier.height(12.dp))
                        PinField(
                            label = "Fake PIN (4–8 digits)",
                            value = uiState.fakePin,
                            onValueChange = viewModel::onFakePinChange,
                            testTag = TestTags.SETUP_FAKE_PIN,
                        )
                    }
                }
                SetupStep.Biometric -> {
                    Text(
                        text = "Unlock faster with fingerprint or face when available.",
                        color = VaultTextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ToggleRow(
                        label = "Enable biometric unlock",
                        checked = uiState.biometricEnabled,
                        onCheckedChange = viewModel::onBiometricEnabledChange,
                    )
                }
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.currentStep != SetupStep.EnterPin) {
                OutlinedButton(
                    onClick = viewModel::onBackStep,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading,
                ) {
                    Text("Back")
                }
            }
            Button(
                onClick = viewModel::onNextStep,
                modifier = Modifier
                    .weight(1f)
                    .testTag(
                        if (uiState.currentStep == SetupStep.Biometric) {
                            TestTags.SETUP_FINISH
                        } else {
                            TestTags.SETUP_NEXT
                        },
                    ),
                enabled = !uiState.isLoading,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        if (uiState.currentStep == SetupStep.Biometric) "Finish" else "Next",
                    )
                }
            }
        }
    }
}

@Composable
private fun PinField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth().testTag(testTag),
        singleLine = true,
    )
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun stepTitle(step: SetupStep): String = when (step) {
    SetupStep.EnterPin -> "Step 1: Create your vault PIN"
    SetupStep.ConfirmPin -> "Step 2: Confirm PIN"
    SetupStep.SecurityQuestion -> "Step 3: Recovery question"
    SetupStep.FakePin -> "Step 4: Optional decoy vault"
    SetupStep.Biometric -> "Step 5: Biometric unlock"
}
