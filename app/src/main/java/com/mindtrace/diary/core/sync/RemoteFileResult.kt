package com.mindtrace.diary.core.sync

sealed class RemoteFileResult {
    data class Found(val data: ByteArray) : RemoteFileResult()
    data object NotFound : RemoteFileResult()
    data class Failure(val message: String, val cause: Exception) : RemoteFileResult()
}
