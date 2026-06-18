package com.mindtrace.diary.domain.model

import androidx.annotation.DrawableRes
import com.mindtrace.diary.R

/**
 * 心情图标包数据类
 * 使用 drawable 资源而非 emoji 字符串
 */
data class MoodIconPack(
    val id: String,
    val name: String,
    val icons: Map<MoodLevel, Int>  // DrawableRes
) {
    /**
     * 获取指定心情等级的图标资源 ID
     */
    @DrawableRes
    fun getIconRes(level: MoodLevel): Int {
        return icons[level] ?: R.drawable.mood_minimalist_okay
    }
}

/**
 * 预置心情图标包
 */
object MoodIconPacks {

    /**
     * Vol.1 极简几何 (Minimalist)
     */
    val minimalist = MoodIconPack(
        id = "minimalist",
        name = "极简几何",
        icons = mapOf(
            MoodLevel.GREAT to R.drawable.mood_minimalist_great,
            MoodLevel.GOOD to R.drawable.mood_minimalist_good,
            MoodLevel.OKAY to R.drawable.mood_minimalist_okay,
            MoodLevel.BAD to R.drawable.mood_minimalist_bad,
            MoodLevel.AWFUL to R.drawable.mood_minimalist_awful
        )
    )

    /**
     * Vol.2 简笔漫感 (Anime Outline)
     */
    val anime = MoodIconPack(
        id = "anime",
        name = "简笔漫感",
        icons = mapOf(
            MoodLevel.GREAT to R.drawable.mood_anime_great,
            MoodLevel.GOOD to R.drawable.mood_anime_good,
            MoodLevel.OKAY to R.drawable.mood_anime_okay,
            MoodLevel.BAD to R.drawable.mood_anime_bad,
            MoodLevel.AWFUL to R.drawable.mood_anime_awful
        )
    )

    /**
     * Vol.3 不定形团团 (Blobs)
     */
    val blob = MoodIconPack(
        id = "blob",
        name = "不定形团团",
        icons = mapOf(
            MoodLevel.GREAT to R.drawable.mood_blob_great,
            MoodLevel.GOOD to R.drawable.mood_blob_good,
            MoodLevel.OKAY to R.drawable.mood_blob_okay,
            MoodLevel.BAD to R.drawable.mood_blob_bad,
            MoodLevel.AWFUL to R.drawable.mood_blob_awful
        )
    )

    /**
     * Vol.4 软萌圆圆 (Soft Round)
     */
    val soft = MoodIconPack(
        id = "soft",
        name = "软萌圆圆",
        icons = mapOf(
            MoodLevel.GREAT to R.drawable.mood_soft_great,
            MoodLevel.GOOD to R.drawable.mood_soft_good,
            MoodLevel.OKAY to R.drawable.mood_soft_okay,
            MoodLevel.BAD to R.drawable.mood_soft_bad,
            MoodLevel.AWFUL to R.drawable.mood_soft_awful
        )
    )

    /**
     * Vol.5 像素风 (Pixel Art)
     */
    val pixel = MoodIconPack(
        id = "pixel",
        name = "像素风",
        icons = mapOf(
            MoodLevel.GREAT to R.drawable.mood_pixel_great,
            MoodLevel.GOOD to R.drawable.mood_pixel_good,
            MoodLevel.OKAY to R.drawable.mood_pixel_okay,
            MoodLevel.BAD to R.drawable.mood_pixel_bad,
            MoodLevel.AWFUL to R.drawable.mood_pixel_awful
        )
    )

    /**
     * 所有可用的图标包列表
     */
    val allPacks: List<MoodIconPack> = listOf(minimalist, anime, blob, soft, pixel)

    /**
     * 默认图标包
     */
    val default: MoodIconPack = minimalist

    /**
     * 根据 ID 获取图标包
     */
    fun getById(id: String?): MoodIconPack {
        if (id == null) return default
        return allPacks.find { it.id == id } ?: default
    }
}
