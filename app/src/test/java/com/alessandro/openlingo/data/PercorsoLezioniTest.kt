package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.ProgressoLezione
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Le lezioni sono le unità del percorso A0 → B2: la numerazione è progressiva,
 * la chiave è l'id dell'unità e l'accesso dipende dai prerequisiti oppure dal
 * livello già raggiunto dal profilo.
 */
class PercorsoLezioniTest {

    @Test
    fun leLezioniSonoLeUnitaDelPercorsoNumerateInOrdine() {
        val lezioni = PercorsoLezioni.tutte(emptyMap(), null)

        assertEquals(GrammaticaSyllabus.unita.size, lezioni.size)
        assertEquals((1..lezioni.size).toList(), lezioni.map { it.numero })
        assertEquals(GrammaticaSyllabus.unita.map { it.id }, lezioni.map { it.id })
        // La chiave stabile è l'id dell'unità, non il numero.
        assertEquals(1, PercorsoLezioni.numeroDi("a0-alfabeto"))
        assertEquals(
            GrammaticaSyllabus.unita.size,
            PercorsoLezioni.numeroDi(GrammaticaSyllabus.unita.last().id)
        )
    }

    @Test
    fun unProfiloB1ApreFinoAlB1SenzaPrerequisiti() {
        val lezioni = PercorsoLezioni.tutte(emptyMap(), "B1 - Intermedio")

        val sottoB1 = lezioni.filter { PercorsoLezioni.ordineLivello(it.livello) <= 3 }
        assertTrue(
            "Emma deve poter aprire tutte le lezioni fino al B1",
            sottoB1.all { it.accessibile }
        )

        val b2 = lezioni.filter { it.livello == "B2" }
        assertTrue("Le unità B2 esistono", b2.isNotEmpty())
        assertTrue(
            "Il B2 resta bloccato finché i prerequisiti non sono completati",
            b2.none { it.accessibile }
        )
    }

    @Test
    fun completareIPrerequisitiSbloccaIlB2() {
        val prerequisiti = GrammaticaSyllabus.perId("b2-passiv-modal")!!.prerequisiti
        val mappa = prerequisiti.associateWith { id ->
            PadronanzaGrammatica(
                unitaId = id,
                percentuale = 100,
                corrette = 12,
                totali = 12,
                completata = true,
                ultimaVolta = 1L,
                prossimaRipasso = 0L,
                ripassi = 1
            )
        }

        val lezione = PercorsoLezioni.perId("b2-passiv-modal", mappa, "B1")
        assertNotNull(lezione)
        assertTrue("Con i prerequisiti fatti il B2 si apre", lezione!!.accessibile)
    }

    @Test
    fun ilProfiloA0ApreLeBasiMaNonLungaOltre() {
        val lezioni = PercorsoLezioni.tutte(emptyMap(), "A0")

        assertTrue(lezioni.first().accessibile)
        assertTrue(
            "Il livello già raggiunto apre tutte le unità A0",
            lezioni.filter { it.livello == "A0" }.all { it.accessibile }
        )
        assertFalse(
            "Le unità A1 restano chiuse finché i prerequisiti non sono completati",
            lezioni.first { it.livello == "A1" }.accessibile
        )
        assertEquals("a0-alfabeto", lezioni.first().id)
    }

    @Test
    fun laLezioneConsigliataPerB1ParteDalLivelloB1() {
        val consigliata = PercorsoLezioni.consigliata(emptyMap(), "B1")

        assertNotNull(consigliata)
        assertEquals("b1-futur", consigliata!!.id)
        assertEquals("B1", consigliata.livello)
        assertTrue(consigliata.accessibile)
    }

    @Test
    fun laLezioneSuccessivaSegueIlPercorso() {
        val completata = PadronanzaGrammatica(
            unitaId = "a0-alfabeto",
            corrette = 12,
            totali = 12,
            percentuale = 100,
            completata = true,
            ultimaVolta = 1L,
            prossimaRipasso = 0L,
            ripassi = 1
        )
        val mappa = mapOf("a0-alfabeto" to completata)
        val prossima = PercorsoLezioni.prossima("a0-alfabeto", mappa, "A0")

        assertNotNull(prossima)
        assertEquals("a0-pronomi-sein", prossima!!.id)
    }

