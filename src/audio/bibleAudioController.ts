import {
  AudioLanguage,
  AudioPlaybackState,
  BibleBook,
  BibleVerse,
  SystemLanguage,
} from '../types/bible';
import { getAppStrings } from '../localization/appStrings';
import { bibleDb } from '../data/bibleDb';

type StateListener = (state: AudioPlaybackState) => void;

class BibleAudioController {
  private listeners = new Set<StateListener>();
  private state: AudioPlaybackState = {
    isPlaying: false,
    isActiveSession: false,
    bookId: 1,
    bookNameEn: 'Genesis',
    bookNameNe: 'उत्पत्ति',
    chapter: 1,
    currentVerse: 1,
    totalVerses: 0,
    audioLanguage: AudioLanguage.BILINGUAL,
    speechRate: 1.0,
    statusNote: null,
    isEnReady: true,
    isNeReady: false,
    isKoReady: false,
  };

  private systemLanguage: SystemLanguage = SystemLanguage.ENGLISH;
  private preferOncvInBilingual = false;
  private currentBook: BibleBook | null = null;
  private currentVerses: BibleVerse[] = [];
  private currentIndex = 0;
  private bilingualPhase: 0 | 1 = 0;
  private sessionSeq = 0; // Guards against stale onend/onerror callbacks after cancel()
  private voices: SpeechSynthesisVoice[] = [];

