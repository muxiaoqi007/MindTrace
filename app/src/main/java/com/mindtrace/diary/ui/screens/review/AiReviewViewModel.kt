package com.mindtrace.diary.ui.screens.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.domain.model.AiReview
import com.mindtrace.diary.domain.repository.AiReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiReviewDetailUiState(
    val review: AiReview? = null,
    val isLoading: Boolean = true
)

data class AiReviewListUiState(
    val reviews: List<AiReview> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AiReviewViewModel @Inject constructor(
    private val aiReviewRepository: AiReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reviewId: String? = savedStateHandle["reviewId"]

    private val _detailUiState = MutableStateFlow(AiReviewDetailUiState())
    val detailUiState: StateFlow<AiReviewDetailUiState> = _detailUiState.asStateFlow()

    private val _listUiState = MutableStateFlow(AiReviewListUiState())
    val listUiState: StateFlow<AiReviewListUiState> = _listUiState.asStateFlow()

    init {
        if (reviewId != null) {
            loadReviewDetail(reviewId)
        }
        loadReviewList()
    }

    private fun loadReviewDetail(id: String) {
        viewModelScope.launch {
            aiReviewRepository.getReviewByIdFlow(id).collect { review ->
                _detailUiState.update {
                    it.copy(review = review, isLoading = false)
                }
                // 标记为已读
                if (review != null && !review.isRead) {
                    aiReviewRepository.markAsRead(id)
                }
            }
        }
    }

    private fun loadReviewList() {
        viewModelScope.launch {
            aiReviewRepository.getAllReviews().collect { reviews ->
                _listUiState.update {
                    it.copy(reviews = reviews, isLoading = false)
                }
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            aiReviewRepository.markAsRead(id)
        }
    }

    fun deleteReview(id: String) {
        viewModelScope.launch {
            aiReviewRepository.deleteReview(id)
        }
    }
}
