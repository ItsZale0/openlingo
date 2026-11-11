package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Controlli permanenti sui contenuti di TUTTO il percorso grammaticale.
 *
 * Sono gli stessi controlli usati durante l'audit manuale, trasformati in test:
 * se una unità futura reintroduce una domanda duplicata, due opzioni identiche
 * o una glossa italiana che non torna con la risposta, la build fallisce prima
 * che l'esercizio arrivi allo studente.
 */
class AuditContenutiTest {

    private val unita = GrammaticaSyllabus.unita

    private val glossaDeterminativa =
        Regex("\\((?:il|lo|la|i|gli|le|l')\\s+([^)]*)\\)", RegexOption.IGNORE_CASE)
    private val glossaIndeterminativa =
        Regex("\\((?:un|uno|una|un')\\s+([^)]*)\\)", RegexOption.IGNORE_CASE)

    private val articoliDeterminativi = setOf("der", "die", "das", "den", "dem", "des")
    private val articoliIndeterminativi = setOf(
        "ein", "eine", "einen", "einem", "eines",
        "kein", "keine", "keinen", "keinem", "keines"
    )

    /**
     * Forme grammaticali chiuse che gli esercizi non devono mai chiedere
     * senza spiegazione: possessivi e kein. Gli articoli e i pronomi sono
     * spesso già forniti nella consegna o appartengono a unità precedenti,
     * quindi qui non vengono controllati.
     */
    private val formeChiuse = setOf(
        "kein", "keine", "keinen", "keinem", "keiner", "keines",
        "mein", "meine", "meinen", "meinem", "meiner", "meines",
        "dein", "deine", "deinen", "deinem", "deiner", "deines",
        "sein", "seine", "seinen", "seinem", "seiner", "seines",
        "ihr", "ihre", "ihren", "ihrem", "ihrer", "ihres",
        "unser", "unsere", "unseren", "unserem", "unserer", "unseres",
        "euer", "eure", "euren", "eurem", "eurer", "eures"
    )

    /** Tutto il testo della teoria, tabelle comprese. */
    private fun testoTeoria(u: UnitaGrammatica): String = buildString {
        appendLine(u.teoria.regola.joinToString(" "))
        u.teoria.tabelle.forEach { t ->
            appendLine(t.titolo)
            appendLine(t.intestazioni.joinToString(" "))
            t.righe.forEach { appendLine(it.joinToString(" ")) }
            appendLine(t.nota)
        }
        u.teoria.esempi.forEach { appendLine("${it.tedesco} ${it.italiano}") }
        u.teoria.erroriTipici.forEach { appendLine("${it.sbagliato} ${it.corretto} ${it.spiegazione}") }
        appendLine(u.teoria.merksatz)
    }

    private fun primaParola(testo: String): String =
        testo.trim().split(Regex("\\s+")).firstOrNull()?.lowercase().orEmpty()

    /** Gli id sono l'identità usata da rotazione e progressi: devono essere unici. */
    @Test
    fun `gli id degli esercizi sono unici in tutto il percorso`() {
        val perId = unita.flatMap { u -> u.esercizi.map { u.id to it.id } }
            .groupBy({ it.second }, { it.first })
            .filterValues { it.size > 1 }
        assertTrue("Id ripetuti: $perId", perId.isEmpty())
    }

    /**
     * La stessa domanda non deve comparire in due unità: chi ripassa ritrova
     * la stessa frase e non capisce se sta lavorando su un errore o su un
     * argomento nuovo.
     */
    @Test
    fun `nessuna domanda si ripete tra unita diverse`() {
        val perDomanda = unita
            .flatMap { u -> u.esercizi.map { DeduplicaEsercizi.normalizza(it.domanda) to "${u.id}/${it.id}" } }
            .groupBy({ it.first }, { it.second })
            .filterValues { it.size > 1 }
        assertTrue("Domande ripetute: $perDomanda", perDomanda.isEmpty())
    }

    /**
     * Opzioni identiche rendono la domanda ambigua. Il confronto è
     * case-sensitive: "lesen" e "Lesen" sono due opzioni diverse e legittime.
     */
    @Test
    fun `le opzioni di una scelta multipla non si ripetono`() {
        val problemi = unita.flatMap { u ->
            u.esercizi
                .filter { it.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA }
                .filter { it.opzioni.distinct().size != it.opzioni.size }
                .map { "${u.id}/${it.id}: ${it.opzioni}" }
        }
        assertTrue("Opzioni duplicate: $problemi", problemi.isEmpty())
    }

    /**
     * Se la consegna dice "(il pallone)" la risposta non può essere "einen":
     * la glossa italiana e l'articolo tedesco devono raccontare la stessa cosa.
     * Il controllo è volutamente ristretto ai casi non ambigui (un articolo
     * all'inizio della risposta), per non produrre falsi allarmi.
     */
    @Test
    fun `la glossa italiana e coerente con la risposta`() {
        val problemi = mutableListOf<String>()
        unita.forEach { u ->
            u.esercizi.forEach { e ->
                val risposta = DifficoltaEsercizi.risposta(e)
                val prima = primaParola(risposta)
                val det = glossaDeterminativa.find(e.domanda)
                val indet = glossaIndeterminativa.find(e.domanda)
                if (det != null && prima in articoliIndeterminativi) {
                    problemi += "${u.id}/${e.id}: «${det.value}» → «$risposta»"
                }
                if (indet != null && prima in articoliDeterminativi) {
                    problemi += "${u.id}/${e.id}: «${indet.value}» → «$risposta»"
                }
            }
        }
        assertTrue("Glosse incoerenti: $problemi", problemi.isEmpty())
    }

