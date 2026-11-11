package com.alessandro.openlingo.data

/**
 * Helper compatti per scrivere le unità del percorso senza ripetere
 * continuamente i nomi dei campi. Restano semplici funzioni di fabbrica:
 * non cambiano il modello salvato né la logica di valutazione.
 */

/**
 * BANCHI ESERCIZI EXTRA per unità.
 *
 * Fonte: repertorio di reference (Grammatik Aktiv A1-B1) + autoring dedicato.
 * Ogni chiave è un id di unità di [GrammaticaSyllabus]; il valore è un banco
 * di esercizi INEDITI dello stesso formato. Vengono appesi agli esercizi
 * "core" dell'unità: più materia per ogni argomento, sempre qualcosa di nuovo
 * da affrontare.
 */
val EserciziExtra: Map<String, List<EsercizioUnita>> = mapOf(

    // ============================ A0 ============================

    // ---- A0: sein e pronomi soggetto ----
    "a0-pronomi-sein" to listOf(
        sceltaUnita("a0-pronomi-sein-xe1", "___ bist müde, Adam.", listOf("Du", "Sie", "Dich", "Deine"), 0,
            "du + bist: 2ª persona singolare di sein."),
        completaUnita("a0-pronomi-sein-xe2", "Marco ___ Student.", "ist",
            "3ª persona singolare: er/es/sie → ist."),
        completaUnita("a0-pronomi-sein-xe3", "Wir ___ aus Bozen.", "sind",
            "1ª persona plurale → sind."),
        completaUnita("a0-pronomi-sein-xe4", "Anna und ich ___ hier.", "sind",
            "„io e Anna“ = noi → sind."),
        sceltaUnita("a0-pronomi-sein-xe5", "Wie ___ Sie, Frau Wagner?", listOf("sind", "ist", "bist", "seid"), 0,
            "Sie (Lei, cortesia) → sind."),
        completaUnita("a0-pronomi-sein-xe6", "Ich ___ Thomas.", "bin",
            "io → bin.")
    ),

    // ---- A0: articoli ----
    "a0-articoli" to listOf(
        sceltaUnita("a0-articoli-xe1", "___ Apotheke ist gleich hier.", listOf("Die", "Der", "Das", "Den"), 0,
            "Apotheke è femminile → die."),
        completaUnita("a0-articoli-xe2", "___ Krankenhaus ist neu.", "das",
            "Krankenhaus è neutro → das."),
        sceltaUnita("a0-articoli-xe3", "Das ist ___ Frau Doktor Weber.", listOf("die", "der", "das", "ein"), 0,
            "die Frau è femminile → die."),
        completaUnita("a0-articoli-xe4", "Er ist ___ Arzt.", "ein",
            "maschile → ein."),
        completaUnita("a0-articoli-xe5", "Das ist ___ Apotheke.", "eine",
            "femminile → eine."),
        completaUnita("a0-articoli-xe6", "___ Zimmer ist frei. (la camera)", "Das",
            "Zimmer è neutro → das.")
    ),

    // ---- A0: plurale ----
    "a0-plurale" to listOf(
        completaUnita("a0-plurale-xe1", "Ein Mann, zwei ___.", "Männer",
            "Plurale con umlaut + er.", "Scrivi solo il plurale."),
        completaUnita("a0-plurale-xe2", "Eine Frau, drei ___.", "Frauen",
            "Plurale in -en.", "Scrivi solo il plurale."),
        sceltaUnita("a0-plurale-xe3", "Ein Kind, zwei ___.", listOf("Kinder", "Kinds", "Kindern", "Kindes"), 0,
            "Plurale in -er."),
        completaUnita("a0-plurale-xe4", "Das Fenster, die ___.", "Fenster",
            "Plurale senza desinenza.", "Scrivi solo il plurale."),
        completaUnita("a0-plurale-xe5", "Die Schwester, zwei ___.", "Schwestern",
            "Plurale in -en.", "Scrivi solo il plurale."),
        completaUnita("a0-plurale-xe6", "Der Arzt, drei ___.", "Ärzte",
            "Plurale con umlaut + e.", "Scrivi solo il plurale.")
    ),

    // ---- A0: frase semplice e ordine delle parole ----
    "a0-frase" to listOf(
        sceltaUnita("a0-frase-xe1", "___ machen Sie in Ihrer Freizeit?", listOf("Was", "Wohin", "Woher", "Wann"), 0,
            "Domanda sull'oggetto → was."),
        sceltaUnita("a0-frase-xe2", "„Der Film ___ um 20 Uhr.“", listOf("beginnt", "beginnen", "beginne", "beginnst"), 0,
            "Der Film = 3ª persona singolare → beginnt."),
        trasformaUnita("a0-frase-xe3", "Maria kauft heute ein Auto. Inizia con „Heute“.", "Heute kauft Maria ein Auto.",
            "In inversione il verbo resta in posizione 2."),
        produciUnita("a0-frase-xe4", "„La sera mangio la pizza.“", "Am Abend esse ich Pizza.",
            "Tempo in posizione 1, poi verbo, poi soggetto."),
        trasformaUnita("a0-frase-xe5", "Rendi domanda: „Du sprichst Deutsch.“", "Sprichst du Deutsch?",
            "Domanda sì/no: inversione verbo-soggetto."),
        completaUnita("a0-frase-xe6", "Wo ___ du? — In Köln.", "wohnst",
            "2ª persona singolare di wohnen.")
    ),

    // ============================ A1 ============================

    // ---- A1: presente ----
    "a1-praesens" to listOf(
        completaUnita("a1-praesens-xe1", "Ich ___ (kommen) aus Mexiko.", "komme",
            "1ª persona singolare → -e."),
        completaUnita("a1-praesens-xe2", "Du ___ (haben) Hunger.", "hast",
            "haben è irregolare: du hast."),
        sceltaUnita("a1-praesens-xe3", "Er ___ (fahren) nach Hamburg.", listOf("fährt", "fahrt", "fahren", "legt"), 0,
            "Vokalwechsel a → ä: er fährt."),
        completaUnita("a1-praesens-xe4", "Du ___ (sprechen) sehr gut Deutsch.", "sprichst",
            "Vokalwechsel e → i: du sprichst."),
        completaUnita("a1-praesens-xe5", "Ihr ___ (arbeiten) viel.", "arbeitet",
            "Verbo in -t: ihr arbeitet."),
        produciUnita("a1-praesens-xe6", "„Io leggo un libro.“", "Ich lese ein Buch.",
            "lesen → ich lese.")
    ),

    // ---- A1: modali ----
    "a1-modali" to listOf(
        completaUnita("a1-modali-xe1", "Ich ___ heute arbeiten. (obbligo)", "muss",
            "müssen = dovere."),
        sceltaUnita("a1-modali-xe2", "___ wir hier draußen essen?", listOf("Dürfen", "Müssen", "Wollen", "Sollen"), 0,
            "Permesso/consenso → dürfen."),
        completaUnita("a1-modali-xe3", "Anna ___ sehr gut singen. (capacità)", "kann",
            "können = poter/saper."),
        completaUnita("a1-modali-xe4", "Ich ___ morgen mit ins Kino. (voglio)", "will",
            "wollen = volere."),
        sceltaUnita("a1-modali-xe5", "Bei der Erkältung ___ man viel Wasser trinken. (consiglio)",
            listOf("soll", "muss", "kann", "darf"), 0,
            "Consiglio → sollen."),
        produciUnita("a1-modali-xe6", "„Devi riposarti.“", "Du musst dich ausruhen.",
            "müssen + infinito in fondo.")
    ),

    // ---- A1: verbi separabili ----
    "a1-separabili" to listOf(
        completaUnita("a1-separabili-xe1", "Der Zug kommt um 8 Uhr ___. (ankommen)", "an",
            "Prefisso separabile in fondo alla frase."),
        completaUnita("a1-separabili-xe2", "Ich kaufe im Supermarkt ___. (einkaufen)", "ein",
            "Prefisso separabile in fondo."),
        trasformaUnita("a1-separabili-xe3", "Frasi ricomposte: „um sechs Uhr“ + „aufstehen“ + „Ich“.", "Ich stehe um sechs Uhr auf.",
            "Verbo coniugato in posizione 2, prefisso alla fine."),
        completaUnita("a1-separabili-xe4", "Herr Gross ___ zum Kurs mit. (mitkommen)", "kommt",
            "mitkommen → kommt … mit."),
        completaUnita("a1-separabili-xe5", "Wann fängt der Film ___? (anfangen)", "an",
            "Prefisso separabile in fondo."),
        sceltaUnita("a1-separabili-xe6", "„Ich rufe dich an.“ Dove sta „an“?",
            listOf(
                "In fondo, dopo il resto della frase",
                "Subito dopo il verbo coniugato",
                "All'inizio della frase",
                "Prima del verbo coniugato"
            ), 0,
            "Il prefisso separabile chiude la frase.")
    ),

    // ---- A1: imperativo ----
    "a1-imperativo" to listOf(
        sceltaUnita("a1-imperativo-xe1", "___ Sie bitte kurz! (warten)",
            listOf("Warten", "Warte", "Wartet", "Wart"), 0,
            "Forma con Sie: infinito + Sie."),
        completaUnita("a1-imperativo-xe2", "___ die Tasse! (du, trinken)", "Trink",
            "Du-form senza pronome: la -e finale cade.", "Scrivi il verbo (iniziale maiuscola).",
            listOf("Trinke")),
        completaUnita("a1-imperativo-xe3", "___ nicht so viel Fernsehen! (ihr, gucken)", "Guckt",
            "Forma con ihr: come la 2ª persona plurale.", "Scrivi il verbo (iniziale maiuscola).",
            listOf("Schaut")),
        sceltaUnita("a1-imperativo-xe4", "(wir) ___ die Tür!", listOf("Öffnen", "Öffnet", "Öffne", "Öffnest"), 0,
            "wir-form: 1ª persona plurale + pronome."),
        produciUnita("a1-imperativo-xe5", "„Bevi molto acqua.“", "Trink viel Wasser.",
            "Du-form: verbo nudo.", "Scrivi la frase completa in tedesco.",
            listOf("Trinke viel Wasser.")),
        trasformaUnita("a1-imperativo-xe6", "Rendi con la forma di cortesia: „Warte kurz.“", "Warten Sie kurz.",
            "Sie-form: verbo + Sie.")
    ),

    // ---- A1: frasi interrogative W ----
    "a1-fragewoerter" to listOf(
        sceltaUnita("a1-fragewoerter-xe1", "___ viel kostet das Ticket?", listOf("Wie", "Was", "Wo", "Wann"), 0,
            "Quantità/prezzo → wie viel."),
        completaUnita("a1-fragewoerter-xe2", "___ fährt der Bus? — Um 9 Uhr.", "wann",
            "Orario → wann."),
        completaUnita("a1-fragewoerter-xe3", "___ wohnst du? — Bei meinen Eltern.", "wo",
            "Luogo (stato) → wo."),
        sceltaUnita("a1-fragewoerter-xe4", "___ geht es Ihnen?", listOf("Wie", "Was", "Woher", "Wohin"), 0,
            "Formula di saluto → wie."),
        completaUnita("a1-fragewoerter-xe5", "___ heißt du? — Ich heiße Sami.", "wie",
            "Nome → wie heißt …?"),
        produciUnita("a1-fragewoerter-xe6", "Chiedi „di dove sei?“ a un compagno (tu).", "Woher kommst du?",
            "Provenienza → woher.")
    ),

    // ---- A1: akkusativ ----
    "a1-akkusativ" to listOf(
        sceltaUnita("a1-akkusativ-xe1", "Ich nehme ___ Apfel.", listOf("einen", "ein", "eine", "einem"), 0,
            "Maschile singolare accusativo → einen."),
        completaUnita("a1-akkusativ-xe2", "Der Arzt untersucht ___ Patient.", "den",
            "Maschile accusativo definito → den."),
        completaUnita("a1-akkusativ-xe3", "Ich habe ___ Fieber. (niente)", "kein",
            "Fieber è neutro: negazione → kein."),
        sceltaUnita("a1-akkusativ-xe4", "Du liest ___ Buch. (il libro, neutro)", listOf("das", "den", "dem", "des"), 0,
            "Neutro: nominativo e accusativo coincidono → das."),
        completaUnita("a1-akkusativ-xe5", "Sie kauft ___ Ball. (un pallone)", "einen",
            "Maschile accusativo indeterminato → einen."),
        produciUnita("a1-akkusativ-xe6", "„Prendo un appuntamento per martedì.“", "Ich nehme einen Termin am Dienstag.",
            "Termin maschile: accusativo einen.", "Scrivi la frase completa in tedesco.",
            listOf("Ich nehme einen Termin für Dienstag."))
    ),

    // ---- A1: possessivi ----
    "a1-possessivi" to listOf(
        sceltaUnita("a1-possessivi-xe1", "Das ist ___ Schwester. (mia)", listOf("meine", "meiner", "mein", "meinen"), 0,
            "femminile nominativo → meine."),
        completaUnita("a1-possessivi-xe2", "Das ist ___ Auto. (di te)", "dein",
            "Neutro nominativo → dein."),
        completaUnita("a1-possessivi-xe3", "___ Wohnung ist groß. (nostra)", "unsere",
            "femminile nominativo → unsere."),
        completaUnita("a1-possessivi-xe4", "Er liebt ___ Kind. (suo)", "sein",
            "Neutro nominativo → sein."),
        completaUnita("a1-possessivi-xe5", "Wo ist ___ Handy? (vostro)", "euer",
            "Neutro nominativo → euer."),
        produciUnita("a1-possessivi-xe6", "„Mia sorella parla anche italiano.“", "Meine Schwester spricht auch Italienisch.",
            "Schwester femminile → meine.")
    ),

    // ============================ A2 ============================

    // ---- A2: perfekt ----
    "a2-perfekt" to listOf(
        completaUnita("a2-perfekt-xe1", "Ich bin gestern ins Kino ___. (gehen)", "gegangen",
            "Movimento → sein + participio ge-…-en."),
        sceltaUnita("a2-perfekt-xe2", "Wir ___ gestern Pizza gegessen.", listOf("haben", "sind", "hat", "waren"), 0,
            "essen usa l'ausiliare haben."),
        completaUnita("a2-perfekt-xe3", "Er hat ein Auto ___. (kaufen)", "gekauft",
            "Participio debole: ge- + tema + -t."),
        completaUnita("a2-perfekt-xe4", "Sie ist nach Berlin ___. (fahren)", "gefahren",
            "Movimento → sein + gefahren."),
        completaUnita("a2-perfekt-xe5", "Ich habe nur wenig ___. (schlafen)", "geschlafen",
            "Verbo forte: ge- + schlafen."),
        trasformaUnita("a2-perfekt-xe6", "Da Präteritum a Perfekt: „Ich war beim Arzt.“", "Ich bin beim Arzt gewesen.",
            "sein al Perfekt: bin + gewesen.")
    ),

    // ---- A2: dativo ----
    "a2-dativo" to listOf(
        completaUnita("a2-dativo-xe1", "Der Arzt gibt ___ Patient einen Termin.", "dem",
            "geben regge il dativo → dem Patient."),
        sceltaUnita("a2-dativo-xe2", "Sie dankt ___ Lehrerin.", listOf("der", "die", "dem", "den"), 0,
            "Lehrerin femminile dativo → der."),
        completaUnita("a2-dativo-xe3", "Das Buch gehört ___ Kollegin.", "der",
            "femminile dativo → der."),
        completaUnita("a2-dativo-xe4", "Wir schreiben ___ Eltern.", "den",
            "Dativo plurale → den + -n: den Eltern."),
        sceltaUnita("a2-dativo-xe5", "Ich fahre mit ___ Fahrrad.", listOf("dem", "der", "den", "des"), 0,
            "Fahrrad è neutro; al dativo l'articolo è dem."),
        trasformaUnita("a2-dativo-xe6", "„Devi aiutare tua madre.“", "Du musst deiner Mutter helfen.",
            "helfen + dativo: deiner Mutter.")
    ),

    // ---- A2: preposizioni Wechsel (wo? wohin?) ----
    "a2-wechselpraepositionen" to listOf(
        sceltaUnita("a2-wechselpraepositionen-xe1", "Ich gehe ___ Arzt.", listOf("zum", "zur", "dem", "der"), 0,
            "Arzt maschile + movimento → zum (zu + dem)."),
        completaUnita("a2-wechselpraepositionen-xe2", "Ich bleibe heute ___ Haus. (dentro)", "im",
            "Stato (wo?) → in + dativo: im Haus.", "Scrivi la preposizione contratta.",
            listOf("in dem")),
        completaUnita("a2-wechselpraepositionen-xe3", "Ich gehe ___ Supermarkt. (dentro)", "in den",
            "Movimento (wohin?) + Supermarkt maschile → in den."),
        completaUnita("a2-wechselpraepositionen-xe4", "Die Milch steht ___ Kühlschrank. (dentro)", "im",
            "Stato (wo?) → in + dativo: in dem Kühlschrank = im Kühlschrank.",
            "Scrivi la preposizione contratta.",
            listOf("in dem")),
        completaUnita("a2-wechselpraepositionen-xe5", "Das Bild hängt ___ Wand.", "an der",
            "Stato (wo?) + Wand femminile → an der."),
        produciUnita("a2-wechselpraepositionen-xe6", "„Vado in farmacia.“ (movimento)", "Ich gehe in die Apotheke.",
            "Movimento + femminile → in die.")
    ),

    // ---- A2: präteritum ----
    "a2-praeteritum" to listOf(
        completaUnita("a2-praeteritum-xe1", "Gestern ___ ich beim Arzt. (sein)", "war",
            "Präteritum di sein → war."),
        completaUnita("a2-praeteritum-xe2", "Wir ___ gestern keine Zeit. (haben)", "hatten",
            "Präteritum di haben → hatten."),
        completaUnita("a2-praeteritum-xe3", "Als Kind ___ ich oft im Garten. (spielen)", "spielte",
            "Debole: tema + -te."),
        sceltaUnita("a2-praeteritum-xe4", "Es ___ viele Bäume im Garten.", listOf("gab", "gibt", "geben", "gabe"), 0,
            "Präteritum di geben → gab."),
        completaUnita("a2-praeteritum-xe5", "Du ___ gestern nicht arbeiten. (können)", "konntest",
            "Präteritum di können → konntest."),
        produciUnita("a2-praeteritum-xe6", "„Ieri non potevo venire.“", "Ich konnte gestern nicht kommen.",
            "können al Präteritum → konnte.", "Scrivi la frase completa in tedesco.",
            listOf("Gestern konnte ich nicht kommen."))
    ),

    // ---- A2: riflessivi ----
    "a2-riflessivi" to listOf(
        completaUnita("a2-riflessivi-xe1", "Ich putze ___ die Zähne.", "mir",
            "Con una parte del corpo il riflessivo è al dativo: mir."),
        completaUnita("a2-riflessivi-xe2", "Sie duscht ___ jeden Morgen.", "sich",
            "3ª persona singolare → sich."),
        sceltaUnita("a2-riflessivi-xe3", "Die Kinder freuen ___ auf die Ferien.", listOf("sich", "euch", "uns", "mich"), 0,
            "3ª persona plurale → sich."),
        completaUnita("a2-riflessivi-xe4", "Wir müssen ___ beeilen.", "uns",
            "sich beeilen, 1ª persona plurale → uns."),
        completaUnita("a2-riflessivi-xe5", "Setzt ___ bitte! (ihr)", "euch",
            "sich setzen, ihr-form → euch."),
        produciUnita("a2-riflessivi-xe6", "„Ieri mi sono rilassato.“", "Gestern habe ich mich erholt.",
            "sich erholen al Perfekt: habe mich erholt.")
    ),

    // ---- A2: subordinate weil/wenn ----
    "a2-subordinate" to listOf(
        completaUnita("a2-subordinate-xe1", "___ ich Zeit habe, lerne ich Deutsch.", "wenn",
            "Condizione → wenn."),
        completaUnita("a2-subordinate-xe2", "___ ich krank bin, bleibe ich zu Hause.", "weil",
            "Causa → weil."),
        sceltaUnita("a2-subordinate-xe3", "Im weil-Satz steht das Verb …",
            listOf(
                "am Ende des Satzes",
                "auf Position 2",
                "direkt nach weil",
                "in Position 1"
            ), 0,
            "Nella subordinata il verbo va in fondo."),
        sceltaUnita("a2-subordinate-xe4", "___ es regnet, gehe ich spazieren.",
            listOf("Obwohl", "Weil", "Wenn", "Dass"), 0,
            "Concessione → obwohl + verbo in fondo."),
        trasformaUnita("a2-subordinate-xe5", "Collega con weil: „Ich gehe zum Arzt. Ich habe Bauchschmerzen.“",
            "Ich gehe zum Arzt, weil ich Bauchschmerzen habe.",
            "Causa nella subordinata: verbo in fondo.",
            "Scrivi la frase completa in tedesco.",
            listOf("Ich gehe zum Arzt weil ich Bauchschmerzen habe.")),
        trasformaUnita("a2-subordinate-xe6", "Collega con wenn: „Ich habe Zeit. Wir lernen Deutsch.“",
            "Wenn ich Zeit habe, lernen wir Deutsch.",
            "Condizione nella subordinata: verbo in fondo.",
            "Scrivi la frase completa in tedesco.",
            listOf("Wenn ich Zeit habe lernen wir Deutsch."))
    ),

    // ---- A2: relative ----
    "a2-relative" to listOf(
        completaUnita("a2-relative-xe1", "Der Mann, ___ dort steht, ist Arzt.", "der",
            "Pronome relativo al maschile nominativo → der."),
        completaUnita("a2-relative-xe2", "Die Frau, ___ ich geholfen habe, ist Ärztin.", "der",
            "helfen + dativo femminile → der."),
        completaUnita("a2-relative-xe3", "Das Auto, ___ ich gekauft habe, ist alt.", "das",
            "Neutro accusativo → das."),
        completaUnita("a2-relative-xe4", "Die Kinder, mit ___ er spielt, sind nett.", "denen",
            "Dativo plurale → denen."),
        sceltaUnita("a2-relative-xe5", "Das Haus, ___ einen Garten hat, ist alt.",
            listOf("das", "die", "den", "dem"), 0,
            "Neutro nominativo → das."),
        produciUnita("a2-relative-xe6", "Collega: „Ich kenne einen Arzt. Der Arzt spricht Russisch.“",
            "Ich kenne einen Arzt, der Russisch spricht.",
            "Relativo al soggetto: der.", "Scrivi la frase completa in tedesco.",
            listOf("Ich kenne einen Arzt der Russisch spricht."))
    ),

    // ---- A2: passivo ----
    "a2-passivo" to listOf(
        completaUnita("a2-passivo-xe1", "Die Rechnungen ___ heute bezahlt.", "werden",
            "Plurale → werden."),
        sceltaUnita("a2-passivo-xe2", "Im Krankenhaus ___ viel operiert.",
            listOf("wird", "werden", "ist", "hat"), 0,
            "Singolare → wird + participio."),
        completaUnita("a2-passivo-xe3", "Die Wäsche ___ gewaschen.", "wird",
            "Singolare → wird."),
        completaUnita("a2-passivo-xe4", "Die Patienten ___ ins Krankenhaus gebracht.", "werden",
            "Plurale → werden."),
        produciUnita("a2-passivo-xe5", "„Il paziente viene curato.“", "Der Patient wird behandelt.",
            "Passivo presente: wird + participio.",
            "Scrivi la frase completa in tedesco.",
            listOf("Der Patient wird gepflegt.")),
        sceltaUnita("a2-passivo-xe6", "Gestern ___ die Formulare geschickt.",
            listOf("wurden", "wurde", "worden", "waren"), 0,
            "Passivo del Präteritum, plurale → wurden.")
    ),

    // ---- A2: komparativ ----
    "a2-komparativ" to listOf(
        completaUnita("a2-komparativ-xe1", "Der Bus ist ___ als die Bahn. (langsam)", "langsamer",
            "Comparativo: tema + -er + als."),
        completaUnita("a2-komparativ-xe2", "Die Metro ist ___ als der Bus. (schnell)", "schneller",
            "Comparativo: -er."),
        completaUnita("a2-komparativ-xe3", "Er ist der ___ Arzt der Klinik. (gut)", "beste",
            "Superlativo irregolare di gut → der beste."),
        sceltaUnita("a2-komparativ-xe4", "Anna singt am ___. (schön)",
            listOf("schönsten", "schönste", "schöner", "schönerste"), 0,
            "am + superlativo in -en: am schönsten."),
        completaUnita("a2-komparativ-xe5", "Sie kauft ___ Äpfel als wir. (billig)", "billigere",
            "Comparativo attributivo: -er + -e."),
        produciUnita("a2-komparativ-xe6", "„Leo è più alto di Sara.“", "Leo ist größer als Sara.",
            "Comparativo irregolare di groß → größer.")
    ),

    // ============================ B1 ============================

    // ---- B1: declinazione aggettivi ----
    "b1-adjektivdeklination" to listOf(
        completaUnita("b1-adjektivdeklination-xe1", "Ich trinke gern ___ Kaffee. (frisch)", "frischen",
            "maschile accusativo con der-articolo → -en."),
        sceltaUnita("b1-adjektivdeklination-xe2", "Das ist eine ___ Idee. (gut)",
            listOf("gute", "guten", "gutes", "guter"), 0,
            "Dopo eine: desinenza base → gute."),
        completaUnita("b1-adjektivdeklination-xe3", "Er kauft ___ Auto. (neu, senza articolo)", "neues",
            "Neutro senza articolo → -es."),
        completaUnita("b1-adjektivdeklination-xe4", "mit ___ Freunden (gut)", "guten",
            "Dativo plurale → -en."),
        completaUnita("b1-adjektivdeklination-xe5", "die ___ Probleme (klein)", "kleinen",
            "Plurale con die → -en."),
        produciUnita("b1-adjektivdeklination-xe6", "„Ho comprato una macchina nuova.“",
            "Ich habe ein neues Auto gekauft.",
            "Neutro con ein → neues.")
    ),

    // ---- B1: Konjunktiv II ----
    "b1-konjunktiv2" to listOf(
        completaUnita("b1-konjunktiv2-xe1", "Ich ___ gern mit dem Arzt sprechen.", "würde",
            "Forma polita: würde + infinito (desiderio)."),
        completaUnita("b1-konjunktiv2-xe2", "___ du mir helfen? (potresti)", "könntest",
            "Konjunktiv II di können → könntest."),
        sceltaUnita("b1-konjunktiv2-xe3", "Ich ___ dich gern besuchen, wenn ich Zeit hätte.",
            listOf("würde", "hätte", "wäre", "könnte"), 0,
            "Du chiedermi di venire a trovarti: würde + infinito."),
        completaUnita("b1-konjunktiv2-xe4", "Wenn ich du ___, würde ich zum Arzt gehen.", "wäre",
            "Konjunktiv II di sein → wäre."),
        sceltaUnita("b1-konjunktiv2-xe5", "Der Konjunktiv II von „können“ ist ___",
            listOf("könnte", "kann", "konnte", "können"), 0,
            "können → könnte."),
        produciUnita("b1-konjunktiv2-xe6", "„Vorrei un appuntamento il martedì.“ (con cortesia)",
            "Ich hätte gern einen Termin am Dienstag.",
            "hätte + gern: richiesta cortese nel Konjunktiv II.")
    ),

    // ---- B1: genitivo ----
    "b1-genitiv" to listOf(
        completaUnita("b1-genitiv-xe1", "Das ist das Auto ___ Vaters. (di me)", "meines",
            "Genitivo maschile di mein → meines."),
        sceltaUnita("b1-genitiv-xe2", "Die Telefonnummer ___ Chefs habe ich.",
            listOf("des", "dem", "den", "der"), 0,
            "Genitivo maschile/neutro → des."),
        completaUnita("b1-genitiv-xe3", "Das ist die Praxis ___ Ärztin. (della)", "der",
            "Genitivo femminile → der."),
        completaUnita("b1-genitiv-xe4", "Die Meinung ___ Eltern zählt auch.", "der",
            "Genitivo plurale → der."),
        produciUnita("b1-genitiv-xe5", "„Il libro del mio collega è nuovo.“", "Das Buch meines Kollegen ist neu.",
            "Genitivo maschile + n-Deklination → meines Kollegen."),
        sceltaUnita("b1-genitiv-xe6", "Wegen ___ Regens bleiben wir im Haus.",
            listOf("des", "dem", "den", "der"), 0,
            "der Regen → des Regens.")
    ),

    // ---- B1: plusquamperfekt ----
    "b1-plusquamperfekt" to listOf(
        completaUnita("b1-plusquamperfekt-xe1", "Ich ___ zu lange geschlafen.", "hatte",
            "Plusquamperfekt: hatte + participio."),
        sceltaUnita("b1-plusquamperfekt-xe2", "Als der Bus kam, ___ wir schon gewartet.",
            listOf("hatten", "hatte", "haben", "waren"), 0,
            "warten con haben, plurale → hatten."),
        completaUnita("b1-plusquamperfekt-xe3", "Er ___ ins Büro gekommen. (Plusquamperfekt)", "war",
            "Per il Plusquamperfekt: war + participio."),
        completaUnita("b1-plusquamperfekt-xe4", "Als ich ankam, ___ das Meeting schon begonnen.", "hatte",
            "beginnen con haben → hatte."),
        produciUnita("b1-plusquamperfekt-xe5", "„Avevo già mangiato quando è arrivato.“",
            "Ich hatte schon gegessen, als er kam.",
            "Passato prima → Plusquamperfekt, evento dopo → Präteritum.",
            "Scrivi la frase completa in tedesco.",
            listOf("Ich hatte schon gegessen als er kam.")),
        sceltaUnita("b1-plusquamperfekt-xe6", "Narrare il passato: ich ___ schon dort gewesen. (Plusquamperfekt)",
            listOf("war", "wurde", "hatte", "bin"), 0,
            "Plusquamperfekt di sein: war gewesen.")
    ),

    // ---- B1: connettori ----
    "b1-konnektoren" to listOf(
        completaUnita("b1-konnektoren-xe1", "Ich wollte kommen, ___ ich war krank.", "aber",
            "Contrapposizione tra due frasi principali → aber."),
        sceltaUnita("b1-konnektoren-xe2", "Es hat geregnet, ___ sind wir zu Hause geblieben.",
            listOf("deshalb", "obwohl", "sondern", "denn"), 0,
            "Causa prima, conseguenza dopo: deshalb + verbo in posizione 2."),
        sceltaUnita("b1-konnektoren-xe3", "Ich bleibe zu Hause, ___ es regnet. (ecco perché, senza congiungere)",
            listOf("denn", "weil", "deshalb", "darum"), 0,
            "denn non occupa una posizione: ordine normale della frase dopo."),
        sceltaUnita("b1-konnektoren-xe4", "Ich trinke keinen Kaffee, ___ Tee.",
            listOf("sondern", "und", "aber", "sonst"), 0,
            "Dopo una negazione → sondern."),
        completaUnita("b1-konnektoren-xe5", "Ich lerne Deutsch, ___ ich in Deutschland arbeiten möchte. (con „denn“)",
            "denn",
            "denn + ordine normale della frase."),
        trasformaUnita("b1-konnektoren-xe6", "Collega con deshalb: „Das Auto ist kaputt. Wir gehen zu Fuß.“",
            "Das Auto ist kaputt, deshalb gehen wir zu Fuß.",
            "deshalb + verbo in posizione 2.",
            "Scrivi la frase completa in tedesco.",
            listOf("Das Auto ist kaputt deshalb gehen wir zu Fuß."))
    ),

    // ---- B1: discorso indiretto ----
    "b1-indirekte-rede" to listOf(
        completaUnita("b1-indirekte-rede-xe1", "Ich frage, ___ du heute Zeit hast.", "ob",
            "Domanda sì/no indiretta → ob."),
        completaUnita("b1-indirekte-rede-xe2", "Sie fragt mich, ___ ich komme. (quando)", "wann",
            "Domanda W indiretta: la parola W resta."),
        completaUnita("b1-indirekte-rede-xe3", "Er sagt, ___ er krank ist.", "dass",
            "Affermazione indiretta → dass."),
        sceltaUnita("b1-indirekte-rede-xe4", "Quale indiretta corretta? (domanda „Kommst du?“)",
            listOf(
                "Ich frage, ob du kommst.",
                "Ich frage, ob kommst du.",
                "Ich frage, dass du kommst.",
                "Ich frage, kommst du."
            ), 0,
            "Domanda sì/no → ob + verbo alla fine."),
        trasformaUnita("b1-indirekte-rede-xe5", "Indiretta: „Er sagt: „Ich habe Kopfschmerzen.““",
            "Er sagt, dass er Kopfschmerzen hat.",
            "dass + soggetto + verbo in fondo.",
            "Scrivi la frase completa in tedesco.",
            listOf("Er sagt dass er Kopfschmerzen hat.")),
        completaUnita("b1-indirekte-rede-xe6", "Ich frage ihn, ___ er wohnt. (dove)", "wo",
            "Domanda W indiretta: la parola W resta.")
    )
)

