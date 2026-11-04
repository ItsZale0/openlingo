package com.alessandro.tedesco.data

/**
 * Modalità Immersione: didattica della modalità separata attivabile da Impostazioni.
 *
 * Metodo a 5 passi, da "Alle lernen Deutsch falsch (So funktioniert es)":
 *  1. Mentalità: il tedesco è logico; lo si capisce, non lo si memorizza.
 *  2. Input massiccio: circa 80% ascolto+lettura e 20% produzione, con
 *     materiale comprensibile (qui: episodi con il 95%+ di parole note).
 *  3. Vocabolario in FRASI (non parole isolate) + scomposizione dei
 *     composti: ogni pezzo conosciuto apre dieci composti nuovi.
 *  4. Shadowing: ripetere in tempo reale con un parlante nativo (qui: TTS).
 *  5. Sistema in 3 blocchi: immersione, ripasso, ascolto passivo
 *     (bonus: 15-20 minuti prima di dormire, da svegli: il sonno consolida).
 *
 * Scelte didattiche (dalla ricerca su input comprensibile, narrow reading,
 * repeated reading e retrieval practice):
 *  - ogni livello ha una SERIE di episodi con gli stessi personaggi e lo
 *    stesso tema: meno carico lessicale, più comprensione e più piacere;
 *  - tre parole chiave vengono pre-insegnate prima della storia, così il
 *    testo resta sopra la soglia del 95% di parole note;
 *  - il significato delle frasi da annotare è nascosto: l'utente lo recupera
 *    a memoria e poi riceve subito il feedback (retrieval + feedback);
 *  - le frasi della storia tornano nell'ascolto passivo: rilettura e
 *    ri-ascolto, non materiale sempre nuovo.
 *
 * Tutto offline: frasi e micro-storie sono scritte per livello e la
 * lettura ad alta voce usa il TTS già presente in casa.
 */

/** Una frase del percorso, con la traduzione rivelabile a richiesta. */
data class FraseImmersione(
    val livello: String,
    val tedesco: String,
    val italiano: String,
    /** Parola da evidenziare nella frase (per la raccolta). */
    val parolaChiave: String = "",
    /** Significato della parola chiave, mostrato dopo il recupero. */
    val significatoChiave: String = ""
)

/** Parola pre-insegnata prima della lettura: abbassa il carico lessicale. */
data class Glossa(
    val parola: String,
    val significato: String
)

/** Micro-storia comprensibile: input continuo con difficoltà calibrata. */
data class StoriaImmersione(
    val livello: String,
    /** Nome della serie: gli episodi condividono personaggi e tema. */
    val serie: String,
    val episodio: Int,
    val titolo: String,
    val contesto: String,
    /** Percentuale stimata di parole già note nel testo. */
    val copertura: Int,
    val minuti: Int,
    val paroleChiave: List<Glossa>,
    val frasi: List<FraseImmersione>,
    val domande: List<DomandaComprensione>,
    /**
     * Unità del percorso grammaticale che spiegano le strutture della storia.
     *
     * L'immersione mostra la struttura, non la spiega: il collegamento porta
     * alla regola esplicita, così l'incontro con il testo ha un "dove
     * guardare" e la regola ha subito un esempio vero.
     */
    val regoleCollegate: List<String> = emptyList()
)

/** Domanda di comprensione: verifica leggera, nessuna tabella di grammatica. */
data class DomandaComprensione(
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    /** Perché quella risposta: il feedback spiega, non solo corregge. */
    val spiegazione: String = ""
) {
    fun corretta(scelta: Int?): Boolean = scelta == rispostaCorretta
}

/** Parola ad alta frequenza vista dentro una frase vera. */
data class ParolaInContesto(
    val parola: String,
    val significato: String,
    val frase: String,
    val traduzione: String
)

/** Composto: le parti concatenate compongono la parola intera. */
data class Composto(
    val composto: String,
    val parti: List<String>,
    val significati: List<String>,
    val italiano: String
) {
    fun scomposizione(): String = parti.joinToString(" + ")
}

/** Frase da ombreggiare (blocco shadowing), con una nota sul ritmo. */
data class FraseShadowing(
    val tedesco: String,
    val italiano: String,
    val notaRitmo: String
)

/** Fonte della raccolta: frase + parola da evidenziare + significato. */
data class FraseMiniera(
    val tedesco: String,
    val italiano: String,
    val parola: String,
    val significato: String
)

object DidatticaImmersione {

    val LIVELLI: List<String> = listOf("A0", "A1", "A2", "B1", "B2")

    /** Normalizza "B1 - Intermedio" → "B1"; comunque un livello esistente. */
    fun perLivello(livello: String?): String {
        val codice = livello?.uppercase()
            ?.substringBefore(" - ")?.substringBefore(" ")?.trim().orEmpty()
        return if (codice in LIVELLI) codice else "A1"
    }

    // --------------------------------------------------------------
    // Blocco 1 — Immersione: serie di micro-storie comprensibili
    // --------------------------------------------------------------

    /** Tutti gli episodi del livello (narrow reading: stesso mondo, nuovo testo). */
    fun storie(livello: String?): List<StoriaImmersione> = when (perLivello(livello)) {
        "A0" -> listOf(storiaA0Ep1(), storiaA0Ep2())
        "A1" -> listOf(storiaA1Ep1(), storiaA1Ep2())
        "A2" -> listOf(storiaA2Ep1(), storiaA2Ep2())
        "B1" -> listOf(storiaB1Ep1(), storiaB1Ep2())
        else -> listOf(storiaB2Ep1(), storiaB2Ep2())
    }

