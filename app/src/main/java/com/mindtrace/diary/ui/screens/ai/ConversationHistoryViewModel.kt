package com.mindtrace.diary.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.repository.AIConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationHistoryViewModel @Inject constructor(
    private val conversationRepository: AIConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationHistoryUiState())
    val uiState: StateFlow<ConversationHistoryUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            conversationRepository.getAllConversations().collect { conversations ->
                _uiState.update {
                    it.copy(
                        conversations = conversations,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            try {
                conversationRepository.deleteConversation(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "删除失败: ${e.message}") }
            }
        }
    }

    fun deleteAllConversations() {
        viewModelScope.launch {
            try {
                conversationRepository.deleteAllConversations()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "删除失败: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
