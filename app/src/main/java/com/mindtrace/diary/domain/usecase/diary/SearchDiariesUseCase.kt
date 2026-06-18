package com.mindtrace.diary.domain.usecase.diary

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(query: String): Flow<List<Diary>> {
        return diaryRepository.searchDiaries(query)
    }
}