    /** Episodio attivo: ruota tra gli episodi della serie a ogni sessione. */
    fun storiaAttiva(livello: String?, giro: Int = 0): StoriaImmersione {
        val episodi = storie(livello)
        if (episodi.isEmpty()) return storie("A1").first()
        val indice = ((giro % episodi.size) + episodi.size) % episodi.size
        return episodi[indice]
    }

    /**
     * Unità grammaticali che spiegano le strutture della storia.
     *
     * Restituisce le unità reali del percorso (non gli id): la UI mostra il
     * titolo e apre la regola con un tocco. Gli id sconosciuti vengono
     * ignorati invece di rompere la sessione.
     */
    fun regoleDellaStoria(storia: StoriaImmersione): List<UnitaGrammatica> =
        storia.regoleCollegate.mapNotNull { GrammaticaSyllabus.perId(it) }

    /**
     * Cinque frasi da annotare, diverse a ogni giro.
     *
     * La rotazione avanza di due posizioni per volta: con sei frasi i tre
     * giri iniziali danno insiemi distinti (0-4, 2-0, 4-2), quindi non si
     * ripete lo stesso blocco due sere di fila.
     */
    fun frasiNuove(livello: String?, giro: Int = 0): List<FraseImmersione> {
        val tutte = frasiMiniera(livello)
        if (tutte.isEmpty()) return emptyList()
        val n = tutte.size
        val taglio = ((giro % n) * 2 % n + n) % n
        val ordine = tutte.drop(taglio) + tutte.take(taglio)
        return ordine.take(5).map { f ->
            FraseImmersione(
                livello = perLivello(livello),
                tedesco = f.tedesco,
                italiano = f.italiano,
                parolaChiave = f.parola,
                significatoChiave = f.significato
            )
        }
    }

    private fun frase(tedesco: String, italiano: String) = FraseImmersione("", tedesco, italiano)

    private fun domanda(
        domanda: String,
        opzioni: List<String>,
        rispostaCorretta: Int,
        spiegazione: String = ""
    ) = DomandaComprensione(domanda, opzioni, rispostaCorretta, spiegazione)

    private fun glossa(parola: String, significato: String) = Glossa(parola, significato)

    // ---------------------------- A0 ----------------------------

    private fun storiaA0Ep1() = StoriaImmersione(
        livello = "A0",
        serie = "Moritz",
        episodio = 1,
        titolo = "Ein Tag mit Moritz",
        regoleCollegate = listOf("a1-separabili", "a0-frase"),
        contesto = "Una frase alla volta, mai due regole insieme. Il tedesco non è caos: sono scaglioni che si ripetono.",
        copertura = 96,
        minuti = 3,
        paroleChiave = listOf(
            glossa("aufstehen", "alzarsi"),
            glossa("duschen", "farsi la doccia"),
            glossa("Loch", "buco")
        ),
        frasi = listOf(
            frase("Moritz steht um halb sieben auf.", "Moritz si alza alle sette e mezza."),
            frase("Er macht zuerst Kaffee.", "Prima fa il caffè."),
            frase("Der Kaffee ist stark.", "Il caffè è forte."),
            frase("Danach duscht er kurz.", "Dopo si fa una doccia veloce."),
            frase("Seine Tasche steht an der Tür.", "La sua borsa è alla porta."),
            frase("Seine Wohnung ist klein.", "Il suo appartamento è piccolo."),
            frase("Im Fenster ist ein kleines Loch.", "Nella finestra c'è un buco piccolo."),
            frase("Der Wind kommt durch das Loch.", "Il vento entra dal buco."),
            frase("Moritz schließt das Fenster.", "Moritz chiude la finestra.")
        ),
        domande = listOf(
            domanda(
                "A che ora si alza Moritz?",
                listOf("Alle fünf", "Um halb sieben", "Um neun Uhr"),
                1,
                "„Halb sieben“ è le 6:30: in tedesco si conta la mezz'ora verso le sette."
            ),
            domanda(
                "Cosa fa per primo?",
                listOf("Duscht", "Macht Kaffee", "Liest"),
                1,
                "La seconda frase dice „Er macht zuerst Kaffee“: prima il caffè, poi la doccia."
            ),
            domanda(
                "Perché chiude la finestra?",
                listOf("Weil der Wind kommt", "Weil es dunkel ist", "Weil es kalt ist"),
                0,
                "Il vento entra dal buco: „Der Wind kommt durch das Loch“."
            )
        )
    )

