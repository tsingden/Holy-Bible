import React, { useState } from 'react';
import {
  BookOpen,
  CheckCircle2,
  Circle,
  Download,
  Globe,
  Headphones,
  Languages,
  Moon,
  Palette,
  Play,
  RefreshCw,
  Shuffle,
  Sparkles,
  Volume2,
  VolumeX,
} from 'lucide-react';
import {
  ALL_PAGE_SOUND_STYLES,
  ALL_THEME_PRESETS,
  AudioLanguage,
  BibleLanguage,
  PageTurnSoundStyle,
  ReadingMode,
  SCRIPTURE_FONT_META,
  SYSTEM_LANGUAGE_META,
  SystemLanguage,
  THEME_PRESET_SPECS,
  getFontFamilyForBibleLanguage,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { argbToCss, cssWithAlpha } from '../theme/themeTokens';
import {
  formatBytes,
  formatNumber,
  formatPercent,
  formatSpeed,
} from '../utils/formatters';
import { LiquidGlassButton, LiquidGlassSurface } from '../components/LiquidGlassShell';
import { CopyrightAndCreditsSection } from '../components/CopyrightAndCreditsSection';

export const SettingsScreen: React.FC = () => {
  const {
    preferences,
    audioState,
    totalStorageBytes,
    tokens,
    palette,
    strings,
    navigate,
    openLanguageSheet,
    showToast,
    setSystemLanguage,
    applyThemePreset,
    setDarkMode,
    setReadingMode,
    setFontSize,
    setPageSoundEnabled,
    setPageSoundStyle,
    setPageSoundVolume,
    previewPageSound,
    playForwardSound,
    playBackwardSound,
    setAudioLanguage,
    setAudioSpeed,
    refreshAudioVoices,
  } = useBibleApp();

  const sysLang = preferences.systemLanguage;
  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';

  const currentTheme = preferences.themeConfig;
  const [lastAudibleStyle, setLastAudibleStyle] = useState<PageTurnSoundStyle>(
    preferences.pageSoundStyle !== PageTurnSoundStyle.SILENT &&
      preferences.pageSoundStyle !== PageTurnSoundStyle.RANDOM
      ? preferences.pageSoundStyle
      : PageTurnSoundStyle.REALISTIC_BIBLE
  );

  const handleSelectPageSound = (style: PageTurnSoundStyle) => {
    if (style !== PageTurnSoundStyle.SILENT && style !== PageTurnSoundStyle.RANDOM) {
      setLastAudibleStyle(style);
    }
    setPageSoundStyle(style);
  };

  const effectiveAudible =
    preferences.pageSoundEnabled && preferences.pageSoundStyle !== PageTurnSoundStyle.SILENT;
  const volumePercent = Math.round(preferences.pageSoundVolume * 100);

  return (
    <div className="flex-1 overflow-y-auto pb-28">
      <div className="mx-auto flex w-full max-w-[680px] flex-col gap-3.5 px-4 pt-3">
        <h1
          className="text-[23px] font-bold"
          style={{ fontFamily: headerFont, color: tokens.textPrimary }}
        >
          {strings.settingsTitle}
        </h1>

        {/* 0. System Language Selector */}
        <SettingsCard title={strings.systemLanguageCardTitle} icon={<Globe className="h-5 w-5" />}>
          <div className="text-sm font-semibold" style={{ color: tokens.textPrimary }}>
            {strings.systemLanguageSubtitle}
          </div>
          <p className="mt-0.5 text-xs" style={{ color: tokens.textSecondary }}>
            {strings.systemLanguageHint}
          </p>
          <div className="mt-2.5 grid grid-cols-2 gap-2.5">
            {[SystemLanguage.ENGLISH, SystemLanguage.NEPALI].map((lang) => {
              const meta = SYSTEM_LANGUAGE_META[lang];
              const isSelected = preferences.systemLanguage === lang;
              return (
                <button
                  key={lang}
                  type="button"
                  onClick={() => setSystemLanguage(lang)}
                  className="flex items-center justify-between rounded-[14px] border px-3 py-3 text-left transition-colors"
                  style={{
                    backgroundColor: isSelected
                      ? cssWithAlpha(tokens.accentColor, 0.2)
                      : cssWithAlpha(tokens.glassSurface, 0.6),
                    borderColor: isSelected ? tokens.accentColor : tokens.borderTop,
                    borderWidth: isSelected ? '1.6px' : '0.8px',
                  }}
                >
                  <div>
                    <div
                      className="text-[14.5px] font-bold"
                      style={{ color: isSelected ? tokens.accentColor : tokens.textPrimary }}
                    >
                      {meta.nativeName}
                    </div>
                    <div className="text-[11.5px]" style={{ color: tokens.textSecondary }}>
                      {meta.displayName}
                    </div>
                  </div>
                  {isSelected ? (
                    <CheckCircle2 className="h-5 w-5" style={{ color: tokens.accentColor }} />
                  ) : (
                    <Circle className="h-5 w-5" style={{ color: tokens.textSecondary }} />
                  )}
                </button>
              );
            })}
          </div>
        </SettingsCard>

        {/* 1. Liquid Glass & Theme Studio */}
        <SettingsCard title={strings.themesCardTitle} icon={<Palette className="h-5 w-5" />}>
          <div className="flex items-center justify-between gap-2">
            <div className="min-w-0 flex-1">
              <div className="text-[15.5px] font-bold" style={{ color: tokens.accentColor }}>
                {strings.themePresetName(currentTheme.preset)}
              </div>
              <div className="text-xs" style={{ color: tokens.textSecondary }}>
                {strings.themeSummaryStats(
                  Math.round(currentTheme.transparencyLevel * 100),
                  Math.round(currentTheme.blurIntensity),
                  SCRIPTURE_FONT_META[currentTheme.fontStyle].label
                )}
              </div>
            </div>
            <span
              className="shrink-0 rounded-full px-2.5 py-1 text-[11px] font-semibold"
              style={{
                backgroundColor: cssWithAlpha(tokens.accentColor, 0.16),
                color: tokens.accentColor,
              }}
            >
              {strings.readingBgName(currentTheme.readingPageBackground)}
            </span>
          </div>

          <div className="no-scrollbar mt-2.5 flex gap-2 overflow-x-auto pb-1">
            {ALL_THEME_PRESETS.map((preset) => {
              const spec = THEME_PRESET_SPECS[preset];
              const isSelected = currentTheme.preset === preset;
              return (
                <button
                  key={preset}
                  type="button"
                  onClick={() => applyThemePreset(preset)}
                  className="w-[114px] shrink-0 rounded-[14px] border p-2 text-left transition-colors"
                  style={{
                    backgroundColor: isSelected
                      ? cssWithAlpha(tokens.accentColor, 0.22)
                      : tokens.glassSurface,
                    borderColor: isSelected ? tokens.accentColor : tokens.borderTop,
                    borderWidth: isSelected ? '1.6px' : '0.8px',
                  }}
                >
                  <div className="flex items-center justify-between">
                    <span
                      className="h-[22px] w-[22px] rounded-full border border-white/60"
                      style={{
                        background: `linear-gradient(135deg, ${argbToCss(
                          spec.defaultBackgroundArgb,
                          1
                        )}, ${argbToCss(spec.defaultSecondaryBgArgb, 1)})`,
                      }}
                    />
                    <span
                      className="h-3 w-3 rounded-full"
                      style={{ backgroundColor: argbToCss(spec.defaultAccentArgb, 1) }}
                    />
                  </div>
                  <div
                    className={`mt-1.5 truncate text-[11.5px] ${
                      isSelected ? 'font-bold' : 'font-medium'
                    }`}
                    style={{ color: tokens.textPrimary }}
                  >
                    {strings.themePresetName(preset)}
                  </div>
                </button>
              );
            })}
          </div>

          <LiquidGlassButton
            tokens={tokens}
            onClick={() => navigate('theme_studio')}
            className="mt-3 w-full text-[13.5px]"
          >
            <Sparkles className="h-[18px] w-[18px]" />
            {strings.customizeThemeButton}
          </LiquidGlassButton>
        </SettingsCard>

        {/* 2. Quick Night Study Dark Mode Toggle */}
        <SettingsCard title={strings.brightnessCardTitle} icon={<Moon className="h-5 w-5" />}>
          <div className="flex items-center justify-between gap-3">
            <div className="min-w-0 flex-1">
              <div className="font-semibold" style={{ color: tokens.textPrimary }}>
                {strings.nightDarkModeLabel}
              </div>
              <div className="text-sm" style={{ color: tokens.textSecondary }}>
                {preferences.darkMode ? strings.nightDarkModeOnSub : strings.nightDarkModeOffSub}
              </div>
            </div>
            <GlassSwitch
              checked={preferences.darkMode}
              onChange={setDarkMode}
              accentColor={tokens.accentColor}
            />
          </div>
        </SettingsCard>

        {/* 3. Languages & Offline Packs */}
        <SettingsCard title={strings.languagesCardTitle} icon={<Languages className="h-5 w-5" />}>
          <div className="text-sm font-semibold" style={{ color: tokens.textPrimary }}>
            {strings.activePairLabel(
              strings.bibleLanguageName(preferences.primaryLanguage),
              strings.bibleLanguageName(preferences.secondaryLanguage)
            )}
          </div>
          <div className="mt-0.5 text-sm" style={{ color: tokens.textSecondary }}>
            {strings.offlineStorageUsedLabel(formatBytes(totalStorageBytes, sysLang))}
          </div>
          <div className="mt-2.5 grid grid-cols-2 gap-2.5">
            <button
              type="button"
              onClick={openLanguageSheet}
              className="flex min-h-[44px] items-center justify-center gap-1.5 rounded-xl border px-3 text-xs font-semibold"
              style={{ borderColor: tokens.borderTop, color: tokens.textPrimary }}
            >
              <Languages className="h-4 w-4" />
              {strings.languagesButton}
            </button>
            <LiquidGlassButton
              tokens={tokens}
              onClick={() => navigate('downloads')}
              className="text-xs"
            >
              <Download className="h-4 w-4" />
              {strings.downloadsButton}
            </LiquidGlassButton>
          </div>
        </SettingsCard>

        {/* 4. Book Layout & Typography */}
        <SettingsCard title={strings.typographyCardTitle} icon={<BookOpen className="h-5 w-5" />}>
          <div className="text-sm font-semibold" style={{ color: tokens.textPrimary }}>
            {strings.readingModeLabel}
          </div>
          <div className="mt-1.5 grid grid-cols-2 gap-2">
            {[
              ReadingMode.BILINGUAL_SPREAD,
              ReadingMode.BILINGUAL_PARALLEL,
              ReadingMode.SINGLE_PRIMARY,
              ReadingMode.SINGLE_SECONDARY,
            ].map((mode) => {
              const selected = preferences.readingMode === mode;
              const label =
                mode === ReadingMode.SINGLE_PRIMARY
                  ? strings.singleLanguageOnly(
                      strings.bibleLanguageName(preferences.primaryLanguage)
                    )
                  : mode === ReadingMode.SINGLE_SECONDARY
                  ? strings.singleLanguageOnly(
                      strings.bibleLanguageName(preferences.secondaryLanguage)
                    )
                  : strings.readingModeName(mode);
              return (
                <button
                  key={mode}
                  type="button"
                  onClick={() => setReadingMode(mode)}
                  className="truncate rounded-xl border px-2.5 py-2 text-xs font-semibold transition-colors"
                  style={{
                    backgroundColor: selected
                      ? cssWithAlpha(tokens.accentColor, 0.2)
                      : 'transparent',
                    borderColor: selected ? tokens.accentColor : tokens.borderTop,
                    color: selected ? tokens.accentColor : tokens.textPrimary,
                  }}
                >
                  {label}
                </button>
              );
            })}
          </div>

          <div
            className="my-2.5 h-[0.7px] w-full"
            style={{ backgroundColor: cssWithAlpha(tokens.borderTop, 0.5) }}
          />

          <div className="flex items-center justify-between">
            <span className="font-semibold" style={{ color: tokens.textPrimary }}>
              {strings.scriptureFontSizeLabel}
            </span>
            <span className="font-bold" style={{ color: tokens.accentColor }}>
              {formatNumber(Math.round(preferences.fontSizeSp), sysLang)} px
            </span>
          </div>
          <input
            type="range"
            min={13}
            max={24}
            step={1}
            value={Math.round(preferences.fontSizeSp)}
            onChange={(e) => setFontSize(Number(e.target.value))}
            className="mt-2 w-full accent-current"
            style={{ color: tokens.accentColor }}
          />

          <div
            className="mt-2.5 rounded-xl border p-3"
            style={{
              backgroundColor: palette.paperBackground,
              borderColor: tokens.borderTop,
            }}
          >
            <p
              style={{
                fontFamily: getFontFamilyForBibleLanguage(
                  BibleLanguage.ENGLISH,
                  tokens.fontStyle
                ),
                fontSize: `${preferences.fontSizeSp}px`,
                lineHeight: 1.38,
                color: palette.inkPrimary,
              }}
            >
              In the beginning God created the heaven and the earth.
            </p>
            <p
              className="mt-1"
              style={{
                fontFamily: getFontFamilyForBibleLanguage(BibleLanguage.NEPALI, tokens.fontStyle),
                fontSize: `${preferences.fontSizeSp * 0.95}px`,
                lineHeight: 1.38,
                color: palette.inkSecondary,
              }}
            >
              आदिमा परमेश्‍वरले आकाशमण्डल र पृथ्वी सृष्‍टि गर्नुभयो ।
            </p>
          </div>
        </SettingsCard>

        {/* 5. Page-Turning Sound Studio */}
        <SettingsCard title={strings.pageSoundCardTitle} icon={<Volume2 className="h-5 w-5" />}>
          <div className="flex items-center justify-between gap-3">
            <div className="min-w-0 flex-1">
              <div className="font-semibold" style={{ color: tokens.textPrimary }}>
                {strings.pageSoundToggleTitle}
              </div>
              <div className="text-sm" style={{ color: tokens.textSecondary }}>
                {!preferences.pageSoundEnabled
                  ? strings.pageSoundMuted
                  : preferences.pageSoundStyle === PageTurnSoundStyle.SILENT
                  ? strings.pageSoundSilentMode
                  : strings.pageSoundActiveStatus(
                      strings.pageSoundName(preferences.pageSoundStyle),
                      volumePercent
                    )}
              </div>
            </div>
            <GlassSwitch
              checked={preferences.pageSoundEnabled}
              onChange={setPageSoundEnabled}
              accentColor={tokens.accentColor}
            />
          </div>

          {/* Random Sound Quick Switch */}
          <div
            className="mt-2.5 flex items-center justify-between gap-2 rounded-[14px] border px-3 py-2"
            style={{
              backgroundColor:
                preferences.pageSoundEnabled &&
                preferences.pageSoundStyle === PageTurnSoundStyle.RANDOM
                  ? cssWithAlpha(tokens.accentColor, 0.16)
                  : cssWithAlpha(tokens.glassSurface, 0.65),
              borderColor:
                preferences.pageSoundEnabled &&
                preferences.pageSoundStyle === PageTurnSoundStyle.RANDOM
                  ? tokens.accentColor
                  : tokens.borderTop,
            }}
          >
            <div className="flex min-w-0 flex-1 items-center gap-2.5">
              <Shuffle className="h-5 w-5 shrink-0" style={{ color: tokens.accentColor }} />
              <div className="min-w-0">
                <div className="text-[13.5px] font-semibold" style={{ color: tokens.textPrimary }}>
                  {strings.randomSoundModeTitle}
                </div>
                <div className="text-[11.5px]" style={{ color: tokens.textSecondary }}>
                  {strings.randomSoundModeSub}
                </div>
              </div>
            </div>
            <GlassSwitch
              checked={
                preferences.pageSoundEnabled &&
                preferences.pageSoundStyle === PageTurnSoundStyle.RANDOM
              }
              onChange={(useRandom) =>
                handleSelectPageSound(useRandom ? PageTurnSoundStyle.RANDOM : lastAudibleStyle)
              }
              accentColor={tokens.accentColor}
            />
          </div>

          {/* Volume Slider */}
          <div className="mt-3 flex items-center justify-between text-sm">
            <span className="font-semibold" style={{ color: tokens.textPrimary }}>
              {strings.pageSoundVolumeTitle}
            </span>
            <span className="font-bold" style={{ color: tokens.accentColor }}>
              {formatPercent(volumePercent, sysLang)}
            </span>
          </div>
          <input
            type="range"
            min={0}
            max={100}
            step={5}
            disabled={!effectiveAudible}
            value={volumePercent}
            onChange={(e) => setPageSoundVolume(Number(e.target.value) / 100)}
            className="mt-1.5 w-full accent-current disabled:opacity-40"
            style={{ color: tokens.accentColor }}
          />

          <div
            className="my-2.5 h-[0.7px] w-full"
            style={{ backgroundColor: cssWithAlpha(tokens.borderTop, 0.5) }}
          />

          <div className="text-sm font-semibold" style={{ color: tokens.textPrimary }}>
            {strings.choosePageSoundTitle}
          </div>
          <div className="text-xs" style={{ color: tokens.textSecondary }}>
            {strings.choosePageSoundSub}
          </div>

          <div className="mt-2 space-y-2">
            {ALL_PAGE_SOUND_STYLES.map((style) => {
              const isSelected = preferences.pageSoundStyle === style;
              const isAudible = style !== PageTurnSoundStyle.SILENT;
              return (
                <div
                  key={style}
                  onClick={() => handleSelectPageSound(style)}
                  className="flex cursor-pointer items-center justify-between gap-2 rounded-[14px] border px-3 py-2 transition-colors"
                  style={{
                    backgroundColor: isSelected
                      ? cssWithAlpha(tokens.accentColor, 0.18)
                      : cssWithAlpha(tokens.glassSurface, 0.55),
                    borderColor: isSelected ? tokens.accentColor : tokens.borderTop,
                    borderWidth: isSelected ? '1.5px' : '0.8px',
                  }}
                >
                  <div className="flex min-w-0 flex-1 items-center gap-2.5">
                    {isSelected ? (
                      <CheckCircle2
                        className="h-5 w-5 shrink-0"
                        style={{ color: tokens.accentColor }}
                      />
                    ) : style === PageTurnSoundStyle.RANDOM ? (
                      <Shuffle
                        className="h-5 w-5 shrink-0"
                        style={{ color: tokens.textSecondary }}
                      />
                    ) : !isAudible ? (
                      <VolumeX
                        className="h-5 w-5 shrink-0"
                        style={{ color: tokens.textSecondary }}
                      />
                    ) : (
                      <Circle className="h-5 w-5 shrink-0" style={{ color: tokens.textSecondary }} />
                    )}
                    <div className="min-w-0">
                      <div
                        className={`text-[13.5px] ${isSelected ? 'font-bold' : 'font-medium'}`}
                        style={{ color: tokens.textPrimary }}
                      >
                        {strings.pageSoundName(style)}
                      </div>
                      <div
                        className="truncate text-[11.5px]"
                        style={{ color: tokens.textSecondary }}
                      >
                        {strings.pageSoundDescription(style)}
                      </div>
                    </div>
                  </div>

                  {isAudible && (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        previewPageSound(style, true);
                      }}
                      className="inline-flex shrink-0 items-center gap-1 rounded-xl border px-2.5 py-2 text-[11.5px] font-semibold transition-colors hover:brightness-95"
                      style={{
                        backgroundColor: cssWithAlpha(tokens.accentColor, 0.14),
                        borderColor: cssWithAlpha(tokens.accentColor, 0.45),
                        color: tokens.accentColor,
                      }}
                    >
                      <Play className="h-3.5 w-3.5" />
                      {strings.previewButton}
                    </button>
                  )}
                </div>
              );
            })}
          </div>

          <div className="mt-2.5 grid grid-cols-2 gap-2.5">
            <button
              type="button"
              disabled={!effectiveAudible}
              onClick={playForwardSound}
              className="rounded-xl border px-3 py-2 text-xs font-semibold disabled:opacity-45"
              style={{ borderColor: tokens.borderTop, color: tokens.textPrimary }}
            >
              {strings.testForwardButton}
            </button>
            <button
              type="button"
              disabled={!effectiveAudible}
              onClick={playBackwardSound}
              className="rounded-xl border px-3 py-2 text-xs font-semibold disabled:opacity-45"
              style={{ borderColor: tokens.borderTop, color: tokens.textPrimary }}
            >
              {strings.testBackwardButton}
            </button>
          </div>
        </SettingsCard>

        {/* 6. Audio Bible Narration */}
        <SettingsCard title={strings.audioNarrationCardTitle} icon={<Headphones className="h-5 w-5" />}>
          <div className="text-sm font-semibold" style={{ color: tokens.textPrimary }}>
            {strings.narrationLanguageLabel}
          </div>
          <div className="no-scrollbar mt-1.5 flex gap-1.5 overflow-x-auto pb-1">
            {[
              AudioLanguage.BILINGUAL,
              AudioLanguage.ENGLISH,
              AudioLanguage.NEPALI,
              AudioLanguage.ONCV,
              AudioLanguage.KOREAN,
            ].map((lang) => {
              const selected = preferences.audioLanguage === lang;
              return (
                <button
                  key={lang}
                  type="button"
                  onClick={() => setAudioLanguage(lang)}
                  className="shrink-0 rounded-full border px-3 py-1 text-[11px] font-semibold transition-colors"
                  style={{
                    backgroundColor: selected
                      ? cssWithAlpha(tokens.accentColor, 0.2)
                      : 'transparent',
                    borderColor: selected ? tokens.accentColor : tokens.borderTop,
                    color: selected ? tokens.accentColor : tokens.textPrimary,
                  }}
                >
                  {strings.audioLanguageName(lang)}
                </button>
              );
            })}
          </div>

          <div className="mt-3 flex items-center justify-between">
            <span className="font-semibold" style={{ color: tokens.textPrimary }}>
              {strings.speechSpeedLabel}
            </span>
            <span className="font-bold" style={{ color: tokens.accentColor }}>
              {formatSpeed(preferences.audioSpeed, sysLang)}
            </span>
          </div>
          <input
            type="range"
            min={0.7}
            max={1.5}
            step={0.1}
            value={preferences.audioSpeed}
            onChange={(e) => setAudioSpeed(Number(e.target.value))}
            className="mt-1.5 w-full accent-current"
            style={{ color: tokens.accentColor }}
          />

          <p className="mt-2 text-xs leading-relaxed" style={{ color: tokens.textSecondary }}>
            {strings.deviceVoicesStatus(
              audioState.isEnReady,
              audioState.isNeReady,
              audioState.isKoReady
            )}
          </p>

          <button
            type="button"
            onClick={() => {
              refreshAudioVoices();
              showToast(strings.voicesRefreshedToast);
            }}
            className="mt-2 inline-flex items-center gap-1.5 rounded-xl border px-3 py-2 text-xs font-semibold"
            style={{ borderColor: tokens.borderTop, color: tokens.textPrimary }}
          >
            <RefreshCw className="h-3.5 w-3.5" style={{ color: tokens.accentColor }} />
            {strings.refreshSpeechVoices}
          </button>
        </SettingsCard>

        {/* 7. Copyright & Credits */}
        <CopyrightAndCreditsSection />
      </div>
    </div>
  );
};

