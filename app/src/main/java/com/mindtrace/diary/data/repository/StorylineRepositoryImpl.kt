package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.StorylineDao
import com.mindtrace.diary.core.database.entity.StorylineEntity
import com.mindtrace.diary.core.database.entity.StorylineSourceEntity
import com.mindtrace.diary.domain.model.Storyline
import com.mindtrace.diary.domain.model.StorylineCandidate
import com.mindtrace.diary.domain.model.StorylineSource
import com.mindtrace.diary.domain.model.StorylineStatus
import com.mindtrace.diary.domain.model.StorylineType
import com.mindtrace.diary.domain.repository.StorylineRepository
import com.mindtrace.diary.domain.usecase.storyline.StorylineCandidateDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorylineRepositoryImpl @Inject constructor(private val dao: StorylineDao) : StorylineRepository {
    override fun observeAll(): Flow<List<Storyline>> = combine(dao.observeStorylines(), dao.observeSources()) { lines, sources ->
        val grouped = sources.groupBy(StorylineSourceEntity::storylineId)
        lines.map { line ->
            Storyline(
                id = line.id, name = line.name, normalizedName = line.normalizedName,
                type = runCatching { StorylineType.valueOf(line.type) }.getOrDefault(StorylineType.OTHER),
                status = runCatching { StorylineStatus.valueOf(line.status) }.getOrDefault(StorylineStatus.CANDIDATE),
                sources = grouped[line.id].orEmpty().map { StorylineSource(it.id, it.storylineId, it.diaryId, LocalDate.ofEpochDay(it.date), it.excerpt) },
                createdAt = line.createdAt.toDateTime(), updatedAt = line.updatedAt.toDateTime()
            )
        }
    }

    override suspend fun addCandidates(values: List<StorylineCandidate>) {
        val now = LocalDateTime.now().toMillis()
        values.forEach { value ->
            val id = UUID.randomUUID().toString()
            val inserted = dao.insertStoryline(StorylineEntity(id, value.name, value.normalizedName, value.type.name, StorylineStatus.CANDIDATE.name, now, now))
            if (inserted != -1L) {
                dao.insertSources(value.evidence.map { evidence ->
                    StorylineSourceEntity(UUID.randomUUID().toString(), id, evidence.diaryId, evidence.date.toEpochDay(), evidence.excerpt)
                })
            }
        }
    }

    override suspend fun confirm(id: String) = status(id, StorylineStatus.CONFIRMED)
    override suspend fun reject(id: String) = status(id, StorylineStatus.REJECTED)
    override suspend fun archive(id: String) = status(id, StorylineStatus.ARCHIVED)
    override suspend fun rename(id: String, name: String) = dao.rename(id, name.trim(), StorylineCandidateDetector.normalize(name), LocalDateTime.now().toMillis())
    override suspend fun merge(sourceId: String, targetId: String) = dao.merge(sourceId, targetId)
    override suspend fun removeSource(sourceId: String) = dao.deleteSource(sourceId)

    override suspend fun splitSource(sourceId: String, newName: String) {
        val all = observeAllOnce()
        val source = all.flatMap(Storyline::sources).firstOrNull { it.id == sourceId } ?: return
        val now = LocalDateTime.now().toMillis()
        val newId = UUID.randomUUID().toString()
        dao.upsertStoryline(StorylineEntity(newId, newName.trim(), StorylineCandidateDetector.normalize(newName), StorylineType.OTHER.name, StorylineStatus.CONFIRMED.name, now, now))
        dao.deleteSource(sourceId)
        dao.insertSources(listOf(StorylineSourceEntity(UUID.randomUUID().toString(), newId, source.diaryId, source.date.toEpochDay(), source.excerpt)))
    }

    private suspend fun observeAllOnce(): List<Storyline> = observeAll().first()
    private suspend fun status(id: String, value: StorylineStatus) = dao.updateStatus(id, value.name, LocalDateTime.now().toMillis())
    private fun LocalDateTime.toMillis() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun Long.toDateTime() = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
