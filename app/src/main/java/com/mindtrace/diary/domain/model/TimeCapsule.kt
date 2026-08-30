package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

data class TimeCapsuleDraft(
    val title: String,
    val message: String,
    val prediction: String = "",
    val question: String = "",
    val mediaUris: List<String> = emptyList(),
    val unlockAt: LocalDateTime
)

data class TimeCapsuleSummary(
    val id: String,
    val title: String,
    val mediaCount: Int,
    val unlockAt: LocalDateTime,
    val createdAt: LocalDateTime,
    val openedAt: LocalDateTime?
)

data class TimeCapsule(
    val id: String,
    val title: String,
    val message: String,
    val prediction: String,
    val question: String,
    val mediaUris: List<String>,
    val unlockAt: LocalDateTime,
    val createdAt: LocalDateTime,
    val openedAt: LocalDateTime?
)
