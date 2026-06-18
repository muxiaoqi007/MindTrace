package com.mindtrace.diary.core.database.dao

import androidx.room.*
import com.mindtrace.diary.core.database.entity.AIMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AIMemoryDao {

    @Query("SELECT * FROM ai_memories WHERE isActive = 1 ORDER BY importance DESC, updatedAt DESC")
    fun getAllActiveMemories(): Flow<List<AIMemoryEntity>>

    @Query("SELECT * FROM ai_memories ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<AIMemoryEntity>>

    @Query("SELECT * FROM ai_memories WHERE type = :type AND isActive = 1 ORDER BY importance DESC")
    fun getMemoriesByType(type: String): Flow<List<AIMemoryEntity>>

    @Query("SELECT * FROM ai_memories WHERE category = :category AND isActive = 1 ORDER BY importance DESC")
    fun getMemoriesByCategory(category: String): Flow<List<AIMemoryEntity>>

    @Query("SELECT * FROM ai_memories WHERE id = :id")
    suspend fun getMemoryById(id: String): AIMemoryEntity?

    @Query("SELECT * FROM ai_memories WHERE source = :source AND isActive = 1")
    suspend fun getMemoriesBySource(source: String): List<AIMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AIMemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<AIMemoryEntity>)

    @Update
    suspend fun updateMemory(memory: AIMemoryEntity)

    @Query("UPDATE ai_memories SET isActive = :isActive WHERE id = :id")
    suspend fun setMemoryActive(id: String, isActive: Boolean)

    @Query("DELETE FROM ai_memories WHERE id = :id")
    suspend fun deleteMemory(id: String)

    @Query("DELETE FROM ai_memories WHERE type = :type")
    suspend fun deleteMemoriesByType(type: String)

    @Query("DELETE FROM ai_memories")
    suspend fun deleteAllMemories()

    @Query("SELECT COUNT(*) FROM ai_memories WHERE isActive = 1")
    fun getActiveMemoryCount(): Flow<Int>

    // 搜索记忆
    @Query("SELECT * FROM ai_memories WHERE isActive = 1 AND content LIKE '%' || :query || '%' ORDER BY importance DESC")
    fun searchMemories(query: String): Flow<List<AIMemoryEntity>>
}
