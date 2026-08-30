package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "daily_media_picks", indices = [Index(value = ["date"], unique = true)])
data class DailyMediaPickEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val uri: String,
    val type: String,
    val createdAt: Long
)
