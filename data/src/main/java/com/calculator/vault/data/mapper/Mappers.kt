package com.calculator.vault.data.mapper

import com.calculator.vault.data.local.entity.FakeContentEntity
import com.calculator.vault.data.local.entity.IntruderLogEntity
import com.calculator.vault.data.local.entity.SecureBookmarkEntity
import com.calculator.vault.data.local.entity.SecureNoteEntity
import com.calculator.vault.data.local.entity.VaultAppEntity
import com.calculator.vault.domain.model.FakeContent
import com.calculator.vault.domain.model.FakeContentType
import com.calculator.vault.domain.model.IntruderLog
import com.calculator.vault.domain.model.SecureBookmark
import com.calculator.vault.domain.model.SecureNote
import com.calculator.vault.domain.model.VaultApp

fun VaultAppEntity.toDomain() = VaultApp(
    id = id,
    packageName = packageName,
    appName = appName,
    isFavorite = isFavorite,
    isFake = isFake,
    lastOpenedAt = lastOpenedAt,
    addedAt = addedAt,
)

fun VaultApp.toEntity() = VaultAppEntity(
    id = id,
    packageName = packageName,
    appName = appName,
    isFavorite = isFavorite,
    isFake = isFake,
    lastOpenedAt = lastOpenedAt,
    addedAt = addedAt,
)

fun FakeContentEntity.toDomain() = FakeContent(
    id = id,
    title = title,
    subtitle = subtitle,
    type = FakeContentType.valueOf(type),
)

fun FakeContent.toEntity() = FakeContentEntity(
    id = id,
    title = title,
    subtitle = subtitle,
    type = type.name,
)

fun IntruderLogEntity.toDomain() = IntruderLog(
    id = id,
    timestamp = timestamp,
    photoPath = photoPath,
    attemptCount = attemptCount,
)

fun IntruderLog.toEntity() = IntruderLogEntity(
    id = id,
    timestamp = timestamp,
    photoPath = photoPath,
    attemptCount = attemptCount,
)

fun SecureNoteEntity.toDomain() = SecureNote(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun SecureNote.toEntity() = SecureNoteEntity(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun SecureBookmarkEntity.toDomain() = SecureBookmark(
    id = id,
    title = title,
    url = url,
    createdAt = createdAt,
)

fun SecureBookmark.toEntity() = SecureBookmarkEntity(
    id = id,
    title = title,
    url = url,
    createdAt = createdAt,
)
