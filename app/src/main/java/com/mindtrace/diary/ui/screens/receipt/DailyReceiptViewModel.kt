package com.mindtrace.diary.ui.screens.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.usecase.receipt.GetDailyReceiptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyReceiptViewModel @Inject constructor(
    getDailyReceipt: GetDailyReceiptUseCase
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = selectedDate
        .flatMapLatest { date ->
            getDailyReceipt(date)
                .map { receipt ->
                    DailyReceiptUiState(
                        selectedDate = date,
                        receipt = receipt,
                        isLoading = false
                    )
                }
                .onStart {
                    emit(DailyReceiptUiState(selectedDate = date, isLoading = true))
                }
                .catch { error ->
                    emit(
                        DailyReceiptUiState(
                            selectedDate = date,
                            isLoading = false,
                            error = error.message ?: "加载每日小票失败"
                        )
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyReceiptUiState()
        )

    fun previousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        if (selectedDate.value.isBefore(LocalDate.now())) {
            selectedDate.value = selectedDate.value.plusDays(1)
        }
    }

    fun today() {
        selectedDate.value = LocalDate.now()
    }
}
