export enum BibleLanguage {
  ENGLISH = 'ENGLISH',
  NEPALI = 'NEPALI',
  ONCV = 'ONCV',
  KOREAN = 'KOREAN',
}

export interface BibleLanguageMeta {
  id: BibleLanguage;
  code: 'en' | 'ne' | 'ne_oncv' | 'ko';
  packFile: string;
  displayName: string;
  nativeName: string;
  translationName: string;
  licenseNote: string;
  shortBadge: string;
  usesDevanagari: boolean;
  isRtl: boolean;
  otStart: number;
  otEnd: number;
  ntStart: number;
  ntEnd: number;
}

export const BIBLE_LANGUAGE_META: Record<BibleLanguage, BibleLanguageMeta> = {
  [BibleLanguage.ENGLISH]: {
    id: BibleLanguage.ENGLISH,
    code: 'en',
    packFile: '/data/pack_en.dat',
    displayName: 'English',
    nativeName: 'English (KJV)',
    translationName: 'King James Version',
    licenseNote: 'Public Domain • 66 Books (OT & NT)',
    shortBadge: 'EN',
    usesDevanagari: false,
    isRtl: false,
    otStart: 1,
    otEnd: 39,
    ntStart: 40,
    ntEnd: 66,
  },
  [BibleLanguage.NEPALI]: {
    id: BibleLanguage.NEPALI,
    code: 'ne',
    packFile: '/data/pack_ne.dat',
    displayName: 'Nepali (ULB)',
    nativeName: 'नेपाली (ULB)',
    translationName: 'Unlocked Literal Bible (ULB)',
    licenseNote: 'Door43 World Missions Community © 2019 • CC BY-SA 4.0 • 66 Books (OT & NT)',
    shortBadge: 'ULB',
    usesDevanagari: true,
    isRtl: false,
    otStart: 1,
    otEnd: 39,
    ntStart: 40,
    ntEnd: 66,
  },
  [BibleLanguage.ONCV]: {
    id: BibleLanguage.ONCV,
    code: 'ne_oncv',
    packFile: '/data/pack_ne_oncv.dat',
    displayName: 'Nepali (ONCV)',
    nativeName: 'नेपाली (ONCV)',
    translationName: 'Old Nepali Contemporary Version (ONCV)',
    licenseNote: 'Biblica, Inc. © 1998, 2006, 2021, 2024 • CC BY-SA 4.0 • 66 Books (OT & NT)',
    shortBadge: 'ONCV',
    usesDevanagari: true,
    isRtl: false,
    otStart: 1,
    otEnd: 39,
    ntStart: 40,
    ntEnd: 66,
  },
  [BibleLanguage.KOREAN]: {
    id: BibleLanguage.KOREAN,
    code: 'ko',
    packFile: '/data/pack_ko.dat',
    displayName: 'Korean',
    nativeName: '한국어 (개역한글)',
    translationName: 'Korean Revised Version (KRV)',
    licenseNote: 'Public Domain (1961) • 66 Books (OT & NT)',
    shortBadge: 'KO',
    usesDevanagari: false,
    isRtl: false,
    otStart: 1,
    otEnd: 39,
    ntStart: 40,
    ntEnd: 66,
  },
};

export const ALL_BIBLE_LANGUAGES: BibleLanguage[] = [
  BibleLanguage.ENGLISH,
  BibleLanguage.NEPALI,
  BibleLanguage.ONCV,
  BibleLanguage.KOREAN,
];

export function bibleLanguageFromCode(code?: string | null, fallback = BibleLanguage.ENGLISH): BibleLanguage {
  const found = ALL_BIBLE_LANGUAGES.find((l) => BIBLE_LANGUAGE_META[l].code === code);
  return found ?? fallback;
}

export function getLanguageSupportedBookIds(lang: BibleLanguage): number[] {
  const meta = BIBLE_LANGUAGE_META[lang];
  const ids: number[] = [];
  for (let i = meta.otStart; i <= meta.otEnd; i++) ids.push(i);
  for (let i = meta.ntStart; i <= meta.ntEnd; i++) ids.push(i);
  return ids;
}

export function languageSupportsBook(lang: BibleLanguage, bookId: number): boolean {
  const meta = BIBLE_LANGUAGE_META[lang];
  return (bookId >= meta.otStart && bookId <= meta.otEnd) || (bookId >= meta.ntStart && bookId <= meta.ntEnd);
}

