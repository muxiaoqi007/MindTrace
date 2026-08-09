package com.mindtrace.diary.core.ai

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mindtrace.diary.core.datastore.AIConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(AIProviderException.fromHttpStatus(response.code))
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(AIProviderException("AI 服务返回了空响应"))
                val chatResponse = gson.fromJson(responseBody, ChatCompletionResponse::class.java)
                val choice = chatResponse.choices.firstOrNull()
                    ?: return@withContext Result.failure(AIProviderException("AI 服务没有返回可用内容"))
                val content = choice.message.content
                if (content.isBlank()) {
                    return@withContext Result.failure(AIProviderException("AI 服务返回了空内容"))
                }

                Result.success(ChatResponse(content = content, finishReason = choice.finishReason))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AIProviderException.fromFailure(e))
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
                if (call.isCanceled()) {
                    close()
                } else {
                    close(AIProviderException.fromFailure(e))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { safeResponse ->
                    if (!safeResponse.isSuccessful) {
                        close(AIProviderException.fromHttpStatus(safeResponse.code))
                        return
                    }

                    try {
                        val source = safeResponse.body?.source()
                            ?: run {
                                close(AIProviderException("AI 服务返回了空响应"))
                                return
                            }
                        var terminalSent = false

                        while (!source.exhausted() && !terminalSent) {
                            val line = source.readUtf8Line() ?: continue
                            for (chunk in OpenAIStreamDecoder.decodeLine(line)) {
                                if (chunk.isFinished) {
                                    if (!terminalSent) {
                                        trySend(chunk)
                                        terminalSent = true
                                    }
                                } else {
                                    trySend(chunk)
                                }
                            }
                        }

                        // Some compatible providers close the stream without [DONE].
                        if (!terminalSent) {
                            trySend(StreamChunk(content = "", isFinished = true))
                        }
                        close()
                    } catch (e: Exception) {
                        close(AIProviderException.fromFailure(e))
                    }
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
