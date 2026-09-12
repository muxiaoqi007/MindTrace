package com.mindtrace.diary.ui.screens.home

import com.mindtrace.diary.domain.model.DailyInsight
import com.mindtrace.diary.domain.model.MorningBrief
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
    /** 晨间简报（仅早晨时段在首页展示，其余时段仍展示今日洞察） */
    val morningBrief: MorningBrief? = null,
    val briefLoading: Boolean = false,
    /** 简报完全生成失败（连本地拼装也失败）时置位 */
    val briefFailed: Boolean = false,
    /** 今天在简报卡写下的"每日意图"，当晚由深夜回信回收对照 */
    val todayIntention: String? = null,
    /** 底部快捷输入是否处于"问 AI"模式 */
    val isAskAIMode: Boolean = false,
    val isAIConfigured: Boolean = false,
    val error: String? = null
)

enum class FilterType {
    ALL, DIARY, FLASHNOTE, TODO
}