export enum SystemLanguage {
  ENGLISH = 'ENGLISH',
  NEPALI = 'NEPALI',
}

export const SYSTEM_LANGUAGE_META: Record<
  SystemLanguage,
  { code: 'en' | 'ne'; displayName: string; nativeName: string }
> = {
  [SystemLanguage.ENGLISH]: { code: 'en', displayName: 'English', nativeName: 'English' },
  [SystemLanguage.NEPALI]: { code: 'ne', displayName: 'Nepali', nativeName: 'नेपाली' },
};

export function systemLanguageFromCode(code?: string | null): SystemLanguage {
  return code === 'ne' ? SystemLanguage.NEPALI : SystemLanguage.ENGLISH;
}

export enum ButtonStyleOption {
  LIQUID_GLASS = 'LIQUID_GLASS',
  PILL_FILLED = 'PILL_FILLED',
  OUTLINED_GLASS = 'OUTLINED_GLASS',
  CLASSIC_ELEVATED = 'CLASSIC_ELEVATED',
}

export enum NavBarAppearance {
  FLOATING_GLASS_DOCK = 'FLOATING_GLASS_DOCK',
  FROSTED_FULL_BAR = 'FROSTED_FULL_BAR',
  SOLID_MINIMAL = 'SOLID_MINIMAL',
}

export enum ScriptureFontStyle {
  CLASSIC_SERIF = 'CLASSIC_SERIF',
  LITERARY_SERIF = 'LITERARY_SERIF',
  BOOK_SERIF = 'BOOK_SERIF',
  APPLE_CLEAN_SANS = 'APPLE_CLEAN_SANS',
  MODERN_SANS = 'MODERN_SANS',
}

export const SCRIPTURE_FONT_META: Record<
  ScriptureFontStyle,
  { label: string; sampleTag: string; cssFamily: string }
> = {
  [ScriptureFontStyle.CLASSIC_SERIF]: {
    label: 'Crimson Pro',
    sampleTag: 'Classic Serif',
    cssFamily: '"Crimson Pro", Georgia, serif',
  },
  [ScriptureFontStyle.LITERARY_SERIF]: {
    label: 'Lora',
    sampleTag: 'Literary Serif',
    cssFamily: '"Lora", Georgia, serif',
  },
  [ScriptureFontStyle.BOOK_SERIF]: {
    label: 'Merriweather',
    sampleTag: 'Editorial Book',
    cssFamily: '"Merriweather", Georgia, serif',
  },
  [ScriptureFontStyle.APPLE_CLEAN_SANS]: {
    label: 'Inter',
    sampleTag: 'Clean Apple Sans',
    cssFamily: '"Inter", system-ui, sans-serif',
  },
  [ScriptureFontStyle.MODERN_SANS]: {
    label: 'Plus Jakarta Sans',
    sampleTag: 'Modern Geometric',
    cssFamily: '"Plus Jakarta Sans", system-ui, sans-serif',
  },
};

export function getFontFamilyForBibleLanguage(lang: BibleLanguage, style: ScriptureFontStyle): string {
  switch (lang) {
    case BibleLanguage.ENGLISH:
      return SCRIPTURE_FONT_META[style].cssFamily;
    case BibleLanguage.NEPALI:
    case BibleLanguage.ONCV:
      return '"Noto Serif Devanagari", "Crimson Pro", serif';
    case BibleLanguage.KOREAN:
      return '"Noto Serif KR", "Inter", serif';
  }
}

export enum ReadingPageBackground {
  MATCH_THEME = 'MATCH_THEME',
  FROSTED_GLASS = 'FROSTED_GLASS',
  CREAM_PARCHMENT = 'CREAM_PARCHMENT',
  PURE_WHITE = 'PURE_WHITE',
  SEPIA_WARM = 'SEPIA_WARM',
  NIGHT_OBSIDIAN = 'NIGHT_OBSIDIAN',
  AMOLED_BLACK = 'AMOLED_BLACK',
}

