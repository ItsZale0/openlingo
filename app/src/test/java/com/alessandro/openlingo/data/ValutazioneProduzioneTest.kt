package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test della valutazione della produzione scritta (criteri ufficiali Goethe). */
class ValutazioneProduzioneTest {

    private val NOW = 1_700_000_000_000L

    // ---- Correttezza formale (criterio oggettivo, anti-gonfiaggio) ----

    @Test
    fun `un testo senza errori ha correttezza piena`() {
        assertEquals(100f, CalcoloProduzione.punteggioStrutture(0, 50), 0.01f)
    }

    @Test
    fun `pochi errori sono tollerati`() {
        // 2 errori su qualunque lunghezza: nessuna penalita' (tolleranza)
        assertEquals(100f, CalcoloProduzione.punteggioStrutture(2, 18), 0.01f)
        assertEquals(100f, CalcoloProduzione.punteggioStrutture(2, 100), 0.01f)
    }

    @Test
    fun `la penalita cresce con la densita di errori`() {
        val poco = CalcoloProduzione.punteggioStrutture(3, 100)   // 1 netto / 100
        val molto = CalcoloProduzione.punteggioStrutture(12, 100) // 10 netti / 100
        assertTrue("piu' errori = meno punti", molto < poco)
        assertEquals(93f, poco, 1f)
        assertEquals(33f, molto, 1f)
    }

    @Test
    fun `un testo corto con molti errori non arriva a zero ingiustamente`() {
        // 4 errori su 18 parole: 2 netti -> non 0, ma basso
        val v = CalcoloProduzione.punteggioStrutture(4, 18)
        assertTrue("non zero", v > 0f)
        assertTrue("basso", v < 40f)
    }

    @Test
    fun `densita altissima porta a zero`() {
        assertEquals(0f, CalcoloProduzione.punteggioStrutture(20, 50), 0.01f)
    }

    @Test
    fun `testo vuoto non produce punteggio`() {
        assertEquals(0f, CalcoloProduzione.punteggioStrutture(0, 0), 0.01f)
    }

    // ---- Adempimento del compito ----

    @Test
    fun `un testo troppo corto non adempie al compito`() {
        // A1 richiede 15 parole
        assertTrue(CalcoloProduzione.punteggioAdempimento(5, "A1") < 50f)
        assertEquals(100f, CalcoloProduzione.punteggioAdempimento(15, "A1"), 0.01f)
        assertEquals(100f, CalcoloProduzione.punteggioAdempimento(40, "A1"), 0.01f)
    }

    @Test
    fun `la lunghezza attesa cresce col livello`() {
        assertTrue(CalcoloProduzione.paroleAttese("A1") < CalcoloProduzione.paroleAttese("A2"))
        assertTrue(CalcoloProduzione.paroleAttese("A2") < CalcoloProduzione.paroleAttese("B1"))
        assertTrue(CalcoloProduzione.paroleAttese("B1") < CalcoloProduzione.paroleAttese("B2"))
        assertEquals(15, CalcoloProduzione.paroleAttese("A0"))
        assertEquals(80, CalcoloProduzione.paroleAttese("B1"))
    }

    // ---- Punteggio complessivo (media dei 4 criteri, come l'esame) ----

    @Test
    fun `il punteggio e la media dei 4 criteri`() {
        val v = ValutazioneProduzione(erfuellung = 80f, kohaerenz = 60f, wortschatz = 70f, strukturen = 90f)
        assertEquals(75f, v.punteggio, 0.01f)
    }

    @Test
    fun `la soglia di superamento e il 60 percento ufficiale`() {
        assertTrue(ValutazioneProduzione(60f, 60f, 60f, 60f).superato)
        assertFalse(ValutazioneProduzione(59f, 59f, 59f, 59f).superato)
    }

    @Test
    fun `il praedikat segue i valori ufficiali`() {
        assertEquals("sehr gut", ValutazioneProduzione(95f, 95f, 95f, 95f).praedikat)
        assertEquals("gut", ValutazioneProduzione(85f, 85f, 85f, 85f).praedikat)
        assertEquals("befriedigend", ValutazioneProduzione(75f, 75f, 75f, 75f).praedikat)
        assertEquals("ausreichend", ValutazioneProduzione(65f, 65f, 65f, 65f).praedikat)
        assertEquals("nicht bestanden", ValutazioneProduzione(50f, 50f, 50f, 50f).praedikat)
    }

    @Test
    fun `indica il criterio piu debole`() {
        val v = ValutazioneProduzione(90f, 40f, 80f, 70f)
        assertEquals("Coerenza", v.criterioDebole)
    }

    // ---- Costruzione dalla risposta del tutor ----

    @Test
    fun `costruisci ricalcola strutture e non si fida del modello`() {
        // Il tutor dice lessico 90 ma il testo ha 10 errori su 40 parole
        val v = CalcoloProduzione.costruisci(
            numErrori = 10, numParole = 40, livello = "A2",
            erfuellungTutor = 90f, kohaerenzTutor = 90f, wortschatzTutor = 90f,
            correzione = "ok", now = NOW
        )
        assertEquals(90f, v.wortschatz, 0.01f)
        // strutture calcolate dalla densita': 8 netti / 40 = 20% -> 0
        assertEquals(0f, v.strukturen, 0.01f)
        assertTrue("il punteggio scende", v.punteggio < 70f)
    }

    @Test
    fun `conta le parole ignorando punteggiatura`() {
        assertEquals(5, CalcoloProduzione.contaParole("Ich bin zwanzig Jahre alt."))
        assertEquals(1, CalcoloProduzione.contaParole("  Hallo!  "))
        assertEquals(0, CalcoloProduzione.contaParole("   ...   "))
    }

    @Test
    fun `media delle ultime valutazioni`() {
        val vals = listOf(
            ValutazioneProduzione(50f, 50f, 50f, 50f),
            ValutazioneProduzione(70f, 70f, 70f, 70f),
            ValutazioneProduzione(90f, 90f, 90f, 90f)
        )
        assertEquals(70f, CalcoloProduzione.mediaUltime(vals), 0.01f)
        assertEquals(0f, CalcoloProduzione.mediaUltime(emptyList()), 0.01f)
    }
}
