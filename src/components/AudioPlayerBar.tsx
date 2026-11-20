import React from 'react';
import { AudioLines, Pause, Play, SkipBack, SkipForward, X } from 'lucide-react';
import { AudioLanguage, AudioPlaybackState, SystemLanguage } from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { cssWithAlpha } from '../theme/themeTokens';
import { formatSpeed, toDevanagari } from '../utils/formatters';
import { LiquidGlassSurface } from './LiquidGlassShell';

interface AudioPlayerBarProps {
  audioState: AudioPlaybackState;
  onTogglePlayPause: () => void;
  onPrevVerse: () => void;
  onNextVerse: () => void;
  onCycleLanguage: (lang: AudioLanguage) => void;
  onCycleSpeed: (speed: number) => void;
  onClose: () => void;
  onJumpToPlaying: () => void;
}

export const AudioPlayerBar: React.FC<AudioPlayerBarProps> = ({
  audioState,
  onTogglePlayPause,
  onPrevVerse,
  onNextVerse,
  onCycleLanguage,
  onCycleSpeed,
  onClose,
  onJumpToPlaying,
}) => {
  const { tokens, strings, preferences } = useBibleApp();
  if (!audioState.isActiveSession) return null;

  const sysLang = preferences.systemLanguage;
  const neRef = `${audioState.bookNameNe} ${toDevanagari(audioState.chapter)}:${toDevanagari(
    audioState.currentVerse
  )}`;
  const enRef = `${audioState.bookNameEn} ${audioState.chapter}:${audioState.currentVerse}`;

  const handleSpeedClick = () => {
    const next =
      audioState.speechRate < 0.9 ? 1.0 : audioState.speechRate < 1.1 ? 1.25 : 0.8;
    onCycleSpeed(next);
  };

  const handleLangClick = () => {
    const order = [
      AudioLanguage.BILINGUAL,
      AudioLanguage.ENGLISH,
      AudioLanguage.NEPALI,
      AudioLanguage.ONCV,
      AudioLanguage.KOREAN,
    ];
    const idx = order.indexOf(audioState.audioLanguage);
    const nextLang = order[(idx + 1) % order.length];
    onCycleLanguage(nextLang);
  };

  return (
    <div className="pointer-events-none absolute inset-x-0 bottom-0 z-30 flex justify-center px-3 pb-2.5">
      <LiquidGlassSurface
        tokens={tokens}
        elevated
        className="pointer-events-auto w-full max-w-2xl rounded-[20px] px-3.5 py-2.5"
      >
        <div className="flex items-center justify-between gap-2">
          <button
            type="button"
            onClick={onJumpToPlaying}
            className="flex min-w-0 flex-1 items-center gap-2 rounded-lg py-0.5 text-left"
          >
            <AudioLines className="h-5 w-5 shrink-0" style={{ color: tokens.accentColor }} />
            <div className="min-w-0">
              <div className="truncate text-[13.5px] font-bold" style={{ color: tokens.textPrimary }}>
                {sysLang === SystemLanguage.NEPALI ? neRef : enRef}
              </div>
              <div className="truncate text-[11.5px]" style={{ color: tokens.textSecondary }}>
                {sysLang === SystemLanguage.NEPALI ? enRef : neRef}
              </div>
            </div>
          </button>

          <div className="flex shrink-0 items-center gap-1.5">
            <button
              type="button"
              onClick={handleSpeedClick}
              className="rounded-lg px-2 py-1 text-[11px] font-semibold"
              style={{
                backgroundColor: cssWithAlpha(tokens.accentColor, 0.16),
                color: tokens.textPrimary,
              }}
            >
              {formatSpeed(audioState.speechRate, sysLang)}
            </button>
            <button
              type="button"
              onClick={handleLangClick}
              className="rounded-lg px-2 py-1 text-[11px] font-semibold"
              style={{
                backgroundColor: cssWithAlpha(tokens.accentColor, 0.24),
                color: tokens.textPrimary,
              }}
            >
              {strings.audioLanguageName(audioState.audioLanguage)}
            </button>
          </div>
        </div>

        <div className="mt-1 flex items-center justify-between">
          <div className="flex items-center gap-1">
            <button
              type="button"
              onClick={onPrevVerse}
              aria-label={strings.audioPrevDesc}
              className="rounded-full p-2 hover:bg-black/5"
              style={{ color: tokens.textPrimary }}
            >
              <SkipBack className="h-5 w-5" />
            </button>
            <button
              type="button"
              onClick={onTogglePlayPause}
              aria-label={audioState.isPlaying ? strings.audioPauseDesc : strings.audioPlayDesc}
              className="rounded-full p-1.5 hover:bg-black/5"
              style={{ color: tokens.accentColor }}
            >
              {audioState.isPlaying ? (
                <Pause className="h-7 w-7" />
              ) : (
                <Play className="h-7 w-7" />
              )}
            </button>
            <button
              type="button"
              onClick={onNextVerse}
              aria-label={strings.audioNextDesc}
              className="rounded-full p-2 hover:bg-black/5"
              style={{ color: tokens.textPrimary }}
            >
              <SkipForward className="h-5 w-5" />
            </button>
          </div>

          <button
            type="button"
            onClick={onClose}
            aria-label={strings.audioStopDesc}
            className="rounded-full p-2 hover:bg-black/5"
            style={{ color: tokens.textSecondary }}
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {audioState.statusNote && (
          <p className="mt-1 text-[10.5px] leading-snug" style={{ color: tokens.textSecondary }}>
            {audioState.statusNote}
          </p>
        )}
      </LiquidGlassSurface>
    </div>
  );
};