export enum ThemePreset {
  IOS_LIQUID_GLASS = 'IOS_LIQUID_GLASS',
  CRYSTAL_CLEAR_LIQUID_GLASS = 'CRYSTAL_CLEAR_LIQUID_GLASS',
  CRYSTAL_CLEAR_DARK_GLASS = 'CRYSTAL_CLEAR_DARK_GLASS',
  TRANSPARENT_GLASS = 'TRANSPARENT_GLASS',
  DARK_GLASS = 'DARK_GLASS',
  LIGHT_GLASS = 'LIGHT_GLASS',
  AMOLED_BLACK = 'AMOLED_BLACK',
  CLASSIC_BIBLE = 'CLASSIC_BIBLE',
  ELEGANT_BLUE = 'ELEGANT_BLUE',
  NATURE_GREEN = 'NATURE_GREEN',
  GOLDEN_PREMIUM = 'GOLDEN_PREMIUM',
}

export interface ThemePresetSpec {
  id: ThemePreset;
  displayName: string;
  subtitle: string;
  isDarkDefault: boolean;
  isGlassDefault: boolean;
  defaultBackgroundArgb: number;
  defaultSecondaryBgArgb: number;
  defaultAccentArgb: number;
  defaultTextArgb: number;
  defaultTransparency: number;
  defaultBlurRadius: number;
  defaultButtonStyle: ButtonStyleOption;
  defaultNavBarStyle: NavBarAppearance;
  defaultReadingPageBg: ReadingPageBackground;
}

