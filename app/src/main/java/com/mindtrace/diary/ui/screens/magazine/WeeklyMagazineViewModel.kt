package com.mindtrace.diary.ui.screens.magazine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.WeeklyMagazine
import com.mindtrace.diary.domain.usecase.magazine.GetWeeklyMagazineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class WeeklyMagazineUiState(val anchor: LocalDate = LocalDate.now(), val magazine: WeeklyMagazine? = null)

@HiltViewModel
class WeeklyMagazineViewModel @Inject constructor(getMagazine: GetWeeklyMagazineUseCase) : ViewModel() {
    private val anchor = MutableStateFlow(LocalDate.now())
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<WeeklyMagazineUiState> = anchor.flatMapLatest { date ->
        getMagazine(date).map { WeeklyMagazineUiState(date, it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeeklyMagazineUiState())

    fun previous() { anchor.value = anchor.value.minusWeeks(1) }
    fun next() { if (anchor.value.minusWeeks(1).isBefore(LocalDate.now())) anchor.value = anchor.value.plusWeeks(1).coerceAtMost(LocalDate.now()) }
}

private fun LocalDate.coerceAtMost(maximum: LocalDate): LocalDate = if (isAfter(maximum)) maximum else this
