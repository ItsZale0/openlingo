# OpenLingo Desktop (Rust)

A from-scratch Rust rebuild of the OpenLingo desktop application.

## Features
- Native GUI built with `eframe`/`egui`
- Reads/writes compatible profile data from `~/.tedesco-desktop/` (`profili.json`, `preferenze.json`)
- **Forward-compatible data handling:** preserves unmodeled/unknown profile and preference fields on load/save (to avoid dropping data managed by the Kotlin/Compose app)
- Screens: Home, Review, Exercises, Vocabulary, Grammar, Progress, Settings
- Profile selection/creation, reload/save
- **SRS-compatible review answers:** "Knew it / Didn't know" persists review cards (intervals/repetitions/ease/lapses/due), review log, and study streak/record to profile data
- **Practice/exercise modes:** article/gender quiz (der/die/das) using all active vocabulary words for selected level and below (deduped, shuffled), meaning multiple-choice quiz, typing quiz, cloze (from word examples), and mixed sessions; answers update SRS/review progress
- **Vocabulary management:** search/filter (by text and level), add custom words, archive/restore words
- **Grammar mastery view:** lists tracked grammar units from profile `padronanzaGrammatica` with parsed score/attempts/completion/last activity
- **Progress stats:** active/reviewed/mature words, due count, streak/record, grammar completion summary, words by level, review log entries

## Build
```bash
cargo build --release
```

Binary: `target/release/openlingo-desktop.exe` (on Windows)

## Test
```bash
cargo test
```

Current tests cover data compatibility/round-trip preservation, SRS behavior and persistence, vocabulary search/add/archive, grammar mastery parsing, article-quiz/typing exercise logic, and core store operations.

## Notes
- This is a from-scratch Rust rebuild with growing feature coverage (data compatibility, SRS persistence, vocabulary/exercise practice, grammar mastery inspection, core screens). Full parity with `:core`/Compose desktop (complete grammar syllabus/unit flows and all original exercise/content systems, feed/cloud sync & backup, AI tutor/generation, listening/pronunciation/TTS, comprehension/production, etc.) remains incremental work in progress.
- Data directory: `%USERPROFILE%\.tedesco-desktop\`
