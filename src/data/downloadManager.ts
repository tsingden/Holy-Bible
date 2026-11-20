import {
  ALL_BIBLE_LANGUAGES,
  BIBLE_LANGUAGE_META,
  BibleLanguage,
  BookDownloadState,
  DownloadStatus,
  LanguageDownloadSummary,
  getLanguageSupportedBookIds,
  languageSupportsBook,
} from '../types/bible';
import { bibleDb } from './bibleDb';

export interface DownloadManagerSnapshot {
  bookStates: Map<string, BookDownloadState>; // `${langCode}:${bookId}`
  downloadedBookKeys: Set<string>;
  languageSummaries: LanguageDownloadSummary[];
  totalStorageBytes: number;
  dataVersion: number;
  isInitialized: boolean;
}

type Listener = (snapshot: DownloadManagerSnapshot) => void;

class BibleDownloadManager {
  private listeners = new Set<Listener>();
  private initialized = false;
  private initPromise: Promise<void> | null = null;
  private processing = false;
  private totalStorageBytes = 0;
  private dataVersion = 0;
  private statesMap = new Map<string, BookDownloadState>();

  subscribe(listener: Listener): () => void {
    this.listeners.add(listener);
    listener(this.getSnapshot());
    return () => {
      this.listeners.delete(listener);
    };
  }

  getSnapshot(): DownloadManagerSnapshot {
    const downloadedBookKeys = new Set<string>();
    for (const [key, st] of this.statesMap.entries()) {
      if (st.status === DownloadStatus.DOWNLOADED) {
        downloadedBookKeys.add(key);
      }
    }

    const languageSummaries: LanguageDownloadSummary[] = ALL_BIBLE_LANGUAGES.map((lang) => {
      const code = BIBLE_LANGUAGE_META[lang].code;
      const supported = getLanguageSupportedBookIds(lang);
      let downloadedBooks = 0;
      let queuedOrActiveBooks = 0;
      let downloading = 0;
      let paused = 0;
      let totalBytesOnDisk = 0;

      for (const bookId of supported) {
        const st = this.statesMap.get(`${code}:${bookId}`);
        switch (st?.status) {
          case DownloadStatus.DOWNLOADED:
            downloadedBooks++;
            totalBytesOnDisk += st.bytesSize;
            break;
          case DownloadStatus.DOWNLOADING:
            queuedOrActiveBooks++;
            downloading++;
            break;
          case DownloadStatus.QUEUED:
            queuedOrActiveBooks++;
            break;
          case DownloadStatus.PAUSED:
            paused++;
            break;
          default:
            break;
        }
      }

      return {
        language: lang,
        totalBooks: supported.length,
        downloadedBooks,
        queuedOrActiveBooks,
        isDownloading: downloading > 0 || queuedOrActiveBooks > 0,
        isPaused: paused > 0 && downloading === 0 && queuedOrActiveBooks === 0,
        totalBytesOnDisk,
      };
    });

    return {
      bookStates: new Map(this.statesMap),
      downloadedBookKeys,
      languageSummaries,
      totalStorageBytes: this.totalStorageBytes,
      dataVersion: this.dataVersion,
      isInitialized: this.initialized,
    };
  }

  private emit(): void {
    const snap = this.getSnapshot();
    for (const listener of this.listeners) {
      listener(snap);
    }
  }

  private async reloadStatesFromDb(bumpDataVersion = false): Promise<void> {
    const list = await bibleDb.getDownloadedBooksOnce();
    const map = new Map<string, BookDownloadState>();
    for (const item of list) {
      map.set(`${item.languageCode}:${item.bookId}`, item);
    }
    this.statesMap = map;
    this.totalStorageBytes = await bibleDb.getTotalStorageBytes();
    if (bumpDataVersion) this.dataVersion++;
    this.emit();
  }

