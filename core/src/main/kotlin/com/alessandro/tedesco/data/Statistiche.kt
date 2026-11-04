package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.local.WordSource
import com.alessandro.tedesco.data.CalcoloCompetenze
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Conteggio delle parole per una singola lezione. */
data class ConteggioLezione(
    val lezione: Int,
    val totale: Int,
    val mature: Int
)

/** Attività di ripasso in un giorno specifico. */
data class AttivitaGiorno(
    val etichetta: String,
    val ripassi: Int
)

/** Punto di progresso cumulativo in un giorno specifico. */
data class ProgressPoint(
    val etichetta: String,
    val paroleMature: Int,
    val paroleTotali: Int
)

/** Statistiche complete dell'app, calcolate in modo puro da CalcoloStatistiche. */
data class Statistiche(
    val paroleTotali: Int,
    val paroleSynced: Int,
    val paroleCustom: Int,
    val daRipassare: Int,
    val nuove: Int,
    val inApprendimento: Int,
    val mature: Int,
    val lezioniTotali: Int,
    val streakGiorni: Int,
    val accuratezza: Float,
    val ripassiTotali: Int,
    val erroriTotali: Int,
    val paroleApprese: Int,
    val obiettivoB1: Int,
    val progressoB1: Float,
    val livelloStimato: LivelloCEFR,
    val perLezione: List<ConteggioLezione>,
    val ultimi7giorni: List<AttivitaGiorno>,
    val punteggiCompetenze: CalcoloCompetenze.PunteggiCompetenze?,
    val progresso: List<ProgressPoint>,
    // --- Valori UFFICIALI delle certificazioni (Goethe/telc/ÖSD) ---
    /** Unita lessicali RICETTIVE della Wortliste Goethe per il livello corrente. */
    val wortlisteUfficiale: Int,
    /** Vocabolario ATTIVO richiesto (circa meta' della Wortliste) per il livello. */
    val vocabolarioAttivoUfficiale: Int,
    /** Guided learning hours cumulate da zero (Cambridge English) per il livello. */
    val oreGuidaCumulate: IntRange,
    /** Prädikat ufficiale Goethe sul progresso verso il livello obiettivo. */
    val praedikatVersoObiettivo: CertificazioniUfficiali.Prädikat
)

/**
 * Motore di calcolo puro per le statistiche.
 * Nessun accesso a Android, nessuna coroutine, nessun I/O: girabile in un unit test JVM.
 */
object CalcoloStatistiche {

    // Obiettivo di parole MATURE (SRS) per il traguardo B1: vocabolario ATTIVO
    // ufficiale Goethe per il B1 = Wortliste 2400 / 2 = 1200 parole attive.
    // (Fonte: Wortliste Goethe B1 ~2400 unita; "attivo = circa meta'").
    const val OBIETTIVO_B1 = 1200

    private val FORMATTER_GIORNO = DateTimeFormatter.ofPattern("EEE", Locale.ITALIAN)

