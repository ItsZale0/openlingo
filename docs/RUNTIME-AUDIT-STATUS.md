# Runtime / EN-IT / Personalization Audit Status (Verifiable Artifacts)

**Generated:** 2026-11-11 (automated/static audit pass from workspace `C:\Users\zampe\AppData\Local\Temp\opencode\tedesco-master`)  
**Scope:** Concrete, verifiable evidence collected in this environment for EN/IT i18n, personalization, and runtime-smoke readiness. This document explicitly separates **verified/static evidence**, **exercisable automated checks**, and **manual/runtime-blocked checks**.

## 1) Static evidence (command outputs)

### i18n/runtime translation infrastructure (Kotlin/Compose + app resources)
- `rg -l "Traduttore" core/src app/src desktop/src` → **9 files** reference `Traduttore` (runtime translation/i18n infrastructure present in core/app/desktop).
- `rg -l "Traduttore" desktop/src` → **2 files** in desktop module.
- Android resources: `app/src/main/res` includes **`values`** and **`values-en`** (plus drawable/mipmap/xml dirs) — `Get-ChildItem app/src/main/res -Directory` confirms locale/resource directories.
- Sample usage markers found (static): `contestoUtente` is used across core/app/desktop.  
  `rg -n "contestoUtente" core/src app/src desktop/src` (first matches) returned hits in:
  - `desktop/src/.../DesktopStore.kt` (config copy), `desktop/src/.../ui/Impostazioni.kt`
  - `core/src/.../data/AscoltoData.kt`
  - `app/src/.../ui/DialoghiScreen.kt`
  - `app/src/.../data/remote/AiService.kt`, `GeneratoreEsercizi.kt`, `TutorService.kt`, `data/ProfileManager.kt`

### Personalization / context / fallback markers (static)
- `rg -n "sanitario|contestoSanitarioRichiesto|senza chiave|no-key|fallback" core/src app/src desktop/src` (sample hits) shows healthcare-context/personalization strings and fallback-related logic/tests exist (e.g. `Traduttore` healthcare strings, `AdaptiveTestEngine` fallback rounds, tests asserting no inflated fallback without real tests, preset/profile tests for medical context). This confirms **code paths exist**; it does **not** prove runtime behavior across all flows.

### Rust desktop (`desktop-rust/`) i18n/status (static + tested)
- Runtime EN/IT toggle implemented (persisted in `preferenze.json` `lingua`): `ui_lang`, language selector/save in Settings, localized key nav/home/settings labels.  
  `rg -n "ui_lang|prefs\.lingua" desktop-rust/src` shows usage in `src/main.rs` (including `tests::ui_translation_helper_respects_language`).
- Prior hardcoded `Ciao` greeting replaced by localized greeting in Rust UI (incremental EN/IT parity; verified by merged/tested change).
- **Limitation:** Rust UI localization is incremental (key labels/screens) and not full parity with Kotlin `Traduttore` coverage; Rust app does not execute AI personalization/provider flows.

## 2) Exercisable automated checks (verified)

### Rust (`desktop-rust/`) — latest verified run
```text
cargo test
running 14 tests
...
test tests::ui_translation_helper_respects_language ... ok
test tests::german_choice_quiz_builds_options ... ok
test tests::export_import_bundle_roundtrip ... ok
...
test result: ok. 14 passed; 0 failed; 0 ignored; 0 measured; 0 filtered out
---TEST:0---

cargo build --release
Finished `release` profile [optimized] target(s)
---BUILD:0---
openlingo-desktop.exe 5,826,048 bytes (2026-11-11 10:54:36)
SHA256: 813AB4A7C9A33E7BC92CA566848E7CF118A924B48C56C25D2EE301296F4EF968
```
Covers (among others): SRS/persistence, forward-compatible data round-trip preservation, vocab search/add/archive, exercise builders (article/meaning/german/typing/cloze), backup bundle export/import, EN/IT translation helper behavior.

### Kotlin/Android (offline) — latest observed
```text
.\gradle-temp\gradle-8.11.1\bin\gradle.bat --offline :app:testDebugUnitTest :core:test
BUILD SUCCESSFUL in 41s (30 actionable tasks: 1 executed, 29 up-to-date)
```
Note: an earlier same-session full run reported `585 tests completed, 1 failed, 1 skipped` (`PianoProgressoSessioniRegressionTest > feed con sessioni vuote aggiorna la lista sessioni`) while an isolated run and a subsequent full rerun succeeded; no app/core Kotlin source was modified by Rust/doc/merge increments in this session.

## 3) Manual / runtime-blocked (cannot be exhaustively verified in this environment)

The following require interactive runtime execution and/or external credentials/devices and therefore **cannot be fully evidenced as "all flows passed"** here:

- **Runtime GUI smoke across all Android + desktop (Compose) + Rust flows/screens/themes:** requires Android emulator/device and interactive desktop GUI sessions; not automated/executed end-to-end in this CLI environment.
- **Exhaustive EN↔IT runtime audit across all flows:** verifying that in EN mode *all* non-German user-visible text (UI/exercises/content/metadata/hints/feedback/AI) is English across every screen/flow/theme in running apps requires manual walkthrough/runtime inspection.
- **Personalization runtime verification (AI-key + `contestoUtente`)**: requires valid provider API keys, configured contexts, and live AI responses to confirm adapted outputs across all relevant flows.
- **No-key personalization/fallback runtime verification**: requires exercising offline/no-key paths across all applicable flows to confirm generic goal-appropriate defaults and safe fallbacks at runtime.
- **Listening/pronunciation/TTS, comprehension/production, AI tutor/generation, feed/cloud sync (network/GitHub)** end-to-end runtime behavior: depend on device audio/runtime services, network/provider access, tokens, and interactive flows not fully automatable/evidenced here.
- **Full Rust feature parity with `:core`/Compose desktop:** large implementation scope remains (complete grammar syllabus/unit & exercise flows, all original exercise/content/lesson systems, feed/cloud sync & backup network flows, AI tutor/generation, TTS/listening, comprehension/production, etc.).

## 4) Publication status
- PRs merged to `master` during this continuation: **#30, #32, #33, #34, #35** (subsequent increments, including this audit artifact/code, should be published via PRs merged per repo rules/checks/review when created).
- Local temp/untracked artifacts and `desktop-rust/target/` are not committed.

## 5) Recommended manual verification checklist (for user/runtime)
1. Run Android app (EN/IT) and Compose desktop: smoke every screen/flow/theme; confirm EN mode text coverage and intentional German preservation.
2. Exercise AI-key flows with `contestoUtente` set (and no-key flows) across tutor/exercises/lessons/dialogues/production/listening where applicable; verify adapted vs generic fallback behavior.
3. Validate feed/cloud sync and GitHub backup with a test repo/token; validate listening/pronunciation/TTS and comprehension/production flows on device.
4. Compare Rust desktop behavior against Kotlin desktop for supported flows; treat remaining Rust gaps per section 3 as incremental work.
