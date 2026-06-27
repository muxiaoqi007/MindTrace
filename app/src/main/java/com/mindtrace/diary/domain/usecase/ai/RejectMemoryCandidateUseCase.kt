package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.domain.model.MemoryCandidateStatus
import com.mindtrace.diary.domain.repository.AIMemoryCandidateRepository
import javax.inject.Inject

class RejectMemoryCandidateUseCase @Inject constructor(
    private val candidateRepository: AIMemoryCandidateRepository
) {
    suspend operator fun invoke(candidateId: String): Result<Unit> {
        return try {
            val candidate = candidateRepository.getCandidateById(candidateId)
                ?: return Result.failure(IllegalArgumentException("候选记忆不存在"))

            if (candidate.status == MemoryCandidateStatus.PENDING) {
                candidateRepository.markRejected(candidate.id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
