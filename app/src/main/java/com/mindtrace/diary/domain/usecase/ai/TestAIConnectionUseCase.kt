package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.domain.repository.AIRepository
import javax.inject.Inject

/**
 * 测试 AI 连接用例
 */
class TestAIConnectionUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(): Boolean {
        return aiRepository.testConnection()
    }
}
