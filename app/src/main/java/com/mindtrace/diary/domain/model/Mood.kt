package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

data class Mood(
    val id: String,
    val moodType: MoodLevel,
    val note: String? = null,
    val date: LocalDateTime,
    val createdAt: LocalDateTime,
    val syncedAt: LocalDateTime? = null,
    val isDeleted: Boolean = false
)
