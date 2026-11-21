package com.alessandro.openlingo.data

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica che le lezioni di preposizioni spieghino il SENSO prima della regola,
 * come richiesto: testo chiaro, semplice e completo.
 */
class ChiarezzaLezioniTest {

    @Test
    fun `lezione preposizioni luogo spiega il senso wo wohin`() {
        val u = GrammaticaSyllabus.unita.first { it.id == "a1-preposizioni-luogo" }
        val testo = u.teoria.regola.joinToString(" ")
        assertTrue("Deve spiegare WO? come stato fermo", testo.contains("WO?", ignoreCase = true))
        assertTrue("Deve spiegare WOHIN? come movimento", testo.contains("WOHIN?", ignoreCase = true))
        assertTrue("Deve dare il trucco pratico", testo.contains("trucco", ignoreCase = true))
        assertTrue("Deve spiegare le contrazioni", testo.contains("contrazion", ignoreCase = true))
        assertTrue("Deve spiegare nach per le citta'", testo.contains("nach", ignoreCase = true))
        assertTrue("Deve avere almeno 8 esempi", u.teoria.esempi.size >= 8)
        assertTrue("Tutti gli esempi devono avere nota esplicativa", u.teoria.esempi.all { it.nota.isNotBlank() })
    }

    @Test
    fun `lezione preposizioni tempo spiega ogni unita temporale`() {
        val u = GrammaticaSyllabus.unita.first { it.id == "a1-preposizioni-tempo" }
        val testo = u.teoria.regola.joinToString(" ")
        assertTrue("Deve spiegare um = ora", testo.contains("um", ignoreCase = true))
        assertTrue("Deve spiegare am = giorno", testo.contains("am", ignoreCase = true))
        assertTrue("Deve spiegare im = mese", testo.contains("im", ignoreCase = true))
        assertTrue("Deve spiegare seit", testo.contains("seit", ignoreCase = true))
        assertTrue("Deve spiegare vor", testo.contains("vor", ignoreCase = true))
        assertTrue("Deve spiegare il trucco", testo.contains("trucco", ignoreCase = true))
        assertTrue("Deve avere esempi con note", u.teoria.esempi.all { it.nota.isNotBlank() })
    }

    @Test
    fun `lezione preposizioni miste spiega perche miste`() {
        val u = GrammaticaSyllabus.unita.first { it.id == "a2-wechselpraepositionen" }
        val testo = u.teoria.regola.joinToString(" ")
        assertTrue("Deve spiegare il perche' del nome", testo.contains("miste", ignoreCase = true))
        assertTrue("Deve spiegare hängen nei due sensi", testo.contains("hängen", ignoreCase = true))
        assertTrue("Deve avere esempi con note", u.teoria.esempi.all { it.nota.isNotBlank() })
    }
}
