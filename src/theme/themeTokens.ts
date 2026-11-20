import {
  AppThemeConfig,
  ButtonStyleOption,
  NavBarAppearance,
  ReadingPageBackground,
  ScriptureFontStyle,
  THEME_PRESET_SPECS,
  ThemePreset,
} from '../types/bible';

// Sacred Heritage & Illuminated Parchment Palette (Color.kt exact values)
export const COLORS = {
  BurgundyLeather: 0xff5b1826,
  BurgundyDeep: 0xff410d19,
  BurgundySoft: 0xff7d293a,
  AntiqueGold: 0xffb58938,
  GoldShimmer: 0xffd9b25f,
  GoldSoftContainer: 0xfff5e9ce,
  ParchmentPaper: 0xfffaf4e6,
  ParchmentEdge: 0xffede2c8,
  ParchmentOuterBg: 0xffe7dec9,
  SpineShadowColor: 0xff3e2c1e,
  InkEspresso: 0xff231b15,
  InkSepiaMuted: 0xff6b5a48,
  InkVerseNumber: 0xff8d3b2f,
  NightCharcoal: 0xff13100e,
  NightParchment: 0xff1e1915,
  NightParchmentEdge: 0xff2b231d,
  NightOuterBg: 0xff0e0b0a,
  NightInkIvory: 0xffede3d2,
  NightInkMuted: 0xffb5a692,
  NightInkVerseNumber: 0xffd99a6c,
  HighlightGoldLight: 0x66f6d56a,
  HighlightRoseLight: 0x66f2a7b5,
  HighlightSageLight: 0x669fd69b,
  HighlightSkyLight: 0x668ac4eb,
  HighlightGoldDark: 0x55b89028,
  HighlightRoseDark: 0x55b85a6b,
  HighlightSageDark: 0x554e8c4a,
  HighlightSkyDark: 0x55427aa1,
  ActiveVerseLight: 0x33b58938,
  ActiveVerseDark: 0x44d9b25f,
};

export function argbToRgb(argb: number): { r: number; g: number; b: number; a: number } {
  const unsigned = argb >>> 0;
  const a = ((unsigned >>> 24) & 0xff) / 255;
  const r = (unsigned >>> 16) & 0xff;
  const g = (unsigned >>> 8) & 0xff;
  const b = unsigned & 0xff;
  return { r, g, b, a };
}

export function argbToCss(argb: number, overrideAlpha?: number): string {
  const { r, g, b, a } = argbToRgb(argb);
  const alpha = overrideAlpha !== undefined ? Math.max(0, Math.min(1, overrideAlpha)) : a;
  return `rgba(${r}, ${g}, ${b}, ${alpha.toFixed(3)})`;
}

export function cssWithAlpha(cssColor: string, alpha: number): string {
  const clamped = Math.max(0, Math.min(1, alpha));
  const match = cssColor.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/);
  if (!match) return cssColor;
  return `rgba(${match[1]}, ${match[2]}, ${match[3]}, ${clamped.toFixed(3)})`;
}

export function relativeLuminance(argb: number): number {
  const { r, g, b } = argbToRgb(argb);
  const lin = (channel: number) => {
    const c = channel / 255;
    return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
  };
  return 0.2126 * lin(r) + 0.7152 * lin(g) + 0.0722 * lin(b);
}

export function blendArgb(aArgb: number, bArgb: number, ratio: number): number {
  const t = Math.max(0, Math.min(1, ratio));
  const a = argbToRgb(aArgb);
  const b = argbToRgb(bArgb);
  const r = Math.round(a.r * (1 - t) + b.r * t);
  const g = Math.round(a.g * (1 - t) + b.g * t);
  const bl = Math.round(a.b * (1 - t) + b.b * t);
  return ((0xff << 24) | (r << 16) | (g << 8) | bl) >>> 0;
}

export interface LiquidGlassTokens {
  config: AppThemeConfig;
  isDark: boolean;
  isGlassActive: boolean;
  isCrystalClear: boolean;
  backdropStartArgb: number;
  backdropStart: string;
  backdropEnd: string;
  orbPrimary: string;
  orbSecondary: string;
  orbTertiary: string;
  glassSurface: string;
  glassSurfaceElevated: string;
  specularTop: string;
  borderTop: string;
  borderBottom: string;
  accentArgb: number;
  accentColor: string;
  onAccentColor: string;
  textPrimaryArgb: number;
  textPrimary: string;
  textSecondary: string;
  transparencyLevel: number;
  blurIntensityDp: number;
  buttonStyle: ButtonStyleOption;
  navBarAppearance: NavBarAppearance;
  fontStyle: ScriptureFontStyle;
  readingPageBackground: ReadingPageBackground;
}

