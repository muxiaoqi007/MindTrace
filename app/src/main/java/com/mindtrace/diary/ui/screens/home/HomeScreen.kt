package com.mindtrace.diary.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mindtrace.diary.core.util.DateUtils
import com.mindtrace.diary.domain.model.TimelineItem
import com.mindtrace.diary.ui.components.MoodChip
import com.mindtrace.diary.ui.theme.Spacing
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDiaryClick: (String) -> Unit,
    onSearchClick: () -> Unit = {},
    onAIChatClick: () -> Unit = {},
    onAskAIQuestion: (String) -> Unit = {},
    onAISettingsClick: () -> Unit = {},
    onDailyReceiptClick: () -> Unit = {},
    onMemoryWalkClick: () -> Unit = {},
    onDailyMaterialClick: () -> Unit = {},
    onLifeFacetsClick: () -> Unit = {},
    onTimeCapsulesClick: () -> Unit = {},
    onWeeklyMagazineClick: () -> Unit = {},
    onStorylinesClick: () -> Unit = {},
    onPersonalLexiconClick: () -> Unit = {},
    onOneSecondLifeClick: () -> Unit = {},
    onMapFootprintsClick: () -> Unit = {},
    onPrintArchiveClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAllFeatures by remember { mutableStateOf(false) }

    val isTodoMode = uiState.selectedFilter == FilterType.TODO

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val now = LocalTime.now()
                    Column {
                        Text(
                            text = greetingFor(now),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = todayLabel(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAIChatClick) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadReviewCount > 0) {
                                    Badge {
                                        Text(
                                            text = if (uiState.unreadReviewCount > 99) "99+" else uiState.unreadReviewCount.toString()
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI 伙伴")
                        }
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            QuickInputBar(
                value = uiState.quickInput,
                onValueChange = viewModel::updateQuickInput,
                onSend = viewModel::submitQuickInput,
                isLoading = uiState.isAddingItem,
                placeholder = if (isTodoMode) "添加一个待办..." else "记录一个闪念..."
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, top = Spacing.sm, bottom = Spacing.lg),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // —— 今日洞察 ——
            item(key = "insight") {
                DailyInsightCard(
                    insight = uiState.dailyInsight,
                    isLoading = uiState.insightLoading,
                    insightFailed = uiState.insightFailed,
                    isAIConfigured = uiState.isAIConfigured,
                    onRefresh = { viewModel.refreshInsight() },
                    onConfigureAI = onAISettingsClick,
                    onAskAI = onAskAIQuestion
                )
            }

            // —— 此刻灵感：按时间段推荐的功能入口 ——
            item(key = "suggestions") {
                SuggestionSection(
                    onDailyReceiptClick = onDailyReceiptClick,
                    onMemoryWalkClick = onMemoryWalkClick,
                    onDailyMaterialClick = onDailyMaterialClick,
                    onLifeFacetsClick = onLifeFacetsClick,
                    onTimeCapsulesClick = onTimeCapsulesClick,
                    onWeeklyMagazineClick = onWeeklyMagazineClick,
                    onStorylinesClick = onStorylinesClick,
                    onPersonalLexiconClick = onPersonalLexiconClick,
                    onOneSecondLifeClick = onOneSecondLifeClick,
                    onMapFootprintsClick = onMapFootprintsClick,
                    onPrintArchiveClick = onPrintArchiveClick,
                    onShowAll = { showAllFeatures = true }
                )
            }

            // —— 筛选 ——
            item(key = "filters") {
                FilterChips(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = viewModel::setFilter,
                    modifier = Modifier.padding(vertical = Spacing.sm)
                )
            }

            if (uiState.isLoading) {
                item(key = "loading") {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.items.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xxl)
                    )
                }
            } else {
                // Group items by date
                val groupedItems = uiState.items.groupBy { item ->
                    item.createdAt.toLocalDate()
                }.toSortedMap(compareByDescending { it })

                groupedItems.forEach { (date, items) ->
                    // Date header
                    item(key = "header_$date") {
                        DateHeader(date = date)
                    }

                    // Timeline items for this date
                    items(items, key = { it.id }) { item ->
                        TimelineItemRow(
                            item = item,
                            isLast = item == items.last(),
                            onDiaryClick = onDiaryClick,
                            onToggleTodo = viewModel::toggleTodo,
                            onDeleteTodo = viewModel::deleteTodo,
                            onDeleteFlashNote = viewModel::deleteFlashNote
                        )
                    }

                    // Spacer between date groups
                    item(key = "spacer_$date") {
                        Spacer(modifier = Modifier.height(Spacing.lg))
                    }
                }
            }
        }
    }

    if (showAllFeatures) {
        AllFeaturesSheet(
            onDismiss = { showAllFeatures = false },
            onDailyReceiptClick = onDailyReceiptClick,
            onMemoryWalkClick = onMemoryWalkClick,
            onDailyMaterialClick = onDailyMaterialClick,
            onLifeFacetsClick = onLifeFacetsClick,
            onTimeCapsulesClick = onTimeCapsulesClick,
            onWeeklyMagazineClick = onWeeklyMagazineClick,
            onStorylinesClick = onStorylinesClick,
            onPersonalLexiconClick = onPersonalLexiconClick,
            onOneSecondLifeClick = onOneSecondLifeClick,
            onMapFootprintsClick = onMapFootprintsClick,
            onPrintArchiveClick = onPrintArchiveClick
        )
    }
}

