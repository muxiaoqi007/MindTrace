package com.mindtrace.diary.core.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.mindtrace.diary.domain.model.DailyReceipt
import com.mindtrace.diary.domain.model.MoodLevel
import java.time.format.DateTimeFormatter
import java.util.Locale

object DailyReceiptImageRenderer {
    private const val WIDTH = 1080
    private const val HEIGHT = 1580
    private const val HORIZONTAL_PADDING = 92f
    private const val RECEIPT_BACKGROUND = 0xFFFFFCF3.toInt()
    private const val RECEIPT_TEXT = 0xFF29251F.toInt()
    private const val RECEIPT_MUTED = 0xFF716B61.toInt()

    fun render(receipt: DailyReceipt): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(RECEIPT_BACKGROUND)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RECEIPT_TEXT
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        var y = 116f

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 52f
        canvas.drawText("MINDTRACE", WIDTH / 2f, y, paint)
        y += 58f
        paint.textSize = 28f
        paint.color = RECEIPT_MUTED
        canvas.drawText("今日生活小票", WIDTH / 2f, y, paint)
        y += 74f

        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 30f
        paint.color = RECEIPT_TEXT
        canvas.drawText(
            receipt.date.format(DateTimeFormatter.ofPattern("yyyy / MM / dd   EEEE", Locale.CHINA)),
            WIDTH / 2f,
            y,
            paint
        )
        y += 62f
        y = drawDivider(canvas, paint, y)

        paint.textAlign = Paint.Align.LEFT
        y = drawPair(canvas, paint, y, "心情", receipt.mood?.let(::moodText) ?: "未记录")
        y = drawPair(canvas, paint, y, "写下日记", "${receipt.diaryCount} 篇")
        y = drawPair(canvas, paint, y, "收集闪念", "${receipt.flashNoteCount} 条")
        y = drawPair(canvas, paint, y, "完成待办", "${receipt.completedTodoCount} 项")
        y = drawPair(canvas, paint, y, "仍在路上", "${receipt.pendingTodoCount} 项")
        y = drawPair(canvas, paint, y, "日记字数", "${receipt.wordCount} 字")
        y += 18f
        y = drawDivider(canvas, paint, y)

        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 30f
        canvas.drawText("今日一句", HORIZONTAL_PADDING, y, paint)
        y += 54f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 32f
        val highlight = receipt.highlight?.let { "“$it”" } ?: "今天还没有留下文字记录。"
        y = drawWrappedText(canvas, paint, highlight, y, lineHeight = 48f)
        y += 32f

        if (receipt.keywords.isNotEmpty()) {
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            paint.textSize = 30f
            canvas.drawText("今日关键词", HORIZONTAL_PADDING, y, paint)
            y += 52f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            paint.textSize = 30f
            y = drawWrappedText(
                canvas = canvas,
                paint = paint,
                text = receipt.keywords.joinToString("  /  "),
                y = y,
                lineHeight = 44f
            )
            y += 28f
        }

        y = drawDivider(canvas, paint, y)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 34f
        canvas.drawText("TOTAL", HORIZONTAL_PADDING, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            if (receipt.hasContent) "认真生活了 1 天" else "等待今天的第一笔记录",
            WIDTH - HORIZONTAL_PADDING,
            y,
            paint
        )

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 24f
        paint.color = RECEIPT_MUTED
        canvas.drawText("所有内容均由本机记录生成", WIDTH / 2f, HEIGHT - 104f, paint)
        canvas.drawText("MINDTRACE · KEEP THE SMALL THINGS", WIDTH / 2f, HEIGHT - 62f, paint)
        return bitmap
    }

    private fun drawPair(canvas: Canvas, paint: Paint, y: Float, label: String, value: String): Float {
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 32f
        paint.color = RECEIPT_TEXT
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(label, HORIZONTAL_PADDING, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, WIDTH - HORIZONTAL_PADDING, y, paint)
        return y + 58f
    }

    private fun drawDivider(canvas: Canvas, paint: Paint, y: Float): Float {
        paint.color = RECEIPT_MUTED
        paint.strokeWidth = 2f
        var x = HORIZONTAL_PADDING
        while (x < WIDTH - HORIZONTAL_PADDING) {
            canvas.drawLine(x, y, (x + 14f).coerceAtMost(WIDTH - HORIZONTAL_PADDING), y, paint)
            x += 26f
        }
        paint.color = RECEIPT_TEXT
        return y + 64f
    }

    private fun drawWrappedText(
        canvas: Canvas,
        paint: Paint,
        text: String,
        y: Float,
        lineHeight: Float
    ): Float {
        val maxWidth = WIDTH - HORIZONTAL_PADDING * 2
        var line = StringBuilder()
        var currentY = y
        text.forEach { character ->
            val candidate = line.toString() + character
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line.toString(), HORIZONTAL_PADDING, currentY, paint)
                currentY += lineHeight
                line = StringBuilder().append(character)
            } else {
                line.append(character)
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line.toString(), HORIZONTAL_PADDING, currentY, paint)
            currentY += lineHeight
        }
        return currentY
    }

    private fun moodText(mood: MoodLevel): String = when (mood) {
        MoodLevel.GREAT -> "非常好 :)"
        MoodLevel.GOOD -> "不错 :)"
        MoodLevel.OKAY -> "平静 :|"
        MoodLevel.BAD -> "有点低落 :(("
        MoodLevel.AWFUL -> "很难熬 :((("
    }
}
