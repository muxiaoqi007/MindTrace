package com.mindtrace.diary.domain.model

import java.time.LocalDateTime

/**
 * Represents a timestamped entry within a diary.
 * Can be a flash note or main content.
 */
data class DiaryEntry(
    val id: String,
    val content: String,
    val images: List<String> = emptyList(),
    val timestamp: LocalDateTime,
    val type: EntryType
)

/**
 * Type of diary entry
 */
enum class EntryType {
    FLASH_NOTE,    // Quick flash note entry
    MAIN_CONTENT,  // Main diary content
    REFLECTION;    // User answer to an optional AI follow-up question

    companion object {
        fun fromString(value: String?): EntryType? {
            return entries.find { it.name == value }
        }
    }
}
