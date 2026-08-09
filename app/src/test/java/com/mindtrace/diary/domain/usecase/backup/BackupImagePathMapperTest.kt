package com.mindtrace.diary.domain.usecase.backup

import com.mindtrace.diary.core.database.entity.ContentBlockData
import com.mindtrace.diary.core.database.entity.DiaryEntity
import com.mindtrace.diary.core.database.entity.DiaryEntryData
import com.mindtrace.diary.core.database.entity.FlashNoteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupImagePathMapperTest {

    @Test
    fun collectPathsIncludesCompatibilityBlocksEntriesAndFlashNotes() {
        val diary = diaryFixture()
        val note = FlashNoteEntity(content = "note", images = listOf("/flash.jpg"))

        val paths = BackupImagePathMapper.collectImagePaths(listOf(diary), listOf(note))

        assertEquals(
            setOf("/legacy.jpg", "/missing.jpg", "/block.jpg", "/entry.jpg", "/flash.jpg"),
            paths
        )
    }

    @Test
    fun remapDiaryUpdatesEveryImageRepresentation() {
        val mapping = mapOf(
            "/legacy.jpg" to "images/legacy.jpg",
            "/block.jpg" to "images/block.jpg",
            "/entry.jpg" to "images/entry.jpg"
        )

        val remapped = BackupImagePathMapper.remapDiary(diaryFixture(), mapping)

        assertEquals(listOf("images/legacy.jpg", "/missing.jpg"), remapped.images)
        assertEquals("images/block.jpg", remapped.contentBlocks[1].path)
        assertEquals(null, remapped.contentBlocks[0].path)
        assertEquals(listOf("images/entry.jpg"), remapped.entries.single().images)
    }

    @Test
    fun remapFlashNotePreservesUnknownPaths() {
        val note = FlashNoteEntity(
            content = "note",
            images = listOf("/flash.jpg", "/unknown.jpg")
        )

        val remapped = BackupImagePathMapper.remapFlashNote(
            note,
            mapOf("/flash.jpg" to "images/flash.jpg")
        )

        assertEquals(listOf("images/flash.jpg", "/unknown.jpg"), remapped.images)
    }

    private fun diaryFixture() = DiaryEntity(
        title = "title",
        content = "content",
        images = listOf("/legacy.jpg", "/missing.jpg"),
        contentBlocks = listOf(
            ContentBlockData(id = "text", type = "TEXT", text = "hello"),
            ContentBlockData(id = "image", type = "IMAGE", path = "/block.jpg")
        ),
        entries = listOf(
            DiaryEntryData(
                id = "entry",
                content = "entry",
                images = listOf("/entry.jpg"),
                timestamp = 1,
                type = "FLASH_NOTE"
            )
        )
    )
}
