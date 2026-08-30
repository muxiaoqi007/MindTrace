package com.mindtrace.diary.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindtrace.diary.core.database.entity.FacetCheckInEntity
import com.mindtrace.diary.core.database.entity.LifeFacetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeFacetDao {
    @Query("SELECT * FROM life_facets ORDER BY createdAt ASC")
    suspend fun getAllFacetsOnce(): List<LifeFacetEntity>

    @Query("SELECT * FROM facet_check_ins ORDER BY date ASC")
    suspend fun getAllCheckInsOnce(): List<FacetCheckInEntity>
    @Query("SELECT * FROM life_facets WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun observeActiveFacets(): Flow<List<LifeFacetEntity>>

    @Query("SELECT * FROM facet_check_ins ORDER BY date DESC, createdAt DESC")
    fun observeAllCheckIns(): Flow<List<FacetCheckInEntity>>

    @Query("SELECT * FROM facet_check_ins WHERE date = :date ORDER BY createdAt ASC")
    fun observeCheckInsByDate(date: Long): Flow<List<FacetCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFacet(facet: LifeFacetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: FacetCheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFacets(facets: List<LifeFacetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIns(checkIns: List<FacetCheckInEntity>)

    @Query("UPDATE life_facets SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveFacet(id: String, updatedAt: Long)

    @Query("DELETE FROM facet_check_ins WHERE facetId = :facetId AND date = :date")
    suspend fun deleteCheckIn(facetId: String, date: Long)
}
