package com.mindtrace.diary.core.ai

import kotlin.math.min

/** Conservative character budget for model-agnostic OpenAI-compatible APIs. */
object AIMessageBudget {
    const val DEFAULT_MAX_MESSAGES = 20
    const val DEFAULT_MAX_CHARS_PER_MESSAGE = 6_000
    const val DEFAULT_MAX_TOTAL_CHARS = 24_000

    fun trim(
        messages: List<ChatMessage>,
        maxMessages: Int = DEFAULT_MAX_MESSAGES,
        maxCharsPerMessage: Int = DEFAULT_MAX_CHARS_PER_MESSAGE,
        maxTotalChars: Int = DEFAULT_MAX_TOTAL_CHARS
    ): List<ChatMessage> {
        require(maxMessages > 0)
        require(maxCharsPerMessage > 0)
        require(maxTotalChars > 0)

        var remaining = maxTotalChars
        val newestFirst = mutableListOf<ChatMessage>()
        for (message in messages.takeLast(maxMessages).asReversed()) {
            if (remaining <= 0) break
            val allowed = min(maxCharsPerMessage, remaining)
            val boundedContent = truncateMiddle(message.content, allowed)
            if (boundedContent.isEmpty()) continue
            newestFirst += message.copy(content = boundedContent)
            remaining -= boundedContent.length
        }
        return newestFirst.asReversed()
    }

    private fun truncateMiddle(value: String, maxLength: Int): String {
        if (value.length <= maxLength) return value
        if (maxLength <= ELLIPSIS.length) return value.take(maxLength)

        val available = maxLength - ELLIPSIS.length
        val headLength = (available + 1) / 2
        val tailLength = available - headLength
        return value.take(headLength) + ELLIPSIS + value.takeLast(tailLength)
    }

    private const val ELLIPSIS = "…"
}
