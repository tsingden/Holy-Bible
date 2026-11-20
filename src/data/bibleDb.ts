import { gunzipSync } from 'fflate';
import {
  AudioLanguage,
  BIBLE_LANGUAGE_META,
  BibleBook,
  BibleLanguage,
  BiblePreferencesState,
  BibleVerse,
  BookDownloadState,
  BookmarkEntity,
  DailyVerseData,
  DownloadStatus,
  HighlightColor,
  HighlightEntity,
  NoteEntity,
  PageTurnSoundStyle,
  ReadingHistoryEntity,
  ReadingMode,
  SearchResult,
  SystemLanguage,
  ThemePreset,
  createThemeConfigFromPreset,
} from '../types/bible';

const IDB_NAME = 'holy_bible_offline_db';
const IDB_VERSION = 1;
const STORE_BOOKS = 'books';
const STORE_PACK_BOOKS = 'pack_books';
const STORE_DOWNLOAD_STATES = 'download_states';

const LS_PREFS = 'holy_bible_prefs_v1';
const LS_BOOKMARKS = 'holy_bible_bookmarks_v1';
const LS_HIGHLIGHTS = 'holy_bible_highlights_v1';
const LS_NOTES = 'holy_bible_notes_v1';
const LS_HISTORY = 'holy_bible_history_v1';

const DAILY_VERSE_COORDS: Array<[number, number, number]> = [
  [43, 3, 16], // John 3:16
  [19, 23, 1], // Psalm 23:1
  [20, 3, 5], // Proverbs 3:5
  [50, 4, 13], // Philippians 4:13
  [45, 8, 28], // Romans 8:28
  [6, 1, 9], // Joshua 1:9
  [23, 41, 10], // Isaiah 41:10
  [40, 11, 28], // Matthew 11:28
  [19, 119, 105], // Psalm 119:105
  [46, 13, 4], // 1 Corinthians 13:4
  [24, 29, 11], // Jeremiah 29:11
  [19, 46, 1], // Psalm 46:1
  [1, 1, 1], // Genesis 1:1
  [40, 5, 14], // Matthew 5:14
  [19, 121, 1], // Psalm 121:1
  [45, 12, 2], // Romans 12:2
  [50, 4, 6], // Philippians 4:6
  [23, 40, 31], // Isaiah 40:31
  [43, 14, 6], // John 14:6
  [48, 5, 22], // Galatians 5:22
  [58, 11, 1], // Hebrews 11:1
  [19, 27, 1], // Psalm 27:1
  [40, 6, 33], // Matthew 6:33
  [43, 1, 1], // John 1:1
];

interface StoredBookVerses {
  key: string; // `${languageCode}:${bookId}`
  languageCode: string;
  bookId: number;
  bytesSize: number;
  chapters: Record<number, Record<number, { text: string; norm: string }>>;
}

interface StoredDownloadRecord {
  key: string; // `${languageCode}:${bookId}`
  languageCode: string;
  bookId: number;
  status: DownloadStatus;
  bytesSize: number;
  updatedAt: number;
}

type ParsedPackByBook = Map<
  number,
  {
    bytesSize: number;
    chapters: Record<number, Record<number, { text: string; norm: string }>>;
  }
>;

function normalizeSearchText(raw: string): string {
  return raw
    .trim()
    .toLowerCase()
    .replace(/\u200d/g, '')
    .replace(/\u200c/g, '')
    .normalize('NFC');
}

const utf8Encoder = new TextEncoder();
const utf8Decoder = new TextDecoder('utf-8');

class BibleIndexedDb {
  private dbPromise: Promise<IDBDatabase> | null = null;
  private booksCache: BibleBook[] | null = null;
  private parsedPackCache = new Map<string, ParsedPackByBook>();

