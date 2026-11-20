import {
  AudioLanguage,
  BIBLE_LANGUAGE_META,
  BibleBook,
  BibleLanguage,
  ButtonStyleOption,
  CopyrightLicenseCategory,
  NavBarAppearance,
  PageTurnSoundStyle,
  ReadingMode,
  ReadingPageBackground,
  SystemLanguage,
  THEME_PRESET_SPECS,
  ThemePreset,
  getBookNameForLanguage,
} from '../types/bible';
import { formatNumber, toDevanagariDigits } from '../utils/formatters';

export interface BibleSourceAttribution {
  versionId: string;
  title: string;
  fullTranslationName: string;
  ownerPublisher: string;
  copyrightNotice: string;
  sourceUrls: string[];
  license: string;
  licenseCategory: CopyrightLicenseCategory;
  usagePermissions: string;
  translationCredits: string;
}

export interface CreditLinkItem {
  label: string;
  url: string;
}

export interface CreditEntryItem {
  title: string;
  subtitle: string;
  description: string;
  links?: CreditLinkItem[];
}

export interface AppStrings {
  appTitle: string;
  appSubtitleLine: string;
  navHome: string;
  navBible: string;
  navSearch: string;
  navSaved: string;
  navSettings: string;

  back: string;
  close: string;
  cancel: string;
  save: string;
  delete: string;
  done: string;
  reset: string;
  manage: string;
  download: string;
  clear: string;
  pause: string;
  resume: string;
  loading: string;

  homeThemesDesc: string;
  homeLanguagesDesc: string;
  homeSearchDesc: string;
  homeSettingsDesc: string;
  homeBibleCoverDesc: string;
  heroTitle: string;
  heroNepaliTitle: string;
  heroEditionFormat: (primary: string, secondary: string) => string;
  continueReadingPrefix: (bookName: string, ch: string, v: string) => string;
  openHolyBible: string;
  oldTestament: string;
  oldTestamentSub: string;
  oldTestamentSubtitle: string;
  newTestament: string;
  newTestamentSub: string;
  newTestamentSubtitle: string;
  downloadBibleCardTitle: string;
  downloadBibleCardStatus: (ready: number, total: number, size: string) => string;
  dailyVerseBadge: string;
  shareDailyVerseDesc: string;
  readChapter: string;
  audioBibleCardTitle: string;
  audioBibleListenHint: string;
  audioBibleDesc: string;
  audioBiblePauseDesc: string;
  audioBiblePlayDesc: string;
  quickLanguages: string;
  quickLanguagesSub: string;
  quickSearch: string;
  quickSearchSub: string;
  quickSaved: string;
  quickSavedSub: string;
  nnrvShortcutTitle: string;
  nnrvShortcutSubtitle: string;
  nnrvShortcutMeta: string;
  nnrvShortcutButton: string;
  nnrvOpenBrowserError: string;

  backToHomeDesc: string;
  chooseBookChapterDesc: string;
  selectLanguagesDesc: string;
  pageSoundOnDesc: string;
  pageSoundOffDesc: string;
  audioBibleDescReader: string;
  readerSettingsDesc: string;
  prevChapterDesc: string;
  nextChapterDesc: string;
  readerScrollSwipeHint: (versesCount: number) => string;
  bookmarkedDesc: string;
  hasNoteDesc: string;
  versesCountLabel: (count: number) => string;
  langNotDownloadedTitle: (langName: string, bookName: string) => string;
  langScopeNoticeTitle: (langName: string) => string;
  langNotDownloadedBody: (bookName: string, nativeLang: string) => string;
  langUnavailableBody: (bookName: string, langName: string) => string;
  downloadBookButton: (bookName: string) => string;
  allDownloadsButton: string;
  chapterWord: string;

  closeVerseBarDesc: string;
  notePrefix: (note: string) => string;
  highlightLabel: string;
  listenAction: string;
  bookmarkAction: string;
  bookmarkedAction: string;
  addNoteAction: string;
  editNoteAction: string;
  whatsappAction: string;
  facebookAction: string;
  copyAction: string;
  moreAction: string;
  personalNoteTitle: (ref: string) => string;
  notePlaceholder: string;

  selectBookTitle: string;
  selectVerseTitle: (book: string, chapter: number) => string;
  booksCountSubtitle: string;
  chooseChapterSubtitle: (chapters: number) => string;
  tapVerseOrChapterSubtitle: string;
  closePickerDesc: string;
  searchBookPlaceholder: string;
  filterAll66: string;
  filterOld39: string;
  filterNew27: string;
  chaptersShort: (count: number) => string;
  openChapterFromVerse1: (chapter: number) => string;

  bibleLanguagesTitle: string;
  bibleLanguagesSubtitle: string;
  closeLanguageSelectorDesc: string;
  primaryLeftPageLabel: string;
  compareRightPageLabel: string;
  swapLanguagesDesc: string;
  spreadModeShort: string;
  parallelModeShort: string;
  tabPrimaryPrefix: (lang: string) => string;
  tabComparePrefix: (lang: string) => string;
  booksCountWord: (count: number) => string;
  offlineReadyLabel: string;
  activeLabel: string;
  offlineLabel: string;
  booksDownloadedProgress: (done: number, total: number) => string;
  manageOfflineDownloads: string;

  searchScriptureTitle: string;
  searchPlaceholder: string;
  clearSearchDesc: string;
  filterAllBible: string;
  filterOldShort: string;
  filterNewShort: string;
  filterAllLanguages: string;
  searchPromptEmpty: string;
  searchNoResults: (query: string) => string;
  searchMatchedCount: (count: number) => string;

  studyLibraryTitle: string;
  tabBookmarks: (count: number) => string;
  tabHighlights: (count: number) => string;
  tabNotes: (count: number) => string;
  tabHistory: (count: number) => string;
  noBookmarksTitle: string;
  noBookmarksSub: string;
  noHighlightsTitle: string;
  noHighlightsSub: string;
  noNotesTitle: string;
  noNotesSub: string;
  noHistoryTitle: string;
  noHistorySub: string;
  clearHistory: string;
  removeBookmarkDesc: string;
  clearHighlightDesc: string;
  editNoteDesc: string;
  deleteNoteDesc: string;

  offlineDownloadsTitle: string;
  offlineDownloadsSubtitle: string;
  storageUsedLabel: (size: string) => string;
  languageBooksOfflineCount: (done: number, total: number) => string;
  completeBibleOffline: string;
  downloadBibleButton: string;
  pauseAllDesc: string;
  resumeAllDesc: string;
  statusOfflineReady: string;
  statusDownloading: string;
  statusPaused: string;
  statusPartial: string;
  statusNotDownloaded: string;
  booksSlashCount: (done: number, total: number) => string;
  downloadRemaining: string;
  downloadAll: string;
  readOffline: string;
  deletePackDesc: (langName: string) => string;
  individualBooksHeader: (count: number) => string;
  hideBooksDesc: string;
  showBooksDesc: string;
  bookOfflineSize: (size: string) => string;
  bookDownloading: string;
  bookQueued: string;
  bookPaused: string;
  bookNotDownloaded: string;
  deletePackDialogTitle: (langName: string) => string;
  deletePackDialogBody: (langName: string) => string;
  keepButton: string;

  settingsTitle: string;
  systemLanguageCardTitle: string;
  systemLanguageSubtitle: string;
  systemLanguageHint: string;
  themesCardTitle: string;
  themeSummaryStats: (opacityPct: number, blurDp: number, fontLabel: string) => string;
  customizeThemeButton: string;
  brightnessCardTitle: string;
  nightDarkModeLabel: string;
  nightDarkModeOnSub: string;
  nightDarkModeOffSub: string;
  languagesCardTitle: string;
  activePairLabel: (p: string, s: string) => string;
  offlineStorageUsedLabel: (size: string) => string;
  languagesButton: string;
  downloadsButton: string;
  typographyCardTitle: string;
  readingModeLabel: string;
  singleLanguageOnly: (langName: string) => string;
  scriptureFontSizeLabel: string;
  pageSoundCardTitle: string;
  pageSoundToggleTitle: string;
  pageSoundMuted: string;
  pageSoundSilentMode: string;
  pageSoundActiveStatus: (styleName: string, volumePct: number) => string;
  randomSoundModeTitle: string;
  randomSoundModeSub: string;
  pageSoundVolumeTitle: string;
  choosePageSoundTitle: string;
  choosePageSoundSub: string;
  selectedDesc: string;
  previewButton: string;
  testForwardButton: string;
  testBackwardButton: string;
  audioNarrationCardTitle: string;
  narrationLanguageLabel: string;
  speechSpeedLabel: string;
  deviceVoicesStatus: (enReady: boolean, neReady: boolean, koReady: boolean) => string;
  voiceReady: string;
  voiceNotInstalled: string;
  voiceInstallTts: string;
  refreshSpeechVoices: string;
  voicesRefreshedToast: string;
  licensesCardTitle: string;
  authenticEditionsHeading: string;
  versionNameAbbrevLabel: string;
  fullTranslationNameLabel: string;
  copyrightOwnerLabel: string;
  copyrightNoticeLabel: string;
  publisherCreditLabel: string;
  licenseLabel: string;
  usagePermissionsLabel: string;
  sourceLabel: string;
  copyrightNotVerifiedYet: string;
  copyrightCategoryLabel: (category: CopyrightLicenseCategory) => string;
  openSourceLinkError: string;
  expandSectionDesc: string;
  collapseSectionDesc: string;
  subsectionBibleCopyrightTitle: string;
  subsectionBibleCopyrightSubtitle: string;
  subsectionBibleTranslationCreditsTitle: string;
  subsectionBibleTranslationCreditsSubtitle: string;
  subsectionAudioMediaCreditsTitle: string;
  subsectionAudioMediaCreditsSubtitle: string;
  subsectionDeveloperTechCreditsTitle: string;
  subsectionDeveloperTechCreditsSubtitle: string;
  bibleSourceAttributions: BibleSourceAttribution[];
  bibleTranslationCreditItems: CreditEntryItem[];
  audioMediaCreditItems: CreditEntryItem[];
  developerTechCreditItems: CreditEntryItem[];

  themeStudioTitle: string;
  themeStudioSubtitle: string;
  themePresetsHeading: (count: number) => string;
  activePresetLabel: (name: string) => string;
  liquidGlassOpticsHeading: string;
  hardwareBlurSub: string;
  glassOpacityLabel: string;
  glassBlurLabel: string;
  colorsHeading: string;
  colorsSub: string;
  backgroundColorLabel: string;
  accentColorLabel: string;
  textColorLabel: string;
  autoContrastButton: string;
  controlsNavHeading: string;
  controlsNavSub: string;
  buttonStyleLabel: string;
  navBarAppearanceLabel: string;
  readingPageTypographyHeading: string;
  readingPageTypographySub: string;
  readingPageBgLabel: string;
  scriptureFontStyleLabel: string;
  livePreviewPrefix: (presetName: string) => string;
  livePreviewReadButton: string;
  applyThemeButton: string;

