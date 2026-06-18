package com.mindtrace.diary.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.TimelineItem
import com.mindtrace.diary.domain.repository.AiReviewRepository
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import com.mindtrace.diary.domain.repository.TodoRepository
import com.mindtrace.diary.domain.usecase.diary.AppendFlashNoteToDiaryUseCase
import com.mindtrace.diary.domain.usecase.diary.RemoveFlashNoteFromDiaryUseCase
import com.mindtrace.diary.domain.usecase.flashnote.DeleteFlashNoteUseCase
import com.mindtrace.diary.domain.usecase.todo.DeleteTodoUseCase
import com.mindtrace.diary.domain.usecase.todo.SaveTodoUseCase
import com.mindtrace.diary.domain.usecase.todo.ToggleTodoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val flashNoteRepository: FlashNoteRepository,
    private val todoRepository: TodoRepository,
    private val aiReviewRepository: AiReviewRepository,
    private val toggleTodoUseCase: ToggleTodoUseCase,
    private val appendFlashNoteToDiaryUseCase: AppendFlashNoteToDiaryUseCase,
    private val saveTodoUseCase: SaveTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val deleteFlashNoteUseCase: DeleteFlashNoteUseCase,
    private val removeFlashNoteFromDiaryUseCase: RemoveFlashNoteFromDiaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTimeline()
        loadUnreadCount()
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            aiReviewRepository.getUnreadCount().collect { count ->
                _uiState.update { it.copy(unreadReviewCount = count) }
            }
        }
    }

    private fun loadTimeline() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                diaryRepository.getAllDiaries(),
                flashNoteRepository.getAllFlashNotes(),
                todoRepository.getAllTodos()
            ) { diaries, flashNotes, todos ->
                val items = mutableListOf<TimelineItem>()

                // Map diaries to timeline items
                diaries.forEach { diary ->
                    items.add(
                        TimelineItem.DiaryItem(
                            id = diary.id,
                            title = diary.title,
                            contentPreview = diary.content.take(100),
                            firstImage = diary.images.firstOrNull(),
                            mood = diary.mood,
                            createdAt = diary.createdAt
                        )
                    )

                    // Also add diary entries (flash notes) as timeline items
                    diary.entries.forEach { entry ->
                        items.add(
                            TimelineItem.FlashNoteItem(
                                id = entry.id,
                                content = entry.content,
                                firstImage = entry.images.firstOrNull(),
                                createdAt = entry.timestamp,
                                diaryId = diary.id
                            )
                        )
                    }
                }

                // Map flash notes to timeline items
                flashNotes.forEach { note ->
                    items.add(
                        TimelineItem.FlashNoteItem(
                            id = note.id,
                            content = note.content,
                            firstImage = note.images.firstOrNull(),
                            createdAt = note.createdAt,
                            diaryId = null
                        )
                    )
                }

                // Map todos to timeline items
                todos.forEach { todo ->
                    items.add(
                        TimelineItem.TodoItem(
                            id = todo.id,
                            content = todo.content,
                            isCompleted = todo.isCompleted,
                            dueDate = todo.dueDate,
                            createdAt = todo.createdAt
                        )
                    )
                }

                items.sortedByDescending { it.createdAt }
            }.collect { items ->
                _uiState.update { state ->
                    val filteredItems = filterItems(items, state.selectedFilter, state.searchQuery)
                    state.copy(
                        isLoading = false,
                        items = filteredItems
                    )
                }
            }
        }
    }

    fun setFilter(filter: FilterType) {
        _uiState.update { it.copy(selectedFilter = filter, quickInput = "") }
        loadTimeline()
    }

    fun toggleTodo(id: String) {
        viewModelScope.launch {
            try {
                toggleTodoUseCase(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateQuickInput(value: String) {
        _uiState.update { it.copy(quickInput = value) }
    }

    fun submitQuickInput() {
        val content = _uiState.value.quickInput.trim()
        if (content.isBlank()) return

        if (_uiState.value.selectedFilter == FilterType.TODO) {
            quickAddTodo(content)
        } else {
            quickAddFlashNote(content)
        }
    }

    private fun quickAddFlashNote(content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingItem = true) }
            try {
                appendFlashNoteToDiaryUseCase(content)
                _uiState.update { it.copy(quickInput = "", isAddingItem = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isAddingItem = false) }
            }
        }
    }

    private fun quickAddTodo(content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingItem = true) }
            try {
                saveTodoUseCase(content = content)
                _uiState.update { it.copy(quickInput = "", isAddingItem = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isAddingItem = false) }
            }
        }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch {
            try {
                deleteTodoUseCase(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteFlashNote(id: String, diaryId: String?) {
        viewModelScope.launch {
            try {
                if (diaryId != null) {
                    // Flash note is part of a diary entry
                    removeFlashNoteFromDiaryUseCase(id, diaryId)
                } else {
                    // Flash note is standalone
                    deleteFlashNoteUseCase(id)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun filterItems(
        items: List<TimelineItem>,
        filter: FilterType,
        query: String
    ): List<TimelineItem> {
        var filtered = when (filter) {
            FilterType.ALL -> items
            FilterType.DIARY -> items.filterIsInstance<TimelineItem.DiaryItem>()
            FilterType.FLASHNOTE -> items.filterIsInstance<TimelineItem.FlashNoteItem>()
            FilterType.TODO -> items.filterIsInstance<TimelineItem.TodoItem>()
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter { item ->
                when (item) {
                    is TimelineItem.DiaryItem ->
                        item.title.contains(query, ignoreCase = true) ||
                        item.contentPreview.contains(query, ignoreCase = true)
                    is TimelineItem.FlashNoteItem ->
                        item.content.contains(query, ignoreCase = true)
                    is TimelineItem.TodoItem ->
                        item.content.contains(query, ignoreCase = true)
                }
            }
        }

        return filtered
    }
}
