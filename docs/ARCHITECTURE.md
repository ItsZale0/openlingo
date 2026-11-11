# Architecture

This document describes how OpenLingo is organized and which technical choices guide the project. It is intended for contributors and anyone evaluating the codebase.

## Goals and constraints

- **Offline first**: the core learning experience does not depend on a network connection.
- **No default profile**: data belongs to a profile explicitly created by the user.
- **Local data**: no mandatory account and no proprietary backend.
- **Optional AI**: AI features use a user-provided key and are not required to study.
- **Lightweight stack**: Kotlin, Compose, DataStore and JSON; no Room, Hilt or KSP, with manual dependency wiring.
- **Data compatibility**: the application/package identifier and persistence formats remain stable across versions.

## Modules

```
:core         learning engine and content (Kotlin/JVM, no Android dependency)
:app          Android application (Compose, Material 3)
:desktop      desktop application (Compose Desktop, Kotlin)
desktop-rust/ desktop application rebuilt from scratch in Rust (eframe/egui, Cargo project)
```

`:core` contains vocabulary, grammar, exercises, dialogues, listening, adaptive engines, backup and merge logic. It does not depend on Android and can be tested with the JVM alone. `:app` and `:desktop` (Kotlin/Compose) are user interfaces on top of the same engine. `desktop-rust/` is an independent Rust rebuild that targets compatibility with existing desktop profile data and incrementally reimplements features (currently: core screens, profile/data I/O with preservation of unknown fields, SRS-compatible review persistence, vocabulary search/add/archive, grammar mastery inspection, and progress stats) rather than depending on `:core`.

Dependencies:

```
app          ──▶ core
desktop      ──▶ core
desktop-rust     (standalone; reads/writes compatible desktop profile data)
```

## Profiles and data

Each profile is described by `ProfiloConfig` and contains:

- name, starting level and color theme;
- personal context (`contestoUtente`): study, work, interests, goals, used to personalize AI-generated content;
- learning state: words, reviews, mistakes, grammar, listening, production, generated lessons, badges, streaks, study plan and sessions.

Profiles are stored as JSON. On Android the document lives in a DataStore (`profili_data`); on desktop (Compose and Rust rebuild) in `profili.json` and `preferenze.json` under `~/.tedesco-desktop/`. New profiles receive a `profilo_<timestamp>` identifier; historical profiles keep their identifier to avoid data loss. The Rust desktop app is designed to read/write compatible profile/preference data (including SRS review state/logs/streak fields it manages) while preserving unmodeled/unknown fields on round-trip to avoid dropping data owned by the Kotlin/Compose app.

No profile is created automatically: the welcome screen guides creation of the first profile, and migration of existing data happens only when necessary.

## Learning engine

### Levels and content

Levels follow the Common European Framework of Reference: A0, A1, A2, B1, B2. Vocabulary is cumulative and verified by dedicated tests:

| Level | Cumulative words | Reference target |
|---|---:|---:|
| A1 | 670 | 650 |
| A2 | 1,378 | 1,300 |
| B1 | 2,503 | 2,400 |
| B2 | 3,108 | 3,000 |

Content includes words with articles, plurals and example sentences, grammar units, dialogues, listening exercises, writing/production texts and immersion passages.

### Exercises

Offline exercises cover six types: meaning, German word, article, sentence completion, writing and listening. AI-generated variants must have a single correct answer and are filtered to avoid ambiguity.

### Spaced repetition

Vocabulary uses an **SM-2** variant: each answer updates the interval, ease factor and next review date. Grammar and mistakes have dedicated, simpler review queues consistent with the same model. The daily session and study plan select what to present based on due items and competencies.

### Adaptive engines

`AdaptiveTestEngine` and `AdaptiveSessionEngine` estimate the user's level and compose sessions calibrated to weaker areas, avoiding repetition of the same exercises. Competencies (vocabulary, grammar, listening, production) are calculated from real review data, not simulated values.

## AI layer

AI features are isolated in `:app` because they require network access and user configuration:

- `TutorService`: chat and explanations, with Groq or Gemini providers;
- `GeneratoreEsercizi`: exercises and checks with a single-correct-answer constraint;
- `GeneratoreLezioni`: lessons and materials based on the active profile context.

Prompts include the active profile's personal context. API keys are stored in local DataStore and are never included in the repository or in cloud backup files. Without a key, these classes are not used and the app remains fully functional offline.

## Updates

The `updates/version.json` manifest on the `master` branch declares:

- `versionCode` and `versionName`;
- `apkUrl` and changelog;
- `apkSha256`, the SHA-256 hash of the published APK.

The app compares the local version with the manifest, downloads the APK to cache and accepts it only if:

1. size and header are compatible with an APK;
2. the SHA-256 matches the declared hash;
3. the signature matches the installed app.

Point 3 is guaranteed by using the same signing key for all releases; changing it would prevent in-place updates.

## Backup and export

- **Full export** (`EsportaDati`): a single JSON document with all profiles, preferences, theme and notifications. The format is versioned and validated before import.
- **GitHub backup**: optional, using a repository and token chosen by the user. Merging between backup and local data is handled by `BackupMerge`, which compares timestamps and keeps the most recent version of each item.

The export does not contain the GitHub token, but it does contain AI profile API keys: treat it as sensitive data.

## Testing

The unit test suite covers:

- learning engine, levels, vocabulary and exercise generation;
- profiles, migration, isolation and no automatic profile creation;
- spaced repetition, adaptive engines and competency calculation;
- backup, merge, export and format validation;
- manifest parsing, APK verification and remote APIs.

Tests run with `:app:testDebugUnitTest` and `:core:test`. The project does not require instrumented tests: Android-specific parts are designed to be testable on the JVM.

## Known limitations

- Public APKs may be signed with a development/debug-oriented setup depending on release configuration; store distribution requires an appropriate release key.
- AI features depend on the availability and limits of the chosen provider.
- Dynamic testing (DAST) of network surfaces is not automated; the security report documents the current scope and residual risks.
- The Rust desktop app (`desktop-rust/`) does not yet have full feature parity with `:core`/Compose desktop (e.g. complete grammar syllabus/exercise flows, feed/cloud sync & backup, AI tutor/generation, listening/pronunciation/TTS, comprehension/production). It currently provides a compatible, tested foundation (forward-compatible data I/O, profile management, SRS review persistence, vocabulary search/add/archive, grammar mastery/progress views, core screens) and is being expanded incrementally.
