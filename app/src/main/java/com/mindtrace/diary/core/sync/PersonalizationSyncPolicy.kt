package com.mindtrace.diary.core.sync

import com.mindtrace.diary.domain.model.ExportData

/** Deterministic merge for personalization tables that do not carry sync tombstones. */
object PersonalizationSyncPolicy {
    fun merge(local: ExportData, remote: ExportData): ExportData = ExportData(
        version = maxOf(local.version, remote.version, 3),
        exportTime = maxOf(local.exportTime, remote.exportTime),
        lifeFacets = latest(local.lifeFacets.orEmpty(), remote.lifeFacets.orEmpty(), { it.id }, { it.updatedAt }),
        facetCheckIns = latest(local.facetCheckIns.orEmpty(), remote.facetCheckIns.orEmpty(), { it.id }, { it.createdAt }),
        timeCapsules = latest(local.timeCapsules.orEmpty(), remote.timeCapsules.orEmpty(), { it.id }, { it.openedAt ?: it.createdAt }),
        storylines = latest(local.storylines.orEmpty(), remote.storylines.orEmpty(), { it.id }, { it.updatedAt }),
        storylineSources = latest(local.storylineSources.orEmpty(), remote.storylineSources.orEmpty(), { it.id }, { it.date }),
        lexiconEntries = latest(local.lexiconEntries.orEmpty(), remote.lexiconEntries.orEmpty(), { it.id }, { it.updatedAt }),
        lexiconEvidence = latest(local.lexiconEvidence.orEmpty(), remote.lexiconEvidence.orEmpty(), { it.id }, { it.date }),
        dailyMediaPicks = latest(local.dailyMediaPicks.orEmpty(), remote.dailyMediaPicks.orEmpty(), { it.date.toString() }, { it.createdAt })
    )

    private fun <T> latest(local: List<T>, remote: List<T>, key: (T) -> String, updatedAt: (T) -> Long): List<T> =
        (remote + local).groupBy(key).values.map { values -> values.maxBy(updatedAt) }.sortedBy(key)
}