  audioActiveDesc: string;
  audioPrevDesc: string;
  audioNextDesc: string;
  audioPlayDesc: string;
  audioPauseDesc: string;
  audioStopDesc: string;
  ttsNepaliMissingNotice: string;
  ttsKoreanMissingNotice: string;
  toastVerseCopied: string;
  toastFacebookCopied: string;
  shareAppBrand: string;

  bibleLanguageName: (lang: BibleLanguage) => string;
  themePresetName: (preset: ThemePreset) => string;
  themePresetSubtitle: (preset: ThemePreset) => string;
  buttonStyleName: (opt: ButtonStyleOption) => string;
  navBarAppearanceName: (opt: NavBarAppearance) => string;
  readingBgName: (bg: ReadingPageBackground) => string;
  readingModeName: (mode: ReadingMode) => string;
  audioLanguageName: (lang: AudioLanguage) => string;
  pageSoundName: (style: PageTurnSoundStyle) => string;
  pageSoundDescription: (style: PageTurnSoundStyle) => string;
  bookDisplayName: (book: BibleBook, lang: BibleLanguage) => string;
}

export const EnglishAppStrings: AppStrings = {
  appTitle: 'HOLY BIBLE',
  appSubtitleLine: 'English • Nepali (ULB & ONCV) • Korean',
  navHome: 'Home',
  navBible: 'Bible',
  navSearch: 'Search',
  navSaved: 'Saved',
  navSettings: 'Settings',

  back: 'Back',
  close: 'Close',
  cancel: 'Cancel',
  save: 'Save',
  delete: 'Delete',
  done: 'Done',
  reset: 'Reset',
  manage: 'Manage',
  download: 'Download',
  clear: 'Clear',
  pause: 'Pause',
  resume: 'Resume',
  loading: 'Loading...',

  homeThemesDesc: 'Liquid Glass Themes',
  homeLanguagesDesc: 'Bible Languages',
  homeSearchDesc: 'Search Bible',
  homeSettingsDesc: 'Settings',
  homeBibleCoverDesc: 'Holy Bible Cover',
  heroTitle: 'HOLY BIBLE',
  heroNepaliTitle: 'पवित्र बाइबल',
  heroEditionFormat: (p, s) => `${p} & ${s} Flip Book Edition`,
  continueReadingPrefix: (book, ch, v) => `Continue Reading • ${book} ${ch}:${v}`,
  openHolyBible: 'Open Holy Bible',
  oldTestament: 'Old Testament',
  oldTestamentSub: 'पुरानो नियम',
  oldTestamentSubtitle: '39 Books • Genesis – Malachi',
  newTestament: 'New Testament',
  newTestamentSub: 'नयाँ नियम',
  newTestamentSubtitle: '27 Books • Matthew – Revelation',
  downloadBibleCardTitle: 'Download Bible • Offline Packs',
  downloadBibleCardStatus: (ready, total, size) => `${ready}/${total} languages ready • ${size} stored`,
  dailyVerseBadge: 'DAILY VERSE • आजको पद',
  shareDailyVerseDesc: 'Share daily verse',
  readChapter: 'Read Chapter',
  audioBibleCardTitle: 'Audio Bible • अडियो बाइबल',
  audioBibleListenHint: 'Listen in English, Nepali & Korean',
  audioBibleDesc: 'Audio Bible',
  audioBiblePauseDesc: 'Pause Audio Bible',
  audioBiblePlayDesc: 'Play Audio Bible',
  quickLanguages: 'Languages',
  quickLanguagesSub: '4 Translations',
  quickSearch: 'Search',
  quickSearchSub: 'All Offline',
  quickSaved: 'Saved',
  quickSavedSub: 'Notes & Marks',
  nnrvShortcutTitle: 'NNRV Nepali Bible',
  nnrvShortcutSubtitle: 'Read NNRV Bible Online',
  nnrvShortcutMeta: 'Bible.com • Nepali New Revised Version • https://www.bible.com/',
  nnrvShortcutButton: 'Read NNRV on Bible.com',
  nnrvOpenBrowserError: 'Unable to open browser for Bible.com',

  backToHomeDesc: 'Back to Home',
  chooseBookChapterDesc: 'Choose Book and Chapter',
  selectLanguagesDesc: 'Select Bible Languages',
  pageSoundOnDesc: 'Page turn sounds ON',
  pageSoundOffDesc: 'Page turn sounds OFF',
  audioBibleDescReader: 'Audio Bible',
  readerSettingsDesc: 'Reader Settings',
  prevChapterDesc: 'Previous chapter',
  nextChapterDesc: 'Next chapter',
  readerScrollSwipeHint: (n) => `Scroll chapter • Swipe to flip (${n} verses)`,
  bookmarkedDesc: 'Bookmarked',
  hasNoteDesc: 'Has personal note',
  versesCountLabel: (n) => `${n} verses`,
  langNotDownloadedTitle: (lang, book) => `${lang} (${book}) is not downloaded yet`,
  langScopeNoticeTitle: (lang) => `${lang} scope notice`,
  langNotDownloadedBody: (book, native) => `Download ${book} in ${native} for 100% offline reading.`,
  langUnavailableBody: (book, lang) => `${book} is unavailable in ${lang}.`,
  downloadBookButton: (book) => `Download ${book}`,
  allDownloadsButton: 'All Downloads',
  chapterWord: 'Chapter',

  closeVerseBarDesc: 'Close verse bar',
  notePrefix: (note) => `Note: ${note}`,
  highlightLabel: 'Highlight:',
  listenAction: 'Listen',
  bookmarkAction: 'Bookmark',
  bookmarkedAction: 'Bookmarked',
  addNoteAction: 'Add Note',
  editNoteAction: 'Edit Note',
  whatsappAction: 'WhatsApp',
  facebookAction: 'Facebook',
  copyAction: 'Copy',
  moreAction: 'More',
  personalNoteTitle: (ref) => `Personal Note • ${ref}`,
  notePlaceholder: 'Write your reflection, prayer, or study note...',

  selectBookTitle: 'Select Book • पुस्तक छान्नुहोस्',
  selectVerseTitle: (book, ch) => `${book} ${ch} • Select Verse`,
  booksCountSubtitle: '66 Books of the Holy Bible',
  chooseChapterSubtitle: (ch) => `Choose Chapter (${ch})`,
  tapVerseOrChapterSubtitle: 'Tap a verse or read entire chapter',
  closePickerDesc: 'Close picker',
  searchBookPlaceholder: 'Search book in any language...',
  filterAll66: 'All (66)',
  filterOld39: 'Old Test. (39)',
  filterNew27: 'New Test. (27)',
  chaptersShort: (ch) => `${ch} ch`,
  openChapterFromVerse1: (ch) => `Open Chapter ${ch} from Verse 1`,

  bibleLanguagesTitle: 'Bible Languages & Compare',
  bibleLanguagesSubtitle: 'Switch or compare any two languages side-by-side',
  closeLanguageSelectorDesc: 'Close language selector',
  primaryLeftPageLabel: 'PRIMARY (LEFT PAGE)',
  compareRightPageLabel: 'COMPARE (RIGHT PAGE)',
  swapLanguagesDesc: 'Swap primary and secondary languages',
  spreadModeShort: 'Spread',
  parallelModeShort: 'Parallel',
  tabPrimaryPrefix: (l) => `Primary: ${l}`,
  tabComparePrefix: (l) => `Compare: ${l}`,
  booksCountWord: (n) => `${n} Books`,
  offlineReadyLabel: 'Offline Ready',
  activeLabel: 'Active',
  offlineLabel: 'Offline',
  booksDownloadedProgress: (done, total) => `${done} of ${total} books downloaded offline`,
  manageOfflineDownloads: 'Manage Offline Downloads',

  searchScriptureTitle: 'Search Scripture • बाइबल खोज्नुहोस्',
  searchPlaceholder: 'Search English, ULB, ONCV, or Korean...',
  clearSearchDesc: 'Clear search',
  filterAllBible: 'All Bible',
  filterOldShort: 'Old Test.',
  filterNewShort: 'New Test.',
  filterAllLanguages: 'All Languages',
  searchPromptEmpty: 'Enter at least 2 characters to search\nall downloaded offline Bible languages',
  searchNoResults: (q) => `No matching verses found in downloaded packs for "${q}"`,
  searchMatchedCount: (n) => `${n} verses matched`,

  studyLibraryTitle: 'Study Library • अध्ययन संग्रह',
  tabBookmarks: (n) => `Bookmarks (${n})`,
  tabHighlights: (n) => `Highlights (${n})`,
  tabNotes: (n) => `Notes (${n})`,
  tabHistory: (n) => `History (${n})`,
  noBookmarksTitle: 'No Bookmarks Yet',
  noBookmarksSub: 'Tap any verse while reading to bookmark it in both English and Nepali.',
  noHighlightsTitle: 'No Highlighted Verses',
  noHighlightsSub: 'Select any verse in the Flip Book and choose Gold, Rose, Sage, or Sky highlight.',
  noNotesTitle: 'No Personal Notes',
  noNotesSub: 'Tap any verse and select Add Note to write reflections or sermon notes.',
  noHistoryTitle: 'No Reading History Yet',
  noHistorySub: 'Chapters you read in the Holy Bible will appear here for quick return.',
  clearHistory: 'Clear History',
  removeBookmarkDesc: 'Remove bookmark',
  clearHighlightDesc: 'Clear highlight',
  editNoteDesc: 'Edit note',
  deleteNoteDesc: 'Delete note',

  offlineDownloadsTitle: 'Offline Bible Downloads',
  offlineDownloadsSubtitle: '100% offline reading, search & chapter navigation',
  storageUsedLabel: (size) => `Browser Storage Used: ${size}`,
  languageBooksOfflineCount: (done, total) => `${done} of ${total} language books offline`,
  completeBibleOffline: 'Complete Bible Offline',
  downloadBibleButton: 'Download Bible',
  pauseAllDesc: 'Pause all',
  resumeAllDesc: 'Resume all',
  statusOfflineReady: 'Offline Ready',
  statusDownloading: 'Downloading',
  statusPaused: 'Paused',
  statusPartial: 'Partial',
  statusNotDownloaded: 'Not Downloaded',
  booksSlashCount: (done, total) => `${done} / ${total} books`,
  downloadRemaining: 'Download Remaining',
  downloadAll: 'Download All',
  readOffline: 'Read Offline',
  deletePackDesc: (l) => `Delete ${l} offline pack`,
  individualBooksHeader: (n) => `Individual Books (${n})`,
  hideBooksDesc: 'Hide books',
  showBooksDesc: 'Show books',
  bookOfflineSize: (s) => `Offline (${s})`,
  bookDownloading: 'Downloading...',
  bookQueued: 'Queued',
  bookPaused: 'Paused',
  bookNotDownloaded: 'Not downloaded',
  deletePackDialogTitle: (l) => `Delete ${l} Offline Pack?`,
  deletePackDialogBody: (l) =>
    `This removes downloaded ${l} chapters from your offline storage. You can re-download it anytime.`,
  keepButton: 'Keep',

  settingsTitle: 'Settings • सेटिङहरू',
  systemLanguageCardTitle: 'System Language',
  systemLanguageSubtitle: 'Choose app interface language',
  systemLanguageHint: 'Translates menus, buttons, settings, notifications, and system messages.',
  themesCardTitle: 'Liquid Glass & Themes',
  themeSummaryStats: (op, blur, font) => `${op}% opacity • ${blur}dp blur • ${font}`,
  customizeThemeButton: 'Customize Theme & Live Preview',
  brightnessCardTitle: 'Reading Theme Brightness',
  nightDarkModeLabel: 'Night Study Dark Mode',
  nightDarkModeOnSub: 'Dark Glass & low-glare reading',
  nightDarkModeOffSub: 'Bright Liquid Glass daylight mode',
  languagesCardTitle: 'Languages & Offline Packs',
  activePairLabel: (p, s) => `Active Pair: ${p} ↔ ${s}`,
  offlineStorageUsedLabel: (s) => `Offline storage used: ${s}`,
  languagesButton: 'Languages',
  downloadsButton: 'Downloads',
  typographyCardTitle: 'Book Layout & Typography',
  readingModeLabel: 'Reading Mode',
  singleLanguageOnly: (l) => `${l} Only`,
  scriptureFontSizeLabel: 'Scripture Font Size',
  pageSoundCardTitle: 'Page-Turning Sound',
  pageSoundToggleTitle: 'Page-Turning Sound Audio',
  pageSoundMuted: 'Muted (Sound OFF)',
  pageSoundSilentMode: 'Silent Mode (No Sound)',
  pageSoundActiveStatus: (name, vol) => `${name} • ${vol}% volume`,
  randomSoundModeTitle: 'Random Sound Mode',
  randomSoundModeSub: 'Varies realistic paper sounds on every flip',
  pageSoundVolumeTitle: 'Page Sound Volume',
  choosePageSoundTitle: 'Choose Page-Turning Sound',
  choosePageSoundSub: 'Tap Preview to listen before selecting your favorite sound',
  selectedDesc: 'Selected',
  previewButton: 'Preview',
  testForwardButton: 'Test Forward',
  testBackwardButton: 'Test Backward',
  audioNarrationCardTitle: 'Audio Bible Narration',
  narrationLanguageLabel: 'Narration Language',
  speechSpeedLabel: 'Speech Speed',
  deviceVoicesStatus: (en, ne, ko) =>
    `Browser Voices — English: ${en ? 'Ready' : 'Not installed'} • Nepali: ${ne ? 'Ready' : 'Not detected'} • Korean: ${ko ? 'Ready' : 'Not detected'}`,
  voiceReady: 'Ready',
  voiceNotInstalled: 'Not installed',
  voiceInstallTts: 'Install OS Voice',
  refreshSpeechVoices: 'Refresh Speech Voices',
  voicesRefreshedToast: 'Refreshed available system speech voices',
  licensesCardTitle: 'Copyright & Credits',
  authenticEditionsHeading: 'About • Bible licenses, translation sources, audio/media & developer credits',
  versionNameAbbrevLabel: 'Bible Version & Abbreviation',
  fullTranslationNameLabel: 'Full Translation Name',
  copyrightOwnerLabel: 'Copyright Owner / Publisher',
  copyrightNoticeLabel: 'Copyright Notice',
  publisherCreditLabel: 'Translation Credits',
  licenseLabel: 'License Type',
  usagePermissionsLabel: 'Usage & Distribution Permissions',
  sourceLabel: 'Official Source Website',
  copyrightNotVerifiedYet: 'Copyright information not verified yet.',
  copyrightCategoryLabel: (category) => {
    switch (category) {
      case CopyrightLicenseCategory.OPEN_LICENSE:
        return 'Open License';
      case CopyrightLicenseCategory.PUBLIC_DOMAIN:
        return 'Public Domain';
      case CopyrightLicenseCategory.COPYRIGHTED:
        return 'Copyrighted';
      case CopyrightLicenseCategory.UNVERIFIED:
        return 'Not Verified';
    }
  },
  openSourceLinkError: 'Unable to open browser for source website',
  expandSectionDesc: 'Expand section',
  collapseSectionDesc: 'Collapse section',
  subsectionBibleCopyrightTitle: 'A. Bible Translation Copyright & Sources',
  subsectionBibleCopyrightSubtitle: 'ULB, ONCV, KJV & KRV licenses, owners & official links',
  subsectionBibleTranslationCreditsTitle: 'B. Bible Translation Credits',
  subsectionBibleTranslationCreditsSubtitle: 'Translation contributors, Bible publishers & text sources',
  subsectionAudioMediaCreditsTitle: 'C. Audio & Media Credits',
  subsectionAudioMediaCreditsSubtitle: 'Audio Bible providers, voice contributors & media sources',
  subsectionDeveloperTechCreditsTitle: 'D. Developer & Technology Credits',
  subsectionDeveloperTechCreditsSubtitle: 'App developer info, third-party libraries & open-source credits',
  bibleSourceAttributions: [
    {
      versionId: 'ULB',
      title: 'ULB – Unlocked Literal Bible (Nepali)',
      fullTranslationName: 'Nepali Unlocked Literal Bible (पवित्र बाइबल, npiulb)',
      ownerPublisher: 'Door43 World Missions Community (unfoldingWord®)',
      copyrightNotice: 'Copyright © 2019 by Door43 World Missions Community',
      sourceUrls: [
        'https://door43.org/',
        'https://ebible.org/find/show.php?id=npiulb',
        'https://creativecommons.org/licenses/by-sa/4.0/',
      ],
      license: 'Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)',
      licenseCategory: CopyrightLicenseCategory.OPEN_LICENSE,
      usagePermissions:
        'Free to share, copy, distribute, display, and adapt (including offline distribution) with attribution to Door43 World Missions Community / unfoldingWord®, indication of changes, and ShareAlike under CC BY-SA 4.0.',
      translationCredits:
        'Translated and contributed by Door43 World Missions Community and unfoldingWord® Contributors • 66 Books (OT & NT)',
    },
    {
      versionId: 'ONCV',
      title: 'ONCV – Old Nepali Contemporary Version',
      fullTranslationName:
        'Biblica® Open Nepali Contemporary Version™ (नेपाली समकालीन सर्वसुलभ संस्करण, npioncb)',
      ownerPublisher: 'Biblica, Inc.',
      copyrightNotice: 'Copyright © 1998, 2006, 2021, 2024 by Biblica, Inc.',
      sourceUrls: [
        'https://www.biblica.com/',
        'https://open.bible/',
        'https://ebible.org/find/show.php?id=npioncb',
      ],
      license: 'Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)',
      licenseCategory: CopyrightLicenseCategory.OPEN_LICENSE,
      usagePermissions:
        'Free to copy, share, distribute, and adapt in written, electronic, or audio form under CC BY-SA 4.0, provided attribution is given to Biblica, Inc., copyright notice is preserved, Biblica® trademark rules are respected, and derivative works use CC BY-SA 4.0.',
      translationCredits:
        'Biblica, Inc. Translation Team (Biblica® Open Nepali Contemporary Version™) • 66 Books (OT & NT)',
    },
    {
      versionId: 'KJV',
      title: 'KJV – King James Version (English)',
      fullTranslationName: 'Authorized King James Version (1769 Standard Text)',
      ownerPublisher:
        'Public Domain (1611 Authorized Version; 1769 Oxford/Cambridge Standard Text)',
      copyrightNotice:
        'Public Domain worldwide (Outside the United Kingdom, no copyright claims; inside the UK, printing is subject to perpetual Crown patent).',
      sourceUrls: ['https://ebible.org/find/show.php?id=eng-kjv2006'],
      license: 'Public Domain (UK Crown Patent applies inside the United Kingdom)',
      licenseCategory: CopyrightLicenseCategory.PUBLIC_DOMAIN,
      usagePermissions:
        'Freely copyable, distributable, and embeddable worldwide outside the UK without royalties or permission. Commercial printing inside the United Kingdom requires authorization from Crown patentees.',
      translationCredits:
        'Authorized King James Version Translators (1611) & Benjamin Blayney (1769 Standard Edition); digital text via eBible.org • 66 Books (OT & NT)',
    },
    {
      versionId: 'KRV',
      title: 'KRV – Korean Revised Version (개역한글)',
      fullTranslationName: '성경전서 개역한글판 (Korean Revised Version, 1961 Edition)',
      ownerPublisher: 'Korean Bible Society (대한성서공회, 1961 Original Publisher)',
      copyrightNotice:
        'Public Domain (1961 Korean Revised Version copyright term expired around 2011–2012; distinct from the copyrighted 1998 New Korean Revised Version / 개역개정).',
      sourceUrls: ['https://www.bskorea.or.kr/', 'https://ebible.org/'],
      license: 'Public Domain (Expired Copyright – 1961 개역한글 Text)',
      licenseCategory: CopyrightLicenseCategory.PUBLIC_DOMAIN,
      usagePermissions:
        'The 1961 Korean Revised Version (개역한글) text is in the public domain and may be freely read, copied, and distributed. Note: The 1998 New Korean Revised Version (개역개정) remains copyrighted and is not used here.',
      translationCredits:
        'Korean Bible Society (대한성서공회) 1961 Revised Hangul Translation Committee • 66 Books (OT & NT)',
    },
  ],
  bibleTranslationCreditItems: [
    {
      title: 'Translation Contributors',
      subtitle: 'Scholars, translators & community reviewers',
      description:
        '• ULB (Nepali): Door43 World Missions Community & unfoldingWord® Contributors\n• ONCV (Nepali): Biblica® Open Nepali Contemporary Version™ Translation Team\n• KJV (English): Authorized King James Version Translators (1611) & Benjamin Blayney (1769)\n• KRV (Korean): Korean Bible Society 1961 Revised Hangul Translation Committee',
    },
    {
      title: 'Bible Publishers',
      subtitle: 'Stewards & publishing organizations',
      description:
        '• Door43 World Missions Community / unfoldingWord® (ULB)\n• Biblica, Inc. — The International Bible Society (ONCV)\n• Oxford & Cambridge 1769 Standard Text (KJV Public Domain; Crown Patent inside the UK)\n• Korean Bible Society / 대한성서공회 (1961 Korean Revised Version)',
      links: [
        { label: 'unfoldingWord / Door43', url: 'https://door43.org/' },
        { label: 'Biblica, Inc.', url: 'https://www.biblica.com/' },
        { label: 'Korean Bible Society', url: 'https://www.bskorea.or.kr/' },
      ],
    },
    {
      title: 'Bible Text Sources',
      subtitle: 'Verified repositories & external links',
      description:
        '• eBible.org Digital Scripture Archives (npiulb, npioncb, eng-kjv2006)\n• Open.Bible & Door43 Open-Licensed Bible Repositories\n• External Bible.com NNRV Reference Link (provided via Home Page shortcut)',
      links: [
        { label: 'eBible.org', url: 'https://ebible.org/' },
        { label: 'Open.Bible', url: 'https://open.bible/' },
        { label: 'Bible.com (NNRV)', url: 'https://www.bible.com/bible/1483/GEN.1.NNRV' },
      ],
    },
  ],
  audioMediaCreditItems: [
    {
      title: 'Audio Bible Providers',
      subtitle: 'Speech synthesis & chapter sequencing',
      description:
        'Verse-by-verse Audio Bible playback uses the Web Speech API (SpeechSynthesis) with bilingual verse-by-verse sequencing and speed control.',
    },
    {
      title: 'Narrators & Voice Contributors',
      subtitle: 'Operating system & browser voice packs',
      description:
        'Narration voices are provided by the Text-to-Speech voices installed on your OS and browser for English, Nepali (ne-NP), and Korean (ko-KR).',
    },
    {
      title: 'Image & Media Sources',
      subtitle: 'Bundled artwork, icons & page-turning audio',
      description:
        '• Custom leather Holy Bible cover artwork, daily devotional banner & app emblem bundled for offline viewing\n• 10 realistic Bible page-turning audio samples bundled for low-latency Web Audio API playback\n• Crisp vector iconography powered by Lucide Icons',
    },
  ],
  developerTechCreditItems: [
    {
      title: 'App Developer Information',
      subtitle: 'Holy Bible — English • Nepali (ULB & ONCV) • Korean',
      description:
        'Designed and built as an offline-first multilingual Holy Bible study web app featuring 3D dual-page flip-book reading, Audio Bible narration, bookmarks, highlights, study notes, search, and Liquid Glass & Classic themes.',
    },
    {
      title: 'Web Libraries & Fonts',
      subtitle: 'React, TypeScript & Typography stack',
      description:
        '• React 19, TypeScript, Vite & Tailwind CSS\n• IndexedDB + fflate gzip decompression for instant offline storage\n• Bundled Fonts: Playfair Display, Crimson Pro, Lora, Merriweather, Inter, Plus Jakarta Sans, Noto Serif Devanagari & Noto Serif KR',
      links: [
        { label: 'Google Fonts', url: 'https://fonts.google.com/' },
        { label: 'CC BY-SA 4.0', url: 'https://creativecommons.org/licenses/by-sa/4.0/' },
      ],
    },
  ],

  themeStudioTitle: 'Liquid Glass & Theme Studio',
  themeStudioSubtitle: 'Live preview changes before applying',
  themePresetsHeading: (count) => `Theme Presets (${count} Styles)`,
  activePresetLabel: (name) => `Active: ${name}`,
  liquidGlassOpticsHeading: 'Liquid Glass Optics',
  hardwareBlurSub: 'Backdrop filter frosted glass blur active',
  glassOpacityLabel: 'Glass Opacity / Transparency',
  glassBlurLabel: 'Glass Blur Intensity',
  colorsHeading: 'Colors',
  colorsSub: 'Customize backdrop, accent, and text colors',
  backgroundColorLabel: 'Background Color',
  accentColorLabel: 'Accent Color',
  textColorLabel: 'Text Color',
  autoContrastButton: 'Auto Contrast',
  controlsNavHeading: 'Controls & Navigation Style',
  controlsNavSub: 'Customize button geometry and bottom bar appearance',
  buttonStyleLabel: 'Button Style',
  navBarAppearanceLabel: 'Navigation Bar Appearance',
  readingPageTypographyHeading: 'Bible Reading Page & Typography',
  readingPageTypographySub: 'Customize reading paper backdrop, font style & size',
  readingPageBgLabel: 'Reading Page Background',
  scriptureFontStyleLabel: 'Scripture Font Style',
  livePreviewPrefix: (name) => `LIVE PREVIEW • ${name}`,
  livePreviewReadButton: 'Read',
  applyThemeButton: 'Apply Theme',

  audioActiveDesc: 'Audio Bible active',
  audioPrevDesc: 'Previous verse',
  audioNextDesc: 'Next verse',
  audioPlayDesc: 'Play audio',
  audioPauseDesc: 'Pause audio',
  audioStopDesc: 'Stop audio',
  ttsNepaliMissingNotice:
    'Nepali TTS voice not detected in this browser; install a Nepali voice in your OS speech settings for native pronunciation.',
  ttsKoreanMissingNotice:
    'Korean TTS voice not detected in this browser; install a Korean voice in your OS speech settings for native pronunciation.',
  toastVerseCopied: 'Verse copied to clipboard',
  toastFacebookCopied: 'Verse copied! Paste it into Facebook to share',
  shareAppBrand: 'Holy Bible',

  bibleLanguageName: (lang) => BIBLE_LANGUAGE_META[lang].displayName,
  themePresetName: (preset) => THEME_PRESET_SPECS[preset].displayName,
  themePresetSubtitle: (preset) => THEME_PRESET_SPECS[preset].subtitle,
  buttonStyleName: (opt) => {
    switch (opt) {
      case ButtonStyleOption.LIQUID_GLASS:
        return 'Liquid Glass';
      case ButtonStyleOption.PILL_FILLED:
        return 'Filled Pill';
      case ButtonStyleOption.OUTLINED_GLASS:
        return 'Crystal Outline';
      case ButtonStyleOption.CLASSIC_ELEVATED:
        return 'Classic Book';
    }
  },
  navBarAppearanceName: (opt) => {
    switch (opt) {
      case NavBarAppearance.FLOATING_GLASS_DOCK:
        return 'Floating Glass Dock';
      case NavBarAppearance.FROSTED_FULL_BAR:
        return 'Frosted Glass Bar';
      case NavBarAppearance.SOLID_MINIMAL:
        return 'Classic Solid Bar';
    }
  },
  readingBgName: (bg) => {
    switch (bg) {
      case ReadingPageBackground.MATCH_THEME:
        return 'Match Theme';
      case ReadingPageBackground.FROSTED_GLASS:
        return 'Frosted Glass';
      case ReadingPageBackground.CREAM_PARCHMENT:
        return 'Cream Parchment';
      case ReadingPageBackground.PURE_WHITE:
        return 'Crisp White';
      case ReadingPageBackground.SEPIA_WARM:
        return 'Warm Sepia';
      case ReadingPageBackground.NIGHT_OBSIDIAN:
        return 'Night Obsidian';
      case ReadingPageBackground.AMOLED_BLACK:
        return 'AMOLED Black';
    }
  },
  readingModeName: (mode) => {
    switch (mode) {
      case ReadingMode.BILINGUAL_SPREAD:
        return 'Book Spread';
      case ReadingMode.BILINGUAL_PARALLEL:
        return 'Parallel';
      case ReadingMode.SINGLE_PRIMARY:
        return 'Single Primary';
      case ReadingMode.SINGLE_SECONDARY:
        return 'Single Secondary';
    }
  },
  audioLanguageName: (lang) => {
    switch (lang) {
      case AudioLanguage.BILINGUAL:
        return 'EN + NE';
      case AudioLanguage.ENGLISH:
        return 'English';
      case AudioLanguage.NEPALI:
        return 'नेपाली (ULB)';
      case AudioLanguage.ONCV:
        return 'नेपाली (ONCV)';
      case AudioLanguage.KOREAN:
        return '한국어';
    }
  },
  pageSoundName: (style) => {
    switch (style) {
      case PageTurnSoundStyle.REALISTIC_BIBLE:
        return 'Realistic Bible Page';
      case PageTurnSoundStyle.SOFT_PAPER:
        return 'Soft Paper Flip';
      case PageTurnSoundStyle.OLD_VINTAGE_BIBLE:
        return 'Old & Vintage Bible';
      case PageTurnSoundStyle.GENTLE_RUSTLE:
        return 'Gentle Paper Rustling';
      case PageTurnSoundStyle.CLASSIC_HARDCOVER:
        return 'Classic Hardcover Book';
      case PageTurnSoundStyle.FAST_FLIP:
        return 'Fast Page Flip';
      case PageTurnSoundStyle.SLOW_SOFT:
        return 'Slow & Soft Turn';
      case PageTurnSoundStyle.ANTIQUE_PARCHMENT:
        return 'Thick & Antique Parchment';
      case PageTurnSoundStyle.NATURAL_MOVEMENT:
        return 'Subtle Paper Movement';
      case PageTurnSoundStyle.SOFT_DIGITAL:
        return 'Soft Digital Page';
      case PageTurnSoundStyle.RANDOM:
        return 'Random Sound Mode';
      case PageTurnSoundStyle.SILENT:
        return 'Silent Mode (No Sound)';
    }
  },
  pageSoundDescription: (style) => {
    switch (style) {
      case PageTurnSoundStyle.REALISTIC_BIBLE:
        return 'Crisp thin India-paper curl & gentle settle';
      case PageTurnSoundStyle.SOFT_PAPER:
        return 'Gentle brushed cream paper turn';
      case PageTurnSoundStyle.OLD_VINTAGE_BIBLE:
        return 'Warm fibrous aged Bible leaf texture';
      case PageTurnSoundStyle.GENTLE_RUSTLE:
        return 'Airy, peaceful dual-wave leaf rustle';
      case PageTurnSoundStyle.CLASSIC_HARDCOVER:
        return 'Traditional bound book flip with subtle weight';
      case PageTurnSoundStyle.FAST_FLIP:
        return 'Brisk light flick for quick browsing';
      case PageTurnSoundStyle.SLOW_SOFT:
        return 'Unhurried, meditative page glide';
      case PageTurnSoundStyle.ANTIQUE_PARCHMENT:
        return 'Richer parchment grain & heavier page settle';
      case PageTurnSoundStyle.NATURAL_MOVEMENT:
        return 'Whisper-quiet minimal paper touch';
      case PageTurnSoundStyle.SOFT_DIGITAL:
        return 'Feather-light modern satin breeze';
      case PageTurnSoundStyle.RANDOM:
        return 'Automatically varies paper sounds each turn';
      case PageTurnSoundStyle.SILENT:
        return 'Complete silence while flipping pages';
    }
  },
  bookDisplayName: getBookNameForLanguage,
};

