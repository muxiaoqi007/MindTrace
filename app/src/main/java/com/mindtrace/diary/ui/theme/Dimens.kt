package com.mindtrace.diary.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 全局间距与尺寸 token。
 * 屏幕布局一律从这里取值，保证节奏一致：
 * - xs/sm 用于图标与文字之间的微间距
 * - md/lg 是卡片与列表的默认内边距
 * - xl/xxl 用于分区之间的大间隔
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
