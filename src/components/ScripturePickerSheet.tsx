import React, { useEffect, useMemo, useState } from 'react';
import { ArrowLeft, BookOpen, Search, X } from 'lucide-react';
import {
  BIBLE_LANGUAGE_META,
  BibleBook,
  BibleLanguage,
  SystemLanguage,
  getBookNameForLanguage,
  getFontFamilyForBibleLanguage,
  languageSupportsBook,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { cssWithAlpha } from '../theme/themeTokens';
import { formatNumber, toDevanagari } from '../utils/formatters';
import { LiquidGlassSurface } from './LiquidGlassShell';

interface ScripturePickerSheetProps {
  open: boolean;
  initialBookId: number;
  initialChapter: number;
  initialTestamentFilter?: 'OT' | 'NT' | null;
  onSelectLocation: (bookId: number, chapter: number, verse: number | null) => void;
  onDismiss: () => void;
}

export const ScripturePickerSheet: React.FC<ScripturePickerSheetProps> = ({
  open,
  initialBookId,
  initialChapter,
  initialTestamentFilter = null,
  onSelectLocation,
  onDismiss,
}) => {
  const { books, preferences, tokens, palette, strings, loadChapterVerseCount } = useBibleApp();
  const sysLang = preferences.systemLanguage;
  const primaryLang = preferences.primaryLanguage;
  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';

  const [step, setStep] = useState<0 | 1 | 2>(0);
  const [testamentFilter, setTestamentFilter] = useState<'OT' | 'NT' | null>(
    initialTestamentFilter
  );
  const [bookSearch, setBookSearch] = useState('');
  const [chosenBook, setChosenBook] = useState<BibleBook | null>(null);
  const [chosenChapter, setChosenChapter] = useState<number>(initialChapter);
  const [verseCount, setVerseCount] = useState<number>(0);

  useEffect(() => {
    if (open) {
      setStep(0);
      setTestamentFilter(initialTestamentFilter);
      setBookSearch('');
      setChosenBook(books.find((b) => b.id === initialBookId) ?? books[0] ?? null);
      setChosenChapter(initialChapter);
    }
  }, [open, initialBookId, initialChapter, initialTestamentFilter, books]);

  useEffect(() => {
    if (open && step === 2 && chosenBook) {
      let active = true;
      void loadChapterVerseCount(chosenBook.id, chosenChapter).then((cnt) => {
        if (active) setVerseCount(Math.max(1, cnt));
      });
      return () => {
        active = false;
      };
    }
  }, [open, step, chosenBook, chosenChapter, loadChapterVerseCount]);

  const filteredBooks = useMemo(() => {
    const q = bookSearch.trim().toLowerCase();
    return books.filter((b) => {
      const matchesTestament = !testamentFilter || b.testament === testamentFilter;
      const matchesQuery =
        !q ||
        b.nameEn.toLowerCase().includes(q) ||
        b.nameNe.includes(bookSearch.trim()) ||
        b.nameKo.includes(bookSearch.trim()) ||
        b.abbrev.toLowerCase().includes(q);
      return matchesTestament && matchesQuery;
    });
  }, [books, testamentFilter, bookSearch]);

  if (!open) return null;

  const bookLabel = chosenBook
    ? sysLang === SystemLanguage.NEPALI
      ? chosenBook.nameNe
      : chosenBook.nameEn
    : '';

  const headerTitle =
    step === 0
      ? strings.selectBookTitle
      : step === 1
      ? `${sysLang === SystemLanguage.NEPALI ? chosenBook?.nameNe ?? '' : chosenBook?.nameEn ?? ''} • ${
          chosenBook ? getBookNameForLanguage(chosenBook, primaryLang) : ''
        }`
      : strings.selectVerseTitle(bookLabel, chosenChapter);

  const headerSubtitle =
    step === 0
      ? strings.booksCountSubtitle
      : step === 1
      ? strings.chooseChapterSubtitle(chosenBook?.chapterCount ?? 0)
      : strings.tapVerseOrChapterSubtitle;

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
        className="animate-modal-pop flex h-[84vh] w-full max-w-2xl flex-col sm:h-[80vh]"
        onClick={(e) => e.stopPropagation()}
      >
        <LiquidGlassSurface
          tokens={tokens}
          elevated
          className="flex h-full flex-col rounded-t-3xl p-4 sm:rounded-3xl"
        >
          {/* Header */}
          <div className="flex shrink-0 items-center justify-between gap-2 pb-2.5">
            <div className="flex min-w-0 flex-1 items-center gap-2">
              {step > 0 ? (
                <button
                  type="button"
                  onClick={() => setStep((s) => (s === 2 ? 1 : 0))}
                  aria-label={strings.back}
                  className="rounded-full p-2 transition-colors hover:bg-black/5"
                  style={{ color: tokens.textPrimary }}
                >
                  <ArrowLeft className="h-5 w-5" />
                </button>
              ) : (
                <BookOpen className="mr-1 h-5 w-5 shrink-0" style={{ color: tokens.accentColor }} />
              )}
              <div className="min-w-0">
                <h2
                  className="truncate text-[18px] font-bold"
                  style={{ fontFamily: headerFont, color: tokens.textPrimary }}
                >
                  {headerTitle}
                </h2>
                <p className="truncate text-xs" style={{ color: tokens.textSecondary }}>
                  {headerSubtitle}
                </p>
              </div>
            </div>
            <button
              type="button"
              onClick={onDismiss}
              aria-label={strings.closePickerDesc}
              className="rounded-full p-2 transition-colors hover:bg-black/5"
              style={{ color: tokens.textPrimary }}
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Step 0: Books */}
          {step === 0 && (
            <>
              <div className="shrink-0 space-y-2 pb-2">
                <div
                  className="flex items-center gap-2 rounded-xl border px-3 py-2"
                  style={{
                    borderColor: tokens.borderTop,
                    backgroundColor: tokens.glassSurface,
                  }}
                >
                  <Search className="h-4 w-4 shrink-0" style={{ color: tokens.accentColor }} />
                  <input
                    type="text"
                    value={bookSearch}
                    onChange={(e) => setBookSearch(e.target.value)}
                    placeholder={strings.searchBookPlaceholder}
                    className="w-full bg-transparent text-sm outline-none placeholder:opacity-60"
                    style={{ color: tokens.textPrimary }}
                  />
                </div>
                <div className="flex flex-wrap gap-2">
                  {[
                    { id: null, label: strings.filterAll66 },
                    { id: 'OT' as const, label: strings.filterOld39 },
                    { id: 'NT' as const, label: strings.filterNew27 },
                  ].map((chip) => {
                    const selected = testamentFilter === chip.id;
                    return (
                      <button
                        key={String(chip.id)}
                        type="button"
                        onClick={() => setTestamentFilter(chip.id)}
                        className="rounded-full border px-3 py-1 text-xs font-semibold transition-colors"
                        style={{
                          backgroundColor: selected
                            ? cssWithAlpha(tokens.accentColor, 0.2)
                            : 'transparent',
                          borderColor: selected ? tokens.accentColor : tokens.borderTop,
                          color: selected ? tokens.accentColor : tokens.textPrimary,
                        }}
                      >
                        {chip.label}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="flex-1 space-y-1.5 overflow-y-auto pb-4 pr-0.5">
                {filteredBooks.map((book) => {
                  const isCurrent = book.id === initialBookId;
                  const secondaryTitle =
                    sysLang === SystemLanguage.NEPALI
                      ? !BIBLE_LANGUAGE_META[primaryLang].usesDevanagari &&
                        languageSupportsBook(primaryLang, book.id)
                        ? getBookNameForLanguage(book, primaryLang)
                        : book.nameEn
                      : primaryLang !== BibleLanguage.ENGLISH &&
                        languageSupportsBook(primaryLang, book.id)
                      ? getBookNameForLanguage(book, primaryLang)
                      : book.nameNe;

                  const secondaryFont =
                    sysLang === SystemLanguage.NEPALI
                      ? !BIBLE_LANGUAGE_META[primaryLang].usesDevanagari &&
                        languageSupportsBook(primaryLang, book.id)
                        ? getFontFamilyForBibleLanguage(primaryLang, tokens.fontStyle)
                        : '"Playfair Display", Georgia, serif'
                      : primaryLang !== BibleLanguage.ENGLISH &&
                        languageSupportsBook(primaryLang, book.id)
                      ? getFontFamilyForBibleLanguage(primaryLang, tokens.fontStyle)
                      : '"Noto Serif Devanagari", serif';

                  return (
                    <button
                      key={book.id}
                      type="button"
                      onClick={() => {
                        setChosenBook(book);
                        setStep(1);
                      }}
                      className="flex w-full items-center justify-between rounded-xl border px-3.5 py-2.5 text-left transition-colors"
                      style={{
                        backgroundColor: isCurrent
                          ? cssWithAlpha(tokens.accentColor, 0.22)
                          : tokens.glassSurface,
                        borderColor: isCurrent ? tokens.accentColor : tokens.borderTop,
                      }}
                    >
                      <div className="flex min-w-0 items-center gap-3">
                        <span
                          className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-xs font-bold"
                          style={{
                            backgroundColor: cssWithAlpha(tokens.accentColor, 0.14),
                            color: tokens.accentColor,
                          }}
                        >
                          {formatNumber(book.id, sysLang)}
                        </span>
                        <div className="min-w-0">
                          <div
                            className="truncate text-[15.5px] font-semibold"
                            style={{ fontFamily: headerFont, color: tokens.textPrimary }}
                          >
                            {sysLang === SystemLanguage.NEPALI ? book.nameNe : book.nameEn}
                          </div>
                          <div
                            className="truncate text-[13.5px]"
                            style={{ fontFamily: secondaryFont, color: tokens.textSecondary }}
                          >
                            {secondaryTitle}
                          </div>
                        </div>
                      </div>
                      <span
                        className="ml-2 shrink-0 text-xs font-semibold"
                        style={{ color: tokens.accentColor }}
                      >
                        {strings.chaptersShort(book.chapterCount)}
                      </span>
                    </button>
                  );
                })}
              </div>
            </>
          )}

          {/* Step 1: Chapters Grid */}
          {step === 1 && chosenBook && (
            <div className="grid flex-1 grid-cols-5 content-start gap-2 overflow-y-auto pb-4 sm:grid-cols-6">
              {Array.from({ length: chosenBook.chapterCount }, (_, idx) => idx + 1).map((ch) => {
                const isSelected = chosenBook.id === initialBookId && ch === initialChapter;
                return (
                  <button
                    key={ch}
                    type="button"
                    onClick={() => {
                      setChosenChapter(ch);
                      setStep(2);
                    }}
                    className="flex aspect-square flex-col items-center justify-center rounded-xl border transition-transform active:scale-95"
                    style={{
                      backgroundColor: isSelected ? tokens.accentColor : tokens.glassSurface,
                      borderColor: tokens.isGlassActive
                        ? tokens.borderTop
                        : cssWithAlpha(palette.goldAccent, 0.35),
                    }}
                  >
                    <span
                      className="text-[15px] font-bold"
                      style={{
                        color: isSelected ? tokens.onAccentColor : tokens.textPrimary,
                      }}
                    >
                      {sysLang === SystemLanguage.NEPALI ? toDevanagari(ch) : ch}
                    </span>
                    <span
                      className="text-[11px]"
                      style={{
                        color: isSelected
                          ? cssWithAlpha(tokens.onAccentColor, 0.85)
                          : tokens.textSecondary,
                      }}
                    >
                      {sysLang === SystemLanguage.NEPALI ? ch : toDevanagari(ch)}
                    </span>
                  </button>
                );
              })}
            </div>
          )}

          {/* Step 2: Verses Grid */}
          {step === 2 && chosenBook && (
            <>
              <div className="shrink-0 pb-2">
                <button
                  type="button"
                  onClick={() => onSelectLocation(chosenBook.id, chosenChapter, null)}
                  className="text-sm font-semibold underline-offset-2 hover:underline"
                  style={{ color: tokens.accentColor }}
                >
                  {strings.openChapterFromVerse1(chosenChapter)}
                </button>
              </div>
              <div className="grid flex-1 grid-cols-5 content-start gap-2 overflow-y-auto pb-4 sm:grid-cols-7">
                {Array.from({ length: Math.max(1, verseCount) }, (_, idx) => idx + 1).map((v) => (
                  <button
                    key={v}
                    type="button"
                    onClick={() => onSelectLocation(chosenBook.id, chosenChapter, v)}
                    className="flex aspect-square items-center justify-center rounded-xl border text-sm font-semibold transition-transform active:scale-95"
                    style={{
                      backgroundColor: tokens.glassSurface,
                      borderColor: tokens.borderTop,
                      color: tokens.textPrimary,
                    }}
                  >
                    {formatNumber(v, sysLang)}
                  </button>
                ))}
              </div>
            </>
          )}
        </LiquidGlassSurface>
      </div>
    </div>
  );
};