    private fun storiaA0Ep2() = StoriaImmersione(
        livello = "A0",
        serie = "Moritz",
        episodio = 2,
        titolo = "Das Paket für Moritz",
        regoleCollegate = listOf("a1-imperativo"),
        contesto = "Stessi personaggi, stessa casa: cambia solo una cosa (un pacco). Il resto del vocabolario si ripete, e capire diventa facile.",
        copertura = 95,
        minuti = 3,
        paroleChiave = listOf(
            glossa("Paket", "pacco"),
            glossa("unterschreiben", "firmare"),
            glossa("sich freuen", "rallegrarsi")
        ),
        frasi = listOf(
            frase("Am Dienstag klingelt es an der Tür.", "Martedì suona il campanello."),
            frase("Moritz öffnet die Tür.", "Moritz apre la porta."),
            frase("Ein Mann hält ein Paket in der Hand.", "Un uomo tiene un pacco in mano."),
            frase("Das Paket ist für Moritz.", "Il pacco è per Moritz."),
            frase("Der Mann sagt: „Bitte unterschreiben Sie hier.“", "L'uomo dice: «Firmi qui, per favore.»"),
            frase("Moritz schreibt seinen Namen.", "Moritz scrive il suo nome."),
            frase("Im Paket ist ein neues Buch.", "Nel pacco c'è un libro nuovo."),
            frase("Moritz freut sich sehr.", "Moritz è molto contento.")
        ),
        domande = listOf(
            domanda(
                "Cosa porta l'uomo?",
                listOf("Ein Paket", "Ein Buch", "Eine Tasche"),
                0,
                "L'uomo ha un pacco in mano; il libro è dentro il pacco."
            ),
            domanda(
                "Cosa deve fare Moritz?",
                listOf("Unterschreiben", "Bezahlen", "Telefonieren"),
                0,
                "„Bitte unterschreiben Sie hier“: deve firmare, non pagare."
            ),
            domanda(
                "Cosa c'è nel pacco?",
                listOf("Ein Buch", "Ein Kaffee", "Ein Fenster"),
                0,
                "L'ultima frase lo dice: „Im Paket ist ein neues Buch“."
            )
        )
    )

    // ---------------------------- A1 ----------------------------

    private fun storiaA1Ep1() = StoriaImmersione(
        livello = "A1",
        serie = "Lena",
        episodio = 1,
        titolo = "Einkauf am Markt",
        regoleCollegate = listOf("a1-akkusativ", "a2-subordinate"),
        contesto = "Le stesse strutture in una scena reale: 1000 parole usuali coprono l'85% della conversazione quotidiana.",
        copertura = 96,
        minuti = 3,
        paroleChiave = listOf(
            glossa("Verkäufer", "venditore"),
            glossa("Darum", "per questo"),
            glossa("Radweg", "pista ciclabile")
        ),
        frasi = listOf(
            frase("Am Samstag geht Lena auf den Markt.", "Il sabato Lena va al mercato."),
            frase("Sie kauft Brot, Käse und Äpfel.", "Compra pane, formaggio e mele."),
            frase("Der Verkäufer hat keine Äpfel mehr.", "Il venditore non ha più mele."),
            frase("Darum nimmt sie Birnen.", "Per questo prende le pere."),
            frase("Weil sie viel kauft, braucht sie eine Tasche.", "Poiché compra tanto, le serve una borsa."),
            frase("Später fährt sie mit dem Rad nach Hause.", "Più tardi rientra a casa in bici."),
            frase("Der Radweg ist eine große Hilfe.", "La pista ciclabile è un grande aiuto."),
            frase("Dort ist ein schöner schattiger Hof.", "Lì c'è un cortile ombreggiato."),
            frase("Lena trinkt dort einen Kaffee.", "Lena si beve lì un caffè.")
        ),
        domande = listOf(
            domanda(
                "Cosa prende al posto delle mele?",
                listOf("Birnen", "Eier", "Zitronen"),
                0,
                "„Darum nimmt sie Birnen“: pere al posto delle mele."
            ),
            domanda(
                "Come torna a casa?",
                listOf("Mit dem Rad", "Mit dem Zug", "Mit dem Bus"),
                0,
                "„Später fährt sie mit dem Rad nach Hause“."
            ),
            domanda(
                "Perché prende una borsa?",
                listOf("Weil sie viel kauft", "Weil es regnet", "Weil sie müde ist"),
                0,
                "La frase con „weil“ spiega il motivo: compra molto."
            )
        )
    )

    private fun storiaA1Ep2() = StoriaImmersione(
        livello = "A1",
        serie = "Lena",
        episodio = 2,
        titolo = "Lena am Bahnhof",
        regoleCollegate = listOf("a1-preposizioni-luogo"),
        contesto = "La stessa Lena, un altro posto: la stazione. Le parole del viaggio tornano utili subito.",
        copertura = 95,
        minuti = 3,
        paroleChiave = listOf(
            glossa("Verspätung", "ritardo"),
            glossa("Fahrkarte", "biglietto"),
            glossa("Gleis", "binario")
        ),
        frasi = listOf(
            frase("Am Freitag fährt Lena nach Köln.", "Il venerdì Lena va a Colonia."),
            frase("Sie ist früh am Bahnhof.", "È presto alla stazione."),
            frase("Der Zug hat zehn Minuten Verspätung.", "Il treno ha dieci minuti di ritardo."),
            frase("Lena kauft eine Fahrkarte am Automaten.", "Lena compra un biglietto alla macchinetta."),
            frase("Der Automat nimmt keine Karte.", "La macchinetta non accetta la carta."),
            frase("Darum zahlt sie mit Münzen.", "Per questo paga con monete."),
            frase("Auf Gleis drei wartet schon der Zug.", "Sul binario tre il treno aspetta già."),
            frase("Lena findet einen Platz am Fenster.", "Lena trova un posto al finestrino.")
        ),
        domande = listOf(
            domanda(
                "Dove va Lena?",
                listOf("Nach Köln", "Nach Hamburg", "Nach Hause"),
                0,
                "Prima frase: „Am Freitag fährt Lena nach Köln“."
            ),
            domanda(
                "Perché paga con monete?",
                listOf("Der Automat nimmt keine Karte", "Sie hat kein Geld", "Der Zug ist weg"),
                0,
                "La macchinetta non accetta la carta, quindi servono monete."
            ),
            domanda(
                "Dove aspetta il treno?",
                listOf("Auf Gleis drei", "Am Markt", "Zu Hause"),
                0,
                "„Auf Gleis drei wartet schon der Zug“."
            )
        )
    )

