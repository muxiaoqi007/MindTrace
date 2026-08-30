package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.LexiconCandidate
import com.mindtrace.diary.domain.model.LexiconEntry
import kotlinx.coroutines.flow.Flow

interface LexiconRepository {
    fun observeAll(): Flow<List<LexiconEntry>>
    fun observeConfirmed(): Flow<List<LexiconEntry>>
    suspend fun addCandidates(values: List<LexiconCandidate>)
    suspend fun confirm(id: String)
    suspend fun reject(id: String)
    suspend fun correct(id: String, meaning: String?)
}
