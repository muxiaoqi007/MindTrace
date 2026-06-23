package com.mindtrace.diary.ui.screens.diary

import com.mindtrace.diary.domain.model.ContentBlock
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.DiaryEntry
import com.mindtrace.diary.domain.model.MoodLevel
import java.time.LocalDate
import java.util.UUID

data class DiaryEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val diary: Diary? = null,
    val title: String = "",
    val content: String = "",
    val images: List<String> = emptyList(),
    val contentBlocks: List<ContentBlock> = listOf(ContentBlock.Text(id = UUID.randomUUID().toString())),
    val mood: MoodLevel? = null,
    val weather: String? = null,
    val location: String? = null,
    val tags: List<String> = emptyList(),
    val entries: List<DiaryEntry> = emptyList(),
    val date: LocalDate? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)

data class DiaryDetailUiState(
    val isLoading: Boolean = true,
    val diary: Diary? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)
