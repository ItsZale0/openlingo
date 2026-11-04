package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.WordEntity

/**
 * Calcola i 4 punteggi di competenza (0-100) per il livello B1.
 *
 * - Lessico: parole mature / 2000 * 100 (cap 100)
 * - Grammatica: basata su errori nelle review (accuratezza) e test di grammatica
 * - Comprensione: basata su quiz di comprensione (da implementare)
 * - Produzione: basata su frasi complete (da implementare)
 */
object CalcoloCompetenze {

    const val OBIETTIVO_PAROLE_B1 = 2000

    /** Minimo di test per dimensione perché il livello possa avanzare. */
    private fun minTest(livello: LivelloCEFR): Int = when (livello) {
        LivelloCEFR.A0, LivelloCEFR.A1 -> 3
        LivelloCEFR.A2 -> 4
        LivelloCEFR.B1 -> 6
        else -> 8
    }

    /**
     * Requisiti per raggiungere un livello: criterio UFFICIALE delle certificazioni.
     *
     * La Bestehensgrenze ufficiale Goethe / telc / ÖSD e' il **60%** per ogni modulo
     * (fonte: Durchfuehrungsbestimmungen Goethe B1/B2, telc, ÖSD ZB1). Non esistono
     * soglie piu' alte: si passa con 60. Quindi la soglia applicata e' 60%, non i
     * valori "inventati" (70/78/82/85) usati prima.
     *
     * Lessico: percentuale di parole ATTIVE del livello che sono mature. Il target
     * assoluto ufficiale (Wortliste) e' mostrato nelle statistiche via
     * CertificazioniUfficiali; qui si misura il progresso reale sulle parole
     * disponibili nel feed (raggiungibile anche con un vocabolario piccolo).
     */
    private data class Requisito(
        val percentualeParole: Float,
        val grammatica: Float,
        val comprensione: Float,
        val produzione: Float
    )

    // Tutte le soglie = 60% (Bestehensgrenze ufficiale Goethe/telc/ÖSD).
    private val SOGLIA = CertificazioniUfficiali.SOGLIA_SUPERAMENTO
    private val REQ_A1 = Requisito(SOGLIA, SOGLIA, SOGLIA, SOGLIA)
    private val REQ_A2 = Requisito(SOGLIA, SOGLIA, SOGLIA, SOGLIA)
    private val REQ_B1 = Requisito(SOGLIA, SOGLIA, SOGLIA, SOGLIA)
    private val REQ_B2 = Requisito(SOGLIA, SOGLIA, SOGLIA, SOGLIA)

    private fun mediaUltimi(punteggi: List<Float>): Float =
        if (punteggi.isEmpty()) 0f else punteggi.takeLast(5).average().toFloat()

    data class PunteggiCompetenze(
        val lessico: Float,      // 0-100
        val grammatica: Float,   // 0-100
        val comprensione: Float, // 0-100
        val produzione: Float,   // 0-100
        val livelloComplessivo: LivelloCEFR
    )

