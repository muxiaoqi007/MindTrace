package com.mindtrace.diary.domain.usecase.storyline

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.StorylineCandidate
import com.mindtrace.diary.domain.model.StorylineCandidateEvidence
import com.mindtrace.diary.domain.model.StorylineType
import java.util.Locale

object StorylineCandidateDetector {
    const val MIN_EVIDENCE = 2

    fun detect(diaries: List<Diary>, suppressedNames: Set<String> = emptySet()): List<StorylineCandidate> {
        val evidenceByTag = linkedMapOf<String, MutableList<Pair<String, StorylineCandidateEvidence>>>()
        diaries.filterNot { it.isDeleted || it.excludeFromResurfacing }.forEach { diary ->
            val date = diary.date ?: diary.createdAt.toLocalDate()
            val excerpt = diary.content.replace(Regex("\\s+"), " ").trim().take(120)
            (diary.tags + diary.aiTags).map(String::trim).filter { it.length >= 2 }.distinctBy(::normalize).forEach { display ->
                evidenceByTag.getOrPut(normalize(display)) { mutableListOf() }
                    .add(display to StorylineCandidateEvidence(diary.id, date, excerpt))
            }
        }
        return evidenceByTag.mapNotNull { (normalized, values) ->
            val distinct = values.distinctBy { it.second.diaryId }
            if (distinct.size < MIN_EVIDENCE || normalized in suppressedNames) null
            else StorylineCandidate(
                name = distinct.first().first,
                normalizedName = normalized,
                type = inferType(distinct.first().first),
                evidence = distinct.map { it.second }.sortedBy { it.date }
            )
        }.sortedByDescending { it.evidence.size }
    }

    fun normalize(value: String): String = value.trim().lowercase(Locale.ROOT).replace(Regex("[\\s_-]+"), "")

    private fun inferType(name: String): StorylineType = when {
        listOf("学习", "练习", "阅读", "写作", "跑步", "健身", "英语", "编程").any(name::contains) -> StorylineType.SKILL
        listOf("妈", "爸", "家人", "朋友", "伴侣", "同事", "孩子").any(name::contains) -> StorylineType.RELATIONSHIP
        listOf("项目", "产品", "创业", "开发", "计划").any(name::contains) -> StorylineType.PROJECT
        else -> StorylineType.OTHER
    }
}
