package com.mindtrace.diary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class MapFootprintBuilderTest {
    private val date = LocalDate.of(2026, 8, 10)

    @Test fun onlyIncludesExplicitValidCoordinatesAndSelectedMonth() {
        val values = MapFootprintBuilder.build(
            listOf(diary("valid", 30.2, 120.1), diary("none", null, null), diary("invalid", 130.0, 20.0)),
            YearMonth.of(2026, 8)
        )
        assertEquals(listOf("valid"), values.map { it.diaryId })
    }

    @Test fun excludesPrivateEntriesAndClustersNearbyCoordinates() {
        val values = MapFootprintBuilder.build(listOf(diary("a", 30.2001, 120.1001), diary("b", 30.2002, 120.1002), diary("hidden", 30.2, 120.1, true)))
        val cluster = MapFootprintBuilder.cluster(values).single()
        assertEquals(2, cluster.points.size)
        assertTrue(cluster.name.isNotBlank())
    }

    private fun diary(id: String, lat: Double?, lon: Double?, hidden: Boolean = false) = Diary(
        id = id, title = "", content = "足迹 $id", location = "西湖", latitude = lat, longitude = lon, date = date,
        createdAt = date.atStartOfDay(), updatedAt = date.atStartOfDay(), excludeFromResurfacing = hidden
    )
}
