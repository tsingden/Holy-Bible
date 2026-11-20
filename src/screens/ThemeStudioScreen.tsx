import React, { useMemo, useState } from 'react';
import {
  ArrowLeft,
  BookOpen,
  Bookmark,
  Check,
  Home,
  Palette,
  RotateCcw,
  Search,
  Sparkles,
  X,
} from 'lucide-react';
import {
  ALL_THEME_PRESETS,
  AppThemeConfig,
  ButtonStyleOption,
  NavBarAppearance,
  ReadingPageBackground,
  SCRIPTURE_FONT_META,
  ScriptureFontStyle,
  SystemLanguage,
  THEME_PRESET_SPECS,
  createThemeConfigFromPreset,
  getFontFamilyForBibleLanguage,
  BibleLanguage,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import {
  argbToCss,
  cssWithAlpha,
  relativeLuminance,
  resolveBiblePalette,
  resolveLiquidGlassTokens,
} from '../theme/themeTokens';
import { formatNumber, formatPercent } from '../utils/formatters';
import {
  GlassSwatchCircle,
  LiquidGlassBackdrop,
  LiquidGlassButton,
  LiquidGlassSurface,
} from '../components/LiquidGlassShell';

const BACKGROUND_SWATCHES: Array<[string, number]> = [
  ['Crystal Sky', 0xffe6effa],
  ['Deep Slate', 0xff12151e],
  ['True Black', 0xff000000],
  ['Royal Navy', 0xff0b1f3a],
  ['Pearl Frost', 0xfff4f7fb],
  ['Cream Paper', 0xfffaf4e6],
  ['Forest Mist', 0xffeaf4ee],
  ['Velvet Espresso', 0xff1a140e],
  ['Deep Ocean', 0xff0d1b2a],
];

const TEXT_SWATCHES: Array<[string, number]> = [
  ['Obsidian', 0xff0f172a],
  ['Crisp White', 0xffffffff],
  ['Soft Ivory', 0xfffaf1df],
  ['Warm Sepia', 0xff231b15],
  ['Slate Ink', 0xff334155],
  ['Champagne Gold', 0xfff5d07a],
];

const ACCENT_SWATCHES: Array<[string, number]> = [
  ['Apple Blue', 0xff0a84ff],
  ['Cyan Frost', 0xff5ac8fa],
  ['Burgundy', 0xff5b1826],
  ['Antique Gold', 0xffd9b25f],
  ['Emerald', 0xff1e7a55],
  ['Royal Indigo', 0xff5e5ce6],
  ['Rose Coral', 0xffe11d48],
  ['Warm Amber', 0xffd97706],
];

const ALL_BUTTON_STYLES: ButtonStyleOption[] = [
  ButtonStyleOption.LIQUID_GLASS,
  ButtonStyleOption.PILL_FILLED,
  ButtonStyleOption.OUTLINED_GLASS,
  ButtonStyleOption.CLASSIC_ELEVATED,
];

const ALL_NAV_APPEARANCES: NavBarAppearance[] = [
  NavBarAppearance.FLOATING_GLASS_DOCK,
  NavBarAppearance.FROSTED_FULL_BAR,
  NavBarAppearance.SOLID_MINIMAL,
];

const ALL_READING_BACKGROUNDS: ReadingPageBackground[] = [
  ReadingPageBackground.MATCH_THEME,
  ReadingPageBackground.FROSTED_GLASS,
  ReadingPageBackground.CREAM_PARCHMENT,
  ReadingPageBackground.PURE_WHITE,
  ReadingPageBackground.SEPIA_WARM,
  ReadingPageBackground.NIGHT_OBSIDIAN,
  ReadingPageBackground.AMOLED_BLACK,
];

const ALL_FONT_STYLES: ScriptureFontStyle[] = [
  ScriptureFontStyle.CLASSIC_SERIF,
  ScriptureFontStyle.LITERARY_SERIF,
  ScriptureFontStyle.BOOK_SERIF,
  ScriptureFontStyle.APPLE_CLEAN_SANS,
  ScriptureFontStyle.MODERN_SANS,
];

export const ThemeStudioScreen: React.FC = () => {
  const {
    preferences,
    tokens: liveTokens,
    strings,
    goBack,
    saveThemeConfig,
    setFontSize,
  } = useBibleApp();

  const [draftConfig, setDraftConfig] = useState<AppThemeConfig>(preferences.themeConfig);
  const [draftFontSize, setDraftFontSize] = useState<number>(preferences.fontSizeSp);

  const sysLang = preferences.systemLanguage;
  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';

  const previewTokens = useMemo(() => resolveLiquidGlassTokens(draftConfig), [draftConfig]);
  const previewPalette = useMemo(() => resolveBiblePalette(previewTokens), [previewTokens]);

  const effectiveBgArgb =
    draftConfig.customBackgroundArgb ??
    THEME_PRESET_SPECS[draftConfig.preset].defaultBackgroundArgb;
  const effectiveAccentArgb =
    draftConfig.customAccentArgb ?? THEME_PRESET_SPECS[draftConfig.preset].defaultAccentArgb;

  return (
    <div className="flex h-full w-full flex-col overflow-hidden">
      {/* Top Header */}
      <LiquidGlassSurface
        tokens={liveTokens}
        elevated
        className="z-20 shrink-0 rounded-b-[20px] px-2 py-2"
      >
        <div className="mx-auto flex max-w-[680px] items-center justify-between gap-2">
          <button
            type="button"
            onClick={goBack}
            aria-label={strings.back}
            className="rounded-full p-2 hover:bg-black/5"
            style={{ color: liveTokens.textPrimary }}
          >
            <ArrowLeft className="h-5 w-5" />
          </button>
          <div className="min-w-0 flex-1">
            <h1
              className="truncate text-[19px] font-bold"
              style={{ fontFamily: headerFont, color: liveTokens.textPrimary }}
            >
              {strings.themeStudioTitle}
            </h1>
            <p className="truncate text-xs" style={{ color: liveTokens.textSecondary }}>
              {strings.themeStudioSubtitle}
            </p>
          </div>
          <button
            type="button"
            onClick={() => {
              setDraftConfig(createThemeConfigFromPreset(draftConfig.preset));
              setDraftFontSize(16.5);
            }}
            className="inline-flex items-center gap-1 rounded-xl px-2.5 py-1.5 text-xs font-semibold hover:bg-black/5"
            style={{ color: liveTokens.accentColor }}
          >
            <RotateCcw className="h-4 w-4" />
            {strings.reset}
          </button>
        </div>
      </LiquidGlassSurface>

      {/* Live Interactive Preview */}
      <div className="mx-auto w-full max-w-[680px] shrink-0 px-4 py-2.5">
        <div
          className="overflow-hidden rounded-[22px]"
          style={{ border: `1.2px solid ${previewTokens.borderTop}` }}
        >
          <LiquidGlassBackdrop tokens={previewTokens}>
            <div className="space-y-2 p-3">
              {/* Mini Glass Toolbar */}
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-1.5">
                  <Sparkles className="h-4 w-4" style={{ color: previewTokens.accentColor }} />
                  <span
                    className="text-[11px] font-bold"
                    style={{ color: previewTokens.textPrimary }}
                  >
                    {strings.livePreviewPrefix(strings.themePresetName(draftConfig.preset))}
                  </span>
                </div>
                <span
                  className="rounded-full px-2 py-0.5 text-[10px] font-bold"
                  style={{
                    backgroundColor: previewTokens.glassSurfaceElevated,
                    color: previewTokens.accentColor,
                  }}
                >
                  EN • NE
                </span>
              </div>

              {/* Mini Reading Page Surface Preview */}
              <div
                className="rounded-[14px] border p-2.5"
                style={{
                  backgroundColor: previewPalette.paperBackground,
                  borderColor: previewTokens.borderTop,
                }}
              >
                <div
                  className="text-xs font-bold"
                  style={{
                    fontFamily: '"Playfair Display", Georgia, serif',
                    color: previewPalette.verseNumberInk,
                  }}
                >
                  John 3:16 • यूहन्ना ३:१६
                </div>
                <p
                  className="mt-0.5 line-clamp-2"
                  style={{
                    fontFamily: getFontFamilyForBibleLanguage(
                      BibleLanguage.ENGLISH,
                      draftConfig.fontStyle
                    ),
                    fontSize: `${(draftFontSize * 0.78).toFixed(1)}px`,
                    lineHeight: 1.38,
                    color: previewPalette.inkPrimary,
                  }}
                >
                  For God so loved the world, that he gave his only begotten Son...
                </p>
                <p
                  className="mt-0.5 truncate"
                  style={{
                    fontFamily: '"Noto Serif Devanagari", serif',
                    fontSize: `${(draftFontSize * 0.75).toFixed(1)}px`,
                    color: previewPalette.inkSecondary,
                  }}
                >
                  किनभने परमेश्‍वरले संसारलाई यस्तो प्रेम गर्नुभयो...
                </p>
              </div>

              {/* Mini Button + Navigation Bar Preview */}
              <div className="flex items-center gap-2">
                <LiquidGlassButton
                  tokens={previewTokens}
                  className="min-h-[34px] px-3 py-1 text-[11px]"
                >
                  {strings.livePreviewReadButton}
                </LiquidGlassButton>

                <LiquidGlassSurface
                  tokens={previewTokens}
                  elevated
                  className={`flex flex-1 items-center justify-around px-2.5 py-1.5 ${
                    draftConfig.navBarAppearance === NavBarAppearance.FLOATING_GLASS_DOCK
                      ? 'rounded-[18px]'
                      : 'rounded-lg'
                  }`}
                >
                  <Home className="h-4 w-4" style={{ color: previewTokens.accentColor }} />
                  <BookOpen className="h-4 w-4" style={{ color: previewTokens.textSecondary }} />
                  <Search className="h-4 w-4" style={{ color: previewTokens.textSecondary }} />
                  <Bookmark className="h-4 w-4" style={{ color: previewTokens.textSecondary }} />
                </LiquidGlassSurface>
              </div>
            </div>
          </LiquidGlassBackdrop>
        </div>
      </div>

      {/* Scrollable Customization Controls */}
      <div className="mx-auto w-full max-w-[680px] flex-1 space-y-3 overflow-y-auto px-4 pb-6 pt-1">
        {/* 1. Theme Presets */}
        <StudioSectionCard
          title={strings.themePresetsHeading(ALL_THEME_PRESETS.length)}
          subtitle={strings.activePresetLabel(strings.themePresetName(draftConfig.preset))}
        >
          <div className="no-scrollbar flex gap-2.5 overflow-x-auto py-1">
            {ALL_THEME_PRESETS.map((preset) => {
              const spec = THEME_PRESET_SPECS[preset];
              const isSelected = draftConfig.preset === preset;
              return (
                <button
                  key={preset}
                  type="button"
                  onClick={() =>
                    setDraftConfig({
                      ...createThemeConfigFromPreset(preset),
                      fontStyle: draftConfig.fontStyle,
                    })
                  }
                  className="w-[154px] shrink-0 rounded-2xl border p-2.5 text-left transition-colors"
                  style={{
                    backgroundColor: isSelected
                      ? cssWithAlpha(liveTokens.accentColor, 0.16)
                      : liveTokens.glassSurface,
                    borderColor: isSelected ? liveTokens.accentColor : liveTokens.borderTop,
                    borderWidth: isSelected ? '2px' : '1px',
                  }}
                >
                  <div
                    className="flex h-11 w-full items-center justify-between rounded-[10px] border border-white/40 px-2"
                    style={{
                      background: `linear-gradient(135deg, ${argbToCss(
                        spec.defaultBackgroundArgb,
                        1
                      )}, ${argbToCss(spec.defaultSecondaryBgArgb, 1)})`,
                    }}
                  >
                    <span
                      className="text-[15px] font-bold"
                      style={{ color: argbToCss(spec.defaultTextArgb, 1) }}
                    >
                      Aa
                    </span>
                    <span
                      className="h-4 w-4 rounded-full"
                      style={{ backgroundColor: argbToCss(spec.defaultAccentArgb, 1) }}
                    />
                  </div>
                  <div
                    className="mt-2 truncate text-[13px] font-bold"
                    style={{ color: liveTokens.textPrimary }}
                  >
                    {strings.themePresetName(preset)}
                  </div>
                  <div
                    className="line-clamp-2 text-[10.5px] leading-[14px]"
                    style={{ color: liveTokens.textSecondary }}
                  >
                    {strings.themePresetSubtitle(preset)}
                  </div>
                </button>
              );
            })}
          </div>
        </StudioSectionCard>

        {/* 2. Transparency & Glass Blur */}
        <StudioSectionCard
          title={strings.liquidGlassOpticsHeading}
          subtitle={strings.hardwareBlurSub}
        >
          <div className="flex justify-between text-[13.5px]">
            <span className="font-semibold" style={{ color: liveTokens.textPrimary }}>
              {strings.glassOpacityLabel}
            </span>
            <span className="font-bold" style={{ color: liveTokens.accentColor }}>
              {formatPercent(Math.round(draftConfig.transparencyLevel * 100), sysLang)}
            </span>
          </div>
          <input
            type="range"
            min={18}
            max={95}
            step={1}
            value={Math.round(draftConfig.transparencyLevel * 100)}
            onChange={(e) =>
              setDraftConfig((prev) => ({
                ...prev,
                transparencyLevel: Number(e.target.value) / 100,
              }))
            }
            className="mt-1.5 w-full accent-current"
            style={{ color: liveTokens.accentColor }}
          />

          <div className="mt-3 flex justify-between text-[13.5px]">
            <span className="font-semibold" style={{ color: liveTokens.textPrimary }}>
              {strings.glassBlurLabel}
            </span>
            <span className="font-bold" style={{ color: liveTokens.accentColor }}>
              {formatNumber(Math.round(draftConfig.blurIntensity), sysLang)} px
            </span>
          </div>
          <input
            type="range"
            min={0}
            max={32}
            step={1}
            value={Math.round(draftConfig.blurIntensity)}
            onChange={(e) =>
              setDraftConfig((prev) => ({
                ...prev,
                blurIntensity: Number(e.target.value),
              }))
            }
            className="mt-1.5 w-full accent-current"
            style={{ color: liveTokens.accentColor }}
          />
        </StudioSectionCard>

        {/* 3. Colors */}
        <StudioSectionCard title={strings.colorsHeading} subtitle={strings.colorsSub}>
          <div className="text-[13px] font-semibold" style={{ color: liveTokens.textPrimary }}>
            {strings.backgroundColorLabel}
          </div>
          <div className="no-scrollbar mt-1 flex gap-1 overflow-x-auto">
            {BACKGROUND_SWATCHES.map(([name, argb]) => (
              <GlassSwatchCircle
                key={name}
                argb={argb}
                title={name}
                selected={effectiveBgArgb === argb}
                onClick={() => {
                  const dark = relativeLuminance(argb) < 0.44;
                  setDraftConfig((prev) => ({
                    ...prev,
                    customBackgroundArgb: argb,
                    isDarkSurface: dark,
                  }));
                }}
              />
            ))}
          </div>

          <div className="mt-2.5 text-[13px] font-semibold" style={{ color: liveTokens.textPrimary }}>
            {strings.accentColorLabel}
          </div>
          <div className="no-scrollbar mt-1 flex gap-1 overflow-x-auto">
            {ACCENT_SWATCHES.map(([name, argb]) => (
              <GlassSwatchCircle
                key={name}
                argb={argb}
                title={name}
                selected={effectiveAccentArgb === argb}
                onClick={() =>
                  setDraftConfig((prev) => ({ ...prev, customAccentArgb: argb }))
                }
              />
            ))}
          </div>

          <div className="mt-2.5 flex items-center justify-between">
            <span className="text-[13px] font-semibold" style={{ color: liveTokens.textPrimary }}>
              {strings.textColorLabel}
            </span>
            <button
              type="button"
              onClick={() => setDraftConfig((prev) => ({ ...prev, customTextArgb: null }))}
              className="text-xs font-semibold hover:underline"
              style={{ color: liveTokens.accentColor }}
            >
              {strings.autoContrastButton}
            </button>
          </div>
          <div className="no-scrollbar mt-1 flex gap-1 overflow-x-auto">
            {TEXT_SWATCHES.map(([name, argb]) => (
              <GlassSwatchCircle
                key={name}
                argb={argb}
                title={name}
                selected={draftConfig.customTextArgb === argb}
                onClick={() =>
                  setDraftConfig((prev) => ({ ...prev, customTextArgb: argb }))
                }
              />
            ))}
          </div>
        </StudioSectionCard>

        {/* 4. Button Style & Navigation Bar Appearance */}
        <StudioSectionCard
          title={strings.controlsNavHeading}
          subtitle={strings.controlsNavSub}
        >
          <div className="text-[13px] font-semibold" style={{ color: liveTokens.textPrimary }}>
            {strings.buttonStyleLabel}
          </div>
          <div className="no-scrollbar mt-1.5 flex gap-2 overflow-x-auto">
            {ALL_BUTTON_STYLES.map((style) => {
              const selected = draftConfig.buttonStyle === style;
              return (
                <button
                  key={style}
                  type="button"
                  onClick={() => setDraftConfig((prev) => ({ ...prev, buttonStyle: style }))}
                  className="shrink-0 rounded-full border px-3 py-1.5 text-xs font-semibold transition-colors"
                  style={{
                    backgroundColor: selected
                      ? cssWithAlpha(liveTokens.accentColor, 0.2)
                      : 'transparent',
                    borderColor: selected ? liveTokens.accentColor : liveTokens.borderTop,
                    color: selected ? liveTokens.accentColor : liveTokens.textPrimary,
                  }}
                >
                  {strings.buttonStyleName(style)}
                </button>
              );
            })}
          </div>

          <div className="mt-3 text-[13px] font-semibold" style={{ color: liveTokens.textPrimary }}>
            {strings.navBarAppearanceLabel}
          </div>
          <div className="no-scrollbar mt-1.5 flex gap-2 overflow-x-auto">
            {ALL_NAV_APPEARANCES.map((nav) => {
              const selected = draftConfig.navBarAppearance === nav;
              return (
                <button
                  key={nav}
                  type="button"
                  onClick={() => setDraftConfig((prev) => ({ ...prev, navBarAppearance: nav }))}
                  className="shrink-0 rounded-full border px-3 py-1.5 text-xs font-semibold transition-colors"
                  style={{
                    backgroundColor: selected
                      ? cssWithAlpha(liveTokens.accentColor, 0.2)
                      : 'transparent',
                    borderColor: selected ? liveTokens.accentColor : liveTokens.borderTop,
                    color: selected ? liveTokens.accentColor : liveTokens.textPrimary,
                  }}
                >
                  {strings.navBarAppearanceName(nav)}
                </button>
              );
            })}
          </div>
        </StudioSectionCard>

        {/* 5. Reading Page Background & Typography */}
        <StudioSectionCard
          title={strings.readingPageTypographyHeading}
          subtitle={strings.readingPageTypographySub}
        >
          <div className="text-[13px] font-semibold" style={{ color: liveTokens.textPrimary }}>
            {strings.readingPageBgLabel}
          </div>
          <div className="no-scrollbar mt-1.5 flex gap-2 overflow-x-auto">
            {ALL_READING_BACKGROUNDS.map((bg) => {
              const selected = draftConfig.readingPageBackground === bg;
              return (
                <button
                  key={bg}
                  type="button"
                  onClick={() =>
                    setDraftConfig((prev) => ({ ...prev, readingPageBackground: bg }))
                  }
                  className="shrink-0 rounded-full border px-3 py-1.5 text-xs font-semibold transition-colors"
                  style={{
                    backgroundColor: selected
                      ? cssWithAlpha(liveTokens.accentColor, 0.2)
                      : 'transparent',
                    borderColor: selected ? liveTokens.accentColor : liveTokens.borderTop,
                    color: selected ? liveTokens.accentColor : liveTokens.textPrimary,
                  }}
                >
                  {strings.readingBgName(bg)}
                </button>
              );
            })}
          </div>

          <div className="mt-3 text-[13px] font-semibold" style={{ color: liveTokens.textPrimary }}>
            {strings.scriptureFontStyleLabel}
          </div>
          <div className="no-scrollbar mt-1.5 flex gap-2 overflow-x-auto">
            {ALL_FONT_STYLES.map((font) => {
              const meta = SCRIPTURE_FONT_META[font];
              const selected = draftConfig.fontStyle === font;
              return (
                <button
                  key={font}
                  type="button"
                  onClick={() => setDraftConfig((prev) => ({ ...prev, fontStyle: font }))}
                  className="shrink-0 rounded-full border px-3 py-1.5 text-xs font-semibold transition-colors"
                  style={{
                    fontFamily: meta.cssFamily,
                    backgroundColor: selected
                      ? cssWithAlpha(liveTokens.accentColor, 0.2)
                      : 'transparent',
                    borderColor: selected ? liveTokens.accentColor : liveTokens.borderTop,
                    color: selected ? liveTokens.accentColor : liveTokens.textPrimary,
                  }}
                >
                  {meta.label}
                </button>
              );
            })}
          </div>

          <div className="mt-3 flex justify-between text-[13px]">
            <span className="font-semibold" style={{ color: liveTokens.textPrimary }}>
              {strings.scriptureFontSizeLabel}
            </span>
            <span className="font-bold" style={{ color: liveTokens.accentColor }}>
              {formatNumber(Math.round(draftFontSize), sysLang)} px
            </span>
          </div>
          <input
            type="range"
            min={13}
            max={24}
            step={1}
            value={Math.round(draftFontSize)}
            onChange={(e) => setDraftFontSize(Number(e.target.value))}
            className="mt-1.5 w-full accent-current"
            style={{ color: liveTokens.accentColor }}
          />
        </StudioSectionCard>
      </div>

      {/* Bottom Apply / Cancel Bar */}
      <LiquidGlassSurface
        tokens={liveTokens}
        elevated
        className="z-20 shrink-0 rounded-t-[22px] px-4 py-3"
      >
        <div className="mx-auto flex max-w-[680px] items-center gap-3">
          <button
            type="button"
            onClick={goBack}
            className="flex h-12 flex-1 items-center justify-center gap-1.5 rounded-full border text-sm font-semibold"
            style={{ borderColor: liveTokens.borderTop, color: liveTokens.textPrimary }}
          >
            <X className="h-[18px] w-[18px]" />
            {strings.cancel}
          </button>
          <button
            type="button"
            onClick={() => {
              saveThemeConfig(draftConfig);
              setFontSize(draftFontSize);
              goBack();
            }}
            className="flex h-12 flex-[1.4] items-center justify-center gap-1.5 rounded-full text-sm font-bold shadow"
            style={{
              backgroundColor: liveTokens.accentColor,
              color: liveTokens.onAccentColor,
            }}
          >
            <Check className="h-[18px] w-[18px]" />
            {strings.applyThemeButton}
          </button>
        </div>
      </LiquidGlassSurface>
    </div>
  );
};

const StudioSectionCard: React.FC<{
  title: string;
  subtitle: string;
  children: React.ReactNode;
}> = ({ title, subtitle, children }) => {
  const { tokens, preferences } = useBibleApp();
  const sysLang = preferences.systemLanguage;
  return (
    <LiquidGlassSurface tokens={tokens} className="rounded-[20px] p-4">
      <div className="flex items-center gap-2">
        <Palette className="h-[18px] w-[18px] shrink-0" style={{ color: tokens.accentColor }} />
        <div>
          <h2
            className="text-base font-bold"
            style={{
              fontFamily:
                sysLang === SystemLanguage.NEPALI
                  ? '"Noto Serif Devanagari", serif'
                  : '"Playfair Display", Georgia, serif',
              color: tokens.textPrimary,
            }}
          >
            {title}
          </h2>
          <p className="text-[11.5px]" style={{ color: tokens.textSecondary }}>
            {subtitle}
          </p>
        </div>
      </div>
      <div
        className="my-2.5 h-[0.7px] w-full"
        style={{ backgroundColor: cssWithAlpha(tokens.borderTop, 0.5) }}
      />
      {children}
    </LiquidGlassSurface>
  );
};
