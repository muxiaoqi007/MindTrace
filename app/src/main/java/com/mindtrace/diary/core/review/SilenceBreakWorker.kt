package com.mindtrace.diary.core.review

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.email.EmailSender
import com.mindtrace.diary.core.notification.NotificationHelper
import com.mindtrace.diary.domain.model.AiReview
import com.mindtrace.diary.domain.model.MidnightReviewPersona
import com.mindtrace.diary.domain.model.ReviewType
import com.mindtrace.diary.domain.repository.AiReviewRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * 沉默唤醒 Worker
 * 每日检查用户是否长时间未活跃，如果超过配置的时间则发送提醒通知
 */
@HiltWorker
class SilenceBreakWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsDataStore: SettingsDataStore,
    private val notificationHelper: NotificationHelper,
    private val aiReviewRepository: AiReviewRepository,
    private val emailSender: EmailSender
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SilenceBreakWorker"

        // 提醒消息列表，随机选择一条
        private val REMINDER_MESSAGES = listOf(
            "好久不见，最近过得怎么样？",
            "有段时间没见到你了，想念你的文字。",
            "生活中发生了什么有趣的事吗？来记录一下吧。",
            "无论是开心还是烦恼，都可以写下来。",
            "今天的心情如何？来聊聊吧。",
            "记录生活，是给未来的自己最好的礼物。",
            "哪怕只是一句话，也是珍贵的记忆。"
        )
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting silence break check")

        return try {
            // 检查是否是测试模式
            val isTest = inputData.getBoolean(SilenceBreakScheduler.KEY_IS_TEST, false)

            // 1. 检查功能是否启用（测试模式下跳过此检查）
            val config = settingsDataStore.silenceBreakConfig.first()
            if (!isTest && !config.enabled) {
                Log.d(TAG, "Silence break is disabled")
                return Result.success()
            }

            // 2. 获取最后活跃时间（测试模式下跳过时间检查）
            var inactiveDays = 0L
            val shouldSend = if (isTest) {
                Log.d(TAG, "Test mode: skipping time check")
                inactiveDays = config.emailThresholdDays.toLong() // 模拟达到邮件阈值
                true
            } else {
                val lastActiveTime = settingsDataStore.lastActiveTime.first()
                if (lastActiveTime == null) {
                    Log.d(TAG, "No last active time recorded, updating now")
                    settingsDataStore.updateLastActiveTime()
                    return Result.success()
                }

                // 3. 计算不活跃时长
                val currentTime = System.currentTimeMillis()
                val inactiveHours = TimeUnit.MILLISECONDS.toHours(currentTime - lastActiveTime)
                inactiveDays = TimeUnit.MILLISECONDS.toDays(currentTime - lastActiveTime)
                Log.d(TAG, "User inactive for $inactiveHours hours ($inactiveDays days), threshold is ${config.hours} hours")

                inactiveHours >= config.hours
            }

            // 4. 如果符合条件，发送提醒
            if (shouldSend) {
                val message = REMINDER_MESSAGES.random()

                // 发送通知
                notificationHelper.showSilenceBreakNotification(message)

                // 保存为 AiReview
                val review = AiReview(
                    id = UUID.randomUUID().toString(),
                    date = LocalDate.now(),
                    content = message,
                    diaryIds = emptyList(),
                    persona = MidnightReviewPersona.WISE_FRIEND,
                    isRead = false,
                    createdAt = System.currentTimeMillis(),
                    type = ReviewType.SILENCE_BREAK
                )
                aiReviewRepository.saveReview(review)

                Log.d(TAG, "Sent silence break notification and saved as AiReview")

                // 5. 检查是否需要发送邮件
                if (config.emailEnabled && inactiveDays >= config.emailThresholdDays) {
                    sendEmailNotification(inactiveDays.toInt())
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in silence break check", e)
            Result.failure()
        }
    }

    /**
     * 发送邮件通知
     */
    private suspend fun sendEmailNotification(inactiveDays: Int) {
        try {
            val emailConfig = settingsDataStore.emailConfig.first()
            if (!emailConfig.isConfigured) {
                Log.w(TAG, "Email not configured, skipping email notification")
                return
            }

            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
            val subject = "MindTrace 沉默唤醒提醒"
            val body = """
                |亲爱的用户：
                |
                |您已经 $inactiveDays 天没有使用 MindTrace 记录生活了。
                |
                |生活中的点点滴滴，都值得被记录。无论是开心的、烦恼的、平凡的瞬间，
                |都是属于您独一无二的记忆。
                |
                |打开 MindTrace，写下今天的心情吧。
                |
                |---
                |此邮件由 MindTrace 自动发送
                |$today
            """.trimMargin()

            emailSender.sendEmail(emailConfig, subject, body)
                .onSuccess {
                    Log.d(TAG, "Email notification sent successfully")
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to send email notification", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending email notification", e)
        }
    }
}
