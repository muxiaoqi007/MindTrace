package com.mindtrace.diary.ui.navigation

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

    object FlashNoteAdd : Screen(
        route = "flashnote/add",
        title = "添加闪念"
    )

    object TodoList : Screen(
        route = "todo",
        title = "待办事项"
    )

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

    object AIChat : Screen(
        route = "ai/chat?conversationId={conversationId}",
        title = "AI 伙伴"
    ) {
        fun createRoute(conversationId: String? = null): String {
            return if (conversationId != null) "ai/chat?conversationId=$conversationId" else "ai/chat"
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
        val bottomNavItems = listOf(Home, Calendar, Statistics, Settings)
    }
}
