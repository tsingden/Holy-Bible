import React from 'react';
import { BookOpen, Search, X } from 'lucide-react';
import {
  ALL_BIBLE_LANGUAGES,
  BIBLE_LANGUAGE_META,
  BibleLanguage,
  SystemLanguage,
  getFontFamilyForBibleLanguage,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { cssWithAlpha } from '../theme/themeTokens';
import { formatNumber, toDevanagari } from '../utils/formatters';
import { LiquidGlassSurface } from '../components/LiquidGlassShell';

export const SearchScreen: React.FC = () => {
  const {
    preferences,
    tokens,
    palette,
    strings,
    searchQuery,
    searchTestamentFilter,
    searchLanguageFilter,
    searchResults,
    isSearching,
    updateSearchQuery,
    updateSearchFilter,
    updateSearchLanguageFilter,
    openChapter,
    navigate,
  } = useBibleApp();

  const sysLang = preferences.systemLanguage;
  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <div className="mx-auto flex h-full w-full max-w-[680px] flex-col px-4 pt-3">
        <h1
          className="text-[22px] font-bold"
          style={{ fontFamily: headerFont, color: tokens.textPrimary }}
        >
          {strings.searchScriptureTitle}
        </h1>

        {/* Search Input */}
        <div
          className="mt-2.5 flex items-center gap-2 rounded-[14px] border px-3 py-2.5"
          style={{
            backgroundColor: tokens.glassSurface,
            borderColor: tokens.borderTop,
          }}
        >
          <Search className="h-4 w-4 shrink-0" style={{ color: tokens.accentColor }} />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => updateSearchQuery(e.target.value)}
            placeholder={strings.searchPlaceholder}
            className="w-full bg-transparent text-sm outline-none placeholder:opacity-60"
            style={{ color: tokens.textPrimary }}
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => updateSearchQuery('')}
              aria-label={strings.clearSearchDesc}
              className="rounded-full p-1 hover:bg-black/5"
              style={{ color: tokens.textSecondary }}
            >
              <X className="h-4 w-4" />
            </button>
          )}
        </div>

        {/* Testament Filter Chips */}
        <div className="mt-2.5 flex flex-wrap gap-2">
          {[
            { id: null, label: strings.filterAllBible },
            { id: 'OT' as const, label: strings.filterOldShort },
            { id: 'NT' as const, label: strings.filterNewShort },
          ].map((chip) => {
            const selected = searchTestamentFilter === chip.id;
            return (
              <button
                key={String(chip.id)}
                type="button"
                onClick={() => updateSearchFilter(chip.id)}
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

        {/* Language Filter Chips */}
        <div className="no-scrollbar mt-2 flex gap-1.5 overflow-x-auto pb-1">
          <button
            type="button"
            onClick={() => updateSearchLanguageFilter(null)}
            className="shrink-0 rounded-full border px-3 py-1 text-xs font-semibold transition-colors"
            style={{
              backgroundColor:
                searchLanguageFilter === null
                  ? cssWithAlpha(tokens.accentColor, 0.2)
                  : 'transparent',
              borderColor:
                searchLanguageFilter === null ? tokens.accentColor : tokens.borderTop,
              color: searchLanguageFilter === null ? tokens.accentColor : tokens.textPrimary,
            }}
          >
            {strings.filterAllLanguages}
          </button>
          {ALL_BIBLE_LANGUAGES.map((lang) => {
            const selected = searchLanguageFilter === lang;
            return (
              <button
                key={lang}
                type="button"
                onClick={() => updateSearchLanguageFilter(lang)}
                className="shrink-0 rounded-full border px-3 py-1 text-xs font-semibold transition-colors"
                style={{
                  backgroundColor: selected
                    ? cssWithAlpha(tokens.accentColor, 0.2)
                    : 'transparent',
                  borderColor: selected ? tokens.accentColor : tokens.borderTop,
                  color: selected ? tokens.accentColor : tokens.textPrimary,
                }}
              >
                {strings.bibleLanguageName(lang)}
              </button>
            );
          })}
        </div>

        {/* Results / Empty / Loading */}
        <div className="mt-2 flex min-h-0 flex-1 flex-col">
          {searchQuery.trim().length < 2 ? (
            <div className="flex flex-1 flex-col items-center justify-center px-6 text-center">
              <BookOpen className="mb-2 h-8 w-8" style={{ color: palette.goldAccent }} />
              <p
                className="whitespace-pre-line text-sm"
                style={{ color: tokens.textSecondary }}
              >
                {strings.searchPromptEmpty}
              </p>
            </div>
          ) : isSearching ? (
            <div className="flex flex-1 items-center justify-center">
              <div
                className="h-8 w-8 animate-spin rounded-full border-4 border-t-transparent"
                style={{
                  borderColor: `${palette.leatherAccent} transparent ${palette.leatherAccent} ${palette.leatherAccent}`,
                }}
              />
            </div>
          ) : searchResults.length === 0 ? (
            <div className="flex flex-1 items-center justify-center px-6 text-center">
              <p className="text-base" style={{ color: tokens.textSecondary }}>
                {strings.searchNoResults(searchQuery)}
              </p>
            </div>
          ) : (
            <>
              <div
                className="pb-1.5 text-xs font-semibold"
                style={{ color: tokens.textSecondary }}
              >
                {strings.searchMatchedCount(searchResults.length)}
              </div>
              <div className="flex-1 space-y-2.5 overflow-y-auto pb-28 pr-0.5">
                {searchResults.map((item) => {
                  const refText =
                    sysLang === SystemLanguage.NEPALI
                      ? `${item.bookNameNe} ${formatNumber(
                          item.chapter,
                          sysLang
                        )}:${formatNumber(item.verse, sysLang)} • ${item.bookNameEn} ${
                          item.chapter
                        }:${item.verse}`
                      : `${item.bookNameEn} ${item.chapter}:${item.verse} • ${
                          item.bookNameNe
                        } ${toDevanagari(item.chapter)}:${toDevanagari(item.verse)}`;

                  const matchedText =
                    item.matchedLanguage === BibleLanguage.ENGLISH
                      ? item.textEn
                      : item.matchedLanguage === BibleLanguage.NEPALI
                      ? item.textNe
                      : item.matchedLanguage === BibleLanguage.ONCV
                      ? item.textOncv
                      : item.textKo;

                  const fallbackNe = item.textNe.trim() ? item.textNe : item.textOncv;

                  return (
                    <LiquidGlassSurface
                      key={`${item.bookId}:${item.chapter}:${item.verse}:${item.matchedLanguage}`}
                      tokens={tokens}
                      onClick={() => {
                        void openChapter(item.bookId, item.chapter, item.verse);
                        navigate('reader');
                      }}
                      className="rounded-2xl p-3.5 text-left"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span
                          className="min-w-0 flex-1 truncate text-[14.5px] font-bold"
                          style={{ fontFamily: headerFont, color: tokens.accentColor }}
                        >
                          {refText}
                        </span>
                        <span
                          className="shrink-0 rounded-md px-1.5 py-0.5 text-[10px] font-bold"
                          style={{
                            backgroundColor: cssWithAlpha(tokens.accentColor, 0.18),
                            color: tokens.accentColor,
                          }}
                        >
                          {strings.bibleLanguageName(item.matchedLanguage)}
                        </span>
                      </div>

                      {matchedText.trim() && (
                        <p
                          dir={BIBLE_LANGUAGE_META[item.matchedLanguage].isRtl ? 'rtl' : 'ltr'}
                          className="mt-1.5 text-[15.5px] leading-[22px]"
                          style={{
                            fontFamily: getFontFamilyForBibleLanguage(
                              item.matchedLanguage,
                              tokens.fontStyle
                            ),
                            color: tokens.textPrimary,
                          }}
                        >
                          {matchedText}
                        </p>
                      )}

                      {item.matchedLanguage !== BibleLanguage.ENGLISH && item.textEn.trim() ? (
                        <p
                          className="mt-1 text-sm leading-[19px]"
                          style={{
                            fontFamily: getFontFamilyForBibleLanguage(
                              BibleLanguage.ENGLISH,
                              tokens.fontStyle
                            ),
                            color: tokens.textSecondary,
                          }}
                        >
                          {item.textEn}
                        </p>
                      ) : item.matchedLanguage === BibleLanguage.ENGLISH &&
                        fallbackNe.trim() ? (
                        <p
                          className="mt-1 text-sm leading-[20px]"
                          style={{
                            fontFamily: '"Noto Serif Devanagari", serif',
                            color: tokens.textSecondary,
                          }}
                        >
                          {fallbackNe}
                        </p>
                      ) : null}
                    </LiquidGlassSurface>
                  );
                })}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
