package com.mindtrace.diary.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.CalendarDay
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.usecase.calendar.GetCalendarDaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = true,
    val yearMonth: YearMonth = YearMonth.now(),
    val days: List<CalendarDay> = emptyList(),
    val selectedDate: CalendarDay? = null,
    val selectedDateDiaries: List<Diary> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getCalendarDaysUseCase: GetCalendarDaysUseCase,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCalendar()
    }

    private fun loadCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getCalendarDaysUseCase(_uiState.value.yearMonth).collect { days ->
                val today = days.find { it.isToday }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        days = days,
                        selectedDate = today
                    )
                }

                today?.let { selectDate(it) }
            }
        }
    }

    fun previousMonth() {
        _uiState.update { it.copy(yearMonth = it.yearMonth.minusMonths(1)) }
        loadCalendar()
    }

    fun nextMonth() {
        _uiState.update { it.copy(yearMonth = it.yearMonth.plusMonths(1)) }
        loadCalendar()
    }

    fun selectDate(day: CalendarDay) {
        _uiState.update { it.copy(selectedDate = day) }

        viewModelScope.launch {
            diaryRepository.getDiariesByDate(day.date).collect { diaries ->
                _uiState.update { it.copy(selectedDateDiaries = diaries) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
