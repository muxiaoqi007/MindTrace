package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.AIMemoryCandidateDao
import com.mindtrace.diary.data.mapper.toDomain
import com.mindtrace.diary.data.mapper.toEntity
import com.mindtrace.diary.domain.model.AIMemoryCandidate
import com.mindtrace.diary.domain.model.MemoryCandidateStatus
import com.mindtrace.diary.domain.model.MemoryCategory
import com.mindtrace.diary.domain.repository.AIMemoryCandidateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIMemoryCandidateRepositoryImpl @Inject constructor(
    private val dao: AIMemoryCandidateDao
) : AIMemoryCandidateRepository {

    override fun getPendingCandidates(): Flow<List<AIMemoryCandidate>> {
        return dao.getPendingCandidates().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAllCandidates(): Flow<List<AIMemoryCandidate>> {
        return dao.getAllCandidates().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getPendingCount(): Flow<Int> {
        return dao.getPendingCount()
    }

    override suspend fun getCandidateById(id: String): AIMemoryCandidate? {
        return dao.getCandidateById(id)?.toDomain()
    }

    override suspend fun getPendingCandidatesBySource(source: String): List<AIMemoryCandidate> {
        return dao.getPendingCandidatesBySource(source).map { it.toDomain() }
    }

    override suspend fun addCandidate(
        content: String,
        category: MemoryCategory,
        source: String?,
        importance: Float,
        confidence: Float,
        evidence: String?,
        reason: String?
    ): AIMemoryCandidate {
        val now = LocalDateTime.now()
        val candidate = AIMemoryCandidate(
            id = UUID.randomUUID().toString(),
            category = category,
            content = content,
            source = source,
            importance = importance.coerceIn(0f, 1f),
            confidence = confidence.coerceIn(0f, 1f),
            evidence = evidence?.takeIf { it.isNotBlank() },
            reason = reason?.takeIf { it.isNotBlank() },
            status = MemoryCandidateStatus.PENDING,
            createdAt = now,
            updatedAt = now
        )
        dao.insertCandidate(candidate.toEntity())
        return candidate
    }

    override suspend fun addCandidates(candidates: List<AIMemoryCandidate>) {
        dao.insertCandidates(candidates.map { it.toEntity() })
    }

    override suspend fun updateCandidate(candidate: AIMemoryCandidate) {
        dao.updateCandidate(candidate.copy(updatedAt = LocalDateTime.now()).toEntity())
    }

    override suspend fun markApproved(id: String) {
        markStatus(id, MemoryCandidateStatus.APPROVED)
    }

    override suspend fun markRejected(id: String) {
        markStatus(id, MemoryCandidateStatus.REJECTED)
    }

    override suspend fun deleteCandidate(id: String) {
        dao.deleteCandidate(id)
    }

    override suspend fun deleteReviewedCandidates() {
        dao.deleteReviewedCandidates()
    }

    private suspend fun markStatus(id: String, status: MemoryCandidateStatus) {
        val now = System.currentTimeMillis()
        dao.updateStatus(
            id = id,
            status = status.name.lowercase(),
            updatedAt = now,
            reviewedAt = now
        )
    }
}
