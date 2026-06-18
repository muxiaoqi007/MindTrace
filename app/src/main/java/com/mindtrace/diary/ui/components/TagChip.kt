package com.mindtrace.diary.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 标签 Chip 组件
 */
@Composable
fun TagChip(
    tag: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    if (onRemove != null) {
        // 带删除按钮的标签
        InputChip(
            selected = selected,
            onClick = { onClick?.invoke() },
            label = { Text(tag) },
            trailingIcon = {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "移除标签",
                        modifier = Modifier.size(14.dp)
                    )
                }
            },
            modifier = modifier
        )
    } else {
        // 普通标签
        SuggestionChip(
            onClick = { onClick?.invoke() },
            label = { Text(tag) },
            modifier = modifier
        )
    }
}

/**
 * 可点击的标签（用于标签管理页面）
 */
@Composable
fun TagItemChip(
    tag: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Badge {
                Text(count.toString())
            }
        }
    }
}