private fun greetingFor(time: LocalTime): String = when (time.hour) {
    in 5..10 -> "早上好"
    in 11..12 -> "中午好"
    in 13..17 -> "下午好"
    in 18..23 -> "晚上好"
    else -> "夜深了"
}

private fun todayLabel(): String {
    val today = LocalDate.now()
    return "${today.monthValue}月${today.dayOfMonth}日 · ${DateUtils.getChineseDayOfWeek(today)}"
}

// ---------- 今日洞察 ----------

@Composable
private fun DailyInsightCard(
    insight: com.mindtrace.diary.domain.model.DailyInsight?,
    isLoading: Boolean,
    insightFailed: Boolean,
    isAIConfigured: Boolean,
    onRefresh: () -> Unit,
    onConfigureAI: () -> Unit,
    onAskAI: (String) -> Unit
) {
    // 已配置但生成失败且无缓存时，保留卡片给用户重试入口

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.md)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = "今日洞察",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.weight(1f))
                if (isAIConfigured) {
                    IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp), enabled = !isLoading) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "换一条洞察",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            when {
                isLoading -> {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .background(
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f),
                                    MaterialTheme.shapes.small
                                )
                        )
                        Spacer(Modifier.height(Spacing.xs))
                    }
                    Text(
                        text = "正在回顾你最近的日记…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                insight != null -> {
                    Text(
                        text = insight.observation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Surface(
                        onClick = { onAskAI(insight.question) },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                        ) {
                            Text(
                                text = insight.question,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "去和 AI 聊聊",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                insightFailed -> {
                    Text(
                        text = "这次没能生成洞察，可能网络不佳或日记还太少。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    TextButton(onClick = onRefresh) {
                        Text("再试一次")
                    }
                }
                else -> {
                    Text(
                        text = "配置 AI 伙伴后，每天会基于你的日记生成一条今日洞察，陪你把想法聊开。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    TextButton(onClick = onConfigureAI) {
                        Text("去配置")
                    }
                }
            }
        }
    }
}

// ---------- 此刻灵感 / 功能入口 ----------

private enum class FeatureId {
    DAILY_RECEIPT, MEMORY_WALK, DAILY_MATERIAL, LIFE_FACETS, TIME_CAPSULES,
    WEEKLY_MAGAZINE, STORYLINES, PERSONAL_LEXICON, ONE_SECOND_LIFE,
    MAP_FOOTPRINTS, PRINT_ARCHIVE
}

