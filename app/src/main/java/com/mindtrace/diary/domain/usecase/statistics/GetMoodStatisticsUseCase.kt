package com.mindtrace.diary.domain.usecase.statistics

import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class MoodStatistics(
    val moodDistribution: Map<MoodLevel, Int>,
    val totalEntries: Int,
    val totalWords: Int,
    val writingDays: Int
)

class GetMoodStatisticsUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<MoodStatistics> {
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()

        return diaryRepository.getDiariesByDateRange(firstDay, lastDay).map { diaries ->
            val moodCounts = diaries
                .mapNotNull { it.mood }
                .groupingBy { it }
                .eachCount()

            val totalWords = diaries.sumOf { it.content.length }
            val writingDays = diaries.map { it.createdAt.toLocalDate() }.distinct().size

            MoodStatistics(
                moodDistribution = moodCounts,
                totalEntries = diaries.size,
                totalWords = totalWords,
                writingDays = writingDays
            )
        }
    }
}
