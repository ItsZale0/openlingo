package com.alessandro.tedesco.data

/**
 * Curriculum strutturato per livello CEFR.
 * Ogni lezione ha un argomento specifico e progressivo.
 * Il generatore AI usa questo framework per insegnare contenuto NUOVO,
 * non solo ripasso.
 */
object Curriculum {

    private val a1 = listOf(
        "Saluti e presentazioni (ich heiße, ich bin, wie alt bist du)",
        "Numeri 0-100, età e date",
        "La famiglia (Vater, Mutter, Bruder, Schwester)",
        "Pronomi personali e verbo sein/haben",
        "Articoli der/die/das e plurali",
        "Città e paese (wohnst du, Stadt, Dorf)",
        "Cibo e bevande (essen, trinken, Hunger/Durst)",
        "La giornata e le ore (Uhrzeit, morgens, abends)",
        "Tempo libero e hobby (spielen, lesen, Sport)",
        "Casa e stanze (Wohnung, Zimmer, Küche)",
        "Vestiti e colori (Kleidung, Farben)",
        "Salute e corpo (Kopf, Bauch, krank, Arzt)",
        "Viaggi e mezzi (Zug, Bus, Flugzeug, fahren)",
        "Lavoro e professioni (Arbeit, Beruf, Lehrer)",
        "Tempo atmosferico (Wetter, Sonne, Regen)",
        "Animali (Hund, Katze, Vogel)",
        "Soldi, negozi e prezzi (Geld, kaufen, kosten)",
        "Al telefono (anrufen, Hallo, auf Wiederhören)",
        "Al ristorante (bestellen, Rechnung, lecker)",
        "In farmacia (Medikament, Schmerzen, Rezept)"
    )

    private val a2 = listOf(
        "Perfetto (haben/sein + Partizip II)",
        "Accusativo (wen? was? den/die/das)",
        "Dativo (wem? dem/der/dem)",
        "Verbi modali (können, müssen, wollen, dürfen)",
        "Frasi subordinate con weil e dass",
        "Comparativi e superlativi",
        "Preposizioni con dativo (mit, von, zu, bei)",
        "Preposizioni con accusativo (durch, für, gegen, ohne)",
        "Imperativo (sei, seid, seien Sie)",
        "Pronomi relativi (der/die/das als Relativpronomen)",
        "Verbi separabili (aufstehen, anrufen, einkaufen)",
        "Präteritum di sein e haben (war, hatte)",
        "Lessico avanzato: lavoro e carriera",
        "Lessico: viaggi e turismo",
        "Lessico: salute e medicina",
        "Lessico: tempo libero e sport",
        "Lessico: cibo e ristorante",
        "Lessico: casa e arredamento",
        "Lessico: tecnologia e media",
        "Lessico: ambiente e natura"
    )

    private val b1 = listOf(
        "Adjektivdeklination (declinazione dell'aggettivo: groß/große/großen)",
        "Genitiv (des/der, wegen, trotz, während)",
        "Relativsätze (der/die/das, den/dem/deren/dessen)",
        "Konnektoren (deshalb, trotzdem, obwohl, außer, während)",
        "Verben mit Präpositionen (warten auf, sich freuen über, sich interessieren für)",
        "Wechselpräpositionen (in/an/auf + Akkusativ o Dativ)",
        "Reflexive Verben (sich waschen, sich freuen, sich beeilen)",
        "Indirekte Fragen (Wissen Sie, wo...? Können Sie mir sagen, ob...?)",
        "Passiv mit Modalverben (muss gemacht werden, soll untersucht werden)",
        "Präteritum vs Perfekt (war/hatte/ging vs bin gegangen)",
        "Konjunktiv II (wäre, hätte, würde)",
        "Lessico: lavoro e professione sanitaria",
        "Lessico: salute, corpo e sintomi",
        "Lessico: relazioni e conflitti",
        "Lessico: istruzione e formazione",
        "Lessico: ambiente e sostenibilità",
        "Lessico: tecnologia e innovazione",
        "Lessico: cultura e tradizione",
        "Lessico: economia e business",
        "Lessico: emozioni e sentimenti"
    )

    /**
     * Restituisce l'argomento della lezione per il livello e il numero dato.
     *
     * Il curriculum ha 20 temi per livello. Dopo il ventesimo NON si riparte da
     * capo con lo stesso tema (ripetere "Saluti" alla lezione 21 annoia e non
     * insegna nulla): si passa a un approfondimento dello stesso tema, con
     * vocabolario e strutture nuove. Il generatore riceve comunque il numero
     * della lezione e il livello, quindi calibra il contenuto.
     */
    fun argomento(livello: String, numero: Int): String {
        val lista = when (livello.uppercase().substringBefore(" ")) {
            "A0", "A1" -> a1
            "A2" -> a2
            "B1" -> b1
            "B2", "C1", "C2" -> b1 // fallback
            else -> a1
        }
        val n = numero.coerceAtLeast(1)
        val giro = (n - 1) / lista.size
        val idx = (n - 1) % lista.size
        val base = lista[idx]
        return if (giro == 0) base
        else "$base — approfondimento ${giro + 1}: nuove parole e strutture sullo stesso tema"
    }

    /**
     * Restituisce il focus grammaticale per il livello.
     */
    fun focusGrammaticale(livello: String): String {
        return when (livello.uppercase().substringBefore(" ")) {
            "A0", "A1" -> "presente, frasi SVO, articoli der/die/das, vocabolario quotidiano. NIENTE passato, subordinate, congiuntivo."
            "A2" -> "presente + Perfetto, accusativo/dativo, modali, verbi separabili, frasi semplici legate."
            "B1" -> "Adjektivdeklination, Genitiv, Relativsätze, Konnektoren, Verben mit Präpositionen, Wechselpräpositionen, reflexive Verben, indirekte Fragen, Passiv (anche con modali), Präteritum vs Perfekt, Konjunktiv II."
            "B2" -> "congiuntivo, lessico astratto, strutture complesse."
            else -> "presente, frasi SVO, articoli der/die/das, vocabolario quotidiano."
        }
    }
}
