package com.mindtrace.diary.domain.usecase.flashnote

import com.mindtrace.diary.domain.model.FlashNote
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllFlashNotesUseCase @Inject constructor(
    private val flashNoteRepository: FlashNoteRepository
) {
    operator fun invoke(): Flow<List<FlashNote>> {
        return flashNoteRepository.getAllFlashNotes()
    }
}