  private open(): Promise<IDBDatabase> {
    if (this.dbPromise) return this.dbPromise;
    this.dbPromise = new Promise((resolve, reject) => {
      const req = indexedDB.open(IDB_NAME, IDB_VERSION);
      req.onupgradeneeded = () => {
        const db = req.result;
        if (!db.objectStoreNames.contains(STORE_BOOKS)) {
          db.createObjectStore(STORE_BOOKS, { keyPath: 'id' });
        }
        if (!db.objectStoreNames.contains(STORE_PACK_BOOKS)) {
          const store = db.createObjectStore(STORE_PACK_BOOKS, { keyPath: 'key' });
          store.createIndex('languageCode', 'languageCode', { unique: false });
          store.createIndex('bookId', 'bookId', { unique: false });
        }
        if (!db.objectStoreNames.contains(STORE_DOWNLOAD_STATES)) {
          db.createObjectStore(STORE_DOWNLOAD_STATES, { keyPath: 'key' });
        }
      };
      req.onsuccess = () => resolve(req.result);
      req.onerror = () => reject(req.error);
    });
    return this.dbPromise;
  }

  private async withStore<T>(
    storeName: string,
    mode: IDBTransactionMode,
    run: (store: IDBObjectStore) => IDBRequest<T>
  ): Promise<T> {
    const db = await this.open();
    return new Promise((resolve, reject) => {
      const tx = db.transaction(storeName, mode);
      const store = tx.objectStore(storeName);
      const req = run(store);
      req.onsuccess = () => resolve(req.result);
      req.onerror = () => reject(req.error);
    });
  }

  async warmup(): Promise<void> {
    await this.getAllBooks();
  }

  async getAllBooks(): Promise<BibleBook[]> {
    if (this.booksCache && this.booksCache.length === 66) {
      return this.booksCache;
    }

    const existing = await this.withStore<BibleBook[]>(STORE_BOOKS, 'readonly', (s) => s.getAll());
    if (existing && existing.length === 66) {
      const sorted = [...existing].sort((a, b) => a.id - b.id);
      this.booksCache = sorted;
      return sorted;
    }

    const res = await fetch('/data/bible_books_meta.dat');
    if (!res.ok) throw new Error('Failed to fetch bible_books_meta.dat');
    const compressed = new Uint8Array(await res.arrayBuffer());
    const decompressed = gunzipSync(compressed);
    const jsonStr = utf8Decoder.decode(decompressed);
    const parsed = JSON.parse(jsonStr) as Array<{
      id: number;
      testament: 'OT' | 'NT';
      abbrev: string;
      nameEn: string;
      nameNe: string;
      nameKo?: string;
      chapterCount: number;
    }>;

    const books: BibleBook[] = parsed
      .map((item) => ({
        id: item.id,
        testament: item.testament,
        abbrev: item.abbrev,
        nameEn: item.nameEn,
        nameNe: item.nameNe,
        nameKo: item.nameKo ?? '',
        chapterCount: item.chapterCount,
      }))
      .sort((a, b) => a.id - b.id);

    const db = await this.open();
    await new Promise<void>((resolve, reject) => {
      const tx = db.transaction(STORE_BOOKS, 'readwrite');
      const store = tx.objectStore(STORE_BOOKS);
      for (const book of books) {
        store.put(book);
      }
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error);
    });

