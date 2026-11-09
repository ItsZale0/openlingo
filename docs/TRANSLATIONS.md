# Translating Deutsch

Deutsch ships a **bilingual interface** (Italian + English) and the UI switches
language at runtime from **Settings → Language**. The same mechanism is designed
to grow into more languages.

## How the UI translation works

- All UI strings live in Kotlin source as Italian literals.
- Each literal that needs a translation is wrapped in `t("...")`
  (`core/src/main/kotlin/com/alessandro/tedesco/i18n/Traduttore.kt`).
- When the interface language is English, `Traduttore.traduci()` looks the string
  up in an exact-match map (`esatto`, ~730 entries) and, on miss, tries a list of
  regex patterns (`schemi`, ~110 entries) for interpolated strings such as
  `"12 parole"` → `"12 words"`.
- Italian is the single source of truth: `t()` returns the input unchanged when
  the language is Italian, so no string is duplicated.

## Contributing a translation

1. Fork the repository and create a branch.
2. Open `core/src/main/kotlin/com/alessandro/tedesco/i18n/Traduttore.kt`.
3. Add entries to the `esatto` map: `"Italian string" to "Translated string",`.
   - Keep the Italian key byte-for-byte identical to the source literal
     (including punctuation and spacing).
   - For interpolated strings, add a regex to `schemi` (see existing examples)
     and the corresponding replacement with `$1`, `$2`, …
4. For new interface languages, add the language code to `LinguaSettings.OPZIONI`
   (Android) and to the desktop `Preferenze`, and extend `Traduttore` to a map
   keyed by language (or a parallel object per language).
5. Run the tests: `./gradlew :app:testDebugUnitTest` (includes `TraduttoreTest`).
6. Open a pull request with the label `translation`.

## Content translation (didactic material)

The teaching content (word meanings, grammar theory, lessons, dialogues,
exercises, podcasts) is currently in Italian. Translating it to other languages
requires:

- Adding an English (or other language) gloss for each word in
  `core/.../LessicoLivelli.kt` and the other content files.
- Localizing the grammar units, lessons, dialogues and exercise prompts.

This is a large, ongoing effort. Contributions are very welcome: open an issue
with the label `translation` describing which section you want to tackle, so
work is not duplicated.

## Tips

- Search the codebase for `Text("` and `t("` to find unwrapped strings.
- The helper script `wrap5.ps1` (in the repo history) shows how literals were
  wrapped; reuse it as a reference.
- Always verify with `:app:compileDebugKotlin` and the unit tests before
  submitting.
- Do not translate identifiers, routes, keys or German words: only user-visible
  Italian prose.
