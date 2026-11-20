import React, { useEffect, useState } from 'react';
import {
  Bookmark,
  BookmarkCheck,
  Copy,
  FileEdit,
  PlayCircle,
  Share2,
  X,
} from 'lucide-react';
import {
  BIBLE_LANGUAGE_META,
  BibleBook,
  BibleLanguage,
  BibleVerse,
  HighlightColor,
  SystemLanguage,
  getBookNameForLanguage,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { cssWithAlpha } from '../theme/themeTokens';
import {
  buildWhatsAppUrl,
  copyTextToClipboard,
  formatNumber,
  formatVerseShareText,
} from '../utils/formatters';
import { LiquidGlassDialog, LiquidGlassSurface } from './LiquidGlassShell';

interface VerseActionBarProps {
  book: BibleBook;
  verse: BibleVerse;
  primaryLanguage: BibleLanguage;
  secondaryLanguage: BibleLanguage;
  isBookmarked: boolean;
  currentHighlight: HighlightColor;
  existingNote?: string;
  onToggleBookmark: () => void;
  onSelectHighlight: (color: HighlightColor) => void;
  onOpenNoteDialog: () => void;
  onListenFromHere: () => void;
  onDismiss: () => void;
}

const HIGHLIGHT_SWATCHES: Array<{ color: HighlightColor; hex: string; label: string }> = [
  { color: HighlightColor.GOLD, hex: '#F6D56A', label: 'Gold' },
  { color: HighlightColor.ROSE, hex: '#F2A7B5', label: 'Rose' },
  { color: HighlightColor.SAGE, hex: '#9FD69B', label: 'Sage' },
  { color: HighlightColor.SKY, hex: '#8AC4EB', label: 'Sky' },
];

export const VerseActionBar: React.FC<VerseActionBarProps> = ({
  book,
  verse,
  primaryLanguage,
  secondaryLanguage,
  isBookmarked,
  currentHighlight,
  existingNote,
  onToggleBookmark,
  onSelectHighlight,
  onOpenNoteDialog,
  onListenFromHere,
  onDismiss,
}) => {
  const { palette, tokens, strings, preferences, showToast } = useBibleApp();
  const sysLang = preferences.systemLanguage;
  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';

  const chP = formatNumber(
    verse.chapter,
    BIBLE_LANGUAGE_META[primaryLanguage].usesDevanagari ? SystemLanguage.NEPALI : sysLang
  );
  const vP = formatNumber(
    verse.verse,
    BIBLE_LANGUAGE_META[primaryLanguage].usesDevanagari ? SystemLanguage.NEPALI : sysLang
  );
  const chS = formatNumber(
    verse.chapter,
    BIBLE_LANGUAGE_META[secondaryLanguage].usesDevanagari ? SystemLanguage.NEPALI : sysLang
  );
  const vS = formatNumber(
    verse.verse,
    BIBLE_LANGUAGE_META[secondaryLanguage].usesDevanagari ? SystemLanguage.NEPALI : sysLang
  );

  const shareText = formatVerseShareText(
    book,
    verse,
    primaryLanguage,
    secondaryLanguage,
    strings.shareAppBrand
  );

  const handleCopy = async () => {
    await copyTextToClipboard(shareText);
    showToast(strings.toastVerseCopied);
  };

  const handleFacebook = async () => {
    await copyTextToClipboard(shareText);
    showToast(strings.toastFacebookCopied);
  };

  const handleWebShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: `${getBookNameForLanguage(book, primaryLanguage)} ${verse.chapter}:${verse.verse}`,
          text: shareText,
        });
        return;
      } catch {
        // Fallback to copy
      }
    }
    await copyTextToClipboard(shareText);
    showToast(strings.toastVerseCopied);
  };

  return (
    <div className="pointer-events-none absolute inset-x-0 bottom-0 z-30 flex justify-center px-3 pb-2.5">
      <LiquidGlassSurface
        tokens={tokens}
        elevated
        className="pointer-events-auto w-full max-w-2xl rounded-[22px] p-3.5"
      >
        <div className="flex items-center justify-between gap-2">
          <div className="min-w-0 flex-1">
            <div
              className="truncate text-[15px] font-bold"
              style={{ fontFamily: headerFont, color: palette.leatherAccent }}
            >
              {getBookNameForLanguage(book, primaryLanguage)} {chP}:{vP} •{' '}
              {getBookNameForLanguage(book, secondaryLanguage)} {chS}:{vS}
            </div>
            {existingNote?.trim() && (
              <p className="truncate text-xs" style={{ color: tokens.textSecondary }}>
                {strings.notePrefix(existingNote)}
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={onDismiss}
            aria-label={strings.closeVerseBarDesc}
            className="rounded-full p-1.5 hover:bg-black/5"
            style={{ color: tokens.textPrimary }}
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Highlight Swatches */}
        <div className="mt-2.5 flex items-center gap-2.5">
          <span className="text-xs font-medium" style={{ color: tokens.textSecondary }}>
            {strings.highlightLabel}
          </span>
          {HIGHLIGHT_SWATCHES.map((s) => {
            const selected = currentHighlight === s.color;
            return (
              <button
                key={s.color}
                type="button"
                aria-label={s.label}
                onClick={() =>
                  onSelectHighlight(selected ? HighlightColor.NONE : s.color)
                }
                className="h-7 w-7 rounded-full transition-transform active:scale-95"
                style={{
                  backgroundColor: s.hex,
                  border: selected
                    ? `2.5px solid ${tokens.textPrimary}`
                    : '1px solid rgba(128,128,128,0.45)',
                }}
              />
            );
          })}
          {currentHighlight !== HighlightColor.NONE && (
            <button
              type="button"
              onClick={() => onSelectHighlight(HighlightColor.NONE)}
              className="text-xs font-semibold"
              style={{ color: tokens.accentColor }}
            >
              {strings.clear}
            </button>
          )}
        </div>

        {/* Action Chips */}
        <div className="no-scrollbar mt-2.5 flex gap-2 overflow-x-auto pb-0.5">
          <ActionPill
            icon={<PlayCircle className="h-4 w-4" />}
            label={strings.listenAction}
            onClick={onListenFromHere}
          />
          <ActionPill
            icon={
              isBookmarked ? (
                <BookmarkCheck className="h-4 w-4" />
              ) : (
                <Bookmark className="h-4 w-4" />
              )
            }
            label={isBookmarked ? strings.bookmarkedAction : strings.bookmarkAction}
            onClick={onToggleBookmark}
          />
          <ActionPill
            icon={<FileEdit className="h-4 w-4" />}
            label={existingNote?.trim() ? strings.editNoteAction : strings.addNoteAction}
            onClick={onOpenNoteDialog}
          />
          <a
            href={buildWhatsAppUrl(shareText)}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex shrink-0 items-center gap-1.5 rounded-xl border px-3 py-2 text-xs font-medium transition-colors hover:brightness-95"
            style={{
              backgroundColor: tokens.glassSurface,
              borderColor: tokens.borderTop,
              color: tokens.textPrimary,
            }}
          >
            <Share2 className="h-4 w-4" style={{ color: tokens.accentColor }} />
            <span>{strings.whatsappAction}</span>
          </a>
          <ActionPill
            icon={<Share2 className="h-4 w-4" />}
            label={strings.facebookAction}
            onClick={() => void handleFacebook()}
          />
          <ActionPill
            icon={<Copy className="h-4 w-4" />}
            label={strings.copyAction}
            onClick={() => void handleCopy()}
          />
          <ActionPill
            icon={<Share2 className="h-4 w-4" />}
            label={strings.moreAction}
            onClick={() => void handleWebShare()}
          />
        </div>
      </LiquidGlassSurface>
    </div>
  );
};

