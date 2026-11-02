package com.alessandro.tedesco.data

/**
 * SECONDO BANCO DI ESERCIZI EXTRA.
 *
 * Serve alle unità che avevano solo il banco "core" (6-8 esercizi): con un
 * banco così piccolo una sessione ruotata non avrebbe nulla da ruotare.
 * Qui ogni unità riceve 6 esercizi in più, sullo stesso argomento e nello
 * stesso formato delle lezioni, così il percorso può proporre sessioni
 * diverse senza perdere coerenza.
 *
 * Gli id usano il suffisso "-x2e" per non toccare le chiavi già salvate.
 */
internal val EserciziExtra2: Map<String, List<EsercizioUnita>> = mapOf(

    // ============================ A2 ============================

    // ---- A2: declinazione dell'aggettivo ----
    "a2-adjektivdeklination" to listOf(
        sceltaUnita("adj-x2e1", "Ich trinke gern ___ Tee.", listOf("heißen", "heiße", "heißer", "heißes"), 0,
            "Senza articolo, accusativo maschile: heißen Tee."),
        completaUnita("adj-x2e2", "Das ist ein ___ (neu) Auto.", "neues",
            "Neutro nominativo dopo ein: neues Auto."),
        sceltaUnita("adj-x2e3", "Wir wohnen in ___ Wohnung.", listOf("einer kleinen", "eine kleine", "einer kleine", "eine kleinen"), 0,
            "Dativo femminile: in einer kleinen Wohnung."),
        completaUnita("adj-x2e4", "Die ___ (alt) Frau wartet draußen.", "alte",
            "Dopo die, nominativo femminile: die alte Frau."),
        trasformaUnita("adj-x2e5", "Rendi al plurale: „Der kleine Junge spielt.“", "Die kleinen Jungen spielen.",
            "Plurale con articolo determinativo: die kleinen Jungen."),
        produciUnita("adj-x2e6", "„Ho una macchina nuova.“", "Ich habe ein neues Auto.",
            "Neutro accusativo dopo ein: ein neues Auto.")
    ),

    // ---- A2: Konjunktiv II di base ----
    "a2-konjunktiv2" to listOf(
        sceltaUnita("konj2-x2e1", "Ich ___ gern ein Wasser, bitte.", listOf("hätte", "würde", "wäre", "könnte"), 0,
            "Richiesta cortese: ich hätte gern."),
        completaUnita("konj2-x2e2", "Wenn ich du ___, würde ich mitkommen.", "wäre",
            "Ipotesi irreale con sein: wenn ich du wäre."),
        sceltaUnita("konj2-x2e3", "___ du mir bitte helfen?", listOf("Könntest", "Kanntest", "Müsstest", "Würdest"), 0,
            "Richiesta cortese: Könntest du...?"),
        completaUnita("konj2-x2e4", "Wir ___ (müssen) früher losfahren.", "müssten",
            "Konjunktiv II di müssen: müssten."),
        trasformaUnita("konj2-x2e5", "Rendi cortese: „Helfen Sie mir!“", "Könnten Sie mir helfen?",
            "La richiesta cortese usa könnten + infinito."),
        produciUnita("konj2-x2e6", "„Se fossi ricco, viaggerei.“", "Wenn ich reich wäre, würde ich reisen.",
            "Ipotesi irreale: wäre + würde + infinito.")
    ),

    // ---- A2: verbi con preposizione ----
    "a2-verben-praepositionen" to listOf(
        sceltaUnita("vpr-x2e1", "Ich interessiere mich ___ Musik.", listOf("für", "auf", "an", "über"), 0,
            "sich interessieren für + accusativo."),
        completaUnita("vpr-x2e2", "Wir warten ___ den Zug.", "auf",
            "warten auf + accusativo: auf den Zug."),
        sceltaUnita("vpr-x2e3", "Er denkt oft ___ seine Familie.", listOf("an", "auf", "für", "über"), 0,
            "denken an + accusativo."),
        completaUnita("vpr-x2e4", "Sie freut sich ___ das Geschenk.", "über",
            "sich freuen über + accusativo (cosa già ricevuta)."),
        sceltaUnita("vpr-x2e5", "Ich danke dir ___ die Hilfe.", listOf("für", "über", "auf", "an"), 0,
            "danken für + accusativo."),
        produciUnita("vpr-x2e6", "„Partecipo alla riunione.“", "Ich nehme an der Besprechung teil.",
            "teilnehmen an + dativo: an der Besprechung.")
    ),

    // ---- A2: pronomi indefiniti ----
    "a2-indefinitpronomen" to listOf(
        sceltaUnita("indef-x2e1", "___ hat angerufen, aber ich weiß nicht wer.", listOf("Jemand", "Niemand", "Etwas", "Alles"), 0,
            "jemand = qualcuno (persona)."),
        completaUnita("indef-x2e2", "Ich habe ___ Neues gehört.", "etwas",
            "etwas Neues = qualcosa di nuovo."),
        sceltaUnita("indef-x2e3", "Hast du ___ gesehen?", listOf("jemanden", "jemand", "etwas", "alles"), 0,
            "Al caso accusativo jemand diventa jemanden."),
        completaUnita("indef-x2e4", "___ ist perfekt.", "Nichts",
            "nichts = niente; il verbo resta al singolare."),
        sceltaUnita("indef-x2e5", "Ich möchte ___ Kaltes trinken.", listOf("etwas", "jemand", "niemand", "alles"), 0,
            "etwas Kaltes = qualcosa di freddo."),
        produciUnita("indef-x2e6", "„Tutti sono arrivati.“", "Alle sind angekommen.",
            "alle = tutti; con il verbo al plurale.")
    ),

    // ---- A2: domande indirette ----
    "a2-indirekte-fragen" to listOf(
        sceltaUnita("indirfrag-x2e1", "Ich weiß nicht, ___ er wohnt.", listOf("wo", "wohin", "woher", "wann"), 0,
            "Domanda indiretta di luogo: wo."),
        completaUnita("indirfrag-x2e2", "Kannst du mir sagen, ___ der Zug fährt?", "wann",
            "Il verbo coniugato va in fondo: wann der Zug fährt."),
        sceltaUnita("indirfrag-x2e3", "Sie fragt, ___ ich Zeit habe.", listOf("ob", "dass", "weil", "wenn"), 0,
            "Domanda sì/no riferita: ob."),
        completaUnita("indirfrag-x2e4", "Ich möchte wissen, ___ du am Wochenende machst.", "was",
            "Domanda indiretta sull'oggetto: was."),
        trasformaUnita("indirfrag-x2e5", "Trasforma in domanda indiretta: „Wo ist die Apotheke?“", "Weißt du, wo die Apotheke ist?",
            "Nella domanda indiretta il verbo va in fondo."),
        produciUnita("indirfrag-x2e6", "„Mi chiedo se lui viene.“", "Ich frage mich, ob er kommt.",
            "ob introduce la domanda indiretta sì/no.")
    ),

    // ---- A2: verbi con accusativo e dativo ----
    "a2-verben-akk-dativ" to listOf(
        sceltaUnita("akkdativ-x2e1", "Ich schenke ___ ein Buch.", listOf("ihm", "ihn", "er", "sein"), 0,
            "schenken + dativo (persona): ihm."),
        completaUnita("akkdativ-x2e2", "Er zeigt ___ (wir) die Fotos.", "uns",
            "zeigen + dativo: uns."),
        sceltaUnita("akkdativ-x2e3", "Ich erkläre ___ die Regel.", listOf("dir", "dich", "du", "dein"), 0,
            "erklären + dativo: dir."),
        completaUnita("akkdativ-x2e4", "Sie bringt ___ (ich) einen Kaffee.", "mir",
            "bringen + dativo: mir."),
        sceltaUnita("akkdativ-x2e5", "Ich gebe ___ die Bücher.", listOf("den Kindern", "der Kinder", "die Kinder", "das Kind"), 0,
            "Plurale dativo: den Kindern."),
        produciUnita("akkdativ-x2e6", "„Mostro le foto a loro.“", "Ich zeige ihnen die Fotos.",
            "zeigen + dativo plurale: ihnen.")
    ),

    // ============================ B1 ============================

    // ---- B1: n-Deklination ----
    "b1-n-deklination" to listOf(
        sceltaUnita("ndekl-x2e1", "Ich spreche mit dem ___.", listOf("Studenten", "Student", "Students", "Studentem"), 0,
            "Dopo preposizione, dativo: dem Studenten."),
        completaUnita("ndekl-x2e2", "Das ist das Auto des ___ (Kollege).", "Kollegen",
            "Genitivo della n-Deklination: des Kollegen."),
        sceltaUnita("ndekl-x2e3", "Der ___ hilft mir.", listOf("Student", "Studenten", "Students", "Studentem"), 0,
            "Nominativo singolare senza -n: der Student."),
        completaUnita("ndekl-x2e4", "Ich kenne den ___ (Junge) gut.", "Jungen",
            "Accusativo: den Jungen."),
        sceltaUnita("ndekl-x2e5", "Wir danken dem ___.", listOf("Kunden", "Kunde", "Kund", "Kundes"), 0,
            "Dativo della n-Deklination: dem Kunden."),
        produciUnita("ndekl-x2e6", "„Ho parlato con il collega.“", "Ich habe mit dem Kollegen gesprochen.",
            "Nach einer Präposition steht der Kollege im Dativ: dem Kollegen.")
    ),

    // ---- B1: preposizioni con genitivo ----
    "b1-genitiv-praepositionen" to listOf(
        sceltaUnita("genpraep-x2e1", "___ des Regens bleiben wir zu Hause.", listOf("Wegen", "Trotz", "Während", "Statt"), 0,
            "wegen + genitivo: wegen des Regens (a causa della pioggia)."),
        completaUnita("genpraep-x2e2", "___ des schlechten Wetters gehen wir spazieren.", "Trotz",
            "trotz + genitivo: nonostante il brutto tempo."),
        sceltaUnita("genpraep-x2e3", "___ der Arbeit trinke ich Kaffee.", listOf("Während", "Statt", "Wegen", "Trotz"), 0,
            "während + genitivo: durante il lavoro."),
        completaUnita("genpraep-x2e4", "Er kommt ___ seines Bruders.", "statt",
            "statt + genitivo: al posto di suo fratello."),
        sceltaUnita("genpraep-x2e5", "___ einer Woche muss ich antworten.", listOf("Innerhalb", "Während", "Trotz", "Wegen"), 0,
            "innerhalb + genitivo: entro una settimana."),
        produciUnita("genpraep-x2e6", "„Nonostante la pioggia, usciamo.“", "Trotz des Regens gehen wir aus.",
            "trotz + genitivo: trotz des Regens.")
    ),

    // ---- B1: aggettivi sostantivati ----
    "b1-adjektive-als-nomen" to listOf(
        sceltaUnita("adjnomen-x2e1", "Der ___ kommt heute.", listOf("Neue", "Neuer", "Neuen", "Neues"), 0,
            "Dopo der, nominativo maschile debole: der Neue."),
        completaUnita("adjnomen-x2e2", "Ich sehe einen ___ (neu).", "Neuen",
            "Accusativo maschile debole: einen Neuen."),
        sceltaUnita("adjnomen-x2e3", "Das ___ ist, dass wir gewonnen haben.", listOf("Beste", "Bester", "Besten", "Bestes"), 0,
            "Superlativo sostantivato neutro: das Beste."),
        completaUnita("adjnomen-x2e4", "Sie hilft dem ___ (krank).", "Kranken",
            "Dativo maschile: dem Kranken."),
        sceltaUnita("adjnomen-x2e5", "Die ___ brauchen Hilfe.", listOf("Alten", "Alte", "Alter", "Altes"), 0,
            "Plurale con articolo determinativo: die Alten."),
        produciUnita("adjnomen-x2e6", "„I giovani imparano velocemente.“", "Die Jungen lernen schnell.",
            "die Jungen = i giovani (aggettivo sostantivato al plurale).")
    ),

    // ---- B1: avverbiali preposizionali ----
    "b1-praepositionaladverbien" to listOf(
        sceltaUnita("praepadv-x2e1", "Worauf wartest du? — Ich warte ___.", listOf("darauf", "daran", "darüber", "dafür"), 0,
            "warten auf → darauf."),
        completaUnita("praepadv-x2e2", "Ich freue mich ___.", "darauf",
            "sich freuen auf → darauf."),
        sceltaUnita("praepadv-x2e3", "___ denkst du?", listOf("Woran", "Worauf", "Wofür", "Womit"), 0,
            "denken an → woran."),
        completaUnita("praepadv-x2e4", "Ich interessiere mich ___.", "dafür",
            "sich interessieren für → dafür."),
        sceltaUnita("praepadv-x2e5", "Ich habe nicht ___ gedacht.", listOf("daran", "darauf", "dafür", "damit"), 0,
            "denken an → daran."),
        produciUnita("praepadv-x2e6", "„Ci conto.“", "Ich rechne damit.",
            "rechnen mit → damit.")
    ),

    // ---- B1: frasi comparative ----
    "b1-komparativsaetze" to listOf(
        sceltaUnita("komp-x2e1", "Je mehr ich lerne, ___ besser verstehe ich.", listOf("desto", "als", "wie", "je"), 0,
            "je + comparativo ... desto + comparativo."),
        completaUnita("komp-x2e2", "Er ist größer ___ ich.", "als",
            "Confronto tra due cose diverse: als."),
        sceltaUnita("komp-x2e3", "Sie ist genauso fleißig ___ ihre Schwester.", listOf("wie", "als", "desto", "je"), 0,
            "Uguaglianza: (genau)so ... wie."),
        completaUnita("komp-x2e4", "Je länger ich warte, ___ (desto) ungeduldiger werde ich.", "desto",
            "Seconda parte del confronto proporzionale: desto."),
        sceltaUnita("komp-x2e5", "Das ist ___ als ich dachte.", listOf("schwieriger", "schwierig", "schwierigste", "am schwierigsten"), 0,
            "Comparativo davanti a als: schwieriger."),
        produciUnita("komp-x2e6", "„Più tempo hai, più cose puoi fare.“", "Je mehr Zeit du hast, desto mehr Dinge kannst du machen.",
            "Struttura je ... desto con due comparativi.")
    ),

    // ---- B1: passivo al passato ----
    "b1-passiv-vergangenheit" to listOf(
        sceltaUnita("passver-x2e1", "Das Haus ___ 1990 gebaut.", listOf("wurde", "wird", "ist", "hat"), 0,
            "Passivo al Präteritum singolare: wurde gebaut."),
        completaUnita("passver-x2e2", "Die Briefe ___ gestern geschickt.", "wurden",
            "Plurale al Präteritum: wurden geschickt."),
        sceltaUnita("passver-x2e3", "Die Rechnung ___ gestern bezahlt worden.", listOf("ist", "wird", "wurde", "hat"), 0,
            "Passivo al Perfekt: ist ... worden."),
        completaUnita("passver-x2e4", "Die Prüfung ist gut bestanden ___.", "worden",
            "Nel passivo al Perfekt il participio di werden è worden."),
        sceltaUnita("passver-x2e5", "Früher ___ hier viel gebaut.", listOf("wurde", "worden", "ist", "hat"), 0,
            "Präteritum passivo: wurde gebaut."),
        produciUnita("passver-x2e6", "„Il libro fu scritto nel 1920.“", "Das Buch wurde 1920 geschrieben.",
            "Passivo al Präteritum: wurde + Partizip II.")
    ),

    // ---- B1: frasi temporali ----
    "b1-temporale-nebensaetze" to listOf(
        sceltaUnita("temp-x2e1", "___ ich koche, höre ich Musik.", listOf("Während", "Nachdem", "Bevor", "Seitdem"), 0,
            "während = mentre (contemporaneità)."),
        completaUnita("temp-x2e2", "___ ich gegessen habe, gehe ich spazieren.", "Nachdem",
            "nachdem = dopo che; verbo in fondo."),
        sceltaUnita("temp-x2e3", "Ich warte hier, ___ du zurückkommst.", listOf("bis", "seit", "damit", "obwohl"), 0,
            "bis = finché/fino a quando."),
        completaUnita("temp-x2e4", "___ er kam, regnete es.", "Als",
            "als = quando (un evento passato unico)."),
        sceltaUnita("temp-x2e5", "___ ich in Berlin wohne, lerne ich Deutsch.", listOf("Seitdem", "Als", "Wenn", "Damit"), 0,
            "seitdem = da quando."),
        produciUnita("temp-x2e6", "„Prima di uscire, chiudo la finestra.“", "Bevor ich ausgehe, schließe ich das Fenster.",
            "bevor = prima che; nella subordinata il verbo va in fondo.")
    ),

    // ============================ B2 ============================

    // ---- B2: passivo sostitutivo ----
    "b2-passiv-ersatz" to listOf(
        sceltaUnita("passersatz-x2e1", "Das Formular ___ sich online ausfüllen.", listOf("lässt", "wird", "ist", "hat"), 0,
            "sich lassen + infinito: lässt sich ausfüllen."),
        completaUnita("passersatz-x2e2", "Die Aufgabe ist bis morgen zu ___ (lösen).", "lösen",
            "sein + zu + infinito: ist zu lösen."),
        sceltaUnita("passersatz-x2e3", "___ kann das Problem leicht lösen.", listOf("Man", "Wer", "Es", "Das"), 0,
            "Passivo sostitutivo con man: Man kann das Problem lösen."),
        completaUnita("passersatz-x2e4", "Das Problem ___ sich leicht erklären.", "lässt",
            "sich lassen + infinito: lässt sich erklären."),
        sceltaUnita("passersatz-x2e5", "Die Rechnung ist sofort zu ___.", listOf("bezahlen", "bezahlt", "bezahlend", "bezahlt werden"), 0,
            "sein + zu + infinito attivo con valore passivo: zu bezahlen."),
        produciUnita("passersatz-x2e6", "„Questa domanda si può discutere.“", "Diese Frage lässt sich diskutieren.",
            "Passivo sostitutivo: sich lassen + infinito.")
    ),

    // ---- B2: passivo stativo ----
    "b2-zustandspassiv" to listOf(
        sceltaUnita("zustand-x2e1", "Die Tür ist ___.", listOf("geöffnet", "öffnen", "geöffnen", "öffnend"), 0,
            "Passivo stativo: sein + Partizip II (geöffnet)."),
        completaUnita("zustand-x2e2", "Das Geschäft ist heute ___ (schließen).", "geschlossen",
            "Stato risultante: ist geschlossen."),
        sceltaUnita("zustand-x2e3", "Der Tisch war schon ___.", listOf("gedeckt", "decken", "gedeckt werden", "deckend"), 0,
            "Passivo stativo al passato: war gedeckt."),
        completaUnita("zustand-x2e4", "Die Straße ist ___ (sperren).", "gesperrt",
            "La strada è chiusa (stato): ist gesperrt."),
        sceltaUnita("zustand-x2e5", "Die Arbeit ___ erledigt.", listOf("ist", "wird", "hat", "lässt"), 0,
            "Risultato/stato: sein + Partizip II → ist erledigt."),
        produciUnita("zustand-x2e6", "„La finestra è chiusa.“", "Das Fenster ist geschlossen.",
            "Passivo stativo: sein + Partizip II.")
    ),

    // ---- B2: particelle modali ----
    "b2-modalpartikeln" to listOf(
        sceltaUnita("modalpart-x2e1", "Wo warst du ___ so lange?", listOf("denn", "damit", "deshalb", "obwohl"), 0,
            "denn rende la domanda interessata, non ostile."),
        completaUnita("modalpart-x2e2", "Nimm ___ noch ein Stück Kuchen!", "doch",
            "doch rende l'invito più cordiale e insistente."),
        sceltaUnita("modalpart-x2e3", "Das ist ___ nicht mein Problem.", listOf("ja", "denn", "mal", "eben"), 0,
            "ja sottolinea che il fatto è evidente."),
        completaUnita("modalpart-x2e4", "Schau ___ mal, wer da kommt!", "doch",
            "doch + mal: invito gentile a guardare."),
        sceltaUnita("modalpart-x2e5", "Das war ___ klar!", listOf("ja", "denn", "mal", "halt"), 0,
            "ja esprime ovvietà: era ovviamente chiaro."),
        produciUnita("modalpart-x2e6", "„Non è poi così difficile.“", "Das ist doch nicht so schwer.",
            "doch attenua e incoraggia: non è poi così difficile.")
    ),

    // ---- B2: parole di riferimento nel testo ----
    "b2-referenzwoerter" to listOf(
        sceltaUnita("referenz-x2e1", "Ich habe zwei Bücher: ___ hier ist neu.", listOf("dieses", "dieser", "diesem", "diesen"), 0,
            "Buch è neutro: dieses Buch."),
        sceltaUnita("referenz-x2e2", "___ , der zu spät kommt, verliert den Platz.", listOf("Derjenige", "Diejenige", "Dasjenige", "Denjenige"), 0,
            "derjenige = colui che; maschile nominativo."),
        completaUnita("referenz-x2e3", "Wir wohnen in ___ Haus wie unsere Eltern.", "demselben",
            "derselbe al dativo neutro: in demselben Haus."),
        sceltaUnita("referenz-x2e4", "Das ist ___ Meinung wie meine.", listOf("dieselbe", "dasselbe", "derselbe", "diejenige"), 0,
            "Meinung è femminile: dieselbe Meinung."),
        sceltaUnita("referenz-x2e5", "Er hat ein Auto. ___ ist rot.", listOf("Das", "Der", "Die", "Den"), 0,
            "Ripresa neutra dell'intera informazione: das."),
        produciUnita("referenz-x2e6", "„Lo stesso vale per me.“", "Dasselbe gilt für mich.",
            "dasselbe riprende la stessa cosa già detta.")
    ),

    // ---- B2: passivo con i modali ----
    "b2-passiv-modal" to listOf(
        sceltaUnita("b2pm-x2e1", "Die Akten ___ morgen unterschrieben werden.", listOf("müssen", "muss", "müsst", "müssten"), 0,
            "Soggetto plurale: die Akten müssen ... werden."),
        completaUnita("b2pm-x2e2", "Das Medikament darf nicht mit Alkohol ___ werden. (nehmen)", "genommen",
            "Partizip II di nehmen: genommen."),
        sceltaUnita("b2pm-x2e3", "Die Wohnung ___ sofort renoviert werden.", listOf("muss", "müssen", "musst", "mögen"), 0,
            "Soggetto singolare: die Wohnung muss ... werden."),
        produciUnita("b2pm-x2e4", "„La ricetta deve essere firmata dal medico.“", "Das Rezept muss vom Arzt unterschrieben werden.",
            "müssen + Partizip II + werden; l'agente con von + dativo.")
    ),

    // ---- B2: Konjunktiv II passato ----
    "b2-konjunktiv2-vergangenheit" to listOf(
        sceltaUnita("b2kv-x2e1", "Wenn wir früher losgefahren ___, hätten wir den Zug bekommen.", listOf("wären", "hätten", "waren", "hatten"), 0,
            "losfahren indica movimento: wären losgefahren."),
        completaUnita("b2kv-x2e2", "Ich ___ das nicht gemacht. (haben, Konjunktiv II)", "hätte",
            "Konjunktiv II passato: hätte gemacht."),
        sceltaUnita("b2kv-x2e3", "Er ___ beinahe den Bus verpasst.", listOf("hätte", "wäre", "hatte", "war"), 0,
            "verpassen forma il passato con haben: hätte verpasst."),
        produciUnita("b2kv-x2e4", "„Se avessi saputo, sarei venuto.“", "Wenn ich das gewusst hätte, wäre ich gekommen.",
            "Konjunktiv II passato: hätte + Partizip II, wäre + Partizip II.")
    ),

    // ---- B2: costruzioni participiali ----
    "b2-partizipialkonstruktionen" to listOf(
        sceltaUnita("b2pk-x2e1", "Die von der Firma ___ Geräte sind teuer.", listOf("gekauften", "gekauft", "gekaufte", "kaufen"), 0,
            "Plurale con articolo determinativo: die gekauften Geräte."),
        completaUnita("b2pk-x2e2", "Der Patient, der wartet → der ___ Patient. (warten, Partizip I)", "wartende",
            "Participio presente + desinenza: der wartende Patient."),
        sceltaUnita("b2pk-x2e3", "Das ___ Formular bitte unterschreiben.", listOf("ausgefüllte", "ausgefüllt", "ausfüllen", "ausfüllende"), 0,
            "Participio passato attributivo: das ausgefüllte Formular."),
        produciUnita("b2pk-x2e4", "„Il documento firmato ieri è arrivato.“", "Das gestern unterschriebene Dokument ist angekommen.",
            "Participio passato con avverbio: das gestern unterschriebene Dokument.")
    ),

    // ---- B2: verbi con preposizione ----
    "b2-verben-praepositionen" to listOf(
        sceltaUnita("b2vp-x2e1", "Wir freuen uns ___ die Ferien.", listOf("auf", "für", "an", "über"), 0,
            "sich freuen auf + accusativo (attesa)."),
        completaUnita("b2vp-x2e2", "Sie hat sich ___ den neuen Job beworben. (preposizione)", "um",
            "sich bewerben um + accusativo."),
        sceltaUnita("b2vp-x2e3", "Ich habe mich ___ das Angebot entschieden.", listOf("für", "auf", "an", "über"), 0,
            "sich entscheiden für + accusativo."),
        produciUnita("b2vp-x2e4", "„Dipende dal tempo.“", "Das hängt vom Wetter ab.",
            "abhängen von + dativo: von dem → vom Wetter.")
    ),

    // ---- B2: relative con was/wo ----
    "b2-relative-was-wo" to listOf(
        sceltaUnita("b2rw-x2e1", "Das ist das Beste, ___ ich je gesehen habe.", listOf("was", "das", "wo", "wer"), 0,
            "Dopo un superlativo sostantivato si usa was."),
        completaUnita("b2rw-x2e2", "Die Stadt, ___ ich geboren bin, ist klein.", "wo",
            "Relativa di luogo: wo."),
        sceltaUnita("b2rw-x2e3", "___ du mir erzählt hast, ist unglaublich.", listOf("Was", "Das", "Wo", "Wer"), 0,
            "La relativa che riprende un'intera frase usa was."),
        produciUnita("b2rw-x2e4", "„Chi non rischia, non guadagna.“", "Wer nicht wagt, der nicht gewinnt.",
            "Struttura correlativa wer ... der ...")
    ),

    // ---- B2: stile nominale ----
    "b2-nominalstil" to listOf(
        sceltaUnita("b2ns-x2e1", "Die ___ der Ergebnisse dauerte lange.", listOf("Auswertung", "auswerten", "ausgewertet", "Auswerten"), 0,
            "auswerten → die Auswertung (sostantivo femminile in -ung)."),
        completaUnita("b2ns-x2e2", "verbessern → die ___", "Verbesserung",
            "Suffisso -ung: die Verbesserung."),
        sceltaUnita("b2ns-x2e3", "___ seiner Erfahrung konnte er helfen.", listOf("Wegen", "Weil", "Obwohl", "Trotz"), 0,
            "wegen + genitivo: a causa della sua esperienza."),
        produciUnita("b2ns-x2e4", "„L'aumento dei prezzi preoccupa i clienti.“", "Die Erhöhung der Preise beunruhigt die Kunden.",
            "Stile nominale: die Erhöhung der Preise (genitivo).")
    )
)
