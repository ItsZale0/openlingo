package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.RipassoLogEntity
import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.TestComprensione
import com.alessandro.openlingo.data.local.TestGrammatica
import com.alessandro.openlingo.data.local.TestProduzione
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test dei criteri CEFR: il livello e il massimo in cui TUTTE le dimensioni
 * (lessico maturo, grammatica, comprensione, produzione) raggiungono la soglia.
 * Niente fallback gonfiati: senza test reali non si sale di livello.
 */
class CompetenzeScreenTest {

    private val NOW = 1_700_000_000_000L

    private fun parola(id: String, archived: Boolean = false) = WordEntity(
        id = id, german = "w$id", italian = "p$id", archived = archived
    )
    private fun review(wordId: String, repetitions: Int, lapses: Int = 0) = ReviewEntity(
        wordId = wordId, repetitions = repetitions, lapses = lapses
    )
    /** Log che VERIFICA le parole indicate (2 risposte corrette = verificata). */
    private fun logVerificate(ids: Collection<String>) = ids.mapIndexed { i, id ->
        RipassoLogEntity(id, NOW + i, true, verifica = true)
    }
    private fun tGramm(p: Float) = TestGrammatica(NOW, p, 0, 10)
    private fun tComp(p: Float) = TestComprensione(NOW, p, 0, 10)
    private fun tProd(p: Float) = TestProduzione(NOW, p, 0, 10)

    @Test
    fun `PunteggiCompetenze has all 4 scores plus livello`() {
        val p = CalcoloCompetenze.PunteggiCompetenze(50f, 60f, 70f, 80f, LivelloCEFR.B1)
        assertEquals(50f, p.lessico, 0.01f)
        assertEquals(80f, p.produzione, 0.01f)
        assertEquals(LivelloCEFR.B1, p.livelloComplessivo)
    }

    @Test
    fun `lessico capped at 100`() {
        val parole = (1..3000).map { parola("p$it") }
        val reviews = (1..3000).associate { "p$it" to review("p$it", 5) }
        val log = logVerificate((1..3000).map { "p$it" })
        val r = CalcoloCompetenze.calcola(parole, reviews, ProgressoUtente(), NOW, log)
        assertEquals(100f, r.lessico, 0.01f)
    }

    @Test
    fun `lessico is 0 when no mature words`() {
        // repetitions alte NON bastano: il lessico conta solo le parole VERIFICATE.
        val parole = listOf(parola("1"), parola("2"))
        val reviews = mapOf("1" to review("1", 5), "2" to review("2", 5))
        val r = CalcoloCompetenze.calcola(parole, reviews, ProgressoUtente(), NOW, emptyList())
        assertEquals(0f, r.lessico, 0.01f)
    }

    @Test
    fun `grammatica is 0 when no reviews`() {
        val r = CalcoloCompetenze.calcola(listOf(parola("1")), emptyMap(), ProgressoUtente(accuratezzaMedia = 0.75f), NOW)
        assertEquals(0f, r.grammatica, 0.01f)
    }

    @Test
    fun `grammatica is 0 when reviews exist but no repetitions or lapses`() {
        val r = CalcoloCompetenze.calcola(listOf(parola("1")), mapOf("1" to review("1", 0, 0)), ProgressoUtente(), NOW)
        assertEquals(0f, r.grammatica, 0.01f)
    }

    @Test
    fun `comprensione e produzione sono 0 senza test`() {
        val r = CalcoloCompetenze.calcola(emptyList(), emptyMap(), ProgressoUtente(accuratezzaMedia = 0.6f), NOW)
        assertEquals(0f, r.comprensione, 0.01f)
        assertEquals(0f, r.produzione, 0.01f)
    }

    @Test
    fun `comprensione e produzione usano i test reali`() {
        val prog = ProgressoUtente(
            testComprensione = listOf(tComp(80f), tComp(90f)),
            testProduzione = listOf(tProd(70f))
        )
        val r = CalcoloCompetenze.calcola(emptyList(), emptyMap(), prog, NOW)
        assertEquals(85f, r.comprensione, 0.01f)
        assertEquals(70f, r.produzione, 0.01f)
    }

    @Test
    fun `senza test resta A0 anche con molto lessico`() {
        val parole = (1..500).map { parola("p$it") }
        val reviews = (1..500).associate { "p$it" to review("p$it", 5) }
        val r = CalcoloCompetenze.calcola(parole, reviews, ProgressoUtente(), NOW)
        assertEquals(LivelloCEFR.A0, r.livelloComplessivo)
    }

    @Test
    fun `A0 quando una dimensione e sotto soglia`() {
        val parole = (1..40).map { parola("p$it") }
        val reviews = (1..40).associate { "p$it" to review("p$it", 5) }
        val prog = ProgressoUtente(
            testGrammatica = List(3) { tGramm(90f) },
            testComprensione = List(3) { tComp(90f) },
            testProduzione = List(3) { tProd(50f) }   // sotto 70
        )
        assertEquals(LivelloCEFR.A0, CalcoloCompetenze.calcola(parole, reviews, prog, NOW).livelloComplessivo)
    }

    @Test
    fun `A1 quando tutte le dimensioni superano la soglia A1`() {
        val parole = (1..40).map { parola("p$it") }
        val reviews = (1..40).associate { "p$it" to review("p$it", 5) }
        val log = logVerificate((1..40).map { "p$it" })  // 40/40 verificate = 100% lessico
        val prog = ProgressoUtente(
            testGrammatica = List(3) { tGramm(75f) },
            testComprensione = List(3) { tComp(75f) },
            testProduzione = List(3) { tProd(75f) }
        )
        assertEquals(LivelloCEFR.A1, CalcoloCompetenze.calcola(parole, reviews, prog, NOW, log).livelloComplessivo)
    }

    @Test
    fun `serve il numero minimo di test per livello`() {
        val parole = (1..40).map { parola("p$it") }
        val reviews = (1..40).associate { "p$it" to review("p$it", 5) }
        // Solo 2 test per dimensione: sotto il minimo (3)
        val prog = ProgressoUtente(
            testGrammatica = List(2) { tGramm(90f) },
            testComprensione = List(2) { tComp(90f) },
            testProduzione = List(2) { tProd(90f) }
        )
        assertEquals(LivelloCEFR.A0, CalcoloCompetenze.calcola(parole, reviews, prog, NOW).livelloComplessivo)
    }

    @Test
    fun `archived words excluded from lessico`() {
        val parole = listOf(parola("1"), parola("2", archived = true), parola("3", archived = true))
        val reviews = mapOf("1" to review("1", 5), "2" to review("2", 5), "3" to review("3", 5))
        // Solo la parola 1 e' attiva e verificata -> lessico = 100% delle attive
        val log = logVerificate(listOf("1", "2", "3"))
        val r = CalcoloCompetenze.calcola(parole, reviews, ProgressoUtente(), NOW, log)
        assertEquals(100f, r.lessico, 0.01f)
    }

    @Test
    fun `Statistiche includes punteggiCompetenze`() {
        val stats = CalcoloStatistiche.calcola(emptyList(), emptyMap(), ProgressoUtente(), NOW)
        assertTrue(stats.punteggiCompetenze != null)
    }
}
