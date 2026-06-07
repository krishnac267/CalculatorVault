package com.calculator.vault.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.calculator.vault.data.local.dao.FakeContentDao
import com.calculator.vault.data.local.dao.VaultAppDao
import com.calculator.vault.data.mapper.toDomain
import com.calculator.vault.data.mapper.toEntity
import com.calculator.vault.domain.model.InstalledApp
import com.calculator.vault.domain.model.PinValidationResult
import com.calculator.vault.domain.model.SecuritySettings
import com.calculator.vault.domain.model.VaultBackup
import com.calculator.vault.domain.model.VaultSessionState
import com.calculator.vault.domain.repository.InstalledAppRepository
import com.calculator.vault.domain.repository.SecurityRepository
import com.calculator.vault.security.auth.PinManager
import com.calculator.vault.security.auth.SessionManager
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    private val pinManager: PinManager,
    private val sessionManager: SessionManager,
    private val vaultAppDao: VaultAppDao,
    private val fakeContentDao: FakeContentDao,
    private val gson: Gson,
) : SecurityRepository {

    private val _settingsFlow = MutableStateFlow(buildSettings())

    override suspend fun isSetupComplete(): Boolean = pinManager.isSetupComplete()

    override suspend fun setupVault(
        pin: String,
        fakePin: String?,
        securityQuestion: String,
        securityAnswer: String,
        biometricEnabled: Boolean,
    ) {
        require(pin.matches(PIN_REGEX)) { "PIN must be 4–8 digits" }
        if (!fakePin.isNullOrBlank()) {
            require(fakePin.matches(PIN_REGEX)) { "Fake PIN must be 4–8 digits" }
            require(fakePin != pin) { "Fake PIN must differ from real PIN" }
        }
        pinManager.storePin(pin, isFakePin = false)
        if (!fakePin.isNullOrBlank()) {
            pinManager.storePin(fakePin, isFakePin = true)
            pinManager.putBoolean(KEY_FAKE_VAULT, true)
        } else {
            pinManager.clearFakePin()
            pinManager.putBoolean(KEY_FAKE_VAULT, false)
        }
        pinManager.storeSecurityAnswer(securityAnswer)
        pinManager.putString(KEY_SECURITY_QUESTION, securityQuestion)
        pinManager.putBoolean(KEY_BIOMETRIC, biometricEnabled)
        sessionManager.setTimeoutMinutes(DEFAULT_TIMEOUT)
        pinManager.putInt(KEY_TIMEOUT, DEFAULT_TIMEOUT)
        pinManager.markSetupComplete()
        emitSettings()
    }

    override suspend fun validatePin(pin: String): PinValidationResult {
        if (!pin.matches(PIN_REGEX)) return PinValidationResult.Invalid
        if (!pinManager.isSetupComplete()) return PinValidationResult.Invalid
        return when {
            pinManager.verifyPin(pin, isFakePin = false) -> PinValidationResult.RealVault
            pinManager.getBoolean(KEY_FAKE_VAULT) &&
                pinManager.hasFakePin() &&
                pinManager.verifyPin(pin, isFakePin = true) -> PinValidationResult.FakeVault
            else -> PinValidationResult.Invalid
        }
    }

    override suspend fun changePin(oldPin: String, newPin: String): Boolean {
        if (!newPin.matches(PIN_REGEX)) return false
        if (!pinManager.verifyPin(oldPin, isFakePin = false)) return false
        if (pinManager.hasFakePin() && pinManager.verifyPin(newPin, isFakePin = true)) return false
        pinManager.storePin(newPin, isFakePin = false)
        return true
    }

    override suspend fun verifySecurityAnswer(answer: String): Boolean =
        pinManager.verifySecurityAnswer(answer)

    override fun observeSettings(): Flow<SecuritySettings> = _settingsFlow.asStateFlow()

    override suspend fun getSettings(): SecuritySettings = buildSettings()

    override suspend fun updateSettings(settings: SecuritySettings) {
        pinManager.putBoolean(KEY_BIOMETRIC, settings.biometricEnabled)
        pinManager.putBoolean(KEY_FAKE_VAULT, settings.fakeVaultEnabled)
        pinManager.putBoolean(KEY_LAUNCHER, settings.launcherModeEnabled)
        pinManager.putBoolean(KEY_INTRUDER, settings.intruderCaptureEnabled)
        sessionManager.setTimeoutMinutes(settings.sessionTimeoutMinutes)
        pinManager.putInt(KEY_TIMEOUT, settings.sessionTimeoutMinutes)
        if (!settings.fakeVaultEnabled) {
            pinManager.clearFakePin()
        }
        emitSettings()
    }

    override suspend fun exportBackup(): VaultBackup = VaultBackup(
        exportedAt = System.currentTimeMillis(),
        settings = buildSettings(),
        vaultApps = vaultAppDao.getAll().map { it.toDomain() },
        fakeContent = fakeContentDao.getAll().map { it.toDomain() },
    )

    override suspend fun importBackup(backup: VaultBackup, pin: String): Boolean {
        if (!pinManager.verifyPin(pin, isFakePin = false)) return false
        vaultAppDao.deleteAll()
        backup.vaultApps.forEach { vaultAppDao.insert(it.toEntity()) }
        fakeContentDao.deleteAll()
        if (backup.fakeContent.isNotEmpty()) {
            fakeContentDao.insertAll(backup.fakeContent.map { it.toEntity() })
        }
        updateSettings(backup.settings)
        return true
    }

    override fun observeSessionState(): Flow<VaultSessionState> = sessionManager.sessionState

    override suspend fun unlockSession(state: VaultSessionState) {
        sessionManager.unlock(state)
        emitSettings()
    }

    override suspend fun lockSession() {
        sessionManager.lock()
        emitSettings()
    }

    override suspend fun refreshSession() = sessionManager.refresh()

    override suspend fun isSessionExpired(): Boolean = sessionManager.isExpired()

    override suspend fun recordFailedPinAttempt(): Int = pinManager.incrementFailedAttempts()

    override suspend fun resetFailedPinAttempts() = pinManager.resetFailedAttempts()

    override suspend fun refreshSettingsCache() {
        emitSettings()
    }

    private fun emitSettings() {
        _settingsFlow.value = buildSettings()
    }

    private fun buildSettings() = SecuritySettings(
        isSetupComplete = pinManager.isSetupComplete(),
        securityQuestion = pinManager.getString(KEY_SECURITY_QUESTION),
        biometricEnabled = pinManager.getBoolean(KEY_BIOMETRIC),
        fakeVaultEnabled = pinManager.getBoolean(KEY_FAKE_VAULT),
        sessionTimeoutMinutes = pinManager.getInt(KEY_TIMEOUT, DEFAULT_TIMEOUT),
        launcherModeEnabled = pinManager.getBoolean(KEY_LAUNCHER),
        intruderCaptureEnabled = pinManager.getBoolean(KEY_INTRUDER, true),
    )

    companion object {
        private val PIN_REGEX = Regex("^\\d{4,8}$")
        private const val KEY_SECURITY_QUESTION = "security_question"
        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val KEY_FAKE_VAULT = "fake_vault_enabled"
        private const val KEY_LAUNCHER = "launcher_mode_enabled"
        private const val KEY_INTRUDER = "intruder_capture_enabled"
        private const val KEY_TIMEOUT = "session_timeout_minutes"
        private const val DEFAULT_TIMEOUT = 5
    }
}

@Singleton
class InstalledAppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : InstalledAppRepository {

    override suspend fun getLaunchableApps(): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val ourPackage = context.packageName
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == ourPackage) return@mapNotNull null
                val appInfo = try {
                    pm.getApplicationInfo(packageName, 0)
                } catch (_: PackageManager.NameNotFoundException) {
                    return@mapNotNull null
                }
                InstalledApp(
                    packageName = packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    override suspend fun launchApp(packageName: String) {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName) ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    }
}
