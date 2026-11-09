# Domande frequenti

## Devo creare un account?

No. Deutsch non richiede registrazione e non ha pubblicità o tracker. Al primo
avvio si crea un profilo locale.

## Funziona senza internet?

Sì. Vocabolario, esercizi offline, ripasso spaziato, grammatica, ascolto e
produzione funzionano senza connessione. Internet serve solo per le funzioni AI
facoltative, il backup su GitHub e il controllo degli aggiornamenti.

## L'AI è obbligatoria?

No. Se non inserisci una chiave API, l'app usa solo i contenuti e gli esercizi
inclusi. Con una chiave Groq o Gemini ottieni tutor, esercizi e correzioni
personalizzate.

## Dove finiscono i miei dati?

Restano sul dispositivo. Puoi esportarli in un unico file JSON da
**Impostazioni → Backup → Esporta tutti i dati**. Vedi
[EXPORT-DATI.md](EXPORT-DATI.md).

## Posso usare l'app in più persone?

Sì. Ogni profilo ha dati separati: progressi, ripassi, conversazioni e lezioni.
Puoi crearne quanti vuoi dalle impostazioni.

## Ho aggiornato l'app: perdo i progressi?

No. Gli aggiornamenti mantengono lo stesso identificativo del pacchetto e la
stessa firma, quindi profili e progressi restano disponibili. Prima di un
cambio dispositivo, esporta comunque tutti i dati.

## Come si aggiorna l'app?

L'app controlla `updates/version.json` sul repository e propone la nuova
versione. Prima dell'installazione verifica lo SHA-256 dell'APK scaricato. Puoi
anche scaricare manualmente l'ultima release dalla pagina GitHub.

## Ho salvato il backup su GitHub ma non lo trovo

Dal ripristino l'app prova in ordine:

1. il file del profilo attivo (`tedesco-backup-<profilo>.json`);
2. il backup condiviso `tedesco-backup.json`;
3. qualunque altro backup valido presente nel repository, dal più recente.

Se non viene trovato nulla, controlla che:

- il repository configurato sia quello giusto (utente/nome-repo);
- il token abbia il permesso **Contents: Read and write** sul repository;
- il repository contenga almeno un file `tedesco-backup*.json`.

Il messaggio d'errore ora indica il repository e i nomi dei file cercati. In
alternativa puoi usare **Esporta tutti i dati** e reimportare il file JSON.

## Su quali piattaforme è disponibile?

Android 8.0+ e desktop (Windows, macOS, Linux). Non esiste una versione iOS.

## Posso contribuire?

Sì: correzioni ai contenuti, traduzioni, miglioramenti dell'interfaccia, test e
segnalazioni. Leggi [CONTRIBUTING.md](../CONTRIBUTING.md).

## Ho trovato un problema di sicurezza. Dove lo segnalo?

Secondo [SECURITY.md](../SECURITY.md), non tramite issue pubbliche.

## Perché il progetto si definisce "vibecoded"?

Perché è sviluppato in collaborazione con agenti AI, con revisione e verifica
umana. La nota completa è in fondo al [README](../README.md#nota-sullo-sviluppo-progetto-vibecoded).
