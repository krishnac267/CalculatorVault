package com.calculator.vault.domain.usecase

import com.calculator.vault.domain.model.FakeContent
import com.calculator.vault.domain.model.InstalledApp
import com.calculator.vault.domain.model.IntruderLog
import com.calculator.vault.domain.model.PinValidationResult
import com.calculator.vault.domain.model.SecuritySettings
import com.calculator.vault.domain.model.VaultApp
import com.calculator.vault.domain.model.VaultBackup
import com.calculator.vault.domain.model.VaultSessionState
import com.calculator.vault.domain.repository.FakeVaultRepository
import com.calculator.vault.domain.repository.IntruderRepository
import com.calculator.vault.domain.repository.InstalledAppRepository
import com.calculator.vault.domain.repository.SecurityRepository
import com.calculator.vault.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsSetupCompleteUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke(): Boolean = securityRepository.isSetupComplete()
}

class SetupVaultUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
    private val fakeVaultRepository: FakeVaultRepository,
) {
    suspend operator fun invoke(
        pin: String,
        fakePin: String?,
        securityQuestion: String,
        securityAnswer: String,
        biometricEnabled: Boolean,
    ) {
        securityRepository.setupVault(
            pin = pin,
            fakePin = fakePin,
            securityQuestion = securityQuestion,
            securityAnswer = securityAnswer,
            biometricEnabled = biometricEnabled,
        )
        fakeVaultRepository.seedDefaultFakeContent()
    }
}

class ValidatePinUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke(pin: String): PinValidationResult =
        securityRepository.validatePin(pin)
}

class HandlePinAttemptUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
    private val intruderRepository: IntruderRepository,
) {
    suspend operator fun invoke(
        pin: String,
        captureIntruderPhoto: suspend () -> String?,
    ): PinValidationResult {
        if (!securityRepository.isSetupComplete()) return PinValidationResult.Invalid

        val result = securityRepository.validatePin(pin)
        when (result) {
            is PinValidationResult.RealVault -> {
                securityRepository.resetFailedPinAttempts()
                securityRepository.unlockSession(VaultSessionState.REAL_VAULT)
            }
            is PinValidationResult.FakeVault -> {
                securityRepository.resetFailedPinAttempts()
                securityRepository.unlockSession(VaultSessionState.FAKE_VAULT)
            }
            else -> {
                val attempts = securityRepository.recordFailedPinAttempt()
                val settings = securityRepository.getSettings()
                if (attempts >= INTRUDER_THRESHOLD && settings.intruderCaptureEnabled) {
                    val photoPath = captureIntruderPhoto()
                    intruderRepository.addLog(
                        IntruderLog(
                            timestamp = System.currentTimeMillis(),
                            photoPath = photoPath,
                            attemptCount = attempts,
                        ),
                    )
                    securityRepository.resetFailedPinAttempts()
                }
            }
        }
        return result
    }

    companion object {
        private const val INTRUDER_THRESHOLD = 3
    }
}

class RefreshSessionUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke() = securityRepository.refreshSession()
}

class UnlockVaultWithBiometricUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke(): Boolean {
        if (!securityRepository.getSettings().biometricEnabled) return false
        securityRepository.unlockSession(VaultSessionState.REAL_VAULT)
        return true
    }
}

class ObserveVaultAppsUseCase @Inject constructor(
    private val vaultRepository: VaultRepository,
) {
    operator fun invoke(isFakeVault: Boolean): Flow<List<VaultApp>> =
        if (isFakeVault) {
            vaultRepository.observeVaultApps(includeFake = true)
        } else {
            vaultRepository.observeVaultApps(includeFake = false)
        }
}

class ObserveFavoritesUseCase @Inject constructor(
    private val vaultRepository: VaultRepository,
) {
    operator fun invoke(): Flow<List<VaultApp>> = vaultRepository.observeFavorites()
}

class ObserveRecentAppsUseCase @Inject constructor(
    private val vaultRepository: VaultRepository,
) {
    operator fun invoke(): Flow<List<VaultApp>> = vaultRepository.observeRecent()
}

class LaunchVaultAppUseCase @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val installedAppRepository: InstalledAppRepository,
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke(packageName: String) {
        vaultRepository.recordLaunch(packageName)
        securityRepository.refreshSession()
        installedAppRepository.launchApp(packageName)
    }
}

class GetInstalledAppsUseCase @Inject constructor(
    private val installedAppRepository: InstalledAppRepository,
) {
    suspend operator fun invoke(): List<InstalledApp> =
        installedAppRepository.getLaunchableApps()
}

class AddAppToVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository,
) {
    suspend operator fun invoke(app: VaultApp) = vaultRepository.addApp(app)
}

class RemoveAppFromVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository,
) {
    suspend operator fun invoke(packageName: String) = vaultRepository.removeApp(packageName)
}

class ToggleFavoriteUseCase @Inject constructor(
    private val vaultRepository: VaultRepository,
) {
    suspend operator fun invoke(packageName: String) = vaultRepository.toggleFavorite(packageName)
}

class ObserveSettingsUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    operator fun invoke(): Flow<SecuritySettings> = securityRepository.observeSettings()
}

class UpdateSettingsUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke(settings: SecuritySettings) =
        securityRepository.updateSettings(settings)
}

class ChangePinUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke(oldPin: String, newPin: String): Boolean =
        securityRepository.changePin(oldPin, newPin)
}

class ObserveIntruderLogsUseCase @Inject constructor(
    private val intruderRepository: IntruderRepository,
) {
    operator fun invoke(): Flow<List<IntruderLog>> = intruderRepository.observeLogs()
}

class ObserveFakeContentUseCase @Inject constructor(
    private val fakeVaultRepository: FakeVaultRepository,
) {
    operator fun invoke(): Flow<List<FakeContent>> = fakeVaultRepository.observeFakeContent()
}

class ExportBackupUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke(): VaultBackup = securityRepository.exportBackup()
}

class ImportBackupUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke(backup: VaultBackup, pin: String): Boolean =
        securityRepository.importBackup(backup, pin)
}

class LockSessionUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke() = securityRepository.lockSession()
}

class ObserveSessionStateUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    operator fun invoke() = securityRepository.observeSessionState()
}

class CheckSessionExpiredUseCase @Inject constructor(
    private val securityRepository: SecurityRepository,
) {
    suspend operator fun invoke(): Boolean = securityRepository.isSessionExpired()
}
