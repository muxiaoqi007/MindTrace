package com.mindtrace.diary.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageUtils {
    private const val IMAGE_DIR = "images"
    private const val MAX_IMAGE_SIZE = 1920
    private const val COMPRESSION_QUALITY = 85

    suspend fun saveImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val scaledBitmap = scaleBitmap(originalBitmap)

            val imageDir = File(context.filesDir, IMAGE_DIR)
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }

            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(imageDir, fileName)

            FileOutputStream(file).use { outputStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
            }

            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return bitmap
        }

        val ratio = minOf(
            MAX_IMAGE_SIZE.toFloat() / width,
            MAX_IMAGE_SIZE.toFloat() / height
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun deleteImage(imagePath: String): Boolean {
        return try {
            File(imagePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getImageFile(context: Context, fileName: String): File {
        val imageDir = File(context.filesDir, IMAGE_DIR)
        return File(imageDir, fileName)
    }

    fun createTempImageFile(context: Context): File {
        val imageDir = File(context.cacheDir, IMAGE_DIR)
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        return File.createTempFile("temp_image_", ".jpg", imageDir)
    }
}
