package com.example.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.BibleVerse
import com.example.data.model.HighlightColor
import com.example.data.model.ReadingMode
import com.example.data.model.ScriptureFontStyle
import com.example.data.model.SystemLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.BibleBookPalette
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.util.DevanagariNumberUtils
import com.example.util.NepaliFormatUtils
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun BiblePageSheet(
    book: BibleBook,
    chapter: Int,
    verses: List<BibleVerse>,
    readingMode: ReadingMode,
    primaryLanguage: BibleLanguage = BibleLanguage.ENGLISH,
    secondaryLanguage: BibleLanguage = BibleLanguage.NEPALI,
    downloadedBookKeys: Set<Pair<String, Int>> = emptySet(),
    fontSizeSp: Float,
    selectedVerseKey: String?,
    activeAudioVerseKey: String?,
    targetScrollVerse: Int?,
    bookmarkedKeys: Set<String>,
    highlightMap: Map<String, HighlightColor>,
    noteKeys: Set<String>,
    onVerseClick: (BibleVerse) -> Unit,
    onTopVisibleVerseChanged: (Int) -> Unit = {},
    onDownloadLanguageBook: (BibleLanguage, Int) -> Unit = { _, _ -> },
    onOpenDownloadsManager: () -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    isStaticPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = LocalBiblePalette.current
    val fontStyle = LocalLiquidGlass.current.fontStyle

    if (!isStaticPreview) {
        LaunchedEffect(book.id, chapter, targetScrollVerse, verses.size) {
            if (targetScrollVerse != null && verses.isNotEmpty()) {
                val idx = verses.indexOfFirst { it.verse == targetScrollVerse }
                if (idx >= 0) {
                    listState.animateScrollToItem((idx + 1).coerceAtMost(verses.size))
                }
            }
        }

        LaunchedEffect(book.id, chapter, verses) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .map { index ->
                    val verseIdx = (index - 1).coerceIn(0, (verses.size - 1).coerceAtLeast(0))
                    verses.getOrNull(verseIdx)?.verse ?: 1
                }
                .distinctUntilChanged()
                .collect { visibleVerse ->
                    onTopVisibleVerseChanged(visibleVerse)
                }
        }
    }

    val primaryHasContent = remember(verses, primaryLanguage, downloadedBookKeys, book.id) {
        downloadedBookKeys.contains(primaryLanguage.code to book.id) ||
            verses.any { it.textFor(primaryLanguage).isNotBlank() }
    }
    val secondaryHasContent = remember(verses, secondaryLanguage, downloadedBookKeys, book.id) {
        downloadedBookKeys.contains(secondaryLanguage.code to book.id) ||
            verses.any { it.textFor(secondaryLanguage).isNotBlank() }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(palette.paperBackground)
    ) {
        val isCompactWidth = maxWidth < 380.dp

        Column(modifier = Modifier.fillMaxSize()) {
            RunningBookHeader(
                book = book,
                chapter = chapter,
                readingMode = readingMode,
                primaryLanguage = primaryLanguage,
                secondaryLanguage = secondaryLanguage,
                palette = palette,
                fontStyle = fontStyle,
                isCompact = isCompactWidth
            )

            if (isStaticPreview) {
                val startIdx = (listState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                val previewVerses = verses.drop(startIdx).take(14)
                Column(modifier = Modifier.fillMaxSize()) {
                    if (startIdx == 0) {
                        ChapterTitleBanner(
                            book = book,
                            chapter = chapter,
                            readingMode = readingMode,
                            primaryLanguage = primaryLanguage,
                            secondaryLanguage = secondaryLanguage,
                            palette = palette,
                            fontStyle = fontStyle,
                            isCompact = isCompactWidth
                        )
                    }
                    previewVerses.forEach { verse ->
                        VerseContentItem(
                            verse = verse,
                            readingMode = readingMode,
                            primaryLanguage = primaryLanguage,
                            secondaryLanguage = secondaryLanguage,
                            fontSizeSp = fontSizeSp,
                            fontStyle = fontStyle,
                            selectedVerseKey = selectedVerseKey,
                            activeAudioVerseKey = activeAudioVerseKey,
                            bookmarkedKeys = bookmarkedKeys,
                            highlightMap = highlightMap,
                            noteKeys = noteKeys,
                            palette = palette,
                            isCompact = isCompactWidth,
                            onVerseClick = {}
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("bible_chapter_list"),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 108.dp)
                ) {
                    item(key = "chapter_banner_${book.id}_$chapter") {
                        ChapterTitleBanner(
                            book = book,
                            chapter = chapter,
                            readingMode = readingMode,
                            primaryLanguage = primaryLanguage,
                            secondaryLanguage = secondaryLanguage,
                            palette = palette,
                            fontStyle = fontStyle,
                            isCompact = isCompactWidth
                        )
                    }

                    // Show helpful offline download or testament scope banner when needed
                    val showPrimaryMissing = readingMode != ReadingMode.SINGLE_SECONDARY && !primaryHasContent
                    val showSecondaryMissing =
                        (readingMode == ReadingMode.BILINGUAL_SPREAD ||
                            readingMode == ReadingMode.BILINGUAL_PARALLEL ||
                            readingMode == ReadingMode.SINGLE_SECONDARY) &&
                            !secondaryHasContent

                    if (showPrimaryMissing || showSecondaryMissing) {
                        item(key = "lang_status_notice_${book.id}_${primaryLanguage.code}_${secondaryLanguage.code}") {
                            LanguageAvailabilityBanner(
                                book = book,
                                showPrimaryMissing = showPrimaryMissing,
                                showSecondaryMissing = showSecondaryMissing,
                                primaryLanguage = primaryLanguage,
                                secondaryLanguage = secondaryLanguage,
                                palette = palette,
                                onDownloadLanguageBook = onDownloadLanguageBook,
                                onOpenDownloadsManager = onOpenDownloadsManager
                            )
                        }
                    }

                    items(
                        items = verses,
                        key = { it.key }
                    ) { verse ->
                        VerseContentItem(
                            verse = verse,
                            readingMode = readingMode,
                            primaryLanguage = primaryLanguage,
                            secondaryLanguage = secondaryLanguage,
                            fontSizeSp = fontSizeSp,
                            fontStyle = fontStyle,
                            selectedVerseKey = selectedVerseKey,
                            activeAudioVerseKey = activeAudioVerseKey,
                            bookmarkedKeys = bookmarkedKeys,
                            highlightMap = highlightMap,
                            noteKeys = noteKeys,
                            palette = palette,
                            isCompact = isCompactWidth,
                            onVerseClick = onVerseClick
                        )
                    }

                    item(key = "chapter_footer_${book.id}_$chapter") {
                        ChapterEndFooter(
                            book = book,
                            chapter = chapter,
                            totalVerses = verses.size,
                            readingMode = readingMode,
                            primaryLanguage = primaryLanguage,
                            secondaryLanguage = secondaryLanguage,
                            palette = palette
                        )
                    }
                }
            }
        }

        if (readingMode == ReadingMode.BILINGUAL_SPREAD) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(14.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                palette.spineShadow.copy(alpha = if (palette.isDark) 0.45f else 0.14f),
                                palette.spineShadow.copy(alpha = if (palette.isDark) 0.62f else 0.24f),
                                palette.spineShadow.copy(alpha = if (palette.isDark) 0.42f else 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun LanguageAvailabilityBanner(
    book: BibleBook,
    showPrimaryMissing: Boolean,
    showSecondaryMissing: Boolean,
    primaryLanguage: BibleLanguage,
    secondaryLanguage: BibleLanguage,
    palette: BibleBookPalette,
    onDownloadLanguageBook: (BibleLanguage, Int) -> Unit,
    onOpenDownloadsManager: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showPrimaryMissing) {
            MissingLanguageCard(
                book = book,
                language = primaryLanguage,
                palette = palette,
                onDownloadBook = { onDownloadLanguageBook(primaryLanguage, book.id) },
                onOpenDownloadsManager = onOpenDownloadsManager
            )
        }
        if (showSecondaryMissing && (!showPrimaryMissing || secondaryLanguage != primaryLanguage)) {
            MissingLanguageCard(
                book = book,
                language = secondaryLanguage,
                palette = palette,
                onDownloadBook = { onDownloadLanguageBook(secondaryLanguage, book.id) },
                onOpenDownloadsManager = onOpenDownloadsManager
            )
        }
    }
}

@Composable
private fun MissingLanguageCard(
    book: BibleBook,
    language: BibleLanguage,
    palette: BibleBookPalette,
    onDownloadBook: () -> Unit,
    onOpenDownloadsManager: () -> Unit
) {
    val isSupported = language.supportsBook(book.id)
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val bookLabel = if (sysLang == SystemLanguage.NEPALI) book.nameNe else book.nameEn
    val langLabel = strings.bibleLanguageName(language)
    LiquidGlassSurface(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = palette.leatherAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSupported) {
                        strings.langNotDownloadedTitle(langLabel, bookLabel)
                    } else {
                        strings.langScopeNoticeTitle(langLabel)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = palette.leatherAccent
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSupported) {
                    strings.langNotDownloadedBody(bookLabel, language.nativeName)
                } else {
                    strings.langUnavailableBody(bookLabel, langLabel)
                },
                fontSize = 12.sp,
                color = palette.inkSecondary
            )
            if (isSupported) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDownloadBook,
                        colors = ButtonDefaults.buttonColors(containerColor = palette.leatherAccent),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("inline_download_${language.code}_${book.id}")
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.downloadBookButton(bookLabel), fontSize = 11.5.sp)
                    }
                    OutlinedButton(
                        onClick = onOpenDownloadsManager,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(strings.allDownloadsButton, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RunningBookHeader(
    book: BibleBook,
    chapter: Int,
    readingMode: ReadingMode,
    primaryLanguage: BibleLanguage,
    secondaryLanguage: BibleLanguage,
    palette: BibleBookPalette,
    fontStyle: ScriptureFontStyle,
    isCompact: Boolean
) {
    val hPad = if (isCompact) 10.dp else 16.dp
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = hPad, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (readingMode != ReadingMode.SINGLE_SECONDARY) {
                Text(
                    text = formatHeaderTitle(book, chapter, primaryLanguage),
                    fontFamily = primaryLanguage.fontFamily(fontStyle),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            if (readingMode != ReadingMode.SINGLE_PRIMARY) {
                Text(
                    text = formatHeaderTitle(book, chapter, secondaryLanguage),
                    fontFamily = secondaryLanguage.fontFamily(fontStyle),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = if (readingMode == ReadingMode.SINGLE_SECONDARY) TextAlign.Start else TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        HorizontalDivider(
            thickness = 0.7.dp,
            color = palette.goldAccent.copy(alpha = 0.35f)
        )
    }
}

@Composable
private fun ChapterTitleBanner(
    book: BibleBook,
    chapter: Int,
    readingMode: ReadingMode,
    primaryLanguage: BibleLanguage,
    secondaryLanguage: BibleLanguage,
    palette: BibleBookPalette,
    fontStyle: ScriptureFontStyle,
    isCompact: Boolean
) {
    val primaryHeadingFont = if (primaryLanguage == BibleLanguage.ENGLISH) {
        if (fontStyle == ScriptureFontStyle.CLASSIC_SERIF) PlayfairDisplayFamily else fontStyle.englishFontFamily()
    } else {
        primaryLanguage.fontFamily(fontStyle)
    }
    val secondaryHeadingFont = if (secondaryLanguage == BibleLanguage.ENGLISH) {
        if (fontStyle == ScriptureFontStyle.CLASSIC_SERIF) PlayfairDisplayFamily else fontStyle.englishFontFamily()
    } else {
        secondaryLanguage.fontFamily(fontStyle)
    }

    if (readingMode == ReadingMode.BILINGUAL_SPREAD) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(top = 10.dp, bottom = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (isCompact) 8.dp else 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = book.nameFor(primaryLanguage),
                    fontFamily = primaryHeadingFont,
                    fontSize = if (isCompact) 17.5.sp else 19.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.leatherAccent,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = formatChapterLabel(chapter, primaryLanguage),
                    fontFamily = primaryHeadingFont,
                    fontStyle = if (primaryLanguage == BibleLanguage.ENGLISH) FontStyle.Italic else FontStyle.Normal,
                    fontSize = 12.5.sp,
                    color = palette.goldAccent
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (isCompact) 8.dp else 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = book.nameFor(secondaryLanguage),
                    fontFamily = secondaryHeadingFont,
                    fontSize = if (isCompact) 17.5.sp else 19.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.leatherAccent,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = formatChapterLabel(chapter, secondaryLanguage),
                    fontFamily = secondaryHeadingFont,
                    fontStyle = if (secondaryLanguage == BibleLanguage.ENGLISH) FontStyle.Italic else FontStyle.Normal,
                    fontSize = 12.5.sp,
                    color = palette.goldAccent
                )
            }
        }
    } else {
        val activeSingleLang = if (readingMode == ReadingMode.SINGLE_SECONDARY) secondaryLanguage else primaryLanguage
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (readingMode) {
                    ReadingMode.SINGLE_PRIMARY -> book.nameFor(primaryLanguage)
                    ReadingMode.SINGLE_SECONDARY -> book.nameFor(secondaryLanguage)
                    else -> "${book.nameFor(primaryLanguage)} • ${book.nameFor(secondaryLanguage)}"
                },
                fontFamily = if (readingMode == ReadingMode.BILINGUAL_PARALLEL || activeSingleLang == BibleLanguage.ENGLISH) {
                    if (fontStyle == ScriptureFontStyle.CLASSIC_SERIF) PlayfairDisplayFamily else fontStyle.englishFontFamily()
                } else {
                    activeSingleLang.fontFamily(fontStyle)
                },
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = palette.leatherAccent,
                textAlign = TextAlign.Center
            )
            Text(
                text = when (readingMode) {
                    ReadingMode.SINGLE_PRIMARY -> formatChapterLabel(chapter, primaryLanguage)
                    ReadingMode.SINGLE_SECONDARY -> formatChapterLabel(chapter, secondaryLanguage)
                    else -> "${formatChapterLabel(chapter, primaryLanguage)} • ${formatChapterLabel(chapter, secondaryLanguage)}"
                },
                fontFamily = PlayfairDisplayFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                color = palette.goldAccent,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VerseContentItem(
    verse: BibleVerse,
    readingMode: ReadingMode,
    primaryLanguage: BibleLanguage,
    secondaryLanguage: BibleLanguage,
    fontSizeSp: Float,
    fontStyle: ScriptureFontStyle,
    selectedVerseKey: String?,
    activeAudioVerseKey: String?,
    bookmarkedKeys: Set<String>,
    highlightMap: Map<String, HighlightColor>,
    noteKeys: Set<String>,
    palette: BibleBookPalette,
    isCompact: Boolean,
    onVerseClick: (BibleVerse) -> Unit
) {
    val isSelected = verse.key == selectedVerseKey
    val isAudioActive = verse.key == activeAudioVerseKey
    val highlight = highlightMap[verse.key] ?: HighlightColor.NONE
    val isBookmarked = bookmarkedKeys.contains(verse.key)
    val hasNote = noteKeys.contains(verse.key)
    val bgColor = resolveVerseBackground(isAudioActive, isSelected, highlight, palette)

    if (readingMode == ReadingMode.BILINGUAL_SPREAD) {
        val outerPad = if (isCompact) 8.dp else 14.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onVerseClick(verse) }
                .padding(vertical = 3.dp)
        ) {
            // Left Primary Page Verse
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = outerPad, end = 6.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(bgColor)
                    .padding(horizontal = 5.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LanguageVerseText(
                        verseNumber = verse.verse,
                        text = verse.textFor(primaryLanguage),
                        language = primaryLanguage,
                        fontSizeSp = fontSizeSp * 0.95f,
                        fontStyle = fontStyle,
                        palette = palette
                    )
                    if (isBookmarked || hasNote) {
                        AnnotationIconsRow(isBookmarked, hasNote, palette)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right Secondary Page Verse
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 6.dp, end = outerPad)
                    .clip(RoundedCornerShape(5.dp))
                    .background(bgColor)
                    .padding(horizontal = 5.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LanguageVerseText(
                        verseNumber = verse.verse,
                        text = verse.textFor(secondaryLanguage),
                        language = secondaryLanguage,
                        fontSizeSp = fontSizeSp * 0.95f,
                        fontStyle = fontStyle,
                        palette = palette
                    )
                    if (isBookmarked || hasNote) {
                        AnnotationIconsRow(isBookmarked, hasNote, palette)
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompact) 12.dp else 18.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .clickable { onVerseClick(verse) }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (readingMode == ReadingMode.SINGLE_PRIMARY || readingMode == ReadingMode.BILINGUAL_PARALLEL) {
                LanguageVerseText(
                    verseNumber = verse.verse,
                    text = verse.textFor(primaryLanguage),
                    language = primaryLanguage,
                    fontSizeSp = fontSizeSp,
                    fontStyle = fontStyle,
                    palette = palette
                )
            }

            if (readingMode == ReadingMode.BILINGUAL_PARALLEL) {
                Spacer(modifier = Modifier.height(5.dp))
            }

            if (readingMode == ReadingMode.SINGLE_SECONDARY || readingMode == ReadingMode.BILINGUAL_PARALLEL) {
                LanguageVerseText(
                    verseNumber = verse.verse,
                    text = verse.textFor(secondaryLanguage),
                    language = secondaryLanguage,
                    fontSizeSp = fontSizeSp * 0.98f,
                    fontStyle = fontStyle,
                    palette = palette
                )
            }

            if (isBookmarked || hasNote) {
                AnnotationIconsRow(isBookmarked, hasNote, palette)
            }
        }
    }
}

@Composable
private fun LanguageVerseText(
    verseNumber: Int,
    text: String,
    language: BibleLanguage,
    fontSizeSp: Float,
    fontStyle: ScriptureFontStyle,
    palette: BibleBookPalette
) {
    val displayNumber = if (language.usesDevanagari) {
        DevanagariNumberUtils.toDevanagari(verseNumber)
    } else {
        verseNumber.toString()
    }
    val lineMultiplier = when (language) {
        BibleLanguage.KOREAN -> 1.50f
        BibleLanguage.NEPALI, BibleLanguage.ONCV -> 1.48f
        BibleLanguage.ENGLISH -> 1.44f
    }
    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val textDirection = if (language.isRtl) TextDirection.Rtl else TextDirection.Ltr
    val textAlign = if (language.isRtl) TextAlign.Right else TextAlign.Start

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = palette.verseNumberInk,
                        fontSize = (fontSizeSp * 0.72f).sp,
                        fontWeight = FontWeight.Bold,
                        baselineShift = BaselineShift.Superscript,
                        fontFamily = PlusJakartaSansFamily
                    )
                ) {
                    append("$displayNumber ")
                }
                withStyle(
                    SpanStyle(
                        color = if (text.isBlank()) palette.inkSecondary else palette.inkPrimary,
                        fontSize = fontSizeSp.sp,
                        fontFamily = language.fontFamily(fontStyle)
                    )
                ) {
                    append(text.ifBlank { "—" })
                }
            },
            style = TextStyle(
                textDirection = textDirection,
                textAlign = textAlign,
                lineHeight = (fontSizeSp * lineMultiplier).sp
            ),
            softWrap = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AnnotationIconsRow(
    isBookmarked: Boolean,
    hasNote: Boolean,
    palette: BibleBookPalette
) {
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isBookmarked) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = strings.bookmarkedDesc,
                tint = palette.leatherAccent,
                modifier = Modifier.size(12.dp)
            )
        }
        if (hasNote) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = strings.hasNoteDesc,
                tint = palette.goldAccent,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun ChapterEndFooter(
    book: BibleBook,
    chapter: Int,
    totalVerses: Int,
    readingMode: ReadingMode,
    primaryLanguage: BibleLanguage,
    secondaryLanguage: BibleLanguage,
    palette: BibleBookPalette
) {
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val chPrimary = if (primaryLanguage.usesDevanagari || sysLang == SystemLanguage.NEPALI) {
        DevanagariNumberUtils.toDevanagari(chapter)
    } else {
        chapter.toString()
    }
    val chSecondary = if (secondaryLanguage.usesDevanagari || sysLang == SystemLanguage.NEPALI) {
        DevanagariNumberUtils.toDevanagari(chapter)
    } else {
        chapter.toString()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            thickness = 0.7.dp,
            color = palette.goldAccent.copy(alpha = 0.35f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = when (readingMode) {
                ReadingMode.SINGLE_PRIMARY ->
                    "✦ ${book.nameFor(primaryLanguage)} $chPrimary (${strings.versesCountLabel(totalVerses)}) ✦"
                ReadingMode.SINGLE_SECONDARY ->
                    "✦ ${book.nameFor(secondaryLanguage)} $chSecondary (${strings.versesCountLabel(totalVerses)}) ✦"
                else ->
                    "✦ ${book.nameFor(primaryLanguage)} $chPrimary (${NepaliFormatUtils.formatNumber(totalVerses, sysLang)}) • ${book.nameFor(secondaryLanguage)} $chSecondary ✦"
            },
            fontFamily = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily,
            fontSize = 12.sp,
            color = palette.inkSecondary,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatHeaderTitle(book: BibleBook, chapter: Int, language: BibleLanguage): String {
    val name = book.nameFor(language)
    return when (language) {
        BibleLanguage.ENGLISH -> "${name.uppercase()} $chapter"
        BibleLanguage.NEPALI, BibleLanguage.ONCV -> "$name ${DevanagariNumberUtils.toDevanagari(chapter)}"
        BibleLanguage.KOREAN -> "$name $chapter"
    }
}

private fun formatChapterLabel(chapter: Int, language: BibleLanguage): String = when (language) {
    BibleLanguage.ENGLISH -> "Chapter $chapter"
    BibleLanguage.NEPALI, BibleLanguage.ONCV -> "अध्याय ${DevanagariNumberUtils.toDevanagari(chapter)}"
    BibleLanguage.KOREAN -> "제${chapter}장"
}

private fun resolveVerseBackground(
    isAudioActive: Boolean,
    isSelected: Boolean,
    highlight: HighlightColor,
    palette: BibleBookPalette
): Color {
    return when {
        isAudioActive -> palette.activeVerseTint
        isSelected -> palette.goldAccent.copy(alpha = 0.22f)
        highlight == HighlightColor.GOLD -> palette.highlightGold
        highlight == HighlightColor.ROSE -> palette.highlightRose
        highlight == HighlightColor.SAGE -> palette.highlightSage
        highlight == HighlightColor.SKY -> palette.highlightSky
        else -> Color.Transparent
    }
}
