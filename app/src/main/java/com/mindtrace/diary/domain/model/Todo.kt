package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

data class Todo(
    val id: String,
    val content: String,
    val isCompleted: Boolean = false,
    val dueDate: LocalDateTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime? = null,
    val syncedAt: LocalDateTime? = null,
    val isDeleted: Boolean = false
)
