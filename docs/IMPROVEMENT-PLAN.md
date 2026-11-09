# Piano di miglioramento – Deutsch

## Obiettivo
Portare l'app da beta personale a release pubblica robusta, migliorando test, accessibilità, architettura e osservabilità.

## Tranche 1 – Quick wins (fatto)
- Template issue (bug/feature)
- CODEOWNERS, Dependabot
- SECURITY.md, SECURITY-AUDIT.md
- Export/import dati documentato

## Tranche 2 – Testing
- Aggiungere JaCoCo (richiede dipendenza offline o CI online)
- Coverage report su CI
- Test e2e/integration per profilo, backup, AI mockata

## Tranche 3 – Architettura
- Split TedescoViewModel per feature
- Ridurre accoppiamento UI/logica
- Estrazione di repository/use-case

## Tranche 4 – Accessibilità
- Verifica contrasto WCAG AA
- Touch target >= 48dp
- TalkBack labels e ordine focus
- Riduzione motion

## Tranche 5 – Osservabilità
- Crash reporting esterno (Sentry/Crashlytics) opzionale
- Analytics funnel core
- Log strutturati

## Tranche 6 – Distribution
- Play Store listing, Data Safety form
- Screenshot/metadata
- Onboarding ottimizzato
