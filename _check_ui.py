import xml.etree.ElementTree as ET, re, sys
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
it_patterns = re.compile(r"\b(Ripasso|Nessuna parola|Clicca|Tocca|Premi|Continua|Avanti|Indietro|Annulla|Conferma|Salva|Elimina|Caricamento|Errore|Nessun|Nessuna|Attenzione|Completato|Riprova|Impostazioni|Profilo|Lingua|Chiaro|Scuro|Sistema|Inizia|Termina|Esci|Torna|Aggiorna|Obiettivi|Statistiche|Lezione|Esercizio|Domanda|Risposta|Spiegazione|Ascolta|Pronuncia|Comprensione|Produzione|Parole|Frasi|Progresso|Consigli|Piano di oggi)\b", re.I)
for name in ['_ui_en_rev.xml','_ui_en_gram.xml']:
    try:
        t=ET.parse(name); r=t.getroot()
    except Exception as e:
        print(name,'parse err',e); continue
    texts=[]
    for n in r.iter('node'):
        tx=n.attrib.get('text','').strip(); cd=n.attrib.get('content-desc','').strip()
        if tx: texts.append(tx)
        if cd: texts.append(cd)
    print('---',name,len(texts))
    for x in texts[:120]:
        print(x)
    print('ITALIAN_HITS?')
    for x in texts:
        if it_patterns.search(x):
            print('  HIT:',x)
