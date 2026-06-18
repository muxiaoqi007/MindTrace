package com.mindtrace.diary.ui.screens.ai

import com.mindtrace.diary.domain.model.AIMemory
import com.mindtrace.diary.domain.model.MemoryCategory

/**
 * 记忆管理 UI 状态
 */
data class MemoryManagementUiState(
    val memories: List<AIMemory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingMemory: AIMemory? = null,
    val selectedCategory: MemoryCategory? = null
)
