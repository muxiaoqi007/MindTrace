package com.mindtrace.diary.core.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.mindtrace.diary.domain.model.DailyReceipt
import java.io.File
import java.io.FileOutputStream

object DailyReceiptShareManager {
    fun createShareIntent(context: Context, receipt: DailyReceipt): Intent {
        val directory = File(context.cacheDir, "shared_receipts").apply {
            check(exists() || mkdirs()) { "无法创建小票缓存目录" }
        }
        val output = File(directory, "MindTrace-receipt-${receipt.date}.png")
        val bitmap = DailyReceiptImageRenderer.render(receipt)
        try {
            FileOutputStream(output).use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    "无法生成小票图片"
                }
            }
        } finally {
            bitmap.recycle()
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            output
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "MindTrace 每日小票 ${receipt.date}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
