package com.calculator.vault.data.di

import android.content.Context
import androidx.room.Room
import com.calculator.vault.data.local.dao.FakeContentDao
import com.calculator.vault.data.local.dao.IntruderLogDao
import com.calculator.vault.data.local.dao.VaultAppDao
import com.calculator.vault.data.local.database.VaultDatabase
import com.calculator.vault.data.repository.FakeVaultRepositoryImpl
import com.calculator.vault.data.repository.InstalledAppRepositoryImpl
import com.calculator.vault.data.repository.IntruderRepositoryImpl
import com.calculator.vault.data.repository.SecurityRepositoryImpl
import com.calculator.vault.data.repository.VaultRepositoryImpl
import com.calculator.vault.domain.repository.FakeVaultRepository
import com.calculator.vault.domain.repository.InstalledAppRepository
import com.calculator.vault.domain.repository.IntruderRepository
import com.calculator.vault.domain.repository.SecurityRepository
import com.calculator.vault.domain.repository.VaultRepository
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VaultDatabase =
        Room.databaseBuilder(context, VaultDatabase::class.java, "vault_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideVaultAppDao(db: VaultDatabase): VaultAppDao = db.vaultAppDao()

    @Provides
    fun provideFakeContentDao(db: VaultDatabase): FakeContentDao = db.fakeContentDao()

    @Provides
    fun provideIntruderLogDao(db: VaultDatabase): IntruderLogDao = db.intruderLogDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository

    @Binds
    @Singleton
    abstract fun bindVaultRepository(impl: VaultRepositoryImpl): VaultRepository

    @Binds
    @Singleton
    abstract fun bindFakeVaultRepository(impl: FakeVaultRepositoryImpl): FakeVaultRepository

    @Binds
    @Singleton
    abstract fun bindIntruderRepository(impl: IntruderRepositoryImpl): IntruderRepository

    @Binds
    @Singleton
    abstract fun bindInstalledAppRepository(impl: InstalledAppRepositoryImpl): InstalledAppRepository
}
