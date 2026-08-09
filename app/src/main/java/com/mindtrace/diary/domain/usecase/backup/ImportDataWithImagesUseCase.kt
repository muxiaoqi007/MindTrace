package com.mindtrace.diary.domain.usecase.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mindtrace.diary.core.database.AppDatabase
import com.mindtrace.diary.core.database.dao.DiaryDao
import com.mindtrace.diary.core.database.dao.FlashNoteDao
import com.mindtrace.diary.core.database.dao.TodoDao
import com.mindtrace.diary.domain.model.ExportData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * 导入数据用例（包含图片）
 * 从 ZIP 文件恢复数据，包含 data.json 和 images/ 文件夹
 */
class ImportDataWithImagesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
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
            val extractedFiles = mutableListOf<File>()

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zip ->
                        var entry = zip.nextEntry
                        var totalExtractedBytes = 0L
                        while (entry != null) {
                            val entryName = entry.name
                            when {
                                entryName == DATA_ENTRY_NAME -> {
                                    check(exportData == null) { "备份包含重复的 data.json" }
                                    val content = zip.readEntryBytes(MAX_DATA_BYTES).toString(Charsets.UTF_8)
                                    exportData = gson.fromJson(content, ExportData::class.java)
                                }
                                entryName.startsWith(IMAGE_ENTRY_PREFIX) && !entry.isDirectory -> {
                                    require(isSafeImageEntry(entryName)) { "备份包含非法图片路径" }
                                    val extension = entryName.substringAfterLast('.', "jpg")
                                        .takeIf { it.matches(Regex("[A-Za-z0-9]{1,8}")) }
                                        ?: "jpg"
                                    val localFile = File(imagesDir, "imported_${UUID.randomUUID()}.$extension")
                                    extractedFiles += localFile
                                    val written = localFile.outputStream().use { output ->
                                        zip.copyEntryTo(output, MAX_IMAGE_BYTES)
                                    }
                                    totalExtractedBytes += written
                                    require(totalExtractedBytes <= MAX_TOTAL_IMAGE_BYTES) {
                                        "备份图片总大小超过限制"
                                    }
                                    extractedImages[entryName] = localFile.absolutePath
                                }
                            }
                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                    }
                } ?: return@withContext Result.failure(Exception("无法打开文件"))

                val data = exportData ?: error("文件格式错误：缺少 data.json")
                require(data.version in SUPPORTED_VERSIONS) { "不支持的备份版本：${data.version}" }
                val archivedReferences = BackupImagePathMapper
                    .collectImagePaths(data.diaries, data.flashNotes)
                    .filter { path -> path.startsWith(IMAGE_ENTRY_PREFIX) }
                require(archivedReferences.all(extractedImages::containsKey)) {
                    "备份缺少被引用的图片文件"
                }

                // 重映射所有兼容表示中的图片路径：归档路径 -> 本地路径
                val diaries = data.diaries.map { diary ->
                    BackupImagePathMapper.remapDiary(diary, extractedImages)
                }
                val flashNotes = data.flashNotes.map { note ->
                    BackupImagePathMapper.remapFlashNote(note, extractedImages)
                }

                // 导入数据
                var diaryCount = 0
                var flashNoteCount = 0
                var todoCount = 0

                database.withTransaction {
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
                extractedFiles.forEach { file -> file.delete() }
                throw e
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isSafeImageEntry(name: String): Boolean {
        if (!name.startsWith(IMAGE_ENTRY_PREFIX)) return false
        val relativeName = name.removePrefix(IMAGE_ENTRY_PREFIX)
        return relativeName.isNotBlank() &&
            '/' !in relativeName &&
            '\\' !in relativeName &&
            relativeName != "." &&
            relativeName != ".."
    }

    private fun ZipInputStream.readEntryBytes(maxBytes: Long): ByteArray {
        val output = ByteArrayOutputStream()
        copyEntryTo(output, maxBytes)
        return output.toByteArray()
    }

    private fun ZipInputStream.copyEntryTo(output: java.io.OutputStream, maxBytes: Long): Long {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = read(buffer)
            if (read < 0) break
            total += read
            require(total <= maxBytes) { "备份条目大小超过限制" }
            output.write(buffer, 0, read)
        }
        return total
    }

    private companion object {
        const val DATA_ENTRY_NAME = "data.json"
        const val IMAGE_ENTRY_PREFIX = "images/"
        const val MAX_DATA_BYTES = 25L * 1024 * 1024
        const val MAX_IMAGE_BYTES = 50L * 1024 * 1024
        const val MAX_TOTAL_IMAGE_BYTES = 500L * 1024 * 1024
        val SUPPORTED_VERSIONS = 1..2
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
