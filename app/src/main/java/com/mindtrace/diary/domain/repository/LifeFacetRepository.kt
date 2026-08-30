package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.FacetCheckIn
import com.mindtrace.diary.domain.model.LifeFacet
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LifeFacetRepository {
    fun observeFacets(): Flow<List<LifeFacet>>
    fun observeAllCheckIns(): Flow<List<FacetCheckIn>>
    fun observeCheckIns(date: LocalDate): Flow<List<FacetCheckIn>>
    suspend fun saveFacet(facet: LifeFacet)
    suspend fun checkIn(checkIn: FacetCheckIn)
    suspend fun archiveFacet(id: String)
    suspend fun removeCheckIn(facetId: String, date: LocalDate)
}
