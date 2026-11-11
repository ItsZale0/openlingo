# OpenLingo Desktop (Rust)

A from-scratch Rust rebuild of the OpenLingo desktop application.

## Features
- Native GUI built with `eframe`/`egui`
- Reads/writes compatible profile data from `~/.tedesco-desktop/` (`profili.json`, `preferenze.json`)
- Screens: Home, Review (due/new word browser), Vocabulary, Progress, Settings
- Profile selection/creation, reload/save, basic stats (words, reviewed, due, streak)

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
- This rebuild focuses on a working desktop shell and compatible data access. Advanced SRS scheduling, grammar syllabus flows, sync, and AI features from the original Kotlin/Compose desktop app and `:core` can be ported incrementally.
- Data directory: `%USERPROFILE%\.tedesco-desktop\`
