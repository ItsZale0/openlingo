package com.alessandro.tedesco.data

import java.util.Locale

/**
 * Controllo automatico della correttezza degli esercizi di tedesco.
 *
 * Perché esiste: l'AI può sbagliare l'accordo tra articolo e nome (il caso
 * classico è "einen Buch", con Buch neutro). Un esercizio sbagliato è peggio
 * di un esercizio in meno, perché insegna una regola falsa: qui i controlli
 * ovvi vengono fatti prima di mostrare l'esercizio.
 *
 * Il controllo non pretende di sostituire un insegnante: verifica solo ciò che
 * si può verificare con certezza, cioè
 * - la struttura (buco presente, risposta dentro le opzioni, niente doppioni);
 * - l'accordo articolo/nome per i nomi di cui conosciamo il genere.
 *
 * Tutti i testi vengono normalizzati come in [DeduplicaEsercizi] (minuscole,
 * umlaut ae/oe/ue, ss), così "Bücher" e "buecher" sono la stessa parola.
 *
 * Il dizionario contiene solo forme SINGOLARI non ambigue: un nome sconosciuto
 * viene ignorato, quindi non si producono falsi allarmi. Meglio un controllo in
 * meno che un esercizio corretto segnalato come sbagliato.
 */
object ControlloGrammatica {

    private enum class Genere(val etichetta: String) {
        MASCHILE("maschile"),
        FEMMINILE("femminile"),
        NEUTRO("neutro"),
        PLURALE("plurale")
    }

    /** Nomi frequenti (singolare) con il loro genere. Chiavi già normalizzate. */
    private val nomi: Map<String, Genere> = buildMap {
        fun m(vararg parole: String) = parole.forEach { put(it, Genere.MASCHILE) }
        fun f(vararg parole: String) = parole.forEach { put(it, Genere.FEMMINILE) }
        fun n(vararg parole: String) = parole.forEach { put(it, Genere.NEUTRO) }
        fun p(vararg parole: String) = parole.forEach { put(it, Genere.PLURALE) }

        // ---- Maschili ----
        m(
            "mann", "hund", "tisch", "apfel", "kaffee", "tee", "wein", "saft",
            "kaese", "fisch", "vater", "bruder", "sohn", "onkel", "freund",
            "arzt", "student", "patient", "junge", "kollege", "name",
            "termin", "stuhl", "schrank", "garten", "baum", "berg",
            "fluss", "bahnhof", "zug", "bus", "film", "brief", "tag",
            "morgen", "abend", "monat", "sommer", "winter", "herbst", "fruehling",
            "beruf", "job", "schuh", "rock", "mantel", "hut",
            "kopf", "arm", "fuss", "magen", "hals", "zahn",
            "muskel", "befund", "antrag", "therapieplan", "rollstuhl",
            "satz", "text", "anfang", "grund", "platz", "punkt",
            "preis", "markt", "laden", "salat", "reis", "zucker",
            "honig", "schmerz", "husten", "schnupfen", "plan", "bericht",
            "vertrag", "kunde", "chef", "nachbar", "gast", "bildschirm",
            "ausweis", "pass", "rucksack", "regenschirm", "ring", "schal",
            "handschuh", "anzug", "teppich", "vorhang", "herd", "ofen",
            "kuehlschrank", "abfall", "korb", "hammer", "nagel", "stift",
            "bleistift", "betrag", "himmel", "regen", "schnee", "wind",
            "urlaub", "mensch", "herr", "blutdruck", "puls", "schlaf",
            "stress", "krankengymnast", "physiotherapeut", "therapeut",
            "hausarzt", "facharzt", "hautarzt", "zahnarzt", "darm"
        )
        // ---- Femminili ----
        f(
            "frau", "mutter", "schwester", "tochter", "tante", "freundin",
            "lehrerin", "aerztin", "studentin", "patientin", "wohnung", "schule",
            "stadt", "strasse", "kueche", "tuer", "wand", "blume", "katze",
            "maus", "milch", "suppe", "pizza", "nudel", "kartoffel", "tomate",
            "zwiebel", "banane", "orange", "zeitung", "zeitschrift", "karte",
            "fahrkarte", "rechnung", "frage", "antwort", "arbeit", "firma",
            "sprache", "uebung", "aufgabe", "pruefung", "note", "woche", "nacht",
            "stunde", "minute", "sekunde", "uhr", "sonne", "welt", "familie",
            "gesundheit", "krankheit", "schulter", "hand", "nase", "backe",
            "zunge", "haut", "wunde", "tablette", "medizin", "therapie",
            "behandlung", "untersuchung", "anamnese", "diagnose", "reha",
            "klinik", "praxis", "apotheke", "versicherung", "kollegin", "chefin",
            "sekretaerin", "sachbearbeiterin", "tasse", "gabel", "flasche",
            "dose", "packung", "tube", "creme", "salbe", "spritze", "birne",
            "erdbeere", "traube", "zitrone", "melone", "gurke", "karotte",
            "bohne", "erbse", "linse", "nuss", "mandel", "butter", "sahne",
            "schokolade", "torte", "semmel", "brezel", "wurst", "kette", "hose",
            "jacke", "bluse", "muetze", "schuessel", "pfanne", "kanne", "burg",
            "bruecke", "ampel", "kreuzung", "haltestelle", "station", "post",
            "bank", "bibliothek", "kirche", "tankstelle", "werkstatt", "fabrik",
            "abteilung", "besprechung", "sitzung", "pause", "mittagspause",
            "freizeit", "karriere", "ausbildung", "erfahrung", "kenntnis",
            "faehigkeit", "moeglichkeit", "entscheidung", "loesung", "meinung",
            "idee", "information", "nachricht", "geschichte", "erzaehlung",
            "beschreibung", "erklaerung", "wiederholung", "vorbereitung",
            "teilnahme", "anmeldung", "unterschrift", "bestaetigung",
            "einladung", "leistung", "zeit", "dauer", "natur", "umwelt", "luft",
            "erde", "wolke", "temperatur", "waerme", "kaelte", "feuchtigkeit",
            "bewegung", "verletzung", "mobilitaet", "haltung", "wirbelsaeule"
        )
        // ---- Neutri ----
        n(
            "buch", "haus", "kind", "auto", "jahr", "wort",
            "problem", "telefon", "brot", "wasser", "bier", "ei", "heft",
            "fruehstueck", "kleid", "hemd", "bild", "spiel", "land",
            "licht", "foto", "radio", "bett", "dach", "glas", "tier", "pferd",
            "schaf", "schwein", "huhn", "insekt", "museum", "kino",
            "restaurant", "hotel", "buero", "geschaeft", "papier", "geld",
            "fleisch", "obst", "gemuese", "salz", "eis", "fahrrad", "motorrad",
            "flugzeug", "schiff", "boot", "werkzeug", "geraet", "handy",
            "tablet", "internet", "projekt", "thema",
            "system", "beispiel", "recht", "ziel", "interesse", "ergebnis",
            "verhalten", "gespraech", "protokoll", "rezept",
            "medikament", "symptom", "praeparat",
            "gelenk", "bein", "ohr", "auge", "herz", "gehirn", "blut", "fieber"
        )
        // ---- Solo plurali (non ambigui) ----
        p("leute", "eltern", "geschwister", "ferien", "moebel", "lebensmittel", "kosten")
    }