export interface BibleBookPalette {
  paperBackground: string;
  paperEdge: string;
  deskBackground: string;
  inkPrimary: string;
  inkSecondary: string;
  verseNumberInk: string;
  spineShadow: string;
  goldAccent: string;
  leatherAccent: string;
  activeVerseTint: string;
  highlightGold: string;
  highlightRose: string;
  highlightSage: string;
  highlightSky: string;
  isDark: boolean;
}

export function resolveLiquidGlassTokens(config: AppThemeConfig): LiquidGlassTokens {
  const spec = THEME_PRESET_SPECS[config.preset];
  const baseBg = config.customBackgroundArgb ?? spec.defaultBackgroundArgb;
  const accent = config.customAccentArgb ?? spec.defaultAccentArgb;
  const textBase = config.customTextArgb ?? spec.defaultTextArgb;

  const isDark =
    config.customBackgroundArgb !== null
      ? relativeLuminance(baseBg) < 0.44
      : config.isDarkSurface;

  const safeTextPrimaryArgb =
    config.customTextArgb !== null ? textBase : isDark ? 0xfff3f6fb : 0xff141a24;

  const textPrimary = argbToCss(safeTextPrimaryArgb, 1);
  const textSecondary = argbToCss(safeTextPrimaryArgb, isDark ? 0.74 : 0.68);
  const onAccentColor = relativeLuminance(accent) < 0.48 ? '#FFFFFF' : '#141820';

  const preset = config.preset;
  const isCrystalClear =
    preset === ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS ||
    preset === ThemePreset.CRYSTAL_CLEAR_DARK_GLASS;
  const isGlassActive =
    spec.isGlassDefault || config.transparencyLevel < 0.88 || config.blurIntensity > 1;
  const alpha = Math.max(0.15, Math.min(0.96, config.transparencyLevel));

  let backdropStartArgb = baseBg;
  let backdropEndArgb = baseBg;
  let orb1 = argbToCss(accent, 0.34);
  let orb2 = 'rgba(94, 92, 230, 0.24)';
  let orb3 = 'rgba(100, 210, 255, 0.28)';
  let surfaceTintArgb = 0xffffffff;

  switch (preset) {
    case ThemePreset.IOS_LIQUID_GLASS:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xffdcebfc;
      backdropEndArgb =
        config.customBackgroundArgb !== null ? blendArgb(baseBg, accent, 0.14) : 0xfff3ebfa;
      orb1 = argbToCss(accent, 0.34);
      orb2 = argbToCss(0xff5e5ce6, 0.24);
      orb3 = argbToCss(0xff64d2ff, 0.28);
      surfaceTintArgb = isDark ? 0xff172233 : 0xffffffff;
      break;
    case ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xffd2ecff;
      backdropEndArgb =
        config.customBackgroundArgb !== null ? blendArgb(baseBg, accent, 0.16) : 0xffeee8ff;
      orb1 = argbToCss(0xff38bdf8, 0.42);
      orb2 = argbToCss(0xff818cf8, 0.32);
      orb3 = argbToCss(0xffa5f3fc, 0.46);
      surfaceTintArgb = isDark ? 0xff132238 : 0xffffffff;
      break;
    case ThemePreset.CRYSTAL_CLEAR_DARK_GLASS:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xff060f1d;
      backdropEndArgb =
        config.customBackgroundArgb !== null ? blendArgb(baseBg, accent, 0.18) : 0xff112642;
      orb1 = argbToCss(0xff0ea5e9, 0.38);
      orb2 = argbToCss(0xff6366f1, 0.30);
      orb3 = argbToCss(0xff22d3ee, 0.30);
      surfaceTintArgb = isDark ? 0xff132238 : 0xffffffff;
      break;
    case ThemePreset.TRANSPARENT_GLASS:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xff0a1628;
      backdropEndArgb =
        config.customBackgroundArgb !== null ? blendArgb(baseBg, accent, 0.2) : 0xff16324f;
      orb1 = argbToCss(accent, 0.42);
      orb2 = argbToCss(0xff7b61ff, 0.30);
      orb3 = argbToCss(0xff38bdf8, 0.30);
      surfaceTintArgb = isDark ? 0xff1a2f4b : 0xffffffff;
      break;
    case ThemePreset.DARK_GLASS:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xff0f131c;
      backdropEndArgb =
        config.customBackgroundArgb !== null ? blendArgb(baseBg, accent, 0.12) : 0xff1a2234;
      orb1 = argbToCss(accent, 0.26);
      orb2 = argbToCss(0xff4f46e5, 0.22);
      orb3 = argbToCss(0xff0ea5e9, 0.18);
      surfaceTintArgb = 0xff1b2333;
      break;
    case ThemePreset.LIGHT_GLASS:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xfff2f6fc;
      backdropEndArgb =
        config.customBackgroundArgb !== null ? blendArgb(baseBg, accent, 0.10) : 0xffe7effa;
      orb1 = argbToCss(accent, 0.20);
      orb2 = argbToCss(0xffa78bfa, 0.16);
      orb3 = argbToCss(0xff7dd3fc, 0.22);
      surfaceTintArgb = 0xffffffff;
      break;
    case ThemePreset.AMOLED_BLACK:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xff000000;
      backdropEndArgb = config.customBackgroundArgb !== null ? baseBg : 0xff05070b;
      orb1 = argbToCss(accent, 0.14);
      orb2 = argbToCss(0xff1d4ed8, 0.10);
      orb3 = 'rgba(0,0,0,0)';
      surfaceTintArgb = 0xff10131a;
      break;
    case ThemePreset.CLASSIC_BIBLE:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xffe7dec9;
      backdropEndArgb = config.customBackgroundArgb !== null ? baseBg : 0xfff5ecd9;
      orb1 = argbToCss(accent, 0.08);
      orb2 = argbToCss(COLORS.AntiqueGold, 0.10);
      orb3 = 'rgba(0,0,0,0)';
      surfaceTintArgb = isDark ? 0xff1e1915 : COLORS.ParchmentPaper;
      break;
    case ThemePreset.ELEGANT_BLUE:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xff09192f;
      backdropEndArgb =
        config.customBackgroundArgb !== null ? blendArgb(baseBg, accent, 0.16) : 0xff132e53;
      orb1 = argbToCss(accent, 0.34);
      orb2 = argbToCss(0xff60a5fa, 0.24);
      orb3 = argbToCss(0xff38bdf8, 0.18);
      surfaceTintArgb = 0xff163156;
      break;
    case ThemePreset.NATURE_GREEN:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xffe4f2e9;
      backdropEndArgb =
        config.customBackgroundArgb !== null ? blendArgb(baseBg, accent, 0.12) : 0xffcde5d6;
      orb1 = argbToCss(accent, 0.26);
      orb2 = argbToCss(0xff34d399, 0.22);
      orb3 = argbToCss(0xffa7f3d0, 0.26);
      surfaceTintArgb = isDark ? 0xff142a20 : 0xfff9fcf9;
      break;
    case ThemePreset.GOLDEN_PREMIUM:
      backdropStartArgb = config.customBackgroundArgb !== null ? baseBg : 0xff16100b;
      backdropEndArgb =
        config.customBackgroundArgb !== null ? blendArgb(baseBg, accent, 0.15) : 0xff281d13;
      orb1 = argbToCss(accent, 0.30);
      orb2 = argbToCss(0xffb58938, 0.22);
      orb3 = argbToCss(0xff8c4a27, 0.18);
      surfaceTintArgb = isDark ? 0xff241b12 : 0xfffff8ec;
      break;
  }

  const glassSurface = argbToCss(surfaceTintArgb, alpha);
  const glassSurfaceElevated = isCrystalClear
    ? argbToCss(surfaceTintArgb, Math.min(0.84, alpha + 0.1))
    : argbToCss(surfaceTintArgb, Math.min(0.97, alpha + 0.16));

  const specularTop =
    isCrystalClear && isDark
      ? argbToCss(0xffe0f7ff, 0.3)
      : isCrystalClear
      ? 'rgba(255, 255, 255, 0.94)'
      : isDark
      ? 'rgba(255, 255, 255, 0.18)'
      : 'rgba(255, 255, 255, 0.78)';

  const borderTop =
    isCrystalClear && isDark
      ? argbToCss(0xffcffafe, 0.44)
      : isCrystalClear
      ? 'rgba(255, 255, 255, 0.96)'
      : isDark
      ? 'rgba(255, 255, 255, 0.28)'
      : 'rgba(255, 255, 255, 0.85)';

  const borderBottom = isCrystalClear
    ? argbToCss(accent, isDark ? 0.42 : 0.36)
    : argbToCss(accent, isDark ? 0.24 : 0.18);

  return {
    config,
    isDark,
    isGlassActive,
    isCrystalClear,
    backdropStartArgb,
    backdropStart: argbToCss(backdropStartArgb, 1),
    backdropEnd: argbToCss(backdropEndArgb, 1),
    orbPrimary: orb1,
    orbSecondary: orb2,
    orbTertiary: orb3,
    glassSurface,
    glassSurfaceElevated,
    specularTop,
    borderTop,
    borderBottom,
    accentArgb: accent,
    accentColor: argbToCss(accent, 1),
    onAccentColor,
    textPrimaryArgb: safeTextPrimaryArgb,
    textPrimary,
    textSecondary,
    transparencyLevel: alpha,
    blurIntensityDp: config.blurIntensity,
    buttonStyle: config.buttonStyle,
    navBarAppearance: config.navBarAppearance,
    fontStyle: config.fontStyle,
    readingPageBackground: config.readingPageBackground,
  };
}

