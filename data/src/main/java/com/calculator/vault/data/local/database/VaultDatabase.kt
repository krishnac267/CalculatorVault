package com.calculator.vault.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.calculator.vault.data.local.dao.FakeContentDao
import com.calculator.vault.data.local.dao.IntruderLogDao
import com.calculator.vault.data.local.dao.VaultAppDao
import com.calculator.vault.data.local.entity.FakeContentEntity
import com.calculator.vault.data.local.entity.IntruderLogEntity
import com.calculator.vault.data.local.entity.VaultAppEntity

@Database(
    entities = [
        VaultAppEntity::class,
        FakeContentEntity::class,
        IntruderLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultAppDao(): VaultAppDao
    abstract fun fakeContentDao(): FakeContentDao
    abstract fun intruderLogDao(): IntruderLogDao
}
