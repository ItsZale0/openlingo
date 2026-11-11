package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.RipassoLogEntity
import com.alessandro.openlingo.data.local.WordEntity
import com.alessandro.openlingo.data.local.WordSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcoloStatisticheTest {

    private val now = 1_700_000_000_000L // timestamp arbitrario

    private fun parola(
        id: String,
        lesson: Int = 1,
        archived: Boolean = false,
        source: WordSource = WordSource.SYNCED
    ) = WordEntity(
        id = id,
        german = "w$id",
        italian = "p$id",
        lesson = lesson,
        archived = archived,
        source = source
    )

    private fun review(
        wordId: String,
        repetitions: Int = 0,
        lapses: Int = 0,
        dueAt: Long = 0L,
        lastReviewedAt: Long? = null
    ) = ReviewEntity(
        wordId = wordId,
        repetitions = repetitions,
        lapses = lapses,
        dueAt = dueAt,
        lastReviewedAt = lastReviewedAt
    )

    // --- Test 1: lista vuota ---
    @Test
    fun `lista vuota restituisce zeri`() {
        val stats = CalcoloStatistiche.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(),
            now = now
        )
        assertEquals(0, stats.paroleTotali)
        assertEquals(0, stats.paroleSynced)
        assertEquals(0, stats.paroleCustom)
        assertEquals(0, stats.daRipassare)
        assertEquals(0, stats.nuove)
        assertEquals(0, stats.inApprendimento)
        assertEquals(0, stats.mature)
        assertEquals(0, stats.lezioniTotali)
        assertEquals(0f, stats.accuratezza, 0.001f)
        assertEquals(0, stats.ripassiTotali)
        assertEquals(0, stats.erroriTotali)
        assertEquals(0, stats.paroleApprese)
        assertEquals(0f, stats.progressoB1, 0.001f)
        assertEquals(LivelloCEFR.A0, stats.livelloStimato)
        assertTrue(stats.perLezione.isEmpty())
        assertEquals(7, stats.ultimi7giorni.size)
    }

    // --- Test 2: conteggio nuove / mature ---
    @Test
    fun `conteggio nuove inApprendimento mature`() {
        val parole = listOf(
            parola("a"), // nuova (senza review)
            parola("b"), // in apprendimento (repetitions 2)
            parola("c"), // mature (repetitions 5)
            parola("d")  // mature (repetitions 4)
        )
        val reviews = mapOf(
            "b" to review("b", repetitions = 2),
            "c" to review("c", repetitions = 5),
            "d" to review("d", repetitions = 4)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(1, stats.nuove)
        assertEquals(1, stats.inApprendimento)
        assertEquals(2, stats.mature)
        assertEquals(2, stats.paroleApprese)
    }

    // --- Test 3: calcolo accuratezza ---
    @Test
    fun `accuratezza calcolata correttamente`() {
        val parole = listOf(parola("a"), parola("b"))
        val reviews = mapOf(
            "a" to review("a", repetitions = 8, lapses = 2),
            "b" to review("b", repetitions = 4, lapses = 2)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        // ripassiTotali = 12, erroriTotali = 4, accuratezza = 12/16 = 0.75
        assertEquals(12, stats.ripassiTotali)
        assertEquals(4, stats.erroriTotali)
        assertEquals(0.75f, stats.accuratezza, 0.001f)
    }

    // --- Test 4: parole archiviate ignorate ---
    @Test
    fun `parole archiviate ignorate in tutti i conteggi`() {
        val parole = listOf(
            parola("a", archived = false),
            parola("b", archived = true),
            parola("c", archived = true)
        )
        val reviews = mapOf(
            "a" to review("a", repetitions = 5),
            "b" to review("b", repetitions = 5),
            "c" to review("c", repetitions = 5)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(1, stats.paroleTotali)
        // parola "a" non è archiviata e ha source=SYNCED
        assertEquals(1, stats.paroleSynced)
        assertEquals(1, stats.mature)
        assertEquals(1, stats.paroleApprese)
        assertEquals(1, stats.lezioniTotali)
    }

    // --- Test 5: progressoB1 non oltre 1f ---
    @Test
    fun `progressoB1 non supera 1f`() {
        // Crea 2500 parole mature (oltre l'obiettivo di 2000)
        val parole = (1..2500).map { parola("p$it") }
        val reviews = (1..2500).associate { "p$it" to review("p$it", repetitions = 5) }
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(2500, stats.paroleApprese)
        assertEquals(1f, stats.progressoB1, 0.001f)
    }

    // --- Test 6: livelloStimato segue i criteri CEFR reali (corregge i livelli gonfiati) ---
    @Test
    fun `livelloStimato NON resta gonfiato se i criteri non sono soddisfatti`() {
        // 10 parole mature, nessun test -> non si supera alcuna soglia CEFR
        val parole = (1..10).map { parola("p$it") }
        val reviews = (1..10).associate { "p$it" to review("p$it", repetitions = 5) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.B1)  // livello gonfiato salvato
        val stats = CalcoloStatistiche.calcola(parole, reviews, progresso, now)
        // Deve tornare A0: un livello salvato per errore non va conservato
        assertEquals(LivelloCEFR.A0, stats.livelloStimato)
    }

    // --- Test 6b: statistiche reali dal log eventi ---
    @Test
    fun `ripassi e accuratezza vengono dal log eventi`() {
        val parole = (1..3).map { parola("p$it") }
        val reviews = (1..3).associate { "p$it" to review("p$it", repetitions = 5) }
        val log = listOf(
            RipassoLogEntity("p1", now - 1000, corretta = true),
            RipassoLogEntity("p1", now - 2000, corretta = false),
            RipassoLogEntity("p2", now - 3000, corretta = true),
            RipassoLogEntity("p3", now - 4000, corretta = true),
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now, log)
        assertEquals(4, stats.ripassiTotali)      // 4 risposte date
        assertEquals(1, stats.erroriTotali)       // 1 sbagliata
        assertEquals(0.75f, stats.accuratezza, 0.001f)  // 3/4
    }

    // --- Test 7: 7 giorni di attivita' ---
    @Test
    fun `ultimi 7 giorni sempre 7 elementi`() {
        val stats = CalcoloStatistiche.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(),
            now = now
        )
        assertEquals(7, stats.ultimi7giorni.size)
        // Verifica che l'ultimo elemento sia oggi
        val oggi = java.time.LocalDate.ofEpochDay(now / (1000 * 60 * 60 * 24))
        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEE", java.util.Locale.ITALIAN)
        val etichettaOggi = oggi.format(formatter)
        assertEquals(etichettaOggi, stats.ultimi7giorni.last().etichetta)
    }

    // --- Test 8: il livello NON sale con le sole parole (servono i test CEFR) ---
    @Test
    fun `livelloStimato non sale senza test anche con molte parole`() {
        // 500 parole mature ma NESSUN test di competenza -> resta A0
        val parole500 = (1..500).map { parola("p$it") }
        val reviews500 = (1..500).associate { "p$it" to review("p$it", repetitions = 5) }
        val stats = CalcoloStatistiche.calcola(parole500, reviews500, ProgressoUtente(), now)
        assertEquals(LivelloCEFR.A0, stats.livelloStimato)
    }

    // --- Test 9: daRipassare ---
    @Test
    fun `daRipassare conta solo review scadute`() {
        val parole = listOf(parola("a"), parola("b"), parola("c"))
        val reviews = mapOf(
            "a" to review("a", dueAt = now - 1000), // scaduta
            "b" to review("b", dueAt = now + 1000), // non scaduta
            "c" to review("c", dueAt = now)          // scaduta (dueAt <= now)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(2, stats.daRipassare)
    }

    // --- Test 10: perLezione ordinata e corretta ---
    @Test
    fun `perLezione ordinata per lezione crescente`() {
        val parole = listOf(
            parola("a", lesson = 3),
            parola("b", lesson = 1),
            parola("c", lesson = 2),
            parola("d", lesson = 1)
        )
        val reviews = mapOf(
            "a" to review("a", repetitions = 5),
            "b" to review("b", repetitions = 2),
            "c" to review("c", repetitions = 5),
            "d" to review("d", repetitions = 0)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(3, stats.perLezione.size)
        assertEquals(1, stats.perLezione[0].lezione)
        assertEquals(2, stats.perLezione[0].totale)
        assertEquals(0, stats.perLezione[0].mature)
        assertEquals(2, stats.perLezione[1].lezione)
        assertEquals(1, stats.perLezione[1].totale)
        assertEquals(1, stats.perLezione[1].mature)
        assertEquals(3, stats.perLezione[2].lezione)
        assertEquals(1, stats.perLezione[2].totale)
        assertEquals(1, stats.perLezione[2].mature)
    }

    // --- Test 11: progresso 30 giorni ---
    @Test
    fun `progresso sempre 30 elementi`() {
        val stats = CalcoloStatistiche.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(),
            now = now
        )
        assertEquals(30, stats.progresso.size)
    }

    // --- Test 12: progresso con parole mature ---
    @Test
    fun `progresso conta parole mature correttamente`() {
        val oggi = java.time.LocalDate.ofEpochDay(now / (1000 * 60 * 60 * 24))
        val ieri = oggi.minusDays(1)
        val dueGiorniFa = oggi.minusDays(2)

        val tsIeri = ieri.toEpochDay() * 1000 * 60 * 60 * 24 + 1000
        val tsDueGiorniFa = dueGiorniFa.toEpochDay() * 1000 * 60 * 60 * 24 + 1000

        val parole = listOf(
            parola("a"),
            parola("b"),
            parola("c")
        )
        val reviews = mapOf(
            "a" to review("a", repetitions = 5, lastReviewedAt = tsIeri),
            "b" to review("b", repetitions = 5, lastReviewedAt = tsDueGiorniFa),
            "c" to review("c", repetitions = 2, lastReviewedAt = tsIeri)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)

        // Ultimo giorno: parole "imparate" = con prima risposta registrata prima di oggi
        val ultimo = stats.progresso.last()
        assertEquals(3, ultimo.paroleTotali)
        assertEquals(3, ultimo.paroleMature) // a, b, c hanno tutte una risposta registrata

        // Il progresso è cumulativo: l'ultimo elemento ha il valore massimo
        val maxMature = stats.progresso.maxOf { it.paroleMature }
        assertEquals(3, maxMature)
    }

    // --- Test 13: progresso vuoto quando non ci sono parole ---
    @Test
    fun `progresso zeri quando nessuna parola`() {
        val stats = CalcoloStatistiche.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(),
            now = now
        )
        assertTrue(stats.progresso.all { it.paroleMature == 0 && it.paroleTotali == 0 })
    }
}
