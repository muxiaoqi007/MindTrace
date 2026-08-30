package com.mindtrace.diary.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.core.util.DateUtils
import com.mindtrace.diary.domain.model.AIMemory
import com.mindtrace.diary.domain.model.SelfNarrative
import com.mindtrace.diary.domain.model.SelfPortrait
import com.mindtrace.diary.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelfPortraitScreen(
    onNavigateBack: () -> Unit,
    viewModel: SelfPortraitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自我画像") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.portrait == null -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text("暂无画像数据")
                }
            }
            else -> {
                SelfPortraitContent(
                    portrait = uiState.portrait!!,
                    narrative = uiState.narrative,
                    isNarrativeLoading = uiState.isNarrativeLoading,
                    isAIConfigured = uiState.isAIConfigured,
                    onRegenerateNarrative = viewModel::refreshNarrative,
                    onAccurate = viewModel::markMemoryAccurate,
                    onInaccurate = viewModel::markMemoryInaccurate,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelfPortraitContent(
    portrait: SelfPortrait,
    narrative: SelfNarrative?,
    isNarrativeLoading: Boolean,
    isAIConfigured: Boolean,
    onRegenerateNarrative: () -> Unit,
    onAccurate: (String) -> Unit,
    onInaccurate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = modifier.fillMaxSize()
    ) {
        // —— 招牌功能：AI 眼中的你 ——
        item {
            NarrativeCard(
                narrative = narrative,
                isLoading = isNarrativeLoading,
                isAIConfigured = isAIConfigured,
                onRegenerate = onRegenerateNarrative
            )
        }

        item {
            InfoCard(
                text = "画像基于本地日记统计和你确认过的长期记忆生成，不是诊断结论；你可以通过\"像我/不准确\"纠正它。"
            )
        }

        item {
            OverviewCard(portrait = portrait)
        }

        if (portrait.topTags.isNotEmpty()) {
            item { SectionTitle("高频标签") }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    portrait.topTags.take(5).forEach { (tag, count) ->
                        AssistChip(onClick = { }, label = { Text("$tag · $count") })
                    }
                }
            }
        }

        memorySection("我可能是这样的人", portrait.personalityMemories, onAccurate, onInaccurate)
        memorySection("我的偏好", portrait.preferenceMemories, onAccurate, onInaccurate)
        memorySection("我的目标", portrait.goalMemories, onAccurate, onInaccurate)
        memorySection("重要关系", portrait.relationshipMemories, onAccurate, onInaccurate)
        memorySection("个人事实", portrait.factMemories, onAccurate, onInaccurate)

        item {
            Text(
                text = "更新时间：${DateUtils.formatDateTime(portrait.updatedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * "AI 眼中的你"叙事卡：应用最核心的记忆系统 + AI 的可视化出口
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NarrativeCard(
    narrative: SelfNarrative?,
    isLoading: Boolean,
    isAIConfigured: Boolean,
    onRegenerate: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SelfImprovement,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.size(Spacing.sm))
                Text(
                    "AI 眼中的你",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                if (narrative != null && !isLoading) {
                    IconButton(onClick = onRegenerate, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "重新生成",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            when {
                isLoading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                                        MaterialTheme.shapes.small
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        "正在回忆关于你的一切…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                narrative != null -> {
                    Text(
                        text = narrative.narrative,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (narrative.keywords.isNotEmpty()) {
                        Spacer(Modifier.height(Spacing.md))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            narrative.keywords.forEach { keyword ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(keyword) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        text = "💡 ${narrative.suggestion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "生成于 ${DateUtils.formatDateTime(narrative.generatedAt)} · 依据你确认过的记忆",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f)
                    )
                }
                isAIConfigured -> {
                    Text(
                        text = "让 AI 根据你的日记和记忆，写一段\"TA 眼中的你\"。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Button(onClick = onRegenerate) {
                        Text("生成画像")
                    }
                }
                else -> {
                    Text(
                        text = "在设置中配置 AI 伙伴后，这里会出现一段\"TA 眼中的你\"——基于你确认过的记忆写成。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.memorySection(
    title: String,
    memories: List<AIMemory>,
    onAccurate: (String) -> Unit,
    onInaccurate: (String) -> Unit
) {
    if (memories.isEmpty()) return
    item { SectionTitle(title) }
    items(memories, key = { it.id }) { memory ->
        MemoryInsightCard(
            memory = memory,
            onAccurate = { onAccurate(memory.id) },
            onInaccurate = { onInaccurate(memory.id) }
        )
    }
}

@Composable
private fun OverviewCard(portrait: SelfPortrait) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SelfImprovement, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.size(Spacing.md))
                Text("最近的你", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                StatText("日记", portrait.totalDiaries.toString())
                StatText("字数", portrait.totalWords.toString())
                StatText("连续", "${portrait.writingStreak}天")
            }
            portrait.dominantMoodName?.let {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "常见心情：$it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatText(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(Spacing.md))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun MemoryInsightCard(
    memory: AIMemory,
    onAccurate: () -> Unit,
    onInaccurate: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(memory.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "重要性 ${(memory.importance * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = onAccurate, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(Spacing.xs))
                    Text("像我")
                }
                OutlinedButton(onClick = onInaccurate, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(Spacing.xs))
                    Text("不准确")
                }
            }
        }
    }
}