export const THEME_PRESET_SPECS: Record<ThemePreset, ThemePresetSpec> = {
  [ThemePreset.IOS_LIQUID_GLASS]: {
    id: ThemePreset.IOS_LIQUID_GLASS,
    displayName: 'iOS Liquid Glass',
    subtitle: 'Frosted crystal translucency & luminous depth',
    isDarkDefault: false,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xffe6effa,
    defaultSecondaryBgArgb: 0xffd0e3fa,
    defaultAccentArgb: 0xff0a84ff,
    defaultTextArgb: 0xff0f172a,
    defaultTransparency: 0.58,
    defaultBlurRadius: 22,
    defaultButtonStyle: ButtonStyleOption.LIQUID_GLASS,
    defaultNavBarStyle: NavBarAppearance.FLOATING_GLASS_DOCK,
    defaultReadingPageBg: ReadingPageBackground.FROSTED_GLASS,
  },
  [ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS]: {
    id: ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS,
    displayName: 'Crystal Clear Liquid Glass',
    subtitle: 'Ultra-transparent light glass & prism refraction',
    isDarkDefault: false,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xffd9eeff,
    defaultSecondaryBgArgb: 0xffeee8ff,
    defaultAccentArgb: 0xff0077ff,
    defaultTextArgb: 0xff091326,
    defaultTransparency: 0.22,
    defaultBlurRadius: 12,
    defaultButtonStyle: ButtonStyleOption.LIQUID_GLASS,
    defaultNavBarStyle: NavBarAppearance.FLOATING_GLASS_DOCK,
    defaultReadingPageBg: ReadingPageBackground.FROSTED_GLASS,
  },
  [ThemePreset.CRYSTAL_CLEAR_DARK_GLASS]: {
    id: ThemePreset.CRYSTAL_CLEAR_DARK_GLASS,
    displayName: 'Crystal Clear Dark Glass',
    subtitle: 'Dark crystal glass with sapphire refraction',
    isDarkDefault: true,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xff07111f,
    defaultSecondaryBgArgb: 0xff102542,
    defaultAccentArgb: 0xff6ae2ff,
    defaultTextArgb: 0xfff8fbff,
    defaultTransparency: 0.20,
    defaultBlurRadius: 12,
    defaultButtonStyle: ButtonStyleOption.LIQUID_GLASS,
    defaultNavBarStyle: NavBarAppearance.FLOATING_GLASS_DOCK,
    defaultReadingPageBg: ReadingPageBackground.FROSTED_GLASS,
  },
  [ThemePreset.TRANSPARENT_GLASS]: {
    id: ThemePreset.TRANSPARENT_GLASS,
    displayName: 'Transparent Glass',
    subtitle: 'Ultra-clear crystal layers & specular edges',
    isDarkDefault: true,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xff0d1b2a,
    defaultSecondaryBgArgb: 0xff1b3a57,
    defaultAccentArgb: 0xff5ac8fa,
    defaultTextArgb: 0xfff8fafc,
    defaultTransparency: 0.32,
    defaultBlurRadius: 26,
    defaultButtonStyle: ButtonStyleOption.OUTLINED_GLASS,
    defaultNavBarStyle: NavBarAppearance.FLOATING_GLASS_DOCK,
    defaultReadingPageBg: ReadingPageBackground.FROSTED_GLASS,
  },
  [ThemePreset.DARK_GLASS]: {
    id: ThemePreset.DARK_GLASS,
    displayName: 'Dark Glass',
    subtitle: 'Smoked obsidian frost & midnight shimmer',
    isDarkDefault: true,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xff12151e,
    defaultSecondaryBgArgb: 0xff1e2436,
    defaultAccentArgb: 0xff64d2ff,
    defaultTextArgb: 0xfff1f5f9,
    defaultTransparency: 0.56,
    defaultBlurRadius: 20,
    defaultButtonStyle: ButtonStyleOption.LIQUID_GLASS,
    defaultNavBarStyle: NavBarAppearance.FLOATING_GLASS_DOCK,
    defaultReadingPageBg: ReadingPageBackground.NIGHT_OBSIDIAN,
  },
  [ThemePreset.LIGHT_GLASS]: {
    id: ThemePreset.LIGHT_GLASS,
    displayName: 'Light Glass',
    subtitle: 'Airy pearl frost over soft alabaster sky',
    isDarkDefault: false,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xfff4f7fb,
    defaultSecondaryBgArgb: 0xffe3ecf7,
    defaultAccentArgb: 0xff007aff,
    defaultTextArgb: 0xff1c2430,
    defaultTransparency: 0.72,
    defaultBlurRadius: 18,
    defaultButtonStyle: ButtonStyleOption.LIQUID_GLASS,
    defaultNavBarStyle: NavBarAppearance.FROSTED_FULL_BAR,
    defaultReadingPageBg: ReadingPageBackground.FROSTED_GLASS,
  },
  [ThemePreset.AMOLED_BLACK]: {
    id: ThemePreset.AMOLED_BLACK,
    displayName: 'AMOLED Black',
    subtitle: 'True pitch black OLED contrast & subtle frost',
    isDarkDefault: true,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xff000000,
    defaultSecondaryBgArgb: 0xff090b10,
    defaultAccentArgb: 0xffffd166,
    defaultTextArgb: 0xfff5f5f7,
    defaultTransparency: 0.45,
    defaultBlurRadius: 12,
    defaultButtonStyle: ButtonStyleOption.OUTLINED_GLASS,
    defaultNavBarStyle: NavBarAppearance.FLOATING_GLASS_DOCK,
    defaultReadingPageBg: ReadingPageBackground.AMOLED_BLACK,
  },
  [ThemePreset.CLASSIC_BIBLE]: {
    id: ThemePreset.CLASSIC_BIBLE,
    displayName: 'Classic Bible',
    subtitle: 'Burgundy leather, antique gold & cream paper',
    isDarkDefault: false,
    isGlassDefault: false,
    defaultBackgroundArgb: 0xfffaf4e6,
    defaultSecondaryBgArgb: 0xffede2c8,
    defaultAccentArgb: 0xff5b1826,
    defaultTextArgb: 0xff231b15,
    defaultTransparency: 0.94,
    defaultBlurRadius: 0,
    defaultButtonStyle: ButtonStyleOption.CLASSIC_ELEVATED,
    defaultNavBarStyle: NavBarAppearance.SOLID_MINIMAL,
    defaultReadingPageBg: ReadingPageBackground.CREAM_PARCHMENT,
  },
  [ThemePreset.ELEGANT_BLUE]: {
    id: ThemePreset.ELEGANT_BLUE,
    displayName: 'Elegant Blue',
    subtitle: 'Royal sapphire depth & crisp silver frost',
    isDarkDefault: true,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xff0b1f3a,
    defaultSecondaryBgArgb: 0xff153866,
    defaultAccentArgb: 0xff4da3ff,
    defaultTextArgb: 0xfff0f6ff,
    defaultTransparency: 0.54,
    defaultBlurRadius: 22,
    defaultButtonStyle: ButtonStyleOption.PILL_FILLED,
    defaultNavBarStyle: NavBarAppearance.FLOATING_GLASS_DOCK,
    defaultReadingPageBg: ReadingPageBackground.FROSTED_GLASS,
  },
  [ThemePreset.NATURE_GREEN]: {
    id: ThemePreset.NATURE_GREEN,
    displayName: 'Nature Green',
    subtitle: 'Serene botanical emerald & warm sage calm',
    isDarkDefault: false,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xffeaf4ee,
    defaultSecondaryBgArgb: 0xffd2e8da,
    defaultAccentArgb: 0xff1e7a55,
    defaultTextArgb: 0xff15291e,
    defaultTransparency: 0.66,
    defaultBlurRadius: 18,
    defaultButtonStyle: ButtonStyleOption.LIQUID_GLASS,
    defaultNavBarStyle: NavBarAppearance.FLOATING_GLASS_DOCK,
    defaultReadingPageBg: ReadingPageBackground.CREAM_PARCHMENT,
  },
  [ThemePreset.GOLDEN_PREMIUM]: {
    id: ThemePreset.GOLDEN_PREMIUM,
    displayName: 'Golden Premium',
    subtitle: 'Burnished champagne gold & velvet espresso',
    isDarkDefault: true,
    isGlassDefault: true,
    defaultBackgroundArgb: 0xff1a140e,
    defaultSecondaryBgArgb: 0xff2f2316,
    defaultAccentArgb: 0xffd9b25f,
    defaultTextArgb: 0xfffaf1df,
    defaultTransparency: 0.58,
    defaultBlurRadius: 20,
    defaultButtonStyle: ButtonStyleOption.LIQUID_GLASS,
    defaultNavBarStyle: NavBarAppearance.FLOATING_GLASS_DOCK,
    defaultReadingPageBg: ReadingPageBackground.SEPIA_WARM,
  },
};

