package com.mindtrace.diary.domain.model

import java.time.LocalDate

data class DailyReceipt(
    val date: LocalDate,
    val mood: MoodLevel?,
    val diaryCount: Int,
    val flashNoteCount: Int,
    val completedTodoCount: Int,
    val pendingTodoCount: Int,
    val wordCount: Int,
    val highlight: String?,
    val keywords: List<String>
) {
    val hasContent: Boolean
        get() = diaryCount > 0 || flashNoteCount > 0 ||
            completedTodoCount > 0 || pendingTodoCount > 0
}