  constructor() {
    this.detectVoices();
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      window.speechSynthesis.addEventListener('voiceschanged', () => {
        this.detectVoices();
      });
    }
  }

  subscribe(listener: StateListener): () => void {
    this.listeners.add(listener);
    listener(this.state);
    return () => {
      this.listeners.delete(listener);
    };
  }

  getState(): AudioPlaybackState {
    return this.state;
  }

  private emit(): void {
    const snap = { ...this.state };
    for (const l of this.listeners) l(snap);
  }

  private patchState(partial: Partial<AudioPlaybackState>): void {
    this.state = { ...this.state, ...partial };
    this.emit();
  }

  setSystemLanguage(sysLang: SystemLanguage): void {
    this.systemLanguage = sysLang;
    if (this.state.isActiveSession) {
      this.patchState({
        statusNote: this.buildVoiceNotice(
          this.state.audioLanguage,
          this.state.isNeReady,
          this.state.isKoReady
        ),
      });
    }
  }

  detectVoices(): void {
    if (typeof window === 'undefined' || !('speechSynthesis' in window)) {
      this.patchState({ isEnReady: false, isNeReady: false, isKoReady: false });
      return;
    }
    const list = window.speechSynthesis.getVoices();
    this.voices = list;

    if (list.length === 0) {
      // Many browsers populate voices lazily; assume EN basic availability until voices load
      this.patchState({ isEnReady: true, isNeReady: false, isKoReady: false });
      return;
    }

    const isEnReady = list.some((v) => v.lang.toLowerCase().startsWith('en'));
    const isNeReady = list.some((v) => v.lang.toLowerCase().startsWith('ne'));
    const isKoReady = list.some((v) => v.lang.toLowerCase().startsWith('ko'));

    this.patchState({
      isEnReady,
      isNeReady,
      isKoReady,
      statusNote: this.state.isActiveSession
        ? this.buildVoiceNotice(this.state.audioLanguage, isNeReady, isKoReady)
        : this.state.statusNote,
    });
  }

  private buildVoiceNotice(
    language: AudioLanguage,
    isNeReady: boolean,
    isKoReady: boolean
  ): string | null {
    const strings = getAppStrings(this.systemLanguage);
    if (
      !isNeReady &&
      (language === AudioLanguage.NEPALI ||
        language === AudioLanguage.ONCV ||
        language === AudioLanguage.BILINGUAL)
    ) {
      return strings.ttsNepaliMissingNotice;
    }
    if (!isKoReady && language === AudioLanguage.KOREAN) {
      return strings.ttsKoreanMissingNotice;
    }
    return null;
  }

  private cancelSpeech(): void {
    this.sessionSeq++;
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      try {
        window.speechSynthesis.cancel();
      } catch {
        // Ignore
      }
    }
  }

  startChapterAudio(
    book: BibleBook,
    verses: BibleVerse[],
    startVerse: number,
    language: AudioLanguage,
    speed: number,
    preferOncvInBilingual = false
  ): void {
    if (verses.length === 0) return;
    this.cancelSpeech();
    this.detectVoices();

    this.currentBook = book;
    this.currentVerses = verses;
    const idx = Math.max(
      0,
      verses.findIndex((v) => v.verse === startVerse)
    );
    this.currentIndex = idx;
    this.preferOncvInBilingual = preferOncvInBilingual;
    this.bilingualPhase =
      language === AudioLanguage.NEPALI || language === AudioLanguage.ONCV ? 1 : 0;

    this.patchState({
      isPlaying: true,
      isActiveSession: true,
      bookId: book.id,
      bookNameEn: book.nameEn,
      bookNameNe: book.nameNe,
      chapter: verses[idx].chapter,
      currentVerse: verses[idx].verse,
      totalVerses: verses.length,
      audioLanguage: language,
      speechRate: speed,
      statusNote: this.buildVoiceNotice(language, this.state.isNeReady, this.state.isKoReady),
    });

    this.speakCurrent();
  }

  togglePlayPause(): void {
    if (!this.state.isActiveSession) return;
    if (this.state.isPlaying) {
      this.cancelSpeech();
      this.patchState({ isPlaying: false });
    } else {
      this.patchState({ isPlaying: true });
      this.speakCurrent();
    }
  }

  nextVerse(): void {
    if (!this.state.isActiveSession || this.currentVerses.length === 0) return;
    this.cancelSpeech();
    if (this.currentIndex + 1 < this.currentVerses.length) {
      this.currentIndex++;
      this.bilingualPhase = 0;
      const v = this.currentVerses[this.currentIndex];
      this.patchState({
        chapter: v.chapter,
        currentVerse: v.verse,
        totalVerses: this.currentVerses.length,
      });
      if (this.state.isPlaying) this.speakCurrent();
    } else {
      void this.advanceToNextChapter();
    }
  }

  previousVerse(): void {
    if (!this.state.isActiveSession || this.currentVerses.length === 0) return;
    this.cancelSpeech();
    if (this.currentIndex > 0) {
      this.currentIndex--;
      this.bilingualPhase = 0;
      const v = this.currentVerses[this.currentIndex];
      this.patchState({
        chapter: v.chapter,
        currentVerse: v.verse,
        totalVerses: this.currentVerses.length,
      });
      if (this.state.isPlaying) this.speakCurrent();
    }
  }

  setLanguage(language: AudioLanguage): void {
    this.patchState({
      audioLanguage: language,
      statusNote: this.buildVoiceNotice(language, this.state.isNeReady, this.state.isKoReady),
    });
    if (this.state.isPlaying) {
      this.cancelSpeech();
      this.bilingualPhase =
        language === AudioLanguage.NEPALI || language === AudioLanguage.ONCV ? 1 : 0;
      this.speakCurrent();
    }
  }

  setSpeed(speed: number): void {
    this.patchState({ speechRate: speed });
    if (this.state.isPlaying) {
      this.cancelSpeech();
      this.speakCurrent();
    }
  }

  stopAudio(): void {
    this.cancelSpeech();
    this.patchState({
      isPlaying: false,
      isActiveSession: false,
      statusNote: null,
    });
  }

  private findBestVoice(prefixes: string[]): SpeechSynthesisVoice | null {
    for (const prefix of prefixes) {
      const match = this.voices.find((v) => v.lang.toLowerCase().startsWith(prefix.toLowerCase()));
      if (match) return match;
    }
    return null;
  }

  private speakCurrent(): void {
    if (!this.state.isPlaying) return;
    if (typeof window === 'undefined' || !('speechSynthesis' in window)) return;

    const verse = this.currentVerses[this.currentIndex];
    if (!verse) return;

    const langMode = this.state.audioLanguage;
    let textToSpeak = '';
    let targetLangTag = 'en-US';
    let voice: SpeechSynthesisVoice | null = null;

    if (langMode === AudioLanguage.KOREAN) {
      textToSpeak = verse.textKo;
      voice = this.findBestVoice(['ko-kr', 'ko']);
      targetLangTag = voice?.lang ?? (this.state.isKoReady ? 'ko-KR' : 'en-US');
    } else if (langMode === AudioLanguage.NEPALI) {
      textToSpeak = verse.textNe.trim() ? verse.textNe : verse.textOncv;
      voice = this.findBestVoice(['ne-np', 'ne', 'hi-in', 'hi']);
      targetLangTag = voice?.lang ?? 'ne-NP';
    } else if (langMode === AudioLanguage.ONCV) {
      textToSpeak = verse.textOncv.trim() ? verse.textOncv : verse.textNe;
      voice = this.findBestVoice(['ne-np', 'ne', 'hi-in', 'hi']);
      targetLangTag = voice?.lang ?? 'ne-NP';
    } else if (langMode === AudioLanguage.ENGLISH) {
      textToSpeak = verse.textEn;
      voice = this.findBestVoice(['en-us', 'en-gb', 'en']);
      targetLangTag = voice?.lang ?? 'en-US';
    } else {
      // BILINGUAL
      const speakNepali = this.bilingualPhase === 1;
      const nepaliText = this.preferOncvInBilingual
        ? verse.textOncv.trim() || verse.textNe
        : verse.textNe.trim() || verse.textOncv;
      textToSpeak = speakNepali ? nepaliText : verse.textEn;
      if (speakNepali) {
        voice = this.findBestVoice(['ne-np', 'ne', 'hi-in', 'hi']);
        targetLangTag = voice?.lang ?? 'ne-NP';
      } else {
        voice = this.findBestVoice(['en-us', 'en-gb', 'en']);
        targetLangTag = voice?.lang ?? 'en-US';
      }
    }

    const activeSeq = this.sessionSeq;

    if (!textToSpeak.trim()) {
      setTimeout(() => {
        if (activeSeq === this.sessionSeq) this.handleUtteranceFinished(activeSeq);
      }, 60);
      return;
    }

    const utterance = new SpeechSynthesisUtterance(textToSpeak);
    utterance.lang = targetLangTag;
    if (voice) utterance.voice = voice;
    utterance.rate = Math.max(0.5, Math.min(2.0, this.state.speechRate));

    utterance.onend = () => {
      if (activeSeq === this.sessionSeq) {
        this.handleUtteranceFinished(activeSeq);
      }
    };

    utterance.onerror = () => {
      if (activeSeq === this.sessionSeq) {
        this.handleUtteranceFinished(activeSeq);
      }
    };

    try {
      window.speechSynthesis.speak(utterance);
    } catch {
      this.stopAudio();
    }
  }

  private handleUtteranceFinished(seq: number): void {
    if (seq !== this.sessionSeq || !this.state.isPlaying) return;
    if (this.state.audioLanguage === AudioLanguage.BILINGUAL && this.bilingualPhase === 0) {
      this.bilingualPhase = 1;
      this.speakCurrent();
      return;
    }

    this.bilingualPhase = 0;
    if (this.currentIndex + 1 < this.currentVerses.length) {
      this.currentIndex++;
      const v = this.currentVerses[this.currentIndex];
      this.patchState({
        chapter: v.chapter,
        currentVerse: v.verse,
        totalVerses: this.currentVerses.length,
      });
      this.speakCurrent();
    } else {
      void this.advanceToNextChapter();
    }
  }

  private async advanceToNextChapter(): Promise<void> {
    const book = this.currentBook;
    if (!book) return;
    const currentChap = this.currentVerses[0]?.chapter ?? this.state.chapter;

    let nextBook = book;
    let nextChap = currentChap + 1;
    if (nextChap > book.chapterCount) {
      const nextId = book.id < 66 ? book.id + 1 : 1;
      const loaded = await bibleDb.getBook(nextId);
      if (!loaded) return;
      nextBook = loaded;
      nextChap = 1;
    }

    const nextVerses = await bibleDb.getChapterVerses(nextBook.id, nextChap);
    if (nextVerses.length > 0) {
      this.currentBook = nextBook;
      this.currentVerses = nextVerses;
      this.currentIndex = 0;
      this.bilingualPhase = 0;
      this.patchState({
        bookId: nextBook.id,
        bookNameEn: nextBook.nameEn,
        bookNameNe: nextBook.nameNe,
        chapter: nextChap,
        currentVerse: nextVerses[0].verse,
        totalVerses: nextVerses.length,
      });
      if (this.state.isPlaying) {
        this.speakCurrent();
      }
    } else {
      this.stopAudio();
    }
  }
}

export const bibleAudioController = new BibleAudioController();
