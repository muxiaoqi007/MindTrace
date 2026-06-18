package com.mindtrace.diary.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.usecase.statistics.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val yearMonth: YearMonth = YearMonth.now(),
    // 月度统计
    val monthlyStatistics: MoodStatistics? = null,
    // 总览统计
    val overview: OverviewStatistics? = null,
    // 心情趋势数据
    val moodTrend: List<MoodTrendPoint> = emptyList(),
    // 错误信息
    val error: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getMoodStatisticsUseCase: GetMoodStatisticsUseCase,
    private val getOverviewStatisticsUseCase: GetOverviewStatisticsUseCase,
    private val getMoodTrendUseCase: GetMoodTrendUseCase,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadAllStatistics()
    }

    private fun loadAllStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 并行加载所有统计数据
            launch { loadOverviewStatistics() }
            launch { loadMoodTrend() }
            launch { loadMonthlyStatistics() }
        }
    }

    private suspend fun loadOverviewStatistics() {
        getOverviewStatisticsUseCase().collect { overview ->
            _uiState.update {
                it.copy(
                    overview = overview,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadMoodTrend() {
        getMoodTrendUseCase(days = 7).collect { trend ->
            _uiState.update { it.copy(moodTrend = trend) }
        }
    }

    private suspend fun loadMonthlyStatistics() {
        getMoodStatisticsUseCase(_uiState.value.yearMonth).collect { statistics ->
            _uiState.update {
                it.copy(
                    monthlyStatistics = statistics,
                    isLoading = false
                )
            }
        }
    }

    fun previousMonth() {
        _uiState.update { it.copy(yearMonth = it.yearMonth.minusMonths(1)) }
        viewModelScope.launch {
            loadMonthlyStatistics()
        }
    }

    fun nextMonth() {
        _uiState.update { it.copy(yearMonth = it.yearMonth.plusMonths(1)) }
        viewModelScope.launch {
            loadMonthlyStatistics()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
