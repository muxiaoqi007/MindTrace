package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "storylines", indices = [Index(value = ["normalizedName"], unique = true)])
data class StorylineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val normalizedName: String,
    val type: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "storyline_sources", indices = [Index(value = ["storylineId", "diaryId"], unique = true)])
data class StorylineSourceEntity(
    @PrimaryKey val id: String,
    val storylineId: String,
    val diaryId: String,
    val date: Long,
    val excerpt: String
)
