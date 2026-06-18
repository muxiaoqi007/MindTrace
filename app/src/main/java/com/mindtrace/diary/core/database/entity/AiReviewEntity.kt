package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ai_reviews")
data class AiReviewEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long,              // 日期时间戳（当天开始时间）
    val content: String,         // AI 回信内容
    val diaryIds: String,        // JSON 数组，关联的日记 ID
    val persona: String,         // 使用的 Persona ID
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val type: String = "MIDNIGHT_REVIEW",  // 回信类型: MIDNIGHT_REVIEW 或 SILENCE_BREAK
    val userReply: String? = null,         // 用户回复
    val userReplyAt: Long? = null          // 用户回复时间
)
