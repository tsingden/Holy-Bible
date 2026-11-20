import React, { useCallback, useEffect, useImperativeHandle, useRef, useState } from 'react';
import {
  BibleBook,
  BibleLanguage,
  BibleVerse,
  ChapterSnapshot,
  HighlightColor,
  ReadingMode,
  ThemePreset,
} from '../types/bible';
import { useBibleApp } from '../context/BibleAppContext';
import { COLORS, argbToCss, cssWithAlpha } from '../theme/themeTokens';
import { BiblePageSheet } from './BiblePageSheet';

export interface FlipBookHandle {
  flipForward: () => void;
  flipBackward: () => void;
}

interface FlipBookPagerProps {
  book: BibleBook;
  chapter: number;
  verses: BibleVerse[];
  prevSnapshot: ChapterSnapshot | null;
  nextSnapshot: ChapterSnapshot | null;
  readingMode: ReadingMode;
  primaryLanguage: BibleLanguage;
  secondaryLanguage: BibleLanguage;
  downloadedBookKeys: Set<string>;
  fontSizeSp: number;
  selectedVerseKey: string | null;
  activeAudioVerseKey: string | null;
  targetScrollVerse: number | null;
  bookmarkedKeys: Set<string>;
  highlightMap: Map<string, HighlightColor>;
  noteKeys: Set<string>;
  onRequestPreviousChapter: () => void;
  onRequestNextChapter: () => void;
  onPlayForwardSound: () => void;
  onPlayBackwardSound: () => void;
  onVerseClick: (verse: BibleVerse) => void;
  onTopVisibleVerseChanged: (verseNum: number) => void;
  onDownloadLanguageBook: (lang: BibleLanguage, bookId: number) => void;
  onOpenDownloadsManager: () => void;
}

