package com.mindtrace.diary.ui.screens.print

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.export.GeneratedPrintArchive
import com.mindtrace.diary.core.export.PrintArchiveManager
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.PrintArchiveConfig
import com.mindtrace.diary.domain.model.PrintArchiveSelector
import com.mindtrace.diary.domain.model.PrintArchiveType
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.usecase.print.BuildPrintArchiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

enum class PrintArchiveAction { SHARE, PRINT }
data class PrintArchiveResult(val archive: GeneratedPrintArchive, val action: PrintArchiveAction)

data class PrintArchiveUiState(
    val config: PrintArchiveConfig = PrintArchiveConfig(startDate = LocalDate.now().minusMonths(1), endDate = LocalDate.now()),
    val selectedDiaries: List<Diary> = emptyList(),
    val tags: List<String> = emptyList(),
    val estimatedPages: Int = 0,
    val isGenerating: Boolean = false,
    val result: PrintArchiveResult? = null,
    val error: String? = null
)

@HiltViewModel
class PrintArchiveViewModel @Inject constructor(
    diaryRepository: DiaryRepository,
    private val buildContent: BuildPrintArchiveUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val config = MutableStateFlow(PrintArchiveUiState().config)
    private val transient = MutableStateFlow(PrintArchiveUiState())
    val uiState: StateFlow<PrintArchiveUiState> = combine(
        config, diaryRepository.getAllDiaries(), transient
    ) { selectedConfig, diaries, state ->
        val selection = PrintArchiveSelector.select(selectedConfig, diaries)
        val pages = when (selectedConfig.type) {
            PrintArchiveType.JOURNAL -> PrintArchiveSelector.estimatedJournalPages(selection.diaries, selectedConfig.fontScale, selectedConfig.includePhotos)
            PrintArchiveType.RECEIPTS -> selection.dates.size
            PrintArchiveType.WEEKLY_MAGAZINES -> selection.weekAnchors.size
        }
        state.copy(
            config = selectedConfig,
            selectedDiaries = selection.diaries,
            tags = diaries.flatMap { it.tags + it.aiTags }.map(String::trim).filter(String::isNotEmpty).distinct().sorted(),
            estimatedPages = pages
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrintArchiveUiState())

    fun setType(value: PrintArchiveType) = update { copy(type = value) }
    fun setStartDate(value: LocalDate) = update { copy(startDate = value, endDate = maxOf(endDate, value)) }
    fun setEndDate(value: LocalDate) = update { copy(endDate = value, startDate = minOf(startDate, value)) }
    fun setTag(value: String?) = update { copy(tag = value?.takeIf(String::isNotBlank)) }
    fun setFontScale(value: Float) = update { copy(fontScale = value.coerceIn(.8f, 1.5f)) }
    fun setIncludePhotos(value: Boolean) = update { copy(includePhotos = value) }
    fun setIncludeMetadata(value: Boolean) = update { copy(includeMetadata = value) }
    fun setIncludePrivate(value: Boolean) = update { copy(includePrivate = value) }

    fun generate(action: PrintArchiveAction) {
        if (transient.value.isGenerating) return
        val snapshot = config.value
        viewModelScope.launch {
            transient.value = transient.value.copy(isGenerating = true, error = null)
            runCatching {
                val content = buildContent(snapshot)
                withContext(Dispatchers.IO) { PrintArchiveManager.generate(context, content) }
            }.onSuccess { archive ->
                transient.value = transient.value.copy(isGenerating = false, result = PrintArchiveResult(archive, action))
            }.onFailure { error ->
                transient.value = transient.value.copy(isGenerating = false, error = error.message ?: "PDF 生成失败")
            }
        }
    }

    fun consumeResult() { transient.value = transient.value.copy(result = null) }
    fun clearError() { transient.value = transient.value.copy(error = null) }
    private fun update(block: PrintArchiveConfig.() -> PrintArchiveConfig) { config.value = config.value.block() }
}