export const ALL_THEME_PRESETS: ThemePreset[] = [
  ThemePreset.IOS_LIQUID_GLASS,
  ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS,
  ThemePreset.CRYSTAL_CLEAR_DARK_GLASS,
  ThemePreset.TRANSPARENT_GLASS,
  ThemePreset.DARK_GLASS,
  ThemePreset.LIGHT_GLASS,
  ThemePreset.AMOLED_BLACK,
  ThemePreset.CLASSIC_BIBLE,
  ThemePreset.ELEGANT_BLUE,
  ThemePreset.NATURE_GREEN,
  ThemePreset.GOLDEN_PREMIUM,
];

export interface AppThemeConfig {
  preset: ThemePreset;
  isDarkSurface: boolean;
  customBackgroundArgb: number | null;
  customTextArgb: number | null;
  customAccentArgb: number | null;
  transparencyLevel: number;
  blurIntensity: number;
  buttonStyle: ButtonStyleOption;
  navBarAppearance: NavBarAppearance;
  fontStyle: ScriptureFontStyle;
  readingPageBackground: ReadingPageBackground;
}

export function createThemeConfigFromPreset(preset: ThemePreset): AppThemeConfig {
  const spec = THEME_PRESET_SPECS[preset];
  return {
    preset,
    isDarkSurface: spec.isDarkDefault,
    customBackgroundArgb: null,
    customTextArgb: null,
    customAccentArgb: null,
    transparencyLevel: spec.defaultTransparency,
    blurIntensity: spec.defaultBlurRadius,
    buttonStyle: spec.defaultButtonStyle,
    navBarAppearance: spec.defaultNavBarStyle,
    fontStyle: ScriptureFontStyle.CLASSIC_SERIF,
    readingPageBackground: spec.defaultReadingPageBg,
  };
}

export enum ReadingMode {
  BILINGUAL_SPREAD = 'BILINGUAL_SPREAD',
  BILINGUAL_PARALLEL = 'BILINGUAL_PARALLEL',
  SINGLE_PRIMARY = 'SINGLE_PRIMARY',
  SINGLE_SECONDARY = 'SINGLE_SECONDARY',
}

export enum AudioLanguage {
  BILINGUAL = 'BILINGUAL',
  ENGLISH = 'ENGLISH',
  NEPALI = 'NEPALI',
  ONCV = 'ONCV',
  KOREAN = 'KOREAN',
}

export enum HighlightColor {
  NONE = 'NONE',
  GOLD = 'GOLD',
  ROSE = 'ROSE',
  SAGE = 'SAGE',
  SKY = 'SKY',
}

export enum DownloadStatus {
  NOT_DOWNLOADED = 'NOT_DOWNLOADED',
  QUEUED = 'QUEUED',
  DOWNLOADING = 'DOWNLOADING',
  PAUSED = 'PAUSED',
  DOWNLOADED = 'DOWNLOADED',
}

