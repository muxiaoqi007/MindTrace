package com.mindtrace.diary.ui.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.Priority
import com.mindtrace.diary.domain.repository.TodoRepository
import com.mindtrace.diary.domain.usecase.todo.DeleteTodoUseCase
import com.mindtrace.diary.domain.usecase.todo.SaveTodoUseCase
import com.mindtrace.diary.domain.usecase.todo.ToggleTodoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val saveTodoUseCase: SaveTodoUseCase,
    private val toggleTodoUseCase: ToggleTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        loadTodos()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                todoRepository.getPendingTodos(),
                todoRepository.getCompletedTodos()
            ) { pending, completed ->
                Pair(pending, completed)
            }.collect { (pending, completed) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingTodos = pending,
                        completedTodos = completed
                    )
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                newTodoContent = "",
                newTodoPriority = Priority.MEDIUM,
                newTodoDueDate = null
            )
        }
    }

    fun updateNewTodoContent(content: String) {
        _uiState.update { it.copy(newTodoContent = content) }
    }

    fun updateNewTodoPriority(priority: Priority) {
        _uiState.update { it.copy(newTodoPriority = priority) }
    }

    fun updateNewTodoDueDate(dueDate: LocalDateTime?) {
        _uiState.update { it.copy(newTodoDueDate = dueDate) }
    }

    fun addTodo() {
        viewModelScope.launch {
            try {
                saveTodoUseCase(
                    content = _uiState.value.newTodoContent,
                    priority = _uiState.value.newTodoPriority,
                    dueDate = _uiState.value.newTodoDueDate
                )
                hideAddDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "添加失败") }
            }
        }
    }

    fun toggleTodo(id: String) {
        viewModelScope.launch {
            try {
                toggleTodoUseCase(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "操作失败") }
            }
        }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch {
            try {
                deleteTodoUseCase(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "删除失败") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