  async initialize(seedDefaultsIfEmpty = true): Promise<void> {
    if (this.initialized) return;
    if (this.initPromise) return this.initPromise;

    this.initPromise = (async () => {
      await bibleDb.warmup();
      const existing = await bibleDb.getDownloadedBooksOnce();

      if (existing.length === 0 && seedDefaultsIfEmpty) {
        const defaultLangs = [BibleLanguage.ENGLISH, BibleLanguage.NEPALI, BibleLanguage.ONCV];
        const entities: BookDownloadState[] = [];

        for (const lang of defaultLangs) {
          const code = BIBLE_LANGUAGE_META[lang].code;
          const ids = getLanguageSupportedBookIds(lang);
          const sizes = await bibleDb.installBooksFromPack(lang, ids);
          for (const bookId of ids) {
            entities.push({
              languageCode: code,
              bookId,
              status: DownloadStatus.DOWNLOADED,
              bytesSize: sizes[bookId] ?? 0,
            });
          }
        }
        await bibleDb.upsertDownloadedBooks(entities);
      } else {
        const validCodes = new Set(ALL_BIBLE_LANGUAGES.map((l) => BIBLE_LANGUAGE_META[l].code));
        const obsoleteCodes = Array.from(
          new Set(existing.map((x) => x.languageCode).filter((c) => !validCodes.has(c as 'en' | 'ne' | 'ne_oncv' | 'ko')))
        );
        for (const code of obsoleteCodes) {
          await bibleDb.deleteDownloadedLanguage(code);
        }

        const interrupted = existing.filter((x) => x.status === DownloadStatus.DOWNLOADING);
        if (interrupted.length > 0) {
          await bibleDb.upsertDownloadedBooks(
            interrupted.map((x) => ({ ...x, status: DownloadStatus.QUEUED }))
          );
        }
      }

      this.initialized = true;
      await this.reloadStatesFromDb(true);
      void this.startQueueIfNeeded();
    })();

    return this.initPromise;
  }

  async downloadCompleteBible(): Promise<void> {
    await this.initialize(false);
    const toQueue: BookDownloadState[] = [];
    for (const lang of ALL_BIBLE_LANGUAGES) {
      const code = BIBLE_LANGUAGE_META[lang].code;
      for (const bookId of getLanguageSupportedBookIds(lang)) {
        const cur = this.statesMap.get(`${code}:${bookId}`);
        if (!cur || cur.status !== DownloadStatus.DOWNLOADED) {
          toQueue.push({
            languageCode: code,
            bookId,
            status: DownloadStatus.QUEUED,
            bytesSize: cur?.bytesSize ?? 0,
          });
        }
      }
    }
    if (toQueue.length > 0) {
      await bibleDb.upsertDownloadedBooks(toQueue);
      await this.reloadStatesFromDb();
      void this.startQueueIfNeeded();
    }
  }

  async downloadLanguage(lang: BibleLanguage): Promise<void> {
    await this.initialize(false);
    const code = BIBLE_LANGUAGE_META[lang].code;
    const toQueue: BookDownloadState[] = [];
    for (const bookId of getLanguageSupportedBookIds(lang)) {
      const cur = this.statesMap.get(`${code}:${bookId}`);
      if (!cur || cur.status !== DownloadStatus.DOWNLOADED) {
        toQueue.push({
          languageCode: code,
          bookId,
          status: DownloadStatus.QUEUED,
          bytesSize: cur?.bytesSize ?? 0,
        });
      }
    }
    if (toQueue.length > 0) {
      await bibleDb.upsertDownloadedBooks(toQueue);
      await this.reloadStatesFromDb();
      void this.startQueueIfNeeded();
    }
  }

  async downloadBook(lang: BibleLanguage, bookId: number): Promise<void> {
    if (!languageSupportsBook(lang, bookId)) return;
    await this.initialize(false);
    const code = BIBLE_LANGUAGE_META[lang].code;
    const cur = this.statesMap.get(`${code}:${bookId}`);
    if (cur?.status === DownloadStatus.DOWNLOADED) return;

    await bibleDb.upsertDownloadedBooks([
      {
        languageCode: code,
        bookId,
        status: DownloadStatus.QUEUED,
        bytesSize: 0,
      },
    ]);
    await this.reloadStatesFromDb();
    void this.startQueueIfNeeded();
  }

  async pauseLanguage(lang: BibleLanguage): Promise<void> {
    const code = BIBLE_LANGUAGE_META[lang].code;
    const all = await bibleDb.getDownloadedBooksOnce();
    const toPause = all.filter(
      (x) =>
        x.languageCode === code &&
        (x.status === DownloadStatus.QUEUED || x.status === DownloadStatus.DOWNLOADING)
    );
    if (toPause.length > 0) {
      await bibleDb.upsertDownloadedBooks(
        toPause.map((x) => ({ ...x, status: DownloadStatus.PAUSED }))
      );
      await this.reloadStatesFromDb();
    }
  }

  async pauseAll(): Promise<void> {
    const all = await bibleDb.getDownloadedBooksOnce();
    const toPause = all.filter(
      (x) => x.status === DownloadStatus.QUEUED || x.status === DownloadStatus.DOWNLOADING
    );
    if (toPause.length > 0) {
      await bibleDb.upsertDownloadedBooks(
        toPause.map((x) => ({ ...x, status: DownloadStatus.PAUSED }))
      );
      await this.reloadStatesFromDb();
    }
  }

