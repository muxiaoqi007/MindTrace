package com.mindtrace.diary.domain.usecase.facets

import com.mindtrace.diary.domain.model.FacetCheckIn
import com.mindtrace.diary.domain.model.FacetInsight
import com.mindtrace.diary.domain.model.FacetMoodObservation
import com.mindtrace.diary.domain.model.FacetOptionInsight

object FacetCorrelationCalculator {
    const val MIN_SAMPLE_SIZE = 3

    fun calculate(
        facetId: String,
        options: List<String>,
        checkIns: List<FacetCheckIn>,
        moods: List<FacetMoodObservation>
    ): FacetInsight {
        val relevant = checkIns.filter { it.facetId == facetId }
        val moodByDate = moods.groupBy(FacetMoodObservation::date)
            .mapValues { (_, values) -> values.map(FacetMoodObservation::score).average().toFloat() }
        val baseline = relevant.mapNotNull { moodByDate[it.date] }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()

        val orderedOptions = (options + relevant.map(FacetCheckIn::option)).distinct()
        val insights = orderedOptions.map { option ->
            val optionCheckIns = relevant.filter { it.option == option }
            val evidence = optionCheckIns.mapNotNull { checkIn ->
                moodByDate[checkIn.date]?.let { score -> checkIn.date to score }
            }.distinctBy { it.first }
            val average = evidence.map { it.second }
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.toFloat()
            val enough = evidence.size >= MIN_SAMPLE_SIZE
            FacetOptionInsight(
                option = option,
                sampleCount = evidence.size,
                averageMood = average,
                moodDifference = if (enough && average != null && baseline != null) average - baseline else null,
                evidenceDates = evidence.map { it.first }.sortedDescending(),
                hasEnoughEvidence = enough
            )
        }

        return FacetInsight(facetId = facetId, baselineMood = baseline, options = insights)
    }
}
