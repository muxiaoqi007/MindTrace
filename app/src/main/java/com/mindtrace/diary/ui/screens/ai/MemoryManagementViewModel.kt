package com.mindtrace.diary.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.AIMemory
import com.mindtrace.diary.domain.model.MemoryCategory
import com.mindtrace.diary.domain.model.MemoryType
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MemoryManagementViewModel @Inject constructor(
    private val memoryRepository: AIMemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryManagementUiState())
    val uiState: StateFlow<MemoryManagementUiState> = _uiState.asStateFlow()

    init {
        loadMemories()
    }

    private fun loadMemories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            memoryRepository.getAllMemories().collect { memories ->
                val filtered = _uiState.value.selectedCategory?.let { category ->
                    memories.filter { it.category == category }
                } ?: memories

                _uiState.update {
                    it.copy(
                        memories = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun filterByCategory(category: MemoryCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadMemories()
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingMemory = null) }
    }

    fun showEditDialog(memory: AIMemory) {
        _uiState.update { it.copy(showAddDialog = true, editingMemory = memory) }
    }

    fun hideDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingMemory = null) }
    }

    fun addMemory(content: String, category: MemoryCategory) {
        viewModelScope.launch {
            try {
                memoryRepository.addManualMemory(
                    content = content,
                    category = category,
                    importance = 0.8f
                )
                hideDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "添加失败: ${e.message}") }
            }
        }
    }

    fun updateMemory(memory: AIMemory, content: String, category: MemoryCategory) {
        viewModelScope.launch {
            try {
                val updated = memory.copy(
                    content = content,
                    category = category,
                    updatedAt = LocalDateTime.now()
                )
                memoryRepository.updateMemory(updated)
                hideDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "更新失败: ${e.message}") }
            }
        }
    }

    fun toggleMemoryActive(memory: AIMemory) {
        viewModelScope.launch {
            try {
                memoryRepository.setMemoryActive(memory.id, !memory.isActive)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "操作失败: ${e.message}") }
            }
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            try {
                memoryRepository.deleteMemory(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "删除失败: ${e.message}") }
            }
        }
    }

    fun deleteAllAutoMemories() {
        viewModelScope.launch {
            try {
                memoryRepository.deleteAllAutoMemories()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "删除失败: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
