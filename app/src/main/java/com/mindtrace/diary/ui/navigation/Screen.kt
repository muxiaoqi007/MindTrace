package com.mindtrace.diary.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Home : Screen(
        route = "home",
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Calendar : Screen(
        route = "calendar",
        title = "日历",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )

    object Statistics : Screen(
        route = "statistics",
        title = "统计",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )

    object Settings : Screen(
        route = "settings",
        title = "设置",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    object DiaryEdit : Screen(
        route = "diary/edit?id={id}",
        title = "编辑日记"
    ) {
        fun createRoute(id: String? = null): String {
            return if (id != null) "diary/edit?id=$id" else "diary/edit"
        }
    }

    object DiaryDetail : Screen(
        route = "diary/{id}",
        title = "日记详情"
    ) {
        fun createRoute(id: String): String = "diary/$id"
    }

    object History : Screen(
        route = "history",
        title = "历史上的今天"
    )

    object Tags : Screen(
        route = "tags",
        title = "标签管理"
    )

    object TagDiaries : Screen(
        route = "tags/{tag}",
        title = "标签日记"
    ) {
        fun createRoute(tag: String): String = "tags/$tag"
    }

    object Search : Screen(
        route = "search",
        title = "搜索"
    )

    object DailyReceipt : Screen(
        route = "receipt",
        title = "每日小票"
    )

    object MemoryWalk : Screen(
        route = "memory-walk",
        title = "随机漫步"
    )

    object DailyMaterial : Screen(
        route = "daily-material",
        title = "今日素材篮"
    )

    object LifeFacets : Screen(
        route = "life-facets",
        title = "生活切面"
    )

    object TimeCapsules : Screen(
        route = "time-capsules",
        title = "时光胶囊"
    )

    object WeeklyMagazine : Screen(
        route = "weekly-magazine",
        title = "每周生活杂志"
    )

    object Storylines : Screen(
        route = "storylines",
        title = "人生故事线"
    )

    object PersonalLexicon : Screen(
        route = "personal-lexicon",
        title = "我的词典"
    )

    object OneSecondLife : Screen(
        route = "one-second-life",
        title = "一秒人生"
    )

    object MapFootprints : Screen(
        route = "map-footprints",
        title = "地图足迹"
    )

    object PrintArchive : Screen(
        route = "print-archive",
        title = "打印归档"
    )

    object AIChat : Screen(
        route = "ai/chat?conversationId={conversationId}&seed={seed}",
        title = "AI 伙伴"
    ) {
        fun createRoute(conversationId: String? = null, seed: String? = null): String {
            val params = mutableListOf<String>()
            conversationId?.let { params.add("conversationId=$it") }
            seed?.let { params.add("seed=${Uri.encode(it)}") }
            return if (params.isEmpty()) "ai/chat" else "ai/chat?${params.joinToString("&")}"
        }
    }

    object AISettings : Screen(
        route = "ai/settings",
        title = "AI 设置"
    )

    object AIConversationHistory : Screen(
        route = "ai/history",
        title = "会话历史"
    )

    object AIMemoryManagement : Screen(
        route = "ai/memory",
        title = "记忆管理"
    )

    object AIMemoryCenter : Screen(
        route = "ai/memory/center",
        title = "记忆中心"
    )

    object AIMemoryInbox : Screen(
        route = "ai/memory/inbox",
        title = "候选记忆"
    )

    object AISoulConfig : Screen(
        route = "ai/soul",
        title = "Soul 设置"
    )

    object AISelfPortrait : Screen(
        route = "ai/self-portrait",
        title = "自我画像"
    )

    object AiReviewList : Screen(
        route = "ai/reviews",
        title = "回信历史"
    )

    object AiReviewDetail : Screen(
        route = "ai/review/{reviewId}",
        title = "回信详情"
    ) {
        fun createRoute(reviewId: String): String = "ai/review/$reviewId"
    }

    object WebDAVSettings : Screen(
        route = "webdav/settings",
        title = "WebDAV 同步"
    )

    companion object {
        // 必须惰性求值：若在任一 Screen 子对象自身初始化期间（即首次访问该子对象时
        // 触发基类 <clinit>）急切求值，读到的 INSTANCE 尚未赋值，列表会捕获 null 并
        // 导致 BottomNavBar 启动即 NPE。
        val bottomNavItems: List<Screen> by lazy { listOf(Home, Calendar, Statistics, Settings) }
    }
}
