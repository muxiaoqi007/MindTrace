package com.mindtrace.diary.ui.screens.capsule

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.domain.model.TimeCapsule
import com.mindtrace.diary.domain.model.TimeCapsuleSummary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeCapsuleScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimeCapsuleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var showComposer by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<TimeCapsuleSummary?>(null) }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        }
        viewModel.addMedia(uris.map(Uri::toString))
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("时光胶囊") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showComposer = true }) {
                Icon(Icons.Default.Add, contentDescription = "写给未来")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("把今天封存，交给未来开启。", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("正文、预测和问题会加密保存，到期前不参与搜索和 AI。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (state.summaries.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("还没有胶囊", fontWeight = FontWeight.Bold)
                            Text("写一封信，或加入一张照片、一段语音。")
                        }
                    }
                }
            }
            items(state.summaries, key = TimeCapsuleSummary::id) { summary ->
                CapsuleRow(summary, onOpen = { viewModel.open(summary.id) }, onDelete = { deleteTarget = summary })
            }
        }
    }

    if (showComposer) {
        AlertDialog(
            onDismissRequest = { if (!state.isSaving) { showComposer = false; viewModel.resetEditor() } },
            title = { Text("写给未来的自己") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { OutlinedTextField(state.title, viewModel::updateTitle, label = { Text("标题（可选）") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(state.message, viewModel::updateMessage, label = { Text("想说的话") }, minLines = 4, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(state.prediction, viewModel::updatePrediction, label = { Text("对未来的一个预测（可选）") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(state.question, viewModel::updateQuestion, label = { Text("想问未来自己的问题（可选）") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("开启日期：${state.unlockDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))}")
                        }
                    }
                    item {
                        OutlinedButton(onClick = { mediaPicker.launch(arrayOf("image/*", "audio/*")) }) {
                            Icon(Icons.Default.AttachFile, null)
                            Text("  添加照片 / 语音")
                        }
                        state.mediaUris.forEachIndexed { index, uri ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("附件 ${index + 1}", modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.removeMedia(uri) }) { Icon(Icons.Default.Delete, "移除附件") }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = state.message.isNotBlank() && state.unlockDate.isAfter(LocalDate.now()) && !state.isSaving,
                    onClick = { viewModel.seal { showComposer = false } }
                ) { Text(if (state.isSaving) "封存中…" else "封存") }
            },
            dismissButton = { TextButton(onClick = { showComposer = false; viewModel.resetEditor() }) { Text("取消") } }
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.unlockDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.updateUnlockDate(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(pickerState) }
    }

    state.opened?.let { OpenedCapsuleDialog(it, onDismiss = viewModel::closeOpened) }
    deleteTarget?.let { summary ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除胶囊？") },
            text = { Text("可以在不提前展示内容的情况下删除，删除后无法恢复。") },
            confirmButton = { TextButton(onClick = { viewModel.delete(summary.id); deleteTarget = null }) { Text("删除") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun CapsuleRow(summary: TimeCapsuleSummary, onOpen: () -> Unit, onDelete: () -> Unit) {
    val unlocked = !summary.unlockAt.isAfter(java.time.LocalDateTime.now())
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (unlocked) Icons.Default.LockOpen else Icons.Default.Lock, null, modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(summary.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (unlocked) "已到期·点击开启" else "${summary.unlockAt.format(DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA))} 开启",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (summary.mediaCount > 0) Text("${summary.mediaCount} 个附件", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "删除胶囊") }
        }
    }
}

@Composable
private fun OpenedCapsuleDialog(capsule: TimeCapsule, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(capsule.title) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text(capsule.message, style = MaterialTheme.typography.bodyLarge) }
                if (capsule.prediction.isNotBlank()) item { Card { Column(Modifier.padding(12.dp)) { Text("当时的预测", fontWeight = FontWeight.Bold); Text(capsule.prediction) } } }
                if (capsule.question.isNotBlank()) item { Card { Column(Modifier.padding(12.dp)) { Text("当时留下的问题", fontWeight = FontWeight.Bold); Text(capsule.question) } } }
                items(capsule.mediaUris) { raw ->
                    OutlinedButton(onClick = {
                        val uri = Uri.parse(raw)
                        val type = context.contentResolver.getType(uri) ?: "*/*"
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, type).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) }
                    }) { Text("打开照片 / 语音附件") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("收好") } }
    )
}
