package com.mindtrace.diary.domain.usecase.backup

import com.mindtrace.diary.core.database.dao.DailyMediaPickDao
import com.mindtrace.diary.core.database.dao.LexiconDao
import com.mindtrace.diary.core.database.dao.LifeFacetDao
import com.mindtrace.diary.core.database.dao.StorylineDao
import com.mindtrace.diary.core.database.dao.TimeCapsuleDao
import com.mindtrace.diary.domain.model.ExportData
import javax.inject.Inject

/** Keeps every durable personalization table in the normal backup/restore path. */
class PersonalizationBackupDataSource @Inject constructor(
    private val lifeFacetDao: LifeFacetDao,
    private val timeCapsuleDao: TimeCapsuleDao,
    private val storylineDao: StorylineDao,
    private val lexiconDao: LexiconDao,
    private val dailyMediaPickDao: DailyMediaPickDao
) {
    suspend fun appendTo(base: ExportData): ExportData = base.copy(
        lifeFacets = lifeFacetDao.getAllFacetsOnce(),
        facetCheckIns = lifeFacetDao.getAllCheckInsOnce(),
        timeCapsules = timeCapsuleDao.getAllOnce(),
        storylines = storylineDao.getAllStorylinesOnce(),
        storylineSources = storylineDao.getAllSourcesOnce(),
        lexiconEntries = lexiconDao.getAllEntriesOnce(),
        lexiconEvidence = lexiconDao.getAllEvidenceOnce(),
        dailyMediaPicks = dailyMediaPickDao.getAllOnce()
    )

    suspend fun restore(data: ExportData): Int {
        val facets = data.lifeFacets.orEmpty()
        val checkIns = data.facetCheckIns.orEmpty()
        val capsules = data.timeCapsules.orEmpty()
        val storylines = data.storylines.orEmpty()
        val sources = data.storylineSources.orEmpty()
        val entries = data.lexiconEntries.orEmpty()
        val evidence = data.lexiconEvidence.orEmpty()
        val media = data.dailyMediaPicks.orEmpty()
        if (facets.isNotEmpty()) lifeFacetDao.upsertFacets(facets)
        if (checkIns.isNotEmpty()) lifeFacetDao.upsertCheckIns(checkIns)
        if (capsules.isNotEmpty()) timeCapsuleDao.insertAll(capsules)
        if (storylines.isNotEmpty()) storylineDao.upsertStorylines(storylines)
        if (sources.isNotEmpty()) storylineDao.insertSources(sources)
        if (entries.isNotEmpty()) lexiconDao.upsertEntries(entries)
        if (evidence.isNotEmpty()) lexiconDao.insertEvidence(evidence)
        if (media.isNotEmpty()) dailyMediaPickDao.upsertAll(media)
        return facets.size + checkIns.size + capsules.size + storylines.size + sources.size + entries.size + evidence.size + media.size
    }
}
