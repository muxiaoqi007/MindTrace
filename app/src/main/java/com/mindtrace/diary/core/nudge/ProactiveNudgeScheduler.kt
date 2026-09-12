package com.mindtrace.diary.core.nudge

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 主动关怀调度器
 * 负责调度和取消主动关怀的每日定时检查
 */
object ProactiveNudgeScheduler {
    const val WORK_NAME = "proactive_nudge_work"

    /**
     * 调度下一次主动关怀任务
     * @param context 上下文
     * @param hour 小时（0-23）
     * @param minute 分钟（0-59）
     */
    fun scheduleNext(context: Context, hour: Int, minute: Int) {
        val delay = calculateDelayUntil(hour, minute)

        val workRequest = OneTimeWorkRequestBuilder<ProactiveNudgeWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * 取消主动关怀任务
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * 计算到指定时间的延迟毫秒数
     * 如果今天的指定时间已过，则计算到明天该时间的延迟
     */
    private fun calculateDelayUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 如果目标时间已过，设置为明天
        if (target.before(now) || target == now) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }

    /**
     * 立即执行一次主动关怀任务（用于测试）
     */
    fun executeNow(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<ProactiveNudgeWorker>()
            .addTag("${WORK_NAME}_immediate")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
