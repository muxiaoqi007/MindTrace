package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.domain.repository.AIRepository
import javax.inject.Inject

/**
 * 清空 AI 对话历史用例
 */
class ClearAIHistoryUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke() {
        aiRepository.clearHistory()
    }
}
