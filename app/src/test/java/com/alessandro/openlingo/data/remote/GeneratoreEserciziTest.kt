package com.alessandro.openlingo.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regressione: il modello può restituire la stessa domanda più volte nello
 * stesso batch (id diversi, testo uguale) e può ignorare il numero richiesto.
 * `parseEsercizi` deve deduplicare, filtrare lo storico e troncare a `quanti`.
 */
class GeneratoreEserciziTest {

    private val generatore = GeneratoreEsercizi()

    private fun json(vararg esercizi: String): String =
        """{"esercizi":[${esercizi.joinToString(",")}]}"""

    private fun esercizio(domanda: String, livello: String = "A1"): String =
        "{\"domanda\":\"$domanda\"," +
            "\"opzioni\":[\"a\",\"b\",\"c\",\"d\"]," +
            "\"rispostaCorretta\":0," +
            "\"spiegazione\":\"spiegazione in italiano\"," +
            "\"livello\":\"$livello\"}"

    private fun ascolto(domanda: String, frase: String): String =
        "{\"domanda\":\"$domanda\"," +
            "\"fraseTedesca\":\"$frase\"," +
            "\"traduzioneItaliana\":\"traduzione\"," +
            "\"opzioni\":[\"a\",\"b\",\"c\",\"d\"]," +
            "\"rispostaCorretta\":0," +
            "\"spiegazione\":\"spiegazione in italiano\"," +
            "\"livello\":\"A1\"}"

    @Test
    fun `scarta le domande duplicate nel batch`() {
        val risposta = json(
            esercizio("Come si dice 'ciao'?"),
            esercizio("come si dice ciao"),
            esercizio("Come si dice 'grazie'?")
        )
        val risultato = generatore.parseEsercizi(risposta, TipoEsercizio.PRODUZIONE, "A1")
        assertEquals(2, risultato.size)
    }

    @Test
    fun `tronca al numero richiesto anche se il modello ne manda di piu`() {
        val risposta = json(
            esercizio("Domanda uno"),
            esercizio("Domanda due"),
            esercizio("Domanda tre")
        )
        val risultato = generatore.parseEsercizi(
            risposta, TipoEsercizio.PRODUZIONE, "A1", quanti = 2
        )
        assertEquals(2, risultato.size)
    }

    @Test
    fun `i duplicati non consumano i posti disponibili`() {
        val risposta = json(
            esercizio("Domanda uno"),
            esercizio("domanda uno"),
            esercizio("Domanda due")
        )
        val risultato = generatore.parseEsercizi(
            risposta, TipoEsercizio.PRODUZIONE, "A1", quanti = 2
        )
        assertEquals(2, risultato.size)
        assertEquals("Domanda due", risultato[1].domanda)
    }

    @Test
    fun `ascolto - stessa domanda su frasi diverse resta valida`() {
        val risposta = json(
            ascolto("Cosa dice il parlante?", "Guten Morgen!"),
            ascolto("Cosa dice il parlante?", "Danke schön!")
        )
        val risultato = generatore.parseEsercizi(risposta, TipoEsercizio.ASCOLTO, "A1")
        assertEquals(2, risultato.size)
    }

    @Test
    fun `ascolto - stessa domanda sulla stessa frase viene deduplicata`() {
        val risposta = json(
            ascolto("Cosa dice il parlante?", "Guten Morgen!"),
            ascolto("cosa dice il parlante", "guten morgen")
        )
        val risultato = generatore.parseEsercizi(risposta, TipoEsercizio.ASCOLTO, "A1")
        assertEquals(1, risultato.size)
    }
}
