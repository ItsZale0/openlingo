package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test dell'analisi dinamica della produzione scritta. */
class ProduzioneAnalisiTest {

    private fun mk(
        erfuellung: Float = 60f, kohaerenz: Float = 60f,
        wortschatz: Float = 60f, strukturen: Float = 60f,
        numErrori: Int = 2, numParole: Int = 40,
        livello: String = "A1", data: Long = 0L
    ) = ValutazioneProduzione(erfuellung, kohaerenz, wortschatz, strukturen, numErrori, numParole, livello, "ok", data)

    @Test
    fun `trend positivo quando i punteggi salgono`() {
        val l = listOf(mk(erfuellung = 40f), mk(erfuellung = 50f), mk(erfuellung = 70f))
        assertTrue(CalcoloProduzione.calcolaTrend(l) > 0f)
    }

    @Test
    fun `trend negativo quando i punteggi scendono`() {
        val l = listOf(mk(erfuellung = 80f), mk(erfuellung = 60f), mk(erfuellung = 40f))
        assertTrue(CalcoloProduzione.calcolaTrend(l) < 0f)
    }

    @Test
    fun `trend zero con un solo elemento`() {
        assertEquals(0f, CalcoloProduzione.calcolaTrend(listOf(mk())), 0.001f)
    }

    @Test
    fun `criterio piu debole e quello col punteggio minimo`() {
        val l = listOf(mk(erfuellung = 80f, kohaerenz = 70f, wortschatz = 50f, strukturen = 90f))
        val (nome, valore) = CalcoloProduzione.criterioDebole(l)!!
        assertEquals("Wortschatz", nome)
        assertEquals(50f, valore, 0.001f)
    }

    @Test
    fun `criterio debole null senza dati`() {
        assertNull(CalcoloProduzione.criterioDebole(emptyList()))
    }

    @Test
    fun `obiettivo settimanale scala col livello`() {
        assertEquals(1, CalcoloProduzione.obiettivoSettimanale(com.alessandro.openlingo.data.local.LivelloCEFR.A0))
        assertEquals(3, CalcoloProduzione.obiettivoSettimanale(com.alessandro.openlingo.data.local.LivelloCEFR.B1))
        assertEquals(4, CalcoloProduzione.obiettivoSettimanale(com.alessandro.openlingo.data.local.LivelloCEFR.B2))
    }

    @Test
    fun `conta solo le produzioni dell'ultima settimana`() {
        val ora = System.currentTimeMillis()
        val settimanaFa = ora - 7L * 24 * 60 * 60 * 1000
        val vecchia = ora - 10L * 24 * 60 * 60 * 1000
        val l = listOf(mk(data = vecchia), mk(data = ora), mk(data = ora))
        assertEquals(2, CalcoloProduzione.contaSettimana(l))
    }

    @Test
    fun `media delle ultime 5`() {
        val l = (1..8).map { mk(erfuellung = it * 10f) }
        // ultime 5: 40,50,60,70,80 -> media 60
        assertEquals(60f, CalcoloProduzione.mediaUltime(l), 0.001f)
    }

    @Test
    fun `produzione storico ha punteggio totale corretto`() {
        val s = ProduzioneStorico(data = 0L, erfuellung = 80f, kohaerenz = 60f, wortschatz = 70f, strukturen = 50f, numErrori = 3, numParole = 50, livello = "A1", praedikat = "gut")
        assertEquals(65f, s.punteggioTotale, 0.001f)
    }
}