    // ---------------------------- A2 ----------------------------

    private fun storiaA2Ep1() = StoriaImmersione(
        livello = "A2",
        serie = "Marco",
        episodio = 1,
        titolo = "Beim Hautarzt",
        regoleCollegate = listOf("a2-perfekt"),
        contesto = "Perfekt in una scena reale: le azioni finite raccontate con le parole di tutti i giorni.",
        copertura = 95,
        minuti = 3,
        paroleChiave = listOf(
            glossa("Ausschlag", "eruzione cutanea"),
            glossa("Salbe", "pomata"),
            glossa("Juckreiz", "prurito")
        ),
        frasi = listOf(
            frase("Letzte Woche hat Marco einen Ausschlag am Bein bekommen.", "La settimana scorsa Marco ha avuto un'eruzione sulla gamba."),
            frase("Er hat schnell einen Termin beim Hautarzt vereinbart.", "Ha fissato in fretta un appuntamento dal dermatologo."),
            frase("Der Arzt hat die Haut genau betrachtet.", "Il medico ha guardato la pelle con attenzione."),
            frase("„Haben Sie am Wochenende viel geschwitzt?“, fragte er.", "«Ha sudato molto nel weekend?», chiese."),
            frase("„Ein bisschen“, sagte Marco.", "«Un po'», disse Marco."),
            frase("Der Arzt hat gesagt, der Ausschlag ist harmlos.", "Il medico ha detto che l'eruzione è innocua."),
            frase("Er hat eine Salbe gegeben.", "Ha dato una pomata."),
            frase("Der Juckreiz ist bald verschwunden.", "Il prurito è sparito presto."),
            frase("Heute geht es Marco wieder gut.", "Oggi Marco sta di nuovo bene.")
        ),
        domande = listOf(
            domanda(
                "Cosa ha avuto Marco?",
                listOf("Einen Ausschlag", "Eine Grippe", "Husten"),
                0,
                "„Einen Ausschlag am Bein“: un'eruzione sulla gamba."
            ),
            domanda(
                "Chi ha visto?",
                listOf("Den Zahnarzt", "Den Hautarzt", "Den Hausarzt"),
                1,
                "Hautarzt = dermatologo; Zahnarzt è il dentista."
            ),
            domanda(
                "Cosa gli ha dato il medico?",
                listOf("Eine Salbe", "Tabletten", "Einen Verband"),
                0,
                "„Er hat eine Salbe gegeben“: una pomata."
            )
        )
    )

    private fun storiaA2Ep2() = StoriaImmersione(
        livello = "A2",
        serie = "Marco",
        episodio = 2,
        titolo = "Marco in der Apotheke",
        regoleCollegate = listOf("a1-modali"),
        contesto = "Dopo la visita: la ricetta, la farmacia e le istruzioni. Il tedesco che serve davvero quando si è pazienti.",
        copertura = 95,
        minuti = 3,
        paroleChiave = listOf(
            glossa("Rezept", "ricetta medica"),
            glossa("Apotheke", "farmacia"),
            glossa("Packung", "confezione")
        ),
        frasi = listOf(
            frase("Marco hat ein Rezept vom Hautarzt bekommen.", "Marco ha ricevuto una ricetta dal dermatologo."),
            frase("Er geht in die Apotheke an der Ecke.", "Va nella farmacia all'angolo."),
            frase("Die Apothekerin liest das Rezept genau.", "La farmacista legge la ricetta con attenzione."),
            frase("Sie sagt: „Die Salbe müssen Sie zweimal am Tag benutzen.“", "Dice: «La pomata deve usarla due volte al giorno.»"),
            frase("Marco fragt: „Darf ich damit duschen?“", "Marco chiede: «Posso fare la doccia con questa?»"),
            frase("„Ja, aber bitte nicht zu heiß“, antwortet sie.", "«Sì, ma non troppo calda», risponde."),
            frase("Sie gibt ihm noch eine Packung Tabletten.", "Gli dà anche una confezione di compresse."),
            frase("Nach einer Woche ist die Haut viel besser.", "Dopo una settimana la pelle è molto meglio.")
        ),
        domande = listOf(
            domanda(
                "Cosa ha ricevuto Marco?",
                listOf("Ein Rezept", "Ein Paket", "Eine Fahrkarte"),
                0,
                "Il dermatologo gli ha dato una ricetta: „ein Rezept“."
            ),
            domanda(
                "Quante volte al giorno usa la pomata?",
                listOf("Zweimal", "Einmal", "Dreimal"),
                0,
                "„Zweimal am Tag“: due volte al giorno."
            ),
            domanda(
                "Cosa gli dà ancora la farmacista?",
                listOf("Tabletten", "Kaffee", "Ein Buch"),
                0,
                "Oltre alla pomata, una confezione di compresse."
            )
        )
    )

    // ---------------------------- B1 ----------------------------

