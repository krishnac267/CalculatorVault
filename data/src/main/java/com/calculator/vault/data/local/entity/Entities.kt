package com.calculator.vault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_apps")
data class VaultAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val isFavorite: Boolean = false,
    val isFake: Boolean = false,
    val lastOpenedAt: Long? = null,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "fake_content")
data class FakeContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val type: String,
)

@Entity(tableName = "intruder_logs")
data class IntruderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val photoPath: String?,
    val attemptCount: Int,
)
