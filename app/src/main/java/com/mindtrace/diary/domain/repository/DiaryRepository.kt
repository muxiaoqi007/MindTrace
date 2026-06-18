package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.Diary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DiaryRepository {
    fun getAllDiaries(): Flow<List<Diary>>
    fun getDiariesPaged(limit: Int, offset: Int): Flow<List<Diary>>
    suspend fun getDiaryById(id: String): Diary?
    fun getDiaryByIdFlow(id: String): Flow<Diary?>
    suspend fun getDiaryByDate(date: LocalDate): Diary?
    fun getDiariesByDate(date: LocalDate): Flow<List<Diary>>
    fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>>
    fun searchDiaries(query: String): Flow<List<Diary>>
    fun getHistoryOnThisDay(): Flow<List<Diary>>
    fun getDiaryCount(): Flow<Int>
    fun getTotalWordCount(): Flow<Int>
    suspend fun insertDiary(diary: Diary)
    suspend fun updateDiary(diary: Diary)
    suspend fun deleteDiary(id: String)
    suspend fun getUnsyncedDiaries(): List<Diary>
    suspend fun updateSyncTime(id: String, syncedAt: Long)

    // 标签相关
    suspend fun getAllTags(): List<String>
    fun getDiariesByTag(tag: String): Flow<List<Diary>>
}
