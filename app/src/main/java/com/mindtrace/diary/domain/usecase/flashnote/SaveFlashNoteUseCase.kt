package com.mindtrace.diary.domain.usecase.flashnote

import com.mindtrace.diary.domain.model.FlashNote
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class SaveFlashNoteUseCase @Inject constructor(
    private val flashNoteRepository: FlashNoteRepository
) {
    suspend operator fun invoke(
        id: String? = null,
        content: String,
        images: List<String> = emptyList()
    ) {
        val now = LocalDateTime.now()
        val flashNote = FlashNote(
            id = id ?: UUID.randomUUID().toString(),
            content = content,
            images = images,
            createdAt = if (id == null) now else flashNoteRepository.getFlashNoteById(id)?.createdAt ?: now,
            updatedAt = now
        )

        if (id == null) {
            flashNoteRepository.insertFlashNote(flashNote)
        } else {
            flashNoteRepository.updateFlashNote(flashNote)
        }
    }
}
