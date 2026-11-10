package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.WordEntity
import kotlin.math.min

/**
 * Esercizi di lessico generati OFFLINE dalle parole del profilo.
 *
 * Perché non un solo tipo: il riconoscimento passivo (vedere "die Wohnung" e
 * scegliere "appartamento") è molto più facile del richiamo attivo (leggere
 * "appartamento" e scrivere "die Wohnung"). Chi si allena solo sul primo tipo
 * "riconosce" le parole ma non le sa usare. Qui i tipi si alternano:
 *
 * 1. SIGNIFICATO  – DE → IT, scelta multipla
 * 2. TEDESCO      – IT → DE, scelta multipla
 * 3. ARTICOLO     – der/die/das (solo sostantivi)
 * 4. CLOZE        – completa la frase d'esempio
 * 5. SCRIVI       – richiamo attivo: scrivi la parola tedesca
 * 6. ASCOLTO      – l'app pronuncia la parola, tu la scrivi
 *
 * Ogni esercizio restituisce la parola coinvolta: la UI registra l'esito nello
 * stesso ripasso a intervalli del lessico, quindi anche gli esercizi contano
 * come ripasso e non serve una seconda coda separata.
 */
enum class TipoEsercizioLessico(val etichetta: String) {
    SIGNIFICATO("Scegli il significato"),
    TEDESCO("Scegli la parola tedesca"),
    ARTICOLO("Scegli l'articolo"),
    CLOZE("Completa la frase"),
    SCRIVI("Scrivi in tedesco"),
    ASCOLTO("Ascolta e scrivi")
}

data class EsercizioLessico(
    val parola: WordEntity,
    val tipo: TipoEsercizioLessico,
    /** Testo della domanda, già pronto da mostrare. */
    val domanda: String,
    /** Opzioni per la scelta multipla (vuoto = risposta da scrivere). */
    val opzioni: List<String> = emptyList(),
    /** Risposta attesa (testo esatto, senza articolo per le risposte scritte). */
    val corretta: String,
    /** Indice della risposta corretta in [opzioni], -1 se scritta. */
    val indiceCorretto: Int = -1,
    /** Traduzione/parola da mostrare dopo la risposta. */
    val spiegazione: String = ""
) {
    val aScelta: Boolean get() = opzioni.isNotEmpty()
}

object EserciziLessico {

    private val ARTICOLI = listOf(
        "der", "die", "das", "den", "dem", "des",
        "ein", "eine", "einen", "einem", "einer", "eines"
    )

    /** Parola tedesca completa di articolo, come va mostrata. */
    fun tedescoConArticolo(w: WordEntity): String = ParolaTedesca.conArticolo(w.article, w.german)

    /** Solo il sostantivo, senza articolo, per confronti e risposte scritte. */
    fun senzaArticolo(testo: String): String {
        var t = testo.trim()
        val prima = t.substringBefore(' ').lowercase()
        if (prima in ARTICOLI) t = t.substringAfter(' ', "").trim()
        return t
    }

    /** true se la parola è un sostantivo con articolo noto. */
    fun haArticolo(w: WordEntity): Boolean =
        w.article?.isNotBlank() == true || ParolaTedesca.iniziaConArticolo(w.german)

    /**
     * Normalizza per il confronto: minuscole, umlaut in forma ae/oe/ue/ss,
     * niente punteggiatura, spazi compattati.
     */
    fun normalizza(testo: String): String = testo.trim().lowercase()
        .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    /** Distanza di Levenshtein, usata per tollerare un errore di battitura. */
    fun distanza(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var precedente = IntArray(b.length + 1) { it }
        var corrente = IntArray(b.length + 1)
        a.forEachIndexed { i, ca ->
            corrente[0] = i + 1
            b.forEachIndexed { j, cb ->
                val costo = if (ca == cb) 0 else 1
                corrente[j + 1] = min(
                    min(corrente[j] + 1, precedente[j + 1] + 1),
                    precedente[j] + costo
                )
            }
            val tmp = precedente
            precedente = corrente
            corrente = tmp
        }
        return precedente[b.length]
    }

    /**
     * Verifica una risposta scritta. Tollerante su: maiuscole, umlaut scritti
     * ae/oe/ue, articolo davanti al sostantivo, un refuso su parole lunghe.
     */
    fun rispostaScrittaCorretta(esercizio: EsercizioLessico, risposta: String): Boolean {
        val attesa = normalizza(senzaArticolo(esercizio.corretta))
        val data = normalizza(senzaArticolo(risposta))
        if (attesa.isEmpty() || data.isEmpty()) return false
        if (attesa == data) return true
        // Un refuso è accettato solo su parole abbastanza lunghe: su "Brot"
        // un carattere sbagliato è metà parola.
        return attesa.length >= 8 && distanza(attesa, data) <= 1
    }

