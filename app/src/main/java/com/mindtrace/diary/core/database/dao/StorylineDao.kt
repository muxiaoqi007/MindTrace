package com.mindtrace.diary.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mindtrace.diary.core.database.entity.StorylineEntity
import com.mindtrace.diary.core.database.entity.StorylineSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StorylineDao {
    @Query("SELECT * FROM storylines ORDER BY updatedAt DESC")
    suspend fun getAllStorylinesOnce(): List<StorylineEntity>

    @Query("SELECT * FROM storyline_sources ORDER BY date ASC")
    suspend fun getAllSourcesOnce(): List<StorylineSourceEntity>
    @Query("SELECT * FROM storylines ORDER BY updatedAt DESC")
    fun observeStorylines(): Flow<List<StorylineEntity>>

    @Query("SELECT * FROM storyline_sources ORDER BY date ASC")
    fun observeSources(): Flow<List<StorylineSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStoryline(value: StorylineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStorylines(values: List<StorylineEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStoryline(value: StorylineEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSources(values: List<StorylineSourceEntity>)

    @Query("UPDATE storylines SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query("UPDATE storylines SET name = :name, normalizedName = :normalized, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rename(id: String, name: String, normalized: String, updatedAt: Long)

    @Query("INSERT OR IGNORE INTO storyline_sources (id, storylineId, diaryId, date, excerpt) SELECT id, :targetId, diaryId, date, excerpt FROM storyline_sources WHERE storylineId = :sourceId")
    suspend fun copySources(sourceId: String, targetId: String)

    @Query("DELETE FROM storyline_sources WHERE storylineId = :storylineId")
    suspend fun deleteSources(storylineId: String)

    @Query("DELETE FROM storylines WHERE id = :id")
    suspend fun deleteStoryline(id: String)

    @Query("DELETE FROM storyline_sources WHERE id = :sourceId")
    suspend fun deleteSource(sourceId: String)

    @Transaction
    suspend fun merge(sourceId: String, targetId: String) {
        copySources(sourceId, targetId)
        deleteSources(sourceId)
        deleteStoryline(sourceId)
    }
}
