package com.mindtrace.diary.ui.screens.ai

import com.mindtrace.diary.domain.model.MemoryCategory

data class MemoryCenterUiState(
    val isLoading: Boolean = true,
    val totalMemoryCount: Int = 0,
    val activeMemoryCount: Int = 0,
    val pendingCandidateCount: Int = 0,
    val categoryCounts: Map<MemoryCategory, Int> = emptyMap(),
    val error: String? = null
)
