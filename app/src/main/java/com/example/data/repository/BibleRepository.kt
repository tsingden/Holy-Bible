package com.example.data.repository

import com.example.data.db.BibleAssetDatabase
import com.example.data.db.BookmarkEntity
import com.example.data.db.HighlightEntity
import com.example.data.db.NoteEntity
import com.example.data.db.ReadingHistoryEntity
import com.example.data.db.UserBibleDao
import com.example.data.download.BibleDownloadManager
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
import com.example.data.prefs.BiblePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class BibleRepository(
    private val assetDb: BibleAssetDatabase,
    private val userDao: UserBibleDao,
    private val prefs: BiblePreferences,
    private val downloadManager: BibleDownloadManager
) {
    val preferences: Flow<BiblePreferencesState> = prefs.stateFlow
    val bookmarks: Flow<List<BookmarkEntity>> = userDao.observeBookmarks()
    val highlights: Flow<List<HighlightEntity>> = userDao.observeHighlights()
    val notes: Flow<List<NoteEntity>> = userDao.observeNotes()
    val history: Flow<List<ReadingHistoryEntity>> = userDao.observeHistory()

    val bookDownloadStates: StateFlow<Map<Pair<String, Int>, BookDownloadState>> = downloadManager.bookStates
    val downloadedBookKeys: StateFlow<Set<Pair<String, Int>>> = downloadManager.downloadedBookKeys
    val languageSummaries: StateFlow<List<LanguageDownloadSummary>> = downloadManager.languageSummaries
    val totalStorageBytes: StateFlow<Long> = downloadManager.totalStorageBytes
    val downloadDataVersion: StateFlow<Int> = downloadManager.dataVersion

    suspend fun warmupDatabase() {
        val needsOncvSeed = !prefs.isOncvDefaultSeeded()
        downloadManager.initialize(
            seedDefaultsIfEmpty = true,
            seedOncvIfMissing = needsOncvSeed
        )
        if (needsOncvSeed) {
            prefs.markOncvDefaultSeeded()
        }
    }

    suspend fun getAllBooks(): List<BibleBook> = assetDb.getAllBooks()

    suspend fun getBook(bookId: Int): BibleBook? = assetDb.getBook(bookId)

    suspend fun getChapterVerses(bookId: Int, chapter: Int): List<BibleVerse> =
        assetDb.getChapterVerses(bookId, chapter)

    suspend fun getChapterVerseCount(bookId: Int, chapter: Int): Int =
        assetDb.getChapterVerseCountFromAnyPack(bookId, chapter)

    suspend fun getDailyVerse(): DailyVerseData? = assetDb.getDailyVerse()

    suspend fun searchVerses(
        query: String,
        testamentFilter: String? = null,
        languageFilter: BibleLanguage? = null
    ): List<SearchResult> = assetDb.searchVerses(query, testamentFilter, languageFilter)

    // Download Manager controls
    suspend fun downloadCompleteBible() = downloadManager.downloadCompleteBible()
    suspend fun downloadLanguage(language: BibleLanguage) = downloadManager.downloadLanguage(language)
    suspend fun downloadBook(language: BibleLanguage, bookId: Int) = downloadManager.downloadBook(language, bookId)
    suspend fun pauseLanguageDownload(language: BibleLanguage) = downloadManager.pauseLanguage(language)
    suspend fun pauseAllDownloads() = downloadManager.pauseAll()
    suspend fun resumeLanguageDownload(language: BibleLanguage) = downloadManager.resumeLanguage(language)
    suspend fun resumeAllDownloads() = downloadManager.resumeAll()
    suspend fun cancelLanguageDownload(language: BibleLanguage) = downloadManager.cancelLanguage(language)
    suspend fun cancelBookDownload(language: BibleLanguage, bookId: Int) = downloadManager.cancelBook(language, bookId)
    suspend fun deleteLanguageDownload(language: BibleLanguage) = downloadManager.deleteLanguage(language)
    suspend fun deleteBookDownload(language: BibleLanguage, bookId: Int) = downloadManager.deleteBook(language, bookId)

    fun observeChapterHighlights(bookId: Int, chapter: Int): Flow<List<HighlightEntity>> =
        userDao.observeChapterHighlights(bookId, chapter)

    fun observeChapterNotes(bookId: Int, chapter: Int): Flow<List<NoteEntity>> =
        userDao.observeChapterNotes(bookId, chapter)

    suspend fun toggleBookmark(
        book: BibleBook,
        verse: BibleVerse,
        currentlyBookmarked: Boolean,
        preferOncv: Boolean = false
    ) {
        if (currentlyBookmarked) {
            userDao.deleteBookmark(verse.key)
        } else {
            val nePreview = if (preferOncv) verse.textOncv.ifBlank { verse.textNe } else verse.textNe.ifBlank { verse.textOncv }
            userDao.insertBookmark(
                BookmarkEntity(
                    verseKey = verse.key,
                    bookId = book.id,
                    chapter = verse.chapter,
                    verse = verse.verse,
                    bookNameEn = book.nameEn,
                    bookNameNe = book.nameNe,
                    previewEn = verse.textEn.ifBlank { verse.textKo.ifBlank { nePreview } },
                    previewNe = nePreview
                )
            )
        }
    }

    suspend fun removeBookmark(verseKey: String) = userDao.deleteBookmark(verseKey)

    suspend fun setHighlight(
        book: BibleBook,
        verse: BibleVerse,
        color: HighlightColor,
        preferOncv: Boolean = false
    ) {
        if (color == HighlightColor.NONE) {
            userDao.deleteHighlight(verse.key)
        } else {
            val nePreview = if (preferOncv) verse.textOncv.ifBlank { verse.textNe } else verse.textNe.ifBlank { verse.textOncv }
            userDao.upsertHighlight(
                HighlightEntity(
                    verseKey = verse.key,
                    bookId = book.id,
                    chapter = verse.chapter,
                    verse = verse.verse,
                    bookNameEn = book.nameEn,
                    bookNameNe = book.nameNe,
                    previewEn = verse.textEn.ifBlank { verse.textKo.ifBlank { nePreview } },
                    previewNe = nePreview,
                    colorKey = color.key
                )
            )
        }
    }

    suspend fun saveNote(
        book: BibleBook,
        verse: BibleVerse,
        noteText: String,
        preferOncv: Boolean = false
    ) {
        val trimmed = noteText.trim()
        if (trimmed.isEmpty()) {
            userDao.deleteNote(verse.key)
        } else {
            val nePreview = if (preferOncv) verse.textOncv.ifBlank { verse.textNe } else verse.textNe.ifBlank { verse.textOncv }
            userDao.upsertNote(
                NoteEntity(
                    verseKey = verse.key,
                    bookId = book.id,
                    chapter = verse.chapter,
                    verse = verse.verse,
                    bookNameEn = book.nameEn,
                    bookNameNe = book.nameNe,
                    versePreviewEn = verse.textEn.ifBlank { verse.textKo.ifBlank { nePreview } },
                    versePreviewNe = nePreview,
                    noteText = trimmed
                )
            )
        }
    }

    suspend fun deleteNote(verseKey: String) = userDao.deleteNote(verseKey)

    suspend fun recordReadingVisit(book: BibleBook, chapter: Int, verse: Int) {
        prefs.saveLastRead(book.id, chapter, verse)
        userDao.upsertHistory(
            ReadingHistoryEntity(
                chapterKey = "${book.id}:$chapter",
                bookId = book.id,
                chapter = chapter,
                verse = verse,
                bookNameEn = book.nameEn,
                bookNameNe = book.nameNe
            )
        )
    }

    suspend fun clearHistory() = userDao.clearHistory()

    suspend fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)
    suspend fun saveThemeConfig(config: com.example.data.model.AppThemeConfig) = prefs.saveThemeConfig(config)
    suspend fun setFontSize(sp: Float) = prefs.setFontSize(sp)
    suspend fun setPageSoundEnabled(enabled: Boolean) = prefs.setPageSoundEnabled(enabled)
    suspend fun setPageSoundStyle(style: PageTurnSoundStyle) = prefs.setPageSoundStyle(style)
    suspend fun setPageSoundVolume(volume: Float) = prefs.setPageSoundVolume(volume)
    suspend fun setReadingMode(mode: ReadingMode) = prefs.setReadingMode(mode)
    suspend fun setPrimaryLanguage(lang: BibleLanguage) = prefs.setPrimaryLanguage(lang)
    suspend fun setSecondaryLanguage(lang: BibleLanguage) = prefs.setSecondaryLanguage(lang)
    suspend fun swapLanguages() = prefs.swapLanguages()
    suspend fun setAudioLanguage(lang: AudioLanguage) = prefs.setAudioLanguage(lang)
    suspend fun setAudioSpeed(speed: Float) = prefs.setAudioSpeed(speed)
    suspend fun setSystemLanguage(language: SystemLanguage) = prefs.setSystemLanguage(language)
}
