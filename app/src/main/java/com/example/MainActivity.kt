package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.HighlightColor
import com.example.data.model.ReadingMode
import com.example.data.model.SystemLanguage
import com.example.ui.BibleViewModel
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.components.ScripturePickerSheet
import com.example.ui.localization.LocalAppStrings
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThemeStudioScreen
import com.example.ui.theme.HolyBibleTheme
import com.example.ui.theme.LiquidGlassBackdrop
import com.example.ui.theme.LiquidGlassBottomNav
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NavDestinationItem
import java.util.Locale

private const val ROUTE_HOME = "home"
private const val ROUTE_READER = "reader"
private const val ROUTE_SEARCH = "search"
private const val ROUTE_LIBRARY = "library"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_DOWNLOADS = "downloads"
private const val ROUTE_THEME_STUDIO = "theme_studio"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HolyBibleApp()
        }
    }
}

@Composable
fun HolyBibleApp(viewModel: BibleViewModel = viewModel()) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val books by viewModel.books.collectAsStateWithLifecycle()
    val dailyVerse by viewModel.dailyVerse.collectAsStateWithLifecycle()
    val readerState by viewModel.readerState.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val languageSummaries by viewModel.languageSummaries.collectAsStateWithLifecycle()
    val bookDownloadStates by viewModel.bookDownloadStates.collectAsStateWithLifecycle()
    val downloadedBookKeys by viewModel.downloadedBookKeys.collectAsStateWithLifecycle()
    val totalStorageBytes by viewModel.totalStorageBytes.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val bookmarkedKeys by viewModel.bookmarkedKeys.collectAsStateWithLifecycle()
    val highlightMap by viewModel.highlightMap.collectAsStateWithLifecycle()
    val notesMap by viewModel.notesMap.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchTestamentFilter.collectAsStateWithLifecycle()
    val searchLanguageFilter by viewModel.searchLanguageFilter.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val app = context.applicationContext as BibleApplication
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

    var homeTestamentPickerFilter by remember { mutableStateOf<String?>(null) }
    var showHomePicker by remember { mutableStateOf(false) }
    var showGlobalLanguageSheet by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Audio playback continues smoothly regardless of optional notification permission
    }

    fun requestNotificationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(ROUTE_HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(preferences.systemLanguage) {
        val targetLocale = when (preferences.systemLanguage) {
            SystemLanguage.ENGLISH -> Locale.US
            SystemLanguage.NEPALI -> Locale.forLanguageTag("ne-NP")
        }
        Locale.setDefault(targetLocale)
    }

    val showChromeNav = currentRoute != ROUTE_READER &&
        currentRoute != ROUTE_DOWNLOADS &&
        currentRoute != ROUTE_THEME_STUDIO

    HolyBibleTheme(
        darkTheme = preferences.darkMode,
        themeConfig = preferences.themeConfig,
        systemLanguage = preferences.systemLanguage
    ) {
        val glassTokens = LocalLiquidGlass.current
        val strings = LocalAppStrings.current
        val navItems = remember(strings) {
            listOf(
                NavDestinationItem(ROUTE_HOME, strings.navHome, Icons.Filled.Home, "nav_home"),
                NavDestinationItem(ROUTE_READER, strings.navBible, Icons.AutoMirrored.Filled.MenuBook, "nav_bible"),
                NavDestinationItem(ROUTE_SEARCH, strings.navSearch, Icons.Filled.Search, "nav_search"),
                NavDestinationItem(ROUTE_LIBRARY, strings.navSaved, Icons.Filled.Bookmarks, "nav_saved"),
                NavDestinationItem(ROUTE_SETTINGS, strings.navSettings, Icons.Filled.Settings, "nav_settings")
            )
        }

        LiquidGlassBackdrop(
            tokens = glassTokens,
            modifier = Modifier.fillMaxSize()
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 600.dp && showChromeNav

                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (showChromeNav && !useNavRail) {
                            LiquidGlassBottomNav(
                                items = navItems,
                                currentRoute = currentRoute,
                                onNavigate = ::navigateToTab,
                                tokens = glassTokens
                            )
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavRail) {
                            NavigationRail(
                                containerColor = glassTokens.glassSurfaceElevated,
                                contentColor = glassTokens.textPrimary
                            ) {
                                navItems.forEach { item ->
                                    NavigationRailItem(
                                        selected = currentRoute == item.route,
                                        onClick = { navigateToTab(item.route) },
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = ROUTE_HOME,
                                enterTransition = {
                                    fadeIn(animationSpec = tween(220)) + scaleIn(
                                        initialScale = 0.985f,
                                        animationSpec = tween(220)
                                    )
                                },
                                exitTransition = {
                                    fadeOut(animationSpec = tween(180))
                                },
                                popEnterTransition = {
                                    fadeIn(animationSpec = tween(220)) + scaleIn(
                                        initialScale = 0.985f,
                                        animationSpec = tween(220)
                                    )
                                },
                                popExitTransition = {
                                    fadeOut(animationSpec = tween(180))
                                }
                            ) {
                                composable(ROUTE_HOME) {
                                    HomeScreen(
                                        books = books,
                                        preferences = preferences,
                                        dailyVerse = dailyVerse,
                                        audioState = audioState,
                                        languageSummaries = languageSummaries,
                                        totalStorageBytes = totalStorageBytes,
                                        onContinueReading = { navigateToTab(ROUTE_READER) },
                                        onOpenTestament = { testament ->
                                            homeTestamentPickerFilter = testament
                                            showHomePicker = true
                                        },
                                        onOpenReaderAt = { bookId, ch, v ->
                                            viewModel.openChapter(bookId, ch, v)
                                            navigateToTab(ROUTE_READER)
                                        },
                                        onToggleAudio = {
                                            requestNotificationIfNeeded()
                                            if (audioState.isActiveSession) {
                                                viewModel.audioController.togglePlayPause()
                                            } else {
                                                viewModel.startAudioFromHome()
                                            }
                                        },
                                        onOpenLanguages = { showGlobalLanguageSheet = true },
                                        onOpenDownloads = { navController.navigate(ROUTE_DOWNLOADS) },
                                        onOpenSearch = { navigateToTab(ROUTE_SEARCH) },
                                        onOpenLibrary = { navigateToTab(ROUTE_LIBRARY) },
                                        onOpenSettings = { navigateToTab(ROUTE_SETTINGS) },
                                        onOpenThemeStudio = { navController.navigate(ROUTE_THEME_STUDIO) }
                                    )
                                }

                                composable(ROUTE_READER) {
                                    ReaderScreen(
                                        readerState = readerState,
                                        books = books,
                                        preferences = preferences,
                                        audioState = audioState,
                                        languageSummaries = languageSummaries,
                                        downloadedBookKeys = downloadedBookKeys,
                                        bookmarkedKeys = bookmarkedKeys,
                                        highlightMap = highlightMap,
                                        notesMap = notesMap,
                                        loadChapterVerseCount = { bId, ch ->
                                            app.repository.getChapterVerseCount(bId, ch)
                                        },
                                        onBack = { navigateToTab(ROUTE_HOME) },
                                        onOpenLocation = { bookId, ch, v ->
                                            viewModel.openChapter(bookId, ch, v)
                                        },
                                        onNextChapter = { viewModel.goToNextChapter() },
                                        onPrevChapter = { viewModel.goToPreviousChapter() },
                                        onClearTargetHighlight = { viewModel.clearTargetHighlight() },
                                        onSavePageProgress = { viewModel.savePageProgress(it) },
                                        onSelectVerse = { viewModel.selectVerse(it) },
                                        onToggleBookmark = { viewModel.toggleBookmark(it) },
                                        onSetHighlight = { v, c -> viewModel.setHighlight(v, c) },
                                        onSaveNote = { v, txt -> viewModel.saveNote(v, txt) },
                                        onDeleteNote = { key -> viewModel.deleteNote(key) },
                                        onSetReadingMode = { viewModel.setReadingMode(it) },
                                        onSelectPrimaryLanguage = { viewModel.setPrimaryLanguage(it) },
                                        onSelectSecondaryLanguage = { viewModel.setSecondaryLanguage(it) },
                                        onSwapLanguages = { viewModel.swapLanguages() },
                                        onDownloadLanguage = { viewModel.downloadLanguage(it) },
                                        onDownloadBook = { lang, bId -> viewModel.downloadBook(lang, bId) },
                                        onTogglePageSound = { viewModel.setPageSoundEnabled(it) },
                                        onPlayForwardSound = { viewModel.paperSoundPlayer.playForward() },
                                        onPlayBackwardSound = { viewModel.paperSoundPlayer.playBackward() },
                                        onStartAudio = { v ->
                                            requestNotificationIfNeeded()
                                            viewModel.startAudioFromVerse(v)
                                        },
                                        onToggleAudioPlayPause = { viewModel.audioController.togglePlayPause() },
                                        onAudioPrevVerse = { viewModel.audioController.previousVerse() },
                                        onAudioNextVerse = { viewModel.audioController.nextVerse() },
                                        onAudioLanguageChange = { viewModel.setAudioLanguage(it) },
                                        onAudioSpeedChange = { viewModel.setAudioSpeed(it) },
                                        onStopAudio = { viewModel.audioController.stopAudio() },
                                        onOpenDownloads = { navController.navigate(ROUTE_DOWNLOADS) },
                                        onOpenSettings = { navigateToTab(ROUTE_SETTINGS) }
                                    )
                                }

                                composable(ROUTE_SEARCH) {
                                    SearchScreen(
                                        query = searchQuery,
                                        testamentFilter = searchFilter,
                                        languageFilter = searchLanguageFilter,
                                        results = searchResults,
                                        isSearching = isSearching,
                                        onQueryChange = { viewModel.updateSearchQuery(it) },
                                        onFilterChange = { viewModel.updateSearchFilter(it) },
                                        onLanguageFilterChange = { viewModel.updateSearchLanguageFilter(it) },
                                        onResultClick = { res ->
                                            viewModel.openChapter(res.bookId, res.chapter, res.verse)
                                            navigateToTab(ROUTE_READER)
                                        }
                                    )
                                }

                                composable(ROUTE_LIBRARY) {
                                    LibraryScreen(
                                        bookmarks = bookmarks,
                                        highlights = highlights,
                                        notes = notes,
                                        history = history,
                                        onOpenVerse = { bookId, ch, v ->
                                            viewModel.openChapter(bookId, ch, v)
                                            navigateToTab(ROUTE_READER)
                                        },
                                        onRemoveBookmark = { viewModel.removeBookmark(it) },
                                        onClearHighlight = { entity ->
                                            viewModel.openChapter(entity.bookId, entity.chapter, entity.verse)
                                            viewModel.setHighlight(
                                                com.example.data.model.BibleVerse(
                                                    id = 0,
                                                    bookId = entity.bookId,
                                                    chapter = entity.chapter,
                                                    verse = entity.verse,
                                                    textEn = entity.previewEn,
                                                    textNe = entity.previewNe
                                                ),
                                                HighlightColor.NONE
                                            )
                                        },
                                        onUpdateNote = { noteEntity, text ->
                                            viewModel.saveNote(
                                                com.example.data.model.BibleVerse(
                                                    id = 0,
                                                    bookId = noteEntity.bookId,
                                                    chapter = noteEntity.chapter,
                                                    verse = noteEntity.verse,
                                                    textEn = noteEntity.versePreviewEn,
                                                    textNe = noteEntity.versePreviewNe
                                                ),
                                                text
                                            )
                                        },
                                        onDeleteNote = { viewModel.deleteNote(it) },
                                        onClearHistory = { viewModel.clearHistory() }
                                    )
                                }

                                composable(ROUTE_SETTINGS) {
                                    SettingsScreen(
                                        preferences = preferences,
                                        audioState = audioState,
                                        totalStorageBytes = totalStorageBytes,
                                        onSetSystemLanguage = { viewModel.setSystemLanguage(it) },
                                        onSetDarkMode = { viewModel.setDarkMode(it) },
                                        onSetFontSize = { viewModel.setFontSize(it) },
                                        onSetPageSound = { viewModel.setPageSoundEnabled(it) },
                                        onSetPageSoundStyle = { viewModel.setPageSoundStyle(it) },
                                        onSetPageSoundVolume = { viewModel.setPageSoundVolume(it) },
                                        onPreviewPageSound = { viewModel.previewPageSound(it) },
                                        onTestForwardSound = { viewModel.paperSoundPlayer.playForward() },
                                        onTestBackwardSound = { viewModel.paperSoundPlayer.playBackward() },
                                        onSetReadingMode = { viewModel.setReadingMode(it) },
                                        onOpenLanguageSelector = { showGlobalLanguageSheet = true },
                                        onOpenDownloads = { navController.navigate(ROUTE_DOWNLOADS) },
                                        onSetAudioLanguage = { viewModel.setAudioLanguage(it) },
                                        onSetAudioSpeed = { viewModel.setAudioSpeed(it) },
                                        onApplyThemePreset = { viewModel.applyThemePreset(it) },
                                        onOpenThemeStudio = { navController.navigate(ROUTE_THEME_STUDIO) }
                                    )
                                }

                                composable(ROUTE_THEME_STUDIO) {
                                    ThemeStudioScreen(
                                        currentConfig = preferences.themeConfig,
                                        currentFontSizeSp = preferences.fontSizeSp,
                                        onApplyTheme = { config, fontSize ->
                                            viewModel.saveThemeConfig(config)
                                            viewModel.setFontSize(fontSize)
                                            navController.popBackStack()
                                        },
                                        onCancel = {
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                composable(ROUTE_DOWNLOADS) {
                                    DownloadsScreen(
                                        books = books,
                                        languageSummaries = languageSummaries,
                                        bookDownloadStates = bookDownloadStates,
                                        totalStorageBytes = totalStorageBytes,
                                        onBack = { navController.popBackStack() },
                                        onDownloadCompleteBible = { viewModel.downloadCompleteBible() },
                                        onDownloadLanguage = { viewModel.downloadLanguage(it) },
                                        onDownloadBook = { lang, bookId -> viewModel.downloadBook(lang, bookId) },
                                        onPauseLanguage = { viewModel.pauseLanguageDownload(it) },
                                        onPauseAll = { viewModel.pauseAllDownloads() },
                                        onResumeLanguage = { viewModel.resumeLanguageDownload(it) },
                                        onResumeAll = { viewModel.resumeAllDownloads() },
                                        onCancelLanguage = { viewModel.cancelLanguageDownload(it) },
                                        onCancelBook = { lang, bookId -> viewModel.cancelBookDownload(lang, bookId) },
                                        onDeleteLanguage = { viewModel.deleteLanguageDownload(it) },
                                        onDeleteBook = { lang, bookId -> viewModel.deleteBookDownload(lang, bookId) },
                                        onReadLanguage = { lang ->
                                            viewModel.setPrimaryLanguage(lang)
                                            if (lang.supportsBook(preferences.lastBookId)) {
                                                viewModel.openChapter(preferences.lastBookId, preferences.lastChapter, preferences.lastVerse)
                                            } else {
                                                val firstSupported = lang.supportedBookIds.firstOrNull() ?: 1
                                                viewModel.openChapter(firstSupported, 1, 1)
                                            }
                                            if (preferences.readingMode == ReadingMode.SINGLE_SECONDARY) {
                                                viewModel.setReadingMode(ReadingMode.BILINGUAL_SPREAD)
                                            }
                                            navigateToTab(ROUTE_READER)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (showHomePicker && books.isNotEmpty()) {
                    ScripturePickerSheet(
                        books = books,
                        initialBookId = if (homeTestamentPickerFilter == "NT") 40 else 1,
                        initialChapter = 1,
                        initialTestamentFilter = homeTestamentPickerFilter,
                        primaryLanguage = preferences.primaryLanguage,
                        loadChapterVerseCount = { bId, ch ->
                            app.repository.getChapterVerseCount(bId, ch)
                        },
                        onSelectLocation = { bookId, ch, v ->
                            showHomePicker = false
                            viewModel.openChapter(bookId, ch, v)
                            navigateToTab(ROUTE_READER)
                        },
                        onDismiss = { showHomePicker = false }
                    )
                }

                if (showGlobalLanguageSheet) {
                    LanguageSelectorSheet(
                        primaryLanguage = preferences.primaryLanguage,
                        secondaryLanguage = preferences.secondaryLanguage,
                        readingMode = preferences.readingMode,
                        languageSummaries = languageSummaries,
                        onSelectPrimary = { viewModel.setPrimaryLanguage(it) },
                        onSelectSecondary = { viewModel.setSecondaryLanguage(it) },
                        onSwapLanguages = { viewModel.swapLanguages() },
                        onSetReadingMode = { viewModel.setReadingMode(it) },
                        onDownloadLanguage = { viewModel.downloadLanguage(it) },
                        onOpenDownloadsManager = { navController.navigate(ROUTE_DOWNLOADS) },
                        onDismiss = { showGlobalLanguageSheet = false }
                    )
                }
            }
        }
    }
}

