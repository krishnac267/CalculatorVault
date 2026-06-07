package com.calculator.vault.domain.repository

import com.calculator.vault.domain.model.FakeContent
import com.calculator.vault.domain.model.InstalledApp
import com.calculator.vault.domain.model.IntruderLog
import com.calculator.vault.domain.model.PinValidationResult
import com.calculator.vault.domain.model.SecuritySettings
import com.calculator.vault.domain.model.VaultApp
import com.calculator.vault.domain.model.VaultBackup
import com.calculator.vault.domain.model.VaultSessionState
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    suspend fun isSetupComplete(): Boolean
    suspend fun setupVault(
        pin: String,
        fakePin: String?,
        securityQuestion: String,
        securityAnswer: String,
        biometricEnabled: Boolean,
    )
    suspend fun validatePin(pin: String): PinValidationResult
    suspend fun changePin(oldPin: String, newPin: String): Boolean
    suspend fun verifySecurityAnswer(answer: String): Boolean
    fun observeSettings(): Flow<SecuritySettings>
    suspend fun updateSettings(settings: SecuritySettings)
    suspend fun exportBackup(): VaultBackup
    suspend fun importBackup(backup: VaultBackup, pin: String): Boolean
    fun observeSessionState(): Flow<VaultSessionState>
    suspend fun unlockSession(state: VaultSessionState)
    suspend fun lockSession()
    suspend fun refreshSession()
    suspend fun isSessionExpired(): Boolean
    suspend fun recordFailedPinAttempt(): Int
    suspend fun resetFailedPinAttempts()
    suspend fun getSettings(): SecuritySettings
    suspend fun refreshSettingsCache()
}

interface VaultRepository {
    fun observeVaultApps(includeFake: Boolean = false): Flow<List<VaultApp>>
    fun observeFavorites(): Flow<List<VaultApp>>
    fun observeRecent(limit: Int = 10): Flow<List<VaultApp>>
    suspend fun addApp(app: VaultApp)
    suspend fun removeApp(packageName: String)
    suspend fun toggleFavorite(packageName: String)
    suspend fun recordLaunch(packageName: String)
    suspend fun getVaultApp(packageName: String): VaultApp?
}

interface FakeVaultRepository {
    fun observeFakeContent(): Flow<List<FakeContent>>
    suspend fun seedDefaultFakeContent()
}

interface IntruderRepository {
    fun observeLogs(): Flow<List<IntruderLog>>
    suspend fun addLog(log: IntruderLog)
    suspend fun clearLogs()
}

interface InstalledAppRepository {
    suspend fun getLaunchableApps(): List<InstalledApp>
    suspend fun launchApp(packageName: String)
}
