package com.mindtrace.diary.ui.screens.material

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.domain.model.DailyMaterial
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMaterialScreen(
    onNavigateBack: () -> Unit,
    onOpenDiary: (String) -> Unit,
    viewModel: DailyMaterialViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.savedDiaryId) {
        state.savedDiaryId?.let { id ->
            viewModel.consumeSavedDiary()
            onOpenDiary(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今日素材篮") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    DateNavigator(
                        date = state.selectedDate,
                        canGoForward = state.canGoForward,
                        onPrevious = viewModel::previousDay,
                        onNext = viewModel::nextDay,
                        onToday = viewModel::today
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "选中 ${state.selectedKeys.size} / ${state.items.size} 条",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::selectAll) { Text("全选") }
                        TextButton(onClick = viewModel::clearSelection) { Text("清空") }
                    }
                }
                if (state.items.isEmpty()) {
                    item {
                        Text(
                            text = "这天还没有可整理的素材。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 40.dp)
                        )
                    }
                } else {
                    items(state.items, key = DailyMaterial::key) { material ->
                        MaterialRow(
                            material = material,
                            selected = material.key in state.selectedKeys,
                            onClick = { viewModel.toggle(material.key) }
                        )
                    }
                }
                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("整理稿预览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = viewModel::updateTitle,
                        label = { Text("日记标题") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.draft.ifEmpty { "勾选上方素材后，这里会生成整理稿。" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("允许 AI 分析", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "默认关闭；关闭时仅在本地整理保存",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = state.allowAI, onCheckedChange = viewModel::setAllowAI)
                    }
                }
                state.error?.let { error ->
                    item { Text(error, color = MaterialTheme.colorScheme.error) }
                }
                item {
                    Button(
                        onClick = viewModel::save,
                        enabled = state.draft.isNotBlank() && !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isSaving) CircularProgressIndicator(strokeWidth = 2.dp)
                        else Icon(Icons.Default.Save, contentDescription = null)
                        Text(if (state.isSaving) "  保存中…" else "  保存为日记")
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun MaterialRow(material: DailyMaterial, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(
                if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    "${material.timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))}  ${material.type.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                material.title?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                Text(material.text, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun DateNavigator(
    date: LocalDate,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "前一天")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)))
            if (date != LocalDate.now()) TextButton(onClick = onToday) { Text("回到今天") }
        }
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = "后一天")
        }
    }
}
