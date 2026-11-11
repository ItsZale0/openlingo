package com.alessandro.openlingo.data

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guardia contro le ripetizioni nei pool statici: due esercizi con lo stesso
 * testo (a meno di maiuscole/punteggiatura) sono un bug, perché lo studente
 * li vede come la stessa domanda.
 */
class PoolSenzaDuplicatiTest {

    private fun duplicati(chiavi: List<String>): List<String> {
        val visti = HashSet<String>()
        return chiavi.filter { chiave ->
            chiave.isNotBlank() && !visti.add(DeduplicaEsercizi.normalizza(chiave))
        }
    }

    @Test
    fun `QuizData comprensione senza domande duplicate`() {
        val dup = duplicati(QuizData.domandeComprensione.map { it.domanda })
        assertTrue("Domande duplicate: $dup", dup.isEmpty())
    }

    @Test
    fun `QuizData produzione senza domande duplicate`() {
        val dup = duplicati(QuizData.domandeProduzione.map { it.domanda })
        assertTrue("Domande duplicate: $dup", dup.isEmpty())
    }

    @Test
    fun `BancaProduttiva senza prompt duplicati per livello e tipo`() {
        // Per dettato e riordino il prompt è una consegna generica: il contenuto
        // vero è la risposta (la frase da scrivere/ricostruire).
        val dup = duplicati(
            BancaProduttiva.tutti.map {
                val contenuto = when (it.tipo) {
                    TipoProduttivo.DETTATO, TipoProduttivo.RIORDINO -> it.risposta
                    else -> it.prompt
                }
                "${it.livello}|${it.tipo}|$contenuto"
            }
        )
        assertTrue("Esercizi duplicati: $dup", dup.isEmpty())
    }

    @Test
    fun `Sprechuebungen senza prompt duplicati per livello`() {
        val dup = duplicati(Sprechuebungen.prompts.map { "${it.livello}|${it.prompt}" })
        assertTrue("Prompt duplicati: $dup", dup.isEmpty())
    }

    @Test
    fun `ogni dialogo ha domande diverse`() {
        Dialoghi.dialoghi.forEach { dialogo ->
            val dup = duplicati(dialogo.domande.map { it.domanda })
            assertTrue("Dialogo ${dialogo.id} con domande duplicate: $dup", dup.isEmpty())
        }
    }

    @Test
    fun `Pronuncia senza coppie minime duplicate`() {
        val dup = duplicati(Pronuncia.coppie.map { "${it.a}|${it.b}" })
        assertTrue("Coppie duplicate: $dup", dup.isEmpty())
    }
}
