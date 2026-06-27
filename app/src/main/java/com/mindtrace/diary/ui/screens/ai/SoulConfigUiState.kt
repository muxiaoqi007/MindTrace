package com.mindtrace.diary.ui.screens.ai

import com.mindtrace.diary.core.datastore.SoulConfig

data class SoulConfigUiState(
    val isLoading: Boolean = true,
    val config: SoulConfig = SoulConfig(),
    val isSaved: Boolean = false,
    val error: String? = null
)
