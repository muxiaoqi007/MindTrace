package com.mindtrace.diary.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mindtrace.diary.domain.model.MoodIconPack
import com.mindtrace.diary.domain.model.MoodIconPacks

/**
 * CompositionLocal for providing the current MoodIconPack throughout the app
 */
val LocalMoodIconPack = compositionLocalOf<MoodIconPack> { MoodIconPacks.default }

/**
 * Provider composable that wraps content with the current mood icon pack
 */
@Composable
fun ProvideMoodIconPack(
    moodIconPackId: String,
    content: @Composable () -> Unit
) {
    val iconPack = MoodIconPacks.getById(moodIconPackId)
    CompositionLocalProvider(LocalMoodIconPack provides iconPack) {
        content()
    }
}
