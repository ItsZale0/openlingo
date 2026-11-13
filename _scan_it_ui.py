import re, os, sys
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

it_words = re.compile(r'\b(Clicca|Tocca|Inserisci|Scrivi|Seleziona|Scegli|Continua|Avanti|Indietro|Annulla|Conferma|Salva|Elimina|Caricamento|Errore|Nessun|Nessuna|Nessuno|Attenzione|Completato|Riprova|Successivo|Precedente|Impostazioni|Profilo|Profili|Lingua|Tema|Chiaro|Scuro|Sistema|Apri|Chiudi|Modifica|Aggiungi|Rimuovi|Cerca|Filtra|Mostra|Nascondi|Inizia|Termina|Fine|Esci|Torna|Aggiorna|Scarica|Carica|Invia|Invia|Copia|Incolla|Condividi|Esporta|Importa|Backup|Ripristina|Sincronizza|Accedi|Registrati|Password|Email|Nome|Cognome|Data|Ora|Oggi|Ieri|Domani|Settimana|Mese|Anno|Minuti|Secondi|Livello|Punteggio|Risultato|Corretto|Sbagliato|Domanda|Risposta|Spiegazione|Esercizio|Lezione|Parola|Parole|Frase|Frasi|Testo|Ascolta|Parla|Scrivi|Leggi|Traduci|Traduzione|Pronuncia|Grammatica|Vocabolario|Comprensione|Produzione|Dialogo|Dialoghi|Verifica|Progresso|Statistiche|Obiettivi|Notifiche|Promemoria|Aiuto|Informazioni|Versione|Licenza|Privacy|Termini|Feedback|Segnala|Contatta)\b')

roots = [
    'app/src/main/java/com/alessandro/tedesco/ui',
    'desktop/src/main/kotlin/com/alessandro/tedesco/desktop',
]
for root in roots:
    if not os.path.isdir(root):
        continue
    for dp, dn, fn in os.walk(root):
        for f in fn:
            if not f.endswith('.kt'):
                continue
            p = os.path.join(dp, f)
            try:
                with open(p, encoding='utf-8') as fh:
                    lines = fh.readlines()
            except Exception:
                continue
            for i, l in enumerate(lines, 1):
                if '"' not in l:
                    continue
                # skip if line already translated/wrapped or comment
                stripped = l.strip()
                if stripped.startswith('//') or stripped.startswith('*'):
                    continue
                if 't(' in l or 'stringResource(' in l or 'R.string' in l:
                    continue
                for m in re.finditer(r'"([^"\n]{3,})"', l):
                    s = m.group(1)
                    # skip German-only or code-like
                    if s.startswith('http') or s.startswith('content://'):
                        continue
                    if it_words.search(s):
                        # avoid pure identifiers
                        try:
                            print(f'{p}:{i}: {s}')
                        except UnicodeEncodeError:
                            print(f'{p}:{i}: {s.encode("utf-8","replace").decode("utf-8")}')
                        break
