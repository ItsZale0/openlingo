import re

path_trad = 'core/src/main/kotlin/com/alessandro/tedesco/i18n/Traduttore.kt'
quiz = open('core/src/main/kotlin/com/alessandro/tedesco/data/QuizData.kt', encoding='utf-8').read()

def esc(s): return s.replace('\\','\\\\').replace('"','\\"')

pairs = {}

dmap = {
 "Was bedeutet 'der Tisch'?": "What does 'der Tisch' mean?",
 "Was bedeutet 'die Küche'?": "What does 'die Küche' mean?",
 "Quale parola tedesca significa 'lavorare'?": "Which German word means 'to work'?",
 "Come si dice 'la settimana' in tedesco?": "How do you say 'the week' in German?",
 "Was bedeutet 'Ich habe zwei Brüder.'?": "What does 'Ich habe zwei Brüder.' mean?",
 "Quale parola tedesca significa 'mangiare'?": "Which German word means 'to eat'?",
 "Was bedeutet 'Das Wetter ist schön.'?": "What does 'Das Wetter ist schön.' mean?",
 "Come si dice 'domani' in tedesco?": "How do you say 'tomorrow' in German?",
 "Was bedeutet 'Ich wohne in Berlin.'?": "What does 'Ich wohne in Berlin.' mean?",
 "Quale parola tedesca significa 'parlare'?": "Which German word means 'to speak'?",
}
pairs.update(dmap)

omap = {
 "il tavolo":"the table","la sedia":"the chair","la porta":"the door","la finestra":"the window",
 "il bagno":"the bathroom","la cucina":"the kitchen","la camera":"the bedroom","il soggiorno":"the living room",
 "Io ho due fratelli.":"I have two brothers.","Io ho due sorelle.":"I have two sisters.","Io vado dal fratello.":"I'm going to my brother's.","Io amo i miei fratelli.":"I love my brothers.",
 "Il tempo è bello.":"The weather is nice.","Il tempo è brutto.":"The weather is bad.","Fa freddo.":"It's cold.","Fa caldo.":"It's hot.",
 "Abito a Berlino.":"I live in Berlin.","Lavoro a Berlino.":"I work in Berlin.","Vado a Berlino.":"I'm going to Berlin.","Amo Berlino.":"I love Berlin.",
}
pairs.update(omap)

smap = {
 "'Der Tisch' significa 'il tavolo'.":"'Der Tisch' means 'the table'.",
 "'Die Küche' significa 'la cucina'.":"'Die Küche' means 'the kitchen'.",
 "'Arbeiten' significa 'lavorare'. 'Wohnen' = abitare, 'lernen' = imparare, 'spielen' = giocare.":"'Arbeiten' means 'to work'. 'Wohnen' = to live, 'lernen' = to learn, 'spielen' = to play.",
 "'Die Woche' significa 'la settimana'. 'Der Tag' = il giorno, 'der Monat' = il mese, 'das Jahr' = l'anno.":"'Die Woche' means 'the week'. 'Der Tag' = the day, 'der Monat' = the month, 'das Jahr' = the year.",
 "'Zwei Brüder' = due fratelli. 'Schwestern' = sorelle.":"'Zwei Brüder' = two brothers. 'Schwestern' = sisters.",
 "'Essen' significa 'mangiare'. 'Trinken' = bere, 'schlafen' = dormire, 'lesen' = leggere.":"'Essen' means 'to eat'. 'Trinken' = to drink, 'schlafen' = to sleep, 'lesen' = to read.",
 "'Schön' = bello. 'Das Wetter' = il tempo (meteo).":"'Schön' = nice/beautiful. 'Das Wetter' = the weather.",
 "'Morgen' = domani. 'Gestern' = ieri, 'heute' = oggi, 'jetzt' = adesso.":"'Morgen' = tomorrow. 'Gestern' = yesterday, 'heute' = today, 'jetzt' = now.",
 "'Wohnen' = abitare. 'In Berlin' = a Berlino.":"'Wohnen' = to live. 'In Berlin' = in Berlin.",
 "'Sprechen' = parlare. 'Sagen' = dire, 'hören' = sentire, 'sehen' = vedere.":"'Sprechen' = to speak. 'Sagen' = to say, 'hören' = to hear, 'sehen' = to see.",
 "Konjunktiv II per desideri irreali: 'würde' + infinito.":"Konjunktiv II for hypothetical wishes: 'würde' + infinitive.",
 "Perfekt con 'sein' per verbi di movimento: 'ich bin gegangen'.":"Perfekt with 'sein' for verbs of movement: 'ich bin gegangen'.",
 "Perfekt con 'haben' per la maggior parte dei verbi: 'er hat gemacht'.":"Perfekt with 'haben' for most verbs: 'er hat gemacht'.",
 "Futur con 'werden': 'wir werden reisen'.":"Future with 'werden': 'wir werden reisen'.",
 "'Sich freuen auf' = attendere con gioia (futuro). 'Sich freuen über' = essere felice di (passato).":"'Sich freuen auf' = to look forward to (future). 'Sich freuen über' = to be happy about (past/present).",
 "'Dass' introduce una frase subordinata: 'dass er nicht viene'.":"‘Dass’ introduces a subordinate clause: 'dass er nicht kommt'.",
 "Perfekt: Partizip II 'gelesen' con 'haben'.":"Perfekt: Partizip II 'gelesen' with 'haben'.",
 "Presente con 'seit' + periodo: 'sie wohnt seit...'.":"Present tense with 'seit' + period: 'sie wohnt seit...'.",
 "Presente indicativo: 'ich kann' (possibilità attuale).":"Present indicative: 'ich kann' (current possibility).",
 "'Alles' = tutto (neutro). 'Er hat mir alles erzählt' = mi ha raccontato tutto.":"'Alles' = everything (neuter). 'Er hat mir alles erzählt' = he told me everything.",
}
pairs.update(smap)

keys_in_quiz = set(re.findall(r'"((?:[^"\\]|\\.)*)"', quiz))
filtered = {k:v for k,v in pairs.items() if k in keys_in_quiz and k != v}

s = open(path_trad, encoding='utf-8').read()
if 'esattoChunk18' in s:
    print('already exists', len(filtered))
    raise SystemExit(0)

lines = ['    private fun esattoChunk18(): Map<String, String> = mapOf(']
seen=set()
for k,v in filtered.items():
    if k in seen: continue
    seen.add(k)
    lines.append('            "{}" to "{}",'.format(esc(k), esc(v)))
lines.append('    )')
chunk='\n'.join(lines)

marker = '\n    private val esatto: Map<String, String> by lazy {'
if marker not in s or 'esattoChunk17()' not in s:
    print('marker missing'); raise SystemExit(1)
s = s.replace(marker, '\n' + chunk + '\n' + marker)
s = s.replace('        r.putAll(esattoChunk17())\n        r\n    }',
              '        r.putAll(esattoChunk17())\n        r.putAll(esattoChunk18())\n        r\n    }')
open(path_trad,'w',encoding='utf-8').write(s)
print('ok', len(filtered))
