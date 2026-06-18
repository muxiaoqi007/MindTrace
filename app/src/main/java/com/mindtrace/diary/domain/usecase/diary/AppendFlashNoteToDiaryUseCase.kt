package com.mindtrace.diary.domain.usecase.diary

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.DiaryEntry
import com.mindtrace.diary.domain.model.EntryType
import com.mindtrace.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for appending a flash note to today's diary.
 * If no diary exists for today, creates a new one.
 */
class AppendFlashNoteToDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    /**
     * Appends a flash note with the given content and optional images to today's diary.
     * @param content The flash note content
     * @param images Optional list of image paths
     * @return The updated or newly created diary
     */
    suspend operator fun invoke(content: String, images: List<String> = emptyList()): Diary {
        val today = LocalDate.now()
        val now = LocalDateTime.now()

        val entry = DiaryEntry(
            id = UUID.randomUUID().toString(),
            content = content,
            images = images,
            timestamp = now,
            type = EntryType.FLASH_NOTE
        )

        val existingDiary = diaryRepository.getDiaryByDate(today)

        return if (existingDiary != null) {
            // Append to existing diary
            val updatedDiary = existingDiary.copy(
                entries = existingDiary.entries + entry,
                updatedAt = now
            )
            diaryRepository.updateDiary(updatedDiary)
            updatedDiary
        } else {
            // Create new diary for today
            val newDiary = Diary(
                id = UUID.randomUUID().toString(),
                title = "",
                content = "",
                entries = listOf(entry),
                date = today,
                createdAt = now,
                updatedAt = now
            )
            diaryRepository.insertDiary(newDiary)
            newDiary
        }
    }
}
