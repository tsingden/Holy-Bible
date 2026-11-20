import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import {
  AppRoute,
  AppThemeConfig,
  AudioLanguage,
  AudioPlaybackState,
  BibleBook,
  BibleLanguage,
  BiblePreferencesState,
  BibleVerse,
  BookDownloadState,
  BookmarkEntity,
  ChapterSnapshot,
  DailyVerseData,
  HighlightColor,
  HighlightEntity,
  LanguageDownloadSummary,
  NoteEntity,
  PageTurnSoundStyle,
  ReadingHistoryEntity,
  ReadingMode,
  ReadingPageBackground,
  SearchResult,
  SystemLanguage,
  ThemePreset,
  createThemeConfigFromPreset,
} from '../types/bible';
import { AppStrings, getAppStrings } from '../localization/appStrings';
import {
  LiquidGlassTokens,
  BibleBookPalette,
  resolveBiblePalette,
  resolveLiquidGlassTokens,
} from '../theme/themeTokens';
import {
  bibleDb,
  loadBookmarks,
  loadHighlights,
  loadHistory,
  loadNotes,
  loadPreferences,
  saveBookmarks,
  saveHighlights,
  saveHistory,
  saveNotes,
  savePreferences,
} from '../data/bibleDb';
import { bibleDownloadManager } from '../data/downloadManager';
import { bibleAudioController } from '../audio/bibleAudioController';
import { paperSoundPlayer } from '../audio/paperSoundPlayer';

export interface ReaderUiState {
  isLoading: boolean;
  currentBook: BibleBook | null;
  currentChapter: number;
  verses: BibleVerse[];
  prevChapterSnapshot: ChapterSnapshot | null;
  nextChapterSnapshot: ChapterSnapshot | null;
  selectedVerse: BibleVerse | null;
  targetHighlightVerse: number | null;
}

interface BibleAppContextValue {
  route: AppRoute;
  navigate: (to: AppRoute) => void;
  goBack: () => void;

  showPickerDialog: boolean;
  pickerInitialTestament: 'OT' | 'NT' | null;
  openScripturePicker: (testament?: 'OT' | 'NT' | null) => void;
  closeScripturePicker: () => void;

  showLanguageSheet: boolean;
  openLanguageSheet: () => void;
  closeLanguageSheet: () => void;

  toastMessage: string | null;
  showToast: (msg: string) => void;

  preferences: BiblePreferencesState;
  tokens: LiquidGlassTokens;
  palette: BibleBookPalette;
  strings: AppStrings;

  books: BibleBook[];
  dailyVerse: DailyVerseData | null;
  readerState: ReaderUiState;
  audioState: AudioPlaybackState;

  languageSummaries: LanguageDownloadSummary[];
  bookDownloadStates: Map<string, BookDownloadState>;
  downloadedBookKeys: Set<string>;
  totalStorageBytes: number;

  bookmarks: BookmarkEntity[];
  highlights: HighlightEntity[];
  notes: NoteEntity[];
  history: ReadingHistoryEntity[];
  bookmarkedKeys: Set<string>;
  highlightMap: Map<string, HighlightColor>;
  notesMap: Map<string, NoteEntity>;

  searchQuery: string;
  searchTestamentFilter: 'OT' | 'NT' | null;
  searchLanguageFilter: BibleLanguage | null;
  searchResults: SearchResult[];
  isSearching: boolean;
  updateSearchQuery: (q: string) => void;
  updateSearchFilter: (t: 'OT' | 'NT' | null) => void;
  updateSearchLanguageFilter: (l: BibleLanguage | null) => void;

  openChapter: (
    bookId: IntLike,
    chapter: IntLike,
    highlightVerse?: number | null,
    recordHistory?: boolean
  ) => Promise<void>;
  goToNextChapter: () => void;
  goToPreviousChapter: () => void;
  clearTargetHighlight: () => void;
  savePageProgress: (firstVerse: number) => void;
  selectVerse: (verse: BibleVerse | null) => void;
  loadChapterVerseCount: (bookId: number, chapter: number) => Promise<number>;

  toggleBookmark: (verse: BibleVerse) => void;
  removeBookmark: (verseKey: string) => void;
  setHighlight: (verse: BibleVerse, color: HighlightColor) => void;
  saveNote: (verse: BibleVerse, noteText: string) => void;
  deleteNote: (verseKey: string) => void;
  clearHistory: () => void;

