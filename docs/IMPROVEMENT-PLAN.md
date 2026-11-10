# Improvement plan - OpenLingo

## Goal
Take the app from personal beta to a robust public release: tests, accessibility,
architecture and observability.

## Tranche 1 - Quick wins (done)
- Issue templates (bug/feature/translation)
- CODEOWNERS, Dependabot
- SECURITY.md, SECURITY-AUDIT.md
- Documented export/import of data

## Tranche 2 - Testing
- Add JaCoCo (requires offline dependency or online CI)
- Coverage report on CI
- End-to-end/integration tests for profile, backup, mocked AI

## Tranche 3 - Architecture
- Split large view models (e.g. `TedescoViewModel`) by feature
- Reduce UI/logic coupling
- Extract repositories/use-cases
- Generalize from single-target-language assumptions toward multi-course/target-language support

## Tranche 4 - Accessibility
- WCAG AA contrast check
- Touch targets >= 48dp
- TalkBack labels and focus order
- Reduced motion support

## Tranche 5 - Observability
- Optional external crash reporting (Sentry/Crashlytics)
- Core funnel analytics (privacy-preserving/opt-in)
- Structured logs

## Tranche 6 - Distribution
- Play Store listing, Data Safety form (if applicable)
- Screenshots/metadata
- Optimized onboarding

## Tranche 7 - Internationalization and localization (in progress)
- Bilingual UI: Italian and English, switchable at runtime from Settings.
- Runtime translation layer (`Traduttore`) with exact maps + pattern schemes.
- Default to device/system language where appropriate.
- Roadmap: extend to additional UI languages and improve full EN coverage across app and desktop.
- Roadmap: support additional target learning languages/courses (e.g. Danish, Dutch) reusing the same engine and localization mechanisms.
