package com.mindtrace.diary.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.domain.model.SelfNarrative
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import com.mindtrace.diary.domain.usecase.ai.GenerateSelfNarrativeUseCase
import com.mindtrace.diary.domain.usecase.ai.GetSelfPortraitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelfPortraitViewModel @Inject constructor(
    private val getSelfPortraitUseCase: GetSelfPortraitUseCase,
    private val generateSelfNarrativeUseCase: GenerateSelfNarrativeUseCase,
    private val memoryRepository: AIMemoryRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelfPortraitUiState())
    val uiState: StateFlow<SelfPortraitUiState> = _uiState.asStateFlow()

    init {
        refresh()
        observeAIConfig()
        loadNarrative()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                _uiState.update {
                    it.copy(isLoading = false, portrait = getSelfPortraitUseCase())
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "生成自我画像失败")
                }
            }
        }
    }

    /** 刷新叙事画像：先读缓存，缺失或用户强刷时重新生成 */
    fun refreshNarrative() {
        generateNarrative(force = true)
    }

    fun markMemoryAccurate(memoryId: String) {
        viewModelScope.launch {
            try {
                val memory = memoryRepository.getMemoryById(memoryId) ?: return@launch
                val reinforced = memory.copy(
                    importance = (memory.importance + ACCURACY_REINFORCE_STEP).coerceAtMost(1f)
                )
                memoryRepository.updateMemory(reinforced)
                _uiState.update { it.copy(message = "已记住：这条画像很像你") }
                refresh()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "反馈失败") }
            }
        }
    }

    fun markMemoryInaccurate(memoryId: String) {
        viewModelScope.launch {
            try {
                memoryRepository.setMemoryActive(memoryId, false)
                _uiState.update { it.copy(message = "已停用这条记忆，后续画像和 AI 上下文将不再引用") }
                refresh()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "反馈失败") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun observeAIConfig() {
        viewModelScope.launch {
            settingsDataStore.aiConfig.collect { config ->
                _uiState.update {
                    it.copy(isAIConfigured = config.isConfigured && config.enabled)
                }
            }
        }
    }

    private fun loadNarrative() {
        viewModelScope.launch {
            val cached = generateSelfNarrativeUseCase.getCachedOrNull()
            if (cached != null) {
                _uiState.update { it.copy(narrative = cached) }
                return@launch
            }
            // 直接读一次配置，避免与 observeAIConfig 的 collect 产生初始化竞态
            val config = settingsDataStore.aiConfig.first()
            if (config.isConfigured && config.enabled) {
                generateNarrative(force = false)
            }
        }
    }

    private fun generateNarrative(force: Boolean) {
        if (_uiState.value.isNarrativeLoading) return
        _uiState.update { it.copy(isNarrativeLoading = true, error = null) }
        viewModelScope.launch {
            try {
                if (force) {
                    val result = generateSelfNarrativeUseCase()
                    result.fold(
                        onSuccess = { narrative ->
                            _uiState.update { it.copy(isNarrativeLoading = false, narrative = narrative) }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(isNarrativeLoading = false, error = e.message ?: "生成画像失败")
                            }
                        }
                    )
                } else {
                    // 非强制时静默失败，不打扰用户
                    val narrative: SelfNarrative? = generateSelfNarrativeUseCase().getOrNull()
                    _uiState.update {
                        if (narrative != null) it.copy(isNarrativeLoading = false, narrative = narrative)
                        else it.copy(isNarrativeLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isNarrativeLoading = false, error = e.message ?: "生成画像失败") }
            }
        }
    }

    private companion object {
        const val ACCURACY_REINFORCE_STEP = 0.1f
    }
}
