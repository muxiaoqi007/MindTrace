package com.mindtrace.diary.domain.repository

import com.mindtrace.diary.domain.model.Storyline
import com.mindtrace.diary.domain.model.StorylineCandidate
import kotlinx.coroutines.flow.Flow

interface StorylineRepository {
    fun observeAll(): Flow<List<Storyline>>
    suspend fun addCandidates(values: List<StorylineCandidate>)
    suspend fun confirm(id: String)
    suspend fun reject(id: String)
    suspend fun archive(id: String)
    suspend fun rename(id: String, name: String)
    suspend fun merge(sourceId: String, targetId: String)
    suspend fun removeSource(sourceId: String)
    suspend fun splitSource(sourceId: String, newName: String)
}
