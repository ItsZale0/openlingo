package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.RipassoLogEntity
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test della verifica OGGETTIVA del lessico (scelta multipla, non self-report). */
class VerificaLessicoTest {

    private fun parola(id: String, de: String, it: String) =
        WordEntity(id = id, german = de, italian = it)

    private fun log(id: String, corretta: Boolean, ts: Long = 1000L, verifica: Boolean = true) =
        RipassoLogEntity(wordId = id, timestamp = ts, corretta = corretta, verifica = verifica)

    @Test
    fun `le domande hanno la risposta corretta tra le opzioni`() {
        val parole = listOf(
            parola("1", "das Haus", "la casa"),
            parola("2", "der Hund", "il cane"),
            parola("3", "die Katze", "il gatto"),
            parola("4", "das Buch", "il libro")
        )
        val domande = VerificaLessico.costruisciDomande(parole)
        assertEquals(4, domande.size)
        domande.forEach { d ->
            assertTrue("opzioni non vuote", d.opzioni.isNotEmpty())
            assertTrue("corretta presente", d.opzioni.contains(d.corretta))
        }
    }

    @Test
    fun `i distrattori non includono lo stesso significato della risposta`() {
        val parole = listOf(
            parola("1", "das Haus", "la casa"),
            parola("2", "die Wohnung", "la casa"),  // stesso significato!
            parola("3", "der Hund", "il cane"),
            parola("4", "das Buch", "il libro")
        )
        val domande = VerificaLessico.costruisciDomande(parole)
        val d = domande.first { it.parola.id == "1" }
        // "la casa" non deve apparire due volte
        assertEquals(1, d.opzioni.count { it == "la casa" })
    }

    @Test
    fun `punteggio lessico e percentuale di verificate`() {
        val verifiche = mapOf("1" to true, "2" to true, "3" to false, "4" to false)
        assertEquals(50f, VerificaLessico.punteggioLessico(4, verifiche), 0.01f)
        assertEquals(0f, VerificaLessico.punteggioLessico(0, emptyMap()), 0.01f)
    }

    @Test
    fun `una parola e verificata se l ultima verifica e corretta`() {
        // Una sola verifica corretta BASTA: il quiz chiede ogni parola una volta,
        // pretendere 2 corrette la renderebbe non verificabile per sempre.
        val v1 = VerificaLessico.verificheDaLog(listOf(log("a", true, 1)))
        assertTrue(v1["a"] ?: false)

        // Verifica corretta poi sbagliata -> non verificata
        val v2 = VerificaLessico.verificheDaLog(listOf(log("a", true, 1), log("a", false, 2)))
        assertFalse(v2["a"] ?: false)

        // Sbagliata poi corretta -> verificata (ultima comanda)
        val v3 = VerificaLessico.verificheDaLog(listOf(log("a", false, 1), log("a", true, 2)))
        assertTrue(v3["a"] ?: false)
    }

    @Test
    fun `il self-report del flashcard non verifica`() {
        // "Lo so" nel ripasso NON e' evidenza: serve la verifica oggettiva.
        val v = VerificaLessico.verificheDaLog(listOf(log("a", true, 1, verifica = false)))
        assertFalse(v["a"] ?: false)
    }

    @Test
    fun `le parole fragili sono le sbagliate non verificate`() {
        val parole = listOf(parola("1", "a", "A"), parola("2", "b", "B"), parola("3", "c", "C"))
        val log = listOf(log("1", false), log("2", true, 1), log("2", true, 2))
        val verifiche = VerificaLessico.verificheDaLog(log)
        val fragili = VerificaLessico.paroleFragili(parole, log, verifiche)
        assertEquals(listOf("1"), fragili.map { it.id })
        // Un "Non lo so" del flashcard NON rende fragile
        val logFlash = listOf(log("3", false, verifica = false))
        val fragiliFlash = VerificaLessico.paroleFragili(parole, logFlash, VerificaLessico.verificheDaLog(logFlash))
        assertTrue(fragiliFlash.isEmpty())
    }

    @Test
    fun `le parole da verificare escludono quelle gia verificate`() {
        val parole = (1..5).map { parola("$it", "w$it", "i$it") }
        val log = listOf(log("1", true, 1))
        val verifiche = VerificaLessico.verificheDaLog(log)
        val daVerificare = VerificaLessico.paroleDaVerificare(parole, log, verifiche, 10)
        assertFalse(daVerificare.any { it.id == "1" })
        assertEquals(4, daVerificare.size)
    }

    @Test
    fun `esito calcola punteggio e parole verificate`() {
        val e = VerificaLessico.Esito(
            totale = 10, corrette = 7,
            paroleVerificate = listOf("a","b","c","d","e","f","g"),
            paroleDaRivedere = listOf("h","i","j")
        )
        assertEquals(70f, e.punteggio, 0.01f)
        assertEquals(7, e.paroleVerificate.size)
    }

    @Test
    fun `con meno di 2 parole non si generano domande`() {
        assertEquals(0, VerificaLessico.costruisciDomande(listOf(parola("1","a","A"))).size)
        assertEquals(0, VerificaLessico.costruisciDomande(emptyList()).size)
    }

    @Test
    fun `dopo aver verificato tutto non resta nulla da verificare`() {
        // Riproduce il bug segnalato: "ho completato il lessico ma mi ripropone le stesse".
        val parole = (1..10).map { parola("$it", "w$it", "i$it") }
        // Una verifica CORRETTA per ogni parola (il quiz le chiede una volta sola)
        val log = parole.mapIndexed { i, w -> log(w.id, true, ts = 1000L + i) }
        val verifiche = VerificaLessico.verificheDaLog(log)

        // Tutte verificate
        assertEquals(10, verifiche.count { it.value })
        // Niente da verificare, niente fragili
        assertTrue(VerificaLessico.paroleDaVerificare(parole, log, verifiche, 10).isEmpty())
        assertTrue(VerificaLessico.paroleFragili(parole, log, verifiche).isEmpty())
        // E il punteggio lessico e' 100%
        assertEquals(100f, VerificaLessico.punteggioLessico(parole.size, verifiche), 0.01f)
    }

    @Test
    fun `le parole sbagliate tornano fragili finche non verificate`() {
        val parole = (1..4).map { parola("$it", "w$it", "i$it") }
        // 3 verificate, 1 sbagliata
        val log = listOf(log("1", true, 1), log("2", true, 2), log("3", true, 3), log("4", false, 4))
        val verifiche = VerificaLessico.verificheDaLog(log)
        assertEquals(listOf("4"), VerificaLessico.paroleFragili(parole, log, verifiche).map { it.id })
        // Dopo una verifica corretta non e' piu' fragile
        val log2 = log + log("4", true, 5)
        val v2 = VerificaLessico.verificheDaLog(log2)
        assertTrue(VerificaLessico.paroleFragili(parole, log2, v2).isEmpty())
    }
}
