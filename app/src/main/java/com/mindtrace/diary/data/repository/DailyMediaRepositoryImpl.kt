package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.DailyMediaPickDao
import com.mindtrace.diary.core.database.entity.DailyMediaPickEntity
import com.mindtrace.diary.domain.model.DailyMediaPick
import com.mindtrace.diary.domain.model.DailyMediaType
import com.mindtrace.diary.domain.repository.DailyMediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyMediaRepositoryImpl @Inject constructor(private val dao: DailyMediaPickDao) : DailyMediaRepository {
    override fun observeAll(): Flow<List<DailyMediaPick>> = dao.observeAll().map { values -> values.map { entity ->
        DailyMediaPick(entity.id, LocalDate.ofEpochDay(entity.date), entity.uri, runCatching { DailyMediaType.valueOf(entity.type) }.getOrDefault(DailyMediaType.IMAGE), entity.createdAt.toDateTime())
    } }
    override suspend fun save(pick: DailyMediaPick) = dao.upsert(DailyMediaPickEntity(pick.id, pick.date.toEpochDay(), pick.uri, pick.type.name, pick.createdAt.toMillis()))
    override suspend fun delete(date: LocalDate) = dao.deleteDate(date.toEpochDay())
    private fun LocalDateTime.toMillis() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun Long.toDateTime() = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
