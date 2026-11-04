package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR

import kotlinx.serialization.Serializable

/**
 * VALUTAZIONE della produzione scritta su criteri UFFICIALI Goethe/telc/ÖSD.
 *
 * La prova di Schreiben non si valuta con un unico voto: Goethe e telc usano
 * QUATTRO criteri, ognuno con lo stesso peso, e il punteggio e' la loro media.
 * La soglia di superamento e' il 60% (Bestehensgrenze ufficiale).
 *
 * Criteri (Goethe B1/B2 Schreiben, telc Schreiben):
 * - Erfuellung: adempimento del compito (risponde a quanto chiesto?)
 * - Kohaerenz: coerenza e struttura (testo collegato, ordine logico)
 * - Wortschatz: ricchezza e appropriatezza del lessico
 * - Strukturen: correttezza formale (grammatica, ortografia, casi)
 *
 * A differenza di un "voto" arbitrario del modello, il punteggio e' ancorato a
 * dati oggettivi del testo (numero di errori per numero di parole): un testo
 * con pochi errori e sufficiente lunghezza non puo' prendere un voto basso.
 */
@Serializable
data class ValutazioneProduzione(
    /** 0-100 per criterio (criteri ufficiali Goethe). */
    val erfuellung: Float = 0f,
    val kohaerenz: Float = 0f,
    val wortschatz: Float = 0f,
    val strukturen: Float = 0f,
    /** Numero di errori individuati dal tutor. */
    val numErrori: Int = 0,
    /** Numero di parole scritte. */
    val numParole: Int = 0,
    /** Livello dello studente al momento della prova. */
    val livello: String = "",
    /** La correzione testuale del tutor (markdown). */
    val correzione: String = "",
    val data: Long = 0L
) {
    /** Punteggio complessivo = media dei 4 criteri (come l'esame). */
    val punteggio: Float get() = (erfuellung + kohaerenz + wortschatz + strukturen) / 4f

    val superato: Boolean get() = punteggio >= CertificazioniUfficiali.SOGLIA_SUPERAMENTO

    /** Prädikat ufficiale (sehr gut / gut / befriedigend / ausreichend / nicht bestanden). */
    val praedikat: String get() = CertificazioniUfficiali.praedikat(punteggio).etichetta

    /** Dimensione piu' debole: cosa migliorare. */
    val criterioDebole: String get() = listOf(
        "Adempimento del compito" to erfuellung,
        "Coerenza" to kohaerenz,
        "Lessico" to wortschatz,
        "Correttezza formale" to strukturen
    ).minBy { it.second }.first
}

/**
 * Calcola la valutazione dai dati OGGETTIVI del testo + il giudizio del tutor.
 *
 * Regola anti-gonfiaggio: il punteggio di "Strukturen" (correttezza formale)
 * e' calcolato dalla DENSITA' di errori (errori per 100 parole), non dal
 * giudizio libero del modello. Un modello generoso non puo' regalare un voto
 * alto a un testo pieno di errori.
 */

/** Singola produzione nel storico. */
data class ProduzioneStorico(
    val data: Long,
    val erfuellung: Float,
    val kohaerenz: Float,
    val wortschatz: Float,
    val strukturen: Float,
    val numErrori: Int,
    val numParole: Int,
    val livello: String,
    val praedikat: String
) {
    val punteggioTotale: Float get() = (erfuellung + kohaerenz + wortschatz + strukturen) / 4f
}

/** Calcoli derivati dalla produzione scritta. */
object CalcoloProduzione {

    /**
     * Errori "gratis" prima di penalizzare: ogni testo, anche di un madrelingua
     * alle prime prove, tollera qualche imprecisione. Contarli tutti da zero
     * rende il punteggio severo in modo irrealistico, soprattutto sui testi corti.
     */
    private const val ERRORI_TOLLERATI = 2

    /** Errori ogni 100 parole oltre i quali la correttezza formale e' 0. */
    private const val ERRORI_MAX_PER_100 = 15f

    /** Sotto questa lunghezza il compito non e' "adempiuto" a sufficienza. */
    private const val PAROLE_MINIME = 15

    /**
     * Punteggio di correttezza formale dalla densita' di errori, con tolleranza.
     *
     * - fino a [ERRORI_TOLLERATI] errori: 100 (nessuna penalita')
     * - densita' (errori oltre la tolleranza) >= ERRORI_MAX_PER_100: 0
     * - in mezzo: lineare
     *
     * La tolleranza evita che un testo corto con 3-4 errori finisca a 0: la
     * densita' su poche parole e' statisticamente instabile.
     */
    fun punteggioStrutture(numErrori: Int, numParole: Int): Float {
        if (numParole <= 0) return 0f
        val erroriNet = (numErrori - ERRORI_TOLLERATI).coerceAtLeast(0)
        if (erroriNet == 0) return 100f
        val densita = erroriNet.toFloat() / numParole * 100f
        val scala = (ERRORI_MAX_PER_100 - densita) / ERRORI_MAX_PER_100
        return (scala * 100f).coerceIn(0f, 100f)
    }

