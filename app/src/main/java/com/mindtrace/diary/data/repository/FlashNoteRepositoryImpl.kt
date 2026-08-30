package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.FlashNoteDao
import com.mindtrace.diary.core.database.entity.FlashNoteEntity
import com.mindtrace.diary.core.util.DateUtils
import com.mindtrace.diary.domain.model.FlashNote
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashNoteRepositoryImpl @Inject constructor(
    private val flashNoteDao: FlashNoteDao
) : FlashNoteRepository {

    override fun getAllFlashNotes(): Flow<List<FlashNote>> {
        return flashNoteDao.getAllFlashNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFlashNotesPaged(limit: Int, offset: Int): Flow<List<FlashNote>> {
        return flashNoteDao.getFlashNotesPaged(limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getFlashNoteById(id: String): FlashNote? {
        return flashNoteDao.getFlashNoteById(id)?.toDomain()
    }

    override fun getFlashNoteByIdFlow(id: String): Flow<FlashNote?> {
        return flashNoteDao.getFlashNoteByIdFlow(id).map { it?.toDomain() }
    }

    override fun getFlashNotesByDate(date: LocalDate): Flow<List<FlashNote>> {
        val startTime = DateUtils.getStartOfDay(date)
        val endTime = DateUtils.getEndOfDay(date)
        return flashNoteDao.getFlashNotesByDateRange(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchFlashNotes(query: String): Flow<List<FlashNote>> {
        return flashNoteDao.searchFlashNotes(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFlashNoteCount(): Flow<Int> {
        return flashNoteDao.getFlashNoteCount()
    }

    override suspend fun insertFlashNote(flashNote: FlashNote) {
        flashNoteDao.insertFlashNote(flashNote.toEntity())
    }

    override suspend fun updateFlashNote(flashNote: FlashNote) {
        flashNoteDao.updateFlashNote(flashNote.toEntity())
    }

    override suspend fun deleteFlashNote(id: String) {
        flashNoteDao.softDeleteFlashNote(id)
    }

    override suspend fun getUnsyncedFlashNotes(): List<FlashNote> {
        return flashNoteDao.getUnsyncedFlashNotes().map { it.toDomain() }
    }

    override suspend fun updateSyncTime(id: String, syncedAt: Long) {
        flashNoteDao.updateSyncTime(id, syncedAt)
    }

    private fun FlashNoteEntity.toDomain(): FlashNote {
        return FlashNote(
            id = id,
            content = content,
            images = images,
            createdAt = DateUtils.fromEpochMillis(createdAt),
            updatedAt = DateUtils.fromEpochMillis(updatedAt),
            syncedAt = syncedAt?.let { DateUtils.fromEpochMillis(it) },
            isDeleted = isDeleted,
            excludeFromAI = excludeFromAI,
            excludeFromResurfacing = excludeFromResurfacing
        )
    }

    private fun FlashNote.toEntity(): FlashNoteEntity {
        return FlashNoteEntity(
            id = id,
            content = content,
            images = images,
            createdAt = DateUtils.toEpochMillis(createdAt),
            updatedAt = DateUtils.toEpochMillis(updatedAt),
            syncedAt = syncedAt?.let { DateUtils.toEpochMillis(it) },
            isDeleted = isDeleted,
            excludeFromAI = excludeFromAI,
            excludeFromResurfacing = excludeFromResurfacing
        )
    }
}
