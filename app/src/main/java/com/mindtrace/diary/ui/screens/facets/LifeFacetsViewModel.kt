package com.mindtrace.diary.ui.screens.facets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.FacetCheckIn
import com.mindtrace.diary.domain.model.FacetInsight
import com.mindtrace.diary.domain.model.FacetMoodObservation
import com.mindtrace.diary.domain.model.LifeFacet
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.LifeFacetRepository
import com.mindtrace.diary.domain.usecase.facets.FacetCorrelationCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class LifeFacetsUiState(
    val facets: List<LifeFacet> = emptyList(),
    val todayCheckIns: Map<String, FacetCheckIn> = emptyMap(),
    val insights: Map<String, FacetInsight> = emptyMap()
)

@HiltViewModel
class LifeFacetsViewModel @Inject constructor(
    private val repository: LifeFacetRepository,
    diaryRepository: DiaryRepository
) : ViewModel() {
    val uiState: StateFlow<LifeFacetsUiState> = combine(
        repository.observeFacets(),
        repository.observeAllCheckIns(),
        diaryRepository.getAllDiaries()
    ) { facets, checkIns, diaries ->
        val moods = diaries.filterNot { it.isDeleted || it.excludeFromResurfacing }
            .mapNotNull { diary ->
                diary.mood?.let { mood ->
                    FacetMoodObservation(
                        date = diary.date ?: diary.createdAt.toLocalDate(),
                        score = mood.score.toFloat()
                    )
                }
            }
        LifeFacetsUiState(
            facets = facets,
            todayCheckIns = checkIns.filter { it.date == LocalDate.now() }.associateBy(FacetCheckIn::facetId),
            insights = facets.associate { facet ->
                facet.id to FacetCorrelationCalculator.calculate(facet.id, facet.options, checkIns, moods)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LifeFacetsUiState())

    fun saveFacet(name: String, icon: String, color: Long, options: List<String>) {
        val cleanName = name.trim()
        val cleanOptions = options.map(String::trim).filter(String::isNotEmpty).distinct()
        if (cleanName.isEmpty() || cleanOptions.isEmpty()) return
        viewModelScope.launch {
            val now = LocalDateTime.now()
            repository.saveFacet(
                LifeFacet(
                    id = UUID.randomUUID().toString(),
                    name = cleanName,
                    icon = icon.trim().ifEmpty { "✦" },
                    color = color,
                    options = cleanOptions,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun selectOption(facetId: String, option: String) {
        viewModelScope.launch {
            val existing = uiState.value.todayCheckIns[facetId]
            if (existing?.option == option) {
                repository.removeCheckIn(facetId, LocalDate.now())
            } else {
                repository.checkIn(
                    FacetCheckIn(
                        id = "$facetId:${LocalDate.now().toEpochDay()}",
                        facetId = facetId,
                        option = option,
                        date = LocalDate.now(),
                        createdAt = LocalDateTime.now()
                    )
                )
            }
        }
    }

    fun archiveFacet(id: String) {
        viewModelScope.launch { repository.archiveFacet(id) }
    }
}
