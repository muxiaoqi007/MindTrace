package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.domain.model.MemoryCandidateStatus
import com.mindtrace.diary.domain.repository.AIMemoryCandidateRepository
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ApproveMemoryCandidateUseCase @Inject constructor(
    private val candidateRepository: AIMemoryCandidateRepository,
    private val memoryRepository: AIMemoryRepository
) {
    suspend operator fun invoke(candidateId: String): Result<Unit> {
        return try {
            val candidate = candidateRepository.getCandidateById(candidateId)
                ?: return Result.failure(IllegalArgumentException("候选记忆不存在"))

            if (candidate.status != MemoryCandidateStatus.PENDING) {
                return Result.success(Unit)
            }

            val normalized = candidate.content.trim().lowercase()
            val exists = memoryRepository.getAllActiveMemories().first()
                .any { it.content.trim().lowercase() == normalized }

            if (!exists) {
                memoryRepository.addAutoMemory(
                    content = candidate.content.trim(),
                    category = candidate.category,
                    source = candidate.source ?: "candidate:${candidate.id}",
                    importance = candidate.importance
                )
            }

            candidateRepository.markApproved(candidate.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
