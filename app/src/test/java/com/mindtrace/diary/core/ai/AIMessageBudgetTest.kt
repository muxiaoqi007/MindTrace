package com.mindtrace.diary.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIMessageBudgetTest {

    @Test
    fun keepsNewestMessagesInChronologicalOrder() {
        val messages = (1..25).map { index ->
            ChatMessage(ChatMessage.Role.USER, "message-$index")
        }

        val result = AIMessageBudget.trim(messages, maxMessages = 20)

        assertEquals(20, result.size)
        assertEquals("message-6", result.first().content)
        assertEquals("message-25", result.last().content)
    }

    @Test
    fun boundsIndividualMessageWhileKeepingBeginningAndEnd() {
        val content = "START" + "x".repeat(100) + "END"

        val result = AIMessageBudget.trim(
            listOf(ChatMessage(ChatMessage.Role.USER, content)),
            maxCharsPerMessage = 20,
            maxTotalChars = 20
        ).single().content

        assertTrue(result.length <= 20)
        assertTrue(result.startsWith("START"))
        assertTrue(result.endsWith("END"))
    }

    @Test
    fun totalRequestStaysWithinBudget() {
        val messages = (1..10).map { index ->
            ChatMessage(ChatMessage.Role.ASSISTANT, "$index-" + "x".repeat(20))
        }

        val result = AIMessageBudget.trim(
            messages,
            maxMessages = 10,
            maxCharsPerMessage = 30,
            maxTotalChars = 55
        )

        assertTrue(result.sumOf { it.content.length } <= 55)
        assertEquals("10-", result.last().content.take(3))
    }
}
