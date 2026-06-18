package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

sealed class TimelineItem(
    open val id: String,
    open val createdAt: LocalDateTime
) {
    data class DiaryItem(
        override val id: String,
        val title: String,
        val contentPreview: String,
        val firstImage: String?,
        val mood: MoodLevel?,
        override val createdAt: LocalDateTime
    ) : TimelineItem(id, createdAt)

    data class FlashNoteItem(
        override val id: String,
        val content: String,
        val firstImage: String?,
        override val createdAt: LocalDateTime,
        val diaryId: String? = null  // 关联的日记ID，用于删除
    ) : TimelineItem(id, createdAt)

    data class TodoItem(
        override val id: String,
        val content: String,
        val isCompleted: Boolean,
        val dueDate: LocalDateTime?,
        override val createdAt: LocalDateTime
    ) : TimelineItem(id, createdAt)
}