    /** Forme che possono accompagnare un nome, per genere (qualsiasi caso). */
    private val compatibili: Map<Genere, Set<String>> = mapOf(
        Genere.MASCHILE to setOf(
            "der", "den", "dem", "des", "ein", "einen", "einem", "eines",
            "kein", "keinen", "keinem", "keines",
            "mein", "meinen", "meinem", "meines", "dein", "deinen", "deinem",
            "deines", "sein", "seinen", "seinem", "seines", "ihr", "ihren",
            "ihrem", "ihres", "unser", "unseren", "unserem", "unseres",
            "euer", "euren", "eurem", "eures"
        ),
        Genere.FEMMINILE to setOf(
            "die", "der", "eine", "einer", "keine", "keiner",
            "meine", "meiner", "deine", "deiner", "seine", "seiner",
            "ihre", "ihrer", "unsere", "unserer", "eure", "eurer"
        ),
        Genere.NEUTRO to setOf(
            "das", "dem", "des", "ein", "einem", "eines",
            "kein", "keinem", "keines", "mein", "meinem", "meines",
            "dein", "deinem", "deines", "sein", "seinem", "seines",
            "ihr", "ihrem", "ihres", "unser", "unserem", "unseres",
            "euer", "eurem", "eures"
        ),
        Genere.PLURALE to setOf(
            "die", "den", "der", "keine", "keinen", "keiner",
            "meine", "meinen", "meiner", "deine", "deinen", "deiner",
            "seine", "seinen", "seiner", "ihre", "ihren", "ihrer",
            "unsere", "unseren", "unserer", "eure", "euren", "eurer"
        )
    )

    private val articoli: Set<String> = compatibili.values.flatten().toSet()

