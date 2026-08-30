package com.mindtrace.diary.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindtrace.diary.core.database.entity.DailyMediaPickEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMediaPickDao {
    @Query("SELECT * FROM daily_media_picks ORDER BY date ASC") suspend fun getAllOnce(): List<DailyMediaPickEntity>
    @Query("SELECT * FROM daily_media_picks ORDER BY date ASC") fun observeAll(): Flow<List<DailyMediaPickEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(value: DailyMediaPickEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(values: List<DailyMediaPickEntity>)
    @Query("DELETE FROM daily_media_picks WHERE date = :date") suspend fun deleteDate(date: Long)
}
