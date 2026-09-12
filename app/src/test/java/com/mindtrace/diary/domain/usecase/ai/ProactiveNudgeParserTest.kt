package com.mindtrace.diary.domain.usecase.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProactiveNudgeParserTest {

    @Test
    fun parsesCleanJson() {
        assertEquals("今晚早点休息，好吗？", ProactiveNudgeParser.parse("""{"message":"今晚早点休息，好吗？"}"""))
    }

    @Test
    fun parsesJsonWrappedInMarkdownFence() {
        val parsed = ProactiveNudgeParser.parse(
            "好的：\n```json\n{\"message\":\"照顾好自己\"}\n```"
        )

        assertEquals("照顾好自己", parsed)
    }

    @Test
    fun trimsWhitespace() {
        assertEquals("带空格的话", ProactiveNudgeParser.parse("""{"message":"  带空格的话  "}"""))
    }

    @Test
    fun rejectsMissingBlankOrOverlongMessage() {
        assertNull(ProactiveNudgeParser.parse("""{"text":"字段名不对"}"""))
        assertNull(ProactiveNudgeParser.parse("""{"message":"   "}"""))
        assertNull(ProactiveNudgeParser.parse("""{"message":"${"长".repeat(61)}"}"""))
        assertNull(ProactiveNudgeParser.parse("没有任何 JSON"))
    }
}
