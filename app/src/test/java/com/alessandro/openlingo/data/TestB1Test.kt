package com.alessandro.openlingo.data

import org.junit.Test
import org.junit.Assert.*

class TestB1Test {

    @Test
    fun `tutteLeDomande restituisce 20 domande`() {
        val domande = TestB1Data.tutteLeDomande()
        assertEquals(20, domande.size)
    }

    @Test
    fun `ogni sezione ha 5 domande`() {
        assertEquals(5, TestB1Data.domandeLesen.size)
        assertEquals(5, TestB1Data.domandeHoeren.size)
        assertEquals(5, TestB1Data.domandeSchreiben.size)
        assertEquals(5, TestB1Data.domandeSprechen.size)
    }

    @Test
    fun `ogni domanda ha 4 opzioni`() {
        TestB1Data.tutteLeDomande().forEach { domanda ->
            assertEquals("Domanda ${domanda.id} deve avere 4 opzioni", 4, domanda.opzioni.size)
        }
    }

    @Test
    fun `risposta corretta è valida`() {
        TestB1Data.tutteLeDomande().forEach { domanda ->
            assertTrue(
                "Domanda ${domanda.id}: risposta corretta ${domanda.rispostaCorretta} fuori range",
                domanda.rispostaCorretta in 0 until domanda.opzioni.size
            )
        }
    }
}
