package com.mindtrace.diary.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

enum class DailyMediaType { IMAGE, VIDEO }

data class DailyMediaPick(
    val id: String,
    val date: LocalDate,
    val uri: String,
    val type: DailyMediaType,
    val createdAt: LocalDateTime
)

data class DailyMediaSlot(val date: LocalDate, val pick: DailyMediaPick?)

object DailyMediaTimeline {
    fun month(yearMonth: YearMonth, picks: List<DailyMediaPick>): List<DailyMediaSlot> {
        val byDate = picks.filter { YearMonth.from(it.date) == yearMonth }
            .sortedBy(DailyMediaPick::createdAt)
            .associateBy(DailyMediaPick::date)
        return (1..yearMonth.lengthOfMonth()).map { day ->
            val date = yearMonth.atDay(day)
            DailyMediaSlot(date, byDate[date])
        }
    }
}
