package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

/**
 * AI 长期记忆
 */
data class AIMemory(
    val id: String,
    val type: MemoryType,
    val category: MemoryCategory,
    val content: String,
    val source: String? = null,
    val importance: Float = 0.5f,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * 记忆类型
 */
enum class MemoryType {
    AUTO,    // 自动从日记/会话提取
    MANUAL;  // 用户手动设置

    companion object {
        fun fromString(value: String): MemoryType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: AUTO
        }
    }
}

/**
 * 记忆分类
 */
enum class MemoryCategory(val displayName: String) {
    PERSONALITY("性格特点"),
    PREFERENCE("偏好习惯"),
    FACT("个人事实"),
    EVENT("重要事件"),
    RELATIONSHIP("人际关系"),
    GOAL("目标愿望"),
    OTHER("其他");

    companion object {
        fun fromString(value: String): MemoryCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
