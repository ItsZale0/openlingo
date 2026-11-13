# -*- coding: utf-8 -*-
import re

trad_path = r'core/src/main/kotlin/com/alessandro/tedesco/i18n/Traduttore.kt'
imm_path = r'core/src/main/kotlin/com/alessandro/tedesco/data/DidatticaImmersione.kt'

with open(trad_path, encoding='utf-8') as f:
    trad = f.read()
with open(imm_path, encoding='utf-8') as f:
    imm = f.read()

# Extract unique contexts
ctxs = []
seen=set()
for m in re.finditer(r'contesto\s*=\s*"([^"]+)"', imm):
    v=m.group(1)
    if v not in seen:
        seen.add(v)
        ctxs.append(v)

translations = {
    "Una frase alla volta, mai due regole insieme. Il tedesco non è caos: sono scaglioni che si ripetono.": "One sentence at a time, never two rules together. German isn't chaos: it's patterns that repeat.",
    "Stessi personaggi, stessa casa: cambia solo una cosa (un pacco). Il resto del vocabolario si ripete, e capire diventa facile.": "Same characters, same house: only one thing changes (a package). The rest of the vocabulary repeats, and understanding becomes easy.",
    "Le stesse strutture in una scena reale: 1000 parole usuali coprono l'85% della conversazione quotidiana.": "The same structures in a real scene: 1,000 common words cover 85% of everyday conversation.",
    "La stessa Lena, un altro posto: la stazione. Le parole del viaggio tornano utili subito.": "The same Lena, a different place: the station. Travel words become useful right away.",
    "Perfekt in una scena reale: le azioni finite raccontate con le parole di tutti i giorni.": "Perfekt in a real scene: completed actions told with everyday words.",
    "Dopo la visita: la ricetta, la farmacia e le istruzioni. Il tedesco che serve davvero quando si è pazienti.": "After the visit: the prescription, the pharmacy, and the instructions. The German you really need as a patient.",
    "Proposizioni con verbo in fondo: la regola, vista dentro una frase vera, non in una tabella.": "Clauses with the verb at the end: the rule, seen inside a real sentence, not in a table.",
    "La stessa donna, un capitolo nuovo: il lavoro. Congiunzioni e verbo in fondo tornano nella stessa forma, così si riconoscono senza tradurre.": "The same woman, a new chapter: work. Conjunctions and the verb at the end return in the same form, so you recognize them without translating.",
    "Passivo con modali e Konjunktiv: le forme della burocrazia reale, riconosciute nel testo prima che spiegate.": "Passive with modals and Konjunktiv: the forms of real bureaucracy, recognized in the text before being explained.",
    "La stessa pratica, un passo avanti: il colloquio allo sportello. Passivo, Konjunktiv e connettivi in una scena che si ripete nella vita vera.": "The same process, one step further: the conversation at the counter. Passive, Konjunktiv, and connectors in a scene that repeats in real life.",
}

# add chunk21 before schemi if not present
if 'esattoChunk21' in trad:
    print('chunk21 already exists')
    exit(0)

# build entries for found contexts
entries = []
for c in ctxs:
    if c in translations:
        entries.append((c, translations[c]))
    else:
        # fallback: skip unknown, we'll note
        pass

# also add a few common DidatticaImmersione UI strings if missing
extra = [
    ("Cosa significa?", "What does it mean?"),
    ("Cosa significa", "What does it mean"),
]

lines = []
for k,v in entries+extra:
    k_esc = k.replace('\\','\\\\').replace('"','\\"')
    v_esc = v.replace('\\','\\\\').replace('"','\\"')
    lines.append(f'        "{k_esc}" to "{v_esc}",')

chunk = "\n    private fun esattoChunk21(): Map<String, String> = mapOf(\n" + "\n".join(lines) + "\n    )\n"

marker = "\n    private val schemi: List<Pair<Regex, String>> = listOf("
if marker not in trad:
    print('schemi marker not found')
    exit(1)

trad = trad.replace(marker, "\n" + chunk + marker)

# add to aggregator
agg_marker = "        r.putAll(esattoChunk20())\n        r\n    }"
if agg_marker not in trad:
    # try flexible
    if "r.putAll(esattoChunk20())" in trad and "r.putAll(esattoChunk21())" not in trad:
        trad = trad.replace("r.putAll(esattoChunk20())", "r.putAll(esattoChunk20())\n        r.putAll(esattoChunk21())")
else:
    trad = trad.replace(agg_marker, "        r.putAll(esattoChunk20())\n        r.putAll(esattoChunk21())\n        r\n    }")

with open(trad_path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(trad)

print('added', len(entries), 'contexts and', len(extra), 'extra')
