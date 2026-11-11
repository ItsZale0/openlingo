---
name: app-translation
description: Translate the entire OpenLingo/Tedesco app UI and content into a user-chosen language at runtime (except intentional German learning content), using the project's i18n system (Traduttore/t(), locale/settings) safely without freezing language or breaking German material. Use when the user asks to localize the app to a specific language (e.g. English, Spanish, French), fix missing translations in a selected language, or repeat localization for another language.
---

# App translation (choose target language)

Use this skill to make **all non-German, user-visible text** appear in a user-chosen target language when that language is selected, while preserving intentional German content (German words, sentences, exercises' target-language material, examples, audio/TTS source text, grammar terms shown as German where intended).

## 1) Establish target and scope
- Confirm the **target language** (code, name, and locale, e.g. `en`/English, `es`/Spanish) and the **source/default language** (this codebase's authored strings are largely Italian).
- Define "non-German user-visible text": navigation, titles, labels, buttons, dialogs, hints, instructions, feedback, explanations, questions/prompts/options/metadata shown to the user, error/empty states, settings, notifications, reports — **except** intentional German learning content.
- Decide behavior for bilingual/help text (e.g. show translation alongside German when pedagogically intended) and for content that must remain language-neutral.

## 2) Understand the i18n architecture (this repo)
- **Runtime translator:** `core/src/main/kotlin/com/alessandro/openlingo/i18n/Traduttore.kt`
  - `Traduttore.lingua` holds active language; `t(testo)` / `Traduttore.traduci(...)` returns translated text for the active non-default language, otherwise original.
  - Exact-match dictionary is chunked (`esattoChunkN`) and aggregated lazily into `esatto`; `schemi` provides regex/pattern replacements for parameterized/dynamic strings.
- **Language selection/persistence:** app `sync/LinguaSettings.kt` + `ui/MainActivity.kt`/`ImpostazioniScreen.kt`; desktop `DesktopStore` preferences (`lingua`) + `desktop/Main.kt`/settings UI. UI must recompute on language change.
- **Rule:** wrap user-visible literals at **render/call time** with `t(...)`. Never freeze translations in top-level vals, object init, enums, or static data structures that outlive a language switch.

## 3) Audit (exhaustive, do not guess)
- **UI:** `app/src/main/java/com/alessandro/openlingo/ui/**`, `desktop/src/main/kotlin/**/ui/**` (and desktop shell/screens). Find hardcoded user-facing strings in Compose/Text/labels/placeholders/contentDescription/snackbars/dialogs/tooltips.
- **Data/content:** `core/src/main/kotlin/com/alessandro/openlingo/data/**` — exercise generators, static datasets, lessons, quizzes, dialogs, scenarios, titles/descriptions/instructions/prompts/options/explanations/feedback/metadata.
- **Search tactics:** look for `Text("...")`, `label =`, `placeholder`, `contentDescription`, string fields like `titolo/domanda/spiegazione/opzioni/descrizione/istruzione/messaggio/testo/frase/nota`, and common source-language words. Cross-check that strings shown to users are either German-intent or passed through `t()`.
- Distinguish: **German learning content** (keep) vs **UI/explanation/translation text** (translate). When mixed, translate only the non-German parts or provide mapped translations that preserve embedded German.

## 4) Implement translations (runtime-safe)
- **Direct UI literals:** replace `"..."` with `t("...")` at use site (composable/render). Avoid `t("...${x}...")` dynamic keys; prefer stable keys + composition or regex `schemi` (e.g. `t("...") + value` patterns that match existing scheme forms).
- **Static data objects/vals:** do NOT call `t()` in static initializers if language can change. Prefer: translate at display sites (`t(item.titolo)` in UI — works if UI recomposes), or refactor to functions/getters/providers that compute per language, or store translatable keys. Ensure chosen approach updates on language switch.
- **Dynamically generated strings:** use exact keys and/or `schemi` regex entries in `Traduttore` for parameterized forms; keep placeholders consistent.
- **Add dictionary entries:** for the target language add exact mappings in an appropriate `esattoChunkN` (and wire it in the lazy aggregator if new) and any needed `schemi` patterns. Match source keys **exactly** (including punctuation/apostrophes/whitespace/Unicode variants present in code); handle mojibake/variant forms if they exist.
- **Multi-language extension (if adding a language beyond existing support):** extend `Traduttore`/settings to support the new language code end-to-end (storage, selection UI, `t()` dispatch per language, resources/locale where applicable). Do not hardcode only one non-default language if the task is to make it reusable for another chosen language — structure maps/schemes per language or equivalent.
- **Personalization/context:** preserve goal/context-appropriate behavior (e.g. AI-key + user context vs no-key generic defaults by goal, safe fallbacks) and ensure generated/fallback user-facing text is translated.

## 5) Verify
- **Build:** compile with project toolchain (offline Gradle in this worktree when applicable), e.g. `:core:compileKotlin :app:compileDebugKotlin :desktop:compileKotlin` (and assemble as needed). Fix errors/regressions.
- **Static checks:** re-scan for remaining unwrapped/non-translated non-German literals and missing keys/regex matches for known dynamic forms.
- **Runtime (required for completeness):** run app (device/emulator/ADB) and desktop, switch to the target language, and audit **all** sections, navigation, dialogs, settings, notifications, and **all** exercise types/flows including personalization paths (AI key+context and no-key/default). Record evidence (screenshots/logs/notes). Confirm intentional German remains German.
- **Regression:** spot-check default/source language still renders correctly.

## 6) Deliverables and hygiene
- List changed files and key translation/fix points.
- Report: what is covered, what remains, and evidence (command/test/runtime results). If runtime verification is blocked (no device/ADB, missing credentials/manual step), state the blocker explicitly and provide exact steps for the user.
- Do not commit/push/release excluded files; follow repo rules for versioning, changelog/release language, and required data export before push when applicable.

## Definition of done (target language mode)
- Every user-visible string is in the selected target language **except intentional German content**.
- No frozen-language UI/data (updates correctly on language switch).
- Build passes and full runtime EN↔IT-equivalent audit for the target language is evidenced across all sections, exercises, and personalization paths. If any part is unverified, report it as incomplete with blockers.