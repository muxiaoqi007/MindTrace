package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.ai.LLMProvider
import com.mindtrace.diary.core.ai.OpenAICompatibleProvider
import com.mindtrace.diary.core.datastore.AIConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMProviderFactoryImpl @Inject constructor() : LLMProviderFactory {
    override fun create(config: AIConfig): LLMProvider {
        return OpenAICompatibleProvider(config)
    }
}
