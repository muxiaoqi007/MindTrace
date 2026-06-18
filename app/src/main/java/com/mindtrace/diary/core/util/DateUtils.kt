package com.mindtrace.diary.core.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm")
    private val monthDayFormatter = DateTimeFormatter.ofPattern("MM-dd")
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

    fun formatDate(dateTime: LocalDateTime): String = dateTime.format(dateFormatter)

    fun formatTime(dateTime: LocalDateTime): String = dateTime.format(timeFormatter)

    fun formatDateTime(dateTime: LocalDateTime): String = dateTime.format(dateTimeFormatter)

    fun formatRelativeDate(dateTime: LocalDateTime): String {
        val today = LocalDate.now()
        val date = dateTime.toLocalDate()

        return when {
            date == today -> "今天 ${formatTime(dateTime)}"
            date == today.minusDays(1) -> "昨天 ${formatTime(dateTime)}"
            date == today.minusDays(2) -> "前天 ${formatTime(dateTime)}"
            date.year == today.year -> "${date.monthValue}月${date.dayOfMonth}日 ${formatTime(dateTime)}"
            else -> formatDateTime(dateTime)
        }
    }

    fun getMonthDay(dateTime: LocalDateTime): String = dateTime.format(monthDayFormatter)

    fun getYear(dateTime: LocalDateTime): String = dateTime.format(yearFormatter)

    fun getYearsAgo(dateTime: LocalDateTime): Int {
        return LocalDate.now().year - dateTime.year
    }

    fun getChineseDayOfWeek(date: LocalDate): String {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }

    fun getChineseMonth(yearMonth: YearMonth): String {
        return "${yearMonth.year}年${yearMonth.monthValue}月"
    }

    fun toEpochMillis(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun fromEpochMillis(millis: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    }

    fun getStartOfDay(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfDay(date: LocalDate): Long {
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStartOfMonth(yearMonth: YearMonth): Long {
        return yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfMonth(yearMonth: YearMonth): Long {
        return yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
