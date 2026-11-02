package com.alessandro.tedesco.data

// ============================================================
// Unità aggiunte dopo audit vs percorsi ufficiali (Goethe/Klett/Uni Wien).
// Colmano i gap ad alto impatto individuati nel confronto.
// ============================================================

// ---------------------------- A2 (5 unità) ----------------------------

internal fun unitaA2Adjektivdeklination(): UnitaGrammatica = unitaGrammatica(
    id = "a2-adjektivdeklination",
    livello = "A2",
    titolo = "La declinazione dell'aggettivo",
    sottotitolo = "Der kleine Hund, ein kleiner Hund, kleiner Hund",
    obiettivo = "Declinare l'aggettivo dopo articolo determinativo, indeterminativo e senza articolo.",
    minuti = 16,
    prerequisiti = listOf("a1-akkusativ", "a2-dativo"),
    regola = listOf(
        "La desinenza dell'aggettivo dipende da tre fattori: genere/numero del sostantivo, caso, e tipo di articolo che lo precede.",
        "Dopo articolo determinativo (der/die/das) l'aggettivo è 'debole': desinenza -e (nom/akk plurale) o -en (tutti gli altri casi).",
        "Dopo articolo indeterminativo (ein/eine) o kein/possessivi l'aggettivo è 'forte' dove l'articolo non porta la marca del caso: nominativo maschile -er, neutro -es, accusativo maschile -en; negli altri casi -e/-en.",
        "Senza articolo (aggettivo nudo) l'aggettivo è 'forte': maschile/neutro nominativo -er/-es, tutti gli altri casi -en.",
        "La regola d'oro: l'articolo 'forte' rende l'aggettivo 'debole' e viceversa; se manca l'articolo, l'aggettivo fa tutto il lavoro."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Dopo articolo determinativo (debole)",
            intestazioni = listOf("Caso", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("Nominativo", "der klein-e Hund", "die klein-e Katze", "das klein-e Kind", "die klein-en Hunde"),
                listOf("Accusativo", "den klein-en Hund", "die klein-e Katze", "das klein-e Kind", "die klein-en Hunde"),
                listOf("Dativo", "dem klein-en Hund", "der klein-en Katze", "dem klein-en Kind", "den klein-en Hunden"),
                listOf("Genitivo", "des klein-en Hundes", "der klein-en Katze", "des klein-en Kindes", "der klein-en Hunde")
            )
        ),
        tabellaUnita(
            titolo = "Dopo ein/kein (forte dove l'articolo è debole)",
            intestazioni = listOf("Caso", "Maschile", "Femminile", "Neutro"),
            righe = listOf(
                listOf("Nominativo", "ein klein-er Hund", "eine klein-e Katze", "ein klein-es Kind"),
                listOf("Accusativo", "einen klein-en Hund", "eine klein-e Katze", "ein klein-es Kind"),
                listOf("Dativo", "einem klein-en Hund", "einer klein-en Katze", "einem klein-en Kind"),
                listOf("Genitivo", "eines klein-en Hundes", "einer klein-en Katze", "eines klein-en Kindes")
            )
        ),
        tabellaUnita(
            titolo = "Senza articolo (forte)",
            intestazioni = listOf("Caso", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("Nominativo", "klein-er Hund", "klein-e Katze", "klein-es Kind", "klein-e Hunde"),
                listOf("Accusativo", "klein-en Hund", "klein-e Katze", "klein-es Kind", "klein-e Hunde"),
                listOf("Dativo", "klein-em Hund", "klein-er Katze", "klein-em Kind", "klein-en Hunden")
            ),
            nota = "Senza articolo l'aggettivo porta la marca del caso da solo."
        )
    ),
    esempi = listOf(
        esempioUnita("Der kleine Hund spielt im Garten.", "Il cagnolino gioca in giardino."),
        esempioUnita("Ich sehe einen kleinen Hund.", "Vedo un cagnolino."),
        esempioUnita("Das ist ein kleines Kind.", "È un bambino piccolo."),
        esempioUnita("Mit dem kleinen Hund spazieren gehen.", "Passeggiare con il cagnolino."),
        esempioUnita("Frischer Kaffee und frische Milch.", "Caffè fresco e latte fresco."),
        esempioUnita("Kleine Hunde sind oft laut.", "I cagnolini sono spesso rumorosi.")
    ),
    errori = listOf(
        erroreUnita("Der klein Hund spielt.", "Der kleine Hund spielt.", "Dopo articolo determinativo l'aggettivo è debole: -e."),
        erroreUnita("Ich sehe ein klein Hund.", "Ich sehe einen kleinen Hund.", "Accusativo maschile: articolo einen + aggettivo -en."),
        erroreUnita("Mit dem klein Hund.", "Mit dem kleinen Hund.", "Dativo: aggettivo -en anche dopo articolo determinativo.")
    ),
    merksatz = "Articolo forte = aggettivo debole (-e/-en). Articolo debole = aggettivo forte (-er/-es/-en). Nessun articolo = aggettivo forte.",
    esercizi = listOf(
        sceltaUnita("a2adj-01", "Der ___ Hund spielt im Garten.", listOf("kleine", "kleiner", "kleinen", "kleines"), 0, "Dopo der (nominativo) l'aggettivo è debole: kleine."),
        sceltaUnita("a2adj-02", "Ich sehe einen ___ Hund.", listOf("kleine", "kleiner", "kleinen", "kleines"), 2, "Accusativo maschile: einen kleinen."),
        sceltaUnita("a2adj-03", "Das ist ein ___ Kind.", listOf("kleine", "kleiner", "kleinen", "kleines"), 3, "Nominativo neutro dopo ein: kleines."),
        sceltaUnita("a2adj-04", "Mit dem ___ Hund spazieren gehen.", listOf("kleine", "kleiner", "kleinen", "kleines"), 2, "Dativo: kleinen."),
        sceltaUnita("a2adj-05", "___ Kaffee und ___ Milch, bitte.", listOf("Frischer / frische", "Frische / frischer", "Frischen / frischen", "Frisches / frisches"), 0, "Senza articolo: maschile nominativo -er, femminile -e."),
        sceltaUnita("a2adj-06", "Ich mag ___ Hunde.", listOf("kleine", "kleiner", "kleinen", "kleines"), 0, "Plurale nominativo senza articolo: kleine."),
        produciUnita("a2-adj-prod", "„Il medico ha una visita importante.“", "Der Arzt hat einen wichtigen Termin.", "Termin è maschile: all'accusativo con articolo indeterminativo → einen wichtigen Termin."),
    )
)

