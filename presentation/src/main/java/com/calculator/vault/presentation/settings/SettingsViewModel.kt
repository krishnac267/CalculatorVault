package com.calculator.vault.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.model.SecuritySettings
import com.calculator.vault.domain.usecase.ChangePinUseCase
import com.calculator.vault.domain.usecase.ExportBackupUseCase
import com.calculator.vault.domain.usecase.ObserveSettingsUseCase
import com.calculator.vault.domain.usecase.UpdateSettingsUseCase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: SecuritySettings = SecuritySettings(),
    val oldPin: String = "",
    val newPin: String = "",
    val confirmNewPin: String = "",
    val backupJson: String? = null,
    val message: String? = null,
    val errorMessage: String? = null,
    val isExporting: Boolean = false,
    val isChangingPin: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val changePinUseCase: ChangePinUseCase,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val gson: Gson,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSettingsUseCase().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun onOldPinChange(value: String) {
        if (value.length <= 8 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(oldPin = value, errorMessage = null) }
        }
    }

    fun onNewPinChange(value: String) {
        if (value.length <= 8 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(newPin = value, errorMessage = null) }
        }
    }

    fun onConfirmNewPinChange(value: String) {
        if (value.length <= 8 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(confirmNewPin = value, errorMessage = null) }
        }
    }

    fun onBiometricEnabledChange(enabled: Boolean) {
        updateSettings { it.copy(biometricEnabled = enabled) }
    }

    fun onFakeVaultEnabledChange(enabled: Boolean) {
        updateSettings { it.copy(fakeVaultEnabled = enabled) }
    }

    fun onIntruderCaptureEnabledChange(enabled: Boolean) {
        updateSettings { it.copy(intruderCaptureEnabled = enabled) }
    }

    fun onSessionTimeoutChange(minutes: Int) {
        updateSettings { it.copy(sessionTimeoutMinutes = minutes.coerceIn(1, 60)) }
    }

    fun changePin() {
        val state = _uiState.value
        when {
            state.oldPin.length !in 4..8 -> {
                _uiState.update { it.copy(errorMessage = "Current PIN must be 4–8 digits") }
                return
            }
            state.newPin.length !in 4..8 -> {
                _uiState.update { it.copy(errorMessage = "New PIN must be 4–8 digits") }
                return
            }
            state.newPin != state.confirmNewPin -> {
                _uiState.update { it.copy(errorMessage = "New PINs do not match") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPin = true, errorMessage = null) }
            val success = changePinUseCase(state.oldPin, state.newPin)
            _uiState.update {
                it.copy(
                    isChangingPin = false,
                    oldPin = "",
                    newPin = "",
                    confirmNewPin = "",
                    message = if (success) "PIN changed successfully" else null,
                    errorMessage = if (!success) "Current PIN is incorrect" else null,
                )
            }
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            try {
                val backup = exportBackupUseCase()
                val json = gson.toJson(backup)
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        backupJson = json,
                        message = "Backup exported",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = e.message ?: "Export failed",
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, errorMessage = null) }
    }

    private fun updateSettings(transform: (SecuritySettings) -> SecuritySettings) {
        val updated = transform(_uiState.value.settings)
        _uiState.update { it.copy(settings = updated) }
        viewModelScope.launch {
            updateSettingsUseCase(updated)
        }
    }
}
