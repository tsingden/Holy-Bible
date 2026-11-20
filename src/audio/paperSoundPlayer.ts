import {
  AUDIBLE_PAGE_SOUND_STYLES,
  PAGE_SOUND_AUDIO_FILES,
  PageTurnSoundStyle,
} from '../types/bible';

class PaperSoundPlayer {
  private ctx: AudioContext | null = null;
  private bufferCache = new Map<string, AudioBuffer>();
  private loadingPromises = new Map<string, Promise<AudioBuffer | null>>();
  private lastRandomStyle: PageTurnSoundStyle | null = null;

  private getAudioContext(): AudioContext | null {
    if (typeof window === 'undefined') return null;
    if (!this.ctx) {
      const AudioCtx =
        window.AudioContext ||
        (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
      if (!AudioCtx) return null;
      this.ctx = new AudioCtx();
    }
    if (this.ctx.state === 'suspended') {
      void this.ctx.resume().catch(() => {});
    }
    return this.ctx;
  }

  private async loadBuffer(url: string): Promise<AudioBuffer | null> {
    const cached = this.bufferCache.get(url);
    if (cached) return cached;

    const inFlight = this.loadingPromises.get(url);
    if (inFlight) return inFlight;

    const promise = (async () => {
      try {
        const ctx = this.getAudioContext();
        if (!ctx) return null;
        const res = await fetch(url);
        if (!res.ok) return null;
        const arrayBuf = await res.arrayBuffer();
        const decoded = await ctx.decodeAudioData(arrayBuf);
        this.bufferCache.set(url, decoded);
        return decoded;
      } catch {
        return null;
      } finally {
        this.loadingPromises.delete(url);
      }
    })();

    this.loadingPromises.set(url, promise);
    return promise;
  }

  private resolveConcreteStyle(style: PageTurnSoundStyle): Exclude<
    PageTurnSoundStyle,
    PageTurnSoundStyle.RANDOM | PageTurnSoundStyle.SILENT
  > | null {
    if (!style || style === PageTurnSoundStyle.SILENT) return null;
    if (style === PageTurnSoundStyle.RANDOM) {
      const candidates =
        AUDIBLE_PAGE_SOUND_STYLES.length > 1 && this.lastRandomStyle
          ? AUDIBLE_PAGE_SOUND_STYLES.filter((s) => s !== this.lastRandomStyle)
          : AUDIBLE_PAGE_SOUND_STYLES;
      const chosen = candidates[Math.floor(Math.random() * candidates.length)];
      this.lastRandomStyle = chosen;
      return chosen as Exclude<
        PageTurnSoundStyle,
        PageTurnSoundStyle.RANDOM | PageTurnSoundStyle.SILENT
      >;
    }
    return style as Exclude<
      PageTurnSoundStyle,
      PageTurnSoundStyle.RANDOM | PageTurnSoundStyle.SILENT
    >;
  }

  private async triggerPlayback(url: string, gainLevel: number, rate: number): Promise<void> {
    const ctx = this.getAudioContext();
    if (!ctx) return;
    const buffer = await this.loadBuffer(url);
    if (!buffer) return;

    const source = ctx.createBufferSource();
    source.buffer = buffer;
    source.playbackRate.value = rate;

    const gain = ctx.createGain();
    gain.gain.value = Math.max(0, Math.min(1, gainLevel));

    source.connect(gain);
    gain.connect(ctx.destination);
    source.start(0);
  }

  playForward(enabled: boolean, style: PageTurnSoundStyle, volume = 0.72): void {
    if (!enabled || style === PageTurnSoundStyle.SILENT) return;
    const concrete = this.resolveConcreteStyle(style);
    if (!concrete) return;
    const url = PAGE_SOUND_AUDIO_FILES[concrete].fwd;
    const jitterVol = Math.max(0, Math.min(1, volume * (0.93 + Math.random() * 0.09)));
    const rate = 0.97 + Math.random() * 0.06;
    void this.triggerPlayback(url, jitterVol, rate);
  }

  playBackward(enabled: boolean, style: PageTurnSoundStyle, volume = 0.72): void {
    if (!enabled || style === PageTurnSoundStyle.SILENT) return;
    const concrete = this.resolveConcreteStyle(style);
    if (!concrete) return;
    const url = PAGE_SOUND_AUDIO_FILES[concrete].bwd;
    const jitterVol = Math.max(0, Math.min(1, volume * (0.93 + Math.random() * 0.09)));
    const rate = 0.96 + Math.random() * 0.06;
    void this.triggerPlayback(url, jitterVol, rate);
  }

  preview(style: PageTurnSoundStyle, volume = 0.72, forward = true): void {
    if (style === PageTurnSoundStyle.SILENT) return;
    const concrete = this.resolveConcreteStyle(style);
    if (!concrete) return;
    const pair = PAGE_SOUND_AUDIO_FILES[concrete];
    const url = forward ? pair.fwd : pair.bwd;
    const previewVol = Math.max(0.28, Math.min(1, volume));
    void this.triggerPlayback(url, previewVol, 1.0);
  }
}

export const paperSoundPlayer = new PaperSoundPlayer();
