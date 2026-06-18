package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.AiReview
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * AI 回信仓库接口
 */
interface AiReviewRepository {
    /**
     * 获取所有回信
     */
    fun getAllReviews(): Flow<List<AiReview>>

    /**
     * 获取最近的回信
     */
    fun getRecentReviews(limit: Int): Flow<List<AiReview>>

    /**
     * 根据 ID 获取回信
     */
    suspend fun getReviewById(id: String): AiReview?

    /**
     * 根据 ID 获取回信 Flow
     */
    fun getReviewByIdFlow(id: String): Flow<AiReview?>

    /**
     * 根据日期获取回信
     */
    suspend fun getReviewByDate(date: LocalDate): AiReview?

    /**
     * 获取未读回信
     */
    fun getUnreadReviews(): Flow<List<AiReview>>

    /**
     * 获取未读数量
     */
    fun getUnreadCount(): Flow<Int>

    /**
     * 保存回信
     */
    suspend fun saveReview(review: AiReview)

    /**
     * 标记为已读
     */
    suspend fun markAsRead(id: String)

    /**
     * 标记所有为已读
     */
    suspend fun markAllAsRead()

    /**
     * 保存用户回复
     */
    suspend fun saveUserReply(id: String, reply: String)

    /**
     * 获取指定日期有用户回复的回信
     */
    suspend fun getReviewWithReplyByDate(date: LocalDate): AiReview?

    /**
     * 删除回信
     */
    suspend fun deleteReview(id: String)
}