    @Test
    fun laTeoriaSiGeneraSenzaAi() {
        val unita = GrammaticaSyllabus.perId("a1-akkusativ")!!
        val markdown = PercorsoLezioni.teoriaMarkdown(unita)

        assertTrue(markdown.contains(unita.titolo))
        assertTrue("La regola deve essere nel testo", markdown.contains("La regola"))
        assertTrue("Gli esempi devono essere nel testo", markdown.contains("Esempi"))
        assertFalse(markdown.contains("TODO"))
    }

    @Test
    fun ilPromemoriaDellaTeoriaNonSvelaLeRisposte() {
        // La teoria deve spiegare le regole testate dagli esercizi, ma NON può
        // dire quale opzione scegliere: sarebbe un esercizio già risolto.
        fun radice(parola: String) = if (parola.length >= 5) parola.take(5) else parola

        GrammaticaSyllabus.unita.forEach { unita ->
            val parolePromemoria = DeduplicaEsercizi
                .normalizza(PercorsoLezioni.promemoriaEsercizi(unita).joinToString(" | "))
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .map { radice(it) }
                .toSet()
            unita.esercizi.forEach { esercizio ->
                val risposte = when (esercizio.tipo) {
                    TipoEsercizioGrammatica.SCELTA_MULTIPLA ->
                        listOf(esercizio.opzioni[esercizio.rispostaCorretta])

                    else -> listOf(esercizio.rispostaTesto) + esercizio.alternative
                }
                risposte.filter { it.isNotBlank() }.forEach { risposta ->
                    val r = DeduplicaEsercizi.normalizza(risposta)
                    if (r.length >= 2) {
                        assertFalse(
                            "Il promemoria di ${unita.id} svela la risposta \"$risposta\": " +
                                PercorsoLezioni.promemoriaEsercizi(unita),
                            radice(r) in parolePromemoria
                        )
                    }
                }
            }
        }
    }

    @Test
    fun ilPromemoriaNonRipeteCioCheLaTeoriaGiaDice() {
        val unita = GrammaticaSyllabus.perId("a1-akkusativ")!!
        val testoTeoria = DeduplicaEsercizi.normalizza(
            PercorsoLezioni.teoriaMarkdown(unita).substringBefore("### Promemoria")
        )
        PercorsoLezioni.promemoriaEsercizi(unita).forEach { voce ->
            val normale = DeduplicaEsercizi.normalizza(voce)
            assertFalse(
                "Promemoria duplicato della teoria: $voce",
                testoTeoria.contains(normale)
            )
        }
    }

    @Test
    fun laSezionePromemoriaCompareSoloQuandoServe() {
        // Le unità con la teoria già completa non devono mostrare una sezione
        // vuota; quelle con regole mancanti sì, senza risposte dentro.
        val conPromemoria = GrammaticaSyllabus.unita.filter {
            PercorsoLezioni.promemoriaEsercizi(it).isNotEmpty()
        }
        assertTrue("Nessuna unità ha un promemoria", conPromemoria.isNotEmpty())
        conPromemoria.forEach { unita ->
            assertTrue(
                PercorsoLezioni.teoriaMarkdown(unita).contains("### Promemoria")
            )
        }
    }

    @Test
    fun laLezioneApertaRiprendeDalPuntoSalvato() {
        val lezione = PercorsoLezioni.perId("a1-akkusativ", emptyMap(), "B1")!!
        val totale = lezione.unita.esercizi.size

        val aperta = LezioneAperta(
            lezione = lezione,
            progresso = ProgressoLezione(indice = 3, corrette = 2, totali = totale)
        )
        assertEquals(3, aperta.indice)
        assertFalse(aperta.conclusa)

        // Un indice fuori range non deve rompere la UI.
        val fuoriRange = LezioneAperta(
            lezione = lezione,
            progresso = ProgressoLezione(indice = totale + 10, corrette = totale, totali = totale)
        )
        assertEquals(totale, fuoriRange.indice)
        assertTrue(fuoriRange.conclusa)
    }

    @Test
    fun ordineLivelloAccettaCodiceELabelLungo() {
        assertEquals(PercorsoLezioni.ordineLivello("B1"), PercorsoLezioni.ordineLivello("B1 - Intermedio"))
        assertEquals(PercorsoLezioni.ordineLivello("b2"), PercorsoLezioni.ordineLivello("B2"))
        assertTrue(PercorsoLezioni.ordineLivello("B2") > PercorsoLezioni.ordineLivello("A0"))
    }
}
