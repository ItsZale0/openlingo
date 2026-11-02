package com.alessandro.tedesco.data

// ============================================================
// Unità aggiunte (parte 4): residui vs Grammatik Aktiv A1-B1
// ============================================================

internal fun unitaA2VerbenAkkDativ(): UnitaGrammatica = unitaGrammatica(
    id = "a2-verben-akk-dativ",
    livello = "A2",
    titolo = "Verbi con accusativo e dativo",
    sottotitolo = "Sagen, zeigen, erklären, schicken, wünschen",
    obiettivo = "Usare i verbi che reggono sia un complemento oggetto (accusativo) sia un complemento di termine (dativo).",
    minuti = 12,
    prerequisiti = listOf("a1-akkusativ", "a2-dativo"),
    regola = listOf(
        "Alcuni verbi tedeschi reggono due oggetti: un accusativo (cosa) e un dativo (persona).",
        "Verbi tipici: sagen (dire), zeigen (mostrare), erklären (spiegare), schicken (mandare), wünschen (augurare), geben (dare), bringen (portare), empfehlen (raccomandare), erzählen (raccontare), versprechen (promettere).",
        "Ordine tipico: verbo + dativo (persona) + accusativo (cosa): Ich sage dir die Wahrheit.",
        "Se l'accusativo è un pronome, viene prima del dativo: Ich sage es dir. (non: Ich sage dir es.)",
        "Se il dativo è un pronome, viene prima dell'accusativo (se l'accusativo è un nome): Ich sage dir die Wahrheit."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Verbi con accusativo e dativo",
            intestazioni = listOf("Verbo", "Dativo (persona)", "Accusativo (cosa)", "Esempio"),
            righe = listOf(
                listOf("sagen", "dir/mir", "die Wahrheit", "Ich sage dir die Wahrheit."),
                listOf("zeigen", "dir/mir", "das Buch", "Ich zeige dir das Buch."),
                listOf("erklären", "dir/mir", "die Regel", "Ich erkläre dir die Regel."),
                listOf("schicken", "dir/mir", "einen Brief", "Ich schicke dir einen Brief."),
                listOf("wünschen", "dir/mir", "viel Glück", "Ich wünsche dir viel Glück."),
                listOf("geben", "dir/mir", "das Buch", "Ich gebe dir das Buch."),
                listOf("bringen", "dir/mir", "das Wasser", "Ich bringe dir das Wasser."),
                listOf("empfehlen", "dir/mir", "das Restaurant", "Ich empfehle dir das Restaurant.")
            )
        ),
        tabellaUnita(
            titolo = "Ordine degli oggetti",
            intestazioni = listOf("Caso", "Regola", "Esempio"),
            righe = listOf(
                listOf("Entrambi nomi", "dativo prima, accusativo dopo", "Ich sage dir die Wahrheit."),
                listOf("Accusativo pronome", "accusativo prima, dativo dopo", "Ich sage es dir."),
                listOf("Dativo pronome", "dativo prima, accusativo dopo", "Ich sage dir das Buch.")
            ),
            nota = "Se entrambi sono pronomi: accusativo prima, dativo dopo (Ich sage es dir)."
        ),
        tabellaUnita(
            titolo = "Pronomi al dativo (ripasso)",
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
            ),
            nota = "Plurale: den + -n sul sostantivo (den Kindern)."
        )
    ),
    esempi = listOf(
        esempioUnita("Ich sage dir die Wahrheit.", "Ti dico la verità."),
        esempioUnita("Ich zeige dir das Buch.", "Ti mostro il libro."),
        esempioUnita("Ich schicke dir einen Brief.", "Ti mando una lettera."),
        esempioUnita("Ich wünsche dir viel Glück.", "Ti auguro molta fortuna."),
        esempioUnita("Ich sage es dir.", "Te lo dico.")
    ),
    errori = listOf(
        erroreUnita("Ich sage die Wahrheit dir.", "Ich sage dir die Wahrheit.", "Dativo (persona) prima, accusativo (cosa) dopo."),
        erroreUnita("Ich sage dir es.", "Ich sage es dir.", "Se l'accusativo è un pronome, viene prima del dativo."),
        erroreUnita("Ich zeige das Buch dir.", "Ich zeige dir das Buch.", "Dativo prima, accusativo dopo.")
    ),
    merksatz = "Verbi con due oggetti: dativo (persona) + accusativo (cosa); se l'accusativo è un pronome, viene prima.",
    esercizi = listOf(
        sceltaUnita("vad-01", "Ich sage ___ die Wahrheit.", listOf("dir", "dich", "du", "dein"), 0, "sagen + dativo (persona): dir."),
        sceltaUnita("vad-02", "Ich zeige ___ das Buch.", listOf("dir", "dich", "du", "dein"), 0, "zeigen + dativo: dir."),
        sceltaUnita("vad-03", "Ich schicke ___ einen Brief.", listOf("dir", "dich", "du", "dein"), 0, "schicken + dativo: dir."),
        sceltaUnita("vad-04", "Ich wünsche ___ viel Glück.", listOf("dir", "dich", "du", "dein"), 0, "wünschen + dativo: dir."),
        sceltaUnita("vad-05", "Ich sage ___ dir.", listOf("es", "ihn", "ihr", "sie"), 0, "Accusativo pronome prima del dativo: es dir."),
        sceltaUnita("vad-06", "Ich gebe ___ das Buch.", listOf("dir", "dich", "du", "dein"), 0, "geben + dativo: dir.")
,
        produciUnita("a2-verben-akd-prod", "„Do il libro a lui.“", "Ich gebe ihm das Buch.", "geben + dativo (ihm) + accusativo (das Buch)."),
    )
)

