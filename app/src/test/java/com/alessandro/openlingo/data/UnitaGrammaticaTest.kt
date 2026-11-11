package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Qualità dei banchi di esercizi delle lezioni.
 *
 * Le lezioni non sono più un elenco fisso da imparare a memoria: la sessione
 * ruota. Perché la rotazione abbia senso servono però banchi abbastanza grandi,
 * id unici e nessuna domanda ripetuta.
 */
class UnitaGrammaticaTest {

    private val unita = GrammaticaSyllabus.unita

    @Test
    fun `ci sono unita nel percorso`() {
        assertTrue("Il percorso è vuoto", unita.isNotEmpty())
    }

    @Test
    fun `ogni unita ha almeno 12 esercizi`() {
        val scarse = unita.filter { it.esercizi.size < 12 }
            .map { "${it.id}=${it.esercizi.size}" }
        assertTrue(
            "Unità con banco troppo piccolo per ruotare: $scarse",
            scarse.isEmpty()
        )
    }

    @Test
    fun `gli id degli esercizi sono unici in tutta l'app`() {
        val tutti = unita.flatMap { u -> u.esercizi.map { it.id } }
        val duplicati = tutti.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue("Id esercizio duplicati: $duplicati", duplicati.isEmpty())
    }

    @Test
    fun `nessuna domanda ripetuta dentro una unita`() {
        unita.forEach { u ->
            val chiavi = u.esercizi.map { DeduplicaEsercizi.normalizza(it.domanda) }
            val duplicati = chiavi.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
            assertTrue("Unità ${u.id} con domande duplicate: $duplicati", duplicati.isEmpty())
        }
    }

    @Test
    fun `nessun esercizio segnaposto generico`() {
        val segnaposto = unita.flatMap { u ->
            u.esercizi.filter {
                it.domanda.contains("questa struttura grammaticale", ignoreCase = true) ||
                    it.rispostaTesto == "Ich habe das gemacht."
            }.map { "${u.id}/${it.id}" }
        }
        assertTrue("Esercizi segnaposto rimasti: $segnaposto", segnaposto.isEmpty())
    }

    @Test
    fun `ogni unita ha almeno un esercizio di produzione`() {
        val senza = unita.filter { u ->
            u.esercizi.none { it.tipo == TipoEsercizioGrammatica.PRODUZIONE }
        }.map { it.id }
        assertTrue("Unità senza produzione: $senza", senza.isEmpty())
    }
}
