# Architettura

Questo documento descrive come è organizzato Deutsch e quali scelte tecniche
guidano il progetto. È pensato per chi vuole contribuire o valutare il codice.

## Obiettivi e vincoli

- **Offline first**: il nucleo didattico non dipende dalla rete.
- **Nessun profilo predefinito**: i dati appartengono a un profilo creato
  esplicitamente dall'utente.
- **Dati locali**: nessun account, nessun backend proprietario.
- **AI opzionale**: le funzioni AI usano una chiave fornita dall'utente e non
  sono necessarie per studiare.
- **Stack leggero**: Kotlin, Compose, DataStore e JSON; niente Room, Hilt o KSP,
  dependency injection manuale.
- **Compatibilità dei dati**: l'identificativo del pacchetto e i formati di
  salvataggio restano stabili tra le versioni.

## Moduli

```
:core      motore didattico e contenuti (Kotlin/JVM, nessuna dipendenza Android)
:app       applicazione Android (Compose, Material 3)
:desktop   applicazione desktop (Compose Desktop)
```

`:core` contiene vocabolario, grammatica, esercizi, dialoghi, ascolto, motori
adattivi, backup e merge. Non conosce Android: può essere testato con la sola
JVM. `:app` e `:desktop` sono due interfacce sullo stesso motore.

Dipendenze:

```
app ──────► core
desktop ──► core
```

## Profili e dati

Ogni profilo è descritto da `ProfiloConfig` e contiene:

- nome, livello di partenza e tema colore;
- contesto personale (`contestoUtente`): studio, lavoro, interessi, obiettivi,
  usato per personalizzare i contenuti AI;
- stato didattico: parole, ripassi, errori, grammatica, ascolto, produzione,
  lezioni generate, badge, streak, piano e sessioni.

I profili sono salvati come JSON. Su Android il documento vive in un DataStore
(`profili_data`), su desktop in `profili.json` e `preferenze.json` accanto ai
dati dell'app. I nuovi profili ricevono un identificativo `profilo_<timestamp>`;
i profili storici mantengono il proprio identificativo per non perdere dati.

Non viene creato alcun profilo automaticamente: la schermata di benvenuto guida
la creazione del primo profilo e la migrazione dei dati esistenti avviene solo
quando necessario.

## Motore didattico

### Livelli e contenuti

I livelli seguono il Quadro comune europeo: A0, A1, A2, B1, B2. Il lessico è
cumulativo e verificato da test dedicati:

| Livello | Parole cumulative | Obiettivo di riferimento |
|---|---|---|
| A1 | 670 | 650 |
| A2 | 1.378 | 1.300 |
| B1 | 2.503 | 2.400 |
| B2 | 3.108 | 3.000 |

I contenuti comprendono parole con articolo, plurale e frase d'esempio,
unità di grammatica, dialoghi, esercizi di ascolto, testi per la produzione e
brani per l'immersione.

### Esercizi

Gli esercizi offline coprono sei tipologie: significato, parola tedesca,
articolo, completamento della frase, scrittura e ascolto. Le varianti generate
dall'AI devono avere una sola risposta corretta e vengono filtrate per evitare
ambiguità.

### Ripetizione spaziata

Il lessico usa una variante di **SM-2**: ogni risposta aggiorna intervallo,
fattore di facilità e data del prossimo ripasso. Grammatica ed errori hanno
code di ripasso dedicate, più semplici ma coerenti con lo stesso modello.
Sessione giornaliera e piano di studio scelgono cosa proporre in base alle
scadenze e alle competenze.

### Motori adattivi

`AdaptiveTestEngine` e `AdaptiveSessionEngine` stimano il livello dell'utente e
compongono sessioni calibrate sulle aree più deboli, evitando di ripetere gli
stessi esercizi. Le competenze (lessico, grammatica, ascolto, produzione) sono
calcolate da dati reali di ripasso, non da valori simulati.

## Livello AI

Le funzioni AI sono isolate in `:app`, perché richiedono rete e configurazione
utente:

- `TutorService`: chat e spiegazioni, con provider Groq o Gemini;
- `GeneratoreEsercizi`: esercizi e verifiche con vincolo di unica risposta;
- `GeneratoreLezioni`: lezioni e materiali a partire dal contesto del profilo.

I prompt includono il contesto personale del profilo attivo. Le chiavi API sono
salvate nel DataStore locale e non vengono mai incluse nel repository né nei
file di backup cloud. Senza chiave, queste classi non vengono usate e l'app
resta pienamente funzionante offline.

## Aggiornamenti

Il manifest `updates/version.json` sul branch `master` dichiara:

- `versionCode` e `versionName`;
- `apkUrl` e changelog;
- `apkSha256`, lo SHA-256 dell'APK pubblicato.

L'app confronta la versione locale con il manifest, scarica l'APK nella cache e
lo accetta solo se:

1. la dimensione e l'intestazione sono compatibili con un APK;
2. lo SHA-256 coincide con quello dichiarato;
3. la firma è la stessa dell'app installata.

Il punto 3 è garantito dal fatto che tutte le release usano la stessa chiave di
firma; cambiarla renderebbe impossibile l'aggiornamento in-place.

## Backup ed esportazione

- **Esportazione completa** (`EsportaDati`): un unico documento JSON con tutti i
  profili, le preferenze, il tema e le notifiche. Il formato è versionato e
  validato prima dell'importazione.
- **Backup su GitHub**: facoltativo, usa un repository e un token scelti
  dall'utente. Il merge tra backup e dati locali è gestito da `BackupMerge`, che
  confronta le date e conserva la versione più recente di ogni elemento.

L'esportazione non contiene il token GitHub, ma contiene le chiavi API dei
profili AI: è un dato sensibile.

## Test

La suite unitaria copre:

- motore didattico, livelli, lessico e generazione degli esercizi;
- profili, migrazione, isolamento e mancata creazione automatica;
- ripetizione spaziata, motori adattivi e calcolo delle competenze;
- backup, merge, esportazione e validazione del formato;
- parsing del manifest, verifica degli APK e API remote.

I test girano con `:app:testDebugUnitTest` e `:core:test`. Il progetto non usa
test strumentali obbligatori: le parti Android sono progettate per essere
testabili sulla JVM.

## Limiti noti

- L'APK pubblico è firmato con una chiave di sviluppo: non è adatto agli store
  senza una chiave di release dedicata.
- Le funzioni AI dipendono dalla disponibilità e dai limiti del provider scelto.
- Il test dinamico (DAST) delle superfici di rete non è automatizzato; il report
  in [SECURITY-AUDIT.md](../SECURITY-AUDIT.md) descrive lo stato attuale.

Per il processo di rilascio vedi [RELEASE.md](RELEASE.md).
