# Translating OpenLingo

OpenLingo's interface and learning support content can be localized at runtime.
The app already supports a bilingual interface flow (Italian/English), and the
same mechanism is intended to support additional languages.

**Important:** in any target-language mode, all **non-German user-visible text**
must appear in that language. **Intentional German learning content** (German
words, sentences, examples, target exercises, audio/TTS source German, and
grammar material meant to be shown in German) must remain in German.

## How translation works

- **Source strings:** most UI and content literals are authored in Italian in
  Kotlin source.
- **Runtime wrapper:** user-visible strings are wrapped with `t("...")`
  (`core/src/main/kotlin/com/alessandro/tedesco/i18n/Traduttore.kt`). `t()` calls
  `Traduttore.traduci(...)`.
- **Language state:** the active language is stored in `Traduttore.lingua` and
  selected/persisted via Android `sync/LinguaSettings` + Settings UI
  (`ui/ImpostazioniScreen`, applied in `ui/MainActivity`) and desktop preferences
  (`desktop/DesktopStore` / desktop settings).
- **Dictionaries:** exact translations live in chunked maps
  (`esattoChunk0..N`) merged lazily into `esatto`. Parameterized/dynamic forms are
  handled by regex `schemi` entries (e.g. counts, percentages, time, labels with
  variables).
- **Fallback:** if no exact/regex translation is found, the original string is
  returned. This is useful for intentional German content but means missing
  translations will leak through in the target language.

## Contributing translations (existing or new language)

### 1. Choose the target language
- Decide the language code/name/locale (e.g. `en`, `es`, `fr`).
- If the language is not yet selectable, add it end-to-end:
  - Android: `app/src/main/java/com/alessandro/tedesco/sync/LinguaSettings.kt` (`OPZIONI`) and Settings UI behavior.
  - Desktop: `desktop/src/main/kotlin/.../DesktopStore.kt` preferences + desktop settings/UI.
  - `core/.../i18n/Traduttore.kt`: extend translation dispatch/storage so `t()` can serve the new language (do not assume only one non-default language if making it reusable).
  - Apply locale/resource handling where needed.

### 2. Audit what must be translated
Audit **all** user-visible, non-German text in:
- `app/src/main/java/com/alessandro/tedesco/ui/**`
- `desktop/src/main/kotlin/**` (UI/screens/shell)
- `core/src/main/kotlin/com/alessandro/tedesco/data/**` (generators, datasets, lessons, quizzes, dialogs, scenarios, prompts, options, explanations, feedback, metadata)

Look for hardcoded literals in `Text(...)`, labels, placeholders,
`contentDescription`, dialogs/snackbars/notifications, and content fields such as
`titolo`, `descrizione`, `domanda`, `opzioni`, `spiegazione`, `istruzione`,
`messaggio`, `testo`, `frase`, `nota`, etc.

Do **not** translate intentional German content. When German and UI text are mixed,
translate only the non-German parts or provide mappings that preserve embedded German.

### 3. Implement runtime-safe translations
- **Wrap at render/call time:** use `t("...")` where strings are displayed or
  produced for the UI. Do not freeze translations in top-level vals, object
  initializers, enums, or static data that outlives a language switch.
- **Static data:** if data is static, prefer translating at display sites
  (e.g. `Text(t(item.titolo))`) or via runtime getters/functions so it updates on
  language change.
- **Avoid dynamic keys inside `t()`:** do not build keys like `t("${n} parole")`
  unless covered by a matching `schemi` regex; prefer stable keys + composition or
  regex schemes.
- **Add dictionary entries:**
  - Exact strings: add `"source" to "translation"` in an appropriate
    `esattoChunkN` in `Traduttore.kt`. Keys must match the source literal
    **exactly** (punctuation, quotes/apostrophes, spacing, Unicode). If you add a
    new chunk, wire it into the lazy `esatto` aggregator.
  - Parameterized strings: add/update a `Regex(...) to "..."` entry in `schemi`
    with capture groups (`$1`, `$2`, ...).
  - Include mojibake/variant forms only when they exist in source data and are
    actually rendered.
- **Personalization:** keep goal/context behavior intact (AI-key + user context vs
  no-key generic defaults by goal, safe fallbacks) and ensure fallback/generated
  user-facing text is localized.

### 4. Verify
- **Build (project toolchain):**
  ```bash
  ./gradlew :core:compileKotlin :app:compileDebugKotlin :desktop:compileKotlin
  ./gradlew :app:testDebugUnitTest :core:test
  ```
  (Use the repo's configured JDK/Android SDK/Gradle setup; offline build is possible once dependencies are cached.)
- **Static checks:** re-scan for remaining untranslated/unwrapped non-German
  user-visible strings and missing dictionary/scheme coverage.
- **Runtime audit (required for completeness):** run Android and desktop, switch to
  the target language, and verify **all** sections, navigation, dialogs, settings,
  notifications, and **all** exercise types/flows including personalization paths
  (AI key + context and no-key/default). Confirm intentional German remains German
  and the default/source language still works.

### 5. Submit
- Open a branch and PR describing the language, scope covered, and verification
  performed (build/tests/runtime evidence). Note any known gaps or blockers.
- Do not include secrets, personal exports, or excluded/local artifacts.

## Reusable workflow

See `.opencode/skills/app-translation/SKILL.md` for a step-by-step, reusable
workflow to translate the app to any chosen language and to repeat the process
for additional languages.

## Tips
- Prefer translating at UI render sites for static content to avoid freezing the
  language.
- German learning material must not be "translated away"; translate surrounding UI
  and explanations instead.
- Keep keys stable and exact; use regex schemes for variable parts.
- Large content files (dialogs, quizzes, lessons, scenarios) often need dictionary
  coverage plus correct `t()` usage at display points.
