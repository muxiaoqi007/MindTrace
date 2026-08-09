package com.mindtrace.diary.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class AIProviderExceptionTest {
    @Test
    fun mapsAuthenticationErrorsWithoutLeakingResponseBody() {
        val error = AIProviderException.fromHttpStatus(401)

        assertEquals("API Key 无效或没有访问权限", error.message)
        assertEquals(401, error.statusCode)
        assertFalse(error.retryable)
    }

    @Test
    fun marksRateLimitAndServerErrorsRetryable() {
        assertTrue(AIProviderException.fromHttpStatus(429).retryable)
        assertTrue(AIProviderException.fromHttpStatus(503).retryable)
    }

    @Test
    fun convertsTransportFailuresToSafeMessages() {
        val timeout = AIProviderException.fromFailure(SocketTimeoutException("secret host"))
        val network = AIProviderException.fromFailure(IOException("secret url"))

        assertEquals("AI 服务响应超时，请稍后重试", timeout.message)
        assertEquals("无法连接 AI 服务，请检查网络和接口地址", network.message)
    }
}
