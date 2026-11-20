import React, { useEffect, useRef, useState } from 'react';
import {
  ArrowLeft,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  Headphones,
  Languages,
  Pause,
  Settings,
  Volume2,
  VolumeX,
} from 'lucide-react';
import {
  BIBLE_LANGUAGE_META,
  BibleLanguage,
  BibleVerse,
  HighlightColor,
  ReadingMode,
  SystemLanguage,
  getBookNameForLanguage,
  getFontFamilyForBibleLanguage,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { cssWithAlpha } from '../theme/themeTokens';
import { formatNumber, toDevanagari } from '../utils/formatters';
import { LiquidGlassSurface } from '../components/LiquidGlassShell';
import { FlipBookHandle, FlipBookPager } from '../components/FlipBookPager';
import { AudioPlayerBar } from '../components/AudioPlayerBar';
import { NoteEditorDialog, VerseActionBar } from '../components/VerseActionBar';

export const ReaderScreen: React.FC = () => {
  const {
    readerState,
    preferences,
    audioState,
    downloadedBookKeys,
    bookmarkedKeys,
    highlightMap,
    notesMap,
    palette,
    tokens,
    strings,
    goBack,
    navigate,
    openScripturePicker,
    openLanguageSheet,
    openChapter,
    goToNextChapter,
    goToPreviousChapter,
    clearTargetHighlight,
    savePageProgress,
    selectVerse,
    toggleBookmark,
    setHighlight,
    saveNote,
    deleteNote,
    setPageSoundEnabled,
    playForwardSound,
    playBackwardSound,
    startAudioFromVerse,
    toggleAudioPlayPause,
    audioPrevVerse,
    audioNextVerse,
    setAudioLanguage,
    setAudioSpeed,
    stopAudio,
    downloadBook,
  } = useBibleApp();

  const [noteEditingVerse, setNoteEditingVerse] = useState<BibleVerse | null>(null);
  const flipRef = useRef<FlipBookHandle | null>(null);

  const sysLang = preferences.systemLanguage;
  const book = readerState.currentBook;
  const primaryLang = preferences.primaryLanguage;
  const secondaryLang = preferences.secondaryLanguage;

  // Keep Reader in sync if Audio Bible auto-advances into the next chapter
  useEffect(() => {
    if (
      audioState.isPlaying &&
      book &&
      (audioState.bookId !== book.id || audioState.chapter !== readerState.currentChapter)
    ) {
      void openChapter(audioState.bookId, audioState.chapter, audioState.currentVerse);
    }
  }, [
    audioState.isPlaying,
    audioState.bookId,
    audioState.chapter,
    audioState.currentVerse,
    book,
    readerState.currentChapter,
    openChapter,
  ]);

  const primaryMeta = BIBLE_LANGUAGE_META[primaryLang];
  const secondaryMeta = BIBLE_LANGUAGE_META[secondaryLang];

  const primaryChap = primaryMeta.usesDevanagari
    ? toDevanagari(readerState.currentChapter)
    : String(readerState.currentChapter);
  const secondaryChap = secondaryMeta.usesDevanagari
    ? toDevanagari(readerState.currentChapter)
    : String(readerState.currentChapter);

  const pillLabel =
    preferences.readingMode === ReadingMode.SINGLE_PRIMARY
      ? primaryMeta.shortBadge
      : preferences.readingMode === ReadingMode.SINGLE_SECONDARY
      ? secondaryMeta.shortBadge
      : `${primaryMeta.shortBadge}•${secondaryMeta.shortBadge}`;

  const activeAudioKey = audioState.isActiveSession
    ? `${audioState.bookId}:${audioState.chapter}:${audioState.currentVerse}`
    : null;

  const targetScrollVerse =
    audioState.isPlaying &&
    book &&
    audioState.bookId === book.id &&
    audioState.chapter === readerState.currentChapter
      ? audioState.currentVerse
      : readerState.targetHighlightVerse;

  const selectedVerseKey =
    readerState.selectedVerse?.key ??
    (readerState.targetHighlightVerse && book
      ? `${book.id}:${readerState.currentChapter}:${readerState.targetHighlightVerse}`
      : null);

  const existingNoteText = noteEditingVerse
    ? notesMap.get(noteEditingVerse.key)?.noteText ?? ''
    : '';

  return (
    <div
      className="flex h-full w-full flex-col overflow-hidden"
      style={{ backgroundColor: palette.deskBackground }}
    >
      {/* Reader Top Liquid Glass Toolbar */}
      <LiquidGlassSurface
        tokens={tokens}
        elevated
        className="z-20 shrink-0 rounded-b-[20px] px-1.5 py-1.5"
      >
        <div className="mx-auto flex max-w-5xl items-center justify-between gap-1">
          <button
            type="button"
            onClick={goBack}
            aria-label={strings.backToHomeDesc}
            className="rounded-full p-2 transition-colors hover:bg-black/5"
            style={{ color: tokens.textPrimary }}
          >
            <ArrowLeft className="h-5 w-5" />
          </button>

          {/* Book & Chapter Selector Pill */}
          <button
            type="button"
            onClick={() => openScripturePicker(null)}
            aria-label={strings.chooseBookChapterDesc}
            className="flex min-w-0 flex-1 items-center justify-center gap-1 rounded-[14px] px-2.5 py-1.5 transition-colors hover:brightness-95"
            style={{
              backgroundColor: cssWithAlpha(tokens.accentColor, tokens.isDark ? 0.24 : 0.14),
            }}
          >
            <div className="min-w-0 text-center">
              <div
                className="truncate text-[14.5px] font-bold leading-tight"
                style={{
                  fontFamily:
                    primaryLang === BibleLanguage.ENGLISH
                      ? '"Playfair Display", Georgia, serif'
                      : getFontFamilyForBibleLanguage(primaryLang, tokens.fontStyle),
                  color: tokens.textPrimary,
                }}
              >
                {book ? `${getBookNameForLanguage(book, primaryLang)} ${primaryChap}` : strings.loading}
              </div>
              {book && preferences.readingMode !== ReadingMode.SINGLE_PRIMARY && (
                <div
                  className="truncate text-[11px] leading-tight"
                  style={{
                    fontFamily: getFontFamilyForBibleLanguage(secondaryLang, tokens.fontStyle),
                    color: tokens.textSecondary,
                  }}
                >
                  {getBookNameForLanguage(book, secondaryLang)} {secondaryChap}
                </div>
              )}
            </div>
            <ChevronDown className="h-4 w-4 shrink-0" style={{ color: tokens.accentColor }} />
          </button>

          {/* Language & Compare Selector Pill */}
          <button
            type="button"
            onClick={openLanguageSheet}
            aria-label={strings.selectLanguagesDesc}
            className="ml-1 flex shrink-0 items-center gap-1 rounded-xl border px-2 py-1.5 text-[11px] font-bold"
            style={{
              backgroundColor: tokens.glassSurface,
              borderColor: tokens.borderTop,
              color: tokens.textPrimary,
            }}
          >
            <Languages className="h-3.5 w-3.5" style={{ color: tokens.accentColor }} />
            <span>{pillLabel}</span>
          </button>

          {/* Page Flip Sound Toggle */}
          <button
            type="button"
            onClick={() => setPageSoundEnabled(!preferences.pageSoundEnabled)}
            aria-label={
              preferences.pageSoundEnabled ? strings.pageSoundOnDesc : strings.pageSoundOffDesc
            }
            title={
              preferences.pageSoundEnabled ? strings.pageSoundOnDesc : strings.pageSoundOffDesc
            }
            className="rounded-full p-2 transition-colors hover:bg-black/5"
            style={{
              color: preferences.pageSoundEnabled ? tokens.accentColor : tokens.textSecondary,
            }}
          >
            {preferences.pageSoundEnabled ? (
              <Volume2 className="h-5 w-5" />
            ) : (
              <VolumeX className="h-5 w-5" />
            )}
          </button>

          {/* Audio Bible Toggle */}
          <button
            type="button"
            onClick={() => {
              if (
                audioState.isActiveSession &&
                book &&
                audioState.bookId === book.id &&
                audioState.chapter === readerState.currentChapter
              ) {
                toggleAudioPlayPause();
              } else {
                startAudioFromVerse(1);
              }
            }}
            aria-label={strings.audioBibleDescReader}
            title={strings.audioBibleDescReader}
            className="rounded-full p-2 transition-colors hover:bg-black/5"
            style={{
              color: audioState.isActiveSession ? palette.goldAccent : tokens.textPrimary,
            }}
          >
            {audioState.isPlaying ? (
              <Pause className="h-5 w-5" />
            ) : (
              <Headphones className="h-5 w-5" />
            )}
          </button>

          {/* Settings */}
          <button
            type="button"
            onClick={() => navigate('settings')}
            aria-label={strings.readerSettingsDesc}
            title={strings.readerSettingsDesc}
            className="rounded-full p-2 transition-colors hover:bg-black/5"
            style={{ color: tokens.textPrimary }}
          >
            <Settings className="h-5 w-5" />
          </button>
        </div>
      </LiquidGlassSurface>

      {/* Main Full-Chapter Scrollable 3D Flip-Book Area */}
      <div className="relative flex min-h-0 flex-1 flex-col">
        {readerState.isLoading || !book ? (
          <div className="flex flex-1 items-center justify-center">
            <div
              className="h-8 w-8 animate-spin rounded-full border-4 border-t-transparent"
              style={{ borderColor: `${palette.leatherAccent} transparent ${palette.leatherAccent} ${palette.leatherAccent}` }}
            />
          </div>
        ) : (
          <>
            <div className="flex min-h-0 flex-1 flex-col">
              <FlipBookPager
                ref={flipRef}
                book={book}
                chapter={readerState.currentChapter}
                verses={readerState.verses}
                prevSnapshot={readerState.prevChapterSnapshot}
                nextSnapshot={readerState.nextChapterSnapshot}
                readingMode={preferences.readingMode}
                primaryLanguage={primaryLang}
                secondaryLanguage={secondaryLang}
                downloadedBookKeys={downloadedBookKeys}
                fontSizeSp={preferences.fontSizeSp}
                selectedVerseKey={selectedVerseKey}
                activeAudioVerseKey={activeAudioKey}
                targetScrollVerse={targetScrollVerse}
                bookmarkedKeys={bookmarkedKeys}
                highlightMap={highlightMap}
                noteKeys={new Set(notesMap.keys())}
                onRequestPreviousChapter={goToPreviousChapter}
                onRequestNextChapter={goToNextChapter}
                onPlayForwardSound={playForwardSound}
                onPlayBackwardSound={playBackwardSound}
                onVerseClick={(v) => {
                  clearTargetHighlight();
                  selectVerse(v);
                }}
                onTopVisibleVerseChanged={savePageProgress}
                onDownloadLanguageBook={downloadBook}
                onOpenDownloadsManager={() => navigate('downloads')}
              />
            </div>

            {readerState.selectedVerse ? (
              <VerseActionBar
                book={book}
                verse={readerState.selectedVerse}
                primaryLanguage={primaryLang}
                secondaryLanguage={secondaryLang}
                isBookmarked={bookmarkedKeys.has(readerState.selectedVerse.key)}
                currentHighlight={
                  highlightMap.get(readerState.selectedVerse.key) ?? HighlightColor.NONE
                }
                existingNote={notesMap.get(readerState.selectedVerse.key)?.noteText}
                onToggleBookmark={() =>
                  readerState.selectedVerse && toggleBookmark(readerState.selectedVerse)
                }
                onSelectHighlight={(c) =>
                  readerState.selectedVerse && setHighlight(readerState.selectedVerse, c)
                }
                onOpenNoteDialog={() => setNoteEditingVerse(readerState.selectedVerse)}
                onListenFromHere={() => {
                  if (readerState.selectedVerse) {
                    startAudioFromVerse(readerState.selectedVerse.verse);
                    selectVerse(null);
                  }
                }}
                onDismiss={() => selectVerse(null)}
              />
            ) : audioState.isActiveSession ? (
              <AudioPlayerBar
                audioState={audioState}
                onTogglePlayPause={toggleAudioPlayPause}
                onPrevVerse={audioPrevVerse}
                onNextVerse={audioNextVerse}
                onCycleLanguage={setAudioLanguage}
                onCycleSpeed={setAudioSpeed}
                onClose={stopAudio}
                onJumpToPlaying={() => {
                  void openChapter(
                    audioState.bookId,
                    audioState.chapter,
                    audioState.currentVerse
                  );
                }}
              />
            ) : (
              <div className="flex shrink-0 justify-center px-3 pb-1.5 pt-0 sm:px-6">
                <LiquidGlassSurface
                  tokens={tokens}
                  elevated
                  className="flex max-w-full items-center gap-1.5 rounded-full px-2 py-0.5"
                >
                  <button
                    type="button"
                    onClick={() => flipRef.current?.flipBackward()}
                    disabled={!readerState.prevChapterSnapshot}
                    aria-label={strings.prevChapterDesc}
                    className="flex h-[26px] w-[32px] shrink-0 touch-manipulation items-center justify-center rounded-full transition-all hover:bg-black/5 active:scale-95 disabled:opacity-35"
                    style={{ color: tokens.textPrimary }}
                  >
                    <ChevronLeft className="h-4 w-4" />
                  </button>
                  <span
                    className="max-w-[230px] truncate px-0.5 text-[10.5px] font-medium leading-tight sm:max-w-none"
                    style={{ color: tokens.textSecondary }}
                  >
                    {strings.readerScrollSwipeHint(readerState.verses.length)}
                  </span>
                  <button
                    type="button"
                    onClick={() => flipRef.current?.flipForward()}
                    disabled={!readerState.nextChapterSnapshot}
                    aria-label={strings.nextChapterDesc}
                    className="flex h-[26px] w-[32px] shrink-0 touch-manipulation items-center justify-center rounded-full transition-all hover:bg-black/5 active:scale-95 disabled:opacity-35"
                    style={{ color: tokens.textPrimary }}
                  >
                    <ChevronRight className="h-4 w-4" />
                  </button>
                </LiquidGlassSurface>
              </div>
            )}
          </>
        )}
      </div>

      {noteEditingVerse && book && (
        <NoteEditorDialog
          open={true}
          referenceTitle={`${
            sysLang === SystemLanguage.NEPALI
              ? book.nameNe
              : getBookNameForLanguage(book, primaryLang)
          } ${formatNumber(noteEditingVerse.chapter, sysLang)}:${formatNumber(
            noteEditingVerse.verse,
            sysLang
          )}`}
          initialNote={existingNoteText}
          onSave={(txt) => {
            saveNote(noteEditingVerse, txt);
            setNoteEditingVerse(null);
          }}
          onDelete={
            existingNoteText.trim()
              ? () => {
                  deleteNote(noteEditingVerse.key);
                  setNoteEditingVerse(null);
                }
              : undefined
          }
          onDismiss={() => setNoteEditingVerse(null)}
        />
      )}
    </div>
  );
};
