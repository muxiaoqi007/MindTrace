package com.mindtrace.diary.core.sync

import com.mindtrace.diary.core.database.entity.DailyMediaPickEntity
import com.mindtrace.diary.core.database.entity.LifeFacetEntity
import com.mindtrace.diary.domain.model.ExportData
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalizationSyncPolicyTest {
    @Test
    fun `newest mutable record wins and independent records are retained`() {
        val local = ExportData(
            version = 3,
            lifeFacets = listOf(facet("same", "本地新名称", 20), facet("local", "本地", 10)),
            dailyMediaPicks = listOf(media("local-media", 100, 20))
        )
        val remote = ExportData(
            version = 3,
            lifeFacets = listOf(facet("same", "远端旧名称", 10), facet("remote", "远端", 10)),
            dailyMediaPicks = listOf(media("remote-media", 100, 10), media("another-day", 200, 10))
        )

        val merged = PersonalizationSyncPolicy.merge(local, remote)

        assertEquals(setOf("local", "remote", "same"), merged.lifeFacets.map { it.id }.toSet())
        assertEquals("本地新名称", merged.lifeFacets.single { it.id == "same" }.name)
        assertEquals(setOf("local-media", "another-day"), merged.dailyMediaPicks.map { it.id }.toSet())
    }

    private fun facet(id: String, name: String, updatedAt: Long) = LifeFacetEntity(
        id = id, name = name, icon = "●", color = 0, options = listOf("好"),
        isArchived = false, createdAt = 1, updatedAt = updatedAt
    )

    private fun media(id: String, date: Long, createdAt: Long) = DailyMediaPickEntity(
        id = id, date = date, uri = "content://$id", type = "IMAGE", createdAt = createdAt
    )
}
