package com.mindtrace.diary.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "lexicon_entries", indices = [Index(value = ["type", "normalizedTerm"], unique = true)])
data class LexiconEntryEntity(
    @PrimaryKey val id: String,
    val term: String,
    val normalizedTerm: String,
    val type: String,
    val generatedMeaning: String,
    val correctedMeaning: String?,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "lexicon_evidence", indices = [Index(value = ["entryId", "diaryId"], unique = true)])
data class LexiconEvidenceEntity(
    @PrimaryKey val id: String,
    val entryId: String,
    val diaryId: String,
    val date: Long,
    val excerpt: String
)
