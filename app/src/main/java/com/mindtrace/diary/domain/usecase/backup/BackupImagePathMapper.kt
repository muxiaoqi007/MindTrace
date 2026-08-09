package com.mindtrace.diary.domain.usecase.backup

import com.mindtrace.diary.core.database.entity.DiaryEntity
import com.mindtrace.diary.core.database.entity.FlashNoteEntity

/** Keeps all compatibility representations of an image path in sync. */
object BackupImagePathMapper {
    fun collectImagePaths(
        diaries: List<DiaryEntity>,
        flashNotes: List<FlashNoteEntity>
    ): Set<String> = buildSet {
        diaries.forEach { diary ->
            addAll(diary.images)
            diary.contentBlocks.mapNotNullTo(this) { block -> block.path }
            diary.entries.forEach { entry -> addAll(entry.images) }
        }
        flashNotes.forEach { note -> addAll(note.images) }
    }

    fun remapDiary(diary: DiaryEntity, mapping: Map<String, String>): DiaryEntity {
        fun remap(path: String): String = mapping[path] ?: path

        return diary.copy(
            images = diary.images.map(::remap),
            contentBlocks = diary.contentBlocks.map { block ->
                block.copy(path = block.path?.let(::remap))
            },
            entries = diary.entries.map { entry ->
                entry.copy(images = entry.images.map(::remap))
            }
        )
    }

    fun remapFlashNote(
        flashNote: FlashNoteEntity,
        mapping: Map<String, String>
    ): FlashNoteEntity = flashNote.copy(
        images = flashNote.images.map { path -> mapping[path] ?: path }
    )
}
