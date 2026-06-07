package com.calculator.vault.data.testing

import android.content.Context
import com.calculator.vault.data.local.dao.FakeContentDao
import com.calculator.vault.data.local.dao.IntruderLogDao
import com.calculator.vault.data.local.dao.VaultAppDao
import com.calculator.vault.domain.repository.SecurityRepository
import com.calculator.vault.security.auth.PinManager
import com.calculator.vault.security.auth.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Resets app state without `pm clear`, which kills the instrumentation process
 * when tests run in the target app process.
 */
class ResetAppForTestingUseCase @Inject constructor(
    private val pinManager: PinManager,
    private val securityRepository: SecurityRepository,
    private val sessionManager: SessionManager,
    private val vaultAppDao: VaultAppDao,
    private val fakeContentDao: FakeContentDao,
    private val intruderLogDao: IntruderLogDao,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke() {
        vaultAppDao.deleteAll()
        fakeContentDao.deleteAll()
        intruderLogDao.clearAll()
        pinManager.clearAllForTesting()
        securityRepository.refreshSettingsCache()
        sessionManager.lock()
        context.filesDir.listFiles()
            ?.filter { it.name.startsWith("intruder_") }
            ?.forEach { it.delete() }
    }
}
