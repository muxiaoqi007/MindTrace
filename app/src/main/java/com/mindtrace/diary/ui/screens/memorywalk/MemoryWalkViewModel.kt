package com.mindtrace.diary.ui.screens.memorywalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.MemoryWalkMode
import com.mindtrace.diary.domain.model.MemoryWalkPlan
import com.mindtrace.diary.domain.model.MemoryWalkStop
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.usecase.flashnote.SaveFlashNoteUseCase
import com.mindtrace.diary.domain.usecase.memorywalk.GetMemoryWalkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoryWalkUiState(
    val mode: MemoryWalkMode = MemoryWalkMode.SURPRISE,
    val keyword: String = "",
    val mood: MoodLevel = MoodLevel.GOOD,
    val plan: MemoryWalkPlan? = null,
    val currentStopIndex: Int = 0,
    val response: String = "",
    val isLoading: Boolean = false,
    val isSavingResponse: Boolean = false,
    val message: String? = null,
    val error: String? = null
) {
    val currentStop: MemoryWalkStop?
        get() = plan?.stops?.getOrNull(currentStopIndex)
}

@HiltViewModel
class MemoryWalkViewModel @Inject constructor(
    private val getMemoryWalk: GetMemoryWalkUseCase,
    private val saveFlashNote: SaveFlashNoteUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MemoryWalkUiState())
    val uiState: StateFlow<MemoryWalkUiState> = _uiState.asStateFlow()

    fun setMode(mode: MemoryWalkMode) {
        _uiState.update { it.copy(mode = mode, plan = null, error = null, message = null) }
    }

    fun setKeyword(keyword: String) {
        _uiState.update { it.copy(keyword = keyword, error = null) }
    }

    fun setMood(mood: MoodLevel) {
        _uiState.update { it.copy(mood = mood, error = null) }
    }

    fun startWalk() {
        val state = _uiState.value
        if (state.mode == MemoryWalkMode.KEYWORD && state.keyword.isBlank()) {
            _uiState.update { it.copy(error = "先输入一个想漫步的关键词") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            runCatching {
                getMemoryWalk(
                    mode = state.mode,
                    keyword = state.keyword,
                    mood = state.mood,
                    seed = System.nanoTime()
                )
            }.onSuccess { plan ->
                if (plan.stops.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            plan = null,
                            error = "还没有找到符合条件的旧记录，换一种路线试试吧"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            plan = plan,
                            currentStopIndex = 0,
                            response = ""
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message ?: "生成漫步路线失败")
                }
            }
        }
    }

    fun previousStop() {
        _uiState.update { state ->
            state.copy(
                currentStopIndex = (state.currentStopIndex - 1).coerceAtLeast(0),
                response = "",
                message = null
            )
        }
    }

    fun nextStop() {
        _uiState.update { state ->
            val lastIndex = (state.plan?.stops?.lastIndex ?: 0).coerceAtLeast(0)
            state.copy(
                currentStopIndex = (state.currentStopIndex + 1).coerceAtMost(lastIndex),
                response = "",
                message = null
            )
        }
    }

    fun updateResponse(response: String) {
        _uiState.update { it.copy(response = response, message = null) }
    }

    fun saveResponse() {
        val state = _uiState.value
        val response = state.response.trim()
        val stop = state.currentStop ?: return
        if (response.isEmpty() || state.isSavingResponse) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingResponse = true, error = null) }
            runCatching {
                saveFlashNote(
                    content = "「记忆漫步回应 · ${stop.memory.date}」\n$response"
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(isSavingResponse = false, response = "", message = "回应已保存为闪念")
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSavingResponse = false,
                        error = error.message ?: "保存回应失败"
                    )
                }
            }
        }
    }

    fun finishWalk() {
        _uiState.update { it.copy(plan = null, currentStopIndex = 0, response = "", message = null) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
