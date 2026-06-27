package com.mindtrace.diary.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mindtrace.diary.core.security.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WEBDAV_URL = stringPreferencesKey("webdav_url")
        val WEBDAV_USERNAME = stringPreferencesKey("webdav_username")
        val WEBDAV_PASSWORD = stringPreferencesKey("webdav_password")
        val WEBDAV_PATH = stringPreferencesKey("webdav_path")
        val AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        // AI 配置
        val AI_ENABLED = booleanPreferencesKey("ai_enabled")
        val AI_BASE_URL = stringPreferencesKey("ai_base_url")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val AI_SYSTEM_PROMPT = stringPreferencesKey("ai_system_prompt")
        // 心情图标包配置
        val MOOD_ICON_PACK = stringPreferencesKey("mood_icon_pack")
        // 深夜回信配置
        val MIDNIGHT_REVIEW_ENABLED = booleanPreferencesKey("midnight_review_enabled")
        val MIDNIGHT_REVIEW_HOUR = intPreferencesKey("midnight_review_hour")
        val MIDNIGHT_REVIEW_MINUTE = intPreferencesKey("midnight_review_minute")
        val MIDNIGHT_REVIEW_PERSONA = stringPreferencesKey("midnight_review_persona")
        // 沉默唤醒配置
        val SILENCE_BREAK_ENABLED = booleanPreferencesKey("silence_break_enabled")
        val SILENCE_BREAK_HOURS = intPreferencesKey("silence_break_hours")
        val LAST_ACTIVE_TIME = longPreferencesKey("last_active_time")
        // 沉默唤醒邮件配置
        val SILENCE_EMAIL_ENABLED = booleanPreferencesKey("silence_email_enabled")
        val SILENCE_EMAIL_THRESHOLD_DAYS = intPreferencesKey("silence_email_threshold_days")
        // 邮件服务器配置
        val EMAIL_SMTP_HOST = stringPreferencesKey("email_smtp_host")
        val EMAIL_SMTP_PORT = intPreferencesKey("email_smtp_port")
        val EMAIL_USERNAME = stringPreferencesKey("email_username")
        val EMAIL_PASSWORD = stringPreferencesKey("email_password")
        val EMAIL_RECIPIENT = stringPreferencesKey("email_recipient")
        val EMAIL_USE_SSL = booleanPreferencesKey("email_use_ssl")
        // 日记分析配置
        val DIARY_ANALYSIS_ENABLED = booleanPreferencesKey("diary_analysis_enabled")
        // 自动学习记忆配置
        val MEMORY_LEARNING_ENABLED = booleanPreferencesKey("memory_learning_enabled")
        // 主对话 AI 人格
        val CHAT_PERSONA_ID = stringPreferencesKey("chat_persona_id")
        // Soul / 用户相处配置
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val AI_DISPLAY_NAME = stringPreferencesKey("ai_display_name")
        val SOUL_RELATIONSHIP_NOTE = stringPreferencesKey("soul_relationship_note")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            ThemeMode.fromString(preferences[Keys.THEME_MODE])
        }

    val webDavConfig: Flow<WebDavConfig> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            WebDavConfig(
                url = preferences[Keys.WEBDAV_URL] ?: "",
                username = preferences[Keys.WEBDAV_USERNAME] ?: "",
                password = cryptoManager.decrypt(preferences[Keys.WEBDAV_PASSWORD] ?: ""),
                path = preferences[Keys.WEBDAV_PATH] ?: "/MindTrace"
            )
        }

    val autoSyncEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[Keys.AUTO_SYNC_ENABLED] ?: false
        }

    val lastSyncTime: Flow<Long?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[Keys.LAST_SYNC_TIME]
        }

    val aiConfig: Flow<AIConfig> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            AIConfig(
                enabled = preferences[Keys.AI_ENABLED] ?: false,
                baseUrl = preferences[Keys.AI_BASE_URL] ?: "",
                apiKey = cryptoManager.decrypt(preferences[Keys.AI_API_KEY] ?: ""),
                model = preferences[Keys.AI_MODEL] ?: "gpt-3.5-turbo",
                systemPrompt = preferences[Keys.AI_SYSTEM_PROMPT] ?: AIConfig.DEFAULT_SYSTEM_PROMPT
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setWebDavConfig(config: WebDavConfig) {
        context.dataStore.edit { preferences ->
            preferences[Keys.WEBDAV_URL] = config.url
            preferences[Keys.WEBDAV_USERNAME] = config.username
            preferences[Keys.WEBDAV_PASSWORD] = cryptoManager.encrypt(config.password)
            preferences[Keys.WEBDAV_PATH] = config.path
        }
    }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AUTO_SYNC_ENABLED] = enabled
        }
    }

    suspend fun setLastSyncTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_TIME] = time
        }
    }

    suspend fun clearWebDavConfig() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.WEBDAV_URL)
            preferences.remove(Keys.WEBDAV_USERNAME)
            preferences.remove(Keys.WEBDAV_PASSWORD)
            preferences.remove(Keys.WEBDAV_PATH)
        }
    }

    suspend fun setAIConfig(config: AIConfig) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AI_ENABLED] = config.enabled
            preferences[Keys.AI_BASE_URL] = config.baseUrl
            preferences[Keys.AI_API_KEY] = cryptoManager.encrypt(config.apiKey)
            preferences[Keys.AI_MODEL] = config.model
            preferences[Keys.AI_SYSTEM_PROMPT] = config.systemPrompt
        }
    }

    suspend fun setAIEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AI_ENABLED] = enabled
        }
    }

    val moodIconPackId: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[Keys.MOOD_ICON_PACK] ?: "classic"
        }

    suspend fun setMoodIconPack(packId: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MOOD_ICON_PACK] = packId
        }
    }

    // 深夜回信配置
    val midnightReviewConfig: Flow<MidnightReviewConfig> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            MidnightReviewConfig(
                enabled = preferences[Keys.MIDNIGHT_REVIEW_ENABLED] ?: false,
                hour = preferences[Keys.MIDNIGHT_REVIEW_HOUR] ?: 22,
                minute = preferences[Keys.MIDNIGHT_REVIEW_MINUTE] ?: 0,
                persona = preferences[Keys.MIDNIGHT_REVIEW_PERSONA] ?: "parallel_self"
            )
        }

    suspend fun setMidnightReviewConfig(config: MidnightReviewConfig) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MIDNIGHT_REVIEW_ENABLED] = config.enabled
            preferences[Keys.MIDNIGHT_REVIEW_HOUR] = config.hour
            preferences[Keys.MIDNIGHT_REVIEW_MINUTE] = config.minute
            preferences[Keys.MIDNIGHT_REVIEW_PERSONA] = config.persona
        }
    }

    suspend fun setMidnightReviewEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MIDNIGHT_REVIEW_ENABLED] = enabled
        }
    }

    // 沉默唤醒配置
    val silenceBreakConfig: Flow<SilenceBreakConfig> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            SilenceBreakConfig(
                enabled = preferences[Keys.SILENCE_BREAK_ENABLED] ?: false,
                hours = preferences[Keys.SILENCE_BREAK_HOURS] ?: 72,
                emailEnabled = preferences[Keys.SILENCE_EMAIL_ENABLED] ?: false,
                emailThresholdDays = preferences[Keys.SILENCE_EMAIL_THRESHOLD_DAYS] ?: 7
            )
        }

    // 邮件服务器配置
    val emailConfig: Flow<EmailConfig> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            EmailConfig(
                smtpHost = preferences[Keys.EMAIL_SMTP_HOST] ?: "",
                smtpPort = preferences[Keys.EMAIL_SMTP_PORT] ?: 465,
                username = preferences[Keys.EMAIL_USERNAME] ?: "",
                password = cryptoManager.decrypt(preferences[Keys.EMAIL_PASSWORD] ?: ""),
                recipient = preferences[Keys.EMAIL_RECIPIENT] ?: "",
                useSSL = preferences[Keys.EMAIL_USE_SSL] ?: true
            )
        }

    val lastActiveTime: Flow<Long?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[Keys.LAST_ACTIVE_TIME]
        }

    suspend fun setSilenceBreakConfig(config: SilenceBreakConfig) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SILENCE_BREAK_ENABLED] = config.enabled
            preferences[Keys.SILENCE_BREAK_HOURS] = config.hours
            preferences[Keys.SILENCE_EMAIL_ENABLED] = config.emailEnabled
            preferences[Keys.SILENCE_EMAIL_THRESHOLD_DAYS] = config.emailThresholdDays
        }
    }

    suspend fun setSilenceBreakEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SILENCE_BREAK_ENABLED] = enabled
        }
    }

    suspend fun setSilenceEmailEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SILENCE_EMAIL_ENABLED] = enabled
        }
    }

    suspend fun setSilenceEmailThresholdDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SILENCE_EMAIL_THRESHOLD_DAYS] = days
        }
    }

    suspend fun setEmailConfig(config: EmailConfig) {
        context.dataStore.edit { preferences ->
            preferences[Keys.EMAIL_SMTP_HOST] = config.smtpHost
            preferences[Keys.EMAIL_SMTP_PORT] = config.smtpPort
            preferences[Keys.EMAIL_USERNAME] = config.username
            preferences[Keys.EMAIL_PASSWORD] = cryptoManager.encrypt(config.password)
            preferences[Keys.EMAIL_RECIPIENT] = config.recipient
            preferences[Keys.EMAIL_USE_SSL] = config.useSSL
        }
    }

    suspend fun updateLastActiveTime() {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_ACTIVE_TIME] = System.currentTimeMillis()
        }
    }

    // 日记分析配置
    val diaryAnalysisEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[Keys.DIARY_ANALYSIS_ENABLED] ?: false
        }

    suspend fun setDiaryAnalysisEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DIARY_ANALYSIS_ENABLED] = enabled
        }
    }

    // 自动学习记忆：开启后保存日记会让 AI 提取关于你的长期记忆
    val memoryLearningEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[Keys.MEMORY_LEARNING_ENABLED] ?: false
        }

    suspend fun setMemoryLearningEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MEMORY_LEARNING_ENABLED] = enabled
        }
    }

    // 主对话 AI 人格（id 对应 ChatPersona.id），默认温暖伙伴
    val chatPersonaId: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[Keys.CHAT_PERSONA_ID] ?: "warm_companion"
        }

    suspend fun setChatPersonaId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CHAT_PERSONA_ID] = id
        }
    }

    val soulConfig: Flow<SoulConfig> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            SoulConfig(
                chatPersonaId = preferences[Keys.CHAT_PERSONA_ID] ?: "warm_companion",
                userDisplayName = preferences[Keys.USER_DISPLAY_NAME] ?: "",
                aiDisplayName = preferences[Keys.AI_DISPLAY_NAME] ?: "MindTrace",
                relationshipNote = preferences[Keys.SOUL_RELATIONSHIP_NOTE] ?: "",
                customSystemPrompt = preferences[Keys.AI_SYSTEM_PROMPT] ?: AIConfig.DEFAULT_SYSTEM_PROMPT
            )
        }

    suspend fun setSoulConfig(config: SoulConfig) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CHAT_PERSONA_ID] = config.chatPersonaId
            preferences[Keys.USER_DISPLAY_NAME] = config.userDisplayName
            preferences[Keys.AI_DISPLAY_NAME] = config.aiDisplayName
            preferences[Keys.SOUL_RELATIONSHIP_NOTE] = config.relationshipNote
            preferences[Keys.AI_SYSTEM_PROMPT] = config.customSystemPrompt
        }
    }

    suspend fun setUserDisplayName(name: String) {
        context.dataStore.edit { preferences -> preferences[Keys.USER_DISPLAY_NAME] = name }
    }

    suspend fun setAiDisplayName(name: String) {
        context.dataStore.edit { preferences -> preferences[Keys.AI_DISPLAY_NAME] = name }
    }

    suspend fun setSoulRelationshipNote(note: String) {
        context.dataStore.edit { preferences -> preferences[Keys.SOUL_RELATIONSHIP_NOTE] = note }
    }
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromString(value: String?): ThemeMode {
            return entries.find { it.name == value } ?: SYSTEM
        }
    }
}

