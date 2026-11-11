# OpenLingo Desktop (Rust)

A from-scratch Rust rebuild of the OpenLingo desktop application.

## Features
- Native GUI built with `eframe`/`egui`
- Reads/writes compatible profile data from `~/.tedesco-desktop/` (`profili.json`, `preferenze.json`)
- **Forward-compatible data handling:** preserves unmodeled/unknown profile and preference fields on load/save (to avoid dropping data managed by the Kotlin/Compose app)
- **Theming:** Compose-like color themes (teal, navy, indigo, forest, rose, amber) and light/dark/system modes, matching the Kotlin/Compose desktop palettes (persisted in `preferenze.json` `temaColore`/`temaModalita`)
- **UI language:** runtime English/Italian toggle (persisted in `preferenze.json` `lingua`) for key Rust desktop UI labels/screens (incremental; not full parity with Kotlin `Traduttore` coverage)
- Screens: Home, Review, Exercises, Vocabulary, Grammar, Progress, Settings
- Profile selection/creation, reload/save
- **SRS-compatible review answers:** "Knew it / Didn't know" persists review cards (intervals/repetitions/ease/lapses/due), review log, and study streak/record to profile data
- **Practice/exercise modes:** article/gender quiz (der/die/das) using all active vocabulary words for selected level and below (deduped, shuffled), meaning multiple-choice quiz, German-choice quiz, typing quiz, cloze (from word examples), mixed sessions, **guided session** (prioritizes due + new words, falls back to active pool), and **weak words review** (targets lapses/low-ease/recent errors from saved review data); answers update SRS/review progress
- **Vocabulary management:** search/filter (by text and level), add custom words, archive/restore words
- **Grammar & lessons:** CEFR syllabus topics by level (A1/A2/B1) with current lesson/topic and lesson progression controls (advance/mark complete, reset) persisted compatibly in profile progress; **"Practice lesson" flow** launches a guided session and allows marking lesson complete on session completion; **embedded grammar units** (expanded A0–B1 representative subset: articles, pronouns/sein, word order, accusative, sein/haben, modals, separable verbs, Perfekt intro, dative, weil/dass, comparatives, dative prepositions, two-way prepositions, passive intro, adjective declension intro, relative clauses intro, connectors, reflexive verbs, Präteritum vs Perfekt, genitive intro, Konjunktiv II intro) with theory/examples and interactive practice exercises (multiple-choice and typing/completion), **rotated/shuffled** per session — unit list supports level filter (ALL/A0/A1/A2/B1), shows mastery/progression status and recommended next unit; practice completion records mastery to `padronanzaGrammatica` (percent, attempts/correct, completion, timestamps/next review) compatibly; plus unit mastery/progression view from saved data (score/attempts/correct/reviews, completion/mastery/due state, next/last review timestamps)
- **Progress / study plan:** active/reviewed/mature words, due/new counts, streak/record, grammar tracked/completed/due, suggested next actions, lesson/level/goal progression, words by level, review log entries
- **Backup:** export/import compatible profile+preferences JSON bundle from Settings (path-based)

## Build
```bash
cargo build --release
```

Binary: `target/release/openlingo-desktop.exe` (on Windows)

## Test
```bash
cargo test
```

Current tests cover data compatibility/round-trip preservation, SRS behavior and persistence, vocabulary search/add/archive, grammar mastery/progression parsing, curriculum/lesson syllabus and progression persistence, embedded grammar unit practice/mastery recording, exercise builders (article/meaning/german/typing/cloze/guided session), study plan summaries, backup bundle round-trip, and core store operations.

## Notes
- This is a from-scratch Rust rebuild with growing feature coverage (data compatibility, SRS persistence, vocabulary/exercise practice, grammar mastery inspection, core screens). Full parity with `:core`/Compose desktop (complete grammar syllabus/unit flows and all original exercise/content systems, feed/cloud sync & backup, AI tutor/generation, listening/pronunciation/TTS, comprehension/production, etc.) remains incremental work in progress.
- Data directory: `%USERPROFILE%\.tedesco-desktop\`
