package com.mindtrace.diary.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * 标签选择器组件
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    selectedTags: List<String>,
    suggestedTags: List<String> = emptyList(),
    onTagAdd: (String) -> Unit,
    onTagRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newTagText by remember { mutableStateOf("") }
    var showAddField by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "标签",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 已选标签
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedTags.forEach { tag ->
                TagChip(
                    tag = tag,
                    selected = true,
                    onRemove = { onTagRemove(tag) }
                )
            }

            // 添加标签按钮
            if (!showAddField) {
                SuggestionChip(
                    onClick = { showAddField = true },
                    label = { Text("+") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "添加标签", modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // 添加标签输入框
        if (showAddField) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    placeholder = { Text("输入标签名称") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newTagText.isNotBlank()) {
                                onTagAdd(newTagText.trim())
                                newTagText = ""
                            }
                            showAddField = false
                        }
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        if (newTagText.isNotBlank()) {
                            onTagAdd(newTagText.trim())
                            newTagText = ""
                        }
                        showAddField = false
                    }
                ) {
                    Text("确定")
                }
            }
        }

        // 推荐标签
        val availableSuggestions = suggestedTags.filter { it !in selectedTags }
        if (availableSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "推荐标签",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableSuggestions.take(6).forEach { tag ->
                    TagChip(
                        tag = tag,
                        onClick = { onTagAdd(tag) }
                    )
                }
            }
        }
    }
}
