# Holy Bible (React + TypeScript + Vite)

An offline-first multilingual Holy Bible web application featuring synchronized dual-page flip-book reading across English (KJV), Nepali (ULB & ONCV), and Korean (KRV).

## Features

- **3D Flip-Book Reader**: Side-by-side book spread, parallel, and single-language views with drag-to-flip and button animations.
- **100% Offline Scripture Packs**: Gzipped Bible datasets decompressed in-browser with `fflate` and persisted in IndexedDB, with granular per-language and per-book download management.
- **Audio Bible Narration**: Verse-by-verse Web Speech API playback supporting Bilingual (`EN + NE`), English, Nepali (`ULB` & `ONCV`), and Korean with adjustable speed and chapter auto-advance.
- **Page-Turning Sound Studio**: 10 bundled authentic paper-flip WAV samples plus Random and Silent modes with volume control.
- **Study Tools**: Verse bookmarks, 4 highlight colors, personal notes, full-text offline search, daily devotional verse, and reading history.
- **Liquid Glass & Theme Studio**: 11 theme presets, customizable glass opacity/blur, colors, button & nav styles, 7 reading paper backgrounds, and 5 bundled scripture typography options, with full English and Nepali UI localization.

## Development

```bash
npm install
npm run dev
```

Runs the Vite development server on port `3000`.

## Build & Lint

```bash
npm run build
npm run lint
```
