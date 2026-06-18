package com.mindtrace.diary.domain.usecase.tag

import com.mindtrace.diary.domain.repository.DiaryRepository
import javax.inject.Inject

/**
 * 获取所有标签用例
 */
class GetAllTagsUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(): List<String> {
        return diaryRepository.getAllTags()
    }
}
