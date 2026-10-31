package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookmarkEntity
import com.example.data.BookPageContent
import com.example.data.ChapterEntity
import com.example.data.FolioEntity
import com.example.data.TurnMode3D
import com.example.ui.components.BookmarkNoteDialog
import com.example.ui.components.BookmarksSheet
import com.example.ui.components.ChapterOutlineSheet
import com.example.ui.components.PageOverviewGridSheet
import com.example.ui.components.PageTurn3DContainer
import com.example.ui.components.ReaderSettingsSheet
import com.example.ui.components.SingleFolioPageSurface
import com.example.ui.theme.AntiqueBrass
import com.example.ui.theme.CrimsonRibbon
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PaperTheme
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.ReaderFontChoice

@Composable
fun ReaderScreen(
    folio: FolioEntity,
    chapters: List<ChapterEntity>,
    bookmarks: List<BookmarkEntity>,
    renderedPdfBitmaps: Map<Int, Bitmap>,
    turnMode: TurnMode3D,
    paperTheme: PaperTheme,
    fontChoice: ReaderFontChoice,
    fontScale: Float,
    getBookPage: (Int) -> BookPageContent?,
    onRequestPdfRender: (Int) -> Unit,
    onPageChanged: (Int) -> Unit,
    onQuickToggleBookmark: () -> Unit,
    onSaveBookmarkWithNote: (pageIndex: Int, note: String, ribbonColorHex: Long) -> Unit,
    onRemoveBookmarkOnPage: (pageIndex: Int) -> Unit,
    onDeleteBookmark: (BookmarkEntity) -> Unit,
    onAddCustomChapter: (title: String, subtitle: String, isSubSection: Boolean) -> Unit,
    onRegenerateOutline: () -> Unit = {},
    onSelectTurnMode: (TurnMode3D) -> Unit,
    onSelectPaperTheme: (PaperTheme) -> Unit,
    onSelectFontChoice: (ReaderFontChoice) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onBackToLibrary: () -> Unit
) {
    BackHandler(onBack = onBackToLibrary)

    var showChrome by remember { mutableStateOf(true) }
    var showChapterOutlineSheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showPageGridSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var editingBookmarkPage by remember { mutableStateOf<Int?>(null) }

    // Programmatic 3D turn trigger (+1 = animate forward turn, -1 = animate backward turn)
    var externalTurnRequest by remember { mutableIntStateOf(0) }

    LaunchedEffect(folio.id, folio.currentPage, paperTheme) {
        if (folio.isPdf) {
            onRequestPdfRender(folio.currentPage)
        }
    }

    val currentChapter = remember(chapters, folio.currentPage) {
        chapters.filter { folio.currentPage in it.startPage..it.endPage }
            .maxByOrNull { it.level }
            ?: chapters.lastOrNull { it.startPage <= folio.currentPage }
            ?: chapters.firstOrNull()
    }

    val currentBookmark = remember(bookmarks, folio.currentPage) {
        bookmarks.find { it.pageIndex == folio.currentPage }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(paperTheme.tableBackground)
            .testTag("reader_screen_root")
    ) {
        // Main 3D Page-Turning Viewport
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Chrome Bar space
            Spacer(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(if (showChrome) 64.dp else 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                PageTurn3DContainer(
                    currentPage = folio.currentPage,
                    totalPages = folio.totalPages,
                    turnMode = turnMode,
                    paperTheme = paperTheme,
                    externalTurnRequest = externalTurnRequest,
                    onExternalTurnConsumed = { externalTurnRequest = 0 },
                    onPageChanged = onPageChanged,
                    onToggleChrome = { showChrome = !showChrome },
                    pageContent = { pageIdx ->
                        val pageBookmark = bookmarks.find { it.pageIndex == pageIdx }
                        SingleFolioPageSurface(
                            folioTitle = folio.title,
                            pageIndex = pageIdx,
                            totalPages = folio.totalPages,
                            isPdf = folio.isPdf,
                            pdfBitmap = renderedPdfBitmaps[pageIdx],
                            bookPage = getBookPage(pageIdx),
                            bookmark = pageBookmark,
                            paperTheme = paperTheme,
                            readerFontFamily = fontChoice.fontFamily,
                            fontScale = fontScale,
                            onToggleRibbon = onQuickToggleBookmark,
                            onOpenBookmarkNoteDialog = { editingBookmarkPage = pageIdx }
                        )
                    }
                )
            }

            // Bottom Chrome Bar space
            Spacer(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(if (showChrome) 118.dp else 8.dp)
            )
        }

        // Top Translucent Atelier Reader Bar
        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToLibrary,
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Library"
                        )
                    }

                    // Active Chapter & Folio Title Pill (tapping opens Chapter Outline)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showChapterOutlineSheet = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = folio.title,
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentChapter?.title ?: "Leaf ${folio.currentPage + 1}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Chapter Outline Button
                    IconButton(
                        onClick = { showChapterOutlineSheet = true },
                        modifier = Modifier.testTag("open_chapter_outline_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = "Chapter Outline"
                        )
                    }

                    // Place/Edit Bookmark with Margin Note Button
                    IconButton(
                        onClick = { editingBookmarkPage = folio.currentPage },
                        modifier = Modifier.testTag("bookmark_with_note_button")
                    ) {
                        Icon(
                            imageVector = if (currentBookmark != null) Icons.Filled.Bookmark else Icons.Outlined.BookmarkAdd,
                            contentDescription = "Place or Edit Ribbon Bookmark",
                            tint = if (currentBookmark != null) Color(currentBookmark.ribbonColorHex) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // All Bookmarks Drawer Button
                    IconButton(
                        onClick = { showBookmarksSheet = true },
                        modifier = Modifier.testTag("open_bookmarks_sheet_button")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Bookmarks,
                                contentDescription = "Bookmarks & Margin Notes"
                            )
                            if (bookmarks.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CrimsonRibbon)
                                )
                            }
                        }
                    }

                    // Page Overview Grid Button
                    IconButton(
                        onClick = { showPageGridSheet = true },
                        modifier = Modifier.testTag("open_page_grid_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Page Overview"
                        )
                    }

                    // 3D Physics & Typography Settings Button
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("open_reader_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Typography & 3D Physics"
                        )
                    }
                }
            }
        }

        // Bottom Scrubber with Chapter Ticks, Bookmark Dots & 3D Page Turn Controls
        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Chapter & Bookmark Tick Overlay + Page Scrubber
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Draw Chapter Boundary Ticks & Colored Bookmark Dots along the scrubber track
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .height(18.dp)
                        ) {
                            val w = size.width
                            val cy = size.height / 2f
                            val maxIdx = (folio.totalPages - 1).coerceAtLeast(1).toFloat()

                            // Chapter start tick marks
                            chapters.filter { it.level == 0 }.forEach { ch ->
                                val x = (ch.startPage / maxIdx) * w
                                drawLine(
                                    color = AntiqueBrass.copy(alpha = 0.7f),
                                    start = Offset(x, cy - 8.dp.toPx()),
                                    end = Offset(x, cy + 8.dp.toPx()),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }

                            // Bookmark colored ribbon dots
                            bookmarks.forEach { bm ->
                                val x = (bm.pageIndex / maxIdx) * w
                                drawCircle(
                                    color = Color(bm.ribbonColorHex),
                                    radius = 4.5.dp.toPx(),
                                    center = Offset(x, cy - 8.dp.toPx())
                                )
                            }
                        }

                        if (folio.totalPages > 1) {
                            Slider(
                                value = folio.currentPage.toFloat(),
                                onValueChange = { newVal ->
                                    onPageChanged(newVal.toInt())
                                },
                                valueRange = 0f..(folio.totalPages - 1).toFloat(),
                                steps = (folio.totalPages - 2).coerceAtLeast(0),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reader_page_scrubber")
                            )
                        }
                    }

                    // Bottom Control Row: Prev Page | Chapter & Mode Badge | Next Page
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animated 3D Previous Page Turn Button
                        Surface(
                            onClick = {
                                if (folio.currentPage > 0) {
                                    externalTurnRequest = -1
                                }
                            },
                            enabled = folio.currentPage > 0,
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.testTag("prev_page_3d_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Page",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Prev Leaf",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Center Page & 3D Mode Status
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSettingsSheet = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PAGE ${folio.currentPage + 1} OF ${folio.totalPages}",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${turnMode.title} · ${paperTheme.displayName.substringBefore(" ")}",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Animated 3D Next Page Turn Button
                        Surface(
                            onClick = {
                                if (folio.currentPage < folio.totalPages - 1) {
                                    externalTurnRequest = 1
                                }
                            },
                            enabled = folio.currentPage < folio.totalPages - 1,
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("next_page_3d_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Turn Leaf",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Page",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets & Dialogs
    if (showChapterOutlineSheet) {
        ChapterOutlineSheet(
            folioTitle = folio.title,
            currentPage = folio.currentPage,
            totalPages = folio.totalPages,
            chapters = chapters,
            bookmarks = bookmarks,
            onSelectPage = onPageChanged,
            onAddCustomChapter = onAddCustomChapter,
            onRegenerateOutline = onRegenerateOutline,
            onDismiss = { showChapterOutlineSheet = false }
        )
    }

    if (showBookmarksSheet) {
        BookmarksSheet(
            folioTitle = folio.title,
            currentPage = folio.currentPage,
            bookmarks = bookmarks,
            isCurrentPageBookmarked = currentBookmark != null,
            onJumpToPage = onPageChanged,
            onAddOrEditCurrentPageBookmark = { editingBookmarkPage = folio.currentPage },
            onEditBookmark = { bm -> editingBookmarkPage = bm.pageIndex },
            onDeleteBookmark = onDeleteBookmark,
            onDismiss = { showBookmarksSheet = false }
        )
    }

    if (showPageGridSheet) {
        PageOverviewGridSheet(
            folioTitle = folio.title,
            totalPages = folio.totalPages,
            currentPage = folio.currentPage,
            chapters = chapters,
            bookmarks = bookmarks,
            onSelectPage = onPageChanged,
            onDismiss = { showPageGridSheet = false }
        )
    }

    if (showSettingsSheet) {
        ReaderSettingsSheet(
            turnMode = turnMode,
            onSelectTurnMode = onSelectTurnMode,
            paperTheme = paperTheme,
            onSelectPaperTheme = onSelectPaperTheme,
            fontChoice = fontChoice,
            onSelectFontChoice = onSelectFontChoice,
            fontScale = fontScale,
            onFontScaleChange = onFontScaleChange,
            isPdf = folio.isPdf,
            onDismiss = { showSettingsSheet = false }
        )
    }

    editingBookmarkPage?.let { targetPageIdx ->
        val existingBm = bookmarks.find { it.pageIndex == targetPageIdx }
        val chapterName = chapters
            .filter { targetPageIdx in it.startPage..it.endPage }
            .maxByOrNull { it.level }?.title
            ?: "Leaf ${targetPageIdx + 1}"

        BookmarkNoteDialog(
            pageIndex = targetPageIdx,
            chapterTitle = chapterName,
            existingBookmark = existingBm,
            onSave = { note, colorHex ->
                onSaveBookmarkWithNote(targetPageIdx, note, colorHex)
            },
            onRemove = if (existingBm != null) {
                { onRemoveBookmarkOnPage(targetPageIdx) }
            } else null,
            onDismiss = { editingBookmarkPage = null }
        )
    }
}
