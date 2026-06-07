package com.calculator.vault.presentation.intruder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.model.IntruderLog
import com.calculator.vault.domain.usecase.ObserveIntruderLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntruderLogUiState(
    val logs: List<IntruderLog> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class IntruderLogViewModel @Inject constructor(
    private val observeIntruderLogsUseCase: ObserveIntruderLogsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntruderLogUiState())
    val uiState: StateFlow<IntruderLogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeIntruderLogsUseCase().collect { logs ->
                _uiState.update { it.copy(logs = logs.sortedByDescending { log -> log.timestamp }, isLoading = false) }
            }
        }
    }
}
