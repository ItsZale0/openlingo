package com.alessandro.tedesco.data

// ============================================================
// Unità aggiunte (parte 2): B1 e B2
// ============================================================

// ---------------------------- B1 (6 unità) ----------------------------

internal fun unitaB1Plusquamperfekt(): UnitaGrammatica = unitaGrammatica(
    id = "b1-plusquamperfekt",
    livello = "B1",
    titolo = "Plusquamperfekt",
    sottotitolo = "Il passato del passato",
    obiettivo = "Formare il piuccheperfetto e usarlo per azioni concluse prima di un'altra azione passata.",
    minuti = 12,
    prerequisiti = listOf("a2-praeteritum"),
    regola = listOf(
        "Il Plusquamperfekt esprime un'azione conclusa prima di un'altra azione passata: Als ich ankam, war er schon gegangen.",
        "Forma: ausiliare al Präteritum (hatte/war) + Partizip II — come il Perfekt, ma con hatte/war invece di hat/ist.",
        "Ausiliare (come nel Perfekt): haben per la maggior parte dei verbi, sein per i verbi di movimento/cambiamento di stato.",
        "Con nachdem la frase con il Plusquamperfekt va prima: Nachdem er gegessen hatte, ging er.",
        "Con als il Plusquamperfekt esprime l'anteriorità rispetto all'azione principale."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Formazione",
            intestazioni = listOf("Tempo", "Ausiliare", "Partizip II", "Esempio"),
            righe = listOf(
                listOf("Perfekt", "hat / ist", "gegessen", "Er hat gegessen."),
                listOf("Plusquamperfekt", "hatte / war", "gegessen", "Er hatte gegessen."),
                listOf("Plusquamperfekt", "war", "gegangen", "Sie war gegangen.")
            ),
            nota = "L'ausiliare del Plusquamperfekt è sempre al Präteritum (hatte/war)."
        )
    ),
    esempi = listOf(
        esempioUnita("Als ich ankam, war er schon gegangen.", "Quando arrivai, era già partito."),
        esempioUnita("Nachdem sie gegangen war, räumte ich auf.", "Dopo che se ne era andata, feci ordine."),
        esempioUnita("Ich hatte das Buch schon gelesen, als du mir davon erzähltest.", "Avevo già letto il libro quando me ne parlasti."),
        esempioUnita("Er war schon abgereist, als wir ankamen.", "Era già partito quando arrivammo.")
    ),
    errori = listOf(
        erroreUnita("Als ich ankam, hat er schon gegessen.", "Als ich ankam, war er schon gegangen.", "Azione precedente in contesto passato: Plusquamperfekt (war ... gegangen)."),
        erroreUnita("Nachdem er gegessen hat, ging er.", "Nachdem er gegessen hatte, ging er.", "Dopo nachdem il verbo va al Plusquamperfekt."),
        erroreUnita("Er hatte gegangen.", "Er war gegangen.", "Verbi di movimento: ausiliare sein (war).")
    ),
    merksatz = "Plusquamperfekt = hatte/war (Präteritum) + Partizip II; l'azione precedente prende il Plusquamperfekt.",
    esercizi = listOf(
        sceltaUnita("pq-01", "Als ich ankam, ___ er schon gegessen.", listOf("hat", "hatte", "war", "ist"), 1, "Azione precedente: hatte gegessen."),
        sceltaUnita("pq-02", "Nachdem sie ___, räumte ich auf.", listOf("gegangen ist", "gegangen war", "gegangen hat", "gegangen hatte"), 1, "Nachdem + Plusquamperfekt: war gegangen."),
        sceltaUnita("pq-03", "Er ___ das Buch schon gelesen, als du anriefst.", listOf("hat", "hatte", "war", "ist"), 1, "Plusquamperfekt: hatte gelesen."),
        sceltaUnita("pq-04", "Sie ___ schon abgereist, als wir ankamen.", listOf("hat", "hatte", "war", "ist"), 2, "Verbo di movimento: war abgereist."),
        sceltaUnita("pq-05", "Ich ___ die Nachricht schon gehört, als er mir davon erzählte.", listOf("habe", "hatte", "bin", "war"), 1, "Plusquamperfekt: hatte gehört."),
        produciUnita("b1-plusq-prod", "„Quando arrivai, lui aveva già mangiato.“", "Als ich ankam, hatte er schon gegessen.", "Plusquamperfekt: hatte + Partizip II (gegessen)."),
    )
)

