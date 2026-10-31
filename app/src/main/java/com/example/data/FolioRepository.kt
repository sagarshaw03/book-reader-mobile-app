package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.ui.theme.PaperTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FolioRepository(
    private val context: Context,
    private val dao: FolioDao,
    val pdfRendererHelper: PdfPageRendererHelper = PdfPageRendererHelper()
) {
    val allFolios: Flow<List<FolioEntity>> = dao.getAllFolios()
    val allBookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()

    fun observeFolioById(folioId: String): Flow<FolioEntity?> = dao.observeFolioById(folioId)
    fun getChaptersForFolio(folioId: String): Flow<List<ChapterEntity>> = dao.getChaptersForFolio(folioId)
    fun getBookmarksForFolio(folioId: String): Flow<List<BookmarkEntity>> = dao.getBookmarksForFolio(folioId)

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val (archFile, astroFile) = SampleContentGenerator.ensureSamplePdfsGenerated(context)
        if (dao.getFolioCount() > 0) return@withContext

        val now = System.currentTimeMillis()
        val folios = listOf(
            FolioEntity(
                id = "folio_arch_pdf",
                title = "De Architectura: Geometric Domes",
                author = "Marcus Vitruvius & Filippo Brunelleschi",
                subtitle = "Illustrated PDF Folio · 8 Copperplate Vector Leaves",
                format = "PDF",
                coverStyle = "ARCHITECTURE",
                accentHex = 0xFFD94E34,
                filePathOrKey = archFile.absolutePath,
                totalPages = 8,
                currentPage = 0,
                lastOpenedTimestamp = now - 10_000L,
                isFavorite = true
            ),
            FolioEntity(
                id = "folio_botanica_book",
                title = "Hortus Malabaricus: Cloud Forest Flora",
                author = "Dr. Eleanor Vance, FLS",
                subtitle = "Literary Field Edition · Reflowable Serif & Botanical Plates",
                format = "BOOK",
                coverStyle = "BOTANICA",
                accentHex = 0xFF2E7D5B,
                filePathOrKey = "BOOK_BOTANICA",
                totalPages = SampleContentGenerator.getBookPages("BOOK_BOTANICA").size,
                currentPage = 0,
                lastOpenedTimestamp = now - 30_000L,
                isFavorite = true
            ),
            FolioEntity(
                id = "folio_astro_pdf",
                title = "Uranometria: Celestial Mechanics",
                author = "Johannes Hevelius & Tycho Brahe",
                subtitle = "Astronomical PDF Codex · Astrolabes & Orbital Charts",
                format = "PDF",
                coverStyle = "CELESTIAL",
                accentHex = 0xFF3A6EA5,
                filePathOrKey = astroFile.absolutePath,
                totalPages = 6,
                currentPage = 0,
                lastOpenedTimestamp = now - 60_000L,
                isFavorite = false
            ),
            FolioEntity(
                id = "folio_typography_book",
                title = "The Art of the Printed Leaf",
                author = "Aldus Manutius & Francesco Griffo",
                subtitle = "Venetian Bindery Treatise · Typography, Rag Paper & Margins",
                format = "BOOK",
                coverStyle = "TYPOGRAPHY",
                accentHex = 0xFFD49B35,
                filePathOrKey = "BOOK_TYPOGRAPHY",
                totalPages = SampleContentGenerator.getBookPages("BOOK_TYPOGRAPHY").size,
                currentPage = 0,
                lastOpenedTimestamp = now - 90_000L,
                isFavorite = false
            )
        )
        dao.insertFolios(folios)

        // Seed comprehensive hierarchical Chapter Outlines (main chapters + sub-sections)
        val chapters = listOf(
            // De Architectura PDF
            ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 1, title = "I. Harmonic Proportions", subtitle = "The Euclidean Canon & Golden Ratio", startPage = 0, endPage = 1, level = 0),
            ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 1, title = "Modular Intercolumniation", subtitle = "Eustyle Colonnade & Optical Entasis", startPage = 1, endPage = 1, level = 1),
            ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 2, title = "II. Hemispherical Domes", subtitle = "Oculus Compression Rings & Coffering", startPage = 2, endPage = 3, level = 0),
            ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 2, title = "Double-Shell Herringbone Masonry", subtitle = "Brunelleschi's Spinapesce Spiral", startPage = 3, endPage = 3, level = 1),
            ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 3, title = "III. Acoustic Amphitheaters", subtitle = "Harmonic Vessels & Cuneus Geometry", startPage = 4, endPage = 5, level = 0),
            ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 3, title = "Velarium Rigging & Vomitoria", subtitle = "Radial Pier Crowd Circulation", startPage = 5, endPage = 5, level = 1),
            ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 4, title = "IV. Chiaroscuro & Daylight", subtitle = "Clerestory Apertures & Solstice Rays", startPage = 6, endPage = 7, level = 0),
            ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 4, title = "Colophon of the Guild", subtitle = "Iron-Gall Ink & Copperplate Preservation", startPage = 7, endPage = 7, level = 1),

            // Hortus Malabaricus Book
            ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 1, title = "Chapter I · The Cloud Forest Canopy", subtitle = "Epiphytic Orchids & High-Altitude Mist", startPage = 0, endPage = 1, level = 0),
            ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 1, title = "1.2 Bryophyte Micro-Watersheds", subtitle = "Canopy Moss Water Retention", startPage = 1, endPage = 1, level = 1),
            ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 2, title = "Chapter II · Fibonacci Phyllotaxis", subtitle = "The Golden Angle in Rosette Succulents", startPage = 2, endPage = 3, level = 0),
            ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 2, title = "2.2 Fractal Venation", subtitle = "Anastomosing Leaf Hydraulics", startPage = 3, endPage = 3, level = 1),
            ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 3, title = "Chapter III · Nocturnal Pollination", subtitle = "Moonflower Opening Kinematics", startPage = 4, endPage = 5, level = 0),
            ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 3, title = "3.2 Thermogenic Arums", subtitle = "Botanical Furnaces in Frosty Valleys", startPage = 5, endPage = 5, level = 1),
            ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 4, title = "Chapter IV · Pigments & Botanical Inks", subtitle = "Oak Galls, Indigo Vats & Saffron", startPage = 6, endPage = 7, level = 0),
            ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 4, title = "4.2 The Living Herbarium", subtitle = "Archival Preservation & Colophon", startPage = 7, endPage = 7, level = 1),

            // Uranometria PDF
            ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 1, title = "I. The Armillary Sphere", subtitle = "Equinoctial Rings & Ecliptic Obliquity", startPage = 0, endPage = 1, level = 0),
            ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 1, title = "Planispheric Astrolabe Projection", subtitle = "Stereographic Rete & Tympan", startPage = 1, endPage = 1, level = 1),
            ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 2, title = "II. Planetary Epicycles", subtitle = "Retrograde Loops & Synodic Rosettes", startPage = 2, endPage = 3, level = 0),
            ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 2, title = "Keplerian Elliptical Sectors", subtitle = "Equal Areas in Equal Times", startPage = 3, endPage = 3, level = 1),
            ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 3, title = "III. Selenography & Libration", subtitle = "Lunar Terminator Shadow Micrometry", startPage = 4, endPage = 5, level = 0),
            ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 3, title = "Circumpolar Nocturnal Dial", subtitle = "Polaris & Northern Guard Stars", startPage = 5, endPage = 5, level = 1),

            // Venetian Typography Book
            ChapterEntity(folioId = "folio_typography_book", chapterNumber = 1, title = "Chapter I · The Venetian Matrix", subtitle = "Steel Punchcutting & Counterpunches", startPage = 0, endPage = 1, level = 0),
            ChapterEntity(folioId = "folio_typography_book", chapterNumber = 1, title = "1.2 The Portable Octavo", subtitle = "Aldus Manutius & Chancery Italic", startPage = 1, endPage = 1, level = 1),
            ChapterEntity(folioId = "folio_typography_book", chapterNumber = 2, title = "Chapter II · Canons of Page Construction", subtitle = "Villard de Honnecourt & 2:3:4:6 Margins", startPage = 2, endPage = 3, level = 0),
            ChapterEntity(folioId = "folio_typography_book", chapterNumber = 2, title = "2.2 Rag Paper Grain & Chain Lines", subtitle = "Fabriano Laid Moulds & Folding Axis", startPage = 3, endPage = 3, level = 1),
            ChapterEntity(folioId = "folio_typography_book", chapterNumber = 3, title = "Chapter III · The Bindery & Silk Ribbon", subtitle = "Smyth Sewing & Articulated Spine Arch", startPage = 4, endPage = 5, level = 0),
            ChapterEntity(folioId = "folio_typography_book", chapterNumber = 3, title = "3.2 Marginalia & Reader's Dialogue", subtitle = "Manicules, Ribbons & Living Books", startPage = 5, endPage = 5, level = 1)
        )
        dao.insertChapters(chapters)

        // Seed curated starter bookmarks with rich margin notes
        val starterBookmarks = listOf(
            BookmarkEntity(
                folioId = "folio_arch_pdf",
                folioTitle = "De Architectura: Geometric Domes",
                pageIndex = 2,
                chapterTitle = "II. Hemispherical Domes",
                snippet = "PLATE III — Meridional Cross-Section of the Rotunda & Oculus Ring.",
                note = "Compare graded aggregate density: travertine at drum base to volcanic pumice at the oculus compression ring.",
                ribbonColorHex = RibbonPreset.CRIMSON.colorHex,
                createdAt = now - 7200_000L
            ),
            BookmarkEntity(
                folioId = "folio_botanica_book",
                folioTitle = "Hortus Malabaricus: Cloud Forest Flora",
                pageIndex = 2,
                chapterTitle = "Chapter II · Fibonacci Phyllotaxis",
                snippet = "“Botany is geometry rendered in chlorophyll and sunlight.”",
                note = "137.5° golden divergence angle prevents self-shading in alpine rosettes.",
                ribbonColorHex = RibbonPreset.EMERALD.colorHex,
                createdAt = now - 3600_000L
            ),
            BookmarkEntity(
                folioId = "folio_typography_book",
                folioTitle = "The Art of the Printed Leaf",
                pageIndex = 2,
                chapterTitle = "Chapter II · Canons of Page Construction",
                snippet = "“Margins are the silent architecture that allows the voice of the text to resonate.”",
                note = "Villard's diagram gives the classic 2 : 3 : 4 : 6 inner-top-outer-bottom margin ratio.",
                ribbonColorHex = RibbonPreset.BRASS.colorHex,
                createdAt = now - 1800_000L
            )
        )
        dao.insertBookmarks(starterBookmarks)
    }

    suspend fun importExternalPdf(uri: Uri): FolioEntity? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var displayName = "Imported Folio.pdf"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex) ?: displayName
                }
            }

            val cleanTitle = displayName.removeSuffix(".pdf").removeSuffix(".PDF")
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .ifBlank { "Imported PDF Folio" }

            val importDir = File(context.filesDir, "imported_pdfs").apply { mkdirs() }
            val destFile = File(importDir, "pdf_${UUID.randomUUID().toString().take(8)}.pdf")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            val pageCount = pdfRendererHelper.getPdfPageCount(destFile).coerceAtLeast(1)
            val folioId = "imported_${UUID.randomUUID().toString().take(8)}"

            val newFolio = FolioEntity(
                id = folioId,
                title = cleanTitle,
                author = "Imported PDF Document",
                subtitle = "Local PDF Folio · $pageCount ${if (pageCount == 1) "Page" else "Pages"}",
                format = "PDF",
                coverStyle = "IMPORTED",
                accentHex = 0xFFD94E34,
                filePathOrKey = destFile.absolutePath,
                totalPages = pageCount,
                currentPage = 0,
                lastOpenedTimestamp = System.currentTimeMillis(),
                isFavorite = false
            )
            dao.insertFolio(newFolio)

            // Generate smart default chapter outline sections for the imported PDF
            val generatedChapters = mutableListOf<ChapterEntity>()
            if (pageCount <= 4) {
                for (p in 0 until pageCount) {
                    generatedChapters.add(
                        ChapterEntity(
                            folioId = folioId,
                            chapterNumber = p + 1,
                            title = "Section ${p + 1} · Folio Leaf ${p + 1}",
                            subtitle = "Page ${p + 1} of $pageCount",
                            startPage = p,
                            endPage = p,
                            level = 0
                        )
                    )
                }
            } else {
                val chunk = (pageCount / 4).coerceAtLeast(2)
                var chapterNum = 1
                var start = 0
                while (start < pageCount) {
                    val end = (start + chunk - 1).coerceAtMost(pageCount - 1)
                    generatedChapters.add(
                        ChapterEntity(
                            folioId = folioId,
                            chapterNumber = chapterNum,
                            title = "Part $chapterNum · Pages ${start + 1}–${end + 1}",
                            subtitle = "Imported PDF Section ($chunk leaves)",
                            startPage = start,
                            endPage = end,
                            level = 0
                        )
                    )
                    start = end + 1
                    chapterNum++
                }
            }
            dao.insertChapters(generatedChapters)
            newFolio
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProgress(folioId: String, pageIndex: Int) {
        dao.updateReadingProgress(folioId, pageIndex)
    }

    suspend fun toggleFavorite(folio: FolioEntity) {
        dao.updateFavorite(folio.id, !folio.isFavorite)
    }

    suspend fun saveOrUpdateBookmark(
        folio: FolioEntity,
        pageIndex: Int,
        chapterTitle: String,
        snippet: String,
        note: String,
        ribbonColorHex: Long
    ) {
        val existing = dao.getBookmarkForPage(folio.id, pageIndex)
        if (existing != null) {
            dao.insertBookmark(
                existing.copy(
                    note = note,
                    ribbonColorHex = ribbonColorHex,
                    chapterTitle = chapterTitle
                )
            )
        } else {
            dao.insertBookmark(
                BookmarkEntity(
                    folioId = folio.id,
                    folioTitle = folio.title,
                    pageIndex = pageIndex,
                    chapterTitle = chapterTitle,
                    snippet = snippet,
                    note = note,
                    ribbonColorHex = ribbonColorHex
                )
            )
        }
    }

    suspend fun removeBookmarkForPage(folioId: String, pageIndex: Int) {
        dao.deleteBookmarkForPage(folioId, pageIndex)
    }

    suspend fun deleteBookmarkById(bookmarkId: Long) {
        dao.deleteBookmarkById(bookmarkId)
    }

    suspend fun addCustomChapter(
        folioId: String,
        chapterNumber: Int,
        title: String,
        subtitle: String,
        pageIndex: Int,
        isSubSection: Boolean
    ) {
        dao.insertChapter(
            ChapterEntity(
                folioId = folioId,
                chapterNumber = chapterNumber,
                title = title,
                subtitle = subtitle.ifBlank { "Page ${pageIndex + 1}" },
                startPage = pageIndex,
                endPage = pageIndex,
                level = if (isSubSection) 1 else 0
            )
        )
    }
}
