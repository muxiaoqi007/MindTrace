package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

data class FlashNote(
    val id: String,
    val content: String,
    val images: List<String> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val syncedAt: LocalDateTime? = null,
    val isDeleted: Boolean = false,
    val excludeFromAI: Boolean = false,
    val excludeFromResurfacing: Boolean = false
)
