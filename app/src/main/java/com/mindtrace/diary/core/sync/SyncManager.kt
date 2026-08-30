package com.mindtrace.diary.core.sync

import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mindtrace.diary.core.database.AppDatabase
import com.mindtrace.diary.core.database.dao.DiaryDao
import com.mindtrace.diary.core.database.dao.FlashNoteDao
import com.mindtrace.diary.core.database.dao.TodoDao
import com.mindtrace.diary.core.database.entity.DiaryEntity
import com.mindtrace.diary.core.database.entity.FlashNoteEntity
import com.mindtrace.diary.core.database.entity.TodoEntity
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.domain.model.ExportData
import com.mindtrace.diary.domain.usecase.backup.PersonalizationBackupDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val webDavClient: WebDavClient,
    private val database: AppDatabase,
    private val diaryDao: DiaryDao,
    private val flashNoteDao: FlashNoteDao,
    private val todoDao: TodoDao,
    private val personalization: PersonalizationBackupDataSource,
    private val settingsDataStore: SettingsDataStore
) {
    private val gson = Gson()

    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            if (!config.isConfigured) return@withContext SyncResult.NotConfigured

            webDavClient.ensureDirectory(config.path).getOrThrow()

            val localDiaries = diaryDao.getAllDiariesForSync()
            val localFlashNotes = flashNoteDao.getAllFlashNotesForSync()
            val localTodos = todoDao.getAllTodosForSync()
            val localPersonalization = personalization.appendTo(ExportData(version = 3))

            val remoteDiaries = downloadSnapshot<DiaryEntity>(DIARIES_PATH, DIARY_LIST_TYPE).records
            val remoteFlashNotes = downloadSnapshot<FlashNoteEntity>(FLASH_NOTES_PATH, FLASH_NOTE_LIST_TYPE).records
            val remoteTodos = downloadSnapshot<TodoEntity>(TODOS_PATH, TODO_LIST_TYPE).records
            val remotePersonalization = downloadPersonalization().records

            val mergedDiaries = SyncMergePolicy.merge(
                localDiaries,
                remoteDiaries,
                DiaryEntity::id,
                DiaryEntity::updatedAt,
                DiaryEntity::isDeleted
            )
            val mergedFlashNotes = SyncMergePolicy.merge(
                localFlashNotes,
                remoteFlashNotes,
                FlashNoteEntity::id,
                FlashNoteEntity::updatedAt,
                FlashNoteEntity::isDeleted
            )
            val mergedTodos = SyncMergePolicy.merge(
                localTodos,
                remoteTodos,
                TodoEntity::id,
                TodoEntity::updatedAt,
                TodoEntity::isDeleted
            )
            val mergedPersonalization = PersonalizationSyncPolicy.merge(localPersonalization, remotePersonalization)

            val uploadedCount = countUnsynced(localDiaries) +
                countUnsynced(localFlashNotes) +
                countUnsynced(localTodos)
            val downloadedCount = countChanged(localDiaries, mergedDiaries, DiaryEntity::id) +
                countChanged(localFlashNotes, mergedFlashNotes, FlashNoteEntity::id) +
                countChanged(localTodos, mergedTodos, TodoEntity::id)

            // Always upload complete snapshots. Uploading only the local delta would
            // replace the remote file and silently discard all other records.
            webDavClient.uploadFile(DIARIES_PATH, gson.toJson(mergedDiaries).toByteArray()).getOrThrow()
            webDavClient.uploadFile(FLASH_NOTES_PATH, gson.toJson(mergedFlashNotes).toByteArray()).getOrThrow()
            webDavClient.uploadFile(TODOS_PATH, gson.toJson(mergedTodos).toByteArray()).getOrThrow()
            webDavClient.uploadFile(PERSONALIZATION_PATH, gson.toJson(mergedPersonalization).toByteArray()).getOrThrow()

            val syncTime = System.currentTimeMillis()
            database.withTransaction {
                if (mergedDiaries.isNotEmpty()) diaryDao.insertDiaries(mergedDiaries)
                if (mergedFlashNotes.isNotEmpty()) flashNoteDao.insertFlashNotes(mergedFlashNotes)
                if (mergedTodos.isNotEmpty()) todoDao.insertTodos(mergedTodos)
                personalization.restore(mergedPersonalization)
                mergedDiaries.forEach { diaryDao.updateSyncTime(it.id, syncTime) }
                mergedFlashNotes.forEach { flashNoteDao.updateSyncTime(it.id, syncTime) }
                mergedTodos.forEach { todoDao.updateSyncTime(it.id, syncTime) }
            }
            settingsDataStore.setLastSyncTime(syncTime)

            SyncResult.Success(uploadedCount, downloadedCount, syncTime)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "同步失败", e)
        }
    }

    suspend fun testConnection(): Boolean = webDavClient.testConnection()

    /** Downloads and validates every snapshot before replacing any local row. */
    suspend fun restoreFromCloud(): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            if (!config.isConfigured) return@withContext RestoreResult.NotConfigured

            val diaries = downloadSnapshot<DiaryEntity>(DIARIES_PATH, DIARY_LIST_TYPE)
            val flashNotes = downloadSnapshot<FlashNoteEntity>(FLASH_NOTES_PATH, FLASH_NOTE_LIST_TYPE)
            val todos = downloadSnapshot<TodoEntity>(TODOS_PATH, TODO_LIST_TYPE)
            val personalizationSnapshot = downloadPersonalization()
            if (!diaries.found && !flashNotes.found && !todos.found && !personalizationSnapshot.found) {
                return@withContext RestoreResult.Error("云端没有可恢复的 MindTrace 数据")
            }

            validateUniqueIds(diaries.records, DiaryEntity::id, "日记")
            validateUniqueIds(flashNotes.records, FlashNoteEntity::id, "闪念")
            validateUniqueIds(todos.records, TodoEntity::id, "待办")

            val restoreTime = System.currentTimeMillis()
            database.withTransaction {
                diaryDao.deleteAllDiaries()
                flashNoteDao.deleteAllFlashNotes()
                todoDao.deleteAllTodos()
                if (diaries.records.isNotEmpty()) diaryDao.insertDiaries(diaries.records)
                if (flashNotes.records.isNotEmpty()) flashNoteDao.insertFlashNotes(flashNotes.records)
                if (todos.records.isNotEmpty()) todoDao.insertTodos(todos.records)
                if (personalizationSnapshot.found) personalization.restore(personalizationSnapshot.records)
                diaries.records.forEach { diaryDao.updateSyncTime(it.id, restoreTime) }
                flashNotes.records.forEach { flashNoteDao.updateSyncTime(it.id, restoreTime) }
                todos.records.forEach { todoDao.updateSyncTime(it.id, restoreTime) }
            }
            settingsDataStore.setLastSyncTime(restoreTime)
            RestoreResult.Success(
                diaries.records.size + flashNotes.records.size + todos.records.size +
                    personalizationRecordCount(personalizationSnapshot.records)
            )
        } catch (e: Exception) {
            RestoreResult.Error(e.message ?: "恢复失败")
        }
    }

    private suspend fun <T> downloadSnapshot(path: String, type: Type): RemoteSnapshot<T> {
        return when (val result = webDavClient.downloadFile(path)) {
            is RemoteFileResult.Found -> {
                val records = try {
                    gson.fromJson<List<T>>(String(result.data, Charsets.UTF_8), type) ?: emptyList()
                } catch (e: Exception) {
                    throw IOException("云端文件 $path 格式错误", e)
                }
                RemoteSnapshot(found = true, records = records)
            }
            RemoteFileResult.NotFound -> RemoteSnapshot(found = false, records = emptyList())
            is RemoteFileResult.Failure -> throw IOException(
                "下载云端文件 $path 失败：${result.message}",
                result.cause
            )
        }
    }

    private suspend fun downloadPersonalization(): RemotePersonalization =
        when (val result = webDavClient.downloadFile(PERSONALIZATION_PATH)) {
            is RemoteFileResult.Found -> {
                val value = try {
                    gson.fromJson(String(result.data, Charsets.UTF_8), ExportData::class.java) ?: ExportData(version = 3)
                } catch (e: Exception) {
                    throw IOException("云端个性化数据格式错误", e)
                }
                RemotePersonalization(found = true, records = value)
            }
            RemoteFileResult.NotFound -> RemotePersonalization(found = false, records = ExportData(version = 3))
            is RemoteFileResult.Failure -> throw IOException("下载云端个性化数据失败：${result.message}", result.cause)
        }

    private fun personalizationRecordCount(data: ExportData): Int =
        data.lifeFacets.orEmpty().size + data.facetCheckIns.orEmpty().size + data.timeCapsules.orEmpty().size +
            data.storylines.orEmpty().size + data.storylineSources.orEmpty().size + data.lexiconEntries.orEmpty().size +
            data.lexiconEvidence.orEmpty().size + data.dailyMediaPicks.orEmpty().size

    private fun <T> countChanged(local: List<T>, merged: List<T>, id: (T) -> String): Int {
        val localById = local.associateBy(id)
        return merged.count { record -> localById[id(record)] != record }
    }

    private fun countUnsynced(records: List<DiaryEntity>): Int =
        records.count { it.syncedAt == null || it.updatedAt > it.syncedAt }

    @JvmName("countUnsyncedFlashNotes")
    private fun countUnsynced(records: List<FlashNoteEntity>): Int =
        records.count { it.syncedAt == null || it.updatedAt > it.syncedAt }

    @JvmName("countUnsyncedTodos")
    private fun countUnsynced(records: List<TodoEntity>): Int =
        records.count { it.syncedAt == null || it.updatedAt > it.syncedAt }

    private fun <T> validateUniqueIds(records: List<T>, id: (T) -> String, label: String) {
        require(records.map(id).distinct().size == records.size) { "云端${label}数据包含重复 ID" }
    }

    private data class RemoteSnapshot<T>(val found: Boolean, val records: List<T>)
    private data class RemotePersonalization(val found: Boolean, val records: ExportData)

    private companion object {
        const val DIARIES_PATH = "/diaries.json"
        const val FLASH_NOTES_PATH = "/flash_notes.json"
        const val TODOS_PATH = "/todos.json"
        const val PERSONALIZATION_PATH = "/personalization.json"
        val DIARY_LIST_TYPE: Type = object : TypeToken<List<DiaryEntity>>() {}.type
        val FLASH_NOTE_LIST_TYPE: Type = object : TypeToken<List<FlashNoteEntity>>() {}.type
        val TODO_LIST_TYPE: Type = object : TypeToken<List<TodoEntity>>() {}.type
    }
}
