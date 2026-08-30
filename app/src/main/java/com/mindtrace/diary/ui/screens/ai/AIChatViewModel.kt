package com.mindtrace.diary.ui.screens.ai

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.ai.ChatMessage
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.domain.model.AIConversation
import com.mindtrace.diary.domain.model.AIMessage
import com.mindtrace.diary.domain.model.AiReview
import com.mindtrace.diary.domain.repository.AIConversationRepository
import com.mindtrace.diary.domain.repository.AIRepository
import com.mindtrace.diary.domain.repository.AiReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val conversationRepository: AIConversationRepository,
    private val aiReviewRepository: AiReviewRepository,
    private val settingsDataStore: SettingsDataStore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: String? = savedStateHandle[CONVERSATION_ID_KEY]
    private var sendJob: Job? = null
    private var conversationLoadJob: Job? = null

    init {
        // 首页"今日洞察"等问题可以通过 seed 参数预填输入框
        savedStateHandle.get<String>(SEED_KEY)
            ?.takeIf { it.isNotBlank() }
            ?.let { seed -> _uiState.update { it.copy(inputText = seed) } }
        viewModelScope.launch {
            settingsDataStore.aiConfig.collect { config ->
                _uiState.update {
                    it.copy(isAIConfigured = config.isConfigured, isAIEnabled = config.enabled)
                }
            }
        }
        currentConversationId?.let(::beginLoadConversation)
        viewModelScope.launch {
            aiReviewRepository.getUnreadReviews().collect { reviews ->
                _uiState.update { it.copy(unreadReviews = reviews) }
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun setIncludePersonalContext(include: Boolean) {
        if (_uiState.value.isStreaming || _uiState.value.isLoading) return
        _uiState.update { it.copy(includePersonalContext = include) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val messageText = state.inputText.trim()
        if (messageText.isBlank() || state.isLoading || state.isStreaming || sendJob?.isActive == true) return

        _uiState.update {
            it.copy(inputText = "", isStreaming = true, streamingContent = "", error = null)
        }

        sendJob = viewModelScope.launch {
            val responseBuilder = StringBuilder()
            var responseFinalized = false
            try {
                val conversationId = ensureConversation()
                val userMessage = AIMessage(
                    role = AIMessage.Role.USER,
                    content = messageText,
                    timestamp = LocalDateTime.now()
                )
                conversationRepository.addMessage(conversationId, userMessage)

                val userChatMessage = ChatMessage(ChatMessage.Role.USER, messageText)
                val requestMessages = _uiState.value.messages + userChatMessage
                val updatedConversation = conversationRepository.getConversationById(conversationId)
                _uiState.update {
                    it.copy(messages = requestMessages, currentConversation = updatedConversation ?: it.currentConversation)
                }

                aiRepository.chatStream(
                    messages = requestMessages,
                    includeContext = state.includePersonalContext
                ).collect { chunk ->
                    if (responseFinalized) return@collect
                    if (chunk.isFinished) {
                        responseFinalized = true
                        finalizeAssistantResponse(conversationId, responseBuilder.toString())
                    } else {
                        responseBuilder.append(chunk.content)
                        _uiState.update { it.copy(streamingContent = responseBuilder.toString()) }
                    }
                }

                // Defensive fallback for providers that complete their Flow without a marker.
                if (!responseFinalized) {
                    responseFinalized = true
                    finalizeAssistantResponse(conversationId, responseBuilder.toString())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val partialContent = responseBuilder.toString()
                if (partialContent.isNotBlank()) {
                    currentConversationId?.let { conversationId ->
                        finalizeAssistantResponse(
                            conversationId = conversationId,
                            content = partialContent,
                            warning = "连接中断，已保留生成到一半的内容"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isStreaming = false,
                            streamingContent = "",
                            error = e.message ?: "发送失败"
                        )
                    }
                }
            } finally {
                val finishingJob = currentCoroutineContext()[Job]
                if (sendJob === finishingJob) {
                    sendJob = null
                    _uiState.update { stateAfter ->
                        if (stateAfter.isStreaming) stateAfter.copy(isStreaming = false) else stateAfter
                    }
                }
            }
        }
    }

    private suspend fun finalizeAssistantResponse(
        conversationId: String,
        content: String,
        warning: String? = null
    ) {
        if (content.isBlank()) {
            _uiState.update {
                it.copy(isStreaming = false, streamingContent = "", error = "AI 没有返回内容，请重试")
            }
            return
        }

        val assistantMessage = AIMessage(
            role = AIMessage.Role.ASSISTANT,
            content = content,
            timestamp = LocalDateTime.now()
        )
        conversationRepository.addMessage(conversationId, assistantMessage)
        val updatedConversation = conversationRepository.getConversationById(conversationId)
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(ChatMessage.Role.ASSISTANT, content),
                currentConversation = updatedConversation ?: it.currentConversation,
                isStreaming = false,
                streamingContent = "",
                error = warning
            )
        }
    }

    private suspend fun ensureConversation(): String {
        currentConversationId?.let { return it }
        val conversation = conversationRepository.createConversation()
        setCurrentConversationId(conversation.id)
        _uiState.update { it.copy(currentConversation = conversation) }
        return conversation.id
    }

    fun clearHistory() {
        cancelActiveGeneration()
        cancelConversationLoad()
        val conversationIdToDelete = currentConversationId
        setCurrentConversationId(null)
        _uiState.update {
            it.copy(messages = emptyList(), currentConversation = null, streamingContent = "", error = null)
        }
        viewModelScope.launch {
            conversationIdToDelete?.let { conversationRepository.deleteConversation(it) }
        }
    }

    fun startNewConversation() {
        cancelActiveGeneration()
        cancelConversationLoad()
        setCurrentConversationId(null)
        _uiState.update {
            it.copy(messages = emptyList(), currentConversation = null, streamingContent = "", error = null)
        }
    }

    fun loadConversation(conversationId: String) {
        cancelActiveGeneration()
        beginLoadConversation(conversationId)
    }

    private fun beginLoadConversation(conversationId: String) {
        cancelConversationLoad()
        _uiState.update { it.copy(isLoading = true, error = null) }
        conversationLoadJob = viewModelScope.launch {
            try {
                loadConversationState(conversationId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "加载对话失败") }
            } finally {
                val finishingJob = currentCoroutineContext()[Job]
                if (conversationLoadJob === finishingJob) {
                    conversationLoadJob = null
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private suspend fun loadConversationState(conversationId: String) {
        val conversation = conversationRepository.getConversationById(conversationId)
        if (conversation == null) {
            if (currentConversationId == conversationId) setCurrentConversationId(null)
            _uiState.update { it.copy(messages = emptyList(), currentConversation = null, error = "对话不存在或已删除") }
            return
        }
        setCurrentConversationId(conversationId)
        _uiState.update {
            it.copy(currentConversation = conversation, messages = conversation.messages.toChatMessages())
        }
    }

    private fun setCurrentConversationId(conversationId: String?) {
        currentConversationId = conversationId
        savedStateHandle[CONVERSATION_ID_KEY] = conversationId
    }

    private fun cancelActiveGeneration() {
        sendJob?.cancel()
        sendJob = null
        _uiState.update { it.copy(isStreaming = false, streamingContent = "") }
    }

    private fun cancelConversationLoad() {
        conversationLoadJob?.cancel()
        conversationLoadJob = null
        _uiState.update { it.copy(isLoading = false) }
    }

    private fun List<AIMessage>.toChatMessages(): List<ChatMessage> = map { message ->
        ChatMessage(role = ChatMessage.Role.valueOf(message.role.name), content = message.content)
    }

    fun toggleReviewPanel() {
        _uiState.update { it.copy(showReviewPanel = !it.showReviewPanel) }
    }

    fun selectReview(review: AiReview) {
        _uiState.update { it.copy(selectedReview = review, replyText = review.userReply ?: "") }
        viewModelScope.launch { aiReviewRepository.markAsRead(review.id) }
    }

    fun dismissReview() {
        _uiState.update { it.copy(selectedReview = null, replyText = "") }
    }

    fun updateReplyText(text: String) {
        _uiState.update { it.copy(replyText = text) }
    }

    fun submitReply() {
        val review = _uiState.value.selectedReview ?: return
        val reply = _uiState.value.replyText.trim()
        if (reply.isBlank()) return

        viewModelScope.launch {
            aiReviewRepository.saveUserReply(review.id, reply)
            _uiState.update { it.copy(selectedReview = null, replyText = "") }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private companion object {
        const val CONVERSATION_ID_KEY = "conversationId"
        const val SEED_KEY = "seed"
    }
}
