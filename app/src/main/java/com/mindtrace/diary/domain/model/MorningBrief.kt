package com.mindtrace.diary.domain.model

import java.time.LocalDate

/**
 * 晨间简报：每天早上主动递给用户的一份"今日开场"。
 * 数据部分（连续记录/待办/历史上的今天）本地拼装，永远可用；
 * 文字部分（问候/观察/提问）由 AI 生成，AI 不可用时降级为本地模板。
 */
data class MorningBrief(
    val forDate: LocalDate,
    val greeting: String,
    /** 基于昨日日记的一句观察；无昨日日记或纯本地降级时为 null */
    val observation: String?,
    /** 引导提问，点击可带去和 AI 聊天；降级时为 null */
    val question: String?,
    val streakDays: Int,
    val pendingTodoCount: Int,
    /** 未完成待办预览（最多 3 条，截断后的内容） */
    val pendingTodos: List<String>,
    /** 历史上的今天距今年数；无则 null */
    val memoryYearsAgo: Int?,
    val memoryExcerpt: String?,
    val unreadReviewCount: Int,
    /** true 表示文字部分为本地模板（未调用或未成功调用 AI） */
    val isLocal: Boolean
)
