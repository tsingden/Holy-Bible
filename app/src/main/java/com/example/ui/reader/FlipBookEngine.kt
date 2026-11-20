package com.example.ui.reader

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.BibleVerse
import com.example.data.model.HighlightColor
import com.example.data.model.ReadingMode
import com.example.data.model.ThemePreset
import com.example.ui.ChapterSnapshot
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class FlipBookController {
    internal var onFlipForward: (() -> Unit)? = null
    internal var onFlipBackward: (() -> Unit)? = null

    fun flipForward() {
        onFlipForward?.invoke()
    }

    fun flipBackward() {
        onFlipBackward?.invoke()
    }
}

@Composable
fun rememberFlipBookController(): FlipBookController = remember { FlipBookController() }

@Composable
fun FlipBookPager(
    book: BibleBook,
    chapter: Int,
    verses: List<BibleVerse>,
    prevSnapshot: ChapterSnapshot?,
    nextSnapshot: ChapterSnapshot?,
    readingMode: ReadingMode,
    primaryLanguage: BibleLanguage,
    secondaryLanguage: BibleLanguage,
    downloadedBookKeys: Set<Pair<String, Int>>,
    fontSizeSp: Float,
    selectedVerseKey: String?,
    activeAudioVerseKey: String?,
    targetScrollVerse: Int?,
    bookmarkedKeys: Set<String>,
    highlightMap: Map<String, HighlightColor>,
    noteKeys: Set<String>,
    controller: FlipBookController,
    onRequestPreviousChapter: () -> Unit,
    onRequestNextChapter: () -> Unit,
    onPlayForwardSound: () -> Unit,
    onPlayBackwardSound: () -> Unit,
    onVerseClick: (BibleVerse) -> Unit,
    onTopVisibleVerseChanged: (Int) -> Unit,
    onDownloadLanguageBook: (BibleLanguage, Int) -> Unit,
    onOpenDownloadsManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val listState: LazyListState = rememberLazyListState()

    val flipProgress = remember { Animatable(0f) }
    var dragTouchYFraction by remember { mutableFloatStateOf(0.5f) }
    var isAnimatingFlip by remember { mutableStateOf(false) }

    LaunchedEffect(book.id, chapter) {
        flipProgress.snapTo(0f)
        isAnimatingFlip = false
        if (targetScrollVerse == null) {
            listState.scrollToItem(0)
        }
    }

    val canGoForward = nextSnapshot != null
    val canGoBackward = prevSnapshot != null

    fun commitForwardTurn() {
        if (isAnimatingFlip || !canGoForward) return
        scope.launch {
            isAnimatingFlip = true
            onPlayForwardSound()
            flipProgress.animateTo(
                targetValue = -1f,
                animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
            )
            onRequestNextChapter()
            flipProgress.snapTo(0f)
            isAnimatingFlip = false
        }
    }

    fun commitBackwardTurn() {
        if (isAnimatingFlip || !canGoBackward) return
        scope.launch {
            isAnimatingFlip = true
            onPlayBackwardSound()
            flipProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
            )
            onRequestPreviousChapter()
            flipProgress.snapTo(0f)
            isAnimatingFlip = false
        }
    }

    controller.onFlipForward = { commitForwardTurn() }
    controller.onFlipBackward = { commitBackwardTurn() }

    val isClassicLeatherFrame = tokens.config.preset == ThemePreset.CLASSIC_BIBLE
    val frameShape = RoundedCornerShape(if (isClassicLeatherFrame) 10.dp else 18.dp)
    val frameColor = if (isClassicLeatherFrame) BurgundyDeep else tokens.glassSurface.copy(alpha = 0.45f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.deskBackground)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(frameShape)
            .background(frameColor)
            .then(
                if (!isClassicLeatherFrame) {
                    Modifier.border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(tokens.borderTop, tokens.borderBottom)),
                        shape = frameShape
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val edgeWidth = 6.dp.toPx()
            val edgeColor = palette.paperEdge
            val lineColor = palette.spineShadow.copy(alpha = if (palette.isDark) 0.45f else 0.16f)

            drawRect(color = edgeColor, topLeft = Offset.Zero, size = Size(edgeWidth, size.height))
            drawRect(
                color = edgeColor,
                topLeft = Offset(size.width - edgeWidth, 0f),
                size = Size(edgeWidth, size.height)
            )
            drawRect(
                color = edgeColor,
                topLeft = Offset(0f, size.height - 3.dp.toPx()),
                size = Size(size.width, 3.dp.toPx())
            )
            for (i in 1..3) {
                val xLeft = i * (edgeWidth / 4f)
                val xRight = size.width - i * (edgeWidth / 4f)
                drawLine(lineColor, Offset(xLeft, 2f), Offset(xLeft, size.height - 2f), strokeWidth = 0.8f)
                drawLine(lineColor, Offset(xRight, 2f), Offset(xRight, size.height - 2f), strokeWidth = 0.8f)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(4.dp))
                .clipToBounds()
                .pointerInput(book.id, chapter, canGoForward, canGoBackward) {
                    var accumulatedDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            if (!isAnimatingFlip) {
                                accumulatedDrag = 0f
                                dragTouchYFraction = if (size.height > 0) {
                                    (offset.y / size.height).coerceIn(0.1f, 0.9f)
                                } else 0.5f
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            if (isAnimatingFlip) return@detectHorizontalDragGestures
                            change.consume()
                            accumulatedDrag += dragAmount
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            var rawProgress = (accumulatedDrag / (width * 0.72f)).coerceIn(-1f, 1f)
                            if (rawProgress < 0f && !canGoForward) rawProgress *= 0.15f
                            if (rawProgress > 0f && !canGoBackward) rawProgress *= 0.15f
                            scope.launch {
                                flipProgress.snapTo(rawProgress)
                            }
                        },
                        onDragEnd = {
                            if (isAnimatingFlip) return@detectHorizontalDragGestures
                            val p = flipProgress.value
                            when {
                                p < -0.22f && canGoForward -> commitForwardTurn()
                                p > 0.22f && canGoBackward -> commitBackwardTurn()
                                else -> {
                                    scope.launch {
                                        flipProgress.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                flipProgress.animateTo(0f, tween(200))
                            }
                        }
                    )
                }
        ) {
            val p = flipProgress.value

            if (abs(p) < 0.002f) {
                BiblePageSheet(
                    book = book,
                    chapter = chapter,
                    verses = verses,
                    readingMode = readingMode,
                    primaryLanguage = primaryLanguage,
                    secondaryLanguage = secondaryLanguage,
                    downloadedBookKeys = downloadedBookKeys,
                    fontSizeSp = fontSizeSp,
                    selectedVerseKey = selectedVerseKey,
                    activeAudioVerseKey = activeAudioVerseKey,
                    targetScrollVerse = targetScrollVerse,
                    bookmarkedKeys = bookmarkedKeys,
                    highlightMap = highlightMap,
                    noteKeys = noteKeys,
                    onVerseClick = onVerseClick,
                    onTopVisibleVerseChanged = onTopVisibleVerseChanged,
                    onDownloadLanguageBook = onDownloadLanguageBook,
                    onOpenDownloadsManager = onOpenDownloadsManager,
                    listState = listState,
                    isStaticPreview = false
                )
            } else if (p < 0f) {
                val forwardProgress = (-p).coerceIn(0f, 1f)
                val targetBook = nextSnapshot?.book ?: book
                val targetChap = nextSnapshot?.chapter ?: chapter
                val targetVerses = nextSnapshot?.verses ?: emptyList()

                BiblePageSheet(
                    book = targetBook,
                    chapter = targetChap,
                    verses = targetVerses,
                    readingMode = readingMode,
                    primaryLanguage = primaryLanguage,
                    secondaryLanguage = secondaryLanguage,
                    downloadedBookKeys = downloadedBookKeys,
                    fontSizeSp = fontSizeSp,
                    selectedVerseKey = selectedVerseKey,
                    activeAudioVerseKey = activeAudioVerseKey,
                    targetScrollVerse = null,
                    bookmarkedKeys = bookmarkedKeys,
                    highlightMap = highlightMap,
                    noteKeys = noteKeys,
                    onVerseClick = {},
                    isStaticPreview = true
                )

                if (readingMode == ReadingMode.BILINGUAL_SPREAD) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                clip = true
                                shape = HalfRectShape(leftHalf = true)
                            }
                    ) {
                        BiblePageSheet(
                            book = book,
                            chapter = chapter,
                            verses = verses,
                            readingMode = readingMode,
                            primaryLanguage = primaryLanguage,
                            secondaryLanguage = secondaryLanguage,
                            downloadedBookKeys = downloadedBookKeys,
                            fontSizeSp = fontSizeSp,
                            selectedVerseKey = selectedVerseKey,
                            activeAudioVerseKey = activeAudioVerseKey,
                            targetScrollVerse = null,
                            bookmarkedKeys = bookmarkedKeys,
                            highlightMap = highlightMap,
                            noteKeys = noteKeys,
                            onVerseClick = {},
                            listState = listState,
                            isStaticPreview = true
                        )
                    }

                    val angle = -180f * forwardProgress
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                cameraDistance = 16f * density.density
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                                rotationY = angle
                                clip = true
                                shape = HalfRectShape(leftHalf = forwardProgress >= 0.5f)
                            }
                    ) {
                        if (forwardProgress < 0.5f) {
                            BiblePageSheet(
                                book = book,
                                chapter = chapter,
                                verses = verses,
                                readingMode = readingMode,
                                primaryLanguage = primaryLanguage,
                                secondaryLanguage = secondaryLanguage,
                                downloadedBookKeys = downloadedBookKeys,
                                fontSizeSp = fontSizeSp,
                                selectedVerseKey = selectedVerseKey,
                                activeAudioVerseKey = activeAudioVerseKey,
                                targetScrollVerse = null,
                                bookmarkedKeys = bookmarkedKeys,
                                highlightMap = highlightMap,
                                noteKeys = noteKeys,
                                onVerseClick = {},
                                listState = listState,
                                isStaticPreview = true
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f }
                            ) {
                                BiblePageSheet(
                                    book = targetBook,
                                    chapter = targetChap,
                                    verses = targetVerses,
                                    readingMode = readingMode,
                                    primaryLanguage = primaryLanguage,
                                    secondaryLanguage = secondaryLanguage,
                                    downloadedBookKeys = downloadedBookKeys,
                                    fontSizeSp = fontSizeSp,
                                    selectedVerseKey = selectedVerseKey,
                                    activeAudioVerseKey = activeAudioVerseKey,
                                    targetScrollVerse = null,
                                    bookmarkedKeys = bookmarkedKeys,
                                    highlightMap = highlightMap,
                                    noteKeys = noteKeys,
                                    onVerseClick = {},
                                    isStaticPreview = true
                                )
                            }
                        }
                    }
                } else {
                    val curlFraction = 1f - forwardProgress
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                cameraDistance = 16f * density.density
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                rotationY = -12f * sin(Math.PI * forwardProgress).toFloat()
                                clip = true
                                shape = HorizontalClipShape(startFraction = 0f, endFraction = curlFraction)
                            }
                    ) {
                        BiblePageSheet(
                            book = book,
                            chapter = chapter,
                            verses = verses,
                            readingMode = readingMode,
                            primaryLanguage = primaryLanguage,
                            secondaryLanguage = secondaryLanguage,
                            downloadedBookKeys = downloadedBookKeys,
                            fontSizeSp = fontSizeSp,
                            selectedVerseKey = selectedVerseKey,
                            activeAudioVerseKey = activeAudioVerseKey,
                            targetScrollVerse = null,
                            bookmarkedKeys = bookmarkedKeys,
                            highlightMap = highlightMap,
                            noteKeys = noteKeys,
                            onVerseClick = {},
                            listState = listState,
                            isStaticPreview = true
                        )
                    }
                }

                PageCurlShadowCanvas(
                    progress = forwardProgress,
                    isForward = true,
                    isSpread = readingMode == ReadingMode.BILINGUAL_SPREAD,
                    touchYFraction = dragTouchYFraction,
                    paperColor = palette.paperBackground,
                    paperEdgeColor = palette.paperEdge,
                    shadowColor = palette.spineShadow
                )
            } else {
                val backwardProgress = p.coerceIn(0f, 1f)
                val targetBook = prevSnapshot?.book ?: book
                val targetChap = prevSnapshot?.chapter ?: chapter
                val targetVerses = prevSnapshot?.verses ?: emptyList()

                BiblePageSheet(
                    book = targetBook,
                    chapter = targetChap,
                    verses = targetVerses,
                    readingMode = readingMode,
                    primaryLanguage = primaryLanguage,
                    secondaryLanguage = secondaryLanguage,
                    downloadedBookKeys = downloadedBookKeys,
                    fontSizeSp = fontSizeSp,
                    selectedVerseKey = selectedVerseKey,
                    activeAudioVerseKey = activeAudioVerseKey,
                    targetScrollVerse = null,
                    bookmarkedKeys = bookmarkedKeys,
                    highlightMap = highlightMap,
                    noteKeys = noteKeys,
                    onVerseClick = {},
                    isStaticPreview = true
                )

                if (readingMode == ReadingMode.BILINGUAL_SPREAD) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                clip = true
                                shape = HalfRectShape(leftHalf = false)
                            }
                    ) {
                        BiblePageSheet(
                            book = book,
                            chapter = chapter,
                            verses = verses,
                            readingMode = readingMode,
                            primaryLanguage = primaryLanguage,
                            secondaryLanguage = secondaryLanguage,
                            downloadedBookKeys = downloadedBookKeys,
                            fontSizeSp = fontSizeSp,
                            selectedVerseKey = selectedVerseKey,
                            activeAudioVerseKey = activeAudioVerseKey,
                            targetScrollVerse = null,
                            bookmarkedKeys = bookmarkedKeys,
                            highlightMap = highlightMap,
                            noteKeys = noteKeys,
                            onVerseClick = {},
                            listState = listState,
                            isStaticPreview = true
                        )
                    }

                    val angle = 180f * backwardProgress
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                cameraDistance = 16f * density.density
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                                rotationY = angle
                                clip = true
                                shape = HalfRectShape(leftHalf = backwardProgress < 0.5f)
                            }
                    ) {
                        if (backwardProgress < 0.5f) {
                            BiblePageSheet(
                                book = book,
                                chapter = chapter,
                                verses = verses,
                                readingMode = readingMode,
                                primaryLanguage = primaryLanguage,
                                secondaryLanguage = secondaryLanguage,
                                downloadedBookKeys = downloadedBookKeys,
                                fontSizeSp = fontSizeSp,
                                selectedVerseKey = selectedVerseKey,
                                activeAudioVerseKey = activeAudioVerseKey,
                                targetScrollVerse = null,
                                bookmarkedKeys = bookmarkedKeys,
                                highlightMap = highlightMap,
                                noteKeys = noteKeys,
                                onVerseClick = {},
                                listState = listState,
                                isStaticPreview = true
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f }
                            ) {
                                BiblePageSheet(
                                    book = targetBook,
                                    chapter = targetChap,
                                    verses = targetVerses,
                                    readingMode = readingMode,
                                    primaryLanguage = primaryLanguage,
                                    secondaryLanguage = secondaryLanguage,
                                    downloadedBookKeys = downloadedBookKeys,
                                    fontSizeSp = fontSizeSp,
                                    selectedVerseKey = selectedVerseKey,
                                    activeAudioVerseKey = activeAudioVerseKey,
                                    targetScrollVerse = null,
                                    bookmarkedKeys = bookmarkedKeys,
                                    highlightMap = highlightMap,
                                    noteKeys = noteKeys,
                                    onVerseClick = {},
                                    isStaticPreview = true
                                )
                            }
                        }
                    }
                } else {
                    val curlStart = backwardProgress
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                cameraDistance = 16f * density.density
                                transformOrigin = TransformOrigin(1f, 0.5f)
                                rotationY = 12f * sin(Math.PI * backwardProgress).toFloat()
                                clip = true
                                shape = HorizontalClipShape(startFraction = curlStart, endFraction = 1f)
                            }
                    ) {
                        BiblePageSheet(
                            book = book,
                            chapter = chapter,
                            verses = verses,
                            readingMode = readingMode,
                            primaryLanguage = primaryLanguage,
                            secondaryLanguage = secondaryLanguage,
                            downloadedBookKeys = downloadedBookKeys,
                            fontSizeSp = fontSizeSp,
                            selectedVerseKey = selectedVerseKey,
                            activeAudioVerseKey = activeAudioVerseKey,
                            targetScrollVerse = null,
                            bookmarkedKeys = bookmarkedKeys,
                            highlightMap = highlightMap,
                            noteKeys = noteKeys,
                            onVerseClick = {},
                            listState = listState,
                            isStaticPreview = true
                        )
                    }
                }

                PageCurlShadowCanvas(
                    progress = backwardProgress,
                    isForward = false,
                    isSpread = readingMode == ReadingMode.BILINGUAL_SPREAD,
                    touchYFraction = dragTouchYFraction,
                    paperColor = palette.paperBackground,
                    paperEdgeColor = palette.paperEdge,
                    shadowColor = palette.spineShadow
                )
            }
        }
    }
}

