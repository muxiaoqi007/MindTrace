package com.mindtrace.diary.domain.usecase.review

import com.mindtrace.diary.core.ai.ChatMessage
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.domain.model.AiReview
import com.mindtrace.diary.domain.model.MidnightReviewPersona
import com.mindtrace.diary.domain.model.ReviewType
import com.mindtrace.diary.domain.repository.AiReviewRepository
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.data.repository.LLMProviderFactory
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * 生成深夜回信的用例
 */
class GenerateMidnightReviewUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val aiReviewRepository: AiReviewRepository,
    private val settingsDataStore: SettingsDataStore,
    private val llmProviderFactory: LLMProviderFactory
) {
    /**
     * 生成今日的深夜回信
     * @return 生成的回信，如果今日无日记或生成失败则返回 null
     */
    suspend operator fun invoke(): Result<AiReview?> {
        return try {
            // 1. 检查 AI 是否配置
            val aiConfig = settingsDataStore.aiConfig.first()
            if (!aiConfig.isConfigured || !aiConfig.enabled) {
                return Result.success(null)
            }

            // 2. 获取深夜回信配置
            val reviewConfig = settingsDataStore.midnightReviewConfig.first()
            if (!reviewConfig.enabled) {
                return Result.success(null)
            }

            // 3. 获取今日日记
            val today = LocalDate.now()

            val todayDiaries = diaryRepository.getDiariesByDateRange(today, today.plusDays(1)).first()
            if (todayDiaries.isEmpty()) {
                return Result.success(null)
            }

            // 4. 检查今日是否已有回信
            val existingReview = aiReviewRepository.getReviewByDate(today)
            if (existingReview != null) {
                return Result.success(existingReview)
            }

            // 5. 构建日记内容摘要
            val diaryContent = todayDiaries.joinToString("\n\n") { diary ->
                buildString {
                    if (diary.title.isNotBlank()) {
                        append("【${diary.title}】\n")
                    }
                    append(diary.content)
                    diary.mood?.let { mood ->
                        append("\n心情: ${mood.name}")
                    }
                }
            }

            // 6. 获取 Persona
            val persona = MidnightReviewPersona.fromId(reviewConfig.persona)

            // 7. 获取昨天的用户回复（如果有）
            val yesterdayReview = aiReviewRepository.getReviewWithReplyByDate(today.minusDays(1))
            val userReplyContext = yesterdayReview?.userReply?.let { reply ->
                "\n\n---\n用户对昨天回信的回复：$reply"
            } ?: ""

            // 8. 调用 LLM 生成回信
            val provider = llmProviderFactory.create(aiConfig)
            val messages = listOf(
                ChatMessage(ChatMessage.Role.SYSTEM, persona.systemPrompt),
                ChatMessage(
                    ChatMessage.Role.USER,
                    """以下是我今天的日记，请给我写一封简短的回信（不超过80字）：

$diaryContent$userReplyContext"""
                )
            )

            val response = provider.chat(messages)
            if (response.isFailure) {
                return Result.failure(response.exceptionOrNull() ?: Exception("生成回信失败"))
            }

            val content = response.getOrNull()?.content ?: return Result.failure(Exception("回信内容为空"))

            // 9. 保存回信
            val review = AiReview(
                id = UUID.randomUUID().toString(),
                date = today,
                content = content.take(200), // 限制长度
                diaryIds = todayDiaries.map { it.id },
                persona = persona,
                isRead = false,
                createdAt = System.currentTimeMillis(),
                type = ReviewType.MIDNIGHT_REVIEW
            )

            aiReviewRepository.saveReview(review)

            Result.success(review)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
