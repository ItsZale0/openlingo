package com.alessandro.openlingo.data

/**
 * Unità B2 del percorso grammaticale (versione 1.43.3).
 *
 * Il percorso arriva così fino al B2, il livello obiettivo di Emma.
 * Ogni unità mantiene la stessa struttura delle precedenti: teoria, tabelle,
 * esempi, errori tipici, Merksatz e 8 esercizi. Gli ID sono stabili perché
 * sono le chiavi della padronanza salvata nel profilo.
 */

internal fun unitaB2PassivModal(): UnitaGrammatica = unitaGrammatica(
    id = "b2-passiv-modal",
    livello = "B2",
    titolo = "Passivo con i modali",
    sottotitolo = "Das muss gemacht werden",
    obiettivo = "Esprimere obblighi e possibilità al passivo con i verbi modali.",
    minuti = 18,
    prerequisiti = listOf("a2-passivo", "b1-futur"),
    regola = listOf(
        "Nel passivo con modale la frase ha **due infiniti in fondo**: il modale coniugato in seconda posizione e `Partizip II + werden` alla fine.",
        "Schema: **Subjekt + Modalverb + ... + Partizip II + werden**. Es: Der Antrag muss ausgefüllt werden.",
        "Il soggetto della frase passiva è ciò che subisce l'azione; l'agente, se espresso, va con **von + dativo**.",
        "Con i modali il passivo si usa molto nel linguaggio amministrativo e sanitario: Die Tabletten müssen zweimal täglich genommen werden.",
        "Al passato: **modalverb al Präteritum + Partizip II + werden**: Das Formular musste ausgefüllt werden.",
        "Con **nicht** la negazione sta prima di Partizip II + werden: Das darf nicht gemacht werden.",
        "Attenzione all'ordine: il modale NON va in fondo e `werden` resta all'infinito."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Presente: müssen + passivo",
            intestazioni = listOf("Persona", "Forma", "Esempio"),
            righe = listOf(
                listOf("ich", "muss ... werden", "Ich muss untersucht werden."),
                listOf("du", "musst ... werden", "Du musst untersucht werden."),
                listOf("er/sie/es", "muss ... werden", "Er muss untersucht werden."),
                listOf("wir", "müssen ... werden", "Wir müssen untersucht werden."),
                listOf("ihr", "müsst ... werden", "Ihr müsst untersucht werden."),
                listOf("sie/Sie", "müssen ... werden", "Sie müssen untersucht werden.")
            )
        ),
        tabellaUnita(
            titolo = "Modali frequenti al passivo",
            intestazioni = listOf("Modale", "Significato", "Esempio"),
            righe = listOf(
                listOf("müssen", "dovere", "Die Rechnung muss bezahlt werden."),
                listOf("können", "potere", "Der Termin kann verschoben werden."),
                listOf("dürfen", "essere permesso", "Hier darf nicht geraucht werden."),
                listOf("sollen", "dovere (indicazione)", "Die Tablette soll morgens genommen werden.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Der Antrag muss ausgefüllt werden.", "Il modulo deve essere compilato."),
        esempioUnita("Die Patienten müssen regelmäßig untersucht werden.", "I pazienti devono essere visitati regolarmente."),
        esempioUnita("Hier darf nicht geraucht werden.", "Qui non è permesso fumare."),
        esempioUnita("Der Termin konnte leider nicht verschoben werden.", "L'appuntamento purtroppo non ha potuto essere spostato."),
        esempioUnita("Die Rechnung soll bis Freitag bezahlt werden.", "La fattura deve essere pagata entro venerdì.")
    ),
    errori = listOf(
        erroreUnita(
            "Das Formular muss ausgefüllt wird.",
            "Der Antrag muss ausgefüllt werden.",
            "Dopo il modale il passivo resta all'infinito: werden, non wird."
        ),
        erroreUnita(
            "Das Formular muss werden ausgefüllt.",
            "Der Antrag muss ausgefüllt werden.",
            "Partizip II prima di werden, entrambi in fondo."
        ),
        erroreUnita(
            "Das darf gemacht nicht werden.",
            "Das darf nicht gemacht werden.",
            "nicht precede il blocco Partizip II + werden."
        )
    ),
    merksatz = "Modale + ... + Partizip II + werden. Due infiniti in fondo, modale in seconda posizione.",
    esercizi = listOf(
        sceltaUnita(
            "b2pm-01",
            "Das Formular ___ ausgefüllt werden.",
            listOf("muss", "musst", "müssen", "müsst"),
            0,
            "Soggetto neutro singolare (das Formular): muss."
        ),
        sceltaUnita(
            "b2pm-02",
            "Die Tabletten müssen zweimal täglich ___ werden.",
            listOf("nehmen", "genommen", "genommenen", "nimmt"),
            1,
            "Al passivo serve il Partizip II: genommen."
        ),
        sceltaUnita(
            "b2pm-03",
            "Hier ___ nicht geraucht werden.",
            listOf("darf", "dürfen", "darfst", "dürfte"),
            0,
            "Soggetto impersonale-es: hier darf nicht geraucht werden."
        ),
        sceltaUnita(
            "b2pm-04",
            "Der Termin ___ leider nicht verschoben werden.",
            listOf("konnte", "kann", "gekonnt", "können"),
            0,
            "Al passato il modale va al Präteritum: konnte."
        ),
        completaUnita(
            "b2pm-05",
            "Die Rechnung muss bis Freitag ___ werden. (bezahlen)",
            "bezahlt",
            "Partizip II di bezahlen: bezahlt, senza ge-."
        ),
        completaUnita(
            "b2pm-06",
            "Die Patienten müssen regelmäßig ___ werden. (untersuchen)",
            "untersucht",
            "Partizip II di untersuchen: untersucht, senza ge-."
        ),
        trasformaUnita(
            "b2pm-07",
            "Man muss das Formular ausfüllen. (passivo con modale)",
            "Das Formular muss ausgefüllt werden.",
            "Il complemento oggetto diventa soggetto: Der Antrag muss ausgefüllt werden."
        ),
        produciUnita(
            "b2pm-08",
            "Traduci: I documenti devono essere firmati.",
            "Die Dokumente müssen unterschrieben werden.",
            "müssen + unterschrieben + werden.",
            alternative = listOf("die dokumente müssen unterschrieben werden")
        )
    )
)

internal fun unitaB2Konjunktiv2Vergangenheit(): UnitaGrammatica = unitaGrammatica(
    id = "b2-konjunktiv2-vergangenheit",
    livello = "B2",
    titolo = "Konjunktiv II passato",
    sottotitolo = "Hätte ich das gewusst ...",
    obiettivo = "Esprimere rimpianti e ipotesi irreali sul passato.",
    minuti = 18,
    prerequisiti = listOf("b1-konjunktiv2"),
    regola = listOf(
        "Il Konjunktiv II passato si forma con **hätte/wäre + Partizip II**: Ich hätte mehr gelernt.",
        "Usa **hätte** con la maggior parte dei verbi; usa **wäre** con i verbi di movimento e cambiamento di stato (gehen, fahren, kommen, bleiben, werden).",
        "Esprime un'ipotesi **irreale nel passato** o un rimpianto: Wenn ich Zeit gehabt hätte, wäre ich gekommen.",
        "Nel periodo ipotetico il verbo della subordinata va in fondo: Wenn ich das gewusst hätte, ...",
        "La proposizione principale può iniziare con il verbo: Hätte ich das gewusst, wäre ich geblieben.",
        "I modali al Konjunktiv II passato usano l'infinito: Ich hätte das machen können.",
        "Non si usa il Konjunktiv II passato per fatti reali: per il passato reale si usa il Perfekt."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "hätte / wäre + Partizip II",
            intestazioni = listOf("Persona", "hätte", "wäre"),
            righe = listOf(
                listOf("ich", "hätte", "wäre"),
                listOf("du", "hättest", "wärst"),
                listOf("er/sie/es", "hätte", "wäre"),
                listOf("wir", "hätten", "wären"),
                listOf("ihr", "hättet", "wärt"),
                listOf("sie/Sie", "hätten", "wären")
            )
        ),
        tabellaUnita(
            titolo = "Quale ausiliare?",
            intestazioni = listOf("Verbo", "Konjunktiv II passato"),
            righe = listOf(
                listOf("lernen", "ich hätte gelernt"),
                listOf("machen", "ich hätte gemacht"),
                listOf("gehen", "ich wäre gegangen"),
                listOf("fahren", "ich wäre gefahren"),
                listOf("bleiben", "ich wäre geblieben"),
                listOf("können", "ich hätte ... können")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Wenn ich das gewusst hätte, wäre ich geblieben.", "Se l'avessi saputo, sarei rimasto."),
        esempioUnita("Ich hätte mehr lernen sollen.", "Avrei dovuto studiare di più."),
        esempioUnita("Wir wären früher gefahren, aber es hat geregnet.", "Saremmo partiti prima, ma ha piovuto."),
        esempioUnita("Hätte ich Zeit gehabt, hätte ich dir geholfen.", "Se avessi avuto tempo, ti avrei aiutato."),
        esempioUnita("Sie wäre gerne Ärztin geworden.", "Le sarebbe piaciuto diventare medico.")
    ),
    errori = listOf(
        erroreUnita(
            "Wenn ich das gewusst habe, wäre ich gekommen.",
            "Wenn ich das gewusst hätte, wäre ich geblieben.",
            "L'ipotesi irreale vuole il Konjunktiv II: hätte gewusst."
        ),
        erroreUnita(
            "Ich hätte nach Berlin gefahren.",
            "Ich wäre nach Berlin gefahren.",
            "fahren esprime movimento: ausiliare wäre."
        ),
        erroreUnita(
            "Ich hätte das machen gekonnt.",
            "Ich hätte das machen können.",
            "Con i modali resta l'infinito: können."
        )
    ),
    merksatz = "hätte/wäre + Partizip II. Movimento → wäre. Modali → infinito in fondo.",
    esercizi = listOf(
        sceltaUnita(
            "b2kv-01",
            "Wenn ich Zeit ___, wäre ich gekommen.",
            listOf("gehabt hätte", "hatte", "habe", "hätte gehabt"),
            0,
            "Subordinata con verbo in fondo: gehabt hätte."
        ),
        sceltaUnita(
            "b2kv-02",
            "Wir ___ früher gefahren.",
            listOf("hätten", "wären", "waren", "hatten"),
            1,
            "fahren è movimento: wir wären gefahren."
        ),
        sceltaUnita(
            "b2kv-03",
            "Ich ___ mehr lernen sollen.",
            listOf("hätte", "wäre", "hatte", "war"),
            0,
            "lernen usa haben: ich hätte lernen sollen."
        ),
        sceltaUnita(
            "b2kv-04",
            "Sie ___ gerne Ärztin geworden.",
            listOf("hätte", "wäre", "hatte", "war"),
            1,
            "werden esprime cambiamento di stato: wäre geworden."
        ),
        completaUnita(
            "b2kv-05",
            "Wenn ich das gewusst ___, wäre ich geblieben. (haben)",
            "hätte",
            "Konjunktiv II di haben alla terza persona singolare: hätte."
        ),
        completaUnita(
            "b2kv-06",
            "Ich ___ dir geholfen. (haben)",
            "hätte",
            "helfen usa haben: ich hätte geholfen."
        ),
        trasformaUnita(
            "b2kv-07",
            "Ich habe das nicht gewusst. (Konjunktiv II passato)",
            "Ich hätte das gewusst.",
            "Trasforma il fatto reale in ipotesi: hätte gewusst."
        ),
        produciUnita(
            "b2kv-08",
            "Traduci: Se avessi tempo, verrei.",
            "Wenn ich Zeit hätte, würde ich kommen.",
            "Ipotesi irreale nel presente: hätte + würde + infinito.",
            alternative = listOf("wenn ich zeit hätte, käme ich", "wenn ich zeit hätte, würde ich kommen")
        )
    )
)

internal fun unitaB2Partizipialkonstruktionen(): UnitaGrammatica = unitaGrammatica(
    id = "b2-partizipialkonstruktionen",
    livello = "B2",
    titolo = "Costruzioni participiali",
    sottotitolo = "Die gestern bestellte Ware",
    obiettivo = "Riconoscere e usare il participio attributivo al posto delle relative.",
    minuti = 20,
    prerequisiti = listOf("b1-adjektivdeklination", "b1-partizipien"),
    regola = listOf(
        "Una frase relativa può essere compressa in un **participio attributivo**: Die Ware, die gestern bestellt wurde → Die gestern bestellte Ware.",
        "Il participio si comporta come un aggettivo e prende la **desinenza** dell'articolo che lo precede.",
        "Participio presente (**Partizip I**): infinito + d, con valore attivo: der schlafende Patient (il paziente che dorme).",
        "Participio passato (**Partizip II**) con valore passivo: die bestellte Ware (la merce che è stata ordinata).",
        "Le informazioni aggiuntive stanno **prima** del participio: die gestern vom Arzt untersuchte Patientin.",
        "Con i verbi separabili il participio resta unito: die eingekaufte Ware.",
        "Le costruzioni participiali sono tipiche dello stile scritto e giornalistico; nel parlato si preferisce la relativa."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Da relativa a participio",
            intestazioni = listOf("Frase relativa", "Participio attributivo"),
            righe = listOf(
                listOf("die Ware, die bestellt wurde", "die bestellte Ware"),
                listOf("der Patient, der schläft", "der schlafende Patient"),
                listOf("das Formular, das ausgefüllt wurde", "das ausgefüllte Formular"),
                listOf("die Rechnung, die gestern kam", "die gestern gekommene Rechnung")
            )
        ),
        tabellaUnita(
            titolo = "Desinenze (nominativo)",
            intestazioni = listOf("Genere", "Con articolo definito", "Senza articolo"),
            righe = listOf(
                listOf("maschile", "der bestellte", "bestellter"),
                listOf("femminile", "die bestellte", "bestellte"),
                listOf("neutro", "das bestellte", "bestelltes"),
                listOf("plurale", "die bestellten", "bestellte")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Die gestern bestellte Ware kommt morgen an.", "La merce ordinata ieri arriva domani."),
        esempioUnita("Der schlafende Patient darf nicht gestört werden.", "Il paziente che dorme non deve essere disturbato."),
        esempioUnita("Das von der Ärztin ausgefüllte Formular liegt auf dem Tisch.", "Il modulo compilato dalla dottoressa è sul tavolo."),
        esempioUnita("Die eingekauften Medikamente sind teuer.", "I medicinali acquistati sono costosi."),
        esempioUnita("Der zu operierende Patient ist vorbereitet.", "Il paziente da operare è pronto.")
    ),
    errori = listOf(
        erroreUnita(
            "Die Ware, die gestern bestellt wurde, kommt morgen an. (participio)",
            "Die gestern bestellte Ware kommt morgen an.",
            "La relativa si comprime in participio attributivo."
        ),
        erroreUnita(
            "Die bestellt Ware",
            "Die bestellte Ware",
            "Il participio attributivo prende la desinenza dell'aggettivo."
        ),
        erroreUnita(
            "Der schlafend Patient",
            "Der schlafende Patient",
            "Participio presente + desinenza: schlafende."
        )
    ),
    merksatz = "Relativa → participio + desinenza davanti al nome. Partizip I = attivo, Partizip II = passivo.",
    esercizi = listOf(
        sceltaUnita(
            "b2pk-01",
            "Die gestern ___ Ware kommt morgen an.",
            listOf("bestellt", "bestellte", "bestellten", "bestellen"),
            1,
            "Femminile singolare con articolo definito: bestellte."
        ),
        sceltaUnita(
            "b2pk-02",
            "Der ___ Patient darf nicht gestört werden.",
            listOf("schlafend", "schlafende", "schlafender", "schlafen"),
            1,
            "Maschile singolare nominativo con articolo definito: schlafende."
        ),
        sceltaUnita(
            "b2pk-03",
            "Das von der Ärztin ___ Formular liegt auf dem Tisch.",
            listOf("ausgefüllt", "ausgefüllte", "ausgefüllten", "ausfüllen"),
            1,
            "Neutro singolare nominativo: ausgefüllte."
        ),
        sceltaUnita(
            "b2pk-04",
            "Die ___ Medikamente sind teuer.",
            listOf("eingekauft", "eingekaufte", "eingekauften", "einkaufen"),
            2,
            "Plurale con articolo definito: eingekauften."
        ),
        completaUnita(
            "b2pk-05",
            "Der Mann, der arbeitet → der ___ Mann. (arbeiten, Partizip I)",
            "arbeitende",
            "Participio presente + desinenza: arbeitende."
        ),
        completaUnita(
            "b2pk-06",
            "Das Formular, das ausgefüllt wurde → das ___ Formular. (ausfüllen, Partizip II)",
            "ausgefüllte",
            "Participio passato + desinenza: ausgefüllte."
        ),
        trasformaUnita(
            "b2pk-07",
            "Die Rechnung, die gestern kam, ist bezahlt. (participio attributivo)",
            "Die gestern gekommene Rechnung ist bezahlt.",
            "La relativa diventa: die gestern gekommene Rechnung."
        ),
        produciUnita(
            "b2pk-08",
            "Traduci: I pazienti visitati ieri stanno bene.",
            "Die gestern untersuchten Patienten sind gesund.",
            "Participio attributivo plurale: die gestern untersuchten Patienten.",
            alternative = listOf("die gestern untersuchten patienten sind gesund")
        )
    )
)

internal fun unitaB2VerbenPraepositionen(): UnitaGrammatica = unitaGrammatica(
    id = "b2-verben-praepositionen",
    livello = "B2",
    titolo = "Verbi con preposizione",
    sottotitolo = "warten auf, sich freuen über, denken an",
    obiettivo = "Usare il verbo giusto con la preposizione e il caso corretti.",
    minuti = 18,
    prerequisiti = listOf("a1-akkusativ", "a2-dativo"),
    regola = listOf(
        "Molti verbi tedeschi reggono una **preposizione fissa** che non si traduce parola per parola: warten **auf** + accusativo, helfen **bei** + dativo.",
        "La preposizione può essere **accusativa** (warten auf, sich freuen auf, denken an, sich interessieren für) o **dativa** (helfen bei, sich beschäftigen mit, danken für + accusativo).",
        "Con le persone si usano spesso i **pronomi preposizionali**: darauf, daran, damit, davon, womit, worauf.",
        "Per cose si usa **da-/wo- + preposizione**: Ich warte darauf. Worauf wartest du?",
        "Per persone si usa **preposizione + pronome**: Ich warte auf dich. Ich denke an sie.",
        "Alcune coppie frequenti: sich freuen **auf** (attesa) / sich freuen **über** (cosa già avvenuta), denken **an**, sprechen **über** / **von**, sich erinnern **an**.",
        "Altre coppie frequenti: sich bewerben **um** + accusativo, abhängen **von** + dativo (separabile: hängt ... ab).",
        "La preposizione non cambia con il tempo verbale: Ich habe auf dich gewartet."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Verbi e preposizioni frequenti",
            intestazioni = listOf("Verbo", "Preposizione", "Caso", "Esempio"),
            righe = listOf(
                listOf("warten", "auf", "accusativo", "Ich warte auf den Bus."),
                listOf("sich freuen", "auf", "accusativo", "Ich freue mich auf den Urlaub."),
                listOf("denken", "an", "accusativo", "Ich denke an dich."),
                listOf("sich interessieren", "für", "accusativo", "Er interessiert sich für Medizin."),
                listOf("helfen", "bei", "dativo", "Sie hilft mir bei der Arbeit."),
                listOf("sich beschäftigen", "mit", "dativo", "Wir beschäftigen uns mit dem Thema."),
                listOf("danken", "für", "accusativo", "Ich danke dir für die Hilfe."),
                listOf("sich bewerben", "um", "accusativo", "Sie bewirbt sich um die Stelle."),
                listOf("abhängen", "von", "dativo", "Die Entscheidung hängt vom Wetter ab.")
            )
        ),
        tabellaUnita(
            titolo = "Pronomi preposizionali",
            intestazioni = listOf("Cosa", "Domanda", "Risposta"),
            righe = listOf(
                listOf("auf etwas warten", "Worauf wartest du?", "Ich warte darauf."),
                listOf("an etwas denken", "Woran denkst du?", "Ich denke daran."),
                listOf("mit etwas beginnen", "Womit beginnst du?", "Ich beginne damit.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich warte auf den Arzt.", "Aspetto il medico."),
        esempioUnita("Sie freut sich auf das Wochenende.", "Non vede l'ora che arrivi il fine settimana."),
        esempioUnita("Er interessiert sich für Physiotherapie.", "Si interessa di fisioterapia."),
        esempioUnita("Ich danke dir für die Hilfe.", "Ti ringrazio per l'aiuto."),
        esempioUnita("Worauf wartest du? — Ich warte auf den Bus.", "Cosa aspetti? — Aspetto l'autobus."),
        esempioUnita("Denkst du oft an deine Familie?", "Pensi spesso alla tua famiglia?")
    ),
    errori = listOf(
        erroreUnita(
            "Ich warte für den Bus.",
            "Ich warte auf den Bus.",
            "warten regge auf + accusativo, non für."
        ),
        erroreUnita(
            "Ich interessiere mich über Medizin.",
            "Ich interessiere mich für Medizin.",
            "sich interessieren regge für + accusativo."
        ),
        erroreUnita(
            "Ich warte auf ihn. (per una cosa)",
            "Ich warte darauf.",
            "Per le cose si usa il pronome preposizionale: darauf."
        )
    ),
    merksatz = "Ogni verbo ha la sua preposizione. Cose → da-/wo- + preposizione; persone → preposizione + pronome.",
    esercizi = listOf(
        sceltaUnita(
            "b2vp-01",
            "Wir warten ___ den Arzt.",
            listOf("für", "auf", "an", "über"),
            1,
            "warten auf + accusativo."
        ),
        sceltaUnita(
            "b2vp-02",
            "Sie interessiert sich ___ Physiotherapie.",
            listOf("für", "auf", "an", "mit"),
            0,
            "sich interessieren für + accusativo."
        ),
        sceltaUnita(
            "b2vp-03",
            "Er hilft mir ___ der Arbeit.",
            listOf("für", "bei", "auf", "über"),
            1,
            "helfen bei + dativo."
        ),
        sceltaUnita(
            "b2vp-04",
            "Ich denke oft ___ meine Familie.",
            listOf("an", "auf", "für", "über"),
            0,
            "denken an + accusativo."
        ),
        completaUnita(
            "b2vp-05",
            "Worauf wartest du? — Ich warte ___. (pronome per cosa)",
            "darauf",
            "Per una cosa si usa darauf."
        ),
        completaUnita(
            "b2vp-06",
            "Ich danke dir ___ die Hilfe. (preposizione)",
            "für",
            "danken für + accusativo."
        ),
        trasformaUnita(
            "b2vp-07",
            "Ich freue mich auf den Urlaub. (domanda con worauf)",
            "Worauf freust du dich?",
            "La preposizione si unisce a wo-: worauf."
        ),
        produciUnita(
            "b2vp-08",
            "Traduci: Aspetto una risposta.",
            "Ich warte auf eine Antwort.",
            "warten auf + accusativo: auf eine Antwort.",
            alternative = listOf("ich warte auf eine antwort")
        )
    )
)

internal fun unitaB2RelativeWasWo(): UnitaGrammatica = unitaGrammatica(
    id = "b2-relative-was-wo",
    livello = "B2",
    titolo = "Relative con was, wo e wer",
    sottotitolo = "Das, was ich meine · Die Stadt, wo ich lebe",
    obiettivo = "Usare was, wo e wer nelle frasi relative in modo corretto.",
    minuti = 17,
    prerequisiti = listOf("a2-relative", "b1-genitiv"),
    regola = listOf(
        "**was** si usa dopo i pronomi indefiniti (alles, etwas, nichts, vieles), dopo i superlativi (das Beste) e dopo un'intera frase: Das ist alles, was ich weiß.",
        "**wo** sostituisce un complemento di luogo: Die Stadt, wo ich lebe. È comune nel parlato; nello scritto si preferisce in der ich lebe.",
        "**wer** introduce una relativa senza antecedente con valore di 'chi': Wer viel lernt, versteht mehr.",
        "**wo** può anche sostituire espressioni temporali: In dem Moment, wo er kam.",
        "Dopo **was** il verbo va in fondo alla frase, come in ogni relativa.",
        "Se il pronome relativo è soggetto, il verbo concorda: Die Leute, die hier arbeiten.",
        "Non si usa **was** dopo un nome concreto: *Das Buch, was ich lese* è sbagliato → Das Buch, das ich lese."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Quale pronome relativo?",
            intestazioni = listOf("Antecedente", "Pronome", "Esempio"),
            righe = listOf(
                listOf("alles / etwas / nichts", "was", "Das ist alles, was ich habe."),
                listOf("das Beste / das Einzige", "was", "Das Beste, was passieren kann."),
                listOf("intera frase", "was", "Er kam zu spät, was mich ärgerte."),
                listOf("luogo", "wo", "Die Stadt, wo ich wohne."),
                listOf("senza antecedente (chi)", "wer", "Wer nicht fragt, lernt nichts."),
                listOf("nome concreto", "der/die/das", "Das Buch, das ich lese.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Das ist alles, was ich darüber weiß.", "È tutto quello che so a riguardo."),
        esempioUnita("Die Stadt, wo ich aufgewachsen bin, ist klein.", "La città dove sono cresciuto è piccola."),
        esempioUnita("Wer fleißig übt, wird besser.", "Chi si esercita con impegno migliora."),
        esempioUnita("Er hat die Prüfung bestanden, was alle freut.", "Ha superato l'esame, cosa che rallegra tutti."),
        esempioUnita("Das Einzige, was mir fehlt, ist Zeit.", "L'unica cosa che mi manca è il tempo.")
    ),
    errori = listOf(
        erroreUnita(
            "Das Buch, was ich lese, ist gut.",
            "Das Buch, das ich lese, ist gut.",
            "Dopo un nome concreto si usa der/die/das, non was."
        ),
        erroreUnita(
            "Alles, das ich weiß.",
            "Alles, was ich weiß.",
            "Dopo alles si usa was."
        ),
        erroreUnita(
            "Wer viel lernt, versteht mehr. (verbo in seconda posizione nella relativa)",
            "Wer viel lernt, versteht mehr.",
            "La relativa con wer ha il verbo in fondo; la principale resta in seconda posizione."
        )
    ),
    merksatz = "was dopo alles/etwas/nichts e frasi intere; wo per il luogo; wer per 'chi' senza antecedente.",
    esercizi = listOf(
        sceltaUnita(
            "b2rw-01",
            "Das ist alles, ___ ich weiß.",
            listOf("das", "was", "wo", "wer"),
            1,
            "Dopo alles si usa was."
        ),
        sceltaUnita(
            "b2rw-02",
            "Das Buch, ___ auf dem Tisch liegt, gehört mir.",
            listOf("was", "das", "wo", "wer"),
            1,
            "Nome concreto neutro: das."
        ),
        sceltaUnita(
            "b2rw-03",
            "Die Stadt, ___ ich wohne, ist ruhig.",
            listOf("was", "das", "wo", "wer"),
            2,
            "Complemento di luogo: wo."
        ),
        sceltaUnita(
            "b2rw-04",
            "___ nicht fragt, lernt nichts.",
            listOf("Was", "Wo", "Wer", "Das"),
            2,
            "Relativa senza antecedente con valore di 'chi': wer."
        ),
        completaUnita(
            "b2rw-05",
            "Das Einzige, ___ mir fehlt, ist Zeit.",
            "was",
            "Dopo das Einzige si usa was."
        ),
        completaUnita(
            "b2rw-06",
            "Er kam zu spät, ___ mich ärgerte.",
            "was",
            "La relativa che riprende un'intera frase usa was."
        ),
        trasformaUnita(
            "b2rw-07",
            "Ich wohne in einer Stadt. Die Stadt ist groß. (relativa con wo)",
            "Die Stadt, wo ich wohne, ist groß.",
            "Unisci con wo: Die Stadt, wo ich wohne, ist groß."
        ),
        produciUnita(
            "b2rw-08",
            "Traduci: Chi lavora molto, ha poco tempo.",
            "Wer viel arbeitet, hat wenig Zeit.",
            "Relativa con wer: Wer viel arbeitet, ...",
            alternative = listOf("wer viel arbeitet, hat wenig zeit")
        )
    )
)

internal fun unitaB2Nominalstil(): UnitaGrammatica = unitaGrammatica(
    id = "b2-nominalstil",
    livello = "B2",
    titolo = "Stile nominale",
    sottotitolo = "Das Lesen, die Entscheidung, die Verbesserung",
    obiettivo = "Trasformare frasi verbali in espressioni nominali tipiche del B2.",
    minuti = 20,
    prerequisiti = listOf("b1-nominalisierung"),
    regola = listOf(
        "Lo stile nominale sostituisce un verbo con un **nome**: entscheiden → die Entscheidung, lesen → das Lesen.",
        "I nomi in **-ung** derivano da verbi e sono femminili: die Verbesserung, die Behandlung, die Entscheidung.",
        "L'infinito sostantivato è neutro: **das Lesen**, **das Sprechen**, **das Warten**.",
        "I nomi in **-heit/-keit** derivano da aggettivi e sono femminili: die Gesundheit, die Möglichkeit.",
        "Nello stile nominale le preposizioni sostituiscono le congiunzioni: **wegen** (perché), **trotz** (benché), **während** (mentre), **nach** (dopo).",
        "Le espressioni nominali rendono il testo più formale e compatto, tipico di lettere, referti e testi B2.",
        "Non abusare dello stile nominale nel parlato: suona rigido. Usalo nello scritto formale."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Verbo → nome",
            intestazioni = listOf("Verbo", "Nome", "Genere"),
            righe = listOf(
                listOf("behandeln", "die Behandlung", "femminile"),
                listOf("entscheiden", "die Entscheidung", "femminile"),
                listOf("verbessern", "die Verbesserung", "femminile"),
                listOf("lesen", "das Lesen", "neutro"),
                listOf("gesund sein", "die Gesundheit", "femminile"),
                listOf("möglich sein", "die Möglichkeit", "femminile")
            )
        ),
        tabellaUnita(
            titolo = "Congiunzione → preposizione",
            intestazioni = listOf("Verbale", "Nominale"),
            righe = listOf(
                listOf("weil er krank ist", "wegen seiner Krankheit"),
                listOf("obwohl es regnet", "trotz des Regens"),
                listOf("während ich warte", "während des Wartens"),
                listOf("nachdem er gekommen ist", "nach seiner Ankunft")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Die Behandlung des Patienten dauert zwei Wochen.", "Il trattamento del paziente dura due settimane."),
        esempioUnita("Wegen seiner Krankheit konnte er nicht kommen.", "A causa della sua malattia non ha potuto venire."),
        esempioUnita("Trotz des Regens gehen wir spazieren.", "Nonostante la pioggia andiamo a passeggio."),
        esempioUnita("Das Lesen hilft beim Lernen.", "La lettura aiuta nell'apprendimento."),
        esempioUnita("Die Verbesserung der Qualität ist notwendig.", "Il miglioramento della qualità è necessario.")
    ),
    errori = listOf(
        erroreUnita(
            "Die Behandeln ist wichtig.",
            "Die Behandlung ist wichtig.",
            "Da behandeln deriva il nome femminile die Behandlung."
        ),
        erroreUnita(
            "Wegen der Regen bleiben wir zu Hause.",
            "Wegen des Regens bleiben wir zu Hause.",
            "wegen regge il genitivo: des Regens."
        ),
        erroreUnita(
            "Das Gesundheit ist wichtig.",
            "Die Gesundheit ist wichtig.",
            "I nomi in -heit sono femminili: die Gesundheit."
        )
    ),
    merksatz = "Verbo → nome (-ung femminile, infinito neutro). Congiunzione → preposizione (wegen, trotz, während, nach).",
    esercizi = listOf(
        sceltaUnita(
            "b2ns-01",
            "Die ___ des Patienten dauert zwei Wochen.",
            listOf("behandeln", "Behandlung", "behandelt", "Behandelung"),
            1,
            "Da behandeln deriva die Behandlung."
        ),
        sceltaUnita(
            "b2ns-02",
            "___ seiner Krankheit konnte er nicht kommen.",
            listOf("Weil", "Wegen", "Obwohl", "Trotz"),
            1,
            "La preposizione che sostituisce 'weil' è wegen + genitivo."
        ),
        sceltaUnita(
            "b2ns-03",
            "___ des Regens gehen wir spazieren.",
            listOf("Wegen", "Trotz", "Während", "Nach"),
            1,
            "trotz = nonostante: trotz des Regens."
        ),
        sceltaUnita(
            "b2ns-04",
            "Das ___ hilft beim Lernen.",
            listOf("lesen", "Lesen", "Lesung", "gelesen"),
            1,
            "L'infinito sostantivato è neutro e maiuscolo: das Lesen."
        ),
        completaUnita(
            "b2ns-05",
            "entscheiden → die ___",
            "Entscheidung",
            "Suffisso -ung, genere femminile: die Entscheidung."
        ),
        completaUnita(
            "b2ns-06",
            "gesund → die ___",
            "Gesundheit",
            "Suffisso -heit, genere femminile: die Gesundheit."
        ),
        trasformaUnita(
            "b2ns-07",
            "Weil er krank ist, bleibt er zu Hause. (stile nominale)",
            "Wegen seiner Krankheit bleibt er zu Hause.",
            "weil + frase → wegen + genitivo: wegen seiner Krankheit."
        ),
        produciUnita(
            "b2ns-08",
            "Traduci: Il miglioramento della qualità è importante.",
            "Die Verbesserung der Qualität ist wichtig.",
            "Verbesserung + genitivo: die Verbesserung der Qualität.",
            alternative = listOf("die verbesserung der qualität ist wichtig")
        )
    )
)
