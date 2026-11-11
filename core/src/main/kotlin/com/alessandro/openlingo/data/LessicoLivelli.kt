package com.alessandro.openlingo.data

/**
 * Lessico unificato A1-B2, condiviso da Android e desktop.
 *
 * Unisce:
 * - i quattro elenchi curati per livello ([LessicoA1], [LessicoA2], [LessicoB1],
 *   [LessicoB2]);
 * - la [WortlisteB1] storica (lista B1 + lessico sanitario), così chi aveva già
 *   quelle parole non le perde.
 *
 * Deduplica per LEMMA (senza articolo, minuscolo): "die Wand" e "die Wand"
 * sono la stessa parola anche se arrivano da file diversi. Vince la PRIMA
 * occorrenza in ordine di livello (A1 → A2 → B1 → B2), perché una parola che
 * serve già ad A1 non deve apparire come "nuova" a B2.
 */
object LessicoLivelli {

    data class Voce(
        val tedesco: String,
        val italiano: String,
        val esempio: String,
        val tema: String,
        val livello: String
    )

    val LIVELLI = listOf("A1", "A2", "B1", "B2")

    /**
     * Obiettivi CUMULATIVI per livello, secondo le indicazioni Goethe.
     *
     * Le Wortliste ufficiali non sono "parole nuove per livello" ma il
     * vocabolario complessivo che serve all'esame: ~650 ad A1, ~1.300 ad A2,
     * ~2.400 a B1. Per B2 non esiste un elenco ufficiale Goethe; l'app punta a
     * 3.000 parole complessive, una soglia prudente per leggere testi B2.
     *
     * Quindi "nuove per livello" + "copertura cumulativa" sono due numeri
     * diversi: le schede mostrano le nuove, la copertura dice a che punto sei
     * rispetto al traguardo dell'esame.
     */
    val TARGET_CUMULATIVI = mapOf("A1" to 650, "A2" to 1_300, "B1" to 2_400, "B2" to 3_000)

    /** Traguardo cumulativo del livello (650 / 1.300 / 2.400 / 3.000). */
    fun targetCumulativo(livello: String): Int =
        TARGET_CUMULATIVI[livello.uppercase()] ?: TARGET_CUMULATIVI.getValue("B2")

    /**
     * Parole coperte fino a questo livello incluso: A2=670+702=1.372, ecc.
     * È il numero da confrontare con [targetCumulativo].
     */
    fun cumulativo(livello: String): Int {
        val fino = LIVELLI.indexOfFirst { it.equals(livello, ignoreCase = true) }
        if (fino < 0) return 0
        return LIVELLI.take(fino + 1).sumOf { conteggio(it) }
    }

    /** Tutte le voci, senza duplicati di lemma. */
    val tutte: List<Voce> by lazy {
        val viste = HashSet<String>()
        buildList {
            aggiungiSeNuove(this@buildList, viste, parse(LessicoA1.grezzo, "A1"))
            aggiungiSeNuove(this@buildList, viste, parse(LessicoA2.grezzo, "A2"))
            aggiungiSeNuove(this@buildList, viste, parse(LessicoB1.grezzo, "B1"))
            aggiungiSeNuove(this@buildList, viste, parse(LessicoB2.grezzo, "B2"))
            aggiungiSeNuove(this@buildList, viste, WortlisteB1.tutte.map { w ->
                Voce(w.tedesco, w.italiano, w.esempio, w.tema, w.livello)
            })
        }
    }

    private fun aggiungiSeNuove(
        destinazione: MutableList<Voce>,
        viste: MutableSet<String>,
        voci: List<Voce>
    ) {
        voci.forEach { v ->
            val chiave = chiaveParola(v.tedesco)
            if (chiave.isNotBlank() && viste.add(chiave)) destinazione.add(v)
        }
    }

    /** "die Wohnung" -> "wohnung"; "der Termin" -> "termin". */
    fun lemma(tedesco: String): String = tedesco.trim().lowercase()
        .removePrefix("der ").removePrefix("die ").removePrefix("das ")
        .trim()

    /**
     * Chiave di deduplica: lemma + categoria grammaticale.
     *
     * Serve perché "der Morgen" (mattina, sostantivo) e "morgen" (domani,
     * avverbio) hanno lo stesso lemma ma sono due parole diverse: senza il
     * prefisso una delle due sparirebbe dall'elenco.
     *
     * Il sostantivo si riconosce dall'articolo oppure dall'iniziale maiuscola
     * (i nomi tedeschi sono sempre maiuscoli), così funziona anche sulle parole
     * che arrivano dal feed senza il campo `article`.
     */
    fun chiaveParola(tedesco: String): String {
        val t = tedesco.trim()
        if (t.isBlank()) return ""
        val conArticolo = WortlisteB1.articoloDi(t) != null
        return chiaveParola(t, if (conArticolo) "x" else null)
    }

    /**
     * Come [chiaveParola], ma con l'articolo già separato.
     *
     * Le parole del profilo salvano l'articolo in un campo a parte
     * (`german = "Wohnung"`, `article = "die"`): senza questo overload una voce
     * minuscola arrivata dal feed verrebbe classificata come non-sostantivo e
     * reimportata come doppione.
     */
    fun chiaveParola(german: String, article: String?): String {
        val t = german.trim()
        if (t.isBlank()) return ""
        val sostantivo = !article.isNullOrBlank() ||
            WortlisteB1.articoloDi(t) != null ||
            t.substringAfterLast(' ').firstOrNull()?.isUpperCase() == true
        return (if (sostantivo) "n:" else "w:") + lemma(t)
    }

    fun perLivello(livello: String): List<Voce> =
        tutte.filter { it.livello.equals(livello, ignoreCase = true) }

    fun temi(livello: String? = null): List<String> =
        (if (livello == null) tutte else perLivello(livello))
            .map { it.tema }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    fun cerca(query: String): List<Voce> {
        val q = query.trim()
        if (q.isBlank()) return tutte
        return tutte.filter {
            it.tedesco.contains(q, ignoreCase = true) ||
                it.italiano.contains(q, ignoreCase = true)
        }
    }

    /** Quante voci per livello (per l'etichetta della sezione). */
    fun conteggio(livello: String): Int = perLivello(livello).size

    /** Articolo determinativo iniziale, se la voce è un sostantivo. */
    fun articoloDi(tedesco: String): String? = WortlisteB1.articoloDi(tedesco)

    fun parolaSenzaArticolo(tedesco: String): String = WortlisteB1.parolaSenzaArticolo(tedesco)

    /**
     * Analizza il testo grezzo. Formato: `tedesco|italiano|esempio|tema`.
     * Le righe malformate vengono scartate in silenzio: un elenco con una riga
     * rotta non deve impedire l'avvio della sezione lessico.
     */
    fun parse(grezzo: String, livello: String): List<Voce> = grezzo.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains('|') }
        .mapNotNull { riga ->
            val parti = riga.split('|').map { it.trim() }
            if (parti.size < 4) return@mapNotNull null
            val tedesco = parti[0]
            val italiano = parti[1]
            if (tedesco.isBlank() || italiano.isBlank()) return@mapNotNull null
            Voce(tedesco, italiano, parti[2], parti[3], livello)
        }
        .toList()
}
