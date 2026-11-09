# Deutsch

**Deutsch** è un'app Android (Kotlin · Compose · Material 3) e desktop (Compose
Desktop) per imparare o migliorare la lingua tedesca, pensata per studenti
italiani.

> Dalla versione 1.48.73 l'app si chiama **Deutsch**: il nome generico sostituisce
> quello precedente legato al profilo personale. L'identificativo del pacchetto
> resta invariato, così profili e progressi esistenti continuano a funzionare.

## Caratteristiche principali

- **Nessun profilo predefinito**: alla prima installazione l'utente crea il
  proprio profilo (nome, livello di partenza, contesto personale).
- **Offline di base**: vocabolario A1–B2 incluso nell'app, ripasso spaziato,
  esercizi offline di varia tipologia, grammatica, ascolto, produzione.
- **AI opzionale**: tutor e generazione di esercizi con un provider AI
  (Groq/Gemini) configurato dall'utente; senza chiave l'app funziona offline.
- **Personalizzazione**: contesto utente (professione, obiettivi, interessi)
  usato dai prompt AI per personalizzare contenuti e vocabolario.
- **Backup e sincronizzazione**: export/import dati completi (locale o GitHub).

## Requisiti

- Android 8.0+ (API 26+), Kotlin 2.0, JDK 17, Compose, Material 3.
- Desktop: Windows/macOS/Linux, JVM 17+.

## Build

```bash
./gradlew :app:assembleDebug
./gradlew :desktop:compileKotlin
```

## Licenza

Questo progetto è distribuito sotto la **GNU General Public License v3.0 o
successiva** (GPL-3.0-or-later). Vedi [LICENSE](LICENSE).

## Dati e privacy

- Nessun tracker, nessun account obbligatorio: i dati restano sul dispositivo
  dell'utente.
- Backup e export sono disponibili in Impostazioni (JSON, con opzione di
  cifratura/modalità cloud).
- Dettagli su come esportare e trasportare tutti i propri dati:
  [docs/EXPORT-DATI.md](docs/EXPORT-DATI.md).

## Sicurezza

- SHA-256 dell'APK verificato prima dell'aggiornamento automatico.
- Backup cloud con token GitHub a scelta dell'utente; nessun token incorporato.
- Analisi statica (SAST) e revisione delle superfici di rete: [SECURITY.md](SECURITY.md).

## Documentazione per sviluppatori

- [CONTRIBUTING.md](CONTRIBUTING.md)
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
- [SECURITY.md](SECURITY.md)
- [docs/EXPORT-DATI.md](docs/EXPORT-DATI.md)
