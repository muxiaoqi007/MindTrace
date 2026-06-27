package com.mindtrace.diary.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.repository.AIMemoryCandidateRepository
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryCenterViewModel @Inject constructor(
    private val memoryRepository: AIMemoryRepository,
    private val candidateRepository: AIMemoryCandidateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryCenterUiState())
    val uiState: StateFlow<MemoryCenterUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            combine(
                memoryRepository.getAllMemories(),
                candidateRepository.getPendingCount()
            ) { memories, pendingCount ->
                MemoryCenterUiState(
                    isLoading = false,
                    totalMemoryCount = memories.size,
                    activeMemoryCount = memories.count { it.isActive },
                    pendingCandidateCount = pendingCount,
                    categoryCounts = memories
                        .filter { it.isActive }
                        .groupingBy { it.category }
                        .eachCount()
                )
            }.catch { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "加载记忆中心失败")
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
