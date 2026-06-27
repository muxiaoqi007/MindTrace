package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.AIMemoryCandidate
import com.mindtrace.diary.domain.model.MemoryCategory
import kotlinx.coroutines.flow.Flow

interface AIMemoryCandidateRepository {
    fun getPendingCandidates(): Flow<List<AIMemoryCandidate>>
    fun getAllCandidates(): Flow<List<AIMemoryCandidate>>
    fun getPendingCount(): Flow<Int>

    suspend fun getCandidateById(id: String): AIMemoryCandidate?

    suspend fun getPendingCandidatesBySource(source: String): List<AIMemoryCandidate>

    suspend fun addCandidate(
        content: String,
        category: MemoryCategory,
        source: String?,
        importance: Float = 0.5f
    ): AIMemoryCandidate

    suspend fun addCandidates(candidates: List<AIMemoryCandidate>)
    suspend fun updateCandidate(candidate: AIMemoryCandidate)
    suspend fun markApproved(id: String)
    suspend fun markRejected(id: String)
    suspend fun deleteCandidate(id: String)
    suspend fun deleteReviewedCandidates()
}
