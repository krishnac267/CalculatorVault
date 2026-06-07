package com.calculator.vault.domain.model

/** Premium entitlement state for feature gating. */
data class PremiumStatus(
    val isPremium: Boolean = false,
    val activeProductId: String? = null,
    val isLifetime: Boolean = false,
)

/** User-created secure note stored in encrypted Room. */
data class SecureNote(
    val id: Long = 0,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Private bookmark stored in encrypted Room. */
data class SecureBookmark(
    val id: Long = 0,
    val title: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Device integrity signal for user-facing warnings. */
data class DeviceSecurityStatus(
    val isRooted: Boolean = false,
    val isEmulator: Boolean = false,
) {
    val shouldWarn: Boolean get() = isRooted || isEmulator
}
