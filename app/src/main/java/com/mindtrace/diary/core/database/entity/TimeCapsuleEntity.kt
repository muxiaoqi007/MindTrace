package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_capsules")
data class TimeCapsuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val encryptedMessage: String,
    val encryptedPrediction: String,
    val encryptedQuestion: String,
    val mediaUris: List<String>,
    val unlockAt: Long,
    val createdAt: Long,
    val openedAt: Long?
)
