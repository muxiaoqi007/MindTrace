package com.mindtrace.diary.domain.usecase.calendar

import com.mindtrace.diary.domain.model.CalendarDay
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetCalendarDaysUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<List<CalendarDay>> {
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        val today = LocalDate.now()

        return diaryRepository.getDiariesByDateRange(firstDayOfMonth, lastDayOfMonth).map { diaries ->
            val diariesByDate = diaries.groupBy { it.createdAt.toLocalDate() }

            val days = mutableListOf<CalendarDay>()
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

            // Add days from previous month to fill the first week
            val prevMonth = yearMonth.minusMonths(1)
            val lastDayOfPrevMonth = prevMonth.atEndOfMonth()
            for (i in firstDayOfWeek - 1 downTo 0) {
                val date = lastDayOfPrevMonth.minusDays(i.toLong())
                days.add(
                    CalendarDay(
                        date = date,
                        isCurrentMonth = false,
                        isToday = date == today
                    )
                )
            }

            // Add days of current month
            for (day in 1..lastDayOfMonth.dayOfMonth) {
                val date = yearMonth.atDay(day)
                val dayDiaries = diariesByDate[date] ?: emptyList()
                val firstDiary = dayDiaries.firstOrNull()

                days.add(
                    CalendarDay(
                        date = date,
                        hasEntry = dayDiaries.isNotEmpty(),
                        mood = firstDiary?.mood,
                        firstImage = firstDiary?.images?.firstOrNull(),
                        entryCount = dayDiaries.size,
                        isToday = date == today,
                        isCurrentMonth = true
                    )
                )
            }

            // Add days from next month to complete the last week
            val remaining = 42 - days.size
            for (i in 1..remaining) {
                val date = lastDayOfMonth.plusDays(i.toLong())
                days.add(
                    CalendarDay(
                        date = date,
                        isCurrentMonth = false,
                        isToday = date == today
                    )
                )
            }

            days
        }
    }
}
