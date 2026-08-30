package com.mindtrace.diary.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindtrace.diary.core.database.entity.LexiconEntryEntity
import com.mindtrace.diary.core.database.entity.LexiconEvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LexiconDao {
    @Query("SELECT * FROM lexicon_entries ORDER BY updatedAt DESC") suspend fun getAllEntriesOnce(): List<LexiconEntryEntity>
    @Query("SELECT * FROM lexicon_evidence ORDER BY date ASC") suspend fun getAllEvidenceOnce(): List<LexiconEvidenceEntity>
    @Query("SELECT * FROM lexicon_entries ORDER BY type, updatedAt DESC") fun observeEntries(): Flow<List<LexiconEntryEntity>>
    @Query("SELECT * FROM lexicon_evidence ORDER BY date DESC") fun observeEvidence(): Flow<List<LexiconEvidenceEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertEntry(value: LexiconEntryEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertEvidence(values: List<LexiconEvidenceEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertEntries(values: List<LexiconEntryEntity>)
    @Query("UPDATE lexicon_entries SET status = :status, updatedAt = :updatedAt WHERE id = :id") suspend fun updateStatus(id: String, status: String, updatedAt: Long)
    @Query("UPDATE lexicon_entries SET correctedMeaning = :meaning, status = 'CONFIRMED', updatedAt = :updatedAt WHERE id = :id") suspend fun correct(id: String, meaning: String?, updatedAt: Long)
}