const ActionPill: React.FC<{
  icon: React.ReactNode;
  label: string;
  onClick: () => void;
}> = ({ icon, label, onClick }) => {
  const { tokens } = useBibleApp();
  return (
    <button
      type="button"
      onClick={onClick}
      className="inline-flex shrink-0 items-center gap-1.5 rounded-xl border px-3 py-2 text-xs font-medium transition-colors hover:brightness-95"
      style={{
        backgroundColor: tokens.glassSurface,
        borderColor: tokens.borderTop,
        color: tokens.textPrimary,
      }}
    >
      <span style={{ color: tokens.accentColor }}>{icon}</span>
      <span>{label}</span>
    </button>
  );
};

export const NoteEditorDialog: React.FC<{
  open: boolean;
  referenceTitle: string;
  initialNote: string;
  onSave: (text: string) => void;
  onDelete?: () => void;
  onDismiss: () => void;
}> = ({ open, referenceTitle, initialNote, onSave, onDelete, onDismiss }) => {
  const { tokens, strings, preferences } = useBibleApp();
  const [noteText, setNoteText] = useState(initialNote);

  useEffect(() => {
    if (open) setNoteText(initialNote);
  }, [open, initialNote]);

  const headerFont =
    preferences.systemLanguage === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';

  return (
    <LiquidGlassDialog open={open} onClose={onDismiss} tokens={tokens}>
      <h3
        className="text-lg font-bold"
        style={{ fontFamily: headerFont, color: tokens.textPrimary }}
      >
        {strings.personalNoteTitle(referenceTitle)}
      </h3>

      <textarea
        rows={5}
        value={noteText}
        onChange={(e) => setNoteText(e.target.value)}
        placeholder={strings.notePlaceholder}
        className="mt-3 w-full rounded-xl border p-3 text-sm outline-none"
        style={{
          backgroundColor: cssWithAlpha(tokens.glassSurface, 0.5),
          borderColor: tokens.borderTop,
          color: tokens.textPrimary,
        }}
      />

      <div className="mt-4 flex items-center justify-end gap-2">
        {onDelete && initialNote.trim() && (
          <button
            type="button"
            onClick={onDelete}
            className="rounded-xl border px-3.5 py-2 text-xs font-semibold"
            style={{ borderColor: tokens.borderTop, color: tokens.textPrimary }}
          >
            {strings.delete}
          </button>
        )}
        <button
          type="button"
          onClick={onDismiss}
          className="rounded-xl px-3.5 py-2 text-xs font-semibold"
          style={{ color: tokens.textSecondary }}
        >
          {strings.cancel}
        </button>
        <button
          type="button"
          onClick={() => onSave(noteText)}
          className="rounded-xl px-4 py-2 text-xs font-bold"
          style={{
            backgroundColor: tokens.accentColor,
            color: tokens.onAccentColor,
          }}
        >
          {strings.save}
        </button>
      </div>
    </LiquidGlassDialog>
  );
};
