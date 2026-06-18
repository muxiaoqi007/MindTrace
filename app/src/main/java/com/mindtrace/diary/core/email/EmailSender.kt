package com.mindtrace.diary.core.email

import android.util.Log
import com.mindtrace.diary.core.datastore.EmailConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * 邮件发送工具类
 */
@Singleton
class EmailSender @Inject constructor() {

    companion object {
        private const val TAG = "EmailSender"
    }

    /**
     * 发送邮件
     * @param config 邮件配置
     * @param subject 邮件主题
     * @param body 邮件正文
     * @return 是否发送成功
     */
    suspend fun sendEmail(
        config: EmailConfig,
        subject: String,
        body: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!config.isConfigured) {
                return@withContext Result.failure(IllegalStateException("邮件配置不完整"))
            }

            val props = Properties().apply {
                put("mail.smtp.host", config.smtpHost)
                put("mail.smtp.port", config.smtpPort.toString())
                put("mail.smtp.auth", "true")

                if (config.useSSL) {
                    put("mail.smtp.ssl.enable", "true")
                    put("mail.smtp.socketFactory.port", config.smtpPort.toString())
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    put("mail.smtp.socketFactory.fallback", "false")
                } else {
                    put("mail.smtp.starttls.enable", "true")
                }

                put("mail.smtp.connectiontimeout", "10000")
                put("mail.smtp.timeout", "10000")
                put("mail.smtp.writetimeout", "10000")
            }

            val authenticator = object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(config.username, config.password)
                }
            }

            val session = Session.getInstance(props, authenticator)

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.recipient))
                setSubject(subject, "UTF-8")
                setText(body, "UTF-8")
            }

            Transport.send(message)
            Log.d(TAG, "邮件发送成功: $subject")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "邮件发送失败", e)
            Result.failure(e)
        }
    }

    /**
     * 测试邮件连接
     */
    suspend fun testConnection(config: EmailConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!config.isConfigured) {
                return@withContext Result.failure(IllegalStateException("邮件配置不完整"))
            }

            val props = Properties().apply {
                put("mail.smtp.host", config.smtpHost)
                put("mail.smtp.port", config.smtpPort.toString())
                put("mail.smtp.auth", "true")

                if (config.useSSL) {
                    put("mail.smtp.ssl.enable", "true")
                    put("mail.smtp.socketFactory.port", config.smtpPort.toString())
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                }

                put("mail.smtp.connectiontimeout", "5000")
                put("mail.smtp.timeout", "5000")
            }

            val authenticator = object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(config.username, config.password)
                }
            }

            val session = Session.getInstance(props, authenticator)
            val transport = session.getTransport("smtp")
            transport.connect(config.smtpHost, config.smtpPort, config.username, config.password)
            transport.close()

            Log.d(TAG, "邮件连接测试成功")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "邮件连接测试失败", e)
            Result.failure(e)
        }
    }
}
