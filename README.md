# Deutsch

[![Ultima release](https://img.shields.io/github/v/release/ItsZale0/tedesco-app?label=release)](https://github.com/ItsZale0/tedesco-app/releases/latest)
[![Build APK](https://github.com/ItsZale0/tedesco-app/actions/workflows/build.yml/badge.svg)](https://github.com/ItsZale0/tedesco-app/actions/workflows/build.yml)
[![Licenza: GPL-3.0-or-later](https://img.shields.io/badge/licenza-GPL--3.0--or--later-blue)](LICENSE)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/ItsZale0/tedesco-app/badge)](https://securityscorecards.dev/viewer/?uri=github.com/ItsZale0/tedesco-app)

**Deutsch** è un corso di tedesco A0–B2 per Android e desktop, offline-first e
open source. Riunisce vocabolario, grammatica, ascolto, produzione, ripasso
spaziato e un tutor AI opzionale in un'unica app, con profili separati per ogni
persona che la usa.

Nato come progetto personale, è pensato per chiunque voglia studiare il tedesco
partendo dall'italiano: nessun account, nessuna pubblicità, nessun dato inviato
a servizi esterni se non quelli scelti esplicitamente dall'utente.

## Indice

- [Funzionalità](#funzionalità)
- [Piattaforme e requisiti](#piattaforme-e-requisiti)
- [Installazione](#installazione)
- [Primo avvio](#primo-avvio)
- [Intelligenza artificiale (opzionale)](#intelligenza-artificiale-opzionale)
- [Dati, backup e privacy](#dati-backup-e-privacy)
- [Sicurezza](#sicurezza)
- [Sviluppo](#sviluppo)
- [Struttura del repository](#struttura-del-repository)
- [Documentazione](#documentazione)
- [Contribuire](#contribuire)
- [Licenza](#licenza)
- [Nota sullo sviluppo: progetto vibecoded](#nota-sullo-sviluppo-progetto-vibecoded)

## Funzionalità

| Area | Contenuti |
|---|---|
| **Vocabolario** | Lessico cumulativo A1–B2: 3.108 parole (A1 670, A2 1.378, B1 2.503, B2 3.108) con articoli, plurali e frasi d'esempio |
| **Ripasso** | Ripetizione spaziata SM-2 su lessico, grammatica ed errori, con sessione giornaliera e piano di studio |
| **Esercizi** | Sei tipi offline (significato, parola tedesca, articolo, completamento, scrittura, ascolto) e generazione AI facoltativa |
| **Grammatica** | Percorso per livello con unità teoriche ed esercizi dedicati |
| **Ascolto e pronuncia** | Dettato, dialoghi, podcast, sintesi vocale (TTS) e riconoscimento vocale |
| **Produzione** | Scrittura guidata con correzione AI opzionale ed esercizi di produzione orale |
| **Tutor AI** | Chat, spiegazioni, esercizi e lezioni adattati al contesto personale dell'utente |
| **Profili** | Dati completamente separati per profilo, con contesto personale (studio, lavoro, interessi) |
| **Backup** | Esportazione/importazione completa e backup su GitHub facoltativo |
| **Desktop** | La stessa base didattica in un'app Compose Desktop |

L'interfaccia è in italiano e usa Material 3, con temi colore selezionabili
(teal, navy, indaco, verde, rosa, ambra) su mobile e desktop.

## Piattaforme e requisiti

| Piattaforma | Requisiti |
|---|---|
| Android | Android 8.0 o successivo (API 26+) |
| Desktop | Windows, macOS o Linux con JVM 17+ |

L'app Android è distribuita come APK firmato tramite le release GitHub. Il
pacchetto desktop per Windows è disponibile come archivio portabile e come
eseguibile singolo; per Linux e macOS è possibile compilare dai sorgenti.

## Installazione

1. Apri la pagina delle [release](https://github.com/ItsZale0/tedesco-app/releases/latest).
2. Scarica:
   - `Deutsch-v<versione>.apk` per Android;
   - `Deutsch-desktop-v<versione>-windows.zip` oppure `Deutsch.exe` per Windows.
3. Su Android potresti dover autorizzare l'installazione da origini sconosciute
   per il browser o il file manager usato.
4. L'app verifica automaticamente la presenza di aggiornamenti e, prima di
   installarli, controlla lo SHA-256 dell'APK scaricato.

## Primo avvio

Non esiste alcun profilo predefinito. Al primo avvio si sceglie il livello di
partenza e si crea un profilo personale; da quel momento ogni profilo mantiene
separati progressi, ripassi, conversazioni e lezioni.

Per aggiungere altre persone basta creare nuovi profili dalle impostazioni. Se
si proviene da una versione precedente, il profilo esistente viene conservato e
l'identificativo dell'applicazione resta invariato.

## Intelligenza artificiale (opzionale)

Il nucleo didattico funziona interamente offline. Le funzioni AI si attivano
solo se l'utente inserisce una propria chiave API, salvata esclusivamente sul
dispositivo:

- **Groq** per chat, generazione di esercizi e correzioni;
- **Gemini** come alternativa.

Senza chiave l'app resta utilizzabile: vocabolario, esercizi offline, ripasso,
grammatica, ascolto e produzione continuano a funzionare. Il contesto personale
del profilo viene usato per adattare i contenuti generati, senza essere inviato
altrove se non al provider AI scelto.

## Dati, backup e privacy

- I dati restano sul dispositivo: nessun account obbligatorio e nessun tracker.
- **Esportazione completa**: un unico file JSON con tutti i profili, i ripassi,
  il tutor, le lezioni e le preferenze. Guida: [docs/EXPORT-DATI.md](docs/EXPORT-DATI.md).
- **Backup su GitHub**: facoltativo, con un token personale a permessi limitati
  che l'utente può revocare in qualsiasi momento.
- Il file di esportazione contiene le chiavi API dei profili AI: va conservato
  come un dato sensibile. Non contiene invece il token GitHub.

## Sicurezza

- Verifica SHA-256 dell'APK prima dell'aggiornamento automatico.
- Backup di sistema Android disabilitato, con regole di estrazione esplicite.
- Nessun segreto, token o chiave di firma incluso nel repository.
- Analisi statica (SAST) e report: [SECURITY-AUDIT.md](SECURITY-AUDIT.md).
- Segnalazioni: [SECURITY.md](SECURITY.md).
- OpenSSF Scorecard attivo su ogni push in `master`; punteggio corrente 3,1/10
  (obiettivo minimo del progetto: 0,4).

## Sviluppo

Il progetto usa Kotlin 2.0, Gradle 8.11.1, JDK 17, Jetpack Compose e Material 3.
Non usa Room, Hilt o KSP: la persistenza si basa su DataStore e serializzazione
JSON, con dependency injection manuale.

```bash
# Build Android
./gradlew :app:assembleDebug

# Build desktop
./gradlew :desktop:run

# Test
./gradlew :app:testDebugUnitTest :core:test

# Tutto insieme
./gradlew :core:jar :app:assembleDebug :desktop:compileKotlin
```

Per compilare servono `JAVA_HOME` (JDK 17) e `ANDROID_HOME` (Android SDK con
piattaforma 35 e build-tools 35.0.0). Il progetto è compilabile anche offline
una volta scaricate le dipendenze.

I test coprono il motore didattico, i profili, la migrazione dei dati, il
backup, l'aggiornamento e le API remote; la suite conta oltre 570 test unitari.

## Struttura del repository

```
core/      motore didattico e contenuti, senza dipendenze Android
app/       applicazione Android (Compose, Material 3)
desktop/   applicazione desktop (Compose Desktop)
docs/      documentazione utente e tecnica
updates/   manifest di aggiornamento (versione e SHA-256 dell'APK)
```

## Documentazione

- [Architettura](docs/ARCHITECTURE.md): moduli, flusso dei dati, motore didattico.
- [Domande frequenti](docs/FAQ.md): uso quotidiano, aggiornamenti, backup.
- [Esportazione dati](docs/EXPORT-DATI.md): guida passo passo.
- [Processo di rilascio](docs/RELEASE.md): versionamento, tag, asset e checksum.
- [Contribuire](CONTRIBUTING.md) e [Codice di condotta](CODE_OF_CONDUCT.md).
- [Sicurezza](SECURITY.md) e [report di audit](SECURITY-AUDIT.md).

## Contribuire

Ogni contributo è benvenuto: correzioni ai contenuti didattici, traduzioni,
miglioramenti dell'interfaccia, test e segnalazioni di bug. Prima di aprire una
pull request leggi [CONTRIBUTING.md](CONTRIBUTING.md).

Le segnalazioni di vulnerabilità vanno inviate secondo [SECURITY.md](SECURITY.md),
non tramite issue pubbliche.

## Licenza

Distribuito sotto **GNU General Public License v3.0 o successiva**
(GPL-3.0-or-later). Vedi [LICENSE](LICENSE).

I contenuti didattici sono originali o di pubblico dominio; eventuali fonti
esterne sono citate nel codice o nella documentazione.

## Nota sullo sviluppo: progetto vibecoded

Deutsch è un progetto **vibecoded**: la maggior parte del codice, dei test e
della documentazione è stata scritta in collaborazione con agenti AI, sotto
direzione, revisione e verifica umana.

Che cosa significa in pratica:

- le decisioni di prodotto, la struttura dei contenuti e la revisione finale
  sono umane;
- ogni modifica viene compilata, testata e controllata prima di una release;
- le release sono verificate con SHA-256 e la pipeline CI esegue build, test e
  analisi OpenSSF Scorecard;
- l'AI accelera lo sviluppo, ma non sostituisce la responsabilità: bug, limiti
  ed errori nei contenuti restano possibili.

Se trovi qualcosa che non funziona o un contenuto da correggere, apri una issue:
è il modo più utile per migliorare il progetto.
