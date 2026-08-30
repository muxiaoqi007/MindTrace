package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.TimeCapsule
import com.mindtrace.diary.domain.model.TimeCapsuleDraft
import com.mindtrace.diary.domain.model.TimeCapsuleSummary
import kotlinx.coroutines.flow.Flow
import java.time.Clock

interface TimeCapsuleRepository {
    fun observeSummaries(): Flow<List<TimeCapsuleSummary>>
    suspend fun seal(draft: TimeCapsuleDraft): String
    suspend fun openIfUnlocked(id: String, clock: Clock = Clock.systemDefaultZone()): TimeCapsule?
    suspend fun delete(id: String)
}
