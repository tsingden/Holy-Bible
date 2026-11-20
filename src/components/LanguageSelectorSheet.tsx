import React, { useMemo, useState } from 'react';
import { ArrowLeftRight, CheckCircle2, Download, Languages, X } from 'lucide-react';
import {
  ALL_BIBLE_LANGUAGES,
  BIBLE_LANGUAGE_META,
  ReadingMode,
  SystemLanguage,
  getLanguageSupportedBookIds,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { cssWithAlpha } from '../theme/themeTokens';
import { formatNumber } from '../utils/formatters';
import { LiquidGlassSurface } from './LiquidGlassShell';

interface LanguageSelectorSheetProps {
  open: boolean;
  onOpenDownloadsManager: () => void;
  onDismiss: () => void;
}

export const LanguageSelectorSheet: React.FC<LanguageSelectorSheetProps> = ({
  open,
  onOpenDownloadsManager,
  onDismiss,
}) => {
  const {
    preferences,
    tokens,
    palette,
    strings,
    languageSummaries,
    setPrimaryLanguage,
    setSecondaryLanguage,
    swapLanguages,
    setReadingMode,
    downloadLanguage,
  } = useBibleApp();

  const [editingSlot, setEditingSlot] = useState<0 | 1>(0);
  const sysLang = preferences.systemLanguage;
  const primaryLang = preferences.primaryLanguage;
  const secondaryLang = preferences.secondaryLanguage;
  const readingMode = preferences.readingMode;

  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';

  const summaryMap = useMemo(() => {
    const m = new Map(languageSummaries.map((s) => [s.language, s]));
    return m;
  }, [languageSummaries]);

  if (!open) return null;

  const activeLang = editingSlot === 0 ? primaryLang : secondaryLang;

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center sm:items-center sm:p-4"
      style={{
        backgroundColor: `rgba(0,0,0,${tokens.isCrystalClear ? 0.24 : 0.38})`,
        backdropFilter: 'blur(4px)',
      }}
      onClick={onDismiss}
    >
      <div
        className="animate-modal-pop flex h-[86vh] w-full max-w-xl flex-col sm:h-[82vh]"
        onClick={(e) => e.stopPropagation()}
      >
        <LiquidGlassSurface
          tokens={tokens}
          elevated
          className="flex h-full flex-col rounded-t-3xl p-4 sm:rounded-3xl"
        >
          {/* Header */}
          <div className="flex shrink-0 items-center justify-between gap-2 pb-2.5">
            <div className="flex items-center gap-2.5">
              <Languages className="h-5 w-5 shrink-0" style={{ color: tokens.accentColor }} />
              <div>
                <h2
                  className="text-[18px] font-bold"
                  style={{ fontFamily: headerFont, color: tokens.textPrimary }}
                >
                  {strings.bibleLanguagesTitle}
                </h2>
                <p className="text-xs" style={{ color: tokens.textSecondary }}>
                  {strings.bibleLanguagesSubtitle}
                </p>
              </div>
            </div>
            <button
              type="button"
              onClick={onDismiss}
              aria-label={strings.closeLanguageSelectorDesc}
              className="rounded-full p-2 hover:bg-black/5"
              style={{ color: tokens.textPrimary }}
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Active Side-by-Side Pair Preview with Swap Button */}
          <div
            className="flex shrink-0 items-center justify-between rounded-2xl border px-3.5 py-2.5"
            style={{
              backgroundColor: tokens.glassSurface,
              borderColor: tokens.borderTop,
            }}
          >
            <div className="min-w-0 flex-1">
              <div className="text-[10px] font-bold" style={{ color: tokens.textSecondary }}>
                {strings.primaryLeftPageLabel}
              </div>
              <div className="truncate text-[13.5px] font-bold" style={{ color: tokens.textPrimary }}>
                {BIBLE_LANGUAGE_META[primaryLang].nativeName}
              </div>
            </div>

            <button
              type="button"
              onClick={swapLanguages}
              aria-label={strings.swapLanguagesDesc}
              className="mx-2 rounded-full p-2 transition-colors hover:bg-black/5"
              style={{ color: tokens.accentColor }}
            >
              <ArrowLeftRight className="h-5 w-5" />
            </button>

            <div className="min-w-0 flex-1 text-right">
              <div className="text-[10px] font-bold" style={{ color: tokens.textSecondary }}>
                {strings.compareRightPageLabel}
              </div>
              <div className="truncate text-[13.5px] font-bold" style={{ color: tokens.textPrimary }}>
                {BIBLE_LANGUAGE_META[secondaryLang].nativeName}
              </div>
            </div>
          </div>

          {/* Quick Reading Layout Mode Chips */}
          <div className="mt-2.5 grid shrink-0 grid-cols-4 gap-1.5">
            {[
              ReadingMode.BILINGUAL_SPREAD,
              ReadingMode.BILINGUAL_PARALLEL,
              ReadingMode.SINGLE_PRIMARY,
              ReadingMode.SINGLE_SECONDARY,
            ].map((mode) => {
              const selected = readingMode === mode;
              const shortLabel =
                mode === ReadingMode.BILINGUAL_SPREAD
                  ? strings.spreadModeShort
                  : mode === ReadingMode.BILINGUAL_PARALLEL
                  ? strings.parallelModeShort
                  : mode === ReadingMode.SINGLE_PRIMARY
                  ? strings.bibleLanguageName(primaryLang)
                  : strings.bibleLanguageName(secondaryLang);
              return (
                <button
                  key={mode}
                  type="button"
                  onClick={() => setReadingMode(mode)}
                  className="truncate rounded-xl border px-2 py-1.5 text-[11.5px] font-semibold transition-colors"
                  style={{
                    backgroundColor: selected
                      ? cssWithAlpha(tokens.accentColor, 0.2)
                      : 'transparent',
                    borderColor: selected ? tokens.accentColor : tokens.borderTop,
                    color: selected ? tokens.accentColor : tokens.textPrimary,
                  }}
                >
                  {shortLabel}
                </button>
              );
            })}
          </div>

          {/* Slot Tabs */}
          <div
            className="mt-2.5 grid shrink-0 grid-cols-2 border-b"
            style={{ borderColor: tokens.borderTop }}
          >
            {[0, 1].map((idx) => {
              const active = editingSlot === idx;
              const label =
                idx === 0
                  ? strings.tabPrimaryPrefix(strings.bibleLanguageName(primaryLang))
                  : strings.tabComparePrefix(strings.bibleLanguageName(secondaryLang));
              return (
                <button
                  key={idx}
                  type="button"
                  onClick={() => setEditingSlot(idx as 0 | 1)}
                  className="truncate border-b-2 py-2 text-xs font-bold transition-colors"
                  style={{
                    borderColor: active ? tokens.accentColor : 'transparent',
                    color: active ? tokens.accentColor : tokens.textSecondary,
                  }}
                >
                  {label}
                </button>
              );
            })}
          </div>

          {/* Language Cards */}
          <div className="mt-2.5 flex-1 space-y-2 overflow-y-auto pb-3 pr-0.5">
            {ALL_BIBLE_LANGUAGES.map((lang) => {
              const meta = BIBLE_LANGUAGE_META[lang];
              const isSelected = lang === activeLang;
              const summary = summaryMap.get(lang);
              const isFull =
                summary !== undefined &&
                summary.totalBooks > 0 &&
                summary.downloadedBooks >= summary.totalBooks;
              const isPartial =
                !summary?.isDownloading && (summary?.downloadedBooks ?? 0) > 0 && !isFull;

              return (
                <div
                  key={lang}
                  onClick={() =>
                    editingSlot === 0 ? setPrimaryLanguage(lang) : setSecondaryLanguage(lang)
                  }
                  className="cursor-pointer rounded-2xl border p-3 transition-colors"
                  style={{
                    backgroundColor: isSelected
                      ? cssWithAlpha(tokens.accentColor, 0.22)
                      : tokens.glassSurface,
                    borderColor: isSelected ? tokens.accentColor : tokens.borderTop,
                    borderWidth: isSelected ? '1.5px' : '1px',
                  }}
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="min-w-0 flex-1">
                      <div className="text-[15px] font-bold" style={{ color: tokens.textPrimary }}>
                        {strings.bibleLanguageName(lang)} — {meta.nativeName}
                      </div>
                      <div className="text-xs" style={{ color: tokens.textSecondary }}>
                        {meta.translationName} •{' '}
                        {strings.booksCountWord(getLanguageSupportedBookIds(lang).length)}
                      </div>
                    </div>

                    {isFull ? (
                      <div
                        className="flex shrink-0 items-center gap-1 text-xs font-semibold"
                        style={{ color: palette.leatherAccent }}
                      >
                        <CheckCircle2 className="h-4 w-4" />
                        <span>{isSelected ? strings.activeLabel : strings.offlineLabel}</span>
                      </div>
                    ) : summary?.isDownloading ? (
                      <span
                        className="shrink-0 text-xs font-bold"
                        style={{ color: palette.goldAccent }}
                      >
                        {formatNumber(summary.downloadedBooks, sysLang)}/
                        {formatNumber(summary.totalBooks, sysLang)}
                      </span>
                    ) : (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          downloadLanguage(lang);
                        }}
                        className="inline-flex h-8 shrink-0 items-center gap-1.5 rounded-xl border px-2.5 text-xs font-semibold"
                        style={{ borderColor: tokens.accentColor, color: tokens.accentColor }}
                      >
                        <Download className="h-3.5 w-3.5" />
                        {strings.download}
                      </button>
                    )}
                  </div>

                  {summary?.isDownloading && (
                    <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-black/15">
                      <div
                        className="h-full rounded-full transition-all"
                        style={{
                          width: `${
                            summary.totalBooks > 0
                              ? (summary.downloadedBooks / summary.totalBooks) * 100
                              : 0
                          }%`,
                          backgroundColor: tokens.accentColor,
                        }}
                      />
                    </div>
                  )}

                  {isPartial && summary && (
                    <p className="mt-1 text-[11px]" style={{ color: tokens.textSecondary }}>
                      {strings.booksDownloadedProgress(summary.downloadedBooks, summary.totalBooks)}
                    </p>
                  )}
                </div>
              );
            })}
          </div>

          {/* Bottom Bar */}
          <div
            className="flex shrink-0 items-center justify-between border-t pt-2.5"
            style={{ borderColor: tokens.borderTop }}
          >
            <button
              type="button"
              onClick={() => {
                onDismiss();
                onOpenDownloadsManager();
              }}
              className="inline-flex items-center gap-1.5 text-xs font-semibold"
              style={{ color: tokens.accentColor }}
            >
              <Download className="h-4 w-4" />
              {strings.manageOfflineDownloads}
            </button>
            <button
              type="button"
              onClick={onDismiss}
              className="rounded-xl px-4 py-2 text-xs font-bold"
              style={{
                backgroundColor: tokens.accentColor,
                color: tokens.onAccentColor,
              }}
            >
              {strings.done}
            </button>
          </div>
        </LiquidGlassSurface>
      </div>
    </div>
  );
};
