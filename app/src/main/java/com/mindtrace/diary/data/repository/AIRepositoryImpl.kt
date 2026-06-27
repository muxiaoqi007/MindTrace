package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.ai.ChatMessage
import com.mindtrace.diary.core.ai.ChatResponse
import com.mindtrace.diary.core.ai.LLMProvider
import com.mindtrace.diary.core.ai.StreamChunk
import com.mindtrace.diary.core.database.dao.AIMemoryDao
import com.mindtrace.diary.core.datastore.AIConfig
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.data.mapper.toDomain
import com.mindtrace.diary.domain.model.ChatPersona
import com.mindtrace.diary.domain.repository.AIRepository
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.TodoRepository
import com.mindtrace.diary.domain.usecase.ai.BuildAIContextUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val diaryRepository: DiaryRepository,
    private val todoRepository: TodoRepository,
    private val llmProviderFactory: LLMProviderFactory,
    private val aiMemoryDao: AIMemoryDao,
    private val buildAIContextUseCase: BuildAIContextUseCase
) : AIRepository {

    private val _conversationHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    private var currentProvider: LLMProvider? = null
    private var currentConfig: AIConfig? = null

    private suspend fun getProvider(): LLMProvider? {
        val config = settingsDataStore.aiConfig.first()
        if (!config.isConfigured || !config.enabled) {
            return null
        }

        // 如果配置变化，重新创建 provider
        if (currentConfig != config) {
            currentConfig = config
            currentProvider = llmProviderFactory.create(config)
        }

        return currentProvider
    }

    override suspend fun chat(
        messages: List<ChatMessage>,
        includeContext: Boolean
    ): Result<ChatResponse> {
        val provider = getProvider()
            ?: return Result.failure(IllegalStateException("AI 未配置或未启用"))

        val config = currentConfig ?: return Result.failure(IllegalStateException("AI 配置无效"))

        val fullMessages = buildMessageList(messages, includeContext, config)
        return provider.chat(fullMessages)
    }

    override fun chatStream(
        messages: List<ChatMessage>,
        includeContext: Boolean
    ): Flow<StreamChunk> = flow {
        val provider = getProvider()
        if (provider == null) {
            emit(StreamChunk("AI 未配置或未启用", isFinished = true))
            return@flow
        }

        val config = currentConfig
        if (config == null) {
            emit(StreamChunk("AI 配置无效", isFinished = true))
            return@flow
        }

        val fullMessages = buildMessageList(messages, includeContext, config)
        provider.chatStream(fullMessages).collect { chunk ->
            emit(chunk)
        }
    }

    private suspend fun buildMessageList(
        messages: List<ChatMessage>,
        includeContext: Boolean,
        config: AIConfig
    ): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()

        // 添加系统提示词（根据所选 AI 人格；CUSTOM 时使用用户自定义提示词）
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
            val query = messages
                .filter { it.role == ChatMessage.Role.USER }
                .joinToString("\n") { it.content }
            val aiContext = buildAIContextUseCase(query)
            if (aiContext.isNotBlank()) {
                systemPrompt += "\n\n$aiContext"
            }
        }
        result.add(ChatMessage(ChatMessage.Role.SYSTEM, systemPrompt))

        // 添加对话历史
        result.addAll(_conversationHistory.value)

        // 添加当前消息
        result.addAll(messages)

        return result
    }

    /**
     * 获取长期记忆摘要
     */
    private suspend fun getMemorySummary(): String {
        return try {
            val memories = aiMemoryDao.getAllActiveMemories().first()
            if (memories.isEmpty()) return ""

            val grouped = memories.map { it.toDomain() }.groupBy { it.category }
            val summaryParts = mutableListOf<String>()

            grouped.forEach { (category, categoryMemories) ->
                val items = categoryMemories.take(5).joinToString("；") { it.content }
                summaryParts.add("【${category.displayName}】$items")
            }

            "关于用户的长期记忆：\n${summaryParts.joinToString("\n")}"
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun testConnection(): Boolean {
        val provider = getProvider() ?: return false
        return provider.testConnection()
    }

    override fun getConversationHistory(): Flow<List<ChatMessage>> {
        return _conversationHistory.asStateFlow()
    }

    override suspend fun addToHistory(message: ChatMessage) {
        val current = _conversationHistory.value.toMutableList()
        current.add(message)
        // 保留最近 20 条消息，避免上下文过长
        if (current.size > 20) {
            _conversationHistory.value = current.takeLast(20)
        } else {
            _conversationHistory.value = current
        }
    }

    override suspend fun clearHistory() {
        _conversationHistory.value = emptyList()
    }

    override suspend fun getDiaryContextSummary(): String {
        return try {
            val recentDiaries = diaryRepository.getDiariesPaged(5, 0).first()
            if (recentDiaries.isEmpty()) {
                return ""
            }

            val today = LocalDate.now()
            val summaries = recentDiaries.mapNotNull { diary ->
                val diaryDate = diary.date ?: diary.createdAt.toLocalDate()
                val dateLabel = formatRelativeDate(diaryDate, today)
                val mood = diary.mood?.let { "心情: ${it.name}" } ?: ""
                val tags = if (diary.tags.isNotEmpty()) "标签: ${diary.tags.joinToString(", ")}" else ""
                val preview = diary.content.take(100).replace("\n", " ")

                buildString {
                    append("[$dateLabel] ")
                    if (mood.isNotBlank()) append("$mood ")
                    if (tags.isNotBlank()) append("$tags ")
                    append("- $preview")
                    if (diary.content.length > 100) append("...")
                }
            }

            "最近日记摘要：\n${summaries.joinToString("\n")}"
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 格式化相对日期（今天、昨天、前天、具体日期）
     */
    private fun formatRelativeDate(date: LocalDate, today: LocalDate): String {
        val daysDiff = ChronoUnit.DAYS.between(date, today)
        return when {
            daysDiff == 0L -> "今天"
            daysDiff == 1L -> "昨天"
            daysDiff == 2L -> "前天"
            daysDiff in 3..6 -> "${daysDiff}天前"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MM月dd日")
                date.format(formatter)
            }
        }
    }

    /**
     * 获取待办上下文摘要
     */
    private suspend fun getTodoContextSummary(): String {
        return try {
            val pendingTodos = todoRepository.getPendingTodos().first()
            if (pendingTodos.isEmpty()) {
                return ""
            }

            val today = LocalDate.now()
            val summaries = pendingTodos.take(10).map { todo ->
                val dueInfo = todo.dueDate?.let { dueDateTime ->
                    val dueDate = dueDateTime.toLocalDate()
                    val dateLabel = formatRelativeDate(dueDate, today)
                    val isOverdue = dueDate.isBefore(today)
                    if (isOverdue) "（已过期: $dateLabel）" else "（截止: $dateLabel）"
                } ?: ""
                val priorityLabel = when (todo.priority) {
                    com.mindtrace.diary.domain.model.Priority.HIGH -> "【高优先级】"
                    com.mindtrace.diary.domain.model.Priority.MEDIUM -> ""
                    com.mindtrace.diary.domain.model.Priority.LOW -> "【低优先级】"
                }
                "$priorityLabel${todo.content}$dueInfo"
            }

            "用户的待办事项（未完成）：\n${summaries.joinToString("\n• ", prefix = "• ")}"
        } catch (e: Exception) {
            ""
        }
    }
}

/**
 * LLM Provider 工厂
 * 根据配置创建对应的 Provider
 */
interface LLMProviderFactory {
    fun create(config: AIConfig): LLMProvider
}
