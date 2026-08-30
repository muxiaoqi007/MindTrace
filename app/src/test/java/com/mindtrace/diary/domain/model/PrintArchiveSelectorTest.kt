package com.mindtrace.diary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PrintArchiveSelectorTest {
    private val start = LocalDate.of(2026, 8, 1)

    @Test fun filtersByInclusiveDateAndTag() {
        val config = PrintArchiveConfig(startDate = start, endDate = start.plusDays(5), tag = "旅行")
        val result = PrintArchiveSelector.select(config, listOf(diary("a", 1, listOf("旅行")), diary("b", 6, listOf("旅行")), diary("c", 2, listOf("工作"))))
        assertEquals(listOf("a"), result.diaries.map(Diary::id))
    }

    @Test fun excludesPrivateByDefaultButAllowsExplicitInclusion() {
        val privateDiary = diary("private", 1, emptyList(), true)
        val base = PrintArchiveConfig(startDate = start, endDate = start.plusDays(2))
        assertTrue(PrintArchiveSelector.select(base, listOf(privateDiary)).diaries.isEmpty())
        assertEquals(1, PrintArchiveSelector.select(base.copy(includePrivate = true), listOf(privateDiary)).diaries.size)
    }

    @Test fun producesAllReceiptDatesAndDistinctMondayWeeks() {
        val config = PrintArchiveConfig(type = PrintArchiveType.RECEIPTS, startDate = start, endDate = start.plusDays(10))
        val result = PrintArchiveSelector.select(config, emptyList())
        assertEquals(11, result.dates.size)
        assertEquals(result.weekAnchors.distinct(), result.weekAnchors)
    }

    @Test fun pageEstimateRespondsToFontAndPhotos() {
        val diary = Diary("d", "标题", "x".repeat(1800), images = listOf("1", "2", "3"), createdAt = start.atStartOfDay(), updatedAt = start.atStartOfDay())
        val compact = PrintArchiveSelector.estimatedJournalPages(listOf(diary), .8f, false)
        val large = PrintArchiveSelector.estimatedJournalPages(listOf(diary), 1.5f, true)
        assertTrue(large > compact)
    }

    private fun diary(id: String, offset: Long, tags: List<String>, hidden: Boolean = false) = Diary(
        id, "", "内容", tags = tags, date = start.plusDays(offset),
        createdAt = start.plusDays(offset).atStartOfDay(), updatedAt = start.plusDays(offset).atStartOfDay(), excludeFromResurfacing = hidden
    )
}
