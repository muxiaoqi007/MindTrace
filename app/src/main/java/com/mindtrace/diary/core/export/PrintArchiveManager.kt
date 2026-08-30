package com.mindtrace.diary.core.export

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.mindtrace.diary.domain.model.PrintArchiveContent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class GeneratedPrintArchive(val file: File, val pageCount: Int)

object PrintArchiveManager {
    fun generate(context: Context, content: PrintArchiveContent): GeneratedPrintArchive {
        val dir = File(context.cacheDir, "print_archives").apply { check(exists() || mkdirs()) }
        val file = File(dir, "MindTrace-archive-${content.config.startDate}-${content.config.endDate}.pdf")
        val pages = PrintArchiveRenderer.render(content, file)
        return GeneratedPrintArchive(file, pages)
    }

    fun shareIntent(context: Context, archive: GeneratedPrintArchive): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archive.file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "MindTrace 打印归档")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun print(context: Context, archive: GeneratedPrintArchive) {
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        manager.print(
            "MindTrace 日记归档",
            PdfFilePrintAdapter(archive),
            PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).setColorMode(PrintAttributes.COLOR_MODE_COLOR).build()
        )
    }
}

private class PdfFilePrintAdapter(private val archive: GeneratedPrintArchive) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?, newAttributes: PrintAttributes?, cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback, extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) { callback.onLayoutCancelled(); return }
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder(archive.file.name)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(archive.pageCount)
                .build(),
            oldAttributes != newAttributes
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>?, destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?, callback: WriteResultCallback
    ) {
        if (destination == null) { callback.onWriteFailed("无法打开打印输出"); return }
        if (cancellationSignal?.isCanceled == true) { callback.onWriteCancelled(); return }
        try {
            val requested = pages.orEmpty()
            val allPages = requested.isEmpty() || requested.any { it == PageRange.ALL_PAGES }
            if (allPages) {
                FileInputStream(archive.file).use { input ->
                    FileOutputStream(destination.fileDescriptor).use { output -> input.copyTo(output) }
                }
            } else {
                writeSelectedPages(requested, destination, cancellationSignal)
            }
            if (cancellationSignal?.isCanceled == true) callback.onWriteCancelled()
            else callback.onWriteFinished(if (allPages) arrayOf(PageRange.ALL_PAGES) else requested)
        } catch (error: Exception) {
            callback.onWriteFailed(error.message ?: "写入打印文档失败")
        }
    }

    private fun writeSelectedPages(
        ranges: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?
    ) {
        val source = ParcelFileDescriptor.open(archive.file, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(source).use { renderer ->
            val output = PdfDocument()
            try {
                var outputNumber = 1
                (0 until renderer.pageCount).filter { index -> ranges.any { index in it.start..it.end } }.forEach { index ->
                    if (cancellationSignal?.isCanceled == true) return@forEach
                    renderer.openPage(index).use { page ->
                        val bitmap = Bitmap.createBitmap(PrintArchiveRenderer.PAGE_WIDTH, PrintArchiveRenderer.PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
                        try {
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                            val target = output.startPage(PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, outputNumber++).create())
                            target.canvas.drawBitmap(bitmap, 0f, 0f, null)
                            output.finishPage(target)
                        } finally { bitmap.recycle() }
                    }
                }
                FileOutputStream(destination.fileDescriptor).use(output::writeTo)
            } finally { output.close() }
        }
    }
}