    /**
     * Una scelta multipla senza una risposta valida non può essere corretta:
     * meglio bloccarla in build che mostrarla con l'indice sbagliato.
     */
    @Test
    fun `ogni esercizio ha una risposta valida`() {
        val problemi = mutableListOf<String>()
        unita.forEach { u ->
            u.esercizi.forEach { e ->
                when (e.tipo) {
                    TipoEsercizioGrammatica.SCELTA_MULTIPLA -> {
                        if (e.opzioni.size < 2) problemi += "${u.id}/${e.id}: opzioni insufficienti"
                        if (e.rispostaCorretta !in e.opzioni.indices) {
                            problemi += "${u.id}/${e.id}: indice ${e.rispostaCorretta} fuori range"
                        }
                    }

                    else -> if (e.rispostaTesto.isBlank()) {
                        problemi += "${u.id}/${e.id}: risposta vuota"
                    }
                }
            }
        }
        assertTrue("Risposte non valide: $problemi", problemi.isEmpty())
    }

    /**
     * Ogni esercizio deve avere una spiegazione: è il momento in cui lo
     * studente capisce l'errore, non un dettaglio estetico.
     */
    @Test
    fun `ogni esercizio ha una spiegazione`() {
        val senza = unita.flatMap { u ->
            u.esercizi
                .filter {
                    it.spiegazione.isBlank() ||
                        it.spiegazione.equals("Nessuna spiegazione disponibile.", ignoreCase = true)
                }
                .map { "${u.id}/${it.id}" }
        }
        assertTrue("Esercizi senza spiegazione: $senza", senza.isEmpty())
    }

    /** La teoria di ogni unità non può essere vuota: gli esercizi la richiamano. */
    @Test
    fun `ogni unita ha una teoria vera`() {
        unita.forEach { u ->
            assertTrue("${u.id}: teoria vuota", u.teoria.regola.isNotEmpty())
            assertTrue("${u.id}: merksatz vuoto", u.teoria.merksatz.isNotBlank())
        }
    }

    /**
     * Se un esercizio chiede una forma chiusa (articolo, possessivo, kein,
     * pronome, dimostrativo) che lo studente deve scrivere, quella forma deve
     * comparire nella teoria dell'unità: altrimenti l'esercizio chiede una
     * cosa che non è mai stata spiegata. Le forme già fornite nella consegna
     * non contano, perché non sono oggetto dell'esercizio.
     */
    @Test
    fun `la teoria nomina le forme chiuse richieste dagli esercizi`() {
        val problemi = mutableListOf<String>()
        unita.forEach { u ->
            val teoria = testoTeoria(u).lowercase()
            val paroleTeoria = Regex("[a-zäöüß]+").findAll(teoria).map { it.value }.toSet()
            u.esercizi.forEach { e ->
                val risposta = DifficoltaEsercizi.risposta(e)
                val consegna = e.domanda.lowercase()
                val mancanti = Regex("[a-zäöüß]+").findAll(risposta.lowercase())
                    .map { it.value }
                    .filter { it in formeChiuse }
                    .filter { it !in paroleTeoria }
                    .filter { parola ->
                        !Regex("\\b${Regex.escape(parola)}\\b").containsMatchIn(consegna)
                    }
                    .distinct()
                    .toList()
                if (mancanti.isNotEmpty()) {
                    problemi += "${u.id}/${e.id}: $mancanti → «$risposta»"
                }
            }
        }
        assertTrue("Forme non spiegate nella teoria: $problemi", problemi.isEmpty())
    }

    /** Nessun esercizio del percorso deve superare il massimo della scala. */
    @Test
    fun `nessuna unita ha esercizi fuori scala`() {
        unita.forEach { u ->
            u.esercizi.forEach { e ->
                assertEquals(
                    "${u.id}/${e.id}",
                    DifficoltaEsercizi.punteggio(e).coerceIn(
                        DifficoltaEsercizi.MINIMA,
                        DifficoltaEsercizi.MASSIMA
                    ),
                    DifficoltaEsercizi.punteggio(e)
                )
            }
        }
    }

    /**
     * La teoria non deve ripetere la risposta esatta di un esercizio nei suoi
     * esempi o negli errori tipici: altrimenti l'esercizio è già svolto.
     * Si controllano solo le risposte di almeno 4 parole, per non bloccare
     * le forme grammaticali che la teoria deve necessariamente nominare.
     */
    @Test
    fun `la teoria non ripete le risposte degli esercizi`() {
        val problemi = mutableListOf<String>()
        unita.forEach { u ->
            val spoiler = buildString {
                u.teoria.esempi.forEach { appendLine(it.tedesco) }
                u.teoria.erroriTipici.forEach { appendLine(it.corretto) }
            }.lowercase()
            u.esercizi.forEach { e ->
                val risposta = DifficoltaEsercizi.risposta(e).trim().lowercase()
                if (risposta.split(Regex("\\s+")).size >= 4 &&
                    Regex("\\b${Regex.escape(risposta)}\\b").containsMatchIn(spoiler)
                ) {
                    problemi += "${u.id}/${e.id}: «${DifficoltaEsercizi.risposta(e)}»"
                }
            }
        }
        assertTrue("Risposte spoilerate nella teoria: $problemi", problemi.isEmpty())
    }
}
