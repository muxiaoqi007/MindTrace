package com.mindtrace.diary.core.brief

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.notification.NotificationHelper
import com.mindtrace.diary.domain.usecase.ai.GenerateMorningBriefUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * 晨间简报 Worker
 * 在指定时间生成简报并发送通知；简报不依赖 AI，AI 不可用时降级为本地拼装
 */
@HiltWorker
class MorningBriefWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsDataStore: SettingsDataStore,
    private val generateMorningBriefUseCase: GenerateMorningBriefUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "MorningBriefWorker"
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting morning brief work")

        return try {
            // 1. 检查功能是否启用
            val config = settingsDataStore.morningBriefConfig.first()
            if (!config.enabled) {
                Log.d(TAG, "Morning brief is disabled")
                return Result.success()
            }

            // 2. 生成简报（含缓存写入；AI 失败时为本地降级版）
            generateMorningBriefUseCase().fold(
                onSuccess = { brief ->
                    Log.d(TAG, "Morning brief generated (local=${brief.isLocal})")
                    // 3. 发送通知
                    notificationHelper.showMorningBriefNotification(brief)
                    // 4. 调度明天的任务
                    scheduleNext(config.hour, config.minute)
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to generate morning brief", error)
                    if (runAttemptCount < MAX_RETRY_COUNT) {
                        Result.retry()
                    } else {
                        // 即使失败也要调度明天的任务
                        scheduleNext(config.hour, config.minute)
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in morning brief work", e)
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                try {
                    val config = settingsDataStore.morningBriefConfig.first()
                    scheduleNext(config.hour, config.minute)
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to schedule next morning brief", e2)
                }
                Result.failure()
            }
        }
    }

    private fun scheduleNext(hour: Int, minute: Int) {
        MorningBriefScheduler.scheduleNext(context, hour, minute)
    }
}
