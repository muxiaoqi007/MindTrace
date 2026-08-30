package com.mindtrace.diary.domain.usecase.memorywalk

import com.mindtrace.diary.domain.model.MemoryWalkCandidate
import com.mindtrace.diary.domain.model.MemoryWalkMode
import com.mindtrace.diary.domain.model.MemoryWalkSourceType
import com.mindtrace.diary.domain.model.MoodLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MemoryWalkPlannerTest {
    private val today = LocalDate.of(2026, 8, 23)

    @Test
    fun surpriseWalkExcludesTodayAndReturnsUniqueStops() {
        val candidates = listOf(
            candidate("today", today),
            candidate("one", LocalDate.of(2023, 2, 1)),
            candidate("two", LocalDate.of(2024, 3, 2)),
            candidate("three", LocalDate.of(2025, 4, 3)),
            candidate("four", LocalDate.of(2025, 6, 4))
        )

        val plan = MemoryWalkPlanner.plan(
            candidates = candidates,
            mode = MemoryWalkMode.SURPRISE,
            today = today,
            seed = 42L
        )

        assertEquals(3, plan.stops.size)
        assertEquals(3, plan.stops.map { it.memory.id }.distinct().size)
        assertTrue(plan.stops.none { it.memory.date == today })
        assertEquals(3, plan.stops.map { it.memory.date.year }.distinct().size)
    }

    @Test
    fun keywordWalkMatchesContentTitleAndTagsIgnoringCase() {
        val candidates = listOf(
            candidate("content", today.minusYears(1), excerpt = "开始学习画画"),
            candidate("title", today.minusYears(2), title = "我的画画计划"),
            candidate("tag", today.minusYears(3), tags = listOf("画画")),
            candidate("other", today.minusYears(4), excerpt = "今天跑步")
        )

        val plan = MemoryWalkPlanner.plan(
            candidates = candidates,
            mode = MemoryWalkMode.KEYWORD,
            keyword = " 画画 ",
            today = today,
            seed = 7L
        )

        assertEquals(setOf("content", "title", "tag"), plan.stops.map { it.memory.id }.toSet())
    }

    @Test
    fun moodWalkOnlyReturnsRequestedMood() {
        val candidates = listOf(
            candidate("good-one", today.minusYears(1), mood = MoodLevel.GOOD),
            candidate("bad", today.minusYears(2), mood = MoodLevel.BAD),
            candidate("good-two", today.minusYears(3), mood = MoodLevel.GOOD)
        )

        val plan = MemoryWalkPlanner.plan(
            candidates = candidates,
            mode = MemoryWalkMode.MOOD,
            mood = MoodLevel.GOOD,
            today = today,
            seed = 3L
        )

        assertEquals(2, plan.stops.size)
        assertTrue(plan.stops.all { it.memory.mood == MoodLevel.GOOD })
    }

    @Test
    fun sameSeedProducesSameWalk() {
        val candidates = (1..8).map { index ->
            candidate("id-$index", today.minusDays(index.toLong()))
        }

        val first = MemoryWalkPlanner.plan(candidates, MemoryWalkMode.SURPRISE, today = today, seed = 99L)
        val second = MemoryWalkPlanner.plan(candidates, MemoryWalkMode.SURPRISE, today = today, seed = 99L)

        assertEquals(first.stops.map { it.memory.id }, second.stops.map { it.memory.id })
    }

    private fun candidate(
        id: String,
        date: LocalDate,
        title: String? = null,
        excerpt: String = id,
        tags: List<String> = emptyList(),
        mood: MoodLevel? = null
    ) = MemoryWalkCandidate(
        id = id,
        sourceType = MemoryWalkSourceType.DIARY,
        date = date,
        title = title,
        excerpt = excerpt,
        mood = mood,
        tags = tags
    )
}
