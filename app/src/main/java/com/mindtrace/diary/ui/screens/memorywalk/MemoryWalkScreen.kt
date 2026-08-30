package com.mindtrace.diary.ui.screens.memorywalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.domain.model.MemoryWalkMode
import com.mindtrace.diary.domain.model.MemoryWalkSourceType
import com.mindtrace.diary.domain.model.MemoryWalkStop
import com.mindtrace.diary.domain.model.MoodLevel
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryWalkScreen(
    onNavigateBack: () -> Unit,
    onOpenDiary: (String) -> Unit,
    viewModel: MemoryWalkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message, uiState.error) {
        val feedback = uiState.error ?: uiState.message
        if (feedback != null) {
            snackbarHostState.showSnackbar(feedback)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("随机漫步") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.plan != null) {
                        IconButton(onClick = viewModel::startWalk, enabled = !uiState.isLoading) {
                            Icon(Icons.Default.Refresh, contentDescription = "换一条路线")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.plan == null) {
            MemoryWalkSetup(
                state = uiState,
                onModeChange = viewModel::setMode,
                onKeywordChange = viewModel::setKeyword,
                onMoodChange = viewModel::setMood,
                onStart = viewModel::startWalk,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            MemoryWalkJourney(
                state = uiState,
                onPrevious = viewModel::previousStop,
                onNext = viewModel::nextStop,
                onResponseChange = viewModel::updateResponse,
                onSaveResponse = viewModel::saveResponse,
                onFinish = viewModel::finishWalk,
                onOpenDiary = onOpenDiary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun MemoryWalkSetup(
    state: MemoryWalkUiState,
    onModeChange: (MemoryWalkMode) -> Unit,
    onKeywordChange: (String) -> Unit,
    onMoodChange: (MoodLevel) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "从过去出发，走三站，再回到今天。",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "漫步只读取本机旧记录，不调用 AI。每条路线会避开今天，并尽量跨越不同年份。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Text("选择路线", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeChip("随便走走", MemoryWalkMode.SURPRISE, state.mode, onModeChange)
                ModeChip("按一个词", MemoryWalkMode.KEYWORD, state.mode, onModeChange)
                ModeChip("按心情", MemoryWalkMode.MOOD, state.mode, onModeChange)
            }
        }

        if (state.mode == MemoryWalkMode.KEYWORD) {
            item {
                OutlinedTextField(
                    value = state.keyword,
                    onValueChange = onKeywordChange,
                    label = { Text("想沿着什么词漫步？") },
                    placeholder = { Text("例如：画画、妈妈、旅行") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (state.mode == MemoryWalkMode.MOOD) {
            item {
                Text("选择一种旧心情", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MoodLevel.entries.size) { index ->
                        val mood = MoodLevel.entries[index]
                        FilterChip(
                            selected = state.mood == mood,
                            onClick = { onMoodChange(mood) },
                            label = { Text(mood.label) }
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onStart,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isLoading) "正在寻找旧时光" else "开始漫步")
            }
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    mode: MemoryWalkMode,
    selectedMode: MemoryWalkMode,
    onModeChange: (MemoryWalkMode) -> Unit
) {
    FilterChip(
        selected = selectedMode == mode,
        onClick = { onModeChange(mode) },
        label = { Text(label, maxLines = 1) }
    )
}

@Composable
private fun MemoryWalkJourney(
    state: MemoryWalkUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onResponseChange: (String) -> Unit,
    onSaveResponse: () -> Unit,
    onFinish: () -> Unit,
    onOpenDiary: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val plan = state.plan ?: return
    val stop = state.currentStop ?: return
    val isLast = state.currentStopIndex == plan.stops.lastIndex

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "第 ${state.currentStopIndex + 1} / ${plan.stops.size} 站",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (state.currentStopIndex + 1f) / plan.stops.size },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            MemoryStopCard(
                stop = stop,
                onOpenDiary = onOpenDiary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "停一下",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stop.reflectionPrompt,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.response,
                onValueChange = onResponseChange,
                label = { Text("写一句此刻回应（可选）") },
                minLines = 2,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSaveResponse,
                enabled = state.response.isNotBlank() && !state.isSavingResponse
            ) {
                Text(if (state.isSavingResponse) "正在保存" else "保存为闪念")
            }
        }

        item {
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onPrevious,
                    enabled = state.currentStopIndex > 0
                ) {
                    Text("上一站")
                }
                Button(onClick = if (isLast) onFinish else onNext) {
                    Text(if (isLast) "结束漫步" else "下一站")
                }
            }
        }
    }
}

@Composable
private fun MemoryStopCard(
    stop: MemoryWalkStop,
    onOpenDiary: (String) -> Unit
) {
    val memory = stop.memory
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = memory.date.format(DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = memory.title ?: if (memory.sourceType == MemoryWalkSourceType.DIARY) "一篇日记" else "一条闪念",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Text(memory.excerpt, style = MaterialTheme.typography.bodyLarge)

            val metadata = buildList {
                memory.mood?.label?.let(::add)
                addAll(memory.tags.take(3))
            }
            if (metadata.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = metadata.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (memory.sourceType == MemoryWalkSourceType.DIARY) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = { onOpenDiary(memory.id) }) {
                    Text("打开原日记")
                }
            }
        }
    }
}
