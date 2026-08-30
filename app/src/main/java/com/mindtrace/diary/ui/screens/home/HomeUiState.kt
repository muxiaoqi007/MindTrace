package com.mindtrace.diary.ui.screens.home

import com.mindtrace.diary.domain.model.DailyInsight
import com.mindtrace.diary.domain.model.TimelineItem

data class HomeUiState(
    val isLoading: Boolean = true,
    val items: List<TimelineItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: FilterType = FilterType.ALL,
    val quickInput: String = "",
    val isAddingItem: Boolean = false,
    val unreadReviewCount: Int = 0,
    /** AI 今日洞察（缓存的当天结果，可能为空表示尚未生成/不可用） */
    val dailyInsight: DailyInsight? = null,
    val insightLoading: Boolean = false,
    /** 生成失败时置位，用于在卡片内提供重试入口 */
    val insightFailed: Boolean = false,
    val isAIConfigured: Boolean = false,
    val error: String? = null
)

enum class FilterType {
    ALL, DIARY, FLASHNOTE, TODO
}
