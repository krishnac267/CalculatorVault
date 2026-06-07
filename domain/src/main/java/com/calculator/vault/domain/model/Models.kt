package com.calculator.vault.domain.model

/** Domain model for an app stored in the vault. */
data class VaultApp(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val isFavorite: Boolean = false,
    val isFake: Boolean = false,
    val lastOpenedAt: Long? = null,
    val addedAt: Long = System.currentTimeMillis(),
)

/** Decoy content shown in fake vault mode. */
data class FakeContent(
    val id: Long = 0,
    val title: String,
    val subtitle: String,
    val type: FakeContentType,
)

enum class FakeContentType {
    APP,
    PHOTO,
    NOTE,
}

/** Intruder detection record. */
data class IntruderLog(
    val id: Long = 0,
    val timestamp: Long,
    val photoPath: String?,
    val attemptCount: Int,
)

/** User security configuration. */
data class SecuritySettings(
    val isSetupComplete: Boolean = false,
    val securityQuestion: String = "",
    val securityAnswerHash: String = "",
    val biometricEnabled: Boolean = false,
    val fakeVaultEnabled: Boolean = false,
    val sessionTimeoutMinutes: Int = 5,
    val launcherModeEnabled: Boolean = false,
    val intruderCaptureEnabled: Boolean = true,
)

/** Result of PIN validation. */
sealed class PinValidationResult {
    data object RealVault : PinValidationResult()
    data object FakeVault : PinValidationResult()
    data object Invalid : PinValidationResult()
    data class CalculatorResult(val value: String) : PinValidationResult()
}

/** Installed app available to add to vault. */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
)

/** Vault session state. */
enum class VaultSessionState {
    LOCKED,
    REAL_VAULT,
    FAKE_VAULT,
}

/** Export payload for encrypted backup. */
data class VaultBackup(
    val version: Int = 2,
    val exportedAt: Long,
    val settings: SecuritySettings,
    val vaultApps: List<VaultApp>,
    val fakeContent: List<FakeContent>,
    val secureNotes: List<SecureNote> = emptyList(),
    val secureBookmarks: List<SecureBookmark> = emptyList(),
)
