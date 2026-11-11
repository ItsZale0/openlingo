package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.EsercizioGenEntity
import com.alessandro.openlingo.data.local.toDomandaTest
import com.alessandro.openlingo.data.local.toEsercizioAscolto
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regressione della famiglia di crash "lista vuota":
 * le sezioni esercizi indicizzavano la lista (elenco[indice]) dopo aver marcato
 * "caricato = true" anche quando la generazione non aveva prodotto nulla, e
 * convertivano entita' con opzioni vuote. Qui si blinda la conversione.
 */
class EserciziVuotiTest {

    private fun vuoto() = EsercizioGenEntity(
        id = "x",
        tipo = "produzione",
        domanda = "?",
        opzioni = emptyList(),
        rispostaCorretta = 0,
        spiegazione = "",
        livello = "A0"
    )

    @Test
    fun `conversione in DomandaTest senza opzioni non lancia`() {
        val d = vuoto().toDomandaTest()
        assertEquals(0, d.rispostaCorretta)
        assertEquals(0, d.opzioni.size)
    }

    @Test
    fun `conversione in EsercizioAscolto senza opzioni non lancia`() {
        val a = vuoto().toEsercizioAscolto()
        assertEquals(0, a.rispostaCorretta)
    }

    @Test
    fun `rispostaCorretta fuori range viene limitata`() {
        val e = vuoto().copy(opzioni = listOf("a", "b"), rispostaCorretta = 9)
        assertEquals(1, e.toDomandaTest().rispostaCorretta)
    }
}
