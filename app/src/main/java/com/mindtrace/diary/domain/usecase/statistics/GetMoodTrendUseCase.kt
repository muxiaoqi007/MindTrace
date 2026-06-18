package com.mindtrace.diary.domain.usecase.statistics

import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * 心情趋势数据点
 */
data class MoodTrendPoint(
    val date: LocalDate,
    val mood: MoodLevel?,
    val moodScore: Float  // 心情分数，用于计算趋势（1-5分）
)

/**
 * 获取最近N天心情趋势的 UseCase
 */
class GetMoodTrendUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    /**
     * @param days 获取最近多少天的数据，默认7天
     */
    operator fun invoke(days: Int = 7): Flow<List<MoodTrendPoint>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong() - 1)

        return diaryRepository.getDiariesByDateRange(startDate, endDate.plusDays(1)).map { diaries ->
            // 按日期分组，取每天最后一条日记的心情
            val moodByDate = diaries
                .sortedBy { it.createdAt }
                .groupBy { it.createdAt.toLocalDate() }
                .mapValues { (_, dayDiaries) -> dayDiaries.lastOrNull()?.mood }

            // 生成所有日期的数据点
            generateSequence(startDate) { it.plusDays(1) }
                .takeWhile { !it.isAfter(endDate) }
                .map { date ->
                    val mood = moodByDate[date]
                    MoodTrendPoint(
                        date = date,
                        mood = mood,
                        moodScore = mood?.score?.toFloat() ?: 0f
                    )
                }
                .toList()
        }
    }
}
