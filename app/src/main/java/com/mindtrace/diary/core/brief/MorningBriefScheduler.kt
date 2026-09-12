package com.mindtrace.diary.core.brief

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 晨间简报调度器
 * 负责调度和取消晨间简报的定时任务
 */
object MorningBriefScheduler {
    const val WORK_NAME = "morning_brief_work"

    /**
     * 调度下一次晨间简报任务
     * @param context 上下文
     * @param hour 小时（0-23）
     * @param minute 分钟（0-59）
     */
    fun scheduleNext(context: Context, hour: Int, minute: Int) {
        val delay = calculateDelayUntil(hour, minute)

        val workRequest = OneTimeWorkRequestBuilder<MorningBriefWorker>()
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
     * 取消晨间简报任务
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
     * 立即执行一次晨间简报任务（用于测试）
     */
    fun executeNow(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<MorningBriefWorker>()
            .addTag("${WORK_NAME}_immediate")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
