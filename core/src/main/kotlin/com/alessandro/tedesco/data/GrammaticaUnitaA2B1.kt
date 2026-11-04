package com.alessandro.tedesco.data

// =====================================================================
// PERCORSO A2 -> B1
// Casi, tempi del passato, subordinate, passivo e strutture B1.
// =====================================================================

internal fun unitaA2Dativo(): UnitaGrammatica = unitaGrammatica(
    id = "a2-dativo",
    livello = "A2",
    titolo = "Il dativo",
    sottotitolo = "Il caso del complemento di termine",
    obiettivo = "Usare articoli, pronomi e verbi che reggono il dativo.",
    minuti = 15,
    prerequisiti = listOf("a1-akkusativ"),
    regola = listOf(
        "Il dativo è il caso del complemento di termine: risponde a Wem? (a chi?).",
        "Gli articoli cambiano: der -> dem, die -> der, das -> dem; al plurale den + -n sul sostantivo.",
        "Gli articoli indeterminativi e i possessivi prendono le desinenze del dativo: einem/einer/einem, meinem/meiner/meinem, deinem/deiner/deinem, seinem/seiner/seinem.",
        "Al plurale il dativo è den + -n sul sostantivo (den Kindern); anche kein diventa keinen (keinen Kindern).",
        "I pronomi al dativo sono mir, dir, ihm, ihr, ihm, uns, euch, ihnen.",
        "Molti verbi reggono il dativo: helfen, danken, gefallen, gehören, passen, schmecken, antworten, folgen. Anche le preposizioni aus, bei, mit, nach, seit, von, zu reggono il dativo."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Articoli definiti",
            intestazioni = listOf("", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("Nominativo", "der", "die", "das", "die"),
                listOf("Accusativo", "den", "die", "das", "die"),
                listOf("Dativo", "dem", "der", "dem", "den + -n")
            ),
            nota = "Al dativo plurale il sostantivo aggiunge spesso -n: den Kindern."
        ),
        tabellaUnita(
            titolo = "Possessivi al dativo",
            intestazioni = listOf("Persona", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("ich", "meinem", "meiner", "meinem", "meinen"),
                listOf("du", "deinem", "deiner", "deinem", "deinen"),
                listOf("er / es", "seinem", "seiner", "seinem", "seinen"),
                listOf("sie (lei)", "ihrem", "ihrer", "ihrem", "ihren"),
                listOf("wir", "unserem", "unserer", "unserem", "unseren"),
                listOf("ihr", "eurem", "eurer", "eurem", "euren"),
                listOf("sie / Sie", "ihrem / Ihrem", "ihrer / Ihrer", "ihrem / Ihrem", "ihren / Ihren")
            ),
            nota = "Al dativo cambiano tutti i generi: maschile e neutro -em, femminile -er, plurale -en."
        ),
        tabellaUnita(
            titolo = "Pronomi personali al dativo",
            intestazioni = listOf("Nominativo", "Dativo"),
            righe = listOf(
                listOf("ich", "mir"),
                listOf("du", "dir"),
                listOf("er", "ihm"),
                listOf("sie", "ihr"),
                listOf("es", "ihm"),
                listOf("wir", "uns"),
                listOf("ihr", "euch"),
                listOf("sie / Sie", "ihnen / Ihnen")
            )
        ),
        tabellaUnita(
            titolo = "Verbi e preposizioni con dativo",
            intestazioni = listOf("Parola", "Esempio"),
            righe = listOf(
                listOf("helfen", "Ich helfe dem Mann."),
                listOf("danken", "Wir danken der Lehrerin."),
                listOf("gefallen", "Das Buch gefällt mir."),
                listOf("gehören", "Das Auto gehört meinem Vater."),
                listOf("mit", "Ich fahre mit dem Bus."),
                listOf("seit", "Seit einem Jahr lerne ich Deutsch."),
                listOf("zu", "Ich gehe zu dem Arzt.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich helfe dem Mann.", "Aiuto l'uomo."),
        esempioUnita("Das Buch gehört der Frau.", "Il libro appartiene alla donna."),
        esempioUnita("Wir danken den Kindern.", "Ringraziamo i bambini."),
        esempioUnita("Das gefällt mir.", "Questo mi piace."),
        esempioUnita("Ich fahre mit dem Bus.", "Vado in autobus.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich helfe der Mann.",
            "Ich helfe dem Mann.",
            "helfen regge il dativo: der Mann -> dem Mann."
        ),
        erroreUnita(
            "Ich danke für dich.",
            "Ich danke dir.",
            "danken vuole il dativo diretto, senza für: ich danke dir."
        ),
        erroreUnita(
            "mit der Auto",
            "mit dem Auto",
            "Auto è neutro; al dativo l'articolo è dem: mit dem Auto."
        )
    ),
    merksatz = "Dativo: dem, der, dem, den + -n. Possessivi: meinem, meiner, meinem, meinen. Pronomi: mir, dir, ihm, ihr, ihm, uns, euch, ihnen.",
    esercizi = listOf(
        sceltaUnita(
            "dat-01",
            "Ich helfe ___ Mann.",
            listOf("der", "den", "dem", "des"),
            2,
            "helfen regge il dativo: dem Mann."
        ),
        sceltaUnita(
            "dat-02",
            "Das Buch gehört ___ Frau.",
            listOf("die", "der", "dem", "den"),
            1,
            "Frau è femminile; al dativo l'articolo è der."
        ),
        sceltaUnita(
            "dat-03",
            "Wir danken ___ Lehrer.",
            listOf("der", "den", "dem", "des"),
            2,
            "Lehrer è maschile singolare; al dativo: dem Lehrer."
        ),
        sceltaUnita(
            "dat-04",
            "Sie antwortet ___ Kindern.",
            listOf("die", "der", "den", "dem"),
            2,
            "Plurale al dativo: den Kindern."
        ),
        sceltaUnita(
            "dat-05",
            "Das gefällt ___.",
            listOf("ich", "mich", "mir", "mein"),
            2,
            "gefallen regge il dativo: mir."
        ),
        completaUnita(
            "dat-06",
            "Ich fahre mit ___ Bus.",
            "dem",
            "mit regge il dativo; Bus è maschile: dem Bus."
        ),
        completaUnita(
            "dat-07",
            "Er kommt aus ___ Schweiz.",
            "der",
            "die Schweiz è femminile; al dativo: aus der Schweiz."
        ),
        completaUnita(
            "dat-08",
            "Wir wohnen bei ___ Eltern.",
            "den",
            "Plurale al dativo: bei den Eltern."
        ),
        completaUnita(
            "dat-09",
            "Gib ___ das Buch. (ich)",
            "mir",
            "Il pronome dativo di ich è mir."
        ),
        trasformaUnita(
            "dat-10",
            "Sostituisci il complemento: Ich helfe dem Mann. -> Ich helfe ___.",
            "ihm",
            "dem Mann maschile diventa ihm."
        ),
        trasformaUnita(
            "dat-11",
            "Sostituisci il complemento: Das Buch gehört der Frau. -> Es gehört ___.",
            "ihr",
            "der Frau femminile diventa ihr."
        ),
        produciUnita(
            "dat-12",
            "Traduci: «Mi piace Berlino.»",
            "Berlin gefällt mir.",
            "gefallen + dativo: Berlin gefällt mir.",
            "Usa il verbo gefallen."
        )
    )
)

internal fun unitaA2Praeteritum(): UnitaGrammatica = unitaGrammatica(
    id = "a2-praeteritum",
    livello = "A2",
    titolo = "Il Präteritum",
    sottotitolo = "Il passato della narrazione",
    obiettivo = "Raccontare al passato con verbi regolari e irregolari.",
    minuti = 16,
    prerequisiti = listOf("a2-perfekt"),
    regola = listOf(
        "Il Präteritum è il passato semplice; si usa soprattutto nello scritto, nei racconti e nelle notizie. Nel parlato prevale spesso il Perfekt.",
        "I verbi regolari aggiungono -te alla radice: machen -> machte, arbeiten -> arbeitete.",
        "I verbi irregolari cambiano la radice e prendono le desinenze senza -te: gehen -> ging, lesen -> las, kommen -> kam.",
        "sein diventa war, haben diventa hatte, werden diventa wurde.",
        "I modali al Präteritum sono konnte, musste, wollte, durfte, sollte, mochte."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Verbi regolari e irregolari",
            intestazioni = listOf("Pronome", "machen", "gehen", "sein", "haben"),
            righe = listOf(
                listOf("ich", "machte", "ging", "war", "hatte"),
                listOf("du", "machtest", "gingst", "warst", "hattest"),
                listOf("er / sie / es", "machte", "ging", "war", "hatte"),
                listOf("wir", "machten", "gingen", "waren", "hatten"),
                listOf("ihr", "machtet", "gingt", "wart", "hattet"),
                listOf("sie / Sie", "machten", "gingen", "waren", "hatten")
            ),
            nota = "Nei verbi regolari si sente chiaramente la -te; negli irregolari cambia la vocale della radice."
        ),
        tabellaUnita(
            titolo = "Modali al Präteritum",
            intestazioni = listOf("Infinito", "ich", "er / sie / es"),
            righe = listOf(
                listOf("können", "konnte", "konnte"),
                listOf("müssen", "musste", "musste"),
                listOf("wollen", "wollte", "wollte"),
                listOf("dürfen", "durfte", "durfte"),
                listOf("sollen", "sollte", "sollte")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich war gestern im Kino.", "Ieri ero al cinema."),
        esempioUnita("Wir hatten keine Zeit.", "Non avevamo tempo."),
        esempioUnita("Er ging nach Berlin.", "Andò a Berlino."),
        esempioUnita("Sie las das Buch.", "Lei lesse il libro."),
        esempioUnita("Ich konnte nicht kommen.", "Non potevo venire.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich gehte nach Hause.",
            "Ich ging zum Arzt.",
            "gehen è irregolare: il Präteritum è ging, non gehte."
        ),
        erroreUnita(
            "Ich habe ging nach Hause.",
            "Ich ging zum Arzt.",
            "Il Präteritum non si forma con haben + participio: è una forma verbale unica."
        ),
        erroreUnita(
            "Er warst müde.",
            "Er war müde.",
            "La terza persona singolare di sein al Präteritum è war."
        )
    ),
    merksatz = "Regolari: radice + te. Irregolari: nuova radice + desinenze. sein: war, haben: hatte.",
    esercizi = listOf(
        sceltaUnita(
            "praet-01",
            "Ich ___ gestern im Kino.",
            listOf("war", "warst", "waren", "bin"),
            0,
            "sein al Präteritum: ich war."
        ),
        sceltaUnita(
            "praet-02",
            "Wir ___ keine Zeit.",
            listOf("hatte", "hattest", "hatten", "haben"),
            2,
            "haben al Präteritum: wir hatten."
        ),
        sceltaUnita(
            "praet-03",
            "Er ___ nach Berlin.",
            listOf("gehte", "ging", "gegangen", "geht"),
            1,
            "gehen è irregolare: er ging."
        ),
        sceltaUnita(
            "praet-04",
            "Sie ___ das Buch.",
            listOf("las", "laste", "gelesen", "liest"),
            0,
            "lesen al Präteritum: sie las."
        ),
        sceltaUnita(
            "praet-05",
            "Du ___ viel.",
            listOf("arbeitete", "arbeitetest", "gearbeitet", "arbeitest"),
            1,
            "arbeiten al Präteritum: du arbeitetest."
        ),
        completaUnita(
            "praet-06",
            "Ich ___ (machen) die Hausaufgaben.",
            "machte",
            "machen è regolare: ich machte."
        ),
        completaUnita(
            "praet-07",
            "Wir ___ (kommen) zu spät.",
            "kamen",
            "kommen è irregolare: wir kamen."
        ),
        completaUnita(
            "praet-08",
            "Er ___ (sein) müde.",
            "war",
            "sein al Präteritum: er war."
        ),
        trasformaUnita(
            "praet-09",
            "Trasforma al Präteritum: Ich gehe nach Hause.",
            "Ich ging nach Hause.",
            "gehen -> ging.",
            "Scrivi la frase completa."
        ),
        trasformaUnita(
            "praet-10",
            "Trasforma al Präteritum: Wir haben Zeit.",
            "Wir hatten Zeit.",
            "haben -> hatten."
        ),
        completaUnita(
            "praet-11",
            "Sie ___ (können) nicht kommen.",
            "konnte",
            "können al Präteritum: sie konnte."
        ),
        produciUnita(
            "praet-12",
            "Traduci: «Ieri ho comprato un libro.»",
            "Gestern kaufte ich ein Buch.",
            "kaufen al Präteritum: kaufte. È accettabile anche il Perfekt nel parlato.",
            "Usa Gestern e il Präteritum di kaufen.",
            listOf("Gestern habe ich ein Buch gekauft.")
        )
    )
)

internal fun unitaA2Riflessivi(): UnitaGrammatica = unitaGrammatica(
    id = "a2-riflessivi",
    livello = "A2",
    titolo = "I verbi riflessivi",
    sottotitolo = "Sich freuen, sich interessieren, sich beeilen",
    obiettivo = "Usare i pronomi riflessivi e i verbi riflessivi più comuni.",
    minuti = 14,
    prerequisiti = listOf("a1-akkusativ"),
    regola = listOf(
        "I verbi riflessivi hanno un pronome che rimanda al soggetto: mich, dich, sich, uns, euch, sich.",
        "La maggior parte dei riflessivi usa il pronome all'accusativo; alcuni verbi vogliono il dativo: Ich wasche mir die Hände.",
        "Verbi frequenti: sich freuen, sich interessieren für, sich waschen, sich treffen, sich beeilen, sich erholen, sich setzen, sich fühlen.",
        "Il pronome riflessivo sta di solito dopo il verbo coniugato e prima degli altri complementi: Ich freue mich auf den Urlaub.",
        "In tedesco alcuni verbi sono riflessivi anche se in italiano non lo sono: sich beeilen (affrettarsi), sich erholen (riposarsi)."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Pronomi riflessivi",
            intestazioni = listOf("Persona", "Accusativo", "Dativo"),
            righe = listOf(
                listOf("ich", "mich", "mir"),
                listOf("du", "dich", "dir"),
                listOf("er / sie / es", "sich", "sich"),
                listOf("wir", "uns", "uns"),
                listOf("ihr", "euch", "euch"),
                listOf("sie / Sie", "sich", "sich")
            )
        ),
        tabellaUnita(
            titolo = "Verbi riflessivi frequenti",
            intestazioni = listOf("Verbo", "Significato", "Esempio"),
            righe = listOf(
                listOf("sich freuen", "rallegrarsi", "Ich freue mich."),
                listOf("sich interessieren für", "interessarsi a", "Er interessiert sich für Musik."),
                listOf("sich beeilen", "affrettarsi", "Wir beeilen uns."),
                listOf("sich treffen", "incontrarsi", "Sie treffen sich um 8."),
                listOf("sich erholen", "riposarsi", "Ich erhole mich."),
                listOf("sich setzen", "sedersi", "Setz dich!"),
                listOf("sich waschen", "lavarsi", "Ich wasche mich.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich freue mich auf den Urlaub.", "Non vedo l'ora che arrivino le vacanze."),
        esempioUnita("Du interessierst dich für Musik.", "Ti interessi di musica."),
        esempioUnita("Wir treffen uns um 8 Uhr.", "Ci incontriamo alle 8."),
        esempioUnita("Sie erholt sich im Garten.", "Si riposa in giardino."),
        esempioUnita("Ich wasche mir die Hände.", "Mi lavo le mani.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich freue auf den Urlaub.",
            "Ich freue mich auf den Urlaub.",
            "sich freuen richiede il pronome riflessivo: ich freue mich."
        ),
        erroreUnita(
            "Ich wasche mich die Hände.",
            "Ich wasche mir die Hände.",
            "Con una parte del corpo il riflessivo è al dativo: mir die Hände."
        ),
        erroreUnita(
            "Wir treffen sich um 8.",
            "Wir treffen uns um 8.",
            "Con wir il pronome riflessivo è uns."
        )
    ),
    merksatz = "Riflessivi: mich, dich, sich, uns, euch, sich. Con parti del corpo spesso il dativo: mir.",
    esercizi = listOf(
        sceltaUnita(
            "rif-01",
            "Ich freue ___ auf den Urlaub.",
            listOf("mich", "mir", "sich", "dich"),
            0,
            "sich freuen all'accusativo: ich freue mich."
        ),
        sceltaUnita(
            "rif-02",
            "Du interessierst ___ für Musik.",
            listOf("mich", "dich", "sich", "dir"),
            1,
            "Con du il riflessivo è dich."
        ),
        sceltaUnita(
            "rif-03",
            "Er beeilt ___.",
            listOf("mich", "dich", "sich", "ihm"),
            2,
            "Con er il riflessivo è sich."
        ),
        sceltaUnita(
            "rif-04",
            "Wir treffen ___ um 8.",
            listOf("uns", "euch", "sich", "mich"),
            0,
            "Con wir il riflessivo è uns."
        ),
        sceltaUnita(
            "rif-05",
            "Sie (plurale) erholen ___.",
            listOf("sich", "uns", "euch", "ihnen"),
            0,
            "Con sie plurale il riflessivo è sich."
        ),
        completaUnita(
            "rif-06",
            "Ich wasche ___ die Hände.",
            "mir",
            "Con una parte del corpo il riflessivo è al dativo: mir."
        ),
        completaUnita(
            "rif-07",
            "Sie (singolare) setzt ___.",
            "sich",
            "Con sie singolare il riflessivo è sich."
        ),
        completaUnita(
            "rif-08",
            "Ihr müsst ___ beeilen.",
            "euch",
            "Con ihr il riflessivo è euch."
        ),
        trasformaUnita(
            "rif-09",
            "Fai riflessiva: Ich freue / auf das Wochenende.",
            "Ich freue mich auf das Wochenende.",
            "sich freuen: ich freue mich auf ..."
        ),
        trasformaUnita(
            "rif-10",
            "Fai riflessiva: Wir treffen / um 7.",
            "Wir treffen uns um 7.",
            "sich treffen: wir treffen uns."
        ),
        completaUnita(
            "rif-11",
            "Er interessiert ___ für Politik.",
            "sich",
            "Con er il riflessivo è sich."
        ),
        produciUnita(
            "rif-12",
            "Traduci: «Mi riposo un po'.»",
            "Ich erhole mich ein bisschen.",
            "sich erholen: ich erhole mich; ein bisschen = un po'.",
            "Usa sich erholen."
        )
    )
)

internal fun unitaA2Subordinate(): UnitaGrammatica = unitaGrammatica(
    id = "a2-subordinate",
    livello = "A2",
    titolo = "Le frasi subordinate",
    sottotitolo = "Weil, dass, wenn: il verbo alla fine",
    obiettivo = "Collegare due frasi con le congiunzioni più comuni e mettere il verbo alla fine.",
    minuti = 16,
    prerequisiti = listOf("a1-praesens"),
    regola = listOf(
        "Nella frase subordinata il verbo coniugato va alla fine: Ich bleibe zu Hause, weil ich krank bin.",
        "Le congiunzioni più comuni sono weil (perché), dass (che), wenn (se/quando), obwohl (sebbene), als (quando, per un evento passato), während (mentre), bevor (prima che), nachdem (dopo che), damit (affinché), ob (se, nelle domande indirette).",
        "Se la subordinata viene prima, la principale comincia dopo la virgola con il verbo: Weil ich krank bin, bleibe ich zu Hause.",
        "Espressioni utili: am Ende des Satzes (alla fine della frase), am Anfang (all'inizio).",
        "weil e denn significano entrambi «perché», ma denn è coordinante e lascia il verbo in seconda posizione: Ich bleibe zu Hause, denn ich bin krank.",
        "wenn si usa per presente, futuro o eventi ripetuti; als per un singolo evento nel passato: Als ich klein war, ..."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Congiunzioni subordinate",
            intestazioni = listOf("Congiunzione", "Significato", "Esempio"),
            righe = listOf(
                listOf("weil", "perché", "weil ich krank bin"),
                listOf("dass", "che", "dass er kommt"),
                listOf("wenn", "se/quando", "wenn ich Zeit habe"),
                listOf("obwohl", "sebbene", "obwohl es regnet"),
                listOf("als", "quando (passato)", "als ich klein war"),
                listOf("während", "mentre", "während ich arbeite"),
                listOf("bevor", "prima che", "bevor ich gehe"),
                listOf("nachdem", "dopo che", "nachdem ich gegessen habe"),
                listOf("damit", "affinché", "damit du es verstehst"),
                listOf("ob", "se (domanda indiretta)", "ob er kommt")
            ),
            nota = "Dopo tutte queste congiunzioni il verbo coniugato va in fondo."
        ),
        tabellaUnita(
            titolo = "Ordine delle frasi",
            intestazioni = listOf("Schema", "Esempio"),
            righe = listOf(
                listOf("Principale + subordinata", "Ich bleibe zu Hause, weil ich krank bin."),
                listOf("Subordinata + principale", "Weil ich krank bin, bleibe ich zu Hause."),
                listOf("Domanda indiretta", "Ich weiß nicht, ob er kommt.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich bleibe zu Hause, weil ich krank bin.", "Resto a casa perché sono malato."),
        esempioUnita("Er sagt, dass er keine Zeit hat.", "Dice che non ha tempo."),
        esempioUnita("Als ich klein war, wohnte ich in Rom.", "Quando ero piccolo, abitavo a Roma."),
        esempioUnita("Obwohl es regnet, gehen wir spazieren.", "Sebbene piova, andiamo a passeggiare."),
        esempioUnita("Ich weiß nicht, ob er kommt.", "Non so se viene.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich bleibe zu Hause, weil ich bin krank.",
            "Ich bleibe zu Hause, weil ich krank bin.",
            "Dopo weil il verbo coniugato va alla fine."
        ),
        erroreUnita(
            "Als ich war klein, ...",
            "Als ich klein war, ...",
            "Nella subordinata il verbo war va alla fine."
        ),
        erroreUnita(
            "Ich glaube, dass er kommt morgen.",
            "Ich glaube, dass er morgen kommt.",
            "Dopo dass il verbo coniugato va alla fine: ... morgen kommt."
        )
    ),
    merksatz = "Subordinata: verbo alla fine. weil, dass, wenn, obwohl, als, während, bevor, nachdem, ob.",
    esercizi = listOf(
        sceltaUnita(
            "sub-01",
            "Ich bleibe zu Hause, ___ ich krank bin.",
            listOf("weil", "denn", "aber", "oder"),
            0,
            "weil introduce una subordinata con il verbo alla fine: ... weil ich krank bin."
        ),
        sceltaUnita(
            "sub-02",
            "Er sagt, ___ er keine Zeit hat.",
            listOf("weil", "dass", "wenn", "als"),
            1,
            "dass introduce una frase completiva: ... dass er keine Zeit hat."
        ),
        sceltaUnita(
            "sub-03",
            "___ ich klein war, wohnte ich in Rom.",
            listOf("Wenn", "Als", "Ob", "Damit"),
            1,
            "Per un singolo evento passato si usa als."
        ),
        sceltaUnita(
            "sub-04",
            "Ich rufe dich an, ___ ich ankomme.",
            listOf("als", "wenn", "ob", "damit"),
            1,
            "Per un evento presente/futuro si usa wenn."
        ),
        sceltaUnita(
            "sub-05",
            "Sie lernt Deutsch, ___ sie in Berlin arbeiten will.",
            listOf("weil", "dass", "ob", "als"),
            0,
            "Il motivo si esprime con weil: ... weil sie ... will."
        ),
        completaUnita(
            "sub-06",
            "Ich weiß, dass er ___ (kommen).",
            "kommt",
            "Dopo dass il verbo coniugato va alla fine: ... er kommt."
        ),
        completaUnita(
            "sub-07",
            "___ ich krank bin, bleibe ich im Bett.",
            "Weil",
            "La subordinata causale introdotta da weil: Weil ich krank bin, ..."
        ),
        completaUnita(
            "sub-08",
            "Er kommt nicht, ___ er keine Zeit hat.",
            "weil",
            "La causa si esprime con weil; il verbo hat è alla fine."
        ),
        trasformaUnita(
            "sub-09",
            "Unisci con weil: Ich bleibe zu Hause. Ich bin müde.",
            "Ich bleibe zu Hause, weil ich müde bin.",
            "weil richiede il verbo alla fine: ... weil ich müde bin.",
            "Scrivi la frase completa.",
            listOf("Ich bleibe zu Hause weil ich müde bin.")
        ),
        trasformaUnita(
            "sub-10",
            "Unisci con dass: Er sagt. Er kommt morgen.",
            "Er sagt, dass er morgen kommt.",
            "Dopo dass il verbo kommt va alla fine.",
            "Scrivi la frase completa.",
            listOf("Er sagt dass er morgen kommt.")
        ),
        completaUnita(
            "sub-11",
            "___ ich in Berlin war, habe ich viele Museen besucht.",
            "Als",
            "Un evento passato singolo: als ich in Berlin war."
        ),
        produciUnita(
            "sub-12",
            "Traduci: «Penso che tu abbia ragione.»",
            "Ich denke, dass du recht hast.",
            "dass + verbo alla fine: ... dass du recht hast.",
            "Usa dass e recht haben.",
            listOf("Ich denke dass du recht hast.")
        )
    )
)

internal fun unitaA2Relative(): UnitaGrammatica = unitaGrammatica(
    id = "a2-relative",
    livello = "A2",
    titolo = "Le frasi relative",
    sottotitolo = "Der Mann, den ich sehe",
    obiettivo = "Collegare informazioni su una persona o una cosa con il pronome relativo corretto.",
    minuti = 16,
    prerequisiti = listOf("a2-subordinate"),
    regola = listOf(
        "La frase relativa descrive un sostantivo e si mette tra virgole; il verbo coniugato va alla fine.",
        "Il pronome relativo prende genere e numero dall'antecedente, ma il caso dalla sua funzione nella frase relativa.",
        "Nominativo: der/die/das/die; accusativo: den/die/das/die; dativo: dem/der/dem/denen; genitivo: dessen (maschile/neutro) e deren (femminile/plurale).",
        "Se il pronome relativo è preceduto da una preposizione, la preposizione resta davanti: Der Mann, mit dem ich spreche.",
        "Preposizioni frequenti: mit dem/der/denen, auf den/die/das, von dem/der/denen.",
        "Dopo alles, nichts, etwas, vieles si usa was; per i luoghi si può usare wo: Die Stadt, wo ich lebe."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Pronomi relativi",
            intestazioni = listOf("Caso", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("Nominativo", "der", "die", "das", "die"),
                listOf("Accusativo", "den", "die", "das", "die"),
                listOf("Dativo", "dem", "der", "dem", "denen"),
                listOf("Genitivo", "dessen", "deren", "dessen", "deren")
            ),
            nota = "Il pronome relativo si declina come l'articolo determinativo, con alcune forme proprie al dativo plurale e al genitivo."
        ),
        tabellaUnita(
            titolo = "Esempi per caso",
            intestazioni = listOf("Funzione", "Esempio"),
            righe = listOf(
                listOf("Soggetto", "Der Mann, der dort steht, ist mein Chef."),
                listOf("Oggetto", "Der Mann, den ich sehe, ist mein Chef."),
                listOf("Dativo", "Der Mann, dem ich helfe, ist mein Chef."),
                listOf("Preposizione", "Der Mann, mit dem ich spreche, ist mein Chef."),
                listOf("Genitivo", "Der Mann, dessen Auto rot ist, ist mein Chef.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Der Mann, der dort steht, ist mein Chef.", "L'uomo che sta là è il mio capo."),
        esempioUnita("Das Buch, das ich lese, ist spannend.", "Il libro che leggo è avvincente."),
        esempioUnita("Die Frau, der ich helfe, ist meine Nachbarin.", "La donna a cui aiuto è la mia vicina."),
        esempioUnita("Die Kinder, mit denen ich spiele, sind nett.", "I bambini con cui gioco sono simpatici."),
        esempioUnita("Alles, was er sagt, ist wahr.", "Tutto ciò che dice è vero.")
    ),
    errori = listOf(
        erroreUnita(
            "Der Mann, der ich sehe, ...",
            "Der Mann, den ich sehe, ...",
            "Nella relativa il pronome è l'oggetto diretto: accusativo den."
        ),
        erroreUnita(
            "Die Frau, die ich helfe, ...",
            "Die Frau, der ich helfe, ...",
            "helfen regge il dativo: der Frau -> der."
        ),
        erroreUnita(
            "Das Buch, was ich lese, ...",
            "Das Buch, das ich lese, ...",
            "Il pronome relativo concorda con Buch: das. was si usa dopo alles/nichts/etwas."
        )
    ),
    merksatz = "Genere e numero dall'antecedente, caso dalla funzione. Verbo alla fine della relativa.",
    esercizi = listOf(
        sceltaUnita(
            "rel-01",
            "Der Mann, ___ dort steht, ist mein Chef.",
            listOf("der", "den", "dem", "dessen"),
            0,
            "Il pronome è soggetto della relativa: der."
        ),
        sceltaUnita(
            "rel-02",
            "Das Buch, ___ ich lese, ist spannend.",
            listOf("der", "die", "das", "den"),
            2,
            "Buch è neutro e il pronome è oggetto: das."
        ),
        sceltaUnita(
            "rel-03",
            "Die Frau, ___ ich sehe, ist meine Nachbarin.",
            listOf("der", "die", "den", "dem"),
            1,
            "Frau è femminile; all'accusativo il pronome è die."
        ),
        sceltaUnita(
            "rel-04",
            "Der Freund, ___ ich helfe, ist krank.",
            listOf("der", "den", "dem", "dessen"),
            2,
            "helfen regge il dativo: dem Freund -> dem."
        ),
        sceltaUnita(
            "rel-05",
            "Die Kinder, ___ ich spiele, sind nett.",
            listOf("mit die", "mit den", "mit denen", "mit der"),
            2,
            "Plurale al dativo dopo mit: mit denen."
        ),
        completaUnita(
            "rel-06",
            "Das ist der Mann, ___ Auto rot ist.",
            "dessen",
            "Il genitivo maschile del pronome relativo è dessen."
        ),
        completaUnita(
            "rel-07",
            "Die Frau, ___ ich spreche, ist Lehrerin.",
            "mit der",
            "sprechen mit + dativo femminile: mit der."
        ),
        completaUnita(
            "rel-08",
            "Das ist das Buch, ___ ich dir erzählt habe.",
            "von dem",
            "erzählen von + dativo neutro: von dem."
        ),
        trasformaUnita(
            "rel-09",
            "Unisci: Das ist der Mann. Er wohnt neben mir.",
            "Das ist der Mann, der neben mir wohnt.",
            "Il pronome relativo è soggetto: der; il verbo wohnt va alla fine.",
            "Scrivi la frase completa.",
            listOf("Das ist der Mann der neben mir wohnt.")
        ),
        trasformaUnita(
            "rel-10",
            "Unisci: Die Frau ist meine Tante. Ich besuche sie.",
            "Die Frau, die ich besuche, ist meine Tante.",
            "Il pronome relativo è oggetto femminile: die.",
            "Scrivi la frase completa.",
            listOf("Die Frau die ich besuche ist meine Tante.")
        ),
        completaUnita(
            "rel-11",
            "Alles, ___ er sagt, ist wahr.",
            "was",
            "Dopo alles si usa was."
        ),
        produciUnita(
            "rel-12",
            "Traduci: «La città in cui vivo è bella.»",
            "Die Stadt, in der ich lebe, ist schön.",
            "Stadt è femminile; in + dativo -> in der. Il verbo lebe va alla fine.",
            "Scrivi la frase completa.",
            listOf("Die Stadt in der ich lebe ist schön.")
        )
    )
)

internal fun unitaA2Passivo(): UnitaGrammatica = unitaGrammatica(
    id = "a2-passivo",
    livello = "A2",
    titolo = "Il passivo",
    sottotitolo = "Werden + Partizip II",
    obiettivo = "Trasformare frasi attive in passive e riconoscere il passivo nei tempi principali.",
    minuti = 16,
    prerequisiti = listOf("a2-perfekt"),
    regola = listOf(
        "Il passivo si forma con werden + Partizip II: Das Haus wird gebaut.",
        "Al Präteritum: Das Haus wurde gebaut. Al Perfekt: Das Haus ist gebaut worden.",
        "L'agente si introduce con von + dativo; lo strumento con mit + dativo: Das Bild wird von dem Künstler gemalt.",
        "Con i modali: Modalverb + Partizip II + werden: Das muss gemacht werden.",
        "Il passivo impersonale è comune: Hier wird nicht geraucht. Il passivo di stato usa sein + Partizip II: Die Tür ist geöffnet."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Attivo e passivo",
            intestazioni = listOf("Tempo", "Attivo", "Passivo"),
            righe = listOf(
                listOf("Presente", "Der Lehrer erklärt die Regel.", "Die Regel wird erklärt."),
                listOf("Präteritum", "Der Lehrer erklärte die Regel.", "Die Regel wurde erklärt."),
                listOf("Perfekt", "Der Lehrer hat die Regel erklärt.", "Die Regel ist erklärt worden."),
                listOf("Con modale", "Man muss die Regel erklären.", "Die Regel muss erklärt werden.")
            ),
            nota = "Nel passivo l'oggetto dell'attivo diventa soggetto."
        ),
        tabellaUnita(
            titolo = "Agente e strumento",
            intestazioni = listOf("Complemento", "Preposizione", "Esempio"),
            righe = listOf(
                listOf("Persona", "von + dativo", "von dem Lehrer"),
                listOf("Cosa/mezzo", "mit + dativo", "mit dem Stift"),
                listOf("Stato risultante", "sein + Partizip II", "Die Tür ist geöffnet.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Das Brot wird jeden Tag gebacken.", "Il pane viene cotto ogni giorno."),
        esempioUnita("Die Briefe wurden gestern geschrieben.", "Le lettere furono scritte ieri."),
        esempioUnita("Das Auto ist repariert worden.", "L'auto è stata riparata."),
        esempioUnita("Hier wird nicht geraucht.", "Qui non si fuma."),
        esempioUnita("Die Arbeit muss gemacht werden.", "Il lavoro deve essere fatto.")
    ),
    errori = listOf(
        erroreUnita(
            "Das Haus wird gebaut von die Firma.",
            "Die Straße wird von der Firma gebaut.",
            "von regge il dativo: von der Firma; inoltre il participio va di solito alla fine."
        ),
        erroreUnita(
            "Die Arbeit ist gemacht.",
            "Die Arbeit wird gemacht.",
            "Se descrivi un'azione in corso, usa werden + Partizip II. Ist gemacht descrive lo stato risultante."
        ),
        erroreUnita(
            "Das muss gemacht.",
            "Das muss gemacht werden.",
            "Con i modali il passivo richiede werden alla fine."
        )
    ),
    merksatz = "Passivo: werden + Partizip II. Agente: von + dativo. Con modale: ... gemacht werden.",
    esercizi = listOf(
        sceltaUnita(
            "pas-01",
            "Das Brot ___ jeden Tag gebacken.",
            listOf("wird", "werden", "ist", "wurde"),
            0,
            "Presente passivo: das Brot wird gebacken."
        ),
        sceltaUnita(
            "pas-02",
            "Die Briefe ___ gestern geschrieben.",
            listOf("wurde", "wurden", "werden", "sind"),
            1,
            "Plurale al Präteritum passivo: die Briefe wurden geschrieben."
        ),
        sceltaUnita(
            "pas-03",
            "Das Auto ___ repariert worden.",
            listOf("wird", "wurde", "ist", "hat"),
            2,
            "Perfekt passivo: ist ... worden."
        ),
        sceltaUnita(
            "pas-04",
            "Hier ___ nicht geraucht.",
            listOf("wird", "werden", "ist", "wurde"),
            0,
            "Passivo impersonale al presente: Hier wird nicht geraucht."
        ),
        sceltaUnita(
            "pas-05",
            "Die Arbeit muss ___ werden.",
            listOf("machen", "gemacht", "machte", "macht"),
            1,
            "Con il modale il passivo usa il Partizip II: gemacht werden."
        ),
        completaUnita(
            "pas-06",
            "Die Tür ___ (öffnen) jeden Morgen.",
            "wird geöffnet",
            "Presente passivo: die Tür wird geöffnet."
        ),
        completaUnita(
            "pas-07",
            "Das Problem ___ (lösen) gestern.",
            "wurde gelöst",
            "Präteritum passivo: das Problem wurde gelöst."
        ),
        completaUnita(
            "pas-08",
            "Der Kuchen ___ gebacken worden.",
            "ist",
            "Perfekt passivo: der Kuchen ist gebacken worden."
        ),
        trasformaUnita(
            "pas-09",
            "Trasforma al passivo: Der Lehrer erklärt die Regel.",
            "Die Regel wird vom Lehrer erklärt.",
            "L'oggetto die Regel diventa soggetto; l'agente è von + dativo: vom Lehrer.",
            "Scrivi la frase completa.",
            listOf("Die Regel wird von dem Lehrer erklärt.")
        ),
        trasformaUnita(
            "pas-10",
            "Trasforma al passivo: Die Firma baut das Haus.",
            "Das Haus wird von der Firma gebaut.",
            "Das Haus diventa soggetto; von der Firma è l'agente al dativo.",
            "Scrivi la frase completa."
        ),
        completaUnita(
            "pas-11",
            "Das Formular muss ___ (ausfüllen) werden.",
            "ausgefüllt",
            "Il Partizip II di ausfüllen è ausgefüllt."
        ),
        produciUnita(
            "pas-12",
            "Traduci: «La lettera è stata scritta ieri.»",
            "Der Brief wurde gestern geschrieben.",
            "Passivo al Präteritum: wurde + geschrieben.",
            "Usa der Brief e gestern."
        )
    )
)

internal fun unitaB1Adjektivdeklination(): UnitaGrammatica = unitaGrammatica(
    id = "b1-adjektivdeklination",
    livello = "B1",
    titolo = "La declinazione dell'aggettivo",
    sottotitolo = "Debole, mista e forte",
    obiettivo = "Mettere la desinenza corretta all'aggettivo davanti al sostantivo.",
    minuti = 18,
    prerequisiti = listOf("a2-dativo"),
    regola = listOf(
        "L'aggettivo davanti al sostantivo prende una desinenza; dopo il verbo (predicativo) resta invariato: Das Auto ist neu.",
        "Dopo l'articolo determinativo (der/die/das) si usa la declinazione debole: al nominativo singolare -e, negli altri casi spesso -en.",
        "Dopo l'articolo indeterminativo (ein/eine/ein), kein e i possessivi si usa la declinazione mista: il maschile e il neutro al nominativo prendono -er/-es, gli altri casi spesso -en.",
        "Senza articolo si usa la declinazione forte: l'aggettivo deve mostrare da solo il caso e il genere.",
        "Le tabelle vanno imparate per blocchi: prima il nominativo e l'accusativo, poi il dativo e il genitivo."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Declinazione debole (dopo der/die/das)",
            intestazioni = listOf("Caso", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("Nominativo", "der alte Mann", "die alte Frau", "das alte Kind", "die alten Leute"),
                listOf("Accusativo", "den alten Mann", "die alte Frau", "das alte Kind", "die alten Leute"),
                listOf("Dativo", "dem alten Mann", "der alten Frau", "dem alten Kind", "den alten Leuten"),
                listOf("Genitivo", "des alten Mannes", "der alten Frau", "des alten Kindes", "der alten Leute")
            )
        ),
        tabellaUnita(
            titolo = "Declinazione mista (dopo ein/kein/possessivi)",
            intestazioni = listOf("Caso", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("Nominativo", "ein alter Mann", "eine alte Frau", "ein altes Kind", "keine alten Leute"),
                listOf("Accusativo", "einen alten Mann", "eine alte Frau", "ein altes Kind", "keine alten Leute"),
                listOf("Dativo", "einem alten Mann", "einer alten Frau", "einem alten Kind", "keinen alten Leuten")
            ),
            nota = "Al nominativo maschile e neutro l'aggettivo prende la desinenza che mancherebbe all'articolo: -er, -es."
        ),
        tabellaUnita(
            titolo = "Declinazione forte (senza articolo)",
            intestazioni = listOf("Caso", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("Nominativo", "alter Mann", "alte Frau", "altes Kind", "alte Leute"),
                listOf("Accusativo", "alten Mann", "alte Frau", "altes Kind", "alte Leute"),
                listOf("Dativo", "altem Mann", "alter Frau", "altem Kind", "alten Leuten")
            ),
            nota = "La desinenza forte è quella che rende riconoscibile il caso anche senza articolo."
        )
    ),
    esempi = listOf(
        esempioUnita("Der alte Mann liest eine Zeitung.", "L'uomo anziano legge un giornale."),
        esempioUnita("Ich sehe den kleinen Jungen.", "Vedo il cane piccolo."),
        esempioUnita("Das ist ein guter Roman.", "Questo è un buon film."),
        esempioUnita("Mit dem neuen Auto fahre ich nach Berlin.", "Con la nuova auto vado a Berlino."),
        esempioUnita("Kaltes Wasser ist gesund.", "L'acqua fredda è sana.")
    ),
    errori = listOf(
        erroreUnita(
            "Der neue Auto ist schnell.",
            "Das neue Auto ist schnell.",
            "Auto è neutro: das neue Auto."
        ),
        erroreUnita(
            "Ein neu Auto steht dort.",
            "Ein neues Auto steht dort.",
            "Dopo ein, al nominativo neutro, l'aggettivo prende -es: ein neues Auto."
        ),
        erroreUnita(
            "Ich sehe den klein Hund.",
            "Ich sehe den kleinen Jungen.",
            "Dopo den, all'accusativo maschile, l'aggettivo prende -en."
        )
    ),
    merksatz = "Dopo der/die/das: -e/-en. Dopo ein/kein: -er/-e/-es. Senza articolo: desinenza forte.",
    esercizi = listOf(
        sceltaUnita(
            "adj-01",
            "Der ___ Mann kommt.",
            listOf("alt", "alte", "alten", "alter"),
            1,
            "Dopo der, nominativo maschile, l'aggettivo prende -e: der alte Mann."
        ),
        sceltaUnita(
            "adj-02",
            "Die ___ Frau singt.",
            listOf("schön", "schöne", "schönen", "schöner"),
            1,
            "Dopo die, nominativo femminile, l'aggettivo prende -e: die schöne Frau."
        ),
        sceltaUnita(
            "adj-03",
            "Das ___ Kind spielt.",
            listOf("klein", "kleine", "kleinen", "kleiner"),
            1,
            "Dopo das, nominativo neutro, l'aggettivo prende -e: das kleine Kind."
        ),
        sceltaUnita(
            "adj-04",
            "Ein ___ Auto steht dort.",
            listOf("neu", "neue", "neues", "neuen"),
            2,
            "Dopo ein, nominativo neutro, l'aggettivo prende -es: ein neues Auto."
        ),
        sceltaUnita(
            "adj-05",
            "Ich sehe den ___ Mann.",
            listOf("alt", "alte", "alten", "alter"),
            2,
            "Dopo den, accusativo maschile, l'aggettivo prende -en: den alten Mann."
        ),
        completaUnita(
            "adj-06",
            "Ich habe ein ___ Buch. (neu)",
            "neues",
            "Dopo ein, accusativo neutro, l'aggettivo prende -es: ein neues Buch."
        ),
        completaUnita(
            "adj-07",
            "Die ___ Schuhe sind teuer. (rot)",
            "roten",
            "Dopo die, plurale, l'aggettivo prende -en: die roten Schuhe."
        ),
        completaUnita(
            "adj-08",
            "Mit dem ___ Auto fahre ich. (neu)",
            "neuen",
            "Dopo dem, dativo neutro, l'aggettivo prende -en: mit dem neuen Auto."
        ),
        trasformaUnita(
            "adj-09",
            "Inserisci l'aggettivo: Das ist ein / gut / Film.",
            "Das ist ein guter Film.",
            "Dopo ein, nominativo maschile, l'aggettivo prende -er: ein guter Film."
        ),
        trasformaUnita(
            "adj-10",
            "Inserisci l'aggettivo: Ich sehe den / klein / Hund.",
            "Ich sehe den kleinen Hund.",
            "Dopo den, accusativo maschile, l'aggettivo prende -en."
        ),
        completaUnita(
            "adj-11",
            "___ Wasser ist kalt. (kalt, senza articolo)",
            "Kaltes",
            "Senza articolo, nominativo neutro, l'aggettivo prende -es: kaltes Wasser."
        ),
        produciUnita(
            "adj-12",
            "Traduci: «La nuova casa è grande.»",
            "Das neue Haus ist groß.",
            "Haus è neutro; dopo das l'aggettivo prende -e: das neue Haus.",
            "Usa das neue Haus."
        )
    )
)

internal fun unitaB1Konnektoren(): UnitaGrammatica = unitaGrammatica(
    id = "b1-konnektoren",
    livello = "B1",
    titolo = "Connettori e connettivi",
    sottotitolo = "Coordinanti, subordinanti e avverbiali",
    obiettivo = "Collegare frasi e idee con il connettore giusto e la posizione verbale corretta.",
    minuti = 18,
    prerequisiti = listOf("a2-subordinate"),
    regola = listOf(
        "I connettori coordinanti (und, aber, oder, denn, sondern) non cambiano l'ordine della frase: Ich bleibe zu Hause, denn ich bin krank.",
        "I connettori subordinanti (weil, obwohl, wenn, dass, damit, während, bevor, nachdem) mandano il verbo coniugato alla fine.",
        "I connettivi avverbiali (deshalb, darum, trotzdem, außerdem, sonst, dann) occupano la prima posizione e richiedono il verbo in seconda: Ich bin krank, deshalb bleibe ich im Bett.",
        "sondern si usa dopo una negazione per correggere: Er ist nicht groß, sondern klein.",
        "Le coppie più comuni sono nicht nur ... sondern auch, sowohl ... als auch, entweder ... oder, weder ... noch, je ... desto."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Tipi di connettore",
            intestazioni = listOf("Tipo", "Esempi", "Posizione del verbo"),
            righe = listOf(
                listOf("Coordinante", "und, aber, oder, denn, sondern", "nessun cambiamento"),
                listOf("Subordinante", "weil, obwohl, wenn, dass, damit", "verbo alla fine"),
                listOf("Avverbiale", "deshalb, darum, trotzdem, außerdem", "verbo in seconda posizione"),
                listOf("Coppia", "nicht nur ... sondern auch", "dipende dalla struttura")
            ),
            nota = "Prima di scegliere il connettore, decidi che tipo di relazione vuoi esprimere: causa, contrasto, conseguenza, aggiunta."
        ),
        tabellaUnita(
            titolo = "Posizione in pratica",
            intestazioni = listOf("Frase", "Connettore", "Verbo"),
            righe = listOf(
                listOf("Ich bleibe zu Hause, weil ich krank bin.", "weil", "fine"),
                listOf("Ich bin krank, deshalb bleibe ich im Bett.", "deshalb", "seconda"),
                listOf("Ich bin krank, aber ich gehe zur Arbeit.", "aber", "seconda"),
                listOf("Obwohl es regnet, gehen wir ins Kino.", "obwohl", "fine")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich bleibe zu Hause, weil ich krank bin.", "Resto a casa perché sono malato."),
        esempioUnita("Ich bin krank, deshalb bleibe ich im Bett.", "Sono malato, perciò resto a casa."),
        esempioUnita("Er ist nicht groß, sondern klein.", "Non è alto, ma basso."),
        esempioUnita("Obwohl es regnet, gehen wir ins Kino.", "Sebbene piova, andiamo al cinema."),
        esempioUnita("Ich lerne nicht nur Deutsch, sondern auch Spanisch.", "Non imparo solo il tedesco, ma anche lo spagnolo.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich bleibe zu Hause, weil ich bin krank.",
            "Ich bleibe zu Hause, weil ich krank bin.",
            "weil è subordinante: il verbo coniugato va alla fine."
        ),
        erroreUnita(
            "Deshalb ich bleibe zu Hause.",
            "Deshalb bleibe ich zu Hause.",
            "deshalb occupa la prima posizione; il verbo viene subito dopo."
        ),
        erroreUnita(
            "Er ist nicht groß, aber klein.",
            "Er ist nicht groß, sondern klein.",
            "Dopo una negazione, per correggere si usa sondern."
        )
    ),
    merksatz = "Coordinanti: ordine normale. Subordinanti: verbo alla fine. Avverbiali: verbo in seconda.",
    esercizi = listOf(
        sceltaUnita(
            "kon-01",
            "Er lernt Deutsch, ___ er in Berlin arbeiten will.",
            listOf("weil", "denn", "deshalb", "aber"),
            0,
            "weil introduce la causa e manda il verbo alla fine."
        ),
        sceltaUnita(
            "kon-02",
            "Ich bin krank, ___ bleibe ich zu Hause.",
            listOf("weil", "deshalb", "obwohl", "dass"),
            1,
            "deshalb esprime la conseguenza e richiede il verbo in seconda posizione."
        ),
        sceltaUnita(
            "kon-03",
            "Er ist nicht groß, ___ klein.",
            listOf("aber", "sondern", "oder", "denn"),
            1,
            "Dopo una negazione, per correggere si usa sondern."
        ),
        sceltaUnita(
            "kon-04",
            "___ ich Zeit habe, komme ich.",
            listOf("Wenn", "Dass", "Deshalb", "Aber"),
            0,
            "wenn introduce una condizione e manda il verbo alla fine."
        ),
        sceltaUnita(
            "kon-05",
            "Sie lernt Deutsch, ___ sie in Berlin studieren will.",
            listOf("weil", "deshalb", "aber", "sondern"),
            0,
            "Il motivo si esprime con weil."
        ),
        completaUnita(
            "kon-06",
            "Ich bin müde, ___ gehe ich schlafen.",
            "deshalb",
            "La conseguenza si esprime con deshalb + verbo in seconda posizione."
        ),
        completaUnita(
            "kon-07",
            "Er kommt nicht, ___ er krank ist.",
            "weil",
            "La causa si esprime con weil; il verbo ist è alla fine."
        ),
        completaUnita(
            "kon-08",
            "___ es regnet, gehen wir spazieren.",
            "Obwohl",
            "Il contrasto si esprime con obwohl; il verbo regnet è alla fine."
        ),
        trasformaUnita(
            "kon-09",
            "Unisci con deshalb: Ich bin krank. Ich bleibe zu Hause.",
            "Ich bin krank, deshalb bleibe ich zu Hause.",
            "deshalb occupa la prima posizione della seconda frase; il verbo bleibe viene dopo."
        ),
        trasformaUnita(
            "kon-10",
            "Unisci con obwohl: Es regnet. Wir gehen spazieren.",
            "Obwohl es regnet, gehen wir spazieren.",
            "obwohl introduce la subordinata con il verbo alla fine; la principale segue con il verbo in seconda posizione.",
            "Scrivi la frase completa.",
            listOf("Obwohl es regnet gehen wir spazieren.")
        ),
        completaUnita(
            "kon-11",
            "Ich habe ___ Zeit noch Geld.",
            "weder",
            "La coppia weder ... noch significa «né ... né»: weder Zeit noch Geld.",
            "Scrivi solo la prima parola della coppia."
        ),
        produciUnita(
            "kon-12",
            "Traduci: «Non solo imparo il tedesco, ma anche l'inglese.»",
            "Ich lerne nicht nur Deutsch, sondern auch Englisch.",
            "La coppia nicht nur ... sondern auch collega i due elementi.",
            "Usa nicht nur ... sondern auch."
        )
    )
)

internal fun unitaB1Konjunktiv2(): UnitaGrammatica = unitaGrammatica(
    id = "b1-konjunktiv2",
    livello = "B1",
    titolo = "Il Konjunktiv II",
    sottotitolo = "Ipotesi, desideri e richieste cortesi",
    obiettivo = "Esprimere situazioni irreali, desideri e formule di cortesia.",
    minuti = 18,
    prerequisiti = listOf("a2-praeteritum"),
    regola = listOf(
        "Il Konjunktiv II si usa per ipotesi irreali, desideri e richieste cortesi: Wenn ich Zeit hätte, würde ich kommen.",
        "Per molti verbi si usa würde + infinito: ich würde kommen, du würdest lernen.",
        "I verbi più frequenti hanno forme proprie: sein -> wäre, haben -> hätte, werden -> würde, können -> könnte, müssen -> müsste, dürfen -> dürfte, wissen -> wüsste.",
        "Nel periodo ipotetico: Wenn ich reich wäre, würde ich reisen. La principale può anche iniziare con dann: Dann würde ich reisen.",
        "Il passato irreale si forma con hätte/wäre + Partizip II: Wenn ich das gewusst hätte, wäre ich gekommen."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Forme importanti",
            intestazioni = listOf("Infinito", "Konjunktiv II", "Esempio"),
            righe = listOf(
                listOf("sein", "wäre", "Ich wäre gerne in Berlin."),
                listOf("haben", "hätte", "Ich hätte eine Frage."),
                listOf("werden", "würde", "Ich würde kommen."),
                listOf("können", "könnte", "Könnten Sie helfen?"),
                listOf("müssen", "müsste", "Ich müsste lernen."),
                listOf("dürfen", "dürfte", "Dürfte ich hereinkommen?"),
                listOf("wissen", "wüsste", "Ich wüsste es nicht.")
            )
        ),
        tabellaUnita(
            titolo = "Periodo ipotetico",
            intestazioni = listOf("Tipo", "Esempio"),
            righe = listOf(
                listOf("Presente irreale", "Wenn ich Zeit hätte, würde ich kommen."),
                listOf("Passato irreale", "Wenn ich das gewusst hätte, wäre ich gekommen."),
                listOf("Richiesta cortese", "Könnten Sie mir bitte helfen?"),
                listOf("Desiderio", "Ich wäre jetzt gerne am Meer.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Wenn ich Zeit hätte, würde ich kommen.", "Se avessi tempo, verrei."),
        esempioUnita("Wenn ich reich wäre, würde ich reisen.", "Se fossi ricco, viaggerei."),
        esempioUnita("Könnten Sie mir bitte helfen?", "Potrebbe aiutarmi, per favore?"),
        esempioUnita("Ich hätte gerne einen Kaffee.", "Vorrei un caffè."),
        esempioUnita("Wenn ich das gewusst hätte, wäre ich gekommen.", "Se l'avessi saputo, sarei venuto.")
    ),
    errori = listOf(
        erroreUnita(
            "Wenn ich würde reich sein, ...",
            "Wenn ich reich wäre, ...",
            "Per sein si usa la forma propria wäre, non würde sein."
        ),
        erroreUnita(
            "Ich würde können kommen.",
            "Ich könnte kommen.",
            "können ha una forma propria al Konjunktiv II: könnte."
        ),
        erroreUnita(
            "Wenn ich Zeit hätte, ich würde kommen.",
            "Wenn ich Zeit hätte, würde ich kommen.",
            "Dopo la subordinata, nella principale il verbo va in seconda posizione: würde ich ..."
        )
    ),
    merksatz = "wäre, hätte, würde, könnte, müsste. Periodo ipotetico: Wenn ..., würde ...",
    esercizi = listOf(
        sceltaUnita(
            "konj-01",
            "Wenn ich Zeit ___, würde ich kommen.",
            listOf("habe", "hätte", "hatte", "haben"),
            1,
            "Ipotesi irreale: wenn ich Zeit hätte."
        ),
        sceltaUnita(
            "konj-02",
            "Wenn ich reich ___, würde ich reisen.",
            listOf("bin", "war", "wäre", "werde"),
            2,
            "sein al Konjunktiv II: wäre."
        ),
        sceltaUnita(
            "konj-03",
            "___ Sie bitte leiser sprechen?",
            listOf("Können", "Könnten", "Konnten", "Könnt"),
            1,
            "Richiesta cortese: Könnten Sie bitte leiser sprechen?"
        ),
        sceltaUnita(
            "konj-04",
            "Ich ___ gerne einen Kaffee.",
            listOf("habe", "hatte", "hätte", "haben"),
            2,
            "Formula cortese: ich hätte gerne ..."
        ),
        sceltaUnita(
            "konj-05",
            "Wenn ich das gewusst ___, wäre ich gekommen.",
            listOf("habe", "hatte", "hätte", "haben"),
            2,
            "Passato irreale: hätte + Partizip II."
        ),
        completaUnita(
            "konj-06",
            "Ich ___ (sein) gerne in Berlin.",
            "wäre",
            "sein al Konjunktiv II: wäre."
        ),
        completaUnita(
            "konj-07",
            "Wir ___ (haben) gerne mehr Zeit.",
            "hätten",
            "haben al Konjunktiv II: wir hätten."
        ),
        completaUnita(
            "konj-08",
            "Er ___ (können) besser lernen.",
            "könnte",
            "können al Konjunktiv II: er könnte."
        ),
        trasformaUnita(
            "konj-09",
            "Trasforma in Konjunktiv II: Ich habe keine Zeit.",
            "Ich hätte keine Zeit.",
            "haben -> hätte."
        ),
        trasformaUnita(
            "konj-10",
            "Trasforma in Konjunktiv II: Ich kann nicht kommen.",
            "Ich könnte nicht kommen.",
            "können -> könnte."
        ),
        completaUnita(
            "konj-11",
            "Wenn ich du ___ (sein), würde ich mitkommen.",
            "wäre",
            "Periodo ipotetico con sein: wenn ich du wäre."
        ),
        produciUnita(
            "konj-12",
            "Traduci: «Potresti aiutarmi?»",
            "Könntest du mir helfen?",
            "Richiesta cortese: Könntest du ...?",
            "Usa können al Konjunktiv II."
        )
    )
)

internal fun unitaB1IndirekteRede(): UnitaGrammatica = unitaGrammatica(
    id = "b1-indirekte-rede",
    livello = "B1",
    titolo = "Il discorso indiretto",
    sottotitolo = "Konjunktiv I e domande riportate",
    obiettivo = "Riferire affermazioni, domande e ordini senza usare le virgolette.",
    minuti = 18,
    prerequisiti = listOf("b1-konjunktiv2"),
    regola = listOf(
        "Nel discorso indiretto si riferisce ciò che ha detto qualcun altro senza virgolette; spesso si usa il Konjunktiv I per prendere le distanze dalla notizia.",
        "Konjunktiv I di sein è sei, di haben è habe, di werden è werde, di können è könne, di müssen è müsse.",
        "Per i verbi regolari il Konjunktiv I si forma con la radice + -e: er komme, sie gehe, sie habe.",
        "Le forme di ich e du spesso coincidono con l'indicativo; in questi casi si usa il Konjunktiv II per evitare ambiguità: Ich sagte, ich hätte keine Zeit.",
        "Le domande indirette usano ob per le domande sì/no e il W-Wort per le domande con W-Wort; il verbo va alla fine. Gli ordini si riportano con sollen o mögen."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Konjunktiv I",
            intestazioni = listOf("Pronome", "sein", "haben", "kommen"),
            righe = listOf(
                listOf("ich", "sei", "habe", "komme"),
                listOf("du", "seist", "habest", "kommest"),
                listOf("er / sie / es", "sei", "habe", "komme"),
                listOf("wir", "seien", "haben", "kommen"),
                listOf("ihr", "seiet", "habet", "kommet"),
                listOf("sie / Sie", "seien", "haben", "kommen")
            ),
            nota = "Le forme di wir e sie spesso coincidono con l'indicativo; per chiarezza si può usare il Konjunktiv II."
        ),
        tabellaUnita(
            titolo = "Da diretto a indiretto",
            intestazioni = listOf("Diretto", "Indiretto"),
            righe = listOf(
                listOf("Er sagt: «Ich bin müde.»", "Er sagt, er sei müde."),
                listOf("Sie sagt: «Ich bin beschäftigt.»", "Sie sagt, sie sei beschäftigt."),
                listOf("Er fragt: «Kommst du?»", "Er fragt, ob ich komme."),
                listOf("Sie fragt: «Wo wohnst du?»", "Sie fragt, wo ich wohne."),
                listOf("Er sagt: «Komm!»", "Er sagt, ich solle kommen.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Er sagt, er sei müde.", "Dice che è stanco."),
        esempioUnita("Sie sagt, sie sei beschäftigt.", "Dice che è occupata."),
        esempioUnita("Der Lehrer sagt, wir müssten mehr lernen.", "L'insegnante dice che dovremmo studiare di più."),
        esempioUnita("Er fragt, ob ich Zeit habe.", "Chiede se ho tempo."),
        esempioUnita("Sie fragte, wo ich wohne.", "Chiese dove abito.")
    ),
    errori = listOf(
        erroreUnita(
            "Er sagt, er ist krank.",
            "Er sagt, er sei müde.",
            "Nel discorso indiretto formale si usa il Konjunktiv I: sei."
        ),
        erroreUnita(
            "Sie fragte, ob kommst du.",
            "Sie fragte, ob ich komme.",
            "Dopo ob il verbo va alla fine: ... ob ich komme."
        ),
        erroreUnita(
            "Er sagt, dass er kommt.",
            "Er sagt, er komme.",
            "La frase con dass è possibile, ma il discorso indiretto richiede spesso il Konjunktiv I."
        )
    ),
    merksatz = "Discorso indiretto: Konjunktiv I. Domande: ob/W-Wort + verbo alla fine.",
    esercizi = listOf(
        sceltaUnita(
            "ind-01",
            "Er sagt, er ___ krank.",
            listOf("ist", "sei", "war", "wäre"),
            1,
            "Konjunktiv I di sein: er sei."
        ),
        sceltaUnita(
            "ind-02",
            "Sie sagt, sie ___ keine Zeit.",
            listOf("hat", "habe", "hatte", "hätte"),
            1,
            "Konjunktiv I di haben: sie habe."
        ),
        sceltaUnita(
            "ind-03",
            "Der Lehrer sagt, wir ___ mehr lernen.",
            listOf("müssen", "müssten", "mussten", "müsst"),
            1,
            "Poiché müssen al Konjunktiv I coincide con l'indicativo, si usa il Konjunktiv II müssten."
        ),
        sceltaUnita(
            "ind-04",
            "Er fragt, ___ ich Zeit habe.",
            listOf("dass", "ob", "wenn", "weil"),
            1,
            "La domanda indiretta sì/no si introduce con ob."
        ),
        sceltaUnita(
            "ind-05",
            "Sie fragte, wo ich ___.",
            listOf("wohne", "wohnst", "wohnen", "wohnte"),
            0,
            "Dopo il W-Wort il verbo va alla fine: ... wo ich wohne."
        ),
        completaUnita(
            "ind-06",
            "Er sagt, er ___ (sein) müde.",
            "sei",
            "Konjunktiv I di sein alla terza persona: sei."
        ),
        completaUnita(
            "ind-07",
            "Sie sagt, sie ___ (haben) Recht.",
            "habe",
            "Konjunktiv I di haben alla terza persona: habe."
        ),
        completaUnita(
            "ind-08",
            "Der Chef sagt, ich ___ (sollen) kommen.",
            "solle",
            "Konjunktiv I di sollen: ich solle."
        ),
        trasformaUnita(
            "ind-09",
            "Trasforma in discorso indiretto: Er sagt: «Ich bin krank.»",
            "Er sagt, er sei krank.",
            "La frase diventa una subordinata senza dass; il verbo è al Konjunktiv I: sei."
        ),
        trasformaUnita(
            "ind-10",
            "Trasforma in discorso indiretto: Sie sagt: «Ich habe keine Zeit.»",
            "Sie sagt, sie habe keine Zeit.",
            "Konjunktiv I di haben: habe."
        ),
        completaUnita(
            "ind-11",
            "Er fragt, ___ ich komme.",
            "ob",
            "Domanda indiretta sì/no: ob."
        ),
        produciUnita(
            "ind-12",
            "Traduci: «Lei dice che viene domani.»",
            "Sie sagt, sie komme morgen.",
            "Discorso indiretto con Konjunktiv I: sie komme.",
            "Usa sagen e il Konjunktiv I di kommen."
        )
    )
)

internal fun unitaB1Nominalisierung(): UnitaGrammatica = unitaGrammatica(
    id = "b1-nominalisierung",
    livello = "B1",
    titolo = "La nominalizzazione",
    sottotitolo = "Trasformare verbi e aggettivi in nomi",
    obiettivo = "Riconoscere e usare i nomi derivati da verbi e aggettivi nello stile formale.",
    minuti = 16,
    prerequisiti = listOf("b1-adjektivdeklination"),
    regola = listOf(
        "In tedesco molti verbi e aggettivi diventano nomi; i nomi si scrivono sempre con la maiuscola.",
        "L'infinito sostantivato è sempre neutro: das Essen, das Lernen, das Lesen, das Schlafen.",
        "I suffissi più comuni sono -ung (die Prüfung), -heit (die Freiheit), -keit (die Möglichkeit), -schaft (die Freundschaft), -nis (das Ergebnis), -tum (das Wachstum).",
        "Le preposizioni si combinano spesso con i nomi: beim Lernen, zum Essen, nach dem Essen, vor dem Schlafen.",
        "Lo stile nominale è tipico della lingua scritta e formale; nel parlato si preferisce spesso la frase verbale."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Verbo -> nome",
            intestazioni = listOf("Verbo", "Nome", "Genere"),
            righe = listOf(
                listOf("lernen", "das Lernen", "neutro"),
                listOf("essen", "das Essen", "neutro"),
                listOf("prüfen", "die Prüfung", "femminile"),
                listOf("entscheiden", "die Entscheidung", "femminile"),
                listOf("ergebnis", "das Ergebnis", "neutro"),
                listOf("wachsen", "das Wachstum", "neutro")
            ),
            nota = "L'infinito sostantivato mantiene il genere neutro: das Lernen, das Essen."
        ),
        tabellaUnita(
            titolo = "Aggettivo -> nome",
            intestazioni = listOf("Aggettivo", "Nome", "Suffisso"),
            righe = listOf(
                listOf("frei", "die Freiheit", "-heit"),
                listOf("möglich", "die Möglichkeit", "-keit"),
                listOf("freundlich", "die Freundlichkeit", "-keit"),
                listOf("sicher", "die Sicherheit", "-heit"),
                listOf("bekannt", "die Bekanntschaft", "-schaft")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Das Lernen macht Spaß.", "Imparare è divertente."),
        esempioUnita("Ich gehe zum Essen.", "Vado a mangiare."),
        esempioUnita("Beim Lesen höre ich Musik.", "Mentre leggo ascolto musica."),
        esempioUnita("Die Prüfung war schwierig.", "L'esame è stato difficile."),
        esempioUnita("Die Entscheidung ist gut.", "La possibilità è buona.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich gehe zum essen.",
            "Ich gehe zum Essen.",
            "I nomi si scrivono con la maiuscola: das Essen -> zum Essen."
        ),
        erroreUnita(
            "Das lernen ist wichtig.",
            "Das Lernen ist wichtig.",
            "L'infinito sostantivato è un nome: Lernen."
        ),
        erroreUnita(
            "die Möglich",
            "die Möglichkeit",
            "Il nome derivato da möglich è die Möglichkeit, con il suffisso -keit."
        )
    ),
    merksatz = "Nomi sempre con la maiuscola. Infinito sostantivato: neutro (das Lernen). Suffissi: -ung, -heit, -keit.",
    esercizi = listOf(
        sceltaUnita(
            "nom-01",
            "Das ___ ist wichtig. (lernen)",
            listOf("lernen", "Lernen", "Lern", "Lernung"),
            1,
            "L'infinito sostantivato è das Lernen."
        ),
        sceltaUnita(
            "nom-02",
            "Die ___ war schwierig. (prüfen)",
            listOf("Prüfen", "Prüfung", "Prüfheit", "Prüfkeit"),
            1,
            "Da prüfen si forma die Prüfung."
        ),
        sceltaUnita(
            "nom-03",
            "Die ___ ist wichtig. (frei)",
            listOf("Frei", "Freiheit", "Freitung", "Freikeit"),
            1,
            "Da frei si forma die Freiheit."
        ),
        sceltaUnita(
            "nom-04",
            "Die ___ fehlt. (möglich)",
            listOf("Möglich", "Möglichkeit", "Möglichung", "Möglichheit"),
            1,
            "Da möglich si forma die Möglichkeit."
        ),
        sceltaUnita(
            "nom-05",
            "Das ___ ist neu. (ergebnis)",
            listOf("Ergebnis", "Ergebnisung", "Ergebnheit", "Ergebnkeit"),
            0,
            "Ergebnis è già un nome: das Ergebnis."
        ),
        completaUnita(
            "nom-06",
            "Ich gehe zum ___ (essen).",
            "Essen",
            "L'infinito sostantivato è das Essen: zum Essen."
        ),
        completaUnita(
            "nom-07",
            "Beim ___ (lesen) höre ich Musik.",
            "Lesen",
            "L'infinito sostantivato è das Lesen: beim Lesen."
        ),
        completaUnita(
            "nom-08",
            "Die ___ (freundlich) ist wichtig.",
            "Freundlichkeit",
            "Da freundlich si forma die Freundlichkeit."
        ),
        trasformaUnita(
            "nom-09",
            "Nominalizza: Ich lerne Deutsch. -> Das ___ von Deutsch ist wichtig.",
            "Lernen",
            "Il verbo lernen diventa il nome neutro das Lernen."
        ),
        trasformaUnita(
            "nom-10",
            "Nominalizza: Wir prüfen das Projekt. -> Die ___ des Projekts ...",
            "Prüfung",
            "Da prüfen si forma die Prüfung."
        ),
        completaUnita(
            "nom-11",
            "Nach dem ___ (schlafen) frühstücke ich.",
            "Schlafen",
            "L'infinito sostantivato è das Schlafen: nach dem Schlafen."
        ),
        produciUnita(
            "nom-12",
            "Traduci: «La possibilità è buona.»",
            "Die Möglichkeit ist gut.",
            "möglich -> die Möglichkeit; l'aggettivo predicativo resta gut.",
            "Usa die Möglichkeit."
        )
    )
)

internal fun unitaB1Genitiv(): UnitaGrammatica = unitaGrammatica(
    id = "b1-genitiv",
    livello = "B1",
    titolo = "Il genitivo",
    sottotitolo = "Des Vaters, der Frau, meiner Schwester",
    obiettivo = "Esprimere possesso e usare le preposizioni che reggono il genitivo.",
    minuti = 16,
    prerequisiti = listOf("a2-dativo"),
    regola = listOf(
        "Il genitivo risponde a Wessen? (di chi?) e indica possesso o appartenenza.",
        "Maschile e neutro prendono des + -s/-es: des Mannes, des Kindes; femminile e plurale usano der: der Frau, der Kinder.",
        "Gli articoli indeterminativi e i possessivi diventano eines/einer/eines e meines, deines, seines, ihres, unseres, eures, ihres.",
        "Le preposizioni wegen, trotz, während, statt, innerhalb, außerhalb, aufgrund reggono il genitivo.",
        "Nel parlato il genitivo è spesso sostituito da von + dativo: das Auto von meinem Vater; nello scritto formale si preferisce das Auto meines Vaters."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Articoli al genitivo",
            intestazioni = listOf("", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("Definito", "des Mannes", "der Frau", "des Kindes", "der Kinder"),
                listOf("Indefinito", "eines Mannes", "einer Frau", "eines Kindes", "keiner Kinder"),
                listOf("Possessivo", "meines Vaters", "meiner Mutter", "meines Kindes", "meiner Eltern")
            ),
            nota = "Il maschile e il neutro aggiungono -s o -es al sostantivo."
        ),
        tabellaUnita(
            titolo = "Preposizioni con genitivo",
            intestazioni = listOf("Preposizione", "Significato", "Esempio"),
            righe = listOf(
                listOf("wegen", "a causa di", "wegen des Wetters"),
                listOf("trotz", "nonostante", "trotz des Regens"),
                listOf("während", "durante", "während des Unterrichts"),
                listOf("statt", "invece di", "statt des Autos"),
                listOf("innerhalb", "all'interno di", "innerhalb einer Woche"),
                listOf("außerhalb", "al di fuori di", "außerhalb der Stadt")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Das ist das Fahrrad meines Bruders.", "Questa è la bicicletta di mio fratello."),
        esempioUnita("Die Tasche der Lehrerin ist rot.", "La borsa della professoressa è rossa."),
        esempioUnita("Die Bücher der Kinder sind neu.", "I libri dei bambini sono nuovi."),
        esempioUnita("Wegen des Wetters bleiben wir zu Hause.", "A causa del tempo restiamo a casa."),
        esempioUnita("Trotz des Regens gehen wir spazieren.", "Nonostante la pioggia andiamo a passeggiare.")
    ),
    errori = listOf(
        erroreUnita(
            "Das ist das Auto von meines Vaters.",
            "Das ist das Fahrrad meines Bruders.",
            "Il genitivo non si combina con von: si usa direttamente meines Vaters."
        ),
        erroreUnita(
            "Wegen dem Wetter bleiben wir zu Hause.",
            "Wegen des Wetters bleiben wir zu Hause.",
            "Nello scritto formale wegen regge il genitivo: wegen des Wetters."
        ),
        erroreUnita(
            "Die Tasche des Frau.",
            "Die Tasche der Lehrerin.",
            "Frau è femminile: il genitivo usa der, non des."
        )
    ),
    merksatz = "Genitivo: des + -s/-es (masc./neutro), der (fem./plur.). Preposizioni: wegen, trotz, während.",
    esercizi = listOf(
        sceltaUnita(
            "gen-01",
            "Das ist das Auto ___ Mannes.",
            listOf("der", "den", "dem", "des"),
            3,
            "Genitivo maschile: des Mannes."
        ),
        sceltaUnita(
            "gen-02",
            "Die Tasche ___ Frau ist rot.",
            listOf("der", "die", "des", "dem"),
            0,
            "Genitivo femminile: der Frau."
        ),
        sceltaUnita(
            "gen-03",
            "Die Bücher ___ Kinder sind neu.",
            listOf("der", "den", "des", "dem"),
            0,
            "Genitivo plurale: der Kinder."
        ),
        sceltaUnita(
            "gen-04",
            "Wegen ___ Wetters bleiben wir zu Hause.",
            listOf("dem", "den", "des", "der"),
            2,
            "wegen regge il genitivo: wegen des Wetters."
        ),
        sceltaUnita(
            "gen-05",
            "Trotz ___ Regens gehen wir spazieren.",
            listOf("dem", "den", "des", "der"),
            2,
            "trotz regge il genitivo: trotz des Regens."
        ),
        completaUnita(
            "gen-06",
            "Das ist das Haus ___ Vaters. (mein)",
            "meines",
            "Il possessivo maschile al genitivo è meines: meines Vaters."
        ),
        completaUnita(
            "gen-07",
            "Das ist das Buch ___ Schwester. (mein)",
            "meiner",
            "Il possessivo femminile al genitivo è meiner: meiner Schwester."
        ),
        completaUnita(
            "gen-08",
            "Die Farbe ___ Autos ist blau. (das)",
            "des",
            "Genitivo neutro: des Autos."
        ),
        trasformaUnita(
            "gen-09",
            "Trasforma con il genitivo: Das ist das Auto von meinem Vater.",
            "Das ist das Auto meines Vaters.",
            "von meinem Vater -> meines Vaters."
        ),
        trasformaUnita(
            "gen-10",
            "Trasforma con il genitivo: Die Tasche von der Frau.",
            "Die Tasche der Frau.",
            "von der Frau -> der Frau."
        ),
        completaUnita(
            "gen-11",
            "Während ___ Unterrichts darf man nicht telefonieren. (der)",
            "des",
            "während regge il genitivo: während des Unterrichts."
        ),
        produciUnita(
            "gen-12",
            "Traduci: «Il libro di mia sorella è nuovo.»",
            "Das Buch meiner Schwester ist neu.",
            "Genitivo femminile del possessivo: meiner Schwester.",
            "Usa das Buch e meiner Schwester."
        )
    )
)
