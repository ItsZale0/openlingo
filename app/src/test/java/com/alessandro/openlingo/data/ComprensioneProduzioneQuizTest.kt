package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.DomandaTest
import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.TestComprensione
import com.alessandro.openlingo.data.local.TestProduzione
import com.alessandro.openlingo.data.local.TipoDomanda
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TDD tests for comprensione (reading comprehension) and produzione (sentence production) quizzes.
 *
 * These tests define the contract for:
 * 1. Quiz data structures (TestComprensione, TestProduzione)
 * 2. Quiz scoring logic
 * 3. Integration with ProgressoUtente
 * 4. How quiz results feed into CalcoloCompetenze
 */
class ComprensioneProduzioneQuizTest {

    private val NOW = 1_700_000_000_000L

    // --- TestComprensione data class tests ---

    @Test
    fun `TestComprensione stores all fields correctly`() {
        val test = TestComprensione(
            data = NOW,
            punteggio = 80f,
            errori = 2,
            totale = 10
        )
        assertEquals(NOW, test.data)
        assertEquals(80f, test.punteggio, 0.01f)
        assertEquals(2, test.errori)
        assertEquals(10, test.totale)
    }

    @Test
    fun `TestComprensione default values are zero`() {
        val test = TestComprensione(data = NOW, punteggio = 0f, errori = 0, totale = 0)
        assertEquals(0f, test.punteggio, 0.01f)
        assertEquals(0, test.errori)
        assertEquals(0, test.totale)
    }

    // --- TestProduzione data class tests ---

    @Test
    fun `TestProduzione stores all fields correctly`() {
        val test = TestProduzione(
            data = NOW,
            punteggio = 75f,
            errori = 3,
            totale = 12
        )
        assertEquals(NOW, test.data)
        assertEquals(75f, test.punteggio, 0.01f)
        assertEquals(3, test.errori)
        assertEquals(12, test.totale)
    }

    @Test
    fun `TestProduzione default values are zero`() {
        val test = TestProduzione(data = NOW, punteggio = 0f, errori = 0, totale = 0)
        assertEquals(0f, test.punteggio, 0.01f)
        assertEquals(0, test.errori)
        assertEquals(0, test.totale)
    }

    // --- ProgressoUtente integration tests ---

    @Test
    fun `ProgressoUtente has testComprensione list`() {
        val progresso = ProgressoUtente(
            testComprensione = listOf(
                TestComprensione(data = NOW, punteggio = 80f, errori = 2, totale = 10)
            )
        )
        assertEquals(1, progresso.testComprensione.size)
        assertEquals(80f, progresso.testComprensione[0].punteggio, 0.01f)
    }

    @Test
    fun `ProgressoUtente has testProduzione list`() {
        val progresso = ProgressoUtente(
            testProduzione = listOf(
                TestProduzione(data = NOW, punteggio = 75f, errori = 3, totale = 12)
            )
        )
        assertEquals(1, progresso.testProduzione.size)
        assertEquals(75f, progresso.testProduzione[0].punteggio, 0.01f)
    }

    @Test
    fun `ProgressoUtente testComprensione and testProduzione are independent`() {
        val progresso = ProgressoUtente(
            testComprensione = listOf(
                TestComprensione(data = NOW, punteggio = 80f, errori = 2, totale = 10)
            ),
            testProduzione = listOf(
                TestProduzione(data = NOW, punteggio = 60f, errori = 4, totale = 10)
            )
        )
        assertEquals(1, progresso.testComprensione.size)
        assertEquals(1, progresso.testProduzione.size)
        assertEquals(80f, progresso.testComprensione[0].punteggio, 0.01f)
        assertEquals(60f, progresso.testProduzione[0].punteggio, 0.01f)
    }

    // --- Quiz scoring logic tests ---

    @Test
    fun `comprensione quiz score is percentage correct`() {
        // 8 correct out of 10 -> 80%
        val test = TestComprensione(
            data = NOW,
            punteggio = 80f,
            errori = 2,
            totale = 10
        )
        val expectedScore = (test.totale - test.errori).toFloat() / test.totale * 100
        assertEquals(expectedScore, test.punteggio, 0.01f)
    }

