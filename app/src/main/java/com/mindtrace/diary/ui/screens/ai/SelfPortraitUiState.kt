package com.mindtrace.diary.ui.screens.ai

import com.mindtrace.diary.domain.model.SelfNarrative
import com.mindtrace.diary.domain.model.SelfPortrait

data class SelfPortraitUiState(
    val isLoading: Boolean = true,
    val portrait: SelfPortrait? = null,
    /** AI 生成的叙事画像（"AI 眼中的你"），可能尚未生成 */
    val narrative: SelfNarrative? = null,
    val isNarrativeLoading: Boolean = false,
    val isAIConfigured: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
