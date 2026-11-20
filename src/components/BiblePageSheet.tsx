import React, { useEffect, useMemo, useRef } from 'react';
import { Bookmark, Download, FileEdit, Info } from 'lucide-react';
import {
  BIBLE_LANGUAGE_META,
  BibleBook,
  BibleLanguage,
  BibleVerse,
  HighlightColor,
  ReadingMode,
  ScriptureFontStyle,
  SystemLanguage,
  getBookNameForLanguage,
  getFontFamilyForBibleLanguage,
  getVerseTextForLanguage,
  languageSupportsBook,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { BibleBookPalette, cssWithAlpha } from '../theme/themeTokens';
import { formatNumber, toDevanagari } from '../utils/formatters';
import { LiquidGlassSurface } from './LiquidGlassShell';

interface BiblePageSheetProps {
  book: BibleBook;
  chapter: number;
  verses: BibleVerse[];
  readingMode: ReadingMode;
  primaryLanguage: BibleLanguage;
  secondaryLanguage: BibleLanguage;
  downloadedBookKeys: Set<string>;
  fontSizeSp: number;
  selectedVerseKey: string | null;
  activeAudioVerseKey: string | null;
  targetScrollVerse: number | null;
  bookmarkedKeys: Set<string>;
  highlightMap: Map<string, HighlightColor>;
  noteKeys: Set<string>;
  onVerseClick: (verse: BibleVerse) => void;
  onTopVisibleVerseChanged?: (verseNum: number) => void;
  onDownloadLanguageBook?: (lang: BibleLanguage, bookId: number) => void;
  onOpenDownloadsManager?: () => void;
  isStaticPreview?: boolean;
  scrollContainerRef?: React.RefObject<HTMLDivElement | null>;
}

function formatHeaderTitle(book: BibleBook, chapter: number, lang: BibleLanguage): string {
  const name = getBookNameForLanguage(book, lang);
  switch (lang) {
    case BibleLanguage.ENGLISH:
      return `${name.toUpperCase()} ${chapter}`;
    case BibleLanguage.NEPALI:
    case BibleLanguage.ONCV:
      return `${name} ${toDevanagari(chapter)}`;
    case BibleLanguage.KOREAN:
      return `${name} ${chapter}`;
  }
}

function formatChapterLabel(chapter: number, lang: BibleLanguage): string {
  switch (lang) {
    case BibleLanguage.ENGLISH:
      return `Chapter ${chapter}`;
    case BibleLanguage.NEPALI:
    case BibleLanguage.ONCV:
      return `अध्याय ${toDevanagari(chapter)}`;
    case BibleLanguage.KOREAN:
      return `제${chapter}장`;
  }
}

function resolveVerseBackground(
  isAudioActive: boolean,
  isSelected: boolean,
  highlight: HighlightColor,
  palette: BibleBookPalette
): string {
  if (isAudioActive) return palette.activeVerseTint;
  if (isSelected) return cssWithAlpha(palette.goldAccent, 0.22);
  switch (highlight) {
    case HighlightColor.GOLD:
      return palette.highlightGold;
    case HighlightColor.ROSE:
      return palette.highlightRose;
    case HighlightColor.SAGE:
      return palette.highlightSage;
    case HighlightColor.SKY:
      return palette.highlightSky;
    default:
      return 'transparent';
  }
}

const LanguageVerseText: React.FC<{
  verseNumber: number;
  text: string;
  language: BibleLanguage;
  fontSizeSp: number;
  fontStyle: ScriptureFontStyle;
  palette: BibleBookPalette;
}> = ({ verseNumber, text, language, fontSizeSp, fontStyle, palette }) => {
  const meta = BIBLE_LANGUAGE_META[language];
  const displayNumber = meta.usesDevanagari ? toDevanagari(verseNumber) : String(verseNumber);
  const lineHeight =
    language === BibleLanguage.KOREAN
      ? 1.5
      : language === BibleLanguage.NEPALI || language === BibleLanguage.ONCV
      ? 1.48
      : 1.44;
  const fontFamily = getFontFamilyForBibleLanguage(language, fontStyle);

  return (
    <p
      dir={meta.isRtl ? 'rtl' : 'ltr'}
      className="m-0 w-full break-words"
      style={{
        fontFamily,
        fontSize: `${fontSizeSp}px`,
        lineHeight,
        color: text.trim() ? palette.inkPrimary : palette.inkSecondary,
      }}
    >
      <sup
        className="mr-1 font-bold"
        style={{
          color: palette.verseNumberInk,
          fontSize: `${(fontSizeSp * 0.72).toFixed(1)}px`,
          fontFamily: '"Plus Jakarta Sans", sans-serif',
        }}
      >
        {displayNumber}
      </sup>
      <span>{text.trim() ? text : '—'}</span>
    </p>
  );
};

export const BiblePageSheet: React.FC<BiblePageSheetProps> = ({
  book,
  chapter,
  verses,
  readingMode,
  primaryLanguage,
  secondaryLanguage,
  downloadedBookKeys,
  fontSizeSp,
  selectedVerseKey,
  activeAudioVerseKey,
  targetScrollVerse,
  bookmarkedKeys,
  highlightMap,
  noteKeys,
  onVerseClick,
  onTopVisibleVerseChanged,
  onDownloadLanguageBook,
  onOpenDownloadsManager,
  isStaticPreview = false,
  scrollContainerRef: externalScrollRef,
}) => {
  const { palette, tokens, strings, preferences } = useBibleApp();
  const sysLang = preferences.systemLanguage;
  const fontStyle = tokens.fontStyle;
  const internalScrollRef = useRef<HTMLDivElement | null>(null);
  const scrollRef = externalScrollRef ?? internalScrollRef;
  const lastReportedVerseRef = useRef<number>(1);

  const primaryCode = BIBLE_LANGUAGE_META[primaryLanguage].code;
  const secondaryCode = BIBLE_LANGUAGE_META[secondaryLanguage].code;

  const primaryHasContent = useMemo(
    () =>
      downloadedBookKeys.has(`${primaryCode}:${book.id}`) ||
      verses.some((v) => getVerseTextForLanguage(v, primaryLanguage).trim().length > 0),
    [downloadedBookKeys, primaryCode, book.id, verses, primaryLanguage]
  );

  const secondaryHasContent = useMemo(
    () =>
      downloadedBookKeys.has(`${secondaryCode}:${book.id}`) ||
      verses.some((v) => getVerseTextForLanguage(v, secondaryLanguage).trim().length > 0),
    [downloadedBookKeys, secondaryCode, book.id, verses, secondaryLanguage]
  );

  useEffect(() => {
    if (isStaticPreview) return;
    if (targetScrollVerse && verses.length > 0) {
      const targetEl = scrollRef.current?.querySelector<HTMLElement>(
        `[data-verse-num="${targetScrollVerse}"]`
      );
      if (targetEl) {
        targetEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    } else if (scrollRef.current && targetScrollVerse === null) {
      scrollRef.current.scrollTop = 0;
    }
  }, [book.id, chapter, targetScrollVerse, verses.length, isStaticPreview, scrollRef]);

  const handleScroll = () => {
    if (isStaticPreview || !onTopVisibleVerseChanged || !scrollRef.current) return;
    const container = scrollRef.current;
    const containerTop = container.getBoundingClientRect().top;
    const items = container.querySelectorAll<HTMLElement>('[data-verse-num]');
    for (let i = 0; i < items.length; i++) {
      const rect = items[i].getBoundingClientRect();
      if (rect.bottom > containerTop + 20) {
        const vNum = Number(items[i].getAttribute('data-verse-num') || '1');
        if (vNum !== lastReportedVerseRef.current) {
          lastReportedVerseRef.current = vNum;
          onTopVisibleVerseChanged(vNum);
        }
        break;
      }
    }
  };

  const showPrimaryMissing =
    readingMode !== ReadingMode.SINGLE_SECONDARY && !primaryHasContent;
  const showSecondaryMissing =
    (readingMode === ReadingMode.BILINGUAL_SPREAD ||
      readingMode === ReadingMode.BILINGUAL_PARALLEL ||
      readingMode === ReadingMode.SINGLE_SECONDARY) &&
    !secondaryHasContent;

  const primaryHeadingFont =
    primaryLanguage === BibleLanguage.ENGLISH
      ? fontStyle === ScriptureFontStyle.CLASSIC_SERIF
        ? '"Playfair Display", Georgia, serif'
        : getFontFamilyForBibleLanguage(primaryLanguage, fontStyle)
      : getFontFamilyForBibleLanguage(primaryLanguage, fontStyle);

  const secondaryHeadingFont =
    secondaryLanguage === BibleLanguage.ENGLISH
      ? fontStyle === ScriptureFontStyle.CLASSIC_SERIF
        ? '"Playfair Display", Georgia, serif'
        : getFontFamilyForBibleLanguage(secondaryLanguage, fontStyle)
      : getFontFamilyForBibleLanguage(secondaryLanguage, fontStyle);

  const visibleVerses = isStaticPreview ? verses.slice(0, 14) : verses;

  const chPrimary =
    BIBLE_LANGUAGE_META[primaryLanguage].usesDevanagari || sysLang === SystemLanguage.NEPALI
      ? toDevanagari(chapter)
      : String(chapter);
  const chSecondary =
    BIBLE_LANGUAGE_META[secondaryLanguage].usesDevanagari || sysLang === SystemLanguage.NEPALI
      ? toDevanagari(chapter)
      : String(chapter);

  return (
    <div
      className="relative flex h-full w-full flex-col overflow-hidden"
      style={{ backgroundColor: palette.paperBackground }}
    >
      {/* Running Book Header */}
      <div className="shrink-0">
        <div className="flex items-center justify-between px-3.5 py-2 sm:px-5">
          {readingMode !== ReadingMode.SINGLE_SECONDARY && (
            <span
              className="truncate text-[11.5px] font-semibold"
              style={{
                fontFamily: getFontFamilyForBibleLanguage(primaryLanguage, fontStyle),
                color: palette.inkSecondary,
              }}
            >
              {formatHeaderTitle(book, chapter, primaryLanguage)}
            </span>
          )}
          {readingMode !== ReadingMode.SINGLE_PRIMARY && (
            <span
              className={`truncate text-[12px] font-semibold ${
                readingMode === ReadingMode.SINGLE_SECONDARY ? 'text-left' : 'text-right'
              }`}
              style={{
                fontFamily: getFontFamilyForBibleLanguage(secondaryLanguage, fontStyle),
                color: palette.inkSecondary,
              }}
            >
              {formatHeaderTitle(book, chapter, secondaryLanguage)}
            </span>
          )}
        </div>
        <div
          className="h-[0.7px] w-full"
          style={{ backgroundColor: cssWithAlpha(palette.goldAccent, 0.35) }}
        />
      </div>

      {/* Scrollable Chapter Verses */}
      <div
        ref={scrollRef}
        onScroll={handleScroll}
        className={`relative flex-1 ${isStaticPreview ? 'overflow-hidden' : 'overflow-y-auto'} pb-28 pt-1`}
      >
        {/* Chapter Title Banner */}
        {readingMode === ReadingMode.BILINGUAL_SPREAD ? (
          <div className="grid grid-cols-2 gap-3 px-2.5 py-2.5 sm:px-4">
            <div className="flex flex-col items-center text-center">
              <h2
                className="text-[17.5px] font-bold leading-tight sm:text-[20px]"
                style={{ fontFamily: primaryHeadingFont, color: palette.leatherAccent }}
              >
                {getBookNameForLanguage(book, primaryLanguage)}
              </h2>
              <span
                className={`text-[12.5px] ${
                  primaryLanguage === BibleLanguage.ENGLISH ? 'italic' : ''
                }`}
                style={{ fontFamily: primaryHeadingFont, color: palette.goldAccent }}
              >
                {formatChapterLabel(chapter, primaryLanguage)}
              </span>
            </div>
            <div className="flex flex-col items-center text-center">
              <h2
                className="text-[17.5px] font-bold leading-tight sm:text-[20px]"
                style={{ fontFamily: secondaryHeadingFont, color: palette.leatherAccent }}
              >
                {getBookNameForLanguage(book, secondaryLanguage)}
              </h2>
              <span
                className={`text-[12.5px] ${
                  secondaryLanguage === BibleLanguage.ENGLISH ? 'italic' : ''
                }`}
                style={{ fontFamily: secondaryHeadingFont, color: palette.goldAccent }}
              >
                {formatChapterLabel(chapter, secondaryLanguage)}
              </span>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center px-4 py-3 text-center">
            <h2
              className="text-[21px] font-bold"
              style={{
                fontFamily:
                  readingMode === ReadingMode.BILINGUAL_PARALLEL ||
                  (readingMode === ReadingMode.SINGLE_SECONDARY
                    ? secondaryLanguage
                    : primaryLanguage) === BibleLanguage.ENGLISH
                    ? fontStyle === ScriptureFontStyle.CLASSIC_SERIF
                      ? '"Playfair Display", Georgia, serif'
                      : getFontFamilyForBibleLanguage(
                          readingMode === ReadingMode.SINGLE_SECONDARY
                            ? secondaryLanguage
                            : primaryLanguage,
                          fontStyle
                        )
                    : getFontFamilyForBibleLanguage(
                        readingMode === ReadingMode.SINGLE_SECONDARY
                          ? secondaryLanguage
                          : primaryLanguage,
                        fontStyle
                      ),
                color: palette.leatherAccent,
              }}
            >
              {readingMode === ReadingMode.SINGLE_PRIMARY
                ? getBookNameForLanguage(book, primaryLanguage)
                : readingMode === ReadingMode.SINGLE_SECONDARY
                ? getBookNameForLanguage(book, secondaryLanguage)
                : `${getBookNameForLanguage(book, primaryLanguage)} • ${getBookNameForLanguage(
                    book,
                    secondaryLanguage
                  )}`}
            </h2>
            <span
              className="text-[13px] italic"
              style={{ fontFamily: '"Playfair Display", Georgia, serif', color: palette.goldAccent }}
            >
              {readingMode === ReadingMode.SINGLE_PRIMARY
                ? formatChapterLabel(chapter, primaryLanguage)
                : readingMode === ReadingMode.SINGLE_SECONDARY
                ? formatChapterLabel(chapter, secondaryLanguage)
                : `${formatChapterLabel(chapter, primaryLanguage)} • ${formatChapterLabel(
                    chapter,
                    secondaryLanguage
                  )}`}
            </span>
          </div>
        )}

        {/* Missing Offline Pack Banner */}
        {!isStaticPreview && (showPrimaryMissing || showSecondaryMissing) && (
          <div className="space-y-2 px-3.5 py-2">
            {showPrimaryMissing && (
              <MissingLanguageCard
                book={book}
                language={primaryLanguage}
                palette={palette}
                onDownload={() => onDownloadLanguageBook?.(primaryLanguage, book.id)}
                onOpenDownloads={() => onOpenDownloadsManager?.()}
              />
            )}
            {showSecondaryMissing &&
              (!showPrimaryMissing || secondaryLanguage !== primaryLanguage) && (
                <MissingLanguageCard
                  book={book}
                  language={secondaryLanguage}
                  palette={palette}
                  onDownload={() => onDownloadLanguageBook?.(secondaryLanguage, book.id)}
                  onOpenDownloads={() => onOpenDownloadsManager?.()}
                />
              )}
          </div>
        )}

        {/* Verses */}
        <div className="divide-y divide-transparent">
          {visibleVerses.map((verse) => {
            const isSelected = verse.key === selectedVerseKey;
            const isAudioActive = verse.key === activeAudioVerseKey;
            const highlight = highlightMap.get(verse.key) ?? HighlightColor.NONE;
            const isBookmarked = bookmarkedKeys.has(verse.key);
            const hasNote = noteKeys.has(verse.key);
            const bgColor = resolveVerseBackground(isAudioActive, isSelected, highlight, palette);

            if (readingMode === ReadingMode.BILINGUAL_SPREAD) {
              return (
                <div
                  key={verse.key}
                  data-verse-num={verse.verse}
                  onClick={() => !isStaticPreview && onVerseClick(verse)}
                  className="grid cursor-pointer grid-cols-2 gap-3 px-2.5 py-1 transition-colors sm:px-3.5"
                >
                  <div
                    className="rounded-md px-1.5 py-1 transition-colors"
                    style={{ backgroundColor: bgColor }}
                  >
                    <LanguageVerseText
                      verseNumber={verse.verse}
                      text={getVerseTextForLanguage(verse, primaryLanguage)}
                      language={primaryLanguage}
                      fontSizeSp={fontSizeSp * 0.95}
                      fontStyle={fontStyle}
                      palette={palette}
                    />
                    {(isBookmarked || hasNote) && (
                      <div className="mt-0.5 flex items-center justify-end gap-1">
                        {isBookmarked && (
                          <Bookmark className="h-3 w-3" style={{ color: palette.leatherAccent }} />
                        )}
                        {hasNote && (
                          <FileEdit className="h-3 w-3" style={{ color: palette.goldAccent }} />
                        )}
                      </div>
                    )}
                  </div>

                  <div
                    className="rounded-md px-1.5 py-1 transition-colors"
                    style={{ backgroundColor: bgColor }}
                  >
                    <LanguageVerseText
                      verseNumber={verse.verse}
                      text={getVerseTextForLanguage(verse, secondaryLanguage)}
                      language={secondaryLanguage}
                      fontSizeSp={fontSizeSp * 0.95}
                      fontStyle={fontStyle}
                      palette={palette}
                    />
                    {(isBookmarked || hasNote) && (
                      <div className="mt-0.5 flex items-center justify-end gap-1">
                        {isBookmarked && (
                          <Bookmark className="h-3 w-3" style={{ color: palette.leatherAccent }} />
                        )}
                        {hasNote && (
                          <FileEdit className="h-3 w-3" style={{ color: palette.goldAccent }} />
                        )}
                      </div>
                    )}
                  </div>
                </div>
              );
            }

            return (
              <div
                key={verse.key}
                data-verse-num={verse.verse}
                onClick={() => !isStaticPreview && onVerseClick(verse)}
                className="mx-3 my-1 cursor-pointer rounded-md px-2.5 py-1.5 transition-colors sm:mx-4"
                style={{ backgroundColor: bgColor }}
              >
                {(readingMode === ReadingMode.SINGLE_PRIMARY ||
                  readingMode === ReadingMode.BILINGUAL_PARALLEL) && (
                  <LanguageVerseText
                    verseNumber={verse.verse}
                    text={getVerseTextForLanguage(verse, primaryLanguage)}
                    language={primaryLanguage}
                    fontSizeSp={fontSizeSp}
                    fontStyle={fontStyle}
                    palette={palette}
                  />
                )}
                {readingMode === ReadingMode.BILINGUAL_PARALLEL && <div className="h-1.5" />}
                {(readingMode === ReadingMode.SINGLE_SECONDARY ||
                  readingMode === ReadingMode.BILINGUAL_PARALLEL) && (
                  <LanguageVerseText
                    verseNumber={verse.verse}
                    text={getVerseTextForLanguage(verse, secondaryLanguage)}
                    language={secondaryLanguage}
                    fontSizeSp={fontSizeSp * 0.98}
                    fontStyle={fontStyle}
                    palette={palette}
                  />
                )}
                {(isBookmarked || hasNote) && (
                  <div className="mt-1 flex items-center justify-end gap-1">
                    {isBookmarked && (
                      <Bookmark className="h-3 w-3" style={{ color: palette.leatherAccent }} />
                    )}
                    {hasNote && (
                      <FileEdit className="h-3 w-3" style={{ color: palette.goldAccent }} />
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Chapter End Footer */}
        {!isStaticPreview && (
          <div className="mt-4 flex flex-col items-center px-5 py-4 text-center">
            <div
              className="mb-2.5 h-[0.7px] w-full"
              style={{ backgroundColor: cssWithAlpha(palette.goldAccent, 0.35) }}
            />
            <p
              className="text-xs"
              style={{
                fontFamily:
                  sysLang === SystemLanguage.NEPALI
                    ? '"Noto Serif Devanagari", serif'
                    : '"Playfair Display", Georgia, serif',
                color: palette.inkSecondary,
              }}
            >
              {readingMode === ReadingMode.SINGLE_PRIMARY
                ? `✦ ${getBookNameForLanguage(book, primaryLanguage)} ${chPrimary} (${strings.versesCountLabel(
                    verses.length
                  )}) ✦`
                : readingMode === ReadingMode.SINGLE_SECONDARY
                ? `✦ ${getBookNameForLanguage(
                    book,
                    secondaryLanguage
                  )} ${chSecondary} (${strings.versesCountLabel(verses.length)}) ✦`
                : `✦ ${getBookNameForLanguage(book, primaryLanguage)} ${chPrimary} (${formatNumber(
                    verses.length,
                    sysLang
                  )}) • ${getBookNameForLanguage(book, secondaryLanguage)} ${chSecondary} ✦`}
            </p>
          </div>
        )}
      </div>

      {/* Central Spine Shadow for Book Spread */}
      {readingMode === ReadingMode.BILINGUAL_SPREAD && (
        <div
          aria-hidden="true"
          className="pointer-events-none absolute inset-y-0 left-1/2 z-10 w-3.5 -translate-x-1/2"
          style={{
            background: `linear-gradient(90deg, transparent 0%, ${cssWithAlpha(
              palette.spineShadow,
              palette.isDark ? 0.45 : 0.14
            )} 30%, ${cssWithAlpha(
              palette.spineShadow,
              palette.isDark ? 0.62 : 0.24
            )} 50%, ${cssWithAlpha(
              palette.spineShadow,
              palette.isDark ? 0.42 : 0.12
            )} 70%, transparent 100%)`,
          }}
        />
      )}
    </div>
  );
};

const MissingLanguageCard: React.FC<{
  book: BibleBook;
  language: BibleLanguage;
  palette: BibleBookPalette;
  onDownload: () => void;
  onOpenDownloads: () => void;
}> = ({ book, language, palette, onDownload, onOpenDownloads }) => {
  const { strings, preferences } = useBibleApp();
  const sysLang = preferences.systemLanguage;
  const isSupported = languageSupportsBook(language, book.id);
  const bookLabel = sysLang === SystemLanguage.NEPALI ? book.nameNe : book.nameEn;
  const langLabel = strings.bibleLanguageName(language);
  const meta = BIBLE_LANGUAGE_META[language];

  return (
    <LiquidGlassSurface className="rounded-2xl p-3">
      <div className="flex items-center gap-2">
        <Info className="h-4 w-4 shrink-0" style={{ color: palette.leatherAccent }} />
        <span className="text-[13px] font-bold" style={{ color: palette.leatherAccent }}>
          {isSupported
            ? strings.langNotDownloadedTitle(langLabel, bookLabel)
            : strings.langScopeNoticeTitle(langLabel)}
        </span>
      </div>
      <p className="mt-1 text-xs" style={{ color: palette.inkSecondary }}>
        {isSupported
          ? strings.langNotDownloadedBody(bookLabel, meta.nativeName)
          : strings.langUnavailableBody(bookLabel, langLabel)}
      </p>
      {isSupported && (
        <div className="mt-2 flex flex-wrap gap-2">
          <button
            type="button"
            onClick={onDownload}
            className="inline-flex h-8 items-center gap-1.5 rounded-lg px-3 text-[11.5px] font-semibold text-white"
            style={{ backgroundColor: palette.leatherAccent }}
          >
            <Download className="h-3.5 w-3.5" />
            {strings.downloadBookButton(bookLabel)}
          </button>
          <button
            type="button"
            onClick={onOpenDownloads}
            className="inline-flex h-8 items-center rounded-lg border px-2.5 text-[11.5px] font-semibold"
            style={{ borderColor: palette.leatherAccent, color: palette.leatherAccent }}
          >
            {strings.allDownloadsButton}
          </button>
        </div>
      )}
    </LiquidGlassSurface>
  );
};
