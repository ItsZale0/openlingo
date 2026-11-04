package com.alessandro.tedesco.data

// =====================================================================
// UNITÀ AGGIUNTIVE DEL PERCORSO A0 -> B1
// Coprono i vuoti più frequenti: possessivi/negazione, preposizioni
// miste, comparativi, futuro, connettori doppi e participi.
// =====================================================================

internal fun unitaA1Possessivi(): UnitaGrammatica = unitaGrammatica(
    id = "a1-possessivi",
    livello = "A1",
    titolo = "Possessivi e negazione",
    sottotitolo = "Mio, tuo, suo… e come dire «non»",
    obiettivo = "Usare i possessivi al nominativo e all'accusativo e distinguere kein da nicht.",
    minuti = 14,
    prerequisiti = listOf("a0-articoli", "a1-akkusativ"),
    regola = listOf(
        "I possessivi hanno una radice (mein-, dein-, sein-, ihr-, unser-, euer-, ihr-, Ihr-) e una desinenza.",
        "Al nominativo maschile e neutro non hanno desinenza: mein Vater, mein Kind. Femminile e plurale prendono -e: meine Mutter, meine Eltern.",
        "All'accusativo cambia solo il maschile: meinen Vater. Femminile, neutro e plurale restano uguali: meine Mutter, mein Kind, meine Eltern.",
        "euer perde la e interna quando prende la desinenza: euer Vater, ma eure Mutter, euren Vater.",
        "kein si declina come ein: kein Vater, keine Mutter, kein Kind, keine Eltern; all'accusativo maschile keinen Vater.",
        "Usa kein davanti a un nome senza articolo o con articolo indeterminativo. Usa nicht per negare verbi, aggettivi e nomi propri: Ich arbeite nicht. Er ist nicht müde."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Possessivi al nominativo",
            intestazioni = listOf("Persona", "Maschile", "Femminile", "Neutro", "Plurale"),
            righe = listOf(
                listOf("ich", "mein", "meine", "mein", "meine"),
                listOf("du", "dein", "deine", "dein", "deine"),
                listOf("er/es", "sein", "seine", "sein", "seine"),
                listOf("sie", "ihr", "ihre", "ihr", "ihre"),
                listOf("wir", "unser", "unsere", "unser", "unsere"),
                listOf("ihr", "euer", "eure", "euer", "eure"),
                listOf("sie/Sie", "ihr/Ihr", "ihre/Ihre", "ihr/Ihr", "ihre/Ihre")
            ),
            nota = "All'accusativo maschile aggiungi -en: meinen Vater, deinen Bruder."
        ),
        tabellaUnita(
            titolo = "kein e nicht",
            intestazioni = listOf("Forma", "Esempio", "Uso"),
            righe = listOf(
                listOf("kein", "Ich habe kein Geld.", "Nome neutro senza articolo"),
                listOf("keine", "Ich habe keine Zeit.", "Nome femminile senza articolo"),
                listOf("keinen", "Ich sehe keinen Hund.", "Nome maschile all'accusativo"),
                listOf("nicht", "Ich arbeite nicht.", "Verbo, aggettivo o nome proprio")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Mein Vater arbeitet in Brixen.", "Mio padre lavora a Bressanone."),
        esempioUnita("Das ist meine Schwester.", "Questa è mia sorella."),
        esempioUnita("Ich sehe meinen Bruder.", "Vedo mio fratello."),
        esempioUnita("Wir lieben unsere Eltern.", "Amiamo i nostri genitori."),
        esempioUnita("Ich habe keine Zeit.", "Non ho tempo.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich habe nicht Zeit.",
            "Ich habe keine Zeit.",
            "Zeit è un nome femminile senza articolo: si nega con keine."
        ),
        erroreUnita(
            "Das ist mein Mutter.",
            "Das ist meine Mutter.",
            "Mutter è femminile: il possessivo prende -e, meine."
        ),
        erroreUnita(
            "Ich sehe mein Bruder.",
            "Ich sehe meinen Bruder.",
            "Bruder è maschile all'accusativo: mein + -en = meinen."
        )
    ),
    merksatz = "mein Vater, meine Mutter, mein Kind, meine Eltern. Accusativo maschile: meinen Vater. Negazione del nome: kein/keine/keinen.",
    esercizi = listOf(
        sceltaUnita(
            "poss-01",
            "___ Vater arbeitet in Brixen. (ich)",
            listOf("mein", "meine", "meinen", "meiner"),
            0,
            "Vater è maschile singolare al nominativo: mein Vater."
        ),
        sceltaUnita(
            "poss-02",
            "Das ist ___ Schwester. (er)",
            listOf("seine", "seinen", "seiner", "seines"),
            0,
            "Schwester è femminile: seine Schwester."
        ),
        sceltaUnita(
            "poss-03",
            "Ich sehe ___ Bruder. (ich)",
            listOf("mein", "meine", "meinen", "meiner"),
            2,
            "Bruder è maschile all'accusativo: meinen Bruder."
        ),
        completaUnita(
            "poss-04",
            "Wir lieben ___ Eltern. (unser)",
            "unsere",
            "Eltern è plurale: unsere Eltern."
        ),
        completaUnita(
            "poss-05",
            "Hast du ___ Hund? (du)",
            "deinen",
            "Hund è maschile all'accusativo: deinen Hund."
        ),
        completaUnita(
            "poss-06",
            "Das ist ___ Buch. (du)",
            "dein",
            "Buch è neutro al nominativo: dein Buch."
        ),
        sceltaUnita(
            "poss-07",
            "___ Kinder spielen im Garten. (wir)",
            listOf("Unser", "Unsere", "Unseren", "Unserer"),
            1,
            "Kinder è plurale: unsere Kinder. A inizio frase: Unsere."
        ),
        sceltaUnita(
            "poss-08",
            "Ich habe ___ Zeit.",
            listOf("keine", "kein", "keinen", "nicht"),
            0,
            "Zeit è femminile senza articolo: keine Zeit."
        ),
        sceltaUnita(
            "poss-09",
            "Er ist ___ Lehrer.",
            listOf("kein", "keine", "keinen", "nicht"),
            0,
            "Lehrer è maschile singolare: kein Lehrer."
        ),
        completaUnita(
            "poss-10",
            "Das ist ___ Problem. (kein, neutro)",
            "kein",
            "Problem è neutro: kein Problem."
        ),
        trasformaUnita(
            "poss-11",
            "Questa è la mia borsa.",
            "Das ist meine Tasche.",
            "borsa = Tasche, femminile: meine Tasche."
        ),
        produciUnita(
            "poss-12",
            "Mio fratello non ha tempo.",
            "Mein Bruder hat keine Zeit.",
            "Mein Bruder (nominativo) + keine Zeit (negazione del nome)."
        )
    )
)

internal fun unitaA2Wechselpraepositionen(): UnitaGrammatica = unitaGrammatica(
    id = "a2-wechselpraepositionen",
    livello = "A2",
    titolo = "Le preposizioni miste",
    sottotitolo = "Dove? e Dove vai?",
    obiettivo = "Scegliere accusativo o dativo con an, auf, in, über, unter, vor, hinter, neben, zwischen.",
    minuti = 16,
    prerequisiti = listOf("a1-preposizioni-luogo", "a2-dativo"),
    regola = listOf(
        "Le preposizioni miste sono nove: an, auf, in, über, unter, vor, hinter, neben, zwischen.",
        "Rispondono a Wo? (dove?) con il dativo quando indicano posizione: Ich bin in der Schule.",
        "Rispondono a Wohin? (dove vai?) con l'accusativo quando indicano movimento o direzione: Ich gehe in die Schule.",
        "Alcuni verbi aiutano: liegen, stehen, sitzen, hängen (stato) vogliono il dativo; legen, stellen, setzen, hängen (movimento) vogliono l'accusativo.",
        "Contrazioni frequenti: in + das = ins, an + das = ans, in + dem = im, an + dem = am, bei + dem = beim.",
        "Attenzione: il dativo plurale aggiunge -n: in den Häusern, mit den Kindern."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Dativo o accusativo?",
            intestazioni = listOf("Domanda", "Caso", "Esempio"),
            righe = listOf(
                listOf("Wo? (posizione)", "Dativo", "Das Buch liegt auf dem Sofa."),
                listOf("Wohin? (movimento)", "Accusativo", "Ich lege das Buch auf den Tisch."),
                listOf("Wo? (posizione)", "Dativo", "Ich bin in der Schule."),
                listOf("Wohin? (movimento)", "Accusativo", "Ich gehe in die Schule.")
            )
        ),
        tabellaUnita(
            titolo = "Le nove preposizioni",
            intestazioni = listOf("Preposizione", "Dativo: dove?", "Accusativo: dove vai?"),
            righe = listOf(
                listOf("an", "am Fenster", "ans Fenster"),
                listOf("auf", "auf dem Tisch", "auf den Tisch"),
                listOf("in", "im Zimmer", "ins Zimmer"),
                listOf("über", "über dem Bett", "über das Bett"),
                listOf("unter", "unter dem Tisch", "unter den Tisch"),
                listOf("vor", "vor der Tür", "vor die Tür"),
                listOf("hinter", "hinter dem Haus", "hinter das Haus"),
                listOf("neben", "neben der Kirche", "neben die Kirche"),
                listOf("zwischen", "zwischen den Stühlen", "zwischen die Stühle")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich bin in der Schule.", "Sono a scuola."),
        esempioUnita("Ich gehe in die Schule.", "Vado a scuola."),
        esempioUnita("Das Buch liegt auf dem Sofa.", "Il libro è sul tavolo."),
        esempioUnita("Ich lege das Buch auf den Tisch.", "Metto il libro sul tavolo."),
        esempioUnita("Die Katze sitzt unter dem Sofa.", "Il gatto è sotto il divano.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich bin in die Schule.",
            "Ich bin in der Schule.",
            "Posizione (Wo?) vuole il dativo: in der Schule."
        ),
        erroreUnita(
            "Ich gehe in der Schule.",
            "Ich gehe in die Schule.",
            "Movimento (Wohin?) vuole l'accusativo: in die Schule."
        ),
        erroreUnita(
            "Ich lege das Buch auf dem Tisch.",
            "Ich lege das Buch auf den Tisch.",
            "legen indica movimento: auf den Tisch."
        )
    ),
    merksatz = "Wo? -> dativo. Wohin? -> accusativo. liegen/stehen/sitzen = dativo; legen/stellen/setzen = accusativo.",
    esercizi = listOf(
        sceltaUnita(
            "wechsel-01",
            "Ich gehe ___ Park.",
            listOf("in den", "im", "am", "beim"),
            0,
            "Movimento verso il parco (maschile): in den Park."
        ),
        sceltaUnita(
            "wechsel-02",
            "Die Schlüssel liegen ___ Tisch.",
            listOf("auf den", "auf dem", "auf der", "auf die"),
            1,
            "Posizione sul tavolo: auf dem Tisch (dativo)."
        ),
        sceltaUnita(
            "wechsel-03",
            "Sie stellt die Flasche ___ Kühlschrank.",
            listOf("in den", "in dem", "im", "in der"),
            0,
            "stellen indica movimento: in den Kühlschrank (accusativo)."
        ),
        completaUnita(
            "wechsel-04",
            "Wir wohnen ___ Berlin. (in)",
            "in",
            "Con i nomi di città non serve l'articolo: in Berlin."
        ),
        completaUnita(
            "wechsel-05",
            "Die Katze sitzt ___ Sofa. (auf)",
            "auf dem",
            "Posizione: auf dem Sofa (dativo)."
        ),
        completaUnita(
            "wechsel-06",
            "Ich hänge das Bild ___ Wand. (an, movimento)",
            "an die",
            "Movimento verso la parete: an die Wand (accusativo femminile)."
        ),
        sceltaUnita(
            "wechsel-07",
            "Er legt das Handy ___ Tasche.",
            listOf("in die", "in der", "auf der", "an der"),
            0,
            "legen indica movimento: in die Tasche."
        ),
        sceltaUnita(
            "wechsel-08",
            "Die Schule ist ___ Kirche.",
            listOf("neben der", "neben die", "neben dem", "neben den"),
            0,
            "Posizione accanto alla chiesa: neben der Kirche (dativo femminile)."
        ),
        completaUnita(
            "wechsel-09",
            "Wir gehen ___ Park. (in, movimento)",
            "in den",
            "Movimento verso il parco: in den Park (accusativo maschile)."
        ),
        trasformaUnita(
            "wechsel-10",
            "Ich bin im Haus. (esprimi movimento)",
            "Ich gehe ins Haus.",
            "Da posizione a movimento: im Haus -> ins Haus."
        ),
        produciUnita(
            "wechsel-11",
            "Il libro è sul tavolo.",
            "Das Buch liegt auf dem Tisch.",
            "Posizione: liegen + auf dem Tisch (dativo).",
            alternative = listOf("Das Buch ist auf dem Tisch.")
        ),
        sceltaUnita(
            "wechsel-12",
            "Die Kinder spielen ___ Garten.",
            listOf("im", "in den", "in der", "am"),
            0,
            "Posizione nel giardino: in dem Garten = im Garten."
        )
    )
)

internal fun unitaA2Komparativ(): UnitaGrammatica = unitaGrammatica(
    id = "a2-komparativ",
    livello = "A2",
    titolo = "Comparativo e superlativo",
    sottotitolo = "Più grande, il più grande",
    obiettivo = "Confrontare due cose e indicare il grado massimo con le forme regolari e irregolari.",
    minuti = 15,
    prerequisiti = listOf("a1-praesens", "a2-dativo"),
    regola = listOf(
        "Il comparativo si forma con aggettivo + -er + als: schnell -> schneller als, alt -> älter als.",
        "Il superlativo si forma con am + aggettivo + -sten, oppure con l'articolo definito + aggettivo + -ste: am schnellsten, der schnellste Zug.",
        "Molti aggettivi brevi prendono l'umlaut: alt -> älter, jung -> jünger, groß -> größer, kalt -> kälter, warm -> wärmer, stark -> stärker, lang -> länger.",
        "Forme irregolari: gut -> besser -> am besten; viel -> mehr -> am meisten; gern -> lieber -> am liebsten; hoch -> höher -> am höchsten; nah -> näher -> am nächsten.",
        "Per indicare uguaglianza si usa so + aggettivo + wie: Er ist so groß wie sein Vater.",
        "Non usare wie con il comparativo: besser als, non besser wie."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Forme regolari",
            intestazioni = listOf("Positivo", "Comparativo", "Superlativo"),
            righe = listOf(
                listOf("schnell", "schneller", "am schnellsten"),
                listOf("alt", "älter", "am ältesten"),
                listOf("groß", "größer", "am größten"),
                listOf("kalt", "kälter", "am kältesten"),
                listOf("lang", "länger", "am längsten")
            )
        ),
        tabellaUnita(
            titolo = "Forme irregolari",
            intestazioni = listOf("Positivo", "Comparativo", "Superlativo"),
            righe = listOf(
                listOf("gut", "besser", "am besten"),
                listOf("viel", "mehr", "am meisten"),
                listOf("gern", "lieber", "am liebsten"),
                listOf("hoch", "höher", "am höchsten"),
                listOf("nah", "näher", "am nächsten")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Mein Bruder ist älter als ich.", "Mio fratello è più grande di me."),
        esempioUnita("Berlin ist größer als München.", "Berlino è più grande di Monaco."),
        esempioUnita("Das ist der beste Film.", "Questo è il film migliore."),
        esempioUnita("Sie läuft am schnellsten.", "Lei corre più veloce di tutti."),
        esempioUnita("Er ist so groß wie sein Vater.", "È alto come suo padre.")
    ),
    errori = listOf(
        erroreUnita(
            "Er ist größer wie ich.",
            "Er ist größer als ich.",
            "Con il comparativo si usa als, non wie."
        ),
        erroreUnita(
            "Das ist der guteste Film.",
            "Das ist der beste Film.",
            "gut è irregolare: gut -> besser -> am besten."
        ),
        erroreUnita(
            "Ich trinke vieler Kaffee als du.",
            "Ich trinke mehr Kaffee als du.",
            "viel è irregolare: mehr, non vieler."
        )
    ),
    merksatz = "Comparativo: -er + als. Superlativo: am -sten / der -ste. Uguaglianza: so ... wie.",
    esercizi = listOf(
        sceltaUnita(
            "komp-01",
            "Mein Bruder ist ___ als ich. (alt)",
            listOf("älter", "alter", "am ältesten", "älteste"),
            0,
            "alt prende l'umlaut: älter als."
        ),
        sceltaUnita(
            "komp-02",
            "Berlin ist ___ als München. (groß)",
            listOf("größer", "großer", "am größten", "größte"),
            0,
            "groß -> größer; il comparativo è größer als."
        ),
        sceltaUnita(
            "komp-03",
            "Das ist der ___ Film. (gut)",
            listOf("beste", "besser", "besten", "am besten"),
            0,
            "Superlativo con articolo: der beste Film."
        ),
        completaUnita(
            "komp-04",
            "Sie läuft am ___ . (schnell)",
            "schnellsten",
            "Superlativo con am: am schnellsten."
        ),
        completaUnita(
            "komp-05",
            "Im Winter ist es ___ als im Herbst. (kalt)",
            "kälter",
            "kalt -> kälter; comparativo con als."
        ),
        sceltaUnita(
            "komp-06",
            "Er ist so groß ___ sein Vater.",
            listOf("wie", "als", "denn", "dass"),
            0,
            "Uguaglianza: so groß wie."
        ),
        sceltaUnita(
            "komp-07",
            "Ich trinke ___ Kaffee als du.",
            listOf("mehr", "viel", "meiste", "am meisten"),
            0,
            "viel è irregolare: mehr Kaffee als."
        ),
        completaUnita(
            "komp-08",
            "Das ist das ___ Buch. (gut, neutro)",
            "beste",
            "Superlativo con articolo neutro: das beste Buch."
        ),
        trasformaUnita(
            "komp-09",
            "Mein Auto ist schnell. Dein Auto ist schneller.",
            "Dein Auto ist schneller als mein Auto.",
            "Confronto con il comparativo: schneller als."
        ),
        produciUnita(
            "komp-10",
            "Questo libro è migliore di quello.",
            "Dieses Buch ist besser als das.",
            "besser als = migliore di.",
            alternative = listOf("Dieses Buch ist besser als jenes.")
        ),
        sceltaUnita(
            "komp-11",
            "Sie ist die ___ Schülerin. (gut)",
            listOf("beste", "besser", "besten", "am besten"),
            0,
            "Superlativo femminile con articolo: die beste Schülerin."
        ),
        completaUnita(
            "komp-12",
            "Er spielt ___ als sein Bruder. (gern)",
            "lieber",
            "gern è irregolare: lieber als."
        )
    )
)

internal fun unitaB1Futur(): UnitaGrammatica = unitaGrammatica(
    id = "b1-futur",
    livello = "B1",
    titolo = "Futuro e infinito con zu",
    sottotitolo = "Piani, previsioni e verbi che reggono zu",
    obiettivo = "Formare il Futur I e usare correttamente l'infinito con zu.",
    minuti = 16,
    prerequisiti = listOf("a1-praesens", "a1-modali"),
    regola = listOf(
        "Il Futur I si forma con werden + infinito in fondo alla frase: Ich werde morgen lernen.",
        "Coniugazione di werden: ich werde, du wirst, er/sie/es wird, wir werden, ihr werdet, sie/Sie werden.",
        "Il futuro si usa per piani, previsioni e promesse. Spesso il presente con un'espressione di tempo è più naturale: Morgen lerne ich Deutsch.",
        "Molti verbi reggono l'infinito con zu: versuchen, vergessen, vorhaben, hoffen, planen, anfangen, aufhören, beschließen.",
        "Con i verbi separabili, zu entra tra prefisso e verbo: anzurufen, aufzustehen, einzukaufen.",
        "I verbi modali e lassen vogliono l'infinito senza zu: Ich muss lernen. Ich lasse das Auto reparieren."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "werden + infinito",
            intestazioni = listOf("Persona", "werden", "Esempio"),
            righe = listOf(
                listOf("ich", "werde", "Ich werde lernen."),
                listOf("du", "wirst", "Du wirst lernen."),
                listOf("er/sie/es", "wird", "Er wird lernen."),
                listOf("wir", "werden", "Wir werden lernen."),
                listOf("ihr", "werdet", "Ihr werdet lernen."),
                listOf("sie/Sie", "werden", "Sie werden lernen.")
            )
        ),
        tabellaUnita(
            titolo = "Infinito con zu",
            intestazioni = listOf("Verbo", "Esempio"),
            righe = listOf(
                listOf("versuchen", "Ich versuche, pünktlich zu kommen."),
                listOf("vergessen", "Ich habe vergessen, anzurufen."),
                listOf("vorhaben", "Ich habe vor, Deutsch zu lernen."),
                listOf("hoffen", "Ich hoffe, dich zu sehen."),
                listOf("aufhören", "Er hört auf, zu rauchen.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich werde morgen nach Bozen fahren.", "Domani andrò a Bolzano."),
        esempioUnita("Wir werden unsere Freunde besuchen.", "Visiteremo i nostri genitori."),
        esempioUnita("Ich versuche, pünktlich zu kommen.", "Cerco di arrivare puntuale."),
        esempioUnita("Ich habe vor, Deutsch zu lernen.", "Ho intenzione di imparare il tedesco."),
        esempioUnita("Ich muss heute arbeiten.", "Oggi devo lavorare.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich werde lernen Deutsch.",
            "Ich werde Deutsch lernen.",
            "Con werden l'infinito va in fondo: Deutsch lernen."
        ),
        erroreUnita(
            "Ich will zu lernen.",
            "Ich will lernen.",
            "I verbi modali vogliono l'infinito senza zu."
        ),
        erroreUnita(
            "Ich versuche zu anzurufen.",
            "Ich versuche anzurufen.",
            "Con i verbi separabili zu entra tra prefisso e verbo: an + zu + rufen."
        )
    ),
    merksatz = "Futur I: werden + infinito in fondo. Infinito con zu: versuchen/vergessen/vorhaben + zu + infinito.",
    esercizi = listOf(
        sceltaUnita(
            "fut-01",
            "Ich ___ morgen nach Bozen fahren.",
            listOf("werde", "wirst", "wird", "werden"),
            0,
            "ich + werden = werde; l'infinito fahren resta in fondo."
        ),
        sceltaUnita(
            "fut-02",
            "Er ___ die Prüfung bestehen.",
            listOf("werde", "wirst", "wird", "werden"),
            2,
            "er + werden = wird."
        ),
        completaUnita(
            "fut-03",
            "Wir ___ im Sommer nach Deutschland fahren. (werden)",
            "werden",
            "wir + werden = werden."
        ),
        completaUnita(
            "fut-04",
            "Sie ___ später Ärztin werden. (werden)",
            "wird",
            "sie (singolare) + werden = wird."
        ),
        sceltaUnita(
            "fut-05",
            "Ich habe vergessen, die Rechnung ___ bezahlen.",
            listOf("zu", "um", "ohne", "für"),
            0,
            "vergessen regge l'infinito con zu: zu bezahlen."
        ),
        sceltaUnita(
            "fut-06",
            "Er versucht, pünktlich ___ kommen.",
            listOf("zu", "um", "ohne", "für"),
            0,
            "versuchen regge l'infinito con zu: zu kommen."
        ),
        completaUnita(
            "fut-07",
            "Ich habe vor, Deutsch ___ lernen. (zu)",
            "zu",
            "vorhaben regge l'infinito con zu: Deutsch zu lernen."
        ),
        trasformaUnita(
            "fut-08",
            "Ich lerne morgen Deutsch. (Futur I)",
            "Ich werde morgen Deutsch lernen.",
            "werden + infinito in fondo: werde ... lernen."
        ),
        trasformaUnita(
            "fut-09",
            "Wir besuchen unsere Eltern. (Futur I)",
            "Wir werden unsere Eltern besuchen.",
            "wir werden + besuchen in fondo."
        ),
        produciUnita(
            "fut-10",
            "Domani andrò a scuola.",
            "Morgen werde ich in die Schule gehen.",
            "Futur I: werde ... gehen.",
            alternative = listOf("Morgen gehe ich in die Schule.")
        ),
        sceltaUnita(
            "fut-11",
            "Ich hoffe, dich bald ___ sehen.",
            listOf("zu", "um", "ohne", "für"),
            0,
            "hoffen regge l'infinito con zu: zu sehen."
        ),
        completaUnita(
            "fut-12",
            "Sie hat keine Zeit ___ kommen. (zu)",
            "zu",
            "Dopo keine Zeit si usa l'infinito con zu: zu kommen."
        )
    )
)

internal fun unitaB1Doppelkonnektoren(): UnitaGrammatica = unitaGrammatica(
    id = "b1-doppelkonnektoren",
    livello = "B1",
    titolo = "I connettori doppi",
    sottotitolo = "Entweder…oder, weder…noch, je…desto",
    obiettivo = "Collegare due alternative, due negazioni o due elementi con i connettori doppi.",
    minuti = 16,
    prerequisiti = listOf("b1-konnektoren"),
    regola = listOf(
        "entweder ... oder = o ... o: Entweder komme ich heute oder am Freitag.",
        "weder ... noch = né ... né: Ich habe weder Zeit noch Geld.",
        "sowohl ... als auch = sia ... sia: Sie spricht sowohl Deutsch als auch Französisch.",
        "nicht nur ... sondern auch = non solo ... ma anche: Er ist nicht nur intelligent, sondern auch fleißig.",
        "je ... desto/umso = quanto più ... tanto più: Je mehr ich lerne, desto besser verstehe ich.",
        "zwar ... aber = certo ... ma: Er ist zwar jung, aber sehr erfahren.",
        "Con je ... desto il verbo della frase con je va in fondo; nella frase con desto il verbo resta al secondo posto."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Connettori doppi",
            intestazioni = listOf("Connettore", "Significato", "Esempio"),
            righe = listOf(
                listOf("entweder ... oder", "o ... o", "Entweder du oder ich."),
                listOf("weder ... noch", "né ... né", "Weder er noch sie kommt."),
                listOf("sowohl ... als auch", "sia ... sia", "Sowohl Deutsch als auch Englisch."),
                listOf("nicht nur ... sondern auch", "non solo ... ma anche", "Nicht nur du, sondern auch ich."),
                listOf("je ... desto", "quanto più ... tanto più", "Je mehr, desto besser."),
                listOf("zwar ... aber", "certo ... ma", "Zwar teuer, aber gut.")
            )
        ),
        tabellaUnita(
            titolo = "Posizione del verbo con je ... desto",
            intestazioni = listOf("Parte", "Esempio"),
            righe = listOf(
                listOf("je + comparativo", "Je mehr ich lerne,"),
                listOf("verbo in fondo", "… desto besser verstehe ich."),
                listOf("desto + comparativo", "… desto besser"),
                listOf("verbo al secondo posto", "… verstehe ich.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Entweder komme ich heute oder am Freitag.", "O vengo oggi o domani."),
        esempioUnita("Ich habe weder Zeit noch Geld.", "Non ho né tempo né soldi."),
        esempioUnita("Sie spricht sowohl Deutsch als auch Französisch.", "Parla sia tedesco sia inglese."),
        esempioUnita("Je mehr ich lerne, desto besser verstehe ich.", "Più imparo, meglio capisco."),
        esempioUnita("Er ist zwar jung, aber sehr erfahren.", "È giovane, ma molto esperto.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich habe nicht Zeit und nicht Geld.",
            "Ich habe weder Zeit noch Geld.",
            "Per «né ... né» si usa weder ... noch."
        ),
        erroreUnita(
            "Je mehr ich lerne, desto besser ich verstehe.",
            "Je mehr ich lerne, desto besser verstehe ich.",
            "Dopo desto + comparativo il verbo va al secondo posto."
        ),
        erroreUnita(
            "Sowohl Deutsch und Englisch.",
            "Sowohl Deutsch als auch Englisch.",
            "Il secondo elemento di sowohl si introduce con als auch."
        )
    ),
    merksatz = "entweder ... oder; weder ... noch; sowohl ... als auch; nicht nur ... sondern auch; je ... desto; zwar ... aber.",
    esercizi = listOf(
        sceltaUnita(
            "dopp-01",
            "___ ich komme, ___ ich bleibe zu Hause.",
            listOf("Entweder ... oder", "Weder ... noch", "Sowohl ... als auch", "Zwar ... aber"),
            0,
            "Due alternative: entweder ... oder."
        ),
        sceltaUnita(
            "dopp-02",
            "Er spricht ___ Deutsch ___ Englisch.",
            listOf("sowohl ... als auch", "weder ... noch", "entweder ... oder", "je ... desto"),
            0,
            "Due elementi presenti: sowohl ... als auch."
        ),
        sceltaUnita(
            "dopp-03",
            "Ich habe ___ Zeit ___ Geld.",
            listOf("weder ... noch", "sowohl ... als auch", "entweder ... oder", "nicht nur ... sondern auch"),
            0,
            "Due negazioni: weder ... noch."
        ),
        sceltaUnita(
            "dopp-04",
            "Sie ist ___ intelligent, ___ auch fleißig.",
            listOf("nicht nur ... sondern", "weder ... noch", "entweder ... oder", "je ... desto"),
            0,
            "Aggiunge una qualità: nicht nur ... sondern auch."
        ),
        completaUnita(
            "dopp-05",
            "___ mehr ich lerne, desto besser verstehe ich.",
            "Je",
            "La correlazione inizia con Je + comparativo."
        ),
        sceltaUnita(
            "dopp-06",
            "Er ist ___ jung, ___ sehr erfahren.",
            listOf("zwar ... aber", "weder ... noch", "sowohl ... als auch", "entweder ... oder"),
            0,
            "Contrasto: zwar ... aber."
        ),
        trasformaUnita(
            "dopp-07",
            "Ich trinke keinen Kaffee und keinen Tee.",
            "Ich trinke weder Kaffee noch Tee.",
            "Due negazioni collegate: weder ... noch."
        ),
        trasformaUnita(
            "dopp-08",
            "Sie spricht Deutsch und auch Englisch.",
            "Sie spricht sowohl Deutsch als auch Englisch.",
            "Due elementi affermativi: sowohl ... als auch."
        ),
        produciUnita(
            "dopp-09",
            "O vengo oggi o domani.",
            "Entweder komme ich heute oder morgen.",
            "entweder ... oder; dopo entweder il verbo può venire subito.",
            alternative = listOf("Ich komme entweder heute oder morgen.")
        ),
        sceltaUnita(
            "dopp-10",
            "___ du früher kommst, ___ mehr Zeit haben wir.",
            listOf("Je ... desto", "Entweder ... oder", "Weder ... noch", "Zwar ... aber"),
            0,
            "Correlazione tra due comparativi: je ... desto."
        ),
        sceltaUnita(
            "dopp-11",
            "Das ist ___ teuer, ___ auch schön.",
            listOf("nicht nur ... sondern", "weder ... noch", "je ... desto", "entweder ... oder"),
            0,
            "Aggiunge un aspetto positivo: nicht nur ... sondern auch."
        ),
        sceltaUnita(
            "dopp-12",
            "Ich habe ___ Hunger ___ Durst.",
            listOf("weder ... noch", "sowohl ... als auch", "entweder ... oder", "nicht nur ... sondern auch"),
            0,
            "Nessuno dei due: weder ... noch."
        )
    )
)

internal fun unitaB1Partizipien(): UnitaGrammatica = unitaGrammatica(
    id = "b1-partizipien",
    livello = "B1",
    titolo = "Participi e frasi ridotte",
    sottotitolo = "Lachend, gelesen, geöffnet",
    obiettivo = "Riconoscere e usare il Partizip I e il Partizip II come aggettivi.",
    minuti = 17,
    prerequisiti = listOf("a2-passivo", "b1-adjektivdeklination"),
    regola = listOf(
        "Il Partizip I si forma con l'infinito + d: lachen -> lachend, laufen -> laufend. Esprime un'azione contemporanea e attiva.",
        "Il Partizip II dei verbi regolari è ge- + radice + -t: machen -> gemacht. Nei verbi forti è spesso ge-...-en: lesen -> gelesen.",
        "Come aggettivo, il participio prende la desinenza dell'aggettivo: der lachende Mann, das gelesene Buch, die geöffnete Tür.",
        "Il Partizip I come aggettivo è attivo: der lachende Mann = l'uomo che ride.",
        "Il Partizip II come aggettivo ha spesso valore passivo o di risultato: das gelesene Buch = il libro che è stato letto.",
        "Le frasi participiali possono essere ampliate: die in Berlin lebenden Studenten = gli studenti che vivono a Berlino."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Partizip I e Partizip II",
            intestazioni = listOf("Infinito", "Partizip I", "Partizip II"),
            righe = listOf(
                listOf("lachen", "lachend", "gelacht"),
                listOf("lesen", "lesend", "gelesen"),
                listOf("kochen", "kochend", "gekocht"),
                listOf("öffnen", "öffnend", "geöffnet"),
                listOf("schreiben", "schreibend", "geschrieben")
            )
        ),
        tabellaUnita(
            titolo = "Come aggettivo",
            intestazioni = listOf("Forma", "Esempio", "Significato"),
            righe = listOf(
                listOf("Partizip I", "der lachende Mann", "l'uomo che ride"),
                listOf("Partizip I", "die studierenden Studenten", "gli studenti che studiano"),
                listOf("Partizip II", "das gelesene Buch", "il libro letto"),
                listOf("Partizip II", "die geöffnete Tür", "la porta aperta")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Der lachende Mann ist mein Lehrer.", "L'uomo che ride è il mio insegnante."),
        esempioUnita("Das gelesene Buch liegt auf dem Tisch.", "Il libro letto è sul tavolo."),
        esempioUnita("Die gekochte Suppe schmeckt gut.", "La zuppa cotta è buona."),
        esempioUnita("Die in Berlin lebenden Studenten kommen aus Italien.", "Gli studenti che vivono a Berlino vengono dall'Italia."),
        esempioUnita("Das geöffnete Fenster ist kaputt.", "La porta aperta è rotta.")
    ),
    errori = listOf(
        erroreUnita(
            "Der lachend Mann.",
            "Der lachende Mann.",
            "Come aggettivo il participio prende la desinenza: lachende."
        ),
        erroreUnita(
            "Das lesen Buch.",
            "Das gelesene Buch.",
            "Per il risultato si usa il Partizip II: gelesen + -e = gelesene."
        ),
        erroreUnita(
            "Die geöffnet Tür.",
            "Die geöffnete Tür.",
            "Il participio come aggettivo prende la desinenza femminile -e: geöffnete."
        )
    ),
    merksatz = "Partizip I = infinito + d (lachend). Partizip II = ge-...-t/en (gelesen). Come aggettivo prendono la desinenza.",
    esercizi = listOf(
        sceltaUnita(
            "part-01",
            "Das Partizip I von lachen ist ___.",
            listOf("lachend", "gelacht", "lachen", "lachende"),
            0,
            "Partizip I: infinito + d = lachend."
        ),
        sceltaUnita(
            "part-02",
            "Das Partizip II von lesen ist ___.",
            listOf("gelesen", "lesend", "lesen", "gelesene"),
            0,
            "lesen è forte: Partizip II = gelesen."
        ),
        completaUnita(
            "part-03",
            "Der ___ Mann ist mein Lehrer. (lachen, Partizip I)",
            "lachende",
            "Der + maschile: lachend + e = lachende."
        ),
        completaUnita(
            "part-04",
            "Das ___ Buch liegt auf dem Tisch. (lesen, Partizip II)",
            "gelesene",
            "Das + neutro: gelesen + e = gelesene."
        ),
        sceltaUnita(
            "part-05",
            "Die ___ Suppe schmeckt gut. (kochen, Partizip II)",
            listOf("gekochte", "kochende", "gekocht", "kochend"),
            0,
            "Risultato: gekocht + e = gekochte; die Suppe è femminile."
        ),
        sceltaUnita(
            "part-06",
            "Der ___ Hund bellt. (bellen, Partizip I)",
            listOf("bellende", "gebellte", "bellen", "gebellt"),
            0,
            "Azione contemporanea: bellend + e = bellende."
        ),
        completaUnita(
            "part-07",
            "Die ___ Studenten kommen aus Italien. (studieren, Partizip I)",
            "studierenden",
            "Plurale con articolo definito: studierend + en = studierenden."
        ),
        completaUnita(
            "part-08",
            "Das ___ Fenster ist kaputt. (brechen, Partizip II)",
            "gebrochene",
            "brechen è forte: gebrochen + e = gebrochene."
        ),
        trasformaUnita(
            "part-09",
            "Ein Mann, der lacht, sitzt dort.",
            "Ein lachender Mann sitzt dort.",
            "La frase relativa si riduce con il Partizip I: lachender."
        ),
        trasformaUnita(
            "part-10",
            "Das Buch, das gelesen wurde, ist interessant.",
            "Das gelesene Buch ist interessant.",
            "Passivo ridotto con il Partizip II: gelesene."
        ),
        produciUnita(
            "part-11",
            "La porta aperta è rotta.",
            "Die geöffnete Tür ist kaputt.",
            "öffnen -> geöffnet + e (die Tür) = geöffnete."
        ),
        sceltaUnita(
            "part-12",
            "Die ___ Aufgabe war schwer. (schreiben, Partizip II)",
            listOf("geschriebene", "schreibende", "geschrieben", "schreibend"),
            0,
            "Risultato: geschrieben + e = geschriebene."
        )
    )
)
