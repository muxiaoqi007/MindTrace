package com.mindtrace.diary.core.database.dao

import androidx.room.*
import com.mindtrace.diary.core.database.entity.MoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM moods WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllMoods(): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE id = :id AND isDeleted = 0")
    suspend fun getMoodById(id: String): MoodEntity?

    @Query("""
        SELECT * FROM moods
        WHERE isDeleted = 0
        AND date >= :startTime
        AND date < :endTime
        ORDER BY date DESC
    """)
    fun getMoodsByDateRange(startTime: Long, endTime: Long): Flow<List<MoodEntity>>

    @Query("""
        SELECT moodType, COUNT(*) as count
        FROM moods
        WHERE isDeleted = 0
        AND date >= :startTime
        AND date < :endTime
        GROUP BY moodType
    """)
    suspend fun getMoodDistribution(startTime: Long, endTime: Long): List<MoodCount>

    @Query("SELECT * FROM moods WHERE syncedAt IS NULL OR date > syncedAt")
    suspend fun getUnsyncedMoods(): List<MoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoods(moods: List<MoodEntity>)

    @Update
    suspend fun updateMood(mood: MoodEntity)

    @Query("UPDATE moods SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteMood(id: String)

    @Query("DELETE FROM moods WHERE id = :id")
    suspend fun deleteMood(id: String)

    @Query("UPDATE moods SET syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncTime(id: String, syncedAt: Long)
}

data class MoodCount(
    val moodType: String,
    val count: Int
)
