package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

/**
 * "AI 眼中的你"：基于长期记忆 + 日记统计生成的第二人称画像叙事。
 * 由 AI 生成并缓存，用户可手动刷新。
 */
data class SelfNarrative(
    val narrative: String,
    val keywords: List<String>,
    val suggestion: String,
    val generatedAt: LocalDateTime
)