export enum PageTurnSoundStyle {
  REALISTIC_BIBLE = 'REALISTIC_BIBLE',
  SOFT_PAPER = 'SOFT_PAPER',
  OLD_VINTAGE_BIBLE = 'OLD_VINTAGE_BIBLE',
  GENTLE_RUSTLE = 'GENTLE_RUSTLE',
  CLASSIC_HARDCOVER = 'CLASSIC_HARDCOVER',
  FAST_FLIP = 'FAST_FLIP',
  SLOW_SOFT = 'SLOW_SOFT',
  ANTIQUE_PARCHMENT = 'ANTIQUE_PARCHMENT',
  NATURAL_MOVEMENT = 'NATURAL_MOVEMENT',
  SOFT_DIGITAL = 'SOFT_DIGITAL',
  RANDOM = 'RANDOM',
  SILENT = 'SILENT',
}

export const AUDIBLE_PAGE_SOUND_STYLES: PageTurnSoundStyle[] = [
  PageTurnSoundStyle.REALISTIC_BIBLE,
  PageTurnSoundStyle.SOFT_PAPER,
  PageTurnSoundStyle.OLD_VINTAGE_BIBLE,
  PageTurnSoundStyle.GENTLE_RUSTLE,
  PageTurnSoundStyle.CLASSIC_HARDCOVER,
  PageTurnSoundStyle.FAST_FLIP,
  PageTurnSoundStyle.SLOW_SOFT,
  PageTurnSoundStyle.ANTIQUE_PARCHMENT,
  PageTurnSoundStyle.NATURAL_MOVEMENT,
  PageTurnSoundStyle.SOFT_DIGITAL,
];

export const ALL_PAGE_SOUND_STYLES: PageTurnSoundStyle[] = [
  ...AUDIBLE_PAGE_SOUND_STYLES,
  PageTurnSoundStyle.RANDOM,
  PageTurnSoundStyle.SILENT,
];

export const PAGE_SOUND_AUDIO_FILES: Record<
  Exclude<PageTurnSoundStyle, PageTurnSoundStyle.RANDOM | PageTurnSoundStyle.SILENT>,
  { fwd: string; bwd: string }
> = {
  [PageTurnSoundStyle.REALISTIC_BIBLE]: {
    fwd: '/audio/page_flip_forward.wav',
    bwd: '/audio/page_flip_backward.wav',
  },
  [PageTurnSoundStyle.SOFT_PAPER]: {
    fwd: '/audio/page_soft_paper_fwd.wav',
    bwd: '/audio/page_soft_paper_bwd.wav',
  },
  [PageTurnSoundStyle.OLD_VINTAGE_BIBLE]: {
    fwd: '/audio/page_old_bible_fwd.wav',
    bwd: '/audio/page_old_bible_bwd.wav',
  },
  [PageTurnSoundStyle.GENTLE_RUSTLE]: {
    fwd: '/audio/page_gentle_rustle_fwd.wav',
    bwd: '/audio/page_gentle_rustle_bwd.wav',
  },
  [PageTurnSoundStyle.CLASSIC_HARDCOVER]: {
    fwd: '/audio/page_classic_hardcover_fwd.wav',
    bwd: '/audio/page_classic_hardcover_bwd.wav',
  },
  [PageTurnSoundStyle.FAST_FLIP]: {
    fwd: '/audio/page_fast_flip_fwd.wav',
    bwd: '/audio/page_fast_flip_bwd.wav',
  },
  [PageTurnSoundStyle.SLOW_SOFT]: {
    fwd: '/audio/page_slow_soft_fwd.wav',
    bwd: '/audio/page_slow_soft_bwd.wav',
  },
  [PageTurnSoundStyle.ANTIQUE_PARCHMENT]: {
    fwd: '/audio/page_antique_parchment_fwd.wav',
    bwd: '/audio/page_antique_parchment_bwd.wav',
  },
  [PageTurnSoundStyle.NATURAL_MOVEMENT]: {
    fwd: '/audio/page_natural_movement_fwd.wav',
    bwd: '/audio/page_natural_movement_bwd.wav',
  },
  [PageTurnSoundStyle.SOFT_DIGITAL]: {
    fwd: '/audio/page_soft_digital_fwd.wav',
    bwd: '/audio/page_soft_digital_bwd.wav',
  },
};

export enum CopyrightLicenseCategory {
  OPEN_LICENSE = 'OPEN_LICENSE',
  PUBLIC_DOMAIN = 'PUBLIC_DOMAIN',
  COPYRIGHTED = 'COPYRIGHTED',
  UNVERIFIED = 'UNVERIFIED',
}

