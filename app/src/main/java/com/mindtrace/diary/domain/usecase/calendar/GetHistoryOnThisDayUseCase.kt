package com.mindtrace.diary.domain.usecase.calendar

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryOnThisDayUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(): Flow<List<Diary>> {
        return diaryRepository.getHistoryOnThisDay()
    }
}
