package com.calculator.vault.data.repository

import com.calculator.vault.data.local.dao.SecureBookmarkDao
import com.calculator.vault.data.local.dao.SecureNoteDao
import com.calculator.vault.data.mapper.toDomain
import com.calculator.vault.data.mapper.toEntity
import com.calculator.vault.domain.model.SecureBookmark
import com.calculator.vault.domain.model.SecureNote
import com.calculator.vault.domain.repository.SecureBookmarkRepository
import com.calculator.vault.domain.repository.SecureNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureNoteRepositoryImpl @Inject constructor(
    private val secureNoteDao: SecureNoteDao,
) : SecureNoteRepository {
    override fun observeNotes(): Flow<List<SecureNote>> =
        secureNoteDao.observeAll().map { notes -> notes.map { it.toDomain() } }

    override suspend fun upsert(note: SecureNote): Long =
        secureNoteDao.upsert(note.toEntity())

    override suspend fun delete(id: Long) {
        secureNoteDao.delete(id)
    }

    override suspend fun search(query: String): List<SecureNote> =
        secureNoteDao.search(query.trim()).map { it.toDomain() }
}

@Singleton
class SecureBookmarkRepositoryImpl @Inject constructor(
    private val secureBookmarkDao: SecureBookmarkDao,
) : SecureBookmarkRepository {
    override fun observeBookmarks(): Flow<List<SecureBookmark>> =
        secureBookmarkDao.observeAll().map { bookmarks -> bookmarks.map { it.toDomain() } }

    override suspend fun upsert(bookmark: SecureBookmark): Long =
        secureBookmarkDao.upsert(bookmark.toEntity())

    override suspend fun delete(id: Long) {
        secureBookmarkDao.delete(id)
    }
}
