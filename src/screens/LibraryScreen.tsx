import React, { useState } from 'react';
import { Bookmark, Edit3, History, Palette, Trash2 } from 'lucide-react';
import {
  BibleVerse,
  HighlightColor,
  NoteEntity,
  SystemLanguage,
  getFontFamilyForBibleLanguage,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { formatDateTime, formatNumber, toDevanagari } from '../utils/formatters';
import { LiquidGlassSurface } from '../components/LiquidGlassShell';
import { NoteEditorDialog } from '../components/VerseActionBar';

const HIGHLIGHT_SWATCH_HEX: Record<HighlightColor, string> = {
  [HighlightColor.GOLD]: '#F6D56A',
  [HighlightColor.ROSE]: '#F2A7B5',
  [HighlightColor.SAGE]: '#9FD69B',
  [HighlightColor.SKY]: '#8AC4EB',
  [HighlightColor.NONE]: '#9CA3AF',
};

export const LibraryScreen: React.FC = () => {
  const {
    bookmarks,
    highlights,
    notes,
    history,
    preferences,
    tokens,
    palette,
    strings,
    openChapter,
    navigate,
    removeBookmark,
    setHighlight,
    saveNote,
    deleteNote,
    clearHistory,
  } = useBibleApp();

  const [selectedTab, setSelectedTab] = useState<0 | 1 | 2 | 3>(0);
  const [editingNote, setEditingNote] = useState<NoteEntity | null>(null);

  const sysLang = preferences.systemLanguage;
  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';
  const secondaryFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Playfair Display", Georgia, serif'
      : '"Noto Serif Devanagari", serif';

  const makePseudoVerse = (
    bookId: number,
    chapter: number,
    verse: number,
    key: string,
    textEn = '',
    textNe = ''
  ): BibleVerse => ({
    id: 0,
    bookId,
    chapter,
    verse,
    key,
    textEn,
    textNe,
    textOncv: '',
    textKo: '',
  });

  const tabs = [
    { id: 0 as const, label: strings.tabBookmarks(bookmarks.length) },
    { id: 1 as const, label: strings.tabHighlights(highlights.length) },
    { id: 2 as const, label: strings.tabNotes(notes.length) },
    { id: 3 as const, label: strings.tabHistory(history.length) },
  ];

  const openVerseInReader = (bookId: number, chapter: number, verse: number | null) => {
    void openChapter(bookId, chapter, verse);
    navigate('reader');
  };

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <div className="mx-auto flex h-full w-full max-w-[680px] flex-col">
        <h1
          className="px-4 py-3 text-[22px] font-bold"
          style={{ fontFamily: headerFont, color: tokens.textPrimary }}
        >
          {strings.studyLibraryTitle}
        </h1>

        {/* Scrollable Tabs */}
        <div
          className="no-scrollbar flex shrink-0 gap-1 overflow-x-auto border-b px-4"
          style={{ borderColor: tokens.borderTop }}
        >
          {tabs.map((tab) => {
            const active = selectedTab === tab.id;
            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setSelectedTab(tab.id)}
                className="shrink-0 border-b-2 px-3 py-2 text-xs font-bold transition-colors"
                style={{
                  borderColor: active ? tokens.accentColor : 'transparent',
                  color: active ? tokens.accentColor : tokens.textSecondary,
                }}
              >
                {tab.label}
              </button>
            );
          })}
        </div>

        {/* Tab Body */}
        <div className="flex-1 overflow-y-auto px-4 pb-28 pt-4">
          {selectedTab === 0 && (
            bookmarks.length === 0 ? (
              <EmptyLibraryState
                title={strings.noBookmarksTitle}
                subtitle={strings.noBookmarksSub}
              />
            ) : (
              <div className="space-y-2.5">
                {bookmarks.map((item) => {
                  const refTitle =
                    sysLang === SystemLanguage.NEPALI
                      ? `${item.bookNameNe} ${formatNumber(item.chapter, sysLang)}:${formatNumber(
                          item.verse,
                          sysLang
                        )} • ${item.bookNameEn}`
                      : `${item.bookNameEn} ${item.chapter}:${item.verse} • ${item.bookNameNe}`;
                  return (
                    <LiquidGlassSurface
                      key={item.verseKey}
                      tokens={tokens}
                      onClick={() => openVerseInReader(item.bookId, item.chapter, item.verse)}
                      className="rounded-2xl p-3.5 text-left"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <div className="flex min-w-0 items-center gap-1.5">
                          <Bookmark
                            className="h-[18px] w-[18px] shrink-0"
                            style={{ color: tokens.accentColor }}
                          />
                          <span
                            className="truncate text-[15px] font-bold"
                            style={{ fontFamily: headerFont, color: tokens.accentColor }}
                          >
                            {refTitle}
                          </span>
                        </div>
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            removeBookmark(item.verseKey);
                          }}
                          aria-label={strings.removeBookmarkDesc}
                          className="rounded-full p-1.5 hover:bg-black/5"
                          style={{ color: tokens.textSecondary }}
                        >
                          <Trash2 className="h-[18px] w-[18px]" />
                        </button>
                      </div>
                      {item.snippetEn && (
                        <p
                          className="mt-1.5 line-clamp-2 text-[15px]"
                          style={{
                            fontFamily: getFontFamilyForBibleLanguage(
                              preferences.primaryLanguage,
                              tokens.fontStyle
                            ),
                            color: tokens.textPrimary,
                          }}
                        >
                          {item.snippetEn}
                        </p>
                      )}
                      {item.snippetNe && (
                        <p
                          className="mt-1 line-clamp-2 text-sm"
                          style={{
                            fontFamily: '"Noto Serif Devanagari", serif',
                            color: tokens.textSecondary,
                          }}
                        >
                          {item.snippetNe}
                        </p>
                      )}
                    </LiquidGlassSurface>
                  );
                })}
              </div>
            )
          )}

          {selectedTab === 1 && (
            highlights.length === 0 ? (
              <EmptyLibraryState
                title={strings.noHighlightsTitle}
                subtitle={strings.noHighlightsSub}
              />
            ) : (
              <div className="space-y-2.5">
                {highlights.map((item) => {
                  const refTitle =
                    sysLang === SystemLanguage.NEPALI
                      ? `${item.bookNameNe} ${formatNumber(item.chapter, sysLang)}:${formatNumber(
                          item.verse,
                          sysLang
                        )}`
                      : `${item.bookNameEn} ${item.chapter}:${item.verse}`;
                  return (
                    <LiquidGlassSurface
                      key={item.verseKey}
                      tokens={tokens}
                      onClick={() => openVerseInReader(item.bookId, item.chapter, item.verse)}
                      className="rounded-2xl p-3.5 text-left"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <div className="flex min-w-0 items-center gap-2">
                          <span
                            className="h-3.5 w-3.5 shrink-0 rounded-full"
                            style={{ backgroundColor: HIGHLIGHT_SWATCH_HEX[item.colorKey] }}
                          />
                          <span
                            className="truncate text-[15px] font-bold"
                            style={{ fontFamily: headerFont, color: tokens.accentColor }}
                          >
                            {refTitle}
                          </span>
                        </div>
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            setHighlight(
                              makePseudoVerse(
                                item.bookId,
                                item.chapter,
                                item.verse,
                                item.verseKey,
                                item.snippetEn,
                                item.snippetNe
                              ),
                              HighlightColor.NONE
                            );
                          }}
                          aria-label={strings.clearHighlightDesc}
                          className="rounded-full p-1.5 hover:bg-black/5"
                          style={{ color: tokens.textSecondary }}
                        >
                          <Trash2 className="h-[18px] w-[18px]" />
                        </button>
                      </div>
                      {item.snippetEn && (
                        <p
                          className="mt-1.5 line-clamp-2 text-[15px]"
                          style={{
                            fontFamily: getFontFamilyForBibleLanguage(
                              preferences.primaryLanguage,
                              tokens.fontStyle
                            ),
                            color: tokens.textPrimary,
                          }}
                        >
                          {item.snippetEn}
                        </p>
                      )}
                      {item.snippetNe && (
                        <p
                          className="mt-1 line-clamp-2 text-sm"
                          style={{
                            fontFamily: '"Noto Serif Devanagari", serif',
                            color: tokens.textSecondary,
                          }}
                        >
                          {item.snippetNe}
                        </p>
                      )}
                    </LiquidGlassSurface>
                  );
                })}
              </div>
            )
          )}

          {selectedTab === 2 && (
            notes.length === 0 ? (
              <EmptyLibraryState title={strings.noNotesTitle} subtitle={strings.noNotesSub} />
            ) : (
              <div className="space-y-2.5">
                {notes.map((item) => {
                  const refTitle =
                    sysLang === SystemLanguage.NEPALI
                      ? `${item.bookNameNe} ${formatNumber(item.chapter, sysLang)}:${formatNumber(
                          item.verse,
                          sysLang
                        )} • ${item.bookNameEn}`
                      : `${item.bookNameEn} ${item.chapter}:${item.verse} • ${item.bookNameNe}`;
                  return (
                    <LiquidGlassSurface
                      key={item.verseKey}
                      tokens={tokens}
                      onClick={() => openVerseInReader(item.bookId, item.chapter, item.verse)}
                      className="rounded-2xl p-3.5 text-left"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span
                          className="min-w-0 flex-1 truncate text-[15px] font-bold"
                          style={{ fontFamily: headerFont, color: tokens.accentColor }}
                        >
                          {refTitle}
                        </span>
                        <div className="flex shrink-0 items-center">
                          <button
                            type="button"
                            onClick={(e) => {
                              e.stopPropagation();
                              setEditingNote(item);
                            }}
                            aria-label={strings.editNoteDesc}
                            className="rounded-full p-1.5 hover:bg-black/5"
                            style={{ color: tokens.textSecondary }}
                          >
                            <Edit3 className="h-[18px] w-[18px]" />
                          </button>
                          <button
                            type="button"
                            onClick={(e) => {
                              e.stopPropagation();
                              deleteNote(item.verseKey);
                            }}
                            aria-label={strings.deleteNoteDesc}
                            className="rounded-full p-1.5 hover:bg-black/5"
                            style={{ color: tokens.textSecondary }}
                          >
                            <Trash2 className="h-[18px] w-[18px]" />
                          </button>
                        </div>
                      </div>
                      <p className="mt-1.5 text-sm font-medium" style={{ color: tokens.textPrimary }}>
                        {item.noteText}
                      </p>
                      <p className="mt-1 text-xs" style={{ color: tokens.textSecondary }}>
                        {formatDateTime(item.updatedAt, sysLang)}
                      </p>
                    </LiquidGlassSurface>
                  );
                })}
              </div>
            )
          )}

          {selectedTab === 3 && (
            history.length === 0 ? (
              <EmptyLibraryState
                title={strings.noHistoryTitle}
                subtitle={strings.noHistorySub}
              />
            ) : (
              <div>
                <div className="mb-2 flex justify-end">
                  <button
                    type="button"
                    onClick={clearHistory}
                    className="rounded-lg px-2.5 py-1 text-xs font-semibold hover:bg-black/5"
                    style={{ color: tokens.accentColor }}
                  >
                    {strings.clearHistory}
                  </button>
                </div>
                <div className="space-y-2">
                  {history.map((item) => {
                    const primaryTitle =
                      sysLang === SystemLanguage.NEPALI
                        ? `${item.bookNameNe} ${toDevanagari(item.chapter)}`
                        : `${item.bookNameEn} ${item.chapter}`;
                    const secondaryTitle =
                      sysLang === SystemLanguage.NEPALI
                        ? `${item.bookNameEn} ${item.chapter}`
                        : `${item.bookNameNe} ${toDevanagari(item.chapter)}`;

                    return (
                      <LiquidGlassSurface
                        key={item.id}
                        tokens={tokens}
                        onClick={() => openVerseInReader(item.bookId, item.chapter, item.verse)}
                        className="flex items-center justify-between gap-3 rounded-[14px] p-3.5 text-left"
                      >
                        <div className="flex min-w-0 items-center gap-2.5">
                          <History
                            className="h-5 w-5 shrink-0"
                            style={{ color: palette.goldAccent }}
                          />
                          <div className="min-w-0">
                            <div
                              className="truncate text-[15.5px] font-bold"
                              style={{ fontFamily: headerFont, color: tokens.textPrimary }}
                            >
                              {primaryTitle}
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
                          className="shrink-0 text-[11px]"
                          style={{ color: tokens.textSecondary }}
                        >
                          {formatDateTime(item.visitedAt, sysLang)}
                        </span>
                      </LiquidGlassSurface>
                    );
                  })}
                </div>
              </div>
            )
          )}
        </div>
      </div>

      {editingNote && (
        <NoteEditorDialog
          open={true}
          referenceTitle={`${
            sysLang === SystemLanguage.NEPALI ? editingNote.bookNameNe : editingNote.bookNameEn
          } ${formatNumber(editingNote.chapter, sysLang)}:${formatNumber(
            editingNote.verse,
            sysLang
          )}`}
          initialNote={editingNote.noteText}
          onSave={(updated) => {
            saveNote(
              makePseudoVerse(
                editingNote.bookId,
                editingNote.chapter,
                editingNote.verse,
                editingNote.verseKey
              ),
              updated
            );
            setEditingNote(null);
          }}
          onDelete={() => {
            deleteNote(editingNote.verseKey);
            setEditingNote(null);
          }}
          onDismiss={() => setEditingNote(null)}
        />
      )}
    </div>
  );
};

const EmptyLibraryState: React.FC<{ title: string; subtitle: string }> = ({ title, subtitle }) => {
  const { palette, tokens, preferences } = useBibleApp();
  const sysLang = preferences.systemLanguage;
  return (
    <div className="flex h-full min-h-[260px] flex-col items-center justify-center px-6 text-center">
      <Palette className="h-9 w-9" style={{ color: palette.goldAccent }} />
      <h3
        className="mt-2.5 text-[18px] font-bold"
        style={{
          fontFamily:
            sysLang === SystemLanguage.NEPALI
              ? '"Noto Serif Devanagari", serif'
              : '"Playfair Display", Georgia, serif',
          color: tokens.textPrimary,
        }}
      >
        {title}
      </h3>
      <p className="mt-1.5 max-w-xs text-sm" style={{ color: tokens.textSecondary }}>
        {subtitle}
      </p>
    </div>
  );
};