internal fun unitaB1NDeklination(): UnitaGrammatica = unitaGrammatica(
    id = "b1-n-deklination",
    livello = "B1",
    titolo = "n-Deklination",
    sottotitolo = "Der Student, den Studenten, dem Studenten",
    obiettivo = "Declinare i sostantivi maschili che prendono -n/-en in tutti i casi tranne il nominativo singolare.",
    minuti = 12,
    prerequisiti = listOf("a2-dativo"),
    regola = listOf(
        "Alcuni sostantivi maschili (per lo più persone, professioni, nazionalità) prendono -n o -en in tutti i casi tranne il nominativo singolare.",
        "Lista tipica: der Herr, der Junge, der Mensch, der Student, der Kollege, der Polizist, der Patient, der Kunde, der Präsident, der Held, der Experte, der Neffe, der Affe, der Löwe, der Nachbar, der Bauer, der Deutsche, der Franzose.",
        "Genitivo singolare: -n (der Studenten) oppure -ens (des Herrn).",
        "Plurale: forma regolare + -n se non termina già in -n.",
        "La desinenza si aggiunge al nominativo singolare: Student → Studenten (akk/dat/gen), Herr → Herrn (akk/dat) / Herrn(s) (gen).",
        "Con preposizioni come mit, bei, zu il dativo resta: mit dem Kollegen."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "n-Deklination: der Student",
            intestazioni = listOf("Caso", "Singolare", "Plurale"),
            righe = listOf(
                listOf("Nominativo", "der Student", "die Studenten"),
                listOf("Accusativo", "den Studenten", "die Studenten"),
                listOf("Dativo", "dem Studenten", "den Studenten"),
                listOf("Genitivo", "des Studenten", "der Studenten")
            )
        ),
        tabellaUnita(
            titolo = "Casi speciali",
            intestazioni = listOf("Nominativo", "Accusativo", "Genitivo"),
            righe = listOf(
                listOf("der Herr", "den Herrn", "des Herrn"),
                listOf("der Junge", "den Jungen", "des Jungen"),
                listOf("der Mensch", "den Menschen", "des Menschen"),
                listOf("der Kollege", "den Kollegen", "des Kollegen")
            ),
            nota = "Il genitivo di Herr è des Herrn (con -n + -s)."
        )
    ),
    esempi = listOf(
        esempioUnita("Der Student kommt.", "Lo studente viene."),
        esempioUnita("Ich sehe den Studenten.", "Vedo lo studente."),
        esempioUnita("Ich helfe dem Studenten.", "Aiuto lo studente."),
        esempioUnita("Das Buch des Studenten ist neu.", "Il libro dello studente è nuovo."),
        esempioUnita("Der Kollege, den ich kenne, ist nett.", "Il collega che conosco è simpatico.")
    ),
    errori = listOf(
        erroreUnita("Ich sehe den Student.", "Ich sehe den Studenten.", "n-Deklination: accusativo -en."),
        erroreUnita("Ich helfe dem Student.", "Ich helfe dem Studenten.", "Dativo: -en."),
        erroreUnita("Das Buch des Student.", "Das Buch des Studenten.", "Genitivo: -en.")
    ),
    merksatz = "Maschile persona/professione → -n/-en in tutti i casi tranne il nominativo singolare.",
    esercizi = listOf(
        sceltaUnita("ndek-01", "Ich sehe den ___.", listOf("Student", "Studenten", "Studente", "Studentes"), 1, "Accusativo n-Deklination: Studenten."),
        sceltaUnita("ndek-02", "Ich helfe dem ___.", listOf("Kollege", "Kollegen", "Kolleges", "Kolleg"), 1, "Dativo: Kollegen."),
        sceltaUnita("ndek-03", "Das Buch des ___ ist neu.", listOf("Student", "Studenten", "Studentes", "Studenter"), 1, "Genitivo: Studenten."),
        sceltaUnita("ndek-04", "Der ___ kommt.", listOf("Student", "Studenten", "Studente", "Studentes"), 0, "Nominativo singolare: senza -n."),
        sceltaUnita("ndek-05", "Ich kenne den ___.", listOf("Herr", "Herrn", "Herres", "Herrs"), 1, "Herr → Herrn (accusativo)."),
        produciUnita("b1-ndekl-prod", "„Il nome del paziente è Müller.“", "Der Name des Patienten ist Müller.", "Patient è un sostantivo della n-Deklination: des Patienten."),
    )
)

