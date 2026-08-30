package com.mindtrace.diary.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mindtrace.diary.R
import com.mindtrace.diary.app.MainActivity
import com.mindtrace.diary.domain.model.AiReview
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID_MIDNIGHT_REVIEW = "midnight_review"
        const val CHANNEL_NAME_MIDNIGHT_REVIEW = "深夜回信"
        const val CHANNEL_DESC_MIDNIGHT_REVIEW = "每晚的 AI 回信通知"

        const val CHANNEL_ID_SILENCE_BREAK = "silence_break"
        const val CHANNEL_NAME_SILENCE_BREAK = "沉默唤醒"
        const val CHANNEL_DESC_SILENCE_BREAK = "长时间未活跃时的提醒"

        const val CHANNEL_ID_TIME_CAPSULE = "time_capsule"
        const val CHANNEL_NAME_TIME_CAPSULE = "时光胶囊"
        const val CHANNEL_DESC_TIME_CAPSULE = "时光胶囊到期可开启时的提醒"

        const val CHANNEL_ID_ONE_SECOND = "one_second_life"
        const val CHANNEL_NAME_ONE_SECOND = "一秒人生"
        const val CHANNEL_DESC_ONE_SECOND = "每日选择一个生活片段的提醒"

        const val NOTIFICATION_ID_MIDNIGHT_REVIEW = 1001
        const val NOTIFICATION_ID_SILENCE_BREAK = 1002
        const val NOTIFICATION_ID_TIME_CAPSULE_BASE = 2000
        const val NOTIFICATION_ID_ONE_SECOND = 3001

        const val EXTRA_REVIEW_ID = "review_id"
    }

    /**
     * 创建所有通知渠道
     * 应在 Application.onCreate() 中调用
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // 深夜回信渠道
            val midnightReviewChannel = NotificationChannel(
                CHANNEL_ID_MIDNIGHT_REVIEW,
                CHANNEL_NAME_MIDNIGHT_REVIEW,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC_MIDNIGHT_REVIEW
                enableLights(true)
                enableVibration(true)
            }

            // 沉默唤醒渠道
            val silenceBreakChannel = NotificationChannel(
                CHANNEL_ID_SILENCE_BREAK,
                CHANNEL_NAME_SILENCE_BREAK,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESC_SILENCE_BREAK
            }

            val timeCapsuleChannel = NotificationChannel(
                CHANNEL_ID_TIME_CAPSULE,
                CHANNEL_NAME_TIME_CAPSULE,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = CHANNEL_DESC_TIME_CAPSULE }

            val oneSecondChannel = NotificationChannel(
                CHANNEL_ID_ONE_SECOND,
                CHANNEL_NAME_ONE_SECOND,
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = CHANNEL_DESC_ONE_SECOND }

            notificationManager.createNotificationChannels(
                listOf(midnightReviewChannel, silenceBreakChannel, timeCapsuleChannel, oneSecondChannel)
            )
        }
    }

    /**
     * 显示深夜回信通知
     */
    @SuppressLint("MissingPermission")
    fun showMidnightReviewNotification(review: AiReview) {
        if (!hasNotificationPermission()) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REVIEW_ID, review.id)
            action = "ACTION_VIEW_REVIEW"
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_MIDNIGHT_REVIEW,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MIDNIGHT_REVIEW)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("来自「${review.persona.displayName}」的回信")
            .setContentText(review.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(review.content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_MIDNIGHT_REVIEW,
                notification
            )
        } catch (_: SecurityException) {
            // Permission can be revoked between the check and notify().
        }
    }

    /**
     * 显示沉默唤醒通知
     */
    @SuppressLint("MissingPermission")
    fun showSilenceBreakNotification(message: String) {
        if (!hasNotificationPermission()) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_SILENCE_BREAK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SILENCE_BREAK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("好久不见")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_SILENCE_BREAK,
                notification
            )
        } catch (_: SecurityException) {
            // Permission can be revoked between the check and notify().
        }
    }

    @SuppressLint("MissingPermission")
    fun showTimeCapsuleNotification(capsuleId: String, title: String) {
        if (!hasNotificationPermission()) return
        val notificationId = NOTIFICATION_ID_TIME_CAPSULE_BASE + (capsuleId.hashCode() and 0x3fff)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = "ACTION_OPEN_TIME_CAPSULE"
            putExtra("time_capsule_id", capsuleId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TIME_CAPSULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("一枚时光胶囊可以开启了")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission can be revoked between the check and notify().
        }
    }

    @SuppressLint("MissingPermission")
    fun showOneSecondReminder() {
        if (!hasNotificationPermission()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = "ACTION_OPEN_ONE_SECOND_LIFE"
        }
        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID_ONE_SECOND, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ONE_SECOND)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("今天的一秒，你想留下什么？")
            .setContentText("可以是一张照片，也可以是一段视频。允许今天空着。")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ONE_SECOND, notification)
        } catch (_: SecurityException) { }
    }

    /**
     * 检查是否有通知权限
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * 取消指定通知
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
