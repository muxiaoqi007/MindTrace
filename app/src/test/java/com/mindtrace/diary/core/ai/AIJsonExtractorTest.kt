package com.mindtrace.diary.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AIJsonExtractorTest {

    @Test
    fun extractsObjectFromMarkdownFenceAndPrefix() {
        val raw = "分析如下：\n```json\n{\"summary\":\"今天很好\",\"tags\":[]}\n```"

        assertEquals(
            "{\"summary\":\"今天很好\",\"tags\":[]}",
            AIJsonExtractor.extractFirstObject(raw)
        )
    }

    @Test
    fun handlesBracesAndEscapesInsideJsonStrings() {
        val json = "{\"content\":\"写下 {想法} 和 \\\"感受\\\"\",\"ok\":true}"

        assertEquals(json, AIJsonExtractor.extractFirstObject("prefix $json suffix"))
    }

    @Test
    fun rejectsUnclosedObject() {
        assertNull(AIJsonExtractor.extractFirstObject("text {\"summary\":\"broken\""))
    }
}
