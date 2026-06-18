package com.mindtrace.diary.domain.usecase.diary

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(): Flow<List<Diary>> {
        return diaryRepository.getAllDiaries()
    }
}
