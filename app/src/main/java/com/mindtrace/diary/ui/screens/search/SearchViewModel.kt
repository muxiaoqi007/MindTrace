package com.mindtrace.diary.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val flashNoteRepository: FlashNoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }

        // Debounce search
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update {
                    it.copy(
                        diaries = emptyList(),
                        flashNotes = emptyList(),
                        isSearching = false
                    )
                }
                return@launch
            }

            delay(300) // Debounce
            search(query)
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }

            try {
                // 并行搜索日记和闪念
                val diariesFlow = diaryRepository.searchDiaries(query)
                val flashNotesFlow = flashNoteRepository.searchFlashNotes(query)

                combine(diariesFlow, flashNotesFlow) { diaries, flashNotes ->
                    Pair(diaries, flashNotes)
                }.collect { (diaries, flashNotes) ->
                    _uiState.update {
                        it.copy(
                            diaries = diaries,
                            flashNotes = flashNotes,
                            isSearching = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        error = e.message ?: "搜索失败"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
