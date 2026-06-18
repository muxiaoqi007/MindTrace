package com.mindtrace.diary.core.review

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.notification.NotificationHelper
import com.mindtrace.diary.domain.usecase.review.GenerateMidnightReviewUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * 深夜回信 Worker
 * 在指定时间执行，生成 AI 回信并发送通知
 */
@HiltWorker
class MidnightReviewWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsDataStore: SettingsDataStore,
    private val generateMidnightReviewUseCase: GenerateMidnightReviewUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "MidnightReviewWorker"
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting midnight review work")

        return try {
            // 1. 检查功能是否启用
            val config = settingsDataStore.midnightReviewConfig.first()
            if (!config.enabled) {
                Log.d(TAG, "Midnight review is disabled")
                return Result.success()
            }

            // 2. 检查 AI 是否配置
            val aiConfig = settingsDataStore.aiConfig.first()
            if (!aiConfig.isConfigured || !aiConfig.enabled) {
                Log.d(TAG, "AI is not configured or disabled")
                scheduleNextReview(config.hour, config.minute)
                return Result.success()
            }

            // 3. 生成回信
            val result = generateMidnightReviewUseCase()

            result.fold(
                onSuccess = { review ->
                    if (review != null) {
                        Log.d(TAG, "Review generated successfully: ${review.id}")
                        // 4. 发送通知
                        notificationHelper.showMidnightReviewNotification(review)
                    } else {
                        Log.d(TAG, "No review generated (no diaries today or already exists)")
                    }
                    // 5. 调度明天的任务
                    scheduleNextReview(config.hour, config.minute)
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to generate review", error)
                    if (runAttemptCount < MAX_RETRY_COUNT) {
                        Result.retry()
                    } else {
                        // 即使失败也要调度明天的任务
                        scheduleNextReview(config.hour, config.minute)
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in midnight review work", e)
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                // 尝试调度明天的任务
                try {
                    val config = settingsDataStore.midnightReviewConfig.first()
                    scheduleNextReview(config.hour, config.minute)
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to schedule next review", e2)
                }
                Result.failure()
            }
        }
    }

    private fun scheduleNextReview(hour: Int, minute: Int) {
        MidnightReviewScheduler.scheduleNext(context, hour, minute)
    }
}
