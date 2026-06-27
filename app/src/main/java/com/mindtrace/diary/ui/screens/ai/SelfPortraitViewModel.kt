package com.mindtrace.diary.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import com.mindtrace.diary.domain.usecase.ai.GetSelfPortraitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelfPortraitViewModel @Inject constructor(
    private val getSelfPortraitUseCase: GetSelfPortraitUseCase,
    private val memoryRepository: AIMemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelfPortraitUiState())
    val uiState: StateFlow<SelfPortraitUiState> = _uiState.asStateFlow()

    init {
        refresh()
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

    fun markMemoryAccurate(memoryId: String) {
        val acknowledged = memoryId.isNotBlank()
        _uiState.update {
            it.copy(message = if (acknowledged) "已收到反馈：这条画像像你" else "已收到反馈")
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
}