    private fun storiaB1Ep1() = StoriaImmersione(
        livello = "B1",
        serie = "Frau Bachmann",
        episodio = 1,
        titolo = "Der Anruf, der alles änderte",
        regoleCollegate = listOf("a2-subordinate", "a2-relative"),
        contesto = "Proposizioni con verbo in fondo: la regola, vista dentro una frase vera, non in una tabella.",
        copertura = 94,
        minuti = 4,
        paroleChiave = listOf(
            glossa("Vermieter", "padrone di casa"),
            glossa("Mietvertrag", "contratto d'affitto"),
            glossa("aufgeben", "arrendersi")
        ),
        frasi = listOf(
            frase("Als Frau Bachmann zurück nach Hamburg kam, war sie unruhig.", "Quando la signora Bachmann tornò ad Amburgo, era agitata."),
            frase("Sie hatte gehofft, dass die Wohnung bald frei wird.", "Aveva sperato che l'appartamento si liberasse presto."),
            frase("Doch der Vermieter rief an und sagte, dass die Miete steigt.", "Ma il padrone di casa chiamò e disse che l'affitto sale."),
            frase("Obwohl sie müde war, lief sie zum Rathaus.", "Sebbene fosse stanca, corse al municipio."),
            frase("Der Termin, den sie sich notiert hatte, war schon vergeben.", "L'appuntamento che si era segnato era già occupato."),
            frase("Am Ende war sie froh, dass sie nicht aufgab.", "Alla fine era felice di non essersi arresa."),
            frase("Danach fand sie eine Wohnung mit kleinem Hof.", "Dopo trovò un appartamento con un piccolo cortile."),
            frase("Sie unterschrieb den Mietvertrag mit ruhiger Hand.", "Firmò il contratto d'affitto con mano tranquilla."),
            frase("„Jetzt kann ich wieder atmen“, sagte sie.", "«Ora posso di nuovo respirare», disse.")
        ),
        domande = listOf(
            domanda(
                "Come si sentiva al ritorno?",
                listOf("Unruhig", "Glücklich", "Ruhig"),
                0,
                "„war sie unruhig“: agitata, non calma."
            ),
            domanda(
                "Dove corse?",
                listOf("Zum Rathaus", "Zum Bahnhof", "Zum Arzt"),
                0,
                "„lief sie zum Rathaus“: al municipio."
            ),
            domanda(
                "Cosa trovò alla fine?",
                listOf("Eine Wohnung", "Ein Auto", "Einen Job"),
                0,
                "„fand sie eine Wohnung mit kleinem Hof“."
            )
        )
    )

    private fun storiaB1Ep2() = StoriaImmersione(
        livello = "B1",
        serie = "Frau Bachmann",
        episodio = 2,
        titolo = "Der erste Tag im neuen Büro",
        regoleCollegate = listOf("b1-konnektoren"),
        contesto = "La stessa donna, un capitolo nuovo: il lavoro. Congiunzioni e verbo in fondo tornano nella stessa forma, così si riconoscono senza tradurre.",
        copertura = 94,
        minuti = 4,
        paroleChiave = listOf(
            glossa("Besprechung", "riunione"),
            glossa("Erfahrung", "esperienza"),
            glossa("zufrieden", "soddisfatto")
        ),
        frasi = listOf(
            frase("Am Montag begann für Frau Bachmann der neue Job.", "Lunedì per la signora Bachmann è iniziato il nuovo lavoro."),
            frase("Sie kam eine halbe Stunde früher, weil sie ruhig ankommen wollte.", "Arrivò mezz'ora prima perché voleva arrivare con calma."),
            frase("Ihre Kollegin zeigte ihr den Schreibtisch und die Küche.", "La collega le mostrò la scrivania e la cucina."),
            frase("Um neun Uhr gab es eine kurze Besprechung mit dem Team.", "Alle nove ci fu una breve riunione con il team."),
            frase("Frau Bachmann stellte viele Fragen, obwohl sie nervös war.", "La signora Bachmann fece molte domande, sebbene fosse nervosa."),
            frase("Der Chef sagte, dass er ihre Erfahrung schätzt.", "Il capo disse che apprezza la sua esperienza."),
            frase("Am Abend war sie müde, aber zufrieden.", "La sera era stanca, ma soddisfatta.")
        ),
        domande = listOf(
            domanda(
                "Perché è arrivata prima?",
                listOf("Weil sie ruhig ankommen wollte", "Weil der Zug Verspätung hatte", "Weil sie müde war"),
                0,
                "La frase con „weil“ dice il motivo: voleva arrivare con calma."
            ),
            domanda(
                "Cosa ha detto il capo?",
                listOf("Dass er ihre Erfahrung schätzt", "Dass sie zu spät ist", "Dass sie gehen soll"),
                0,
                "„dass er ihre Erfahrung schätzt“: apprezza la sua esperienza."
            ),
            domanda(
                "Come si sentiva la sera?",
                listOf("Müde, aber zufrieden", "Wütend", "Krank"),
                0,
                "L'ultima frase: stanca, ma soddisfatta."
            )
        )
    )

    // ---------------------------- B2 ----------------------------

