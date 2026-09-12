package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.domain.model.AIMemory
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.MemoryCategory
import com.mindtrace.diary.domain.model.MemoryType
import com.mindtrace.diary.domain.model.MoodLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ProactiveNudgeRulesTest {

    private val today: LocalDate = LocalDate.of(2026, 9, 12)

    private fun diary(
        date: LocalDate,
        mood: MoodLevel? = null,
        excludeFromAI: Boolean = false,
        title: String = "标题"
    ): Diary = Diary(
        id = "$date-$mood-$title",
        title = title,
        content = "内容",
        mood = mood,
        excludeFromAI = excludeFromAI,
        createdAt = date.atTime(20, 0),
        updatedAt = date.atTime(20, 0)
    )

    @Test
    fun firesLowMoodWhenTodayMoodIsBad() {
        val candidate = ProactiveNudgeRules.detect(
            today = today,
            diaries = listOf(diary(today, MoodLevel.BAD)),
            hour = 20
        )

        assertEquals(NudgeType.LOW_MOOD_TODAY, candidate?.type)
    }

    @Test
    fun doesNotFireLowMoodWhenTodayMoodIsOkayOrBetter() {
        val candidate = ProactiveNudgeRules.detect(
            today = today,
            diaries = listOf(diary(today, MoodLevel.OKAY)),
            hour = 20
        )

        assertNull(candidate)
    }

    @Test
    fun usesLatestDiaryMoodOfDay() {
        val early = diary(today, MoodLevel.AWFUL).copy(id = "early", createdAt = today.atTime(9, 0))
        val late = diary(today, MoodLevel.GOOD).copy(id = "late", createdAt = today.atTime(21, 0))

        val candidate = ProactiveNudgeRules.detect(today, listOf(early, late), hour = 20)

        assertNull(candidate)
    }

    @Test
    fun firesMoodDeclineOnThreeStrictlyFallingDays() {
        val diaries = listOf(
            diary(today, MoodLevel.OKAY),
            diary(today.minusDays(1), MoodLevel.GOOD),
            diary(today.minusDays(2), MoodLevel.GREAT)
        )

        val candidate = ProactiveNudgeRules.detect(today, diaries, hour = 20)

        assertEquals(NudgeType.MOOD_DECLINE, candidate?.type)
    }

    @Test
    fun doesNotFireMoodDeclineOnGapOrPlateau() {
        // 缺了前天的心情记录
        val withGap = listOf(
            diary(today, MoodLevel.OKAY),
            diary(today.minusDays(1), MoodLevel.GOOD)
        )
        assertNull(ProactiveNudgeRules.detect(today, withGap, hour = 20))

        // 前天和昨天一样高，不算连降
        val plateau = listOf(
            diary(today, MoodLevel.OKAY),
            diary(today.minusDays(1), MoodLevel.GOOD),
            diary(today.minusDays(2), MoodLevel.GOOD)
        )
        assertNull(ProactiveNudgeRules.detect(today, plateau, hour = 20))
    }

    @Test
    fun lowMoodWinsOverDecline() {
        val diaries = listOf(
            diary(today, MoodLevel.BAD),
            diary(today.minusDays(1), MoodLevel.OKAY),
            diary(today.minusDays(2), MoodLevel.GREAT)
        )

        val candidate = ProactiveNudgeRules.detect(today, diaries, hour = 20)

        assertEquals(NudgeType.LOW_MOOD_TODAY, candidate?.type)
    }

    @Test
    fun ignoresDiariesExcludedFromAI() {
        val diaries = listOf(
            diary(today, MoodLevel.BAD, excludeFromAI = true)
        )

        val candidate = ProactiveNudgeRules.detect(today, diaries, hour = 20)

        assertNull(candidate)
    }

    @Test
    fun firesStreakBreakInTheEveningWithoutTodayDiary() {
        val diaries = listOf(
            diary(today.minusDays(1)),
            diary(today.minusDays(2)),
            diary(today.minusDays(3))
        )

        val candidate = ProactiveNudgeRules.detect(today, diaries, hour = 19)

        assertEquals(NudgeType.STREAK_BREAK, candidate?.type)
        assertEquals(3, candidate?.streakDays)
    }

    @Test
    fun doesNotFireStreakBreakBeforeEveningOrWithShortStreak() {
        val diaries = listOf(
            diary(today.minusDays(1)),
            diary(today.minusDays(2)),
            diary(today.minusDays(3))
        )
        // 还没到傍晚
        assertNull(ProactiveNudgeRules.detect(today, diaries, hour = 16))

        // 连续记录只有两天
        val shortStreak = listOf(
            diary(today.minusDays(1)),
            diary(today.minusDays(2))
        )
        assertNull(ProactiveNudgeRules.detect(today, shortStreak, hour = 20))

        // 今天已经写了
        assertNull(
            ProactiveNudgeRules.detect(
                today,
                diaries + diary(today),
                hour = 20
            )
        )
    }

    private fun relationshipMemory(
        subject: String,
        importance: Float = 0.5f
    ): AIMemory = AIMemory(
        id = "memory-$subject",
        type = MemoryType.AUTO,
        category = MemoryCategory.RELATIONSHIP,
        content = "用户和${subject}关系很好",
        subject = subject,
        importance = importance,
        createdAt = today.minusDays(30).atTime(12, 0),
        updatedAt = today.minusDays(30).atTime(12, 0)
    )

    @Test
    fun firesPersonRecallWhenSubjectNotMentionedRecently() {
        val memories = listOf(relationshipMemory("小雅"))
        // 20 天前提到过小雅，14 天窗口内没有
        val diaries = listOf(diary(today.minusDays(20), title = "和小雅吃饭"))

        val candidate = ProactiveNudgeRules.detect(today, diaries, memories, hour = 12)

        assertEquals(NudgeType.PERSON_RECALL, candidate?.type)
        assertEquals("小雅", candidate?.subject)
    }

    @Test
    fun doesNotFirePersonRecallWhenMentionedWithinWindow() {
        val memories = listOf(relationshipMemory("小雅"))
        val diaries = listOf(diary(today.minusDays(3), title = "和小雅逛街"))

        assertNull(ProactiveNudgeRules.detect(today, diaries, memories, hour = 12))
    }

    @Test
    fun picksHighestImportanceUnmentionedSubject() {
        val memories = listOf(
            relationshipMemory("小雅", importance = 0.4f),
            relationshipMemory("阿哲", importance = 0.9f)
        )
        val diaries = listOf(diary(today.minusDays(2), title = "和小雅唱歌"))

        val candidate = ProactiveNudgeRules.detect(today, diaries, memories, hour = 12)

        assertEquals("阿哲", candidate?.subject)
    }

    @Test
    fun ignoresNonRelationshipOrKeywordlessMemories() {
        val goalMemory = relationshipMemory("跑步").copy(
            category = MemoryCategory.GOAL,
            subject = "跑步"
        )
        val noSubject = relationshipMemory("")
        val shortSubject = relationshipMemory("妈")

        assertNull(
            ProactiveNudgeRules.detect(
                today,
                emptyList(),
                listOf(goalMemory, noSubject, shortSubject),
                hour = 12
            )
        )
    }

    @Test
    fun personRecallWinsOverStreakBreak() {
        val memories = listOf(relationshipMemory("小雅"))
        val diaries = listOf(
            diary(today.minusDays(1)),
            diary(today.minusDays(2)),
            diary(today.minusDays(3))
        )

        val candidate = ProactiveNudgeRules.detect(today, diaries, memories, hour = 20)

        assertEquals(NudgeType.PERSON_RECALL, candidate?.type)
    }
}
