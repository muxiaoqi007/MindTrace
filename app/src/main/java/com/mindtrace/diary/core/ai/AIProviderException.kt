package com.mindtrace.diary.core.ai

import java.io.IOException
import java.net.SocketTimeoutException

class AIProviderException(
    message: String,
    val statusCode: Int? = null,
    val retryable: Boolean = false,
    cause: Throwable? = null
) : IOException(message, cause) {
    companion object {
        fun fromHttpStatus(statusCode: Int): AIProviderException {
            val message = when (statusCode) {
                400 -> "AI 服务拒绝了请求，请检查模型名称和接口兼容性"
                401, 403 -> "API Key 无效或没有访问权限"
                404 -> "AI 接口地址或模型不存在"
                408 -> "AI 服务响应超时，请稍后重试"
                409 -> "AI 服务暂时无法处理该请求"
                413 -> "发送给 AI 的上下文过长"
                429 -> "AI 请求过于频繁或账户额度不足"
                in 500..599 -> "AI 服务暂时不可用，请稍后重试"
                else -> "AI 请求失败（HTTP $statusCode）"
            }
            return AIProviderException(
                message = message,
                statusCode = statusCode,
                retryable = statusCode == 408 || statusCode == 409 || statusCode == 429 || statusCode >= 500
            )
        }

        fun fromFailure(cause: Exception): AIProviderException = when (cause) {
            is AIProviderException -> cause
            is SocketTimeoutException -> AIProviderException(
                message = "AI 服务响应超时，请稍后重试",
                retryable = true,
                cause = cause
            )
            is IOException -> AIProviderException(
                message = "无法连接 AI 服务，请检查网络和接口地址",
                retryable = true,
                cause = cause
            )
            else -> AIProviderException(
                message = "AI 服务返回了无法解析的数据",
                cause = cause
            )
        }
    }
}
