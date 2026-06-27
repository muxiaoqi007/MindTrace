package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.domain.model.AIMemory
import com.mindtrace.diary.domain.model.MemoryCategory
import com.mindtrace.diary.domain.model.SelfPortrait
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class GetSelfPortraitUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val memoryRepository: AIMemoryRepository
) {
    suspend operator fun invoke(): SelfPortrait {
        val diaries = diaryRepository.getAllDiaries().first()
        val memories = memoryRepository.getAllActiveMemories().first()

        val totalWords = diaries.sumOf { it.content.length }
        val topTags = diaries
            .flatMap { it.tags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(8)
            .map { it.key to it.value }

        val dominantMoodName = diaries
            .mapNotNull { it.mood }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?.label

        return SelfPortrait(
            totalDiaries = diaries.size,
            totalWords = totalWords,
            writingStreak = calculateWritingStreak(diaries.map { it.date ?: it.createdAt.toLocalDate() }.toSet()),
            dominantMoodName = dominantMoodName,
            topTags = topTags,
            personalityMemories = memories.byCategory(MemoryCategory.PERSONALITY),
            preferenceMemories = memories.byCategory(MemoryCategory.PREFERENCE),
            goalMemories = memories.byCategory(MemoryCategory.GOAL),
            relationshipMemories = memories.byCategory(MemoryCategory.RELATIONSHIP),
            factMemories = memories.byCategory(MemoryCategory.FACT),
            recentMemoryCount = memories.count { it.createdAt.isAfter(LocalDateTime.now().minusDays(30)) },
            updatedAt = LocalDateTime.now()
        )
    }

    private fun List<AIMemory>.byCategory(category: MemoryCategory): List<AIMemory> {
        return filter { it.category == category }
            .sortedByDescending { it.importance }
            .take(5)
    }

    private fun calculateWritingStreak(dates: Set<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        var streak = 0
        var cursor = LocalDate.now()
        while (dates.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