    /**
     * Calcola tutte le statistiche a partire dai dati grezzi.
     *
     * @param parole lista completa delle parole (incluse archiviate, qui filtrate)
     * @param reviews mappa wordId -> ReviewEntity
     * @param progresso progresso utente (per livelloCorrente e streakGiorni)
     * @param now timestamp corrente in millisecondi
     */
    fun calcola(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        progresso: ProgressoUtente,
        now: Long,
        ripassiLog: List<RipassoLogEntity> = emptyList()
    ): Statistiche {
        // Filtra le parole archiviate: non devono comparire in nessun conteggio
        val paroleAttive = parole.filter { !it.archived }

        // UN SOLO passaggio sulle parole: prima c'erano ~6 scansioni complete
        // (synced, custom, nuove, in apprendimento, mature, lezioni) piu' un filter.
        var paroleSynced = 0
        var paroleCustom = 0
        var nuove = 0
        var inApprendimento = 0
        var mature = 0
        var daRipassare = 0
        val reviewAttive = HashMap<String, ReviewEntity>(paroleAttive.size * 2)
        val conteggiLezione = HashMap<Int, IntArray>(24)   // lezione -> [totale, mature]
        for (w in paroleAttive) {
            when (w.source) {
                WordSource.SYNCED -> paroleSynced++
                WordSource.CUSTOM -> paroleCustom++
            }
            val r = reviews[w.id]
            if (r == null || r.repetitions == 0) nuove++
            if (r != null) {
                reviewAttive[w.id] = r
                if (r.repetitions in 1..3) inApprendimento++
                if (r.repetitions >= 4) mature++
                if (r.dueAt <= now) daRipassare++
            }
            val conteggio = conteggiLezione.getOrPut(w.lesson) { IntArray(2) }
            conteggio[0]++
            if (r != null && r.repetitions >= 4) conteggio[1]++
        }
        val paroleTotali = paroleAttive.size

        // Lezioni distinte tra le parole attive
        val lezioniDistinte = conteggiLezione.keys.sorted()
        val lezioniTotali = lezioniDistinte.size

        // Ripassi ed errori REALI dal log eventi (una riga per risposta data).
        // Fallback: se il log e vuoto (installazioni vecchie) stima da repetitions/lapses.
        val ripassiTotali: Int
        val erroriTotali: Int
        val accuratezza: Float
        // UN SOLO indice del log per tutto il calcolo (memoizzato).
        val indiceGiorni = IndiceLog.di(ripassiLog)
        if (ripassiLog.isNotEmpty()) {
            // Tutti i conteggi vengono dall'indice: prima erano 2 scansioni complete.
            ripassiTotali = indiceGiorni.totale
            erroriTotali = indiceGiorni.errori
            accuratezza = indiceGiorni.accuratezza
        } else {
            ripassiTotali = reviewAttive.values.sumOf { it.repetitions }
            erroriTotali = reviewAttive.values.sumOf { it.lapses }
            accuratezza = if (ripassiTotali + erroriTotali == 0) 0f
                else ripassiTotali.toFloat() / (ripassiTotali + erroriTotali).toFloat()
        }

        // Parole apprese = mature (repetitions >= 4)
        val paroleApprese = mature

        // Progresso verso B1 (2000 parole)
        val progressoB1 = (paroleApprese.toFloat() / OBIETTIVO_B1.toFloat()).coerceAtMost(1f)

        // Livello stimato: UNICA fonte di verita = criteri CEFR delle competenze.
        // (niente piu soglie-parole separate, niente "mai scendere": cosi un livello
        //  gonfiato in passato viene corretto, non conservato)

        // Conteggio per lezione
        val perLezione = lezioniDistinte.map { lezione ->
            val conteggio = conteggiLezione.getOrPut(lezione) { IntArray(2) }
            ConteggioLezione(lezione = lezione, totale = conteggio[0], mature = conteggio[1])
        }

        // Attività degli ultimi 7 giorni (dal più vecchio a oggi) dal log REALE.
        val oggi = LocalDate.ofEpochDay(now / (1000 * 60 * 60 * 24))
        val ultimi7giorni = (6 downTo 0).map { offset ->
            val giorno = oggi.minusDays(offset.toLong())
            val etichetta = giorno.format(FORMATTER_GIORNO)
            val ripassi = if (ripassiLog.isNotEmpty()) {
                // Un lookup nell'indice invece di una scansione completa del log
                // (erano 7 scansioni e ~35.000 LocalDate per ricalcolo).
                indiceGiorni.ripassiDelGiorno(giorno)
            } else {
                reviewAttive.values.count { r ->
                    r.lastReviewedAt != null && stessoGiorno(r.lastReviewedAt, giorno)
                }
            }
            AttivitaGiorno(etichetta = etichetta, ripassi = ripassi)
        }

        // Calcolo delle 4 competenze
        val punteggiCompetenze = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            now = now
        )

        // Progresso cumulativo degli ultimi 30 giorni
        val progresso30gg = calcolaProgresso(paroleAttive, reviews, now, ripassiLog)