    private fun storiaB2Ep1() = StoriaImmersione(
        livello = "B2",
        serie = "Das Amt",
        episodio = 1,
        titolo = "Der Brief aus dem Rathaus",
        regoleCollegate = listOf("b2-passiv-modal", "b2-konjunktiv2-vergangenheit"),
        contesto = "Passivo con modali e Konjunktiv: le forme della burocrazia reale, riconosciute nel testo prima che spiegate.",
        copertura = 93,
        minuti = 4,
        paroleChiave = listOf(
            glossa("Bescheinigung", "attestato"),
            glossa("Fristverlängerung", "proroga"),
            glossa("Behörde", "ufficio pubblico")
        ),
        frasi = listOf(
            frase("Die Bescheinigung muss bis Ende des Monats vorgelegt werden.", "L'attestato va presentato entro la fine del mese."),
            frase("Ohne die Unterschrift des Chefs wäre der Antrag abgelehnt worden.", "Senza la firma del capo la richiesta sarebbe stata respinta."),
            frase("Ein Formular, das seit Jahren nicht geändert worden war, musste neu ausgefüllt werden.", "Un modulo non cambiato da anni doveva essere ricompilato."),
            frase("Die Frau, deren Anwalt sie kannte, bat um eine kurze Fristverlängerung.", "La donna, di cui conosceva l'avvocato, chiese una breve proroga."),
            frase("Es ist fraglich, ob die Verlängerung gerechtfertigt ist.", "È discutibile che la proroga sia giustificata."),
            frase("Die zuständige Behörde prüft die Unterlagen sorgfältig.", "L'ufficio competente esamina i documenti attentamente."),
            frase("Falls etwas fehlt, wird der Fall erneut geöffnet.", "Se manca qualcosa, il caso viene riaperto."),
            frase("Voraussichtlich wird der Fall am Montag entschieden werden.", "Probabilmente il caso verrà deciso lunedì."),
            frase("Dann kann die Frau ruhig schlafen.", "Allora la signora potrà dormire tranquilla.")
        ),
        domande = listOf(
            domanda(
                "Chi deve presentare l'attestato?",
                listOf("Die Frau", "Die Stadt", "Der Anwalt"),
                0,
                "Il testo parla della donna e della sua pratica: è lei a dover presentare l'attestato."
            ),
            domanda(
                "Cosa chiese la donna?",
                listOf("Eine Fristverlängerung", "Einen Job", "Ein Formular"),
                0,
                "„bat um eine kurze Fristverlängerung“: una breve proroga."
            ),
            domanda(
                "Quando si deciderà il caso?",
                listOf("Am Montag", "Am Freitag", "Nie"),
                0,
                "„Voraussichtlich ... am Montag entschieden“."
            )
        )
    )

    private fun storiaB2Ep2() = StoriaImmersione(
        livello = "B2",
        serie = "Das Amt",
        episodio = 2,
        titolo = "Das Gespräch mit der Sachbearbeiterin",
        regoleCollegate = listOf("b2-passiv-modal", "b1-nominalisierung"),
        contesto = "La stessa pratica, un passo avanti: il colloquio allo sportello. Passivo, Konjunktiv e connettivi in una scena che si ripete nella vita vera.",
        copertura = 93,
        minuti = 4,
        paroleChiave = listOf(
            glossa("Unterlagen", "documenti"),
            glossa("genehmigen", "approvare"),
            glossa("Sachbearbeiterin", "impiegata addetta")
        ),
        frasi = listOf(
            frase("Frau Bachmann hatte einen Termin bei der Sachbearbeiterin.", "La signora Bachmann aveva un appuntamento con l'impiegata."),
            frase("Sie brachte alle Unterlagen mit, die verlangt worden waren.", "Portò tutti i documenti che erano stati richiesti."),
            frase("Die Sachbearbeiterin prüfte die Papiere und schüttelte den Kopf.", "L'impiegata controllò le carte e scosse la testa."),
            frase("Eine Bescheinigung fehlte, obwohl sie sie beantragt hatte.", "Mancava un attestato, sebbene l'avesse richiesto."),
            frase("„Ohne dieses Dokument kann der Antrag nicht bearbeitet werden“, sagte sie.", "«Senza questo documento la richiesta non può essere elaborata», disse."),
            frase("Frau Bachmann bat um eine Fristverlängerung von zwei Wochen.", "La signora Bachmann chiese una proroga di due settimane."),
            frase("Die Verlängerung wurde genehmigt, allerdings nur schriftlich.", "La proroga fu approvata, ma solo per iscritto."),
            frase("Am Ende verließ sie das Amt mit gemischten Gefühlen.", "Alla fine lasciò l'ufficio con sentimenti contrastanti.")
        ),
        domande = listOf(
            domanda(
                "Cosa mancava?",
                listOf("Eine Bescheinigung", "Ein Formular", "Ein Termin"),
                0,
                "„Eine Bescheinigung fehlte“: mancava un attestato."
            ),
            domanda(
                "Quanto tempo ha chiesto di proroga?",
                listOf("Zwei Wochen", "Zwei Tage", "Zwei Monate"),
                0,
                "„eine Fristverlängerung von zwei Wochen“."
            ),
            domanda(
                "Come è stata approvata la proroga?",
                listOf("Nur schriftlich", "Nur mündlich", "Gar nicht"),
                0,
                "„genehmigt, allerdings nur schriftlich“: solo per iscritto."
            )
        )
    )

    // --------------------------------------------------------------
    // Blocco 2 — Raccolta: parole dentro frasi vere
    // --------------------------------------------------------------

