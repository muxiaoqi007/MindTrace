package com.mindtrace.diary.ui.screens.print

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.core.export.PrintArchiveManager
import com.mindtrace.diary.domain.model.PrintArchiveType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintArchiveScreen(onNavigateBack: () -> Unit, viewModel: PrintArchiveViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var dateTarget by remember { mutableStateOf<String?>(null) }
    var typeMenu by remember { mutableStateOf(false) }
    var tagMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.result) {
        state.result?.let { result ->
            when (result.action) {
                PrintArchiveAction.SHARE -> context.startActivity(Intent.createChooser(PrintArchiveManager.shareIntent(context, result.archive), "分享 PDF 归档"))
                PrintArchiveAction.PRINT -> PrintArchiveManager.print(context, result.archive)
            }
            viewModel.consumeResult()
        }
    }
    LaunchedEffect(state.error) { state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() } }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, topBar = {
        TopAppBar(title = { Text("打印归档") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
        })
    }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("把记录做成可保存、可打印的纸上档案。", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("先预览范围和页数，再生成 A4 PDF 或打开 Android 系统打印。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Text("归档类型", fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { typeMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(state.config.type.label) }
                DropdownMenu(typeMenu, { typeMenu = false }) {
                    PrintArchiveType.entries.forEach { type -> DropdownMenuItem({ Text(type.label) }, onClick = { viewModel.setType(type); typeMenu = false }) }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { dateTarget = "start" }, Modifier.weight(1f)) { Text("开始 ${state.config.startDate}") }
                    OutlinedButton(onClick = { dateTarget = "end" }, Modifier.weight(1f)) { Text("结束 ${state.config.endDate}") }
                }
            }
            if (state.config.type == PrintArchiveType.JOURNAL) {
                item {
                    Text("标签筛选", fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { tagMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(state.config.tag ?: "全部标签") }
                    DropdownMenu(tagMenu, { tagMenu = false }) {
                        DropdownMenuItem({ Text("全部标签") }, onClick = { viewModel.setTag(null); tagMenu = false })
                        state.tags.forEach { tag -> DropdownMenuItem({ Text(tag) }, onClick = { viewModel.setTag(tag); tagMenu = false }) }
                    }
                }
                item {
                    Text("字号 ${"%.1f".format(state.config.fontScale)}×", fontWeight = FontWeight.Bold)
                    Slider(state.config.fontScale, viewModel::setFontScale, valueRange = .8f..1.5f)
                    ConfigSwitch("包含照片", "照片会按页宽等比缩放", state.config.includePhotos, viewModel::setIncludePhotos)
                    ConfigSwitch("包含元数据", "日期、心情、天气、地点、坐标与标签", state.config.includeMetadata, viewModel::setIncludeMetadata)
                    ConfigSwitch("显式包含私密条目", "默认排除“不在回顾中出现”的日记", state.config.includePrivate, viewModel::setIncludePrivate)
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("生成预览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("预计 ${state.estimatedPages.coerceAtLeast(1)} 页 A4")
                        if (state.config.type == PrintArchiveType.JOURNAL) {
                            Text("选中 ${state.selectedDiaries.size} 篇日记", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            state.selectedDiaries.take(5).forEach { diary ->
                                Text("· ${diary.date ?: diary.createdAt.toLocalDate()}  ${diary.title.ifBlank { "无题日记" }}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { viewModel.generate(PrintArchiveAction.SHARE) }, enabled = !state.isGenerating, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PictureAsPdf, null); Text("  生成 PDF")
                    }
                    OutlinedButton(onClick = { viewModel.generate(PrintArchiveAction.PRINT) }, enabled = !state.isGenerating, modifier = Modifier.weight(1f)) {
                        if (state.isGenerating) CircularProgressIndicator() else Icon(Icons.Default.Print, null)
                        Text("  系统打印")
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    dateTarget?.let { target ->
        val initial = if (target == "start") state.config.startDate else state.config.endDate
        val picker = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(onDismissRequest = { dateTarget = null }, confirmButton = {
            TextButton(onClick = {
                picker.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    if (target == "start") viewModel.setStartDate(date) else viewModel.setEndDate(date)
                }
                dateTarget = null
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { dateTarget = null }) { Text("取消") } }) { DatePicker(picker) }
    }
}

@Composable
private fun ConfigSwitch(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked, onChange)
    }
}
