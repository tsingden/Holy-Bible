package com.example.ui.localization

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.model.AudioLanguage
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.ButtonStyleOption
import com.example.data.model.CopyrightLicenseCategory
import com.example.data.model.NavBarAppearance
import com.example.data.model.PageTurnSoundStyle
import com.example.data.model.ReadingMode
import com.example.data.model.ReadingPageBackground
import com.example.data.model.ScriptureFontStyle
import com.example.data.model.SystemLanguage
import com.example.data.model.ThemePreset
import com.example.util.NepaliFormatUtils

@Immutable
data class BibleSourceAttribution(
    val versionId: String,
    val title: String,
    val fullTranslationName: String,
    val ownerPublisher: String,
    val copyrightNotice: String,
    val sourceUrls: List<String>,
    val license: String,
    val licenseCategory: CopyrightLicenseCategory,
    val usagePermissions: String,
    val translationCredits: String
)

@Immutable
data class CreditLinkItem(
    val label: String,
    val url: String
)

@Immutable
data class CreditEntryItem(
    val title: String,
    val subtitle: String,
    val description: String,
    val links: List<CreditLinkItem> = emptyList()
)

@Immutable
interface AppStrings {
    // Navigation & App Identity
    val appTitle: String
    val appSubtitleLine: String
    val navHome: String
    val navBible: String
    val navSearch: String
    val navSaved: String
    val navSettings: String

    // Common Actions
    val back: String
    val close: String
    val cancel: String
    val save: String
    val delete: String
    val done: String
    val reset: String
    val manage: String
    val download: String
    val clear: String
    val pause: String
    val resume: String
    val loading: String

    // Home Screen
    val homeThemesDesc: String
    val homeLanguagesDesc: String
    val homeSearchDesc: String
    val homeSettingsDesc: String
    val homeBibleCoverDesc: String
    val heroTitle: String
    val heroNepaliTitle: String
    val heroEditionFormat: (primary: String, secondary: String) -> String
    val continueReadingPrefix: (bookName: String, ch: String, v: String) -> String
    val openHolyBible: String
    val oldTestament: String
    val oldTestamentSub: String
    val oldTestamentSubtitle: String
    val newTestament: String
    val newTestamentSub: String
    val newTestamentSubtitle: String
    val downloadBibleCardTitle: String
    val downloadBibleCardStatus: (ready: Int, total: Int, size: String) -> String
    val dailyVerseBadge: String
    val shareDailyVerseDesc: String
    val readChapter: String
    val audioBibleCardTitle: String
    val audioBibleListenHint: String
    val audioBibleDesc: String
    val audioBiblePauseDesc: String
    val audioBiblePlayDesc: String
    val quickLanguages: String
    val quickLanguagesSub: String
    val quickSearch: String
    val quickSearchSub: String
    val quickSaved: String
    val quickSavedSub: String
    val nnrvShortcutTitle: String
    val nnrvShortcutSubtitle: String
    val nnrvShortcutMeta: String
    val nnrvShortcutButton: String
    val nnrvOpenBrowserError: String

    // Reader Screen & Page Content
    val backToHomeDesc: String
    val chooseBookChapterDesc: String
    val selectLanguagesDesc: String
    val pageSoundOnDesc: String
    val pageSoundOffDesc: String
    val audioBibleDescReader: String
    val readerSettingsDesc: String
    val prevChapterDesc: String
    val nextChapterDesc: String
    val readerScrollSwipeHint: (versesCount: Int) -> String
    val bookmarkedDesc: String
    val hasNoteDesc: String
    val versesCountLabel: (count: Int) -> String
    val langNotDownloadedTitle: (langName: String, bookName: String) -> String
    val langScopeNoticeTitle: (langName: String) -> String
    val langNotDownloadedBody: (bookName: String, nativeLang: String) -> String
    val langUnavailableBody: (bookName: String, langName: String) -> String
    val downloadBookButton: (bookName: String) -> String
    val allDownloadsButton: String
    val chapterWord: String

    // Verse Action Bar & Note Editor
    val closeVerseBarDesc: String
    val notePrefix: (note: String) -> String
    val highlightLabel: String
    val listenAction: String
    val bookmarkAction: String
    val bookmarkedAction: String
    val addNoteAction: String
    val editNoteAction: String
    val whatsappAction: String
    val facebookAction: String
    val copyAction: String
    val moreAction: String
    val personalNoteTitle: (ref: String) -> String
    val notePlaceholder: String

    // Scripture Picker Sheet
    val selectBookTitle: String
    val selectVerseTitle: (book: String, chapter: Int) -> String
    val booksCountSubtitle: String
    val chooseChapterSubtitle: (chapters: Int) -> String
    val tapVerseOrChapterSubtitle: String
    val closePickerDesc: String
    val searchBookPlaceholder: String
    val filterAll66: String
    val filterOld39: String
    val filterNew27: String
    val chaptersShort: (count: Int) -> String
    val openChapterFromVerse1: (chapter: Int) -> String

    // Language Selector Sheet
    val bibleLanguagesTitle: String
    val bibleLanguagesSubtitle: String
    val closeLanguageSelectorDesc: String
    val primaryLeftPageLabel: String
    val compareRightPageLabel: String
    val swapLanguagesDesc: String
    val spreadModeShort: String
    val parallelModeShort: String
    val tabPrimaryPrefix: (lang: String) -> String
    val tabComparePrefix: (lang: String) -> String
    val booksCountWord: (count: Int) -> String
    val offlineReadyLabel: String
    val activeLabel: String
    val offlineLabel: String
    val booksDownloadedProgress: (done: Int, total: Int) -> String
    val manageOfflineDownloads: String

    // Search Screen
    val searchScriptureTitle: String
    val searchPlaceholder: String
    val clearSearchDesc: String
    val filterAllBible: String
    val filterOldShort: String
    val filterNewShort: String
    val filterAllLanguages: String
    val searchPromptEmpty: String
    val searchNoResults: (query: String) -> String
    val searchMatchedCount: (count: Int) -> String

    // Library Screen
    val studyLibraryTitle: String
    val tabBookmarks: (count: Int) -> String
    val tabHighlights: (count: Int) -> String
    val tabNotes: (count: Int) -> String
    val tabHistory: (count: Int) -> String
    val noBookmarksTitle: String
    val noBookmarksSub: String
    val noHighlightsTitle: String
    val noHighlightsSub: String
    val noNotesTitle: String
    val noNotesSub: String
    val noHistoryTitle: String
    val noHistorySub: String
    val clearHistory: String
    val removeBookmarkDesc: String
    val clearHighlightDesc: String
    val editNoteDesc: String
    val deleteNoteDesc: String

    // Downloads Screen
    val offlineDownloadsTitle: String
    val offlineDownloadsSubtitle: String
    val storageUsedLabel: (size: String) -> String
    val languageBooksOfflineCount: (done: Int, total: Int) -> String
    val completeBibleOffline: String
    val downloadBibleButton: String
    val pauseAllDesc: String
    val resumeAllDesc: String
    val statusOfflineReady: String
    val statusDownloading: String
    val statusPaused: String
    val statusPartial: String
    val statusNotDownloaded: String
    val booksSlashCount: (done: Int, total: Int) -> String
    val downloadRemaining: String
    val downloadAll: String
    val readOffline: String
    val deletePackDesc: (langName: String) -> String
    val individualBooksHeader: (count: Int) -> String
    val hideBooksDesc: String
    val showBooksDesc: String
    val bookOfflineSize: (size: String) -> String
    val bookDownloading: String
    val bookQueued: String
    val bookPaused: String
    val bookNotDownloaded: String
    val deletePackDialogTitle: (langName: String) -> String
    val deletePackDialogBody: (langName: String) -> String
    val keepButton: String

    // Settings Screen
    val settingsTitle: String
    val systemLanguageCardTitle: String
    val systemLanguageSubtitle: String
    val systemLanguageHint: String
    val themesCardTitle: String
    val themeSummaryStats: (opacityPct: Int, blurDp: Int, fontLabel: String) -> String
    val customizeThemeButton: String
    val brightnessCardTitle: String
    val nightDarkModeLabel: String
    val nightDarkModeOnSub: String
    val nightDarkModeOffSub: String
    val languagesCardTitle: String
    val activePairLabel: (p: String, s: String) -> String
    val offlineStorageUsedLabel: (size: String) -> String
    val languagesButton: String
    val downloadsButton: String
    val typographyCardTitle: String
    val readingModeLabel: String
    val singleLanguageOnly: (langName: String) -> String
    val scriptureFontSizeLabel: String
    val pageSoundCardTitle: String
    val pageSoundToggleTitle: String
    val pageSoundMuted: String
    val pageSoundSilentMode: String
    val pageSoundActiveStatus: (styleName: String, volumePct: Int) -> String
    val randomSoundModeTitle: String
    val randomSoundModeSub: String
    val pageSoundVolumeTitle: String
    val choosePageSoundTitle: String
    val choosePageSoundSub: String
    val selectedDesc: String
    val previewButton: String
    val testForwardButton: String
    val testBackwardButton: String
    val audioNarrationCardTitle: String
    val narrationLanguageLabel: String
    val speechSpeedLabel: String
    val deviceVoicesStatus: (enReady: Boolean, neReady: Boolean, koReady: Boolean) -> String
    val voiceReady: String
    val voiceNotInstalled: String
    val voiceInstallTts: String
    val openAndroidTtsSettings: String
    val licensesCardTitle: String
    val authenticEditionsHeading: String
    val versionNameAbbrevLabel: String
    val fullTranslationNameLabel: String
    val copyrightOwnerLabel: String
    val copyrightNoticeLabel: String
    val publisherCreditLabel: String
    val licenseLabel: String
    val usagePermissionsLabel: String
    val sourceLabel: String
    val copyrightNotVerifiedYet: String
    val copyrightCategoryLabel: (CopyrightLicenseCategory) -> String
    val openSourceLinkError: String
    val expandSectionDesc: String
    val collapseSectionDesc: String
    val subsectionBibleCopyrightTitle: String
    val subsectionBibleCopyrightSubtitle: String
    val subsectionBibleTranslationCreditsTitle: String
    val subsectionBibleTranslationCreditsSubtitle: String
    val subsectionAudioMediaCreditsTitle: String
    val subsectionAudioMediaCreditsSubtitle: String
    val subsectionDeveloperTechCreditsTitle: String
    val subsectionDeveloperTechCreditsSubtitle: String
    val bibleSourceAttributions: List<BibleSourceAttribution>
    val bibleTranslationCreditItems: List<CreditEntryItem>
    val audioMediaCreditItems: List<CreditEntryItem>
    val developerTechCreditItems: List<CreditEntryItem>

    // Theme Studio Screen
    val themeStudioTitle: String
    val themeStudioSubtitle: String
    val themePresetsHeading: (count: Int) -> String
    val activePresetLabel: (name: String) -> String
    val liquidGlassOpticsHeading: String
    val hardwareBlurSub: String
    val compatFrostSub: String
    val glassOpacityLabel: String
    val glassBlurLabel: String
    val colorsHeading: String
    val colorsSub: String
    val backgroundColorLabel: String
    val accentColorLabel: String
    val textColorLabel: String
    val autoContrastButton: String
    val controlsNavHeading: String
    val controlsNavSub: String
    val buttonStyleLabel: String
    val navBarAppearanceLabel: String
    val readingPageTypographyHeading: String
    val readingPageTypographySub: String
    val readingPageBgLabel: String
    val scriptureFontStyleLabel: String
    val livePreviewPrefix: (presetName: String) -> String
    val livePreviewReadButton: String
    val applyThemeButton: String

