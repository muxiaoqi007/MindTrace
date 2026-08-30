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
import com.mindtrace.diary.ui.screens.history.HistoryScreen
import com.mindtrace.diary.ui.screens.home.HomeScreen
import com.mindtrace.diary.ui.screens.receipt.DailyReceiptScreen
import com.mindtrace.diary.ui.screens.memorywalk.MemoryWalkScreen
import com.mindtrace.diary.ui.screens.material.DailyMaterialScreen
import com.mindtrace.diary.ui.screens.facets.LifeFacetsScreen
import com.mindtrace.diary.ui.screens.capsule.TimeCapsuleScreen
import com.mindtrace.diary.ui.screens.magazine.WeeklyMagazineScreen
import com.mindtrace.diary.ui.screens.storyline.StorylineScreen
import com.mindtrace.diary.ui.screens.lexicon.PersonalLexiconScreen
import com.mindtrace.diary.ui.screens.onesecond.OneSecondLifeScreen
import com.mindtrace.diary.ui.screens.map.MapFootprintsScreen
import com.mindtrace.diary.ui.screens.print.PrintArchiveScreen
import com.mindtrace.diary.ui.screens.search.SearchScreen
import com.mindtrace.diary.ui.screens.settings.SettingsScreen
import com.mindtrace.diary.ui.screens.statistics.StatisticsScreen
import com.mindtrace.diary.ui.screens.tags.TagDiariesScreen
import com.mindtrace.diary.ui.screens.tags.TagsScreen
import com.mindtrace.diary.ui.screens.ai.AIChatScreen
import com.mindtrace.diary.ui.screens.ai.AISettingsScreen
import com.mindtrace.diary.ui.screens.ai.MemoryCenterScreen
import com.mindtrace.diary.ui.screens.ai.ConversationHistoryScreen
import com.mindtrace.diary.ui.screens.ai.MemoryInboxScreen
import com.mindtrace.diary.ui.screens.ai.MemoryManagementScreen
import com.mindtrace.diary.ui.screens.ai.SelfPortraitScreen
import com.mindtrace.diary.ui.screens.ai.SoulConfigScreen
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
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                onAIChatClick = {
                    navController.navigate(Screen.AIChat.createRoute())
                },
                onDailyReceiptClick = {
                    navController.navigate(Screen.DailyReceipt.route)
                },
                onMemoryWalkClick = {
                    navController.navigate(Screen.MemoryWalk.route)
                },
                onDailyMaterialClick = {
                    navController.navigate(Screen.DailyMaterial.route)
                },
                onLifeFacetsClick = {
                    navController.navigate(Screen.LifeFacets.route)
                },
                onTimeCapsulesClick = {
                    navController.navigate(Screen.TimeCapsules.route)
                },
                onWeeklyMagazineClick = {
                    navController.navigate(Screen.WeeklyMagazine.route)
                },
                onStorylinesClick = {
                    navController.navigate(Screen.Storylines.route)
                },
                onPersonalLexiconClick = {
                    navController.navigate(Screen.PersonalLexicon.route)
                },
                onOneSecondLifeClick = {
                    navController.navigate(Screen.OneSecondLife.route)
                },
                onMapFootprintsClick = {
                    navController.navigate(Screen.MapFootprints.route)
                },
                onPrintArchiveClick = {
                    navController.navigate(Screen.PrintArchive.route)
                },
                onAISettingsClick = {
                    navController.navigate(Screen.AISettings.route)
                },
                onAskAIQuestion = { question ->
                    navController.navigate(Screen.AIChat.createRoute(seed = question))
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

        composable(Screen.DailyReceipt.route) {
            DailyReceiptScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MemoryWalk.route) {
            MemoryWalkScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenDiary = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                }
            )
        }

        composable(Screen.DailyMaterial.route) {
            DailyMaterialScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenDiary = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                }
            )
        }

        composable(Screen.LifeFacets.route) {
            LifeFacetsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.TimeCapsules.route) {
            TimeCapsuleScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.WeeklyMagazine.route) {
            WeeklyMagazineScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Storylines.route) {
            StorylineScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenDiary = { id -> navController.navigate(Screen.DiaryDetail.createRoute(id)) }
            )
        }

        composable(Screen.PersonalLexicon.route) {
            PersonalLexiconScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenDiary = { id -> navController.navigate(Screen.DiaryDetail.createRoute(id)) }
            )
        }

        composable(Screen.OneSecondLife.route) {
            OneSecondLifeScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.MapFootprints.route) {
            MapFootprintsScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenDiary = { id -> navController.navigate(Screen.DiaryDetail.createRoute(id)) }
            )
        }

        composable(Screen.PrintArchive.route) {
            PrintArchiveScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.AIChat.route,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("seed") {
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
                    navController.navigate(Screen.AIMemoryCenter.route)
                },
                onNavigateToSoulConfig = {
                    navController.navigate(Screen.AISoulConfig.route)
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
                },
                onOpenDiarySource = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                }
            )
        }

        composable(Screen.AIMemoryCenter.route) {
            MemoryCenterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToInbox = {
                    navController.navigate(Screen.AIMemoryInbox.route)
                },
                onNavigateToMemoryManagement = {
                    navController.navigate(Screen.AIMemoryManagement.route)
                },
                onNavigateToSelfPortrait = {
                    navController.navigate(Screen.AISelfPortrait.route)
                }
            )
        }

        composable(Screen.AIMemoryInbox.route) {
            MemoryInboxScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenDiarySource = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                }
            )
        }

        composable(Screen.AISoulConfig.route) {
            SoulConfigScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AISelfPortrait.route) {
            SelfPortraitScreen(
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
