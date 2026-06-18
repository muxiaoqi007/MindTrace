package com.mindtrace.diary.domain.usecase.diary

import com.mindtrace.diary.domain.repository.DiaryRepository
import javax.inject.Inject

class DeleteDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(id: String) {
        diaryRepository.deleteDiary(id)
    }
}
