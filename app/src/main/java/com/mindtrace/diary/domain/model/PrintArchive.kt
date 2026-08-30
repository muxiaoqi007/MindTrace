package com.mindtrace.diary.domain.model

import java.time.LocalDate

enum class PrintArchiveType(val label: String) {
    JOURNAL("日记合集"), RECEIPTS("每日小票"), WEEKLY_MAGAZINES("每周生活杂志")
}

data class PrintArchiveConfig(
    val type: PrintArchiveType = PrintArchiveType.JOURNAL,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val tag: String? = null,
    val fontScale: Float = 1f,
    val includePhotos: Boolean = true,
    val includeMetadata: Boolean = true,
    val includePrivate: Boolean = false
)

data class PrintArchiveSelection(
    val diaries: List<Diary>,
    val dates: List<LocalDate>,
    val weekAnchors: List<LocalDate>
)

object PrintArchiveSelector {
    fun select(config: PrintArchiveConfig, diaries: List<Diary>): PrintArchiveSelection {
        val selected = diaries.filterNot(Diary::isDeleted)
            .filter { config.includePrivate || !it.excludeFromResurfacing }
            .filter { diary -> (diary.date ?: diary.createdAt.toLocalDate()) in config.startDate..config.endDate }
            .filter { diary -> config.tag.isNullOrBlank() || config.tag in diary.tags || config.tag in diary.aiTags }
            .sortedBy { it.date ?: it.createdAt.toLocalDate() }
        val dates = generateSequence(config.startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(config.endDate) }.toList()
        val weeks = dates.map { it.minusDays((it.dayOfWeek.value - 1).toLong()) }.distinct()
        return PrintArchiveSelection(selected, dates, weeks)
    }

    fun estimatedJournalPages(diaries: List<Diary>, fontScale: Float, includePhotos: Boolean): Int {
        val safeScale = fontScale.coerceIn(.8f, 1.5f)
        return diaries.sumOf { diary ->
            val textUnits = (diary.title.length * 2 + diary.content.length).coerceAtLeast(1)
            val charsPerPage = (900 / safeScale).toInt().coerceAtLeast(300)
            val textPages = (textUnits + charsPerPage - 1) / charsPerPage
            val photoPages = if (includePhotos) (diary.images.size + 2) / 3 else 0
            maxOf(1, textPages + photoPages)
        }
    }
}