    /**
     * Punteggio di adempimento: un testo troppo corto non risponde al compito.
     * Raggiunge 100 al raggiungimento della lunghezza attesa per il livello.
     */
    fun punteggioAdempimento(numParole: Int, livello: String): Float {
        val attese = paroleAttese(livello)
        if (numParole >= attese) return 100f
        return (numParole.toFloat() / attese * 100f).coerceIn(0f, 100f)
    }

    /** Lunghezza attesa (parole) per una prova di produzione scritta, per livello. */
    fun paroleAttese(livello: String): Int = when (livello.uppercase().substringBefore(" ")) {
        "A0", "A1" -> PAROLE_MINIME
        "A2" -> 40
        "B1" -> 80
        "B2" -> 120
        else -> 40
    }

    /**
     * Costruisce la valutazione finale combinando i dati oggettivi del testo con
     * i punteggi che il tutor ha dato su adempimento, coerenza e lessico.
     * "Strukturen" viene SEMPRE ricalcolato dalla densita' di errori.
     */
    fun costruisci(
        numErrori: Int,
        numParole: Int,
        livello: String,
        erfuellungTutor: Float?,
        kohaerenzTutor: Float?,
        wortschatzTutor: Float?,
        correzione: String,
        now: Long
    ): ValutazioneProduzione {
        val adempimento = erfuellungTutor ?: punteggioAdempimento(numParole, livello)
        return ValutazioneProduzione(
            erfuellung = adempimento.coerceIn(0f, 100f),
            kohaerenz = (kohaerenzTutor ?: 0f).coerceIn(0f, 100f),
            wortschatz = (wortschatzTutor ?: 0f).coerceIn(0f, 100f),
            strukturen = punteggioStrutture(numErrori, numParole),
            numErrori = numErrori,
            numParole = numParole,
            livello = livello,
            correzione = correzione,
            data = now
        )
    }

    /** Conta le parole di un testo (le sequenze di lettere/cifre). */
    fun contaParole(testo: String): Int =
        Regex("[\\p{L}\\p{N}]+").findAll(testo).count()

    /** Media delle ultime N valutazioni (per il punteggio produzione complessivo). */
    fun mediaUltime(valutazioni: List<ValutazioneProduzione>, n: Int = 5): Float {
        if (valutazioni.isEmpty()) return 0f
        return valutazioni.takeLast(n).map { it.punteggio }.average().toFloat()
    }

    /** Trend lineare sui punteggi (pendenza della retta di regressione). */
    fun calcolaTrend(valutazioni: List<ValutazioneProduzione>): Float {
        if (valutazioni.size < 2) return 0f
        val n = valutazioni.size
        val x = (0 until n).map { it.toFloat() }
        val y = valutazioni.map { (it.erfuellung + it.kohaerenz + it.wortschatz + it.strukturen) / 4f }
        val xm = x.average()
        val ym = y.average()
        val num = x.zip(y).sumOf { (xi, yi) -> (xi - xm) * (yi - ym).toDouble() }.toFloat()
        val den = x.sumOf { (it - xm) * (it - xm).toDouble() }.toFloat()
        return if (den == 0f) 0f else num / den
    }

    /** Correlazione con le altre competenze (0-1). */
    fun calcolaCorrelazione(
        valutazioni: List<ValutazioneProduzione>,
        competenze: CalcoloCompetenze.PunteggiCompetenze
    ): Float {
        if (valutazioni.isEmpty()) return 0f
        val mediaProd = valutazioni.map { (it.erfuellung + it.kohaerenz + it.wortschatz + it.strukturen) / 4f }.average().toFloat()
        val altre = listOf(competenze.grammatica, competenze.comprensione, competenze.lessico)
        val mediaAltre = altre.average().toFloat()
        return 1f - kotlin.math.abs(mediaProd - mediaAltre) / 100f
    }

    /** Quante produzioni negli ultimi 7 giorni. */
    fun contaSettimana(valutazioni: List<ValutazioneProduzione>): Int {
        val ora = System.currentTimeMillis()
        val settimanaFa = ora - 7L * 24 * 60 * 60 * 1000
        return valutazioni.count { it.data >= settimanaFa }
    }

    /** Quante produzioni servono a settimana per il livello. */
    fun obiettivoSettimanale(livello: LivelloCEFR): Int = when (livello) {
        LivelloCEFR.A0, LivelloCEFR.A1 -> 1
        LivelloCEFR.A2 -> 2
        LivelloCEFR.B1 -> 3
        LivelloCEFR.B2 -> 4
        else -> 2
    }

    /** Criterio più debole (nome, valore). */
    fun criterioDebole(valutazioni: List<ValutazioneProduzione>): Pair<String, Float>? {
        if (valutazioni.isEmpty()) return null
        val ultime = valutazioni.takeLast(5)
        val erfuellung = ultime.map { it.erfuellung }.average().toFloat()
        val kohaerenz = ultime.map { it.kohaerenz }.average().toFloat()
        val wortschatz = ultime.map { it.wortschatz }.average().toFloat()
        val strukturen = ultime.map { it.strukturen }.average().toFloat()
        val criteri = listOf(
            "Erfüllung" to erfuellung,
            "Kohärenz" to kohaerenz,
            "Wortschatz" to wortschatz,
            "Strukturen" to strukturen
        )
        return criteri.minByOrNull { it.second }
    }
}