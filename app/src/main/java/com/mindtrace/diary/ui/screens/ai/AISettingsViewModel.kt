package com.mindtrace.diary.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.domain.usecase.ai.TestAIConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AISettingsViewModel @Inject constructor(
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