    /** Parole che chiudono il sintagma: non si cerca il nome oltre. */
    private val stopWord: Set<String> = setOf(
        "im", "in", "am", "an", "auf", "aus", "bei", "beim", "mit", "nach",
        "seit", "von", "vom", "zu", "zum", "zur", "fuer", "ueber", "unter",
        "vor", "hinter", "neben", "zwischen", "durch", "gegen", "ohne", "um",
        "bis", "entlang", "trotz", "wegen", "waehrend",
        "ich", "du", "er", "sie", "es", "wir", "ihr", "man", "mich", "dich",
        "ihn", "uns", "euch", "mir", "dir", "ihm", "ihnen",
        "und", "oder", "aber", "denn", "sondern", "dass", "weil", "wenn",
        "als", "wie", "ob", "obwohl", "damit",
        "ist", "sind", "war", "waren", "bin", "bist", "hat", "habe", "haben",
        "hatte", "hatten", "wird", "werden", "wurde", "wurden", "kann",
        "koennen", "muss", "muessen", "will", "wollen", "soll", "sollen",
        "darf", "duerfen", "mag", "moegen", "moechte", "moechten",
        "nicht", "auch", "schon", "noch", "nur", "sehr", "gerne", "gern",
        "oft", "immer", "nie", "heute", "gestern", "hier", "dort", "jetzt",
        "dann", "da", "so"
    )

    private fun chiave(parola: String?): String =
        DeduplicaEsercizi.normalizza(parola)

    /**
     * Problemi grammaticali riconoscibili in una frase tedesca.
     * Ritorna una lista vuota quando non c'è nulla da segnalare.
     */
    fun problemiFrase(frase: String?): List<String> {
        if (frase.isNullOrBlank()) return emptyList()
        val problemi = mutableListOf<String>()
        val parole = frase.trim().split(Regex("\\s+"))
        parole.forEachIndexed { indice, parola ->
            val articolo = chiave(parola)
            if (articolo !in articoli) return@forEachIndexed
            var j = indice + 1
            var salti = 0
            var nome: String? = null
            var genere: Genere? = null
            while (j < parole.size && salti <= 1) {
                val candidato = chiave(parole[j])
                val g = nomi[candidato]
                if (g != null) {
                    nome = parole[j]
                    genere = g
                    break
                }
                // Un altro articolo, una preposizione o un pronome chiudono il
                // sintagma: non si va oltre, altrimenti si accoppiano parole
                // che non c'entrano ("die im Garten").
                if (candidato in articoli || candidato in stopWord) break
                if (!sembraAggettivo(parole[j])) break
                j++
                salti++
            }
            val g = genere ?: return@forEachIndexed
            if (articolo !in compatibili.getValue(g)) {
                problemi += "«${parole[indice]} $nome»: " +
                    "«${parole[indice]}» non va con un nome ${g.etichetta}."
            }
        }
        return problemi
    }

    /** Un aggettivo tedesco sta tra articolo e nome ed è minuscolo. */
    private fun sembraAggettivo(parola: String): Boolean {
        if (parola.isEmpty()) return false
        if (parola.first().isUpperCase()) return false
        return parola.lowercase(Locale.ROOT).matches(
            Regex("[a-zäöüß]+(e|en|er|es|em)")
        )
    }

    /**
     * Controllo completo di un esercizio a scelta multipla.
     * [rispostaCorretta] è l'indice dell'opzione giusta.
     */
    fun problemi(
        domanda: String,
        opzioni: List<String>,
        rispostaCorretta: Int
    ): List<String> {
        val problemi = mutableListOf<String>()
        if (domanda.isBlank()) problemi += "domanda vuota"
        if (opzioni.size < 2) problemi += "meno di due opzioni"
        if (rispostaCorretta !in opzioni.indices) {
            problemi += "risposta corretta fuori dalle opzioni"
            return problemi
        }
        // Il confronto è CASE-SENSITIVE: "lesen" (verbo) e "Lesen" (sostantivo
        // nominalizzato) sono due opzioni legittime e diverse, non un doppione.
        if (opzioni.map { it.trim() }.distinct().size != opzioni.size) {
            problemi += "opzioni duplicate"
        }
        val corretta = opzioni[rispostaCorretta]
        if (corretta.isBlank()) problemi += "risposta corretta vuota"
        val frase = if (domanda.contains("___")) {
            domanda.replace("___", corretta)
        } else {
            corretta
        }
        problemi += problemiFrase(frase)
        return problemi
    }

    /** Controllo di un esercizio delle unità, qualunque sia il tipo. */
    fun problemi(esercizio: EsercizioUnita): List<String> = when (esercizio.tipo) {
        TipoEsercizioGrammatica.SCELTA_MULTIPLA ->
            problemi(esercizio.domanda, esercizio.opzioni, esercizio.rispostaCorretta)

        else -> {
            val risposta = esercizio.rispostaTesto.ifBlank {
                esercizio.alternative.firstOrNull().orEmpty()
            }
            if (risposta.isBlank()) {
                listOf("risposta mancante")
            } else {
                val frase = if (esercizio.domanda.contains("___")) {
                    esercizio.domanda.replace("___", risposta)
                } else {
                    risposta
                }
                problemiFrase(frase)
            }
        }
    }
}
