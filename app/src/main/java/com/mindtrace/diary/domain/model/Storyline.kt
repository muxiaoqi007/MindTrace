package com.mindtrace.diary.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class StorylineType(val label: String) { PROJECT("项目"), RELATIONSHIP("关系"), SKILL("技能"), OTHER("其他") }
enum class StorylineStatus { CANDIDATE, CONFIRMED, REJECTED, ARCHIVED }

data class StorylineSource(
    val id: String,
    val storylineId: String,
    val diaryId: String,
    val date: LocalDate,
    val excerpt: String
)

data class Storyline(
    val id: String,
    val name: String,
    val normalizedName: String,
    val type: StorylineType,
    val status: StorylineStatus,
    val sources: List<StorylineSource>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class StorylineCandidate(
    val name: String,
    val normalizedName: String,
    val type: StorylineType,
    val evidence: List<StorylineCandidateEvidence>
)

data class StorylineCandidateEvidence(val diaryId: String, val date: LocalDate, val excerpt: String)
