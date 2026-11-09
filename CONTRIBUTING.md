# Contribuire a Deutsch

Grazie per il tuo interesse a contribuire!

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

## Linee guida

- **Offline first**: il nucleo didattico non deve dipendere dalla rete.
- **Privacy by design**: nessun dato di un utente finisce su server di terze parti
  senza consenso esplicito.
- **FOSS**: le modifiche al codice open-source mantengono la licenza GPL-3.0-or-later.
- **Italiano**: la lingua utente e quella di documentazione e messaggi di prodotto.

## Versioning

- Incrementa `VERSION_CODE` e `VERSION_NAME` in `version.properties`.
- Aggiorna `updates/version.json` (versione, changelog, apkUrl).
- Il tag di rilascio segue il formato `v<versione>`.
