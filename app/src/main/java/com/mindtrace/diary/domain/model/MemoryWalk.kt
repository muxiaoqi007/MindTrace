package com.mindtrace.diary.domain.model

import java.time.LocalDate

enum class MemoryWalkMode {
    SURPRISE,
    KEYWORD,
    MOOD
}

enum class MemoryWalkSourceType {
    DIARY,
    FLASH_NOTE
}

data class MemoryWalkCandidate(
    val id: String,
    val sourceType: MemoryWalkSourceType,
    val date: LocalDate,
    val title: String?,
    val excerpt: String,
    val mood: MoodLevel?,
    val tags: List<String>
)

data class MemoryWalkStop(
    val position: Int,
    val memory: MemoryWalkCandidate,
    val reflectionPrompt: String
)

data class MemoryWalkPlan(
    val mode: MemoryWalkMode,
    val seed: Long,
    val stops: List<MemoryWalkStop>
)