    // Audio Bar, Notification & System Messages
    val audioActiveDesc: String
    val audioPrevDesc: String
    val audioNextDesc: String
    val audioPlayDesc: String
    val audioPauseDesc: String
    val audioStopDesc: String
    val audioNotifChannelName: String
    val audioNotifChannelDesc: String
    val audioNotifPrevious: String
    val audioNotifNext: String
    val audioNotifPlay: String
    val audioNotifPause: String
    val audioNotifStop: String
    val ttsNepaliMissingNotice: String
    val ttsKoreanMissingNotice: String
    val toastVerseCopied: String
    val toastWhatsappNotFound: String
    val toastFacebookCopied: String
    val shareChooserTitle: String
    val shareAppBrand: String

    // Enum Display Helpers
    val bibleLanguageName: (BibleLanguage) -> String
    val themePresetName: (ThemePreset) -> String
    val themePresetSubtitle: (ThemePreset) -> String
    val buttonStyleName: (ButtonStyleOption) -> String
    val navBarAppearanceName: (NavBarAppearance) -> String
    val readingBgName: (ReadingPageBackground) -> String
    val readingModeName: (ReadingMode) -> String
    val audioLanguageName: (AudioLanguage) -> String
    val pageSoundName: (PageTurnSoundStyle) -> String
    val pageSoundDescription: (PageTurnSoundStyle) -> String
    val bookDisplayName: (BibleBook, BibleLanguage) -> String
}

object EnglishAppStrings : AppStrings {
    override val appTitle = "HOLY BIBLE"
    override val appSubtitleLine = "English • Nepali (ULB & ONCV) • Korean"
    override val navHome = "Home"
    override val navBible = "Bible"
    override val navSearch = "Search"
    override val navSaved = "Saved"
    override val navSettings = "Settings"

    override val back = "Back"
    override val close = "Close"
    override val cancel = "Cancel"
    override val save = "Save"
    override val delete = "Delete"
    override val done = "Done"
    override val reset = "Reset"
    override val manage = "Manage"
    override val download = "Download"
    override val clear = "Clear"
    override val pause = "Pause"
    override val resume = "Resume"
    override val loading = "Loading..."

    override val homeThemesDesc = "Liquid Glass Themes"
    override val homeLanguagesDesc = "Bible Languages"
    override val homeSearchDesc = "Search Bible"
    override val homeSettingsDesc = "Settings"
    override val homeBibleCoverDesc = "Holy Bible Cover"
    override val heroTitle = "HOLY BIBLE"
    override val heroNepaliTitle = "पवित्र बाइबल"
    override val heroEditionFormat: (String, String) -> String = { p, s -> "$p & $s Flip Book Edition" }
    override val continueReadingPrefix: (String, String, String) -> String = { book, ch, v -> "Continue Reading • $book $ch:$v" }
    override val openHolyBible = "Open Holy Bible"
    override val oldTestament = "Old Testament"
    override val oldTestamentSub = "पुरानो नियम"
    override val oldTestamentSubtitle = "39 Books • Genesis – Malachi"
    override val newTestament = "New Testament"
    override val newTestamentSub = "नयाँ नियम"
    override val newTestamentSubtitle = "27 Books • Matthew – Revelation"
    override val downloadBibleCardTitle = "Download Bible • Offline Packs"
    override val downloadBibleCardStatus: (Int, Int, String) -> String = { ready, total, size -> "$ready/$total languages ready • $size stored" }
    override val dailyVerseBadge = "DAILY VERSE • आजको पद"
    override val shareDailyVerseDesc = "Share daily verse"
    override val readChapter = "Read Chapter"
    override val audioBibleCardTitle = "Audio Bible • अडियो बाइबल"
    override val audioBibleListenHint = "Listen in English, Nepali & Korean"
    override val audioBibleDesc = "Audio Bible"
    override val audioBiblePauseDesc = "Pause Audio Bible"
    override val audioBiblePlayDesc = "Play Audio Bible"
    override val quickLanguages = "Languages"
    override val quickLanguagesSub = "4 Translations"
    override val quickSearch = "Search"
    override val quickSearchSub = "All Offline"
    override val quickSaved = "Saved"
    override val quickSavedSub = "Notes & Marks"
    override val nnrvShortcutTitle = "NNRV Nepali Bible"
    override val nnrvShortcutSubtitle = "Read NNRV Bible Online"
    override val nnrvShortcutMeta = "Bible.com • Nepali New Revised Version • https://www.bible.com/"
    override val nnrvShortcutButton = "Read NNRV on Bible.com"
    override val nnrvOpenBrowserError = "Unable to open browser for Bible.com"

    override val backToHomeDesc = "Back to Home"
    override val chooseBookChapterDesc = "Choose Book and Chapter"
    override val selectLanguagesDesc = "Select Bible Languages"
    override val pageSoundOnDesc = "Page turn sounds ON"
    override val pageSoundOffDesc = "Page turn sounds OFF"
    override val audioBibleDescReader = "Audio Bible"
    override val readerSettingsDesc = "Reader Settings"
    override val prevChapterDesc = "Previous chapter"
    override val nextChapterDesc = "Next chapter"
    override val readerScrollSwipeHint: (Int) -> String = { n -> "Scroll chapter • Swipe to flip ($n verses)" }
    override val bookmarkedDesc = "Bookmarked"
    override val hasNoteDesc = "Has personal note"
    override val versesCountLabel: (Int) -> String = { n -> "$n verses" }
    override val langNotDownloadedTitle: (String, String) -> String = { lang, book -> "$lang ($book) is not downloaded yet" }
    override val langScopeNoticeTitle: (String) -> String = { lang -> "$lang scope notice" }
    override val langNotDownloadedBody: (String, String) -> String = { book, native -> "Download $book in $native for 100% offline reading." }
    override val langUnavailableBody: (String, String) -> String = { book, lang -> "$book is unavailable in $lang." }
    override val downloadBookButton: (String) -> String = { book -> "Download $book" }
    override val allDownloadsButton = "All Downloads"
    override val chapterWord = "Chapter"

    override val closeVerseBarDesc = "Close verse bar"
    override val notePrefix: (String) -> String = { note -> "Note: $note" }
    override val highlightLabel = "Highlight:"
    override val listenAction = "Listen"
    override val bookmarkAction = "Bookmark"
    override val bookmarkedAction = "Bookmarked"
    override val addNoteAction = "Add Note"
    override val editNoteAction = "Edit Note"
    override val whatsappAction = "WhatsApp"
    override val facebookAction = "Facebook"
    override val copyAction = "Copy"
    override val moreAction = "More"
    override val personalNoteTitle: (String) -> String = { ref -> "Personal Note • $ref" }
    override val notePlaceholder = "Write your reflection, prayer, or study note..."

    override val selectBookTitle = "Select Book • पुस्तक छान्नुहोस्"
    override val selectVerseTitle: (String, Int) -> String = { book, ch -> "$book $ch • Select Verse" }
    override val booksCountSubtitle = "66 Books of the Holy Bible"
    override val chooseChapterSubtitle: (Int) -> String = { ch -> "Choose Chapter ($ch)" }
    override val tapVerseOrChapterSubtitle = "Tap a verse or read entire chapter"
    override val closePickerDesc = "Close picker"
    override val searchBookPlaceholder = "Search book in any language..."
    override val filterAll66 = "All (66)"
    override val filterOld39 = "Old Test. (39)"
    override val filterNew27 = "New Test. (27)"
    override val chaptersShort: (Int) -> String = { ch -> "$ch ch" }
    override val openChapterFromVerse1: (Int) -> String = { ch -> "Open Chapter $ch from Verse 1" }

    override val bibleLanguagesTitle = "Bible Languages & Compare"
    override val bibleLanguagesSubtitle = "Switch or compare any two languages side-by-side"
    override val closeLanguageSelectorDesc = "Close language selector"
    override val primaryLeftPageLabel = "PRIMARY (LEFT PAGE)"
    override val compareRightPageLabel = "COMPARE (RIGHT PAGE)"
    override val swapLanguagesDesc = "Swap primary and secondary languages"
    override val spreadModeShort = "Spread"
    override val parallelModeShort = "Parallel"
    override val tabPrimaryPrefix: (String) -> String = { l -> "Primary: $l" }
    override val tabComparePrefix: (String) -> String = { l -> "Compare: $l" }
    override val booksCountWord: (Int) -> String = { n -> "$n Books" }
    override val offlineReadyLabel = "Offline Ready"
    override val activeLabel = "Active"
    override val offlineLabel = "Offline"
    override val booksDownloadedProgress: (Int, Int) -> String = { done, total -> "$done of $total books downloaded offline" }
    override val manageOfflineDownloads = "Manage Offline Downloads"

    override val searchScriptureTitle = "Search Scripture • बाइबल खोज्नुहोस्"
    override val searchPlaceholder = "Search English, ULB, ONCV, or Korean..."
    override val clearSearchDesc = "Clear search"
    override val filterAllBible = "All Bible"
    override val filterOldShort = "Old Test."
    override val filterNewShort = "New Test."
    override val filterAllLanguages = "All Languages"
    override val searchPromptEmpty = "Enter at least 2 characters to search\nall downloaded offline Bible languages"
    override val searchNoResults: (String) -> String = { q -> "No matching verses found in downloaded packs for \"$q\"" }
    override val searchMatchedCount: (Int) -> String = { n -> "$n verses matched" }

    override val studyLibraryTitle = "Study Library • अध्ययन संग्रह"
    override val tabBookmarks: (Int) -> String = { n -> "Bookmarks ($n)" }
    override val tabHighlights: (Int) -> String = { n -> "Highlights ($n)" }
    override val tabNotes: (Int) -> String = { n -> "Notes ($n)" }
    override val tabHistory: (Int) -> String = { n -> "History ($n)" }
    override val noBookmarksTitle = "No Bookmarks Yet"
    override val noBookmarksSub = "Tap any verse while reading to bookmark it in both English and Nepali."
    override val noHighlightsTitle = "No Highlighted Verses"
    override val noHighlightsSub = "Select any verse in the Flip Book and choose Gold, Rose, Sage, or Sky highlight."
    override val noNotesTitle = "No Personal Notes"
    override val noNotesSub = "Tap any verse and select Add Note to write reflections or sermon notes."
    override val noHistoryTitle = "No Reading History Yet"
    override val noHistorySub = "Chapters you read in the Holy Bible will appear here for quick return."
    override val clearHistory = "Clear History"
    override val removeBookmarkDesc = "Remove bookmark"
    override val clearHighlightDesc = "Clear highlight"
    override val editNoteDesc = "Edit note"
    override val deleteNoteDesc = "Delete note"

