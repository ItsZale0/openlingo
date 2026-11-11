package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test dei banchi EserciziExtra: ogni chiave deve corrispondere a una unità
 * del percorso, gli id degli esercizi devono essere unici su tutta la lista
 * e gli esercizi a scelta devono avere indici coerenti.
 */
class EserciziExtraTest {

    @Test
    fun `ogni chiave del banco corrisponde a una unita del percorso`() {
        val idUnita = GrammaticaSyllabus.unita.map { it.id }.toSet()
        val chiaviSconosciute = EserciziExtra.keys - idUnita
        assertTrue("Chiavi senza unità: $chiaviSconosciute", chiaviSconosciute.isEmpty())
    }

    @Test
    fun `gli id degli esercizi extra non collidono con nessun altro id`() {
        val extraIds = EserciziExtra.values.flatten().map { it.id }
        val coreIds = GrammaticaSyllabus.unita.flatMap { it.esercizi }.map { it.id }.toSet() -
            extraIds.toSet()
        val doppi = extraIds.groupBy { it }.filterValues { it.size > 1 }.keys
        val collisioni = extraIds.toSet() intersect coreIds
        assertTrue("Id extra duplicati: $doppi", doppi.isEmpty())
        assertTrue("Id extra che collidono con quelli esistenti: $collisioni", collisioni.isEmpty())
    }

    @Test
    fun `i banchi coprono le 26 unita mappate sul repertorio`() {
        assertEquals(26, EserciziExtra.size)
    }

    @Test
    fun `ogni banco ha almeno cinque esercizi`() {
        EserciziExtra.forEach { (id, esercizi) ->
            assertTrue("Il banco $id ha solo ${esercizi.size} esercizi", esercizi.size >= 5)
        }
    }

    @Test
    fun `gli esercizi a scelta hanno quattro opzioni e indice valido`() {
        GrammaticaSyllabus.unita.flatMap { it.esercizi }.forEach { e ->
            if (e.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                assertEquals("4 opzioni (${e.id})", 4, e.opzioni.size)
                assertTrue("Indice corretta in range (${e.id})", e.rispostaCorretta in 0 until e.opzioni.size)
            }
        }
    }

    @Test
    fun `le risposte a testo non sono vuote`() {
        GrammaticaSyllabus.unita.flatMap { it.esercizi }.forEach { e ->
            if (e.tipo != TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                assertTrue("Risposta vuota (${e.id})", e.rispostaTesto.isNotBlank())
            }
        }
    }

    @Test
    fun `le unita con banco contengono gli esercizi extra`() {
        val a0Plurale = GrammaticaSyllabus.perId("a0-plurale")
        assertTrue(a0Plurale != null)
        val extraIds = EserciziExtra.getValue("a0-plurale").map { it.id }.toSet()
        val effettivi = a0Plurale!!.esercizi.map { it.id }.toSet()
        assertTrue("Il banco extra deve essere dentro l'unità", effettivi.containsAll(extraIds))
    }
}
