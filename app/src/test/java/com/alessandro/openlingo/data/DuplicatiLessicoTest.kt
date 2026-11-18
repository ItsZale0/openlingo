package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test della logica di confronto tollerante usata per rilevare duplicati
 * nel lessico (minuscole, articolo iniziale, spazi).
 */
class DuplicatiLessicoTest {

    private fun normalizza(t: String): String = t.trim().lowercase()
        .replace(Regex("^\\s*(der|die|das|den|dem|des)\\s+"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

    @Test
    fun `stessa parola con articolo diverso e' duplicato`() {
        assertEquals(normalizza("der Tisch"), normalizza("Tisch"))
        assertEquals(normalizza("die Tisch"), normalizza("der  tisch "))
    }

    @Test
    fun `parole diverse non sono duplicati`() {
        assertEquals(false, normalizza("Tisch") == normalizza("Stuhl"))
        assertEquals(false, normalizza("Haus") == normalizza("Hause"))
    }

    @Test
    fun `spazi multipli vengono compressi`() {
        assertEquals(normalizza("der   grosse   Hund"), normalizza(grosseHund()))
    }

    private fun grosseHund() = "grosse Hund"
}
