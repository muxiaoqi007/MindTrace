package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.Todo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class MorningBriefBuilderTest {

    private val today: LocalDate = LocalDate.of(2026, 9, 12)

    private fun diary(
        date: LocalDate,
        title: String = "标题",
        content: String = "内容"
    ): Diary = Diary(
        id = date.toString(),
        title = title,
        content = content,
        createdAt = date.atTime(20, 0),
        updatedAt = date.atTime(20, 0)
    )

    private fun todo(
        id: String,
        content: String,
        dueDate: LocalDateTime? = null,
        createdAt: LocalDateTime = LocalDateTime.of(2026, 9, 11, 9, 0)
    ): Todo = Todo(
        id = id,
        content = content,
        dueDate = dueDate,
        createdAt = createdAt
    )

    private fun build(
        yesterdayDiaries: List<Diary> = emptyList(),
        allDiaries: List<Diary> = emptyList(),
        pendingTodos: List<Todo> = emptyList(),
        memoryDiary: Diary? = null,
        unreadReviewCount: Int = 0
    ) = MorningBriefBuilder.build(
        today = today,
        yesterdayDiaries = yesterdayDiaries,
        allDiaries = allDiaries,
        pendingTodos = pendingTodos,
        memoryDiary = memoryDiary,
        unreadReviewCount = unreadReviewCount
    )

    @Test
    fun countsStreakIncludingYesterdayWhenTodayNotWritten() {
        val diaries = listOf(
            diary(today.minusDays(1)),
            diary(today.minusDays(2)),
            diary(today.minusDays(3))
        )

        assertEquals(3, build(allDiaries = diaries).streakDays)
    }

    @Test
    fun countsStreakFromTodayWhenWritten() {
        val diaries = listOf(
            diary(today),
            diary(today.minusDays(1)),
            diary(today.minusDays(2))
        )

        assertEquals(3, build(allDiaries = diaries).streakDays)
    }

    @Test
    fun breaksStreakOnGap() {
        val diaries = listOf(
            diary(today),
            diary(today.minusDays(2))
        )

        assertEquals(1, build(allDiaries = diaries).streakDays)
    }

    @Test
    fun zeroStreakWithoutRecentDiaries() {
        val diaries = listOf(diary(today.minusDays(5)))

        assertEquals(0, build(allDiaries = diaries).streakDays)
    }

    @Test
    fun buildsObservationFromLatestYesterdayDiaryTitle() {
        val brief = build(
            yesterdayDiaries = listOf(
                diary(today.minusDays(1), title = "夜跑十公里", content = "内容")
            )
        )

        assertEquals("昨天你写下「夜跑十公里」", brief.observation)
    }

    @Test
    fun fallsBackToContentWhenTitleBlank() {
        val brief = build(
            yesterdayDiaries = listOf(
                diary(today.minusDays(1), title = "", content = "很久没有这么放松的一天了")
            )
        )

        assertEquals("昨天你写下「很久没有这么放松的一天了」", brief.observation)
    }

    @Test
    fun noObservationWithoutYesterdayDiary() {
        assertNull(build().observation)
    }

    @Test
    fun previewsUpToThreeTodosSortedByDueDate() {
        val brief = build(
            pendingTodos = listOf(
                todo("1", "无截止时间的待办"),
                todo("2", "晚上交方案", dueDate = today.atTime(18, 0)),
                todo("3", "回邮件", dueDate = today.atTime(9, 0)),
                todo("4", "超出预览的第四条")
            )
        )

        assertEquals(listOf("回邮件", "晚上交方案", "无截止时间的待办"), brief.pendingTodos)
        assertEquals(4, brief.pendingTodoCount)
    }

    @Test
    fun computesMemoryYearsAgoFromDiaryDate() {
        val brief = build(
            memoryDiary = diary(today.minusYears(2), content = "两年前的今天在海边看日落")
        )

        assertEquals(2, brief.memoryYearsAgo)
        assertEquals("两年前的今天在海边看日落", brief.memoryExcerpt)
    }

    @Test
    fun ignoresMemoryFromCurrentYear() {
        val brief = build(memoryDiary = diary(today.minusYears(0)))

        assertNull(brief.memoryYearsAgo)
        assertNull(brief.memoryExcerpt)
    }

    @Test
    fun localGreetingBranches() {
        assertEquals("新的一天，从第一篇日记开始。", MorningBriefBuilder.greetingFor(0, hasNoDiaries = true))
        assertEquals("新的一天，随时回来记录都可以。", MorningBriefBuilder.greetingFor(0, hasNoDiaries = false))
        assertEquals("早上好，把今天也记下来吧。", MorningBriefBuilder.greetingFor(3, hasNoDiaries = false))
        assertEquals("连续记录 7 天了，今天继续。", MorningBriefBuilder.greetingFor(7, hasNoDiaries = false))
    }

    @Test
    fun builtBriefIsAlwaysLocal() {
        val brief = build(allDiaries = listOf(diary(today)))

        assertEquals(true, brief.isLocal)
        assertNull(brief.question)
    }
}
