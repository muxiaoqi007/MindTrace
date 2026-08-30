package com.mindtrace.diary.ui.screens.onesecond

import android.content.Context
import com.mindtrace.diary.core.datastore.SettingsDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.export.OneSecondMontageExporter
import com.mindtrace.diary.core.media.OneSecondReminderScheduler
import com.mindtrace.diary.domain.model.DailyMediaPick
import com.mindtrace.diary.domain.model.DailyMediaSlot
import com.mindtrace.diary.domain.model.DailyMediaTimeline
import com.mindtrace.diary.domain.model.DailyMediaType
import com.mindtrace.diary.domain.repository.DailyMediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

data class OneSecondLifeUiState(
    val month: YearMonth = YearMonth.now(),
    val slots: List<DailyMediaSlot> = emptyList(),
    val allPicks: List<DailyMediaPick> = emptyList(),
    val reminderEnabled: Boolean = false,
    val isExporting: Boolean = false,
    val exportedFile: File? = null,
    val error: String? = null
)

@HiltViewModel
class OneSecondLifeViewModel @Inject constructor(
    private val repository: DailyMediaRepository,
    private val settings: SettingsDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val transient = MutableStateFlow(OneSecondLifeUiState())

    val uiState: StateFlow<OneSecondLifeUiState> = combine(
        month, repository.observeAll(), settings.oneSecondReminderEnabled, transient
    ) { selectedMonth, picks, reminder, state ->
        state.copy(
            month = selectedMonth,
            slots = DailyMediaTimeline.month(selectedMonth, picks),
            allPicks = picks,
            reminderEnabled = reminder
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OneSecondLifeUiState())

    fun previousMonth() { month.value = month.value.minusMonths(1) }
    fun nextMonth() { if (month.value.isBefore(YearMonth.now())) month.value = month.value.plusMonths(1) }

    fun save(date: LocalDate, uri: String, type: DailyMediaType) {
        viewModelScope.launch {
            repository.save(DailyMediaPick("daily-media-$date", date, uri, type, LocalDateTime.now()))
        }
    }

    fun delete(date: LocalDate) { viewModelScope.launch { repository.delete(date) } }

    fun setReminder(enabled: Boolean) {
        viewModelScope.launch {
            settings.setOneSecondReminderEnabled(enabled)
            if (enabled) OneSecondReminderScheduler.schedule(context) else OneSecondReminderScheduler.cancel(context)
        }
    }

    fun export() {
        val state = uiState.value
        if (state.isExporting) return
        viewModelScope.launch {
            transient.value = transient.value.copy(isExporting = true, error = null)
            runCatching { OneSecondMontageExporter.export(context, state.month, state.allPicks) }
                .onSuccess { transient.value = transient.value.copy(isExporting = false, exportedFile = it) }
                .onFailure { transient.value = transient.value.copy(isExporting = false, error = it.message ?: "合成失败") }
        }
    }

    fun consumeExport() { transient.value = transient.value.copy(exportedFile = null) }
    fun clearError() { transient.value = transient.value.copy(error = null) }
}