  startAudioFromVerse: (startVerse?: number) => void;
  startAudioFromHome: () => void;
  toggleAudioPlayPause: () => void;
  audioNextVerse: () => void;
  audioPrevVerse: () => void;
  stopAudio: () => void;
  setAudioLanguage: (lang: AudioLanguage) => void;
  setAudioSpeed: (speed: number) => void;
  refreshAudioVoices: () => void;

  setDarkMode: (enabled: boolean) => void;
  saveThemeConfig: (config: AppThemeConfig) => void;
  applyThemePreset: (preset: ThemePreset) => void;
  setFontSize: (sp: number) => void;
  setPageSoundEnabled: (enabled: boolean) => void;
  setPageSoundStyle: (style: PageTurnSoundStyle) => void;
  setPageSoundVolume: (vol: number) => void;
  previewPageSound: (style: PageTurnSoundStyle, forward?: boolean) => void;
  playForwardSound: () => void;
  playBackwardSound: () => void;
  setReadingMode: (mode: ReadingMode) => void;
  setPrimaryLanguage: (lang: BibleLanguage) => void;
  setSecondaryLanguage: (lang: BibleLanguage) => void;
  swapLanguages: () => void;
  setSystemLanguage: (lang: SystemLanguage) => void;

  downloadCompleteBible: () => void;
  downloadLanguage: (lang: BibleLanguage) => void;
  downloadBook: (lang: BibleLanguage, bookId: number) => void;
  pauseLanguageDownload: (lang: BibleLanguage) => void;
  pauseAllDownloads: () => void;
  resumeLanguageDownload: (lang: BibleLanguage) => void;
  resumeAllDownloads: () => void;
  cancelLanguageDownload: (lang: BibleLanguage) => void;
  cancelBookDownload: (lang: BibleLanguage, bookId: number) => void;
  deleteLanguageDownload: (lang: BibleLanguage) => void;
  deleteBookDownload: (lang: BibleLanguage, bookId: number) => void;
}

type IntLike = number;

const BibleAppContext = createContext<BibleAppContextValue | null>(null);

function isReaderPreferringOncv(prefs: BiblePreferencesState): boolean {
  if (prefs.readingMode === ReadingMode.SINGLE_PRIMARY) {
    return prefs.primaryLanguage === BibleLanguage.ONCV;
  }
  if (prefs.readingMode === ReadingMode.SINGLE_SECONDARY) {
    return prefs.secondaryLanguage === BibleLanguage.ONCV;
  }
  return (
    prefs.primaryLanguage === BibleLanguage.ONCV ||
    (prefs.secondaryLanguage === BibleLanguage.ONCV &&
      prefs.primaryLanguage !== BibleLanguage.NEPALI)
  );
}

function resolveEffectiveAudioLanguage(prefs: BiblePreferencesState): AudioLanguage {
  if (prefs.audioLanguage === AudioLanguage.NEPALI && isReaderPreferringOncv(prefs)) {
    return AudioLanguage.ONCV;
  }
  return prefs.audioLanguage;
}

async function loadAdjacentSnapshot(
  book: BibleBook,
  chapter: number,
  forward: boolean
): Promise<ChapterSnapshot | null> {
  if (forward) {
    if (chapter < book.chapterCount) {
      const nextVerses = (await bibleDb.getChapterVerses(book.id, chapter + 1)).slice(0, 14);
      return { book, chapter: chapter + 1, verses: nextVerses };
    }
    if (book.id < 66) {
      const nextBook = await bibleDb.getBook(book.id + 1);
      if (!nextBook) return null;
      const nextVerses = (await bibleDb.getChapterVerses(nextBook.id, 1)).slice(0, 14);
      return { book: nextBook, chapter: 1, verses: nextVerses };
    }
    return null;
  } else {
    if (chapter > 1) {
      const prevVerses = (await bibleDb.getChapterVerses(book.id, chapter - 1)).slice(0, 14);
      return { book, chapter: chapter - 1, verses: prevVerses };
    }
    if (book.id > 1) {
      const prevBook = await bibleDb.getBook(book.id - 1);
      if (!prevBook) return null;
      const prevVerses = (
        await bibleDb.getChapterVerses(prevBook.id, prevBook.chapterCount)
      ).slice(0, 14);
      return { book: prevBook, chapter: prevBook.chapterCount, verses: prevVerses };
    }
    return null;
  }
}