data class WebDavConfig(
    val url: String,
    val username: String,
    val password: String,
    val path: String
) {
    val isConfigured: Boolean
        get() = url.isNotBlank() && username.isNotBlank() && password.isNotBlank()
}

/**
 * AI 配置
 * 支持 OpenAI 兼容格式的任意供应商
 */
data class AIConfig(
    val enabled: Boolean = false,
    val baseUrl: String = "",      // 如 https://api.openai.com/v1 或其他兼容供应商
    val apiKey: String = "",
    val model: String = "gpt-3.5-turbo",
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank()

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """你是 MindTrace 日记应用的 AI 伙伴，一个温暖、善解人意的朋友。

你的角色：
- 倾听用户的心声，给予情感支持
- 帮助用户反思和整理思绪
- 记住用户分享过的重要信息
- 适时提出有洞察力的问题

对话风格：
- 温暖友好，像朋友一样交流
- 简洁自然，避免过于正式
- 善于共情，理解用户的感受
- 适度使用 emoji 增加亲切感

注意事项：
- 尊重用户隐私，不主动追问敏感信息
- 鼓励积极的自我探索
- 在用户需要时提供支持，但不过度干预"""
    }
}

data class SoulConfig(
    val chatPersonaId: String = "warm_companion",
    val userDisplayName: String = "",
    val aiDisplayName: String = "MindTrace",
    val relationshipNote: String = "",
    val customSystemPrompt: String = AIConfig.DEFAULT_SYSTEM_PROMPT
)

/**
 * 深夜回信配置
 */
data class MidnightReviewConfig(
    val enabled: Boolean = false,
    val hour: Int = 22,
    val minute: Int = 0,
    val persona: String = "parallel_self"
)

/**
 * 沉默唤醒配置
 */
data class SilenceBreakConfig(
    val enabled: Boolean = false,
    val hours: Int = 72,  // 默认 72 小时（3 天）未活跃时提醒
    val emailEnabled: Boolean = false,  // 是否启用邮件通知
    val emailThresholdDays: Int = 7  // 多少天未活跃时发送邮件
)

/**
 * 邮件服务器配置
 */
data class EmailConfig(
    val smtpHost: String = "",
    val smtpPort: Int = 465,
    val username: String = "",
    val password: String = "",
    val recipient: String = "",
    val useSSL: Boolean = true
) {
    val isConfigured: Boolean
        get() = smtpHost.isNotBlank() && username.isNotBlank() && password.isNotBlank() && recipient.isNotBlank()
}
