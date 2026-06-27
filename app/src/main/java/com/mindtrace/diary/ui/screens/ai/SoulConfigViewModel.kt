package com.mindtrace.diary.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.datastore.AIConfig
import com.mindtrace.diary.core.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoulConfigViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoulConfigUiState())
    val uiState: StateFlow<SoulConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.soulConfig
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "加载 Soul 设置失败") }
                }
                .collect { config ->
                    _uiState.update { it.copy(isLoading = false, config = config, isSaved = false) }
                }
        }
    }

    fun updatePersona(id: String) {
        _uiState.update { it.copy(config = it.config.copy(chatPersonaId = id), isSaved = false) }
    }

    fun updateUserDisplayName(name: String) {
        _uiState.update { it.copy(config = it.config.copy(userDisplayName = name), isSaved = false) }
    }

    fun updateAiDisplayName(name: String) {
        _uiState.update { it.copy(config = it.config.copy(aiDisplayName = name), isSaved = false) }
    }

    fun updateRelationshipNote(note: String) {
        _uiState.update { it.copy(config = it.config.copy(relationshipNote = note), isSaved = false) }
    }

    fun updateCustomSystemPrompt(prompt: String) {
        _uiState.update { it.copy(config = it.config.copy(customSystemPrompt = prompt), isSaved = false) }
    }

    fun resetSystemPrompt() {
        updateCustomSystemPrompt(AIConfig.DEFAULT_SYSTEM_PROMPT)
    }

    fun save() {
        viewModelScope.launch {
            try {
                settingsDataStore.setSoulConfig(_uiState.value.config)
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "保存 Soul 设置失败") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
