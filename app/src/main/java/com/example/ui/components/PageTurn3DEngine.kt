package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.TurnMode3D
import com.example.ui.theme.PaperTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

/**
 * Interactive 3D Page-Turning Engine supporting:
 * 1. CURL_3D: Authentic 3D curling page fold with angled peel geometry, backside paper reflection,
 *    cylindrical fold highlight, and cast shadow onto the revealed page.
 * 2. HINGE_3D: 3D perspective spine hinge (`rotationY` + `cameraDistance`) with dynamic lighting.
 * 3. STACK_3D: Parallax drafting-table sheet stack with 3D depth tilt.
 */
@Composable
fun PageTurn3DContainer(
    currentPage: Int,
    totalPages: Int,
    turnMode: TurnMode3D,
    paperTheme: PaperTheme,
    externalTurnRequest: Int, // +1 for next, -1 for prev, 0 for idle
    onExternalTurnConsumed: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onToggleChrome: () -> Unit,
    pageContent: @Composable (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // +1 when turning forward (currentPage -> currentPage + 1)
    // -1 when turning backward (currentPage -> currentPage - 1)
    var turnDirection by remember { mutableIntStateOf(0) }
    // Vertical touch normalized [0.15f .. 0.85f] to angle the 3D curl fold naturally
    var touchNormalizedY by remember { mutableFloatStateOf(0.65f) }
    val turnProgress = remember { Animatable(0f) }

    suspend fun animateForwardTurn() {
        if (currentPage >= totalPages - 1 || turnDirection != 0) return
        turnDirection = 1
        touchNormalizedY = 0.68f
        turnProgress.snapTo(0.01f)
        turnProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing)
        )
        onPageChanged(currentPage + 1)
        turnProgress.snapTo(0f)
        turnDirection = 0
    }

    suspend fun animateBackwardTurn() {
        if (currentPage <= 0 || turnDirection != 0) return
        turnDirection = -1
        touchNormalizedY = 0.55f
        turnProgress.snapTo(0.99f)
        turnProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing)
        )
        onPageChanged(currentPage - 1)
        turnProgress.snapTo(0f)
        turnDirection = 0
    }

    LaunchedEffect(externalTurnRequest) {
        if (externalTurnRequest > 0) {
            animateForwardTurn()
            onExternalTurnConsumed()
        } else if (externalTurnRequest < 0) {
            animateBackwardTurn()
            onExternalTurnConsumed()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(paperTheme.tableBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .onSizeChanged { containerSize = it }
            .testTag("reader_3d_page_turn_surface")
            .pointerInput(currentPage, totalPages) {
                detectTapGestures(
                    onTap = { offset ->
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        touchNormalizedY = (offset.y / size.height.toFloat().coerceAtLeast(1f))
                            .coerceIn(0.2f, 0.85f)
                        when {
                            offset.x > width * 0.76f && currentPage < totalPages - 1 -> {
                                coroutineScope.launch { animateForwardTurn() }
                            }
                            offset.x < width * 0.24f && currentPage > 0 -> {
                                coroutineScope.launch { animateBackwardTurn() }
                            }
                            else -> onToggleChrome()
                        }
                    }
                )
            }
            .pointerInput(currentPage, totalPages) {
                detectHorizontalDragGestures(
                    onDragStart = { startOffset ->
                        val h = size.height.toFloat().coerceAtLeast(1f)
                        touchNormalizedY = (startOffset.y / h).coerceIn(0.2f, 0.85f)
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val h = size.height.toFloat().coerceAtLeast(1f)
                        touchNormalizedY = (change.position.y / h).coerceIn(0.18f, 0.86f)

                        if (turnDirection == 0) {
                            if (dragAmount < -2f && currentPage < totalPages - 1) {
                                turnDirection = 1
                                coroutineScope.launch { turnProgress.snapTo(0.02f) }
                            } else if (dragAmount > 2f && currentPage > 0) {
                                turnDirection = -1
                                coroutineScope.launch { turnProgress.snapTo(0.95f) }
                            }
                        } else if (turnDirection == 1) {
                            val delta = -dragAmount / (w * 0.85f)
                            val nextVal = (turnProgress.value + delta).coerceIn(0.01f, 0.99f)
                            coroutineScope.launch { turnProgress.snapTo(nextVal) }
                        } else if (turnDirection == -1) {
                            val delta = -dragAmount / (w * 0.85f)
                            val nextVal = (turnProgress.value + delta).coerceIn(0.01f, 0.99f)
                            coroutineScope.launch { turnProgress.snapTo(nextVal) }
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (turnDirection == 1) {
                                if (turnProgress.value > 0.22f) {
                                    turnProgress.animateTo(
                                        1f,
                                        tween(280, easing = FastOutSlowInEasing)
                                    )
                                    onPageChanged(currentPage + 1)
                                } else {
                                    turnProgress.animateTo(
                                        0f,
                                        tween(220, easing = FastOutSlowInEasing)
                                    )
                                }
                                turnProgress.snapTo(0f)
                                turnDirection = 0
                            } else if (turnDirection == -1) {
                                if (turnProgress.value < 0.78f) {
                                    turnProgress.animateTo(
                                        0f,
                                        tween(280, easing = FastOutSlowInEasing)
                                    )
                                    onPageChanged(currentPage - 1)
                                } else {
                                    turnProgress.animateTo(
                                        1f,
                                        tween(220, easing = FastOutSlowInEasing)
                                    )
                                }
                                turnProgress.snapTo(0f)
                                turnDirection = 0
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            turnProgress.animateTo(0f, tween(200))
                            turnDirection = 0
                        }
                    }
                )
            }
    ) {
        // 3D Outer Book Block & Multi-Leaf Stack Edges
        BookBlock3DFrame(paperTheme = paperTheme) {
            val p = turnProgress.value

            if (turnDirection == 0 || p <= 0.001f) {
                // Static Resting Page
                pageContent(currentPage)
            } else {
                // Determine which two pages are interacting during the 3D turn:
                // When turning forward (turnDirection == 1): top turning page is `currentPage`, underneath page is `currentPage + 1`.
                // When turning backward (turnDirection == -1): top turning page is `currentPage - 1` returning from curled state `p` -> `0f`, underneath is `currentPage`.
                val turningPageIndex = if (turnDirection == 1) currentPage else (currentPage - 1).coerceAtLeast(0)
                val revealedPageIndex = if (turnDirection == 1) (currentPage + 1).coerceAtMost(totalPages - 1) else currentPage

                when (turnMode) {
                    TurnMode3D.CURL_3D -> {
                        Curl3DPageTransition(
                            progress = p,
                            touchYNormalized = touchNormalizedY,
                            paperTheme = paperTheme,
                            turningPage = { pageContent(turningPageIndex) },
                            revealedPage = { pageContent(revealedPageIndex) }
                        )
                    }
                    TurnMode3D.HINGE_3D -> {
                        Hinge3DPageTransition(
                            progress = p,
                            paperTheme = paperTheme,
                            turningPage = { pageContent(turningPageIndex) },
                            revealedPage = { pageContent(revealedPageIndex) }
                        )
                    }
                    TurnMode3D.STACK_3D -> {
                        Stack3DPageTransition(
                            progress = p,
                            paperTheme = paperTheme,
                            turningPage = { pageContent(turningPageIndex) },
                            revealedPage = { pageContent(revealedPageIndex) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookBlock3DFrame(
    paperTheme: PaperTheme,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Right & bottom stacked paper thickness grooves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stackThickness = 7.dp.toPx()

            // Right page-edge stack
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        paperTheme.pageBackside,
                        paperTheme.pageBackground,
                        paperTheme.secondaryText.copy(alpha = 0.35f)
                    ),
                    startX = w - stackThickness,
                    endX = w
                ),
                topLeft = Offset(w - stackThickness, 6.dp.toPx()),
                size = Size(stackThickness, h - 10.dp.toPx())
            )

            // Bottom page-edge stack
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        paperTheme.pageBackside,
                        paperTheme.secondaryText.copy(alpha = 0.4f)
                    ),
                    startY = h - stackThickness,
                    endY = h
                ),
                topLeft = Offset(6.dp.toPx(), h - stackThickness),
                size = Size(w - 10.dp.toPx(), stackThickness)
            )
        }

        // Active Page Viewport
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 6.dp, bottom = 5.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp)
                )
                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp))
                .background(paperTheme.pageBackground)
        ) {
            content()
        }
    }
}

