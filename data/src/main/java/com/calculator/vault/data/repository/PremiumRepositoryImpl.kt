package com.calculator.vault.data.repository

import com.calculator.vault.domain.model.PremiumStatus
import com.calculator.vault.domain.repository.PremiumRepository
import com.calculator.vault.security.auth.PinManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val pinManager: PinManager,
) : PremiumRepository {
    private val _status = MutableStateFlow(readStatus())
    override fun observePremiumStatus(): Flow<PremiumStatus> = _status.asStateFlow()

    override suspend fun getPremiumStatus(): PremiumStatus = readStatus()

    override suspend fun setPremiumStatus(status: PremiumStatus) {
        pinManager.putBoolean(KEY_PREMIUM, status.isPremium)
        pinManager.putString(KEY_PRODUCT, status.activeProductId.orEmpty())
        pinManager.putBoolean(KEY_LIFETIME, status.isLifetime)
        _status.value = status
    }

    override suspend fun canAddMoreVaultApps(currentCount: Int, freeLimit: Int): Boolean {
        if (readStatus().isPremium) return true
        return currentCount < freeLimit
    }

    private fun readStatus(): PremiumStatus = PremiumStatus(
        isPremium = pinManager.getBoolean(KEY_PREMIUM),
        activeProductId = pinManager.getString(KEY_PRODUCT).ifBlank { null },
        isLifetime = pinManager.getBoolean(KEY_LIFETIME),
    )

    companion object {
        private const val KEY_PREMIUM = "premium_active"
        private const val KEY_PRODUCT = "premium_product_id"
        private const val KEY_LIFETIME = "premium_lifetime"
    }
}
