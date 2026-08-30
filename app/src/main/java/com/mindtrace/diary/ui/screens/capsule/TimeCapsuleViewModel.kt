package com.mindtrace.diary.ui.screens.capsule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.capsule.TimeCapsuleScheduler
import com.mindtrace.diary.domain.model.TimeCapsule
import com.mindtrace.diary.domain.model.TimeCapsuleDraft
import com.mindtrace.diary.domain.model.TimeCapsuleSummary
import com.mindtrace.diary.domain.repository.TimeCapsuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class TimeCapsuleUiState(
    val summaries: List<TimeCapsuleSummary> = emptyList(),
    val title: String = "",
    val message: String = "",
    val prediction: String = "",
    val question: String = "",
    val mediaUris: List<String> = emptyList(),
    val unlockDate: LocalDate = LocalDate.now().plusMonths(1),
    val opened: TimeCapsule? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TimeCapsuleViewModel @Inject constructor(
    private val repository: TimeCapsuleRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val editor = MutableStateFlow(TimeCapsuleUiState())

    val uiState: StateFlow<TimeCapsuleUiState> = combine(
        repository.observeSummaries(), editor
    ) { summaries, state -> state.copy(summaries = summaries) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeCapsuleUiState())

    fun updateTitle(value: String) = edit { copy(title = value) }
    fun updateMessage(value: String) = edit { copy(message = value) }
    fun updatePrediction(value: String) = edit { copy(prediction = value) }
    fun updateQuestion(value: String) = edit { copy(question = value) }
    fun updateUnlockDate(value: LocalDate) = edit { copy(unlockDate = value) }
    fun addMedia(uris: List<String>) = edit { copy(mediaUris = (mediaUris + uris).distinct()) }
    fun removeMedia(uri: String) = edit { copy(mediaUris = mediaUris - uri) }
    fun clearError() = edit { copy(error = null) }
    fun closeOpened() = edit { copy(opened = null) }

    fun seal(onSuccess: () -> Unit) {
        val state = editor.value
        if (state.message.isBlank() || state.isSaving) return
        viewModelScope.launch {
            edit { copy(isSaving = true, error = null) }
            val unlockAt = state.unlockDate.atTime(9, 0)
            runCatching {
                val title = state.title.trim().ifEmpty { "给未来的自己" }
                val id = repository.seal(
                    TimeCapsuleDraft(
                        title = title,
                        message = state.message,
                        prediction = state.prediction,
                        question = state.question,
                        mediaUris = state.mediaUris,
                        unlockAt = unlockAt
                    )
                )
                TimeCapsuleScheduler.schedule(context, id, title, unlockAt)
            }.onSuccess {
                editor.value = TimeCapsuleUiState()
                onSuccess()
            }.onFailure { error -> edit { copy(isSaving = false, error = error.message ?: "封存失败") } }
        }
    }

    fun open(id: String) {
        viewModelScope.launch {
            val value = repository.openIfUnlocked(id)
            if (value == null) edit { copy(error = "还没到开启时间") }
            else edit { copy(opened = value, error = null) }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            TimeCapsuleScheduler.cancel(context, id)
        }
    }

    fun resetEditor() {
        val summaries = editor.value.summaries
        editor.value = TimeCapsuleUiState(summaries = summaries)
    }

    private fun edit(block: TimeCapsuleUiState.() -> TimeCapsuleUiState) {
        editor.value = editor.value.block()
    }
}
