package com.mindtrace.diary.domain.usecase.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FollowUpQuestionParserTest {
    private val source = "今天第一次一个人去游泳，下水前很紧张，游完却觉得很轻松。"

    @Test
    fun parsesGroundedQuestion() {
        val parsed = FollowUpQuestionParser.parse(
            """```json
                {"question":"下水前的紧张来自什么？","evidence":"下水前很紧张"}
            ```""".trimIndent(),
            source
        )

        assertEquals("下水前的紧张来自什么？", parsed?.question)
    }

    @Test
    fun rejectsInventedEvidence() {
        val parsed = FollowUpQuestionParser.parse(
            """{"question":"你为什么和朋友吵架？","evidence":"和朋友吵架"}""",
            source
        )

        assertNull(parsed)
    }

    @Test
    fun rejectsMissingEvidence() {
        assertNull(FollowUpQuestionParser.parse("""{"question":"你怎么想？"}""", source))
    }
}
