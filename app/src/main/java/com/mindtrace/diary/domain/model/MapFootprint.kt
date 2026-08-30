package com.mindtrace.diary.domain.model

import java.time.LocalDate
import java.time.YearMonth

data class MapFootprint(
    val diaryId: String,
    val date: LocalDate,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val excerpt: String
)

data class FootprintCluster(val key: String, val name: String, val points: List<MapFootprint>)

object MapFootprintBuilder {
    fun build(diaries: List<Diary>, month: YearMonth? = null): List<MapFootprint> = diaries
        .filterNot { it.isDeleted || it.excludeFromResurfacing }
        .mapNotNull { diary ->
            val lat = diary.latitude ?: return@mapNotNull null
            val lon = diary.longitude ?: return@mapNotNull null
            val date = diary.date ?: diary.createdAt.toLocalDate()
            if (lat !in -90.0..90.0 || lon !in -180.0..180.0 || (month != null && YearMonth.from(date) != month)) return@mapNotNull null
            MapFootprint(diary.id, date, diary.location?.takeIf(String::isNotBlank) ?: "未命名位置", lat, lon, diary.content.replace(Regex("\\s+"), " ").trim().take(100))
        }.sortedBy(MapFootprint::date)

    fun cluster(points: List<MapFootprint>): List<FootprintCluster> = points.groupBy { point ->
        "${(point.latitude * 100).toInt()}:${(point.longitude * 100).toInt()}"
    }.map { (key, values) -> FootprintCluster(key, values.groupingBy(MapFootprint::name).eachCount().maxByOrNull { it.value }?.key ?: "未命名位置", values) }
        .sortedByDescending { it.points.size }
}
