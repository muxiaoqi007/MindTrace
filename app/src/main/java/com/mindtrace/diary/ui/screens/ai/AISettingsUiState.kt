package com.mindtrace.diary.ui.screens.ai

import com.mindtrace.diary.core.datastore.AIConfig
import com.mindtrace.diary.core.datastore.ProactiveNudgeConfig

/**
 * AI 设置 UI 状态
 */
data class AISettingsUiState(
    val config: AIConfig = AIConfig(),
    val memoryLearningEnabled: Boolean = false,
    val proactiveNudgeConfig: ProactiveNudgeConfig = ProactiveNudgeConfig(),
    val isTesting: Boolean = false,
    val testResult: Boolean? = null,
    val error: String? = null
)
