package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val moodType: String,
    val note: String? = null,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)
