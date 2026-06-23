package com.mindtrace.diary.ui.screens.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.ui.components.BlockEditor
import com.mindtrace.diary.ui.components.MoodSelector
import com.mindtrace.diary.ui.components.TagSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditScreen(
    diaryId: String?,
    onNavigateBack: () -> Unit,
    viewModel: DiaryEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val suggestedTags by viewModel.suggestedTags.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (diaryId == null) "写日记" else "编辑日记") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveDiary() },
                        enabled = !uiState.isSaving && uiState.title.isNotBlank()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "保存")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = androidx.compose.ui.Alignment.Center,
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
                // 标题输入
                BasicTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.title.isEmpty()) {
                                Text(
                                    text = "标题",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // 图文混排编辑器
                BlockEditor(
                    blocks = uiState.contentBlocks,
                    onBlocksChange = { viewModel.updateContentBlocks(it) },
                    onInsertTextBlock = { afterId -> viewModel.insertTextBlock(afterId) },
                    onInsertImageBlock = { afterId, path -> viewModel.insertImageBlock(afterId, path) },
                    onDeleteBlock = { blockId -> viewModel.deleteBlock(blockId) },
                    onUpdateText = { blockId, text -> viewModel.updateBlockText(blockId, text) },
                    onUpdateImageCaption = { blockId, caption -> viewModel.updateImageCaption(blockId, caption) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.height(24.dp))

                MoodSelector(
                    selectedMood = uiState.mood,
                    onMoodSelected = viewModel::updateMood
                )

                Spacer(modifier = Modifier.height(24.dp))

                TagSelector(
                    selectedTags = uiState.tags,
                    suggestedTags = suggestedTags,
                    onTagAdd = viewModel::addTag,
                    onTagRemove = viewModel::removeTag
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
