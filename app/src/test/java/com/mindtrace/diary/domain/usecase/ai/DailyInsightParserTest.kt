package com.mindtrace.diary.domain.usecase.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyInsightParserTest {

    @Test
    fun parsesCleanJson() {
        val parsed = DailyInsightParser.parse(
            """{"observation":"最近你常常在晚上记录跑步后的畅快","question":"今晚想为身体做点什么？"}"""
        )

        assertEquals("最近你常常在晚上记录跑步后的畅快", parsed?.first)
        assertEquals("今晚想为身体做点什么？", parsed?.second)
    }

    @Test
    fun parsesJsonWrappedInMarkdownFence() {
        val parsed = DailyInsightParser.parse(
            "好的，以下是洞察：\n```json\n{\"observation\":\"观察\",\"question\":\"问题\"}\n```"
        )

        assertEquals("观察", parsed?.first)
        assertEquals("问题", parsed?.second)
    }

    @Test
    fun trimsWhitespaceFields() {
        val parsed = DailyInsightParser.parse("""{"observation":"  带空格的观察  ","question":" 带空格的问题 "}""")

        assertEquals("带空格的观察", parsed?.first)
        assertEquals("带空格的问题", parsed?.second)
    }

    @Test
    fun rejectsMissingFields() {
        assertNull(DailyInsightParser.parse("""{"observation":"只有观察"}"""))
        assertNull(DailyInsightParser.parse("""{"question":"只有问题"}"""))
        assertNull(DailyInsightParser.parse("没有任何 JSON"))
    }

    @Test
    fun rejectsBlankFields() {
        assertNull(DailyInsightParser.parse("""{"observation":"   ","question":"问题"}"""))
        assertNull(DailyInsightParser.parse("""{"observation":"观察","question":""}"""))
    }

    @Test
    fun rejectsOverlongFields() {
        val longObservation = "长".repeat(81)
        val longQuestion = "长".repeat(61)
        assertNull(DailyInsightParser.parse("""{"observation":"$longObservation","question":"问题"}"""))
        assertNull(DailyInsightParser.parse("""{"observation":"观察","question":"$longQuestion"}"""))
    }
}
