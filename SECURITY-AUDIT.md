# Report di sicurezza — Deutsch

Data: 2026-11-09
Versione auditata: 1.48.73

## Ambito e metodologia

- **SAST**: analisi statica del codice sorgente (Kotlin/Compose, moduli `:core`,
  `:app`, `:desktop`, workflow GitHub Actions, configurazioni Gradle) tramite
  pattern scan dedicati (credenziali hardcoded, eval/exec/runtime code loading,
  deserializzazione pericolosa, XSS/dangerouslySetInnerHTML, WebView+Javascript,
  injection).
- **DAST**: non eseguito in questa sessione. Le superfici di rete dell'app
  (API AI Groq/Gemini, GitHub Contents API per il backup) richiedono credenziali
  dell'utente e non sono state sondate dinamicamente; sono state però riviste a
  livello statico (endpoint, intestazioni, gestione degli errori). Un test DAST
  completo resta tra le raccomandazioni prima della pubblicazione sugli store.
- **OpenSSF Scorecard**: obiettivo di qualità del progetto **≥ 0.4**.

## Verifiche eseguite in questa sessione

| # | Area | Risultato |
|---|---|---|
| 1 | Hardcoded secret/API key/tokens nel codice sorgente | **OK**: nessuna chiave reale incorporata; token AI e token GitHub gestiti come configurazione utente (DataStore) |
| 2 | Sistemi di deserializzazione pericolosi (`eval`, `exec`, `ProcessBuilder`, `Runtime.load`, `ObjectInputStream`, pickle) | **OK**: nessun utilizzo in Kotlin/Gradle |
| 3 | XSS / `dangerouslySetInnerHTML` / WebView scripting | **OK**: niente XSS surface in UI Compose; nessun WebView |
| 4 | Integrità download APK | **TROVATO + RIFIXATO**: aggiunta verifica SHA-256 (`VersionInfo.apkSha256`, `VerificaApk.isApkValido`, `sha256(file)`) |
| 5 | Backup dati sul dispositivo | **TROVATO + RIFIXATO**: `allowBackup="false"` + `data_extraction_rules.xml` per cloud backup e trasferimento dispositivo |
| 6 | Keystore di debug | **TROVATO + RIFIXATO**: keystore debug rimosso dal tracking Git (`git rm --cached`) |
| 7 | Deep link / export file | OK: export dati in JSON locale con validazione formato |

## Finding e stato

| Severità | Finding | Stato |
|---|---|---|
| Critico | Keystore debug tracciato nel repository | **Risolto**: rimosso dal version control |
| Alto | Aggiornamento senza verifica hash APK scaricato | **Risolto**: verifica SHA-256 attiva |
| Alto | Backup Android potenzialmente abilitato di default | **Risolto**: `allowBackup=false` + regole |
| Medio | — | Nessuno residuo emerso dallo scan statico |
| Basso | — | Nessuno residuo emerso dallo scan statico |

### Residui accettati

- API AI e token GitHub sono per utente: non sono segreti condivisi e restano locali.
- L'APK pubblicato non è firmato con release-key: firmato con keystore via variabili
  d'ambiente (`TEDESCO_KEYSTORE_FILE`, …) per build release; debug keystore non più
  versionato.

## Raccomandazioni

1. Eseguire OpenSSF Scorecard su ogni release (≥ 0.4 come obiettivo).
2. CI aggiuntiva di analisi SAST (Semgrep/CodeQL) per regressioni.
3. Valutare firma release-key per gli APK pubblicati.
4. Eseguire un test DAST autenticato sulla superficie di rete prima della
   pubblicazione sugli store.

## Conclusioni

Nessun finding critico residuo emerso dall'audit statico e dalla revisione manuale
delle superfici di rischio. Le vulnerabilità classificate come alte sono state
remediate nella sessione di preparazione della release 1.48.73.
