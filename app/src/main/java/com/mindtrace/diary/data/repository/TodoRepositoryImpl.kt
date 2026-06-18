package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.TodoDao
import com.mindtrace.diary.core.database.entity.TodoEntity
import com.mindtrace.diary.core.util.DateUtils
import com.mindtrace.diary.domain.model.Priority
import com.mindtrace.diary.domain.model.Todo
import com.mindtrace.diary.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository {

    override fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPendingTodos(): Flow<List<Todo>> {
        return todoDao.getPendingTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCompletedTodos(): Flow<List<Todo>> {
        return todoDao.getCompletedTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTodoById(id: String): Todo? {
        return todoDao.getTodoById(id)?.toDomain()
    }

    override fun getTodoByIdFlow(id: String): Flow<Todo?> {
        return todoDao.getTodoByIdFlow(id).map { it?.toDomain() }
    }

    override fun getTodosByDueDate(date: LocalDate): Flow<List<Todo>> {
        val startTime = DateUtils.getStartOfDay(date)
        val endTime = DateUtils.getEndOfDay(date)
        return todoDao.getTodosByDueDate(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchTodos(query: String): Flow<List<Todo>> {
        return todoDao.searchTodos(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPendingTodoCount(): Flow<Int> {
        return todoDao.getPendingTodoCount()
    }

    override fun getCompletedTodoCount(): Flow<Int> {
        return todoDao.getCompletedTodoCount()
    }

    override suspend fun insertTodo(todo: Todo) {
        todoDao.insertTodo(todo.toEntity())
    }

    override suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo.toEntity())
    }

    override suspend fun toggleTodoCompletion(id: String) {
        val todo = todoDao.getTodoById(id)
            ?: throw IllegalStateException("Todo with id $id not found")
        todoDao.updateTodoCompletion(id, !todo.isCompleted)
    }

    override suspend fun deleteTodo(id: String) {
        todoDao.softDeleteTodo(id)
    }

    override suspend fun getUnsyncedTodos(): List<Todo> {
        return todoDao.getUnsyncedTodos().map { it.toDomain() }
    }

    override suspend fun updateSyncTime(id: String, syncedAt: Long) {
        todoDao.updateSyncTime(id, syncedAt)
    }

    private fun TodoEntity.toDomain(): Todo {
        return Todo(
            id = id,
            content = content,
            isCompleted = isCompleted,
            dueDate = dueDate?.let { DateUtils.fromEpochMillis(it) },
            priority = Priority.fromString(priority),
            createdAt = DateUtils.fromEpochMillis(createdAt),
            completedAt = completedAt?.let { DateUtils.fromEpochMillis(it) },
            syncedAt = syncedAt?.let { DateUtils.fromEpochMillis(it) },
            isDeleted = isDeleted
        )
    }

    private fun Todo.toEntity(): TodoEntity {
        return TodoEntity(
            id = id,
            content = content,
            isCompleted = isCompleted,
            dueDate = dueDate?.let { DateUtils.toEpochMillis(it) },
            priority = priority.name,
            createdAt = DateUtils.toEpochMillis(createdAt),
            completedAt = completedAt?.let { DateUtils.toEpochMillis(it) },
            syncedAt = syncedAt?.let { DateUtils.toEpochMillis(it) },
            isDeleted = isDeleted
        )
    }
}