internal fun unitaGrammatica(
    id: String,
    livello: String,
    titolo: String,
    sottotitolo: String,
    obiettivo: String,
    minuti: Int,
    prerequisiti: List<String> = emptyList(),
    regola: List<String>,
    tabelle: List<TabellaGrammatica>,
    esempi: List<EsempioGrammatica>,
    errori: List<ErroreTipico>,
    merksatz: String,
    esercizi: List<EsercizioUnita>
): UnitaGrammatica = UnitaGrammatica(
    id = id,
    livello = livello,
    titolo = titolo,
    sottotitolo = sottotitolo,
    obiettivo = obiettivo,
    minuti = minuti,
    prerequisiti = prerequisiti,
    teoria = TeoriaGrammatica(
        regola = regola,
        tabelle = tabelle,
        esempi = esempi,
        erroriTipici = errori,
        merksatz = merksatz
    ),
    esercizi = esercizi
)

internal fun tabellaUnita(
    titolo: String,
    intestazioni: List<String>,
    righe: List<List<String>>,
    nota: String = ""
): TabellaGrammatica = TabellaGrammatica(
    titolo = titolo,
    intestazioni = intestazioni,
    righe = righe,
    nota = nota
)

internal fun esempioUnita(
    tedesco: String,
    italiano: String,
    nota: String = ""
): EsempioGrammatica = EsempioGrammatica(
    tedesco = tedesco,
    italiano = italiano,
    nota = nota
)

