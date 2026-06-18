package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * AI 长期记忆实体
 */
@Entity(tableName = "ai_memories")
data class AIMemoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: String,                     // "auto" 自动提取, "manual" 手动设置
    val category: String,                 // 分类：personality, preference, fact, event, relationship
    val content: String,                  // 记忆内容
    val source: String? = null,           // 来源：diary_id, conversation_id, 或 "user_input"
    val importance: Float = 0.5f,         // 重要性 0-1
    val isActive: Boolean = true,         // 是否启用
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 记忆类型
 */
object MemoryType {
    const val AUTO = "auto"       // 自动从日记/会话提取
    const val MANUAL = "manual"   // 用户手动设置
}

/**
 * 记忆分类
 */
object MemoryCategory {
    const val PERSONALITY = "personality"   // 性格特点
    const val PREFERENCE = "preference"     // 偏好习惯
    const val FACT = "fact"                 // 个人事实（生日、职业等）
    const val EVENT = "event"               // 重要事件
    const val RELATIONSHIP = "relationship" // 人际关系
    const val GOAL = "goal"                 // 目标愿望
    const val OTHER = "other"               // 其他
}