    /** Sei frasi per livello: la riserva da cui escono le cinque del giro. */
    fun frasiMiniera(livello: String?): List<FraseMiniera> = when (perLivello(livello)) {
        "A0" -> listOf(
            FraseMiniera("Der Kaffee ist heute besonders stark.", "Il caffè oggi è particolarmente forte.", "Kaffee", "caffè"),
            FraseMiniera("Das Fenster steht offen.", "La finestra è aperta.", "Fenster", "finestra"),
            FraseMiniera("Meine Tasche ist sehr alt.", "La mia borsa è molto vecchia.", "Tasche", "borsa"),
            FraseMiniera("Die Wohnung ist klein, aber warm.", "L'appartamento è piccolo ma caldo.", "Wohnung", "appartamento"),
            FraseMiniera("Ich dusche nur kurz.", "Mi faccio la doccia solo in fretta.", "dusche", "farsi la doccia"),
            FraseMiniera("Die Tür ist schon zu.", "La porta è già chiusa.", "Tür", "porta")
        )
        "A1" -> listOf(
            FraseMiniera("Ich kaufe jeden Samstag auf dem Markt ein.", "Ogni sabato faccio la spesa al mercato.", "Markt", "mercato"),
            FraseMiniera("Der Verkäufer gibt mir zwei Äpfel.", "Il venditore mi dà due mele.", "Verkäufer", "venditore"),
            FraseMiniera("Wir fahren mit dem Rad zum See.", "Andiamo in bici al lago.", "Rad", "bici"),
            FraseMiniera("Darum nehme ich heute Birnen.", "Per questo oggi prendo le pere.", "Darum", "per questo"),
            FraseMiniera("Der Radweg ist neu und breit.", "La pista ciclabile è nuova e larga.", "Radweg", "pista ciclabile"),
            FraseMiniera("Die Fahrkarte kostet drei Euro.", "Il biglietto costa tre euro.", "Fahrkarte", "biglietto")
        )
        "A2" -> listOf(
            FraseMiniera("Der Ausschlag juckt seit gestern.", "L'eruzione prude da ieri.", "Ausschlag", "eruzione cutanea"),
            FraseMiniera("Ich möchte einen Termin vereinbaren.", "Vorrei fissare un appuntamento.", "vereinbaren", "fissare"),
            FraseMiniera("Im Sommer schwitze ich schnell.", "D'estate sudo facilmente.", "schwitze", "sudare"),
            FraseMiniera("Die Salbe hilft wirklich schnell.", "La pomata aiuta davvero in fretta.", "Salbe", "pomata"),
            FraseMiniera("Der Juckreiz ist endlich weg.", "Il prurito è finalmente sparito.", "Juckreiz", "prurito"),
            FraseMiniera("Das Rezept liegt auf dem Tisch.", "La ricetta è sul tavolo.", "Rezept", "ricetta medica")
        )
        "B1" -> listOf(
            FraseMiniera("Sie war den ganzen Tag unruhig.", "Era agitata tutto il giorno.", "unruhig", "agitato"),
            FraseMiniera("Der Vermieter rief am Abend an.", "Il padrone di casa chiamò la sera.", "Vermieter", "padrone di casa"),
            FraseMiniera("Die Wohnungssuche war wirklich schwierig.", "La ricerca dell'appartamento è stata davvero difficile.", "Wohnungssuche", "ricerca dell'appartamento"),
            FraseMiniera("Das Angebot war wirklich fair.", "L'offerta era davvero equa.", "Angebot", "offerta"),
            FraseMiniera("Der Hof ist ruhig und grün.", "Il cortile è tranquillo e verde.", "Hof", "cortile"),
            FraseMiniera("Der Mietvertrag ist unterschrieben.", "Il contratto d'affitto è firmato.", "Mietvertrag", "contratto d'affitto")
        )
        else -> listOf(
            FraseMiniera("Die Bescheinigung kommt per Post.", "L'attestato arriva per posta.", "Bescheinigung", "attestato"),
            FraseMiniera("Welches Amt ist dafür zuständig?", "Quale ufficio è competente?", "zuständig", "competente"),
            FraseMiniera("Der Preis ist meiner Meinung nach gerechtfertigt.", "Secondo me il prezzo è giustificato.", "gerechtfertigt", "giustificato"),
            FraseMiniera("Die Unterlagen fehlen noch.", "I documenti mancano ancora.", "Unterlagen", "documenti"),
            FraseMiniera("Die Fristverlängerung wurde genehmigt.", "La proroga è stata approvata.", "Fristverlängerung", "proroga"),
            FraseMiniera("Wann wird der Fall entschieden?", "Quando sarà deciso il caso?", "entschieden", "decidere")
        )
    }

    /** Parola ad alta frequenza dentro la sua frase (derivata dalla raccolta). */
    fun paroleNelContesto(livello: String?): List<ParolaInContesto> =
        frasiMiniera(livello).map {
            ParolaInContesto(
                parola = it.parola,
                significato = it.significato,
                frase = it.tedesco,
                traduzione = it.italiano
            )
        }

