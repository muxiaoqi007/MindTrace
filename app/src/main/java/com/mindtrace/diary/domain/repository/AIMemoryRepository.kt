package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.AIMemory
import com.mindtrace.diary.domain.model.MemoryCategory
import com.mindtrace.diary.domain.model.MemoryType
import kotlinx.coroutines.flow.Flow

/**
 * AI 记忆仓库接口
 */
interface AIMemoryRepository {
    /**
     * 获取所有活跃记忆
     */
    fun getAllActiveMemories(): Flow<List<AIMemory>>

    /**
     * 获取所有记忆（包括禁用的）
     */
    fun getAllMemories(): Flow<List<AIMemory>>

    /**
     * 按类型获取记忆
     */
    fun getMemoriesByType(type: MemoryType): Flow<List<AIMemory>>

    /**
     * 按分类获取记忆
     */
    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<AIMemory>>

    /**
     * 根据 ID 获取记忆
     */
    suspend fun getMemoryById(id: String): AIMemory?

    /**
     * 添加记忆
     */
    suspend fun addMemory(memory: AIMemory)

    /**
     * 添加手动记忆
     */
    suspend fun addManualMemory(
        content: String,
        category: MemoryCategory,
        importance: Float = 0.8f
    ): AIMemory

    /**
     * 添加自动记忆
     */
    suspend fun addAutoMemory(
        content: String,
        category: MemoryCategory,
        source: String,
        importance: Float = 0.5f
    ): AIMemory

    /**
     * 更新记忆
     */
    suspend fun updateMemory(memory: AIMemory)

    /**
     * 设置记忆启用状态
     */
    suspend fun setMemoryActive(id: String, isActive: Boolean)

    /**
     * 删除记忆
     */
    suspend fun deleteMemory(id: String)

    /**
     * 删除所有自动记忆
     */
    suspend fun deleteAllAutoMemories()

    /**
     * 获取活跃记忆数量
     */
    fun getActiveMemoryCount(): Flow<Int>

    /**
     * 搜索记忆
     */
    fun searchMemories(query: String): Flow<List<AIMemory>>

    /**
     * 获取记忆摘要（用于 AI 上下文）
     */
    suspend fun getMemorySummary(): String
}
