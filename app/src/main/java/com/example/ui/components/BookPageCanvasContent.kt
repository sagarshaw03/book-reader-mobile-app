package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookmarkEntity
import com.example.data.BookPageContent
import com.example.data.PageDiagramType
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PaperTheme
import com.example.ui.theme.PlayfairDisplayFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SingleFolioPageSurface(
    folioTitle: String,
    pageIndex: Int,
    totalPages: Int,
    isPdf: Boolean,
    pdfBitmap: Bitmap?,
    bookPage: BookPageContent?,
    bookmark: BookmarkEntity?,
    paperTheme: PaperTheme,
    readerFontFamily: FontFamily,
    fontScale: Float,
    onToggleRibbon: () -> Unit,
    onOpenBookmarkNoteDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(paperTheme.pageBackground)
    ) {
        // Subtle aged rag paper vignette & spine gutter shadow on left edge
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Spine gutter gradient along the left binding margin
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        paperTheme.spineShadowColor.copy(alpha = if (paperTheme.isDark) 0.55f else 0.18f),
                        paperTheme.spineShadowColor.copy(alpha = if (paperTheme.isDark) 0.20f else 0.05f),
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = w * 0.09f
                ),
                size = Size(w * 0.09f, h)
            )

            // Subtle laid paper horizontal chain lines
            val lineAlpha = if (paperTheme.isDark) 0.03f else 0.045f
            val step = 28.dp.toPx()
            var y = step
            while (y < h) {
                drawLine(
                    color = paperTheme.secondaryText.copy(alpha = lineAlpha),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        // Main Page Content (PDF Rendered Bitmap OR Typeset Literary Book Page)
        if (isPdf) {
            PdfPageLeafContent(
                folioTitle = folioTitle,
                pageIndex = pageIndex,
                totalPages = totalPages,
                pdfBitmap = pdfBitmap,
                paperTheme = paperTheme
            )
        } else if (bookPage != null) {
            LiteraryBookLeafContent(
                folioTitle = folioTitle,
                pageIndex = pageIndex,
                totalPages = totalPages,
                bookPage = bookPage,
                paperTheme = paperTheme,
                readerFontFamily = readerFontFamily,
                fontScale = fontScale
            )
        }

        // Margin Note Callout Pill at the bottom-left if this page has a user note
        if (bookmark != null && bookmark.note.isNotBlank()) {
            Surface(
                onClick = onOpenBookmarkNoteDialog,
                color = Color(bookmark.ribbonColorHex).copy(alpha = if (paperTheme.isDark) 0.26f else 0.14f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 22.dp, bottom = 36.dp, end = 72.dp)
                    .border(
                        width = 1.dp,
                        color = Color(bookmark.ribbonColorHex).copy(alpha = 0.55f),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = "Margin note",
                        tint = Color(bookmark.ribbonColorHex),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bookmark.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = paperTheme.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Physical Silk Bookmark Ribbon at Top-Right Corner
        SilkBookmarkRibbonPull(
            bookmark = bookmark,
            paperTheme = paperTheme,
            onToggleRibbon = onToggleRibbon,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 26.dp)
        )
    }
}

@Composable
private fun PdfPageLeafContent(
    folioTitle: String,
    pageIndex: Int,
    totalPages: Int,
    pdfBitmap: Bitmap?,
    paperTheme: PaperTheme
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pdfBitmap != null && !pdfBitmap.isRecycled) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = pdfBitmap.asImageBitmap(),
                    contentDescription = "$folioTitle Page ${pageIndex + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = paperTheme.accentColor,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Rendering PDF Plate ${pageIndex + 1} of $totalPages…",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        color = paperTheme.secondaryText
                    )
                }
            }
        }

        // Subtle folio footline
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PDF FOLIO RENDERER",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = paperTheme.secondaryText.copy(alpha = 0.75f)
            )
            Text(
                text = "— ${pageIndex + 1} / $totalPages —",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                color = paperTheme.primaryText.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun LiteraryBookLeafContent(
    folioTitle: String,
    pageIndex: Int,
    totalPages: Int,
    bookPage: BookPageContent,
    paperTheme: PaperTheme,
    readerFontFamily: FontFamily,
    fontScale: Float
) {
    val bodyFontSize = (15.5f * fontScale).sp
    val bodyLineHeight = (24.5f * fontScale).sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 26.dp, end = 24.dp, top = 20.dp, bottom = 14.dp)
    ) {
        // Running Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 42.dp), // Leave room for silk ribbon at top right
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = bookPage.chapterTitle.uppercase(),
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = paperTheme.accentColor,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = paperTheme.secondaryText.copy(alpha = 0.22f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Chapter Opening Ornament & Heading
        if (bookPage.isChapterStart) {
            Text(
                text = "CHAPTER ${bookPage.chapterNumber}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                letterSpacing = 1.6.sp,
                color = paperTheme.accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = bookPage.chapterTitle.substringAfter("· ").ifBlank { bookPage.chapterTitle },
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (22f * fontScale).sp,
                lineHeight = (27f * fontScale).sp,
                color = paperTheme.primaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (!bookPage.sectionHeading.isNullOrBlank()) {
            Text(
                text = bookPage.sectionHeading,
                fontFamily = PlayfairDisplayFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                fontSize = (15f * fontScale).sp,
                color = paperTheme.secondaryText
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // First Paragraph with Oversized Illuminated Drop Cap
        val firstChar = bookPage.dropCapParagraph.firstOrNull()?.toString() ?: ""
        val remainingLead = if (bookPage.dropCapParagraph.isNotEmpty()) {
            bookPage.dropCapParagraph.drop(1)
        } else ""

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp, top = 2.dp)
                    .size(44.dp)
                    .border(
                        width = 1.dp,
                        color = paperTheme.accentColor.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        paperTheme.accentColor.copy(alpha = 0.08f),
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstChar,
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = paperTheme.accentColor
                )
            }
            Text(
                text = remainingLead,
                fontFamily = readerFontFamily,
                fontSize = bodyFontSize,
                lineHeight = bodyLineHeight,
                color = paperTheme.primaryText,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Optional Copperplate Inline Diagram Plate
        if (bookPage.diagramType != PageDiagramType.NONE) {
            BookInlineCopperplateDiagram(
                diagramType = bookPage.diagramType,
                caption = bookPage.diagramCaption,
                paperTheme = paperTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Remaining Body Paragraphs
        bookPage.bodyParagraphs.forEach { paragraph ->
            Text(
                text = paragraph,
                fontFamily = readerFontFamily,
                fontSize = bodyFontSize,
                lineHeight = bodyLineHeight,
                color = paperTheme.primaryText,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // Optional Literary Pull Quote
        if (!bookPage.pullQuote.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = 1.dp,
                        color = paperTheme.accentColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(
                        paperTheme.accentColor.copy(alpha = 0.05f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = bookPage.pullQuote,
                    fontFamily = PlayfairDisplayFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = (14f * fontScale).sp,
                    lineHeight = (20f * fontScale).sp,
                    color = paperTheme.accentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footnote & Folio Leaf Number Footer
        HorizontalDivider(color = paperTheme.secondaryText.copy(alpha = 0.2f), thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = bookPage.footnote ?: folioTitle,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 9.5.sp,
                color = paperTheme.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "LEAF ${pageIndex + 1} · $totalPages",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.5.sp,
                color = paperTheme.primaryText
            )
        }
    }
}

@Composable
private fun BookInlineCopperplateDiagram(
    diagramType: PageDiagramType,
    caption: String?,
    paperTheme: PaperTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, paperTheme.secondaryText.copy(alpha = 0.28f), RoundedCornerShape(6.dp))
            .background(paperTheme.pageBackside.copy(alpha = 0.45f))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = size.height * 0.42f
            val strokeColor = paperTheme.accentColor
            val secondaryStroke = paperTheme.secondaryText.copy(alpha = 0.55f)

            when (diagramType) {
                PageDiagramType.BOTANICAL_LEAF -> {
                    val path = Path().apply {
                        moveTo(cx - r * 1.6f, cy)
                        quadraticTo(cx, cy - r * 1.1f, cx + r * 1.6f, cy)
                        quadraticTo(cx, cy + r * 1.1f, cx - r * 1.6f, cy)
                        close()
                    }
                    drawPath(path, color = strokeColor, style = Stroke(width = 1.6.dp.toPx()))
                    drawLine(strokeColor, Offset(cx - r * 1.8f, cy), Offset(cx + r * 1.6f, cy), strokeWidth = 1.4.dp.toPx())
                    for (i in -3..3) {
                        val vx = cx + i * (r * 0.35f)
                        drawLine(secondaryStroke, Offset(vx, cy), Offset(vx + r * 0.25f, cy - r * 0.45f), strokeWidth = 1.dp.toPx())
                        drawLine(secondaryStroke, Offset(vx, cy), Offset(vx + r * 0.25f, cy + r * 0.45f), strokeWidth = 1.dp.toPx())
                    }
                }
                PageDiagramType.GOLDEN_SPIRAL -> {
                    val rectW = r * 2.8f
                    val rectH = r * 1.73f
                    drawRect(
                        color = strokeColor,
                        topLeft = Offset(cx - rectW / 2f, cy - rectH / 2f),
                        size = Size(rectW, rectH),
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                    drawLine(
                        color = secondaryStroke,
                        start = Offset(cx - rectW / 2f + rectH, cy - rectH / 2f),
                        end = Offset(cx - rectW / 2f + rectH, cy + rectH / 2f),
                        strokeWidth = 1.2.dp.toPx()
                    )
                    drawCircle(
                        color = strokeColor,
                        radius = rectH / 2f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
                PageDiagramType.CELESTIAL_ORRERY -> {
                    for (ring in 1..3) {
                        drawCircle(
                            color = if (ring == 2) strokeColor else secondaryStroke,
                            radius = r * (ring / 3f),
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                    }
                    drawLine(secondaryStroke, Offset(cx - r * 1.8f, cy), Offset(cx + r * 1.8f, cy), strokeWidth = 1.dp.toPx())
                    drawCircle(strokeColor, radius = 4.dp.toPx(), center = Offset(cx + r * 0.66f, cy - r * 0.2f))
                }
                else -> {
                    drawCircle(strokeColor, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.4.dp.toPx()))
                    drawRect(
                        color = secondaryStroke,
                        topLeft = Offset(cx - r * 0.7f, cy - r * 0.7f),
                        size = Size(r * 1.4f, r * 1.4f),
                        style = Stroke(width = 1.1.dp.toPx())
                    )
                }
            }
        }
        if (!caption.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = caption,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 9.5.sp,
                color = paperTheme.secondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SilkBookmarkRibbonPull(
    bookmark: BookmarkEntity?,
    paperTheme: PaperTheme,
    onToggleRibbon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBookmarked = bookmark != null
    val ribbonColor = if (bookmark != null) {
        Color(bookmark.ribbonColorHex)
    } else {
        paperTheme.secondaryText.copy(alpha = 0.32f)
    }

    val ribbonHeight by animateDpAsState(
        targetValue = if (isBookmarked) 88.dp else 44.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ribbonHeight"
    )

    Box(
        modifier = modifier
            .width(36.dp)
            .height(ribbonHeight)
            .clickable(onClick = onToggleRibbon)
            .testTag("page_ribbon_bookmark_toggle"),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val notchY = h - 11.dp.toPx()

            // Drop shadow under silk ribbon
            val shadowPath = Path().apply {
                moveTo(4f, 0f)
                lineTo(w, 0f)
                lineTo(w, h)
                lineTo(w / 2f + 2f, notchY)
                lineTo(4f, h)
                close()
            }
            drawPath(shadowPath, color = Color.Black.copy(alpha = if (isBookmarked) 0.25f else 0.08f))

            // Silk ribbon body with Swallowtail V-notch
            val ribbonPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w - 3f, 0f)
                lineTo(w - 3f, h - 2f)
                lineTo((w - 3f) / 2f, notchY - 2f)
                lineTo(0f, h - 2f)
                close()
            }
            drawPath(
                path = ribbonPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ribbonColor.copy(alpha = if (isBookmarked) 0.95f else 0.45f),
                        ribbonColor
                    )
                )
            )
        }

        Icon(
            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = if (isBookmarked) "Remove Ribbon Bookmark" else "Place Ribbon Bookmark",
            tint = Color.White.copy(alpha = if (isBookmarked) 0.92f else 0.7f),
            modifier = Modifier
                .padding(top = 8.dp)
                .size(16.dp)
        )
    }
}
