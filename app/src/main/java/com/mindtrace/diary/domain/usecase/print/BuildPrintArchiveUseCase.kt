package com.mindtrace.diary.domain.usecase.print

import com.mindtrace.diary.domain.model.PrintArchiveConfig
import com.mindtrace.diary.domain.model.PrintArchiveContent
import com.mindtrace.diary.domain.model.PrintArchiveSelector
import com.mindtrace.diary.domain.model.PrintArchiveType
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import com.mindtrace.diary.domain.repository.TodoRepository
import com.mindtrace.diary.domain.usecase.magazine.WeeklyMagazineBuilder
import com.mindtrace.diary.domain.usecase.receipt.DailyReceiptBuilder
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BuildPrintArchiveUseCase @Inject constructor(
    private val diaries: DiaryRepository,
    private val flashes: FlashNoteRepository,
    private val todos: TodoRepository
) {
    suspend operator fun invoke(config: PrintArchiveConfig): PrintArchiveContent {
        val diaryValues = diaries.getAllDiaries().first()
        val flashValues = flashes.getAllFlashNotes().first()
        val todoValues = todos.getAllTodos().first()
        val selection = PrintArchiveSelector.select(config, diaryValues)
        return when (config.type) {
            PrintArchiveType.JOURNAL -> PrintArchiveContent(config, diaries = selection.diaries)
            PrintArchiveType.RECEIPTS -> PrintArchiveContent(
                config,
                receipts = selection.dates.map { DailyReceiptBuilder.build(it, diaryValues, flashValues, todoValues) }
            )
            PrintArchiveType.WEEKLY_MAGAZINES -> PrintArchiveContent(
                config,
                magazines = selection.weekAnchors.map { WeeklyMagazineBuilder.build(it, diaryValues, flashValues, todoValues) }
            )
        }
    }
}
