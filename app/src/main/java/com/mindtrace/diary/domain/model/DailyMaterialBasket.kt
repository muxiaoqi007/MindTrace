package com.mindtrace.diary.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class DailyMaterialType(val label: String) {
    DIARY("日记"),
    FLASH_NOTE("闪念"),
    COMPLETED_TODO("已完成"),
    PENDING_TODO("待办")
}

data class DailyMaterial(
    val sourceId: String,
    val type: DailyMaterialType,
    val text: String,
    val title: String? = null,
    val timestamp: LocalDateTime
) {
    val key: String = "${type.name}:$sourceId"
}

data class DailyMaterialBasket(
    val date: LocalDate,
    val items: List<DailyMaterial>
)