@Composable
private fun Curl3DPageTransition(
    progress: Float,
    touchYNormalized: Float,
    paperTheme: PaperTheme,
    turningPage: @Composable () -> Unit,
    revealedPage: @Composable () -> Unit
) {
    // Tilt factor based on whether the reader pulls from the bottom corner, middle, or top corner
    val tiltFactor = (touchYNormalized - 0.5f) * 0.28f

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Revealed Destination Page underneath
        Box(modifier = Modifier.fillMaxSize()) {
            revealedPage()
        }

        // 2. Turning Page clipped to the left of the 3D fold axis, plus the curled backside polygon & shadows
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val w = size.width
                    val h = size.height

                    // Fold X position moves from right edge (w) to left spine (0) as progress goes 0 -> 1
                    val foldTopX = (w * (1f - progress) + w * tiltFactor * (1f - abs(progress - 0.5f) * 2f))
                        .coerceIn(0f, w)
                    val foldBottomX = (w * (1f - progress) - w * tiltFactor * (1f - abs(progress - 0.5f) * 2f))
                        .coerceIn(0f, w)
                    val avgFoldX = (foldTopX + foldBottomX) * 0.5f

                    // Cast shadow onto the revealed page to the right of the fold line
                    val shadowWidth = (w * 0.18f * (1f - progress * 0.4f)).coerceAtLeast(16f)
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                paperTheme.spineShadowColor.copy(alpha = 0.42f * (1f - progress * 0.3f)),
                                paperTheme.spineShadowColor.copy(alpha = 0.14f * (1f - progress * 0.3f)),
                                Color.Transparent
                            ),
                            startX = avgFoldX,
                            endX = (avgFoldX + shadowWidth).coerceAtMost(w)
                        ),
                        topLeft = Offset(avgFoldX, 0f),
                        size = Size((w - avgFoldX).coerceAtLeast(0f), h)
                    )

                    // Clip path for the flat remaining portion of the turning page
                    val visiblePagePath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(foldTopX, 0f)
                        lineTo(foldBottomX, h)
                        lineTo(0f, h)
                        close()
                    }

                    clipPath(visiblePagePath, clipOp = ClipOp.Intersect) {
                        this@drawWithContent.drawContent()

                        // Cylindrical curl shadow approaching the fold crease on the front side
                        val creaseLead = (w * 0.12f).coerceAtLeast(12f)
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    paperTheme.spineShadowColor.copy(alpha = 0.16f)
                                ),
                                startX = (avgFoldX - creaseLead).coerceAtLeast(0f),
                                endX = avgFoldX
                            ),
                            topLeft = Offset((avgFoldX - creaseLead).coerceAtLeast(0f), 0f),
                            size = Size(creaseLead, h)
                        )
                    }

                    // 3. Draw the 3D Curled Back-of-Page Flap
                    val flapWidth = (w * 0.28f * sin(progress * Math.PI).toFloat())
                        .coerceAtLeast(w * 0.06f * (1f - progress))
                    val flapLeftTop = (foldTopX - flapWidth).coerceAtLeast(0f)
                    val flapLeftBottom = (foldBottomX - flapWidth * 1.12f).coerceAtLeast(0f)

                    if (avgFoldX > 2f) {
                        // Drop shadow cast by the curled flap onto the front of the turning page
                        val backShadowWidth = 26.dp.toPx()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    paperTheme.spineShadowColor.copy(alpha = 0.28f)
                                ),
                                startX = (flapLeftTop - backShadowWidth).coerceAtLeast(0f),
                                endX = flapLeftTop
                            ),
                            topLeft = Offset((flapLeftTop - backShadowWidth).coerceAtLeast(0f), 0f),
                            size = Size(backShadowWidth, h)
                        )

                        val backsideFlapPath = Path().apply {
                            moveTo(flapLeftTop, 0f)
                            lineTo(foldTopX, 0f)
                            lineTo(foldBottomX, h)
                            lineTo(flapLeftBottom, h)
                            close()
                        }

                        // Fill the curled backside with realistic 3D cylindrical specular highlight
                        drawPath(
                            path = backsideFlapPath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    paperTheme.pageBackside.copy(alpha = 0.96f),
                                    paperTheme.pageBackground,
                                    paperTheme.pageBackside
                                ),
                                startX = minOf(flapLeftTop, flapLeftBottom),
                                endX = maxOf(foldTopX, foldBottomX)
                            )
                        )

                        // Faint bleed-through lines on the backside of the curling paper leaf
                        clipPath(backsideFlapPath) {
                            val lineStep = 24.dp.toPx()
                            var ly = 64.dp.toPx()
                            while (ly < h - 64.dp.toPx()) {
                                drawLine(
                                    color = paperTheme.secondaryText.copy(alpha = 0.09f),
                                    start = Offset(flapLeftTop + 12f, ly),
                                    end = Offset(foldTopX - 14f, ly),
                                    strokeWidth = 2.dp.toPx()
                                )
                                ly += lineStep
                            }
                        }
                    }
                }
        ) {
            turningPage()
        }
    }
}

