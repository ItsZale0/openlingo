package com.alessandro.openlingo.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Il livello e' lungo ("A0 — Principiante assoluto") ma le parole usano codici
 * corti ("A0"). Confrontarli direttamente faceva filtrare lista vuota: da qui in
 * poi si passa sempre da [LivelloCEFR.daTesto] e [LivelloCEFR.codiceBreve].
 */
class LivelloCodiceTest {

    @Test
    fun `un label completo si risolve al livello giusto`() {
        assertEquals(LivelloCEFR.A0, LivelloCEFR.daTesto("A0 — Principiante assoluto"))
        assertEquals(LivelloCEFR.A1, LivelloCEFR.daTesto("A1 — Elementare"))
        assertEquals(LivelloCEFR.B1, LivelloCEFR.daTesto("B1 — Intermedio"))
        assertEquals(LivelloCEFR.B2, LivelloCEFR.daTesto("B2 — Avanzato"))
    }

    @Test
    fun `un codice breve si risolve anche minuscolo`() {
        assertEquals(LivelloCEFR.A0, LivelloCEFR.daTesto("A0"))
        assertEquals(LivelloCEFR.B1, LivelloCEFR.daTesto("b1"))
        assertEquals(LivelloCEFR.C2, LivelloCEFR.daTesto("C2"))
    }

    @Test
    fun `testo vuoto o sconosciuto torna a A0`() {
        assertEquals(LivelloCEFR.A0, LivelloCEFR.daTesto(""))
        assertEquals(LivelloCEFR.A0, LivelloCEFR.daTesto(null))
        assertEquals(LivelloCEFR.A0, LivelloCEFR.daTesto("Z9 — Inventato"))
    }

    @Test
    fun `codiceBreve e' il codice usato dalle parole`() {
        assertEquals("A0", LivelloCEFR.A0.codiceBreve())
        assertEquals("A1", LivelloCEFR.A1.codiceBreve())
        assertEquals("B1", LivelloCEFR.B1.codiceBreve())
        assertEquals("B2", LivelloCEFR.B2.codiceBreve())
    }

    @Test
    fun `il filtro esercizi non supera mai il livello dell utente`() {
        // stessa regola usata in OpenLingoViewModel.generaEsercizi
        val livelloUtente = LivelloCEFR.B1
        val generati = listOf("A0", "A1", "A2", "B1", "B2", "C1").map { EsercizioDiProva(it) }
        val ammessi = generati.filter { LivelloCEFR.daTesto(it.livello).ordine <= livelloUtente.ordine }
        assertEquals(listOf("A0", "A1", "A2", "B1"), ammessi.map { it.livello })
    }

    private data class EsercizioDiProva(val livello: String)
}
