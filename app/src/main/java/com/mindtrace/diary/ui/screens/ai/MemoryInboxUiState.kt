package com.mindtrace.diary.ui.screens.ai

import com.mindtrace.diary.domain.model.AIMemoryCandidate

data class MemoryInboxUiState(
    val isLoading: Boolean = true,
    val candidates: List<AIMemoryCandidate> = emptyList(),
    val editingCandidate: AIMemoryCandidate? = null,
    val isProcessing: Boolean = false,
    val error: String? = null
)
