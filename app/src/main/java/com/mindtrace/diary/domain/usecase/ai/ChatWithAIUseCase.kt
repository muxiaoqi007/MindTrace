package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.core.ai.ChatMessage
import com.mindtrace.diary.core.ai.StreamChunk
import com.mindtrace.diary.domain.repository.AIRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * AI 对话用例
 * 处理与 AI 的对话交互
 */
class ChatWithAIUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    /**
     * 发送消息并获取流式响应
     * @param userMessage 用户消息
     * @param includeContext 是否包含日记上下文
     * @return 流式响应
     */
    operator fun invoke(
        userMessage: String,
        includeContext: Boolean = true
    ): Flow<StreamChunk> {
        val message = ChatMessage(ChatMessage.Role.USER, userMessage)
        val responseBuilder = StringBuilder()

        return aiRepository.chatStream(listOf(message), includeContext)
            .onEach { chunk ->
                if (!chunk.isFinished) {
                    responseBuilder.append(chunk.content)
                }
            }
            .onCompletion { error ->
                if (error == null && responseBuilder.isNotEmpty()) {
                    // 保存用户消息和 AI 响应到历史
                    aiRepository.addToHistory(message)
                    aiRepository.addToHistory(
                        ChatMessage(ChatMessage.Role.ASSISTANT, responseBuilder.toString())
                    )
                }
            }
    }
}
