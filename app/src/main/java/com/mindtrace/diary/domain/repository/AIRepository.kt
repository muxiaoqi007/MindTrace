package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.core.ai.ChatMessage
import com.mindtrace.diary.core.ai.ChatResponse
import com.mindtrace.diary.core.ai.StreamChunk
import kotlinx.coroutines.flow.Flow

/**
 * AI 仓库接口
 * 管理 AI 对话和上下文
 */
interface AIRepository {
    /**
     * 发送消息并获取 AI 响应（非流式）
     */
    suspend fun chat(
        messages: List<ChatMessage>,
        includeContext: Boolean = true
    ): Result<ChatResponse>

    /**
     * 发送消息并获取 AI 响应（流式）
     */
    fun chatStream(
        messages: List<ChatMessage>,
        includeContext: Boolean = true
    ): Flow<StreamChunk>

    /**
     * 测试 AI 连接
     */
    suspend fun testConnection(): Boolean

}