/** 按时间段挑选最贴合当前场景的功能，全天兜底 */
private fun recommendedFeatures(hour: Int, dayOfWeek: DayOfWeek): List<FeatureId> = when (hour) {
    in 5..10 -> listOf(FeatureId.DAILY_MATERIAL, FeatureId.LIFE_FACETS, FeatureId.MEMORY_WALK)
    in 11..13 -> listOf(FeatureId.MEMORY_WALK, FeatureId.ONE_SECOND_LIFE, FeatureId.DAILY_MATERIAL)
    in 14..17 -> listOf(FeatureId.MEMORY_WALK, FeatureId.STORYLINES, FeatureId.ONE_SECOND_LIFE)
    in 18..23 -> {
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            listOf(FeatureId.DAILY_RECEIPT, FeatureId.WEEKLY_MAGAZINE, FeatureId.MEMORY_WALK)
        } else {
            listOf(FeatureId.DAILY_RECEIPT, FeatureId.MEMORY_WALK, FeatureId.TIME_CAPSULES)
        }
    }
    else -> listOf(FeatureId.MEMORY_WALK, FeatureId.TIME_CAPSULES, FeatureId.DAILY_RECEIPT)
}

@Composable
private fun SuggestionSection(
    onDailyReceiptClick: () -> Unit,
    onMemoryWalkClick: () -> Unit,
    onDailyMaterialClick: () -> Unit,
    onLifeFacetsClick: () -> Unit,
    onTimeCapsulesClick: () -> Unit,
    onWeeklyMagazineClick: () -> Unit,
    onStorylinesClick: () -> Unit,
    onPersonalLexiconClick: () -> Unit,
    onOneSecondLifeClick: () -> Unit,
    onMapFootprintsClick: () -> Unit,
    onPrintArchiveClick: () -> Unit,
    onShowAll: () -> Unit
) {
    val allEntries = rememberFeatureEntries(
        onDailyReceiptClick, onMemoryWalkClick, onDailyMaterialClick, onLifeFacetsClick,
        onTimeCapsulesClick, onWeeklyMagazineClick, onStorylinesClick, onPersonalLexiconClick,
        onOneSecondLifeClick, onMapFootprintsClick, onPrintArchiveClick
    )
    val recommended = remember(allEntries) {
        val now = LocalTime.now()
        recommendedFeatures(now.hour, LocalDate.now().dayOfWeek).mapNotNull { allEntries[it] }
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "此刻灵感",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onShowAll, contentPadding = PaddingValues(horizontal = Spacing.sm)) {
                Text("全部功能", style = MaterialTheme.typography.labelLarge)
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(recommended.size) { index ->
                FeatureSuggestionCard(entry = recommended[index])
            }
            item {
                FeatureSuggestionCard(
                    entry = FeatureEntry("全部功能", "11 个灵感工具", Icons.Default.Widgets) { onShowAll() }
                )
            }
        }
    }
}