    fun composti(livello: String?): List<Composto> = when (perLivello(livello)) {
        "A0" -> listOf(
            Composto("Fensterbank", listOf("Fenster", "bank"), listOf("finestra", "panca"), "davanzale"),
            Composto("Wasserfall", listOf("Wasser", "fall"), listOf("acqua", "caduta"), "cascata"),
            Composto("Krankenhaus", listOf("Kranken", "haus"), listOf("malati", "casa"), "ospedale")
        )
        "A1" -> listOf(
            Composto("Radweg", listOf("Rad", "weg"), listOf("bici", "via"), "pista ciclabile"),
            Composto("Markthalle", listOf("Markt", "halle"), listOf("mercato", "sala"), "mercato coperto"),
            Composto("Schwarzbrot", listOf("Schwarz", "brot"), listOf("nero", "pane"), "pane nero")
        )
        "A2" -> listOf(
            Composto("Hautarzt", listOf("Haut", "arzt"), listOf("pelle", "medico"), "dermatologo"),
            Composto("Arztbesuch", listOf("Arzt", "besuch"), listOf("medico", "visita"), "visita medica"),
            Composto("Schmerzmittel", listOf("Schmerz", "mittel"), listOf("dolore", "mezzo"), "antidolorifico")
        )
        "B1" -> listOf(
            Composto("Tagesplan", listOf("Tages", "plan"), listOf("della giornata", "piano"), "programma quotidiano"),
            Composto("Mietvertrag", listOf("Miet", "vertrag"), listOf("affitto", "contratto"), "contratto d'affitto"),
            Composto("Wohnungssuche", listOf("Wohnungs", "suche"), listOf("appartamento", "ricerca"), "ricerca dell'appartamento")
        )
        else -> listOf(
            Composto("Fristverlängerung", listOf("Frist", "verlängerung"), listOf("termine", "proroga"), "proroga del termine"),
            Composto("Krankenhausaufenthalt", listOf("Kranken", "haus", "aufenthalt"), listOf("malati", "casa", "soggiorno"), "ricovero in ospedale"),
            Composto("Antragsformular", listOf("Antrags", "formular"), listOf("richiesta", "modulo"), "modulo di richiesta")
        )
    }

    fun shadowing(livello: String?): List<FraseShadowing> = when (perLivello(livello)) {
        "A0" -> listOf(
            FraseShadowing("Ich stehe auf.", "Mi alzo.", "Verbo separabile: auf va in fondo."),
            FraseShadowing("Der Kaffee ist stark.", "Il caffè è forte.", "Articolo DER corto e duro."),
            FraseShadowing("Ich gehe nach Hause.", "Vado a casa.", "Ritmo regolare: due note di tempo."),
            FraseShadowing("Das ist mein Rad.", "Questa è la mia bici.", "Frase corta, ritmo lento.")
        )
        "A1" -> listOf(
            FraseShadowing("Ich kaufe am Morgen Brot.", "Compro pane al mattino.", "La particella di tempo è in fondo."),
            FraseShadowing("Wir haben keine Äpfel mehr.", "Non abbiamo più mele.", "MEHR cade leggermente, come respiro."),
            FraseShadowing("Kannst du mir helfen?", "Puoi darmi una mano?", "KANNST in prima posizione, tono alto."),
            FraseShadowing("Ich hätte gern ein Kilo Äpfel.", "Vorrei un chilo di mele.", "Frase da mercato, ormai familiare.")
        )
        "A2" -> listOf(
            FraseShadowing("Ich habe den Termin vereinbart.", "Ho fissato l'appuntamento.", "Ausiliario in seconda, Partizip in fondo."),
            FraseShadowing("Am Ende der Woche bin ich müde.", "A fine settimana sono stanco.", "Piccola pausa prima di 'bin'."),
            FraseShadowing("Können wir das morgen besprechen?", "Possiamo parlarne domani?", "Domanda che sale alla fine."),
            FraseShadowing("Das war toll!", "È stato bello!", "Risposta breve, tono pieno.")
        )
        "B1" -> listOf(
            FraseShadowing("Als ich zurückkam, war alles anders.", "Quando sono tornato, tutto era cambiato.", "als + passato, verbo in fondo."),
            FraseShadowing("Obwohl es regnete, ging ich ohne Mantel aus.", "Sebbene piovesse, uscii senza cappotto.", "Obwohl: verbo impilato all'ultimo posto."),
            FraseShadowing("Es wäre gut, wenn wir früher gingen.", "Sarebbe bene se andassimo prima.", "Konjunktiv II, leggero come un respiro."),
            FraseShadowing("Mir scheint, das ist ein gutes Angebot.", "Mi sembra una buona offerta.", "Tono che sale su ANGEBOT.")
        )
        else -> listOf(
            FraseShadowing("Das müsste schnell erledigt werden.", "Andrebbe fatto in fretta.", "Passivo + modale: due infiniti alla fine."),
            FraseShadowing("Es ist fraglich, ob das gerechtfertigt ist.", "È discutibile che sia giustificato.", "Accento forte su gerechtfertigt."),
            FraseShadowing("Der Bericht, den wir erhalten haben, ist unvollständig.", "La relazione ricevuta è incompleta.", "La relativa: verbo in fondo."),
            FraseShadowing("Voraussichtlich wird er am Montag entschieden.", "Probabilmente verrà deciso lunedì.", "VOR-aus-sichtlich: accento all'inizio.")
        )
    }

    // --------------------------------------------------------------
    // Blocco 3 — Ascolto passivo: frasi in loop con TTS
    // --------------------------------------------------------------

    /**
     * Playlist dell'ascolto passivo: prima la storia (ri-ascolto dello stesso
     * testo), poi le frasi della raccolta, poi lo shadowing. Ripetere lo
     * stesso materiale è voluto: la rilettura/ri-ascolto consolidano.
     */
    fun frasiPassive(livello: String?): List<String> =
        (storiaAttiva(livello).frasi.map { it.tedesco } +
            frasiMiniera(livello).map { it.tedesco } +
            shadowing(livello).map { it.tedesco })
            .distinct()
}