    this.booksCache = books;
    return books;
  }

  async getBook(bookId: number): Promise<BibleBook | null> {
    const all = await this.getAllBooks();
    return all.find((b) => b.id === bookId) ?? null;
  }

  private async loadParsedPack(lang: BibleLanguage): Promise<ParsedPackByBook> {
    const meta = BIBLE_LANGUAGE_META[lang];
    const cached = this.parsedPackCache.get(meta.code);
    if (cached) return cached;

    const res = await fetch(meta.packFile);
    if (!res.ok) throw new Error(`Failed to load language pack: ${meta.packFile}`);
    const compressed = new Uint8Array(await res.arrayBuffer());
    const decompressed = gunzipSync(compressed);
    const tsv = utf8Decoder.decode(decompressed);

    const byBook: ParsedPackByBook = new Map();
    let start = 0;
    const len = tsv.length;

    while (start < len) {
      let end = tsv.indexOf('\n', start);
      if (end === -1) end = len;
      const line = tsv.slice(start, end).replace(/\r$/, '');
      start = end + 1;
      if (!line) continue;

      const t1 = line.indexOf('\t');
      if (t1 === -1) continue;
      const t2 = line.indexOf('\t', t1 + 1);
      if (t2 === -1) continue;
      const t3 = line.indexOf('\t', t2 + 1);
      if (t3 === -1) continue;

      const bookId = Number(line.slice(0, t1));
      const chapter = Number(line.slice(t1 + 1, t2));
      const verse = Number(line.slice(t2 + 1, t3));
      const text = line.slice(t3 + 1);

      if (!bookId || !chapter || !verse || !text.trim()) continue;

      let entry = byBook.get(bookId);
      if (!entry) {
        entry = { bytesSize: 0, chapters: {} };
        byBook.set(bookId, entry);
      }
      if (!entry.chapters[chapter]) {
        entry.chapters[chapter] = {};
      }
      entry.chapters[chapter][verse] = {
        text,
        norm: normalizeSearchText(text),
      };
      entry.bytesSize += utf8Encoder.encode(text).length;
    }

    this.parsedPackCache.set(meta.code, byBook);
    return byBook;
  }

  async installBooksFromPack(
    lang: BibleLanguage,
    bookIds: number[]
  ): Promise<Record<number, number>> {
    const code = BIBLE_LANGUAGE_META[lang].code;
    const pack = await this.loadParsedPack(lang);
    const db = await this.open();
    const byteSizes: Record<number, number> = {};

    await new Promise<void>((resolve, reject) => {
      const tx = db.transaction(STORE_PACK_BOOKS, 'readwrite');
      const store = tx.objectStore(STORE_PACK_BOOKS);
      for (const bookId of bookIds) {
        const entry = pack.get(bookId);
        if (!entry) {
          byteSizes[bookId] = 0;
          continue;
        }
        byteSizes[bookId] = entry.bytesSize;
        const record: StoredBookVerses = {
          key: `${code}:${bookId}`,
          languageCode: code,
          bookId,
          bytesSize: entry.bytesSize,
          chapters: entry.chapters,
        };
        store.put(record);
      }
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error);
    });

    return byteSizes;
  }

  async installBookFromPack(lang: BibleLanguage, bookId: number): Promise<number> {
    const sizes = await this.installBooksFromPack(lang, [bookId]);
    return sizes[bookId] ?? 0;
  }

  async removeBook(lang: BibleLanguage, bookId: number): Promise<void> {
    const code = BIBLE_LANGUAGE_META[lang].code;
    await this.withStore(STORE_PACK_BOOKS, 'readwrite', (s) => s.delete(`${code}:${bookId}`));
  }

  async removeLanguage(lang: BibleLanguage): Promise<void> {
    const code = BIBLE_LANGUAGE_META[lang].code;
    const db = await this.open();
    await new Promise<void>((resolve, reject) => {
      const tx = db.transaction(STORE_PACK_BOOKS, 'readwrite');
      const store = tx.objectStore(STORE_PACK_BOOKS);
      const index = store.index('languageCode');
      const req = index.getAllKeys(IDBKeyRange.only(code));
      req.onsuccess = () => {
        for (const k of req.result) {
          store.delete(k);
        }
      };
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error);
    });
  }

  private async getBookVersesRecord(
    langCode: string,
    bookId: number
  ): Promise<StoredBookVerses | undefined> {
    return this.withStore<StoredBookVerses | undefined>(STORE_PACK_BOOKS, 'readonly', (s) =>
      s.get(`${langCode}:${bookId}`)
    );
  }

  async getChapterVerses(bookId: number, chapter: number): Promise<BibleVerse[]> {
    const [enRec, neRec, oncvRec, koRec] = await Promise.all([
      this.getBookVersesRecord('en', bookId),
      this.getBookVersesRecord('ne', bookId),
      this.getBookVersesRecord('ne_oncv', bookId),
      this.getBookVersesRecord('ko', bookId),
    ]);

    const enCh = enRec?.chapters[chapter] ?? {};
    const neCh = neRec?.chapters[chapter] ?? {};
    const oncvCh = oncvRec?.chapters[chapter] ?? {};
    const koCh = koRec?.chapters[chapter] ?? {};

    const verseNums = new Set<number>();
    for (const v of Object.keys(enCh)) verseNums.add(Number(v));
    for (const v of Object.keys(neCh)) verseNums.add(Number(v));
    for (const v of Object.keys(oncvCh)) verseNums.add(Number(v));
    for (const v of Object.keys(koCh)) verseNums.add(Number(v));

    const sortedNums = Array.from(verseNums).sort((a, b) => a - b);
    return sortedNums.map((vNum) => ({
      id: bookId * 1_000_000 + chapter * 1_000 + vNum,
      bookId,
      chapter,
      verse: vNum,
      key: `${bookId}:${chapter}:${vNum}`,
      textEn: enCh[vNum]?.text ?? '',
      textNe: neCh[vNum]?.text ?? '',
      textOncv: oncvCh[vNum]?.text ?? '',
      textKo: koCh[vNum]?.text ?? '',
    }));
  }

  async getChapterVerseCount(bookId: number, chapter: number): Promise<number> {
    const verses = await this.getChapterVerses(bookId, chapter);
    return verses.length;
  }

  async getDailyVerse(): Promise<DailyVerseData | null> {
    const now = new Date();
    const startOfYear = new Date(now.getFullYear(), 0, 0);
    const diff = now.getTime() - startOfYear.getTime();
    const dayOfYear = Math.floor(diff / (1000 * 60 * 60 * 24));
    const idx = ((dayOfYear % DAILY_VERSE_COORDS.length) + DAILY_VERSE_COORDS.length) % DAILY_VERSE_COORDS.length;
    const [bId, ch, v] = DAILY_VERSE_COORDS[idx];

    const book = await this.getBook(bId);
    if (!book) return null;
    const chapterVerses = await this.getChapterVerses(bId, ch);
    const match = chapterVerses.find((item) => item.verse === v) ?? chapterVerses[0];
    if (!match) return null;
    return { book, verse: match };
  }

  async searchVerses(
    rawQuery: string,
    testamentFilter?: 'OT' | 'NT' | null,
    languageFilter?: BibleLanguage | null,
    limit = 100
  ): Promise<SearchResult[]> {
    const normQuery = normalizeSearchText(rawQuery);
    if (normQuery.length < 2) return [];

    const books = await this.getAllBooks();
    const bookMap = new Map<number, BibleBook>();
    for (const b of books) bookMap.set(b.id, b);

    const allPackRecords = await this.withStore<StoredBookVerses[]>(
      STORE_PACK_BOOKS,
      'readonly',
      (s) => s.getAll()
    );

    const byKey = new Map<string, StoredBookVerses>();
    for (const r of allPackRecords) {
      byKey.set(r.key, r);
    }

    const activeLangCodes = languageFilter
      ? [BIBLE_LANGUAGE_META[languageFilter].code]
      : ['en', 'ne', 'ne_oncv', 'ko'];

    const grouped = new Map<
      string,
      {
        bookId: number;
        chapter: number;
        verse: number;
        matchedLanguage: BibleLanguage;
        texts: Record<string, string>;
      }
    >();

    for (const rec of allPackRecords) {
      if (!activeLangCodes.includes(rec.languageCode)) continue;
      const book = bookMap.get(rec.bookId);
      if (!book) continue;
      if (testamentFilter && book.testament !== testamentFilter) continue;

      const bookNameMatched =
        normalizeSearchText(book.nameEn).includes(normQuery) ||
        normalizeSearchText(book.nameNe).includes(normQuery) ||
        normalizeSearchText(book.nameKo).includes(normQuery);

      for (const [chKey, versesObj] of Object.entries(rec.chapters)) {
        const chapter = Number(chKey);
        for (const [vKey, cell] of Object.entries(versesObj)) {
          const verse = Number(vKey);
          const textMatched = cell.norm.includes(normQuery);
          if (!textMatched && !bookNameMatched) continue;

          const coordKey = `${rec.bookId}:${chapter}:${verse}`;
          if (!grouped.has(coordKey)) {
            const matchedLang: BibleLanguage =
              rec.languageCode === 'en'
                ? BibleLanguage.ENGLISH
                : rec.languageCode === 'ne'
                ? BibleLanguage.NEPALI
                : rec.languageCode === 'ne_oncv'
                ? BibleLanguage.ONCV
                : BibleLanguage.KOREAN;

            grouped.set(coordKey, {
              bookId: rec.bookId,
              chapter,
              verse,
              matchedLanguage: matchedLang,
              texts: {
                en: byKey.get(`en:${rec.bookId}`)?.chapters[chapter]?.[verse]?.text ?? '',
                ne: byKey.get(`ne:${rec.bookId}`)?.chapters[chapter]?.[verse]?.text ?? '',
                ne_oncv: byKey.get(`ne_oncv:${rec.bookId}`)?.chapters[chapter]?.[verse]?.text ?? '',
                ko: byKey.get(`ko:${rec.bookId}`)?.chapters[chapter]?.[verse]?.text ?? '',
              },
            });
          }

          if (grouped.size >= limit && !bookNameMatched) {
            break;
          }
        }
        if (grouped.size >= limit) break;
      }
      if (grouped.size >= limit) break;
    }

    const results: SearchResult[] = [];
    for (const item of grouped.values()) {
      const book = bookMap.get(item.bookId);
      if (!book) continue;
      results.push({
        bookId: item.bookId,
        bookNameEn: book.nameEn,
        bookNameNe: book.nameNe,
        testament: book.testament,
        chapter: item.chapter,
        verse: item.verse,
        matchedLanguage: item.matchedLanguage,
        textEn: item.texts.en,
        textNe: item.texts.ne,
        textOncv: item.texts.ne_oncv,
        textKo: item.texts.ko,
      });
    }

    results.sort((a, b) => a.bookId - b.bookId || a.chapter - b.chapter || a.verse - b.verse);
    return results.slice(0, limit);
  }

  async getDownloadedBooksOnce(): Promise<BookDownloadState[]> {
    const records = await this.withStore<StoredDownloadRecord[]>(
      STORE_DOWNLOAD_STATES,
      'readonly',
      (s) => s.getAll()
    );
    return records.map((r) => ({
      languageCode: r.languageCode,
      bookId: r.bookId,
      status: r.status,
      bytesSize: r.bytesSize,
    }));
  }

  async upsertDownloadedBooks(items: BookDownloadState[]): Promise<void> {
    const db = await this.open();
    const now = Date.now();
    await new Promise<void>((resolve, reject) => {
      const tx = db.transaction(STORE_DOWNLOAD_STATES, 'readwrite');
      const store = tx.objectStore(STORE_DOWNLOAD_STATES);
      for (const item of items) {
        const rec: StoredDownloadRecord = {
          key: `${item.languageCode}:${item.bookId}`,
          languageCode: item.languageCode,
          bookId: item.bookId,
          status: item.status,
          bytesSize: item.bytesSize,
          updatedAt: now,
        };
        store.put(rec);
      }
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error);
    });
  }

  async deleteDownloadedBook(languageCode: string, bookId: number): Promise<void> {
    await this.withStore(STORE_DOWNLOAD_STATES, 'readwrite', (s) =>
      s.delete(`${languageCode}:${bookId}`)
    );
  }

  async deleteDownloadedLanguage(languageCode: string): Promise<void> {
    const all = await this.getDownloadedBooksOnce();
    const targets = all.filter((x) => x.languageCode === languageCode);
    const db = await this.open();
    await new Promise<void>((resolve, reject) => {
      const tx = db.transaction(STORE_DOWNLOAD_STATES, 'readwrite');
      const store = tx.objectStore(STORE_DOWNLOAD_STATES);
      for (const t of targets) {
        store.delete(`${t.languageCode}:${t.bookId}`);
      }
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error);
    });
  }

  async getTotalStorageBytes(): Promise<number> {
    const all = await this.withStore<StoredBookVerses[]>(STORE_PACK_BOOKS, 'readonly', (s) =>
      s.getAll()
    );
    let total = 0;
    for (const r of all) {
      total += r.bytesSize || 0;
    }
    return total;
  }
}

