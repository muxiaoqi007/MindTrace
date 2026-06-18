package com.mindtrace.diary.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.datastore.EmailConfig
import com.mindtrace.diary.core.datastore.MidnightReviewConfig
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.datastore.SilenceBreakConfig
import com.mindtrace.diary.core.datastore.ThemeMode
import com.mindtrace.diary.core.datastore.WebDavConfig
import com.mindtrace.diary.core.email.EmailSender
import com.mindtrace.diary.core.notification.NotificationHelper
import com.mindtrace.diary.core.review.MidnightReviewScheduler
import com.mindtrace.diary.core.review.SilenceBreakScheduler
import com.mindtrace.diary.domain.usecase.backup.ExportDataWithImagesUseCase
import com.mindtrace.diary.domain.usecase.backup.ExportWithImagesResult
import com.mindtrace.diary.domain.usecase.backup.ImportDataUseCase
import com.mindtrace.diary.domain.usecase.backup.ImportDataWithImagesUseCase
import com.mindtrace.diary.domain.usecase.backup.ImportResult
import com.mindtrace.diary.domain.usecase.backup.ImportWithImagesResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val webDavConfig: WebDavConfig = WebDavConfig("", "", "", "/MindTrace"),
    val moodIconPackId: String = "classic",
    val midnightReviewConfig: MidnightReviewConfig = MidnightReviewConfig(),
    val silenceBreakConfig: SilenceBreakConfig = SilenceBreakConfig(),
    val emailConfig: EmailConfig = EmailConfig(),
    val diaryAnalysisEnabled: Boolean = false,
    val aiConfigured: Boolean = false,
    val hasNotificationPermission: Boolean = true,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isTestingEmail: Boolean = false,
    val exportMessage: String? = null,
    val importMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore,
    private val notificationHelper: NotificationHelper,
    private val exportDataWithImagesUseCase: ExportDataWithImagesUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val importDataWithImagesUseCase: ImportDataWithImagesUseCase,
    private val emailSender: EmailSender
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkNotificationPermission()
    }

    fun checkNotificationPermission() {
        _uiState.update {
            it.copy(hasNotificationPermission = notificationHelper.hasNotificationPermission())
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsDataStore.themeMode,
                settingsDataStore.webDavConfig,
                settingsDataStore.moodIconPackId,
                settingsDataStore.midnightReviewConfig,
                settingsDataStore.aiConfig,
                settingsDataStore.silenceBreakConfig,
                settingsDataStore.diaryAnalysisEnabled,
                settingsDataStore.emailConfig
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val theme = values[0] as ThemeMode
                val webDav = values[1] as WebDavConfig
                val moodIconPack = values[2] as String
                val midnightReview = values[3] as MidnightReviewConfig
                val aiConfig = values[4] as com.mindtrace.diary.core.datastore.AIConfig
                val silenceBreak = values[5] as SilenceBreakConfig
                val diaryAnalysis = values[6] as Boolean
                val email = values[7] as EmailConfig

                SettingsUiState(
                    themeMode = theme,
                    webDavConfig = webDav,
                    moodIconPackId = moodIconPack,
                    midnightReviewConfig = midnightReview,
                    silenceBreakConfig = silenceBreak,
                    emailConfig = email,
                    diaryAnalysisEnabled = diaryAnalysis,
                    aiConfigured = aiConfig.isConfigured && aiConfig.enabled
                )
            }.collect { state ->
                _uiState.update {
                    it.copy(
                        themeMode = state.themeMode,
                        webDavConfig = state.webDavConfig,
                        moodIconPackId = state.moodIconPackId,
                        midnightReviewConfig = state.midnightReviewConfig,
                        silenceBreakConfig = state.silenceBreakConfig,
                        emailConfig = state.emailConfig,
                        diaryAnalysisEnabled = state.diaryAnalysisEnabled,
                        aiConfigured = state.aiConfigured
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportMessage = null, error = null) }

            exportDataWithImagesUseCase(uri)
                .onSuccess { result ->
                    val message = "导出成功：${result.totalCount} 条数据，${result.imageCount} 张图片"
                    _uiState.update { it.copy(isExporting = false, exportMessage = message) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isExporting = false, error = "导出失败: ${e.message}") }
                }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importMessage = null, error = null) }

            // 检测文件格式
            val isZip = detectZipFormat(uri)

            if (isZip) {
                importDataWithImagesUseCase(uri)
                    .onSuccess { result ->
                        val message = "导入成功：${result.totalCount} 条数据，${result.imageCount} 张图片"
                        _uiState.update { it.copy(isImporting = false, importMessage = message) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isImporting = false, error = "导入失败: ${e.message}") }
                    }
            } else {
                importDataUseCase(uri)
                    .onSuccess { result ->
                        val message = "导入成功：${result.totalCount} 条数据"
                        _uiState.update { it.copy(isImporting = false, importMessage = message) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isImporting = false, error = "导入失败: ${e.message}") }
                    }
            }
        }
    }

    private suspend fun detectZipFormat(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val header = ByteArray(4)
                val bytesRead = stream.read(header)
                bytesRead >= 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }

    fun clearImportMessage() {
        _uiState.update { it.copy(importMessage = null) }
    }

    fun setMoodIconPack(packId: String) {
        viewModelScope.launch {
            settingsDataStore.setMoodIconPack(packId)
        }
    }

    // 深夜回信相关方法
    fun setMidnightReviewEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setMidnightReviewEnabled(enabled)
            if (enabled) {
                val config = _uiState.value.midnightReviewConfig
                MidnightReviewScheduler.scheduleNext(context, config.hour, config.minute)
            } else {
                MidnightReviewScheduler.cancel(context)
            }
        }
    }

    fun setMidnightReviewTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val currentConfig = _uiState.value.midnightReviewConfig
            val newConfig = currentConfig.copy(hour = hour, minute = minute)
            settingsDataStore.setMidnightReviewConfig(newConfig)
            if (newConfig.enabled) {
                MidnightReviewScheduler.scheduleNext(context, hour, minute)
            }
        }
    }

    fun setMidnightReviewPersona(personaId: String) {
        viewModelScope.launch {
            val currentConfig = _uiState.value.midnightReviewConfig
            settingsDataStore.setMidnightReviewConfig(currentConfig.copy(persona = personaId))
        }
    }

    fun testMidnightReview() {
        MidnightReviewScheduler.executeNow(context)
    }

    // 沉默唤醒相关方法
    fun setSilenceBreakEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSilenceBreakEnabled(enabled)
            if (enabled) {
                // 更新最后活跃时间并开始调度
                settingsDataStore.updateLastActiveTime()
                SilenceBreakScheduler.scheduleDaily(context)
            } else {
                SilenceBreakScheduler.cancel(context)
            }
        }
    }

    fun setSilenceBreakHours(hours: Int) {
        viewModelScope.launch {
            val currentConfig = _uiState.value.silenceBreakConfig
            settingsDataStore.setSilenceBreakConfig(currentConfig.copy(hours = hours))
        }
    }

    fun setSilenceEmailEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSilenceEmailEnabled(enabled)
        }
    }

    fun setSilenceEmailThresholdDays(days: Int) {
        viewModelScope.launch {
            settingsDataStore.setSilenceEmailThresholdDays(days)
        }
    }

    fun setEmailConfig(config: EmailConfig) {
        viewModelScope.launch {
            settingsDataStore.setEmailConfig(config)
        }
    }

    fun testEmailConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingEmail = true, error = null) }

            val config = _uiState.value.emailConfig
            emailSender.testConnection(config)
                .onSuccess {
                    _uiState.update { it.copy(isTestingEmail = false, exportMessage = "邮件连接测试成功") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isTestingEmail = false, error = "邮件连接失败: ${e.message}") }
                }
        }
    }

    fun testSilenceBreak() {
        SilenceBreakScheduler.executeNow(context)
    }

    // 日记分析相关方法
    fun setDiaryAnalysisEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDiaryAnalysisEnabled(enabled)
        }
    }
}
