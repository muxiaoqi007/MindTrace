package com.mindtrace.diary.domain.usecase.todo

import com.mindtrace.diary.domain.repository.TodoRepository
import javax.inject.Inject

class DeleteTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(id: String) {
        todoRepository.deleteTodo(id)
    }
}
