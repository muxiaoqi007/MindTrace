package com.mindtrace.diary.domain.usecase.receipt

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.FlashNote
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.model.Todo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class DailyReceiptBuilderTest {
    private val targetDate = LocalDate.of(2026, 8, 23)

    @Test
    fun aggregatesOnlyRecordsBelongingToSelectedDate() {
        val receipt = DailyReceiptBuilder.build(
            date = targetDate,
            diaries = listOf(
                diary("dated", date = targetDate, createdHour = 8, content = "今天 写了 一篇日记", mood = MoodLevel.GOOD),
                diary("fallback", date = null, createdHour = 10, content = "第二篇", mood = MoodLevel.GREAT),
                diary("other", date = targetDate.minusDays(1), createdHour = 12, content = "昨天")
            ),
            flashNotes = listOf(
                flash("today", "傍晚的风很好", 18),
                flash("other", "明天", 2, targetDate.plusDays(1))
            ),
            todos = listOf(
                todo("done", completedAt = targetDate.atTime(9, 0)),
                todo("pending", dueDate = targetDate.atTime(20, 0)),
                todo("old", dueDate = targetDate.minusDays(1).atTime(20, 0))
            )
        )

        assertEquals(2, receipt.diaryCount)
        assertEquals(1, receipt.flashNoteCount)
        assertEquals(1, receipt.completedTodoCount)
        assertEquals(1, receipt.pendingTodoCount)
        assertEquals(MoodLevel.GREAT, receipt.mood)
        assertTrue(receipt.hasContent)
    }

    @Test
    fun usesLatestFlashAsHighlightAndCleansWhitespace() {
        val receipt = DailyReceiptBuilder.build(
            date = targetDate,
            diaries = listOf(diary("diary", targetDate, 8, "日记正文")),
            flashNotes = listOf(
                flash("early", "早上的想法", 9),
                flash("late", "  晚风\n吹过来的时候，突然觉得慢一点也很好。  ", 21)
            ),
            todos = emptyList()
        )

        assertEquals("晚风 吹过来的时候，突然觉得慢一点也很好。", receipt.highlight)
    }

    @Test
    fun ranksNormalizedDiaryTagsAndCountsNonWhitespaceCharacters() {
        val receipt = DailyReceiptBuilder.build(
            date = targetDate,
            diaries = listOf(
                diary(
                    id = "one",
                    date = targetDate,
                    createdHour = 8,
                    content = "一 二\n三",
                    tags = listOf("散步", "项目"),
                    aiTags = listOf("晚风", "散步")
                ),
                diary(
                    id = "two",
                    date = targetDate,
                    createdHour = 10,
                    content = "四五",
                    tags = listOf("项目", "散步")
                )
            ),
            flashNotes = emptyList(),
            todos = emptyList()
        )

        assertEquals(5, receipt.wordCount)
        assertEquals(listOf("散步", "项目", "晚风"), receipt.keywords)
    }

    @Test
    fun returnsHonestEmptyReceiptWithoutInventedContent() {
        val receipt = DailyReceiptBuilder.build(
            date = targetDate,
            diaries = emptyList(),
            flashNotes = emptyList(),
            todos = emptyList()
        )

        assertFalse(receipt.hasContent)
        assertNull(receipt.mood)
        assertNull(receipt.highlight)
        assertTrue(receipt.keywords.isEmpty())
    }

    @Test
    fun excludesEntriesThatAreHiddenFromResurfacing() {
        val receipt = DailyReceiptBuilder.build(
            date = targetDate,
            diaries = listOf(
                diary("visible", targetDate, 8, "可见日记"),
                diary("private", targetDate, 9, "私密日记", excludeFromResurfacing = true)
            ),
            flashNotes = listOf(
                flash("visible", "可见闪念", 10),
                flash("private", "私密闪念", 11, excludeFromResurfacing = true)
            ),
            todos = emptyList()
        )

        assertEquals(1, receipt.diaryCount)
        assertEquals(1, receipt.flashNoteCount)
        assertEquals("可见闪念", receipt.highlight)
    }

    private fun diary(
        id: String,
        date: LocalDate?,
        createdHour: Int,
        content: String,
        mood: MoodLevel? = null,
        tags: List<String> = emptyList(),
        aiTags: List<String> = emptyList(),
        excludeFromResurfacing: Boolean = false
    ) = Diary(
        id = id,
        title = "",
        content = content,
        mood = mood,
        tags = tags,
        aiTags = aiTags,
        date = date,
        excludeFromResurfacing = excludeFromResurfacing,
        createdAt = targetDate.atTime(createdHour, 0),
        updatedAt = targetDate.atTime(createdHour, 0)
    )

    private fun flash(
        id: String,
        content: String,
        hour: Int,
        date: LocalDate = targetDate,
        excludeFromResurfacing: Boolean = false
    ) = FlashNote(
        id = id,
        content = content,
        excludeFromResurfacing = excludeFromResurfacing,
        createdAt = date.atTime(hour, 0),
        updatedAt = date.atTime(hour, 0)
    )

    private fun todo(
        id: String,
        dueDate: LocalDateTime? = null,
        completedAt: LocalDateTime? = null
    ) = Todo(
        id = id,
        content = id,
        isCompleted = completedAt != null,
        dueDate = dueDate,
        createdAt = targetDate.minusDays(2).atStartOfDay(),
        completedAt = completedAt
    )
}
