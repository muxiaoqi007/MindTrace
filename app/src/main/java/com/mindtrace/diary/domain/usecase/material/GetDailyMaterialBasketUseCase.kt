package com.mindtrace.diary.domain.usecase.material

import com.mindtrace.diary.domain.model.DailyMaterialBasket
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import com.mindtrace.diary.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetDailyMaterialBasketUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val flashNoteRepository: FlashNoteRepository,
    private val todoRepository: TodoRepository
) {
    operator fun invoke(date: LocalDate): Flow<DailyMaterialBasket> = combine(
        diaryRepository.getDiariesByDate(date),
        flashNoteRepository.getFlashNotesByDate(date),
        todoRepository.getAllTodos()
    ) { diaries, flashNotes, todos ->
        DailyMaterialBasketBuilder.build(date, diaries, flashNotes, todos)
    }
}
