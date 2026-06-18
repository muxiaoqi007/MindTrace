package com.mindtrace.diary.domain.usecase.todo

import com.mindtrace.diary.domain.model.Todo
import com.mindtrace.diary.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    operator fun invoke(): Flow<List<Todo>> {
        return todoRepository.getAllTodos()
    }
}
