package com.mindtrace.diary.ui.screens.todo

import com.mindtrace.diary.domain.model.Priority
import com.mindtrace.diary.domain.model.Todo
import java.time.LocalDateTime

data class TodoUiState(
    val isLoading: Boolean = true,
    val pendingTodos: List<Todo> = emptyList(),
    val completedTodos: List<Todo> = emptyList(),
    val error: String? = null,
    val newTodoContent: String = "",
    val newTodoPriority: Priority = Priority.MEDIUM,
    val newTodoDueDate: LocalDateTime? = null,
    val showAddDialog: Boolean = false
)
