package com.mindtrace.diary.domain.usecase.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MorningBriefParserTest {

    @Test
    fun parsesCleanJson() {
        val parsed = MorningBriefParser.parse(
            """{"greeting":"早上好，今天有 2 件待办","observation":"昨天你聊到久违的跑步","question":"今天想为身体做点什么？"}"""
        )

        assertEquals("早上好，今天有 2 件待办", parsed?.greeting)
        assertEquals("昨天你聊到久违的跑步", parsed?.observation)
        assertEquals("今天想为身体做点什么？", parsed?.question)
    }

    @Test
    fun parsesJsonWrappedInMarkdownFence() {
        val parsed = MorningBriefParser.parse(
            "好的，以下是简报：\n```json\n{\"greeting\":\"问候\",\"observation\":\"观察\",\"question\":\"问题\"}\n```"
        )

        assertEquals("问候", parsed?.greeting)
        assertEquals("观察", parsed?.observation)
        assertEquals("问题", parsed?.question)
    }

    @Test
    fun treatsBlankObservationAndQuestionAsNull() {
        val parsed = MorningBriefParser.parse(
            """{"greeting":"问候","observation":"","question":"  "}"""
        )

        assertEquals("问候", parsed?.greeting)
        assertNull(parsed?.observation)
        assertNull(parsed?.question)
    }

    @Test
    fun acceptsMissingObservationAndQuestionFields() {
        val parsed = MorningBriefParser.parse("""{"greeting":"只有问候"}""")

        assertEquals("只有问候", parsed?.greeting)
        assertNull(parsed?.observation)
        assertNull(parsed?.question)
    }

    @Test
    fun rejectsMissingOrBlankGreeting() {
        assertNull(MorningBriefParser.parse("""{"observation":"没有问候"}"""))
        assertNull(MorningBriefParser.parse("""{"greeting":"   "}"""))
        assertNull(MorningBriefParser.parse("没有任何 JSON"))
    }

    @Test
    fun rejectsOverlongFields() {
        assertNull(
            MorningBriefParser.parse(
                """{"greeting":"${"长".repeat(49)}","observation":"观察","question":"问题"}"""
            )
        )
        assertNull(
            MorningBriefParser.parse(
                """{"greeting":"问候","observation":"${"长".repeat(81)}","question":"问题"}"""
            )
        )
        assertNull(
            MorningBriefParser.parse(
                """{"greeting":"问候","observation":"观察","question":"${"长".repeat(61)}"}"""
            )
        )
    }
}
