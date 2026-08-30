package com.mindtrace.diary.domain.model

data class PrintArchiveContent(
    val config: PrintArchiveConfig,
    val diaries: List<Diary> = emptyList(),
    val receipts: List<DailyReceipt> = emptyList(),
    val magazines: List<WeeklyMagazine> = emptyList()
)
