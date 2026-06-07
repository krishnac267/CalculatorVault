package com.calculator.vault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calculator.vault.data.local.entity.FakeContentEntity
import com.calculator.vault.data.local.entity.IntruderLogEntity
import com.calculator.vault.data.local.entity.SecureBookmarkEntity
import com.calculator.vault.data.local.entity.SecureNoteEntity
import com.calculator.vault.data.local.entity.VaultAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultAppDao {
    @Query("SELECT * FROM vault_apps WHERE isFake = 0 ORDER BY appName ASC")
    fun observeRealApps(): Flow<List<VaultAppEntity>>

    @Query("SELECT * FROM vault_apps WHERE isFake = 1 ORDER BY appName ASC")
    fun observeFakeApps(): Flow<List<VaultAppEntity>>

    @Query("SELECT * FROM vault_apps WHERE isFavorite = 1 AND isFake = 0 ORDER BY appName ASC")
    fun observeFavorites(): Flow<List<VaultAppEntity>>

    @Query("SELECT * FROM vault_apps WHERE lastOpenedAt IS NOT NULL AND isFake = 0 ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<VaultAppEntity>>

    @Query("SELECT * FROM vault_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): VaultAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: VaultAppEntity)

    @Query("DELETE FROM vault_apps WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("UPDATE vault_apps SET isFavorite = NOT isFavorite WHERE packageName = :packageName")
    suspend fun toggleFavorite(packageName: String)

    @Query("UPDATE vault_apps SET lastOpenedAt = :timestamp WHERE packageName = :packageName")
    suspend fun updateLastOpened(packageName: String, timestamp: Long)

    @Query("SELECT * FROM vault_apps")
    suspend fun getAll(): List<VaultAppEntity>

    @Query("DELETE FROM vault_apps")
    suspend fun deleteAll()
}

@Dao
interface FakeContentDao {
    @Query("SELECT * FROM fake_content ORDER BY id ASC")
    fun observeAll(): Flow<List<FakeContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FakeContentEntity>)

    @Query("SELECT COUNT(*) FROM fake_content")
    suspend fun count(): Int

    @Query("SELECT * FROM fake_content")
    suspend fun getAll(): List<FakeContentEntity>

    @Query("DELETE FROM fake_content")
    suspend fun deleteAll()
}

@Dao
interface IntruderLogDao {
    @Query("SELECT * FROM intruder_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<IntruderLogEntity>>

    @Insert
    suspend fun insert(log: IntruderLogEntity)

    @Query("DELETE FROM intruder_logs")
    suspend fun clearAll()
}

@Dao
interface SecureNoteDao {
    @Query("SELECT * FROM secure_notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SecureNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: SecureNoteEntity): Long

    @Query("DELETE FROM secure_notes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM secure_notes WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    suspend fun search(query: String): List<SecureNoteEntity>

    @Query("SELECT * FROM secure_notes")
    suspend fun getAll(): List<SecureNoteEntity>

    @Query("DELETE FROM secure_notes")
    suspend fun deleteAll()
}

@Dao
interface SecureBookmarkDao {
    @Query("SELECT * FROM secure_bookmarks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SecureBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: SecureBookmarkEntity): Long

    @Query("DELETE FROM secure_bookmarks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM secure_bookmarks")
    suspend fun getAll(): List<SecureBookmarkEntity>

    @Query("DELETE FROM secure_bookmarks")
    suspend fun deleteAll()
}