    override val offlineDownloadsTitle = "Offline Bible Downloads"
    override val offlineDownloadsSubtitle = "100% offline reading, search & chapter navigation"
    override val storageUsedLabel: (String) -> String = { size -> "Device Storage Used: $size" }
    override val languageBooksOfflineCount: (Int, Int) -> String = { done, total -> "$done of $total language books offline" }
    override val completeBibleOffline = "Complete Bible Offline"
    override val downloadBibleButton = "Download Bible"
    override val pauseAllDesc = "Pause all"
    override val resumeAllDesc = "Resume all"
    override val statusOfflineReady = "Offline Ready"
    override val statusDownloading = "Downloading"
    override val statusPaused = "Paused"
    override val statusPartial = "Partial"
    override val statusNotDownloaded = "Not Downloaded"
    override val booksSlashCount: (Int, Int) -> String = { done, total -> "$done / $total books" }
    override val downloadRemaining = "Download Remaining"
    override val downloadAll = "Download All"
    override val readOffline = "Read Offline"
    override val deletePackDesc: (String) -> String = { l -> "Delete $l offline pack" }
    override val individualBooksHeader: (Int) -> String = { n -> "Individual Books ($n)" }
    override val hideBooksDesc = "Hide books"
    override val showBooksDesc = "Show books"
    override val bookOfflineSize: (String) -> String = { s -> "Offline ($s)" }
    override val bookDownloading = "Downloading..."
    override val bookQueued = "Queued"
    override val bookPaused = "Paused"
    override val bookNotDownloaded = "Not downloaded"
    override val deletePackDialogTitle: (String) -> String = { l -> "Delete $l Offline Pack?" }
    override val deletePackDialogBody: (String) -> String = { l -> "This removes downloaded $l chapters from your device storage. You can re-download it anytime." }
    override val keepButton = "Keep"

    override val settingsTitle = "Settings • सेटिङहरू"
    override val systemLanguageCardTitle = "System Language"
    override val systemLanguageSubtitle = "Choose app interface language"
    override val systemLanguageHint = "Translates menus, buttons, settings, notifications, and system messages."
    override val themesCardTitle = "Liquid Glass & Themes"
    override val themeSummaryStats: (Int, Int, String) -> String = { op, blur, font -> "$op% opacity • ${blur}dp blur • $font" }
    override val customizeThemeButton = "Customize Theme & Live Preview"
    override val brightnessCardTitle = "Reading Theme Brightness"
    override val nightDarkModeLabel = "Night Study Dark Mode"
    override val nightDarkModeOnSub = "Dark Glass & low-glare reading"
    override val nightDarkModeOffSub = "Bright Liquid Glass daylight mode"
    override val languagesCardTitle = "Languages & Offline Packs"
    override val activePairLabel: (String, String) -> String = { p, s -> "Active Pair: $p ↔ $s" }
    override val offlineStorageUsedLabel: (String) -> String = { s -> "Offline storage used: $s" }
    override val languagesButton = "Languages"
    override val downloadsButton = "Downloads"
    override val typographyCardTitle = "Book Layout & Typography"
    override val readingModeLabel = "Reading Mode"
    override val singleLanguageOnly: (String) -> String = { l -> "$l Only" }
    override val scriptureFontSizeLabel = "Scripture Font Size"
    override val pageSoundCardTitle = "Page-Turning Sound"
    override val pageSoundToggleTitle = "Page-Turning Sound Audio"
    override val pageSoundMuted = "Muted (Sound OFF)"
    override val pageSoundSilentMode = "Silent Mode (No Sound)"
    override val pageSoundActiveStatus: (String, Int) -> String = { name, vol -> "$name • $vol% volume" }
    override val randomSoundModeTitle = "Random Sound Mode"
    override val randomSoundModeSub = "Varies realistic paper sounds on every flip"
    override val pageSoundVolumeTitle = "Page Sound Volume"
    override val choosePageSoundTitle = "Choose Page-Turning Sound"
    override val choosePageSoundSub = "Tap Preview to listen before selecting your favorite sound"
    override val selectedDesc = "Selected"
    override val previewButton = "Preview"
    override val testForwardButton = "Test Forward"
    override val testBackwardButton = "Test Backward"
    override val audioNarrationCardTitle = "Audio Bible Narration"
    override val narrationLanguageLabel = "Narration Language"
    override val speechSpeedLabel = "Speech Speed"
    override val deviceVoicesStatus: (Boolean, Boolean, Boolean) -> String = { en, ne, ko ->
        "Device Voices — English: ${if (en) "Ready" else "Not installed"} • Nepali: ${if (ne) "Ready" else "Install via TTS"} • Korean: ${if (ko) "Ready" else "Install via TTS"}"
    }
    override val voiceReady = "Ready"
    override val voiceNotInstalled = "Not installed"
    override val voiceInstallTts = "Install via TTS"
    override val openAndroidTtsSettings = "Open Android TTS Voice Settings"
    override val licensesCardTitle = "Copyright & Credits"
    override val authenticEditionsHeading = "About • Bible licenses, translation sources, audio/media & developer credits"
    override val versionNameAbbrevLabel = "Bible Version & Abbreviation"
    override val fullTranslationNameLabel = "Full Translation Name"
    override val copyrightOwnerLabel = "Copyright Owner / Publisher"
    override val copyrightNoticeLabel = "Copyright Notice"
    override val publisherCreditLabel = "Translation Credits"
    override val licenseLabel = "License Type"
    override val usagePermissionsLabel = "Usage & Distribution Permissions"
    override val sourceLabel = "Official Source Website"
    override val copyrightNotVerifiedYet = "Copyright information not verified yet."
    override val copyrightCategoryLabel: (CopyrightLicenseCategory) -> String = { category ->
        when (category) {
            CopyrightLicenseCategory.OPEN_LICENSE -> "Open License"
            CopyrightLicenseCategory.PUBLIC_DOMAIN -> "Public Domain"
            CopyrightLicenseCategory.COPYRIGHTED -> "Copyrighted"
            CopyrightLicenseCategory.UNVERIFIED -> "Not Verified"
        }
    }
    override val openSourceLinkError = "Unable to open browser for source website"
    override val expandSectionDesc = "Expand section"
    override val collapseSectionDesc = "Collapse section"
    override val subsectionBibleCopyrightTitle = "A. Bible Translation Copyright & Sources"
    override val subsectionBibleCopyrightSubtitle = "ULB, ONCV, KJV & KRV licenses, owners & official links"
    override val subsectionBibleTranslationCreditsTitle = "B. Bible Translation Credits"
    override val subsectionBibleTranslationCreditsSubtitle = "Translation contributors, Bible publishers & text sources"
    override val subsectionAudioMediaCreditsTitle = "C. Audio & Media Credits"
    override val subsectionAudioMediaCreditsSubtitle = "Audio Bible providers, voice contributors & media sources"
    override val subsectionDeveloperTechCreditsTitle = "D. Developer & Technology Credits"
    override val subsectionDeveloperTechCreditsSubtitle = "App developer info, third-party libraries & open-source credits"
    override val bibleSourceAttributions = listOf(
        BibleSourceAttribution(
            versionId = "ULB",
            title = "ULB – Unlocked Literal Bible (Nepali)",
            fullTranslationName = "Nepali Unlocked Literal Bible (पवित्र बाइबल, npiulb)",
            ownerPublisher = "Door43 World Missions Community (unfoldingWord®)",
            copyrightNotice = "Copyright © 2019 by Door43 World Missions Community",
            sourceUrls = listOf(
                "https://door43.org/",
                "https://ebible.org/find/show.php?id=npiulb",
                "https://creativecommons.org/licenses/by-sa/4.0/"
            ),
            license = "Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)",
            licenseCategory = CopyrightLicenseCategory.OPEN_LICENSE,
            usagePermissions = "Free to share, copy, distribute, display, and adapt (including offline distribution) with attribution to Door43 World Missions Community / unfoldingWord®, indication of changes, and ShareAlike under CC BY-SA 4.0.",
            translationCredits = "Translated and contributed by Door43 World Missions Community and unfoldingWord® Contributors • 66 Books (OT & NT)"
        ),
        BibleSourceAttribution(
            versionId = "ONCV",
            title = "ONCV – Old Nepali Contemporary Version",
            fullTranslationName = "Biblica® Open Nepali Contemporary Version™ (नेपाली समकालीन सर्वसुलभ संस्करण, npioncb)",
            ownerPublisher = "Biblica, Inc.",
            copyrightNotice = "Copyright © 1998, 2006, 2021, 2024 by Biblica, Inc.",
            sourceUrls = listOf(
                "https://www.biblica.com/",
                "https://open.bible/",
                "https://ebible.org/find/show.php?id=npioncb"
            ),
            license = "Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)",
            licenseCategory = CopyrightLicenseCategory.OPEN_LICENSE,
            usagePermissions = "Free to copy, share, distribute, and adapt in written, electronic, or audio form under CC BY-SA 4.0, provided attribution is given to Biblica, Inc., copyright notice is preserved, Biblica® trademark rules are respected, and derivative works use CC BY-SA 4.0.",
            translationCredits = "Biblica, Inc. Translation Team (Biblica® Open Nepali Contemporary Version™) • 66 Books (OT & NT)"
        ),
        BibleSourceAttribution(
            versionId = "KJV",
            title = "KJV – King James Version (English)",
            fullTranslationName = "Authorized King James Version (1769 Standard Text)",
            ownerPublisher = "Public Domain (1611 Authorized Version; 1769 Oxford/Cambridge Standard Text)",
            copyrightNotice = "Public Domain worldwide (Outside the United Kingdom, no copyright claims; inside the UK, printing is subject to perpetual Crown patent).",
            sourceUrls = listOf(
                "https://ebible.org/find/show.php?id=eng-kjv2006"
            ),
            license = "Public Domain (UK Crown Patent applies inside the United Kingdom)",
            licenseCategory = CopyrightLicenseCategory.PUBLIC_DOMAIN,
            usagePermissions = "Freely copyable, distributable, and embeddable worldwide outside the UK without royalties or permission. Commercial printing inside the United Kingdom requires authorization from Crown patentees.",
            translationCredits = "Authorized King James Version Translators (1611) & Benjamin Blayney (1769 Standard Edition); digital text via eBible.org • 66 Books (OT & NT)"
        ),
        BibleSourceAttribution(
            versionId = "KRV",
            title = "KRV – Korean Revised Version (개역한글)",
            fullTranslationName = "성경전서 개역한글판 (Korean Revised Version, 1961 Edition)",
            ownerPublisher = "Korean Bible Society (대한성서공회, 1961 Original Publisher)",
            copyrightNotice = "Public Domain (1961 Korean Revised Version copyright term expired around 2011–2012; distinct from the copyrighted 1998 New Korean Revised Version / 개역개정).",
            sourceUrls = listOf(
                "https://www.bskorea.or.kr/",
                "https://ebible.org/"
            ),
            license = "Public Domain (Expired Copyright – 1961 개역한글 Text)",
            licenseCategory = CopyrightLicenseCategory.PUBLIC_DOMAIN,
            usagePermissions = "The 1961 Korean Revised Version (개역한글) text is in the public domain and may be freely read, copied, and distributed. Note: The 1998 New Korean Revised Version (개역개정) remains copyrighted and is not used here.",
            translationCredits = "Korean Bible Society (대한성서공회) 1961 Revised Hangul Translation Committee • 66 Books (OT & NT)"
        )
    )
    override val bibleTranslationCreditItems = listOf(
        CreditEntryItem(
            title = "Translation Contributors",
            subtitle = "Scholars, translators & community reviewers",
            description = "• ULB (Nepali): Door43 World Missions Community & unfoldingWord® Contributors\n• ONCV (Nepali): Biblica® Open Nepali Contemporary Version™ Translation Team\n• KJV (English): Authorized King James Version Translators (1611) & Benjamin Blayney (1769)\n• KRV (Korean): Korean Bible Society 1961 Revised Hangul Translation Committee"
        ),
        CreditEntryItem(
            title = "Bible Publishers",
            subtitle = "Stewards & publishing organizations",
            description = "• Door43 World Missions Community / unfoldingWord® (ULB)\n• Biblica, Inc. — The International Bible Society (ONCV)\n• Oxford & Cambridge 1769 Standard Text (KJV Public Domain; Crown Patent inside the UK)\n• Korean Bible Society / 대한성서공회 (1961 Korean Revised Version)",
            links = listOf(
                CreditLinkItem("unfoldingWord / Door43", "https://door43.org/"),
                CreditLinkItem("Biblica, Inc.", "https://www.biblica.com/"),
                CreditLinkItem("Korean Bible Society", "https://www.bskorea.or.kr/")
            )
        ),
        CreditEntryItem(
            title = "Bible Text Sources",
            subtitle = "Verified repositories & external links",
            description = "• eBible.org Digital Scripture Archives (npiulb, npioncb, eng-kjv2006)\n• Open.Bible & Door43 Open-Licensed Bible Repositories\n• External Bible.com NNRV Reference Link (provided via Home Page shortcut)",
            links = listOf(
                CreditLinkItem("eBible.org", "https://ebible.org/"),
                CreditLinkItem("Open.Bible", "https://open.bible/"),
                CreditLinkItem("Bible.com (NNRV)", "https://www.bible.com/bible/1483/GEN.1.NNRV")
            )
        )
    )
    override val audioMediaCreditItems = listOf(
        CreditEntryItem(
            title = "Audio Bible Providers",
            subtitle = "Speech synthesis & foreground playback architecture",
            description = "Verse-by-verse Audio Bible playback is powered by the Android system Text-to-Speech (TTS) engine with integrated foreground media playback controls and bilingual chapter sequencing."
        ),
        CreditEntryItem(
            title = "Narrators & Voice Contributors",
            subtitle = "Device & system voice packs",
            description = "Narration voices are provided by the installed Android Text-to-Speech voice engines and system language voice packs configured on your device for English, Nepali (ne-NP), and Korean (ko-KR)."
        ),
        CreditEntryItem(
            title = "Image & Media Sources",
            subtitle = "Bundled artwork, icons & page-turning audio",
            description = "• Custom leather Holy Bible cover artwork, daily devotional banner & adaptive app icon graphics bundled for offline viewing\n• 10 realistic Bible page-turning audio samples bundled for offline SoundPool playback\n• Vector navigation & UI iconography from Google Material Symbols / Material Icons",
            links = listOf(
                CreditLinkItem("Material Symbols", "https://fonts.google.com/icons")
            )
        )
    )
    override val developerTechCreditItems = listOf(
        CreditEntryItem(
            title = "App Developer Information",
            subtitle = "Holy Bible — English • Nepali (ULB & ONCV) • Korean",
            description = "Designed and built as an offline-first multilingual Holy Bible study app featuring dual-page reading, Audio Bible narration, bookmarks, highlights, study notes, search, and Liquid Glass & Classic themes."
        ),
        CreditEntryItem(
            title = "Third-Party Libraries",
            subtitle = "Core Android, Jetpack & networking stack",
            description = "• Kotlin & Kotlin Coroutines / Flow\n• Jetpack Compose, Material Design 3, Navigation Compose & AndroidX Lifecycle\n• AndroidX Room SQLite Persistence & Jetpack DataStore Preferences\n• Retrofit, OkHttp & Moshi JSON\n• Bundled Fonts: Playfair Display, Crimson Pro, Lora, Merriweather, Inter, Plus Jakarta Sans, Noto Serif Devanagari & Noto Serif KR",
            links = listOf(
                CreditLinkItem("Jetpack Compose", "https://developer.android.com/jetpack/compose"),
                CreditLinkItem("Google Fonts", "https://fonts.google.com/")
            )
        ),
        CreditEntryItem(
            title = "Open-Source Software Credits",
            subtitle = "Community licenses & attributions",
            description = "• Android Open Source Project (AOSP) & AndroidX Jetpack Libraries — Apache License 2.0\n• Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0) for open-licensed Nepali Bible editions\n• JUnit4 & Robolectric for automated local JVM & UI testing",
            links = listOf(
                CreditLinkItem("Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0"),
                CreditLinkItem("CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/"),
                CreditLinkItem("Robolectric", "https://robolectric.org/")
            )
        )
    )