    /**
     * Due traduzioni indicano lo stesso significato?
     *
     * Serve perché in italiano molti verbi hanno sinonimi ("valutare" per
     * *beurteilen* e *bewerten*) e alcune voci sono la stessa parola con una
     * qualifica ("insegnante" / "insegnante (f)"): usarle come distrattori
     * renderebbe la domanda con DUE risposte giuste. Il confronto è per
     * prefisso, così intercetta anche le varianti con parentesi.
     */
    fun stessoSenso(a: String, b: String): Boolean {
        val na = normalizza(a)
        val nb = normalizza(b)
        if (na.isEmpty() || nb.isEmpty()) return false
        if (na == nb) return true
        if (na.length < 4 || nb.length < 4) return false
        return na.startsWith(nb) || nb.startsWith(na)
    }

    /** Distrattori validi: parole diverse, con un significato davvero diverso. */
    private fun distrattori(pool: List<WordEntity>, w: WordEntity): List<WordEntity> = pool
        .filter { it.id != w.id }
        .filter { it.italian.isNotBlank() && !stessoSenso(it.italian, w.italian) }
        .distinctBy { normalizza(it.italian) }

    /**
     * Costruisce una sessione di esercizi.
     *
     * @param parole parole da esercitare (di solito quelle del livello scelto)
     * @param quante quanti esercizi generare
     * @param pool tutte le parole disponibili, per i distrattori
     * @param soloTipo se impostato, genera solo esercizi di questo tipo (es. ARTICOLO per il gender trainer)
     */
    fun costruisci(
        parole: List<WordEntity>,
        quante: Int,
        pool: List<WordEntity> = parole,
        casuale: kotlin.random.Random = kotlin.random.Random.Default,
        soloTipo: TipoEsercizioLessico? = null
    ): List<EsercizioLessico> {
        val base = parole
            .filter { ParolaTedesca.utilizzabile(it) }
            .distinctBy { it.id }
            .shuffled(casuale)
        if (base.isEmpty() || quante <= 0) return emptyList()

        val candidati = pool
            .filter { ParolaTedesca.utilizzabile(it) }
            .filter { it.italian.isNotBlank() }
            .distinctBy { it.id }

        val risultato = mutableListOf<EsercizioLessico>()
        var indice = 0
        // Ciclo di tipi mescolato: ogni giro cambia ordine, così non si può
        // prevedere "dopo il cloze arriva l'ascolto".
        while (risultato.size < quante) {
            val w = base[indice % base.size]
            val tipo = soloTipo ?: tipoPer(w, risultato.size, casuale)
            val esercizio = when (tipo) {
                TipoEsercizioLessico.SIGNIFICATO -> significato(w, candidati, casuale)
                TipoEsercizioLessico.TEDESCO -> tedesco(w, candidati, casuale)
                TipoEsercizioLessico.ARTICOLO -> articolo(w, casuale)
                TipoEsercizioLessico.CLOZE -> cloze(w, candidati, casuale)
                TipoEsercizioLessico.SCRIVI -> scrivi(w, ascolto = false)
                TipoEsercizioLessico.ASCOLTO -> scrivi(w, ascolto = true)
            }
            if (esercizio != null) risultato.add(esercizio)
            indice++
            // Se le parole sono poche e i tipi non applicabili, evitiamo il loop infinito.
            if (indice > quante * 12 + 60) break
        }
        return risultato
    }

    private fun tipoPer(
        w: WordEntity,
        posizione: Int,
        casuale: kotlin.random.Random
    ): TipoEsercizioLessico {
        val disponibili = buildList {
            add(TipoEsercizioLessico.SIGNIFICATO)
            add(TipoEsercizioLessico.TEDESCO)
            add(TipoEsercizioLessico.SCRIVI)
            add(TipoEsercizioLessico.ASCOLTO)
            if (haArticolo(w)) add(TipoEsercizioLessico.ARTICOLO)
            if (fraseConBuco(w) != null) add(TipoEsercizioLessico.CLOZE)
        }
        // Rotazione deterministica mescolata: non tutti gli esercizi dello
        // stesso tipo di fila, ma nemmeno casuale puro (che può ripetere).
        val scelta = disponibili[(posizione + w.id.hashCode().let { if (it < 0) -it else it }) % disponibili.size]
        return scelta
    }

    // ------------------------------------------------------------- tipi

