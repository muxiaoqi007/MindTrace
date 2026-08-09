package com.mindtrace.diary.core.database.dao

import androidx.room.*
import com.mindtrace.diary.core.database.entity.DiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diaries WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diaries WHERE isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllDiariesOnce(): List<DiaryEntity>

    @Query("SELECT * FROM diaries ORDER BY createdAt DESC")
    suspend fun getAllDiariesForSync(): List<DiaryEntity>

    @Query("SELECT * FROM diaries WHERE isDeleted = 0 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun getDiariesPaged(limit: Int, offset: Int): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diaries WHERE id = :id AND isDeleted = 0")
    suspend fun getDiaryById(id: String): DiaryEntity?

    @Query("SELECT * FROM diaries WHERE id = :id AND isDeleted = 0")
    fun getDiaryByIdFlow(id: String): Flow<DiaryEntity?>

    @Query("SELECT * FROM diaries WHERE date = :date AND isDeleted = 0 LIMIT 1")
    suspend fun getDiaryByDate(date: Long): DiaryEntity?

    @Query("""
        SELECT * FROM diaries
        WHERE isDeleted = 0
        AND createdAt >= :startTime
        AND createdAt < :endTime
        ORDER BY createdAt DESC
    """)
    fun getDiariesByDateRange(startTime: Long, endTime: Long): Flow<List<DiaryEntity>>

    @Query("""
        SELECT * FROM diaries
        WHERE isDeleted = 0
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchDiaries(query: String): Flow<List<DiaryEntity>>

    @Query("""
        SELECT DISTINCT strftime('%Y-%m-%d', createdAt / 1000, 'unixepoch', 'localtime') as date,
               mood, images
        FROM diaries
        WHERE isDeleted = 0
        AND createdAt >= :startTime
        AND createdAt < :endTime
        ORDER BY createdAt ASC
    """)
    suspend fun getDiaryDatesWithMood(startTime: Long, endTime: Long): List<DiaryDateInfo>

    @Query("""
        SELECT * FROM diaries
        WHERE isDeleted = 0
        AND strftime('%m-%d', createdAt / 1000, 'unixepoch', 'localtime') = :monthDay
        AND strftime('%Y', createdAt / 1000, 'unixepoch', 'localtime') != :currentYear
        ORDER BY createdAt DESC
    """)
    fun getHistoryOnThisDay(monthDay: String, currentYear: String): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diaries WHERE syncedAt IS NULL OR updatedAt > syncedAt")
    suspend fun getUnsyncedDiaries(): List<DiaryEntity>

    @Query("SELECT COUNT(*) FROM diaries WHERE isDeleted = 0")
    fun getDiaryCount(): Flow<Int>

    @Query("""
        SELECT SUM(LENGTH(content)) FROM diaries WHERE isDeleted = 0
    """)
    fun getTotalWordCount(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaries(diaries: List<DiaryEntity>)

    @Update
    suspend fun updateDiary(diary: DiaryEntity)

    @Query("UPDATE diaries SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteDiary(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM diaries WHERE id = :id")
    suspend fun deleteDiary(id: String)

    @Query("DELETE FROM diaries")
    suspend fun deleteAllDiaries()

    @Query("UPDATE diaries SET syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncTime(id: String, syncedAt: Long)

    // 标签相关查询
    @Query("SELECT DISTINCT tags FROM diaries WHERE isDeleted = 0")
    suspend fun getAllTagsRaw(): List<String>

    @Query("""
        SELECT * FROM diaries
        WHERE isDeleted = 0
        AND tags LIKE '%' || :tag || '%'
        ORDER BY createdAt DESC
    """)
    fun getDiariesByTag(tag: String): Flow<List<DiaryEntity>>
}

data class DiaryDateInfo(
    val date: String,
    val mood: String?,
    val images: List<String>
)
