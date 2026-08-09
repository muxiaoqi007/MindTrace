package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.ai.LLMProvider
import com.mindtrace.diary.core.datastore.AIConfig

interface LLMProviderFactory {
    fun create(config: AIConfig): LLMProvider
}