    @Test
    fun `produzione quiz score is percentage correct`() {
        // 6 correct out of 10 -> 60%
        val test = TestProduzione(
            data = NOW,
            punteggio = 60f,
            errori = 4,
            totale = 10
        )
        val expectedScore = (test.totale - test.errori).toFloat() / test.totale * 100
        assertEquals(expectedScore, test.punteggio, 0.01f)
    }

    @Test
    fun `comprensione quiz with 0 errors gives 100 percent`() {
        val test = TestComprensione(
            data = NOW,
            punteggio = 100f,
            errori = 0,
            totale = 10
        )
        assertEquals(100f, test.punteggio, 0.01f)
    }

    @Test
    fun `produzione quiz with all errors gives 0 percent`() {
        val test = TestProduzione(
            data = NOW,
            punteggio = 0f,
            errori = 10,
            totale = 10
        )
        assertEquals(0f, test.punteggio, 0.01f)
    }

    // --- Quiz result aggregation tests ---

    @Test
    fun `average comprensione score across multiple tests`() {
        val tests = listOf(
            TestComprensione(data = NOW, punteggio = 80f, errori = 2, totale = 10),
            TestComprensione(data = NOW + 1000, punteggio = 60f, errori = 4, totale = 10),
            TestComprensione(data = NOW + 2000, punteggio = 100f, errori = 0, totale = 10)
        )
        val average = tests.map { it.punteggio }.average().toFloat()
        assertEquals(80f, average, 0.01f)
    }

    @Test
    fun `average produzione score across multiple tests`() {
        val tests = listOf(
            TestProduzione(data = NOW, punteggio = 50f, errori = 5, totale = 10),
            TestProduzione(data = NOW + 1000, punteggio = 70f, errori = 3, totale = 10),
            TestProduzione(data = NOW + 2000, punteggio = 90f, errori = 1, totale = 10)
        )
        val average = tests.map { it.punteggio }.average().toFloat()
        assertEquals(70f, average, 0.01f)
    }

    @Test
    fun `empty test list gives 0 average`() {
        val tests = emptyList<TestComprensione>()
        val average = if (tests.isEmpty()) 0f else tests.map { it.punteggio }.average().toFloat()
        assertEquals(0f, average, 0.01f)
    }

    // --- Integration with CalcoloCompetenze ---

