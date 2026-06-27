package com.mindtrace.diary.domain.usecase.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mindtrace.diary.core.ai.ChatMessage
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.data.repository.LLMProviderFactory
import com.mindtrace.diary.domain.model.MemoryCategory
import com.mindtrace.diary.domain.repository.AIMemoryCandidateRepository
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 从日记中自动提取关于用户的长期记忆。
 *
 * 这是「越用越懂我」的核心：每次保存日记后调用 LLM 抽取稳定、有助于日后
 * 理解用户的信息（性格、偏好、人际关系、目标等），去重后写入 AIMemory
 * （type=AUTO）。仅在用户开启「自动学习记忆」且 AI 已配置时运行。
 *
 * 失败不抛出给调用方（返回 Result.failure），保存日记的主流程不受影响。
 */
class ExtractMemoryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val aiMemoryRepository: AIMemoryRepository,
    private val aiMemoryCandidateRepository: AIMemoryCandidateRepository,
    private val settingsDataStore: SettingsDataStore,
    private val llmProviderFactory: LLMProviderFactory
) {
    companion object {
        private const val MIN_CONTENT_LENGTH = 30
        private const val MAX_MEMORIES_PER_DIARY = 5
        private const val MAX_CONTENT_LENGTH = 60

        private const val EXTRACTION_PROMPT = """你是一个善于观察的记忆助手。请从下面这篇日记中，提取「关于用户本人的、长期稳定、有助于日后理解 ta 的信息」。

只提取真正值得长期记住的内容，例如：性格特点、稳定的偏好与习惯、重要的个人事实、影响深远的事件、重要的人际关系、长期的目标与愿望。
不要提取：一次性的琐事、天气、当天的临时情绪波动、泛泛的感慨。
如果没有值得长期记住的内容，请返回空数组。

category 只能取以下之一：
- PERSONALITY（性格特点）
- PREFERENCE（偏好习惯）
- FACT（个人事实）
- EVENT（重要事件）
- RELATIONSHIP（人际关系）
- GOAL（目标愿望）
- OTHER（其他）

importance 为 0 到 1 的小数，越重要越接近 1。
每条 content 用简洁的第三人称陈述（不超过 30 字），就像在为 ta 写备忘。

严格按以下 JSON 格式返回，不要包含任何其他文字：
{"memories": [{"category": "PREFERENCE", "content": "喜欢在咖啡馆写作", "importance": 0.7}]}

日记内容：
"""
    }

    /**
     * @return 本次新增的记忆内容列表（已去重）。AI 未启用/未开启学习/内容过短时返回空列表。
     */
    suspend operator fun invoke(diaryId: String): Result<List<String>> {
        return try {
            val aiConfig = settingsDataStore.aiConfig.first()
            if (!aiConfig.isConfigured || !aiConfig.enabled) {
                return Result.success(emptyList())
            }
            if (!settingsDataStore.memoryLearningEnabled.first()) {
                return Result.success(emptyList())
            }

            val diary = diaryRepository.getDiaryById(diaryId)
                ?: return Result.success(emptyList())
            if (diary.content.length < MIN_CONTENT_LENGTH) {
                return Result.success(emptyList())
            }

            val provider = llmProviderFactory.create(aiConfig)
            val messages = listOf(
                ChatMessage(ChatMessage.Role.USER, EXTRACTION_PROMPT + diary.content)
            )
            val response = provider.chat(messages)
            val content = response.getOrNull()?.content
                ?: return Result.failure(response.exceptionOrNull() ?: Exception("提取记忆失败"))

            val extracted = parseMemories(content)
            if (extracted.isEmpty()) {
                return Result.success(emptyList())
            }

            // 去重：跳过与现有活跃记忆或同日记待确认候选内容相同的条目（归一化后比较）
            val existing = aiMemoryRepository.getAllActiveMemories().first()
                .mapTo(mutableSetOf()) { it.content.trim().lowercase() }
            val source = "diary:$diaryId"
            aiMemoryCandidateRepository.getPendingCandidatesBySource(source)
                .mapTo(existing) { it.content.trim().lowercase() }

            val added = mutableListOf<String>()
            for (item in extracted.take(MAX_MEMORIES_PER_DIARY)) {
                val normalized = item.content.trim().lowercase()
                if (normalized.isEmpty() || !existing.add(normalized)) continue
                aiMemoryCandidateRepository.addCandidate(
                    content = item.content.trim(),
                    category = item.category,
                    source = source,
                    importance = item.importance
                )
                added.add(item.content.trim())
            }

            Result.success(added)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class ExtractedMemory(
        val category: MemoryCategory,
        val content: String,
        val importance: Float
    )

    private fun parseMemories(raw: String): List<ExtractedMemory> {
        return try {
            // 贪婪匹配最外层 JSON 对象（数组中含有嵌套的 {}，不能用 [^}]+）
            val jsonText = Regex("""\{[\s\S]*\}""").find(raw)?.value ?: return emptyList()
            val obj = Gson().fromJson(jsonText, JsonObject::class.java)
            val arr = obj.getAsJsonArray("memories") ?: return emptyList()
            arr.mapNotNull { element ->
                val o = element.asJsonObject
                val text = o.get("content")?.asString?.trim().orEmpty()
                if (text.isEmpty()) return@mapNotNull null
                val category = MemoryCategory.fromString(o.get("category")?.asString ?: "OTHER")
                val importance = (o.get("importance")?.asFloat ?: 0.5f).coerceIn(0f, 1f)
                ExtractedMemory(category, text.take(MAX_CONTENT_LENGTH), importance)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
