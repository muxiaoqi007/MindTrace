package com.mindtrace.diary.domain.model

import java.util.UUID

/**
 * 块级内容模型，用于图文混排编辑器。
 * 每个 ContentBlock 是一个独立的内容单元，可以是文字或图片。
 */
sealed class ContentBlock {
    abstract val id: String

    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        val text: String = ""
    ) : ContentBlock()

    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        val path: String,
        val caption: String = ""
    ) : ContentBlock()
}

/**
 * 从内容块列表中提取纯文本（拼接所有文字块）
 */
fun List<ContentBlock>.toPlainText(): String {
    return filterIsInstance<ContentBlock.Text>()
        .joinToString("\n") { it.text }
}

/**
 * 从内容块列表中提取所有图片路径
 */
fun List<ContentBlock>.toImagePaths(): List<String> {
    return filterIsInstance<ContentBlock.Image>()
        .map { it.path }
}
