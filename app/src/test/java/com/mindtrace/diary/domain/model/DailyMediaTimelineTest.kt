package com.mindtrace.diary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.YearMonth

class DailyMediaTimelineTest {
    private val month = YearMonth.of(2026, 8)

    @Test fun includesEveryDayAndPreservesGaps() {
        val slots = DailyMediaTimeline.month(month, listOf(pick(2)))
        assertEquals(31, slots.size)
        assertNull(slots.first().pick)
        assertEquals("2", slots[1].pick?.id)
    }

    @Test fun ordersByCalendarDate() {
        val slots = DailyMediaTimeline.month(month, listOf(pick(20), pick(3))).filter { it.pick != null }
        assertEquals(listOf(3, 20), slots.map { it.date.dayOfMonth })
    }

    private fun pick(day: Int) = DailyMediaPick(day.toString(), month.atDay(day), "content://$day", DailyMediaType.IMAGE, LocalDateTime.of(2026, 8, day, 12, 0))
}
