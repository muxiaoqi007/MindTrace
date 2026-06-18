package com.mindtrace.diary.domain.usecase.tag

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 按标签获取日记用例
 */
class GetDiariesByTagUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(tag: String): Flow<List<Diary>> {
        return diaryRepository.getDiariesByTag(tag)
    }
}
