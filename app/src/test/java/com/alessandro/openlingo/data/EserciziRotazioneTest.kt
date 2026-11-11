package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test del RotoreEsercizi (rotazione deterministica per giorno) e dei banchi
 * di esercizi espansi: ogni livello CEFR deve avere esercizi Sprechuebungen.
 */
class EserciziRotazioneTest {

    @Test
    fun `la rotazione ritorna tutti gli indici senza duplicati`() {
        val ordine = RotoreEsercizi.ordine("test", 12)
        assertEquals(12, ordine.size)
        assertEquals((0 until 12).toSet(), ordine.toSet())
    }

    @Test
    fun `la rotazione e deterministica nello stesso giorno`() {
        val a = RotoreEsercizi.ordine("dialoghi", 9)
        val b = RotoreEsercizi.ordine("dialoghi", 9)
        assertEquals(a, b)
    }

    @Test
    fun `sezioni diverse hanno ordini diversi`() {
        val a = RotoreEsercizi.ordine("dialoghi", 20)
        val b = RotoreEsercizi.ordine("pronuncia", 20)
        assertTrue("Ordini diversi attesi tra sezioni diverse", a != b)
    }

    @Test
    fun `la rotazione reglista grandezza zero`() {
        assertTrue(RotoreEsercizi.ordine("vuota", 0).isEmpty())
        assertEquals(0, RotoreEsercizi.indiceIniziale("vuota", 0))
    }

    @Test
    fun `sprechuebungen ha esercizi per ogni livello CEFR`() {
        for (livello in listOf("A0", "A1", "A2", "B1", "B2")) {
            val n = Sprechuebungen.perLivello(livello).size
            assertTrue("A0/A1/A2/B1/B2 devono avere Sprechuebungen (livello $livello)", n >= 5)
        }
    }

    @Test
    fun `dialoghi hanno orar fino alla B2 e per ogni battuta una traduzione`() {
        assertTrue("Almeno 10 dialoghi", Dialoghi.dialoghi.size >= 10)
        val livelli = Dialoghi.dialoghi.map { it.livello }.toSet()
        assertTrue("B2 presente", livelli.contains("B2"))
        Dialoghi.dialoghi.forEach { d ->
            assertEquals("Battute/traduzione allineate (${d.id})",
                d.battute.size, d.battute.count { it.traduzione.isNotBlank() })
            assertTrue("Servono almeno 3 domande (${d.id})", d.domande.size >= 3)
            d.domande.forEach { q ->
                assertEquals("4 opzioni per domanda (${d.id})", 4, q.opzioni.size)
                assertTrue("Indice corretta in range (${d.id})", q.corretta in 0..3)
            }
        }
    }
}