internal fun unitaA2Konjunktiv2Basis(): UnitaGrammatica = unitaGrammatica(
    id = "a2-konjunktiv2",
    livello = "A2",
    titolo = "Konjunktiv II di base",
    sottotitolo = "Würde, hätte, wäre: desideri e richieste cortesi",
    obiettivo = "Esprimere desideri, richieste cortesi e possibilità irreali con il Konjunktiv II.",
    minuti = 14,
    prerequisiti = listOf("a1-modali"),
    regola = listOf(
        "Il Konjunktiv II esprime desideri irrealizzabili, richieste cortesi e possibilità ipotetiche.",
        "Forma più comune: würde + Infinitiv (per la maggior parte dei verbi): Ich würde gern kommen.",
        "Per sein, haben, können, müssen, sollen, dürfen, wissen si usano le forme proprie: wäre, hätte, könnte, müsste, sollte, dürfte, wüsste.",
        "Richiesta cortese: Ich hätte gern einen Kaffee. / Könnten Sie mir helfen? / Würden Sie bitte...?",
        "Desiderio irrealizzabile: Wenn ich reich wäre, würde ich reisen. (se fossi ricco, viaggerei)",
        "La forma würde + Infinitiv è sempre corretta; le forme proprie sono più eleganti per sein/haben/können/müssen."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Forme base del Konjunktiv II",
            intestazioni = listOf("Verbo", "Konjunktiv II", "Uso"),
            righe = listOf(
                listOf("machen", "würde machen", "desiderio, richiesta"),
                listOf("sein", "wäre", "stato irrealizzabile"),
                listOf("haben", "hätte", "possesso irrealizzabile"),
                listOf("können", "könnte", "possibilità cortese"),
                listOf("müssen", "müsste", "obbligo cortese"),
                listOf("sollen", "sollte", "consiglio cortese")
            )
        ),
        tabellaUnita(
            titolo = "Strutture tipiche",
            intestazioni = listOf("Struttura", "Esempio"),
            righe = listOf(
                listOf("Desiderio", "Ich hätte gern einen Kaffee."),
                listOf("Richiesta cortese", "Könnten Sie mir helfen?"),
                listOf("Ipotesi irrealizzabile", "Wenn ich Zeit hätte, würde ich kommen."),
                listOf("Consiglio", "Du solltest mehr schlafen.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich hätte gern einen Kaffee.", "Vorrei un caffè."),
        esempioUnita("Könnten Sie mir bitte helfen?", "Potrebbe aiutarmi, per favore?"),
        esempioUnita("Wenn ich reich wäre, würde ich um die Welt reisen.", "Se fossi ricco, viaggerei per il mondo."),
        esempioUnita("Du solltest mehr Wasser trinken.", "Dovresti bere più acqua."),
        esempioUnita("Ich würde gern nach Italien fahren.", "Vorrei andare in Italia.")
    ),
    errori = listOf(
        erroreUnita("Ich würde gern einen Kaffee haben.", "Ich hätte gern einen Kaffee.", "Per 'vorrei' si usa hätte, non würde haben."),
        erroreUnita("Wenn ich reich würde, würde ich reisen.", "Wenn ich reich wäre, würde ich reisen.", "Se fossi ricco: wäre, non würde."),
        erroreUnita("Kannst du mir helfen?", "Könntest du mir helfen?", "Richiesta cortese: Konjunktiv II könnte, non indicativo.")
    ),
    merksatz = "würde + Infinitiv per i verbi normali; hätte/wäre/könnte/müsste per sein/haben/können/müssen.",
    esercizi = listOf(
        sceltaUnita("konj2-01", "Ich ___ gern einen Kaffee.", listOf("würde haben", "hätte", "habe", "hatte"), 1, "Vorrei un caffè: hätte."),
        sceltaUnita("konj2-02", "___ Sie mir bitte helfen?", listOf("Können", "Könnten", "Müssen", "Müssten"), 1, "Richiesta cortese: Könnten Sie...?"),
        sceltaUnita("konj2-03", "Wenn wir mehr Zeit ___, würden wir mehr lesen.", listOf("haben", "hatten", "hätten", "würde haben"), 2, "Ipotesi irrealizzabile: hätten."),
        sceltaUnita("konj2-04", "Du ___ mehr schlafen.", listOf("sollst", "solltest", "musst", "müsstest"), 1, "Consiglio cortese: solltest."),
        sceltaUnita("konj2-05", "Ich ___ gern nach Italien fahren.", listOf("würde", "wäre", "hätte", "könnte"), 0, "Desiderio con verbo normale: würde fahren."),
        produciUnita("a2-konj2-prod", "„Se avessi tempo, verrei.“", "Wenn ich Zeit hätte, würde ich kommen.", "Ipotesi irreale: hätte (avere) + würde + infinito."),
    )
)

internal fun unitaA2VerbenPraepositionen(): UnitaGrammatica = unitaGrammatica(
    id = "a2-verben-praepositionen",
    livello = "A2",
    titolo = "Verbi con preposizione",
    sottotitolo = "Warten auf, denken an, sich interessieren für",
    obiettivo = "Usare i verbi che richiedono una preposizione fissa con il caso corretto.",
    minuti = 14,
    prerequisiti = listOf("a2-dativo"),
    regola = listOf(
        "Alcuni verbi tedeschi richiedono una preposizione fissa, con un caso fisso (accusativo o dativo) che non dipende dalla posizione nel tedesco.",
        "La preposizione si impara insieme al verbo come un blocco: warten auf (+Akk), denken an (+Akk), sich interessieren für (+Akk).",
        "Verbi con preposizione + accusativo: warten auf, denken an, sich interessieren für, sich freuen auf/über, sprechen über, bitten um, danken für, sich kümmern um, teilnehmen an (+Dat).",
        "Verbi con preposizione + dativo: gehören zu, teilnehmen an, sich gewöhnen an (+Akk), sich erinnern an (+Akk).",
        "Il caso è fisso: anche se la preposizione è di luogo, non si applica la regola wo/wohin."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Verbo + preposizione + caso",
            intestazioni = listOf("Verbo", "Preposizione", "Caso", "Esempio"),
            righe = listOf(
                listOf("warten", "auf", "Akk", "Ich warte auf den Bus."),
                listOf("denken", "an", "Akk", "Er denkt an seine Familie."),
                listOf("sich interessieren", "für", "Akk", "Sie interessiert sich für Musik."),
                listOf("sich freuen", "auf / über", "Akk", "Wir freuen uns auf das Wochenende."),
                listOf("sprechen", "über", "Akk", "Sie spricht über das Buch."),
                listOf("bitten", "um", "Akk", "Ich bitte um Hilfe."),
                listOf("danken", "für", "Akk", "Danke für die Blumen."),
                listOf("sich kümmern", "um", "Akk", "Sie kümmert sich um die Kinder."),
                listOf("teilnehmen", "an", "Dat", "Er nimmt an dem Kurs teil."),
                listOf("gehören", "zu", "Dat", "Das Buch gehört zu der Reihe.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich warte auf den Bus.", "Aspetto l'autobus."),
        esempioUnita("Er denkt an seine Familie.", "Pensa alla sua famiglia."),
        esempioUnita("Sie interessiert sich für Musik.", "Si interessa di musica."),
        esempioUnita("Wir freuen uns auf das Wochenende.", "Non vediamo l'ora del weekend."),
        esempioUnita("Sie spricht über das Buch.", "Parla del libro.")
    ),
    errori = listOf(
        erroreUnita("Ich warte für den Bus.", "Ich warte auf den Bus.", "warten richiede auf, non für."),
        erroreUnita("Ich denke über meine Familie.", "Ich denke an meine Familie.", "denken richiede an, non über."),
        erroreUnita("Sie interessiert sich an Musik.", "Sie interessiert sich für Musik.", "sich interessieren richiede für, non an.")
    ),
    merksatz = "Il verbo sceglie la preposizione e il caso: imparali insieme come un blocco unico.",
    esercizi = listOf(
        sceltaUnita("vp-01", "Ich warte ___ den Bus.", listOf("auf", "für", "an", "über"), 0, "warten auf (+Akk)."),
        sceltaUnita("vp-02", "Er denkt ___ seine Familie.", listOf("auf", "an", "für", "über"), 1, "denken an (+Akk)."),
        sceltaUnita("vp-03", "Sie interessiert sich ___ Musik.", listOf("auf", "an", "für", "über"), 2, "sich interessieren für (+Akk)."),
        sceltaUnita("vp-04", "Wir freuen uns ___ das Wochenende.", listOf("auf", "an", "für", "über"), 0, "sich freuen auf (+Akk)."),
        sceltaUnita("vp-05", "Er nimmt ___ dem Kurs teil.", listOf("an", "auf", "für", "über"), 0, "teilnehmen an (+Dat): an dem."),
        sceltaUnita("vp-06", "Das Buch gehört ___ der Reihe.", listOf("an", "auf", "zu", "für"), 2, "gehören zu (+Dat)."),
        produciUnita("a2-vpr-prep-prod", "„Aspetto l'autobus.“", "Ich warte auf den Bus.", "warten auf regge l'accusativo: auf den Bus."),
    )
)

internal fun unitaA2Indefinitpronomen(): UnitaGrammatica = unitaGrammatica(
    id = "a2-indefinitpronomen",
    livello = "A2",
    titolo = "Pronomi indefiniti",
    sottotitolo = "Man, jemand, etwas, alle: soggetti indeterminati",
    obiettivo = "Usare i pronomi indefiniti per soggetti e oggetti generici.",
    minuti = 12,
    prerequisiti = listOf("a1-akkusativ"),
    regola = listOf(
        "man = soggetto impersonale (si): Man sagt das so. (si dice così). man è sempre nominativo singolare.",
        "jemand = qualcuno, niemand = nessuno. Si declinano come aggettivi senza articolo.",
        "etwas = qualcosa, nichts = niente. Sono invariabili (non si declinano).",
        "alle = tutti, einige = alcuni, mehrere = parecchi, manche = alcuni (non tutti). Si declinano come aggettivi senza articolo.",
        "Indefiniti con articolo: ein paar (alcuni), ein bisschen/ein wenig (un po').",
        "jemand/niemand + etwas/nichts si usano anche come oggetto: Ich habe jemanden gesehen. / Ich habe nichts verstanden."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Pronomi indefiniti comuni",
            intestazioni = listOf("Pronome", "Significato", "Declinazione"),
            righe = listOf(
                listOf("man", "si (impersonale)", "invariabile, nominativo"),
                listOf("jemand", "qualcuno", "come aggettivo senza articolo"),
                listOf("niemand", "nessuno", "come aggettivo senza articolo"),
                listOf("etwas", "qualcosa", "invariabile"),
                listOf("nichts", "niente", "invariabile"),
                listOf("alle", "tutti", "come aggettivo senza articolo"),
                listOf("einige", "alcuni", "come aggettivo senza articolo"),
                listOf("mehrere", "parecchi", "come aggettivo senza articolo"),
                listOf("manche", "alcuni (non tutti)", "come aggettivo senza articolo")
            )
        ),
        tabellaUnita(
            titolo = "Declinazione di jemand (come aggettivo senza articolo)",
            intestazioni = listOf("Caso", "Maschile/Femminile/Neutro", "Plurale"),
            righe = listOf(
                listOf("Nominativo", "jemand", "—"),
                listOf("Accusativo", "jemanden", "—"),
                listOf("Dativo", "jemandem", "—"),
                listOf("Genitivo", "jemandes", "—")
            ),
            nota = "jemand e niemand si usano per lo più al singolare; per il plurale si usano alle/einige/mehrere."
        )
    ),
    esempi = listOf(
        esempioUnita("Man kann hier nicht parken.", "Qui non si può parcheggiare."),
        esempioUnita("Jemand hat angerufen.", "Ha telefonato qualcuno."),
        esempioUnita("Ich habe nichts verstanden.", "Non ho capito niente."),
        esempioUnita("Alle sind gekommen.", "Sono venuti tutti."),
        esempioUnita("Ich habe jemanden gesehen.", "Ho visto qualcuno."),
        esempioUnita("Ein paar Studenten sind da.", "Ci sono alcuni studenti.")
    ),
    errori = listOf(
        erroreUnita("Jemand hat angerufen, aber ich habe ihn nicht gehört.", "Jemand hat angerufen, aber ich habe ihn nicht gehört.", "jemand è nominativo; se è oggetto: jemanden."),
        erroreUnita("Man sagt das so.", "Man sagt das so.", "man è sempre soggetto impersonale (nominativo)."),
        erroreUnita("Ich habe nichts gesehen.", "Ich habe nichts gesehen.", "nichts è invariabile: nichts gesehen, non nichts-en.")
    ),
    merksatz = "man = si impersonale; jemand/niemand + qualcuno/nessuno; etwas/nichts = qualcosa/niente (invariabili).",
    esercizi = listOf(
        sceltaUnita("a2ind-01", "___ kann hier nicht parken.", listOf("Man", "Jemand", "Etwas", "Alle"), 0, "Soggetto impersonale: Man."),
        sceltaUnita("a2ind-02", "Ich habe ___ verstanden.", listOf("nichts", "niemand", "kein", "keine"), 0, "Oggetto invariabile: nichts."),
        sceltaUnita("a2ind-03", "___ hat angerufen.", listOf("Man", "Jemand", "Etwas", "Alle"), 1, "Ha telefonato qualcuno: Jemand."),
        sceltaUnita("a2ind-04", "Ich habe ___ gesehen.", listOf("jemand", "jemanden", "jemandem", "jemandes"), 1, "Oggetto accusativo: jemanden."),
        sceltaUnita("a2ind-05", "___ sind gekommen.", listOf("Alle", "Alles", "Jemand", "Etwas"), 0, "Tutti sono venuti: Alle."),
        produciUnita("a2-indef-prod", "„Qualcuno ha chiamato?“", "Hat jemand angerufen?", "jemand = qualcuno; è invariabile e non prende l'articolo."),
    )
)

internal fun unitaA2IndirekteFragen(): UnitaGrammatica = unitaGrammatica(
    id = "a2-indirekte-fragen",
    livello = "A2",
    titolo = "Domande indirette",
    sottotitolo = "Ich weiß nicht, ob er kommt",
    obiettivo = "Riferire una domanda in una frase principale, con il verbo alla fine.",
    minuti = 12,
    prerequisiti = listOf("a2-subordinate"),
    regola = listOf(
        "La domanda indiretta riferisce una domanda senza punto interrogativo; il verbo coniugato va alla fine della frase indiretta.",
        "Domanda sì/no indiretta: ob + verbo alla fine. (Ich weiß nicht, ob er kommt.)",
        "Domanda con W-Wort indiretta: W-Wort + verbo alla fine. (Ich frage, wann er kommt.)",
        "Si introduce con: Ich weiß nicht, ...; Ich frage mich, ...; Er fragt, ...; Ich möchte wissen, ...",
        "Il punto interrogativo si mette solo se la frase principale è una domanda: Weißt du, ob er kommt?"
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Da diretta a indiretta",
            intestazioni = listOf("Diretta", "Indiretta"),
            righe = listOf(
                listOf("Kommst du?", "Ich weiß nicht, ob du kommst."),
                listOf("Wo wohnst du?", "Ich frage, wo du wohnst."),
                listOf("Wann kommst du?", "Ich möchte wissen, wann du kommst."),
                listOf("Hat er Zeit?", "Er fragt, ob er Zeit hat.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich weiß nicht, ob er kommt.", "Non so se viene."),
        esempioUnita("Ich frage, wann du kommst.", "Chiedo quando vieni."),
        esempioUnita("Ich möchte wissen, wie viel das kostet.", "Vorrei sapere quanto costa."),
        esempioUnita("Weißt du, ob er Zeit hat?", "Sai se ha tempo?")
    ),
    errori = listOf(
        erroreUnita("Ich weiß nicht, ob kommt er.", "Ich weiß nicht, ob er kommt.", "Dopo ob il verbo va alla fine."),
        erroreUnita("Ich frage, wann kommst du.", "Ich frage, wann du kommst.", "Dopo il W-Wort il verbo va alla fine."),
        erroreUnita("Ich weiß nicht, ob er kommt?", "Ich weiß nicht, ob er kommt.", "La frase indiretta non ha punto interrogativo (se la principale è affermativa).")
    ),
    merksatz = "Domanda indiretta = verbo alla fine; ob per sì/no, W-Wort per le altre.",
    esercizi = listOf(
        sceltaUnita("indf-01", "Ich weiß nicht, ___ er kommt.", listOf("ob", "wenn", "dass", "weil"), 0, "Domanda sì/no indiretta: ob."),
        sceltaUnita("indf-02", "Ich frage, ___ du wohnst.", listOf("ob", "wo", "dass", "weil"), 1, "Domanda con W-Wort: wo."),
        sceltaUnita("indf-03", "Ich möchte wissen, ___ das kostet.", listOf("ob", "wie viel", "dass", "weil"), 1, "W-Wort: wie viel."),
        sceltaUnita("indf-04", "Weißt du, ___ er Zeit hat?", listOf("ob", "wenn", "dass", "weil"), 0, "Domanda sì/no indiretta: ob."),
        sceltaUnita("indf-05", "Ich frage mich, ___ er das gesagt hat.", listOf("ob", "warum", "dass", "weil"), 1, "Domanda con W-Wort: warum."),
        produciUnita("a2-indir-frag-prod", "„Mi chiedi dove abito.“", "Du fragst mich, wo ich wohne.", "Domanda indiretta: il verbo coniugato va in fondo (wo ich wohne)."),
    )
)

internal fun unitaA1Fragewoerter(): UnitaGrammatica = unitaGrammatica(
    id = "a1-fragewoerter",
    livello = "A1",
    titolo = "Fragewörter",
    sottotitolo = "Wer, was, wo, wann, warum, wie",
    obiettivo = "Formare domande con i principali pronomi e avverbi interrogativi.",
    minuti = 10,
    prerequisiti = listOf("a0-frase"),
    regola = listOf(
        "I Fragewörter (pronomi/avverbi interrogativi) introducono domande aperte: wer (chi), was (cosa), wo (dove), wann (quando), warum (perché), wie (come), wohin (dove, direzione), woher (da dove).",
        "Nelle domande con Fragewort il verbo coniugato va in seconda posizione (dopo il Fragewort): Wo wohnst du?",
        "welcher/welche/welches (quale) si declina come l'articolo determinativo.",
        "wie + aggettivo/avverbio: Wie alt bist du? Wie viel kostet das? Wie oft gehst du?",
        "was für (ein) = che tipo di: Was für ein Auto hast du?"
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Fragewörter principali",
            intestazioni = listOf("Fragewort", "Significato", "Esempio"),
            righe = listOf(
                listOf("wer", "chi", "Wer ist das?"),
                listOf("was", "cosa", "Was machst du?"),
                listOf("wo", "dove", "Wo wohnst du?"),
                listOf("wohin", "dove (direzione)", "Wohin gehst du?"),
                listOf("woher", "da dove", "Woher kommst du?"),
                listOf("wann", "quando", "Wann kommst du?"),
                listOf("warum", "perché", "Warum lernst du Deutsch?"),
                listOf("wie", "come", "Wie geht es dir?"),
                listOf("welcher/welche/welches", "quale", "Welches Buch liest du?")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Wer bist du?", "Chi sei?"),
        esempioUnita("Was machst du?", "Cosa fai?"),
        esempioUnita("Wo wohnst du?", "Dove abiti?"),
        esempioUnita("Wann kommst du?", "Quando vieni?"),
        esempioUnita("Wie alt bist du?", "Quanti anni hai?"),
        esempioUnita("Welches Buch liest du?", "Quale libro leggi?")
    ),
    errori = listOf(
        erroreUnita("Wo du wohnst?", "Wo wohnst du?", "Il verbo va in seconda posizione: Wo wohnst du?"),
        erroreUnita("Was für Auto hast du?", "Was für ein Auto hast du?", "was für + articolo indeterminativo."),
        erroreUnita("Wer ist das? → Wer das ist?", "Wer ist das?", "Fragewort in prima posizione, verbo in seconda.")
    ),
    merksatz = "Fragewort in prima posizione, verbo coniugato in seconda. welcher/welche/welches si declina come l'articolo.",
    esercizi = listOf(
        sceltaUnita("frag-01", "___ wohnst du?", listOf("Wer", "Was", "Wo", "Wann"), 2, "Dove abiti: Wo."),
        sceltaUnita("frag-02", "___ machst du?", listOf("Wer", "Was", "Wo", "Wann"), 1, "Cosa fai: Was."),
        sceltaUnita("frag-03", "___ kommst du?", listOf("Wer", "Was", "Wo", "Wann"), 3, "Quando vieni: Wann."),
        sceltaUnita("frag-04", "___ alt bist du?", listOf("Wer", "Was", "Wo", "Wie"), 3, "Quanti anni: Wie alt."),
        sceltaUnita("frag-05", "___ Buch liest du?", listOf("Wer", "Was", "Welches", "Wie"), 2, "Quale libro: Welches."),
        produciUnita("a1-frage-prod", "„Dove abiti?“", "Wo wohnst du?", "wo = dove; nella domanda il verbo è in seconda posizione."),
    )
)
