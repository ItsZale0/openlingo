# Contributing to OpenLingo

Thanks for your interest in contributing!

OpenLingo is a free and open-source language learning app that aims to feel as approachable and engaging as Duolingo, while being grounded in real didactics and tested learning methods. Contributions that improve learning quality, accessibility, localization, and reliability are welcome.

## How to contribute

1. Fork the repository and create a branch for your change.
2. Keep the existing code style (Kotlin, Compose, Material 3).
3. Add or update relevant unit tests (`:app:testDebugUnitTest`, `:core:test`).
4. Run builds and tests:

```bash
./gradlew :core:jar :app:assembleDebug :desktop:compileKotlin
./gradlew :app:testDebugUnitTest :core:test

# Rust desktop app (from-scratch rebuild)
cd desktop-rust && cargo test && cargo build --release
```

5. Open a pull request with a clear description of the change.

To understand the code organization, read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). For the release process, see [docs/RELEASE.md](docs/RELEASE.md).

## Guidelines

- **Offline first**: the core learning experience must not depend on a network connection.
- **Privacy by design**: no user data is sent to third-party servers without explicit consent.
- **FOSS**: changes to open-source code keep the GPL-3.0-or-later license.
- **Modules**: `:core`/`:app`/`:desktop` are Kotlin/Compose (JDK 17); `desktop-rust/` is a Rust (Cargo) rebuild — keep data-format compatibility in mind when changing shared profile structures.
- **Languages and localization**: the interface is localizable at runtime; intentional target-language learning content (e.g. German words/sentences in the German course) must remain in that target language. To contribute new languages or translations (UI and content), follow [docs/TRANSLATIONS.md](docs/TRANSLATIONS.md) and the reusable workflow in `.opencode/skills/app-translation/SKILL.md`.

## Versioning

- Increment `VERSION_CODE` and `VERSION_NAME` in `version.properties`.
- Update `updates/version.json` (version, changelog, apkUrl, and `apkSha256` for releases).
- Release tags follow the format `v<version>`.
