# OpenLingo

[![Latest release](https://img.shields.io/github/v/release/ItsZale0/openlingo?label=release)](https://github.com/ItsZale0/openlingo/releases/latest)
[![Build APK](https://github.com/ItsZale0/openlingo/actions/workflows/build.yml/badge.svg)](https://github.com/ItsZale0/openlingo/actions/workflows/build.yml)
[![License: GPL-3.0-or-later](https://img.shields.io/badge/license-GPL--3.0--or--later-blue)](LICENSE)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/ItsZale0/openlingo/badge)](https://securityscorecards.dev/viewer/?uri=github.com/ItsZale0/openlingo)

**OpenLingo** is a free and open-source language learning app that aims to feel
as approachable and engaging as Duolingo, while being grounded in real didactics
and tested learning methods (spaced repetition, active recall, comprehensible
input, structured grammar progression, and deliberate practice).

Currently focused on German (A0–B2) for Android and desktop, OpenLingo blends
vocabulary, grammar, listening, speaking, reading, and writing into one calm,
focused learning space — with separate profiles for everyone who uses it.

OpenLingo is offline-first and privacy-respecting: no account required, no ads,
no trackers, and no data shared with external services unless you explicitly
enable optional features. Built for learners starting from Italian or English.

## Table of contents

- [Features](#features)
- [Platforms and requirements](#platforms-and-requirements)
- [Installation](#installation)
- [First run](#first-run)
- [Artificial intelligence (optional)](#artificial-intelligence-optional)
- [Data, backup and privacy](#data-backup-and-privacy)
- [Security](#security)
- [Development](#development)
- [Repository structure](#repository-structure)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)
- [Development note: a vibecoded project](#development-note-a-vibecoded-project)

## Features

| Area | Contents |
|---|---|
| **Vocabulary** | Cumulative A1–B2 lexicon: 3,108 words (A1 670, A2 1,378, B1 2,503, B2 3,108) with articles, plurals and example sentences |
| **Review** | SM-2 spaced repetition on vocabulary, grammar and mistakes, with daily session and study plan |
| **Exercises** | Six offline types (meaning, German word, article, completion, writing, listening) plus optional AI generation |
| **Grammar** | Level-based path with theory units and dedicated exercises |
| **Listening and pronunciation** | Dictation, dialogues, podcasts, text-to-speech and speech recognition |
| **Production** | Guided writing with optional AI correction and oral production exercises |
| **AI tutor** | Chat, explanations, exercises and lessons adapted to the user's personal context |
| **Profiles** | Fully separate data per profile, with personal context (study, work, interests) |
| **Backup** | Full export/import and optional GitHub backup |
| **Desktop** | Compose Desktop app (Kotlin, `:core`) and a from-scratch Rust/eframe rebuild (`desktop-rust/`) with compatible profile data, SRS persistence, vocabulary management, grammar mastery/progress views, and growing feature coverage |

The interface uses Material 3, with selectable colour themes (teal, navy, indigo,
green, rose, amber) on mobile and desktop. The UI is bilingual: Settings →
Language switches the whole interface between Italian and English at runtime,
and the choice is stored on the device.

## Platforms and requirements

| Platform | Requirements |
|---|---|
| Android | Android 8.0 or later (API 26+) |
| Desktop (Compose/Kotlin) | Windows, macOS or Linux with JVM 17+ |
| Desktop (Rust rebuild) | Rust toolchain (Cargo/rustc); builds natively (Windows/Linux/macOS where supported by dependencies) |

The Android app is distributed as a signed APK through GitHub releases. The
Windows desktop package (Compose Desktop) is available as a portable archive and a single
executable; for Linux and macOS you can build from source. The Rust desktop rebuild (`desktop-rust/`) is built from source with Cargo.

## Installation

1. Open the [releases](https://github.com/ItsZale0/openlingo/releases/latest) page.
2. Download the asset for your platform:
   - **Android:** `OpenLingo-v<version>.apk` (or `Deutsch-v<version>.apk` in older releases).
   - **Windows desktop (Compose):** `OpenLingo-desktop-v<version>-windows.zip` / `OpenLingo.exe` if provided, or legacy `Deutsch-desktop-v<version>-windows.zip` / `Deutsch.exe` in older releases.
   - **Rust desktop rebuild:** not currently distributed as a release asset; build from source (`desktop-rust/`, see [Development](#development)).
3. On Android you may need to allow installation from unknown sources for the
   browser or file manager you used.
4. Install/open the downloaded file. The app checks for updates and verifies the
   SHA-256 of the downloaded APK before installing updates.

## First run

There is no default profile. On first launch you choose the starting level and
create a personal profile; from then on each profile keeps progress, reviews,
conversations and lessons fully separate.

To add more people, create new profiles from settings. If you come from a
previous version, your existing profile is preserved and the application id stays
unchanged.

## Artificial intelligence (optional but raccomended)

The teaching core works entirely offline. AI features activate only when the user
enters their own API key, stored exclusively on the device:

- **Groq** for chat, exercise generation and corrections;
- **Gemini** as an alternative;
- plus OpenAI, Anthropic, OpenRouter and Kilo via the multi-provider setup.

Without a key the app stays usable: vocabulary, offline exercises, review,
grammar, listening and production keep working. The profile's personal context is
used to adapt generated content and is sent only to the chosen AI provider.

## Data, backup and privacy

- Data stays on the device: no mandatory account and no trackers.
- **Full export**: a single JSON file with all profiles, reviews, tutor data,
  lessons and preferences. Guide: [docs/EXPORT-DATI.md](docs/EXPORT-DATI.md).
- **GitHub backup**: optional, with a personal token of limited scope that the
  user can revoke at any time.
- The export file contains the AI profile keys: store it as sensitive data. It
  does not contain the GitHub token.

## Security

- SHA-256 verification of the APK before automatic update.
- Android system backup disabled, with explicit extraction rules.
- No secret, token or signing key included in the repository.
- Static analysis (SAST) and report: [SECURITY-AUDIT.md](SECURITY-AUDIT.md).
- Reports: [SECURITY.md](SECURITY.md).
- OpenSSF Scorecard active on every push to `master`.

## Development

The project uses Kotlin 2.0, Gradle 8.11.1, JDK 17, Jetpack Compose and Material 3.
It does not use Room, Hilt or KSP: persistence is based on DataStore and JSON
serialization, with manual dependency injection.

```bash
# Android build
./gradlew :app:assembleDebug

# Desktop (Compose/Kotlin)
./gradlew :desktop:run
# or compile
./gradlew :desktop:compileKotlin

# Desktop (Rust rebuild)
cd desktop-rust
cargo test
cargo build --release
# binary: desktop-rust/target/release/openlingo-desktop(.exe)

# Tests (Kotlin/Android/core)
./gradlew :app:testDebugUnitTest :core:test

# Everything together (Kotlin/Android/Desktop Compose)
./gradlew :core:jar :app:assembleDebug :desktop:compileKotlin
```

For the Kotlin/Android/Compose build you need `JAVA_HOME` (JDK 17) and `ANDROID_HOME` (Android SDK
with platform 35 and build-tools 35.0.0). The project can also be built offline
once dependencies are cached. For the Rust desktop rebuild you need a Rust toolchain (Cargo/rustc).

The Kotlin suite covers the teaching engine, profiles, data migration, backup, updates and
remote APIs; the suite contains over 570 unit tests. The Rust desktop app includes its own unit tests (`cargo test`) for compatible data loading/preservation, SRS persistence, vocabulary management, and grammar mastery parsing.

## Repository structure

```
core/         teaching engine and content, no Android dependencies
app/          Android application (Compose, Material 3)
desktop/      desktop application (Compose Desktop, Kotlin)
desktop-rust/ Rust desktop rebuild (eframe/egui, from scratch; compatible data, SRS, vocab/grammar/progress views)
docs/         user and technical documentation
updates/      update manifest (version and APK SHA-256)
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md): modules, data flow, teaching engine.
- [FAQ](docs/FAQ.md): daily use, updates, backup.
- [Data export](docs/EXPORT-DATI.md): step-by-step guide.
- [Translations](docs/TRANSLATIONS.md): how to contribute UI and content translations.
- [Release process](docs/RELEASE.md): versioning, tags, assets and checksums.
- [Contributing](CONTRIBUTING.md) and [Code of conduct](CODE_OF_CONDUCT.md).
- [Security](SECURITY.md) and [audit report](SECURITY-AUDIT.md).

## Roadmap

The app currently teaches German to speakers of **Italian or English**; the
teaching core is language-agnostic and the UI is fully bilingual (Italian /
English, switchable at runtime from Settings).

Planned directions:

- **More interface languages**: extend the bilingual layer (today Italian and
  English) to Spanish, French, German and other languages, reusing the same
  runtime translation mechanism (`Traduttore`).
- **More source languages for the course**: adapt the German course to
  learners whose mother tongue is not Italian or English, with localized
  explanations and glossaries.
- **Content growth**: expand the lexicon beyond the current B2 target (3,108
  words) and add more grammar units and listening material.
- **OpenSSF Criticality Score**: keep improving supply-chain and security
  signals (currently ~0.22, target >= 0.4 through real adoption).

Contributions on any of these points are welcome (see Contributing).

## Contributing

Every contribution is welcome: corrections to teaching content, translations,
UI improvements, tests and bug reports. Before opening a pull request, read
[CONTRIBUTING.md](CONTRIBUTING.md).

To contribute interface or content translations, or to add a new language, see
[docs/TRANSLATIONS.md](docs/TRANSLATIONS.md) and the reusable workflow in
`.opencode/skills/app-translation/SKILL.md`.

Vulnerability reports must follow [SECURITY.md](SECURITY.md), not public issues.

## License

Distributed under the **GNU General Public License v3.0 or later**
(GPL-3.0-or-later). See [LICENSE](LICENSE).

Teaching content is original or public domain; any external sources are cited in
the code or documentation.

## Development note: a vibecoded project

OpenLingo is a **vibecoded** project: most of the code, tests and documentation
were written in collaboration with AI agents, under human direction, review and
verification.

In practice:
- product decisions, content structure and final review are human;
- every change is compiled, tested and checked before a release;
- releases are verified with SHA-256 and the CI pipeline runs build, tests and
  OpenSSF Scorecard analysis;
- AI accelerates development but does not replace responsibility: bugs, limits
  and content errors are still possible.

If you find something that does not work or content to correct, open an issue:
it is the most useful way to improve the project.
