package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BookmarkEntity
import com.example.data.FolioEntity
import com.example.ui.LibraryTab
import com.example.ui.components.Folio3DCover
import com.example.ui.theme.AntiqueBrass
import com.example.ui.theme.CrimsonRibbon
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PlayfairDisplayFamily

@Composable
fun LibraryScreen(
    folios: List<FolioEntity>,
    allBookmarks: List<BookmarkEntity>,
    selectedTab: LibraryTab,
    searchQuery: String,
    onSelectTab: (LibraryTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenFolio: (folioId: String, pageIndex: Int?) -> Unit,
    onDeleteBookmark: (BookmarkEntity) -> Unit,
    onImportPdfClick: () -> Unit
) {
    val recentFolio = folios.firstOrNull()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("library_screen_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Atelier Bindery Header + Import PDF Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesomeMotion,
                                contentDescription = null,
                                tint = AntiqueBrass,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FOLIO 3D BINDERY",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                letterSpacing = 1.6.sp,
                                color = AntiqueBrass
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.library_title),
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Button(
                        onClick = onImportPdfClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("import_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.import_pdf),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 2. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input")
                )
            }

            // 3. Continue Reading 3D Hero Desk Showcase (when not filtering bookmarks)
            if (recentFolio != null && selectedTab != LibraryTab.BOOKMARKS && searchQuery.isBlank()) {
                item {
                    ContinueReadingHeroCard(
                        folio = recentFolio,
                        bookmarkCount = allBookmarks.count { it.folioId == recentFolio.id },
                        onOpen = { onOpenFolio(recentFolio.id, null) }
                    )
                }
            }

            // 4. Category & Saved Ribbons Filter Bar
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        LibraryFilterTabChip(
                            label = stringResource(R.string.tab_all_folios),
                            selected = selectedTab == LibraryTab.ALL,
                            onClick = { onSelectTab(LibraryTab.ALL) },
                            tag = "tab_chip_all"
                        )
                    }
                    item {
                        LibraryFilterTabChip(
                            label = stringResource(R.string.tab_pdf_documents),
                            selected = selectedTab == LibraryTab.PDFS,
                            onClick = { onSelectTab(LibraryTab.PDFS) },
                            tag = "tab_chip_pdfs"
                        )
                    }
                    item {
                        LibraryFilterTabChip(
                            label = stringResource(R.string.tab_books),
                            selected = selectedTab == LibraryTab.BOOKS,
                            onClick = { onSelectTab(LibraryTab.BOOKS) },
                            tag = "tab_chip_books"
                        )
                    }
                    item {
                        LibraryFilterTabChip(
                            label = "${stringResource(R.string.tab_bookmarks)} (${allBookmarks.size})",
                            selected = selectedTab == LibraryTab.BOOKMARKS,
                            onClick = { onSelectTab(LibraryTab.BOOKMARKS) },
                            tag = "tab_chip_bookmarks"
                        )
                    }
                }
            }

            // 5. Either Saved Ribbons Hub OR Folio Editions List
            if (selectedTab == LibraryTab.BOOKMARKS) {
                if (allBookmarks.isEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    tint = CrimsonRibbon,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.empty_bookmarks_title),
                                    fontFamily = PlayfairDisplayFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.empty_bookmarks_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(allBookmarks, key = { it.id }) { bm ->
                        SavedRibbonLibraryCard(
                            bookmark = bm,
                            onJump = { onOpenFolio(bm.folioId, bm.pageIndex) },
                            onDelete = { onDeleteBookmark(bm) }
                        )
                    }
                }
            } else {
                items(folios, key = { it.id }) { folio ->
                    val folioBookmarks = allBookmarks.filter { it.folioId == folio.id }
                    FolioCollectionCard(
                        folio = folio,
                        bookmarks = folioBookmarks,
                        onOpen = { onOpenFolio(folio.id, null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryFilterTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.testTag(tag)
    )
}

@Composable
private fun ContinueReadingHeroCard(
    folio: FolioEntity,
    bookmarkCount: Int,
    onOpen: () -> Unit
) {
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = AntiqueBrass.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("continue_reading_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Folio3DCover(
                    title = folio.title,
                    author = folio.author,
                    formatBadge = folio.format,
                    coverStyle = folio.coverStyle,
                    accentHex = folio.accentHex,
                    tiltDegreesY = -14f,
                    modifier = Modifier
                        .width(106.dp)
                        .height(148.dp)
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(folio.accentHex).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "READING DESK · ${folio.format}",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(folio.accentHex)
                            )
                        }
                        if (bookmarkCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = CrimsonRibbon,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$bookmarkCount",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = CrimsonRibbon
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = folio.title,
                        fontFamily = PlayfairDisplayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = folio.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Leaf ${folio.currentPage + 1} of ${folio.totalPages}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(folio.progressFraction * 100).toInt()}%",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { folio.progressFraction },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun FolioCollectionCard(
    folio: FolioEntity,
    bookmarks: List<BookmarkEntity>,
    onOpen: () -> Unit
) {
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("folio_card_${folio.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Folio3DCover(
                title = folio.title,
                author = folio.author,
                formatBadge = folio.format,
                coverStyle = folio.coverStyle,
                accentHex = folio.accentHex,
                tiltDegreesY = -10f,
                modifier = Modifier
                    .width(82.dp)
                    .height(116.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (folio.isPdf) Icons.Default.PictureAsPdf else Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color(folio.accentHex),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (folio.isPdf) "PDF FOLIO · 3D RENDER" else "LITERARY BOOK · 3D CURL",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = Color(folio.accentHex)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = folio.title,
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = folio.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = folio.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Page ${folio.currentPage + 1} / ${folio.totalPages}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (bookmarks.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            bookmarks.take(4).forEach { bm ->
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = "Ribbon on page ${bm.pageIndex + 1}",
                                    tint = Color(bm.ribbonColorHex),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${bookmarks.size} ${if (bookmarks.size == 1) "ribbon" else "ribbons"}",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedRibbonLibraryCard(
    bookmark: BookmarkEntity,
    onJump: () -> Unit,
    onDelete: () -> Unit
) {
    val ribbonColor = Color(bookmark.ribbonColorHex)

    Surface(
        onClick = onJump,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = ribbonColor.copy(alpha = 0.55f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("saved_ribbon_card_${bookmark.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = ribbonColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${bookmark.folioTitle} · PAGE ${bookmark.pageIndex + 1}",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ribbonColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = bookmark.chapterTitle,
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove bookmark",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (bookmark.snippet.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bookmark.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (bookmark.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ribbonColor.copy(alpha = 0.14f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "“${bookmark.note}”",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
