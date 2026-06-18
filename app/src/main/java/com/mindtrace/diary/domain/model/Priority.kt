package com.mindtrace.diary.domain.model

enum class Priority(val label: String, val value: Int) {
    HIGH("高优先级", 3),
    MEDIUM("中优先级", 2),
    LOW("低优先级", 1);

    companion object {
        fun fromString(value: String?): Priority {
            return entries.find { it.name == value } ?: MEDIUM
        }
    }
}
