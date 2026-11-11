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
- Runtime EN/IT toggle implemented (persisted in `preferenze.json` `lingua`): `ui_lang`, language selector/save in Settings, localized key nav/home/settings labels; Review screen and core status/feedback messages localized (EN/IT); Grammar & lessons screen includes localized labels/progression actions (incremental).  
  `rg -n "ui_lang|prefs\.lingua" desktop-rust/src` shows usage in `src/main.rs` (including `tests::ui_translation_helper_respects_language`).
- Prior hardcoded `Ciao` greeting replaced by localized greeting in Rust UI (incremental EN/IT parity; verified by merged/tested changes).
- **Grammar/lesson/unit parity increment (automated+tested):** CEFR syllabus topics (A1/A2/B1) and lesson progression controls (advance/reset) persisted to `progresso.lezione_corrente`; **lesson-practice flow** (start guided session from grammar and mark lesson complete on session completion); **embedded grammar units** (expanded A0-B1 representative subset incl. alphabet/pronunciation, plural of nouns, possessives and negation, imperative, prepositions of place, prepositions of time, separable verbs (A1), modal verbs (a1-modali), dative, Präteritum, reflexive verbs, weil/dass, subordinate clauses, comparatives, adjective declension intro, dative prepositions, accusative prepositions, relative clauses intro, connectors, double connectors, reflexive verbs, separable verbs, Präteritum vs Perfekt, Präteritum, passive intro, genitive intro, two-way prepositions, relative clauses, Konjunktiv II intro, future and infinitive with zu intro, indirect speech intro, nominalization intro, participles intro, passive with modal verbs intro) with theory/examples and interactive practice flow (MCQ + typing/completion), **rotated/shuffled exercise order per session**, unit-list level filter (ALL/A0/A1/A2/B1), mastery/progression status and recommended-next selection, recording mastery to `padronanzaGrammatica` (percent, attempts/correct, completion, timestamps/next review) compatibly; unit mastery/due view retained. Verified by `tests::curriculum_lessons_and_progression_update`, `tests::embedded_grammar_units_practice_saves_mastery`. Not full `GrammaticaPercorso` content/unit banks, unlock/gating/rotation rules, or complete theory/exercise systems.
- **Exercise/session parity increment (automated+tested):** added **Guided session** and **Weak words review** exercise flows (guided prioritizes due + new words; weak-words targets lapses/low ease/recent incorrect answers from saved reviews/logs), mixed question types, SRS-updating answers. Verified by `tests::guided_session_builds_mixed_questions`, `tests::difficult_words_selects_lapses_and_errors` (`cargo test`: 19 passed total). Not all original exercise types/session flows or full lesson/content systems.
- **Limitation:** Rust UI localization is incremental (not all screens/prompts/content fully localized) and not full parity with Kotlin `Traduttore` coverage; Rust app does not execute AI personalization/provider flows.

## 2) Exercisable automated checks (verified)

### Rust (`desktop-rust/`) — latest verified run (this increment)
```text
cargo test
running 19 tests
...
test tests::guided_session_builds_mixed_questions ... ok
test tests::difficult_words_selects_lapses_and_errors ... ok
test tests::embedded_grammar_units_practice_saves_mastery ... ok
...
test result: ok. 19 passed; 0 failed; 0 ignored; 0 measured; 0 filtered out; finished in 0.06s
---TEST_EXIT:0---

cargo build --release
Finished `release` profile [optimized] target(s) in 8.42s
---BUILD_EXIT:0---

Binary: desktop-rust/target/release/openlingo-desktop.exe
Name                   Length LastWriteTime
openlingo-desktop.exe 6065664 11/11/2026 13:09:18
SHA256: 7C369E14756357AA8A9122554A75C7C6ABB516535C752D081B2DA5C6AD39B122

Runtime smoke (this PC): openlingo-desktop launched and Responding: True (PID 16672, 2026-11-11 13:09:27).
```

### Rust (`desktop-rust/`) — previous verified run
```text
cargo test
running 15 tests
...
test tests::german_choice_quiz_builds_options ... ok
test tests::study_plan_and_grammar_due_summary ... ok
test tests::ui_translation_helper_respects_language ... ok
...
test result: ok. 15 passed; 0 failed; 0 ignored; 0 measured; 0 filtered out; finished in 0.05s
---TEST:0---

cargo build --release
Finished `release` profile [optimized] target(s) in 7.79s
---BUILD:0---
openlingo-desktop.exe 5,837,312 bytes (2026-11-11 10:58:46)
SHA256: 28EF8F24A72581F8B0FDD52032A0071C15169044BBF4CA4AC40245E7E78615D7
```
Covers (among others): SRS/persistence, forward-compatible data round-trip preservation, vocab search/add/archive, exercise builders (article/meaning/german/typing/cloze), grammar mastery/progression parsing and due-state, study plan summary, backup bundle export/import, EN/IT translation helper behavior.

