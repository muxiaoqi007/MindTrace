package com.mindtrace.diary.core.database.entity

/**
 * 数据库存储用的 ContentBlock 扁平数据类。
 * 通过 Room TypeConverters 序列化为 JSON 存储在 diaries 表中。
 */
data class ContentBlockData(
    val id: String,
    val type: String,       // "TEXT" or "IMAGE"
    val text: String? = null,
    val path: String? = null,
    val caption: String = ""
)
