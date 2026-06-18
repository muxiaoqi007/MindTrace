package com.mindtrace.diary.ui.screens.ai

import com.mindtrace.diary.core.ai.ChatMessage
import com.mindtrace.diary.domain.model.AIConversation
import com.mindtrace.diary.domain.model.AiReview

/**
 * AI 聊天 UI 状态
 */
data class AIChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val streamingContent: String = "",
    val error: String? = null,
    val isAIConfigured: Boolean = false,
    val isAIEnabled: Boolean = false,
    val currentConversation: AIConversation? = null,
    val unreadReviews: List<AiReview> = emptyList(),
    val showReviewPanel: Boolean = false,
    val selectedReview: AiReview? = null,
    val replyText: String = ""
)
