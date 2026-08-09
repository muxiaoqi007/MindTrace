package com.mindtrace.diary.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAIStreamDecoderTest {

    @Test
    fun decodesContentWithOrWithoutSpaceAfterDataPrefix() {
        val withSpace = OpenAIStreamDecoder.decodeLine(
            "data: {\"choices\":[{\"delta\":{\"content\":\"你\"},\"finish_reason\":null}]}"
        )
        val withoutSpace = OpenAIStreamDecoder.decodeLine(
            "data:{\"choices\":[{\"delta\":{\"content\":\"好\"},\"finish_reason\":null}]}"
        )

        assertEquals(listOf(StreamChunk("你")), withSpace)
        assertEquals(listOf(StreamChunk("好")), withoutSpace)
    }

    @Test
    fun finishReasonEmitsContentThenTerminalChunk() {
        val chunks = OpenAIStreamDecoder.decodeLine(
            "data: {\"choices\":[{\"delta\":{\"content\":\"。\"},\"finish_reason\":\"stop\"}]}"
        )

        assertEquals(StreamChunk("。"), chunks[0])
        assertTrue(chunks[1].isFinished)
    }

    @Test
    fun doneMarkerEmitsTerminalChunk() {
        assertEquals(
            listOf(StreamChunk(content = "", isFinished = true)),
            OpenAIStreamDecoder.decodeLine("data: [DONE]")
        )
    }

    @Test
    fun ignoresUsageOnlyAndMalformedEvents() {
        assertTrue(OpenAIStreamDecoder.decodeLine("data: {\"choices\":[],\"usage\":{}}").isEmpty())
        assertTrue(OpenAIStreamDecoder.decodeLine("data: not-json").isEmpty())
        assertTrue(OpenAIStreamDecoder.decodeLine(": keep-alive").isEmpty())
    }
}
