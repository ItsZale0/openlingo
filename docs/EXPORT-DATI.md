# Esportazione dei dati dell'utente

Questa guida spiega come esportare **tutti i dati** salvati nell'app **Deutsch**,
per passare a un nuovo dispositivo o per fare un backup personale.

## Panoramica

L'esportazione produce un singolo file JSON (`Pacchetto`, formato 1) che contiene:

- **tutti i profili utente** salvati (parole, ripassi SRS, risposte del tutor,
  correzioni, grammatica, comprensione, produzione, ascolto, lezioni generate,
  badge, streak, piano di studio, sessioni);
- **le preferenze delle notifiche** e **il tema** scelto.

Il file è lo stesso utilizzato dal backup cloud, quindi è riutilizzabile con la
funzione di ripristino.

## Su Android

1. Apri l'app.
2. Vai in **Impostazioni → Backup / Dati → Esporta tutto** (o la voce equivalente).
3. Scegli il percorso di salvataggio (documenti del dispositivo).
4. Conserva il file in un luogo sicuro.

Il file esportato:

- **non contiene** il token GitHub del backup cloud (credenziale, va reinserito
  manualmente);
- **contiene** le chiavi API dei profili AI (configurazione): il file va custodito
  in un luogo sicuro.

## Su desktop

1. Apri le impostazioni desktop.
2. Usa la voce **Esporta dati completi**.
3. Salva il JSON generato.

## Struttura del file

```json
{
  "formato": 1,
  "creatoIl": 0,
  "versioneApp": "1.48.73",
  "profili": { ... },
  "notifiche": { ... },
  "temaColore": "..."
}
```

## Cosa significa "tutti i dati"

| Dato | Descrizione |
|---|---|
| Profili | Ogni profilo con parole, ripassi, progressi, conversazioni, lezioni. |
| Ripassi | Log delle schedature SRS (ripasso spaziato). |
| Tutor | Chat, correzioni, valutazioni di produzione. |
| Lezioni | Contenuti generati dall'AI e lezioni dal feed. |
| Progressi | Piano, sessioni, badge, streak. |

## Nota importante per il passaggio di dispositivo

- L'export include **tutti i profili**: selezionando il profilo corretto al nuovo
  dispositivo, i progressi tornano disponibili.
- Il **primo avvio non crea profili predefiniti**: il nuovo utente ne crea uno
  (vedi *Benvenuto* / *SchermataBenvenuto*).
- Se si passa da un dispositivo con profili esistenti, il ripristino preserva i
  dati.

## Backup vs esportazione

- **Backup automatico/cloud**: file di backup (formato backup) con token GitHub.
- **Esportazione completa**: pacchetto di dati utente (vedi sopra).

Per un passaggio di dispositivo completo (dati + contesto), usare entrambi.
