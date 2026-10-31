package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "folios")
data class FolioEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val subtitle: String,
    val format: String, // "PDF" or "BOOK"
    val coverStyle: String, // "ARCHITECTURE", "CELESTIAL", "BOTANICA", "TYPOGRAPHY", "IMPORTED"
    val accentHex: Long,
    val filePathOrKey: String,
    val totalPages: Int,
    val currentPage: Int = 0,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    val isPdf: Boolean get() = format == "PDF"
    val progressFraction: Float
        get() = if (totalPages <= 1) 1f else (currentPage + 1).toFloat() / totalPages.toFloat()
}

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["folioId", "pageIndex"])]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folioId: String,
    val folioTitle: String,
    val pageIndex: Int,
    val chapterTitle: String,
    val snippet: String,
    val note: String,
    val ribbonColorHex: Long = 0xFFD94E34,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chapters",
    indices = [Index(value = ["folioId", "startPage"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folioId: String,
    val chapterNumber: Int,
    val title: String,
    val subtitle: String,
    val startPage: Int, // 0-indexed
    val endPage: Int,   // 0-indexed
    val level: Int = 0  // 0 = Main Chapter, 1 = Sub-section
)

enum class TurnMode3D(val title: String, val subtitle: String) {
    CURL_3D(
        title = "3D Page Curl",
        subtitle = "Curling paper fold with translucent backside & crease shadow"
    ),
    HINGE_3D(
        title = "3D Hardcover Hinge",
        subtitle = "Perspective spine rotation with dynamic directional lighting"
    ),
    STACK_3D(
        title = "Parallax Folio Stack",
        subtitle = "Drafting-table depth slide with angled shadow plates"
    )
}

enum class RibbonPreset(val label: String, val colorHex: Long, val color: Color) {
    CRIMSON("Crimson Silk", 0xFFD94E34, Color(0xFFD94E34)),
    BRASS("Antique Gold", 0xFFD49B35, Color(0xFFD49B35)),
    EMERALD("Botanical Green", 0xFF2E7D5B, Color(0xFF2E7D5B)),
    COBALT("Venetian Cobalt", 0xFF3A6EA5, Color(0xFF3A6EA5))
}

enum class PageDiagramType {
    NONE,
    GOLDEN_SPIRAL,
    DOME_SECTION,
    CELESTIAL_ORRERY,
    BOTANICAL_LEAF,
    TYPOGRAPHIC_GRID,
    VENETIAN_COLOPHON
}

data class BookPageContent(
    val pageIndex: Int,
    val chapterNumber: Int,
    val chapterTitle: String,
    val sectionHeading: String?,
    val isChapterStart: Boolean,
    val dropCapParagraph: String,
    val bodyParagraphs: List<String>,
    val pullQuote: String? = null,
    val footnote: String? = null,
    val diagramType: PageDiagramType = PageDiagramType.NONE,
    val diagramCaption: String? = null
)
