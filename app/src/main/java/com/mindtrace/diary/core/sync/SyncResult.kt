package com.mindtrace.diary.core.sync

sealed class SyncResult {
    data class Success(
        val uploadedCount: Int,
        val downloadedCount: Int,
        val syncTime: Long
    ) : SyncResult()

    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : SyncResult()

    data object NotConfigured : SyncResult()
}

sealed class RestoreResult {
    data class Success(val restoredCount: Int) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
    data object NotConfigured : RestoreResult()
}