export const BibleAppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [routeStack, setRouteStack] = useState<AppRoute[]>(['home']);
  const route = routeStack[routeStack.length - 1];

  const [showPickerDialog, setShowPickerDialog] = useState(false);
  const [pickerInitialTestament, setPickerInitialTestament] = useState<'OT' | 'NT' | null>(null);
  const [showLanguageSheet, setShowLanguageSheet] = useState(false);

  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const toastTimerRef = useRef<number | null>(null);

  const [preferences, setPreferences] = useState<BiblePreferencesState>(() => loadPreferences());
  const prefsRef = useRef(preferences);
  prefsRef.current = preferences;

  const [books, setBooks] = useState<BibleBook[]>([]);
  const [dailyVerse, setDailyVerse] = useState<DailyVerseData | null>(null);
  const [readerState, setReaderState] = useState<ReaderUiState>({
    isLoading: true,
    currentBook: null,
    currentChapter: 1,
    verses: [],
    prevChapterSnapshot: null,
    nextChapterSnapshot: null,
    selectedVerse: null,
    targetHighlightVerse: null,
  });
  const readerRef = useRef(readerState);
  readerRef.current = readerState;

  const [audioState, setAudioState] = useState<AudioPlaybackState>(() =>
    bibleAudioController.getState()
  );

  const [downloadSnap, setDownloadSnap] = useState(() => bibleDownloadManager.getSnapshot());
  const lastDataVersionRef = useRef(downloadSnap.dataVersion);

  const [bookmarks, setBookmarks] = useState<BookmarkEntity[]>(() => loadBookmarks());
  const [highlights, setHighlights] = useState<HighlightEntity[]>(() => loadHighlights());
  const [notes, setNotes] = useState<NoteEntity[]>(() => loadNotes());
  const [history, setHistory] = useState<ReadingHistoryEntity[]>(() => loadHistory());

  const [searchQuery, setSearchQuery] = useState('');
  const [searchTestamentFilter, setSearchTestamentFilter] = useState<'OT' | 'NT' | null>(null);
  const [searchLanguageFilter, setSearchLanguageFilter] = useState<BibleLanguage | null>(null);
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [isSearching, setIsSearching] = useState(false);

  const showToast = useCallback((msg: string) => {
    setToastMessage(msg);
    if (toastTimerRef.current) window.clearTimeout(toastTimerRef.current);
    toastTimerRef.current = window.setTimeout(() => {
      setToastMessage(null);
    }, 2600);
  }, []);

  const navigate = useCallback((to: AppRoute) => {
    setRouteStack((prev) => {
      if (prev[prev.length - 1] === to) return prev;
      return [...prev.slice(-10), to];
    });
  }, []);

  const goBack = useCallback(() => {
    setRouteStack((prev) => {
      if (prev.length <= 1) return ['home'];
      return prev.slice(0, -1);
    });
  }, []);

  const openScripturePicker = useCallback((testament: 'OT' | 'NT' | null = null) => {
    setPickerInitialTestament(testament);
    setShowPickerDialog(true);
  }, []);

  const closeScripturePicker = useCallback(() => setShowPickerDialog(false), []);
  const openLanguageSheet = useCallback(() => setShowLanguageSheet(true), []);
  const closeLanguageSheet = useCallback(() => setShowLanguageSheet(false), []);

  const updatePreferences = useCallback(
    (updater: (prev: BiblePreferencesState) => BiblePreferencesState) => {
      setPreferences((prev) => {
        const next = updater(prev);
        prefsRef.current = next;
        savePreferences(next);
        return next;
      });
    },
    []
  );

  // Sync dark class & system language on DOM & audio controller
  useEffect(() => {
    bibleAudioController.setSystemLanguage(preferences.systemLanguage);
    document.documentElement.classList.toggle('dark', preferences.darkMode || preferences.themeConfig.isDarkSurface);
  }, [preferences.systemLanguage, preferences.darkMode, preferences.themeConfig.isDarkSurface]);

  // Persist user annotations
  useEffect(() => saveBookmarks(bookmarks), [bookmarks]);
  useEffect(() => saveHighlights(highlights), [highlights]);
  useEffect(() => saveNotes(notes), [notes]);
  useEffect(() => saveHistory(history), [history]);

  const recordReadingVisit = useCallback(
    (book: BibleBook, chapter: number, verse: number) => {
      updatePreferences((prev) => ({
        ...prev,
        lastBookId: book.id,
        lastChapter: chapter,
        lastVerse: verse,
      }));
      setHistory((prev) => {
        const filtered = prev.filter((item) => !(item.bookId === book.id && item.chapter === chapter));
        const entry: ReadingHistoryEntity = {
          id: `${book.id}:${chapter}`,
          bookId: book.id,
          chapter,
          verse,
          bookNameEn: book.nameEn,
          bookNameNe: book.nameNe,
          visitedAt: Date.now(),
        };
        return [entry, ...filtered].slice(0, 60);
      });
    },
    [updatePreferences]
  );

  const openChapter = useCallback(
    async (
      bookId: number,
      chapter: number,
      highlightVerse: number | null = null,
      recordHistoryFlag = true
    ) => {
      setReaderState((prev) => ({ ...prev, isLoading: true, selectedVerse: null }));
      const book = (await bibleDb.getBook(bookId)) ?? (await bibleDb.getBook(1));
      const safeChapter = book ? Math.max(1, Math.min(book.chapterCount, chapter)) : 1;
      const verses = book ? await bibleDb.getChapterVerses(book.id, safeChapter) : [];

      if (book && recordHistoryFlag) {
        recordReadingVisit(book, safeChapter, highlightVerse ?? 1);
      }

      const [prevSnapshot, nextSnapshot] = book
        ? await Promise.all([
            loadAdjacentSnapshot(book, safeChapter, false),
            loadAdjacentSnapshot(book, safeChapter, true),
          ])
        : [null, null];

      setReaderState({
        isLoading: false,
        currentBook: book,
        currentChapter: safeChapter,
        verses,
        prevChapterSnapshot: prevSnapshot,
        nextChapterSnapshot: nextSnapshot,
        selectedVerse: null,
        targetHighlightVerse: highlightVerse,
      });
    },
    [recordReadingVisit]
  );

  // Initial DB warmup, subscriptions, and chapter hydration
  useEffect(() => {
    const unsubAudio = bibleAudioController.subscribe(setAudioState);
    const unsubDl = bibleDownloadManager.subscribe(setDownloadSnap);

    let mounted = true;
    (async () => {
      await bibleDownloadManager.initialize(true);
      if (!mounted) return;
      const allBooks = await bibleDb.getAllBooks();
      if (!mounted) return;
      setBooks(allBooks);
      const dv = await bibleDb.getDailyVerse();
      if (mounted) setDailyVerse(dv);

      const p = prefsRef.current;
      await openChapter(p.lastBookId, p.lastChapter, p.lastVerse, false);
    })();

    return () => {
      mounted = false;
      unsubAudio();
      unsubDl();
    };
  }, [openChapter]);

  // Search runner
  const runSearch = useCallback(
    async (q: string, testament: 'OT' | 'NT' | null, lang: BibleLanguage | null) => {
      const trimmed = q.trim();
      if (trimmed.length < 2) {
        setSearchResults([]);
        setIsSearching(false);
        return;
      }
      setIsSearching(true);
      const results = await bibleDb.searchVerses(trimmed, testament, lang, 100);
      setSearchResults(results);
      setIsSearching(false);
    },
    []
  );

  // Refresh reader, daily verse, and active search when packs finish downloading or get deleted
  useEffect(() => {
    if (downloadSnap.dataVersion === lastDataVersionRef.current) return;
    lastDataVersionRef.current = downloadSnap.dataVersion;

    const cur = readerRef.current;
    if (cur.currentBook) {
      const b = cur.currentBook;
      const ch = cur.currentChapter;
      void (async () => {
        const [refreshedVerses, prevSnap, nextSnap] = await Promise.all([
          bibleDb.getChapterVerses(b.id, ch),
          loadAdjacentSnapshot(b, ch, false),
          loadAdjacentSnapshot(b, ch, true),
        ]);
        setReaderState((prev) => ({
          ...prev,
          verses: refreshedVerses,
          prevChapterSnapshot: prevSnap,
          nextChapterSnapshot: nextSnap,
        }));
      })();
    }

    if (!dailyVerse) {
      void bibleDb.getDailyVerse().then((dv) => {
        if (dv) setDailyVerse(dv);
      });
    }

    if (searchQuery.trim().length >= 2) {
      void runSearch(searchQuery, searchTestamentFilter, searchLanguageFilter);
    }
  }, [
    downloadSnap.dataVersion,
    dailyVerse,
    searchQuery,
    searchTestamentFilter,
    searchLanguageFilter,
    runSearch,
  ]);

  const updateSearchQuery = useCallback(
    (q: string) => {
      setSearchQuery(q);
      void runSearch(q, searchTestamentFilter, searchLanguageFilter);
    },
    [runSearch, searchTestamentFilter, searchLanguageFilter]
  );

  const updateSearchFilter = useCallback(
    (t: 'OT' | 'NT' | null) => {
      setSearchTestamentFilter(t);
      void runSearch(searchQuery, t, searchLanguageFilter);
    },
    [runSearch, searchQuery, searchLanguageFilter]
  );

  const updateSearchLanguageFilter = useCallback(
    (l: BibleLanguage | null) => {
      setSearchLanguageFilter(l);
      void runSearch(searchQuery, searchTestamentFilter, l);
    },
    [runSearch, searchQuery, searchTestamentFilter]
  );

  const goToNextChapter = useCallback(() => {
    const next = readerRef.current.nextChapterSnapshot;
    if (next) void openChapter(next.book.id, next.chapter, null);
  }, [openChapter]);

  const goToPreviousChapter = useCallback(() => {
    const prev = readerRef.current.prevChapterSnapshot;
    if (prev) void openChapter(prev.book.id, prev.chapter, null);
  }, [openChapter]);

  const clearTargetHighlight = useCallback(() => {
    setReaderState((prev) => ({ ...prev, targetHighlightVerse: null }));
  }, []);

  const savePageProgress = useCallback(
    (firstVerse: number) => {
      const book = readerRef.current.currentBook;
      if (!book) return;
      recordReadingVisit(book, readerRef.current.currentChapter, firstVerse);
    },
    [recordReadingVisit]
  );

  const selectVerse = useCallback(
    (verse: BibleVerse | null) => {
      setReaderState((prev) => ({
        ...prev,
        selectedVerse: prev.selectedVerse?.key === verse?.key ? null : verse,
      }));
      const book = readerRef.current.currentBook;
      if (book && verse) {
        recordReadingVisit(book, verse.chapter, verse.verse);
      }
    },
    [recordReadingVisit]
  );

  const loadChapterVerseCount = useCallback(
    (bookId: number, chapter: number) => bibleDb.getChapterVerseCount(bookId, chapter),
    []
  );

  const bookmarkedKeys = useMemo(
    () => new Set(bookmarks.map((b) => b.verseKey)),
    [bookmarks]
  );

  const highlightMap = useMemo(() => {
    const m = new Map<string, HighlightColor>();
    for (const h of highlights) m.set(h.verseKey, h.colorKey);
    return m;
  }, [highlights]);

  const notesMap = useMemo(() => {
    const m = new Map<string, NoteEntity>();
    for (const n of notes) m.set(n.verseKey, n);
    return m;
  }, [notes]);

  const toggleBookmark = useCallback(
    (verse: BibleVerse) => {
      const book = readerRef.current.currentBook;
      if (!book) return;
      const preferOncv = isReaderPreferringOncv(prefsRef.current);
      setBookmarks((prev) => {
        if (prev.some((b) => b.verseKey === verse.key)) {
          return prev.filter((b) => b.verseKey !== verse.key);
        }
        const snippetNe = preferOncv
          ? verse.textOncv.trim() || verse.textNe
          : verse.textNe.trim() || verse.textOncv;
        const entity: BookmarkEntity = {
          verseKey: verse.key,
          bookId: book.id,
          chapter: verse.chapter,
          verse: verse.verse,
          bookNameEn: book.nameEn,
          bookNameNe: book.nameNe,
          snippetEn: verse.textEn.slice(0, 140),
          snippetNe: snippetNe.slice(0, 140),
          createdAt: Date.now(),
        };
        return [entity, ...prev];
      });
    },
    []
  );

  const removeBookmark = useCallback((verseKey: string) => {
    setBookmarks((prev) => prev.filter((b) => b.verseKey !== verseKey));
  }, []);

  const setHighlight = useCallback((verse: BibleVerse, color: HighlightColor) => {
    const book = readerRef.current.currentBook;
    if (!book) return;
    const preferOncv = isReaderPreferringOncv(prefsRef.current);
    setHighlights((prev) => {
      const filtered = prev.filter((h) => h.verseKey !== verse.key);
      if (color === HighlightColor.NONE) return filtered;
      const snippetNe = preferOncv
        ? verse.textOncv.trim() || verse.textNe
        : verse.textNe.trim() || verse.textOncv;
      const entity: HighlightEntity = {
        verseKey: verse.key,
        bookId: book.id,
        chapter: verse.chapter,
        verse: verse.verse,
        bookNameEn: book.nameEn,
        bookNameNe: book.nameNe,
        colorKey: color,
        snippetEn: verse.textEn.slice(0, 140),
        snippetNe: snippetNe.slice(0, 140),
        updatedAt: Date.now(),
      };
      return [entity, ...filtered];
    });
  }, []);

  const saveNote = useCallback((verse: BibleVerse, noteText: string) => {
    const book = readerRef.current.currentBook;
    if (!book) return;
    const trimmed = noteText.trim();
    setNotes((prev) => {
      const filtered = prev.filter((n) => n.verseKey !== verse.key);
      if (!trimmed) return filtered;
      const entity: NoteEntity = {
        verseKey: verse.key,
        bookId: book.id,
        chapter: verse.chapter,
        verse: verse.verse,
        bookNameEn: book.nameEn,
        bookNameNe: book.nameNe,
        noteText: trimmed,
        updatedAt: Date.now(),
      };
      return [entity, ...filtered];
    });
  }, []);

  const deleteNote = useCallback((verseKey: string) => {
    setNotes((prev) => prev.filter((n) => n.verseKey !== verseKey));
  }, []);

  const clearHistory = useCallback(() => {
    setHistory([]);
  }, []);

  // Audio controls
  const startAudioFromVerse = useCallback((startVerse = 1) => {
    const cur = readerRef.current;
    if (!cur.currentBook || cur.verses.length === 0) return;
    const prefs = prefsRef.current;
    bibleAudioController.startChapterAudio(
      cur.currentBook,
      cur.verses,
      startVerse,
      resolveEffectiveAudioLanguage(prefs),
      prefs.audioSpeed,
      isReaderPreferringOncv(prefs)
    );
  }, []);

  const startAudioFromHome = useCallback(() => {
    const cur = readerRef.current;
    if (cur.currentBook && cur.verses.length > 0) {
      startAudioFromVerse(1);
      return;
    }
    void (async () => {
      const prefs = prefsRef.current;
      const targetBook =
        (await bibleDb.getBook(prefs.lastBookId)) ?? (await bibleDb.getBook(1));
      if (!targetBook) return;
      const verses = await bibleDb.getChapterVerses(targetBook.id, prefs.lastChapter);
      bibleAudioController.startChapterAudio(
        targetBook,
        verses,
        prefs.lastVerse,
        resolveEffectiveAudioLanguage(prefs),
        prefs.audioSpeed,
        isReaderPreferringOncv(prefs)
      );
    })();
  }, [startAudioFromVerse]);

  const toggleAudioPlayPause = useCallback(() => bibleAudioController.togglePlayPause(), []);
  const audioNextVerse = useCallback(() => bibleAudioController.nextVerse(), []);
  const audioPrevVerse = useCallback(() => bibleAudioController.previousVerse(), []);
  const stopAudio = useCallback(() => bibleAudioController.stopAudio(), []);
  const refreshAudioVoices = useCallback(() => bibleAudioController.detectVoices(), []);

  const setAudioLanguage = useCallback(
    (lang: AudioLanguage) => {
      bibleAudioController.setLanguage(lang);
      updatePreferences((p) => ({ ...p, audioLanguage: lang }));
    },
    [updatePreferences]
  );

  const setAudioSpeed = useCallback(
    (speed: number) => {
      bibleAudioController.setSpeed(speed);
      updatePreferences((p) => ({ ...p, audioSpeed: speed }));
    },
    [updatePreferences]
  );

  // Theme & Settings
  const setDarkMode = useCallback(
    (enabled: boolean) => {
      updatePreferences((prev) => {
        const tc = prev.themeConfig;
        let nextPreset = tc.preset;
        if (enabled && tc.preset === ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS) {
          nextPreset = ThemePreset.CRYSTAL_CLEAR_DARK_GLASS;
        } else if (!enabled && tc.preset === ThemePreset.CRYSTAL_CLEAR_DARK_GLASS) {
          nextPreset = ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS;
        } else if (
          enabled &&
          (tc.preset === ThemePreset.IOS_LIQUID_GLASS || tc.preset === ThemePreset.LIGHT_GLASS)
        ) {
          nextPreset = ThemePreset.DARK_GLASS;
        } else if (!enabled && tc.preset === ThemePreset.DARK_GLASS) {
          nextPreset = ThemePreset.IOS_LIQUID_GLASS;
        }

        let nextReadingBg = tc.readingPageBackground;
        if (enabled && tc.readingPageBackground === ReadingPageBackground.CREAM_PARCHMENT) {
          nextReadingBg = ReadingPageBackground.NIGHT_OBSIDIAN;
        } else if (!enabled && tc.readingPageBackground === ReadingPageBackground.NIGHT_OBSIDIAN) {
          nextReadingBg = ReadingPageBackground.CREAM_PARCHMENT;
        }

        const baseTheme =
          nextPreset !== tc.preset
            ? {
                ...createThemeConfigFromPreset(nextPreset),
                fontStyle: tc.fontStyle,
                buttonStyle: tc.buttonStyle,
                navBarAppearance: tc.navBarAppearance,
              }
            : tc;

        return {
          ...prev,
          darkMode: enabled,
          themeConfig: {
            ...baseTheme,
            isDarkSurface: enabled,
            readingPageBackground: nextReadingBg,
          },
        };
      });
    },
    [updatePreferences]
  );

  const saveThemeConfig = useCallback(
    (config: AppThemeConfig) => {
      updatePreferences((prev) => ({
        ...prev,
        darkMode: config.isDarkSurface,
        themeConfig: config,
      }));
    },
    [updatePreferences]
  );

  const applyThemePreset = useCallback(
    (preset: ThemePreset) => {
      updatePreferences((prev) => {
        const next = {
          ...createThemeConfigFromPreset(preset),
          fontStyle: prev.themeConfig.fontStyle,
        };
        return {
          ...prev,
          darkMode: next.isDarkSurface,
          themeConfig: next,
        };
      });
    },
    [updatePreferences]
  );

  const setFontSize = useCallback(
    (sp: number) => {
      updatePreferences((prev) => ({ ...prev, fontSizeSp: sp }));
    },
    [updatePreferences]
  );

  const setPageSoundEnabled = useCallback(
    (enabled: boolean) => {
      updatePreferences((prev) => ({
        ...prev,
        pageSoundEnabled: enabled,
        pageSoundStyle:
          enabled && prev.pageSoundStyle === PageTurnSoundStyle.SILENT
            ? PageTurnSoundStyle.REALISTIC_BIBLE
            : prev.pageSoundStyle,
      }));
    },
    [updatePreferences]
  );

  const setPageSoundStyle = useCallback(
    (style: PageTurnSoundStyle) => {
      updatePreferences((prev) => ({
        ...prev,
        pageSoundStyle: style,
        pageSoundEnabled:
          style !== PageTurnSoundStyle.SILENT ? true : prev.pageSoundEnabled,
      }));
    },
    [updatePreferences]
  );

  const setPageSoundVolume = useCallback(
    (vol: number) => {
      const clamped = Math.max(0, Math.min(1, vol));
      updatePreferences((prev) => ({ ...prev, pageSoundVolume: clamped }));
    },
    [updatePreferences]
  );

  const previewPageSound = useCallback(
    (style: PageTurnSoundStyle, forward = true) => {
      paperSoundPlayer.preview(style, prefsRef.current.pageSoundVolume, forward);
    },
    []
  );

  const playForwardSound = useCallback(() => {
    const p = prefsRef.current;
    paperSoundPlayer.playForward(p.pageSoundEnabled, p.pageSoundStyle, p.pageSoundVolume);
  }, []);

  const playBackwardSound = useCallback(() => {
    const p = prefsRef.current;
    paperSoundPlayer.playBackward(p.pageSoundEnabled, p.pageSoundStyle, p.pageSoundVolume);
  }, []);

  const setReadingMode = useCallback(
    (mode: ReadingMode) => {
      updatePreferences((prev) => ({ ...prev, readingMode: mode }));
    },
    [updatePreferences]
  );

  const setPrimaryLanguage = useCallback(
    (lang: BibleLanguage) => {
      updatePreferences((prev) => ({ ...prev, primaryLanguage: lang }));
    },
    [updatePreferences]
  );

  const setSecondaryLanguage = useCallback(
    (lang: BibleLanguage) => {
      updatePreferences((prev) => ({ ...prev, secondaryLanguage: lang }));
    },
    [updatePreferences]
  );

  const swapLanguages = useCallback(() => {
    updatePreferences((prev) => ({
      ...prev,
      primaryLanguage: prev.secondaryLanguage,
      secondaryLanguage: prev.primaryLanguage,
    }));
  }, [updatePreferences]);

  const setSystemLanguage = useCallback(
    (language: SystemLanguage) => {
      updatePreferences((prev) => ({ ...prev, systemLanguage: language }));
    },
    [updatePreferences]
  );

  const tokens = useMemo(
    () => resolveLiquidGlassTokens(preferences.themeConfig),
    [preferences.themeConfig]
  );
  const palette = useMemo(() => resolveBiblePalette(tokens), [tokens]);
  const strings = useMemo(
    () => getAppStrings(preferences.systemLanguage),
    [preferences.systemLanguage]
  );

  const value: BibleAppContextValue = {
    route,
    navigate,
    goBack,
    showPickerDialog,
    pickerInitialTestament,
    openScripturePicker,
    closeScripturePicker,
    showLanguageSheet,
    openLanguageSheet,
    closeLanguageSheet,
    toastMessage,
    showToast,
    preferences,
    tokens,
    palette,
    strings,
    books,
    dailyVerse,
    readerState,
    audioState,
    languageSummaries: downloadSnap.languageSummaries,
    bookDownloadStates: downloadSnap.bookStates,
    downloadedBookKeys: downloadSnap.downloadedBookKeys,
    totalStorageBytes: downloadSnap.totalStorageBytes,
    bookmarks,
    highlights,
    notes,
    history,
    bookmarkedKeys,
    highlightMap,
    notesMap,
    searchQuery,
    searchTestamentFilter,
    searchLanguageFilter,
    searchResults,
    isSearching,
    updateSearchQuery,
    updateSearchFilter,
    updateSearchLanguageFilter,
    openChapter,
    goToNextChapter,
    goToPreviousChapter,
    clearTargetHighlight,
    savePageProgress,
    selectVerse,
    loadChapterVerseCount,
    toggleBookmark,
    removeBookmark,
    setHighlight,
    saveNote,
    deleteNote,
    clearHistory,
    startAudioFromVerse,
    startAudioFromHome,
    toggleAudioPlayPause,
    audioNextVerse,
    audioPrevVerse,
    stopAudio,
    setAudioLanguage,
    setAudioSpeed,
    refreshAudioVoices,
    setDarkMode,
    saveThemeConfig,
    applyThemePreset,
    setFontSize,
    setPageSoundEnabled,
    setPageSoundStyle,
    setPageSoundVolume,
    previewPageSound,
    playForwardSound,
    playBackwardSound,
    setReadingMode,
    setPrimaryLanguage,
    setSecondaryLanguage,
    swapLanguages,
    setSystemLanguage,
    downloadCompleteBible: () => void bibleDownloadManager.downloadCompleteBible(),
    downloadLanguage: (l) => void bibleDownloadManager.downloadLanguage(l),
    downloadBook: (l, b) => void bibleDownloadManager.downloadBook(l, b),
    pauseLanguageDownload: (l) => void bibleDownloadManager.pauseLanguage(l),
    pauseAllDownloads: () => void bibleDownloadManager.pauseAll(),
    resumeLanguageDownload: (l) => void bibleDownloadManager.resumeLanguage(l),
    resumeAllDownloads: () => void bibleDownloadManager.resumeAll(),
    cancelLanguageDownload: (l) => void bibleDownloadManager.cancelLanguage(l),
    cancelBookDownload: (l, b) => void bibleDownloadManager.cancelBook(l, b),
    deleteLanguageDownload: (l) => void bibleDownloadManager.deleteLanguage(l),
    deleteBookDownload: (l, b) => void bibleDownloadManager.deleteBook(l, b),
  };

  return <BibleAppContext.Provider value={value}>{children}</BibleAppContext.Provider>;
};

export function useBibleApp(): BibleAppContextValue {
  const ctx = useContext(BibleAppContext);
  if (!ctx) throw new Error('useBibleApp must be used inside BibleAppProvider');
  return ctx;
}
