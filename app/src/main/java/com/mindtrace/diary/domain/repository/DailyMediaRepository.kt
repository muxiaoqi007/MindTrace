package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.DailyMediaPick
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyMediaRepository {
    fun observeAll(): Flow<List<DailyMediaPick>>
    suspend fun save(pick: DailyMediaPick)
    suspend fun delete(date: LocalDate)
}