    override val themeStudioTitle = "Liquid Glass & Theme Studio"
    override val themeStudioSubtitle = "Live preview changes before applying"
    override val themePresetsHeading: (Int) -> String = { count -> "Theme Presets ($count Styles)" }
    override val activePresetLabel: (String) -> String = { name -> "Active: $name" }
    override val liquidGlassOpticsHeading = "Liquid Glass Optics"
    override val hardwareBlurSub = "Hardware-accelerated frosted blur enabled"
    override val compatFrostSub = "Compatibility translucent frost mode (Android < 12)"
    override val glassOpacityLabel = "Glass Opacity / Transparency"
    override val glassBlurLabel = "Glass Blur Intensity"
    override val colorsHeading = "Colors"
    override val colorsSub = "Customize backdrop, accent, and text colors"
    override val backgroundColorLabel = "Background Color"
    override val accentColorLabel = "Accent Color"
    override val textColorLabel = "Text Color"
    override val autoContrastButton = "Auto Contrast"
    override val controlsNavHeading = "Controls & Navigation Style"
    override val controlsNavSub = "Customize button geometry and bottom bar appearance"
    override val buttonStyleLabel = "Button Style"
    override val navBarAppearanceLabel = "Navigation Bar Appearance"
    override val readingPageTypographyHeading = "Bible Reading Page & Typography"
    override val readingPageTypographySub = "Customize reading paper backdrop, font style & size"
    override val readingPageBgLabel = "Reading Page Background"
    override val scriptureFontStyleLabel = "Scripture Font Style"
    override val livePreviewPrefix: (String) -> String = { name -> "LIVE PREVIEW • $name" }
    override val livePreviewReadButton = "Read"
    override val applyThemeButton = "Apply Theme"

    override val audioActiveDesc = "Audio Bible active"
    override val audioPrevDesc = "Previous verse"
    override val audioNextDesc = "Next verse"
    override val audioPlayDesc = "Play audio"
    override val audioPauseDesc = "Pause audio"
    override val audioStopDesc = "Stop audio"
    override val audioNotifChannelName = "Audio Bible Playback"
    override val audioNotifChannelDesc = "Controls for background Holy Bible audio reading"
    override val audioNotifPrevious = "Previous"
    override val audioNotifNext = "Next"
    override val audioNotifPlay = "Play"
    override val audioNotifPause = "Pause"
    override val audioNotifStop = "Stop"
    override val ttsNepaliMissingNotice = "Nepali TTS voice pack not detected on this device; install Nepali voice in Android TTS settings for native Nepali pronunciation."
    override val ttsKoreanMissingNotice = "Korean TTS voice pack not detected on this device; install Korean voice in Android TTS settings for native Korean pronunciation."
    override val toastVerseCopied = "Verse copied to clipboard"
    override val toastWhatsappNotFound = "WhatsApp not found; opening share sheet"
    override val toastFacebookCopied = "Verse copied! Opening share sheet for Facebook"
    override val shareChooserTitle = "Share Holy Scripture"
    override val shareAppBrand = "Holy Bible"

    override val bibleLanguageName: (BibleLanguage) -> String = { it.displayName }
    override val themePresetName: (ThemePreset) -> String = { it.displayName }
    override val themePresetSubtitle: (ThemePreset) -> String = { it.subtitle }
    override val buttonStyleName: (ButtonStyleOption) -> String = { it.label }
    override val navBarAppearanceName: (NavBarAppearance) -> String = { it.label }
    override val readingBgName: (ReadingPageBackground) -> String = { it.label }
    override val readingModeName: (ReadingMode) -> String = { it.label }
    override val audioLanguageName: (AudioLanguage) -> String = { it.label }
    override val pageSoundName: (PageTurnSoundStyle) -> String = { it.displayName }
    override val pageSoundDescription: (PageTurnSoundStyle) -> String = { it.description }
    override val bookDisplayName: (BibleBook, BibleLanguage) -> String = { book, lang -> book.nameFor(lang) }
}

object NepaliAppStrings : AppStrings {
    override val appTitle = "पवित्र बाइबल"
    override val appSubtitleLine = "अंग्रेजी • नेपाली (ULB र ONCV) • कोरियन"
    override val navHome = "गृहपृष्ठ"
    override val navBible = "बाइबल"
    override val navSearch = "खोज"
    override val navSaved = "सुरक्षित"
    override val navSettings = "सेटिङहरू"

    override val back = "पछाडि"
    override val close = "बन्द गर्नुहोस्"
    override val cancel = "रद्द गर्नुहोस्"
    override val save = "सुरक्षित गर्नुहोस्"
    override val delete = "मेटाउनुहोस्"
    override val done = "सम्पन्न"
    override val reset = "रिसेट"
    override val manage = "व्यवस्थापन"
    override val download = "डाउनलोड"
    override val clear = "हटाउनुहोस्"
    override val pause = "रोक्नुहोस्"
    override val resume = "पुनः सुरु"
    override val loading = "लोड हुँदैछ..."

    override val homeThemesDesc = "लिक्विड ग्लास थिमहरू"
    override val homeLanguagesDesc = "बाइबल भाषाहरू"
    override val homeSearchDesc = "बाइबल खोज्नुहोस्"
    override val homeSettingsDesc = "सेटिङहरू"
    override val homeBibleCoverDesc = "पवित्र बाइबल आवरण"
    override val heroTitle = "पवित्र बाइबल"
    override val heroNepaliTitle = "HOLY BIBLE"
    override val heroEditionFormat: (String, String) -> String = { p, s -> "$p र $s फ्लिप बुक संस्करण" }
    override val continueReadingPrefix: (String, String, String) -> String = { book, ch, v -> "पढ्न जारी राख्नुहोस् • $book $ch:$v" }
    override val openHolyBible = "पवित्र बाइबल खोल्नुहोस्"
    override val oldTestament = "पुरानो नियम"
    override val oldTestamentSub = "Old Testament"
    override val oldTestamentSubtitle = "३९ पुस्तकहरू • उत्पत्ति – मलाकी"
    override val newTestament = "नयाँ नियम"
    override val newTestamentSub = "New Testament"
    override val newTestamentSubtitle = "२७ पुस्तकहरू • मत्ती – प्रकाश"
    override val downloadBibleCardTitle = "बाइबल डाउनलोड • अफलाइन प्याक"
    override val downloadBibleCardStatus: (Int, Int, String) -> String = { ready, total, size ->
        "${NepaliFormatUtils.formatNumber(ready, SystemLanguage.NEPALI)}/${NepaliFormatUtils.formatNumber(total, SystemLanguage.NEPALI)} भाषा तयार • $size भण्डारण"
    }
    override val dailyVerseBadge = "आजको पद • DAILY VERSE"
    override val shareDailyVerseDesc = "आजको पद सेयर गर्नुहोस्"
    override val readChapter = "अध्याय पढ्नुहोस्"
    override val audioBibleCardTitle = "अडियो बाइबल"
    override val audioBibleListenHint = "अंग्रेजी, नेपाली र कोरियनमा सुन्नुहोस्"
    override val audioBibleDesc = "अडियो बाइबल"
    override val audioBiblePauseDesc = "अडियो बाइबल रोक्नुहोस्"
    override val audioBiblePlayDesc = "अडियो बाइबल बजाउनुहोस्"
    override val quickLanguages = "भाषाहरू"
    override val quickLanguagesSub = "४ अनुवादहरू"
    override val quickSearch = "खोज"
    override val quickSearchSub = "सबै अफलाइन"
    override val quickSaved = "सुरक्षित"
    override val quickSavedSub = "नोट र चिन्ह"
    override val nnrvShortcutTitle = "NNRV नेपाली बाइबल"
    override val nnrvShortcutSubtitle = "अनलाइन NNRV बाइबल पढ्नुहोस्"
    override val nnrvShortcutMeta = "Bible.com • नेपाली नयाँ संशोधित संस्करण • https://www.bible.com/"
    override val nnrvShortcutButton = "Bible.com मा NNRV पढ्नुहोस्"
    override val nnrvOpenBrowserError = "Bible.com खोल्न ब्राउजर उपलब्ध छैन"

