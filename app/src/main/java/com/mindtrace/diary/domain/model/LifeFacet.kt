package com.mindtrace.diary.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class LifeFacet(
    val id: String,
    val name: String,
    val icon: String,
    val color: Long,
    val options: List<String>,
    val isArchived: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class FacetCheckIn(
    val id: String,
    val facetId: String,
    val option: String,
    val date: LocalDate,
    val createdAt: LocalDateTime
)

data class FacetMoodObservation(
    val date: LocalDate,
    val score: Float
)

data class FacetOptionInsight(
    val option: String,
    val sampleCount: Int,
    val averageMood: Float?,
    val moodDifference: Float?,
    val evidenceDates: List<LocalDate>,
    val hasEnoughEvidence: Boolean
)

data class FacetInsight(
    val facetId: String,
    val baselineMood: Float?,
    val options: List<FacetOptionInsight>
)
