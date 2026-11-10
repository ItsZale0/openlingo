# Contributing to OpenLingo

Thanks for your interest in contributing!

OpenLingo is a free and open-source language learning app that aims to feel as approachable and engaging as Duolingo, while being grounded in real didactics and tested learning methods. Contributions that improve learning quality, accessibility, localization, and reliability are welcome.

## Come contribuire

1. Fai fork del repository e crea un branch per la tua modifica.
2. Mantieni lo stile del codice esistente (Kotlin, Compose, Material 3).
3. Aggiungi o aggiorna i test unitari rilevanti (`:app:testDebugUnitTest`,
   `:core:test`).
4. Esegui i build:

```bash
./gradlew :core:jar :app:assembleDebug :desktop:compileKotlin
./gradlew :app:testDebugUnitTest :core:test
```

5. Apri una pull request con una descrizione chiara del cambiamento.

Per capire l'organizzazione del codice leggi
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md); per il processo di rilascio vedi
[docs/RELEASE.md](docs/RELEASE.md).

## Linee guida

- **Offline first**: il nucleo didattico non deve dipendere dalla rete.
- **Privacy by design**: nessun dato di un utente finisce su server di terze parti
  senza consenso esplicito.
- **FOSS**: le modifiche al codice open-source mantengono la licenza GPL-3.0-or-later.
- **Lingue e localizzazione**: l'interfaccia è localizzabile a runtime; i contenuti
  didattici in tedesco devono restare in tedesco. Per contribuire con nuove lingue
  o traduzioni (UI e contenuti), segui [docs/TRANSLATIONS.md](docs/TRANSLATIONS.md)
  e il workflow riutilizzabile in `.opencode/skills/app-translation/SKILL.md`.

## Versioning

- Incrementa `VERSION_CODE` e `VERSION_NAME` in `version.properties`.
- Aggiorna `updates/version.json` (versione, changelog, apkUrl).
- Il tag di rilascio segue il formato `v<versione>`.
