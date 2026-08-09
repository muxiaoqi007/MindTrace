package com.mindtrace.diary.core.database.dao

import androidx.room.*
import com.mindtrace.diary.core.database.entity.FlashNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashNoteDao {
    @Query("SELECT * FROM flash_notes WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllFlashNotes(): Flow<List<FlashNoteEntity>>

    @Query("SELECT * FROM flash_notes WHERE isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllFlashNotesOnce(): List<FlashNoteEntity>

    @Query("SELECT * FROM flash_notes ORDER BY createdAt DESC")
    suspend fun getAllFlashNotesForSync(): List<FlashNoteEntity>

    @Query("SELECT * FROM flash_notes WHERE isDeleted = 0 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun getFlashNotesPaged(limit: Int, offset: Int): Flow<List<FlashNoteEntity>>

    @Query("SELECT * FROM flash_notes WHERE id = :id AND isDeleted = 0")
    suspend fun getFlashNoteById(id: String): FlashNoteEntity?

    @Query("SELECT * FROM flash_notes WHERE id = :id AND isDeleted = 0")
    fun getFlashNoteByIdFlow(id: String): Flow<FlashNoteEntity?>

    @Query("""
        SELECT * FROM flash_notes
        WHERE isDeleted = 0
        AND createdAt >= :startTime
        AND createdAt < :endTime
        ORDER BY createdAt DESC
    """)
    fun getFlashNotesByDateRange(startTime: Long, endTime: Long): Flow<List<FlashNoteEntity>>

    @Query("""
        SELECT * FROM flash_notes
        WHERE isDeleted = 0
        AND content LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchFlashNotes(query: String): Flow<List<FlashNoteEntity>>

    @Query("SELECT * FROM flash_notes WHERE syncedAt IS NULL OR updatedAt > syncedAt")
    suspend fun getUnsyncedFlashNotes(): List<FlashNoteEntity>

    @Query("SELECT COUNT(*) FROM flash_notes WHERE isDeleted = 0")
    fun getFlashNoteCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashNote(flashNote: FlashNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashNotes(flashNotes: List<FlashNoteEntity>)

    @Update
    suspend fun updateFlashNote(flashNote: FlashNoteEntity)

    @Query("UPDATE flash_notes SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteFlashNote(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM flash_notes WHERE id = :id")
    suspend fun deleteFlashNote(id: String)

    @Query("DELETE FROM flash_notes")
    suspend fun deleteAllFlashNotes()

    @Query("UPDATE flash_notes SET syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncTime(id: String, syncedAt: Long)
}
