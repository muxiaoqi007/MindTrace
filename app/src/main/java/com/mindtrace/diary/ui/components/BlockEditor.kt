package com.mindtrace.diary.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mindtrace.diary.core.util.ImageUtils
import com.mindtrace.diary.domain.model.ContentBlock
import kotlinx.coroutines.launch
import java.io.File

/**
 * 块级图文混排编辑器
 *
 * 参考一本日记等 App 的设计：
 * - 每个文字块是独立的 TextField
 * - 图片块全宽圆角显示
 * - 底部工具栏可插入文字/图片
 */
@Composable
fun BlockEditor(
    blocks: List<ContentBlock>,
    onBlocksChange: (List<ContentBlock>) -> Unit,
    onInsertTextBlock: (afterBlockId: String?) -> Unit,
    onInsertImageBlock: (afterBlockId: String?, imagePath: String) -> Unit,
    onDeleteBlock: (blockId: String) -> Unit,
    onUpdateText: (blockId: String, text: String) -> Unit,
    onUpdateImageCaption: (blockId: String, caption: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var focusedBlockId by remember { mutableStateOf<String?>(null) }

    // 图片选择器
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            scope.launch {
                ImageUtils.saveImage(context, uri)?.let { path ->
                    onInsertImageBlock(focusedBlockId, path)
                }
            }
        }
    }

    Column(modifier = modifier) {
        // 内容块列表
        blocks.forEachIndexed { index, block ->
            when (block) {
                is ContentBlock.Text -> {
                    TextBlockView(
                        block = block,
                        onTextChange = { newText -> onUpdateText(block.id, newText) },
                        onFocusChanged = { focused ->
                            if (focused) focusedBlockId = block.id
                        },
                        onDeleteIfEmpty = {
                            // 只有当不是唯一一个文字块时才允许删除
                            val textBlocks = blocks.filterIsInstance<ContentBlock.Text>()
                            if (textBlocks.size > 1) {
                                onDeleteBlock(block.id)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is ContentBlock.Image -> {
                    ImageBlockView(
                        block = block,
                        onDelete = { onDeleteBlock(block.id) },
                        onCaptionChange = { caption -> onUpdateImageCaption(block.id, caption) },
                        onFocusChanged = { focused ->
                            if (focused) focusedBlockId = block.id
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 底部工具栏
        BlockToolbar(
            onAddText = {
                val lastBlockId = blocks.lastOrNull()?.id
                onInsertTextBlock(lastBlockId)
            },
            onAddImage = { imageLauncher.launch("image/*") }
        )
    }
}

/**
 * 文字块视图
 */
@Composable
private fun TextBlockView(
    block: ContentBlock.Text,
    onTextChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onDeleteIfEmpty: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var previousText by remember(block.id) { mutableStateOf(block.text) }

    BasicTextField(
        value = block.text,
        onValueChange = { newText ->
            // 如果文字从有变空，且之前有内容，触发删除
            if (newText.isEmpty() && previousText.isNotEmpty()) {
                onDeleteIfEmpty()
            } else {
                onTextChange(newText)
            }
            previousText = newText
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                if (block.text.isEmpty() && !isFocused) {
                    Text(
                        text = "写下你的想法…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusChanged(focusState.isFocused)
            }
    )
}

/**
 * 图片块视图 - 全宽圆角显示
 */
@Composable
private fun ImageBlockView(
    block: ContentBlock.Image,
    onDelete: () -> Unit,
    onCaptionChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Box {
            // 图片
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

            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除图片",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 图片标题（可选）
        BasicTextField(
            value = block.caption,
            onValueChange = onCaptionChange,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    if (block.caption.isEmpty() && !isFocused) {
                        Text(
                            text = "添加图片说明…（可选）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    onFocusChanged(focusState.isFocused)
                }
        )
    }
}

/**
 * 底部工具栏
 */
@Composable
private fun BlockToolbar(
    onAddText: () -> Unit,
    onAddImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 添加文字按钮
        FilledTonalIconButton(
            onClick = onAddText,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = "添加文字",
                modifier = Modifier.size(20.dp)
            )
        }

        // 添加图片按钮
        FilledTonalIconButton(
            onClick = onAddImage,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "添加图片",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