    override val backToHomeDesc = "गृहपृष्ठमा फर्कनुहोस्"
    override val chooseBookChapterDesc = "पुस्तक र अध्याय छान्नुहोस्"
    override val selectLanguagesDesc = "बाइबल भाषाहरू छान्नुहोस्"
    override val pageSoundOnDesc = "पाना पल्टाउने आवाज चालु"
    override val pageSoundOffDesc = "पाना पल्टाउने आवाज बन्द"
    override val audioBibleDescReader = "अडियो बाइबल"
    override val readerSettingsDesc = "पठन सेटिङहरू"
    override val prevChapterDesc = "अघिल्लो अध्याय"
    override val nextChapterDesc = "अर्को अध्याय"
    override val readerScrollSwipeHint: (Int) -> String = { n ->
        "अध्याय स्क्रोल • पल्टाउन स्वाइप (${NepaliFormatUtils.formatNumber(n, SystemLanguage.NEPALI)} पद)"
    }
    override val bookmarkedDesc = "बुकमार्क गरिएको"
    override val hasNoteDesc = "व्यक्तिगत नोट छ"
    override val versesCountLabel: (Int) -> String = { n -> "${NepaliFormatUtils.formatNumber(n, SystemLanguage.NEPALI)} पद" }
    override val langNotDownloadedTitle: (String, String) -> String = { lang, book -> "$lang ($book) अझै डाउनलोड भएको छैन" }
    override val langScopeNoticeTitle: (String) -> String = { lang -> "$lang उपलब्धता सूचना" }
    override val langNotDownloadedBody: (String, String) -> String = { book, native -> "१००% अफलाइन पढ्न $native मा $book डाउनलोड गर्नुहोस्।" }
    override val langUnavailableBody: (String, String) -> String = { book, lang -> "$book $lang मा उपलब्ध छैन।" }
    override val downloadBookButton: (String) -> String = { book -> "$book डाउनलोड" }
    override val allDownloadsButton = "सबै डाउनलोडहरू"
    override val chapterWord = "अध्याय"

    override val closeVerseBarDesc = "पद पट्टी बन्द गर्नुहोस्"
    override val notePrefix: (String) -> String = { note -> "नोट: $note" }
    override val highlightLabel = "हाइलाइट:"
    override val listenAction = "सुन्नुहोस्"
    override val bookmarkAction = "बुकमार्क"
    override val bookmarkedAction = "बुकमार्क गरियो"
    override val addNoteAction = "नोट थप्नुहोस्"
    override val editNoteAction = "नोट सम्पादन"
    override val whatsappAction = "ह्वाट्सएप"
    override val facebookAction = "फेसबुक"
    override val copyAction = "प्रतिलिपि"
    override val moreAction = "थप"
    override val personalNoteTitle: (String) -> String = { ref -> "व्यक्तिगत नोट • $ref" }
    override val notePlaceholder = "आफ्नो मनन, प्रार्थना वा अध्ययन नोट लेख्नुहोस्..."

    override val selectBookTitle = "पुस्तक छान्नुहोस्"
    override val selectVerseTitle: (String, Int) -> String = { book, ch -> "$book ${NepaliFormatUtils.formatNumber(ch, SystemLanguage.NEPALI)} • पद छान्नुहोस्" }
    override val booksCountSubtitle = "पवित्र बाइबलका ६६ पुस्तकहरू"
    override val chooseChapterSubtitle: (Int) -> String = { ch -> "अध्याय छान्नुहोस् (${NepaliFormatUtils.formatNumber(ch, SystemLanguage.NEPALI)})" }
    override val tapVerseOrChapterSubtitle = "पद ट्याप गर्नुहोस् वा पूरा अध्याय पढ्नुहोस्"
    override val closePickerDesc = "चयन बन्द गर्नुहोस्"
    override val searchBookPlaceholder = "कुनै पनि भाषामा पुस्तक खोज्नुहोस्..."
    override val filterAll66 = "सबै (६६)"
    override val filterOld39 = "पुरानो (३९)"
    override val filterNew27 = "नयाँ (२७)"
    override val chaptersShort: (Int) -> String = { ch -> "${NepaliFormatUtils.formatNumber(ch, SystemLanguage.NEPALI)} अ." }
    override val openChapterFromVerse1: (Int) -> String = { ch ->
        "अध्याय ${NepaliFormatUtils.formatNumber(ch, SystemLanguage.NEPALI)} पद १ बाट खोल्नुहोस्"
    }

    override val bibleLanguagesTitle = "बाइबल भाषा र तुलना"
    override val bibleLanguagesSubtitle = "कुनै दुई भाषा सँगसँगै छान्नुहोस् वा तुलना गर्नुहोस्"
    override val closeLanguageSelectorDesc = "भाषा चयन बन्द गर्नुहोस्"
    override val primaryLeftPageLabel = "मुख्य (बायाँ पाना)"
    override val compareRightPageLabel = "तुलना (दायाँ पाना)"
    override val swapLanguagesDesc = "मुख्य र दोस्रो भाषा साट्नुहोस्"
    override val spreadModeShort = "दुई पाना"
    override val parallelModeShort = "समानान्तर"
    override val tabPrimaryPrefix: (String) -> String = { l -> "मुख्य: $l" }
    override val tabComparePrefix: (String) -> String = { l -> "तुलना: $l" }
    override val booksCountWord: (Int) -> String = { n -> "${NepaliFormatUtils.formatNumber(n, SystemLanguage.NEPALI)} पुस्तक" }
    override val offlineReadyLabel = "अफलाइन तयार"
    override val activeLabel = "सक्रिय"
    override val offlineLabel = "अफलाइन"
    override val booksDownloadedProgress: (Int, Int) -> String = { done, total ->
        "${NepaliFormatUtils.formatNumber(total, SystemLanguage.NEPALI)} मध्ये ${NepaliFormatUtils.formatNumber(done, SystemLanguage.NEPALI)} पुस्तक अफलाइन डाउनलोड भयो"
    }
    override val manageOfflineDownloads = "अफलाइन डाउनलोड व्यवस्थापन"

    override val searchScriptureTitle = "पवित्र शास्त्र खोज्नुहोस्"
    override val searchPlaceholder = "अंग्रेजी, ULB, ONCV वा कोरियनमा खोज्नुहोस्..."
    override val clearSearchDesc = "खोज सफा गर्नुहोस्"
    override val filterAllBible = "सम्पन्न बाइबल"
    override val filterOldShort = "पुरानो नियम"
    override val filterNewShort = "नयाँ नियम"
    override val filterAllLanguages = "सबै भाषा"
    override val searchPromptEmpty = "सबै डाउनलोड गरिएका अफलाइन बाइबल भाषाहरूमा\nखोज्न कम्तीमा २ अक्षर टाइप गर्नुहोस्"
    override val searchNoResults: (String) -> String = { q -> "डाउनलोड गरिएका प्याकहरूमा \"$q\" सँग मिल्ने पद भेटिएन" }
    override val searchMatchedCount: (Int) -> String = { n -> "${NepaliFormatUtils.formatNumber(n, SystemLanguage.NEPALI)} पदहरू भेटिए" }

    override val studyLibraryTitle = "अध्ययन संग्रह"
    override val tabBookmarks: (Int) -> String = { n -> "बुकमार्क (${NepaliFormatUtils.formatNumber(n, SystemLanguage.NEPALI)})" }
    override val tabHighlights: (Int) -> String = { n -> "हाइलाइट (${NepaliFormatUtils.formatNumber(n, SystemLanguage.NEPALI)})" }
    override val tabNotes: (Int) -> String = { n -> "नोट (${NepaliFormatUtils.formatNumber(n, SystemLanguage.NEPALI)})" }
    override val tabHistory: (Int) -> String = { n -> "इतिहास (${NepaliFormatUtils.formatNumber(n, SystemLanguage.NEPALI)})" }
    override val noBookmarksTitle = "अहिलेसम्म कुनै बुकमार्क छैन"
    override val noBookmarksSub = "पढ्दाखेरि कुनै पनि पदमा ट्याप गरी अंग्रेजी र नेपाली दुवैमा बुकमार्क गर्नुहोस्।"
    override val noHighlightsTitle = "कुनै हाइलाइट गरिएका पद छैनन्"
    override val noHighlightsSub = "फ्लिप बुकमा कुनै पद छानी सुनौलो, गुलाबी, हरियो वा आकाशी रङ लगाउनुहोस्।"
    override val noNotesTitle = "कुनै व्यक्तिगत नोट छैन"
    override val noNotesSub = "मनन वा प्रवचन नोट लेख्न कुनै पनि पदमा ट्याप गरी नोट थप्नुहोस् छान्नुहोस्।"
    override val noHistoryTitle = "अहिलेसम्म पठन इतिहास छैन"
    override val noHistorySub = "पवित्र बाइबलमा तपाईंले पढ्नुभएका अध्यायहरू छिटो फर्किन यहाँ देखिनेछन्।"
    override val clearHistory = "इतिहास मेटाउनुहोस्"
    override val removeBookmarkDesc = "बुकमार्क हटाउनुहोस्"
    override val clearHighlightDesc = "हाइलाइट हटाउनुहोस्"
    override val editNoteDesc = "नोट सम्पादन गर्नुहोस्"
    override val deleteNoteDesc = "नोट मेटाउनुहोस्"

    override val offlineDownloadsTitle = "अफलाइन बाइबल डाउनलोड"
    override val offlineDownloadsSubtitle = "१००% अफलाइन पठन, खोज र अध्याय नेभिगेसन"
    override val storageUsedLabel: (String) -> String = { size -> "प्रयोग भएको उपकरण भण्डारण: $size" }
    override val languageBooksOfflineCount: (Int, Int) -> String = { done, total ->
        "${NepaliFormatUtils.formatNumber(total, SystemLanguage.NEPALI)} मध्ये ${NepaliFormatUtils.formatNumber(done, SystemLanguage.NEPALI)} भाषाका पुस्तकहरू अफलाइन"
    }
    override val completeBibleOffline = "पूर्ण बाइबल अफलाइन तयार"
    override val downloadBibleButton = "बाइबल डाउनलोड गर्नुहोस्"
    override val pauseAllDesc = "सबै रोक्नुहोस्"
    override val resumeAllDesc = "सबै पुनः सुरु"
    override val statusOfflineReady = "अफलाइन तयार"
    override val statusDownloading = "डाउनलोड हुँदै"
    override val statusPaused = "रोकिएको"
    override val statusPartial = "आंशिक"
    override val statusNotDownloaded = "डाउनलोड नभएको"
    override val booksSlashCount: (Int, Int) -> String = { done, total ->
        "${NepaliFormatUtils.formatNumber(done, SystemLanguage.NEPALI)} / ${NepaliFormatUtils.formatNumber(total, SystemLanguage.NEPALI)} पुस्तक"
    }
    override val downloadRemaining = "बाँकी डाउनलोड"
    override val downloadAll = "सबै डाउनलोड"
    override val readOffline = "अफलाइन पढ्नुहोस्"
    override val deletePackDesc: (String) -> String = { l -> "$l अफलाइन प्याक मेटाउनुहोस्" }
    override val individualBooksHeader: (Int) -> String = { n -> "व्यक्तिगत पुस्तकहरू (${NepaliFormatUtils.formatNumber(n, SystemLanguage.NEPALI)})" }
    override val hideBooksDesc = "पुस्तकहरू लुकाउनुहोस्"
    override val showBooksDesc = "पुस्तकहरू देखाउनुहोस्"
    override val bookOfflineSize: (String) -> String = { s -> "अफलाइन ($s)" }
    override val bookDownloading = "डाउनलोड हुँदै..."
    override val bookQueued = "पङ्क्तिबद्ध"
    override val bookPaused = "रोकिएको"
    override val bookNotDownloaded = "डाउनलोड नभएको"
    override val deletePackDialogTitle: (String) -> String = { l -> "$l अफलाइन प्याक मेटाउने?" }
    override val deletePackDialogBody: (String) -> String = { l -> "यसले तपाईंको उपकरण भण्डारणबाट डाउनलोड गरिएका $l अध्यायहरू हटाउँछ। तपाईं जहिले पनि पुनः डाउनलोड गर्न सक्नुहुन्छ।" }
    override val keepButton = "राख्नुहोस्"

