package com.mindtrace.diary.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindtrace.diary.core.database.entity.TimeCapsuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeCapsuleDao {
    @Query("SELECT * FROM time_capsules ORDER BY unlockAt ASC")
    suspend fun getAllOnce(): List<TimeCapsuleEntity>
    @Query("SELECT * FROM time_capsules ORDER BY unlockAt ASC")
    fun observeAll(): Flow<List<TimeCapsuleEntity>>

    @Query("SELECT * FROM time_capsules WHERE id = :id")
    suspend fun getById(id: String): TimeCapsuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: TimeCapsuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(values: List<TimeCapsuleEntity>)

    @Query("UPDATE time_capsules SET openedAt = :openedAt WHERE id = :id")
    suspend fun markOpened(id: String, openedAt: Long)

    @Query("DELETE FROM time_capsules WHERE id = :id")
    suspend fun delete(id: String)
}
