import React from 'react';
import {
  BookOpen,
  Bookmark,
  ExternalLink,
  Languages,
  Palette,
  Search,
  Settings,
  Share2,
} from 'lucide-react';
import {
  BIBLE_LANGUAGE_META,
  BibleLanguage,
  SystemLanguage,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { COLORS, argbToCss, cssWithAlpha } from '../theme/themeTokens';
import { buildWhatsAppUrl, formatNumber, formatVerseShareText, toDevanagari } from '../utils/formatters';
import { LiquidGlassSurface } from '../components/LiquidGlassShell';

const BIBLE_COM_NNRV_URL = 'https://www.bible.com/bible/1483/GEN.1.NNRV';

export const HomeScreen: React.FC = () => {
  const {
    books,
    preferences,
    dailyVerse,
    tokens,
    palette,
    strings,
    navigate,
    openChapter,
    openScripturePicker,
    openLanguageSheet,
  } = useBibleApp();

  const sysLang = preferences.systemLanguage;
  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';
  const secondaryHeaderFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Playfair Display", Georgia, serif'
      : '"Noto Serif Devanagari", serif';

  const lastBook = books.find((b) => b.id === preferences.lastBookId) ?? null;
  const primaryMeta = BIBLE_LANGUAGE_META[preferences.primaryLanguage];
  const secondaryMeta = BIBLE_LANGUAGE_META[preferences.secondaryLanguage];

  const continueLabel = lastBook
    ? strings.continueReadingPrefix(
        sysLang === SystemLanguage.NEPALI ? lastBook.nameNe : lastBook.nameEn,
        formatNumber(preferences.lastChapter, sysLang),
        formatNumber(preferences.lastVerse, sysLang)
      )
    : strings.openHolyBible;

  const goldShimmer = argbToCss(COLORS.GoldShimmer, 1);
  const burgundyDeep = argbToCss(COLORS.BurgundyDeep, 1);

  return (
    <div className="flex-1 overflow-y-auto pb-28">
      <div className="mx-auto flex w-full max-w-[680px] flex-col gap-4 px-4 pt-3">
        {/* Top Header */}
        <header className="flex items-center justify-between gap-2">
          <div className="min-w-0 flex-1">
            <h1
              className="truncate text-[24px] font-bold leading-tight"
              style={{
                fontFamily: headerFont,
                letterSpacing: sysLang === SystemLanguage.NEPALI ? '0' : '0.09em',
                color: tokens.textPrimary,
              }}
            >
              {strings.appTitle}
            </h1>
            <p className="truncate text-xs font-medium" style={{ color: tokens.textSecondary }}>
              {strings.appSubtitleLine}
            </p>
          </div>
          <div className="flex shrink-0 items-center">
            <button
              type="button"
              onClick={() => navigate('theme_studio')}
              aria-label={strings.homeThemesDesc}
              title={strings.homeThemesDesc}
              className="rounded-full p-2.5 transition-colors hover:bg-black/5"
              style={{ color: tokens.accentColor }}
            >
              <Palette className="h-5 w-5" />
            </button>
            <button
              type="button"
              onClick={openLanguageSheet}
              aria-label={strings.homeLanguagesDesc}
              title={strings.homeLanguagesDesc}
              className="rounded-full p-2.5 transition-colors hover:bg-black/5"
              style={{ color: tokens.textPrimary }}
            >
              <Languages className="h-5 w-5" />
            </button>
            <button
              type="button"
              onClick={() => navigate('search')}
              aria-label={strings.homeSearchDesc}
              title={strings.homeSearchDesc}
              className="rounded-full p-2.5 transition-colors hover:bg-black/5"
              style={{ color: tokens.textPrimary }}
            >
              <Search className="h-5 w-5" />
            </button>
            <button
              type="button"
              onClick={() => navigate('settings')}
              aria-label={strings.homeSettingsDesc}
              title={strings.homeSettingsDesc}
              className="rounded-full p-2.5 transition-colors hover:bg-black/5"
              style={{ color: tokens.textPrimary }}
            >
              <Settings className="h-5 w-5" />
            </button>
          </div>
        </header>

        {/* 1. Classic Holy Bible Leather Cover Hero Card + Continue Reading */}
        <section
          className="relative h-[275px] w-full overflow-hidden rounded-[22px] shadow-xl"
          style={{ backgroundColor: burgundyDeep }}
        >
          <img
            src="/images/img_bible_cover.jpg"
            alt={strings.homeBibleCoverDesc}
            className="absolute inset-0 h-full w-full object-cover"
          />
          <div
            className="absolute inset-0"
            style={{
              background: `linear-gradient(180deg, ${argbToCss(
                COLORS.BurgundyDeep,
                0.55
              )} 0%, ${argbToCss(COLORS.BurgundyDeep, 0.35)} 45%, ${argbToCss(
                COLORS.BurgundyDeep,
                0.9
              )} 100%)`,
            }}
          />

          {/* Active Language Pair Ribbon Badge */}
          <button
            type="button"
            onClick={openLanguageSheet}
            className="absolute right-6 top-0 z-10 rounded-b-[10px] px-2.5 py-1.5 text-[10.5px] font-bold tracking-wider shadow"
            style={{
              backgroundColor: goldShimmer,
              color: burgundyDeep,
            }}
          >
            {primaryMeta.shortBadge} • {secondaryMeta.shortBadge}
          </button>

          <div className="relative z-10 flex h-full flex-col items-center justify-between p-5">
            <div className="h-1.5" />

            <div className="flex flex-col items-center text-center">
              <h2
                className="text-[30px] font-bold leading-tight"
                style={{
                  fontFamily: headerFont,
                  letterSpacing: sysLang === SystemLanguage.NEPALI ? '0.03em' : '0.18em',
                  color: goldShimmer,
                }}
              >
                {strings.heroTitle}
              </h2>
              <p
                className="mt-1 text-[20px] font-semibold"
                style={{
                  fontFamily: secondaryHeaderFont,
                  color: '#FFF4DC',
                }}
              >
                {strings.heroNepaliTitle}
              </p>
              <p className="mt-1 text-xs" style={{ color: '#EAD9BA' }}>
                {strings.heroEditionFormat(
                  strings.bibleLanguageName(preferences.primaryLanguage),
                  strings.bibleLanguageName(preferences.secondaryLanguage)
                )}
              </p>
            </div>

            <button
              type="button"
              onClick={() => navigate('reader')}
              className="flex h-[50px] w-full items-center justify-center gap-2 rounded-[14px] px-4 text-sm font-bold shadow-md transition-transform hover:brightness-105 active:scale-[0.99]"
              style={{
                backgroundColor: goldShimmer,
                color: burgundyDeep,
              }}
            >
              <BookOpen className="h-5 w-5 shrink-0" />
              <span className="truncate">{continueLabel}</span>
            </button>
          </div>
        </section>

        {/* Old Testament & New Testament Quick Entry */}
        <div className="grid grid-cols-2 gap-2.5">
          <LiquidGlassSurface
            tokens={tokens}
            onClick={() => openScripturePicker('OT')}
            className="rounded-2xl px-3.5 py-2.5 text-left"
          >
            <div
              className="text-[14px] font-bold"
              style={{ fontFamily: headerFont, color: tokens.textPrimary }}
            >
              {strings.oldTestament}
            </div>
            <div className="text-[11px]" style={{ color: tokens.textSecondary }}>
              {strings.oldTestamentSubtitle}
            </div>
          </LiquidGlassSurface>
          <LiquidGlassSurface
            tokens={tokens}
            onClick={() => openScripturePicker('NT')}
            className="rounded-2xl px-3.5 py-2.5 text-left"
          >
            <div
              className="text-[14px] font-bold"
              style={{ fontFamily: headerFont, color: tokens.textPrimary }}
            >
              {strings.newTestament}
            </div>
            <div className="text-[11px]" style={{ color: tokens.textSecondary }}>
              {strings.newTestamentSubtitle}
            </div>
          </LiquidGlassSurface>
        </div>

        {/* 2. Daily Bible Verse Devotional Card */}
        {dailyVerse && (
          <LiquidGlassSurface tokens={tokens} elevated className="relative overflow-hidden rounded-[22px]">
            <img
              src="/images/img_daily_verse_banner.jpg"
              alt=""
              aria-hidden="true"
              className="pointer-events-none absolute inset-0 h-full w-full object-cover"
              style={{ opacity: palette.isDark ? 0.14 : 0.32 }}
            />
            <div className="relative z-10 p-[18px]">
              <div className="flex items-center justify-between gap-2">
                <span
                  className="rounded-lg px-2.5 py-1 text-[11px] font-bold tracking-wider"
                  style={{
                    backgroundColor: cssWithAlpha(palette.leatherAccent, 0.14),
                    color: palette.leatherAccent,
                  }}
                >
                  {strings.dailyVerseBadge}
                </span>

                <a
                  href={buildWhatsAppUrl(
                    formatVerseShareText(
                      dailyVerse.book,
                      dailyVerse.verse,
                      preferences.primaryLanguage,
                      preferences.secondaryLanguage,
                      strings.shareAppBrand
                    )
                  )}
                  target="_blank"
                  rel="noopener noreferrer"
                  aria-label={strings.shareDailyVerseDesc}
                  title={strings.shareDailyVerseDesc}
                  className="rounded-full p-2 transition-colors hover:bg-black/5"
                  style={{ color: palette.leatherAccent }}
                >
                  <Share2 className="h-[18px] w-[18px]" />
                </a>
              </div>

              <div className="mt-2.5 space-y-2">
                {dailyVerse.verse.textEn.trim() && (
                  <p
                    className="text-[17px] leading-[23px]"
                    style={{
                      fontFamily:
                        preferences.themeConfig.fontStyle === 'APPLE_CLEAN_SANS'
                          ? '"Inter", sans-serif'
                          : preferences.themeConfig.fontStyle === 'MODERN_SANS'
                          ? '"Plus Jakarta Sans", sans-serif'
                          : preferences.themeConfig.fontStyle === 'LITERARY_SERIF'
                          ? '"Lora", Georgia, serif'
                          : preferences.themeConfig.fontStyle === 'BOOK_SERIF'
                          ? '"Merriweather", Georgia, serif'
                          : '"Crimson Pro", Georgia, serif',
                      color: tokens.textPrimary,
                    }}
                  >
                    “{dailyVerse.verse.textEn}”
                  </p>
                )}

                {(() => {
                  const preferOncvDaily =
                    preferences.primaryLanguage === BibleLanguage.ONCV ||
                    (preferences.secondaryLanguage === BibleLanguage.ONCV &&
                      preferences.primaryLanguage !== BibleLanguage.NEPALI);
                  const neTxt = preferOncvDaily
                    ? dailyVerse.verse.textOncv.trim() || dailyVerse.verse.textNe
                    : dailyVerse.verse.textNe.trim() || dailyVerse.verse.textOncv;

                  if (!neTxt) return null;
                  return (
                    <p
                      className="text-[15.5px] leading-[23px]"
                      style={{
                        fontFamily: '"Noto Serif Devanagari", serif',
                        color: tokens.textSecondary,
                      }}
                    >
                      “{neTxt}”
                    </p>
                  );
                })()}
              </div>

              <div className="mt-3 flex items-center justify-between gap-2">
                <div
                  className="min-w-0 flex-1 text-[13.5px] font-semibold italic"
                  style={{
                    fontFamily: '"Playfair Display", Georgia, serif',
                    color: tokens.accentColor,
                  }}
                >
                  {dailyVerse.book.nameEn} {dailyVerse.verse.chapter}:{dailyVerse.verse.verse} •{' '}
                  {dailyVerse.book.nameNe} {toDevanagari(dailyVerse.verse.chapter)}:
                  {toDevanagari(dailyVerse.verse.verse)}
                </div>
                <button
                  type="button"
                  onClick={() => {
                    void openChapter(
                      dailyVerse.book.id,
                      dailyVerse.verse.chapter,
                      dailyVerse.verse.verse
                    );
                    navigate('reader');
                  }}
                  className="shrink-0 rounded-xl border px-3 py-1.5 text-xs font-semibold transition-colors hover:bg-black/5"
                  style={{
                    borderColor: tokens.accentColor,
                    color: tokens.accentColor,
                  }}
                >
                  {strings.readChapter}
                </button>
              </div>
            </div>
          </LiquidGlassSurface>
        )}

        {/* 3. External Bible.com NNRV Nepali Bible Shortcut */}
        <LiquidGlassSurface tokens={tokens} className="rounded-[20px] p-4">
          <div className="flex items-center gap-3">
            <div
              className="flex h-[46px] w-[46px] shrink-0 items-center justify-center rounded-[14px]"
              style={{
                backgroundColor: tokens.accentColor,
                color: tokens.onAccentColor,
              }}
            >
              <BookOpen className="h-5 w-5" />
            </div>
            <div className="min-w-0 flex-1">
              <div
                className="text-[16px] font-bold"
                style={{ fontFamily: headerFont, color: tokens.textPrimary }}
              >
                {strings.nnrvShortcutTitle}
              </div>
              <div className="text-[13px]" style={{ color: tokens.textSecondary }}>
                {strings.nnrvShortcutSubtitle}
              </div>
              <div
                className="truncate text-[11px]"
                style={{ color: cssWithAlpha(tokens.textSecondary, 0.85) }}
              >
                {strings.nnrvShortcutMeta}
              </div>
            </div>
          </div>

          <a
            href={BIBLE_COM_NNRV_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="mt-3 flex w-full items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-[13.5px] font-bold transition-transform hover:brightness-105 active:scale-[0.99]"
            style={{
              backgroundColor: tokens.accentColor,
              color: tokens.onAccentColor,
            }}
          >
            <BookOpen className="h-[18px] w-[18px]" />
            <span>{strings.nnrvShortcutButton}</span>
            <ExternalLink className="h-4 w-4" />
          </a>
        </LiquidGlassSurface>

        {/* 4. Quick Actions Grid */}
        <div className="grid grid-cols-3 gap-2.5">
          <QuickActionTile
            icon={<Languages className="h-[22px] w-[22px]" />}
            label={strings.quickLanguages}
            sub={strings.quickLanguagesSub}
            onClick={openLanguageSheet}
          />
          <QuickActionTile
            icon={<Search className="h-[22px] w-[22px]" />}
            label={strings.quickSearch}
            sub={strings.quickSearchSub}
            onClick={() => navigate('search')}
          />
          <QuickActionTile
            icon={<Bookmark className="h-[22px] w-[22px]" />}
            label={strings.quickSaved}
            sub={strings.quickSavedSub}
            onClick={() => navigate('library')}
          />
        </div>
      </div>
    </div>
  );
};

const QuickActionTile: React.FC<{
  icon: React.ReactNode;
  label: string;
  sub: string;
  onClick: () => void;
}> = ({ icon, label, sub, onClick }) => {
  const { tokens } = useBibleApp();
  return (
    <LiquidGlassSurface
      tokens={tokens}
      onClick={onClick}
      className="flex flex-col items-center rounded-2xl px-2 py-3 text-center"
    >
      <span style={{ color: tokens.accentColor }}>{icon}</span>
      <span className="mt-1.5 text-[13px] font-semibold" style={{ color: tokens.textPrimary }}>
        {label}
      </span>
      <span className="text-[10.5px]" style={{ color: tokens.textSecondary }}>
        {sub}
      </span>
    </LiquidGlassSurface>
  );
};