    /**
     * Calcola i 4 punteggi di competenza.
     *
     * @param parole tutte le parole (incluse archiviate)
     * @param reviews mappa wordId -> ReviewEntity
     * @param progresso progresso utente (per test di grammatica/comprensione/produzione)
     * @param now timestamp corrente
     */
    fun calcola(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        progresso: ProgressoUtente,
        now: Long,
        log: List<RipassoLogEntity> = emptyList()
    ): PunteggiCompetenze {
        val paroleAttive = parole.filter { !it.archived }

        // LESSICO: parole VERIFICATE (evidenza oggettiva), non "quante volte viste".
        // Il conteggio `repetitions >= 4` misurava solo quante volte la parola era
        // stata mostrata: si poteva dire "lo so" senza saperlo. Ora conta solo cio'
        // che l'utente ha DIMOSTRATO nel quiz a scelta multipla (VerificaLessico).
        val verifiche = VerificaLessico.verificheDaLog(log)
        // Conta solo le verificate che sono ancora nel vocabolario attivo:
        // una parola archiviata/rimossa non deve gonfiare il punteggio.
        val idAttivi = paroleAttive.map { it.id }.toSet()
        val verificheAttive = verifiche.filterKeys { it in idAttivi }
        val lessico = VerificaLessico.punteggioLessico(paroleAttive.size, verificheAttive)

        // GRAMMATICA: accuratezza delle review (SM-2). Nessun fallback gonfiato.
        val tutteReviews = paroleAttive.mapNotNull { reviews[it.id] }
        val grammaticaReview = if (tutteReviews.isEmpty()) 0f else {
            val ripassi = tutteReviews.sumOf { it.repetitions }
            val errori = tutteReviews.sumOf { it.lapses }
            if (ripassi + errori == 0) 0f
            else (ripassi.toFloat() / (ripassi + errori) * 100)
        }

        // COMPRENSIONE / PRODUZIONE / GRAMMATICA-TEST: SOLO da test reali.
        // Se mancano i test il punteggio è 0: niente fallback che gonfia il livello.
        val comprensione = mediaUltimi(progresso.testComprensione.map { it.punteggio })
        // PRODUZIONE: unisce i quiz a scelta multipla E le produzioni scritte
        // valutate dal tutor. La produzione scritta e' l'evidenza piu' forte
        // (l'utente SCRIVE, non riconosce): pesa allo stesso modo dei quiz.
        val daQuiz: List<Float> = progresso.testProduzione.map { it.punteggio }
        val daScritti: List<Float> = progresso.valutazioniProduzione.map { it.punteggio }
        val produzione = when {
            daQuiz.isEmpty() && daScritti.isEmpty() -> 0f
            else -> (mediaUltimi(daQuiz) * daQuiz.size + mediaUltimi(daScritti) * daScritti.size) /
                (daQuiz.size + daScritti.size).toFloat()
        }
        val grammatica = if (progresso.testGrammatica.isNotEmpty())
            mediaUltimi(progresso.testGrammatica.map { it.punteggio })
        else grammaticaReview

        // CRITERI CEFR (Goethe/telc/ÖSD): il livello è il massimo in cui TUTTE le
        // dimensioni raggiungono la soglia ("maglia più debole"). Servono almeno
        // MIN_TEST test per dimensione + un numero sufficiente di parole mature.
        val nGramm = progresso.testGrammatica.size
        val nComp = progresso.testComprensione.size
        val nProd = progresso.testProduzione.size + progresso.valutazioniProduzione.size

        /**
         * Criterio UFFICIALE: tutte le dimensioni devono superare la Bestehensgrenze
         * del 60% (Goethe/telc/ÖSD) e serve un minimo di evidenza (test svolti),
         * perche' il 60% ufficiale vale su una prova completa, non su una domanda.
         */
        fun raggiunge(r: Requisito, livello: LivelloCEFR): Boolean {
            val min = minTest(livello)
            return lessico >= r.percentualeParole &&
                nGramm >= min && grammatica >= r.grammatica &&
                nComp >= min && comprensione >= r.comprensione &&
                nProd >= min && produzione >= r.produzione
        }

        val livelloComplessivo = when {
            raggiunge(REQ_B2, LivelloCEFR.B2) -> LivelloCEFR.B2
            raggiunge(REQ_B1, LivelloCEFR.B1) -> LivelloCEFR.B1
            raggiunge(REQ_A2, LivelloCEFR.A2) -> LivelloCEFR.A2
            raggiunge(REQ_A1, LivelloCEFR.A1) -> LivelloCEFR.A1
            else -> LivelloCEFR.A0
        }

        return PunteggiCompetenze(
            lessico = lessico,
            grammatica = grammatica,
            comprensione = comprensione,
            produzione = produzione,
            livelloComplessivo = livelloComplessivo
        )
    }
}
