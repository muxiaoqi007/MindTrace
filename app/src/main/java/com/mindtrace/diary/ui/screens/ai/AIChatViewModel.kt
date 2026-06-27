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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val conversationRepository: AIConversationRepository,
    private val aiReviewRepository: AiReviewRepository,
    private val settingsDataStore: SettingsDataStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    // 当前会话 ID
    private var currentConversationId: String? = savedStateHandle.get<String>("conversationId")

    init {
        // 监听 AI 配置状态
        viewModelScope.launch {
            settingsDataStore.aiConfig.collect { config ->
                _uiState.update {
                    it.copy(
                        isAIConfigured = config.isConfigured,
                        isAIEnabled = config.enabled
                    )
                }
            }
        }

        // 加载会话
        viewModelScope.launch {
            loadConversation()
        }

        // 加载未读回信
        loadUnreadReviews()
    }

    private fun loadUnreadReviews() {
        viewModelScope.launch {
            aiReviewRepository.getUnreadReviews().collect { reviews ->
                _uiState.update { it.copy(unreadReviews = reviews) }
            }
        }
    }

    private suspend fun loadConversation() {
        val conversationId = currentConversationId
        if (conversationId != null) {
            // 加载已有会话
            conversationRepository.getConversationByIdFlow(conversationId).collect { conversation ->
                if (conversation != null) {
                    _uiState.update {
                        it.copy(
                            currentConversation = conversation,
                            messages = conversation.messages.map { msg ->
                                ChatMessage(
                                    role = ChatMessage.Role.valueOf(msg.role.name),
                                    content = msg.content
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val message = _uiState.value.inputText.trim()
        if (message.isBlank()) return

        _uiState.update {
            it.copy(
                inputText = "",
                isStreaming = true,
                streamingContent = "",
                error = null
            )
        }

        viewModelScope.launch {
            try {
                // 确保有会话
                val conversationId = ensureConversation()

                // 添加用户消息到会话
                val userMessage = AIMessage(
                    role = AIMessage.Role.USER,
                    content = message,
                    timestamp = LocalDateTime.now()
                )
                conversationRepository.addMessage(conversationId, userMessage)

                // 更新 UI 显示用户消息
                val userChatMessage = ChatMessage(ChatMessage.Role.USER, message)
                _uiState.update {
                    it.copy(messages = it.messages + userChatMessage)
                }

                // 发送到 AI 并获取流式响应
                val responseBuilder = StringBuilder()
                aiRepository.chatStream(listOf(userChatMessage), includeContext = true)
                    .collect { chunk ->
                        if (chunk.isFinished) {
                            // 保存 AI 响应到会话
                            if (responseBuilder.isNotEmpty()) {
                                val assistantMessage = AIMessage(
                                    role = AIMessage.Role.ASSISTANT,
                                    content = responseBuilder.toString(),
                                    timestamp = LocalDateTime.now()
                                )
                                conversationRepository.addMessage(conversationId, assistantMessage)

                                // 添加到 AI Repository 历史
                                aiRepository.addToHistory(userChatMessage)
                                aiRepository.addToHistory(
                                    ChatMessage(ChatMessage.Role.ASSISTANT, responseBuilder.toString())
                                )

                                // 更新 UI
                                _uiState.update {
                                    it.copy(
                                        messages = it.messages + ChatMessage(
                                            ChatMessage.Role.ASSISTANT,
                                            responseBuilder.toString()
                                        ),
                                        isStreaming = false,
                                        streamingContent = ""
                                    )
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isStreaming = false,
                                        streamingContent = ""
                                    )
                                }
                            }
                        } else {
                            responseBuilder.append(chunk.content)
                            _uiState.update {
                                it.copy(streamingContent = responseBuilder.toString())
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStreaming = false,
                        streamingContent = "",
                        error = e.message ?: "发送失败"
                    )
                }
            }
        }
    }

    private suspend fun ensureConversation(): String {
        currentConversationId?.let { return it }

        // 创建新会话
        val conversation = conversationRepository.createConversation()
        currentConversationId = conversation.id
        _uiState.update { it.copy(currentConversation = conversation) }
        return conversation.id
    }

    fun clearHistory() {
        viewModelScope.launch {
            // 清空当前会话
            currentConversationId?.let { id ->
                conversationRepository.deleteConversation(id)
            }
            currentConversationId = null
            aiRepository.clearHistory()
            _uiState.update {
                it.copy(
                    messages = emptyList(),
                    currentConversation = null
                )
            }
        }
    }

    fun startNewConversation() {
        viewModelScope.launch {
            currentConversationId = null
            aiRepository.clearHistory()
            _uiState.update {
                it.copy(
                    messages = emptyList(),
                    currentConversation = null
                )
            }
        }
    }

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            currentConversationId = conversationId
            aiRepository.clearHistory()

            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                // 恢复对话历史到 AI Repository
                conversation.messages.forEach { msg ->
                    aiRepository.addToHistory(
                        ChatMessage(
                            role = ChatMessage.Role.valueOf(msg.role.name),
                            content = msg.content
                        )
                    )
                }

                _uiState.update {
                    it.copy(
                        currentConversation = conversation,
                        messages = conversation.messages.map { msg ->
                            ChatMessage(
                                role = ChatMessage.Role.valueOf(msg.role.name),
                                content = msg.content
                            )
                        }
                    )
                }
            }
        }
    }

    // 回信相关方法
    fun toggleReviewPanel() {
        _uiState.update { it.copy(showReviewPanel = !it.showReviewPanel) }
    }

    fun selectReview(review: AiReview) {
        _uiState.update {
            it.copy(
                selectedReview = review,
                replyText = review.userReply ?: ""
            )
        }
        // 标记为已读
        viewModelScope.launch {
            aiReviewRepository.markAsRead(review.id)
        }
    }

    fun dismissReview() {
        _uiState.update {
            it.copy(
                selectedReview = null,
                replyText = ""
            )
        }
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
            _uiState.update {
                it.copy(
                    selectedReview = null,
                    replyText = ""
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
