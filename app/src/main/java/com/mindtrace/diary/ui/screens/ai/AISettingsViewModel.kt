package com.mindtrace.diary.ui.screens.ai

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.nudge.ProactiveNudgeScheduler
import com.mindtrace.diary.domain.usecase.ai.TestAIConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AISettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore,
    private val testAIConnectionUseCase: TestAIConnectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.aiConfig.collect { config ->
                _uiState.update { it.copy(config = config) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.memoryLearningEnabled.collect { enabled ->
                _uiState.update { it.copy(memoryLearningEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.proactiveNudgeConfig.collect { config ->
                _uiState.update { it.copy(proactiveNudgeConfig = config) }
            }
        }
    }

    fun updateEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(config = it.config.copy(enabled = enabled), testResult = null)
        }
    }

    fun updateMemoryLearning(enabled: Boolean) {
        _uiState.update { it.copy(memoryLearningEnabled = enabled) }
        viewModelScope.launch {
            settingsDataStore.setMemoryLearningEnabled(enabled)
        }
    }

    // 主动关怀相关方法
    fun updateProactiveNudgeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setProactiveNudgeEnabled(enabled)
            if (enabled) {
                val config = _uiState.value.proactiveNudgeConfig
                ProactiveNudgeScheduler.scheduleNext(context, config.hour, config.minute)
            } else {
                ProactiveNudgeScheduler.cancel(context)
            }
        }
    }

    fun updateProactiveNudgeTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val currentConfig = _uiState.value.proactiveNudgeConfig
            val newConfig = currentConfig.copy(hour = hour, minute = minute)
            settingsDataStore.setProactiveNudgeConfig(newConfig)
            if (newConfig.enabled) {
                ProactiveNudgeScheduler.scheduleNext(context, hour, minute)
            }
        }
    }

    fun testProactiveNudge() {
        ProactiveNudgeScheduler.executeNow(context)
    }

    fun updateBaseUrl(url: String) {
        _uiState.update {
            it.copy(config = it.config.copy(baseUrl = url), testResult = null)
        }
    }

    fun updateApiKey(apiKey: String) {
        _uiState.update {
            it.copy(config = it.config.copy(apiKey = apiKey), testResult = null)
        }
    }

    fun updateModel(model: String) {
        _uiState.update {
            it.copy(config = it.config.copy(model = model), testResult = null)
        }
    }

    fun saveConfig() {
        viewModelScope.launch {
            settingsDataStore.setAIConfig(_uiState.value.config)
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, testResult = null, error = null) }
            settingsDataStore.setAIConfig(_uiState.value.config)

            try {
                val result = testAIConnectionUseCase()
                _uiState.update { it.copy(isTesting = false, testResult = result) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTesting = false,
                        testResult = false,
                        error = e.message ?: "连接测试失败"
                    )
                }
            }
        }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