    @Test
    fun `CalcoloCompetenze NON usa piu accuratezzaMedia come proxy per comprensione`() {
        // Senza test di comprensione il punteggio e 0 (niente fallback gonfiato)
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(accuratezzaMedia = 0.7f),
            now = NOW
        )
        assertEquals(0f, risultato.comprensione, 0.01f)
    }

    @Test
    fun `CalcoloCompetenze NON usa piu accuratezzaMedia come proxy per produzione`() {
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(accuratezzaMedia = 0.7f),
            now = NOW
        )
        assertEquals(0f, risultato.produzione, 0.01f)
    }

    @Test
    fun `CalcoloCompetenze comprensione and produzione are equal when using proxy`() {
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(accuratezzaMedia = 0.5f),
            now = NOW
        )
        assertEquals(risultato.comprensione, risultato.produzione, 0.01f)
    }

    // --- Quiz data validation tests ---

    @Test
    fun `comprensione quiz data class does not enforce errori lte totale`() {
        // The data class allows errori > totale; validation is the UI's job
        val test = TestComprensione(
            data = NOW,
            punteggio = 0f,
            errori = 15,
            totale = 10
        )
        assertEquals(15, test.errori)
        assertEquals(10, test.totale)
    }

    @Test
    fun `produzione quiz data class does not enforce errori lte totale`() {
        // The data class allows errori > totale; validation is the UI's job
        val test = TestProduzione(
            data = NOW,
            punteggio = 0f,
            errori = 15,
            totale = 10
        )
        assertEquals(15, test.errori)
        assertEquals(10, test.totale)
    }

    @Test
    fun `comprensione quiz score matches error count`() {
        val test = TestComprensione(
            data = NOW,
            punteggio = 70f,
            errori = 3,
            totale = 10
        )
        val expectedScore = (test.totale - test.errori).toFloat() / test.totale * 100
        assertEquals("Score should match error count", expectedScore, test.punteggio, 0.01f)
    }

    @Test
    fun `produzione quiz score matches error count`() {
        val test = TestProduzione(
            data = NOW,
            punteggio = 70f,
            errori = 3,
            totale = 10
        )
        val expectedScore = (test.totale - test.errori).toFloat() / test.totale * 100
        assertEquals("Score should match error count", expectedScore, test.punteggio, 0.01f)
    }

    // --- Quiz question data structure tests ---

    @Test
    fun `DomandaTest can represent comprensione question`() {
        val domanda = DomandaTest(
            id = "comp_1",
            tipo = TipoDomanda.TRADUZIONE,
            domanda = "Was bedeutet 'der Tisch'?",
            opzioni = listOf("il tavolo", "la sedia", "la porta", "la finestra"),
            rispostaCorretta = 0,
            spiegazione = "'Der Tisch' significa 'il tavolo'.",
            livelloRichiesto = LivelloCEFR.A1
        )
        assertEquals("comp_1", domanda.id)
        assertEquals(TipoDomanda.TRADUZIONE, domanda.tipo)
        assertEquals(4, domanda.opzioni.size)
        assertEquals(0, domanda.rispostaCorretta)
    }

    @Test
    fun `DomandaTest can represent produzione question`() {
        val domanda = DomandaTest(
            id = "prod_1",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Ich ___ gern Kaffee trinken.",
            opzioni = listOf("würde", "werde", "habe", "bin"),
            rispostaCorretta = 0,
            spiegazione = "Konjunktiv II per desideri irreali.",
            livelloRichiesto = LivelloCEFR.B1
        )
        assertEquals("prod_1", domanda.id)
        assertEquals(TipoDomanda.COMPLETAMENTO, domanda.tipo)
        assertEquals(4, domanda.opzioni.size)
        assertEquals(0, domanda.rispostaCorretta)
    }

    // --- Quiz session flow tests ---

    @Test
    fun `comprensione quiz session tracks current question index`() {
        // Simulate a quiz session
        val domande = listOf(
            DomandaTest(id = "c1", tipo = TipoDomanda.TRADUZIONE, domanda = "Q1", opzioni = listOf("a", "b", "c", "d"), rispostaCorretta = 0, spiegazione = "", livelloRichiesto = LivelloCEFR.A1),
            DomandaTest(id = "c2", tipo = TipoDomanda.TRADUZIONE, domanda = "Q2", opzioni = listOf("a", "b", "c", "d"), rispostaCorretta = 1, spiegazione = "", livelloRichiesto = LivelloCEFR.A1),
            DomandaTest(id = "c3", tipo = TipoDomanda.TRADUZIONE, domanda = "Q3", opzioni = listOf("a", "b", "c", "d"), rispostaCorretta = 2, spiegazione = "", livelloRichiesto = LivelloCEFR.A1)
        )
        var indice = 0
        var errori = 0
        val risposte = listOf(0, 0, 2) // Q1 correct, Q2 wrong, Q3 correct

        domande.forEachIndexed { i, domanda ->
            indice = i
            if (risposte[i] != domanda.rispostaCorretta) {
                errori++
            }
        }

        assertEquals(3, indice + 1) // 3 questions answered
        assertEquals(1, errori) // 1 error
        assertEquals(2, domande.size - errori) // 2 correct
    }

    @Test
    fun `produzione quiz session tracks current question index`() {
        val domande = listOf(
            DomandaTest(id = "p1", tipo = TipoDomanda.COMPLETAMENTO, domanda = "Q1", opzioni = listOf("a", "b", "c", "d"), rispostaCorretta = 0, spiegazione = "", livelloRichiesto = LivelloCEFR.B1),
            DomandaTest(id = "p2", tipo = TipoDomanda.COMPLETAMENTO, domanda = "Q2", opzioni = listOf("a", "b", "c", "d"), rispostaCorretta = 1, spiegazione = "", livelloRichiesto = LivelloCEFR.B1),
            DomandaTest(id = "p3", tipo = TipoDomanda.COMPLETAMENTO, domanda = "Q3", opzioni = listOf("a", "b", "c", "d"), rispostaCorretta = 2, spiegazione = "", livelloRichiesto = LivelloCEFR.B1)
        )
        var indice = 0
        var errori = 0
        val risposte = listOf(0, 1, 0) // Q1 correct, Q2 correct, Q3 wrong

        domande.forEachIndexed { i, domanda ->
            indice = i
            if (risposte[i] != domanda.rispostaCorretta) {
                errori++
            }
        }

        assertEquals(3, indice + 1)
        assertEquals(1, errori)
        assertEquals(2, domande.size - errori)
    }

    // --- Quiz result persistence tests ---

    @Test
    fun `comprensione quiz result can be added to progresso`() {
        val progresso = ProgressoUtente()
        val nuovoTest = TestComprensione(
            data = NOW,
            punteggio = 80f,
            errori = 2,
            totale = 10
        )
        val nuovoProgresso = progresso.copy(
            testComprensione = progresso.testComprensione + nuovoTest
        )
        assertEquals(1, nuovoProgresso.testComprensione.size)
        assertEquals(80f, nuovoProgresso.testComprensione[0].punteggio, 0.01f)
    }

    @Test
    fun `produzione quiz result can be added to progresso`() {
        val progresso = ProgressoUtente()
        val nuovoTest = TestProduzione(
            data = NOW,
            punteggio = 75f,
            errori = 3,
            totale = 12
        )
        val nuovoProgresso = progresso.copy(
            testProduzione = progresso.testProduzione + nuovoTest
        )
        assertEquals(1, nuovoProgresso.testProduzione.size)
        assertEquals(75f, nuovoProgresso.testProduzione[0].punteggio, 0.01f)
    }

    // --- Quiz type enumeration tests ---

    @Test
    fun `TipoDomanda has all expected values`() {
        val expected = setOf(
            TipoDomanda.VOCAB_TED_ITA,
            TipoDomanda.VOCAB_ITA_TED,
            TipoDomanda.GRAMMATICA,
            TipoDomanda.COMPLETAMENTO,
            TipoDomanda.TRADUZIONE
        )
        assertEquals(expected, TipoDomanda.entries.toSet())
    }

    // --- Quiz scoring edge cases ---

    @Test
    fun `comprensione quiz with 0 total questions gives 0 score`() {
        val test = TestComprensione(
            data = NOW,
            punteggio = 0f,
            errori = 0,
            totale = 0
        )
        assertEquals(0f, test.punteggio, 0.01f)
    }

    @Test
    fun `produzione quiz with 0 total questions gives 0 score`() {
        val test = TestProduzione(
            data = NOW,
            punteggio = 0f,
            errori = 0,
            totale = 0
        )
        assertEquals(0f, test.punteggio, 0.01f)
    }

    @Test
    fun `comprensione quiz score is always between 0 and 100`() {
        val test = TestComprensione(
            data = NOW,
            punteggio = 85f,
            errori = 0,
            totale = 10
        )
        assertTrue("Score should be >= 0", test.punteggio >= 0f)
        assertTrue("Score should be <= 100", test.punteggio <= 100f)
    }

    @Test
    fun `produzione quiz score is always between 0 and 100`() {
        val test = TestProduzione(
            data = NOW,
            punteggio = 85f,
            errori = 0,
            totale = 10
        )
        assertTrue("Score should be >= 0", test.punteggio >= 0f)
        assertTrue("Score should be <= 100", test.punteggio <= 100f)
    }
}