internal fun unitaB1GenitivPraepositionen(): UnitaGrammatica = unitaGrammatica(
    id = "b1-genitiv-praepositionen",
    livello = "B1",
    titolo = "Preposizioni con genitivo",
    sottotitolo = "Wegen, trotz, während: la causa e la concessione",
    obiettivo = "Usare le preposizioni che reggono il genitivo e conoscere l'eccezione con i pronomi personali.",
    minuti = 12,
    prerequisiti = listOf("b1-genitiv"),
    regola = listOf(
        "Alcune preposizioni reggono il genitivo: wegen (a causa di), trotz (nonostante), während (durante), (an)statt (invece di), außerhalb (fuori di), innerhalb (dentro), oberhalb (sopra), unterhalb (sotto).",
        "wegen esprime la causa: Wegen des Wetters bleibe ich zu Hause.",
        "trotz esprime la concessione (nonostante): Trotz der Kälte gehen wir spazieren.",
        "während esprime il tempo (durante): Während des Films schlief er ein.",
        "anstatt/statt esprime la sostituzione: Anstatt zu arbeiten, spielte er.",
        "Eccezione: con i pronomi personali queste preposizioni reggono il dativo: wegen mir, trotz dir, währenddessen.",
        "Attenzione ai verbi separabili: ausgehen → gehen ... aus (Trotz des Regens gehe ich aus.)."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Preposizioni con genitivo",
            intestazioni = listOf("Preposizione", "Significato", "Esempio"),
            righe = listOf(
                listOf("wegen", "a causa di", "Wegen des Wetters bleibe ich zu Hause."),
                listOf("trotz", "nonostante", "Trotz der Kälte gehen wir spazieren."),
                listOf("während", "durante", "Während des Films schlief er ein."),
                listOf("anstatt / statt", "invece di", "Anstatt zu arbeiten, spielte er."),
                listOf("außerhalb", "fuori di", "Außerhalb der Stadt ist es ruhig."),
                listOf("innerhalb", "dentro", "Innerhalb einer Woche ist es fertig.")
            )
        ),
        tabellaUnita(
            titolo = "Eccezione con i pronomi personali",
            intestazioni = listOf("Con sostantivo (genitivo)", "Con pronome (dativo)"),
            righe = listOf(
                listOf("wegen des Wetters", "wegen mir"),
                listOf("trotz der Kälte", "trotz dir"),
                listOf("während des Films", "währenddessen")
            ),
            nota = "Con i pronomi personali queste preposizioni reggono il dativo."
        )
    ),
    esempi = listOf(
        esempioUnita("Wegen des Wetters bleibe ich zu Hause.", "A causa del tempo resto a casa."),
        esempioUnita("Trotz der Kälte gehen wir spazieren.", "Nonostante il freddo usciamo a passeggiare."),
        esempioUnita("Während des Films schlief er ein.", "Durante il film si addormentò."),
        esempioUnita("Anstatt zu arbeiten, spielte er.", "Invece di lavorare, giocò."),
        esempioUnita("Außerhalb der Stadt ist es ruhig.", "Fuori città è tranquillo.")
    ),
    errori = listOf(
        erroreUnita("Wegen dem Wetter bleibe ich zu Hause.", "Wegen des Wetters bleibe ich zu Hause.", "wegen + genitivo: des Wetters."),
        erroreUnita("Trotz die Kälte gehen wir spazieren.", "Trotz der Kälte gehen wir spazieren.", "trotz + genitivo: der Kälte."),
        erroreUnita("Wegen mir des Wetters.", "Wegen des Wetters / wegen mir.", "Non si mescolano: genitivo con sostantivo, dativo con pronome.")
    ),
    merksatz = "wegen/trotz/während/anstatt + genitivo del sostantivo; con pronomi personali → dativo.",
    esercizi = listOf(
        sceltaUnita("genp-01", "___ des Wetters bleibe ich zu Hause.", listOf("Wegen", "Trotz", "Während", "Statt"), 0, "a causa del tempo: wegen."),
        sceltaUnita("genp-02", "___ der Kälte gehen wir spazieren.", listOf("Wegen", "Trotz", "Während", "Statt"), 1, "nonostante il freddo: trotz."),
        sceltaUnita("genp-03", "___ des Films schlief er ein.", listOf("Wegen", "Trotz", "Während", "Statt"), 2, "durante il film: während."),
        sceltaUnita("genp-04", "___ zu arbeiten, spielte er.", listOf("Wegen", "Trotz", "Während", "Anstatt"), 3, "invece di lavorare: anstatt."),
        sceltaUnita("genp-05", "Außerhalb ___ Stadt ist es ruhig.", listOf("der", "die", "dem", "den"), 0, "außerhalb + genitivo: der Stadt."),
        produciUnita("b1-genpraep-prod", "„Durante la lezione prendo appunti.“", "Während des Unterrichts mache ich Notizen.", "während regge il genitivo: des Unterrichts."),
    )
)