export const bibleDb = new BibleIndexedDb();

// LocalStorage User Data & Preferences Persistence
export const DEFAULT_PREFERENCES: BiblePreferencesState = {
  darkMode: false,
  fontSizeSp: 16.5,
  pageSoundEnabled: true,
  pageSoundStyle: PageTurnSoundStyle.REALISTIC_BIBLE,
  pageSoundVolume: 0.72,
  readingMode: ReadingMode.BILINGUAL_SPREAD,
  primaryLanguage: BibleLanguage.ENGLISH,
  secondaryLanguage: BibleLanguage.NEPALI,
  audioLanguage: AudioLanguage.BILINGUAL,
  audioSpeed: 1.0,
  lastBookId: 1,
  lastChapter: 1,
  lastVerse: 1,
  themeConfig: createThemeConfigFromPreset(ThemePreset.CLASSIC_BIBLE),
  systemLanguage: SystemLanguage.ENGLISH,
};

export function loadPreferences(): BiblePreferencesState {
  try {
    const raw = localStorage.getItem(LS_PREFS);
    if (!raw) return DEFAULT_PREFERENCES;
    const parsed = JSON.parse(raw) as Partial<BiblePreferencesState>;
    const preset = parsed.themeConfig?.preset ?? ThemePreset.CLASSIC_BIBLE;
    const baseTheme = createThemeConfigFromPreset(
      Object.values(ThemePreset).includes(preset) ? preset : ThemePreset.CLASSIC_BIBLE
    );
    return {
      ...DEFAULT_PREFERENCES,
      ...parsed,
      themeConfig: {
        ...baseTheme,
        ...(parsed.themeConfig ?? {}),
      },
    };
  } catch {
    return DEFAULT_PREFERENCES;
  }
}

