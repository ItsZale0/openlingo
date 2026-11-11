package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Le liste storiche del profilo non devono crescere all'infinito: vengono
 * riscritte per intero a ogni salvataggio e serializzate a ogni backup.
 */
class StoricoTest {

    @Test
    fun `sotto il tetto la lista resta intatta`() {
        val lista = (1..10).toList()
        assertEquals(lista, lista.limita())
    }

    @Test
    fun `al tetto esatto non taglia nulla`() {
        val lista = (1..MAX_STORICO).toList()
        assertEquals(MAX_STORICO, lista.limita().size)
    }

    @Test
    fun `oltre il tetto tiene solo le voci piu recenti`() {
        val lista = (1..(MAX_STORICO + 250)).toList()
        val limitata = lista.limita()

        assertEquals(MAX_STORICO, limitata.size)
        assertEquals(251, limitata.first())
        assertEquals(MAX_STORICO + 250, limitata.last())
    }

    @Test
    fun `una lista vuota resta vuota`() {
        assertEquals(emptyList<Int>(), emptyList<Int>().limita())
    }
}