export function resolveBiblePalette(tokens: LiquidGlassTokens): BibleBookPalette {
  const { config } = tokens;
  const accentArgb = tokens.accentArgb;

  let effectiveReadingBg = config.readingPageBackground;
  if (effectiveReadingBg === ReadingPageBackground.MATCH_THEME) {
    if (config.preset === ThemePreset.CLASSIC_BIBLE) {
      effectiveReadingBg = tokens.isDark
        ? ReadingPageBackground.NIGHT_OBSIDIAN
        : ReadingPageBackground.CREAM_PARCHMENT;
    } else if (config.preset === ThemePreset.AMOLED_BLACK) {
      effectiveReadingBg = ReadingPageBackground.AMOLED_BLACK;
    } else if (config.preset === ThemePreset.GOLDEN_PREMIUM) {
      effectiveReadingBg = ReadingPageBackground.SEPIA_WARM;
    } else {
      effectiveReadingBg = ReadingPageBackground.FROSTED_GLASS;
    }
  }

  let paperBackground: string;
  let paperEdge: string;
  let deskBackground: string;
  let inkPrimary: string;
  let inkSecondary: string;
  let verseNumberInk: string;
  let pageIsDark: boolean;

  switch (effectiveReadingBg) {
    case ReadingPageBackground.CREAM_PARCHMENT:
      paperBackground = argbToCss(COLORS.ParchmentPaper, 1);
      paperEdge = argbToCss(COLORS.ParchmentEdge, 1);
      deskBackground = tokens.isGlassActive ? 'transparent' : argbToCss(COLORS.ParchmentOuterBg, 1);
      inkPrimary =
        config.customTextArgb !== null && !tokens.isDark
          ? tokens.textPrimary
          : argbToCss(COLORS.InkEspresso, 1);
      inkSecondary = argbToCss(COLORS.InkSepiaMuted, 1);
      verseNumberInk =
        config.preset === ThemePreset.CLASSIC_BIBLE
          ? argbToCss(COLORS.InkVerseNumber, 1)
          : tokens.accentColor;
      pageIsDark = false;
      break;
    case ReadingPageBackground.PURE_WHITE:
      paperBackground = '#FFFFFF';
      paperEdge = '#EFF3F8';
      deskBackground = tokens.isGlassActive ? 'transparent' : '#E4E9F0';
      inkPrimary = '#111827';
      inkSecondary = '#4B5563';
      verseNumberInk = tokens.accentColor;
      pageIsDark = false;
      break;
    case ReadingPageBackground.SEPIA_WARM:
      paperBackground = '#F3E7D0';
      paperEdge = '#E4D2B2';
      deskBackground = tokens.isGlassActive ? 'transparent' : '#DCC7A3';
      inkPrimary = '#2D2114';
      inkSecondary = '#6E5740';
      verseNumberInk = '#8D4E22';
      pageIsDark = false;
      break;
    case ReadingPageBackground.NIGHT_OBSIDIAN:
      paperBackground = argbToCss(0xff161b26, tokens.isGlassActive ? 0.9 : 1);
      paperEdge = '#222A3A';
      deskBackground = tokens.isGlassActive ? 'transparent' : argbToCss(COLORS.NightOuterBg, 1);
      inkPrimary = '#EBF0F7';
      inkSecondary = '#9BA8BE';
      verseNumberInk =
        relativeLuminance(accentArgb) < 0.35 ? '#7CC4FF' : tokens.accentColor;
      pageIsDark = true;
      break;
    case ReadingPageBackground.AMOLED_BLACK:
      paperBackground = '#000000';
      paperEdge = '#12151C';
      deskBackground = '#000000';
      inkPrimary = '#F3F4F6';
      inkSecondary = '#9CA3AF';
      verseNumberInk =
        relativeLuminance(accentArgb) < 0.35
          ? argbToCss(COLORS.GoldShimmer, 1)
          : tokens.accentColor;
      pageIsDark = true;
      break;
    case ReadingPageBackground.FROSTED_GLASS: {
      const frostedAlpha = tokens.isCrystalClear
        ? Math.max(0.42, Math.min(0.8, tokens.transparencyLevel + 0.24))
        : Math.max(0.46, Math.min(0.92, tokens.transparencyLevel + 0.16));
      if (tokens.isDark) {
        paperBackground = argbToCss(blendArgb(tokens.backdropStartArgb, 0xff1a2233, 0.45), frostedAlpha);
        paperEdge = tokens.isCrystalClear ? 'rgba(207, 250, 254, 0.18)' : 'rgba(255, 255, 255, 0.08)';
        deskBackground = 'transparent';
        inkPrimary = tokens.textPrimary;
        inkSecondary = tokens.textSecondary;
        verseNumberInk =
          relativeLuminance(accentArgb) < 0.35 ? '#7CC4FF' : tokens.accentColor;
        pageIsDark = true;
      } else {
        const lightAlpha = tokens.isCrystalClear
          ? Math.max(0.52, Math.min(0.84, tokens.transparencyLevel + 0.34))
          : Math.max(0.62, Math.min(0.94, tokens.transparencyLevel + 0.22));
        paperBackground = argbToCss(0xfffcfdff, lightAlpha);
        paperEdge = tokens.isCrystalClear ? 'rgba(255, 255, 255, 0.72)' : argbToCss(accentArgb, 0.1);
        deskBackground = 'transparent';
        inkPrimary = tokens.textPrimary;
        inkSecondary = tokens.textSecondary;
        verseNumberInk =
          relativeLuminance(accentArgb) > 0.72
            ? argbToCss(COLORS.InkVerseNumber, 1)
            : tokens.accentColor;
        pageIsDark = false;
      }
      break;
    }
  }

  const primaryBrand =
    config.preset === ThemePreset.CLASSIC_BIBLE && config.customAccentArgb === null
      ? argbToCss(COLORS.BurgundyLeather, 1)
      : tokens.accentColor;

  const secondaryBrand =
    config.preset === ThemePreset.CLASSIC_BIBLE && config.customAccentArgb === null
      ? argbToCss(COLORS.AntiqueGold, 1)
      : argbToCss(blendArgb(accentArgb, pageIsDark ? 0xffffffff : 0xffb58938, 0.28), 1);

  return {
    paperBackground,
    paperEdge,
    deskBackground,
    inkPrimary,
    inkSecondary,
    verseNumberInk,
    spineShadow: pageIsDark ? 'rgba(0, 0, 0, 1)' : argbToCss(COLORS.SpineShadowColor, 1),
    goldAccent: secondaryBrand,
    leatherAccent: primaryBrand,
    activeVerseTint: argbToCss(accentArgb, 0.22),
    highlightGold: pageIsDark
      ? argbToCss(COLORS.HighlightGoldDark)
      : argbToCss(COLORS.HighlightGoldLight),
    highlightRose: pageIsDark
      ? argbToCss(COLORS.HighlightRoseDark)
      : argbToCss(COLORS.HighlightRoseLight),
    highlightSage: pageIsDark
      ? argbToCss(COLORS.HighlightSageDark)
      : argbToCss(COLORS.HighlightSageLight),
    highlightSky: pageIsDark
      ? argbToCss(COLORS.HighlightSkyDark)
      : argbToCss(COLORS.HighlightSkyLight),
    isDark: pageIsDark,
  };
}
