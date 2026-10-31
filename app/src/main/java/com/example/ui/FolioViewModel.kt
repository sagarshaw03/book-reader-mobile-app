package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookmarkEntity
import com.example.data.BookPageContent
import com.example.data.ChapterEntity
import com.example.data.FolioDatabase
import com.example.data.FolioEntity
import com.example.data.FolioRepository
import com.example.data.RibbonPreset
import com.example.data.SampleContentGenerator
import com.example.data.TurnMode3D
import com.example.ui.theme.PaperTheme
import com.example.ui.theme.ReaderFontChoice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class LibraryTab {
    ALL,
    PDFS,
    BOOKS,
    BOOKMARKS
}

@OptIn(ExperimentalCoroutinesApi::class)
class FolioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FolioDatabase.getDatabase(application)
    val repository = FolioRepository(application, database.folioDao())

    private val _selectedFolioId = MutableStateFlow<String?>(null)
    val selectedFolioId: StateFlow<String?> = _selectedFolioId.asStateFlow()

    private val _libraryTab = MutableStateFlow(LibraryTab.ALL)
    val libraryTab: StateFlow<LibraryTab> = _libraryTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Reader Studio Preferences
    private val _turnMode = MutableStateFlow(TurnMode3D.CURL_3D)
    val turnMode: StateFlow<TurnMode3D> = _turnMode.asStateFlow()

    private val _paperTheme = MutableStateFlow(PaperTheme.PARCHMENT)
    val paperTheme: StateFlow<PaperTheme> = _paperTheme.asStateFlow()

    private val _fontChoice = MutableStateFlow(ReaderFontChoice.LORA)
    val fontChoice: StateFlow<ReaderFontChoice> = _fontChoice.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    // Rendered PDF page bitmaps for the active PDF folio
    private val _renderedPdfBitmaps = MutableStateFlow<Map<Int, Bitmap>>(emptyMap())
    val renderedPdfBitmaps: StateFlow<Map<Int, Bitmap>> = _renderedPdfBitmaps.asStateFlow()

    val allFolios: StateFlow<List<FolioEntity>> = repository.allFolios.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredFolios: StateFlow<List<FolioEntity>> = combine(
        allFolios,
        _libraryTab,
        _searchQuery
    ) { folios, tab, query ->
        val byTab = when (tab) {
            LibraryTab.ALL -> folios
            LibraryTab.PDFS -> folios.filter { it.isPdf }
            LibraryTab.BOOKS -> folios.filter { !it.isPdf }
            LibraryTab.BOOKMARKS -> folios
        }
        if (query.isBlank()) {
            byTab
        } else {
            val q = query.trim().lowercase()
            byTab.filter {
                it.title.lowercase().contains(q) ||
                    it.author.lowercase().contains(q) ||
                    it.subtitle.lowercase().contains(q)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeFolio: StateFlow<FolioEntity?> = _selectedFolioId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeFolioById(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val activeChapters: StateFlow<List<ChapterEntity>> = _selectedFolioId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getChaptersForFolio(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeBookmarks: StateFlow<List<BookmarkEntity>> = _selectedFolioId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getBookmarksForFolio(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun selectLibraryTab(tab: LibraryTab) {
        _libraryTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openFolio(folioId: String, jumpToPage: Int? = null) {
        _renderedPdfBitmaps.value = emptyMap()
        _selectedFolioId.value = folioId
        viewModelScope.launch {
            if (jumpToPage != null) {
                repository.updateProgress(folioId, jumpToPage)
            }
        }
    }

    fun closeReader() {
        _selectedFolioId.value = null
    }

    fun updateCurrentPage(pageIndex: Int) {
        val folio = activeFolio.value ?: return
        val clamped = pageIndex.coerceIn(0, (folio.totalPages - 1).coerceAtLeast(0))
        viewModelScope.launch {
            repository.updateProgress(folio.id, clamped)
            if (folio.isPdf) {
                preloadPdfNeighborhood(folio, clamped, _paperTheme.value)
            }
        }
    }

    fun requestPdfRenderForCurrentFolio(folio: FolioEntity, centerPage: Int) {
        if (!folio.isPdf) return
        viewModelScope.launch {
            preloadPdfNeighborhood(folio, centerPage, _paperTheme.value)
        }
    }

    private suspend fun preloadPdfNeighborhood(
        folio: FolioEntity,
        centerPage: Int,
        theme: PaperTheme
    ) {
        val file = File(folio.filePathOrKey)
        if (!file.exists()) return
        val pagesToLoad = listOf(centerPage, centerPage + 1, centerPage - 1)
            .filter { it in 0 until folio.totalPages }

        for (pageIdx in pagesToLoad) {
            val bmp = repository.pdfRendererHelper.renderPage(
                file = file,
                pageIndex = pageIdx,
                targetWidthPx = 1080,
                paperTheme = theme
            )
            if (bmp != null) {
                _renderedPdfBitmaps.value = _renderedPdfBitmaps.value + (pageIdx to bmp)
            }
        }
    }

    fun getBookPageContent(folio: FolioEntity, pageIndex: Int): BookPageContent? {
        if (folio.isPdf) return null
        val pages = SampleContentGenerator.getBookPages(folio.filePathOrKey)
        return pages.getOrNull(pageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0)))
    }

    fun currentChapterForPage(pageIndex: Int): ChapterEntity? {
        val chapters = activeChapters.value
        return chapters
            .filter { pageIndex in it.startPage..it.endPage }
            .maxByOrNull { it.level }
            ?: chapters.lastOrNull { it.startPage <= pageIndex }
            ?: chapters.firstOrNull()
    }

    fun quickToggleBookmarkOnCurrentPage() {
        val folio = activeFolio.value ?: return
        val pageIndex = folio.currentPage
        val existing = activeBookmarks.value.find { it.pageIndex == pageIndex }
        viewModelScope.launch {
            if (existing != null) {
                repository.removeBookmarkForPage(folio.id, pageIndex)
            } else {
                val chapter = currentChapterForPage(pageIndex)?.title ?: "Folio Leaf ${pageIndex + 1}"
                val snippet = if (folio.isPdf) {
                    "PDF Plate ${pageIndex + 1} · $chapter"
                } else {
                    val bp = getBookPageContent(folio, pageIndex)
                    bp?.pullQuote ?: bp?.dropCapParagraph?.take(110)?.plus("…") ?: chapter
                }
                repository.saveOrUpdateBookmark(
                    folio = folio,
                    pageIndex = pageIndex,
                    chapterTitle = chapter,
                    snippet = snippet,
                    note = "",
                    ribbonColorHex = RibbonPreset.CRIMSON.colorHex
                )
            }
        }
    }

    fun saveCustomBookmark(
        pageIndex: Int,
        note: String,
        ribbonColorHex: Long
    ) {
        val folio = activeFolio.value ?: return
        val chapter = currentChapterForPage(pageIndex)?.title ?: "Folio Leaf ${pageIndex + 1}"
        val snippet = if (folio.isPdf) {
            "PDF Plate ${pageIndex + 1} · $chapter"
        } else {
            val bp = getBookPageContent(folio, pageIndex)
            bp?.pullQuote ?: bp?.dropCapParagraph?.take(110)?.plus("…") ?: chapter
        }
        viewModelScope.launch {
            repository.saveOrUpdateBookmark(
                folio = folio,
                pageIndex = pageIndex,
                chapterTitle = chapter,
                snippet = snippet,
                note = note,
                ribbonColorHex = ribbonColorHex
            )
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmarkById(bookmark.id)
        }
    }

    fun removeBookmarkOnPage(pageIndex: Int) {
        val folio = activeFolio.value ?: return
        viewModelScope.launch {
            repository.removeBookmarkForPage(folio.id, pageIndex)
        }
    }

    fun addCustomChapterOnCurrentPage(title: String, subtitle: String, isSubSection: Boolean) {
        val folio = activeFolio.value ?: return
        val nextNum = (activeChapters.value.filter { it.level == 0 }.maxOfOrNull { it.chapterNumber } ?: 0) + 1
        viewModelScope.launch {
            repository.addCustomChapter(
                folioId = folio.id,
                chapterNumber = nextNum,
                title = title,
                subtitle = subtitle,
                pageIndex = folio.currentPage,
                isSubSection = isSubSection
            )
        }
    }

    fun importPdfFromUri(uri: Uri) {
        viewModelScope.launch {
            val imported = repository.importExternalPdf(uri)
            if (imported != null) {
                openFolio(imported.id, jumpToPage = 0)
            }
        }
    }

    fun setTurnMode(mode: TurnMode3D) {
        _turnMode.value = mode
    }

    fun setPaperTheme(theme: PaperTheme) {
        _paperTheme.value = theme
        val folio = activeFolio.value
        if (folio != null && folio.isPdf) {
            _renderedPdfBitmaps.value = emptyMap()
            viewModelScope.launch {
                preloadPdfNeighborhood(folio, folio.currentPage, theme)
            }
        }
    }

    fun setFontChoice(choice: ReaderFontChoice) {
        _fontChoice.value = choice
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
    }
}
