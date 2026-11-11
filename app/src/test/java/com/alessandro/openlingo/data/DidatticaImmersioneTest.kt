package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test per il dataset della Modalità Immersione: la struttura serve al metodo
 * (storie comprensibili, vocabolario in frasi, composti effettivamente
 * scomponibili), quindi ogni livello deve essere coerente e pulito.
 */
class DidatticaImmersioneTest {

    private val segnali = listOf("TODO", "FIXME", "XXX", "lorem", "placeholder", "TBD")

    private fun <T> perLivelli(blocco: (String) -> T): List<T> =
        DidatticaImmersione.LIVELLI.map { blocco(it) }

    @Test
    fun `perLivello normalizza le etichette`() {
        assertEquals("B1", DidatticaImmersione.perLivello("B1 - Intermedio"))
        assertEquals("A0", DidatticaImmersione.perLivello("a0"))
        assertEquals("B2", DidatticaImmersione.perLivello("B2"))
        assertEquals("A1", DidatticaImmersione.perLivello(null))
        assertEquals("A1", DidatticaImmersione.perLivello("non esiste"))
    }

    @Test
    fun `ogni storia ha frasi con traduzione e senza segnaposto`() {
        perLivelli { livello ->
            val storia = DidatticaImmersione.storiaAttiva(livello)
            assertTrue("Storia $livello: titolo vuoto", storia.titolo.isNotBlank())
            assertTrue("Storia $livello: contesto vuoto", storia.contesto.isNotBlank())
            assertTrue("Storia $livello senza frasi", storia.frasi.isNotEmpty())
            storia.frasi.forEach { frase ->
                assertTrue(
                    "Storia $livello: frase senza tedesco (${frase.tedesco})",
                    frase.tedesco.isNotBlank()
                )
                assertTrue(
                    "Storia $livello: frase senza italiano (${frase.tedesco})",
                    frase.italiano.isNotBlank()
                )
                assertTrue(
                    "Storia $livello: doppio spazio in '${frase.tedesco}'",
                    "  " !in frase.tedesco && "  " !in frase.italiano
                )
            }
        }
    }

    @Test
    fun `le domande hanno tre opzioni e un indice valido`() {
        perLivelli { livello ->
            val domande = DidatticaImmersione.storiaAttiva(livello).domande
            assertTrue("Storia $livello senza domande", domande.isNotEmpty())
            domande.forEach { d ->
                assertEquals("Domanda '${d.domanda}': opzioni != 3", 3, d.opzioni.size)
                assertTrue(
                    "Domanda '${d.domanda}': risposta ${d.rispostaCorretta} fuori range",
                    d.rispostaCorretta in 0 until d.opzioni.size
                )
                assertTrue(
                    "Domanda '${d.domanda}': opzioni vuote o ripetute",
                    d.opzioni.all { it.isNotBlank() } && d.opzioni.distinct().size == d.opzioni.size
                )
            }
        }
    }

    @Test
    fun `frasiNuove restituisce cinque frasi diverse per giro`() {
        perLivelli { livello ->
            val giri = (0..2).map { DidatticaImmersione.frasiNuove(livello, it) }
            giri.forEach { gruppo ->
                assertEquals("Livello $livello: a ogni giro servono 5 frasi", 5, gruppo.size)
            }
            val testi = giri.map { gruppo -> gruppo.joinToString("|") { it.tedesco } }
            assertEquals(
                "Livello $livello: i tre giri devono essere distinti",
                3,
                testi.distinct().size
            )
        }
    }

    @Test
    fun `i composti sono la concatenazione esatta delle parti`() {
        perLivelli { livello ->
            val composti = DidatticaImmersione.composti(livello)
            assertTrue("Livello $livello: nessun composto", composti.isNotEmpty())
            composti.forEach { c ->
                assertEquals(
                    "Livello $livello: '${c.composto}' ≠ parti (${c.parti.joinToString("+")})",
                    c.composto,
                    c.parti.joinToString("")
                )
                assertEquals(
                    "Livello $livello: '${c.composto}' ha più parti che significati",
                    c.parti.size,
                    c.significati.size
                )
                assertTrue("Livello $livello: '${c.composto}' senza traduzione", c.italiano.isNotBlank())
            }
        }
    }

    @Test
    fun `parole nel contesto sono complete e senza segnaposto`() {
        perLivelli { livello ->
            val parole = DidatticaImmersione.paroleNelContesto(livello)
            assertTrue("Livello $livello: nessuna parola", parole.isNotEmpty())
            parole.forEach { p ->
                assertTrue("Parola '${p.parola}' senza definizione", p.significato.isNotBlank())
                assertTrue("Parola '${p.parola}' senza frase", p.frase.isNotBlank())
                assertTrue("Parola '${p.parola}' senza traduzione", p.traduzione.isNotBlank())
            }
        }
    }

    @Test
    fun `shadowing ha nota ritmo e niente segnaposto`() {
        perLivelli { livello ->
            val frasi = DidatticaImmersione.shadowing(livello)
            assertTrue("Livello $livello: nessuna frase di shadowing", frasi.isNotEmpty())
            frasi.forEach { s ->
                assertTrue("Shadowing $livello: nota ritmo vuota", s.notaRitmo.isNotBlank())
                assertTrue("Shadowing $livello: traduzione vuota", s.italiano.isNotBlank())
                segnali.forEach { segnale ->
                    assertTrue(
                        "Shadowing $livello: segnaposto '$segnale' in '${s.tedesco}'",
                        !s.tedesco.contains(segnale, ignoreCase = true)
                    )
                }
            }
        }
    }

