package com.mindtrace.diary.core.ai

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mindtrace.diary.core.datastore.AIConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * OpenAI 兼容 API Provider
 * 支持任意兼容 OpenAI API 格式的供应商
 */
class OpenAICompatibleProvider(
    private val config: AIConfig
) : LLMProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val baseUrl: String
        get() = config.baseUrl.trimEnd('/')

    override suspend fun chat(
        messages: List<ChatMessage>,
        model: String?
    ): Result<ChatResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = ChatRequest(
                model = model ?: config.model,
                messages = messages.map { MessageDTO(it.role.toApiString(), it.content) },
                stream = false
            )

            val request = Request.Builder()
                .url("$baseUrl/chat/completions")
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(gson.toJson(requestBody).toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                return@withContext Result.failure(
                    IOException("API error: ${response.code} - $errorBody")
                )
            }

            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(IOException("Empty response"))

            val chatResponse = gson.fromJson(responseBody, ChatCompletionResponse::class.java)
            val content = chatResponse.choices.firstOrNull()?.message?.content ?: ""

            Result.success(ChatResponse(
                content = content,
                finishReason = chatResponse.choices.firstOrNull()?.finishReason
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun chatStream(
        messages: List<ChatMessage>,
        model: String?
    ): Flow<StreamChunk> = callbackFlow {
        val requestBody = ChatRequest(
            model = model ?: config.model,
            messages = messages.map { MessageDTO(it.role.toApiString(), it.content) },
            stream = true
        )

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .post(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    close(IOException("API error: ${response.code}"))
                    return
                }

                try {
                    val source: BufferedSource = response.body?.source()
                        ?: run {
                            close(IOException("Empty response body"))
                            return
                        }

                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: continue

                        if (line.startsWith("data: ")) {
                            val data = line.removePrefix("data: ").trim()

                            if (data == "[DONE]") {
                                trySend(StreamChunk("", isFinished = true))
                                break
                            }

                            try {
                                val chunk = gson.fromJson(data, ChatCompletionChunk::class.java)
                                val content = chunk.choices.firstOrNull()?.delta?.content ?: ""
                                if (content.isNotEmpty()) {
                                    trySend(StreamChunk(content))
                                }
                            } catch (e: Exception) {
                                // 忽略解析错误，继续处理下一行
                            }
                        }
                    }

                    close()
                } catch (e: Exception) {
                    close(e)
                }
            }
        })

        awaitClose {
            call.cancel()
        }
    }

    override suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = chat(
                listOf(ChatMessage(ChatMessage.Role.USER, "Hi")),
                null
            )
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }
}

// ========== API DTOs ==========

private data class ChatRequest(
    val model: String,
    val messages: List<MessageDTO>,
    val stream: Boolean = false,
    val temperature: Float = 0.7f,
    @SerializedName("max_tokens")
    val maxTokens: Int? = null
)

private data class MessageDTO(
    val role: String,
    val content: String
)

private data class ChatCompletionResponse(
    val id: String?,
    val choices: List<Choice>,
    val usage: Usage?
) {
    data class Choice(
        val index: Int,
        val message: MessageDTO,
        @SerializedName("finish_reason")
        val finishReason: String?
    )

    data class Usage(
        @SerializedName("prompt_tokens")
        val promptTokens: Int,
        @SerializedName("completion_tokens")
        val completionTokens: Int,
        @SerializedName("total_tokens")
        val totalTokens: Int
    )
}

private data class ChatCompletionChunk(
    val id: String?,
    val choices: List<ChunkChoice>
) {
    data class ChunkChoice(
        val index: Int,
        val delta: Delta,
        @SerializedName("finish_reason")
        val finishReason: String?
    )

    data class Delta(
        val role: String?,
        val content: String?
    )
}
