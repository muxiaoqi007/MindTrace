package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

/**
 * AI 会话
 */
data class AIConversation(
    val id: String,
    val title: String,
    val messages: List<AIMessage>,
    val relatedDiaryId: String? = null,
    val messageCount: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * AI 消息
 */
data class AIMessage(
    val role: Role,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    enum class Role {
        USER, ASSISTANT, SYSTEM;

        fun toApiString(): String = name.lowercase()

        companion object {
            fun fromString(value: String): Role {
                return entries.find { it.name.equals(value, ignoreCase = true) } ?: USER
            }
        }
    }
}