const SettingsCard: React.FC<{
  title: string;
  icon: React.ReactNode;
  children: React.ReactNode;
}> = ({ title, icon, children }) => {
  const { tokens, preferences } = useBibleApp();
  const sysLang = preferences.systemLanguage;
  return (
    <LiquidGlassSurface tokens={tokens} className="rounded-[20px] p-4">
      <div className="flex items-center gap-2">
        <span style={{ color: tokens.accentColor }}>{icon}</span>
        <h2
          className="text-[17px] font-bold"
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
      </div>
      <div className="mt-3">{children}</div>
    </LiquidGlassSurface>
  );
};

const GlassSwitch: React.FC<{
  checked: boolean;
  onChange: (checked: boolean) => void;
  accentColor: string;
}> = ({ checked, onChange, accentColor }) => (
  <button
    type="button"
    role="switch"
    aria-checked={checked}
    onClick={() => onChange(!checked)}
    className="relative inline-flex h-7 w-12 shrink-0 items-center rounded-full p-0.5 transition-colors"
    style={{
      backgroundColor: checked ? accentColor : 'rgba(128,128,128,0.35)',
    }}
  >
    <span
      className={`h-6 w-6 rounded-full bg-white shadow transition-transform ${
        checked ? 'translate-x-5' : 'translate-x-0'
      }`}
    />
  </button>
);
