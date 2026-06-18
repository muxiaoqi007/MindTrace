package com.mindtrace.diary.core.ai

import kotlinx.coroutines.flow.Flow

/**
 * LLM Provider 接口
 * 定义与大语言模型交互的抽象接口
 */
interface LLMProvider {
    /**
     * 发送聊天请求（非流式）
     */
    suspend fun chat(
        messages: List<ChatMessage>,
        model: String? = null
    ): Result<ChatResponse>

    /**
     * 发送聊天请求（流式）
     */
    fun chatStream(
        messages: List<ChatMessage>,
        model: String? = null
    ): Flow<StreamChunk>

    /**
     * 测试连接
     */
    suspend fun testConnection(): Boolean
}
