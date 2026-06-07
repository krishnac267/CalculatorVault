package com.calculator.vault.domain.premium

import com.calculator.vault.domain.model.PremiumStatus
import com.calculator.vault.domain.repository.PremiumRepository
import com.calculator.vault.domain.usecase.AddAppToVaultUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumGatingTest {

    private class FakePremiumRepo(
        private val premium: Boolean,
    ) : PremiumRepository {
        override fun observePremiumStatus() = flowOf(PremiumStatus(isPremium = premium))
        override suspend fun getPremiumStatus() = PremiumStatus(isPremium = premium)
        override suspend fun setPremiumStatus(status: PremiumStatus) = Unit
        override suspend fun canAddMoreVaultApps(currentCount: Int, freeLimit: Int): Boolean =
            premium || currentCount < freeLimit
    }

    @Test
    fun freeUser_blockedAtLimit() = runBlocking {
        val repo = FakePremiumRepo(premium = false)
        assertTrue(repo.canAddMoreVaultApps(currentCount = 2, freeLimit = 3))
        assertFalse(repo.canAddMoreVaultApps(currentCount = 3, freeLimit = 3))
    }

    @Test
    fun premiumUser_unlimited() = runBlocking {
        val repo = FakePremiumRepo(premium = true)
        assertTrue(repo.canAddMoreVaultApps(currentCount = 100, freeLimit = 3))
    }

    @Test
    fun addAppResult_hasLimitReached() {
        assertTrue(AddAppToVaultUseCase.Result.LimitReached is AddAppToVaultUseCase.Result)
        assertTrue(AddAppToVaultUseCase.Result.Added is AddAppToVaultUseCase.Result)
    }
}
