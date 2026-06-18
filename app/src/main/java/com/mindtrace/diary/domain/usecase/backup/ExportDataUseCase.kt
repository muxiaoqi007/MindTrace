package com.mindtrace.diary.domain.usecase.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mindtrace.diary.core.database.dao.DiaryDao
import com.mindtrace.diary.core.database.dao.FlashNoteDao
import com.mindtrace.diary.core.database.dao.TodoDao
import com.mindtrace.diary.domain.model.ExportData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 导出数据用例
 * 将所有数据导出为 JSON 文件
 */
class ExportDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val diaryDao: DiaryDao,
    private val flashNoteDao: FlashNoteDao,
    private val todoDao: TodoDao
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * 导出数据到指定 URI
     * @param uri 目标文件 URI
     * @return 导出结果
     */
    suspend operator fun invoke(uri: Uri): Result<ExportResult> = withContext(Dispatchers.IO) {
        try {
            // 收集所有数据
            val diaries = diaryDao.getAllDiariesOnce()
            val flashNotes = flashNoteDao.getAllFlashNotesOnce()
            val todos = todoDao.getAllTodosOnce()

            val exportData = ExportData(
                version = 1,
                exportTime = System.currentTimeMillis(),
                diaries = diaries,
                flashNotes = flashNotes,
                todos = todos
            )

            // 写入文件
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(gson.toJson(exportData).toByteArray())
            } ?: return@withContext Result.failure(Exception("无法打开文件"))

            Result.success(
                ExportResult(
                    diaryCount = diaries.size,
                    flashNoteCount = flashNotes.size,
                    todoCount = todos.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ExportResult(
    val diaryCount: Int,
    val flashNoteCount: Int,
    val todoCount: Int
) {
    val totalCount: Int get() = diaryCount + flashNoteCount + todoCount
}
