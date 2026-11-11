package com.alessandro.openlingo.ui

import com.alessandro.openlingo.ui.MessaggioTutor
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Il LazyColumn della bolla usa l'id come chiave. Prima l'id non esisteva: due
 * messaggi con lo stesso testo avevano lo stesso hashCode() e la lista andava in
 * crash con "Key was already used" dopo pochi giri di chat.
 */
class TutorMessaggiTest {

    @Test
    fun `due messaggi identici con id diverso non collidono`() {
        val a = MessaggioTutor("Come si dice gatto?", daUtente = true, id = 1L)
        val b = MessaggioTutor("Come si dice gatto?", daUtente = true, id = 2L)

        assertNotEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, b)
    }

    @Test
    fun `lo stesso messaggio resta uguale a se stesso`() {
        val a = MessaggioTutor("Ciao", daUtente = false, id = 7L)
        val b = MessaggioTutor("Ciao", daUtente = false, id = 7L)

        assertNotEquals(0, a.id)
        org.junit.Assert.assertEquals(a, b)
    }
}