    private fun significato(
        w: WordEntity,
        pool: List<WordEntity>,
        casuale: kotlin.random.Random
    ): EsercizioLessico? {
        val sbagliate = distrattori(pool, w)
            .map { it.italian.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .shuffled(casuale)
            .take(3)
        if (sbagliate.size < 3) return null
        val opzioni = (sbagliate + w.italian.trim()).shuffled(casuale)
        return EsercizioLessico(
            parola = w,
            tipo = TipoEsercizioLessico.SIGNIFICATO,
            domanda = "Che cosa significa «${tedescoConArticolo(w)}»?",
            opzioni = opzioni,
            corretta = w.italian.trim(),
            indiceCorretto = opzioni.indexOf(w.italian.trim()),
            spiegazione = testoTedesco(w)
        )
    }

    private fun tedesco(
        w: WordEntity,
        pool: List<WordEntity>,
        casuale: kotlin.random.Random
    ): EsercizioLessico? {
        val corretta = tedescoConArticolo(w)
        val sbagliate = distrattori(pool, w)
            .map { tedescoConArticolo(it) }
            .filter { it.isNotBlank() && !it.equals(corretta, ignoreCase = true) }
            .distinct()
            .shuffled(casuale)
            .take(3)
        if (sbagliate.size < 3) return null
        val opzioni = (sbagliate + corretta).shuffled(casuale)
        return EsercizioLessico(
            parola = w,
            tipo = TipoEsercizioLessico.TEDESCO,
            domanda = "Come si dice «${w.italian.trim()}» in tedesco?",
            opzioni = opzioni,
            corretta = corretta,
            indiceCorretto = opzioni.indexOf(corretta),
            spiegazione = testoTedesco(w)
        )
    }

    private fun articolo(w: WordEntity, casuale: kotlin.random.Random): EsercizioLessico? {
        val completo = tedescoConArticolo(w)
        val primo = completo.substringBefore(' ').lowercase()
        if (primo !in listOf("der", "die", "das")) return null
        val sostantivo = senzaArticolo(completo)
        if (sostantivo.isBlank()) return null
        val opzioni = listOf("der", "die", "das")
        return EsercizioLessico(
            parola = w,
            tipo = TipoEsercizioLessico.ARTICOLO,
            domanda = "Qual è l'articolo di «$sostantivo»?",
            opzioni = opzioni,
            corretta = primo,
            indiceCorretto = opzioni.indexOf(primo),
            spiegazione = completo + " = " + w.italian.trim()
        )
    }

    private fun cloze(
        w: WordEntity,
        pool: List<WordEntity>,
        casuale: kotlin.random.Random
    ): EsercizioLessico? {
        val frase = fraseConBuco(w) ?: return null
        val corretta = tedescoConArticolo(w)
        val sbagliate = distrattori(pool, w)
            .map { tedescoConArticolo(it) }
            .filter { it.isNotBlank() && !it.equals(corretta, ignoreCase = true) }
            .distinct()
            .shuffled(casuale)
            .take(3)
        // Senza distrattori la frase diventa una domanda aperta: la mostriamo
        // come "scrivi la parola mancante" invece di scartarla.
        if (sbagliate.size < 3) {
            return EsercizioLessico(
                parola = w,
                tipo = TipoEsercizioLessico.CLOZE,
                domanda = frase,
                corretta = senzaArticolo(corretta),
                spiegazione = w.italian.trim()
            )
        }
        val opzioni = (sbagliate + corretta).shuffled(casuale)
        return EsercizioLessico(
            parola = w,
            tipo = TipoEsercizioLessico.CLOZE,
            domanda = frase,
            opzioni = opzioni,
            corretta = corretta,
            indiceCorretto = opzioni.indexOf(corretta),
            spiegazione = w.italian.trim()
        )
    }

    private fun scrivi(w: WordEntity, ascolto: Boolean): EsercizioLessico =
        EsercizioLessico(
            parola = w,
            tipo = if (ascolto) TipoEsercizioLessico.ASCOLTO else TipoEsercizioLessico.SCRIVI,
            domanda = if (ascolto) {
                "Ascolta e scrivi la parola tedesca"
            } else {
                "Scrivi in tedesco: «${w.italian.trim()}»"
            },
            corretta = senzaArticolo(tedescoConArticolo(w)),
            spiegazione = testoTedesco(w)
        )

    // ------------------------------------------------------------ helper

    private fun testoTedesco(w: WordEntity): String {
        val t = tedescoConArticolo(w)
        return if (w.example.isNotBlank()) "$t = ${w.italian.trim()} — ${w.example}"
        else "$t = ${w.italian.trim()}"
    }

    /**
     * Frase d'esempio con la parola sostituita da "___", oppure null se
     * l'esempio non contiene la parola (dati importati da feed esterni).
     */
    fun fraseConBuco(w: WordEntity): String? {
        val esempio = w.example.trim()
        if (esempio.isBlank()) return null
        val completo = tedescoConArticolo(w)
        val lemma = senzaArticolo(completo)
        val candidati = listOf(completo, lemma)
            .filter { it.isNotBlank() }
            .sortedByDescending { it.length }
        val buco = candidati.firstNotNullOfOrNull { target ->
            val regex = Regex("(?i)\\b${Regex.escape(target)}\\b")
            if (regex.containsMatchIn(esempio)) regex.replaceFirst(esempio, "___") else null
        } ?: return null
        // Un buco solo: se la parola compariva due volte la frase resta ambigua.
        if (buco.count { it == '_' } > 3) return null
        return buco
    }
}
