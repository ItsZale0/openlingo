package com.alessandro.openlingo.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    // ── Ambiguità dei nomi deboli (N-Deklination) ──

    @Test
    fun `parse scarta l'esercizio ambiguo su Patienten`() {
        // Caso reale: sia "euren Patienten" (singolare) sia "eure Patienten"
        // (plurale) sono corrette, ma la chiave ne dichiarava una sola.
        val ambiguo = "{\"domanda\":\"Wir besuchen ___ Patienten im Zimmer.\"," +
            "\"opzioni\":[\"euer\",\"eurer\",\"euren\",\"eure\"]," +
            "\"rispostaCorretta\":3,\"spiegazione\":\"plurale\"," +
            "\"livello\":\"A2\"}"
        val valido = "{\"domanda\":\"Ich sehe ___ Hund.\"," +
            "\"opzioni\":[\"einen\",\"eine\",\"ein\",\"einem\"]," +
            "\"rispostaCorretta\":0,\"spiegazione\":\"accusativo\"," +
            "\"livello\":\"A2\"}"
        val risultato = generatore.parseEsercizi(
            json(ambiguo, valido), TipoEsercizio.GRAMMATICA, "A2"
        )
        assertEquals(1, risultato.size)
        assertEquals("Ich sehe ___ Hund.", risultato[0].domanda)
    }

    // ── Revisore: la decisione finale è deterministica ──

    private fun generato(id: String, risposta: Int) = EsercizioGenerato(
        id = id,
        tipo = TipoEsercizio.GRAMMATICA,
        domanda = "Wir besuchen ___ Patienten.",
        opzioni = listOf("a", "b", "c", "d"),
        rispostaCorretta = risposta,
        spiegazione = "spiegazione",
        livello = "A2"
    )

    @Test
    fun `la revisione tiene solo gli esercizi con una sola risposta corretta`() {
        val esercizi = listOf(generato("ok", 2), generato("ambiguo", 1))
        val filtrati = generatore.applicaRevisione(
            esercizi,
            mapOf(0 to listOf(2), 1 to listOf(1, 3))
        )
        assertEquals(listOf("ok"), filtrati.map { it.id })
    }

    @Test
    fun `la revisione scarta una chiave diversa dall'unica corretta`() {
        val filtrati = generatore.applicaRevisione(
            listOf(generato("chiave-sbagliata", 0)),
            mapOf(0 to listOf(2))
        )
        assertTrue(filtrati.isEmpty())
    }

    @Test
    fun `la revisione scarta gli esercizi senza risposta corretta`() {
        val filtrati = generatore.applicaRevisione(
            listOf(generato("vuoto", 0)),
            mapOf(0 to emptyList())
        )
        assertTrue(filtrati.isEmpty())
    }

    @Test
    fun `la revisione lascia stare gli esercizi senza verdetto`() {
        val esercizi = listOf(generato("uno", 0), generato("due", 1))
        val filtrati = generatore.applicaRevisione(esercizi, mapOf(0 to listOf(0)))
        assertEquals(listOf("uno", "due"), filtrati.map { it.id })
    }

    @Test
    fun `le valutazioni del revisore vengono lette`() {
        val risposta =
            """{"valutazioni":[{"indice":0,"corrette":[2]},{"indice":1,"corrette":[0,3]}]}"""
        val valutazioni = generatore.valutazioniDaRisposta(risposta, 2)
        assertEquals(listOf(2), valutazioni[0])
        assertEquals(listOf(0, 3), valutazioni[1])
    }

    @Test
    fun `le valutazioni fuori range vengono ignorate`() {
        val risposta =
            """{"valutazioni":[{"indice":5,"corrette":[1]},{"indice":0,"corrette":[1,7]}]}"""
        assertTrue(generatore.valutazioniDaRisposta(risposta, 2).isEmpty())
    }
}
