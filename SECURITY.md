# Sicurezza

## Reporting di una vulnerabilità

Si prega di aprire una issue privata su GitHub o di contattare direttamente gli
amministratori del repository, descrivendo:

- l'ambiente e la versione dell'app;
- i passi per riprodurre;
- l'impatto stimato.

Non si accettano segnalazioni che coinvolgono attacchi su sistemi terzi.

## Pratiche di sicurezza attive nel progetto

- **Verifica integrità APK**: gli aggiornamenti scaricano l'APK e lo verificano
  tramite SHA-256 (`VersionInfo.apkSha256`, `VerificaApk.isApkValido`).
- **Backup cloud**: token GitHub a scelta dell'utente (fine-grained, permessi
  limitati), salvato solo nel DataStore locale; nessun token incorporato nel repo.
- **Export dati**: i dati personali dell'utente possono essere esportati in JSON
  dal dispositivo (vedi [docs/EXPORT-DATI.md](docs/EXPORT-DATI.md)).
- **Data safety**: `allowBackup="false"` e regole di estrazione dati
  personalizzate per cloud backup e trasferimento dispositivo.
- **Chiavi AI**: configurate dall'utente in Impostazioni; nessuna chiave condivisa
  o incorporata.

## Scoping degli audit

- SAST: analisi statica del codice Kotlin/Compose/Gradle.
- DAST: non applicabile in forma autonoma per un'app Android/ChromeOS; il perimetro
  di valutazione resta l'APK pubblicato e le superfici di rete (API AI, GitHub).
- Ogni finding è classificato con severità (Critico / Alto / Medio / Basso).

## OpenSSF Scorecard

Obiettivo di qualità del progetto: **Scorecard ≥ 0.4** (OSSRF). I risultati
degli step correnti sono riportati nel report di audit.
