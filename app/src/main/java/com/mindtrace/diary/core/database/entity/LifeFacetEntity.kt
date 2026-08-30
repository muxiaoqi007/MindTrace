package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "life_facets")
data class LifeFacetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val color: Long,
    val options: List<String>,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "facet_check_ins",
    indices = [Index(value = ["facetId", "date"], unique = true)]
)
data class FacetCheckInEntity(
    @PrimaryKey val id: String,
    val facetId: String,
    val option: String,
    val date: Long,
    val createdAt: Long
)
