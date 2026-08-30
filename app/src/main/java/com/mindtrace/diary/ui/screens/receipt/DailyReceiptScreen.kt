package com.mindtrace.diary.ui.screens.receipt

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.core.export.DailyReceiptShareManager
import com.mindtrace.diary.domain.model.DailyReceipt
import com.mindtrace.diary.domain.model.MoodLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ReceiptPaper = Color(0xFFFFFCF3)
private val ReceiptInk = Color(0xFF29251F)
private val ReceiptMuted = Color(0xFF716B61)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReceiptScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyReceiptViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSharing by remember { mutableStateOf(false) }

    fun shareReceipt(receipt: DailyReceipt) {
        if (isSharing) return
        scope.launch {
            isSharing = true
            runCatching {
                val shareIntent = withContext(Dispatchers.IO) {
                    DailyReceiptShareManager.createShareIntent(context, receipt)
                }
                context.startActivity(Intent.createChooser(shareIntent, "分享每日小票"))
            }.onFailure { error ->
                snackbarHostState.showSnackbar(error.message ?: "小票图片生成失败")
            }
            isSharing = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("每日小票") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { uiState.receipt?.let(::shareReceipt) },
                        enabled = uiState.receipt != null && !uiState.isLoading && !isSharing
                    ) {
                        if (isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Share, contentDescription = "分享小票图片")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DateNavigator(
                date = uiState.selectedDate,
                canGoForward = uiState.canGoForward,
                onPrevious = viewModel::previousDay,
                onNext = viewModel::nextDay,
                onToday = viewModel::today
            )

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                uiState.error != null -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                uiState.receipt != null -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        DailyReceiptCard(
                            receipt = uiState.receipt!!,
                            modifier = Modifier.widthIn(max = 520.dp)
                        )
                    }
                    item {
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { uiState.receipt?.let(::shareReceipt) },
                            enabled = !isSharing
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("分享为图片")
                        }
                    }
                }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "前一天")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)),
                style = MaterialTheme.typography.titleMedium
            )
            if (date != LocalDate.now()) {
                TextButton(onClick = onToday, modifier = Modifier.height(36.dp)) {
                    Text("回到今天")
                }
            } else {
                Text(
                    text = "今天",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
        }
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = "后一天")
        }
    }
}

@Composable
private fun DailyReceiptCard(
    receipt: DailyReceipt,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = ReceiptPaper,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = "MINDTRACE",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ReceiptInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "今日生活小票",
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                color = ReceiptMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = receipt.date.format(
                    DateTimeFormatter.ofPattern("yyyy / MM / dd   EEEE", Locale.CHINA)
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = ReceiptInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            ReceiptDivider()

            ReceiptRow("心情", receipt.mood?.let(::moodText) ?: "未记录")
            ReceiptRow("写下日记", "${receipt.diaryCount} 篇")
            ReceiptRow("收集闪念", "${receipt.flashNoteCount} 条")
            ReceiptRow("完成待办", "${receipt.completedTodoCount} 项")
            ReceiptRow("仍在路上", "${receipt.pendingTodoCount} 项")
            ReceiptRow("日记字数", "${receipt.wordCount} 字")

            ReceiptDivider()
            Text(
                text = "今日一句",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ReceiptInk
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = receipt.highlight?.let { "“$it”" } ?: "今天还没有留下文字记录。",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                color = ReceiptInk
            )

            if (receipt.keywords.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "今日关键词",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ReceiptInk
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = receipt.keywords.joinToString("  /  "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = ReceiptMuted
                )
            }

            ReceiptDivider()
            ReceiptRow(
                label = "TOTAL",
                value = if (receipt.hasContent) "认真生活了 1 天" else "等待第一笔记录",
                bold = true
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "所有内容均由本机记录生成",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = ReceiptMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = ReceiptInk
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = ReceiptInk,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ReceiptDivider() {
    Spacer(Modifier.height(18.dp))
    HorizontalDivider(color = ReceiptMuted.copy(alpha = 0.45f))
    Spacer(Modifier.height(18.dp))
}

private fun moodText(mood: MoodLevel): String = when (mood) {
    MoodLevel.GREAT -> "非常好  ◉‿◉"
    MoodLevel.GOOD -> "不错  ◕‿◕"
    MoodLevel.OKAY -> "平静  ·‿·"
    MoodLevel.BAD -> "有点低落  ◔̯◔"
    MoodLevel.AWFUL -> "很难熬  ◡︵◡"
}
