package com.mindtrace.diary.domain.usecase.todo

import com.mindtrace.diary.domain.model.Priority
import com.mindtrace.diary.domain.model.Todo
import com.mindtrace.diary.domain.repository.TodoRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class SaveTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(
        id: String? = null,
        content: String,
        dueDate: LocalDateTime? = null,
        priority: Priority = Priority.MEDIUM
    ) {
        val now = LocalDateTime.now()
        val existingTodo = id?.let { todoRepository.getTodoById(it) }

        val todo = Todo(
            id = id ?: UUID.randomUUID().toString(),
            content = content,
            isCompleted = existingTodo?.isCompleted ?: false,
            dueDate = dueDate,
            priority = priority,
            createdAt = existingTodo?.createdAt ?: now,
            completedAt = existingTodo?.completedAt
        )

        if (id == null) {
            todoRepository.insertTodo(todo)
        } else {
            todoRepository.updateTodo(todo)
        }
    }
}
