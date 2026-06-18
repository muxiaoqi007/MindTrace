package com.mindtrace.diary.domain.model

import androidx.compose.ui.graphics.Color

/**
 * 心情等级枚举 - 5 级心情系统
 * 参考 Daylio 设计
 */
enum class MoodLevel(
    val score: Int,
    val label: String,
    val color: Long
) {
    GREAT(5, "非常好", 0xFF4CAF50),
    GOOD(4, "好", 0xFF8BC34A),
    OKAY(3, "一般", 0xFFFFC107),
    BAD(2, "差", 0xFFFF9800),
    AWFUL(1, "非常差", 0xFFF44336);

    fun getColor(): Color = Color(color)

    companion object {
        fun fromString(value: String?): MoodLevel? {
            if (value == null) return null
            return entries.find { it.name == value }
        }

        fun fromScore(score: Int): MoodLevel? {
            return entries.find { it.score == score }
        }

        /**
         * 从旧的 MoodType 迁移到新的 MoodLevel
         */
        fun fromLegacyMoodType(legacyValue: String?): MoodLevel? {
            if (legacyValue == null) return null
            return when (legacyValue) {
                // 新格式直接解析
                "GREAT" -> GREAT
                "GOOD" -> GOOD
                "OKAY" -> OKAY
                "BAD" -> BAD
                "AWFUL" -> AWFUL
                // 旧格式迁移映射
                "VERY_HAPPY", "EXCITED" -> GREAT
                "HAPPY" -> GOOD
                "NEUTRAL" -> OKAY
                "SAD", "ANXIOUS" -> BAD
                "VERY_SAD", "ANGRY" -> AWFUL
                else -> null
            }
        }
    }
}
