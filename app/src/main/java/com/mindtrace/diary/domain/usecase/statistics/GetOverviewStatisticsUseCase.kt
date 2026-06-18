package com.mindtrace.diary.domain.usecase.statistics

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import com.mindtrace.diary.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * 总览统计数据
 */
data class OverviewStatistics(
    val diaryCount: Int,
    val flashNoteCount: Int,
    val todoCompletedCount: Int,
    val todoPendingCount: Int,
    val moodIndex: Float,  // 心情指数 0-100
    val dominantMood: MoodLevel?,  // 主导心情
    val writingStreak: Int,  // 连续写作天数
    val totalWords: Int
)

/**
 * 获取总览统计数据的 UseCase
 */
class GetOverviewStatisticsUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val flashNoteRepository: FlashNoteRepository,
    private val todoRepository: TodoRepository
) {
    operator fun invoke(): Flow<OverviewStatistics> {
        // 第一组 combine：日记相关数据
        val diaryDataFlow = combine(
            diaryRepository.getDiaryCount(),
            diaryRepository.getTotalWordCount(),
            diaryRepository.getAllDiaries()
        ) { count, words, diaries ->
            Triple(count, words ?: 0, diaries)
        }

        // 第二组 combine：闪念和待办数据
        val otherDataFlow = combine(
            flashNoteRepository.getFlashNoteCount(),
            todoRepository.getCompletedTodoCount(),
            todoRepository.getPendingTodoCount()
        ) { flashCount, completed, pending ->
            Triple(flashCount, completed, pending)
        }

        // 合并两组数据
        return combine(diaryDataFlow, otherDataFlow) { diaryData, otherData ->
            val (diaryCount, totalWords, diaries) = diaryData
            val (flashNoteCount, completedTodos, pendingTodos) = otherData

            // 计算心情指数（最近30天的平均心情）
            val recentDiaries = diaries.take(30)
            val moods = recentDiaries.mapNotNull { it.mood }
            val avgMoodScore = if (moods.isNotEmpty()) {
                moods.map { it.score.toFloat() }.average().toFloat()
            } else 0f
            val moodIndex = (avgMoodScore / 5f) * 100f

            // 找出主导心情
            val dominantMood = moods
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key

            // 计算连续写作天数
            val writingStreak = calculateWritingStreak(
                diaries.map { it.createdAt.toLocalDate() }.distinct().sortedDescending()
            )

            OverviewStatistics(
                diaryCount = diaryCount,
                flashNoteCount = flashNoteCount,
                todoCompletedCount = completedTodos,
                todoPendingCount = pendingTodos,
                moodIndex = moodIndex,
                dominantMood = dominantMood,
                writingStreak = writingStreak,
                totalWords = totalWords
            )
        }
    }

    private fun calculateWritingStreak(sortedDates: List<java.time.LocalDate>): Int {
        if (sortedDates.isEmpty()) return 0

        val today = java.time.LocalDate.now()
        val yesterday = today.minusDays(1)

        // 如果今天或昨天没有写作，streak 为 0
        if (sortedDates.firstOrNull() != today && sortedDates.firstOrNull() != yesterday) {
            return 0
        }

        var streak = 0
        var expectedDate = sortedDates.firstOrNull() ?: return 0

        for (date in sortedDates) {
            if (date == expectedDate) {
                streak++
                expectedDate = date.minusDays(1)
            } else if (date.isBefore(expectedDate)) {
                break
            }
        }

        return streak
    }
}
