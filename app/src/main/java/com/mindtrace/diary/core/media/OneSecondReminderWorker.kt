package com.mindtrace.diary.core.media

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class OneSecondReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val settings: SettingsDataStore,
    private val notifications: NotificationHelper
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (settings.oneSecondReminderEnabled.first()) notifications.showOneSecondReminder()
        return Result.success()
    }
}
