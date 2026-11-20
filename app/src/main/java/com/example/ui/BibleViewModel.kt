package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BibleApplication
import com.example.audio.AudioPlaybackState
import com.example.data.db.BookmarkEntity
import com.example.data.db.HighlightEntity
import com.example.data.db.NoteEntity
import com.example.data.db.ReadingHistoryEntity
import com.example.data.model.AudioLanguage
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.BiblePreferencesState
import com.example.data.model.BibleVerse
import com.example.data.model.BookDownloadState
import com.example.data.model.DailyVerseData
import com.example.data.model.HighlightColor
import com.example.data.model.LanguageDownloadSummary
import com.example.data.model.PageTurnSoundStyle
import com.example.data.model.ReadingMode
import com.example.data.model.SearchResult
import com.example.data.model.SystemLanguage
import com.example.ui.localization.SystemLocaleManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChapterSnapshot(
    val book: BibleBook,
    val chapter: Int,
    val verses: List<BibleVerse>
)

data class ReaderUiState(
    val isLoading: Boolean = true,
    val currentBook: BibleBook? = null,
    val currentChapter: Int = 1,
    val verses: List<BibleVerse> = emptyList(),
    val prevChapterSnapshot: ChapterSnapshot? = null,
    val nextChapterSnapshot: ChapterSnapshot? = null,
    val selectedVerse: BibleVerse? = null,
    val targetHighlightVerse: Int? = null
)

class BibleViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BibleApplication
    private val repository = app.repository
    val paperSoundPlayer = app.paperSoundPlayer
    val audioController = app.audioController

    val preferences: StateFlow<BiblePreferencesState> = repository.preferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, BiblePreferencesState())

    val audioState: StateFlow<AudioPlaybackState> = audioController.state

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val highlights: StateFlow<List<HighlightEntity>> = repository.highlights
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<ReadingHistoryEntity>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Offline Download Manager state
    val languageSummaries: StateFlow<List<LanguageDownloadSummary>> = repository.languageSummaries
    val bookDownloadStates: StateFlow<Map<Pair<String, Int>, BookDownloadState>> = repository.bookDownloadStates
    val downloadedBookKeys: StateFlow<Set<Pair<String, Int>>> = repository.downloadedBookKeys
    val totalStorageBytes: StateFlow<Long> = repository.totalStorageBytes

    private val _books = MutableStateFlow<List<BibleBook>>(emptyList())
    val books: StateFlow<List<BibleBook>> = _books.asStateFlow()

    private val _dailyVerse = MutableStateFlow<DailyVerseData?>(null)
    val dailyVerse: StateFlow<DailyVerseData?> = _dailyVerse.asStateFlow()

    private val _readerState = MutableStateFlow(ReaderUiState())
    val readerState: StateFlow<ReaderUiState> = _readerState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchTestamentFilter = MutableStateFlow<String?>(null)
    val searchTestamentFilter: StateFlow<String?> = _searchTestamentFilter.asStateFlow()

    private val _searchLanguageFilter = MutableStateFlow<BibleLanguage?>(null)
    val searchLanguageFilter: StateFlow<BibleLanguage?> = _searchLanguageFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val bookmarkedKeys: StateFlow<Set<String>> = bookmarks
        .combine(MutableStateFlow(Unit)) { list, _ -> list.map { it.verseKey }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val highlightMap: StateFlow<Map<String, HighlightColor>> = highlights
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.associate { it.verseKey to HighlightColor.fromKey(it.colorKey) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val notesMap: StateFlow<Map<String, NoteEntity>> = notes
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.associateBy { it.verseKey }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.warmupDatabase()
            val allBooks = repository.getAllBooks()
            _books.value = allBooks
            _dailyVerse.value = repository.getDailyVerse()

            val prefs = repository.preferences.first()
            SystemLocaleManager.currentLanguage = prefs.systemLanguage
            paperSoundPlayer.enabled = prefs.pageSoundEnabled
            paperSoundPlayer.soundStyle = prefs.pageSoundStyle
            paperSoundPlayer.volume = prefs.pageSoundVolume
            openChapter(
                bookId = prefs.lastBookId,
                chapter = prefs.lastChapter,
                highlightVerse = prefs.lastVerse,
                recordHistory = false
            )
        }

        viewModelScope.launch {
            preferences.collect { prefs ->
                SystemLocaleManager.currentLanguage = prefs.systemLanguage
                paperSoundPlayer.enabled = prefs.pageSoundEnabled
                paperSoundPlayer.soundStyle = prefs.pageSoundStyle
                paperSoundPlayer.volume = prefs.pageSoundVolume
            }
        }

        // Automatically refresh active chapter & daily verse & active search when downloads finish or delete
        viewModelScope.launch {
            repository.downloadDataVersion
                .drop(1)
                .collect {
                    val cur = _readerState.value
                    val curBook = cur.currentBook
                    if (curBook != null) {
                        val refreshedVerses = repository.getChapterVerses(curBook.id, cur.currentChapter)
                        val prevSnapshot = loadAdjacentSnapshot(curBook, cur.currentChapter, forward = false)
                        val nextSnapshot = loadAdjacentSnapshot(curBook, cur.currentChapter, forward = true)
                        _readerState.update {
                            it.copy(
                                verses = refreshedVerses,
                                prevChapterSnapshot = prevSnapshot,
                                nextChapterSnapshot = nextSnapshot
                            )
                        }
                    }
                    if (_dailyVerse.value == null) {
                        _dailyVerse.value = repository.getDailyVerse()
                    }
                    if (_searchQuery.value.trim().length >= 2) {
                        runSearch()
                    }
                }
        }
    }

    fun openChapter(
        bookId: Int,
        chapter: Int,
        highlightVerse: Int? = null,
        recordHistory: Boolean = true
    ) {
        viewModelScope.launch {
            _readerState.update { it.copy(isLoading = true, selectedVerse = null) }
            val book = repository.getBook(bookId) ?: repository.getBook(1)
            val safeChapter = if (book != null) chapter.coerceIn(1, book.chapterCount) else 1
            val verses = if (book != null) repository.getChapterVerses(book.id, safeChapter) else emptyList()

            if (book != null && recordHistory) {
                repository.recordReadingVisit(book, safeChapter, highlightVerse ?: 1)
            }

            // Preload adjacent chapters so 3D page turns reveal authentic neighboring pages
            val prevSnapshot = if (book != null) loadAdjacentSnapshot(book, safeChapter, forward = false) else null
            val nextSnapshot = if (book != null) loadAdjacentSnapshot(book, safeChapter, forward = true) else null

            _readerState.value = ReaderUiState(
                isLoading = false,
                currentBook = book,
                currentChapter = safeChapter,
                verses = verses,
                prevChapterSnapshot = prevSnapshot,
                nextChapterSnapshot = nextSnapshot,
                selectedVerse = null,
                targetHighlightVerse = highlightVerse
            )
        }
    }

    private suspend fun loadAdjacentSnapshot(
        book: BibleBook,
        chapter: Int,
        forward: Boolean
    ): ChapterSnapshot? {
        return if (forward) {
            if (chapter < book.chapterCount) {
                val nextVerses = repository.getChapterVerses(book.id, chapter + 1).take(14)
                ChapterSnapshot(book, chapter + 1, nextVerses)
            } else if (book.id < 66) {
                val nextBook = repository.getBook(book.id + 1) ?: return null
                val nextVerses = repository.getChapterVerses(nextBook.id, 1).take(14)
                ChapterSnapshot(nextBook, 1, nextVerses)
            } else null
        } else {
            if (chapter > 1) {
                val prevVerses = repository.getChapterVerses(book.id, chapter - 1).take(14)
                ChapterSnapshot(book, chapter - 1, prevVerses)
            } else if (book.id > 1) {
                val prevBook = repository.getBook(book.id - 1) ?: return null
                val prevVerses = repository.getChapterVerses(prevBook.id, prevBook.chapterCount).take(14)
                ChapterSnapshot(prevBook, prevBook.chapterCount, prevVerses)
            } else null
        }
    }

    fun goToNextChapter() {
        val next = _readerState.value.nextChapterSnapshot
        if (next != null) {
            openChapter(next.book.id, next.chapter, highlightVerse = null)
        }
    }

    fun goToPreviousChapter() {
        val prev = _readerState.value.prevChapterSnapshot
        if (prev != null) {
            openChapter(prev.book.id, prev.chapter, highlightVerse = null)
        }
    }

    fun clearTargetHighlight() {
        _readerState.update { it.copy(targetHighlightVerse = null) }
    }

    fun savePageProgress(firstVerseOnPage: Int) {
        val book = _readerState.value.currentBook ?: return
        val chap = _readerState.value.currentChapter
        viewModelScope.launch {
            repository.recordReadingVisit(book, chap, firstVerseOnPage)
        }
    }

    fun selectVerse(verse: BibleVerse?) {
        _readerState.update {
            val next = if (it.selectedVerse?.key == verse?.key) null else verse
            it.copy(selectedVerse = next)
        }
        val book = _readerState.value.currentBook
        if (book != null && verse != null) {
            viewModelScope.launch {
                repository.recordReadingVisit(book, verse.chapter, verse.verse)
            }
        }
    }

    private fun isReaderPreferringOncv(prefs: BiblePreferencesState): Boolean {
        return when (prefs.readingMode) {
            ReadingMode.SINGLE_PRIMARY -> prefs.primaryLanguage == BibleLanguage.ONCV
            ReadingMode.SINGLE_SECONDARY -> prefs.secondaryLanguage == BibleLanguage.ONCV
            else -> prefs.primaryLanguage == BibleLanguage.ONCV ||
                (prefs.secondaryLanguage == BibleLanguage.ONCV && prefs.primaryLanguage != BibleLanguage.NEPALI)
        }
    }

    private fun resolveEffectiveAudioLanguage(prefs: BiblePreferencesState): AudioLanguage {
        return if (prefs.audioLanguage == AudioLanguage.NEPALI && isReaderPreferringOncv(prefs)) {
            AudioLanguage.ONCV
        } else {
            prefs.audioLanguage
        }
    }

    fun toggleBookmark(verse: BibleVerse) {
        val book = _readerState.value.currentBook ?: return
        val isBookmarked = bookmarkedKeys.value.contains(verse.key)
        val preferOncv = isReaderPreferringOncv(preferences.value)
        viewModelScope.launch {
            repository.toggleBookmark(book, verse, isBookmarked, preferOncv)
        }
    }

    fun removeBookmark(verseKey: String) {
        viewModelScope.launch {
            repository.removeBookmark(verseKey)
        }
    }

    fun setHighlight(verse: BibleVerse, color: HighlightColor) {
        val book = _readerState.value.currentBook ?: return
        val preferOncv = isReaderPreferringOncv(preferences.value)
        viewModelScope.launch {
            repository.setHighlight(book, verse, color, preferOncv)
        }
    }

    fun saveNote(verse: BibleVerse, noteText: String) {
        val book = _readerState.value.currentBook ?: return
        val preferOncv = isReaderPreferringOncv(preferences.value)
        viewModelScope.launch {
            repository.saveNote(book, verse, noteText, preferOncv)
        }
    }

    fun deleteNote(verseKey: String) {
        viewModelScope.launch {
            repository.deleteNote(verseKey)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun startAudioFromVerse(startVerse: Int = 1) {
        val state = _readerState.value
        val book = state.currentBook ?: return
        val prefs = preferences.value
        audioController.startChapterAudio(
            book = book,
            verses = state.verses,
            startVerse = startVerse,
            language = resolveEffectiveAudioLanguage(prefs),
            speed = prefs.audioSpeed,
            preferOncvInBilingual = isReaderPreferringOncv(prefs)
        )
    }

    fun startAudioFromHome() {
        val state = _readerState.value
        val book = state.currentBook
        if (book != null && state.verses.isNotEmpty()) {
            startAudioFromVerse(1)
        } else {
            viewModelScope.launch {
                val prefs = preferences.value
                val targetBook = repository.getBook(prefs.lastBookId) ?: repository.getBook(1) ?: return@launch
                val verses = repository.getChapterVerses(targetBook.id, prefs.lastChapter)
                audioController.startChapterAudio(
                    book = targetBook,
                    verses = verses,
                    startVerse = prefs.lastVerse,
                    language = resolveEffectiveAudioLanguage(prefs),
                    speed = prefs.audioSpeed,
                    preferOncvInBilingual = isReaderPreferringOncv(prefs)
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        runSearch()
    }

    fun updateSearchFilter(testament: String?) {
        _searchTestamentFilter.value = testament
        runSearch()
    }

    fun updateSearchLanguageFilter(language: BibleLanguage?) {
        _searchLanguageFilter.value = language
        runSearch()
    }

    private fun runSearch() {
        searchJob?.cancel()
        val q = _searchQuery.value.trim()
        if (q.length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = repository.searchVerses(
                query = q,
                testamentFilter = _searchTestamentFilter.value,
                languageFilter = _searchLanguageFilter.value
            )
            _isSearching.value = false
        }
    }

    // Download actions
    fun downloadCompleteBible() {
        viewModelScope.launch { repository.downloadCompleteBible() }
    }

    fun downloadLanguage(language: BibleLanguage) {
        viewModelScope.launch { repository.downloadLanguage(language) }
    }

    fun downloadBook(language: BibleLanguage, bookId: Int) {
        viewModelScope.launch { repository.downloadBook(language, bookId) }
    }

    fun pauseLanguageDownload(language: BibleLanguage) {
        viewModelScope.launch { repository.pauseLanguageDownload(language) }
    }

    fun pauseAllDownloads() {
        viewModelScope.launch { repository.pauseAllDownloads() }
    }

    fun resumeLanguageDownload(language: BibleLanguage) {
        viewModelScope.launch { repository.resumeLanguageDownload(language) }
    }

    fun resumeAllDownloads() {
        viewModelScope.launch { repository.resumeAllDownloads() }
    }

    fun cancelLanguageDownload(language: BibleLanguage) {
        viewModelScope.launch { repository.cancelLanguageDownload(language) }
    }

    fun cancelBookDownload(language: BibleLanguage, bookId: Int) {
        viewModelScope.launch { repository.cancelBookDownload(language, bookId) }
    }

    fun deleteLanguageDownload(language: BibleLanguage) {
        viewModelScope.launch { repository.deleteLanguageDownload(language) }
    }

    fun deleteBookDownload(language: BibleLanguage, bookId: Int) {
        viewModelScope.launch { repository.deleteBookDownload(language, bookId) }
    }

    // Settings & Language actions
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }

    fun saveThemeConfig(config: com.example.data.model.AppThemeConfig) {
        viewModelScope.launch { repository.saveThemeConfig(config) }
    }

    fun applyThemePreset(preset: com.example.data.model.ThemePreset) {
        val currentFontStyle = preferences.value.themeConfig.fontStyle
        val next = com.example.data.model.AppThemeConfig.fromPreset(preset).copy(fontStyle = currentFontStyle)
        viewModelScope.launch { repository.saveThemeConfig(next) }
    }

    fun setFontSize(sp: Float) {
        viewModelScope.launch { repository.setFontSize(sp) }
    }

    fun setPageSoundEnabled(enabled: Boolean) {
        paperSoundPlayer.enabled = enabled
        if (enabled && paperSoundPlayer.soundStyle == PageTurnSoundStyle.SILENT) {
            paperSoundPlayer.soundStyle = PageTurnSoundStyle.REALISTIC_BIBLE
        }
        viewModelScope.launch { repository.setPageSoundEnabled(enabled) }
    }

    fun setPageSoundStyle(style: PageTurnSoundStyle) {
        paperSoundPlayer.soundStyle = style
        if (style.isAudible) {
            paperSoundPlayer.enabled = true
        }
        viewModelScope.launch { repository.setPageSoundStyle(style) }
    }

    fun setPageSoundVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        paperSoundPlayer.volume = clamped
        viewModelScope.launch { repository.setPageSoundVolume(clamped) }
    }

    fun previewPageSound(style: PageTurnSoundStyle) {
        paperSoundPlayer.preview(style)
    }

    fun setReadingMode(mode: ReadingMode) {
        viewModelScope.launch { repository.setReadingMode(mode) }
    }

    fun setPrimaryLanguage(lang: BibleLanguage) {
        viewModelScope.launch { repository.setPrimaryLanguage(lang) }
    }

    fun setSecondaryLanguage(lang: BibleLanguage) {
        viewModelScope.launch { repository.setSecondaryLanguage(lang) }
    }

    fun swapLanguages() {
        viewModelScope.launch { repository.swapLanguages() }
    }

    fun setAudioLanguage(lang: AudioLanguage) {
        audioController.setLanguage(lang)
        viewModelScope.launch { repository.setAudioLanguage(lang) }
    }

    fun setAudioSpeed(speed: Float) {
        audioController.setSpeed(speed)
        viewModelScope.launch { repository.setAudioSpeed(speed) }
    }

    fun setSystemLanguage(language: SystemLanguage) {
        SystemLocaleManager.currentLanguage = language
        audioController.refreshLocalizedStatus()
        viewModelScope.launch { repository.setSystemLanguage(language) }
    }
}