export const NepaliAppStrings: AppStrings = {
  appTitle: 'पवित्र बाइबल',
  appSubtitleLine: 'अंग्रेजी • नेपाली (ULB र ONCV) • कोरियन',
  navHome: 'गृहपृष्ठ',
  navBible: 'बाइबल',
  navSearch: 'खोज',
  navSaved: 'सुरक्षित',
  navSettings: 'सेटिङहरू',

  back: 'पछाडि',
  close: 'बन्द गर्नुहोस्',
  cancel: 'रद्द गर्नुहोस्',
  save: 'सुरक्षित गर्नुहोस्',
  delete: 'मेटाउनुहोस्',
  done: 'सम्पन्न',
  reset: 'रिसेट',
  manage: 'व्यवस्थापन',
  download: 'डाउनलोड',
  clear: 'हटाउनुहोस्',
  pause: 'रोक्नुहोस्',
  resume: 'पुनः सुरु',
  loading: 'लोड हुँदैछ...',

  homeThemesDesc: 'लिक्विड ग्लास थिमहरू',
  homeLanguagesDesc: 'बाइबल भाषाहरू',
  homeSearchDesc: 'बाइबल खोज्नुहोस्',
  homeSettingsDesc: 'सेटिङहरू',
  homeBibleCoverDesc: 'पवित्र बाइबल आवरण',
  heroTitle: 'पवित्र बाइबल',
  heroNepaliTitle: 'HOLY BIBLE',
  heroEditionFormat: (p, s) => `${p} र ${s} फ्लिप बुक संस्करण`,
  continueReadingPrefix: (book, ch, v) => `पढ्न जारी राख्नुहोस् • ${book} ${ch}:${v}`,
  openHolyBible: 'पवित्र बाइबल खोल्नुहोस्',
  oldTestament: 'पुरानो नियम',
  oldTestamentSub: 'Old Testament',
  oldTestamentSubtitle: '३९ पुस्तकहरू • उत्पत्ति – मलाकी',
  newTestament: 'नयाँ नियम',
  newTestamentSub: 'New Testament',
  newTestamentSubtitle: '२७ पुस्तकहरू • मत्ती – प्रकाश',
  downloadBibleCardTitle: 'बाइबल डाउनलोड • अफलाइन प्याक',
  downloadBibleCardStatus: (ready, total, size) =>
    `${formatNumber(ready, SystemLanguage.NEPALI)}/${formatNumber(total, SystemLanguage.NEPALI)} भाषा तयार • ${size} भण्डारण`,
  dailyVerseBadge: 'आजको पद • DAILY VERSE',
  shareDailyVerseDesc: 'आजको पद सेयर गर्नुहोस्',
  readChapter: 'अध्याय पढ्नुहोस्',
  audioBibleCardTitle: 'अडियो बाइबल',
  audioBibleListenHint: 'अंग्रेजी, नेपाली र कोरियनमा सुन्नुहोस्',
  audioBibleDesc: 'अडियो बाइबल',
  audioBiblePauseDesc: 'अडियो बाइबल रोक्नुहोस्',
  audioBiblePlayDesc: 'अडियो बाइबल बजाउनुहोस्',
  quickLanguages: 'भाषाहरू',
  quickLanguagesSub: '४ अनुवादहरू',
  quickSearch: 'खोज',
  quickSearchSub: 'सबै अफलाइन',
  quickSaved: 'सुरक्षित',
  quickSavedSub: 'नोट र चिन्ह',
  nnrvShortcutTitle: 'NNRV नेपाली बाइबल',
  nnrvShortcutSubtitle: 'अनलाइन NNRV बाइबल पढ्नुहोस्',
  nnrvShortcutMeta: 'Bible.com • नेपाली नयाँ संशोधित संस्करण • https://www.bible.com/',
  nnrvShortcutButton: 'Bible.com मा NNRV पढ्नुहोस्',
  nnrvOpenBrowserError: 'Bible.com खोल्न ब्राउजर उपलब्ध छैन',

  backToHomeDesc: 'गृहपृष्ठमा फर्कनुहोस्',
  chooseBookChapterDesc: 'पुस्तक र अध्याय छान्नुहोस्',
  selectLanguagesDesc: 'बाइबल भाषाहरू छान्नुहोस्',
  pageSoundOnDesc: 'पाना पल्टाउने आवाज चालु',
  pageSoundOffDesc: 'पाना पल्टाउने आवाज बन्द',
  audioBibleDescReader: 'अडियो बाइबल',
  readerSettingsDesc: 'पठन सेटिङहरू',
  prevChapterDesc: 'अघिल्लो अध्याय',
  nextChapterDesc: 'अर्को अध्याय',
  readerScrollSwipeHint: (n) =>
    `अध्याय स्क्रोल • पल्टाउन स्वाइप (${formatNumber(n, SystemLanguage.NEPALI)} पद)`,
  bookmarkedDesc: 'बुकमार्क गरिएको',
  hasNoteDesc: 'व्यक्तिगत नोट छ',
  versesCountLabel: (n) => `${formatNumber(n, SystemLanguage.NEPALI)} पद`,
  langNotDownloadedTitle: (lang, book) => `${lang} (${book}) अझै डाउनलोड भएको छैन`,
  langScopeNoticeTitle: (lang) => `${lang} उपलब्धता सूचना`,
  langNotDownloadedBody: (book, native) => `१००% अफलाइन पढ्न ${native} मा ${book} डाउनलोड गर्नुहोस्।`,
  langUnavailableBody: (book, lang) => `${book} ${lang} मा उपलब्ध छैन।`,
  downloadBookButton: (book) => `${book} डाउनलोड`,
  allDownloadsButton: 'सबै डाउनलोडहरू',
  chapterWord: 'अध्याय',

  closeVerseBarDesc: 'पद पट्टी बन्द गर्नुहोस्',
  notePrefix: (note) => `नोट: ${note}`,
  highlightLabel: 'हाइलाइट:',
  listenAction: 'सुन्नुहोस्',
  bookmarkAction: 'बुकमार्क',
  bookmarkedAction: 'बुकमार्क गरियो',
  addNoteAction: 'नोट थप्नुहोस्',
  editNoteAction: 'नोट सम्पादन',
  whatsappAction: 'ह्वाट्सएप',
  facebookAction: 'फेसबुक',
  copyAction: 'प्रतिलिपि',
  moreAction: 'थप',
  personalNoteTitle: (ref) => `व्यक्तिगत नोट • ${ref}`,
  notePlaceholder: 'आफ्नो मनन, प्रार्थना वा अध्ययन नोट लेख्नुहोस्...',

  selectBookTitle: 'पुस्तक छान्नुहोस्',
  selectVerseTitle: (book, ch) =>
    `${book} ${formatNumber(ch, SystemLanguage.NEPALI)} • पद छान्नुहोस्`,
  booksCountSubtitle: 'पवित्र बाइबलका ६६ पुस्तकहरू',
  chooseChapterSubtitle: (ch) =>
    `अध्याय छान्नुहोस् (${formatNumber(ch, SystemLanguage.NEPALI)})`,
  tapVerseOrChapterSubtitle: 'पद ट्याप गर्नुहोस् वा पूरा अध्याय पढ्नुहोस्',
  closePickerDesc: 'चयन बन्द गर्नुहोस्',
  searchBookPlaceholder: 'कुनै पनि भाषामा पुस्तक खोज्नुहोस्...',
  filterAll66: 'सबै (६६)',
  filterOld39: 'पुरानो (३९)',
  filterNew27: 'नयाँ (२७)',
  chaptersShort: (ch) => `${formatNumber(ch, SystemLanguage.NEPALI)} अ.`,
  openChapterFromVerse1: (ch) =>
    `अध्याय ${formatNumber(ch, SystemLanguage.NEPALI)} पद १ बाट खोल्नुहोस्`,

  bibleLanguagesTitle: 'बाइबल भाषा र तुलना',
  bibleLanguagesSubtitle: 'कुनै दुई भाषा सँगसँगै छान्नुहोस् वा तुलना गर्नुहोस्',
  closeLanguageSelectorDesc: 'भाषा चयन बन्द गर्नुहोस्',
  primaryLeftPageLabel: 'मुख्य (बायाँ पाना)',
  compareRightPageLabel: 'तुलना (दायाँ पाना)',
  swapLanguagesDesc: 'मुख्य र दोस्रो भाषा साट्नुहोस्',
  spreadModeShort: 'दुई पाना',
  parallelModeShort: 'समानान्तर',
  tabPrimaryPrefix: (l) => `मुख्य: ${l}`,
  tabComparePrefix: (l) => `तुलना: ${l}`,
  booksCountWord: (n) => `${formatNumber(n, SystemLanguage.NEPALI)} पुस्तक`,
  offlineReadyLabel: 'अफलाइन तयार',
  activeLabel: 'सक्रिय',
  offlineLabel: 'अफलाइन',
  booksDownloadedProgress: (done, total) =>
    `${formatNumber(total, SystemLanguage.NEPALI)} मध्ये ${formatNumber(done, SystemLanguage.NEPALI)} पुस्तक अफलाइन डाउनलोड भयो`,
  manageOfflineDownloads: 'अफलाइन डाउनलोड व्यवस्थापन',

  searchScriptureTitle: 'पवित्र शास्त्र खोज्नुहोस्',
  searchPlaceholder: 'अंग्रेजी, ULB, ONCV वा कोरियनमा खोज्नुहोस्...',
  clearSearchDesc: 'खोज सफा गर्नुहोस्',
  filterAllBible: 'सम्पन्न बाइबल',
  filterOldShort: 'पुरानो नियम',
  filterNewShort: 'नयाँ नियम',
  filterAllLanguages: 'सबै भाषा',
  searchPromptEmpty:
    'सबै डाउनलोड गरिएका अफलाइन बाइबल भाषाहरूमा\nखोज्न कम्तीमा २ अक्षर टाइप गर्नुहोस्',
  searchNoResults: (q) => `डाउनलोड गरिएका प्याकहरूमा "${q}" सँग मिल्ने पद भेटिएन`,
  searchMatchedCount: (n) => `${formatNumber(n, SystemLanguage.NEPALI)} पदहरू भेटिए`,

  studyLibraryTitle: 'अध्ययन संग्रह',
  tabBookmarks: (n) => `बुकमार्क (${formatNumber(n, SystemLanguage.NEPALI)})`,
  tabHighlights: (n) => `हाइलाइट (${formatNumber(n, SystemLanguage.NEPALI)})`,
  tabNotes: (n) => `नोट (${formatNumber(n, SystemLanguage.NEPALI)})`,
  tabHistory: (n) => `इतिहास (${formatNumber(n, SystemLanguage.NEPALI)})`,
  noBookmarksTitle: 'अहिलेसम्म कुनै बुकमार्क छैन',
  noBookmarksSub: 'पढ्दाखेरि कुनै पनि पदमा ट्याप गरी अंग्रेजी र नेपाली दुवैमा बुकमार्क गर्नुहोस्।',
  noHighlightsTitle: 'कुनै हाइलाइट गरिएका पद छैनन्',
  noHighlightsSub: 'फ्लिप बुकमा कुनै पद छानी सुनौलो, गुलाबी, हरियो वा आकाशी रङ लगाउनुहोस्।',
  noNotesTitle: 'कुनै व्यक्तिगत नोट छैन',
  noNotesSub: 'मनन वा प्रवचन नोट लेख्न कुनै पनि पदमा ट्याप गरी नोट थप्नुहोस् छान्नुहोस्।',
  noHistoryTitle: 'अहिलेसम्म पठन इतिहास छैन',
  noHistorySub: 'पवित्र बाइबलमा तपाईंले पढ्नुभएका अध्यायहरू छिटो फर्किन यहाँ देखिनेछन्।',
  clearHistory: 'इतिहास मेटाउनुहोस्',
  removeBookmarkDesc: 'बुकमार्क हटाउनुहोस्',
  clearHighlightDesc: 'हाइलाइट हटाउनुहोस्',
  editNoteDesc: 'नोट सम्पादन गर्नुहोस्',
  deleteNoteDesc: 'नोट मेटाउनुहोस्',

  offlineDownloadsTitle: 'अफलाइन बाइबल डाउनलोड',
  offlineDownloadsSubtitle: '१००% अफलाइन पठन, खोज र अध्याय नेभिगेसन',
  storageUsedLabel: (size) => `प्रयोग भएको ब्राउजर भण्डारण: ${size}`,
  languageBooksOfflineCount: (done, total) =>
    `${formatNumber(total, SystemLanguage.NEPALI)} मध्ये ${formatNumber(done, SystemLanguage.NEPALI)} भाषाका पुस्तकहरू अफलाइन`,
  completeBibleOffline: 'पूर्ण बाइबल अफलाइन तयार',
  downloadBibleButton: 'बाइबल डाउनलोड गर्नुहोस्',
  pauseAllDesc: 'सबै रोक्नुहोस्',
  resumeAllDesc: 'सबै पुनः सुरु',
  statusOfflineReady: 'अफलाइन तयार',
  statusDownloading: 'डाउनलोड हुँदै',
  statusPaused: 'रोकिएको',
  statusPartial: 'आंशिक',
  statusNotDownloaded: 'डाउनलोड नभएको',
  booksSlashCount: (done, total) =>
    `${formatNumber(done, SystemLanguage.NEPALI)} / ${formatNumber(total, SystemLanguage.NEPALI)} पुस्तक`,
  downloadRemaining: 'बाँकी डाउनलोड',
  downloadAll: 'सबै डाउनलोड',
  readOffline: 'अफलाइन पढ्नुहोस्',
  deletePackDesc: (l) => `${l} अफलाइन प्याक मेटाउनुहोस्`,
  individualBooksHeader: (n) => `व्यक्तिगत पुस्तकहरू (${formatNumber(n, SystemLanguage.NEPALI)})`,
  hideBooksDesc: 'पुस्तकहरू लुकाउनुहोस्',
  showBooksDesc: 'पुस्तकहरू देखाउनुहोस्',
  bookOfflineSize: (s) => `अफलाइन (${s})`,
  bookDownloading: 'डाउनलोड हुँदै...',
  bookQueued: 'पङ्क्तिबद्ध',
  bookPaused: 'रोकिएको',
  bookNotDownloaded: 'डाउनलोड नभएको',
  deletePackDialogTitle: (l) => `${l} अफलाइन प्याक मेटाउने?`,
  deletePackDialogBody: (l) =>
    `यसले तपाईंको अफलाइन भण्डारणबाट डाउनलोड गरिएका ${l} अध्यायहरू हटाउँछ। तपाईं जहिले पनि पुनः डाउनलोड गर्न सक्नुहुन्छ।`,
  keepButton: 'राख्नुहोस्',

  settingsTitle: 'सेटिङहरू',
  systemLanguageCardTitle: 'प्रणाली भाषा (System Language)',
  systemLanguageSubtitle: 'एप इन्टरफेसको भाषा छान्नुहोस्',
  systemLanguageHint: 'मेनु, बटन, सेटिङ, सूचना र प्रणाली सन्देशहरूको भाषा परिवर्तन गर्छ।',
  themesCardTitle: 'लिक्विड ग्लास र थिमहरू',
  themeSummaryStats: (op, blur, font) =>
    `${formatNumber(op, SystemLanguage.NEPALI)}% पारदर्शिता • ${formatNumber(blur, SystemLanguage.NEPALI)} dp ब्लर • ${font}`,
  customizeThemeButton: 'थिम अनुकूलन र लाइभ पूर्वावलोकन',
  brightnessCardTitle: 'पठन थिम उज्यालोपन',
  nightDarkModeLabel: 'रात्रि अध्ययन डार्क मोड',
  nightDarkModeOnSub: 'कम चमक र रात्रि पठनका लागि डार्क ग्लास',
  nightDarkModeOffSub: 'दिनको समयका लागि उज्यालो थिम',
  languagesCardTitle: 'बाइबल भाषा र अफलाइन प्याक',
  activePairLabel: (p, s) => `सक्रिय जोडी: ${p} ↔ ${s}`,
  offlineStorageUsedLabel: (s) => `अफलाइन भण्डारण: ${s}`,
  languagesButton: 'भाषाहरू',
  downloadsButton: 'डाउनलोड',
  typographyCardTitle: 'पुस्तक लेआउट र अक्षर शैली',
  readingModeLabel: 'पठन मोड',
  singleLanguageOnly: (l) => `${l} मात्र`,
  scriptureFontSizeLabel: 'शास्त्र अक्षर साइज',
  pageSoundCardTitle: 'पाना पल्टाउने आवाज',
  pageSoundToggleTitle: 'पाना पल्टाउने ध्वनि',
  pageSoundMuted: 'मौन (आवाज बन्द)',
  pageSoundSilentMode: 'मौन मोड (आवाज छैन)',
  pageSoundActiveStatus: (name, vol) =>
    `${name} • ${formatNumber(vol, SystemLanguage.NEPALI)}% भोल्युम`,
  randomSoundModeTitle: 'अनियमित ध्वनि मोड',
  randomSoundModeSub: 'हरेक पाना पल्टाउँदा स्वाभाविक कागजको आवाज बदलिन्छ',
  pageSoundVolumeTitle: 'पाना ध्वनि भोल्युम',
  choosePageSoundTitle: 'पाना पल्टाउने आवाज छान्नुहोस्',
  choosePageSoundSub: "छान्नुअघि सुन्नको लागि 'सुन्नुहोस्' ट्याप गर्नुहोस्",
  selectedDesc: 'छानिएको',
  previewButton: 'सुन्नुहोस्',
  testForwardButton: 'अगाडि परीक्षण',
  testBackwardButton: 'पछाडि परीक्षण',
  audioNarrationCardTitle: 'अडियो बाइबल वाचन',
  narrationLanguageLabel: 'वाचन भाषा',
  speechSpeedLabel: 'वाचन गति',
  deviceVoicesStatus: (en, ne, ko) =>
    `ब्राउजर आवाज — अंग्रेजी: ${en ? 'तयार' : 'स्थापित छैन'} • नेपाली: ${ne ? 'तयार' : 'उपलब्ध छैन'} • कोरियन: ${ko ? 'तयार' : 'उपलब्ध छैन'}`,
  voiceReady: 'तयार',
  voiceNotInstalled: 'स्थापित छैन',
  voiceInstallTts: 'प्रणाली आवाज',
  refreshSpeechVoices: 'आवाज सूची रिफ्रेस गर्नुहोस्',
  voicesRefreshedToast: 'उपलब्ध वाचन आवाजहरू रिफ्रेस गरियो',
  licensesCardTitle: 'प्रतिलिपि अधिकार र श्रेय',
  authenticEditionsHeading:
    'परिचय • बाइबल अनुमतिपत्र, अनुवाद स्रोत, अडियो/मिडिया र विकासकर्ता श्रेय',
  versionNameAbbrevLabel: 'बाइबल संस्करण र संक्षिप्त नाम',
  fullTranslationNameLabel: 'पूर्ण अनुवाद नाम',
  copyrightOwnerLabel: 'प्रतिलिपि अधिकार धनी / प्रकाशक',
  copyrightNoticeLabel: 'प्रतिलिपि अधिकार सूचना',
  publisherCreditLabel: 'अनुवाद श्रेय',
  licenseLabel: 'अनुमतिपत्र प्रकार',
  usagePermissionsLabel: 'प्रयोग र वितरण अनुमति',
  sourceLabel: 'आधिकारिक स्रोत वेबसाइट',
  copyrightNotVerifiedYet: 'प्रतिलिपि अधिकार जानकारी अझै प्रमाणित गरिएको छैन।',
  copyrightCategoryLabel: (category) => {
    switch (category) {
      case CopyrightLicenseCategory.OPEN_LICENSE:
        return 'खुला अनुमतिपत्र (Open License)';
      case CopyrightLicenseCategory.PUBLIC_DOMAIN:
        return 'सार्वजनिक क्षेत्र (Public Domain)';
      case CopyrightLicenseCategory.COPYRIGHTED:
        return 'प्रतिलिपि अधिकार सुरक्षित';
      case CopyrightLicenseCategory.UNVERIFIED:
        return 'प्रमाणित नभएको';
    }
  },
  openSourceLinkError: 'स्रोत वेबसाइट खोल्न ब्राउजर उपलब्ध छैन',
  expandSectionDesc: 'खण्ड खोल्नुहोस्',
  collapseSectionDesc: 'खण्ड बन्द गर्नुहोस्',
  subsectionBibleCopyrightTitle: 'क. बाइबल अनुवाद प्रतिलिपि अधिकार र स्रोतहरू',
  subsectionBibleCopyrightSubtitle: 'ULB, ONCV, KJV र KRV अनुमतिपत्र, धनी र आधिकारिक लिङ्कहरू',
  subsectionBibleTranslationCreditsTitle: 'ख. बाइबल अनुवाद श्रेय',
  subsectionBibleTranslationCreditsSubtitle: 'अनुवाद योगदानकर्ता, बाइबल प्रकाशक र पाठ स्रोतहरू',
  subsectionAudioMediaCreditsTitle: 'ग. अडियो र मिडिया श्रेय',
  subsectionAudioMediaCreditsSubtitle: 'अडियो बाइबल प्रदायक, आवाज योगदानकर्ता र मिडिया स्रोतहरू',
  subsectionDeveloperTechCreditsTitle: 'घ. विकासकर्ता र प्रविधि श्रेय',
  subsectionDeveloperTechCreditsSubtitle: 'एप विकासकर्ता जानकारी, पुस्तकालय र खुला-स्रोत श्रेय',
  bibleSourceAttributions: [
    {
      versionId: 'ULB',
      title: 'ULB – Unlocked Literal Bible (अनलक्ड लिटरल बाइबल)',
      fullTranslationName: 'नेपाली अनलक्ड लिटरल बाइबल (पवित्र बाइबल, npiulb)',
      ownerPublisher: 'Door43 World Missions Community (unfoldingWord®)',
      copyrightNotice: 'प्रतिलिपि अधिकार © २०१९ Door43 World Missions Community',
      sourceUrls: [
        'https://door43.org/',
        'https://ebible.org/find/show.php?id=npiulb',
        'https://creativecommons.org/licenses/by-sa/4.0/',
      ],
      license: 'Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)',
      licenseCategory: CopyrightLicenseCategory.OPEN_LICENSE,
      usagePermissions:
        'Door43 World Missions Community / unfoldingWord® लाई श्रेय दिँदै, परिवर्तन भए उल्लेख गरी CC BY-SA 4.0 अन्तर्गत साझेदारी गरेमा निःशुल्क प्रतिलिपि, वितरण र रूपान्तरण गर्न सकिन्छ।',
      translationCredits:
        'Door43 World Missions Community र unfoldingWord® योगदानकर्ताहरू द्वारा अनुवादित • ६६ पुस्तकहरू',
    },
    {
      versionId: 'ONCV',
      title: 'ONCV – Old Nepali Contemporary Version (नेपाली समकालीन संस्करण)',
      fullTranslationName:
        'Biblica® नेपाली समकालीन सर्वसुलभ संस्करण™ (Open Nepali Contemporary Version, npioncb)',
      ownerPublisher: 'Biblica, Inc.',
      copyrightNotice: 'प्रतिलिपि अधिकार © १९९८, २००६, २०२१, २०२४ Biblica, Inc.',
      sourceUrls: [
        'https://www.biblica.com/',
        'https://open.bible/',
        'https://ebible.org/find/show.php?id=npioncb',
      ],
      license: 'Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)',
      licenseCategory: CopyrightLicenseCategory.OPEN_LICENSE,
      usagePermissions:
        'Biblica, Inc. लाई श्रेय दिई, प्रतिलिपि अधिकार सूचना सुरक्षित राखी, ट्रेडमार्क नियम पालना गर्दै र रूपान्तरित कार्य CC BY-SA 4.0 अन्तर्गत साझा गरेमा लिखित, डिजिटल वा अडियो रूपमा वितरण गर्न सकिन्छ।',
      translationCredits:
        'Biblica, Inc. अनुवाद टोली (Biblica® Open Nepali Contemporary Version™) • ६६ पुस्तकहरू',
    },
    {
      versionId: 'KJV',
      title: 'KJV – King James Version (अंग्रेजी)',
      fullTranslationName: 'Authorized King James Version (१७६९ मानक पाठ)',
      ownerPublisher: 'सार्वजनिक क्षेत्र (१६११ अधिकृत संस्करण; १७६९ अक्सफोर्ड/क्याम्ब्रिज मानक पाठ)',
      copyrightNotice:
        'विश्वभर सार्वजनिक क्षेत्र (बेलायतबाहिर कुनै प्रतिलिपि अधिकार दाबी छैन; बेलायतभित्र मुद्रण शाही पेटेन्ट अधीनमा छ)।',
      sourceUrls: ['https://ebible.org/find/show.php?id=eng-kjv2006'],
      license: 'Public Domain (बेलायतभित्र Crown Patent नियम लागू हुन्छ)',
      licenseCategory: CopyrightLicenseCategory.PUBLIC_DOMAIN,
      usagePermissions:
        'बेलायतबाहिर विश्वभर रोयल्टी वा अनुमति बिना निःशुल्क प्रतिलिपि, वितरण र प्रयोग गर्न सकिन्छ। बेलायतभित्र व्यावसायिक मुद्रणका लागि अधिकृत अनुमति आवश्यक हुन्छ।',
      translationCredits:
        'Authorized King James Version अनुवादकहरू (१६११) र बेन्जामिन ब्लेनी (१७६९ मानक संस्करण); डिजिटल पाठ eBible.org बाट • ६६ पुस्तकहरू',
    },
    {
      versionId: 'KRV',
      title: 'KRV – Korean Revised Version (개역한글)',
      fullTranslationName: '성경전서 개역한글판 (कोरियन संशोधित संस्करण, १९६१)',
      ownerPublisher: 'Korean Bible Society (대한성서공회, १९६१ मूल प्रकाशक)',
      copyrightNotice:
        'सार्वजनिक क्षेत्र (१९६१ कोरियन संशोधित संस्करणको प्रतिलिपि अधिकार अवधि २०११–२०१२ मा समाप्त; १९९८ को 개역개정 संस्करण अझै प्रतिलिपि अधिकार सुरक्षित छ)।',
      sourceUrls: ['https://www.bskorea.or.kr/', 'https://ebible.org/'],
      license: 'Public Domain (अवधि समाप्त प्रतिलिपि अधिकार – १९६१ 개역한글 पाठ)',
      licenseCategory: CopyrightLicenseCategory.PUBLIC_DOMAIN,
      usagePermissions:
        '१९६१ को Korean Revised Version (개역한글) पाठ सार्वजनिक क्षेत्रमा भएकाले निःशुल्क पढ्न, प्रतिलिपि गर्न र वितरण गर्न सकिन्छ। १९९८ को NKRV (개역개정) प्रतिलिपि अधिकार सुरक्षित भएकाले यहाँ समावेश छैन।',
      translationCredits:
        'Korean Bible Society (대한성서공회) १९६१ संशोधित हान्गुल अनुवाद समिति • ६६ पुस्तकहरू',
    },
  ],
  bibleTranslationCreditItems: [
    {
      title: 'अनुवाद योगदानकर्ताहरू',
      subtitle: 'विद्वान, अनुवादक र सामुदायिक समीक्षकहरू',
      description:
        '• ULB (नेपाली): Door43 World Missions Community र unfoldingWord® योगदानकर्ताहरू\n• ONCV (नेपाली): Biblica® Open Nepali Contemporary Version™ अनुवाद टोली\n• KJV (अंग्रेजी): Authorized King James Version अनुवादकहरू (१६११) र बेन्जामिन ब्लेनी (१७६९)\n• KRV (कोरियन): Korean Bible Society १९६१ संशोधित हान्गुल अनुवाद समिति',
    },
    {
      title: 'बाइबल प्रकाशकहरू',
      subtitle: 'प्रकाशन र अभिभावक संस्थाहरू',
      description:
        '• Door43 World Missions Community / unfoldingWord® (ULB)\n• Biblica, Inc. — अन्तर्राष्ट्रिय बाइबल समाज (ONCV)\n• Oxford र Cambridge १७६९ मानक पाठ (KJV सार्वजनिक क्षेत्र; बेलायतभित्र Crown Patent नियम)\n• Korean Bible Society / 대한성서공회 (१९६१ कोरियन संशोधित संस्करण)',
      links: [
        { label: 'unfoldingWord / Door43', url: 'https://door43.org/' },
        { label: 'Biblica, Inc.', url: 'https://www.biblica.com/' },
        { label: 'Korean Bible Society', url: 'https://www.bskorea.or.kr/' },
      ],
    },
    {
      title: 'बाइबल पाठ स्रोतहरू',
      subtitle: 'प्रमाणित अभिलेख र बाह्य लिङ्कहरू',
      description:
        '• eBible.org डिजिटल बाइबल अभिलेख (npiulb, npioncb, eng-kjv2006)\n• Open.Bible र Door43 खुला-अनुमतिपत्र बाइबल भण्डारहरू\n• बाह्य Bible.com NNRV सन्दर्भ लिङ्क (गृहपृष्ठ सर्टकट मार्फत उपलब्ध)',
      links: [
        { label: 'eBible.org', url: 'https://ebible.org/' },
        { label: 'Open.Bible', url: 'https://open.bible/' },
        { label: 'Bible.com (NNRV)', url: 'https://www.bible.com/bible/1483/GEN.1.NNRV' },
      ],
    },
  ],
  audioMediaCreditItems: [
    {
      title: 'अडियो बाइबल प्रदायकहरू',
      subtitle: 'वाचन प्रणाली र अडियो नियन्त्रण',
      description:
        'पद-पद अडियो बाइबल वाचन ब्राउजरको Web Speech API (SpeechSynthesis) र बहुभाषिक अध्याय क्रमबद्ध प्रणालीद्वारा सञ्चालित छ।',
    },
    {
      title: 'वाचक र आवाज योगदानकर्ताहरू',
      subtitle: 'उपकरण र प्रणाली आवाज प्याकहरू',
      description:
        'वाचन आवाजहरू तपाईंको उपकरण र ब्राउजरमा स्थापित अंग्रेजी, नेपाली (ne-NP) र कोरियन (ko-KR) Text-to-Speech आवाज प्याकहरूबाट प्रदान गरिन्छन्।',
    },
    {
      title: 'छवि र मिडिया स्रोतहरू',
      subtitle: 'समेटिएका कलाकृति, आइकन र पाना पल्टाउने ध्वनि',
      description:
        '• अफलाइन पठनका लागि समेटिएका छालाको पवित्र बाइबल आवरण चित्र, दैनिक पद ब्यानर र एप चिन्ह\n• पाना पल्टाउने १० वास्तविक ध्वनि नमूनाहरू (Web Audio API)\n• Lucide Icons बाट स्पष्ट भेक्टर आइकनहरू',
    },
  ],
  developerTechCreditItems: [
    {
      title: 'एप विकासकर्ता जानकारी',
      subtitle: 'पवित्र बाइबल — अंग्रेजी • नेपाली (ULB र ONCV) • कोरियन',
      description:
        '३डी दोहोरो-पृष्ठ पठन, अडियो बाइबल, बुकमार्क, हाइलाइट, अध्ययन नोट, खोज र लिक्विड ग्लास तथा क्लासिक थिमहरूसहितको अफलाइन बहुभाषिक पवित्र बाइबल अध्ययन वेब एप।',
    },
    {
      title: 'प्रयोग गरिएका वेब प्रविधि र फन्टहरू',
      subtitle: 'React, TypeScript र टाइपोग्राफी',
      description:
        '• React, TypeScript, Vite र Tailwind CSS\n• अफलाइन भण्डारणका लागि IndexedDB र fflate gzip\n• समेटिएका फन्टहरू: Playfair Display, Crimson Pro, Lora, Merriweather, Inter, Plus Jakarta Sans, Noto Serif Devanagari र Noto Serif KR',
      links: [
        { label: 'Google Fonts', url: 'https://fonts.google.com/' },
        { label: 'CC BY-SA 4.0', url: 'https://creativecommons.org/licenses/by-sa/4.0/' },
      ],
    },
  ],

  themeStudioTitle: 'लिक्विड ग्लास र थिम स्टुडियो',
  themeStudioSubtitle: 'लागू गर्नुअघि लाइभ पूर्वावलोकन हेर्नुहोस्',
  themePresetsHeading: (count) => `थिम शैलीहरू (${formatNumber(count, SystemLanguage.NEPALI)})`,
  activePresetLabel: (name) => `सक्रिय: ${name}`,
  liquidGlassOpticsHeading: 'लिक्विड ग्लास अप्टिक्स',
  hardwareBlurSub: 'ब्याकड्रप फिल्टर फ्रस्टेड ब्लर सक्रिय छ',
  glassOpacityLabel: 'ग्लास पारदर्शिता',
  glassBlurLabel: 'ग्लास ब्लर तीव्रता',
  colorsHeading: 'रङहरू',
  colorsSub: 'पृष्ठभूमि, आकर्षण र अक्षर रङ अनुकूलन गर्नुहोस्',
  backgroundColorLabel: 'पृष्ठभूमि रङ',
  accentColorLabel: 'मुख्य रङ',
  textColorLabel: 'अक्षर रङ',
  autoContrastButton: 'स्वचालित कन्ट्रास्ट',
  controlsNavHeading: 'बटन र नेभिगेसन शैली',
  controlsNavSub: 'बटन आकार र तल्लो पट्टीको स्वरूप छान्नुहोस्',
  buttonStyleLabel: 'बटन शैली',
  navBarAppearanceLabel: 'नेभिगेसन पट्टी स्वरूप',
  readingPageTypographyHeading: 'बाइबल पठन पाना र अक्षर शैली',
  readingPageTypographySub: 'पठन कागज पृष्ठभूमि, अक्षर शैली र साइज मिलाउनुहोस्',
  readingPageBgLabel: 'पठन पानाको पृष्ठभूमि',
  scriptureFontStyleLabel: 'शास्त्र अक्षर शैली',
  livePreviewPrefix: (name) => `लाइभ पूर्वावलोकन • ${name}`,
  livePreviewReadButton: 'पढ्नुहोस्',
  applyThemeButton: 'थिम लागू गर्नुहोस्',

  audioActiveDesc: 'अडियो बाइबल सक्रिय छ',
  audioPrevDesc: 'अघिल्लो पद',
  audioNextDesc: 'अर्को पद',
  audioPlayDesc: 'अडियो बजाउनुहोस्',
  audioPauseDesc: 'अडियो रोक्नुहोस्',
  audioStopDesc: 'अडियो बन्द गर्नुहोस्',
  ttsNepaliMissingNotice:
    'यो ब्राउजरमा नेपाली TTS आवाज प्याक भेटिएन; शुद्ध नेपाली उच्चारणका लागि प्रणाली वाचन सेटिङमा नेपाली आवाज स्थापना गर्नुहोस्।',
  ttsKoreanMissingNotice:
    'यो ब्राउजरमा कोरियन TTS आवाज प्याक भेटिएन; कोरियन उच्चारणका लागि प्रणाली वाचन सेटिङमा कोरियन आवाज स्थापना गर्नुहोस्।',
  toastVerseCopied: 'पद क्लिपबोर्डमा प्रतिलिपि गरियो',
  toastFacebookCopied: 'पद प्रतिलिपि भयो! फेसबुकमा पेस्ट गरी सेयर गर्नुहोस्',
  shareAppBrand: 'पवित्र बाइबल',

  bibleLanguageName: (lang) => {
    switch (lang) {
      case BibleLanguage.ENGLISH:
        return 'अंग्रेजी';
      case BibleLanguage.NEPALI:
        return 'नेपाली (ULB)';
      case BibleLanguage.ONCV:
        return 'नेपाली (ONCV)';
      case BibleLanguage.KOREAN:
        return 'कोरियन';
    }
  },
  themePresetName: (preset) => {
    switch (preset) {
      case ThemePreset.IOS_LIQUID_GLASS:
        return 'iOS लिक्विड ग्लास';
      case ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS:
        return 'क्रिस्टल क्लियर लिक्विड ग्लास';
      case ThemePreset.CRYSTAL_CLEAR_DARK_GLASS:
        return 'क्रिस्टल क्लियर डार्क ग्लास';
      case ThemePreset.TRANSPARENT_GLASS:
        return 'पारदर्शी ग्लास';
      case ThemePreset.DARK_GLASS:
        return 'डार्क ग्लास';
      case ThemePreset.LIGHT_GLASS:
        return 'लाइट ग्लास';
      case ThemePreset.AMOLED_BLACK:
        return 'AMOLED कालो';
      case ThemePreset.CLASSIC_BIBLE:
        return 'क्लासिक बाइबल';
      case ThemePreset.ELEGANT_BLUE:
        return 'सुरुचिपूर्ण नीलो';
      case ThemePreset.NATURE_GREEN:
        return 'प्रकृति हरियो';
      case ThemePreset.GOLDEN_PREMIUM:
        return 'सुनौलो प्रिमियम';
    }
  },
  themePresetSubtitle: (preset) => {
    switch (preset) {
      case ThemePreset.IOS_LIQUID_GLASS:
        return 'फ्रस्टेड क्रिस्टल पारदर्शिता र चम्किलो गहिराइ';
      case ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS:
        return 'अत्यधिक पारदर्शी उज्यालो ग्लास र प्रिज्म चमक';
      case ThemePreset.CRYSTAL_CLEAR_DARK_GLASS:
        return 'नीलमणि चमक सहितको गाढा क्रिस्टल ग्लास';
      case ThemePreset.TRANSPARENT_GLASS:
        return 'स्पष्ट क्रिस्टल तह र चम्किलो किनाराहरू';
      case ThemePreset.DARK_GLASS:
        return 'गाढा अब्सिडियन फ्रस्ट र रात्रि चमक';
      case ThemePreset.LIGHT_GLASS:
        return 'नरम सेतो आकाशमा हल्का मोती फ्रस्ट';
      case ThemePreset.AMOLED_BLACK:
        return 'शुद्ध कालो OLED कन्ट्रास्ट र हल्का फ्रस्ट';
      case ThemePreset.CLASSIC_BIBLE:
        return 'बरगन्डी छाला, प्राचीन सुनौलो र क्रिम कागज';
      case ThemePreset.ELEGANT_BLUE:
        return 'शाही नीलमणि गहिराइ र सफा चाँदी फ्रस्ट';
      case ThemePreset.NATURE_GREEN:
        return 'शान्त वानस्पतिक हरियो र न्यानो सन्तुलन';
      case ThemePreset.GOLDEN_PREMIUM:
        return 'चम्किलो सुनौलो र गाढा कफी मखमली';
    }
  },
  buttonStyleName: (opt) => {
    switch (opt) {
      case ButtonStyleOption.LIQUID_GLASS:
        return 'लिक्विड ग्लास';
      case ButtonStyleOption.PILL_FILLED:
        return 'भरिपूर्ण पिल';
      case ButtonStyleOption.OUTLINED_GLASS:
        return 'क्रिस्टल आउटलाइन';
      case ButtonStyleOption.CLASSIC_ELEVATED:
        return 'क्लासिक पुस्तक';
    }
  },
  navBarAppearanceName: (opt) => {
    switch (opt) {
      case NavBarAppearance.FLOATING_GLASS_DOCK:
        return 'फ्लोटिङ ग्लास डक';
      case NavBarAppearance.FROSTED_FULL_BAR:
        return 'फ्रस्टेड ग्लास पट्टी';
      case NavBarAppearance.SOLID_MINIMAL:
        return 'क्लासिक ठोस पट्टी';
    }
  },
  readingBgName: (bg) => {
    switch (bg) {
      case ReadingPageBackground.MATCH_THEME:
        return 'थिम अनुसार';
      case ReadingPageBackground.FROSTED_GLASS:
        return 'फ्रस्टेड ग्लास';
      case ReadingPageBackground.CREAM_PARCHMENT:
        return 'क्रिम कागज';
      case ReadingPageBackground.PURE_WHITE:
        return 'चम्किलो सेतो';
      case ReadingPageBackground.SEPIA_WARM:
        return 'न्यानो सेपिया';
      case ReadingPageBackground.NIGHT_OBSIDIAN:
        return 'रात्रि गाढा';
      case ReadingPageBackground.AMOLED_BLACK:
        return 'AMOLED कालो';
    }
  },
  readingModeName: (mode) => {
    switch (mode) {
      case ReadingMode.BILINGUAL_SPREAD:
        return 'पुस्तक पाना';
      case ReadingMode.BILINGUAL_PARALLEL:
        return 'समानान्तर';
      case ReadingMode.SINGLE_PRIMARY:
        return 'एक भाषा';
      case ReadingMode.SINGLE_SECONDARY:
        return 'दोस्रो भाषा';
    }
  },
  audioLanguageName: (lang) => {
    switch (lang) {
      case AudioLanguage.BILINGUAL:
        return 'अंग्रेजी + नेपाली';
      case AudioLanguage.ENGLISH:
        return 'अंग्रेजी';
      case AudioLanguage.NEPALI:
        return 'नेपाली (ULB)';
      case AudioLanguage.ONCV:
        return 'नेपाली (ONCV)';
      case AudioLanguage.KOREAN:
        return 'कोरियन';
    }
  },
  pageSoundName: (style) => {
    switch (style) {
      case PageTurnSoundStyle.REALISTIC_BIBLE:
        return 'यथार्थपरक बाइबल पाना';
      case PageTurnSoundStyle.SOFT_PAPER:
        return 'नरम कागज पल्टाइ';
      case PageTurnSoundStyle.OLD_VINTAGE_BIBLE:
        return 'पुरानो र भिन्टेज बाइबल';
      case PageTurnSoundStyle.GENTLE_RUSTLE:
        return 'हल्का कागज सरसराहट';
      case PageTurnSoundStyle.CLASSIC_HARDCOVER:
        return 'क्लासिक हार्डकभर पुस्तक';
      case PageTurnSoundStyle.FAST_FLIP:
        return 'छिटो पाना पल्टाइ';
      case PageTurnSoundStyle.SLOW_SOFT:
        return 'बिस्तारै र नरम पल्टाइ';
      case PageTurnSoundStyle.ANTIQUE_PARCHMENT:
        return 'बाक्लो प्राचीन चर्मपत्र';
      case PageTurnSoundStyle.NATURAL_MOVEMENT:
        return 'सूक्ष्म कागज चाल';
      case PageTurnSoundStyle.SOFT_DIGITAL:
        return 'नरम डिजिटल पाना';
      case PageTurnSoundStyle.RANDOM:
        return 'अनियमित ध्वनि मोड';
      case PageTurnSoundStyle.SILENT:
        return 'मौन मोड (आवाज छैन)';
    }
  },
  pageSoundDescription: (style) => {
    switch (style) {
      case PageTurnSoundStyle.REALISTIC_BIBLE:
        return 'पातलो बाइबल कागजको स्पष्ट मोड र नरम आवाज';
      case PageTurnSoundStyle.SOFT_PAPER:
        return 'हल्का क्रिम कागज पल्टाउँदाको कोमल ध्वनि';
      case PageTurnSoundStyle.OLD_VINTAGE_BIBLE:
        return 'पुरानो बाइबल पानाको न्यानो र प्राकृतिक बनावट';
      case PageTurnSoundStyle.GENTLE_RUSTLE:
        return 'शान्त, हावा जस्तो पानाको सरसराहट';
      case PageTurnSoundStyle.CLASSIC_HARDCOVER:
        return 'हल्का तौल सहितको परम्परागत पुस्तक पल्टाइ';
      case PageTurnSoundStyle.FAST_FLIP:
        return 'छिटोछिटो पाना पल्टाउँदाको चुस्त आवाज';
      case PageTurnSoundStyle.SLOW_SOFT:
        return 'ध्यानमग्न भएर बिस्तारै पाना सार्ने आवाज';
      case PageTurnSoundStyle.ANTIQUE_PARCHMENT:
        return 'बाक्लो कागज र गहिरो पाना थिग्राइ';
      case PageTurnSoundStyle.NATURAL_MOVEMENT:
        return 'अत्यन्त सूक्ष्म र शान्त कागज स्पर्श';
      case PageTurnSoundStyle.SOFT_DIGITAL:
        return 'आधुनिक साटिन जस्तो हल्का पाना आवाज';
      case PageTurnSoundStyle.RANDOM:
        return 'हरेक पल्टाइमा स्वाभाविक कागज आवाज बदल्छ';
      case PageTurnSoundStyle.SILENT:
        return 'पाना पल्टाउँदा पूर्ण शान्ति (कुनै आवाज छैन)';
    }
  },
  bookDisplayName: getBookNameForLanguage,
};

export function getAppStrings(language: SystemLanguage): AppStrings {
  return language === SystemLanguage.NEPALI ? NepaliAppStrings : EnglishAppStrings;
}

export function formatHeaderNumber(num: number, sysLang: SystemLanguage): string {
  return sysLang === SystemLanguage.NEPALI ? toDevanagariDigits(String(num)) : String(num);
}