  async resumeLanguage(lang: BibleLanguage): Promise<void> {
    const code = BIBLE_LANGUAGE_META[lang].code;
    const all = await bibleDb.getDownloadedBooksOnce();
    const toResume = all.filter(
      (x) => x.languageCode === code && x.status === DownloadStatus.PAUSED
    );
    if (toResume.length > 0) {
      await bibleDb.upsertDownloadedBooks(
        toResume.map((x) => ({ ...x, status: DownloadStatus.QUEUED }))
      );
      await this.reloadStatesFromDb();
      void this.startQueueIfNeeded();
    }
  }

  async resumeAll(): Promise<void> {
    const all = await bibleDb.getDownloadedBooksOnce();
    const toResume = all.filter((x) => x.status === DownloadStatus.PAUSED);
    if (toResume.length > 0) {
      await bibleDb.upsertDownloadedBooks(
        toResume.map((x) => ({ ...x, status: DownloadStatus.QUEUED }))
      );
      await this.reloadStatesFromDb();
      void this.startQueueIfNeeded();
    }
  }

  async cancelLanguage(lang: BibleLanguage): Promise<void> {
    const code = BIBLE_LANGUAGE_META[lang].code;
    const all = await bibleDb.getDownloadedBooksOnce();
    const toCancel = all.filter(
      (x) => x.languageCode === code && x.status !== DownloadStatus.DOWNLOADED
    );
    for (const item of toCancel) {
      await bibleDb.deleteDownloadedBook(item.languageCode, item.bookId);
    }
    await this.reloadStatesFromDb();
  }

  async cancelBook(lang: BibleLanguage, bookId: number): Promise<void> {
    const code = BIBLE_LANGUAGE_META[lang].code;
    const all = await bibleDb.getDownloadedBooksOnce();
    const cur = all.find((x) => x.languageCode === code && x.bookId === bookId);
    if (cur && cur.status !== DownloadStatus.DOWNLOADED) {
      await bibleDb.deleteDownloadedBook(code, bookId);
      await this.reloadStatesFromDb();
    }
  }

  async deleteLanguage(lang: BibleLanguage): Promise<void> {
    await this.pauseLanguage(lang);
    const code = BIBLE_LANGUAGE_META[lang].code;
    await bibleDb.removeLanguage(lang);
    await bibleDb.deleteDownloadedLanguage(code);
    await this.reloadStatesFromDb(true);
  }

  async deleteBook(lang: BibleLanguage, bookId: number): Promise<void> {
    const code = BIBLE_LANGUAGE_META[lang].code;
    await bibleDb.removeBook(lang, bookId);
    await bibleDb.deleteDownloadedBook(code, bookId);
    await this.reloadStatesFromDb(true);
  }

  private async startQueueIfNeeded(): Promise<void> {
    if (this.processing) return;
    this.processing = true;

    try {
      while (true) {
        const all = await bibleDb.getDownloadedBooksOnce();
        const next = all.find((x) => x.status === DownloadStatus.QUEUED);
        if (!next) break;

        const lang = ALL_BIBLE_LANGUAGES.find(
          (l) => BIBLE_LANGUAGE_META[l].code === next.languageCode
        );
        if (!lang) {
          await bibleDb.deleteDownloadedBook(next.languageCode, next.bookId);
          continue;
        }

        await bibleDb.upsertDownloadedBooks([
          { ...next, status: DownloadStatus.DOWNLOADING },
        ]);
        await this.reloadStatesFromDb();

        const bytes = await bibleDb.installBookFromPack(lang, next.bookId);

        const freshList = await bibleDb.getDownloadedBooksOnce();
        const fresh = freshList.find(
          (x) => x.languageCode === next.languageCode && x.bookId === next.bookId
        );
        if (!fresh || fresh.status === DownloadStatus.PAUSED) {
          await bibleDb.removeBook(lang, next.bookId);
          await this.reloadStatesFromDb();
          continue;
        }

        await bibleDb.upsertDownloadedBooks([
          {
            languageCode: next.languageCode,
            bookId: next.bookId,
            status: DownloadStatus.DOWNLOADED,
            bytesSize: bytes,
          },
        ]);
        await this.reloadStatesFromDb(true);

        // Yield briefly so progress bar animates smoothly & pause/cancel stays responsive
        await new Promise((r) => setTimeout(r, 14));
      }
    } finally {
      this.processing = false;
    }
  }
}

export const bibleDownloadManager = new BibleDownloadManager();