internal fun unitaB1PassivVergangenheit(): UnitaGrammatica = unitaGrammatica(
    id = "b1-passiv-vergangenheit",
    livello = "B1",
    titolo = "Passivo in der Vergangenheit",
    sottotitolo = "Passiv Perfekt und Präteritum",
    obiettivo = "Formare e usare il passivo al Perfekt e al Präteritum.",
    minuti = 12,
    prerequisiti = listOf("a2-passivo"),
    regola = listOf(
        "Passivo al Perfekt: sein + Partizip II + worden. Das Haus ist gebaut worden. (la casa è stata costruita)",
        "Passivo al Präteritum: wurde + Partizip II. Das Haus wurde gebaut. (la casa fu costruita / venne costruita)",
        "Ausiliare del Perfekt passivo: sein (non haben).",
        "Forma completa del Perfekt passivo: Partizip II + worden (non geworden).",
        "Il Präteritum passivo è più comune nel tedesco scritto/formale; il Perfekt passivo nel parlato."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Passivo ai vari tempi",
            intestazioni = listOf("Tempo", "Forma", "Esempio"),
            righe = listOf(
                listOf("Präsens", "wird + Partizip II", "Das Haus wird gebaut."),
                listOf("Präteritum", "wurde + Partizip II", "Das Haus wurde gebaut."),
                listOf("Perfekt", "ist + Partizip II + worden", "Das Haus ist gebaut worden."),
                listOf("Plusquamperfekt", "war + Partizip II + worden", "Das Haus war gebaut worden.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Das Haus wurde gebaut.", "La casa fu costruita."),
        esempioUnita("Das Haus ist gebaut worden.", "La casa è stata costruita."),
        esempioUnita("Der Brief wurde geschrieben.", "La lettera fu scritta."),
        esempioUnita("Der Brief ist geschrieben worden.", "La lettera è stata scritta.")
    ),
    errori = listOf(
        erroreUnita("Das Haus ist gebaut geworden.", "Das Haus ist gebaut worden.", "Perfekt passivo: worden (non geworden)."),
        erroreUnita("Das Haus wurde gebaut worden.", "Das Haus wurde gebaut. / Das Haus ist gebaut worden.", "Non si mescolano: Präteritum (wurde) o Perfekt (ist...worden)."),
        erroreUnita("Das Haus hat gebaut worden.", "Das Haus ist gebaut worden.", "Ausiliare del Perfekt passivo: sein (ist), non haben (hat).")
    ),
    merksatz = "Präteritum passivo: wurde + Partizip II. Perfekt passivo: ist + Partizip II + worden.",
    esercizi = listOf(
        sceltaUnita("passv-01", "Das Haus ___ gebaut. (Präteritum)", listOf("wird", "wurde", "ist", "war"), 1, "Präteritum passivo: wurde."),
        sceltaUnita("passv-02", "Das Haus ist gebaut ___. (Perfekt)", listOf("worden", "geworden", "worden sein", "geworden sein"), 0, "Perfekt passivo: worden."),
        sceltaUnita("passv-03", "Der Brief ___ geschrieben. (Präteritum)", listOf("wird", "wurde", "ist", "war"), 1, "Präteritum passivo: wurde."),
        sceltaUnita("passv-04", "Der Brief ist geschrieben ___. (Perfekt)", listOf("worden", "geworden", "worden sein", "geworden sein"), 0, "Perfekt passivo: worden."),
        sceltaUnita("passv-05", "Ausiliare del Perfekt passivo:", listOf("haben", "sein", "werden", "wurde"), 1, "Perfekt passivo: sein (ist)."),
        sceltaUnita("passv-06", "Das Haus war gebaut ___. (Plusquamperfekt)", listOf("worden", "geworden", "worden sein", "geworden sein"), 0, "Plusquamperfekt passivo: worden.")
,
        produciUnita("b1-passver-prod", "„La casa fu costruita nel 1900.“", "Das Haus wurde 1900 gebaut.", "Passivo al Präteritum: wurde + Partizip II (gebaut)."),
    )
)

internal fun unitaB1TemporaleNebensaetze(): UnitaGrammatica = unitaGrammatica(
    id = "b1-temporale-nebensaetze",
    livello = "B1",
    titolo = "Frasi temporali",
    sottotitolo = "Wenn, als, während, bevor, nachdem, seit",
    obiettivo = "Esprimere relazioni temporali con le congiunzioni temporali più importanti.",
    minuti = 14,
    prerequisiti = listOf("a2-subordinate"),
    regola = listOf(
        "Le congiunzioni temporali introducono una subordinata con il verbo alla fine.",
        "wenn: per presente, futuro o eventi ripetuti. Wenn ich Zeit habe, gehe ich ins Kino.",
        "als: per un singolo evento nel passato. Als ich klein war, spielte ich im Garten.",
        "während: mentre (contemporaneità). Während ich arbeite, hört er Musik.",
        "bevor: prima che. Bevor ich gehe, räume ich auf.",
        "nachdem: dopo che (l'azione precedente va al Plusquamperfekt). Nachdem er gegessen hatte, ging er.",
        "seit/seitdem: da quando. Seitdem er hier wohnt, ist er glücklicher.",
        "bis: fino a quando. Ich warte, bis er kommt."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Congiunzioni temporali",
            intestazioni = listOf("Congiunzione", "Uso", "Esempio"),
            righe = listOf(
                listOf("wenn", "presente/futuro/eventi ripetuti", "Wenn ich Zeit habe, gehe ich ins Kino."),
                listOf("als", "singolo evento passato", "Als ich klein war, spielte ich im Garten."),
                listOf("während", "contemporaneità", "Während ich arbeite, hört er Musik."),
                listOf("bevor", "prima che", "Bevor ich gehe, räume ich auf."),
                listOf("nachdem", "dopo che (+Plusquamperfekt)", "Nachdem er gegessen hatte, ging er."),
                listOf("seit/seitdem", "da quando", "Seitdem er hier wohnt, ist er glücklicher."),
                listOf("bis", "fino a quando", "Ich warte, bis er kommt.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Wenn ich Zeit habe, gehe ich ins Kino.", "Se ho tempo, vado al cinema."),
        esempioUnita("Als ich klein war, spielte ich im Garten.", "Quando ero piccolo, giocavo in giardino."),
        esempioUnita("Während ich arbeite, hört er Musik.", "Mentre lavoro, lui ascolta musica."),
        esempioUnita("Bevor ich gehe, räume ich auf.", "Prima di andare, faccio ordine."),
        esempioUnita("Nachdem er gegessen hatte, ging er.", "Dopo che aveva mangiato, se ne andò."),
        esempioUnita("Seitdem er hier wohnt, ist er glücklicher.", "Da quando abita qui, è più felice.")
    ),
    errori = listOf(
        erroreUnita("Als ich Zeit habe, gehe ich ins Kino.", "Wenn ich Zeit habe, gehe ich ins Kino.", "Evento ripetuto/presente: wenn, non als."),
        erroreUnita("Wenn ich klein war, spielte ich im Garten.", "Als ich klein war, spielte ich im Garten.", "Singolo evento passato: als, non wenn."),
        erroreUnita("Nachdem er gegessen hat, ging er.", "Nachdem er gegessen hatte, ging er.", "nachdem + Plusquamperfekt nella subordinata.")
    ),
    merksatz = "wenn = presente/futuro/ripetuto; als = singolo evento passato; nachdem + Plusquamperfekt.",
    esercizi = listOf(
        sceltaUnita("temp-01", "___ ich klein war, spielte ich im Garten.", listOf("Wenn", "Als", "Während", "Bevor"), 1, "Singolo evento passato: Als."),
        sceltaUnita("temp-02", "___ ich Zeit habe, gehe ich ins Kino.", listOf("Wenn", "Als", "Während", "Bevor"), 0, "Presente/evento ripetuto: Wenn."),
        sceltaUnita("temp-03", "___ ich arbeite, hört er Musik.", listOf("Wenn", "Als", "Während", "Bevor"), 2, "Contemporaneità: Während."),
        sceltaUnita("temp-04", "___ ich gehe, räume ich auf.", listOf("Wenn", "Als", "Während", "Bevor"), 3, "Prima che: Bevor."),
        sceltaUnita("temp-05", "Nachdem er gegessen ___, ging er.", listOf("hat", "hatte", "war", "ist"), 1, "nachdem + Plusquamperfekt: hatte."),
        sceltaUnita("temp-06", "___ er hier wohnt, ist er glücklicher.", listOf("Wenn", "Als", "Seitdem", "Bevor"), 2, "Da quando: Seitdem.")
,
        produciUnita("b1-temp-prod", "„Mentre cucino, ascolto la radio.“", "Während ich koche, höre ich Radio.", "während = mentre; nella subordinata il verbo va in fondo."),
    )
)
