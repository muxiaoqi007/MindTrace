package com.mindtrace.diary.domain.usecase.flashnote

import com.mindtrace.diary.domain.repository.FlashNoteRepository
import javax.inject.Inject

class DeleteFlashNoteUseCase @Inject constructor(
    private val flashNoteRepository: FlashNoteRepository
) {
    suspend operator fun invoke(id: String) {
        flashNoteRepository.deleteFlashNote(id)
    }
}
