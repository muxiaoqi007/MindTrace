package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

data class SelfPortrait(
    val totalDiaries: Int,
    val totalWords: Int,
    val writingStreak: Int,
    val dominantMoodName: String?,
    val topTags: List<Pair<String, Int>>,
    val personalityMemories: List<AIMemory>,
    val preferenceMemories: List<AIMemory>,
    val goalMemories: List<AIMemory>,
    val relationshipMemories: List<AIMemory>,
    val factMemories: List<AIMemory>,
    val recentMemoryCount: Int,
    val updatedAt: LocalDateTime
)
