package com.mindtrace.diary.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.repository.AIMemoryCandidateRepository
import com.mindtrace.diary.domain.usecase.ai.ApproveMemoryCandidateUseCase
import com.mindtrace.diary.domain.usecase.ai.RejectMemoryCandidateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryInboxViewModel @Inject constructor(
    private val candidateRepository: AIMemoryCandidateRepository,
    private val approveMemoryCandidateUseCase: ApproveMemoryCandidateUseCase,
    private val rejectMemoryCandidateUseCase: RejectMemoryCandidateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryInboxUiState())
    val uiState: StateFlow<MemoryInboxUiState> = _uiState.asStateFlow()

    init {
        loadCandidates()
    }

    private fun loadCandidates() {
        viewModelScope.launch {
            candidateRepository.getPendingCandidates()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "加载候选记忆失败")
                    }
                }
                .collect { candidates ->
                    _uiState.update {
                        it.copy(isLoading = false, candidates = candidates)
                    }
                }
        }
    }

    fun approve(candidateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            approveMemoryCandidateUseCase(candidateId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "确认记忆失败") }
                }
            _uiState.update { it.copy(isProcessing = false) }
        }
    }

    fun reject(candidateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            rejectMemoryCandidateUseCase(candidateId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "拒绝记忆失败") }
                }
            _uiState.update { it.copy(isProcessing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
