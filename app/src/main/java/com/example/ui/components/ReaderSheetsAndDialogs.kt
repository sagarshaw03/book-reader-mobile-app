package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookmarkEntity
import com.example.data.ChapterEntity
import com.example.data.RibbonPreset
import com.example.data.TurnMode3D
import com.example.ui.theme.AntiqueBrass
import com.example.ui.theme.CrimsonRibbon
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PaperTheme
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.ReaderFontChoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterOutlineSheet(
    folioTitle: String,
    currentPage: Int,
    totalPages: Int,
    chapters: List<ChapterEntity>,
    bookmarks: List<BookmarkEntity>,
    onSelectPage: (Int) -> Unit,
    onAddCustomChapter: (title: String, subtitle: String, isSubSection: Boolean) -> Unit,
    onUpdateChapter: (ChapterEntity) -> Unit = {},
    onDeleteChapter: (ChapterEntity) -> Unit = {},
    onApplyCustomToc: (String) -> Unit = {},
    onScanAndExtractPdfOutline: () -> Unit = {},
    onRegenerateOutline: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddForm by remember { mutableStateOf(false) }
    var showPasteTocDialog by remember { mutableStateOf(false) }
    var editingChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var selectedOutlineTab by remember { mutableIntStateOf(0) } // 0 = Chapters, 1 = All Leaves
    var newTitle by remember { mutableStateOf("") }
    var newSubtitle by remember { mutableStateOf("") }
    var isSubSection by remember { mutableStateOf(false) }

    // Find the most specific active chapter for highlighting
    val exactActiveChapterId = remember(chapters, currentPage) {
        chapters.filter { currentPage in it.startPage..it.endPage }
            .maxByOrNull { it.level }?.id
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .testTag("chapter_outline_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TABLE OF CONTENTS",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = folioTitle,
                        fontFamily = PlayfairDisplayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { showPasteTocDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_paste_toc_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Edit TOC", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showAddForm = !showAddForm },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("toggle_add_chapter_marker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAddForm) "Cancel" else "+ Mark",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (showAddForm) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Add Outline Entry on Page ${currentPage + 1}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Chapter or Section Title") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_chapter_title_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newSubtitle,
                            onValueChange = { newSubtitle = it },
                            label = { Text("Subtitle or Topic Summary (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = isSubSection,
                                    onCheckedChange = { isSubSection = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Indent as sub-section",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        onAddCustomChapter(newTitle.trim(), newSubtitle.trim(), isSubSection)
                                        newTitle = ""
                                        newSubtitle = ""
                                        showAddForm = false
                                    }
                                },
                                enabled = newTitle.isNotBlank(),
                                modifier = Modifier.testTag("save_custom_chapter_button")
                            ) {
                                Text("Add to Outline")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Outline Selection Chips (Chapters vs All Leaves)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedOutlineTab == 0,
                    onClick = { selectedOutlineTab = 0 },
                    label = { Text("Chapters (${chapters.size})") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_chapters_outline")
                )
                FilterChip(
                    selected = selectedOutlineTab == 1,
                    onClick = { selectedOutlineTab = 1 },
                    label = { Text("All Leaves ($totalPages)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesomeMotion,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_all_leaves")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(10.dp))

            if (selectedOutlineTab == 0) {
                // Chapters Outline
                if (chapters.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Chapters Indexed Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Extract outlines from this book, paste the table of contents, or auto-generate sections.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onScanAndExtractPdfOutline,
                                    modifier = Modifier.testTag("scan_outline_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesomeMotion,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scan Book Outlines")
                                }
                                OutlinedButton(
                                    onClick = { showPasteTocDialog = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Paste Contents")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chapters, key = { it.id }) { chapter ->
                            val isExactCurrent = chapter.id == exactActiveChapterId
                            val isContainingCurrent = currentPage in chapter.startPage..chapter.endPage
                            val isCompleted = currentPage > chapter.endPage
                            val chapterBookmarks = bookmarks.filter { it.pageIndex in chapter.startPage..chapter.endPage }

                            Surface(
                                onClick = {
                                    onSelectPage(chapter.startPage)
                                    onDismiss()
                                },
                                color = when {
                                    isExactCurrent -> MaterialTheme.colorScheme.primaryContainer
                                    isContainingCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    chapter.level > 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = if (chapter.level > 0) 22.dp else 0.dp)
                                    .border(
                                        width = if (isExactCurrent) 1.5.dp else 0.8.dp,
                                        color = if (isExactCurrent) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("chapter_item_${chapter.startPage}_${chapter.level}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Chapter status pill
                                    Box(
                                        modifier = Modifier
                                            .size(if (chapter.level == 0) 32.dp else 24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isExactCurrent -> MaterialTheme.colorScheme.primary
                                                    isCompleted -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted && !isExactCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completed",
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Text(
                                                text = if (chapter.level == 0) "${chapter.chapterNumber}" else "§",
                                                fontFamily = JetBrainsMonoFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isExactCurrent) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chapter.title,
                                            fontFamily = if (chapter.level == 0) PlayfairDisplayFamily else MaterialTheme.typography.titleMedium.fontFamily,
                                            fontWeight = if (chapter.level == 0) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = if (chapter.level == 0) 15.sp else 13.5.sp,
                                            color = if (isExactCurrent) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        if (chapter.subtitle.isNotBlank()) {
                                            Text(
                                                text = chapter.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isExactCurrent) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (chapter.startPage == chapter.endPage) {
                                                "P. ${chapter.startPage + 1}"
                                            } else {
                                                "PP. ${chapter.startPage + 1}–${chapter.endPage + 1}"
                                            },
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 11.sp,
                                            color = if (isExactCurrent) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        if (chapterBookmarks.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                chapterBookmarks.take(3).forEach { bm ->
                                                    Icon(
                                                        imageVector = Icons.Filled.Bookmark,
                                                        contentDescription = "Bookmarked in chapter",
                                                        tint = Color(bm.ribbonColorHex),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Quick action buttons: Edit and Delete
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { editingChapter = chapter },
                                            modifier = Modifier.size(28.dp).testTag("edit_chapter_${chapter.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EditNote,
                                                contentDescription = "Edit Chapter",
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteChapter(chapter) },
                                            modifier = Modifier.size(28.dp).testTag("delete_chapter_${chapter.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete Chapter",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // All Leaves Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(totalPages) { pageIdx ->
                        val isCurrent = pageIdx == currentPage
                        val pageChapter = chapters.filter { pageIdx in it.startPage..it.endPage }
                            .maxByOrNull { it.level }
                            ?: chapters.lastOrNull { it.startPage <= pageIdx }
                        val bookmark = bookmarks.find { it.pageIndex == pageIdx }

                        Surface(
                            onClick = {
                                onSelectPage(pageIdx)
                                onDismiss()
                            },
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isCurrent) 1.5.dp else 0.8.dp,
                                    color = if (isCurrent) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("leaf_item_$pageIdx")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCurrent) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${pageIdx + 1}",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pageChapter?.title ?: "Leaf ${pageIdx + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (pageChapter != null && pageChapter.subtitle.isNotBlank()) {
                                        Text(
                                            text = pageChapter.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (bookmark != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = "Bookmark ribbon on leaf",
                                        tint = Color(bookmark.ribbonColorHex),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Paste / Edit Full Table of Contents
    if (showPasteTocDialog) {
        var tocInput by remember(chapters) {
            mutableStateOf(
                if (chapters.isNotEmpty()) {
                    chapters.joinToString("\n") { ch ->
                        "${ch.title} - ${ch.startPage + 1}"
                    }
                } else {
                    "Chapter 1: Introduction - 1\nChapter 2: Core Concepts - 15\nChapter 3: Analysis & Plates - 32"
                }
            )
        }

        AlertDialog(
            onDismissRequest = { showPasteTocDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Book Table of Contents")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Paste or type your book's contents (one chapter per line with page number, e.g. 'Chapter 1: Introduction - 1').",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tocInput,
                        onValueChange = { tocInput = it },
                        label = { Text("Contents List") },
                        minLines = 6,
                        maxLines = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paste_toc_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            onScanAndExtractPdfOutline()
                            showPasteTocDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().testTag("scan_pdf_bookmarks_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesomeMotion,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Scan PDF for Bookmarks / Outline")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tocInput.isNotBlank()) {
                            onApplyCustomToc(tocInput)
                            showPasteTocDialog = false
                        }
                    },
                    modifier = Modifier.testTag("apply_pasted_toc_button")
                ) {
                    Text("Apply Contents")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteTocDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Dialog: Edit Individual Chapter Details
    if (editingChapter != null) {
        val ch = editingChapter!!
        var editTitle by remember(ch) { mutableStateOf(ch.title) }
        var editSubtitle by remember(ch) { mutableStateOf(ch.subtitle) }
        var editStartPage by remember(ch) { mutableStateOf((ch.startPage + 1).toString()) }
        var editEndPage by remember(ch) { mutableStateOf((ch.endPage + 1).toString()) }

        AlertDialog(
            onDismissRequest = { editingChapter = null },
            title = { Text("Edit Chapter Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Chapter Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_chapter_title_input")
                    )
                    OutlinedTextField(
                        value = editSubtitle,
                        onValueChange = { editSubtitle = it },
                        label = { Text("Subtitle / Summary") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editStartPage,
                            onValueChange = { editStartPage = it },
                            label = { Text("Start Page (1..$totalPages)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editEndPage,
                            onValueChange = { editEndPage = it },
                            label = { Text("End Page (1..$totalPages)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isNotBlank()) {
                            val startP = (editStartPage.toIntOrNull() ?: 1) - 1
                            val endP = (editEndPage.toIntOrNull() ?: (startP + 1)) - 1
                            val clampedStart = startP.coerceIn(0, totalPages - 1)
                            val clampedEnd = endP.coerceIn(clampedStart, totalPages - 1)
                            onUpdateChapter(
                                ch.copy(
                                    title = editTitle.trim(),
                                    subtitle = editSubtitle.trim(),
                                    startPage = clampedStart,
                                    endPage = clampedEnd
                                )
                            )
                            editingChapter = null
                        }
                    },
                    modifier = Modifier.testTag("save_edited_chapter_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingChapter = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksSheet(
    folioTitle: String,
    currentPage: Int,
    bookmarks: List<BookmarkEntity>,
    isCurrentPageBookmarked: Boolean,
    onJumpToPage: (Int) -> Unit,
    onAddOrEditCurrentPageBookmark: () -> Unit,
    onEditBookmark: (BookmarkEntity) -> Unit,
    onDeleteBookmark: (BookmarkEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRibbonFilter by remember { mutableStateOf<Long?>(null) }

    val filteredBookmarks = remember(bookmarks, selectedRibbonFilter) {
        if (selectedRibbonFilter == null) bookmarks
        else bookmarks.filter { it.ribbonColorHex == selectedRibbonFilter }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .testTag("bookmarks_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SILK RIBBONS & MARGIN NOTES",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                        color = CrimsonRibbon
                    )
                    Text(
                        text = folioTitle,
                        fontFamily = PlayfairDisplayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        onDismiss()
                        onAddOrEditCurrentPageBookmark()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRibbon, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("sheet_bookmark_current_page_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCurrentPageBookmarked) "Edit Page ${currentPage + 1}" else "Mark Page ${currentPage + 1}",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ribbon Color Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedRibbonFilter == null,
                        onClick = { selectedRibbonFilter = null },
                        label = { Text("All (${bookmarks.size})") }
                    )
                }
                items(RibbonPreset.entries) { preset ->
                    val count = bookmarks.count { it.ribbonColorHex == preset.colorHex }
                    FilterChip(
                        selected = selectedRibbonFilter == preset.colorHex,
                        onClick = {
                            selectedRibbonFilter = if (selectedRibbonFilter == preset.colorHex) null else preset.colorHex
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(preset.color)
                            )
                        },
                        label = { Text("${preset.label} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            if (filteredBookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Ribbon Bookmarks Found",
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the silk ribbon at the top-right corner of any page or press 'Mark Page' above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredBookmarks, key = { it.id }) { bm ->
                        val ribbonColor = Color(bm.ribbonColorHex)
                        Surface(
                            onClick = {
                                onJumpToPage(bm.pageIndex)
                                onDismiss()
                            },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = ribbonColor.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("bookmark_item_page_${bm.pageIndex}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Bookmark,
                                            contentDescription = null,
                                            tint = ribbonColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "PAGE ${bm.pageIndex + 1}",
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ribbonColor
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "· ${bm.chapterTitle}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                onDismiss()
                                                onEditBookmark(bm)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EditNote,
                                                contentDescription = "Edit margin note",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteBookmark(bm) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete bookmark",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                if (bm.snippet.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = bm.snippet,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (bm.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ribbonColor.copy(alpha = 0.14f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Margin Note: ${bm.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageOverviewGridSheet(
    folioTitle: String,
    totalPages: Int,
    currentPage: Int,
    chapters: List<ChapterEntity>,
    bookmarks: List<BookmarkEntity>,
    onSelectPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pages = remember(totalPages) { (0 until totalPages).toList() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .testTag("page_overview_grid_sheet")
        ) {
            Text(
                text = "FOLIO LEAF OVERVIEW",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                letterSpacing = 1.4.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$folioTitle · $totalPages Leaves",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 98.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                items(pages, key = { it }) { pageIdx ->
                    val isCurrent = pageIdx == currentPage
                    val bm = bookmarks.find { it.pageIndex == pageIdx }
                    val chapterStart = chapters.find { it.startPage == pageIdx && it.level == 0 }

                    Surface(
                        onClick = {
                            onSelectPage(pageIdx)
                            onDismiss()
                        },
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(128.dp)
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            if (bm != null) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = "Bookmarked",
                                    tint = Color(bm.ribbonColorHex),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = "LEAF ${pageIdx + 1}",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isCurrent) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                if (chapterStart != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = chapterStart.title,
                                        fontFamily = PlayfairDisplayFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Miniature text lines representation
                                    repeat(4) { line ->
                                        Box(
                                            modifier = Modifier
                                                .padding(bottom = 5.dp)
                                                .fillMaxWidth(if (line == 3) 0.6f else 0.9f)
                                                .height(3.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                                    RoundedCornerShape(2.dp)
                                                )
                                        )
                                    }
                                }
                            }

                            if (isCurrent) {
                                Text(
                                    text = "OPEN",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    turnMode: TurnMode3D,
    onSelectTurnMode: (TurnMode3D) -> Unit,
    paperTheme: PaperTheme,
    onSelectPaperTheme: (PaperTheme) -> Unit,
    fontChoice: ReaderFontChoice,
    onSelectFontChoice: (ReaderFontChoice) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    isPdf: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .testTag("reader_settings_sheet")
        ) {
            Text(
                text = "ATELIER STUDIO SETTINGS",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                letterSpacing = 1.4.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "3D Physics, Paper Stock & Typography",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. 3D Page-Turning Mode Selection
            Text(
                text = "3D Page-Turning Physics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TurnMode3D.entries.forEach { mode ->
                    val selected = mode == turnMode
                    Surface(
                        onClick = { onSelectTurnMode(mode) },
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("turn_mode_option_${mode.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesomeMotion,
                                contentDescription = null,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = mode.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Paper Stock & PDF Filter Palette
            Text(
                text = if (isPdf) "Paper Stock & PDF Contrast Filter" else "Paper Stock & Illumination",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaperTheme.entries.forEach { theme ->
                    val selected = theme == paperTheme
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.pageBackground)
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) AntiqueBrass else theme.secondaryText.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectPaperTheme(theme) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .testTag("paper_theme_option_${theme.name}")
                    ) {
                        Text(
                            text = "Aa",
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = theme.primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = theme.displayName.substringBefore(" "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.secondaryText,
                            maxLines = 1
                        )
                    }
                }
            }

            if (!isPdf) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Book Typeface & Scale",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReaderFontChoice.entries.forEach { choice ->
                        FilterChip(
                            selected = choice == fontChoice,
                            onClick = { onSelectFontChoice(choice) },
                            label = {
                                Text(
                                    text = choice.displayName,
                                    fontFamily = choice.fontFamily,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "A-",
                        fontFamily = PlayfairDisplayFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = fontScale,
                        onValueChange = onFontScaleChange,
                        valueRange = 0.85f..1.25f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    )
                    Text(
                        text = "A+",
                        fontFamily = PlayfairDisplayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun BookmarkNoteDialog(
    pageIndex: Int,
    chapterTitle: String,
    existingBookmark: BookmarkEntity?,
    onSave: (note: String, ribbonColorHex: Long) -> Unit,
    onRemove: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var noteText by remember(existingBookmark) { mutableStateOf(existingBookmark?.note ?: "") }
    var selectedColorHex by remember(existingBookmark) {
        mutableLongStateOf(existingBookmark?.ribbonColorHex ?: RibbonPreset.CRIMSON.colorHex)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "PAGE ${pageIndex + 1} · SILK RIBBON",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = Color(selectedColorHex)
                )
                Text(
                    text = if (existingBookmark != null) "Edit Ribbon & Margin Note" else "Place Ribbon Bookmark",
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Silk Ribbon Color",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RibbonPreset.entries.forEach { preset ->
                        val isSelected = selectedColorHex == preset.colorHex
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(preset.color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = preset.colorHex }
                                .testTag("ribbon_color_${preset.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = preset.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Margin Note or Excerpt Reflection") },
                    placeholder = { Text("Add your thoughts on this page…") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bookmark_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(noteText.trim(), selectedColorHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(selectedColorHex),
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("save_bookmark_dialog_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Ribbon")
            }
        },
        dismissButton = {
            Row {
                if (existingBookmark != null && onRemove != null) {
                    TextButton(
                        onClick = {
                            onRemove()
                            onDismiss()
                        }
                    ) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
