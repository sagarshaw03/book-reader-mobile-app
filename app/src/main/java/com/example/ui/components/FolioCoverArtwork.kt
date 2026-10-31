package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AntiqueBrass
import com.example.ui.theme.IvoryGold
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PlayfairDisplayFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Folio3DCover(
    title: String,
    author: String,
    formatBadge: String,
    coverStyle: String,
    accentHex: Long,
    modifier: Modifier = Modifier,
    tiltDegreesY: Float = -11f
) {
    val accentColor = Color(accentHex)
    val baseDark = when (coverStyle) {
        "ARCHITECTURE" -> Color(0xFF331812)
        "CELESTIAL" -> Color(0xFF132238)
        "BOTANICA" -> Color(0xFF142B21)
        "TYPOGRAPHY" -> Color(0xFF2E2113)
        else -> Color(0xFF2B1917)
    }
    val baseMid = when (coverStyle) {
        "ARCHITECTURE" -> Color(0xFF54251B)
        "CELESTIAL" -> Color(0xFF1F385C)
        "BOTANICA" -> Color(0xFF1F4434)
        "TYPOGRAPHY" -> Color(0xFF4A341C)
        else -> Color(0xFF472723)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = tiltDegreesY
                cameraDistance = 14f * density
            }
    ) {
        // Right-side 3D paper stack thickness block
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, top = 3.dp, bottom = 3.dp)
                .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFD8C7AC),
                            Color(0xFFF3E7D3),
                            Color(0xFFCBB89B)
                        )
                    )
                )
        )

        // Main Hardcover Board
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 6.dp)
                .shadow(12.dp, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp))
                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(baseMid, baseDark),
                        start = Offset.Zero,
                        end = Offset(400f, 600f)
                    )
                )
                .border(
                    width = 1.dp,
                    color = AntiqueBrass.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp)
                )
        ) {
            // Copperplate Foil & Spine Cord Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val spineWidth = w * 0.11f

                // Left leather spine groove & raised cords
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.35f)
                        ),
                        startX = 0f,
                        endX = spineWidth
                    ),
                    size = Size(spineWidth, h)
                )
                for (i in 1..4) {
                    val cordY = h * (i / 5f)
                    drawLine(
                        color = AntiqueBrass.copy(alpha = 0.5f),
                        start = Offset(2f, cordY),
                        end = Offset(spineWidth - 2f, cordY),
                        strokeWidth = 2.5f
                    )
                }

                // Inner gold foil border
                val frameLeft = spineWidth + 10.dp.toPx()
                val frameTop = 10.dp.toPx()
                val frameRight = w - 10.dp.toPx()
                val frameBottom = h - 10.dp.toPx()
                drawRect(
                    color = AntiqueBrass.copy(alpha = 0.42f),
                    topLeft = Offset(frameLeft, frameTop),
                    size = Size(frameRight - frameLeft, frameBottom - frameTop),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Center Emblem Artwork
                val cx = (frameLeft + frameRight) / 2f
                val cy = h * 0.44f
                val r = (frameRight - frameLeft) * 0.28f

                when (coverStyle) {
                    "ARCHITECTURE" -> {
                        // Pantheon Dome & Golden Ratio Compass
                        drawCircle(
                            color = AntiqueBrass.copy(alpha = 0.55f),
                            radius = r,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = accentColor.copy(alpha = 0.35f),
                            radius = r * 0.72f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        val archPath = Path().apply {
                            moveTo(cx - r * 0.65f, cy + r * 0.55f)
                            lineTo(cx - r * 0.65f, cy - r * 0.1f)
                            quadraticTo(cx, cy - r * 0.85f, cx + r * 0.65f, cy - r * 0.1f)
                            lineTo(cx + r * 0.65f, cy + r * 0.55f)
                            close()
                        }
                        drawPath(archPath, color = AntiqueBrass.copy(alpha = 0.65f), style = Stroke(width = 1.5.dp.toPx()))
                    }
                    "CELESTIAL" -> {
                        // Armillary Rings & 8-Point Star
                        drawCircle(
                            color = AntiqueBrass.copy(alpha = 0.6f),
                            radius = r,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawOval(
                            color = AntiqueBrass.copy(alpha = 0.45f),
                            topLeft = Offset(cx - r, cy - r * 0.45f),
                            size = Size(r * 2f, r * 0.9f),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        for (a in 0 until 8) {
                            val angle = Math.toRadians(a * 45.0)
                            val len = if (a % 2 == 0) r * 0.85f else r * 0.45f
                            drawLine(
                                color = IvoryGold.copy(alpha = 0.7f),
                                start = Offset(cx, cy),
                                end = Offset(
                                    cx + (len * cos(angle)).toFloat(),
                                    cy + (len * sin(angle)).toFloat()
                                ),
                                strokeWidth = 1.3.dp.toPx()
                            )
                        }
                    }
                    "BOTANICA" -> {
                        // Botanical rosette & leaf ribs
                        drawCircle(
                            color = AntiqueBrass.copy(alpha = 0.5f),
                            radius = r,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.3.dp.toPx())
                        )
                        for (petals in 0 until 6) {
                            val rad = Math.toRadians(petals * 60.0)
                            val px = cx + (r * 0.42f * cos(rad)).toFloat()
                            val py = cy + (r * 0.42f * sin(rad)).toFloat()
                            drawCircle(
                                color = IvoryGold.copy(alpha = 0.45f),
                                radius = r * 0.42f,
                                center = Offset(px, py),
                                style = Stroke(width = 1.1.dp.toPx())
                            )
                        }
                    }
                    else -> {
                        // Venetian Typographic Diamond & Anchor
                        drawCircle(
                            color = AntiqueBrass.copy(alpha = 0.55f),
                            radius = r,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                        val diamond = Path().apply {
                            moveTo(cx, cy - r * 0.85f)
                            lineTo(cx + r * 0.85f, cy)
                            lineTo(cx, cy + r * 0.85f)
                            lineTo(cx - r * 0.85f, cy)
                            close()
                        }
                        drawPath(diamond, color = IvoryGold.copy(alpha = 0.55f), style = Stroke(width = 1.3.dp.toPx()))
                    }
                }
            }

            // Cover Typography Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 22.dp, end = 14.dp, top = 16.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatBadge.uppercase(),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.sp,
                    color = AntiqueBrass,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = title,
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    color = IvoryGold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = IvoryGold.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
