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
        images: List<String> = emptyList(),
        excludeFromAI: Boolean? = null,
        excludeFromResurfacing: Boolean? = null
    ) {
        val now = LocalDateTime.now()
        val existing = id?.let { flashNoteRepository.getFlashNoteById(it) }
        val flashNote = FlashNote(
            id = id ?: UUID.randomUUID().toString(),
            content = content,
            images = images,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            excludeFromAI = excludeFromAI ?: existing?.excludeFromAI ?: false,
            excludeFromResurfacing = excludeFromResurfacing
                ?: existing?.excludeFromResurfacing
                ?: false
        )

        if (id == null) {
            flashNoteRepository.insertFlashNote(flashNote)
        } else {
            flashNoteRepository.updateFlashNote(flashNote)
        }
    }
}
