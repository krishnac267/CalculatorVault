package com.calculator.vault.presentation.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Coordinates navigation events that originate outside Compose (e.g. lifecycle). */
@Singleton
class VaultNavigationManager @Inject constructor() {
    private val _lockVault = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val lockVault: SharedFlow<Unit> = _lockVault.asSharedFlow()

    fun requestLockVault() {
        _lockVault.tryEmit(Unit)
    }
}
