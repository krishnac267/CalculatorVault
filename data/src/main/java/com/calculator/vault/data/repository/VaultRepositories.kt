package com.calculator.vault.data.repository

import com.calculator.vault.data.local.dao.FakeContentDao
import com.calculator.vault.data.local.dao.IntruderLogDao
import com.calculator.vault.data.local.dao.VaultAppDao
import com.calculator.vault.data.mapper.toDomain
import com.calculator.vault.data.mapper.toEntity
import com.calculator.vault.domain.model.FakeContent
import com.calculator.vault.domain.model.FakeContentType
import com.calculator.vault.domain.model.IntruderLog
import com.calculator.vault.domain.model.VaultApp
import com.calculator.vault.domain.repository.FakeVaultRepository
import com.calculator.vault.domain.repository.IntruderRepository
import com.calculator.vault.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepositoryImpl @Inject constructor(
    private val vaultAppDao: VaultAppDao,
) : VaultRepository {

    override fun observeVaultApps(includeFake: Boolean): Flow<List<VaultApp>> {
        val source = if (includeFake) vaultAppDao.observeFakeApps() else vaultAppDao.observeRealApps()
        return source.map { list -> list.map { it.toDomain() } }
    }

    override fun observeFavorites(): Flow<List<VaultApp>> =
        vaultAppDao.observeFavorites().map { list -> list.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<VaultApp>> =
        vaultAppDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun addApp(app: VaultApp) {
        vaultAppDao.insert(app.toEntity())
    }

    override suspend fun removeApp(packageName: String) {
        vaultAppDao.delete(packageName)
    }

    override suspend fun toggleFavorite(packageName: String) {
        vaultAppDao.toggleFavorite(packageName)
    }

    override suspend fun recordLaunch(packageName: String) {
        vaultAppDao.updateLastOpened(packageName, System.currentTimeMillis())
    }

    override suspend fun getVaultApp(packageName: String): VaultApp? =
        vaultAppDao.getByPackageName(packageName)?.toDomain()
}

@Singleton
class FakeVaultRepositoryImpl @Inject constructor(
    private val fakeContentDao: FakeContentDao,
    private val vaultAppDao: VaultAppDao,
) : FakeVaultRepository {

    override fun observeFakeContent(): Flow<List<FakeContent>> =
        fakeContentDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun seedDefaultFakeContent() {
        if (fakeContentDao.count() > 0) return
        fakeContentDao.insertAll(
            listOf(
                FakeContent(title = "Notes", subtitle = "Personal notes", type = FakeContentType.APP),
                FakeContent(title = "Gallery", subtitle = "12 photos", type = FakeContentType.PHOTO),
                FakeContent(title = "Budget", subtitle = "Monthly tracker", type = FakeContentType.APP),
                FakeContent(title = "Vacation", subtitle = "Beach photo", type = FakeContentType.PHOTO),
                FakeContent(title = "Shopping List", subtitle = "Groceries", type = FakeContentType.NOTE),
            ).map { it.toEntity() },
        )
        vaultAppDao.insert(
            VaultApp(
                packageName = "fake.notes",
                appName = "Notes",
                isFake = true,
            ).toEntity(),
        )
        vaultAppDao.insert(
            VaultApp(
                packageName = "fake.gallery",
                appName = "Gallery",
                isFake = true,
            ).toEntity(),
        )
    }
}

@Singleton
class IntruderRepositoryImpl @Inject constructor(
    private val intruderLogDao: IntruderLogDao,
) : IntruderRepository {

    override fun observeLogs(): Flow<List<IntruderLog>> =
        intruderLogDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addLog(log: IntruderLog) {
        intruderLogDao.insert(log.toEntity())
    }

    override suspend fun clearLogs() {
        intruderLogDao.clearAll()
    }
}