@Composable
private fun rememberFeatureEntries(
    onDailyReceiptClick: () -> Unit,
    onMemoryWalkClick: () -> Unit,
    onDailyMaterialClick: () -> Unit,
    onLifeFacetsClick: () -> Unit,
    onTimeCapsulesClick: () -> Unit,
    onWeeklyMagazineClick: () -> Unit,
    onStorylinesClick: () -> Unit,
    onPersonalLexiconClick: () -> Unit,
    onOneSecondLifeClick: () -> Unit,
    onMapFootprintsClick: () -> Unit,
    onPrintArchiveClick: () -> Unit
): Map<FeatureId, FeatureEntry> = mapOf(
    FeatureId.DAILY_RECEIPT to FeatureEntry("每日小票", "今天的账单式回顾", Icons.AutoMirrored.Filled.ReceiptLong, onDailyReceiptClick),
    FeatureId.MEMORY_WALK to FeatureEntry("记忆漫步", "随机回到某一天", Icons.Default.Explore, onMemoryWalkClick),
    FeatureId.DAILY_MATERIAL to FeatureEntry("今日素材篮", "把今天拼成一篇日记", Icons.Default.Inventory2, onDailyMaterialClick),
    FeatureId.LIFE_FACETS to FeatureEntry("生活切面", "记录并发现影响心情的因素", Icons.Default.Tune, onLifeFacetsClick),
    FeatureId.TIME_CAPSULES to FeatureEntry("时光胶囊", "给未来的自己写封信", Icons.Default.LockClock, onTimeCapsulesClick),
    FeatureId.WEEKLY_MAGAZINE to FeatureEntry("每周生活杂志", "一周的封面故事", Icons.Default.Newspaper, onWeeklyMagazineClick),
    FeatureId.STORYLINES to FeatureEntry("人生故事线", "从标签里发现人生主线", Icons.Default.Timeline, onStorylinesClick),
    FeatureId.PERSONAL_LEXICON to FeatureEntry("我的词典", "你的专属名词表", Icons.Default.MenuBook, onPersonalLexiconClick),
    FeatureId.ONE_SECOND_LIFE to FeatureEntry("一秒人生", "每天一秒，连成电影", Icons.Default.MovieCreation, onOneSecondLifeClick),
    FeatureId.MAP_FOOTPRINTS to FeatureEntry("地图足迹", "去过的地方都在这", Icons.Default.Map, onMapFootprintsClick),
    FeatureId.PRINT_ARCHIVE to FeatureEntry("打印归档", "把日记排成纸质书", Icons.Default.Print, onPrintArchiveClick)
)

private data class FeatureEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun FeatureSuggestionCard(entry: FeatureEntry) {
    Card(
        onClick = entry.onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.width(148.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    entry.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllFeaturesSheet(
    onDismiss: () -> Unit,
    onDailyReceiptClick: () -> Unit,
    onMemoryWalkClick: () -> Unit,
    onDailyMaterialClick: () -> Unit,
    onLifeFacetsClick: () -> Unit,
    onTimeCapsulesClick: () -> Unit,
    onWeeklyMagazineClick: () -> Unit,
    onStorylinesClick: () -> Unit,
    onPersonalLexiconClick: () -> Unit,
    onOneSecondLifeClick: () -> Unit,
    onMapFootprintsClick: () -> Unit,
    onPrintArchiveClick: () -> Unit
) {
    val entries = rememberFeatureEntries(
        onDailyReceiptClick, onMemoryWalkClick, onDailyMaterialClick, onLifeFacetsClick,
        onTimeCapsulesClick, onWeeklyMagazineClick, onStorylinesClick, onPersonalLexiconClick,
        onOneSecondLifeClick, onMapFootprintsClick, onPrintArchiveClick
    )

    val groups = listOf(
        "回顾今天" to listOf(FeatureId.DAILY_RECEIPT, FeatureId.MEMORY_WALK, FeatureId.WEEKLY_MAGAZINE),
        "收集此刻" to listOf(FeatureId.DAILY_MATERIAL, FeatureId.LIFE_FACETS, FeatureId.ONE_SECOND_LIFE),
        "长期视角" to listOf(FeatureId.TIME_CAPSULES, FeatureId.STORYLINES, FeatureId.MAP_FOOTPRINTS),
        "整理归档" to listOf(FeatureId.PERSONAL_LEXICON, FeatureId.PRINT_ARCHIVE)
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xxl)
        ) {
            Text(
                text = "全部功能",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.lg))
            groups.forEach { (groupTitle, ids) ->
                Text(
                    text = groupTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                ids.chunked(3).forEach { rowIds ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.sm)
                    ) {
                        rowIds.forEach { id ->
                            val entry = entries.getValue(id)
                            FeatureSheetCell(
                                entry = entry,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowIds.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.md))
            }
        }
    }
}

@Composable
private fun FeatureSheetCell(
    entry: FeatureEntry,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {
            entry.onClick()
        },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.md, horizontal = Spacing.xs)
        ) {
            Icon(
                entry.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = entry.title,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------- 时间线 ----------

@Composable
private fun DateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val displayText = when (date) {
        today -> "今天"
        today.minusDays(1) -> "昨天"
        today.minusDays(2) -> "前天"
        else -> "${date.monthValue}月${date.dayOfMonth}日"
    }
    val weekDay = DateUtils.getChineseDayOfWeek(date)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.md)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = weekDay,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (date.year != today.year) {
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = "${date.year}年",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineItemRow(
    item: TimelineItem,
    isLast: Boolean,
    onDiaryClick: (String) -> Unit,
    onToggleTodo: (String) -> Unit,
    onDeleteTodo: (String) -> Unit,
    onDeleteFlashNote: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val timelineColor = when (item) {
        is TimelineItem.DiaryItem -> MaterialTheme.colorScheme.primary
        is TimelineItem.FlashNoteItem -> MaterialTheme.colorScheme.secondary
        is TimelineItem.TodoItem -> MaterialTheme.colorScheme.tertiary
    }

    // Swipe to dismiss state
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                when (item) {
                    is TimelineItem.FlashNoteItem -> {
                        onDeleteFlashNote(item.id, item.diaryId)
                        true
                    }
                    is TimelineItem.TodoItem -> {
                        onDeleteTodo(item.id)
                        true
                    }
                    is TimelineItem.DiaryItem -> false // Don't allow swipe delete for diaries
                }
            } else {
                false
            }
        }
    )

    // Check if this item can be swiped
    val canSwipe = item !is TimelineItem.DiaryItem

    Row(modifier = modifier.fillMaxWidth()) {
        // Timeline indicator (left side)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Time
            Text(
                text = DateUtils.formatTime(item.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(timelineColor)
            )

            // Line (if not last item)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(timelineColor.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        // Content card with swipe to dismiss
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else Spacing.md)
        ) {
            if (canSwipe) {
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                else -> Color.Transparent
                            },
                            label = "swipe_color"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, MaterialTheme.shapes.medium)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                ) {
                    when (item) {
                        is TimelineItem.FlashNoteItem -> FlashNoteCard(
                            item = item,
                            onClick = { }
                        )
                        is TimelineItem.TodoItem -> TodoCard(
                            item = item,
                            onClick = { },
                            onToggle = { onToggleTodo(item.id) },
                            onDelete = { onDeleteTodo(item.id) }
                        )
                        else -> {} // DiaryItem handled below
                    }
                }
            } else {
                when (item) {
                    is TimelineItem.DiaryItem -> DiaryCard(
                        item = item,
                        onClick = { onDiaryClick(item.id) }
                    )
                    else -> {} // FlashNote and Todo handled above
                }
            }
        }
    }
}

