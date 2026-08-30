package com.mindtrace.diary.domain.usecase.memorywalk

import com.mindtrace.diary.domain.model.MemoryWalkCandidate
import com.mindtrace.diary.domain.model.MemoryWalkMode
import com.mindtrace.diary.domain.model.MemoryWalkPlan
import com.mindtrace.diary.domain.model.MemoryWalkStop
import com.mindtrace.diary.domain.model.MoodLevel
import java.time.LocalDate
import java.util.Locale
import kotlin.random.Random

object MemoryWalkPlanner {
    fun plan(
        candidates: List<MemoryWalkCandidate>,
        mode: MemoryWalkMode,
        keyword: String = "",
        mood: MoodLevel? = null,
        today: LocalDate = LocalDate.now(),
        seed: Long = System.currentTimeMillis(),
        stopCount: Int = DEFAULT_STOP_COUNT
    ): MemoryWalkPlan {
        require(stopCount > 0)
        val normalizedKeyword = keyword.trim().lowercase(Locale.ROOT)
        val eligible = candidates
            .asSequence()
            .filter { memory -> memory.date.isBefore(today) }
            .filter { memory ->
                when (mode) {
                    MemoryWalkMode.SURPRISE -> true
                    MemoryWalkMode.KEYWORD -> normalizedKeyword.isNotEmpty() && memory.matches(normalizedKeyword)
                    MemoryWalkMode.MOOD -> mood != null && memory.mood == mood
                }
            }
            .distinctBy { memory -> memory.sourceType to memory.id }
            .sortedWith(compareBy<MemoryWalkCandidate> { it.date }.thenBy { it.id })
            .toList()
            .shuffled(Random(seed))

        val selected = mutableListOf<MemoryWalkCandidate>()
        val selectedYears = mutableSetOf<Int>()
        eligible.forEach { memory ->
            if (selected.size < stopCount && selectedYears.add(memory.date.year)) {
                selected += memory
            }
        }
        eligible.forEach { memory ->
            if (selected.size < stopCount && memory !in selected) {
                selected += memory
            }
        }

        return MemoryWalkPlan(
            mode = mode,
            seed = seed,
            stops = selected.mapIndexed { index, memory ->
                MemoryWalkStop(
                    position = index + 1,
                    memory = memory,
                    reflectionPrompt = PROMPTS[index.coerceAtMost(PROMPTS.lastIndex)]
                )
            }
        )
    }

    private fun MemoryWalkCandidate.matches(normalizedKeyword: String): Boolean {
        val searchable = buildList {
            title?.let(::add)
            add(excerpt)
            addAll(tags)
        }
        return searchable.any { value ->
            value.lowercase(Locale.ROOT).contains(normalizedKeyword)
        }
    }

    private val PROMPTS = listOf(
        "看到这段记录，你最先想起的是什么？",
        "它和上一站之间，有没有一条只有你能看见的线？",
        "现在的你，想对当时的自己说些什么？"
    )
    private const val DEFAULT_STOP_COUNT = 3
}