export const FlipBookPager = React.forwardRef<FlipBookHandle, FlipBookPagerProps>(
  (props, ref) => {
    const {
      book,
      chapter,
      verses,
      prevSnapshot,
      nextSnapshot,
      readingMode,
      primaryLanguage,
      secondaryLanguage,
      downloadedBookKeys,
      fontSizeSp,
      selectedVerseKey,
      activeAudioVerseKey,
      targetScrollVerse,
      bookmarkedKeys,
      highlightMap,
      noteKeys,
      onRequestPreviousChapter,
      onRequestNextChapter,
      onPlayForwardSound,
      onPlayBackwardSound,
      onVerseClick,
      onTopVisibleVerseChanged,
      onDownloadLanguageBook,
      onOpenDownloadsManager,
    } = props;

    const { palette, tokens } = useBibleApp();

    const [flipProgress, setFlipProgress] = useState(0); // -1..0 (fwd), 0..1 (bwd)
    const [dragTouchYFraction, setDragTouchYFraction] = useState(0.5);
    const isAnimatingRef = useRef(false);
    const animRafRef = useRef<number | null>(null);
    const stageRef = useRef<HTMLDivElement | null>(null);
    const dragStateRef = useRef<{
      active: boolean;
      startX: number;
      startY: number;
      horizontalLocked: boolean;
    }>({ active: false, startX: 0, startY: 0, horizontalLocked: false });

    const canGoForward = nextSnapshot !== null;
    const canGoBackward = prevSnapshot !== null;

    useEffect(() => {
      if (animRafRef.current) cancelAnimationFrame(animRafRef.current);
      isAnimatingRef.current = false;
      setFlipProgress(0);
    }, [book.id, chapter]);

    const animateProgressTo = useCallback(
      (from: number, to: number, durationMs: number, onComplete?: () => void) => {
        if (animRafRef.current) cancelAnimationFrame(animRafRef.current);
        const startTime = performance.now();
        const step = (now: number) => {
          const elapsed = now - startTime;
          const t = Math.min(1, elapsed / durationMs);
          // FastOutSlowIn cubic ease
          const eased = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
          setFlipProgress(from + (to - from) * eased);
          if (t < 1) {
            animRafRef.current = requestAnimationFrame(step);
          } else {
            onComplete?.();
          }
        };
        animRafRef.current = requestAnimationFrame(step);
      },
      []
    );

    const commitForwardTurn = useCallback(
      (fromProgress = 0) => {
        if (isAnimatingRef.current || !canGoForward) return;
        isAnimatingRef.current = true;
        onPlayForwardSound();
        animateProgressTo(fromProgress, -1, 360, () => {
          onRequestNextChapter();
          setFlipProgress(0);
          isAnimatingRef.current = false;
        });
      },
      [canGoForward, onPlayForwardSound, animateProgressTo, onRequestNextChapter]
    );

    const commitBackwardTurn = useCallback(
      (fromProgress = 0) => {
        if (isAnimatingRef.current || !canGoBackward) return;
        isAnimatingRef.current = true;
        onPlayBackwardSound();
        animateProgressTo(fromProgress, 1, 360, () => {
          onRequestPreviousChapter();
          setFlipProgress(0);
          isAnimatingRef.current = false;
        });
      },
      [canGoBackward, onPlayBackwardSound, animateProgressTo, onRequestPreviousChapter]
    );

    useImperativeHandle(
      ref,
      () => ({
        flipForward: () => commitForwardTurn(0),
        flipBackward: () => commitBackwardTurn(0),
      }),
      [commitForwardTurn, commitBackwardTurn]
    );

    const handlePointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
      if (isAnimatingRef.current || e.button !== 0) return;
      const rect = stageRef.current?.getBoundingClientRect();
      if (rect && rect.height > 0) {
        const yf = Math.max(0.1, Math.min(0.9, (e.clientY - rect.top) / rect.height));
        setDragTouchYFraction(yf);
      }
      dragStateRef.current = {
        active: true,
        startX: e.clientX,
        startY: e.clientY,
        horizontalLocked: false,
      };
    };

    const handlePointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
      const ds = dragStateRef.current;
      if (!ds.active || isAnimatingRef.current) return;
      const dx = e.clientX - ds.startX;
      const dy = e.clientY - ds.startY;

      if (!ds.horizontalLocked) {
        if (Math.abs(dy) > 14 && Math.abs(dy) > Math.abs(dx)) {
          ds.active = false;
          return;
        }
        if (Math.abs(dx) > 12 && Math.abs(dx) > Math.abs(dy)) {
          ds.horizontalLocked = true;
        } else {
          return;
        }
      }

      const width = stageRef.current?.clientWidth || 1;
      let raw = Math.max(-1, Math.min(1, dx / (width * 0.72)));
      if (raw < 0 && !canGoForward) raw *= 0.15;
      if (raw > 0 && !canGoBackward) raw *= 0.15;
      setFlipProgress(raw);
    };

    const handlePointerUp = () => {
      const ds = dragStateRef.current;
      if (!ds.active) return;
      ds.active = false;
      if (!ds.horizontalLocked || isAnimatingRef.current) {
        if (flipProgress !== 0) {
          animateProgressTo(flipProgress, 0, 180);
        }
        return;
      }

      if (flipProgress < -0.22 && canGoForward) {
        commitForwardTurn(flipProgress);
      } else if (flipProgress > 0.22 && canGoBackward) {
        commitBackwardTurn(flipProgress);
      } else {
        animateProgressTo(flipProgress, 0, 200);
      }
    };

    const isClassicLeatherFrame = tokens.config.preset === ThemePreset.CLASSIC_BIBLE;
    const isIdle = Math.abs(flipProgress) < 0.002;
    const isSpread = readingMode === ReadingMode.BILINGUAL_SPREAD;
    const isForward = flipProgress < 0;
    const activeProg = Math.min(1, Math.max(0, Math.abs(flipProgress)));

    const sheetSharedProps = {
      readingMode,
      primaryLanguage,
      secondaryLanguage,
      downloadedBookKeys,
      fontSizeSp,
      selectedVerseKey,
      activeAudioVerseKey,
      bookmarkedKeys,
      highlightMap,
      noteKeys,
      onDownloadLanguageBook,
      onOpenDownloadsManager,
    };

    return (
      <div
        className="relative flex h-full w-full flex-1 flex-col p-1.5 sm:p-2.5"
        style={{ backgroundColor: palette.deskBackground }}
      >
        {/* Outer Leather or Liquid Glass Book Frame */}
        <div
          className={`relative flex h-full w-full flex-1 overflow-hidden p-1 ${
            isClassicLeatherFrame ? 'rounded-xl' : 'rounded-2xl'
          }`}
          style={{
            backgroundColor: isClassicLeatherFrame
              ? argbToCss(COLORS.BurgundyDeep, 1)
              : cssWithAlpha(tokens.glassSurface, 0.45),
            border: isClassicLeatherFrame ? undefined : `1px solid ${tokens.borderTop}`,
            boxShadow: '0 10px 28px rgba(0,0,0,0.22)',
          }}
        >
          {/* Decorative Stacked Paper Side Edges */}
          <div
            aria-hidden="true"
            className="pointer-events-none absolute inset-y-1 left-0.5 w-1.5 opacity-80"
            style={{
              backgroundColor: palette.paperEdge,
              backgroundImage: `repeating-linear-gradient(90deg, transparent, transparent 1.5px, ${cssWithAlpha(
                palette.spineShadow,
                palette.isDark ? 0.45 : 0.16
              )} 2px)`,
            }}
          />
          <div
            aria-hidden="true"
            className="pointer-events-none absolute inset-y-1 right-0.5 w-1.5 opacity-80"
            style={{
              backgroundColor: palette.paperEdge,
              backgroundImage: `repeating-linear-gradient(90deg, transparent, transparent 1.5px, ${cssWithAlpha(
                palette.spineShadow,
                palette.isDark ? 0.45 : 0.16
              )} 2px)`,
            }}
          />

          {/* Interactive 3D Page Stage */}
          <div
            ref={stageRef}
            onPointerDown={handlePointerDown}
            onPointerMove={handlePointerMove}
            onPointerUp={handlePointerUp}
            onPointerCancel={handlePointerUp}
            className="flip-stage relative mx-1 my-0.5 flex-1 overflow-hidden rounded"
            style={{ touchAction: 'pan-y' }}
          >
            {isIdle ? (
              <BiblePageSheet
                {...sheetSharedProps}
                book={book}
                chapter={chapter}
                verses={verses}
                targetScrollVerse={targetScrollVerse}
                onVerseClick={onVerseClick}
                onTopVisibleVerseChanged={onTopVisibleVerseChanged}
              />
            ) : (
              <>
                {/* Revealed Target Chapter Underneath */}
                <div className="absolute inset-0">
                  <BiblePageSheet
                    {...sheetSharedProps}
                    book={(isForward ? nextSnapshot?.book : prevSnapshot?.book) ?? book}
                    chapter={(isForward ? nextSnapshot?.chapter : prevSnapshot?.chapter) ?? chapter}
                    verses={(isForward ? nextSnapshot?.verses : prevSnapshot?.verses) ?? []}
                    targetScrollVerse={null}
                    onVerseClick={() => {}}
                    isStaticPreview
                  />
                </div>

                {isSpread ? (
                  <>
                    {/* Anchored Half of Current Spread */}
                    <div
                      className="absolute inset-0"
                      style={{
                        clipPath: isForward
                          ? 'inset(0 50% 0 0)'
                          : 'inset(0 0 0 50%)',
                      }}
                    >
                      <BiblePageSheet
                        {...sheetSharedProps}
                        book={book}
                        chapter={chapter}
                        verses={verses}
                        targetScrollVerse={null}
                        onVerseClick={() => {}}
                        isStaticPreview
                      />
                    </div>

                    {/* Rotating Half-Leaf 0 -> -180deg (fwd) or 0 -> 180deg (bwd) */}
                    <div
                      className="flip-sheet-3d absolute inset-0"
                      style={{
                        transformOrigin: '50% 50%',
                        transform: `rotateY(${
                          isForward ? -180 * activeProg : 180 * activeProg
                        }deg)`,
                        clipPath: isForward
                          ? activeProg >= 0.5
                            ? 'inset(0 50% 0 0)'
                            : 'inset(0 0 0 50%)'
                          : activeProg < 0.5
                          ? 'inset(0 50% 0 0)'
                          : 'inset(0 0 0 50%)',
                      }}
                    >
                      {activeProg < 0.5 ? (
                        <BiblePageSheet
                          {...sheetSharedProps}
                          book={book}
                          chapter={chapter}
                          verses={verses}
                          targetScrollVerse={null}
                          onVerseClick={() => {}}
                          isStaticPreview
                        />
                      ) : (
                        <div className="h-full w-full" style={{ transform: 'rotateY(180deg)' }}>
                          <BiblePageSheet
                            {...sheetSharedProps}
                            book={(isForward ? nextSnapshot?.book : prevSnapshot?.book) ?? book}
                            chapter={
                              (isForward ? nextSnapshot?.chapter : prevSnapshot?.chapter) ?? chapter
                            }
                            verses={(isForward ? nextSnapshot?.verses : prevSnapshot?.verses) ?? []}
                            targetScrollVerse={null}
                            onVerseClick={() => {}}
                            isStaticPreview
                          />
                        </div>
                      )}
                    </div>
                  </>
                ) : (
                  /* Single-page & Parallel horizontal curl */
                  <div
                    className="flip-sheet-3d absolute inset-0"
                    style={{
                      transformOrigin: isForward ? '0% 50%' : '100% 50%',
                      transform: `rotateY(${
                        (isForward ? -12 : 12) * Math.sin(Math.PI * activeProg)
                      }deg)`,
                      clipPath: isForward
                        ? `inset(0 ${(activeProg * 100).toFixed(2)}% 0 0)`
                        : `inset(0 0 0 ${(activeProg * 100).toFixed(2)}%)`,
                    }}
                  >
                    <BiblePageSheet
                      {...sheetSharedProps}
                      book={book}
                      chapter={chapter}
                      verses={verses}
                      targetScrollVerse={null}
                      onVerseClick={() => {}}
                      isStaticPreview
                    />
                  </div>
                )}

                {/* Dynamic Curl Fold Shadow & Highlight Flap */}
                <PageCurlShadowSvg
                  progress={activeProg}
                  isForward={isForward}
                  isSpread={isSpread}
                  touchYFraction={dragTouchYFraction}
                  paperColor={palette.paperBackground}
                  paperEdgeColor={palette.paperEdge}
                  shadowColor={palette.spineShadow}
                />
              </>
            )}
          </div>
        </div>
      </div>
    );
  }
);

