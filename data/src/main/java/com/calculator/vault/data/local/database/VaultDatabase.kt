package com.calculator.vault.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.calculator.vault.data.local.dao.FakeContentDao
import com.calculator.vault.data.local.dao.IntruderLogDao
import com.calculator.vault.data.local.dao.SecureBookmarkDao
import com.calculator.vault.data.local.dao.SecureNoteDao
import com.calculator.vault.data.local.dao.VaultAppDao
import com.calculator.vault.data.local.entity.FakeContentEntity
import com.calculator.vault.data.local.entity.IntruderLogEntity
import com.calculator.vault.data.local.entity.SecureBookmarkEntity
import com.calculator.vault.data.local.entity.SecureNoteEntity
import com.calculator.vault.data.local.entity.VaultAppEntity

@Database(
    entities = [
        VaultAppEntity::class,
        FakeContentEntity::class,
        IntruderLogEntity::class,
        SecureNoteEntity::class,
        SecureBookmarkEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultAppDao(): VaultAppDao
    abstract fun fakeContentDao(): FakeContentDao
    abstract fun intruderLogDao(): IntruderLogDao
    abstract fun secureNoteDao(): SecureNoteDao
    abstract fun secureBookmarkDao(): SecureBookmarkDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS secure_notes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS secure_bookmarks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        url TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
