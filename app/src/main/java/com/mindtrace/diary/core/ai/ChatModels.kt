package com.mindtrace.diary.core.ai

/**
 * AI 对话消息
 */
data class ChatMessage(
    val role: Role,
    val content: String
) {
    enum class Role {
        SYSTEM,
        USER,
        ASSISTANT;

        fun toApiString(): String = name.lowercase()
    }
}

/**
 * AI 响应
 */
data class ChatResponse(
    val content: String,
    val finishReason: String? = null
)

/**
 * 流式响应块
 */
data class StreamChunk(
    val content: String,
    val isFinished: Boolean = false
)
