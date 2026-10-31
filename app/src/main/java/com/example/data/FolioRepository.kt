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
    suspend fun getFolioById(folioId: String): FolioEntity? = withContext(Dispatchers.IO) { dao.getFolioById(folioId) }
    fun getChaptersForFolio(folioId: String): Flow<List<ChapterEntity>> = dao.getChaptersForFolio(folioId)
    fun getBookmarksForFolio(folioId: String): Flow<List<BookmarkEntity>> = dao.getBookmarksForFolio(folioId)

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val (archFile, astroFile) = SampleContentGenerator.ensureSamplePdfsGenerated(context)
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

        for (f in folios) {
            val existing = dao.getFolioById(f.id)
            if (existing == null) {
                dao.insertFolio(f)
            }
            if (dao.getChapterCountForFolio(f.id) == 0) {
                dao.insertChapters(getDefaultChaptersForFolio(f.id))
            }
        }

        // Seed curated starter bookmarks if bookmarks table is empty
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
        for (bm in starterBookmarks) {
            if (dao.getBookmarkForPage(bm.folioId, bm.pageIndex) == null) {
                dao.insertBookmark(bm)
            }
        }
    }

    fun getDefaultChaptersForFolio(folioId: String): List<ChapterEntity> {
        return when (folioId) {
            "folio_arch_pdf" -> listOf(
                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 1, title = "I. Harmonic Proportions", subtitle = "The Euclidean Canon & Colonnades", startPage = 0, endPage = 1, level = 0),
                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 1, title = "Plate I · The Euclidean Canon & Golden Ratio", subtitle = "Page 1 · Fibonacci Vault Subdivision", startPage = 0, endPage = 0, level = 1),
                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 1, title = "Plate II · Modular Intercolumniation & Entasis", subtitle = "Page 2 · Eustyle Colonnade & Optical Vectors", startPage = 1, endPage = 1, level = 1),

                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 2, title = "II. Hemispherical Domes", subtitle = "Oculus Rings & Masonry Shells", startPage = 2, endPage = 3, level = 0),
                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 2, title = "Plate III · Oculus Compression & Rotunda", subtitle = "Page 3 · Concentric Coffering & Pumice Shell", startPage = 2, endPage = 2, level = 1),
                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 2, title = "Plate IV · Double-Shell Herringbone Masonry", subtitle = "Page 4 · Brunelleschi's Spinapesce Spiral", startPage = 3, endPage = 3, level = 1),

                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 3, title = "III. Acoustic Amphitheaters", subtitle = "Cavea Geometry & Air Circulation", startPage = 4, endPage = 5, level = 0),
                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 3, title = "Plate V · Acoustic Vessels & Cuneus Sightlines", subtitle = "Page 5 · Radial Cavea Wavefront Projection", startPage = 4, endPage = 4, level = 1),
                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 3, title = "Plate VI · Velarium Rigging & Vomitoria", subtitle = "Page 6 · Radial Pier Distribution & Crowd Flow", startPage = 5, endPage = 5, level = 1),

                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 4, title = "IV. Chiaroscuro & Archival Colophon", subtitle = "Daylight Apertures & Iron-Gall Preservation", startPage = 6, endPage = 7, level = 0),
                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 4, title = "Plate VII · Clerestory Rays & Solstice Alignment", subtitle = "Page 7 · Meridian Choir Threshold Alignment", startPage = 6, endPage = 6, level = 1),
                ChapterEntity(folioId = "folio_arch_pdf", chapterNumber = 4, title = "Plate VIII · Colophon & Guild Master Seal", subtitle = "Page 8 · Copperplate Archival Inks", startPage = 7, endPage = 7, level = 1)
            )
            "folio_botanica_book" -> listOf(
                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 1, title = "Chapter I · The Cloud Forest Canopy", subtitle = "High-Altitude Mist & Bryophyte Sponges", startPage = 0, endPage = 1, level = 0),
                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 1, title = "1.1 Epiphytic Orchids & High-Altitude Mist", subtitle = "Page 1 · Velamen Roots & Hawk-Moth Spurs", startPage = 0, endPage = 0, level = 1),
                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 1, title = "1.2 Bryophyte Micro-Watersheds", subtitle = "Page 2 · Canopy Moss Water Retention", startPage = 1, endPage = 1, level = 1),

                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 2, title = "Chapter II · Fibonacci Phyllotaxis", subtitle = "Rosette Geometry & Venation Hydraulics", startPage = 2, endPage = 3, level = 0),
                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 2, title = "2.1 The Golden Angle in Rosette Succulents", subtitle = "Page 3 · 137.5° Divergence & Parastichy", startPage = 2, endPage = 2, level = 1),
                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 2, title = "2.2 Fractal Venation & Hydraulic Redundancy", subtitle = "Page 4 · Anastomosing Areoles & Midrib Lesions", startPage = 3, endPage = 3, level = 1),

                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 3, title = "Chapter III · Nocturnal Pollination", subtitle = "Moonflowers & Crepuscular Furnaces", startPage = 4, endPage = 5, level = 0),
                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 3, title = "3.1 Moonflower Opening Kinematics", subtitle = "Page 5 · Twilight Spectral Shift & Turgor Surge", startPage = 4, endPage = 4, level = 1),
                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 3, title = "3.2 Thermogenic Arums & Botanical Furnaces", subtitle = "Page 6 · Alternative Oxidase & Metabolic Warmth", startPage = 5, endPage = 5, level = 1),

                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 4, title = "Chapter IV · Pigments & Botanical Inks", subtitle = "Oak Galls, Indigos & The Living Herbarium", startPage = 6, endPage = 7, level = 0),
                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 4, title = "4.1 Oak Galls, Indigo Vats & Saffron Glazes", subtitle = "Page 7 · Permanent Inks & Acacia Gum", startPage = 6, endPage = 6, level = 1),
                ChapterEntity(folioId = "folio_botanica_book", chapterNumber = 4, title = "4.2 The Living Herbarium (Epilogue)", subtitle = "Page 8 · Archival Sizing & Florentine Clasps", startPage = 7, endPage = 7, level = 1)
            )
            "folio_astro_pdf" -> listOf(
                ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 1, title = "I. The Armillary Sphere", subtitle = "Equinoctial Brass Rings & Stereographic Tympans", startPage = 0, endPage = 1, level = 0),
                ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 1, title = "Chart I · Brass Armillary Rings & Ecliptic Vector", subtitle = "Page 1 · Vernier Scales & Pinnule Sightings", startPage = 0, endPage = 0, level = 1),
                ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 1, title = "Chart II · Stereographic Astrolabe Projection", subtitle = "Page 2 · Almucantars & Pierced Rete Pointers", startPage = 1, endPage = 1, level = 1),

                ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 2, title = "II. Planetary Epicycles & Orbits", subtitle = "Retrograde Rosettes & Keplerian Ellipses", startPage = 2, endPage = 3, level = 0),
                ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 2, title = "Chart III · Synodic Retrograde Rosettes", subtitle = "Page 3 · Orbital Loops at Opposition", startPage = 2, endPage = 2, level = 1),
                ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 2, title = "Chart IV · Keplerian Elliptical Focus Areas", subtitle = "Page 4 · Equal Areas in Equal Times", startPage = 3, endPage = 3, level = 1),

                ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 3, title = "III. Selenography & Night Sky", subtitle = "Lunar Shadow Micrometry & Circumpolar Dials", startPage = 4, endPage = 5, level = 0),
                ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 3, title = "Chart V · Lunar Terminator Shadow Micrometry", subtitle = "Page 5 · Crater Ramparts & Optical Libration", startPage = 4, endPage = 4, level = 1),
                ChapterEntity(folioId = "folio_astro_pdf", chapterNumber = 3, title = "Chart VI · Circumpolar Nocturnal Dial & Clock", subtitle = "Page 6 · Polaris & Northern Guard Stars", startPage = 5, endPage = 5, level = 1)
            )
            "folio_typography_book" -> listOf(
                ChapterEntity(folioId = "folio_typography_book", chapterNumber = 1, title = "Chapter I · The Venetian Matrix", subtitle = "Steel Punchcutting & The Portable Octavo", startPage = 0, endPage = 1, level = 0),
                ChapterEntity(folioId = "folio_typography_book", chapterNumber = 1, title = "1.1 Punchcutting in Steel & Counterpunches", subtitle = "Page 1 · Francesco Griffo & Counter Light", startPage = 0, endPage = 0, level = 1),
                ChapterEntity(folioId = "folio_typography_book", chapterNumber = 1, title = "1.2 The Invention of the Portable Octavo", subtitle = "Page 2 · Aldus Manutius & Chancery Italic", startPage = 1, endPage = 1, level = 1),

                ChapterEntity(folioId = "folio_typography_book", chapterNumber = 2, title = "Chapter II · Canons of Page Construction", subtitle = "Geometric Margins & Laid Rag Paper Grain", startPage = 2, endPage = 3, level = 0),
                ChapterEntity(folioId = "folio_typography_book", chapterNumber = 2, title = "2.1 Villard de Honnecourt & 2:3:4:6 Margins", subtitle = "Page 3 · Van de Graaf Grid & Diagonal Canon", startPage = 2, endPage = 2, level = 1),
                ChapterEntity(folioId = "folio_typography_book", chapterNumber = 2, title = "2.2 Rag Paper Grain & Deckle Edges", subtitle = "Page 4 · Fabriano Chain Lines & Cylindrical Axis", startPage = 3, endPage = 3, level = 1),

                ChapterEntity(folioId = "folio_typography_book", chapterNumber = 3, title = "Chapter III · The Bindery & Silk Ribbon", subtitle = "Smyth Cord Sewing & Marginalia Dialogue", startPage = 4, endPage = 5, level = 0),
                ChapterEntity(folioId = "folio_typography_book", chapterNumber = 3, title = "3.1 Smyth Sewing & Articulated Spine Arch", subtitle = "Page 5 · Five-Cord Signatures & Hollow Back", startPage = 4, endPage = 4, level = 1),
                ChapterEntity(folioId = "folio_typography_book", chapterNumber = 3, title = "3.2 Marginalia & The Reader's Dialogue", subtitle = "Page 6 · Erasmus, Manicules & Silk Markers", startPage = 5, endPage = 5, level = 1)
            )
            else -> emptyList()
        }
    }

    fun generateDefaultChaptersForPageCount(
        folioId: String,
        totalPages: Int,
        bookTitle: String = ""
    ): List<ChapterEntity> {
        val count = totalPages.coerceAtLeast(1)
        val generated = mutableListOf<ChapterEntity>()
        val standardNames = listOf(
            "Chapter 1 · Introduction & Foundations",
            "Chapter 2 · Core Principles & Background",
            "Chapter 3 · Architectural Framework & Theory",
            "Chapter 4 · Detailed Analyses & Plates",
            "Chapter 5 · Empirical Findings & Observations",
            "Chapter 6 · Comparative Synthesis",
            "Chapter 7 · Critical Commentary & Discussion",
            "Chapter 8 · Epilogue, Appendices & Notes"
        )

        if (count <= 4) {
            for (p in 0 until count) {
                generated.add(
                    ChapterEntity(
                        folioId = folioId,
                        chapterNumber = p + 1,
                        title = standardNames.getOrElse(p) { "Chapter ${p + 1}" },
                        subtitle = "Page ${p + 1} of $count",
                        startPage = p,
                        endPage = p,
                        level = 0
                    )
                )
            }
        } else {
            val numChapters = when {
                count in 5..10 -> 4
                count in 11..25 -> 5
                count in 26..60 -> 6
                else -> 8
            }.coerceAtMost(count)

            val pagesPerChapter = (count.toDouble() / numChapters)
            for (c in 0 until numChapters) {
                val start = (c * pagesPerChapter).toInt().coerceIn(0, count - 1)
                val end = if (c == numChapters - 1) {
                    count - 1
                } else {
                    (((c + 1) * pagesPerChapter) - 1).toInt().coerceIn(start, count - 1)
                }
                val title = standardNames.getOrElse(c) { "Chapter ${c + 1}" }
                generated.add(
                    ChapterEntity(
                        folioId = folioId,
                        chapterNumber = c + 1,
                        title = title,
                        subtitle = if (start == end) "Page ${start + 1}" else "Pages ${start + 1}–${end + 1}",
                        startPage = start,
                        endPage = end,
                        level = 0
                    )
                )
            }
        }
        return generated
    }

    suspend fun ensureChaptersForFolio(folioId: String, totalPages: Int) = withContext(Dispatchers.IO) {
        val existingCount = dao.getChapterCountForFolio(folioId)
        if (existingCount > 0) return@withContext

        val defaults = getDefaultChaptersForFolio(folioId)
        if (defaults.isNotEmpty()) {
            dao.insertChapters(defaults)
            return@withContext
        }

        val folio = dao.getFolioById(folioId)
        if (folio != null && folio.isPdf) {
            val file = File(folio.filePathOrKey)
            if (file.exists()) {
                val extracted = PdfOutlineExtractor.extractOutline(file, folioId, totalPages)
                if (extracted.isNotEmpty()) {
                    dao.insertChapters(extracted)
                    return@withContext
                }
            }
        }

        dao.insertChapters(generateDefaultChaptersForPageCount(folioId, totalPages, folio?.title ?: ""))
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

            // Extract real embedded outline from the uploaded book!
            val extractedChapters = PdfOutlineExtractor.extractOutline(destFile, folioId, pageCount)
            if (extractedChapters.isNotEmpty()) {
                dao.insertChapters(extractedChapters)
            } else {
                val defaultChapters = generateDefaultChaptersForPageCount(folioId, pageCount, cleanTitle)
                dao.insertChapters(defaultChapters)
            }
            newFolio
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateChapter(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        dao.updateChapter(chapter)
    }

    suspend fun deleteChapterById(chapterId: Long) = withContext(Dispatchers.IO) {
        dao.deleteChapterById(chapterId)
    }

    suspend fun replaceChaptersForFolio(folioId: String, newChapters: List<ChapterEntity>) = withContext(Dispatchers.IO) {
        dao.deleteChaptersForFolio(folioId)
        dao.insertChapters(newChapters)
    }

    suspend fun applyCustomToc(folioId: String, totalPages: Int, tocText: String) = withContext(Dispatchers.IO) {
        val parsed = PdfOutlineExtractor.parseCustomTocText(tocText, folioId, totalPages)
        if (parsed.isNotEmpty()) {
            dao.deleteChaptersForFolio(folioId)
            dao.insertChapters(parsed)
        }
    }

    suspend fun extractAndApplyPdfOutline(folio: FolioEntity): Boolean = withContext(Dispatchers.IO) {
        if (!folio.isPdf) return@withContext false
        val file = File(folio.filePathOrKey)
        if (!file.exists()) return@withContext false
        val extracted = PdfOutlineExtractor.extractOutline(file, folio.id, folio.totalPages)
        if (extracted.isNotEmpty()) {
            dao.deleteChaptersForFolio(folio.id)
            dao.insertChapters(extracted)
            true
        } else {
            false
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
