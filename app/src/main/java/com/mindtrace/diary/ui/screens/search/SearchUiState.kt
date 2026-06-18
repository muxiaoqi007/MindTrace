package com.mindtrace.diary.ui.screens.search

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.FlashNote

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val diaries: List<Diary> = emptyList(),
    val flashNotes: List<FlashNote> = emptyList(),
    val error: String? = null
)
