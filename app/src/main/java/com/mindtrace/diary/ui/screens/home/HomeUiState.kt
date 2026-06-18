package com.mindtrace.diary.ui.screens.home

import com.mindtrace.diary.domain.model.TimelineItem

data class HomeUiState(
    val isLoading: Boolean = true,
    val items: List<TimelineItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: FilterType = FilterType.ALL,
    val quickInput: String = "",
    val isAddingItem: Boolean = false,
    val unreadReviewCount: Int = 0,
    val error: String? = null
)

enum class FilterType {
    ALL, DIARY, FLASHNOTE, TODO
}
