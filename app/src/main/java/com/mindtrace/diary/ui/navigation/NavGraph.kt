package com.mindtrace.diary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mindtrace.diary.ui.screens.calendar.CalendarScreen
import com.mindtrace.diary.ui.screens.diary.DiaryDetailScreen
import com.mindtrace.diary.ui.screens.diary.DiaryEditScreen
import com.mindtrace.diary.ui.screens.flashnote.FlashNoteAddScreen
import com.mindtrace.diary.ui.screens.history.HistoryScreen
import com.mindtrace.diary.ui.screens.home.HomeScreen
import com.mindtrace.diary.ui.screens.search.SearchScreen
import com.mindtrace.diary.ui.screens.settings.SettingsScreen
import com.mindtrace.diary.ui.screens.statistics.StatisticsScreen
import com.mindtrace.diary.ui.screens.tags.TagDiariesScreen
import com.mindtrace.diary.ui.screens.tags.TagsScreen
import com.mindtrace.diary.ui.screens.todo.TodoScreen
import com.mindtrace.diary.ui.screens.ai.AIChatScreen
import com.mindtrace.diary.ui.screens.ai.AISettingsScreen
import com.mindtrace.diary.ui.screens.ai.ConversationHistoryScreen
import com.mindtrace.diary.ui.screens.ai.MemoryManagementScreen
import com.mindtrace.diary.ui.screens.webdav.WebDAVSettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onDiaryClick = { id ->
                    navController.navigate(Screen.DiaryDetail.createRoute(id))
                },
                onAddDiary = {
                    navController.navigate(Screen.DiaryEdit.createRoute())
                },
                onAddFlashNote = {
                    navController.navigate(Screen.FlashNoteAdd.route)
                },
                onAddTodo = {
                    navController.navigate(Screen.TodoList.route)
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                onAIChatClick = {
                    navController.navigate(Screen.AIChat.createRoute())
                }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onDiaryClick = { id ->
                    navController.navigate(Screen.DiaryDetail.createRoute(id))
                },
                onAddDiary = { date ->
                    navController.navigate(Screen.DiaryEdit.createRoute())
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onHistoryClick = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onTagsClick = {
                    navController.navigate(Screen.Tags.route)
                },
                onAISettingsClick = {
                    navController.navigate(Screen.AISettings.route)
                },
                onWebDAVSettingsClick = {
                    navController.navigate(Screen.WebDAVSettings.route)
                },
                onMidnightReviewHistoryClick = {
                    navController.navigate(Screen.AiReviewList.route)
                }
            )
        }

        composable(
            route = Screen.DiaryEdit.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val diaryId = backStackEntry.arguments?.getString("id")
            DiaryEditScreen(
                diaryId = diaryId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.DiaryDetail.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val diaryId = backStackEntry.arguments?.getString("id") ?: return@composable
            DiaryDetailScreen(
                diaryId = diaryId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEdit = {
                    navController.navigate(Screen.DiaryEdit.createRoute(diaryId))
                }
            )
        }

        composable(Screen.FlashNoteAdd.route) {
            FlashNoteAddScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TodoList.route) {
            TodoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onDiaryClick = { id ->
                    navController.navigate(Screen.DiaryDetail.createRoute(id))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Tags.route) {
            TagsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTagClick = { tag ->
                    navController.navigate(Screen.TagDiaries.createRoute(tag))
                }
            )
        }

        composable(
            route = Screen.TagDiaries.route,
            arguments = listOf(
                navArgument("tag") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tag = backStackEntry.arguments?.getString("tag") ?: return@composable
            TagDiariesScreen(
                tag = tag,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDiaryClick = { id ->
                    navController.navigate(Screen.DiaryDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDiaryClick = { id ->
                    navController.navigate(Screen.DiaryDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.AIChat.route,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            AIChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.AISettings.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.AIConversationHistory.route)
                }
            )
        }

        composable(Screen.AISettings.route) {
            AISettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMemory = {
                    navController.navigate(Screen.AIMemoryManagement.route)
                }
            )
        }

        composable(Screen.AIConversationHistory.route) {
            ConversationHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onConversationClick = { conversationId ->
                    navController.navigate(Screen.AIChat.createRoute(conversationId)) {
                        popUpTo(Screen.AIConversationHistory.route) { inclusive = true }
                    }
                },
                onNewConversation = {
                    navController.navigate(Screen.AIChat.createRoute()) {
                        popUpTo(Screen.AIConversationHistory.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AIMemoryManagement.route) {
            MemoryManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AiReviewList.route) {
            com.mindtrace.diary.ui.screens.review.AiReviewListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReviewClick = { reviewId ->
                    navController.navigate(Screen.AiReviewDetail.createRoute(reviewId))
                }
            )
        }

        composable(
            route = Screen.AiReviewDetail.route,
            arguments = listOf(
                navArgument("reviewId") { type = NavType.StringType }
            )
        ) {
            com.mindtrace.diary.ui.screens.review.AiReviewDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.WebDAVSettings.route) {
            WebDAVSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
