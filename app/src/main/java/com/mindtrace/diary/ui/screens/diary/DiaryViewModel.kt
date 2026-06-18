package com.mindtrace.diary.ui.screens.diary

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.usecase.diary.DeleteDiaryUseCase
import com.mindtrace.diary.domain.usecase.diary.GetDiaryByIdUseCase
import com.mindtrace.diary.domain.usecase.diary.SaveDiaryUseCase
import com.mindtrace.diary.domain.usecase.tag.GetAllTagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDiaryByIdUseCase: GetDiaryByIdUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase
) : ViewModel() {

    private val diaryId: String? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(DiaryEditUiState())
    val uiState: StateFlow<DiaryEditUiState> = _uiState.asStateFlow()

    private val _suggestedTags = MutableStateFlow<List<String>>(emptyList())
    val suggestedTags: StateFlow<List<String>> = _suggestedTags.asStateFlow()

    init {
        if (diaryId != null) {
            loadDiary(diaryId)
        }
        loadSuggestedTags()
    }

    private fun loadSuggestedTags() {
        viewModelScope.launch {
            try {
                val tags = getAllTagsUseCase()
                _suggestedTags.value = tags
            } catch (e: Exception) {
                // 忽略加载推荐标签的错误
            }
        }
    }

    private fun loadDiary(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getDiaryByIdUseCase(id).collect { diary ->
                if (diary != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            diary = diary,
                            title = diary.title,
                            content = diary.content,
                            images = diary.images,
                            mood = diary.mood,
                            weather = diary.weather,
                            location = diary.location,
                            tags = diary.tags,
                            entries = diary.entries,
                            date = diary.date
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "日记不存在"
                        )
                    }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun updateMood(mood: MoodLevel?) {
        _uiState.update { it.copy(mood = mood) }
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

    fun addTag(tag: String) {
        if (tag.isNotBlank() && tag !in _uiState.value.tags) {
            _uiState.update { it.copy(tags = it.tags + tag) }
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags - tag) }
    }

    fun saveDiary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                saveDiaryUseCase(
                    id = diaryId,
                    title = _uiState.value.title,
                    content = _uiState.value.content,
                    images = _uiState.value.images,
                    mood = _uiState.value.mood,
                    weather = _uiState.value.weather,
                    location = _uiState.value.location,
                    tags = _uiState.value.tags,
                    entries = _uiState.value.entries,
                    date = _uiState.value.date
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

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDiaryByIdUseCase: GetDiaryByIdUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase
) : ViewModel() {

    private val diaryId: String = savedStateHandle["id"] ?: ""

    private val _uiState = MutableStateFlow(DiaryDetailUiState())
    val uiState: StateFlow<DiaryDetailUiState> = _uiState.asStateFlow()

    init {
        loadDiary()
    }

    private fun loadDiary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getDiaryByIdUseCase(diaryId).collect { diary ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        diary = diary,
                        error = if (diary == null) "日记不存在" else null
                    )
                }
            }
        }
    }

    fun deleteDiary() {
        viewModelScope.launch {
            try {
                deleteDiaryUseCase(diaryId)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "删除失败") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
