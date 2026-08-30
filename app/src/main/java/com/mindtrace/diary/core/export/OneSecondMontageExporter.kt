package com.mindtrace.diary.core.export

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import androidx.annotation.OptIn
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.TextOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.mindtrace.diary.domain.model.DailyMediaPick
import com.mindtrace.diary.domain.model.DailyMediaType
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(UnstableApi::class)
object OneSecondMontageExporter {
    suspend fun export(context: Context, month: YearMonth, picks: List<DailyMediaPick>): File {
        val ordered = picks.filter { YearMonth.from(it.date) == month }.sortedBy(DailyMediaPick::date)
        require(ordered.isNotEmpty()) { "这个月还没有可合成的片段" }
        val items = ordered.map { pick ->
            val media = MediaItem.Builder().setUri(pick.uri).apply {
                if (pick.type == DailyMediaType.IMAGE) setImageDurationMs(1_000)
                else setClippingConfiguration(MediaItem.ClippingConfiguration.Builder().setStartPositionMs(0).setEndPositionMs(1_000).build())
            }.build()
            val overlay = OverlayEffect(listOf(TextOverlay.createStaticTextOverlay(SpannableString(pick.date.format(DateTimeFormatter.ISO_DATE)))))
            EditedMediaItem.Builder(media)
                .setRemoveAudio(pick.type == DailyMediaType.VIDEO)
                .setFrameRate(30)
                .setEffects(Effects(emptyList(), listOf(overlay)))
                .build()
        }
        val sequence = EditedMediaItemSequence.Builder(items).build()
        val composition: Composition = Composition.Builder(sequence).build()
        val dir = File(context.cacheDir, "one_second_life").apply { check(exists() || mkdirs()) }
        val output = File(dir, "MindTrace-one-second-$month.mp4")
        if (output.exists()) check(output.delete()) { "无法替换旧的合成文件" }

        return suspendCancellableCoroutine { continuation ->
            lateinit var transformer: Transformer
            val listener = object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    if (continuation.isActive) continuation.resume(output)
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    if (continuation.isActive) continuation.resumeWithException(exportException)
                }
            }
            transformer = Transformer.Builder(context).addListener(listener).build()
            continuation.invokeOnCancellation { transformer.cancel() }
            transformer.start(composition, output.absolutePath)
        }
    }

    fun shareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
