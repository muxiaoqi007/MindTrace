package com.mindtrace.diary.ui.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mindtrace.diary.core.util.DateUtils
import com.mindtrace.diary.domain.model.ContentBlock
import com.mindtrace.diary.domain.model.DiaryEntry
import com.mindtrace.diary.domain.model.EntryType
import com.mindtrace.diary.domain.model.toImagePaths
import com.mindtrace.diary.domain.model.toPlainText
import com.mindtrace.diary.ui.components.ImageGallery
import com.mindtrace.diary.ui.components.MoodChip
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    diaryId: String,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: DiaryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除日记") },
            text = { Text("确定要删除这篇日记吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteDiary()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日记详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.diary == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "日记不存在",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val diary = uiState.diary!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Title
                if (diary.title.isNotEmpty()) {
                    Text(
                        text = diary.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Date and mood
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = DateUtils.formatDateTime(diary.createdAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    diary.mood?.let { mood ->
                        MoodChip(mood = mood)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content blocks (图文混排)
                if (diary.contentBlocks.isNotEmpty()) {
                    val context = LocalContext.current
                    diary.contentBlocks.forEach { block ->
                        when (block) {
                            is ContentBlock.Text -> {
                                if (block.text.isNotEmpty()) {
                                    Text(
                                        text = block.text,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            is ContentBlock.Image -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(block.path))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                if (block.caption.isNotEmpty()) {
                                    Text(
                                        text = block.caption,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    // 向后兼容：旧格式日记没有 contentBlocks
                    if (diary.images.isNotEmpty()) {
                        ImageGallery(images = diary.images)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (diary.content.isNotEmpty()) {
                        Text(
                            text = diary.content,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Flash Note Entries
                if (diary.entries.isNotEmpty()) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "补充记录",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    diary.entries.sortedByDescending { it.timestamp }.forEach { entry ->
                        FlashNoteEntryItem(entry = entry)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (diary.excludeFromAI) {
                    Text(
                        text = "这篇日记已关闭 AI 功能",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (uiState.followUpQuestion == null) {
                    OutlinedButton(
                        onClick = { viewModel.requestFollowUp() },
                        enabled = !uiState.isGeneratingFollowUp
                    ) {
                        if (uiState.isGeneratingFollowUp) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(if (uiState.isGeneratingFollowUp) "正在阅读…" else "请 AI 问我一个问题")
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("可选反思", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.height(6.dp))
                            Text(uiState.followUpQuestion.orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            uiState.followUpEvidence?.let { evidence ->
                                Text(
                                    "根据原文：“$evidence”",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                            OutlinedTextField(
                                value = uiState.followUpAnswer,
                                onValueChange = viewModel::updateFollowUpAnswer,
                                label = { Text("我的回答") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = viewModel::stopFollowUp) { Text("结束") }
                                TextButton(
                                    onClick = { viewModel.requestFollowUp(another = true) },
                                    enabled = !uiState.isGeneratingFollowUp
                                ) { Text("换一个") }
                                Button(
                                    onClick = viewModel::saveFollowUpAnswer,
                                    enabled = uiState.followUpAnswer.isNotBlank() && !uiState.isSavingFollowUp,
                                    modifier = Modifier.weight(1f)
                                ) { Text(if (uiState.isSavingFollowUp) "保存中…" else "保存回答") }
                            }
                        }
                    }
                }

                // Weather and location
                if (diary.weather != null || diary.location != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    diary.weather?.let { weather ->
                        Text(
                            text = "天气: $weather",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    diary.location?.let { location ->
                        Text(
                            text = "位置: $location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tags
                if (diary.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        diary.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashNoteEntryItem(
    entry: DiaryEntry,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Flash icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = if (entry.type == EntryType.REFLECTION) Icons.Default.AutoAwesome else Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = DateUtils.formatTime(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
