package com.mindtrace.diary.core.database.dao

import androidx.room.*
import com.mindtrace.diary.core.database.entity.AiReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiReviewDao {
    @Query("SELECT * FROM ai_reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<AiReviewEntity>>

    @Query("SELECT * FROM ai_reviews ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentReviews(limit: Int): Flow<List<AiReviewEntity>>

    @Query("SELECT * FROM ai_reviews WHERE id = :id")
    suspend fun getReviewById(id: String): AiReviewEntity?

    @Query("SELECT * FROM ai_reviews WHERE id = :id")
    fun getReviewByIdFlow(id: String): Flow<AiReviewEntity?>

    @Query("SELECT * FROM ai_reviews WHERE date = :date AND type = 'MIDNIGHT_REVIEW' LIMIT 1")
    suspend fun getReviewByDate(date: Long): AiReviewEntity?

    @Query("SELECT * FROM ai_reviews WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadReviews(): Flow<List<AiReviewEntity>>

    @Query("SELECT COUNT(*) FROM ai_reviews WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: AiReviewEntity)

    @Update
    suspend fun updateReview(review: AiReviewEntity)

    @Query("UPDATE ai_reviews SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE ai_reviews SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE ai_reviews SET userReply = :reply, userReplyAt = :replyAt WHERE id = :id")
    suspend fun saveUserReply(id: String, reply: String, replyAt: Long)

    @Query("SELECT * FROM ai_reviews WHERE date = :date AND userReply IS NOT NULL LIMIT 1")
    suspend fun getReviewWithReplyByDate(date: Long): AiReviewEntity?

    @Delete
    suspend fun deleteReview(review: AiReviewEntity)

    @Query("DELETE FROM ai_reviews WHERE id = :id")
    suspend fun deleteReviewById(id: String)
}
