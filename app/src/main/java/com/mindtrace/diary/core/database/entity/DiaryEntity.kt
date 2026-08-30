package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "diaries")
data class DiaryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val images: List<String> = emptyList(),
    val contentBlocks: List<ContentBlockData> = emptyList(),
    val mood: String? = null,
    val weather: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val tags: List<String> = emptyList(),
    val entries: List<DiaryEntryData> = emptyList(),
    val date: Long = 0L,  // Start of day timestamp for grouping
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false,
    val excludeFromAI: Boolean = false,
    val excludeFromResurfacing: Boolean = false,
    // AI 分析字段
    val summary: String? = null,           // AI 生成的摘要
    val sentimentScore: Float? = null,     // 情感分数 (0-1, 0=负面, 1=正面)
    val aiTags: List<String> = emptyList() // AI 生成的标签
)
