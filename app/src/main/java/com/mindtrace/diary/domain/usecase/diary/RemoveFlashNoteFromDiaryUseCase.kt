package com.mindtrace.diary.domain.usecase.diary

import com.mindtrace.diary.domain.repository.DiaryRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for removing a flash note entry from a diary.
 */
class RemoveFlashNoteFromDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    /**
     * Removes a flash note entry from the diary that contains it.
     * @param entryId The ID of the flash note entry to remove
     * @param diaryId The ID of the diary containing the entry
     */
    suspend operator fun invoke(entryId: String, diaryId: String) {
        val diary = diaryRepository.getDiaryById(diaryId) ?: return

        val updatedEntries = diary.entries.filter { it.id != entryId }

        val updatedDiary = diary.copy(
            entries = updatedEntries,
            updatedAt = LocalDateTime.now()
        )

        diaryRepository.updateDiary(updatedDiary)
    }
}
