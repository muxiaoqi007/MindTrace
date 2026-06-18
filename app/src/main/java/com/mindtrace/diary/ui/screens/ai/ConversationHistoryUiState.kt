package com.mindtrace.diary.ui.screens.ai

import com.mindtrace.diary.domain.model.AIConversation

/**
 * 会话历史 UI 状态
 */
data class ConversationHistoryUiState(
    val conversations: List<AIConversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
