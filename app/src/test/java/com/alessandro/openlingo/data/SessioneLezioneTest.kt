package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.EsercizioGenEntity
import com.alessandro.openlingo.data.local.RotazioneUnita
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La sessione di una lezione deve cambiare da un tentativo all'altro, ma
 * restare stabile mentre lo studente risponde (altrimenti la domanda
 * cambierebbe sotto le mani).
 *
 * Il test più importante è `due tentativi consecutivi non hanno esercizi in
 * comune`: era esattamente il problema segnalato ("mi riproponi gli stessi
 * esercizi di 20 secondi prima").
 */
class SessioneLezioneTest {

    private val unita = GrammaticaSyllabus.unita

    private fun unitaRicca(): UnitaGrammatica = unita.first { it.esercizi.size >= 12 }

    private fun banco(unita: UnitaGrammatica): List<EsercizioUnita> =
        SessioneLezione.banco(unita)

    @Test
    fun `la stessa semina produce la stessa sessione`() {
        val u = unitaRicca()
        val a = SessioneLezione.nuovaSessione(banco(u), RotazioneUnita(), seme = 123456)
        val b = SessioneLezione.nuovaSessione(banco(u), RotazioneUnita(), seme = 123456)
        assertEquals(a.esercizi.map { it.id }, b.esercizi.map { it.id })
    }

    @Test
    fun `due tentativi consecutivi non hanno esercizi in comune`() {
        unita.filter { it.esercizi.size >= 12 }.forEach { u ->
            val banco = banco(u)
            var rotazione = RotazioneUnita()
            var precedenti = emptySet<String>()
            repeat(6) { tentativo ->
                val sessione = SessioneLezione.nuovaSessione(
                    banco = banco,
                    rotazione = rotazione,
                    seme = tentativo * 7919 + 1
                )
                val ids = sessione.esercizi.map { it.id }.toSet()
                val comuni = ids intersect precedenti
                assertTrue(
                    "${u.id}: tentativo $tentativo ripete ${comuni.size} esercizi: $comuni",
                    comuni.isEmpty()
                )
                assertTrue("${u.id}: sessione vuota", ids.isNotEmpty())
                precedenti = ids
                rotazione = sessione.rotazione
            }
        }
    }

    @Test
    fun `la sessione non supera mai la meta del banco`() {
        unita.forEach { u ->
            val sessione = SessioneLezione.nuovaSessione(banco(u), RotazioneUnita(), seme = 5)
            assertTrue(
                "${u.id}: sessione ${sessione.esercizi.size} su banco ${u.esercizi.size}",
                sessione.esercizi.size <= u.esercizi.size / 2 + 1
            )
            assertTrue("${u.id}: sessione troppo corta", sessione.esercizi.size >= 4)
            assertTrue("${u.id}: sessione oltre il tetto", sessione.esercizi.size <= 8)
        }
    }

    @Test
    fun `la dimensione tiene conto del banco`() {
        assertEquals(0, SessioneLezione.dimensione(0))
        assertEquals(3, SessioneLezione.dimensione(3))
        assertEquals(6, SessioneLezione.dimensione(12))
        assertEquals(8, SessioneLezione.dimensione(16))
        assertEquals(8, SessioneLezione.dimensione(40))
    }

    @Test
    fun `con un banco piccolo si mostrano tutti gli esercizi`() {
        val piccola = UnitaGrammatica(
            id = "test-piccola",
            livello = "A1",
            titolo = "Test",
            sottotitolo = "",
            obiettivo = "",
            minuti = 5,
            teoria = TeoriaGrammatica(regola = listOf("Regola di test.")),
            esercizi = listOf(
                sceltaUnita("t1", "A?", listOf("a", "b"), 0, "spiegazione"),
                sceltaUnita("t2", "B?", listOf("a", "b"), 0, "spiegazione"),
                sceltaUnita("t3", "C?", listOf("a", "b"), 0, "spiegazione")
            )
        )
        assertEquals(3, SessioneLezione.nuovaSessione(banco(piccola), RotazioneUnita(), 1).esercizi.size)
    }

    @Test
    fun `non ci sono esercizi ripetuti nella sessione`() {
        unita.filter { it.esercizi.size >= 12 }.forEach { u ->
            repeat(10) { seme ->
                val ids = SessioneLezione.nuovaSessione(banco(u), RotazioneUnita(), seme)
                    .esercizi.map { it.id }
                assertEquals("${u.id}: duplicati al seme $seme", ids.size, ids.toSet().size)
            }
        }
    }

    @Test
    fun `la prima sessione non chiede di produrre ma copre gli altri tipi`() {
        // Chi apre l'argomento per la prima volta deve vedere la regola in
        // azione, non essere subito messo alla prova con una frase da scrivere.
        unita.filter { it.esercizi.size >= 12 }.forEach { u ->
            val senzaProduzione = u.esercizi.count {
                it.tipo != TipoEsercizioGrammatica.PRODUZIONE
            }
            val n = SessioneLezione.dimensione(u.esercizi.size)
            val sessione = SessioneLezione.nuovaSessione(banco(u), RotazioneUnita(), seme = 42)
            if (senzaProduzione >= n) {
                assertTrue(
                    "${u.id}: la prima sessione contiene una PRODUZIONE",
                    sessione.esercizi.none { it.tipo == TipoEsercizioGrammatica.PRODUZIONE }
                )
            }
            val tipiAttesi = u.esercizi.map { it.tipo }.toSet() -
                TipoEsercizioGrammatica.PRODUZIONE
            val tipiSessione = sessione.esercizi.map { it.tipo }.toSet()
            assertTrue(
                "${u.id}: tipi mancanti ${tipiAttesi - tipiSessione}",
                tipiSessione.containsAll(tipiAttesi)
            )
        }
    }

    @Test
    fun `le sessioni successive propongono al massimo una produzione e un picco`() {
        unita.filter { it.esercizi.size >= 12 }.forEach { u ->
            var rotazione = RotazioneUnita()
            val banco = banco(u)
            repeat(4) { tentativo ->
                val sessione = SessioneLezione.nuovaSessione(
                    banco = banco,
                    rotazione = rotazione,
                    seme = tentativo * 7919 + 3
                )
                val produzioni = sessione.esercizi.count { it.tipo == TipoEsercizioGrammatica.PRODUZIONE }
                val picchi = sessione.esercizi.count {
                    it.tipo != TipoEsercizioGrammatica.PRODUZIONE && DifficoltaEsercizi.ePicco(it)
                }
                assertTrue(
                    "${u.id}: $produzioni produzioni nel tentativo $tentativo",
                    produzioni <= 1
                )
                assertTrue(
                    "${u.id}: $picchi picchi (oltre alla produzione) nel tentativo $tentativo",
                    picchi <= 1
                )
                rotazione = sessione.rotazione
            }
        }
    }

    @Test
    fun `dopo la prima sessione la produzione torna disponibile`() {
        // Una rotazione con un id non più nel banco non è una "prima sessione":
        // il vincolo guarda ciò che lo studente ha già visto, non i contenuti
        // rimossi da un aggiornamento.
        val u = unita.first { unita ->
            unita.esercizi.any { it.tipo == TipoEsercizioGrammatica.PRODUZIONE } &&
                unita.esercizi.size >= 12
        }
        val sessione = SessioneLezione.nuovaSessione(
            banco = banco(u),
            rotazione = RotazioneUnita(visti = listOf("id-sparito")),
            seme = 11
        )
        assertTrue(
            "${u.id}: la produzione non compare più",
            sessione.esercizi.any { it.tipo == TipoEsercizioGrammatica.PRODUZIONE }
        )
    }

    @Test
    fun `la sessione va dal facile al difficile`() {
        unita.forEach { u ->
            val sessione = SessioneLezione.nuovaSessione(banco(u), RotazioneUnita(), seme = 17)
            val punteggi = sessione.esercizi.map { DifficoltaEsercizi.punteggio(it) }
            assertEquals(
                "${u.id}: sessione non ordinata per difficolta: $punteggi",
                punteggi.sorted(),
                punteggi
            )
        }
    }

    @Test
    fun `le opzioni sono mescolate ma la risposta corretta resta giusta`() {
        val u = unitaRicca()
        val originali = u.esercizi.filter { it.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA }
            .associateBy { it.id }
        repeat(20) { seme ->
            SessioneLezione.nuovaSessione(banco(u), RotazioneUnita(), seme)
                .esercizi
                .filter { it.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA }
                .forEach { vista ->
                    val originale = originali.getValue(vista.id)
                    assertEquals(
                        "Risposta corretta cambiata per ${vista.id}",
                        originale.opzioni[originale.rispostaCorretta],
                        vista.opzioni[vista.rispostaCorretta]
                    )
                    assertEquals(
                        "Opzioni alterate per ${vista.id}",
                        originale.opzioni.sorted(),
                        vista.opzioni.sorted()
                    )
                }
        }
    }

    @Test
    fun `almeno una posizione della risposta cambia tra tentativi`() {
        val u = unitaRicca()
        val posizioni = (1..12).map { seme ->
            SessioneLezione.nuovaSessione(banco(u), RotazioneUnita(), seme)
                .esercizi
                .filter { it.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA }
                .map { it.rispostaCorretta }
        }
        assertTrue(
            "La posizione della risposta corretta non cambia mai",
            posizioni.toSet().size > 1
        )
    }

    @Test
    fun `riaprendo la lezione si riprende la stessa sessione`() {
        val u = unitaRicca()
        val prima = SessioneLezione.nuovaSessione(banco(u), RotazioneUnita(), seme = 99)
        val riaperta = SessioneLezione.sessione(banco(u), prima.rotazione)
        assertEquals(prima.esercizi.map { it.id }, riaperta.esercizi.map { it.id })
        assertEquals(
            prima.esercizi.first().let { it.opzioni to it.rispostaCorretta },
            riaperta.esercizi.first().let { it.opzioni to it.rispostaCorretta }
        )
    }

    @Test
    fun `gli esercizi AI entrano nel banco ma non creano doppioni`() {
        val u = unitaRicca()
        val nuovo = EsercizioGenEntity(
            id = "ai-1",
            tipo = "grammatica",
            domanda = "Der ___ Kollege kommt morgen.",
            opzioni = listOf("neue", "neuer", "neuen", "neues"),
            rispostaCorretta = 2,
            spiegazione = "Accusativo maschile.",
            livello = "A2"
        )
        val conAi = SessioneLezione.bancoDaGenerati(u, listOf(nuovo))
        assertEquals(u.esercizi.size + 1, conAi.size)

        // Stessa domanda di un esercizio scritto: scartata.
        val doppione = nuovo.copy(id = "ai-2", domanda = u.esercizi.first().domanda)
        val conDoppione = SessioneLezione.bancoDaGenerati(u, listOf(nuovo, doppione))
        assertEquals(u.esercizi.size + 1, conDoppione.size)
    }

    @Test
    fun `un esercizio AI malformato viene scartato`() {
        val rotto = EsercizioGenEntity(
            id = "ai-rotto",
            tipo = "grammatica",
            domanda = "Domanda?",
            opzioni = listOf("a", "b"),
            rispostaCorretta = 7
        )
        assertNull(SessioneLezione.daGenerato(rotto))
        assertNotNull(
            SessioneLezione.daGenerato(rotto.copy(rispostaCorretta = 1))
        )
    }

    @Test
    fun `un esercizio AI con accordo sbagliato viene scartato`() {
        // "einen Buch" è sbagliato: Buch è neutro. L'esercizio non deve arrivare
        // allo studente solo perché l'AI lo ha prodotto.
        val sbagliato = EsercizioGenEntity(
            id = "ai-sbagliato",
            tipo = "grammatica",
            domanda = "Ich habe ___ Buch.",
            opzioni = listOf("einen", "eine", "ein", "einem"),
            rispostaCorretta = 0,
            spiegazione = "Accusativo maschile."
        )
        assertNull(SessioneLezione.daGenerato(sbagliato))

        val corretto = sbagliato.copy(
            opzioni = listOf("ein", "eine", "einen", "einem"),
            rispostaCorretta = 0
        )
        assertNotNull(SessioneLezione.daGenerato(corretto))
    }
}
