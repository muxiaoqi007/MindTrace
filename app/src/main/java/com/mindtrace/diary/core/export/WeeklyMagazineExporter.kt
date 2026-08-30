package com.mindtrace.diary.core.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.mindtrace.diary.domain.model.WeeklyMagazine
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

object WeeklyMagazineExporter {
    private const val WIDTH = 1080
    private const val HEIGHT = 1920
    private const val PAD = 80f

    fun render(value: WeeklyMagazine): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFFF8F3EA.toInt())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF25211D.toInt() }
        var y = 120f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 64f
        canvas.drawText("MINDTRACE WEEKLY", PAD, y, paint)
        y += 58f
        paint.textSize = 30f
        canvas.drawText("${value.weekStart}  —  ${value.weekEnd}", PAD, y, paint)
        y += 70f
        paint.strokeWidth = 3f
        canvas.drawLine(PAD, y, WIDTH - PAD, y, paint)
        y += 62f

        y = section(canvas, paint, y, "这周的温度", value.moodStrip.joinToString("  ") {
            "${it.date.format(DateTimeFormatter.ofPattern("E"))}:${it.averageScore?.let { score -> "%.1f".format(score) } ?: "-"}"
        })
        y = section(canvas, paint, y, "本周片段", value.topMoments.ifEmpty { listOf("这周还没有日记片段") }.joinToString("\n· ", prefix = "· "))
        y = section(canvas, paint, y, "闪念剪辑", value.flashExcerpts.ifEmpty { listOf("本周暂无闪念") }.joinToString("\n· ", prefix = "· "))
        y = section(canvas, paint, y, "已完成", value.completedGoals.ifEmpty { listOf("本周暂无完成项") }.joinToString("\n· ", prefix = "· "))
        section(canvas, paint, y, "带去下周", value.unresolvedThreads.ifEmpty { listOf("没有待续事项") }.joinToString("\n· ", prefix = "· "))

        paint.textSize = 24f
        paint.typeface = Typeface.DEFAULT
        paint.color = 0xFF716B61.toInt()
        canvas.drawText("由本机记录生成 · ${value.diaryCount} 篇日记 · ${value.flashCount} 条闪念", PAD, HEIGHT - 70f, paint)
        return bitmap
    }

    fun createShareIntent(context: Context, value: WeeklyMagazine, pdf: Boolean): Intent {
        val dir = File(context.cacheDir, "weekly_magazines").apply { check(exists() || mkdirs()) }
        val bitmap = render(value)
        val file = File(dir, "MindTrace-weekly-${value.weekStart}.${if (pdf) "pdf" else "png"}")
        try {
            if (pdf) {
                val document = PdfDocument()
                val page = document.startPage(PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, 1).create())
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)
                FileOutputStream(file).use(document::writeTo)
                document.close()
            } else {
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
        } finally { bitmap.recycle() }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = if (pdf) "application/pdf" else "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun section(canvas: Canvas, paint: Paint, startY: Float, title: String, body: String): Float {
        var y = startY
        paint.color = 0xFF25211D.toInt()
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 35f
        canvas.drawText(title, PAD, y, paint)
        y += 46f
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 28f
        body.lines().take(6).forEach { line ->
            var current = ""
            line.forEach { c ->
                if (paint.measureText(current + c) > WIDTH - PAD * 2) {
                    canvas.drawText(current, PAD, y, paint); y += 39f; current = c.toString()
                } else current += c
            }
            if (current.isNotEmpty()) { canvas.drawText(current, PAD, y, paint); y += 39f }
        }
        return y + 32f
    }
}
