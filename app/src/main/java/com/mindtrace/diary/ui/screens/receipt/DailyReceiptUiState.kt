package com.mindtrace.diary.ui.screens.receipt

import com.mindtrace.diary.domain.model.DailyReceipt
import java.time.LocalDate

data class DailyReceiptUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val receipt: DailyReceipt? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val canGoForward: Boolean
        get() = selectedDate.isBefore(LocalDate.now())
}
