package com.calculator.vault.presentation.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SecureScreenEffect()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        modifier = Modifier.testTag(TestTags.SETTINGS_TITLE),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Change PIN",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(TestTags.SETTINGS_CHANGE_PIN),
                )
                Spacer(modifier = Modifier.height(12.dp))
                PinField("Current PIN", uiState.oldPin, viewModel::onOldPinChange)
                Spacer(modifier = Modifier.height(8.dp))
                PinField("New PIN", uiState.newPin, viewModel::onNewPinChange)
                Spacer(modifier = Modifier.height(8.dp))
                PinField("Confirm new PIN", uiState.confirmNewPin, viewModel::onConfirmNewPinChange)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = viewModel::changePin,
                    enabled = !uiState.isChangingPin,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isChangingPin) {
                        CircularProgressIndicator()
                    } else {
                        Text("Update PIN")
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                ToggleRow(
                    label = "Biometric unlock",
                    checked = uiState.settings.biometricEnabled,
                    onCheckedChange = viewModel::onBiometricEnabledChange,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ToggleRow(
                    label = "Fake vault enabled",
                    checked = uiState.settings.fakeVaultEnabled,
                    onCheckedChange = viewModel::onFakeVaultEnabledChange,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ToggleRow(
                    label = "Intruder detection",
                    checked = uiState.settings.intruderCaptureEnabled,
                    onCheckedChange = viewModel::onIntruderCaptureEnabledChange,
                    modifier = Modifier.testTag(TestTags.SETTINGS_INTRUDER),
                )
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Session timeout",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(TestTags.SETTINGS_SESSION_TIMEOUT),
                )
                Text(
                    text = "${uiState.settings.sessionTimeoutMinutes} minutes",
                    color = VaultTextSecondary,
                )
                Slider(
                    value = uiState.settings.sessionTimeoutMinutes.toFloat(),
                    onValueChange = { viewModel.onSessionTimeoutChange(it.toInt()) },
                    valueRange = 1f..60f,
                    steps = 58,
                )
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Launcher mode", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (uiState.settings.launcherModeEnabled) {
                        "Launcher mode is enabled. Calculator Vault can act as a home screen."
                    } else {
                        "Launcher mode is disabled. Enable in system settings to use as a home app."
                    },
                    color = VaultTextSecondary,
                )
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Backup", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = viewModel::exportBackup,
                    enabled = !uiState.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator()
                    } else {
                        Text("Export backup JSON")
                    }
                }
                uiState.backupJson?.let { json ->
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        label = { Text("Backup JSON") },
                    )
                }
            }

            uiState.message?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }
            uiState.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PinField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
