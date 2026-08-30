package com.mindtrace.diary.core.export

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.PrintArchiveContent
import com.mindtrace.diary.domain.model.PrintArchiveType
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

object PrintArchiveRenderer {
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f
    private const val BOTTOM = 52f

    fun render(content: PrintArchiveContent, output: File): Int {
        val document = PdfDocument()
        var pageCount = 0
        try {
            when (content.config.type) {
                PrintArchiveType.JOURNAL -> pageCount = renderJournal(document, content.diaries, content)
                PrintArchiveType.RECEIPTS -> content.receipts.forEach { receipt ->
                    val page = document.startPage(pageInfo(++pageCount))
                    val bitmap = DailyReceiptImageRenderer.render(receipt)
                    page.canvas.drawBitmap(bitmap, null, fitRect(bitmap.width, bitmap.height), Paint(Paint.ANTI_ALIAS_FLAG))
                    bitmap.recycle()
                    drawFooter(page.canvas, pageCount)
                    document.finishPage(page)
                }
                PrintArchiveType.WEEKLY_MAGAZINES -> content.magazines.forEach { magazine ->
                    val page = document.startPage(pageInfo(++pageCount))
                    val bitmap = WeeklyMagazineExporter.render(magazine)
                    page.canvas.drawBitmap(bitmap, null, fitRect(bitmap.width, bitmap.height), Paint(Paint.ANTI_ALIAS_FLAG))
                    bitmap.recycle()
                    drawFooter(page.canvas, pageCount)
                    document.finishPage(page)
                }
            }
            if (pageCount == 0) {
                val page = document.startPage(pageInfo(++pageCount))
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 20f; color = Color.DKGRAY }
                page.canvas.drawText("所选范围内没有可打印的内容", MARGIN, 120f, paint)
                drawFooter(page.canvas, pageCount)
                document.finishPage(page)
            }
            output.parentFile?.let { check(it.exists() || it.mkdirs()) }
            FileOutputStream(output).use(document::writeTo)
        } finally { document.close() }
        return pageCount
    }

    private fun renderJournal(document: PdfDocument, diaries: List<Diary>, content: PrintArchiveContent): Int {
        var pageCount = 0
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = MARGIN
        val scale = content.config.fontScale.coerceIn(.8f, 1.5f)
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF24211E.toInt(); textSize = 13f * scale }
        val title = Paint(body).apply { textSize = 24f * scale; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD) }
        val meta = Paint(body).apply { textSize = 10.5f * scale; color = 0xFF6F6962.toInt() }

        fun finishPage() {
            page?.let { drawFooter(it.canvas, pageCount); document.finishPage(it) }
            page = null; canvas = null
        }
        fun newPage() {
            finishPage()
            page = document.startPage(pageInfo(++pageCount)); canvas = page!!.canvas; y = MARGIN
            val header = Paint(meta).apply { typeface = Typeface.DEFAULT_BOLD }
            canvas!!.drawText("MINDTRACE · 日记归档", MARGIN, y, header)
            y += 30f
        }
        fun ensure(height: Float) { if (page == null || y + height > PAGE_HEIGHT - BOTTOM) newPage() }
        fun textLines(text: String, paint: Paint): List<String> = wrap(text, paint, PAGE_WIDTH - MARGIN * 2)
        fun drawLines(lines: List<String>, paint: Paint, lineHeight: Float) {
            lines.forEach { line -> ensure(lineHeight); canvas!!.drawText(line, MARGIN, y, paint); y += lineHeight }
        }

        diaries.forEachIndexed { index, diary ->
            ensure(90f)
            if (index > 0) { canvas!!.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, meta); y += 24f }
            drawLines(textLines(diary.title.ifBlank { "无题日记" }, title), title, title.textSize * 1.25f)
            if (content.config.includeMetadata) {
                val date = diary.date ?: diary.createdAt.toLocalDate()
                val metadata = buildList {
                    add(date.format(DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", Locale.CHINA)))
                    diary.mood?.let { add("心情：${it.label}") }
                    diary.weather?.let { add("天气：$it") }
                    diary.location?.let { add("地点：$it") }
                    if (diary.latitude != null && diary.longitude != null) add("坐标：${diary.latitude}, ${diary.longitude}")
                    if (diary.tags.isNotEmpty()) add("标签：${diary.tags.joinToString(" / ")}")
                }.joinToString("   ")
                drawLines(textLines(metadata, meta), meta, meta.textSize * 1.45f)
                y += 10f
            }
            drawLines(textLines(diary.content, body), body, body.textSize * 1.55f)
            if (content.config.includePhotos) diary.images.forEach { path ->
                val bitmap = BitmapFactory.decodeFile(path) ?: return@forEach
                try {
                    val maxWidth = PAGE_WIDTH - MARGIN * 2
                    val height = (bitmap.height.toFloat() / bitmap.width * maxWidth).coerceAtMost(260f)
                    ensure(height + 14f)
                    canvas!!.drawBitmap(bitmap, null, RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + height), Paint(Paint.ANTI_ALIAS_FLAG))
                    y += height + 14f
                } finally { bitmap.recycle() }
            }
            y += 18f
        }
        finishPage()
        return pageCount
    }

    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        text.replace("\r", "").split("\n").forEach { paragraph ->
            if (paragraph.isEmpty()) { result.add(""); return@forEach }
            var line = StringBuilder()
            paragraph.forEach { char ->
                if (paint.measureText(line.toString() + char) > maxWidth && line.isNotEmpty()) {
                    result.add(line.toString()); line = StringBuilder().append(char)
                } else line.append(char)
            }
            if (line.isNotEmpty()) result.add(line.toString())
        }
        return result
    }

    private fun fitRect(width: Int, height: Int): RectF {
        val availableW = PAGE_WIDTH - MARGIN * 2
        val availableH = PAGE_HEIGHT - MARGIN * 2
        val ratio = minOf(availableW / width, availableH / height)
        val w = width * ratio; val h = height * ratio
        return RectF((PAGE_WIDTH - w) / 2, (PAGE_HEIGHT - h) / 2, (PAGE_WIDTH + w) / 2, (PAGE_HEIGHT + h) / 2)
    }

    private fun drawFooter(canvas: Canvas, page: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; color = 0xFF77716A.toInt(); textAlign = Paint.Align.CENTER }
        canvas.drawText("MINDTRACE · $page", PAGE_WIDTH / 2f, PAGE_HEIGHT - 24f, paint)
    }

    private fun pageInfo(number: Int) = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create()
}
