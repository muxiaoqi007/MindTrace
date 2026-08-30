package com.mindtrace.diary.core.capsule

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindtrace.diary.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TimeCapsuleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE).orEmpty().ifEmpty { "给未来的自己" }
        notificationHelper.showTimeCapsuleNotification(id, title)
        return Result.success()
    }

    companion object {
        const val KEY_ID = "capsule_id"
        const val KEY_TITLE = "capsule_title"
    }
}
