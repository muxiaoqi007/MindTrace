package com.mindtrace.diary.ui.screens.onesecond

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mindtrace.diary.core.export.OneSecondMontageExporter
import com.mindtrace.diary.domain.model.DailyMediaSlot
import com.mindtrace.diary.domain.model.DailyMediaType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneSecondLifeScreen(onNavigateBack: () -> Unit, viewModel: OneSecondLifeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var pickDate by remember { mutableStateOf<LocalDate?>(null) }
    var deleteDate by remember { mutableStateOf<LocalDate?>(null) }
    var showMediaSource by remember { mutableStateOf(false) }
    var pendingCaptureFile by remember { mutableStateOf<File?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val date = pickDate
        if (uri != null && date != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            val type = if (context.contentResolver.getType(uri)?.startsWith("video/") == true) DailyMediaType.VIDEO else DailyMediaType.IMAGE
            viewModel.save(date, uri.toString(), type)
        }
        pickDate = null
    }
    val videoCapture = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { saved ->
        val date = pickDate
        val file = pendingCaptureFile
        if (saved && date != null && file != null) {
            viewModel.save(date, Uri.fromFile(file).toString(), DailyMediaType.VIDEO)
        } else if (file != null) {
            file.delete()
        }
        pendingCaptureFile = null
        pickDate = null
    }
    fun launchVideoCapture() {
        val directory = File(context.filesDir, "daily_media").apply { mkdirs() }
        val file = File(directory, "capture-${UUID.randomUUID()}.mp4")
        pendingCaptureFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        videoCapture.launch(uri)
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchVideoCapture() else {
            pickDate = null
        }
    }

    LaunchedEffect(state.exportedFile) {
        state.exportedFile?.let { file ->
            context.startActivity(Intent.createChooser(OneSecondMontageExporter.shareIntent(context, file), "分享一秒人生"))
            viewModel.consumeExport()
        }
    }
    LaunchedEffect(state.error) { state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text("一秒人生") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
        }) }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("每天留下一帧，也允许某天空着。", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("视频只取前 1 秒；月度合成按日期排序并加上日期字幕。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { MonthHeader(state.month, viewModel::previousMonth, viewModel::nextMonth) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth()) { listOf("一", "二", "三", "四", "五", "六", "日").forEach { Text(it, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    state.slots.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            week.forEach { slot ->
                                MediaDayCell(slot, Modifier.weight(1f), onPick = {
                                    if (!slot.date.isAfter(LocalDate.now())) { pickDate = slot.date; showMediaSource = true }
                                }, onDelete = { deleteDate = slot.date })
                            }
                            repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("每晚 20:00 提醒", fontWeight = FontWeight.Bold)
                            Text("提醒只是邀请，没有记录也不会补齐。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(state.reminderEnabled, viewModel::setReminder)
                    }
                }
            }
            item {
                Button(
                    onClick = viewModel::export,
                    enabled = state.slots.any { it.pick != null } && !state.isExporting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isExporting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Icon(Icons.Default.Movie, null)
                    Text(if (state.isExporting) "  正在合成…" else "  合成本月视频")
                }
            }
        }
    }
    deleteDate?.let { date ->
        AlertDialog(
            onDismissRequest = { deleteDate = null }, title = { Text("移除 $date 的片段？") },
            text = { Text("只会移除选择记录，不会删除手机中的原照片或视频。") },
            confirmButton = { TextButton(onClick = { viewModel.delete(date); deleteDate = null }) { Text("移除") } },
            dismissButton = { TextButton(onClick = { deleteDate = null }) { Text("取消") } }
        )
    }
    if (showMediaSource) {
        AlertDialog(
            onDismissRequest = { showMediaSource = false; pickDate = null },
            title = { Text("留下今天的一秒") },
            text = { Text("可以从相册选择，也可以现在拍一小段视频。成片只会使用前 1 秒。") },
            confirmButton = {
                TextButton(onClick = {
                    showMediaSource = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        launchVideoCapture()
                    } else cameraPermission.launch(Manifest.permission.CAMERA)
                }) { Icon(Icons.Default.Videocam, null); Text("  现在拍摄") }
            },
            dismissButton = {
                TextButton(onClick = { showMediaSource = false; picker.launch(arrayOf("image/*", "video/*")) }) {
                    Icon(Icons.Default.AddPhotoAlternate, null); Text("  从相册选")
                }
            }
        )
    }
}

@Composable
private fun MonthHeader(month: YearMonth, previous: () -> Unit, next: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = previous) { Icon(Icons.AutoMirrored.Filled.ArrowLeft, "上一月") }
        Text(month.format(DateTimeFormatter.ofPattern("yyyy年 M月", Locale.CHINA)), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = next, enabled = month.isBefore(YearMonth.now())) { Icon(Icons.AutoMirrored.Filled.ArrowRight, "下一月") }
    }
}

@Composable
private fun MediaDayCell(slot: DailyMediaSlot, modifier: Modifier, onPick: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onPick),
        contentAlignment = Alignment.Center
    ) {
        slot.pick?.let { pick ->
            AsyncImage(pick.uri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (pick.type == DailyMediaType.VIDEO) Icon(Icons.Default.PlayCircle, null)
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) { Icon(Icons.Default.Delete, "移除", Modifier.size(16.dp)) }
        } ?: Icon(Icons.Default.AddPhotoAlternate, "选择 ${slot.date} 的媒体", tint = MaterialTheme.colorScheme.outline)
        Text(slot.date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomStart).background(MaterialTheme.colorScheme.surface.copy(alpha = .75f)).padding(horizontal = 4.dp))
    }
}
