package com.mindtrace.diary.core.sync

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mindtrace.diary.core.database.dao.DiaryDao
import com.mindtrace.diary.core.database.dao.FlashNoteDao
import com.mindtrace.diary.core.database.dao.TodoDao
import com.mindtrace.diary.core.database.entity.DiaryEntity
import com.mindtrace.diary.core.database.entity.FlashNoteEntity
import com.mindtrace.diary.core.database.entity.TodoEntity
import com.mindtrace.diary.core.datastore.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val webDavClient: WebDavClient,
    private val diaryDao: DiaryDao,
    private val flashNoteDao: FlashNoteDao,
    private val todoDao: TodoDao,
    private val settingsDataStore: SettingsDataStore
) {
    private val gson = Gson()

    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            if (!config.isConfigured) {
                return@withContext SyncResult.NotConfigured
            }

            // Ensure sync directory exists
            if (!webDavClient.ensureDirectory(config.path)) {
                return@withContext SyncResult.Error("Failed to create sync directory")
            }

            val lastSyncTime = settingsDataStore.lastSyncTime.first() ?: 0L
            val currentTime = System.currentTimeMillis()

            // Upload local changes
            val uploadedCount = uploadLocalChanges(lastSyncTime)

            // Download remote changes
            val downloadedCount = downloadRemoteChanges(lastSyncTime)

            // Update last sync time
            settingsDataStore.setLastSyncTime(currentTime)

            // Update sync timestamps for all items
            updateSyncTimestamps(currentTime)

            SyncResult.Success(
                uploadedCount = uploadedCount,
                downloadedCount = downloadedCount,
                syncTime = currentTime
            )
        } catch (e: Exception) {
            e.printStackTrace()
            SyncResult.Error(e.message ?: "Unknown error", e)
        }
    }

    private suspend fun uploadLocalChanges(lastSyncTime: Long): Int {
        var count = 0

        // Upload diaries
        val unsyncedDiaries = diaryDao.getUnsyncedDiaries()
        if (unsyncedDiaries.isNotEmpty()) {
            val json = gson.toJson(unsyncedDiaries)
            if (webDavClient.uploadFile("/diaries.json", json.toByteArray())) {
                count += unsyncedDiaries.size
            }
        }

        // Upload flash notes
        val unsyncedFlashNotes = flashNoteDao.getUnsyncedFlashNotes()
        if (unsyncedFlashNotes.isNotEmpty()) {
            val json = gson.toJson(unsyncedFlashNotes)
            if (webDavClient.uploadFile("/flash_notes.json", json.toByteArray())) {
                count += unsyncedFlashNotes.size
            }
        }

        // Upload todos
        val unsyncedTodos = todoDao.getUnsyncedTodos()
        if (unsyncedTodos.isNotEmpty()) {
            val json = gson.toJson(unsyncedTodos)
            if (webDavClient.uploadFile("/todos.json", json.toByteArray())) {
                count += unsyncedTodos.size
            }
        }

        return count
    }

    private suspend fun downloadRemoteChanges(lastSyncTime: Long): Int {
        var count = 0

        // Download diaries
        webDavClient.downloadFile("/diaries.json")?.let { data ->
            val json = String(data)
            val type = object : TypeToken<List<DiaryEntity>>() {}.type
            val remoteDiaries: List<DiaryEntity> = gson.fromJson(json, type) ?: emptyList()

            remoteDiaries.forEach { remoteDiary ->
                val localDiary = diaryDao.getDiaryById(remoteDiary.id)
                if (localDiary == null || remoteDiary.updatedAt > localDiary.updatedAt) {
                    diaryDao.insertDiary(remoteDiary)
                    count++
                }
            }
        }

        // Download flash notes
        webDavClient.downloadFile("/flash_notes.json")?.let { data ->
            val json = String(data)
            val type = object : TypeToken<List<FlashNoteEntity>>() {}.type
            val remoteFlashNotes: List<FlashNoteEntity> = gson.fromJson(json, type) ?: emptyList()

            remoteFlashNotes.forEach { remoteNote ->
                val localNote = flashNoteDao.getFlashNoteById(remoteNote.id)
                if (localNote == null || remoteNote.updatedAt > localNote.updatedAt) {
                    flashNoteDao.insertFlashNote(remoteNote)
                    count++
                }
            }
        }

        // Download todos
        webDavClient.downloadFile("/todos.json")?.let { data ->
            val json = String(data)
            val type = object : TypeToken<List<TodoEntity>>() {}.type
            val remoteTodos: List<TodoEntity> = gson.fromJson(json, type) ?: emptyList()

            remoteTodos.forEach { remoteTodo ->
                val localTodo = todoDao.getTodoById(remoteTodo.id)
                if (localTodo == null || remoteTodo.updatedAt > localTodo.updatedAt) {
                    todoDao.insertTodo(remoteTodo)
                    count++
                }
            }
        }

        return count
    }

    private suspend fun updateSyncTimestamps(syncTime: Long) {
        diaryDao.getUnsyncedDiaries().forEach { diary ->
            diaryDao.updateSyncTime(diary.id, syncTime)
        }
        flashNoteDao.getUnsyncedFlashNotes().forEach { note ->
            flashNoteDao.updateSyncTime(note.id, syncTime)
        }
        todoDao.getUnsyncedTodos().forEach { todo ->
            todoDao.updateSyncTime(todo.id, syncTime)
        }
    }

    suspend fun testConnection(): Boolean {
        return webDavClient.testConnection()
    }

    /**
     * 从云端恢复数据（覆盖本地）
     */
    suspend fun restoreFromCloud(): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            if (!config.isConfigured) {
                return@withContext RestoreResult.NotConfigured
            }

            var totalCount = 0

            // 清空本地数据
            diaryDao.deleteAllDiaries()
            flashNoteDao.deleteAllFlashNotes()
            todoDao.deleteAllTodos()

            // 下载并恢复日记
            webDavClient.downloadFile("/diaries.json")?.let { data ->
                val json = String(data)
                val type = object : TypeToken<List<DiaryEntity>>() {}.type
                val remoteDiaries: List<DiaryEntity> = gson.fromJson(json, type) ?: emptyList()
                if (remoteDiaries.isNotEmpty()) {
                    diaryDao.insertDiaries(remoteDiaries)
                    totalCount += remoteDiaries.size
                }
            }

            // 下载并恢复闪念
            webDavClient.downloadFile("/flash_notes.json")?.let { data ->
                val json = String(data)
                val type = object : TypeToken<List<FlashNoteEntity>>() {}.type
                val remoteFlashNotes: List<FlashNoteEntity> = gson.fromJson(json, type) ?: emptyList()
                if (remoteFlashNotes.isNotEmpty()) {
                    flashNoteDao.insertFlashNotes(remoteFlashNotes)
                    totalCount += remoteFlashNotes.size
                }
            }

            // 下载并恢复待办
            webDavClient.downloadFile("/todos.json")?.let { data ->
                val json = String(data)
                val type = object : TypeToken<List<TodoEntity>>() {}.type
                val remoteTodos: List<TodoEntity> = gson.fromJson(json, type) ?: emptyList()
                if (remoteTodos.isNotEmpty()) {
                    todoDao.insertTodos(remoteTodos)
                    totalCount += remoteTodos.size
                }
            }

            settingsDataStore.setLastSyncTime(System.currentTimeMillis())
            RestoreResult.Success(totalCount)
        } catch (e: Exception) {
            e.printStackTrace()
            RestoreResult.Error(e.message ?: "Unknown error")
        }
    }
}
