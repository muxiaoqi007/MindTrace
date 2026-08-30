package com.mindtrace.diary.domain.model

import com.mindtrace.diary.core.database.entity.DiaryEntity
import com.mindtrace.diary.core.database.entity.FlashNoteEntity
import com.mindtrace.diary.core.database.entity.TodoEntity
import com.mindtrace.diary.core.database.entity.DailyMediaPickEntity
import com.mindtrace.diary.core.database.entity.FacetCheckInEntity
import com.mindtrace.diary.core.database.entity.LexiconEntryEntity
import com.mindtrace.diary.core.database.entity.LexiconEvidenceEntity
import com.mindtrace.diary.core.database.entity.LifeFacetEntity
import com.mindtrace.diary.core.database.entity.StorylineEntity
import com.mindtrace.diary.core.database.entity.StorylineSourceEntity
import com.mindtrace.diary.core.database.entity.TimeCapsuleEntity

/**
 * 导出数据模型
 * 用于数据备份和恢复
 */
data class ExportData(
    val version: Int = 1,
    val exportTime: Long = System.currentTimeMillis(),
    val diaries: List<DiaryEntity> = emptyList(),
    val flashNotes: List<FlashNoteEntity> = emptyList(),
    val todos: List<TodoEntity> = emptyList(),
    val lifeFacets: List<LifeFacetEntity> = emptyList(),
    val facetCheckIns: List<FacetCheckInEntity> = emptyList(),
    val timeCapsules: List<TimeCapsuleEntity> = emptyList(),
    val storylines: List<StorylineEntity> = emptyList(),
    val storylineSources: List<StorylineSourceEntity> = emptyList(),
    val lexiconEntries: List<LexiconEntryEntity> = emptyList(),
    val lexiconEvidence: List<LexiconEvidenceEntity> = emptyList(),
    val dailyMediaPicks: List<DailyMediaPickEntity> = emptyList()
)
