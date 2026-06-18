package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.core.ai.ChatMessage
import com.mindtrace.diary.domain.repository.AIRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取 AI 对话历史用例
 */
class GetAIHistoryUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    operator fun invoke(): Flow<List<ChatMessage>> {
        return aiRepository.getConversationHistory()
    }
}
