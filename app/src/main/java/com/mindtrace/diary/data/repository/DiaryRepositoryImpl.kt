package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.DiaryDao
import com.mindtrace.diary.core.database.entity.DiaryEntity
import com.mindtrace.diary.core.database.entity.DiaryEntryData
import com.mindtrace.diary.core.util.DateUtils
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.DiaryEntry
import com.mindtrace.diary.domain.model.EntryType
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao
) : DiaryRepository {

    override fun getAllDiaries(): Flow<List<Diary>> {
        return diaryDao.getAllDiaries().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDiariesPaged(limit: Int, offset: Int): Flow<List<Diary>> {
        return diaryDao.getDiariesPaged(limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDiaryById(id: String): Diary? {
        return diaryDao.getDiaryById(id)?.toDomain()
    }

    override fun getDiaryByIdFlow(id: String): Flow<Diary?> {
        return diaryDao.getDiaryByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun getDiaryByDate(date: LocalDate): Diary? {
        val dateTimestamp = DateUtils.getStartOfDay(date)
        return diaryDao.getDiaryByDate(dateTimestamp)?.toDomain()
    }

    override fun getDiariesByDate(date: LocalDate): Flow<List<Diary>> {
        val startTime = DateUtils.getStartOfDay(date)
        val endTime = DateUtils.getEndOfDay(date)
        return diaryDao.getDiariesByDateRange(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>> {
        val startTime = DateUtils.getStartOfDay(startDate)
        val endTime = DateUtils.getEndOfDay(endDate)
        return diaryDao.getDiariesByDateRange(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchDiaries(query: String): Flow<List<Diary>> {
        return diaryDao.searchDiaries(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHistoryOnThisDay(): Flow<List<Diary>> {
        val today = LocalDate.now()
        val monthDay = today.format(DateTimeFormatter.ofPattern("MM-dd"))
        val currentYear = today.year.toString()
        return diaryDao.getHistoryOnThisDay(monthDay, currentYear).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDiaryCount(): Flow<Int> {
        return diaryDao.getDiaryCount()
    }

    override fun getTotalWordCount(): Flow<Int> {
        return diaryDao.getTotalWordCount().map { it ?: 0 }
    }

    override suspend fun insertDiary(diary: Diary) {
        diaryDao.insertDiary(diary.toEntity())
    }

    override suspend fun updateDiary(diary: Diary) {
        diaryDao.updateDiary(diary.toEntity())
    }

    override suspend fun deleteDiary(id: String) {
        diaryDao.softDeleteDiary(id)
    }

    override suspend fun getUnsyncedDiaries(): List<Diary> {
        return diaryDao.getUnsyncedDiaries().map { it.toDomain() }
    }

    override suspend fun updateSyncTime(id: String, syncedAt: Long) {
        diaryDao.updateSyncTime(id, syncedAt)
    }

    override suspend fun getAllTags(): List<String> {
        // getAllTagsRaw 返回的是序列化后的 JSON 数组字符串列表
        // 需要解析每个字符串并收集所有唯一标签
        val rawTags = diaryDao.getAllTagsRaw()
        return rawTags
            .flatMap { tagsJson ->
                // Room Converters 会将 List<String> 序列化为 JSON 数组
                // 这里需要手动解析或者重新查询
                try {
                    com.google.gson.Gson().fromJson(tagsJson, Array<String>::class.java)?.toList() ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            .distinct()
            .sorted()
    }

    override fun getDiariesByTag(tag: String): Flow<List<Diary>> {
        return diaryDao.getDiariesByTag(tag).map { entities ->
            entities.filter { entity ->
                // 精确匹配标签（因为 SQL LIKE 可能会匹配到包含子字符串的情况）
                entity.tags.contains(tag)
            }.map { it.toDomain() }
        }
    }

    private fun DiaryEntity.toDomain(): Diary {
        return Diary(
            id = id,
            title = title,
            content = content,
            images = images,
            mood = MoodLevel.fromLegacyMoodType(mood),
            weather = weather,
            location = location,
            tags = tags,
            entries = entries.map { it.toDomainEntry() },
            date = if (date > 0) DateUtils.fromEpochMillis(date).toLocalDate() else null,
            createdAt = DateUtils.fromEpochMillis(createdAt),
            updatedAt = DateUtils.fromEpochMillis(updatedAt),
            syncedAt = syncedAt?.let { DateUtils.fromEpochMillis(it) },
            isDeleted = isDeleted,
            summary = summary,
            sentimentScore = sentimentScore,
            aiTags = aiTags
        )
    }

    private fun Diary.toEntity(): DiaryEntity {
        return DiaryEntity(
            id = id,
            title = title,
            content = content,
            images = images,
            mood = mood?.name,
            weather = weather,
            location = location,
            tags = tags,
            entries = entries.map { it.toEntityEntry() },
            date = date?.let { DateUtils.getStartOfDay(it) } ?: DateUtils.getStartOfDay(createdAt.toLocalDate()),
            createdAt = DateUtils.toEpochMillis(createdAt),
            updatedAt = DateUtils.toEpochMillis(updatedAt),
            syncedAt = syncedAt?.let { DateUtils.toEpochMillis(it) },
            isDeleted = isDeleted,
            summary = summary,
            sentimentScore = sentimentScore,
            aiTags = aiTags
        )
    }

    private fun DiaryEntryData.toDomainEntry(): DiaryEntry {
        return DiaryEntry(
            id = id,
            content = content,
            images = images,
            timestamp = DateUtils.fromEpochMillis(timestamp),
            type = EntryType.fromString(type) ?: EntryType.FLASH_NOTE
        )
    }

    private fun DiaryEntry.toEntityEntry(): DiaryEntryData {
        return DiaryEntryData(
            id = id,
            content = content,
            images = images,
            timestamp = DateUtils.toEpochMillis(timestamp),
            type = type.name
        )
    }
}
