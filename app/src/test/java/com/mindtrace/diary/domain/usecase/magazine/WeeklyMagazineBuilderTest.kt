package com.mindtrace.diary.domain.usecase.magazine

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.FlashNote
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.model.Todo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeeklyMagazineBuilderTest {
    private val monday = LocalDate.of(2026, 8, 17)

    @Test
    fun usesMondayThroughSundayBoundaries() {
        val magazine = WeeklyMagazineBuilder.build(
            monday.plusDays(3),
            listOf(diary("inside", monday, "周一", MoodLevel.GOOD), diary("outside", monday.minusDays(1), "上周", MoodLevel.BAD)),
            emptyList(), emptyList()
        )
        assertEquals(monday, magazine.weekStart)
        assertEquals(monday.plusDays(6), magazine.weekEnd)
        assertEquals(1, magazine.diaryCount)
        assertEquals(4f, magazine.moodStrip.first().averageScore!!, 0f)
    }

    @Test
    fun excludesPrivateSourcesAndKeepsEmptyWeekHonest() {
        val magazine = WeeklyMagazineBuilder.build(
            monday,
            listOf(diary("hidden", monday, "私密", MoodLevel.GREAT, true)),
            listOf(FlashNote("f", "私密闪念", createdAt = monday.atTime(9, 0), updatedAt = monday.atTime(9, 0), excludeFromResurfacing = true)),
            emptyList()
        )
        assertFalse(magazine.hasContent)
        assertTrue(magazine.topMoments.isEmpty())
    }

    @Test
    fun separatesCompletedGoalsAndCarryoverThreads() {
        val magazine = WeeklyMagazineBuilder.build(
            monday, emptyList(), emptyList(),
            listOf(
                Todo("done", "完成报告", true, createdAt = monday.atStartOfDay(), completedAt = monday.plusDays(2).atTime(10, 0)),
                Todo("pending", "继续读书", false, dueDate = monday.plusDays(4).atTime(20, 0), createdAt = monday.atStartOfDay())
            )
        )
        assertEquals(listOf("完成报告"), magazine.completedGoals)
        assertEquals(listOf("继续读书"), magazine.unresolvedThreads)
    }

    private fun diary(id: String, date: LocalDate, content: String, mood: MoodLevel, hidden: Boolean = false) = Diary(
        id = id, title = "", content = content, mood = mood, date = date,
        createdAt = date.atTime(8, 0), updatedAt = date.atTime(8, 0), excludeFromResurfacing = hidden
    )
}
