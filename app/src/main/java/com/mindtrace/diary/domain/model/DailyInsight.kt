package com.mindtrace.diary.domain.model

import java.time.LocalDate

/**
 * 首页"今日洞察"：AI 基于近期日记生成的一条观察 + 一个引导提问。
 * 每天最多生成一次，结果缓存到 DataStore。
 */
data class DailyInsight(
    val observation: String,
    val question: String,
    val forDate: LocalDate
)
