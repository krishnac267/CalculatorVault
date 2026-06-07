package com.calculator.vault.presentation.calculator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.model.PinValidationResult
import com.calculator.vault.domain.model.VaultSessionState
import com.calculator.vault.domain.usecase.HandlePinAttemptUseCase
import com.calculator.vault.domain.usecase.IsSetupCompleteUseCase
import com.calculator.vault.domain.usecase.ObserveSettingsUseCase
import com.calculator.vault.domain.usecase.UnlockVaultWithBiometricUseCase
import com.calculator.vault.security.auth.BiometricAuthenticator
import com.calculator.vault.security.detection.IntruderCaptureCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalculatorUiState(
    val display: String = "0",
    val isSetupComplete: Boolean = false,
    val isCheckingSetup: Boolean = true,
    val showPinSuccessAnimation: Boolean = false,
    val biometricEnabled: Boolean = false,
)

sealed class CalculatorEvent {
    data object NavigateToSetup : CalculatorEvent()
    data object NavigateToRealVault : CalculatorEvent()
    data object NavigateToFakeVault : CalculatorEvent()
}

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val isSetupCompleteUseCase: IsSetupCompleteUseCase,
    private val handlePinAttemptUseCase: HandlePinAttemptUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val unlockVaultWithBiometricUseCase: UnlockVaultWithBiometricUseCase,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val intruderCaptureCoordinator: IntruderCaptureCoordinator,
) : ViewModel() {

    private val engine = CalculatorEngine().apply {
        val expr = savedStateHandle.get<String>(KEY_EXPR)
        if (expr != null) {
            restoreState(
                CalculatorEngine.CalculatorState(
                    expression = expr,
                    memory = savedStateHandle.get<Double>(KEY_MEMORY) ?: 0.0,
                    lastResult = savedStateHandle.get<Double>(KEY_LAST) ?: 0.0,
                ),
            )
        }
    }

    private val _uiState = MutableStateFlow(
        CalculatorUiState(display = engine.getDisplay()),
    )
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CalculatorEvent>()
    val events: SharedFlow<CalculatorEvent> = _events.asSharedFlow()

    private var isValidatingPin = false

    init {
        viewModelScope.launch {
            val setupComplete = isSetupCompleteUseCase()
            _uiState.update {
                it.copy(isSetupComplete = setupComplete, isCheckingSetup = false)
            }
            if (!setupComplete) {
                _events.emit(CalculatorEvent.NavigateToSetup)
            }
        }
        viewModelScope.launch {
            observeSettingsUseCase().collect { settings ->
                _uiState.update { it.copy(biometricEnabled = settings.biometricEnabled) }
            }
        }
    }

    fun onInput(action: CalculatorAction) {
        if (_uiState.value.isCheckingSetup) return
        if (isValidatingPin) return

        when (action) {
            is CalculatorAction.Digit -> engine.inputDigit(action.value)
            CalculatorAction.Decimal -> engine.inputDecimal()
            is CalculatorAction.Operator -> engine.inputOperator(action.symbol)
            CalculatorAction.Percent -> engine.inputPercent()
            CalculatorAction.SquareRoot -> engine.inputSquareRoot()
            CalculatorAction.Power -> engine.inputPower()
            CalculatorAction.Clear -> engine.clear()
            CalculatorAction.ClearEntry -> engine.clearEntry()
            CalculatorAction.Backspace -> engine.backspace()
            CalculatorAction.MemoryClear -> engine.memoryClear()
            CalculatorAction.MemoryRecall -> engine.memoryRecall()
            CalculatorAction.MemoryAdd -> engine.memoryAdd()
            CalculatorAction.MemorySubtract -> engine.memorySubtract()
            CalculatorAction.MemoryStore -> engine.memoryStore()
            CalculatorAction.Equals -> handleEquals()
        }
        persistCalculatorState()
        _uiState.update { it.copy(display = engine.getDisplay()) }
    }

    fun requestBiometricUnlock(activity: FragmentActivity) {
        if (!_uiState.value.biometricEnabled || !_uiState.value.isSetupComplete) return
        biometricAuthenticator.authenticate(
            activity = activity,
            title = "Unlock Vault",
            subtitle = "Use fingerprint or face to access hidden apps",
            onSuccess = {
                viewModelScope.launch {
                    if (unlockVaultWithBiometricUseCase()) {
                        _events.emit(CalculatorEvent.NavigateToRealVault)
                    }
                }
            },
            onError = { /* user cancelled — stay on calculator */ },
        )
    }

    private fun handleEquals() {
        if (engine.isPinAttempt()) {
            if (!_uiState.value.isSetupComplete) return
            viewModelScope.launch {
                isValidatingPin = true
                val pin = engine.getPinForValidation()
                val result = handlePinAttemptUseCase(pin) {
                    intruderCaptureCoordinator.capturePhoto()
                }
                when (result) {
                    PinValidationResult.RealVault -> {
                        _uiState.update { it.copy(showPinSuccessAnimation = true) }
                        delay(PIN_ANIMATION_MS)
                        engine.clear()
                        persistCalculatorState()
                        _uiState.update {
                            it.copy(showPinSuccessAnimation = false, display = engine.getDisplay())
                        }
                        _events.emit(CalculatorEvent.NavigateToRealVault)
                    }
                    PinValidationResult.FakeVault -> {
                        _uiState.update { it.copy(showPinSuccessAnimation = true) }
                        delay(PIN_ANIMATION_MS)
                        engine.clear()
                        persistCalculatorState()
                        _uiState.update {
                            it.copy(showPinSuccessAnimation = false, display = engine.getDisplay())
                        }
                        _events.emit(CalculatorEvent.NavigateToFakeVault)
                    }
                    else -> {
                        engine.clear()
                        persistCalculatorState()
                        _uiState.update { it.copy(display = engine.getDisplay()) }
                    }
                }
                isValidatingPin = false
            }
        } else {
            engine.evaluate()
            persistCalculatorState()
            _uiState.update { it.copy(display = engine.getDisplay()) }
        }
    }

    private fun persistCalculatorState() {
        val state = engine.saveState()
        savedStateHandle[KEY_EXPR] = state.expression
        savedStateHandle[KEY_MEMORY] = state.memory
        savedStateHandle[KEY_LAST] = state.lastResult
    }

    companion object {
        private const val PIN_ANIMATION_MS = 600L
        private const val KEY_EXPR = "calc_expression"
        private const val KEY_MEMORY = "calc_memory"
        private const val KEY_LAST = "calc_last_result"
    }
}

sealed class CalculatorAction {
    data class Digit(val value: String) : CalculatorAction()
    data class Operator(val symbol: String) : CalculatorAction()
    data object Decimal : CalculatorAction()
    data object Percent : CalculatorAction()
    data object SquareRoot : CalculatorAction()
    data object Power : CalculatorAction()
    data object Clear : CalculatorAction()
    data object ClearEntry : CalculatorAction()
    data object Backspace : CalculatorAction()
    data object MemoryClear : CalculatorAction()
    data object MemoryRecall : CalculatorAction()
    data object MemoryAdd : CalculatorAction()
    data object MemorySubtract : CalculatorAction()
    data object MemoryStore : CalculatorAction()
    data object Equals : CalculatorAction()
}
