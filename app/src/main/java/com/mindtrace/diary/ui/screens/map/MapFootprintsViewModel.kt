package com.mindtrace.diary.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.FootprintCluster
import com.mindtrace.diary.domain.model.MapFootprint
import com.mindtrace.diary.domain.model.MapFootprintBuilder
import com.mindtrace.diary.domain.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class MapFootprintsUiState(
    val month: YearMonth = YearMonth.now(),
    val points: List<MapFootprint> = emptyList(),
    val clusters: List<FootprintCluster> = emptyList()
)

@HiltViewModel
class MapFootprintsViewModel @Inject constructor(private val diaries: DiaryRepository) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    val uiState: StateFlow<MapFootprintsUiState> = combine(month, diaries.getAllDiaries()) { selected, values ->
        val points = MapFootprintBuilder.build(values, selected)
        MapFootprintsUiState(selected, points, MapFootprintBuilder.cluster(points))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapFootprintsUiState())

    fun previousMonth() { month.value = month.value.minusMonths(1) }
    fun nextMonth() { if (month.value.isBefore(YearMonth.now())) month.value = month.value.plusMonths(1) }
    fun removeCoordinates(diaryId: String) {
        viewModelScope.launch {
            diaries.getDiaryById(diaryId)?.let { diaries.updateDiary(it.copy(latitude = null, longitude = null)) }
        }
    }
}
