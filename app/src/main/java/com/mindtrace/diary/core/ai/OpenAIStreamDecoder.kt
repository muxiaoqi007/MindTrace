package com.mindtrace.diary.core.ai

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/** Decodes one OpenAI-compatible SSE line without owning transport state. */
object OpenAIStreamDecoder {
    private val gson = Gson()

    fun decodeLine(line: String): List<StreamChunk> {
        val normalized = line.trimStart()
        if (!normalized.startsWith(DATA_PREFIX)) return emptyList()

        val payload = normalized.removePrefix(DATA_PREFIX).trim()
        if (payload == DONE_MARKER) {
            return listOf(StreamChunk(content = "", isFinished = true))
        }
        if (payload.isEmpty()) return emptyList()

        return try {
            val event = gson.fromJson(payload, StreamEvent::class.java)
            val choice = event.choices?.firstOrNull() ?: return emptyList()
            buildList {
                choice.delta?.content
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { content -> add(StreamChunk(content)) }
                if (!choice.finishReason.isNullOrBlank()) {
                    add(StreamChunk(content = "", isFinished = true))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private data class StreamEvent(val choices: List<Choice>?)

    private data class Choice(
        val delta: Delta?,
        @SerializedName("finish_reason") val finishReason: String?
    )

    private data class Delta(val content: String?)

    private const val DATA_PREFIX = "data:"
    private const val DONE_MARKER = "[DONE]"
}
