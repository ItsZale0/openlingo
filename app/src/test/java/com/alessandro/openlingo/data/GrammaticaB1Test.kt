package com.alessandro.openlingo.data

import org.junit.Assert.*
import org.junit.Test

class GrammaticaB1Test {

    @Test
    fun listaEserciziNonVuota() {
        assertTrue(GrammaticaB1.esercizi.isNotEmpty())
    }

    @Test
    fun ogniEsercizioHaQuattroOpzioni() {
        GrammaticaB1.esercizi.forEach { e ->
            assertEquals(4, e.opzioni.size)
        }
    }

    @Test
    fun rispostaCorrettaValida() {
        GrammaticaB1.esercizi.forEach { e ->
            assertTrue(e.rispostaCorretta in 0..3)
        }
    }

    @Test
    fun eserciziCasualiRestituisceNumeroCorretto() {
        val n = 5
        val casuali = GrammaticaB1.eserciziCasuali(n)
        assertEquals(n, casuali.size)
    }
}