    override val settingsTitle = "सेटिङहरू"
    override val systemLanguageCardTitle = "प्रणाली भाषा (System Language)"
    override val systemLanguageSubtitle = "एप इन्टरफेसको भाषा छान्नुहोस्"
    override val systemLanguageHint = "मेनु, बटन, सेटिङ, सूचना र प्रणाली सन्देशहरूको भाषा परिवर्तन गर्छ।"
    override val themesCardTitle = "लिक्विड ग्लास र थिमहरू"
    override val themeSummaryStats: (Int, Int, String) -> String = { op, blur, font ->
        "${NepaliFormatUtils.formatNumber(op, SystemLanguage.NEPALI)}% पारदर्शिता • ${NepaliFormatUtils.formatNumber(blur, SystemLanguage.NEPALI)} dp ब्लर • $font"
    }
    override val customizeThemeButton = "थिम अनुकूलन र लाइभ पूर्वावलोकन"
    override val brightnessCardTitle = "पठन थिम उज्यालोपन"
    override val nightDarkModeLabel = "रात्रि अध्ययन डार्क मोड"
    override val nightDarkModeOnSub = "कम चमक र रात्रि पठनका लागि डार्क ग्लास"
    override val nightDarkModeOffSub = "दिनको समयका लागि उज्यालो थिम"
    override val languagesCardTitle = "बाइबल भाषा र अफलाइन प्याक"
    override val activePairLabel: (String, String) -> String = { p, s -> "सक्रिय जोडी: $p ↔ $s" }
    override val offlineStorageUsedLabel: (String) -> String = { s -> "अफलाइन भण्डारण: $s" }
    override val languagesButton = "भाषाहरू"
    override val downloadsButton = "डाउनलोड"
    override val typographyCardTitle = "पुस्तक लेआउट र अक्षर शैली"
    override val readingModeLabel = "पठन मोड"
    override val singleLanguageOnly: (String) -> String = { l -> "$l मात्र" }
    override val scriptureFontSizeLabel = "शास्त्र अक्षर साइज"
    override val pageSoundCardTitle = "पाना पल्टाउने आवाज"
    override val pageSoundToggleTitle = "पाना पल्टाउने ध्वनि"
    override val pageSoundMuted = "मौन (आवाज बन्द)"
    override val pageSoundSilentMode = "मौन मोड (आवाज छैन)"
    override val pageSoundActiveStatus: (String, Int) -> String = { name, vol ->
        "$name • ${NepaliFormatUtils.formatNumber(vol, SystemLanguage.NEPALI)}% भोल्युम"
    }
    override val randomSoundModeTitle = "अनियमित ध्वनि मोड"
    override val randomSoundModeSub = "हरेक पाना पल्टाउँदा स्वाभाविक कागजको आवाज बदलिन्छ"
    override val pageSoundVolumeTitle = "पाना ध्वनि भोल्युम"
    override val choosePageSoundTitle = "पाना पल्टाउने आवाज छान्नुहोस्"
    override val choosePageSoundSub = "छान्नुअघि सुन्नको लागि 'सुन्नुहोस्' ट्याप गर्नुहोस्"
    override val selectedDesc = "छानिएको"
    override val previewButton = "सुन्नुहोस्"
    override val testForwardButton = "अगाडि परीक्षण"
    override val testBackwardButton = "पछाडि परीक्षण"
    override val audioNarrationCardTitle = "अडियो बाइबल वाचन"
    override val narrationLanguageLabel = "वाचन भाषा"
    override val speechSpeedLabel = "वाचन गति"
    override val deviceVoicesStatus: (Boolean, Boolean, Boolean) -> String = { en, ne, ko ->
        "उपकरण आवाज — अंग्रेजी: ${if (en) "तयार" else "स्थापित छैन"} • नेपाली: ${if (ne) "तयार" else "TTS बाट स्थापना"} • कोरियन: ${if (ko) "तयार" else "TTS बाट स्थापना"}"
    }
    override val voiceReady = "तयार"
    override val voiceNotInstalled = "स्थापित छैन"
    override val voiceInstallTts = "TTS बाट स्थापना"
    override val openAndroidTtsSettings = "एन्ड्रोइड TTS आवाज सेटिङ खोल्नुहोस्"
    override val licensesCardTitle = "प्रतिलिपि अधिकार र श्रेय"
    override val authenticEditionsHeading = "परिचय • बाइबल अनुमतिपत्र, अनुवाद स्रोत, अडियो/मिडिया र विकासकर्ता श्रेय"
    override val versionNameAbbrevLabel = "बाइबल संस्करण र संक्षिप्त नाम"
    override val fullTranslationNameLabel = "पूर्ण अनुवाद नाम"
    override val copyrightOwnerLabel = "प्रतिलिपि अधिकार धनी / प्रकाशक"
    override val copyrightNoticeLabel = "प्रतिलिपि अधिकार सूचना"
    override val publisherCreditLabel = "अनुवाद श्रेय"
    override val licenseLabel = "अनुमतिपत्र प्रकार"
    override val usagePermissionsLabel = "प्रयोग र वितरण अनुमति"
    override val sourceLabel = "आधिकारिक स्रोत वेबसाइट"
    override val copyrightNotVerifiedYet = "प्रतिलिपि अधिकार जानकारी अझै प्रमाणित गरिएको छैन।"
    override val copyrightCategoryLabel: (CopyrightLicenseCategory) -> String = { category ->
        when (category) {
            CopyrightLicenseCategory.OPEN_LICENSE -> "खुला अनुमतिपत्र (Open License)"
            CopyrightLicenseCategory.PUBLIC_DOMAIN -> "सार्वजनिक क्षेत्र (Public Domain)"
            CopyrightLicenseCategory.COPYRIGHTED -> "प्रतिलिपि अधिकार सुरक्षित"
            CopyrightLicenseCategory.UNVERIFIED -> "प्रमाणित नभएको"
        }
    }
    override val openSourceLinkError = "स्रोत वेबसाइट खोल्न ब्राउजर उपलब्ध छैन"
    override val expandSectionDesc = "खण्ड खोल्नुहोस्"
    override val collapseSectionDesc = "खण्ड बन्द गर्नुहोस्"
    override val subsectionBibleCopyrightTitle = "क. बाइबल अनुवाद प्रतिलिपि अधिकार र स्रोतहरू"
    override val subsectionBibleCopyrightSubtitle = "ULB, ONCV, KJV र KRV अनुमतिपत्र, धनी र आधिकारिक लिङ्कहरू"
    override val subsectionBibleTranslationCreditsTitle = "ख. बाइबल अनुवाद श्रेय"
    override val subsectionBibleTranslationCreditsSubtitle = "अनुवाद योगदानकर्ता, बाइबल प्रकाशक र पाठ स्रोतहरू"
    override val subsectionAudioMediaCreditsTitle = "ग. अडियो र मिडिया श्रेय"
    override val subsectionAudioMediaCreditsSubtitle = "अडियो बाइबल प्रदायक, आवाज योगदानकर्ता र मिडिया स्रोतहरू"
    override val subsectionDeveloperTechCreditsTitle = "घ. विकासकर्ता र प्रविधि श्रेय"
    override val subsectionDeveloperTechCreditsSubtitle = "एप विकासकर्ता जानकारी, तेस्रो-पक्ष पुस्तकालय र खुला-स्रोत श्रेय"
    override val bibleSourceAttributions = listOf(
        BibleSourceAttribution(
            versionId = "ULB",
            title = "ULB – Unlocked Literal Bible (अनलक्ड लिटरल बाइबल)",
            fullTranslationName = "नेपाली अनलक्ड लिटरल बाइबल (पवित्र बाइबल, npiulb)",
            ownerPublisher = "Door43 World Missions Community (unfoldingWord®)",
            copyrightNotice = "प्रतिलिपि अधिकार © २०१९ Door43 World Missions Community",
            sourceUrls = listOf(
                "https://door43.org/",
                "https://ebible.org/find/show.php?id=npiulb",
                "https://creativecommons.org/licenses/by-sa/4.0/"
            ),
            license = "Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)",
            licenseCategory = CopyrightLicenseCategory.OPEN_LICENSE,
            usagePermissions = "Door43 World Missions Community / unfoldingWord® लाई श्रेय दिँदै, परिवर्तन भए उल्लेख गरी CC BY-SA 4.0 अन्तर्गत साझेदारी गरेमा निःशुल्क प्रतिलिपि, वितरण र रूपान्तरण गर्न सकिन्छ।",
            translationCredits = "Door43 World Missions Community र unfoldingWord® योगदानकर्ताहरू द्वारा अनुवादित • ६६ पुस्तकहरू"
        ),
        BibleSourceAttribution(
            versionId = "ONCV",
            title = "ONCV – Old Nepali Contemporary Version (नेपाली समकालीन संस्करण)",
            fullTranslationName = "Biblica® नेपाली समकालीन सर्वसुलभ संस्करण™ (Open Nepali Contemporary Version, npioncb)",
            ownerPublisher = "Biblica, Inc.",
            copyrightNotice = "प्रतिलिपि अधिकार © १९९८, २००६, २०२१, २०२४ Biblica, Inc.",
            sourceUrls = listOf(
                "https://www.biblica.com/",
                "https://open.bible/",
                "https://ebible.org/find/show.php?id=npioncb"
            ),
            license = "Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)",
            licenseCategory = CopyrightLicenseCategory.OPEN_LICENSE,
            usagePermissions = "Biblica, Inc. लाई श्रेय दिई, प्रतिलिपि अधिकार सूचना सुरक्षित राखी, ट्रेडमार्क नियम पालना गर्दै र रूपान्तरित कार्य CC BY-SA 4.0 अन्तर्गत साझा गरेमा लिखित, डिजिटल वा अडियो रूपमा वितरण गर्न सकिन्छ।",
            translationCredits = "Biblica, Inc. अनुवाद टोली (Biblica® Open Nepali Contemporary Version™) • ६६ पुस्तकहरू"
        ),
        BibleSourceAttribution(
            versionId = "KJV",
            title = "KJV – King James Version (अंग्रेजी)",
            fullTranslationName = "Authorized King James Version (१७६९ मानक पाठ)",
            ownerPublisher = "सार्वजनिक क्षेत्र (१६११ अधिकृत संस्करण; १७६९ अक्सफोर्ड/क्याम्ब्रिज मानक पाठ)",
            copyrightNotice = "विश्वभर सार्वजनिक क्षेत्र (बेलायतबाहिर कुनै प्रतिलिपि अधिकार दाबी छैन; बेलायतभित्र मुद्रण शाही पेटेन्ट अधीनमा छ)।",
            sourceUrls = listOf(
                "https://ebible.org/find/show.php?id=eng-kjv2006"
            ),
            license = "Public Domain (बेलायतभित्र Crown Patent नियम लागू हुन्छ)",
            licenseCategory = CopyrightLicenseCategory.PUBLIC_DOMAIN,
            usagePermissions = "बेलायतबाहिर विश्वभर रोयल्टी वा अनुमति बिना निःशुल्क प्रतिलिपि, वितरण र प्रयोग गर्न सकिन्छ। बेलायतभित्र व्यावसायिक मुद्रणका लागि अधिकृत अनुमति आवश्यक हुन्छ।",
            translationCredits = "Authorized King James Version अनुवादकहरू (१६११) र बेन्जामिन ब्लेनी (१७६९ मानक संस्करण); डिजिटल पाठ eBible.org बाट • ६६ पुस्तकहरू"
        ),
        BibleSourceAttribution(
            versionId = "KRV",
            title = "KRV – Korean Revised Version (개역한글)",
            fullTranslationName = "성경전서 개역한글판 (कोरियन संशोधित संस्करण, १९६१)",
            ownerPublisher = "Korean Bible Society (대한성서공회, १९६१ मूल प्रकाशक)",
            copyrightNotice = "सार्वजनिक क्षेत्र (१९६१ कोरियन संशोधित संस्करणको प्रतिलिपि अधिकार अवधि २०११–२०१२ मा समाप्त; १९९८ को 개역개정 संस्करण अझै प्रतिलिपि अधिकार सुरक्षित छ)।",
            sourceUrls = listOf(
                "https://www.bskorea.or.kr/",
                "https://ebible.org/"
            ),
            license = "Public Domain (अवधि समाप्त प्रतिलिपि अधिकार – १९६१ 개역한글 पाठ)",
            licenseCategory = CopyrightLicenseCategory.PUBLIC_DOMAIN,
            usagePermissions = "१९६१ को Korean Revised Version (개역한글) पाठ सार्वजनिक क्षेत्रमा भएकाले निःशुल्क पढ्न, प्रतिलिपि गर्न र वितरण गर्न सकिन्छ। १९९८ को NKRV (개역개정) प्रतिलिपि अधिकार सुरक्षित भएकाले यहाँ समावेश छैन।",
            translationCredits = "Korean Bible Society (대한성서공회) १९६१ संशोधित हान्गुल अनुवाद समिति • ६६ पुस्तकहरू"
        )
    )
    override val bibleTranslationCreditItems = listOf(
        CreditEntryItem(
            title = "अनुवाद योगदानकर्ताहरू",
            subtitle = "विद्वान, अनुवादक र सामुदायिक समीक्षकहरू",
            description = "• ULB (नेपाली): Door43 World Missions Community र unfoldingWord® योगदानकर्ताहरू\n• ONCV (नेपाली): Biblica® Open Nepali Contemporary Version™ अनुवाद टोली\n• KJV (अंग्रेजी): Authorized King James Version अनुवादकहरू (१६११) र बेन्जामिन ब्लेनी (१७६९)\n• KRV (कोरियन): Korean Bible Society १९६१ संशोधित हान्गुल अनुवाद समिति"
        ),
        CreditEntryItem(
            title = "बाइबल प्रकाशकहरू",
            subtitle = "प्रकाशन र अभिभावक संस्थाहरू",
            description = "• Door43 World Missions Community / unfoldingWord® (ULB)\n• Biblica, Inc. — अन्तर्राष्ट्रिय बाइबल समाज (ONCV)\n• Oxford र Cambridge १७६९ मानक पाठ (KJV सार्वजनिक क्षेत्र; बेलायतभित्र Crown Patent नियम)\n• Korean Bible Society / 대한성서공회 (१९६१ कोरियन संशोधित संस्करण)",
            links = listOf(
                CreditLinkItem("unfoldingWord / Door43", "https://door43.org/"),
                CreditLinkItem("Biblica, Inc.", "https://www.biblica.com/"),
                CreditLinkItem("Korean Bible Society", "https://www.bskorea.or.kr/")
            )
        ),
        CreditEntryItem(
            title = "बाइबल पाठ स्रोतहरू",
            subtitle = "प्रमाणित अभिलेख र बाह्य लिङ्कहरू",
            description = "• eBible.org डिजिटल बाइबल अभिलेख (npiulb, npioncb, eng-kjv2006)\n• Open.Bible र Door43 खुला-अनुमतिपत्र बाइबल भण्डारहरू\n• बाह्य Bible.com NNRV सन्दर्भ लिङ्क (गृहपृष्ठ सर्टकट मार्फत उपलब्ध)",
            links = listOf(
                CreditLinkItem("eBible.org", "https://ebible.org/"),
                CreditLinkItem("Open.Bible", "https://open.bible/"),
                CreditLinkItem("Bible.com (NNRV)", "https://www.bible.com/bible/1483/GEN.1.NNRV")
            )
        )
    )
    override val audioMediaCreditItems = listOf(
        CreditEntryItem(
            title = "अडियो बाइबल प्रदायकहरू",
            subtitle = "वाचन प्रणाली र पृष्ठभूमि अडियो सेवा",
            description = "पद-पद अडियो बाइबल वाचन एन्ड्रोइड प्रणालीको Text-to-Speech (TTS) इन्जिन र एकीकृत पृष्ठभूमि मिडिया नियन्त्रणद्वारा सञ्चालित छ।"
        ),
        CreditEntryItem(
            title = "वाचक र आवाज योगदानकर्ताहरू",
            subtitle = "उपकरण र प्रणाली आवाज प्याकहरू",
            description = "वाचन आवाजहरू तपाईंको एन्ड्रोइड उपकरणमा स्थापित अंग्रेजी, नेपाली (ne-NP) र कोरियन (ko-KR) Text-to-Speech आवाज प्याकहरूबाट प्रदान गरिन्छन्।"
        ),
        CreditEntryItem(
            title = "छवि र मिडिया स्रोतहरू",
            subtitle = "समेटिएका कलाकृति, आइकन र पाना पल्टाउने ध्वनि",
            description = "• अफलाइन पठनका लागि समेटिएका छालाको पवित्र बाइबल आवरण चित्र, दैनिक पद ब्यानर र एप आइकन कलाकृति\n• पाना पल्टाउने १० वास्तविक ध्वनि नमूनाहरू (SoundPool अडियो)\n• Google Material Symbols / Material Icons बाट भेक्टर आइकनहरू",
            links = listOf(
                CreditLinkItem("Material Symbols", "https://fonts.google.com/icons")
            )
        )
    )
    override val developerTechCreditItems = listOf(
        CreditEntryItem(
            title = "एप विकासकर्ता जानकारी",
            subtitle = "पवित्र बाइबल — अंग्रेजी • नेपाली (ULB र ONCV) • कोरियन",
            description = "दोहोरो-पृष्ठ पठन, अडियो बाइबल, बुकमार्क, हाइलाइट, अध्ययन नोट, खोज र लिक्विड ग्लास तथा क्लासिक थिमहरूसहितको अफलाइन बहुभाषिक पवित्र बाइबल अध्ययन एप।"
        ),
        CreditEntryItem(
            title = "तेस्रो-पक्ष पुस्तकालयहरू",
            subtitle = "मुख्य एन्ड्रोइड, Jetpack र नेटवर्किङ प्रविधि",
            description = "• Kotlin र Kotlin Coroutines / Flow\n• Jetpack Compose, Material Design 3, Navigation Compose र AndroidX Lifecycle\n• AndroidX Room SQLite भण्डारण र Jetpack DataStore Preferences\n• Retrofit, OkHttp र Moshi JSON\n• समेटिएका फन्टहरू: Playfair Display, Crimson Pro, Lora, Merriweather, Inter, Plus Jakarta Sans, Noto Serif Devanagari र Noto Serif KR",
            links = listOf(
                CreditLinkItem("Jetpack Compose", "https://developer.android.com/jetpack/compose"),
                CreditLinkItem("Google Fonts", "https://fonts.google.com/")
            )
        ),
        CreditEntryItem(
            title = "खुला-स्रोत सफ्टवेयर श्रेय",
            subtitle = "समुदाय अनुमतिपत्र र आभार",
            description = "• Android Open Source Project (AOSP) र AndroidX Jetpack पुस्तकालयहरू — Apache License 2.0\n• खुला-अनुमतिपत्र नेपाली बाइबल संस्करणहरूका लागि Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)\n• स्वचालित परीक्षणका लागि JUnit4 र Robolectric",
            links = listOf(
                CreditLinkItem("Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0"),
                CreditLinkItem("CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/"),
                CreditLinkItem("Robolectric", "https://robolectric.org/")
            )
        )
    )

