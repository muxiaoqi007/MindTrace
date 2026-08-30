package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.LexiconDao
import com.mindtrace.diary.core.database.entity.LexiconEntryEntity
import com.mindtrace.diary.core.database.entity.LexiconEvidenceEntity
import com.mindtrace.diary.domain.model.LexiconCandidate
import com.mindtrace.diary.domain.model.LexiconEntry
import com.mindtrace.diary.domain.model.LexiconEvidence
import com.mindtrace.diary.domain.model.LexiconStatus
import com.mindtrace.diary.domain.model.LexiconType
import com.mindtrace.diary.domain.repository.LexiconRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LexiconRepositoryImpl @Inject constructor(private val dao: LexiconDao) : LexiconRepository {
    override fun observeAll(): Flow<List<LexiconEntry>> = combine(dao.observeEntries(), dao.observeEvidence()) { entries, evidence ->
        val grouped = evidence.groupBy(LexiconEvidenceEntity::entryId)
        entries.map { entry ->
            LexiconEntry(
                entry.id, entry.term, entry.normalizedTerm,
                runCatching { LexiconType.valueOf(entry.type) }.getOrDefault(LexiconType.PHRASE),
                entry.generatedMeaning, entry.correctedMeaning,
                runCatching { LexiconStatus.valueOf(entry.status) }.getOrDefault(LexiconStatus.CANDIDATE),
                grouped[entry.id].orEmpty().map { LexiconEvidence(it.id, it.entryId, it.diaryId, LocalDate.ofEpochDay(it.date), it.excerpt) },
                entry.createdAt.toDateTime(), entry.updatedAt.toDateTime()
            )
        }
    }

    override fun observeConfirmed(): Flow<List<LexiconEntry>> = observeAll().map { it.filter { entry -> entry.status == LexiconStatus.CONFIRMED } }

    override suspend fun addCandidates(values: List<LexiconCandidate>) {
        val now = LocalDateTime.now().toMillis()
        values.forEach { candidate ->
            val id = UUID.randomUUID().toString()
            if (dao.insertEntry(LexiconEntryEntity(id, candidate.term, candidate.normalizedTerm, candidate.type.name, candidate.generatedMeaning, null, LexiconStatus.CANDIDATE.name, now, now)) != -1L) {
                dao.insertEvidence(candidate.evidence.map { LexiconEvidenceEntity(UUID.randomUUID().toString(), id, it.diaryId, it.date.toEpochDay(), it.excerpt) })
            }
        }
    }

    override suspend fun confirm(id: String) = dao.updateStatus(id, LexiconStatus.CONFIRMED.name, LocalDateTime.now().toMillis())
    override suspend fun reject(id: String) = dao.updateStatus(id, LexiconStatus.REJECTED.name, LocalDateTime.now().toMillis())
    override suspend fun correct(id: String, meaning: String?) = dao.correct(id, meaning?.trim()?.takeIf(String::isNotEmpty), LocalDateTime.now().toMillis())
    private fun LocalDateTime.toMillis() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun Long.toDateTime() = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
