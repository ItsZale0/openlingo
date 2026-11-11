package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.RipassoLogEntity
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La lista "da ripassare" e' UNICA: la usano sia il contatore mostrato in home sia
 * le carte della sessione di ripasso. Questi test impedisano che tornino a divergere
 * (il bug: schermo aperto = "niente da ripassare", schermo chiuso = "2 da ripassare").
 */
class VerificaRipassoTest {

    private val ORA = 1_700_000_000_000L
    private val ADesso = ORA + 1_000L
    private val PASSATO = ORA - 60_000L
    private val FUTURO = ORA + 86_400_000L * 30

    private fun parola(id: String, de: String, it: String, archiviata: Boolean = false) =
        WordEntity(id = id, german = de, italian = it, archived = archiviata)

    private fun log(id: String, corretta: Boolean, verifica: Boolean = true, ts: Long = PASSATO) =
        RipassoLogEntity(wordId = id, timestamp = ts, corretta = corretta, verifica = verifica)

    private fun review(id: String, dueAt: Long) =
        ReviewEntity(wordId = id, dueAt = dueAt)

    private fun ids(lista: List<WordEntity>) = lista.map { it.id }

    @Test
    fun `unisce fragili e scadute in una sola lista`() {
        val parole = listOf(
            parola("f", "der Hund", "il cane"),     // fragile: sbagliata e mai verificata
            parola("s", "das Haus", "la casa"),     // review in scadenza
            parola("b", "die Katze", "il gatto")    // ok: niente da ripassare
        )
        val log = listOf(log("f", corretta = false))
        val reviews = mapOf("s" to review("s", PASSATO))
        val verifiche = VerificaLessico.verificheDaLog(log)

        val risultato = VerificaLessico.paroleDaRipassare(parole, log, verifiche, reviews, ADesso)

        assertEquals(listOf("f", "s"), ids(risultato))
    }

    @Test
    fun `le fragili vengono per prime`() {
        val parole = listOf(
            parola("s1", "das Haus", "la casa"),
            parola("s2", "der Tisch", "il tavolo"),
            parola("f", "der Hund", "il cane")
        )
        val log = listOf(log("f", corretta = false))
        val reviews = mapOf(
            "s1" to review("s1", PASSATO),
            "s2" to review("s2", ORA - 1_000L)
        )
        val risultato = VerificaLessico.paroleDaRipassare(
            parole, log, VerificaLessico.verificheDaLog(log), reviews, ADesso
        )

        assertEquals("f", risultato.first().id)
    }

    @Test
    fun `una parola sbagliata e poi verificata non c'e' piu`() {
        val parole = listOf(parola("x", "der Hund", "il cane"))
        val log = listOf(
            log("x", corretta = false, ts = PASSATO),
            log("x", corretta = true, ts = PASSATO + 1_000L)  // la verifica corretta viene dopo
        )
        val risultato = VerificaLessico.paroleDaRipassare(
            parole, log, VerificaLessico.verificheDaLog(log), emptyMap(), ADesso
        )

        assertTrue(risultato.isEmpty())
    }

    @Test
    fun `le review non ancora in scadenza non entrano`() {
        val parole = listOf(parola("x", "der Hund", "il cane"))
        val reviews = mapOf("x" to review("x", FUTURO))

        val risultato = VerificaLessico.paroleDaRipassare(
            parole, emptyList(), emptyMap(), reviews, ADesso
        )

        assertTrue(risultato.isEmpty())
    }

    @Test
    fun `archiviate e non utilizzabili sono escluse`() {
        val parole = listOf(
            parola("a", "der Hund", "il cane", archiviata = true),
            parola("v", "", "nessuna"),             // tedesco vuoto
            parola("o", "das Haus", "la casa")      // ok
        )
        val log = listOf(log("a", corretta = false), log("o", corretta = false))
        val reviews = mapOf("v" to review("v", PASSATO))

        val risultato = VerificaLessico.paroleDaRipassare(
            parole, log, VerificaLessico.verificheDaLog(log), reviews, ADesso
        )

        assertEquals(listOf("o"), ids(risultato))
    }

    @Test
    fun `corretta al flashcard ma mai verificata non rende fragile`() {
        // Il self-report ("lo so") non e' evidenza: solo le verifiche contano.
        val parole = listOf(parola("x", "der Hund", "il cane"))
        val log = listOf(log("x", corretta = true, verifica = false))

        val risultato = VerificaLessico.paroleDaRipassare(
            parole, log, VerificaLessico.verificheDaLog(log), emptyMap(), ADesso
        )

        assertTrue(risultato.isEmpty())
    }

    @Test
    fun `il contatore della home e le carte della sessione derivano dalla stessa lista`() {
        // CalcoloProgresso usa la stessa funzione: il numero mostrato deve essere
        // esattamente il numero di carte che vedra' l'utente.
        val parole = listOf(
            parola("f", "der Hund", "il cane"),
            parola("s", "das Haus", "la casa"),
            parola("b", "die Katze", "il gatto")
        )
        val log = listOf(log("f", corretta = false))
        val reviews = mapOf("s" to review("s", PASSATO))
        val verifiche = VerificaLessico.verificheDaLog(log)

        val pr = CalcoloProgresso.calcola(
            parole = parole,
            reviews = reviews,
            progresso = com.alessandro.openlingo.data.local.ProgressoUtente(),
            log = log,
            obiettivo = com.alessandro.openlingo.data.local.LivelloCEFR.A1,
            now = ADesso
        )
        val carte = VerificaLessico.paroleDaRipassare(parole, log, verifiche, reviews, ADesso)

        assertEquals(carte.size, pr.paroleFragili)
        assertEquals(2, pr.paroleFragili)
    }
}
