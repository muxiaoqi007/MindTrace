package com.mindtrace.diary.domain.usecase.facets

import com.mindtrace.diary.domain.model.FacetCheckIn
import com.mindtrace.diary.domain.model.FacetMoodObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FacetCorrelationCalculatorTest {
    private val start = LocalDate.of(2026, 8, 1)

    @Test
    fun hidesDifferenceUntilMinimumSampleIsReached() {
        val result = FacetCorrelationCalculator.calculate(
            facetId = "sleep",
            options = listOf("早睡", "晚睡"),
            checkIns = listOf(checkIn(0, "早睡"), checkIn(1, "早睡")),
            moods = listOf(mood(0, .8f), mood(1, .7f))
        )

        val early = result.options.first { it.option == "早睡" }
        assertFalse(early.hasEnoughEvidence)
        assertNull(early.moodDifference)
        assertEquals(2, early.evidenceDates.size)
    }

    @Test
    fun reportsAssociationAgainstObservedBaselineWithEvidenceDates() {
        val result = FacetCorrelationCalculator.calculate(
            facetId = "sleep",
            options = listOf("早睡", "晚睡"),
            checkIns = listOf(
                checkIn(0, "早睡"), checkIn(1, "早睡"), checkIn(2, "早睡"),
                checkIn(3, "晚睡"), checkIn(4, "晚睡"), checkIn(5, "晚睡")
            ),
            moods = listOf(
                mood(0, .9f), mood(1, .8f), mood(2, .7f),
                mood(3, .3f), mood(4, .4f), mood(5, .5f)
            )
        )

        val early = result.options.first { it.option == "早睡" }
        assertTrue(early.hasEnoughEvidence)
        assertEquals(.2f, early.moodDifference!!, .0001f)
        assertEquals(listOf(start.plusDays(2), start.plusDays(1), start), early.evidenceDates)
    }

    @Test
    fun retainsCustomOptionRecordedAfterFacetCreation() {
        val result = FacetCorrelationCalculator.calculate(
            facetId = "sleep",
            options = listOf("充沛"),
            checkIns = listOf(checkIn(0, "慢热")),
            moods = listOf(mood(0, .6f))
        )

        assertTrue(result.options.any { it.option == "慢热" })
    }

    private fun checkIn(day: Long, option: String) = FacetCheckIn(
        id = "$day-$option",
        facetId = "sleep",
        option = option,
        date = start.plusDays(day),
        createdAt = start.plusDays(day).atTime(8, 0)
    )

    private fun mood(day: Long, score: Float) = FacetMoodObservation(start.plusDays(day), score)
}