    @Test
    fun `frasi passive non sono vuote e senza duplicati interni alla storia`() {
        perLivelli { livello ->
            val frasi = DidatticaImmersione.frasiPassive(livello)
            assertTrue("Livello $livello: ascolto passivo vuoto", frasi.isNotEmpty())
            assertTrue("Livello $livello: frasi passive tutte identiche", frasi.distinct().size >= frasi.size / 2)
        }
    }

    @Test
    fun `nessun segnaposto nel dataset`() {
        val tutte = perLivelli { livello ->
            val storia = DidatticaImmersione.storiaAttiva(livello)
            storia.frasi.map { "${it.tedesco} ${it.italiano}" } +
                storia.domande.map { "${it.domanda} ${it.opzioni.joinToString(" ")}" } +
                DidatticaImmersione.paroleNelContesto(livello)
                    .map { "${it.frase} ${it.traduzione}" }
        }.flatten()
        tutte.forEach { testo ->
            segnali.forEach { segnale ->
                assertTrue(
                    "Segnaposto '$segnale' trovato in: $testo",
                    !testo.contains(segnale, ignoreCase = true)
                )
            }
        }
    }

    @Test
    fun `ogni livello ha almeno due episodi con tre parole pre-insegnate`() {
        perLivelli { livello ->
            val episodi = DidatticaImmersione.storie(livello)
            assertTrue(
                "Livello $livello: servono almeno due episodi (narrow reading)",
                episodi.size >= 2
            )
            episodi.forEach { storia ->
                assertEquals("Storia ${storia.titolo}: livello", livello, storia.livello)
                assertEquals(
                    "Storia ${storia.titolo}: tre parole chiave",
                    3,
                    storia.paroleChiave.size
                )
                assertTrue(
                    "Storia ${storia.titolo}: copertura ${storia.copertura}% fuori scala",
                    storia.copertura in 90..100
                )
                assertTrue(
                    "Storia ${storia.titolo}: minuti non positivi",
                    storia.minuti > 0
                )
                storia.paroleChiave.forEach { g ->
                    assertTrue("Glossa senza parola", g.parola.isNotBlank())
                    assertTrue("Glossa senza significato", g.significato.isNotBlank())
                }
            }
        }
    }

    @Test
    fun `la parola evidenziata esiste davvero nella frase`() {
        perLivelli { livello ->
            DidatticaImmersione.frasiNuove(livello, 0).forEach { frase ->
                assertTrue(
                    "Frase '${frase.tedesco}': parola chiave vuota",
                    frase.parolaChiave.isNotBlank()
                )
                assertTrue(
                    "Frase '${frase.tedesco}': '${frase.parolaChiave}' non è nel testo",
                    frase.tedesco.contains(frase.parolaChiave, ignoreCase = true)
                )
                assertTrue(
                    "Frase '${frase.tedesco}': significato mancante",
                    frase.significatoChiave.isNotBlank()
                )
            }
        }
    }

    @Test
    fun `la rotazione della raccolta resta di cinque frasi`() {
        perLivelli { livello ->
            (0..8).forEach { giro ->
                assertEquals(
                    "Livello $livello, giro $giro: la raccolta deve restare di 5 frasi",
                    5,
                    DidatticaImmersione.frasiNuove(livello, giro).size
                )
            }
        }
    }

    @Test
    fun `ogni episodio rimanda a regole grammaticali esistenti`() {
        val scala = listOf("A0", "A1", "A2", "B1", "B2")
        perLivelli { livello ->
            DidatticaImmersione.storie(livello).forEach { storia ->
                assertTrue(
                    "Storia ${storia.titolo}: nessuna regola collegata",
                    storia.regoleCollegate.isNotEmpty()
                )
                storia.regoleCollegate.forEach { id ->
                    val unita = GrammaticaSyllabus.perId(id)
                    assertTrue(
                        "Storia ${storia.titolo}: l'unità '$id' non esiste nel percorso",
                        unita != null
                    )
                    val distanza = scala.indexOf(unita!!.livello) - scala.indexOf(storia.livello)
                    assertTrue(
                        "Storia ${storia.titolo} (${storia.livello}) rimanda a ${unita.livello}: troppo lontano",
                        distanza in -1..1
                    )
                }
            }
        }
    }

    @Test
    fun `le regole della storia si risolvono in unita reali`() {
        perLivelli { livello ->
            val storia = DidatticaImmersione.storiaAttiva(livello)
            val regole = DidatticaImmersione.regoleDellaStoria(storia)
            assertEquals(
                "Storia ${storia.titolo}: id non risolti",
                storia.regoleCollegate.size,
                regole.size
            )
            regole.forEach { unita ->
                assertTrue("Unità senza titolo", unita.titolo.isNotBlank())
                assertTrue("Unità senza esercizi", unita.esercizi.isNotEmpty())
            }
        }
    }
}
