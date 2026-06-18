package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.AIConversationDao
import com.mindtrace.diary.data.mapper.toDomain
import com.mindtrace.diary.data.mapper.toEntity
import com.mindtrace.diary.domain.model.AIConversation
import com.mindtrace.diary.domain.model.AIMessage
import com.mindtrace.diary.domain.repository.AIConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIConversationRepositoryImpl @Inject constructor(
    private val aiConversationDao: AIConversationDao
) : AIConversationRepository {

    override fun getAllConversations(): Flow<List<AIConversation>> {
        return aiConversationDao.getAllConversations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentConversations(limit: Int): Flow<List<AIConversation>> {
        return aiConversationDao.getRecentConversations(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getConversationById(id: String): AIConversation? {
        return aiConversationDao.getConversationById(id)?.toDomain()
    }

    override fun getConversationByIdFlow(id: String): Flow<AIConversation?> {
        return aiConversationDao.getConversationByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun getConversationByDiaryId(diaryId: String): AIConversation? {
        return aiConversationDao.getConversationByDiaryId(diaryId)?.toDomain()
    }

    override suspend fun createConversation(
        title: String,
        relatedDiaryId: String?
    ): AIConversation {
        val now = LocalDateTime.now()
        val conversation = AIConversation(
            id = UUID.randomUUID().toString(),
            title = title,
            messages = emptyList(),
            relatedDiaryId = relatedDiaryId,
            messageCount = 0,
            createdAt = now,
            updatedAt = now
        )
        aiConversationDao.insertConversation(conversation.toEntity())
        return conversation
    }

    override suspend fun addMessage(conversationId: String, message: AIMessage) {
        val conversation = aiConversationDao.getConversationById(conversationId) ?: return
        val domain = conversation.toDomain()

        val updatedMessages = domain.messages + message
        val updatedConversation = domain.copy(
            messages = updatedMessages,
            messageCount = updatedMessages.size,
            // 如果是第一条用户消息，更新标题
            title = if (domain.messages.isEmpty() && message.role == AIMessage.Role.USER) {
                message.content.take(30) + if (message.content.length > 30) "..." else ""
            } else {
                domain.title
            },
            updatedAt = LocalDateTime.now()
        )

        aiConversationDao.updateConversation(updatedConversation.toEntity())
    }

    override suspend fun updateConversation(conversation: AIConversation) {
        aiConversationDao.updateConversation(conversation.toEntity())
    }

    override suspend fun deleteConversation(id: String) {
        aiConversationDao.deleteConversation(id)
    }

    override suspend fun deleteAllConversations() {
        aiConversationDao.deleteAllConversations()
    }

    override fun getConversationCount(): Flow<Int> {
        return aiConversationDao.getConversationCount()
    }
}