export function savePreferences(prefs: BiblePreferencesState): void {
  try {
    localStorage.setItem(LS_PREFS, JSON.stringify(prefs));
  } catch {
    // Ignore storage quota errors
  }
}

function loadJsonList<T>(key: string): T[] {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function saveJsonList<T>(key: string, items: T[]): void {
  try {
    localStorage.setItem(key, JSON.stringify(items));
  } catch {
    // Ignore
  }
}

export function loadBookmarks(): BookmarkEntity[] {
  return loadJsonList<BookmarkEntity>(LS_BOOKMARKS).sort((a, b) => b.createdAt - a.createdAt);
}

export function saveBookmarks(items: BookmarkEntity[]): void {
  saveJsonList(LS_BOOKMARKS, items);
}

export function loadHighlights(): HighlightEntity[] {
  return loadJsonList<HighlightEntity>(LS_HIGHLIGHTS)
    .filter((h) => h.colorKey !== HighlightColor.NONE)
    .sort((a, b) => b.updatedAt - a.updatedAt);
}

export function saveHighlights(items: HighlightEntity[]): void {
  saveJsonList(LS_HIGHLIGHTS, items);
}

export function loadNotes(): NoteEntity[] {
  return loadJsonList<NoteEntity>(LS_NOTES).sort((a, b) => b.updatedAt - a.updatedAt);
}

export function saveNotes(items: NoteEntity[]): void {
  saveJsonList(LS_NOTES, items);
}

export function loadHistory(): ReadingHistoryEntity[] {
  return loadJsonList<ReadingHistoryEntity>(LS_HISTORY)
    .sort((a, b) => b.visitedAt - a.visitedAt)
    .slice(0, 60);
}

export function saveHistory(items: ReadingHistoryEntity[]): void {
  saveJsonList(LS_HISTORY, items.slice(0, 60));
}
