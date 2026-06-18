package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.Todo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TodoRepository {
    fun getAllTodos(): Flow<List<Todo>>
    fun getPendingTodos(): Flow<List<Todo>>
    fun getCompletedTodos(): Flow<List<Todo>>
    suspend fun getTodoById(id: String): Todo?
    fun getTodoByIdFlow(id: String): Flow<Todo?>
    fun getTodosByDueDate(date: LocalDate): Flow<List<Todo>>
    fun searchTodos(query: String): Flow<List<Todo>>
    fun getPendingTodoCount(): Flow<Int>
    fun getCompletedTodoCount(): Flow<Int>
    suspend fun insertTodo(todo: Todo)
    suspend fun updateTodo(todo: Todo)
    suspend fun toggleTodoCompletion(id: String)
    suspend fun deleteTodo(id: String)
    suspend fun getUnsyncedTodos(): List<Todo>
    suspend fun updateSyncTime(id: String, syncedAt: Long)
}