internal fun unitaB1AdjektiveAlsNomen(): UnitaGrammatica = unitaGrammatica(
    id = "b1-adjektive-als-nomen",
    livello = "B1",
    titolo = "Aggettivi come nomi",
    sottotitolo = "Der Alte, das Gute, der Angestellte",
    obiettivo = "Usare aggettivi sostantivati (con maiuscola) e declinarli correttamente.",
    minuti = 12,
    prerequisiti = listOf("b1-adjektivdeklination"),
    regola = listOf(
        "Un aggettivo può diventare sostantivo: prende la maiuscola e si declina come aggettivo (forte/debole/mista).",
        "Esempi comuni: der Alte (il vecchio), die Alte (la vecchia), das Alte (la cosa vecchia); der/die/das Gute, der/die/das Schöne.",
        "Molto frequenti con persone: der Angestellte (l'impiegato), der Beamte (il funzionario), der Deutsche (il tedesco), der Verwandte (il parente), der Bekannte (il conoscente), der Verletzte (il ferito).",
        "La declinazione segue le regole dell'aggettivo: dopo articolo determinativo desinenza -e/-en, dopo indeterminativo -er/-es/-en, senza articolo -er/-es/-en.",
        "Plurale: die Alten, die Guten, die Angestellten."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Aggettivi sostantivati comuni",
            intestazioni = listOf("Aggettivo", "Singolare", "Plurale", "Significato"),
            righe = listOf(
                listOf("alt", "der/die/das Alte", "die Alten", "il/la vecchio/a; la cosa vecchia"),
                listOf("gut", "der/die/das Gute", "die Guten", "il bene; il buono"),
                listOf("schön", "der/die/das Schöne", "die Schönen", "il bello"),
                listOf("angestellt", "der Angestellte", "die Angestellten", "l'impiegato"),
                listOf("deutsch", "der Deutsche", "die Deutschen", "il tedesco (persona)")
            )
        ),
        tabellaUnita(
            titolo = "Declinazione: der Alte",
            intestazioni = listOf("Caso", "Maschile", "Femminile", "Neutro"),
            righe = listOf(
                listOf("Nominativo", "der Alte", "die Alte", "das Alte"),
                listOf("Accusativo", "den Alten", "die Alte", "das Alte"),
                listOf("Dativo", "dem Alten", "der Alten", "dem Alten"),
                listOf("Genitivo", "des Alten", "der Alten", "des Alten")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Der Alte wartet an der Ecke.", "Il vecchio aspetta all'angolo."),
        esempioUnita("Ich helfe dem Alten.", "Aiuto il vecchio."),
        esempioUnita("Die Kranken werden behandelt.", "I malati vengono curati."),
        esempioUnita("Das Schöne an der Reise ist das Wetter.", "La cosa bella del viaggio è il tempo."),
        esempioUnita("Der Angestellte arbeitet im Büro.", "L'impiegato lavora in ufficio.")
    ),
    errori = listOf(
        erroreUnita("Der alte wartet.", "Der Alte wartet.", "Aggettivo sostantivato = maiuscola."),
        erroreUnita("Ich helfe dem alten.", "Ich helfe dem Alten.", "Declinazione aggettivale: dem Alten."),
        erroreUnita("Die Alte sind gekommen.", "Die Alten sind gekommen.", "Plurale: die Alten.")
    ),
    merksatz = "Aggettivo sostantivato = maiuscola + declinazione aggettivale (come dopo articolo).",
    esercizi = listOf(
        sceltaUnita("adjnom-01", "Der ___ wartet an der Ecke.", listOf("Alte", "Alten", "Alter", "Altes"), 0, "Nominativo maschile: der Alte."),
        sceltaUnita("adjnom-02", "Ich danke dem ___.", listOf("Alte", "Alten", "Alter", "Altes"), 1, "Dativo: dem Alten."),
        sceltaUnita("adjnom-03", "Die ___ werden behandelt.", listOf("Kranke", "Kranken", "Kranker", "Krankes"), 1, "Plurale nominativo: die Kranken."),
        sceltaUnita("adjnom-04", "Das ___ an der Reise ist das Wetter.", listOf("Schöne", "Schönen", "Schöner", "Schönes"), 0, "Nominativo neutro: das Schöne."),
        sceltaUnita("adjnom-05", "Der ___ arbeitet im Büro.", listOf("Angestellte", "Angestellten", "Angestellter", "Angestelltes"), 0, "Nominativo maschile: der Angestellte."),
        produciUnita("b1-adjnomen-prod", "„La cosa più importante è la salute.“", "Das Wichtigste ist die Gesundheit.", "das Wichtigste: aggettivo sostantivato neutro, con la maiuscola."),
    )
)

internal fun unitaB1Praepositionaladverbien(): UnitaGrammatica = unitaGrammatica(
    id = "b1-praepositionaladverbien",
    livello = "B1",
    titolo = "Avverbiali preposizionali",
    sottotitolo = "Damit, darauf, worüber: riferimento a una cosa o a una frase",
    obiettivo = "Usare i pronomi avverbiali (da- + prep, wo- + prep) per evitare ripetizioni.",
    minuti = 12,
    prerequisiti = listOf("a2-subordinate"),
    regola = listOf(
        "Il pronome avverbiale (Präpositionaladverb) = da(r)-/wo(r)- + preposizione: dafür, darauf, damit, worüber, wofür, woran.",
        "da- + preposizione per le affermazioni: Ich denke daran. (= Ich denke an das Problem.)",
        "wo- + preposizione per le domande: Worauf wartest du? (= Auf was wartest du?)",
        "Se la preposizione inizia con vocale, si usa dar-/wor-: darauf, worauf, daran, woran.",
        "Sostituiscono 'preposizione + es/esso': Ich denke an das Problem → Ich denke daran.",
        "Si riferiscono a una cosa o a un'intera frase precedente, evitando ripetizioni."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "da- + preposizione (affermazioni)",
            intestazioni = listOf("Preposizione", "Pronome avverbiale", "Esempio"),
            righe = listOf(
                listOf("an", "daran", "Ich denke daran."),
                listOf("auf", "darauf", "Ich warte darauf."),
                listOf("mit", "damit", "Er spielt damit."),
                listOf("über", "darüber", "Sie spricht darüber."),
                listOf("für", "dafür", "Das ist dafür.")
            )
        ),
        tabellaUnita(
            titolo = "wo- + preposizione (domande)",
            intestazioni = listOf("Preposizione", "Pronome avverbiale", "Esempio"),
            righe = listOf(
                listOf("an", "woran", "Woran denkst du?"),
                listOf("auf", "worauf", "Worauf wartest du?"),
                listOf("über", "worüber", "Worüber spricht sie?"),
                listOf("für", "wofür", "Wofür brauchst du das?")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich denke an das Problem. → Ich denke daran.", "Penso al problema. → Ci penso."),
        esempioUnita("Er spricht über das Buch. → Er spricht darüber.", "Parla del libro. → Ne parla."),
        esempioUnita("Worauf wartest du? — Auf den Bus.", "Cosa aspetti? — L'autobus."),
        esempioUnita("Worüber spricht sie? — Über das Buch.", "Di cosa parla? — Del libro.")
    ),
    errori = listOf(
        erroreUnita("Ich denke an es.", "Ich denke daran.", "Con preposizione + es si usa il pronome avverbiale: daran."),
        erroreUnita("Über was sprichst du?", "Worüber sprichst du?", "In tedesco standard si usa worüber, non 'über was'."),
        erroreUnita("Ich denke daran das Problem.", "Ich denke daran. / Ich denke an das Problem.", "Non si mescolano: o daran (senza nome) o preposizione + nome.")
    ),
    merksatz = "da(r)- per le affermazioni, wo(r)- per le domande; sostituiscono 'preposizione + es'.",
    esercizi = listOf(
        sceltaUnita("pradv-01", "Ich denke ___ (an das Problem).", listOf("daran", "darauf", "damit", "dafür"), 0, "denken an → daran."),
        sceltaUnita("pradv-02", "Er spricht ___ (über das Buch).", listOf("daran", "darauf", "darüber", "dafür"), 2, "sprechen über → darüber."),
        sceltaUnita("pradv-03", "___ wartest du? — Auf den Bus.", listOf("Woran", "Worauf", "Worüber", "Wofür"), 1, "warten auf → worauf."),
        sceltaUnita("pradv-04", "___ spricht sie? — Über das Buch.", listOf("Woran", "Worauf", "Worüber", "Wofür"), 2, "sprechen über → worüber."),
        sceltaUnita("pradv-05", "Ich warte ___ (auf den Bus).", listOf("darauf", "daran", "damit", "dafür"), 0, "warten auf → darauf."),
        produciUnita("b1-praepadv-prod", "„Ci penso ogni giorno.“", "Ich denke jeden Tag daran.", "denken an + cosa → pronome avverbiale daran."),
    )
)

internal fun unitaB1Komparativsaetze(): UnitaGrammatica = unitaGrammatica(
    id = "b1-komparativsaetze",
    livello = "B1",
    titolo = "Frasi comparative",
    sottotitolo = "Je ... desto, als ob, als wenn",
    obiettivo = "Esprimere proporzionalità, ipotesi e confronto con strutture comparative avanzate.",
    minuti = 12,
    prerequisiti = listOf("a2-komparativ"),
    regola = listOf(
        "je ... desto/umso: proporzionalità (più ... più): Je mehr ich lerne, desto besser verstehe ich.",
        "In entrambe le frasi il verbo coniugato va alla fine.",
        "als ob / als wenn: come se (ipotesi irrealizzabile): Er tut, als ob er nichts wüsste. → Konjunktiv II nell'ultima frase.",
        "als (con comparativo): confronto: Er ist größer, als ich dachte.",
        "wie (con comparativo): uguaglianza: Er ist so groß wie ich.",
        "je nachdem: a seconda di: Je nachdem, wie viel Zeit wir haben, gehen wir ins Kino."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Strutture comparative",
            intestazioni = listOf("Struttura", "Significato", "Esempio"),
            righe = listOf(
                listOf("je ... desto/umso", "più ... più", "Je mehr ich lerne, desto besser verstehe ich."),
                listOf("als ob / als wenn", "come se", "Er tut, als ob er nichts wüsste."),
                listOf("als", "che (con comparativo)", "Er ist größer, als ich dachte."),
                listOf("so ... wie", "tanto ... quanto", "Er ist so groß wie ich."),
                listOf("je nachdem", "a seconda di", "Je nachdem, wie viel Zeit wir haben.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Je mehr ich übe, desto flüssiger spreche ich.", "Più mi esercito, più scorrevole parlo."),
        esempioUnita("Er tut, als ob er krank wäre.", "Fa come se fosse malato."),
        esempioUnita("Er ist größer, als ich dachte.", "È più grande di quanto pensassi."),
        esempioUnita("Sie ist so klug wie ihre Schwester.", "È tanto intelligente quanto sua sorella."),
        esempioUnita("Je nachdem, wie spät es ist, gehen wir ins Kino.", "A seconda di come è tardi, andiamo al cinema.")
    ),
    errori = listOf(
        erroreUnita("Je mehr ich lerne, desto besser ich verstehe.", "Je mehr ich lerne, desto besser verstehe ich.", "In entrambe le frasi il verbo va alla fine."),
        erroreUnita("Er tut, als ob er krank ist.", "Er tut, als ob er krank wäre.", "Dopo als ob si usa il Konjunktiv II."),
        erroreUnita("Er ist größer als ich denke.", "Er ist größer, als ich dachte.", "Confronto al passato: als ich dachte (Präteritum).")
    ),
    merksatz = "je ... desto: verbo alla fine in entrambe le frasi; als ob → Konjunktiv II.",
    esercizi = listOf(
        sceltaUnita("b1komp-01", "Je mehr ich ___, desto besser verstehe ich.", listOf("lerne", "gelernt habe", "lernt", "gelernt hatte"), 0, "je ... desto: verbo alla fine: lerne."),
        sceltaUnita("b1komp-02", "Er tut, als ob er nichts ___.", listOf("weiß", "wüsste", "gewusst hat", "gewusst hätte"), 1, "als ob → Konjunktiv II: wüsste."),
        sceltaUnita("b1komp-03", "Er ist größer, ___ ich dachte.", listOf("als", "wie", "desto", "umso"), 0, "Confronto: größer als."),
        sceltaUnita("b1komp-04", "Sie ist so klug ___ ihre Schwester.", listOf("als", "wie", "desto", "umso"), 1, "Uguaglianza: so ... wie."),
        sceltaUnita("b1komp-05", "Je nachdem, ___ viel Zeit wir haben.", listOf("wie", "als", "ob", "wenn"), 0, "je nachdem + wie."),
        produciUnita("b1-komp-prod", "„Più leggo, meglio capisco.“", "Je mehr ich lese, desto besser verstehe ich.", "Confronto proporzionale: je + comparativo ... desto + comparativo."),
    )
)
