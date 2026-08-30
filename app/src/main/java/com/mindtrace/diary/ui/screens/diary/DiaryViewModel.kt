package com.mindtrace.diary.ui.screens.diary

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.ContentBlock
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.model.toImagePaths
import com.mindtrace.diary.domain.model.toPlainText
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.usecase.diary.DeleteDiaryUseCase
import com.mindtrace.diary.domain.usecase.diary.GetDiaryByIdUseCase
import com.mindtrace.diary.domain.usecase.diary.SaveDiaryUseCase
import com.mindtrace.diary.domain.usecase.tag.GetAllTagsUseCase
import com.mindtrace.diary.domain.usecase.ai.GenerateFollowUpQuestionUseCase
import com.mindtrace.diary.domain.model.DiaryEntry
import com.mindtrace.diary.domain.model.EntryType
import java.time.LocalDateTime
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDiaryByIdUseCase: GetDiaryByIdUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase
) : ViewModel() {

    private val diaryId: String? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(DiaryEditUiState())
    val uiState: StateFlow<DiaryEditUiState> = _uiState.asStateFlow()

    private val _suggestedTags = MutableStateFlow<List<String>>(emptyList())
    val suggestedTags: StateFlow<List<String>> = _suggestedTags.asStateFlow()

    init {
        if (diaryId != null) {
            loadDiary(diaryId)
        }
        loadSuggestedTags()
    }

    private fun loadSuggestedTags() {
        viewModelScope.launch {
            try {
                val tags = getAllTagsUseCase()
                _suggestedTags.value = tags
            } catch (e: Exception) {
                // 忽略加载推荐标签的错误
            }
        }
    }

    private fun loadDiary(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getDiaryByIdUseCase(id).collect { diary ->
                if (diary != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            diary = diary,
                            title = diary.title,
                            content = diary.content,
                            images = diary.images,
                            contentBlocks = diary.contentBlocks.ifEmpty {
                                listOf(ContentBlock.Text(id = UUID.randomUUID().toString(), text = diary.content))
                            }.toMutableList(),
                            mood = diary.mood,
                            weather = diary.weather,
                            location = diary.location,
                            latitude = diary.latitude,
                            longitude = diary.longitude,
                            coordinatesCleared = false,
                            tags = diary.tags,
                            entries = diary.entries,
                            date = diary.date,
                            excludeFromAI = diary.excludeFromAI,
                            excludeFromResurfacing = diary.excludeFromResurfacing
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "日记不存在"
                        )
                    }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun updateMood(mood: MoodLevel?) {
        _uiState.update { it.copy(mood = mood) }
    }

    fun addImage(imagePath: String) {
        _uiState.update { it.copy(images = it.images + imagePath) }
    }

    fun removeImage(imagePath: String) {
        _uiState.update { it.copy(images = it.images - imagePath) }
    }

    fun updateImages(images: List<String>) {
        _uiState.update { it.copy(images = images) }
    }

    fun addTag(tag: String) {
        if (tag.isNotBlank() && tag !in _uiState.value.tags) {
            _uiState.update { it.copy(tags = it.tags + tag) }
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags - tag) }
    }

    fun setExcludeFromAI(exclude: Boolean) {
        _uiState.update { it.copy(excludeFromAI = exclude) }
    }

    fun setLocation(name: String?, latitude: Double, longitude: Double) {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return
        _uiState.update {
            it.copy(
                location = name?.trim()?.takeIf(String::isNotEmpty),
                latitude = latitude,
                longitude = longitude,
                coordinatesCleared = false
            )
        }
    }

    fun clearLocationCoordinates() {
        _uiState.update { it.copy(latitude = null, longitude = null, coordinatesCleared = true) }
    }

    fun setExcludeFromResurfacing(exclude: Boolean) {
        _uiState.update { it.copy(excludeFromResurfacing = exclude) }
    }

    // --- 块级编辑操作 ---

    fun updateContentBlocks(blocks: List<ContentBlock>) {
        _uiState.update { it.copy(contentBlocks = blocks) }
    }

    fun updateBlockText(blockId: String, newText: String) {
        _uiState.update { state ->
            val blocks = state.contentBlocks.toMutableList()
            val index = blocks.indexOfFirst { it.id == blockId }
            if (index >= 0 && blocks[index] is ContentBlock.Text) {
                blocks[index] = (blocks[index] as ContentBlock.Text).copy(text = newText)
            }
            state.copy(contentBlocks = blocks)
        }
    }

    fun insertTextBlock(afterBlockId: String? = null) {
        _uiState.update { state ->
            val blocks = state.contentBlocks.toMutableList()
            val newBlock = ContentBlock.Text(id = UUID.randomUUID().toString())
            if (afterBlockId == null) {
                blocks.add(newBlock)
            } else {
                val index = blocks.indexOfFirst { it.id == afterBlockId }
                if (index >= 0) {
                    blocks.add(index + 1, newBlock)
                } else {
                    blocks.add(newBlock)
                }
            }
            state.copy(contentBlocks = blocks)
        }
    }

    fun insertImageBlock(afterBlockId: String? = null, imagePath: String) {
        _uiState.update { state ->
            val blocks = state.contentBlocks.toMutableList()
            val newBlock = ContentBlock.Image(id = UUID.randomUUID().toString(), path = imagePath)
            if (afterBlockId == null) {
                blocks.add(newBlock)
            } else {
                val index = blocks.indexOfFirst { it.id == afterBlockId }
                if (index >= 0) {
                    blocks.add(index + 1, newBlock)
                } else {
                    blocks.add(newBlock)
                }
            }
            state.copy(contentBlocks = blocks)
        }
    }

    fun deleteBlock(blockId: String) {
        _uiState.update { state ->
            val blocks = state.contentBlocks.toMutableList()
            blocks.removeAll { it.id == blockId }
            // 确保至少有一个文字块
            if (blocks.isEmpty()) {
                blocks.add(ContentBlock.Text(id = UUID.randomUUID().toString()))
            }
            state.copy(contentBlocks = blocks)
        }
    }

    fun updateImageCaption(blockId: String, caption: String) {
        _uiState.update { state ->
            val blocks = state.contentBlocks.toMutableList()
            val index = blocks.indexOfFirst { it.id == blockId }
            if (index >= 0 && blocks[index] is ContentBlock.Image) {
                blocks[index] = (blocks[index] as ContentBlock.Image).copy(caption = caption)
            }
            state.copy(contentBlocks = blocks)
        }
    }

    fun saveDiary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val state = _uiState.value
                // 从 contentBlocks 派生 content 和 images
                val content = state.contentBlocks.toPlainText()
                val images = state.contentBlocks.toImagePaths()

                saveDiaryUseCase(
                    id = diaryId,
                    title = state.title,
                    content = content,
                    images = images,
                    contentBlocks = state.contentBlocks,
                    mood = state.mood,
                    weather = state.weather,
                    location = state.location,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    clearCoordinates = state.coordinatesCleared,
                    tags = state.tags,
                    entries = state.entries,
                    date = state.date,
                    excludeFromAI = state.excludeFromAI,
                    excludeFromResurfacing = state.excludeFromResurfacing
                )
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "保存失败"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDiaryByIdUseCase: GetDiaryByIdUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val generateFollowUpQuestion: GenerateFollowUpQuestionUseCase,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val diaryId: String = savedStateHandle["id"] ?: ""

    private val _uiState = MutableStateFlow(DiaryDetailUiState())
    val uiState: StateFlow<DiaryDetailUiState> = _uiState.asStateFlow()

    init {
        loadDiary()
    }

    private fun loadDiary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getDiaryByIdUseCase(diaryId).collect { diary ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        diary = diary,
                        error = if (diary == null) "日记不存在" else null
                    )
                }
            }
        }
    }

    fun deleteDiary() {
        viewModelScope.launch {
            try {
                deleteDiaryUseCase(diaryId)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "删除失败") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun requestFollowUp(another: Boolean = false) {
        val diary = _uiState.value.diary ?: return
        if (diary.excludeFromAI || _uiState.value.isGeneratingFollowUp) return
        val previous = _uiState.value.followUpQuestion.takeIf { another }
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingFollowUp = true, error = null) }
            generateFollowUpQuestion(diaryId, previous)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isGeneratingFollowUp = false,
                            followUpQuestion = result?.question,
                            followUpEvidence = result?.evidence,
                            followUpAnswer = "",
                            error = if (result == null) "AI 未启用、未配置，或日记内容太短" else null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isGeneratingFollowUp = false, error = error.message ?: "生成追问失败")
                    }
                }
        }
    }

    fun updateFollowUpAnswer(value: String) {
        _uiState.update { it.copy(followUpAnswer = value) }
    }

    fun saveFollowUpAnswer() {
        val state = _uiState.value
        val diary = state.diary ?: return
        val question = state.followUpQuestion ?: return
        val answer = state.followUpAnswer.trim()
        if (answer.isEmpty() || state.isSavingFollowUp) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingFollowUp = true) }
            runCatching {
                diaryRepository.updateDiary(
                    diary.copy(
                        entries = diary.entries + DiaryEntry(
                            id = UUID.randomUUID().toString(),
                            content = "AI 追问：$question\n我的回答：$answer",
                            timestamp = LocalDateTime.now(),
                            type = EntryType.REFLECTION
                        ),
                        updatedAt = LocalDateTime.now()
                    )
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSavingFollowUp = false,
                        followUpQuestion = null,
                        followUpEvidence = null,
                        followUpAnswer = ""
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isSavingFollowUp = false, error = error.message ?: "保存回答失败") }
            }
        }
    }

    fun stopFollowUp() {
        _uiState.update { it.copy(followUpQuestion = null, followUpEvidence = null, followUpAnswer = "") }
    }
}
