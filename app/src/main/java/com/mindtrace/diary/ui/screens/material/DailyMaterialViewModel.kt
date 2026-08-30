package com.mindtrace.diary.ui.screens.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.DailyMaterial
import com.mindtrace.diary.domain.usecase.diary.SaveDiaryUseCase
import com.mindtrace.diary.domain.usecase.material.DailyMaterialBasketBuilder
import com.mindtrace.diary.domain.usecase.material.GetDailyMaterialBasketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class DailyMaterialUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val items: List<DailyMaterial> = emptyList(),
    val selectedKeys: Set<String> = emptySet(),
    val title: String = defaultTitle(LocalDate.now()),
    val allowAI: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedDiaryId: String? = null,
    val error: String? = null
) {
    val selectedItems: List<DailyMaterial>
        get() = items.filter { it.key in selectedKeys }
    val draft: String
        get() = DailyMaterialBasketBuilder.composeDraft(selectedDate, selectedItems)
    val canGoForward: Boolean
        get() = selectedDate.isBefore(LocalDate.now())

    companion object {
        fun defaultTitle(date: LocalDate): String =
            date.format(DateTimeFormatter.ofPattern("M月d日的日记", Locale.CHINA))
    }
}

@HiltViewModel
class DailyMaterialViewModel @Inject constructor(
    private val getDailyMaterialBasket: GetDailyMaterialBasketUseCase,
    private val saveDiary: SaveDiaryUseCase
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val _uiState = MutableStateFlow(DailyMaterialUiState())
    val uiState: StateFlow<DailyMaterialUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val basketJob = viewModelScope.launch {
        selectedDate.flatMapLatest { date -> getDailyMaterialBasket(date) }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "加载今日素材失败"
                )
            }
            .collect { basket ->
                val previous = _uiState.value
                val dateChanged = previous.selectedDate != basket.date
                _uiState.value = previous.copy(
                    selectedDate = basket.date,
                    items = basket.items,
                    selectedKeys = if (dateChanged || previous.isLoading) {
                        DailyMaterialBasketBuilder.defaultSelection(basket.items)
                    } else {
                        previous.selectedKeys.intersect(basket.items.mapTo(hashSetOf(), DailyMaterial::key))
                    },
                    title = if (dateChanged) DailyMaterialUiState.defaultTitle(basket.date) else previous.title,
                    isLoading = false,
                    error = null
                )
            }
    }

    fun toggle(key: String) {
        val selected = _uiState.value.selectedKeys.toMutableSet()
        if (!selected.add(key)) selected.remove(key)
        _uiState.value = _uiState.value.copy(selectedKeys = selected)
    }

    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectedKeys = _uiState.value.items.mapTo(linkedSetOf(), DailyMaterial::key)
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedKeys = emptySet())
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun setAllowAI(value: Boolean) {
        _uiState.value = _uiState.value.copy(allowAI = value)
    }

    fun previousDay() = changeDate(selectedDate.value.minusDays(1))

    fun nextDay() {
        if (selectedDate.value.isBefore(LocalDate.now())) changeDate(selectedDate.value.plusDays(1))
    }

    fun today() = changeDate(LocalDate.now())

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.draft.isBlank()) return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            runCatching {
                saveDiary(
                    title = state.title.trim().ifEmpty { DailyMaterialUiState.defaultTitle(state.selectedDate) },
                    content = state.draft,
                    date = state.selectedDate,
                    excludeFromAI = !state.allowAI
                )
            }.onSuccess { diaryId ->
                _uiState.value = _uiState.value.copy(isSaving = false, savedDiaryId = diaryId)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = error.message ?: "保存整理稿失败"
                )
            }
        }
    }

    fun consumeSavedDiary() {
        _uiState.value = _uiState.value.copy(savedDiaryId = null)
    }

    private fun changeDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        selectedDate.value = date
    }
}
