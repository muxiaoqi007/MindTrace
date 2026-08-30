package com.mindtrace.diary.domain.model

import java.time.LocalDate

data class WeeklyMoodDay(val date: LocalDate, val averageScore: Float?)

data class WeeklyMagazine(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val moodStrip: List<WeeklyMoodDay>,
    val topMoments: List<String>,
    val flashExcerpts: List<String>,
    val completedGoals: List<String>,
    val unresolvedThreads: List<String>,
    val diaryCount: Int,
    val flashCount: Int
) {
    val hasContent: Boolean
        get() = diaryCount > 0 || flashCount > 0 || completedGoals.isNotEmpty() || unresolvedThreads.isNotEmpty()
}
