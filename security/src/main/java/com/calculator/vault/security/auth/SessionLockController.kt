package com.calculator.vault.security.auth

import javax.inject.Inject
import javax.inject.Singleton

/** Central policy for when an active vault session must be invalidated. */
@Singleton
class SessionLockController @Inject constructor(
    private val sessionManager: SessionManager,
) {
    fun isVaultUnlocked(): Boolean = sessionManager.isUnlocked()

    fun lockNow() {
        sessionManager.lock()
    }
}
