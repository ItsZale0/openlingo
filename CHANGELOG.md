# Changelog

All notable changes to OpenLingo are documented here. Release tags: https://github.com/ItsZale0/tedesco-app/tags

## v1.51.16 — 2026-11-11

### Backup
- Added **Import from file** in Backup settings to restore/load a previously exported backup JSON (full export or backup format), with merge/replace confirmation flow.

## v1.51.15 — 2026-11-11

### Security
- Updated `SECURITY.md` and `SECURITY-AUDIT.md` with the latest maintenance review (SAST-oriented review of recent changes; DAST not executed) for v1.51.15.
- Documented current security posture, scope, and recommendations; retained status of previously remediated findings.

### Build / Release
- Fixed release verification/CI parsing by ensuring `version.properties` and `updates/version.json` are UTF-8 without BOM and version values are consistent.

## v1.51.14 — 2026-11-11

### Documentation
- Translated `CONTRIBUTING.md` fully to English.

## v1.51.13 — 2026-11-11

### Documentation
- Translated repository documentation and GitHub issue templates to English (`docs/*`, `SECURITY.md`, `SECURITY-AUDIT.md`, `CODE_OF_CONDUCT.md`, `.github/ISSUE_TEMPLATE/*`).
- Updated release/version metadata.

## v1.51.12 — 2026-11-11

### Branding / UI
- Updated About/branding strings to **OpenLingo** (desktop About/footer and related labels).
- Refined English localization text (e.g. German/translation comparison label).

## v1.51.11 — 2026-11-11

### Localization / i18n
- Improved English localization coverage with runtime-safe `t()`/`Traduttore` mappings and rendering fixes (including pronunciation content/labels/meanings/rules, dialogs/listening/written production/status strings, and desktop runtime strings).
- Preserved intentional German learning content in EN mode.

### Branding / Documentation
- Refreshed README, CONTRIBUTING, and app/desktop copy to reflect OpenLingo’s objective: a free, open-source, offline-first language learning app with a Duolingo-like feel, grounded in real didactics and tested methods.

### App
- Theme and language/localization workflow improvements included in the release state.

## v1.51.10
- Use all available vocabulary words for the selected level in the article (der/die/das) trainer.

## v1.51.9
- Added article gender trainer and i18n/content updates.

> For full commit history, see: https://github.com/ItsZale0/tedesco-app/commits/master
