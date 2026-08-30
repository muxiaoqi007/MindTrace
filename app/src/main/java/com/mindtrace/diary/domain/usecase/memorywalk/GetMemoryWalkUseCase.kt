package com.mindtrace.diary.domain.usecase.memorywalk

import com.mindtrace.diary.domain.model.MemoryWalkCandidate
import com.mindtrace.diary.domain.model.MemoryWalkMode
import com.mindtrace.diary.domain.model.MemoryWalkPlan
import com.mindtrace.diary.domain.model.MemoryWalkSourceType
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class GetMemoryWalkUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val flashNoteRepository: FlashNoteRepository
) {
    suspend operator fun invoke(
        mode: MemoryWalkMode,
        keyword: String = "",
        mood: MoodLevel? = null,
        seed: Long = System.currentTimeMillis(),
        today: LocalDate = LocalDate.now()
    ): MemoryWalkPlan {
        val candidates = combine(
            diaryRepository.getAllDiaries(),
            flashNoteRepository.getAllFlashNotes()
        ) { diaries, flashNotes ->
            buildList {
                diaries.filterNot { it.isDeleted || it.excludeFromResurfacing }.forEach { diary ->
                    val excerpt = diary.content.cleanExcerpt()
                    if (excerpt.isNotEmpty()) {
                        add(
                            MemoryWalkCandidate(
                                id = diary.id,
                                sourceType = MemoryWalkSourceType.DIARY,
                                date = diary.date ?: diary.createdAt.toLocalDate(),
                                title = diary.title.trim().takeIf(String::isNotEmpty),
                                excerpt = excerpt,
                                mood = diary.mood,
                                tags = (diary.tags + diary.aiTags).map(String::trim).filter(String::isNotEmpty).distinct()
                            )
                        )
                    }
                }
                flashNotes.filterNot { it.isDeleted || it.excludeFromResurfacing }.forEach { note ->
                    val excerpt = note.content.cleanExcerpt()
                    if (excerpt.isNotEmpty()) {
                        add(
                            MemoryWalkCandidate(
                                id = note.id,
                                sourceType = MemoryWalkSourceType.FLASH_NOTE,
                                date = note.createdAt.toLocalDate(),
                                title = null,
                                excerpt = excerpt,
                                mood = null,
                                tags = emptyList()
                            )
                        )
                    }
                }
            }
        }.first()

        return MemoryWalkPlanner.plan(
            candidates = candidates,
            mode = mode,
            keyword = keyword,
            mood = mood,
            today = today,
            seed = seed
        )
    }

    private fun String.cleanExcerpt(): String = replace(WHITESPACE, " ")
        .trim()
        .take(MAX_EXCERPT_LENGTH)

    private companion object {
        val WHITESPACE = Regex("\\s+")
        const val MAX_EXCERPT_LENGTH = 160
    }
}
