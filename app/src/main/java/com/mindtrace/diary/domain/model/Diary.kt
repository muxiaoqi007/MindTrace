package com.mindtrace.diary.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Diary(
    val id: String,
    val title: String,
    val content: String,
    val images: List<String> = emptyList(),
    val mood: MoodLevel? = null,
    val weather: String? = null,
    val location: String? = null,
    val tags: List<String> = emptyList(),
    val entries: List<DiaryEntry> = emptyList(),
    val date: LocalDate? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val syncedAt: LocalDateTime? = null,
    val isDeleted: Boolean = false,
    // AI 分析字段
    val summary: String? = null,           // AI 生成的摘要
    val sentimentScore: Float? = null,     // 情感分数 (0-1, 0=负面, 1=正面)
    val aiTags: List<String> = emptyList() // AI 生成的标签
)
