package com.mindtrace.diary.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.datastore.ThemeMode
import com.mindtrace.diary.core.notification.NotificationHelper
import com.mindtrace.diary.core.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 通知等外部入口带来的跳转意图，由 NavGraph 消费后清空 */
sealed class DeepLink {
    data object AIChat : DeepLink()
    data class AiReview(val reviewId: String) : DeepLink()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsDataStore.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val moodIconPackId: StateFlow<String> = settingsDataStore.moodIconPackId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "minimalist"
        )

    private val _deepLink = MutableStateFlow<DeepLink?>(null)
    val deepLink: StateFlow<DeepLink?> = _deepLink.asStateFlow()

    init {
        setupAutoSync()
    }

    /** 把启动/通知的 Intent 解析成待消费的深链 */
    fun handleIntent(action: String?, reviewId: String?) {
        if (_deepLink.value != null) return
        _deepLink.value = when (action) {
            NotificationHelper.ACTION_OPEN_AI_CHAT -> DeepLink.AIChat
            NotificationHelper.ACTION_VIEW_REVIEW ->
                reviewId?.takeIf { it.isNotBlank() }?.let { DeepLink.AiReview(it) }
            else -> null
        }
    }

    fun consumeDeepLink() {
        _deepLink.value = null
    }

    private fun setupAutoSync() {
        viewModelScope.launch {
            settingsDataStore.autoSyncEnabled.collect { enabled ->
                // Auto sync will be handled by SyncWorker if enabled
                // This is initialized in the Application class
            }
        }
    }
}
