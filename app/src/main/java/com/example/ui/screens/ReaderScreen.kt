package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackState
import com.example.data.db.NoteEntity
import com.example.data.model.AudioLanguage
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.BiblePreferencesState
import com.example.data.model.BibleVerse
import com.example.data.model.HighlightColor
import com.example.data.model.LanguageDownloadSummary
import com.example.data.model.ReadingMode
import com.example.ui.ReaderUiState
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.components.NoteEditorDialog
import com.example.ui.components.ScripturePickerSheet
import com.example.ui.components.VerseActionBar
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.reader.FlipBookPager
import com.example.ui.reader.rememberFlipBookController
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.util.DevanagariNumberUtils

@Composable
fun ReaderScreen(
    readerState: ReaderUiState,
    books: List<BibleBook>,
    preferences: BiblePreferencesState,
    audioState: AudioPlaybackState,
    languageSummaries: List<LanguageDownloadSummary>,
    downloadedBookKeys: Set<Pair<String, Int>>,
    bookmarkedKeys: Set<String>,
    highlightMap: Map<String, HighlightColor>,
    notesMap: Map<String, NoteEntity>,
    loadChapterVerseCount: suspend (Int, Int) -> Int,
    onBack: () -> Unit,
    onOpenLocation: (bookId: Int, chapter: Int, verse: Int?) -> Unit,
    onNextChapter: () -> Unit,
    onPrevChapter: () -> Unit,
    onClearTargetHighlight: () -> Unit,
    onSavePageProgress: (Int) -> Unit,
    onSelectVerse: (BibleVerse?) -> Unit,
    onToggleBookmark: (BibleVerse) -> Unit,
    onSetHighlight: (BibleVerse, HighlightColor) -> Unit,
    onSaveNote: (BibleVerse, String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onSetReadingMode: (ReadingMode) -> Unit,
    onSelectPrimaryLanguage: (BibleLanguage) -> Unit,
    onSelectSecondaryLanguage: (BibleLanguage) -> Unit,
    onSwapLanguages: () -> Unit,
    onDownloadLanguage: (BibleLanguage) -> Unit,
    onDownloadBook: (BibleLanguage, Int) -> Unit,
    onTogglePageSound: (Boolean) -> Unit,
    onPlayForwardSound: () -> Unit,
    onPlayBackwardSound: () -> Unit,
    onStartAudio: (Int) -> Unit,
    onToggleAudioPlayPause: () -> Unit,
    onAudioPrevVerse: () -> Unit,
    onAudioNextVerse: () -> Unit,
    onAudioLanguageChange: (AudioLanguage) -> Unit,
    onAudioSpeedChange: (Float) -> Unit,
    onStopAudio: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    var showBookPicker by remember { mutableStateOf(false) }
    var showLanguageSelector by remember { mutableStateOf(false) }
    var noteEditingVerse by remember { mutableStateOf<BibleVerse?>(null) }
    val flipController = rememberFlipBookController()

    BackHandler {
        if (readerState.selectedVerse != null) {
            onSelectVerse(null)
        } else {
            onBack()
        }
    }

    val book = readerState.currentBook
    val primaryLang = preferences.primaryLanguage
    val secondaryLang = preferences.secondaryLanguage

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.deskBackground)
    ) {
        // Reader Top Liquid Glass Toolbar
        LiquidGlassSurface(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            elevated = true,
            tokens = tokens,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("reader_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.backToHomeDesc,
                        tint = tokens.textPrimary
                    )
                }

                // Book & Chapter Selector Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.accentColor.copy(alpha = if (tokens.isDark) 0.24f else 0.14f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showBookPicker = true }
                        .testTag("reader_book_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val primaryChap = if (primaryLang.usesDevanagari) {
                                DevanagariNumberUtils.toDevanagari(readerState.currentChapter)
                            } else {
                                readerState.currentChapter.toString()
                            }
                            Text(
                                text = if (book != null) {
                                    "${book.nameFor(primaryLang)} $primaryChap"
                                } else {
                                    strings.loading
                                },
                                fontFamily = if (primaryLang == BibleLanguage.ENGLISH) PlayfairDisplayFamily else primaryLang.fontFamily(tokens.fontStyle),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = tokens.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (book != null && preferences.readingMode != ReadingMode.SINGLE_PRIMARY) {
                                val secChap = if (secondaryLang.usesDevanagari) {
                                    DevanagariNumberUtils.toDevanagari(readerState.currentChapter)
                                } else {
                                    readerState.currentChapter.toString()
                                }
                                Text(
                                    text = "${book.nameFor(secondaryLang)} $secChap",
                                    fontFamily = secondaryLang.fontFamily(tokens.fontStyle),
                                    fontSize = 11.sp,
                                    color = tokens.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = strings.chooseBookChapterDesc,
                            tint = tokens.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Language & Compare Selector Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tokens.glassSurface,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showLanguageSelector = true }
                        .testTag("reader_language_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = strings.selectLanguagesDesc,
                            tint = tokens.accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val pillLabel = when (preferences.readingMode) {
                            ReadingMode.SINGLE_PRIMARY -> primaryLang.shortBadge
                            ReadingMode.SINGLE_SECONDARY -> secondaryLang.shortBadge
                            else -> "${primaryLang.shortBadge}•${secondaryLang.shortBadge}"
                        }
                        Text(
                            text = pillLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary,
                            maxLines = 1
                        )
                    }
                }

                // Page Flip Sound ON/OFF Toggle
                IconButton(
                    onClick = { onTogglePageSound(!preferences.pageSoundEnabled) },
                    modifier = Modifier.testTag("reader_sound_toggle")
                ) {
                    Icon(
                        imageVector = if (preferences.pageSoundEnabled) {
                            Icons.AutoMirrored.Filled.VolumeUp
                        } else {
                            Icons.AutoMirrored.Filled.VolumeOff
                        },
                        contentDescription = if (preferences.pageSoundEnabled) strings.pageSoundOnDesc else strings.pageSoundOffDesc,
                        tint = if (preferences.pageSoundEnabled) tokens.accentColor else tokens.textSecondary
                    )
                }

                // Audio Bible Toggle
                IconButton(
                    onClick = {
                        if (audioState.isActiveSession && audioState.bookId == book?.id && audioState.chapter == readerState.currentChapter) {
                            onToggleAudioPlayPause()
                        } else {
                            onStartAudio(1)
                        }
                    },
                    modifier = Modifier.testTag("reader_audio_button")
                ) {
                    Icon(
                        imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.Headphones,
                        contentDescription = strings.audioBibleDescReader,
                        tint = if (audioState.isActiveSession) palette.goldAccent else tokens.textPrimary
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("reader_settings_button")
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = strings.readerSettingsDesc,
                        tint = tokens.textPrimary
                    )
                }
            }
        }

        // Main Full-Chapter Scrollable 3D Flip-Book Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (readerState.isLoading || book == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = palette.leatherAccent)
                }
            } else {
                LaunchedEffect(audioState.isPlaying, audioState.bookId, audioState.chapter) {
                    if (audioState.isPlaying && (audioState.bookId != book.id || audioState.chapter != readerState.currentChapter)) {
                        onOpenLocation(audioState.bookId, audioState.chapter, audioState.currentVerse)
                    }
                }

                val activeAudioKey = if (audioState.isActiveSession) {
                    "${audioState.bookId}:${audioState.chapter}:${audioState.currentVerse}"
                } else null

                val targetScrollVerse = if (audioState.isPlaying && audioState.bookId == book.id && audioState.chapter == readerState.currentChapter) {
                    audioState.currentVerse
                } else {
                    readerState.targetHighlightVerse
                }

                FlipBookPager(
                    book = book,
                    chapter = readerState.currentChapter,
                    verses = readerState.verses,
                    prevSnapshot = readerState.prevChapterSnapshot,
                    nextSnapshot = readerState.nextChapterSnapshot,
                    readingMode = preferences.readingMode,
                    primaryLanguage = primaryLang,
                    secondaryLanguage = secondaryLang,
                    downloadedBookKeys = downloadedBookKeys,
                    fontSizeSp = preferences.fontSizeSp,
                    selectedVerseKey = readerState.selectedVerse?.key ?: readerState.targetHighlightVerse?.let {
                        "${book.id}:${readerState.currentChapter}:$it"
                    },
                    activeAudioVerseKey = activeAudioKey,
                    targetScrollVerse = targetScrollVerse,
                    bookmarkedKeys = bookmarkedKeys,
                    highlightMap = highlightMap,
                    noteKeys = notesMap.keys,
                    controller = flipController,
                    onRequestPreviousChapter = onPrevChapter,
                    onRequestNextChapter = onNextChapter,
                    onPlayForwardSound = onPlayForwardSound,
                    onPlayBackwardSound = onPlayBackwardSound,
                    onVerseClick = { verse ->
                        onClearTargetHighlight()
                        onSelectVerse(verse)
                    },
                    onTopVisibleVerseChanged = { visibleVerse ->
                        onSavePageProgress(visibleVerse)
                    },
                    onDownloadLanguageBook = onDownloadBook,
                    onOpenDownloadsManager = onOpenDownloads,
                    modifier = Modifier.fillMaxSize()
                )

                val selected = readerState.selectedVerse
                if (selected != null) {
                    VerseActionBar(
                        book = book,
                        verse = selected,
                        primaryLanguage = primaryLang,
                        secondaryLanguage = secondaryLang,
                        isBookmarked = bookmarkedKeys.contains(selected.key),
                        currentHighlight = highlightMap[selected.key] ?: HighlightColor.NONE,
                        existingNote = notesMap[selected.key]?.noteText,
                        onToggleBookmark = { onToggleBookmark(selected) },
                        onSelectHighlight = { color -> onSetHighlight(selected, color) },
                        onOpenNoteDialog = { noteEditingVerse = selected },
                        onListenFromHere = {
                            onStartAudio(selected.verse)
                            onSelectVerse(null)
                        },
                        onDismiss = { onSelectVerse(null) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                } else if (audioState.isActiveSession) {
                    AudioPlayerBar(
                        audioState = audioState,
                        onTogglePlayPause = onToggleAudioPlayPause,
                        onPrevVerse = onAudioPrevVerse,
                        onNextVerse = onAudioNextVerse,
                        onCycleLanguage = onAudioLanguageChange,
                        onCycleSpeed = onAudioSpeedChange,
                        onClose = onStopAudio,
                        onJumpToPlaying = {
                            onOpenLocation(audioState.bookId, audioState.chapter, audioState.currentVerse)
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                } else {
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(24.dp),
                        elevated = true,
                        tokens = tokens,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { flipController.flipBackward() },
                                enabled = readerState.prevChapterSnapshot != null,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("prev_chapter_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = strings.prevChapterDesc,
                                    tint = if (readerState.prevChapterSnapshot != null) tokens.textPrimary else tokens.textSecondary.copy(alpha = 0.4f)
                                )
                            }
                            Text(
                                text = strings.readerScrollSwipeHint(readerState.verses.size),
                                fontSize = 11.sp,
                                color = tokens.textSecondary
                            )
                            IconButton(
                                onClick = { flipController.flipForward() },
                                enabled = readerState.nextChapterSnapshot != null,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("next_chapter_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = strings.nextChapterDesc,
                                    tint = if (readerState.nextChapterSnapshot != null) tokens.textPrimary else tokens.textSecondary.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBookPicker && books.isNotEmpty()) {
        ScripturePickerSheet(
            books = books,
            initialBookId = book?.id ?: 1,
            initialChapter = readerState.currentChapter,
            primaryLanguage = primaryLang,
            loadChapterVerseCount = loadChapterVerseCount,
            onSelectLocation = { bookId, ch, v ->
                onOpenLocation(bookId, ch, v)
                showBookPicker = false
            },
            onDismiss = { showBookPicker = false }
        )
    }

    if (showLanguageSelector) {
        LanguageSelectorSheet(
            primaryLanguage = primaryLang,
            secondaryLanguage = secondaryLang,
            readingMode = preferences.readingMode,
            languageSummaries = languageSummaries,
            onSelectPrimary = onSelectPrimaryLanguage,
            onSelectSecondary = onSelectSecondaryLanguage,
            onSwapLanguages = onSwapLanguages,
            onSetReadingMode = onSetReadingMode,
            onDownloadLanguage = onDownloadLanguage,
            onOpenDownloadsManager = onOpenDownloads,
            onDismiss = { showLanguageSelector = false }
        )
    }

    val editingVerse = noteEditingVerse
    if (editingVerse != null && book != null) {
        val existing = notesMap[editingVerse.key]?.noteText ?: ""
        val refBookName = if (sysLang == com.example.data.model.SystemLanguage.NEPALI) book.nameNe else book.nameFor(primaryLang)
        NoteEditorDialog(
            referenceTitle = "$refBookName ${com.example.util.NepaliFormatUtils.formatNumber(editingVerse.chapter, sysLang)}:${com.example.util.NepaliFormatUtils.formatNumber(editingVerse.verse, sysLang)}",
            initialNote = existing,
            onSave = { text ->
                onSaveNote(editingVerse, text)
                noteEditingVerse = null
            },
            onDelete = if (existing.isNotBlank()) {
                {
                    onDeleteNote(editingVerse.key)
                    noteEditingVerse = null
                }
            } else null,
            onDismiss = { noteEditingVerse = null }
        )
    }
}
