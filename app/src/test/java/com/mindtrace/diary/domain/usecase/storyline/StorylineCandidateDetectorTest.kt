package com.mindtrace.diary.domain.usecase.storyline

import com.mindtrace.diary.domain.model.Diary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StorylineCandidateDetectorTest {
    private val day = LocalDate.of(2026, 8, 1)

    @Test fun requiresTwoDistinctEvidenceDiaries() {
        val result = StorylineCandidateDetector.detect(listOf(diary("1", listOf("写作练习"))))
        assertTrue(result.isEmpty())
    }

    @Test fun createsGroundedCandidateAndSuppressesRejectedName() {
        val diaries = listOf(diary("1", listOf("写作练习")), diary("2", listOf("写作练习"), 1))
        val candidate = StorylineCandidateDetector.detect(diaries).single()
        assertEquals(2, candidate.evidence.size)
        assertTrue(StorylineCandidateDetector.detect(diaries, setOf(candidate.normalizedName)).isEmpty())
    }

    @Test fun excludesPrivateDiaryEvidence() {
        val result = StorylineCandidateDetector.detect(listOf(diary("1", listOf("项目A")), diary("2", listOf("项目A"), 1, true)))
        assertTrue(result.isEmpty())
    }

    private fun diary(id: String, tags: List<String>, offset: Long = 0, hidden: Boolean = false) = Diary(
        id = id, title = "", content = "证据 $id", tags = tags, date = day.plusDays(offset),
        createdAt = day.plusDays(offset).atStartOfDay(), updatedAt = day.plusDays(offset).atStartOfDay(),
        excludeFromResurfacing = hidden
    )
}