@Composable
private fun PageCurlShadowCanvas(
    progress: Float,
    isForward: Boolean,
    isSpread: Boolean,
    touchYFraction: Float,
    paperColor: Color,
    paperEdgeColor: Color,
    shadowColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val arcIntensity = sin(Math.PI * progress).toFloat().coerceIn(0f, 1f)

        val foldX = if (isSpread) {
            val cosFactor = cos(Math.PI * progress).toFloat()
            if (isForward) w * (0.5f + 0.5f * cosFactor) else w * (0.5f - 0.5f * cosFactor)
        } else {
            if (isForward) w * (1f - progress) else w * progress
        }

        val curlWidth = (w * 0.13f * arcIntensity).coerceAtLeast(6f)
        val bowOffset = (w * 0.045f * arcIntensity)
        val anchorY = h * touchYFraction

        val shadowSpan = (w * 0.18f * arcIntensity).coerceAtLeast(10f)
        if (isForward) {
            val shadowLeft = foldX.coerceIn(0f, w)
            val shadowRight = (foldX + shadowSpan).coerceAtMost(w)
            if (shadowRight > shadowLeft) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            shadowColor.copy(alpha = 0.34f * arcIntensity),
                            shadowColor.copy(alpha = 0.12f * arcIntensity),
                            Color.Transparent
                        ),
                        startX = shadowLeft,
                        endX = shadowRight
                    ),
                    topLeft = Offset(shadowLeft, 0f),
                    size = Size(shadowRight - shadowLeft, h)
                )
            }
        } else {
            val shadowRight = foldX.coerceIn(0f, w)
            val shadowLeft = (foldX - shadowSpan).coerceAtLeast(0f)
            if (shadowRight > shadowLeft) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            shadowColor.copy(alpha = 0.12f * arcIntensity),
                            shadowColor.copy(alpha = 0.34f * arcIntensity)
                        ),
                        startX = shadowLeft,
                        endX = shadowRight
                    ),
                    topLeft = Offset(shadowLeft, 0f),
                    size = Size(shadowRight - shadowLeft, h)
                )
            }
        }

        val flapPath = Path().apply {
            if (isForward) {
                val leftEdge = (foldX - curlWidth).coerceAtLeast(0f)
                moveTo(foldX, 0f)
                quadraticTo(foldX - bowOffset, anchorY, foldX, h)
                lineTo(leftEdge, h)
                quadraticTo(leftEdge - bowOffset * 0.5f, anchorY, leftEdge, 0f)
                close()
            } else {
                val rightEdge = (foldX + curlWidth).coerceAtMost(w)
                moveTo(foldX, 0f)
                quadraticTo(foldX + bowOffset, anchorY, foldX, h)
                lineTo(rightEdge, h)
                quadraticTo(rightEdge + bowOffset * 0.5f, anchorY, rightEdge, 0f)
                close()
            }
        }

        val flapStartX = if (isForward) (foldX - curlWidth).coerceAtLeast(0f) else foldX
        val flapEndX = if (isForward) foldX else (foldX + curlWidth).coerceAtMost(w)
        if (flapEndX > flapStartX) {
            drawPath(
                path = flapPath,
                brush = Brush.horizontalGradient(
                    colors = if (isForward) {
                        listOf(
                            shadowColor.copy(alpha = 0.16f * arcIntensity),
                            paperColor.copy(alpha = 0.94f),
                            paperEdgeColor.copy(alpha = 0.98f),
                            shadowColor.copy(alpha = 0.20f * arcIntensity)
                        )
                    } else {
                        listOf(
                            shadowColor.copy(alpha = 0.20f * arcIntensity),
                            paperEdgeColor.copy(alpha = 0.98f),
                            paperColor.copy(alpha = 0.94f),
                            shadowColor.copy(alpha = 0.16f * arcIntensity)
                        )
                    },
                    startX = flapStartX,
                    endX = flapEndX
                )
            )
        }

        if (isSpread) {
            val spineX = w * 0.5f
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        shadowColor.copy(alpha = 0.22f * arcIntensity),
                        Color.Transparent
                    ),
                    startX = spineX - 18f,
                    endX = spineX + 18f
                ),
                topLeft = Offset(spineX - 18f, 0f),
                size = Size(36f, h)
            )
        }
    }
}

private class HalfRectShape(private val leftHalf: Boolean) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val rect = if (leftHalf) {
            Rect(0f, 0f, size.width / 2f, size.height)
        } else {
            Rect(size.width / 2f, 0f, size.width, size.height)
        }
        return androidx.compose.ui.graphics.Outline.Rectangle(rect)
    }
}

private class HorizontalClipShape(
    private val startFraction: Float,
    private val endFraction: Float
) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val left = (size.width * startFraction).coerceIn(0f, size.width)
        val right = (size.width * endFraction).coerceIn(left, size.width)
        return androidx.compose.ui.graphics.Outline.Rectangle(
            Rect(left, 0f, right, size.height)
        )
    }
}
