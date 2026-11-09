# Improvement plan - Deutsch

## Goal
Take the app from personal beta to a robust public release: tests, accessibility,
architecture and observability.

## Tranche 1 - Quick wins (done)
- Issue templates (bug/feature)
- CODEOWNERS, Dependabot
- SECURITY.md, SECURITY-AUDIT.md
- Documented export/import of data

## Tranche 2 - Testing
- Add JaCoCo (requires offline dependency or online CI)
- Coverage report on CI
- e2e/integration tests for profile, backup, mocked AI

## Tranche 3 - Architecture
- Split TedescoViewModel by feature
- Reduce UI/logic coupling
- Extract repositories/use-cases

## Tranche 4 - Accessibility
- WCAG AA contrast check
- Touch targets >= 48dp
- TalkBack labels and focus order
- Reduced motion

## Tranche 5 - Observability
- Optional external crash reporting (Sentry/Crashlytics)
- Core funnel analytics
- Structured logs

## Tranche 6 - Distribution
- Play Store listing, Data Safety form
- Screenshots/metadata
- Optimized onboarding

## Tranche 7 - Internationalization (in progress)
- Bilingual UI: Italian and English, switchable at runtime from Settings.
- Runtime translation layer (`Traduttore`) with exact map + interpolated patterns.
- Default to the device language on first run.
- Roadmap: extend to further interface languages (Spanish, French, German, ...)
  and to German courses for non-Italian/English speakers, reusing the same
  translation mechanism.