### Kotlin/Android (offline) — latest observed
```text
.\gradle-temp\gradle-8.11.1\bin\gradle.bat --offline :app:testDebugUnitTest :desktop:compileKotlin
BUILD SUCCESSFUL in 38s (31 actionable tasks: 6 executed, 25 up-to-date)
.\gradle-temp\gradle-8.11.1\bin\gradle.bat --offline :app:assembleDebug
BUILD SUCCESSFUL in 32s (40 actionable tasks: 6 executed, 34 up-to-date) — APK signature (v1+v2+v3) verified
app unit tests: 600 tests, 0 failures, 0 errors
```
AI exercise correctness (v1.51.17): ambiguous generated items are now rejected deterministically. `ControlloGrammatica` detects weak-noun number ambiguity (`Wir besuchen ___ Patienten` accepts both `euren` and `eure`) only when the case is determinable and the number is not forced; `GeneratoreEsercizi` asks the reviewer to judge every option and keeps an item only when exactly one option is correct and matches the key; the generation prompt warns about N-Deklination and possessive/subject coherence. Covered by `ControlloGrammaticaTest` (18 tests) and `GeneratoreEserciziTest` (12 tests), including the exact screenshot case.

Exercise feedback (v1.51.18): added per-exercise human feedback (✓/✗, "Is the exercise correct?") on AI-generated grammar exercises and grammar-unit exercises. Feedback is stored in `ProfiloStato.feedbackEsercizi` (backward-compatible default), merged conservatively in backups (`false`/wrong is not lost), excluded from generated/unit exercise flows, and used to avoid reintroducing reported-wrong content. UI added in `GrammaticaScreen` and `UnitaGrammaticaScreen`; EN label added in `Traduttore`.
Note: an earlier same-session run reported `585 tests completed, 1 failed, 1 skipped` (`PianoProgressoSessioniRegressionTest > feed con sessioni vuote aggiorna la lista sessioni`) while isolated and subsequent full reruns succeeded; the latest full run is 600/0.

## 3) Manual / runtime-blocked (cannot be exhaustively verified in this environment)

The following require interactive runtime execution and/or external credentials/devices and therefore **cannot be fully evidenced as "all flows passed"** here:

- **Runtime GUI smoke across all Android + desktop (Compose) + Rust flows/screens/themes:** requires Android emulator/device and interactive desktop GUI sessions; not automated/executed end-to-end in this CLI environment.
- **Exhaustive EN↔IT runtime audit across all flows:** verifying that in EN mode *all* non-German user-visible text (UI/exercises/content/metadata/hints/feedback/AI) is English across every screen/flow/theme in running apps requires manual walkthrough/runtime inspection.
- **Personalization runtime verification (AI-key + `contestoUtente`)**: requires valid provider API keys, configured contexts, and live AI responses to confirm adapted outputs across all relevant flows.
- **No-key personalization/fallback runtime verification**: requires exercising offline/no-key paths across all applicable flows to confirm generic goal-appropriate defaults and safe fallbacks at runtime.
- **Listening/pronunciation/TTS, comprehension/production, AI tutor/generation, feed/cloud sync (network/GitHub)** end-to-end runtime behavior: depend on device audio/runtime services, network/provider access, tokens, and interactive flows not fully automatable/evidenced here.
- **Full Rust feature parity with `:core`/Compose desktop:** large implementation scope remains (complete grammar syllabus/unit & exercise flows, all original exercise/content/lesson systems, feed/cloud sync & backup network flows, AI tutor/generation, TTS/listening, comprehension/production, etc.).

## 4) Publication status
- PRs merged to `master` during this continuation: **#30, #32, #33, #34, #35, #36** (subsequent increments should be published via PRs merged per repo rules/checks/review).
- Local temp/untracked artifacts and `desktop-rust/target/` are not committed.

## 5) Recommended manual verification checklist (for user/runtime)
1. Run Android app (EN/IT) and Compose desktop: smoke every screen/flow/theme; confirm EN mode text coverage and intentional German preservation.
2. Exercise AI-key flows with `contestoUtente` set (and no-key flows) across tutor/exercises/lessons/dialogues/production/listening where applicable; verify adapted vs generic fallback behavior.
3. Validate feed/cloud sync and GitHub backup with a test repo/token; validate listening/pronunciation/TTS and comprehension/production flows on device.
4. Compare Rust desktop behavior against Kotlin desktop for supported flows; treat remaining Rust gaps per section 3 as incremental work.
