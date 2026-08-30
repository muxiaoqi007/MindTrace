package com.mindtrace.diary.domain.usecase.lexicon

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.LexiconCandidate
import com.mindtrace.diary.domain.model.LexiconCandidateEvidence
import com.mindtrace.diary.domain.model.LexiconType
import java.util.Locale

object PersonalLexiconExtractor {
    fun extract(diaries: List<Diary>, suppressedKeys: Set<String> = emptySet()): List<LexiconCandidate> {
        data class Mention(val term: String, val type: LexiconType, val evidence: LexiconCandidateEvidence)
        val mentions = buildList {
            diaries.filterNot { it.isDeleted || it.excludeFromResurfacing }.forEach { diary ->
                val date = diary.date ?: diary.createdAt.toLocalDate()
                val excerpt = diary.content.replace(Regex("\\s+"), " ").trim().take(140)
                val evidence = LexiconCandidateEvidence(diary.id, date, excerpt)
                diary.location?.trim()?.takeIf(String::isNotEmpty)?.let { add(Mention(it, LexiconType.PLACE, evidence)) }
                (diary.tags + diary.aiTags).forEach { tag ->
                    PREFIXES.firstOrNull { tag.startsWith(it.first, ignoreCase = true) }?.let { (prefix, type) ->
                        tag.substring(prefix.length).trim().takeIf { it.length >= 2 }?.let { add(Mention(it, type, evidence)) }
                    }
                }
                WISH.findAll(diary.content).map { it.groupValues[1].trim().take(40) }
                    .filter { it.length >= 2 }.forEach { add(Mention(it, LexiconType.WISH, evidence)) }
            }
        }
        return mentions.groupBy { key(it.type, it.term) }.mapNotNull { (key, values) ->
            if (key in suppressedKeys) return@mapNotNull null
            val distinct = values.distinctBy { it.evidence.diaryId }
            val first = distinct.first()
            LexiconCandidate(
                term = first.term,
                normalizedTerm = normalize(first.term),
                type = first.type,
                generatedMeaning = "你在 ${distinct.size} 篇记录中提到“${first.term}”。",
                evidence = distinct.map { it.evidence }.sortedBy { it.date }
            )
        }.sortedWith(compareBy<LexiconCandidate> { it.type.ordinal }.thenByDescending { it.evidence.size })
    }

    fun normalize(value: String): String = value.trim().lowercase(Locale.ROOT).replace(Regex("[\\s_-]+"), "")
    fun key(type: LexiconType, term: String) = "${type.name}:${normalize(term)}"

    private val PREFIXES = listOf(
        "人物:" to LexiconType.PERSON, "人物：" to LexiconType.PERSON,
        "地点:" to LexiconType.PLACE, "地点：" to LexiconType.PLACE,
        "词语:" to LexiconType.PHRASE, "词语：" to LexiconType.PHRASE,
        "愿望:" to LexiconType.WISH, "愿望：" to LexiconType.WISH
    )
    private val WISH = Regex("(?:我希望|希望|我想要|我想)([^\n。！？!?]{2,40})")
}
