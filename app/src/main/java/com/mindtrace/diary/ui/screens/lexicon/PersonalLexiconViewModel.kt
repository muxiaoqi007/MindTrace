package com.mindtrace.diary.ui.screens.lexicon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.LexiconEntry
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.LexiconRepository
import com.mindtrace.diary.domain.usecase.lexicon.PersonalLexiconExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalLexiconViewModel @Inject constructor(
    private val repository: LexiconRepository,
    private val diaries: DiaryRepository
) : ViewModel() {
    val entries: StateFlow<List<LexiconEntry>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun discover() {
        viewModelScope.launch {
            val suppressed = entries.value.mapTo(hashSetOf()) { PersonalLexiconExtractor.key(it.type, it.term) }
            repository.addCandidates(PersonalLexiconExtractor.extract(diaries.getAllDiaries().first(), suppressed))
        }
    }

    fun confirm(id: String) = launch { repository.confirm(id) }
    fun reject(id: String) = launch { repository.reject(id) }
    fun correct(id: String, meaning: String?) = launch { repository.correct(id, meaning) }
    private fun launch(block: suspend () -> Unit) { viewModelScope.launch { block() } }
}
