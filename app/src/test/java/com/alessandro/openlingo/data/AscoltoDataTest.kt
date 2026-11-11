package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test per AscoltoData: verifica che gli esercizi di ascolto siano validi.
 */
class AscoltoDataTest {

    @Test
    fun `esercizi non sono vuoti`() {
        assertTrue("AscoltoData.esercizi non deve essere vuoto", AscoltoData.esercizi.isNotEmpty())
    }

    @Test
    fun `ogni esercizio ha 4 opzioni`() {
        AscoltoData.esercizi.forEach { esercizio ->
            assertEquals(
                "Esercizio ${esercizio.id} deve avere 4 opzioni",
                4,
                esercizio.opzioni.size
            )
        }
    }

    @Test
    fun `risposta corretta è valida`() {
        AscoltoData.esercizi.forEach { esercizio ->
            assertTrue(
                "Esercizio ${esercizio.id}: risposta corretta ${esercizio.rispostaCorretta} fuori range",
                esercizio.rispostaCorretta in 0 until esercizio.opzioni.size
            )
        }
    }

    @Test
    fun `frase tedesca non è vuota`() {
        AscoltoData.esercizi.forEach { esercizio ->
            assertTrue(
                "Esercizio ${esercizio.id}: frase tedesca vuota",
                esercizio.fraseTedesca.isNotBlank()
            )
        }
    }

    @Test
    fun `traduzione italiana non è vuota`() {
        AscoltoData.esercizi.forEach { esercizio ->
            assertTrue(
                "Esercizio ${esercizio.id}: traduzione italiana vuota",
                esercizio.traduzioneItaliana.isNotBlank()
            )
        }
    }

    @Test
    fun `domanda non è vuota`() {
        AscoltoData.esercizi.forEach { esercizio ->
            assertTrue(
                "Esercizio ${esercizio.id}: domanda vuota",
                esercizio.domanda.isNotBlank()
            )
        }
    }

    @Test
    fun `spiegazione non è vuota`() {
        AscoltoData.esercizi.forEach { esercizio ->
            assertTrue(
                "Esercizio ${esercizio.id}: spiegazione vuota",
                esercizio.spiegazione.isNotBlank()
            )
        }
    }

    @Test
    fun `livelli disponibili non sono vuoti`() {
        assertTrue(
            "AscoltoData.livelliDisponibili() non deve essere vuoto",
            AscoltoData.livelliDisponibili().isNotEmpty()
        )
    }

    @Test
    fun `eserciziPerLivello restituisce solo esercizi del livello richiesto`() {
        AscoltoData.livelliDisponibili().forEach { livello ->
            val esercizi = AscoltoData.eserciziPerLivello(livello)
            assertTrue(
                "Nessun esercizio per livello $livello",
                esercizi.isNotEmpty()
            )
            esercizi.forEach { esercizio ->
                assertEquals(
                    "Esercizio ${esercizio.id} ha livello ${esercizio.livello} ma è stato restituito per $livello",
                    livello,
                    esercizio.livello
                )
            }
        }
    }

    @Test
    fun `eserciziPerLivello con livello inesistente restituisce lista vuota`() {
        val esercizi = AscoltoData.eserciziPerLivello("Z9")
        assertEquals(0, esercizi.size)
    }

    @Test
    fun `ogni livello ha almeno 5 esercizi`() {
        AscoltoData.livelliDisponibili().forEach { livello ->
            val esercizi = AscoltoData.eserciziPerLivello(livello)
            assertTrue(
                "Livello $livello ha solo ${esercizi.size} esercizi, ce ne devono essere almeno 5",
                esercizi.size >= 5
            )
        }
    }

    @Test
    fun `ids sono unici`() {
        val ids = AscoltoData.esercizi.map { it.id }
        assertEquals(
            "Gli IDs degli esercizi devono essere unici",
            ids.size,
            ids.toSet().size
        )
    }

    @Test
    fun `livelli sono A1 A2 o B1`() {
        AscoltoData.livelliDisponibili().forEach { livello ->
            assertTrue(
                "Livello $livello non è valido (attesi: A0-A2, B1)",
                livello in listOf("A0", "A1", "A2", "B1")
            )
        }
    }

    // --- Lista realmente usata dall'app (A0 profile-aware + A1/A2/B1) ---

    @Test
    fun `lista runtime ha id unici`() {
        val tutti = getTuttiEserciziAscolto(null)
        assertEquals(
            "La lista runtime contiene id duplicati (le statistiche si confondono)",
            tutti.size,
            tutti.map { it.id }.toSet().size
        )
    }

    @Test
    fun `lista runtime ha sempre 4 opzioni`() {
        getTuttiEserciziAscolto(null).forEach { esercizio ->
            assertEquals(
                "Esercizio ${esercizio.id} deve avere 4 opzioni",
                4,
                esercizio.opzioni.size
            )
        }
    }

    @Test
    fun `ogni livello della lista runtime ha almeno 5 esercizi`() {
        listOf("A0", "A1", "A2", "B1").forEach { livello ->
            val esercizi = AscoltoData.eserciziPerLivello(livello)
            assertTrue(
                "Livello $livello ha solo ${esercizi.size} esercizi",
                esercizi.size >= 5
            )
        }
    }

    @Test
    fun `nessuna frase ripetuta nello stesso livello`() {
        AscoltoData.livelliDisponibili().forEach { livello ->
            val frasi = AscoltoData.eserciziPerLivello(livello)
                .map { DeduplicaEsercizi.normalizza(it.fraseTedesca) }
            assertEquals(
                "Frasi duplicate nel livello $livello",
                frasi.size,
                frasi.toSet().size
            )
        }
    }
}
