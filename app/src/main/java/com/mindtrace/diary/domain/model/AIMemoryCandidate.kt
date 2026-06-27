package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

/**
 * AI 候选记忆。
 * 自动学习产生的内容先进入候选箱，用户确认后才写入长期记忆。
 */
data class AIMemoryCandidate(
    val id: String,
    val category: MemoryCategory,
    val content: String,
    val source: String? = null,
    val importance: Float = 0.5f,
    val confidence: Float = 0.5f,
    val evidence: String? = null,
    val reason: String? = null,
    val status: MemoryCandidateStatus = MemoryCandidateStatus.PENDING,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val reviewedAt: LocalDateTime? = null
)

enum class MemoryCandidateStatus {
    PENDING,
    APPROVED,
    REJECTED;

    companion object {
        fun fromString(value: String?): MemoryCandidateStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: PENDING
        }
    }
}
