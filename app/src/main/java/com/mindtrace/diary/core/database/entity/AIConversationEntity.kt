package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * AI 会话历史实体
 */
@Entity(tableName = "ai_conversations")
data class AIConversationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,                    // 会话标题（第一条消息摘要）
    val messages: List<AIMessageData>,    // 消息列表
    val relatedDiaryId: String? = null,   // 关联的日记ID（可选）
    val messageCount: Int = 0,            // 消息数量
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * AI 消息数据
 */
data class AIMessageData(
    val role: String,      // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