    override val themeStudioTitle = "लिक्विड ग्लास र थिम स्टुडियो"
    override val themeStudioSubtitle = "लागू गर्नुअघि लाइभ पूर्वावलोकन हेर्नुहोस्"
    override val themePresetsHeading: (Int) -> String = { count -> "थिम शैलीहरू (${NepaliFormatUtils.formatNumber(count, SystemLanguage.NEPALI)})" }
    override val activePresetLabel: (String) -> String = { name -> "सक्रिय: $name" }
    override val liquidGlassOpticsHeading = "लिक्विड ग्लास अप्टिक्स"
    override val hardwareBlurSub = "हार्डवेयर-त्वरित फ्रस्टेड ब्लर सक्रिय छ"
    override val compatFrostSub = "कम्प्याटिबिलिटी पारदर्शी फ्रस्ट मोड"
    override val glassOpacityLabel = "ग्लास पारदर्शिता"
    override val glassBlurLabel = "ग्लास ब्लर तीव्रता"
    override val colorsHeading = "रङहरू"
    override val colorsSub = "पृष्ठभूमि, आकर्षण र अक्षर रङ अनुकूलन गर्नुहोस्"
    override val backgroundColorLabel = "पृष्ठभूमि रङ"
    override val accentColorLabel = "मुख्य रङ"
    override val textColorLabel = "अक्षर रङ"
    override val autoContrastButton = "स्वचालित कन्ट्रास्ट"
    override val controlsNavHeading = "बटन र नेभिगेसन शैली"
    override val controlsNavSub = "बटन आकार र तल्लो पट्टीको स्वरूप छान्नुहोस्"
    override val buttonStyleLabel = "बटन शैली"
    override val navBarAppearanceLabel = "नेभिगेसन पट्टी स्वरूप"
    override val readingPageTypographyHeading = "बाइबल पठन पाना र अक्षर शैली"
    override val readingPageTypographySub = "पठन कागज पृष्ठभूमि, अक्षर शैली र साइज मिलाउनुहोस्"
    override val readingPageBgLabel = "पठन पानाको पृष्ठभूमि"
    override val scriptureFontStyleLabel = "शास्त्र अक्षर शैली"
    override val livePreviewPrefix: (String) -> String = { name -> "लाइभ पूर्वावलोकन • $name" }
    override val livePreviewReadButton = "पढ्नुहोस्"
    override val applyThemeButton = "थिम लागू गर्नुहोस्"

