package com.mindtrace.diary.domain.model

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate,
    val hasEntry: Boolean = false,
    val mood: MoodLevel? = null,
    val firstImage: String? = null,
    val entryCount: Int = 0,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isCurrentMonth: Boolean = true
)
