package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test del helper di deduplica per contenuto (usato da AI e schermate). */
class DeduplicaEserciziTest {

    private data class Voce(val id: String, val domanda: String, val extra: String? = null)

    @Test
    fun `normalizza ignora maiuscole umlaut punteggiatura e spazi`() {
        assertEquals(
            DeduplicaEsercizi.normalizza("Come si dice: 'schön'?"),
            DeduplicaEsercizi.normalizza("  come si dice   SCHOEN  ")
        )
    }

    @Test
    fun `distinti tiene la prima occorrenza`() {
        val voci = listOf(
            Voce("1", "Was bedeutet 'der Tisch'?"),
            Voce("2", "was bedeutet der tisch"),
            Voce("3", "Was bedeutet 'die Küche'?")
        )
        val uniche = DeduplicaEsercizi.distinti(voci) { DeduplicaEsercizi.chiave(it.domanda) }
        assertEquals(listOf("1", "3"), uniche.map { it.id })
    }

    @Test
    fun `senzaGiaVisti scarta le domande presenti nello storico`() {
        val voci = listOf(
            Voce("1", "Come si dice 'ciao'?"),
            Voce("2", "Come si dice 'grazie'?")
        )
        val filtrate = DeduplicaEsercizi.senzaGiaVisti(
            voci,
            listOf("come si dice ciao"),
            { DeduplicaEsercizi.chiave(it.domanda) }
        )
        assertEquals(listOf("2"), filtrate.map { it.id })
    }

    @Test
    fun `la frase ascoltata distingue domande generiche uguali`() {
        val voci = listOf(
            Voce("1", "Cosa dice il parlante?", "Guten Morgen!"),
            Voce("2", "Cosa dice il parlante?", "Danke schön!"),
            Voce("3", "cosa dice il parlante", "guten morgen")
        )
        val uniche = DeduplicaEsercizi.distinti(voci) {
            DeduplicaEsercizi.chiave(it.domanda, it.extra)
        }
        assertEquals(listOf("1", "2"), uniche.map { it.id })
    }

    @Test
    fun `unisciTenendoUltimi tiene l'ultima occorrenza e rispetta il cap`() {
        val esistenti = listOf(
            Voce("vecchio", "Domanda A"),
            Voce("b", "Domanda B")
        )
        val nuovi = listOf(
            Voce("nuovo", "domanda a"),
            Voce("c", "Domanda C")
        )
        val uniti = DeduplicaEsercizi.unisciTenendoUltimi(esistenti, nuovi, 10) {
            DeduplicaEsercizi.chiave(it.domanda)
        }
        // Ordine per ultima occorrenza: B, poi la A aggiornata, poi C.
        assertEquals(listOf("b", "nuovo", "c"), uniti.map { it.id })

        val limitati = DeduplicaEsercizi.unisciTenendoUltimi(esistenti, nuovi, 2) {
            DeduplicaEsercizi.chiave(it.domanda)
        }
        assertEquals(listOf("nuovo", "c"), limitati.map { it.id })
    }

    @Test
    fun `una domanda vuota non viene considerata una chiave valida`() {
        val uniche = DeduplicaEsercizi.distinti(listOf(Voce("1", ""), Voce("2", ""))) {
            DeduplicaEsercizi.chiave(it.domanda)
        }
        assertTrue(uniche.isEmpty())
    }
}
