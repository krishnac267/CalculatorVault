package com.calculator.vault.domain.repository

import com.calculator.vault.domain.model.FakeContent
import com.calculator.vault.domain.model.InstalledApp
import com.calculator.vault.domain.model.IntruderLog
import com.calculator.vault.domain.model.PinValidationResult
import com.calculator.vault.domain.model.PremiumStatus
import com.calculator.vault.domain.model.SecureBookmark
import com.calculator.vault.domain.model.SecureNote
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

interface SecureNoteRepository {
    fun observeNotes(): Flow<List<SecureNote>>
    suspend fun upsert(note: SecureNote): Long
    suspend fun delete(id: Long)
    suspend fun search(query: String): List<SecureNote>
}

interface SecureBookmarkRepository {
    fun observeBookmarks(): Flow<List<SecureBookmark>>
    suspend fun upsert(bookmark: SecureBookmark): Long
    suspend fun delete(id: Long)
}

interface PremiumRepository {
    fun observePremiumStatus(): Flow<PremiumStatus>
    suspend fun getPremiumStatus(): PremiumStatus
    suspend fun setPremiumStatus(status: PremiumStatus)
    suspend fun canAddMoreVaultApps(currentCount: Int, freeLimit: Int): Boolean
}

interface AnalyticsTracker {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
    fun setUserProperty(name: String, value: String)
}

