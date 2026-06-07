package com.calculator.vault.security.auth

import com.calculator.vault.domain.model.VaultSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks vault session lifecycle and enforces configurable lock timeout.
 */
@Singleton
class SessionManager @Inject constructor(
    private val pinManager: PinManager,
) {
    private val _sessionState = MutableStateFlow(VaultSessionState.LOCKED)
    val sessionState: StateFlow<VaultSessionState> = _sessionState.asStateFlow()

    private var lastActivityTime: Long = 0L

    fun unlock(state: VaultSessionState) {
        _sessionState.value = state
        refresh()
    }

    fun isUnlocked(): Boolean = _sessionState.value != VaultSessionState.LOCKED

    fun lock() {
        _sessionState.value = VaultSessionState.LOCKED
    }

    fun refresh() {
        lastActivityTime = System.currentTimeMillis()
        pinManager.putLong(KEY_LAST_ACTIVITY, lastActivityTime)
    }

    fun isExpired(): Boolean {
        if (_sessionState.value == VaultSessionState.LOCKED) return false
        val timeoutMinutes = pinManager.getInt(KEY_SESSION_TIMEOUT, DEFAULT_TIMEOUT_MINUTES)
        val lastActivity = pinManager.getLong(KEY_LAST_ACTIVITY, lastActivityTime)
        val elapsed = System.currentTimeMillis() - lastActivity
        return elapsed > timeoutMinutes * 60_000L
    }

    fun setTimeoutMinutes(minutes: Int) {
        pinManager.putInt(KEY_SESSION_TIMEOUT, minutes)
    }

    companion object {
        private const val KEY_LAST_ACTIVITY = "last_activity"
        private const val KEY_SESSION_TIMEOUT = "session_timeout_minutes"
        private const val DEFAULT_TIMEOUT_MINUTES = 5
    }
}
