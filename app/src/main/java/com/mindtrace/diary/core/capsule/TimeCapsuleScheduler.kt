package com.mindtrace.diary.core.capsule

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object TimeCapsuleScheduler {
    fun schedule(context: Context, id: String, title: String, unlockAt: LocalDateTime) {
        val delay = Duration.between(LocalDateTime.now(), unlockAt).toMillis().coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<TimeCapsuleWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder().putString(TimeCapsuleWorker.KEY_ID, id).putString(TimeCapsuleWorker.KEY_TITLE, title).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(id))
    }

    private fun workName(id: String) = "time_capsule_$id"
}
