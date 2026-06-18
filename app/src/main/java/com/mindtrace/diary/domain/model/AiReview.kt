package com.mindtrace.diary.domain.model

import java.time.LocalDate

/**
 * 回信类型
 */
enum class ReviewType {
    MIDNIGHT_REVIEW,    // 深夜回信
    SILENCE_BREAK       // 沉默唤醒
}

/**
 * AI 回信的领域模型
 */
data class AiReview(
    val id: String,
    val date: LocalDate,         // 回信对应的日期
    val content: String,         // AI 回信内容
    val diaryIds: List<String>,  // 关联的日记 ID 列表
    val persona: MidnightReviewPersona,  // 使用的 Persona
    val isRead: Boolean,
    val createdAt: Long,
    val type: ReviewType = ReviewType.MIDNIGHT_REVIEW,  // 回信类型
    val userReply: String? = null,      // 用户回复
    val userReplyAt: Long? = null       // 用户回复时间
)
