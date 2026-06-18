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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * 导出数据用例（包含图片）
 * 将所有数据导出为 ZIP 文件，包含 data.json 和 images/ 文件夹
 */
class ExportDataWithImagesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val diaryDao: DiaryDao,
    private val flashNoteDao: FlashNoteDao,
    private val todoDao: TodoDao
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * 导出数据到指定 URI（ZIP 格式，包含图片）
     * @param uri 目标文件 URI
     * @return 导出结果
     */
    suspend operator fun invoke(uri: Uri): Result<ExportWithImagesResult> = withContext(Dispatchers.IO) {
        try {
            // 收集所有数据
            val diaries = diaryDao.getAllDiariesOnce()
            val flashNotes = flashNoteDao.getAllFlashNotesOnce()
            val todos = todoDao.getAllTodosOnce()

            // 收集所有图片路径
            val allImagePaths = mutableSetOf<String>()
            diaries.forEach { allImagePaths.addAll(it.images) }
            flashNotes.forEach { allImagePaths.addAll(it.images) }

            // 创建路径映射：原始路径 -> 归档路径
            val imageMapping = mutableMapOf<String, String>()
            var imageIndex = 0
            allImagePaths.forEach { path ->
                val file = File(path)
                if (file.exists()) {
                    val ext = file.extension.ifEmpty { "jpg" }
                    imageMapping[path] = "images/image_${imageIndex}.$ext"
                    imageIndex++
                }
            }

            // 更新实体中的路径为归档路径
            val exportDiaries = diaries.map { diary ->
                diary.copy(images = diary.images.map { p -> imageMapping[p] ?: p })
            }
            val exportFlashNotes = flashNotes.map { note ->
                note.copy(images = note.images.map { p -> imageMapping[p] ?: p })
            }

            val exportData = ExportData(
                version = 2,
                exportTime = System.currentTimeMillis(),
                diaries = exportDiaries,
                flashNotes = exportFlashNotes,
                todos = todos
            )

            // 写入 ZIP 文件
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zip ->
                    // 写入 data.json
                    zip.putNextEntry(ZipEntry("data.json"))
                    zip.write(gson.toJson(exportData).toByteArray())
                    zip.closeEntry()

                    // 写入图片
                    imageMapping.forEach { (originalPath, archivePath) ->
                        val file = File(originalPath)
                        if (file.exists()) {
                            zip.putNextEntry(ZipEntry(archivePath))
                            file.inputStream().use { input ->
                                input.copyTo(zip)
                            }
                            zip.closeEntry()
                        }
                    }
                }
            } ?: return@withContext Result.failure(Exception("无法打开文件"))

            Result.success(
                ExportWithImagesResult(
                    diaryCount = diaries.size,
                    flashNoteCount = flashNotes.size,
                    todoCount = todos.size,
                    imageCount = imageMapping.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ExportWithImagesResult(
    val diaryCount: Int,
    val flashNoteCount: Int,
    val todoCount: Int,
    val imageCount: Int
) {
    val totalCount: Int get() = diaryCount + flashNoteCount + todoCount
}
