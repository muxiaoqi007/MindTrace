package com.mindtrace.diary.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class LexiconType(val label: String) { PERSON("人物"), PLACE("地点"), PHRASE("词语"), WISH("愿望") }
enum class LexiconStatus { CANDIDATE, CONFIRMED, REJECTED }

data class LexiconEvidence(val id: String, val entryId: String, val diaryId: String, val date: LocalDate, val excerpt: String)

data class LexiconEntry(
    val id: String,
    val term: String,
    val normalizedTerm: String,
    val type: LexiconType,
    val generatedMeaning: String,
    val correctedMeaning: String?,
    val status: LexiconStatus,
    val evidence: List<LexiconEvidence>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    val displayMeaning: String get() = correctedMeaning?.takeIf(String::isNotBlank) ?: generatedMeaning
}

data class LexiconCandidate(
    val term: String,
    val normalizedTerm: String,
    val type: LexiconType,
    val generatedMeaning: String,
    val evidence: List<LexiconCandidateEvidence>
)

data class LexiconCandidateEvidence(val diaryId: String, val date: LocalDate, val excerpt: String)
