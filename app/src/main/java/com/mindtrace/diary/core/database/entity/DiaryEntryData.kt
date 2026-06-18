package com.mindtrace.diary.core.database.entity

/**
 * Database representation of a diary entry.
 * Used for storing entries as JSON in DiaryEntity.
 */
data class DiaryEntryData(
    val id: String,
    val content: String,
    val images: List<String> = emptyList(),
    val timestamp: Long,
    val type: String
)
