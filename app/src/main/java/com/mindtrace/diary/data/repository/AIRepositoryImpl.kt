package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.ai.AIContextEnvelope
import com.mindtrace.diary.core.ai.AIMessageBudget
import com.mindtrace.diary.core.ai.ChatMessage
import com.mindtrace.diary.core.ai.ChatResponse
import com.mindtrace.diary.core.ai.LLMProvider
import com.mindtrace.diary.core.ai.StreamChunk
import com.mindtrace.diary.core.datastore.AIConfig
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.domain.model.ChatPersona
import com.mindtrace.diary.domain.repository.AIRepository
import com.mindtrace.diary.domain.usecase.ai.BuildAIContextUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val llmProviderFactory: LLMProviderFactory,
    private val buildAIContextUseCase: BuildAIContextUseCase
) : AIRepository {
    private var currentProvider: LLMProvider? = null
    private var currentConfig: AIConfig? = null
    private val providerMutex = Mutex()

    private suspend fun getProvider(): LLMProvider? {
        val config = settingsDataStore.aiConfig.first()
        if (!config.isConfigured || !config.enabled) return null

        return providerMutex.withLock {
            if (currentConfig != config || currentProvider == null) {
                currentProvider = llmProviderFactory.create(config)
                currentConfig = config
            }
            currentProvider
        }
    }

    override suspend fun chat(
        messages: List<ChatMessage>,
        includeContext: Boolean
    ): Result<ChatResponse> {
        val provider = getProvider()
            ?: return Result.failure(IllegalStateException("AI 未配置或未启用"))
        return provider.chat(buildMessageList(messages, includeContext))
    }

    override fun chatStream(
        messages: List<ChatMessage>,
        includeContext: Boolean
    ): Flow<StreamChunk> = flow {
        val provider = getProvider()
            ?: throw IllegalStateException("AI 未配置或未启用")
        provider.chatStream(buildMessageList(messages, includeContext)).collect(::emit)
    }

    private suspend fun buildMessageList(
        messages: List<ChatMessage>,
        includeContext: Boolean
    ): List<ChatMessage> {
        val soulConfig = settingsDataStore.soulConfig.first()
        val persona = ChatPersona.fromId(soulConfig.chatPersonaId)
        var systemPrompt = if (persona == ChatPersona.CUSTOM) {
            soulConfig.customSystemPrompt
        } else {
            persona.systemPrompt
        }

        val soulContext = buildString {
            if (soulConfig.userDisplayName.isNotBlank()) {
                append("- 用户希望被称呼为：${soulConfig.userDisplayName}\n")
            }
            if (soulConfig.aiDisplayName.isNotBlank()) {
                append("- 你的名字是：${soulConfig.aiDisplayName}\n")
            }
            if (soulConfig.relationshipNote.isNotBlank()) {
                append("- 用户希望你这样相处：${soulConfig.relationshipNote}\n")
            }
        }.trim()
        if (soulContext.isNotBlank()) {
            systemPrompt += "\n\n当前相处配置：\n$soulContext"
        }

        if (includeContext) {
            val latestQuery = messages.lastOrNull { it.role == ChatMessage.Role.USER }?.content.orEmpty()
            val personalContext = buildAIContextUseCase(latestQuery)
            if (personalContext.isNotBlank()) {
                systemPrompt += "\n\n${AIContextEnvelope.wrap(personalContext)}"
            }
        }

        val boundedHistory = AIMessageBudget.trim(
            messages.filter { message -> message.role != ChatMessage.Role.SYSTEM }
        )
        return listOf(ChatMessage(ChatMessage.Role.SYSTEM, systemPrompt)) + boundedHistory
    }

    override suspend fun testConnection(): Boolean = getProvider()?.testConnection() ?: false
}