FlipBookPager.displayName = 'FlipBookPager';

const PageCurlShadowSvg: React.FC<{
  progress: number;
  isForward: boolean;
  isSpread: boolean;
  touchYFraction: number;
  paperColor: string;
  paperEdgeColor: string;
  shadowColor: string;
}> = ({
  progress,
  isForward,
  isSpread,
  touchYFraction,
  paperColor,
  paperEdgeColor,
  shadowColor,
}) => {
  const arcIntensity = Math.max(0, Math.min(1, Math.sin(Math.PI * progress)));
  const foldFrac = isSpread
    ? isForward
      ? 0.5 + 0.5 * Math.cos(Math.PI * progress)
      : 0.5 - 0.5 * Math.cos(Math.PI * progress)
    : isForward
    ? 1 - progress
    : progress;

  const foldPct = foldFrac * 100;
  const curlPct = Math.max(1.2, 12 * arcIntensity);
  const shadowPct = Math.max(2, 16 * arcIntensity);

  return (
    <div className="pointer-events-none absolute inset-0 z-20 overflow-hidden">
      {/* Fold Shadow Cast onto Underlying Page */}
      <div
        className="absolute inset-y-0"
        style={{
          left: isForward ? `${foldPct}%` : `${Math.max(0, foldPct - shadowPct)}%`,
          width: `${shadowPct}%`,
          background: isForward
            ? `linear-gradient(90deg, ${cssWithAlpha(shadowColor, 0.34 * arcIntensity)}, ${cssWithAlpha(
                shadowColor,
                0.1 * arcIntensity
              )}, transparent)`
            : `linear-gradient(90deg, transparent, ${cssWithAlpha(
                shadowColor,
                0.1 * arcIntensity
              )}, ${cssWithAlpha(shadowColor, 0.34 * arcIntensity)})`,
        }}
      />

      {/* Curved Paper Highlight Flap */}
      <div
        className="absolute inset-y-0"
        style={{
          left: isForward ? `${Math.max(0, foldPct - curlPct)}%` : `${foldPct}%`,
          width: `${curlPct}%`,
          borderRadius: isForward ? '40% 0 0 40%' : '0 40% 40% 0',
          transformOrigin: `50% ${touchYFraction * 100}%`,
          background: isForward
            ? `linear-gradient(90deg, ${cssWithAlpha(
                shadowColor,
                0.16 * arcIntensity
              )}, ${paperColor}, ${paperEdgeColor}, ${cssWithAlpha(
                shadowColor,
                0.2 * arcIntensity
              )})`
            : `linear-gradient(90deg, ${cssWithAlpha(
                shadowColor,
                0.2 * arcIntensity
              )}, ${paperEdgeColor}, ${paperColor}, ${cssWithAlpha(
                shadowColor,
                0.16 * arcIntensity
              )})`,
        }}
      />

      {/* Extra Center Spine Shadow During Spread Turn */}
      {isSpread && (
        <div
          className="absolute inset-y-0 left-1/2 w-8 -translate-x-1/2"
          style={{
            background: `linear-gradient(90deg, transparent, ${cssWithAlpha(
              shadowColor,
              0.22 * arcIntensity
            )}, transparent)`,
          }}
        />
      )}
    </div>
  );
};
