package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.AiReviewDao
import com.mindtrace.diary.data.mapper.toDomain
import com.mindtrace.diary.data.mapper.toEntity
import com.mindtrace.diary.domain.model.AiReview
import com.mindtrace.diary.domain.repository.AiReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiReviewRepositoryImpl @Inject constructor(
    private val aiReviewDao: AiReviewDao
) : AiReviewRepository {

    override fun getAllReviews(): Flow<List<AiReview>> {
        return aiReviewDao.getAllReviews().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentReviews(limit: Int): Flow<List<AiReview>> {
        return aiReviewDao.getRecentReviews(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReviewById(id: String): AiReview? {
        return aiReviewDao.getReviewById(id)?.toDomain()
    }

    override fun getReviewByIdFlow(id: String): Flow<AiReview?> {
        return aiReviewDao.getReviewByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun getReviewByDate(date: LocalDate): AiReview? {
        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return aiReviewDao.getReviewByDate(timestamp)?.toDomain()
    }

    override fun getUnreadReviews(): Flow<List<AiReview>> {
        return aiReviewDao.getUnreadReviews().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnreadCount(): Flow<Int> {
        return aiReviewDao.getUnreadCount()
    }

    override suspend fun saveReview(review: AiReview) {
        aiReviewDao.insertReview(review.toEntity())
    }

    override suspend fun markAsRead(id: String) {
        aiReviewDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        aiReviewDao.markAllAsRead()
    }

    override suspend fun saveUserReply(id: String, reply: String) {
        aiReviewDao.saveUserReply(id, reply, System.currentTimeMillis())
    }

    override suspend fun getReviewWithReplyByDate(date: LocalDate): AiReview? {
        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return aiReviewDao.getReviewWithReplyByDate(timestamp)?.toDomain()
    }

    override suspend fun deleteReview(id: String) {
        aiReviewDao.deleteReviewById(id)
    }
}
