package com.example.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import com.example.ui.theme.PaperTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class PdfPageRendererHelper {

    private val renderMutex = Mutex()

    // Cache rendered PDF bitmaps keyed by "filePath:pageIndex:width:paperTheme"
    private val bitmapCache = object : LruCache<String, Bitmap>(24 * 1024) { // 24 MB cache
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    suspend fun getPdfPageCount(file: File): Int = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext 0
        renderMutex.withLock {
            try {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        renderer.pageCount
                    }
                }
            } catch (e: Exception) {
                0
            }
        }
    }

    suspend fun renderPage(
        file: File,
        pageIndex: Int,
        targetWidthPx: Int = 1080,
        paperTheme: PaperTheme = PaperTheme.PARCHMENT
    ): Bitmap? = withContext(Dispatchers.IO) {
        val safeWidth = targetWidthPx.coerceIn(320, 1600)
        val cacheKey = "${file.absolutePath}:$pageIndex:$safeWidth:${paperTheme.name}"

        bitmapCache.get(cacheKey)?.let { cached ->
            if (!cached.isRecycled) return@withContext cached
        }

        renderMutex.withLock {
            bitmapCache.get(cacheKey)?.let { cached ->
                if (!cached.isRecycled) return@withLock cached
            }

            val rawBitmap = if (file.exists()) {
                try {
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withLock null
                            renderer.openPage(pageIndex).use { page ->
                                val aspect = page.height.toFloat() / page.width.toFloat().coerceAtLeast(1f)
                                val width = safeWidth
                                val height = (width * aspect).toInt().coerceAtLeast(400)

                                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bmp)
                                canvas.drawColor(android.graphics.Color.WHITE)
                                page.render(
                                    bmp,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                                bmp
                            }
                        }
                    }
                } catch (e: Throwable) {
                    renderBuiltInSamplePageBitmap(file, pageIndex)
                }
            } else {
                renderBuiltInSamplePageBitmap(file, pageIndex)
            } ?: return@withLock null

            val themedBitmap = applyPaperThemeFilter(rawBitmap, paperTheme)
            if (themedBitmap !== rawBitmap) {
                rawBitmap.recycle()
            }
            bitmapCache.put(cacheKey, themedBitmap)
            themedBitmap
        }
    }

    private fun renderBuiltInSamplePageBitmap(file: File, pageIndex: Int): Bitmap? {
        val isCelestial = file.name.contains("uranometria", ignoreCase = true)
        val specs = if (isCelestial) {
            SampleContentGenerator.getCelestialPageSpecs()
        } else {
            SampleContentGenerator.getArchitecturePageSpecs()
        }
        val spec = specs.getOrNull(pageIndex) ?: SampleContentGenerator.PdfPageSpec(
            chapterNum = (pageIndex / 2) + 1,
            chapterTitle = "Plate ${pageIndex + 1}",
            sectionTitle = "Folio Plate Specification",
            leadParagraph = "Detailed architectural and geometric folio plate for leaf ${pageIndex + 1}.",
            secondParagraph = "Harmonic proportioning and archival plate layout for reading and inspection.",
            calloutNote = "Plate ${pageIndex + 1} · Codex Edition",
            diagramKind = pageIndex % 4
        )
        val pageWidth = 720
        val pageHeight = 1020
        val bmp = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        SampleContentGenerator.drawSpecPageOnCanvas(
            canvas = canvas,
            pageWidth = pageWidth,
            pageHeight = pageHeight,
            folioHeader = if (isCelestial) "URANOMETRIA · CELESTIAL CODEX" else "DE ARCHITECTURA · FOLIO EDITION",
            primaryHex = if (isCelestial) android.graphics.Color.rgb(34, 72, 128) else android.graphics.Color.rgb(154, 52, 34),
            secondaryHex = if (isCelestial) android.graphics.Color.rgb(186, 134, 44) else android.graphics.Color.rgb(180, 128, 48),
            index = pageIndex,
            totalPages = (specs.size).coerceAtLeast(pageIndex + 1),
            spec = spec
        )
        return bmp
    }

    private fun applyPaperThemeFilter(source: Bitmap, paperTheme: PaperTheme): Bitmap {
        if (paperTheme == PaperTheme.IVORY) return source

        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val matrix = when (paperTheme) {
            PaperTheme.PARCHMENT -> ColorMatrix(
                floatArrayOf(
                    0.97f, 0.02f, 0.0f, 0f, 0f,
                    0.0f, 0.94f, 0.01f, 0f, 0f,
                    0.0f, 0.0f, 0.88f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PaperTheme.SAGE -> ColorMatrix(
                floatArrayOf(
                    0.93f, 0.02f, 0.0f, 0f, 0f,
                    0.01f, 0.96f, 0.01f, 0f, 0f,
                    0.0f, 0.02f, 0.92f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PaperTheme.MIDNIGHT -> ColorMatrix(
                floatArrayOf(
                    -0.84f, 0f, 0f, 0f, 238f,
                    0f, -0.82f, 0f, 0f, 226f,
                    0f, 0f, -0.78f, 0f, 211f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PaperTheme.IVORY -> ColorMatrix()
        }

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }
}
