package com.mindtrace.diary.ui.screens.ai

import com.mindtrace.diary.domain.model.SelfPortrait

data class SelfPortraitUiState(
    val isLoading: Boolean = true,
    val portrait: SelfPortrait? = null,
    val error: String? = null
)
