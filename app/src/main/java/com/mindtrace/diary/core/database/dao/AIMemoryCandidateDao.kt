package com.mindtrace.diary.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mindtrace.diary.core.database.entity.AIMemoryCandidateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AIMemoryCandidateDao {

    @Query("SELECT * FROM ai_memory_candidates WHERE status = 'pending' ORDER BY importance DESC, createdAt DESC")
    fun getPendingCandidates(): Flow<List<AIMemoryCandidateEntity>>

    @Query("SELECT * FROM ai_memory_candidates ORDER BY createdAt DESC")
    fun getAllCandidates(): Flow<List<AIMemoryCandidateEntity>>

    @Query("SELECT * FROM ai_memory_candidates WHERE id = :id")
    suspend fun getCandidateById(id: String): AIMemoryCandidateEntity?

    @Query("SELECT COUNT(*) FROM ai_memory_candidates WHERE status = 'pending'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM ai_memory_candidates WHERE status = 'pending' AND source = :source")
    suspend fun getPendingCandidatesBySource(source: String): List<AIMemoryCandidateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: AIMemoryCandidateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidates(candidates: List<AIMemoryCandidateEntity>)

    @Update
    suspend fun updateCandidate(candidate: AIMemoryCandidateEntity)

    @Query("UPDATE ai_memory_candidates SET status = :status, updatedAt = :updatedAt, reviewedAt = :reviewedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long, reviewedAt: Long?)

    @Query("DELETE FROM ai_memory_candidates WHERE id = :id")
    suspend fun deleteCandidate(id: String)

    @Query("DELETE FROM ai_memory_candidates WHERE status != 'pending'")
    suspend fun deleteReviewedCandidates()
}
