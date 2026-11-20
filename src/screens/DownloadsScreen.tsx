import React, { useMemo, useState } from 'react';
import {
  ArrowLeft,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  Cloud,
  Download,
  HardDrive,
  Pause,
  Play,
  Trash2,
  XCircle,
} from 'lucide-react';
import {
  BIBLE_LANGUAGE_META,
  BibleLanguage,
  DownloadStatus,
  ReadingMode,
  SystemLanguage,
  getBookNameForLanguage,
  getLanguageSupportedBookIds,
  languageSupportsBook,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { COLORS, argbToCss, cssWithAlpha } from '../theme/themeTokens';
import { formatBytes, formatNumber } from '../utils/formatters';
import {
  LiquidGlassButton,
  LiquidGlassDialog,
  LiquidGlassSurface,
} from '../components/LiquidGlassShell';

export const DownloadsScreen: React.FC = () => {
  const {
    books,
    preferences,
    languageSummaries,
    bookDownloadStates,
    totalStorageBytes,
    tokens,
    palette,
    strings,
    goBack,
    navigate,
    openChapter,
    setPrimaryLanguage,
    setReadingMode,
    downloadCompleteBible,
    downloadLanguage,
    downloadBook,
    pauseLanguageDownload,
    pauseAllDownloads,
    resumeLanguageDownload,
    resumeAllDownloads,
    cancelLanguageDownload,
    cancelBookDownload,
    deleteLanguageDownload,
    deleteBookDownload,
  } = useBibleApp();

  const [expandedMap, setExpandedMap] = useState<Record<string, boolean>>({});
  const [confirmDeleteLang, setConfirmDeleteLang] = useState<BibleLanguage | null>(null);

  const sysLang = preferences.systemLanguage;
  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';

  const totalSupportedAcrossAll = useMemo(
    () => languageSummaries.reduce((acc, s) => acc + s.totalBooks, 0),
    [languageSummaries]
  );
  const totalDownloadedAcrossAll = useMemo(
    () => languageSummaries.reduce((acc, s) => acc + s.downloadedBooks, 0),
    [languageSummaries]
  );
  const anyDownloading = languageSummaries.some((s) => s.isDownloading);
  const anyPaused = languageSummaries.some((s) => s.isPaused);
  const allDownloaded =
    totalSupportedAcrossAll > 0 && totalDownloadedAcrossAll >= totalSupportedAcrossAll;

  const goldShimmer = argbToCss(COLORS.GoldShimmer, 1);
  const burgundyDeep = argbToCss(COLORS.BurgundyDeep, 1);

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <div className="mx-auto flex h-full w-full max-w-[680px] flex-col overflow-y-auto px-4 pb-10 pt-3">
        {/* Top Bar */}
        <div className="flex items-center gap-2 pb-3">
          <button
            type="button"
            onClick={goBack}
            aria-label={strings.back}
            className="rounded-full p-2 hover:bg-black/5"
            style={{ color: tokens.textPrimary }}
          >
            <ArrowLeft className="h-5 w-5" />
          </button>
          <div className="min-w-0 flex-1">
            <h1
              className="truncate text-[21px] font-bold"
              style={{ fontFamily: headerFont, color: tokens.textPrimary }}
            >
              {strings.offlineDownloadsTitle}
            </h1>
            <p className="truncate text-xs" style={{ color: tokens.textSecondary }}>
              {strings.offlineDownloadsSubtitle}
            </p>
          </div>
        </div>

        {/* Master Offline Storage Banner */}
        <div
          className="rounded-[20px] p-4 shadow-md"
          style={{ backgroundColor: palette.leatherAccent }}
        >
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2.5">
              <HardDrive className="h-5 w-5 shrink-0" style={{ color: goldShimmer }} />
              <div>
                <div className="text-sm font-bold" style={{ color: goldShimmer }}>
                  {strings.storageUsedLabel(formatBytes(totalStorageBytes, sysLang))}
                </div>
                <div className="text-xs" style={{ color: cssWithAlpha(goldShimmer, 0.85) }}>
                  {strings.languageBooksOfflineCount(
                    totalDownloadedAcrossAll,
                    totalSupportedAcrossAll
                  )}
                </div>
              </div>
            </div>
            {allDownloaded && (
              <Cloud className="h-5 w-5 shrink-0" style={{ color: goldShimmer }} />
            )}
          </div>

          <div
            className="mt-2.5 h-1.5 w-full overflow-hidden rounded-full"
            style={{ backgroundColor: burgundyDeep }}
          >
            <div
              className="h-full rounded-full transition-all"
              style={{
                width: `${
                  totalSupportedAcrossAll > 0
                    ? (totalDownloadedAcrossAll / totalSupportedAcrossAll) * 100
                    : 0
                }%`,
                backgroundColor: goldShimmer,
              }}
            />
          </div>

          <div className="mt-3 flex flex-wrap gap-2">
            <button
              type="button"
              disabled={allDownloaded}
              onClick={downloadCompleteBible}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-xl px-4 py-2.5 text-sm font-bold transition-transform active:scale-[0.99] disabled:opacity-65"
              style={{ backgroundColor: goldShimmer, color: burgundyDeep }}
            >
              <Download className="h-4 w-4 shrink-0" />
              <span className="truncate">
                {allDownloaded ? strings.completeBibleOffline : strings.downloadBibleButton}
              </span>
            </button>

            {anyDownloading ? (
              <button
                type="button"
                onClick={pauseAllDownloads}
                className="flex items-center gap-1 rounded-xl border px-3 py-2 text-xs font-bold"
                style={{ borderColor: goldShimmer, color: goldShimmer }}
              >
                <Pause className="h-4 w-4" />
                {strings.pause}
              </button>
            ) : anyPaused ? (
              <button
                type="button"
                onClick={resumeAllDownloads}
                className="flex items-center gap-1 rounded-xl border px-3 py-2 text-xs font-bold"
                style={{ borderColor: goldShimmer, color: goldShimmer }}
              >
                <Play className="h-4 w-4" />
                {strings.resume}
              </button>
            ) : null}
          </div>
        </div>

        {/* Per-Language Download Cards */}
        <div className="mt-3.5 space-y-3.5">
          {languageSummaries.map((summary) => {
            const lang = summary.language;
            const meta = BIBLE_LANGUAGE_META[lang];
            const isExpanded = expandedMap[meta.code] === true;
            const supportedBooks = books.filter((b) => languageSupportsBook(lang, b.id));
            const isFullyDownloaded =
              summary.totalBooks > 0 && summary.downloadedBooks >= summary.totalBooks;
            const progressPct =
              summary.totalBooks > 0
                ? Math.min(100, (summary.downloadedBooks / summary.totalBooks) * 100)
                : 0;

            const statusLabel = isFullyDownloaded
              ? strings.statusOfflineReady
              : summary.isDownloading
              ? strings.statusDownloading
              : summary.isPaused
              ? strings.statusPaused
              : summary.downloadedBooks > 0
              ? strings.statusPartial
              : strings.statusNotDownloaded;

            return (
              <LiquidGlassSurface key={meta.code} tokens={tokens} className="rounded-[20px] p-4">
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0 flex-1">
                    <div
                      className="text-[17px] font-bold"
                      style={{ fontFamily: headerFont, color: tokens.accentColor }}
                    >
                      {strings.bibleLanguageName(lang)} • {meta.nativeName}
                    </div>
                    <div className="text-[13px] font-semibold" style={{ color: tokens.textPrimary }}>
                      {meta.translationName}
                    </div>
                    <div className="text-[11.5px]" style={{ color: tokens.textSecondary }}>
                      {meta.licenseNote}
                    </div>
                  </div>
                  <span
                    className="shrink-0 rounded-lg px-2 py-1 text-[11px] font-bold"
                    style={{
                      backgroundColor: isFullyDownloaded
                        ? cssWithAlpha(tokens.accentColor, 0.16)
                        : cssWithAlpha(palette.goldAccent, 0.16),
                      color: isFullyDownloaded ? tokens.accentColor : tokens.textPrimary,
                    }}
                  >
                    {statusLabel}
                  </span>
                </div>

                <div className="mt-2.5 flex justify-between text-xs">
                  <span className="font-semibold" style={{ color: tokens.textPrimary }}>
                    {strings.booksSlashCount(summary.downloadedBooks, summary.totalBooks)}
                  </span>
                  <span style={{ color: tokens.textSecondary }}>
                    {formatBytes(summary.totalBytesOnDisk, sysLang)}
                  </span>
                </div>

                <div className="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-black/15">
                  <div
                    className="h-full rounded-full transition-all"
                    style={{
                      width: `${progressPct}%`,
                      backgroundColor: tokens.accentColor,
                    }}
                  />
                </div>

                <div className="mt-3 flex items-center gap-2">
                  {summary.isDownloading ? (
                    <>
                      <button
                        type="button"
                        onClick={() => pauseLanguageDownload(lang)}
                        className="inline-flex items-center gap-1 rounded-xl border px-3 py-1.5 text-xs font-semibold"
                        style={{ borderColor: tokens.borderTop, color: tokens.textPrimary }}
                      >
                        <Pause className="h-3.5 w-3.5" />
                        {strings.pause}
                      </button>
                      <button
                        type="button"
                        onClick={() => cancelLanguageDownload(lang)}
                        className="inline-flex items-center gap-1 rounded-xl border px-3 py-1.5 text-xs font-semibold"
                        style={{ borderColor: tokens.borderTop, color: tokens.textPrimary }}
                      >
                        <XCircle className="h-3.5 w-3.5" />
                        {strings.cancel}
                      </button>
                    </>
                  ) : summary.isPaused ? (
                    <>
                      <LiquidGlassButton
                        tokens={tokens}
                        onClick={() => resumeLanguageDownload(lang)}
                        className="min-h-[36px] px-3 py-1 text-xs"
                      >
                        <Play className="h-3.5 w-3.5" />
                        {strings.resume}
                      </LiquidGlassButton>
                      <button
                        type="button"
                        onClick={() => cancelLanguageDownload(lang)}
                        className="inline-flex items-center gap-1 rounded-xl border px-3 py-1.5 text-xs font-semibold"
                        style={{ borderColor: tokens.borderTop, color: tokens.textPrimary }}
                      >
                        <XCircle className="h-3.5 w-3.5" />
                        {strings.cancel}
                      </button>
                    </>
                  ) : !isFullyDownloaded ? (
                    <LiquidGlassButton
                      tokens={tokens}
                      onClick={() => downloadLanguage(lang)}
                      className="min-h-[36px] px-3.5 py-1.5 text-xs"
                    >
                      <Download className="h-3.5 w-3.5" />
                      {summary.downloadedBooks > 0
                        ? strings.downloadRemaining
                        : strings.downloadAll}
                    </LiquidGlassButton>
                  ) : (
                    <button
                      type="button"
                      onClick={() => {
                        setPrimaryLanguage(lang);
                        if (languageSupportsBook(lang, preferences.lastBookId)) {
                          void openChapter(
                            preferences.lastBookId,
                            preferences.lastChapter,
                            preferences.lastVerse
                          );
                        } else {
                          const firstId = getLanguageSupportedBookIds(lang)[0] ?? 1;
                          void openChapter(firstId, 1, 1);
                        }
                        if (preferences.readingMode === ReadingMode.SINGLE_SECONDARY) {
                          setReadingMode(ReadingMode.BILINGUAL_SPREAD);
                        }
                        navigate('reader');
                      }}
                      className="inline-flex items-center gap-1.5 rounded-xl border px-3 py-1.5 text-xs font-semibold"
                      style={{ borderColor: tokens.accentColor, color: tokens.accentColor }}
                    >
                      <CheckCircle2 className="h-3.5 w-3.5" />
                      {strings.readOffline}
                    </button>
                  )}

                  <div className="flex-1" />

                  {summary.downloadedBooks > 0 && (
                    <button
                      type="button"
                      onClick={() => setConfirmDeleteLang(lang)}
                      aria-label={strings.deletePackDesc(strings.bibleLanguageName(lang))}
                      className="rounded-full p-2 text-red-500 hover:bg-red-500/10"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  )}
                </div>

                <div
                  className="my-2.5 h-[0.7px] w-full"
                  style={{ backgroundColor: cssWithAlpha(palette.goldAccent, 0.25) }}
                />

                {/* Expand Individual Books */}
                <button
                  type="button"
                  onClick={() =>
                    setExpandedMap((prev) => ({ ...prev, [meta.code]: !isExpanded }))
                  }
                  className="flex w-full items-center justify-between py-1 text-left"
                >
                  <span className="text-[13px] font-semibold" style={{ color: tokens.accentColor }}>
                    {strings.individualBooksHeader(supportedBooks.length)}
                  </span>
                  {isExpanded ? (
                    <ChevronUp className="h-4 w-4" style={{ color: tokens.accentColor }} />
                  ) : (
                    <ChevronDown className="h-4 w-4" style={{ color: tokens.accentColor }} />
                  )}
                </button>

                {isExpanded && (
                  <div className="mt-2 space-y-1">
                    {supportedBooks.map((book) => {
                      const st = bookDownloadStates.get(`${meta.code}:${book.id}`);
                      const status = st?.status ?? DownloadStatus.NOT_DOWNLOADED;
                      const primaryBookTitle =
                        sysLang === SystemLanguage.NEPALI ? book.nameNe : book.nameEn;
                      const localized = getBookNameForLanguage(book, lang);
                      const statusText =
                        status === DownloadStatus.DOWNLOADED
                          ? strings.bookOfflineSize(formatBytes(st?.bytesSize ?? 0, sysLang))
                          : status === DownloadStatus.DOWNLOADING
                          ? strings.bookDownloading
                          : status === DownloadStatus.QUEUED
                          ? strings.bookQueued
                          : status === DownloadStatus.PAUSED
                          ? strings.bookPaused
                          : strings.bookNotDownloaded;

                      const sub =
                        localized !== primaryBookTitle
                          ? `${localized} • ${statusText}`
                          : statusText;

                      return (
                        <div
                          key={book.id}
                          className="flex items-center justify-between gap-2 rounded-xl px-2.5 py-1.5"
                          style={{ backgroundColor: cssWithAlpha(tokens.glassSurface, 0.5) }}
                        >
                          <div className="min-w-0 flex-1">
                            <div
                              className="truncate text-[13px] font-semibold"
                              style={{ color: tokens.textPrimary }}
                            >
                              {formatNumber(book.id, sysLang)}. {primaryBookTitle}
                            </div>
                            <div
                              className="truncate text-[11px]"
                              style={{ color: tokens.textSecondary }}
                            >
                              {sub}
                            </div>
                          </div>

                          {status === DownloadStatus.DOWNLOADED ? (
                            <button
                              type="button"
                              onClick={() => deleteBookDownload(lang, book.id)}
                              aria-label={`${strings.delete} ${primaryBookTitle}`}
                              className="rounded-full p-1.5 text-red-500 hover:bg-red-500/10"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          ) : status === DownloadStatus.QUEUED ||
                            status === DownloadStatus.DOWNLOADING ||
                            status === DownloadStatus.PAUSED ? (
                            <button
                              type="button"
                              onClick={() => cancelBookDownload(lang, book.id)}
                              aria-label={`${strings.cancel} ${primaryBookTitle}`}
                              className="rounded-full p-1.5 hover:bg-black/5"
                              style={{ color: palette.goldAccent }}
                            >
                              <XCircle className="h-4 w-4" />
                            </button>
                          ) : (
                            <button
                              type="button"
                              onClick={() => downloadBook(lang, book.id)}
                              aria-label={`${strings.download} ${primaryBookTitle}`}
                              className="rounded-full p-1.5 hover:bg-black/5"
                              style={{ color: palette.leatherAccent }}
                            >
                              <Download className="h-4 w-4" />
                            </button>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </LiquidGlassSurface>
            );
          })}
        </div>
      </div>

      {/* Delete Language Confirmation Dialog */}
      {confirmDeleteLang && (
        <LiquidGlassDialog
          open={true}
          onClose={() => setConfirmDeleteLang(null)}
          tokens={tokens}
        >
          <h3
            className="text-lg font-bold"
            style={{ fontFamily: headerFont, color: tokens.textPrimary }}
          >
            {strings.deletePackDialogTitle(strings.bibleLanguageName(confirmDeleteLang))}
          </h3>
          <p className="mt-2 text-sm" style={{ color: tokens.textSecondary }}>
            {strings.deletePackDialogBody(strings.bibleLanguageName(confirmDeleteLang))}
          </p>
          <div className="mt-4 flex justify-end gap-2">
            <button
              type="button"
              onClick={() => setConfirmDeleteLang(null)}
              className="rounded-xl px-3.5 py-2 text-xs font-semibold"
              style={{ color: tokens.textSecondary }}
            >
              {strings.keepButton}
            </button>
            <button
              type="button"
              onClick={() => {
                deleteLanguageDownload(confirmDeleteLang);
                setConfirmDeleteLang(null);
              }}
              className="rounded-xl bg-red-600 px-4 py-2 text-xs font-bold text-white hover:bg-red-700"
            >
              {strings.delete}
            </button>
          </div>
        </LiquidGlassDialog>
      )}
    </div>
  );
};
