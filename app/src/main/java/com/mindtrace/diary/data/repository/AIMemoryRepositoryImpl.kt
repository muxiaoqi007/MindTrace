package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.AIMemoryDao
import com.mindtrace.diary.core.database.entity.MemoryType as EntityMemoryType
import com.mindtrace.diary.data.mapper.toDomain
import com.mindtrace.diary.data.mapper.toEntity
import com.mindtrace.diary.domain.model.AIMemory
import com.mindtrace.diary.domain.model.MemoryCategory
import com.mindtrace.diary.domain.model.MemoryType
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIMemoryRepositoryImpl @Inject constructor(
    private val aiMemoryDao: AIMemoryDao
) : AIMemoryRepository {

    override fun getAllActiveMemories(): Flow<List<AIMemory>> {
        return aiMemoryDao.getAllActiveMemories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllMemories(): Flow<List<AIMemory>> {
        return aiMemoryDao.getAllMemories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMemoriesByType(type: MemoryType): Flow<List<AIMemory>> {
        return aiMemoryDao.getMemoriesByType(type.name.lowercase()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMemoriesByCategory(category: MemoryCategory): Flow<List<AIMemory>> {
        return aiMemoryDao.getMemoriesByCategory(category.name.lowercase()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMemoryById(id: String): AIMemory? {
        return aiMemoryDao.getMemoryById(id)?.toDomain()
    }

    override suspend fun addMemory(memory: AIMemory) {
        aiMemoryDao.insertMemory(memory.toEntity())
    }

    override suspend fun addManualMemory(
        content: String,
        category: MemoryCategory,
        importance: Float
    ): AIMemory {
        val now = LocalDateTime.now()
        val memory = AIMemory(
            id = UUID.randomUUID().toString(),
            type = MemoryType.MANUAL,
            category = category,
            content = content,
            source = "user_input",
            importance = importance,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
        aiMemoryDao.insertMemory(memory.toEntity())
        return memory
    }

    override suspend fun addAutoMemory(
        content: String,
        category: MemoryCategory,
        source: String,
        importance: Float
    ): AIMemory {
        val now = LocalDateTime.now()
        val memory = AIMemory(
            id = UUID.randomUUID().toString(),
            type = MemoryType.AUTO,
            category = category,
            content = content,
            source = source,
            importance = importance,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
        aiMemoryDao.insertMemory(memory.toEntity())
        return memory
    }

    override suspend fun updateMemory(memory: AIMemory) {
        aiMemoryDao.updateMemory(memory.toEntity())
    }

    override suspend fun setMemoryActive(id: String, isActive: Boolean) {
        aiMemoryDao.setMemoryActive(id, isActive)
    }

    override suspend fun deleteMemory(id: String) {
        aiMemoryDao.deleteMemory(id)
    }

    override suspend fun deleteAllAutoMemories() {
        aiMemoryDao.deleteMemoriesByType(EntityMemoryType.AUTO)
    }

    override fun getActiveMemoryCount(): Flow<Int> {
        return aiMemoryDao.getActiveMemoryCount()
    }

    override fun searchMemories(query: String): Flow<List<AIMemory>> {
        return aiMemoryDao.searchMemories(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMemorySummary(): String {
        val memories = getAllActiveMemories().first()
        if (memories.isEmpty()) return ""

        val grouped = memories.groupBy { it.category }
        val summaryParts = mutableListOf<String>()

        grouped.forEach { (category, categoryMemories) ->
            val items = categoryMemories.take(5).joinToString("；") { it.content }
            summaryParts.add("【${category.displayName}】$items")
        }

        return "关于用户的记忆：\n${summaryParts.joinToString("\n")}"
    }
}