@Composable
private fun Hinge3DPageTransition(
    progress: Float,
    paperTheme: PaperTheme,
    turningPage: @Composable () -> Unit,
    revealedPage: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Revealed page underneath with dynamic ambient shadow that lifts as the top page swings open
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val shadowAlpha = (1f - progress) * 0.45f
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                paperTheme.spineShadowColor.copy(alpha = shadowAlpha),
                                paperTheme.spineShadowColor.copy(alpha = shadowAlpha * 0.35f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = size.width * (1f - progress * 0.5f).coerceAtLeast(0.2f)
                        )
                    )
                }
        ) {
            revealedPage()
        }

        // Swinging 3D Page hinged on the left book spine
        val rotationDegrees = -progress * 105f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    rotationY = rotationDegrees
                    cameraDistance = 20f * density
                }
                .drawWithContent {
                    drawContent()
                    // Darken page slightly as it angles away from the reader's light source
                    drawRect(
                        color = paperTheme.spineShadowColor.copy(alpha = (progress * 0.38f).coerceIn(0f, 0.45f))
                    )
                }
        ) {
            turningPage()
        }
    }
}

@Composable
private fun Stack3DPageTransition(
    progress: Float,
    paperTheme: PaperTheme,
    turningPage: @Composable () -> Unit,
    revealedPage: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Underneath page scales up from 0.93f -> 1.0f with subtle depth fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val s = 0.93f + 0.07f * progress
                    scaleX = s
                    scaleY = s
                    alpha = 0.6f + 0.4f * progress
                }
        ) {
            revealedPage()
        }

        // Top folio plate slides left and tilts in 3D space
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -size.width * progress * 1.04f
                    rotationY = -progress * 22f
                    rotationZ = -progress * 2.5f
                    cameraDistance = 16f * density
                    shadowElevation = 24f * (1f - progress)
                }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        color = paperTheme.spineShadowColor.copy(alpha = progress * 0.18f)
                    )
                }
        ) {
            turningPage()
        }
    }
}
