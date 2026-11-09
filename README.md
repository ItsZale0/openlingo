# Deutsch

[![Latest release](https://img.shields.io/github/v/release/ItsZale0/tedesco-app?label=release)](https://github.com/ItsZale0/tedesco-app/releases/latest)
[![Build APK](https://github.com/ItsZale0/tedesco-app/actions/workflows/build.yml/badge.svg)](https://github.com/ItsZale0/tedesco-app/actions/workflows/build.yml)
[![License: GPL-3.0-or-later](https://img.shields.io/badge/license-GPL--3.0--or--later-blue)](LICENSE)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/ItsZale0/tedesco-app/badge)](https://securityscorecards.dev/viewer/?uri=github.com/ItsZale0/tedesco-app)

**Deutsch** is a German course (A0–B2) for Android and desktop, offline-first and
open source. It brings together vocabulary, grammar, listening, production,
spaced repetition and an optional AI tutor in a single app, with separate
profiles for each person who uses it.

Originally a personal project, it is meant for anyone who wants to study German
starting from Italian: no account, no ads, and no data sent to external services
except those explicitly chosen by the user.

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
| **Desktop** | The same teaching core in a Compose Desktop app |

The interface uses Material 3, with selectable colour themes (teal, navy, indigo,
green, rose, amber) on mobile and desktop. The UI is bilingual: Settings →
Language switches the whole interface between Italian and English at runtime,
and the choice is stored on the device.

## Platforms and requirements

| Platform | Requirements |
|---|---|
| Android | Android 8.0 or later (API 26+) |
| Desktop | Windows, macOS or Linux with JVM 17+ |

The Android app is distributed as a signed APK through GitHub releases. The
Windows desktop package is available as a portable archive and a single
executable; for Linux and macOS you can build from source.

## Installation

1. Open the [releases](https://github.com/ItsZale0/tedesco-app/releases/latest) page.
2. Download:
   - `Deutsch-v<version>.apk` for Android;
   - `Deutsch-desktop-v<version>-windows.zip` or `Deutsch.exe` for Windows.
3. On Android you may need to allow installation from unknown sources for the
   browser or file manager you used.
4. The app automatically checks for updates and verifies the SHA-256 of the
   downloaded APK before installing.

## First run

There is no default profile. On first launch you choose the starting level and
create a personal profile; from then on each profile keeps progress, reviews,
conversations and lessons fully separate.

To add more people, create new profiles from settings. If you come from a
previous version, your existing profile is preserved and the application id stays
unchanged.

## Artificial intelligence (optional)

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

# Desktop build
./gradlew :desktop:run

# Tests
./gradlew :app:testDebugUnitTest :core:test

# Everything together
./gradlew :core:jar :app:assembleDebug :desktop:compileKotlin
```

For compilation you need `JAVA_HOME` (JDK 17) and `ANDROID_HOME` (Android SDK
with platform 35 and build-tools 35.0.0). The project can also be built offline
once dependencies are cached.

Tests cover the teaching engine, profiles, data migration, backup, updates and
remote APIs; the suite contains over 570 unit tests.

## Repository structure

```
core/      teaching engine and content, no Android dependencies
app/       Android application (Compose, Material 3)
desktop/   desktop application (Compose Desktop)
docs/      user and technical documentation
updates/   update manifest (version and APK SHA-256)
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md): modules, data flow, teaching engine.
- [FAQ](docs/FAQ.md): daily use, updates, backup.
- [Data export](docs/EXPORT-DATI.md): step-by-step guide.
- [Release process](docs/RELEASE.md): versioning, tags, assets and checksums.
- [Contributing](CONTRIBUTING.md) and [Code of conduct](CODE_OF_CONDUCT.md).
- [Security](SECURITY.md) and [audit report](SECURITY-AUDIT.md).

## Contributing

Every contribution is welcome: corrections to teaching content, translations,
UI improvements, tests and bug reports. Before opening a pull request, read
[CONTRIBUTING.md](CONTRIBUTING.md).

Vulnerability reports must follow [SECURITY.md](SECURITY.md), not public issues.

## License

Distributed under the **GNU General Public License v3.0 or later**
(GPL-3.0-or-later). See [LICENSE](LICENSE).

Teaching content is original or public domain; any external sources are cited in
the code or documentation.

## Development note: a vibecoded project

Deutsch is a **vibecoded** project: most of the code, tests and documentation
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
