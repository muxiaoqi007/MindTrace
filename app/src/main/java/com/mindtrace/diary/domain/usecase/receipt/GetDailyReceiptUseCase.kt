package com.mindtrace.diary.domain.usecase.receipt

import com.mindtrace.diary.domain.model.DailyReceipt
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import com.mindtrace.diary.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetDailyReceiptUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val flashNoteRepository: FlashNoteRepository,
    private val todoRepository: TodoRepository
) {
    operator fun invoke(date: LocalDate): Flow<DailyReceipt> = combine(
        diaryRepository.getDiariesByDate(date),
        flashNoteRepository.getFlashNotesByDate(date),
        todoRepository.getAllTodos()
    ) { diaries, flashNotes, todos ->
        DailyReceiptBuilder.build(
            date = date,
            diaries = diaries,
            flashNotes = flashNotes,
            todos = todos
        )
    }
}
