# OpenLingo Desktop (Rust)

A from-scratch Rust rebuild of the OpenLingo desktop application.

## Features
- Native GUI built with `eframe`/`egui`
- Reads/writes compatible profile data from `~/.tedesco-desktop/` (`profili.json`, `preferenze.json`)
- Screens: Home, Review, Vocabulary, Progress, Settings
- Profile selection/creation, reload/save
- **SRS-compatible review answers:** "Knew it / Didn't know" persists review cards (intervals/repetitions/ease/lapses/due), review log, and study streak/record to profile data
- Stats/progress: words, reviewed, due, streak, grammar units tracked, review log entries

## Build
```bash
cargo build --release
```

Binary: `target/release/openlingo-desktop.exe` (on Windows)

## Test
```bash
cargo test
```

## Notes
- This is a from-scratch Rust rebuild with growing feature coverage (data compatibility, SRS persistence, core screens). Full parity with `:core`/Compose desktop (complete grammar syllabus/exercise flows, feed/cloud sync, backup, AI tutor/generation, listening/pronunciation, etc.) is incremental work in progress.
- Data directory: `%USERPROFILE%\.tedesco-desktop\`
