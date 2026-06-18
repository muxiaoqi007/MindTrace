package com.mindtrace.diary.domain.model

/**
 * 旧版心情类型枚举
 * @deprecated 请使用 [MoodLevel] 替代，该类保留仅用于数据迁移兼容
 */
@Deprecated(
    message = "使用 MoodLevel 替代",
    replaceWith = ReplaceWith("MoodLevel", "com.mindtrace.diary.domain.model.MoodLevel")
)
enum class MoodType(val emoji: String, val label: String) {
    VERY_HAPPY("😄", "非常开心"),
    HAPPY("😊", "开心"),
    NEUTRAL("😐", "一般"),
    SAD("😢", "难过"),
    VERY_SAD("😭", "非常难过"),
    ANGRY("😠", "生气"),
    ANXIOUS("😰", "焦虑"),
    EXCITED("🤩", "兴奋");

    /**
     * 转换为新的 MoodLevel
     */
    fun toMoodLevel(): MoodLevel = when (this) {
        VERY_HAPPY, EXCITED -> MoodLevel.GREAT
        HAPPY -> MoodLevel.GOOD
        NEUTRAL -> MoodLevel.OKAY
        SAD, ANXIOUS -> MoodLevel.BAD
        VERY_SAD, ANGRY -> MoodLevel.AWFUL
    }

    companion object {
        fun fromString(value: String?): MoodType? {
            return entries.find { it.name == value }
        }
    }
}
