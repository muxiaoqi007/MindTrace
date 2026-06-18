package com.mindtrace.diary.core.review

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * 沉默唤醒调度器
 * 负责调度每日检查用户活跃状态的任务
 */
object SilenceBreakScheduler {
    const val WORK_NAME = "silence_break_work"
    const val KEY_IS_TEST = "is_test"

    /**
     * 开始每日检查任务
     * 每天检查一次用户是否长时间未活跃
     */
    fun scheduleDaily(context: Context) {
        val constraints = Constraints.Builder()
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SilenceBreakWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) // 首次延迟 1 小时执行
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * 取消沉默唤醒任务
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * 立即执行一次检查（用于测试，跳过时间检查）
     */
    fun executeNow(context: Context) {
        val inputData = Data.Builder()
            .putBoolean(KEY_IS_TEST, true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SilenceBreakWorker>()
            .setInputData(inputData)
            .addTag("${WORK_NAME}_immediate")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
