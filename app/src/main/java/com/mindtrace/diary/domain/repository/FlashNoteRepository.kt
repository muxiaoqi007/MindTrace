package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.FlashNote
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FlashNoteRepository {
    fun getAllFlashNotes(): Flow<List<FlashNote>>
    fun getFlashNotesPaged(limit: Int, offset: Int): Flow<List<FlashNote>>
    suspend fun getFlashNoteById(id: String): FlashNote?
    fun getFlashNoteByIdFlow(id: String): Flow<FlashNote?>
    fun getFlashNotesByDate(date: LocalDate): Flow<List<FlashNote>>
    fun searchFlashNotes(query: String): Flow<List<FlashNote>>
    fun getFlashNoteCount(): Flow<Int>
    suspend fun insertFlashNote(flashNote: FlashNote)
    suspend fun updateFlashNote(flashNote: FlashNote)
    suspend fun deleteFlashNote(id: String)
    suspend fun getUnsyncedFlashNotes(): List<FlashNote>
    suspend fun updateSyncTime(id: String, syncedAt: Long)
}
