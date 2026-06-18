package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.AIConversation
import com.mindtrace.diary.domain.model.AIMessage
import kotlinx.coroutines.flow.Flow

/**
 * AI 会话仓库接口
 */
interface AIConversationRepository {
    /**
     * 获取所有会话
     */
    fun getAllConversations(): Flow<List<AIConversation>>

    /**
     * 获取最近的会话
     */
    fun getRecentConversations(limit: Int): Flow<List<AIConversation>>

    /**
     * 根据 ID 获取会话
     */
    suspend fun getConversationById(id: String): AIConversation?

    /**
     * 获取会话 Flow
     */
    fun getConversationByIdFlow(id: String): Flow<AIConversation?>

    /**
     * 根据日记 ID 获取关联会话
     */
    suspend fun getConversationByDiaryId(diaryId: String): AIConversation?

    /**
     * 创建新会话
     */
    suspend fun createConversation(
        title: String = "新对话",
        relatedDiaryId: String? = null
    ): AIConversation

    /**
     * 添加消息到会话
     */
    suspend fun addMessage(conversationId: String, message: AIMessage)

    /**
     * 更新会话
     */
    suspend fun updateConversation(conversation: AIConversation)

    /**
     * 删除会话
     */
    suspend fun deleteConversation(id: String)

    /**
     * 删除所有会话
     */
    suspend fun deleteAllConversations()

    /**
     * 获取会话数量
     */
    fun getConversationCount(): Flow<Int>
}
