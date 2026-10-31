package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FolioViewModel
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.theme.Folio3DTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Folio3DTheme {
                Folio3DApp()
            }
        }
    }
}

@Composable
fun Folio3DApp(
    viewModel: FolioViewModel = viewModel()
) {
    val context = LocalContext.current
    val folios by viewModel.filteredFolios.collectAsStateWithLifecycle()
    val allBookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
    val selectedTab by viewModel.libraryTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFolio by viewModel.activeFolio.collectAsStateWithLifecycle()
    val activeChapters by viewModel.activeChapters.collectAsStateWithLifecycle()
    val activeBookmarks by viewModel.activeBookmarks.collectAsStateWithLifecycle()
    val renderedPdfBitmaps by viewModel.renderedPdfBitmaps.collectAsStateWithLifecycle()
    val turnMode by viewModel.turnMode.collectAsStateWithLifecycle()
    val paperTheme by viewModel.paperTheme.collectAsStateWithLifecycle()
    val fontChoice by viewModel.fontChoice.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Fallback safely: FolioRepository copies the PDF stream into internal storage
            }
            viewModel.importPdfFromUri(uri)
        }
    }

    Crossfade(
        targetState = activeFolio,
        label = "ScreenCrossfade"
    ) { currentFolio ->
        if (currentFolio == null) {
            LibraryScreen(
                folios = folios,
                allBookmarks = allBookmarks,
                selectedTab = selectedTab,
                searchQuery = searchQuery,
                onSelectTab = viewModel::selectLibraryTab,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onOpenFolio = viewModel::openFolio,
                onDeleteBookmark = viewModel::removeBookmark,
                onImportPdfClick = {
                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                }
            )
        } else {
            ReaderScreen(
                folio = currentFolio,
                chapters = activeChapters,
                bookmarks = activeBookmarks,
                renderedPdfBitmaps = renderedPdfBitmaps,
                turnMode = turnMode,
                paperTheme = paperTheme,
                fontChoice = fontChoice,
                fontScale = fontScale,
                getBookPage = { pageIdx -> viewModel.getBookPageContent(currentFolio, pageIdx) },
                onRequestPdfRender = { pageIdx ->
                    viewModel.requestPdfRenderForCurrentFolio(currentFolio, pageIdx)
                },
                onPageChanged = viewModel::updateCurrentPage,
                onQuickToggleBookmark = viewModel::quickToggleBookmarkOnCurrentPage,
                onSaveBookmarkWithNote = viewModel::saveCustomBookmark,
                onRemoveBookmarkOnPage = viewModel::removeBookmarkOnPage,
                onDeleteBookmark = viewModel::removeBookmark,
                onAddCustomChapter = viewModel::addCustomChapterOnCurrentPage,
                onUpdateChapter = viewModel::updateChapter,
                onDeleteChapter = viewModel::deleteChapter,
                onApplyCustomToc = viewModel::applyCustomTableOfContents,
                onScanAndExtractPdfOutline = viewModel::scanAndExtractPdfOutline,
                onRegenerateOutline = viewModel::ensureChaptersForActiveFolio,
                onSelectTurnMode = viewModel::setTurnMode,
                onSelectPaperTheme = viewModel::setPaperTheme,
                onSelectFontChoice = viewModel::setFontChoice,
                onFontScaleChange = viewModel::setFontScale,
                onBackToLibrary = viewModel::closeReader
            )
        }
    }
}
