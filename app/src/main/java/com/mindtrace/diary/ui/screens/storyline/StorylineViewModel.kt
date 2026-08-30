package com.mindtrace.diary.ui.screens.storyline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.Storyline
import com.mindtrace.diary.domain.model.StorylineStatus
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.StorylineRepository
import com.mindtrace.diary.domain.usecase.storyline.StorylineCandidateDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorylineViewModel @Inject constructor(
    private val repository: StorylineRepository,
    private val diaries: DiaryRepository
) : ViewModel() {
    val storylines: StateFlow<List<Storyline>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun discover() {
        viewModelScope.launch {
            val existing = storylines.value.mapTo(hashSetOf(), Storyline::normalizedName)
            val candidates = StorylineCandidateDetector.detect(diaries.getAllDiaries().first(), existing)
            repository.addCandidates(candidates)
        }
    }

    fun confirm(id: String) = launch { repository.confirm(id) }
    fun reject(id: String) = launch { repository.reject(id) }
    fun archive(id: String) = launch { repository.archive(id) }
    fun rename(id: String, name: String) { if (name.isNotBlank()) launch { repository.rename(id, name) } }
    fun merge(sourceId: String, targetId: String) { if (sourceId != targetId) launch { repository.merge(sourceId, targetId) } }
    fun removeSource(id: String) = launch { repository.removeSource(id) }
    fun splitSource(id: String, name: String) { if (name.isNotBlank()) launch { repository.splitSource(id, name) } }

    private fun launch(block: suspend () -> Unit) { viewModelScope.launch { block() } }
}
