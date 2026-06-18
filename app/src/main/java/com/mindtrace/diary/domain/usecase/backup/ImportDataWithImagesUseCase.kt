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
import java.io.File
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * 导入数据用例（包含图片）
 * 从 ZIP 文件恢复数据，包含 data.json 和 images/ 文件夹
 */
class ImportDataWithImagesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val diaryDao: DiaryDao,
    private val flashNoteDao: FlashNoteDao,
    private val todoDao: TodoDao
) {
    private val gson: Gson = GsonBuilder().create()

    /**
     * 从指定 URI 导入数据（ZIP 格式，包含图片）
     * @param uri 源文件 URI
     * @return 导入结果
     */
    suspend operator fun invoke(uri: Uri): Result<ImportWithImagesResult> = withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
            var exportData: ExportData? = null
            val extractedImages = mutableMapOf<String, String>() // 归档路径 -> 本地路径

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        when {
                            entry.name == "data.json" -> {
                                val content = zip.bufferedReader().readText()
                                exportData = gson.fromJson(content, ExportData::class.java)
                            }
                            entry.name.startsWith("images/") && !entry.isDirectory -> {
                                // 提取图片到本地
                                val fileName = "imported_${System.currentTimeMillis()}_${entry.name.substringAfterLast("/")}"
                                val localFile = File(imagesDir, fileName)
                                localFile.outputStream().use { output ->
                                    zip.copyTo(output)
                                }
                                extractedImages[entry.name] = localFile.absolutePath
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            } ?: return@withContext Result.failure(Exception("无法打开文件"))

            val data = exportData ?: return@withContext Result.failure(Exception("文件格式错误"))

            // 重映射图片路径：归档路径 -> 本地路径
            val diaries = data.diaries.map { diary ->
                diary.copy(images = diary.images.map { p -> extractedImages[p] ?: p })
            }
            val flashNotes = data.flashNotes.map { note ->
                note.copy(images = note.images.map { p -> extractedImages[p] ?: p })
            }

            // 导入数据
            var diaryCount = 0
            var flashNoteCount = 0
            var todoCount = 0

            if (diaries.isNotEmpty()) {
                diaryDao.insertDiaries(diaries)
                diaryCount = diaries.size
            }

            if (flashNotes.isNotEmpty()) {
                flashNoteDao.insertFlashNotes(flashNotes)
                flashNoteCount = flashNotes.size
            }

            if (data.todos.isNotEmpty()) {
                todoDao.insertTodos(data.todos)
                todoCount = data.todos.size
            }

            Result.success(
                ImportWithImagesResult(
                    diaryCount = diaryCount,
                    flashNoteCount = flashNoteCount,
                    todoCount = todoCount,
                    imageCount = extractedImages.size,
                    version = data.version
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ImportWithImagesResult(
    val diaryCount: Int,
    val flashNoteCount: Int,
    val todoCount: Int,
    val imageCount: Int,
    val version: Int
) {
    val totalCount: Int get() = diaryCount + flashNoteCount + todoCount
}
