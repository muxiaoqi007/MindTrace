package com.mindtrace.diary.domain.usecase.diary

import com.mindtrace.diary.domain.model.ContentBlock
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.DiaryEntry
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.usecase.ai.ExtractMemoryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class SaveDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val analyzeDiaryUseCase: AnalyzeDiaryUseCase,
    private val extractMemoryUseCase: ExtractMemoryUseCase
) {
    suspend operator fun invoke(
        id: String? = null,
        title: String,
        content: String,
        images: List<String> = emptyList(),
        contentBlocks: List<ContentBlock> = emptyList(),
        mood: MoodLevel? = null,
        weather: String? = null,
        location: String? = null,
        tags: List<String> = emptyList(),
        entries: List<DiaryEntry> = emptyList(),
        date: LocalDate? = null
    ): String {
        val now = LocalDateTime.now()
        val existingDiary = id?.let { diaryRepository.getDiaryById(it) }

        val diaryId = id ?: UUID.randomUUID().toString()

        val diary = Diary(
            id = diaryId,
            title = title,
            content = content,
            images = images,
            contentBlocks = contentBlocks,
            mood = mood,
            weather = weather,
            location = location,
            tags = tags,
            entries = entries.ifEmpty { existingDiary?.entries ?: emptyList() },
            date = date ?: existingDiary?.date ?: now.toLocalDate(),
            createdAt = existingDiary?.createdAt ?: now,
            updatedAt = now,
            // 保留已有的分析结果
            summary = existingDiary?.summary,
            sentimentScore = existingDiary?.sentimentScore,
            aiTags = existingDiary?.aiTags ?: emptyList()
        )

        if (id == null) {
            diaryRepository.insertDiary(diary)
        } else {
            diaryRepository.updateDiary(diary)
        }

        // 异步触发日记分析与记忆提取（不阻塞保存操作）
        CoroutineScope(Dispatchers.IO).launch {
            try {
                analyzeDiaryUseCase(diaryId)
            } catch (e: Exception) {
                // 分析失败不影响保存
            }
            try {
                extractMemoryUseCase(diaryId)
            } catch (e: Exception) {
                // 记忆提取失败不影响保存
            }
        }

        return diaryId
    }
}
