package com.calculator.vault.domain.usecase

import com.calculator.vault.domain.model.IntruderLog
import com.calculator.vault.domain.model.PinValidationResult
import com.calculator.vault.domain.model.SecuritySettings
import com.calculator.vault.domain.model.VaultSessionState
import com.calculator.vault.domain.repository.IntruderRepository
import com.calculator.vault.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HandlePinAttemptUseCaseTest {

    @Test
    fun correctPin_unlocksRealVault() = runBlockingTest {
        val repo = FakeSecurityRepository(validateResult = PinValidationResult.RealVault)
        val useCase = HandlePinAttemptUseCase(repo, FakeIntruderRepository())
        val result = useCase("1234") { null }
        assertEquals(PinValidationResult.RealVault, result)
        assertEquals(VaultSessionState.REAL_VAULT, repo.sessionState)
    }

    @Test
    fun wrongPinThreeTimes_triggersIntruderLog() = runBlockingTest {
        val repo = FakeSecurityRepository(validateResult = PinValidationResult.Invalid)
        val intruder = FakeIntruderRepository()
        val useCase = HandlePinAttemptUseCase(repo, intruder)
        repeat(3) { useCase("0000") { null } }
        assertEquals(1, intruder.logs.size)
        assertEquals(3, intruder.logs.first().attemptCount)
    }

    @Test
    fun setupIncomplete_doesNotValidate() = runBlockingTest {
        val repo = FakeSecurityRepository(setupComplete = false)
        val useCase = HandlePinAttemptUseCase(repo, FakeIntruderRepository())
        val result = useCase("1234") { null }
        assertEquals(PinValidationResult.Invalid, result)
        assertEquals(0, repo.failedAttempts)
    }

    private fun runBlockingTest(block: suspend () -> Unit) {
        kotlinx.coroutines.runBlocking { block() }
    }
}

private class FakeSecurityRepository(
    private val setupComplete: Boolean = true,
    private val validateResult: PinValidationResult = PinValidationResult.Invalid,
) : SecurityRepository {
    var sessionState: VaultSessionState = VaultSessionState.LOCKED
    var failedAttempts = 0

    override suspend fun isSetupComplete(): Boolean = setupComplete
    override suspend fun setupVault(
        pin: String,
        fakePin: String?,
        securityQuestion: String,
        securityAnswer: String,
        biometricEnabled: Boolean,
    ) = Unit
    override suspend fun validatePin(pin: String): PinValidationResult = validateResult
    override suspend fun changePin(oldPin: String, newPin: String): Boolean = false
    override suspend fun verifySecurityAnswer(answer: String): Boolean = false
    override fun observeSettings(): Flow<SecuritySettings> = flowOf(SecuritySettings())
    override suspend fun getSettings(): SecuritySettings =
        SecuritySettings(intruderCaptureEnabled = true)
    override suspend fun updateSettings(settings: SecuritySettings) = Unit
    override suspend fun exportBackup() = throw UnsupportedOperationException()
    override suspend fun importBackup(backup: com.calculator.vault.domain.model.VaultBackup, pin: String) = false
    override fun observeSessionState(): Flow<VaultSessionState> = flowOf(sessionState)
    override suspend fun unlockSession(state: VaultSessionState) { sessionState = state }
    override suspend fun lockSession() { sessionState = VaultSessionState.LOCKED }
    override suspend fun refreshSession() = Unit
    override suspend fun isSessionExpired(): Boolean = false
    override suspend fun recordFailedPinAttempt(): Int {
        failedAttempts++
        return failedAttempts
    }
    override suspend fun resetFailedPinAttempts() { failedAttempts = 0 }
    override suspend fun refreshSettingsCache() = Unit
}

private class FakeIntruderRepository : IntruderRepository {
    val logs = mutableListOf<IntruderLog>()
    override fun observeLogs(): Flow<List<IntruderLog>> = flowOf(logs)
    override suspend fun addLog(log: IntruderLog) { logs.add(log) }
    override suspend fun clearLogs() { logs.clear() }
}
