package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.LifeFacetDao
import com.mindtrace.diary.core.database.entity.FacetCheckInEntity
import com.mindtrace.diary.core.database.entity.LifeFacetEntity
import com.mindtrace.diary.domain.model.FacetCheckIn
import com.mindtrace.diary.domain.model.LifeFacet
import com.mindtrace.diary.domain.repository.LifeFacetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LifeFacetRepositoryImpl @Inject constructor(
    private val dao: LifeFacetDao
) : LifeFacetRepository {
    override fun observeFacets(): Flow<List<LifeFacet>> =
        dao.observeActiveFacets().map { values -> values.map { it.toDomain() } }

    override fun observeAllCheckIns(): Flow<List<FacetCheckIn>> =
        dao.observeAllCheckIns().map { values -> values.map { it.toDomain() } }

    override fun observeCheckIns(date: LocalDate): Flow<List<FacetCheckIn>> =
        dao.observeCheckInsByDate(date.toEpochDay()).map { values -> values.map { it.toDomain() } }

    override suspend fun saveFacet(facet: LifeFacet) = dao.upsertFacet(facet.toEntity())

    override suspend fun checkIn(checkIn: FacetCheckIn) = dao.upsertCheckIn(checkIn.toEntity())

    override suspend fun archiveFacet(id: String) =
        dao.archiveFacet(id, LocalDateTime.now().toEpochMillis())

    override suspend fun removeCheckIn(facetId: String, date: LocalDate) =
        dao.deleteCheckIn(facetId, date.toEpochDay())

    private fun LifeFacetEntity.toDomain() = LifeFacet(
        id, name, icon, color, options, isArchived,
        createdAt = createdAt.toLocalDateTime(), updatedAt = updatedAt.toLocalDateTime()
    )

    private fun LifeFacet.toEntity() = LifeFacetEntity(
        id, name, icon, color, options, isArchived,
        createdAt = createdAt.toEpochMillis(), updatedAt = updatedAt.toEpochMillis()
    )

    private fun FacetCheckInEntity.toDomain() = FacetCheckIn(
        id = id,
        facetId = facetId,
        option = option,
        date = LocalDate.ofEpochDay(date),
        createdAt = createdAt.toLocalDateTime()
    )

    private fun FacetCheckIn.toEntity() = FacetCheckInEntity(
        id = id,
        facetId = facetId,
        option = option,
        date = date.toEpochDay(),
        createdAt = createdAt.toEpochMillis()
    )

    private fun LocalDateTime.toEpochMillis(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
