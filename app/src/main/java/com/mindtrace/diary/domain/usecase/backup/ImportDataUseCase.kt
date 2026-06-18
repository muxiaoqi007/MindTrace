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
 * 导入数据用例
 * 从 JSON 文件恢复数据
 */
class ImportDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val diaryDao: DiaryDao,
    private val flashNoteDao: FlashNoteDao,
    private val todoDao: TodoDao
) {
    private val gson: Gson = GsonBuilder().create()

    /**
     * 从指定 URI 导入数据
     * @param uri 源文件 URI
     * @param mergeMode 是否合并模式（true: 合并，false: 覆盖）
     * @return 导入结果
     */
    suspend operator fun invoke(uri: Uri, mergeMode: Boolean = true): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            // 读取文件
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("无法打开文件"))

            // 解析 JSON
            val exportData = gson.fromJson(jsonString, ExportData::class.java)
                ?: return@withContext Result.failure(Exception("文件格式错误"))

            // 导入数据
            var diaryCount = 0
            var flashNoteCount = 0
            var todoCount = 0

            // 导入日记
            if (exportData.diaries.isNotEmpty()) {
                diaryDao.insertDiaries(exportData.diaries)
                diaryCount = exportData.diaries.size
            }

            // 导入闪念
            if (exportData.flashNotes.isNotEmpty()) {
                flashNoteDao.insertFlashNotes(exportData.flashNotes)
                flashNoteCount = exportData.flashNotes.size
            }

            // 导入待办
            if (exportData.todos.isNotEmpty()) {
                todoDao.insertTodos(exportData.todos)
                todoCount = exportData.todos.size
            }

            Result.success(
                ImportResult(
                    diaryCount = diaryCount,
                    flashNoteCount = flashNoteCount,
                    todoCount = todoCount,
                    version = exportData.version
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ImportResult(
    val diaryCount: Int,
    val flashNoteCount: Int,
    val todoCount: Int,
    val version: Int
) {
    val totalCount: Int get() = diaryCount + flashNoteCount + todoCount
}
