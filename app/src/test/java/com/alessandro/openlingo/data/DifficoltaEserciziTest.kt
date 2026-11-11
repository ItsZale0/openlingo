package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La difficoltà serve a ordinare le sessioni e a limitare i picchi: se il
 * punteggio non cresce con il lavoro richiesto, l'ordinamento è rumore.
 */
class DifficoltaEserciziTest {

    private fun esercizio(
        tipo: TipoEsercizioGrammatica,
        domanda: String,
        rispostaTesto: String = "antwort",
        opzioni: List<String> = emptyList()
    ) = EsercizioUnita(
        id = "test",
        tipo = tipo,
        domanda = domanda,
        opzioni = opzioni,
        rispostaCorretta = 0,
        rispostaTesto = rispostaTesto
    )

    @Test
    fun `il tipo pesa in ordine crescente`() {
        val domanda = "Kurze Frage?"
        val punteggi = TipoEsercizioGrammatica.entries.map { tipo ->
            DifficoltaEsercizi.punteggio(
                esercizio(
                    tipo = tipo,
                    domanda = domanda,
                    rispostaTesto = "wort",
                    opzioni = listOf("wort", "anderes")
                )
            )
        }
        assertEquals(
            "La scala deve crescere da scelta multipla a produzione: $punteggi",
            punteggi.sorted(),
            punteggi
        )
        assertTrue(punteggi.toSet().size > 1)
    }

    @Test
    fun `una risposta lunga alza il punteggio`() {
        val unaParola = esercizio(
            TipoEsercizioGrammatica.COMPLETAMENTO,
            "Kurze Frage?",
            rispostaTesto = "Wort"
        )
        val frase = unaParola.copy(rispostaTesto = "Ich schreibe einen ganzen Satz.")
        assertTrue(
            DifficoltaEsercizi.punteggio(frase) > DifficoltaEsercizi.punteggio(unaParola)
        )
    }

    @Test
    fun `il punteggio resta sempre tra uno e sei`() {
        GrammaticaSyllabus.unita.forEach { unita ->
            unita.esercizi.forEach { e ->
                val p = DifficoltaEsercizi.punteggio(e)
                assertTrue(
                    "${unita.id}/${e.id}: punteggio $p fuori scala",
                    p in DifficoltaEsercizi.MINIMA..DifficoltaEsercizi.MASSIMA
                )
            }
        }
    }

    @Test
    fun `la produzione e sempre un picco`() {
        TipoEsercizioGrammatica.entries.forEach { tipo ->
            val e = esercizio(tipo, "Kurze Frage?", rispostaTesto = "wort")
            if (tipo == TipoEsercizioGrammatica.PRODUZIONE) {
                assertTrue("${e.tipo} deve essere un picco", DifficoltaEsercizi.ePicco(e))
            }
        }
    }

    @Test
    fun `la risposta di una scelta multipla e l'opzione corretta`() {
        val e = EsercizioUnita(
            id = "test",
            tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
            domanda = "___?",
            opzioni = listOf("a", "b", "c"),
            rispostaCorretta = 2
        )
        assertEquals("c", DifficoltaEsercizi.risposta(e))
    }
}
