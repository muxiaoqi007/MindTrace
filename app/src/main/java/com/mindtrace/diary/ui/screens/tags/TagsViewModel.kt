package com.mindtrace.diary.ui.screens.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.usecase.tag.GetAllTagsUseCase
import com.mindtrace.diary.domain.usecase.tag.GetDiariesByTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagsUiState(
    val isLoading: Boolean = true,
    val tags: List<TagWithCount> = emptyList(),
    val error: String? = null
)

data class TagWithCount(
    val name: String,
    val count: Int
)

data class TagDiariesUiState(
    val isLoading: Boolean = true,
    val tag: String = "",
    val diaries: List<Diary> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val getDiariesByTagUseCase: GetDiariesByTagUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    private val _tagDiariesState = MutableStateFlow(TagDiariesUiState())
    val tagDiariesState: StateFlow<TagDiariesUiState> = _tagDiariesState.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val tags = getAllTagsUseCase()

                // 获取每个标签的日记数量
                val tagsWithCount = tags.map { tag ->
                    val count = getDiariesByTagUseCase(tag).first().size
                    TagWithCount(tag, count)
                }.sortedByDescending { it.count }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tags = tagsWithCount
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }

    fun loadDiariesByTag(tag: String) {
        viewModelScope.launch {
            _tagDiariesState.update { it.copy(isLoading = true, tag = tag) }

            getDiariesByTagUseCase(tag).collect { diaries ->
                _tagDiariesState.update {
                    it.copy(
                        isLoading = false,
                        diaries = diaries
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        loadTags()
    }
}
