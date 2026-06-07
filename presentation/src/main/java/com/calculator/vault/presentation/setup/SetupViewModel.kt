package com.calculator.vault.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.usecase.SetupVaultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SetupStep {
    EnterPin,
    ConfirmPin,
    SecurityQuestion,
    FakePin,
    Biometric,
}

data class SetupUiState(
    val currentStep: SetupStep = SetupStep.EnterPin,
    val pin: String = "",
    val confirmPin: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = "",
    val fakePin: String = "",
    val useFakePin: Boolean = false,
    val biometricEnabled: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)

sealed class SetupEvent {
    data object SetupComplete : SetupEvent()
}

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val setupVaultUseCase: SetupVaultUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SetupEvent>()
    val events: SharedFlow<SetupEvent> = _events.asSharedFlow()

    fun onPinChange(value: String) {
        if (value.length <= 8 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(pin = value, errorMessage = null) }
        }
    }

    fun onConfirmPinChange(value: String) {
        if (value.length <= 8 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(confirmPin = value, errorMessage = null) }
        }
    }

    fun onSecurityQuestionChange(value: String) {
        _uiState.update { it.copy(securityQuestion = value, errorMessage = null) }
    }

    fun onSecurityAnswerChange(value: String) {
        _uiState.update { it.copy(securityAnswer = value, errorMessage = null) }
    }

    fun onFakePinChange(value: String) {
        if (value.length <= 8 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(fakePin = value, errorMessage = null) }
        }
    }

    fun onUseFakePinChange(enabled: Boolean) {
        _uiState.update { it.copy(useFakePin = enabled, fakePin = if (!enabled) "" else it.fakePin) }
    }

    fun onBiometricEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(biometricEnabled = enabled) }
    }

    fun onNextStep() {
        val state = _uiState.value
        val error = validateCurrentStep(state) ?: run {
            advanceStep(state)
            return
        }
        _uiState.update { it.copy(errorMessage = error) }
    }

    fun onBackStep() {
        _uiState.update { state ->
            val previous = when (state.currentStep) {
                SetupStep.EnterPin -> SetupStep.EnterPin
                SetupStep.ConfirmPin -> SetupStep.EnterPin
                SetupStep.SecurityQuestion -> SetupStep.ConfirmPin
                SetupStep.FakePin -> SetupStep.SecurityQuestion
                SetupStep.Biometric -> if (state.useFakePin) SetupStep.FakePin else SetupStep.SecurityQuestion
            }
            state.copy(currentStep = previous, errorMessage = null)
        }
    }

    fun completeSetup() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                setupVaultUseCase(
                    pin = state.pin,
                    fakePin = state.fakePin.takeIf { state.useFakePin && it.isNotBlank() },
                    securityQuestion = state.securityQuestion.trim(),
                    securityAnswer = state.securityAnswer.trim(),
                    biometricEnabled = state.biometricEnabled,
                )
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(SetupEvent.SetupComplete)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Setup failed",
                    )
                }
            }
        }
    }

    private fun advanceStep(state: SetupUiState) {
        val next = when (state.currentStep) {
            SetupStep.EnterPin -> SetupStep.ConfirmPin
            SetupStep.ConfirmPin -> SetupStep.SecurityQuestion
            SetupStep.SecurityQuestion -> SetupStep.FakePin
            SetupStep.FakePin -> SetupStep.Biometric
            SetupStep.Biometric -> {
                completeSetup()
                return
            }
        }
        _uiState.update { it.copy(currentStep = next, errorMessage = null) }
    }

    private fun validateCurrentStep(state: SetupUiState): String? = when (state.currentStep) {
        SetupStep.EnterPin -> when {
            state.pin.length !in 4..8 -> "PIN must be 4–8 digits"
            else -> null
        }
        SetupStep.ConfirmPin -> when {
            state.confirmPin != state.pin -> "PINs do not match"
            else -> null
        }
        SetupStep.SecurityQuestion -> when {
            state.securityQuestion.isBlank() -> "Enter a security question"
            state.securityAnswer.isBlank() -> "Enter a security answer"
            else -> null
        }
        SetupStep.FakePin -> when {
            !state.useFakePin -> null
            state.fakePin.length !in 4..8 -> "Fake PIN must be 4–8 digits"
            state.fakePin == state.pin -> "Fake PIN must differ from real PIN"
            else -> null
        }
        SetupStep.Biometric -> null
    }
}