        return Statistiche(
            paroleTotali = paroleTotali,
            paroleSynced = paroleSynced,
            paroleCustom = paroleCustom,
            daRipassare = daRipassare,
            nuove = nuove,
            inApprendimento = inApprendimento,
            mature = mature,
            lezioniTotali = lezioniTotali,
            streakGiorni = progresso.streakGiorni,
            accuratezza = accuratezza,
            ripassiTotali = ripassiTotali,
            erroriTotali = erroriTotali,
            paroleApprese = paroleApprese,
            obiettivoB1 = OBIETTIVO_B1,
            progressoB1 = progressoB1,
            livelloStimato = punteggiCompetenze.livelloComplessivo,
            perLezione = perLezione,
            ultimi7giorni = ultimi7giorni,
            punteggiCompetenze = punteggiCompetenze,
            progresso = progresso30gg,
            wortlisteUfficiale = CertificazioniUfficiali.wortlisteRicettiva(punteggiCompetenze.livelloComplessivo),
            vocabolarioAttivoUfficiale = CertificazioniUfficiali.vocabolarioAttivoRichiesto(punteggiCompetenze.livelloComplessivo),
            oreGuidaCumulate = CertificazioniUfficiali.oreGuidaCumulate(punteggiCompetenze.livelloComplessivo),
            praedikatVersoObiettivo = CertificazioniUfficiali.praedikat(
                // Progresso verso il livello obiettivo misurato col criterio ufficiale 60%:
                // media delle 4 competenze (0-100).
                listOf(
                    punteggiCompetenze.lessico,
                    punteggiCompetenze.grammatica,
                    punteggiCompetenze.comprensione,
                    punteggiCompetenze.produzione
                ).average().toFloat()
            )
        )
    }

    /**
     * Calcola il progresso cumulativo degli ultimi 30 giorni.
     * Per ogni giorno conta parole mature e totali fino a quella data.
     */
    private fun calcolaProgresso(
        paroleAttive: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        now: Long,
        ripassiLog: List<RipassoLogEntity> = emptyList()
    ): List<ProgressPoint> {
        val oggi = LocalDate.ofEpochDay(now / (1000 * 60 * 60 * 24))
        val formatter = DateTimeFormatter.ofPattern("d/M", Locale.ITALIAN)

        // Prima risposta CORRETTA per parola: dall'indice (prima erano due
        // scansioni complete + una mappa grande a ogni ricalcolo).
        val primaCorretta: Map<String, Long> = if (ripassiLog.isNotEmpty()) {
            IndiceLog.di(ripassiLog).primaCorretta
        } else {
            reviews.values.filter { it.lastReviewedAt != null }
                .associate { it.wordId to (it.lastReviewedAt ?: 0L) }
        }

        // I timestamp delle parole imparate, ordinati UNA volta: cosi' ogni giorno
        // e' una ricerca binaria invece di una scansione (erano 30 x N = ~36.000
        // iterazioni per ogni ricalcolo del progresso).
        val imparateOrdinate = primaCorretta.values.sorted()

        return (29 downTo 0).map { offset ->
            val giorno = oggi.minusDays(offset.toLong())
            val etichetta = giorno.format(formatter)
            val giornoFine = giorno.plusDays(1).toEpochDay() * 1000 * 60 * 60 * 24

            val imparate = quantiMinori(imparateOrdinate, giornoFine)

            ProgressPoint(
                etichetta = etichetta,
                paroleMature = imparate,
                paroleTotali = paroleAttive.size
            )
        }
    }

    /** Quanti valori della lista ORDINATA sono minori di [soglia] (ricerca binaria). */
    private fun quantiMinori(ordinati: List<Long>, soglia: Long): Int {
        var lo = 0
        var hi = ordinati.size
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (ordinati[mid] < soglia) lo = mid + 1 else hi = mid
        }
        return lo
    }

    /**
     * Determina il livello CEFR stimato in base al numero di parole apprese.
     * Soglie: <50 A0, <150 A1, <400 A2, <900 B1, <1800 B2, <3500 C1, >=3500 C2.
     */
    private fun livelloDaParole(paroleApprese: Int): LivelloCEFR {
        return when {
            paroleApprese < 50 -> LivelloCEFR.A0
            paroleApprese < 150 -> LivelloCEFR.A1
            paroleApprese < 400 -> LivelloCEFR.A2
            paroleApprese < 900 -> LivelloCEFR.B1
            paroleApprese < 1800 -> LivelloCEFR.B2
            paroleApprese < 3500 -> LivelloCEFR.C1
            else -> LivelloCEFR.C2
        }
    }

    /**
     * Verifica se un timestamp (millisecondi) cade nello stesso giorno di calendario
     * della LocalDate fornita, usando il timezone di sistema.
     */
    private fun stessoGiorno(timestamp: Long, giorno: LocalDate): Boolean {
        val data = LocalDate.ofEpochDay(timestamp / (1000 * 60 * 60 * 24))
        return data == giorno
    }
}