internal fun erroreUnita(
    sbagliato: String,
    corretto: String,
    spiegazione: String
): ErroreTipico = ErroreTipico(
    sbagliato = sbagliato,
    corretto = corretto,
    spiegazione = spiegazione
)

fun sceltaUnita(
    id: String,
    domanda: String,
    opzioni: List<String>,
    corretta: Int,
    spiegazione: String,
    suggerimento: String = ""
): EsercizioUnita = EsercizioUnita(
    id = id,
    tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
    domanda = domanda,
    opzioni = opzioni,
    rispostaCorretta = corretta,
    spiegazione = spiegazione,
    suggerimento = suggerimento
)

internal fun completaUnita(
    id: String,
    domanda: String,
    risposta: String,
    spiegazione: String,
    suggerimento: String = "Scrivi solo la parola o la forma mancante.",
    alternative: List<String> = emptyList()
): EsercizioUnita = EsercizioUnita(
    id = id,
    tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
    domanda = domanda,
    rispostaTesto = risposta,
    alternative = alternative,
    spiegazione = spiegazione,
    suggerimento = suggerimento
)

internal fun trasformaUnita(
    id: String,
    domanda: String,
    risposta: String,
    spiegazione: String,
    suggerimento: String = "Scrivi la frase completa.",
    alternative: List<String> = emptyList()
): EsercizioUnita = EsercizioUnita(
    id = id,
    tipo = TipoEsercizioGrammatica.TRASFORMAZIONE,
    domanda = domanda,
    rispostaTesto = risposta,
    alternative = alternative,
    spiegazione = spiegazione,
    suggerimento = suggerimento
)

internal fun produciUnita(
    id: String,
    domanda: String,
    risposta: String,
    spiegazione: String,
    suggerimento: String = "Scrivi la frase completa in tedesco.",
    alternative: List<String> = emptyList()
): EsercizioUnita = EsercizioUnita(
    id = id,
    tipo = TipoEsercizioGrammatica.PRODUZIONE,
    domanda = domanda,
    rispostaTesto = risposta,
    alternative = alternative,
    spiegazione = spiegazione,
    suggerimento = suggerimento
)