    override val audioActiveDesc = "अडियो बाइबल सक्रिय छ"
    override val audioPrevDesc = "अघिल्लो पद"
    override val audioNextDesc = "अर्को पद"
    override val audioPlayDesc = "अडियो बजाउनुहोस्"
    override val audioPauseDesc = "अडियो रोक्नुहोस्"
    override val audioStopDesc = "अडियो बन्द गर्नुहोस्"
    override val audioNotifChannelName = "अडियो बाइबल वाचन"
    override val audioNotifChannelDesc = "पृष्ठभूमिमा पवित्र बाइबल अडियो वाचन नियन्त्रणहरू"
    override val audioNotifPrevious = "अघिल्लो"
    override val audioNotifNext = "अर्को"
    override val audioNotifPlay = "बजाउनुहोस्"
    override val audioNotifPause = "रोक्नुहोस्"
    override val audioNotifStop = "बन्द"
    override val ttsNepaliMissingNotice = "यो उपकरणमा नेपाली TTS आवाज प्याक भेटिएन; शुद्ध नेपाली उच्चारणका लागि एन्ड्रोइड TTS सेटिङबाट नेपाली आवाज स्थापना गर्नुहोस्।"
    override val ttsKoreanMissingNotice = "यो उपकरणमा कोरियन TTS आवाज प्याक भेटिएन; कोरियन उच्चारणका लागि एन्ड्रोइड TTS सेटिङबाट कोरियन आवाज स्थापना गर्नुहोस्।"
    override val toastVerseCopied = "पद क्लिपबोर्डमा प्रतिलिपि गरियो"
    override val toastWhatsappNotFound = "ह्वाट्सएप फेला परेन; सेयर मेनु खुल्दैछ"
    override val toastFacebookCopied = "पद प्रतिलिपि भयो! फेसबुकको लागि सेयर मेनु खुल्दैछ"
    override val shareChooserTitle = "पवित्र शास्त्र सेयर गर्नुहोस्"
    override val shareAppBrand = "पवित्र बाइबल"

    override val bibleLanguageName: (BibleLanguage) -> String = { lang ->
        when (lang) {
            BibleLanguage.ENGLISH -> "अंग्रेजी"
            BibleLanguage.NEPALI -> "नेपाली (ULB)"
            BibleLanguage.ONCV -> "नेपाली (ONCV)"
            BibleLanguage.KOREAN -> "कोरियन"
        }
    }
    override val themePresetName: (ThemePreset) -> String = { preset ->
        when (preset) {
            ThemePreset.IOS_LIQUID_GLASS -> "iOS लिक्विड ग्लास"
            ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS -> "क्रिस्टल क्लियर लिक्विड ग्लास"
            ThemePreset.CRYSTAL_CLEAR_DARK_GLASS -> "क्रिस्टल क्लियर डार्क ग्लास"
            ThemePreset.TRANSPARENT_GLASS -> "पारदर्शी ग्लास"
            ThemePreset.DARK_GLASS -> "डार्क ग्लास"
            ThemePreset.LIGHT_GLASS -> "लाइट ग्लास"
            ThemePreset.AMOLED_BLACK -> "AMOLED कालो"
            ThemePreset.CLASSIC_BIBLE -> "क्लासिक बाइबल"
            ThemePreset.ELEGANT_BLUE -> "सुरुचिपूर्ण नीलो"
            ThemePreset.NATURE_GREEN -> "प्रकृति हरियो"
            ThemePreset.GOLDEN_PREMIUM -> "सुनौलो प्रिमियम"
        }
    }
    override val themePresetSubtitle: (ThemePreset) -> String = { preset ->
        when (preset) {
            ThemePreset.IOS_LIQUID_GLASS -> "फ्रस्टेड क्रिस्टल पारदर्शिता र चम्किलो गहिराइ"
            ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS -> "अत्यधिक पारदर्शी उज्यालो ग्लास र प्रिज्म चमक"
            ThemePreset.CRYSTAL_CLEAR_DARK_GLASS -> "नीलमणि चमक सहितको गाढा क्रिस्टल ग्लास"
            ThemePreset.TRANSPARENT_GLASS -> "स्पष्ट क्रिस्टल तह र चम्किलो किनाराहरू"
            ThemePreset.DARK_GLASS -> "गाढा अब्सिडियन फ्रस्ट र रात्रि चमक"
            ThemePreset.LIGHT_GLASS -> "नरम सेतो आकाशमा हल्का मोती फ्रस्ट"
            ThemePreset.AMOLED_BLACK -> "शुद्ध कालो OLED कन्ट्रास्ट र हल्का फ्रस्ट"
            ThemePreset.CLASSIC_BIBLE -> "बरगन्डी छाला, प्राचीन सुनौलो र क्रिम कागज"
            ThemePreset.ELEGANT_BLUE -> "शाही नीलमणि गहिराइ र सफा चाँदी फ्रस्ट"
            ThemePreset.NATURE_GREEN -> "शान्त वानस्पतिक हरियो र न्यानो सन्तुलन"
            ThemePreset.GOLDEN_PREMIUM -> "चम्किलो सुनौलो र गाढा कफी मखमली"
        }
    }
    override val buttonStyleName: (ButtonStyleOption) -> String = { opt ->
        when (opt) {
            ButtonStyleOption.LIQUID_GLASS -> "लिक्विड ग्लास"
            ButtonStyleOption.PILL_FILLED -> "भरिपूर्ण पिल"
            ButtonStyleOption.OUTLINED_GLASS -> "क्रिस्टल आउटलाइन"
            ButtonStyleOption.CLASSIC_ELEVATED -> "क्लासिक पुस्तक"
        }
    }
    override val navBarAppearanceName: (NavBarAppearance) -> String = { opt ->
        when (opt) {
            NavBarAppearance.FLOATING_GLASS_DOCK -> "फ्लोटिङ ग्लास डक"
            NavBarAppearance.FROSTED_FULL_BAR -> "फ्रस्टेड ग्लास पट्टी"
            NavBarAppearance.SOLID_MINIMAL -> "क्लासिक ठोस पट्टी"
        }
    }
    override val readingBgName: (ReadingPageBackground) -> String = { bg ->
        when (bg) {
            ReadingPageBackground.MATCH_THEME -> "थिम अनुसार"
            ReadingPageBackground.FROSTED_GLASS -> "फ्रस्टेड ग्लास"
            ReadingPageBackground.CREAM_PARCHMENT -> "क्रिम कागज"
            ReadingPageBackground.PURE_WHITE -> "चम्किलो सेतो"
            ReadingPageBackground.SEPIA_WARM -> "न्यानो सेपिया"
            ReadingPageBackground.NIGHT_OBSIDIAN -> "रात्रि गाढा"
            ReadingPageBackground.AMOLED_BLACK -> "AMOLED कालो"
        }
    }
    override val readingModeName: (ReadingMode) -> String = { mode ->
        when (mode) {
            ReadingMode.BILINGUAL_SPREAD -> "पुस्तक पाना"
            ReadingMode.BILINGUAL_PARALLEL -> "समानान्तर"
            ReadingMode.SINGLE_PRIMARY -> "एक भाषा"
            ReadingMode.SINGLE_SECONDARY -> "दोस्रो भाषा"
        }
    }
    override val audioLanguageName: (AudioLanguage) -> String = { lang ->
        when (lang) {
            AudioLanguage.BILINGUAL -> "अंग्रेजी + नेपाली"
            AudioLanguage.ENGLISH -> "अंग्रेजी"
            AudioLanguage.NEPALI -> "नेपाली (ULB)"
            AudioLanguage.ONCV -> "नेपाली (ONCV)"
            AudioLanguage.KOREAN -> "कोरियन"
        }
    }
    override val pageSoundName: (PageTurnSoundStyle) -> String = { style ->
        when (style) {
            PageTurnSoundStyle.REALISTIC_BIBLE -> "यथार्थपरक बाइबल पाना"
            PageTurnSoundStyle.SOFT_PAPER -> "नरम कागज पल्टाइ"
            PageTurnSoundStyle.OLD_VINTAGE_BIBLE -> "पुरानो र भिन्टेज बाइबल"
            PageTurnSoundStyle.GENTLE_RUSTLE -> "हल्का कागज सरसराहट"
            PageTurnSoundStyle.CLASSIC_HARDCOVER -> "क्लासिक हार्डकभर पुस्तक"
            PageTurnSoundStyle.FAST_FLIP -> "छिटो पाना पल्टाइ"
            PageTurnSoundStyle.SLOW_SOFT -> "बिस्तारै र नरम पल्टाइ"
            PageTurnSoundStyle.ANTIQUE_PARCHMENT -> "बाक्लो प्राचीन चर्मपत्र"
            PageTurnSoundStyle.NATURAL_MOVEMENT -> "सूक्ष्म कागज चाल"
            PageTurnSoundStyle.SOFT_DIGITAL -> "नरम डिजिटल पाना"
            PageTurnSoundStyle.RANDOM -> "अनियमित ध्वनि मोड"
            PageTurnSoundStyle.SILENT -> "मौन मोड (आवाज छैन)"
        }
    }
    override val pageSoundDescription: (PageTurnSoundStyle) -> String = { style ->
        when (style) {
            PageTurnSoundStyle.REALISTIC_BIBLE -> "पातलो बाइबल कागजको स्पष्ट मोड र नरम आवाज"
            PageTurnSoundStyle.SOFT_PAPER -> "हल्का क्रिम कागज पल्टाउँदाको कोमल ध्वनि"
            PageTurnSoundStyle.OLD_VINTAGE_BIBLE -> "पुरानो बाइबल पानाको न्यानो र प्राकृतिक बनावट"
            PageTurnSoundStyle.GENTLE_RUSTLE -> "शान्त, हावा जस्तो पानाको सरसराहट"
            PageTurnSoundStyle.CLASSIC_HARDCOVER -> "हल्का तौल सहितको परम्परागत पुस्तक पल्टाइ"
            PageTurnSoundStyle.FAST_FLIP -> "छिटोछिटो पाना पल्टाउँदाको चुस्त आवाज"
            PageTurnSoundStyle.SLOW_SOFT -> "ध्यानमग्न भएर बिस्तारै पाना सार्ने आवाज"
            PageTurnSoundStyle.ANTIQUE_PARCHMENT -> "बाक्लो कागज र गहिरो पाना थिग्राइ"
            PageTurnSoundStyle.NATURAL_MOVEMENT -> "अत्यन्त सूक्ष्म र शान्त कागज स्पर्श"
            PageTurnSoundStyle.SOFT_DIGITAL -> "आधुनिक साटिन जस्तो हल्का पाना आवाज"
            PageTurnSoundStyle.RANDOM -> "हरेक पल्टाइमा स्वाभाविक कागज आवाज बदल्छ"
            PageTurnSoundStyle.SILENT -> "पाना पल्टाउँदा पूर्ण शान्ति (कुनै आवाज छैन)"
        }
    }
    override val bookDisplayName: (BibleBook, BibleLanguage) -> String = { book, lang -> book.nameFor(lang) }
}

object SystemLocaleManager {
    @Volatile
    var currentLanguage: SystemLanguage = SystemLanguage.ENGLISH

    fun strings(language: SystemLanguage = currentLanguage): AppStrings = when (language) {
        SystemLanguage.ENGLISH -> EnglishAppStrings
        SystemLanguage.NEPALI -> NepaliAppStrings
    }
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { EnglishAppStrings }
val LocalSystemLanguage = staticCompositionLocalOf { SystemLanguage.ENGLISH }
