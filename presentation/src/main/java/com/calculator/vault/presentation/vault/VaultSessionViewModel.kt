package com.calculator.vault.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.model.VaultSessionState
import com.calculator.vault.domain.usecase.ObserveSessionStateUseCase
import com.calculator.vault.presentation.navigation.VaultNavigationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultSessionViewModel @Inject constructor(
    observeSessionStateUseCase: ObserveSessionStateUseCase,
    private val vaultNavigationManager: VaultNavigationManager,
) : ViewModel() {

    val sessionState: StateFlow<VaultSessionState> = observeSessionStateUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VaultSessionState.LOCKED)

    val lockRequests = vaultNavigationManager.lockVault

    fun requestLock() {
        viewModelScope.launch {
            vaultNavigationManager.requestLockVault()
        }
    }
}
