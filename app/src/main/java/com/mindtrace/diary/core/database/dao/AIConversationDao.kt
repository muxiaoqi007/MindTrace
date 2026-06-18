package com.mindtrace.diary.core.database.dao

import androidx.room.*
import com.mindtrace.diary.core.database.entity.AIConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AIConversationDao {

    @Query("SELECT * FROM ai_conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<AIConversationEntity>>

    @Query("SELECT * FROM ai_conversations ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentConversations(limit: Int): Flow<List<AIConversationEntity>>

    @Query("SELECT * FROM ai_conversations WHERE id = :id")
    suspend fun getConversationById(id: String): AIConversationEntity?

    @Query("SELECT * FROM ai_conversations WHERE id = :id")
    fun getConversationByIdFlow(id: String): Flow<AIConversationEntity?>

    @Query("SELECT * FROM ai_conversations WHERE relatedDiaryId = :diaryId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getConversationByDiaryId(diaryId: String): AIConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AIConversationEntity)

    @Update
    suspend fun updateConversation(conversation: AIConversationEntity)

    @Query("DELETE FROM ai_conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("DELETE FROM ai_conversations")
    suspend fun deleteAllConversations()

    @Query("SELECT COUNT(*) FROM ai_conversations")
    fun getConversationCount(): Flow<Int>
}
