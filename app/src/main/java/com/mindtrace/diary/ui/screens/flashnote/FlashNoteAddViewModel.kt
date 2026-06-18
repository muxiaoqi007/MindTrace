package com.mindtrace.diary.ui.screens.flashnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.usecase.flashnote.SaveFlashNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlashNoteAddUiState(
    val content: String = "",
    val images: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FlashNoteAddViewModel @Inject constructor(
    private val saveFlashNoteUseCase: SaveFlashNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashNoteAddUiState())
    val uiState: StateFlow<FlashNoteAddUiState> = _uiState.asStateFlow()

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun addImage(imagePath: String) {
        _uiState.update { it.copy(images = it.images + imagePath) }
    }

    fun removeImage(imagePath: String) {
        _uiState.update { it.copy(images = it.images - imagePath) }
    }

    fun updateImages(images: List<String>) {
        _uiState.update { it.copy(images = images) }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                saveFlashNoteUseCase(
                    content = _uiState.value.content,
                    images = _uiState.value.images
                )
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "保存失败"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
