package com.mindtrace.diary.domain.usecase.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SelfNarrativeParserTest {

    @Test
    fun parsesCompleteNarrative() {
        val parsed = SelfNarrativeParser.parse(
            """{"narrative":"你是一个喜欢记录生活的人。","keywords":["坚持","温柔","好奇"],"suggestion":"今晚早点休息。"}"""
        )

        assertEquals("你是一个喜欢记录生活的人。", parsed?.narrative)
        assertEquals(listOf("坚持", "温柔", "好奇"), parsed?.keywords)
        assertEquals("今晚早点休息。", parsed?.suggestion)
    }

    @Test
    fun dedupesAndFiltersKeywords() {
        val parsed = SelfNarrativeParser.parse(
            """{"narrative":"叙事","keywords":["重复","重复","","这是一个超过十二个字的超长关键词"],"suggestion":"建议"}"""
        )

        assertEquals(listOf("重复"), parsed?.keywords)
    }

    @Test
    fun rejectsMissingKeywords() {
        assertNull(
            SelfNarrativeParser.parse("""{"narrative":"叙事","keywords":[],"suggestion":"建议"}""")
        )
    }

    @Test
    fun rejectsMissingSuggestion() {
        assertNull(
            SelfNarrativeParser.parse("""{"narrative":"叙事","keywords":["关键词"]}""")
        )
    }

    @Test
    fun rejectsOverlongNarrative() {
        val longNarrative = "长".repeat(601)
        assertNull(
            SelfNarrativeParser.parse("""{"narrative":"$longNarrative","keywords":["关键词"],"suggestion":"建议"}""")
        )
    }

    @Test
    fun toleratesNonStringKeywordElements() {
        val parsed = SelfNarrativeParser.parse(
            """{"narrative":"叙事","keywords":[1,2,"有效"],"suggestion":"建议"}"""
        )

        assertTrue(parsed?.keywords?.contains("有效") == true)
    }
}
