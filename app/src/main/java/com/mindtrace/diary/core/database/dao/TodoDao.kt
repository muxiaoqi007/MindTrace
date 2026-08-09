package com.mindtrace.diary.core.database.dao

import androidx.room.*
import com.mindtrace.diary.core.database.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos WHERE isDeleted = 0 ORDER BY isCompleted ASC, priority DESC, dueDate ASC, createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllTodosOnce(): List<TodoEntity>

    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    suspend fun getAllTodosForSync(): List<TodoEntity>

    @Query("SELECT * FROM todos WHERE isDeleted = 0 AND isCompleted = 0 ORDER BY priority DESC, dueDate ASC, createdAt DESC")
    fun getPendingTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE isDeleted = 0 AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id AND isDeleted = 0")
    suspend fun getTodoById(id: String): TodoEntity?

    @Query("SELECT * FROM todos WHERE id = :id AND isDeleted = 0")
    fun getTodoByIdFlow(id: String): Flow<TodoEntity?>

    @Query("""
        SELECT * FROM todos
        WHERE isDeleted = 0
        AND dueDate >= :startTime
        AND dueDate < :endTime
        ORDER BY dueDate ASC
    """)
    fun getTodosByDueDate(startTime: Long, endTime: Long): Flow<List<TodoEntity>>

    @Query("""
        SELECT * FROM todos
        WHERE isDeleted = 0
        AND content LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchTodos(query: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE syncedAt IS NULL OR updatedAt > syncedAt")
    suspend fun getUnsyncedTodos(): List<TodoEntity>

    @Query("SELECT COUNT(*) FROM todos WHERE isDeleted = 0 AND isCompleted = 0")
    fun getPendingTodoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM todos WHERE isDeleted = 0 AND isCompleted = 1")
    fun getCompletedTodoCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<TodoEntity>)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Query("""
        UPDATE todos
        SET isCompleted = :isCompleted,
            completedAt = CASE WHEN :isCompleted = 1 THEN :timestamp ELSE NULL END,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun updateTodoCompletion(id: String, isCompleted: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE todos SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteTodo(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodo(id: String)

    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()

    @Query("UPDATE todos SET syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncTime(id: String, syncedAt: Long)
}
