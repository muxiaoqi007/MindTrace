package com.mindtrace.diary.domain.model

import com.mindtrace.diary.core.database.entity.DiaryEntity
import com.mindtrace.diary.core.database.entity.FlashNoteEntity
import com.mindtrace.diary.core.database.entity.TodoEntity

/**
 * 导出数据模型
 * 用于数据备份和恢复
 */
data class ExportData(
    val version: Int = 1,
    val exportTime: Long = System.currentTimeMillis(),
    val diaries: List<DiaryEntity> = emptyList(),
    val flashNotes: List<FlashNoteEntity> = emptyList(),
    val todos: List<TodoEntity> = emptyList()
)