@Composable
private fun QuickInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                .imePadding()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (value.isNotBlank()) {
                            onSend()
                            keyboardController?.hide()
                        }
                    }
                ),
                trailingIcon = {
                    if (value.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSend()
                                keyboardController?.hide()
                            },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "发送",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilterChips(
    selectedFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = modifier
    ) {
        items(FilterType.entries) { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            FilterType.ALL -> "全部"
                            FilterType.DIARY -> "日记"
                            FilterType.FLASHNOTE -> "闪念"
                            FilterType.TODO -> "待办"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun DiaryCard(
    item: TimelineItem.DiaryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = item.title.ifEmpty { "日记" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                item.mood?.let { mood ->
                    MoodChip(mood = mood)
                }
            }

            if (item.contentPreview.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = item.contentPreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            item.firstImage?.let { imagePath ->
                Spacer(modifier = Modifier.height(Spacing.sm))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(imagePath))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            }
        }
    }
}

@Composable
private fun FlashNoteCard(
    item: TimelineItem.FlashNoteItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "闪念",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = item.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            item.firstImage?.let { imagePath ->
                Spacer(modifier = Modifier.height(Spacing.sm))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(imagePath))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoCard(
    item: TimelineItem.TodoItem,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteDialog = true }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.sm)
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.size(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                    color = if (item.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                item.dueDate?.let { dueDate ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "截止: ${DateUtils.formatDate(dueDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除待办") },
            text = { Text("确定要删除这个待办事项吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = "还没有任何记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "在下方输入框记录你的第一个闪念吧",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
