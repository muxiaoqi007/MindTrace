package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ai_memory_candidates")
data class AIMemoryCandidateEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val content: String,
    val subject: String = "",             // 关键人物/事物关键词，确认后随记忆一起转正
    val source: String? = null,
    val importance: Float = 0.5f,
    val confidence: Float = 0.5f,
    val evidence: String? = null,
    val reason: String? = null,
    val status: String = MemoryCandidateStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)

object MemoryCandidateStatus {
    const val PENDING = "pending"
    const val APPROVED = "approved"
    const val REJECTED = "rejected"
}
