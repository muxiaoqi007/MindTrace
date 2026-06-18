package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.core.sync.SyncResult

interface SyncRepository {
    suspend fun sync(): SyncResult
    suspend fun uploadData()
    suspend fun downloadData()
    suspend fun testConnection(): Boolean
    suspend fun getLastSyncTime(): Long?
}
