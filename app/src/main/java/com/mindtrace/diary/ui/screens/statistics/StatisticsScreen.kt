package com.mindtrace.diary.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.core.util.DateUtils
import com.mindtrace.diary.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onHistoryClick: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "历史上的今天")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.overview == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ============ 总览卡片 ============
                OverviewSection(uiState)

                Spacer(modifier = Modifier.height(20.dp))

                // ============ 7天心情趋势 ============
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MoodTrendLineChart(
                            trendData = uiState.moodTrend
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ============ 月度统计 ============
                MonthlyStatisticsSection(
                    uiState = uiState,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ============ 心情分布 ============
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MoodDistributionChart(
                            moodDistribution = uiState.monthlyStatistics?.moodDistribution ?: emptyMap()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ============ 历史上的今天入口 ============
                OutlinedCard(
                    onClick = onHistoryClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "历史上的今天",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "查看往年今天的日记",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OverviewSection(uiState: StatisticsUiState) {
    val overview = uiState.overview

    // 第一行：心情指数 + 连续天数（固定高度保持一致）
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 心情指数卡片
        Card(
            modifier = Modifier
                .weight(1f)
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                MoodIndexGauge(
                    moodIndex = overview?.moodIndex ?: 0f,
                    dominantMood = overview?.dominantMood
                )
            }
        }

        // 连续写作天数卡片
        Card(
            modifier = Modifier
                .weight(1f)
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                WritingStreakBadge(
                    streak = overview?.writingStreak ?: 0
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 第二行：统计数字
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 日记数
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            StatNumberCard(
                value = overview?.diaryCount ?: 0,
                label = "日记",
                emoji = "📔",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }

        // 闪念数
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            StatNumberCard(
                value = overview?.flashNoteCount ?: 0,
                label = "闪念",
                emoji = "💡",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }

        // 待办完成
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            StatNumberCard(
                value = overview?.todoCompletedCount ?: 0,
                label = "已完成",
                emoji = "✅",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun MonthlyStatisticsSection(
    uiState: StatisticsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 月份选择器
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "上一个月"
                    )
                }

                Text(
                    text = DateUtils.getChineseMonth(uiState.yearMonth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "下一个月"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 月度统计数据
            uiState.monthlyStatistics?.let { stats ->
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MonthlyStatItem(
                        value = stats.totalEntries.toString(),
                        label = "记录数"
                    )
                    MonthlyStatItem(
                        value = stats.totalWords.toString(),
                        label = "字数"
                    )
                    MonthlyStatItem(
                        value = stats.writingDays.toString(),
                        label = "写作天数"
                    )
                }
            } ?: run {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun MonthlyStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