export interface BibleBook {
  id: number;
  testament: 'OT' | 'NT';
  abbrev: string;
  nameEn: string;
  nameNe: string;
  nameKo: string;
  chapterCount: number;
}

export function getBookNameForLanguage(book: BibleBook, lang: BibleLanguage): string {
  switch (lang) {
    case BibleLanguage.ENGLISH:
      return book.nameEn;
    case BibleLanguage.NEPALI:
    case BibleLanguage.ONCV:
      return book.nameNe;
    case BibleLanguage.KOREAN:
      return book.nameKo?.trim() ? book.nameKo : book.nameEn;
  }
}

export interface BibleVerse {
  id: number;
  bookId: number;
  chapter: number;
  verse: number;
  key: string;
  textEn: string;
  textNe: string;
  textOncv: string;
  textKo: string;
}

export function getVerseTextForLanguage(verse: BibleVerse, lang: BibleLanguage): string {
  switch (lang) {
    case BibleLanguage.ENGLISH:
      return verse.textEn;
    case BibleLanguage.NEPALI:
      return verse.textNe;
    case BibleLanguage.ONCV:
      return verse.textOncv;
    case BibleLanguage.KOREAN:
      return verse.textKo;
  }
}

export interface SearchResult {
  bookId: number;
  bookNameEn: string;
  bookNameNe: string;
  testament: 'OT' | 'NT';
  chapter: number;
  verse: number;
  matchedLanguage: BibleLanguage;
  textEn: string;
  textNe: string;
  textOncv: string;
  textKo: string;
}

export interface DailyVerseData {
  book: BibleBook;
  verse: BibleVerse;
}

export interface BookDownloadState {
  languageCode: string;
  bookId: number;
  status: DownloadStatus;
  bytesSize: number;
}

export interface LanguageDownloadSummary {
  language: BibleLanguage;
  totalBooks: number;
  downloadedBooks: number;
  queuedOrActiveBooks: number;
  isDownloading: boolean;
  isPaused: boolean;
  totalBytesOnDisk: number;
}

export interface BookmarkEntity {
  verseKey: string;
  bookId: number;
  chapter: number;
  verse: number;
  bookNameEn: string;
  bookNameNe: string;
  snippetEn: string;
  snippetNe: string;
  createdAt: number;
}

export interface HighlightEntity {
  verseKey: string;
  bookId: number;
  chapter: number;
  verse: number;
  bookNameEn: string;
  bookNameNe: string;
  colorKey: HighlightColor;
  snippetEn: string;
  snippetNe: string;
  updatedAt: number;
}

export interface NoteEntity {
  verseKey: string;
  bookId: number;
  chapter: number;
  verse: number;
  bookNameEn: string;
  bookNameNe: string;
  noteText: string;
  updatedAt: number;
}

export interface ReadingHistoryEntity {
  id: string;
  bookId: number;
  chapter: number;
  verse: number;
  bookNameEn: string;
  bookNameNe: string;
  visitedAt: number;
}

export interface BiblePreferencesState {
  darkMode: boolean;
  fontSizeSp: number;
  pageSoundEnabled: boolean;
  pageSoundStyle: PageTurnSoundStyle;
  pageSoundVolume: number;
  readingMode: ReadingMode;
  primaryLanguage: BibleLanguage;
  secondaryLanguage: BibleLanguage;
  audioLanguage: AudioLanguage;
  audioSpeed: number;
  lastBookId: number;
  lastChapter: number;
  lastVerse: number;
  themeConfig: AppThemeConfig;
  systemLanguage: SystemLanguage;
}

export interface AudioPlaybackState {
  isPlaying: boolean;
  isActiveSession: boolean;
  bookId: number;
  bookNameEn: string;
  bookNameNe: string;
  chapter: number;
  currentVerse: number;
  totalVerses: number;
  audioLanguage: AudioLanguage;
  speechRate: number;
  statusNote: string | null;
  isEnReady: boolean;
  isNeReady: boolean;
  isKoReady: boolean;
}

export interface ChapterSnapshot {
  book: BibleBook;
  chapter: number;
  verses: BibleVerse[];
}

export type AppRoute =
  | 'home'
  | 'reader'
  | 'search'
  | 'library'
  | 'settings'
  | 'downloads'
  | 'theme_studio';
