package com.mindtrace.diary.core.nudge

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.notification.NotificationHelper
import com.mindtrace.diary.domain.usecase.ai.GenerateProactiveNudgeUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * 主动关怀 Worker
 * 每日在指定时间运行：本地规则命中才生成措辞并发通知，每天最多一条。
 * AI 不可用时措辞自动降级为本地模板。
 */
@HiltWorker
class ProactiveNudgeWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsDataStore: SettingsDataStore,
    private val generateProactiveNudgeUseCase: GenerateProactiveNudgeUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ProactiveNudgeWorker"
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting proactive nudge work")

        return try {
            // 1. 检查功能是否启用
            val config = settingsDataStore.proactiveNudgeConfig.first()
            if (!config.enabled) {
                Log.d(TAG, "Proactive nudge is disabled")
                return Result.success()
            }

            // 2. 规则筛命 + 措辞（命中且未达当日频控才返回非 null）
            generateProactiveNudgeUseCase().fold(
                onSuccess = { nudge ->
                    if (nudge != null) {
                        Log.d(TAG, "Proactive nudge fired: ${nudge.type} (local=${nudge.isLocal})")
                        // 3. 发送通知并记入当日频控
                        notificationHelper.showProactiveNudgeNotification(nudge.message)
                        settingsDataStore.setProactiveLastNudgeDate(LocalDate.now())
                    } else {
                        Log.d(TAG, "No nudge candidate or daily budget used")
                    }
                    // 4. 调度明天的任务
                    scheduleNext(config.hour, config.minute)
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to generate proactive nudge", error)
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
            Log.e(TAG, "Unexpected error in proactive nudge work", e)
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                try {
                    val config = settingsDataStore.proactiveNudgeConfig.first()
                    scheduleNext(config.hour, config.minute)
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to schedule next proactive nudge", e2)
                }
                Result.failure()
            }
        }
    }

    private fun scheduleNext(hour: Int, minute: Int) {
        ProactiveNudgeScheduler.scheduleNext(context, hour, minute)
    }
}
