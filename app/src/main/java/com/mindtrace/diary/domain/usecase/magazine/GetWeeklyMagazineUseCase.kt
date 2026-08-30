package com.mindtrace.diary.domain.usecase.magazine

import com.mindtrace.diary.domain.model.WeeklyMagazine
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import com.mindtrace.diary.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklyMagazineUseCase @Inject constructor(
    private val diaries: DiaryRepository,
    private val flashes: FlashNoteRepository,
    private val todos: TodoRepository
) {
    operator fun invoke(anchor: LocalDate): Flow<WeeklyMagazine> = combine(
        diaries.getAllDiaries(), flashes.getAllFlashNotes(), todos.getAllTodos()
    ) { diaryValues, flashValues, todoValues ->
        WeeklyMagazineBuilder.build(anchor, diaryValues, flashValues, todoValues)
    }
}
