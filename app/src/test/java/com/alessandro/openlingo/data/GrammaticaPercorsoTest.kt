package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.ProfiloStato
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class GrammaticaPercorsoTest {

    private val giorno = TimeUnit.DAYS.toMillis(1)
    private val adesso = 1_700_000_000_000L

    @Test
    fun ilPercorsoContieneTutteLeUnitaA0B2() {
        assertEquals(56, GrammaticaSyllabus.unita.size)
        assertEquals(
            listOf("A0", "A1", "A2", "B1", "B2"),
            GrammaticaSyllabus.unita.map { it.livello }.distinct()
        )
        assertEquals(
            "ID duplicati nel percorso",
            GrammaticaSyllabus.unita.size,
            GrammaticaSyllabus.unita.map { it.id }.distinct().size
        )
    }

    @Test
    fun leUnitaSonoInOrdineDidattico() {
        assertEquals(
            listOf(
                "a0-alfabeto",
                "a0-pronomi-sein",
                "a0-articoli",
                "a0-plurale",
                "a0-frase",
                "a1-praesens",
                "a1-akkusativ",
                "a1-possessivi",
                "a1-modali",
                "a1-separabili",
                "a1-preposizioni-luogo",
                "a1-preposizioni-tempo",
                "a1-imperativo",
                "a1-fragewoerter",
                "a2-perfekt",
                "a2-dativo",
                "a2-wechselpraepositionen",
                "a2-praeteritum",
                "a2-riflessivi",
                "a2-subordinate",
                "a2-relative",
                "a2-passivo",
                "a2-komparativ",
                "a2-adjektivdeklination",
                "a2-konjunktiv2",
                "a2-verben-praepositionen",
                "a2-indefinitpronomen",
                "a2-indirekte-fragen",
                "a2-verben-akk-dativ",
                "b1-futur",
                "b1-adjektivdeklination",
                "b1-partizipien",
                "b1-konnektoren",
                "b1-doppelkonnektoren",
                "b1-konjunktiv2",
                "b1-indirekte-rede",
                "b1-nominalisierung",
                "b1-genitiv",
                "b1-plusquamperfekt",
                "b1-n-deklination",
                "b1-genitiv-praepositionen",
                "b1-adjektive-als-nomen",
                "b1-praepositionaladverbien",
                "b1-komparativsaetze",
                "b1-passiv-vergangenheit",
                "b1-temporale-nebensaetze",
                "b2-passiv-modal",
                "b2-konjunktiv2-vergangenheit",
                "b2-partizipialkonstruktionen",
                "b2-verben-praepositionen",
                "b2-relative-was-wo",
                "b2-nominalstil",
                "b2-passiv-ersatz",
                "b2-zustandspassiv",
                "b2-modalpartikeln",
                "b2-referenzwoerter"
            ),
            GrammaticaSyllabus.unita.map { it.id }
        )
    }

    @Test
    fun ogniUnitaHaTeoriaEdEsercizi() {
        GrammaticaSyllabus.unita.forEach { unita ->
            assertTrue("Teoria mancante in ${unita.id}", unita.teoria.regola.size >= 3)
            assertTrue("Esempi mancanti in ${unita.id}", unita.teoria.esempi.size >= 3)
            assertTrue("Tabelle mancanti in ${unita.id}", unita.teoria.tabelle.isNotEmpty())
            assertTrue("Errori tipici mancanti in ${unita.id}", unita.teoria.erroriTipici.isNotEmpty())
            assertTrue("Merksatz mancante in ${unita.id}", unita.teoria.merksatz.isNotBlank())
            // Le nuove unità aggiunte (gap audit) sono più compatte: 5 esercizi bastano.
            val minimo = 5
            assertTrue("Esercizi insufficienti in ${unita.id}", unita.esercizi.size >= minimo)
            assertEquals(
                "ID esercizi duplicati in ${unita.id}",
                unita.esercizi.size,
                unita.esercizi.map { it.id }.distinct().size
            )
        }
    }

    @Test
    fun ogniUnitaHaUnaProduzioneFinale() {
        GrammaticaSyllabus.unita.forEach { unita ->
            assertTrue(
                "Produzione mancante in ${unita.id}",
                unita.esercizi.any { it.tipo == TipoEsercizioGrammatica.PRODUZIONE }
            )
        }
    }

    @Test
    fun ogniEsercizioHaUnaRispostaValida() {
        GrammaticaSyllabus.unita.forEach { unita ->
            unita.esercizi.forEach { esercizio ->
                when (esercizio.tipo) {
                    TipoEsercizioGrammatica.SCELTA_MULTIPLA -> {
                        assertTrue(
                            "Opzioni mancanti in ${esercizio.id}",
                            esercizio.opzioni.size >= 2
                        )
                        assertEquals(
                            "Opzioni duplicate in ${esercizio.id}",
                            esercizio.opzioni.size,
                            esercizio.opzioni.distinct().size
                        )
                        assertTrue(
                            "Risposta fuori range in ${esercizio.id}",
                            esercizio.rispostaCorretta in esercizio.opzioni.indices
                        )
                    }

                    else -> {
                        assertTrue(
                            "Risposta testuale mancante in ${esercizio.id}",
                            esercizio.rispostaTesto.isNotBlank()
                        )
                    }
                }
                assertTrue(
                    "Spiegazione mancante in ${esercizio.id}",
                    esercizio.spiegazione.isNotBlank()
                )
            }
        }
    }

    @Test
    fun iPrerequisitiEsistono() {
        val ids = GrammaticaSyllabus.unita.map { it.id }.toSet()
        GrammaticaSyllabus.unita.forEach { unita ->
            unita.prerequisiti.forEach { prerequisito ->
                assertTrue(
                    "Prerequisito inesistente ${prerequisito} in ${unita.id}",
                    prerequisito in ids
                )
            }
        }
    }

    @Test
    fun ilPercorsoVuotoHaUnaSolaUnitaDisponibile() {
        val percorso = GrammaticaSyllabus.percorso(emptyMap(), adesso)
        assertEquals(GrammaticaSyllabus.unita.size, percorso.size)
        assertEquals("a0-alfabeto", percorso.first().unita.id)
        assertEquals(StatoUnita.DISPONIBILE, percorso.first().stato)
        assertTrue(
            percorso.drop(1).all { it.stato == StatoUnita.BLOCCATA }
        )
    }

    @Test
    fun completareLaPrimaUnitaSbloccaLaSeconda() {
        val prima = GrammaticaSyllabus.valuta(
            unitaId = "a0-alfabeto",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val mappa = mapOf("a0-alfabeto" to prima)
        val percorso = GrammaticaSyllabus.percorso(mappa, adesso)
        assertEquals(StatoUnita.COMPLETATA, percorso.first { it.unita.id == "a0-alfabeto" }.stato)
        assertEquals(
            StatoUnita.DISPONIBILE,
            percorso.first { it.unita.id == "a0-pronomi-sein" }.stato
        )
        assertEquals(
            StatoUnita.BLOCCATA,
            percorso.first { it.unita.id == "a0-articoli" }.stato
        )
    }

    @Test
    fun laSogliaEPercentoNonPerNumeroAssoluto() {
        val sottoSoglia = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 9,
            totali = 12,
            now = adesso
        )
        assertFalse(sottoSoglia.completata)
        assertEquals(75, sottoSoglia.percentuale)
        assertEquals(0, sottoSoglia.ripassi)

        val sopraSoglia = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        assertTrue(sopraSoglia.completata)
        assertEquals(83, sopraSoglia.percentuale)
        assertEquals(1, sopraSoglia.ripassi)
        assertEquals(adesso + giorno, sopraSoglia.prossimaRipasso)
    }

    @Test
    fun gliIntervalliDiRipassoSonoUnoTreSetteSedici() {
        var padronanza: PadronanzaGrammatica? = null
        val attesi = listOf(1, 3, 7, 16)

        attesi.forEach { giorni ->
            padronanza = GrammaticaSyllabus.valuta(
                unitaId = "a1-akkusativ",
                corrette = 10,
                totali = 12,
                precedente = padronanza,
                now = adesso
            )
            assertEquals(
                "Intervallo errato al ripasso ${padronanza!!.ripassi}",
                adesso + giorni * giorno,
                padronanza!!.prossimaRipasso
            )
        }
    }

    @Test
    fun unRipassoFallitoNonRichiedeLaUnitaGiaCompletata() {
        val completata = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val fallita = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 6,
            totali = 12,
            precedente = completata,
            now = adesso + 2 * giorno
        )
        assertTrue(fallita.completata)
        assertEquals(50, fallita.percentuale)
        assertEquals(adesso + 3 * giorno, fallita.prossimaRipasso)
    }

    @Test
    fun unaUnitaCompletataEDaRipassareQuandoScade() {
        val completata = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val mappa = mapOf("a1-akkusativ" to completata)

        val primaDellaScadenza = GrammaticaSyllabus.percorso(mappa, adesso + giorno / 2)
        assertEquals(
            StatoUnita.COMPLETATA,
            primaDellaScadenza.first { it.unita.id == "a1-akkusativ" }.stato
        )

        val dopoLaScadenza = GrammaticaSyllabus.percorso(mappa, adesso + 2 * giorno)
        assertEquals(
            StatoUnita.DA_RIPASSARE,
            dopoLaScadenza.first { it.unita.id == "a1-akkusativ" }.stato
        )
    }

    @Test
    fun laProssimaUnitaPreferisceIlRipassoScaduto() {
        val completata = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val mappa = mapOf("a1-akkusativ" to completata)
        val prossima = GrammaticaSyllabus.prossimaUnita(mappa, adesso + 2 * giorno)
        assertNotNull(prossima)
        assertEquals("a1-akkusativ", prossima!!.unita.id)
        assertEquals(StatoUnita.DA_RIPASSARE, prossima.stato)
    }

    @Test
    fun cambiareLivelloSbloccaLeUnitaFinoAQuelLivello() {
        // Emma è B1: senza prerequisiti deve comunque poter aprire tutto fino al B1.
        val percorso = GrammaticaSyllabus.percorso(emptyMap(), adesso, livelloProfilo = "B1")
        val finoAB1 = percorso.filter { it.unita.livello in listOf("A0", "A1", "A2", "B1") }
        assertTrue(
            "Le unità fino al B1 devono essere accessibili",
            finoAB1.all { it.accessibile }
        )
        assertTrue(
            "Il B2 resta bloccato finché i prerequisiti non sono completati",
            percorso.filter { it.unita.livello == "B2" }.none { it.accessibile }
        )

        // Alzando ancora il livello si apre anche il B2.
        val b2 = GrammaticaSyllabus.percorso(emptyMap(), adesso, livelloProfilo = "B2")
        assertTrue("Con livello B2 tutto è accessibile", b2.all { it.accessibile })
    }

    @Test
    fun normalizzaMaiuscoleSpaziEPunteggiatura() {
        assertEquals("ich sehe den hund", GrammaticaSyllabus.normalizza("  Ich  sehe   den Hund.  "))
        assertEquals("ich sehe den hund", GrammaticaSyllabus.normalizza("ICH SEHE DEN HUND!"))
        assertEquals("du arbeitest viel", GrammaticaSyllabus.normalizza("Du arbeitest viel?"))
    }

    @Test
    fun accettaLaScritturaSenzaUmlaut() {
        assertEquals(
            GrammaticaSyllabus.traslittera("Ich fähre"),
            GrammaticaSyllabus.traslittera("Ich faehre")
        )
        assertEquals(
            "La dettatura deve accettare ss al posto di ß",
            GrammaticaSyllabus.traslittera("Ich heiße Anna."),
            GrammaticaSyllabus.traslittera("Ich heisse Anna")
        )
        assertTrue(
            GrammaticaSyllabus.rispostaTestoCorretta(
                EsercizioUnita(
                    id = "test",
                    tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                    domanda = "Partizip II di sehen",
                    rispostaTesto = "gesehen"
                ),
                "  GESEHEN. "
            )
        )
    }

    @Test
    fun laRispostaTestualeAccettaLeAlternative() {
        val esercizio = EsercizioUnita(
            id = "test",
            tipo = TipoEsercizioGrammatica.PRODUZIONE,
            domanda = "Traduci",
            rispostaTesto = "Ich arbeite im Krankenhaus.",
            alternative = listOf("Ich arbeite in einem Krankenhaus.")
        )
        assertTrue(GrammaticaSyllabus.rispostaTestoCorretta(esercizio, "ich arbeite im krankenhaus"))
        assertTrue(GrammaticaSyllabus.rispostaTestoCorretta(esercizio, "ICH ARBEITE IN EINEM KRANKENHAUS"))
        assertFalse(GrammaticaSyllabus.rispostaTestoCorretta(esercizio, "Ich arbeite"))
    }

    @Test
    fun ilProfiloSalvaEPuoiRileggereLaPadronanza() {
        val padronanza = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val stato = ProfiloStato(
            padronanzaGrammatica = mapOf("a1-akkusativ" to padronanza)
        )
        val json = Json.encodeToString(stato)
        val riletto = Json.decodeFromString<ProfiloStato>(json)
        assertEquals(83, riletto.padronanzaGrammatica["a1-akkusativ"]?.percentuale)
        assertTrue(riletto.padronanzaGrammatica["a1-akkusativ"]?.completata == true)
    }

    @Test
    fun iPrerequisitiEsistonoESonoPrecedenti() {
        val ids = GrammaticaSyllabus.unita.map { it.id }
        GrammaticaSyllabus.unita.forEachIndexed { indice, unita ->
            unita.prerequisiti.forEach { prerequisito ->
                assertTrue(
                    "Prerequisito inesistente '$prerequisito' in ${unita.id}",
                    prerequisito in ids
                )
                assertTrue(
                    "Il prerequisito '$prerequisito' deve venire prima di ${unita.id}",
                    ids.indexOf(prerequisito) < indice
                )
            }
        }
    }

    @Test
    fun iContenutiNonHannoSegnaposto() {
        val segnaposto = listOf("TODO", "FIXME", "???", "da completare", "lorem ipsum")
        GrammaticaSyllabus.unita.forEach { unita ->
            val testi = buildList {
                addAll(unita.teoria.regola)
                addAll(unita.teoria.esempi.map { it.tedesco })
                addAll(unita.teoria.esempi.map { it.italiano })
                addAll(unita.teoria.erroriTipici.map { it.sbagliato })
                addAll(unita.teoria.erroriTipici.map { it.corretto })
                addAll(unita.teoria.erroriTipici.map { it.spiegazione })
                add(unita.teoria.merksatz)
                unita.esercizi.forEach { esercizio ->
                    add(esercizio.domanda)
                    add(esercizio.spiegazione)
                    add(esercizio.suggerimento)
                    add(esercizio.rispostaTesto)
                    addAll(esercizio.opzioni)
                    addAll(esercizio.alternative)
                }
            }
            testi.filter { it.isNotBlank() }.forEach { testo ->
                segnaposto.forEach { brutto ->
                    assertFalse(
                        "Segnaposto '$brutto' in ${unita.id}: $testo",
                        testo.contains(brutto, ignoreCase = true)
                    )
                }
                assertFalse(
                    "Doppio spazio in ${unita.id}: $testo",
                    testo.contains("  ")
                )
            }
        }
    }

    @Test
    fun leDomandeNonSonoDuplicateNellaStessaUnita() {
        GrammaticaSyllabus.unita.forEach { unita ->
            val domande = unita.esercizi.map { it.domanda.trim().lowercase() }
            assertEquals(
                "Domande duplicate in ${unita.id}",
                domande.size,
                domande.distinct().size
            )
        }
    }

    @Test
    fun ogniEsercizioHaUnaSpiegazione() {
        GrammaticaSyllabus.unita.forEach { unita ->
            unita.esercizi.forEach { esercizio ->
                assertTrue(
                    "Spiegazione mancante in ${esercizio.id}",
                    esercizio.spiegazione.isNotBlank()
                )
            }
        }
    }
}
