package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.TipoDomanda
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveTestEngineTest {

    private val NOW = 1_700_000_000_000L

    private fun parola(id: String, german: String = "w$id", italian: String = "p$id", archived: Boolean = false) = WordEntity(
        id = id,
        german = german,
        italian = italian,
        archived = archived
    )

    private fun review(wordId: String, repetitions: Int, lapses: Int = 0) = ReviewEntity(
        wordId = wordId,
        repetitions = repetitions,
        lapses = lapses
    )

    // 1. Generazione base con configurazione di default
    @Test
    fun `genera domande con configurazione default`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaDomande(
            parole = parole,
            reviews = reviews,
            progresso = progresso
        )

        assertTrue("Dovrebbe generare domande", risultato.domande.isNotEmpty())
        assertTrue("Dovrebbe generare almeno 8 domande", risultato.domande.size >= 8)
        assertEquals(LivelloCEFR.A1, risultato.livelloEffettivo)
    }

    // 2. Tutte le domande hanno 4 opzioni
    @Test
    fun `tutte le domande hanno 4 opzioni`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaDomande(parole, reviews, progresso)

        risultato.domande.forEach { domanda ->
            assertEquals("Domanda ${domanda.id} deve avere 4 opzioni", 4, domanda.opzioni.size)
        }
    }

    // 3. Risposta corretta è valida
    @Test
    fun `risposta corretta è valida`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaDomande(parole, reviews, progresso)

        risultato.domande.forEach { domanda ->
            assertTrue(
                "Domanda ${domanda.id}: risposta corretta ${domanda.rispostaCorretta} fuori range",
                domanda.rispostaCorretta in 0 until domanda.opzioni.size
            )
        }
    }

    // 4. Domande basate sugli errori grammaticali
    @Test
    fun `domande basate su errori grammaticali`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(
            livelloCorrente = LivelloCEFR.A1,
            erroriGrammatica = listOf("Ich ___ gestern ins Kino gegangen.")
        )

        val risultato = AdaptiveTestEngine.generaDomande(parole, reviews, progresso)

        assertTrue("Dovrebbe includere domande di grammatica", risultato.categorieCoperture.contains("Grammatica"))
    }

    // 5. Domande basate sulle parole deboli
    @Test
    fun `domande basate su parole deboli`() {
        val parole = (1..20).map { parola(it.toString()) }
        // Parole deboli: poche ripetizioni
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 1) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaDomande(parole, reviews, progresso)

        assertTrue("Dovrebbe includere domande di vocabolario", risultato.categorieCoperture.contains("Vocabolario"))
        assertTrue("Dovrebbe coprire parole deboli", risultato.paroleCoperte.isNotEmpty())
    }

    // 6. Livello adattivo: accuratezza alta -> sale di livello
    @Test
    fun `livello adattivo sale con accuratezza alta`() {
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val nuovoLivello = AdaptiveTestEngine.calcolaLivelloAdattivo(
            progresso = progresso,
            domandeTotali = 10,
            errori = 0
        )
        assertEquals(LivelloCEFR.A2, nuovoLivello)
    }

    // 7. Livello adattivo: accuratezza bassa -> scende di livello
    @Test
    fun `livello adattivo scende con accuratezza bassa`() {
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.B1)
        val nuovoLivello = AdaptiveTestEngine.calcolaLivelloAdattivo(
            progresso = progresso,
            domandeTotali = 10,
            errori = 8
        )
        assertEquals(LivelloCEFR.A2, nuovoLivello)
    }

    // 8. Livello adattivo: accuratezza media -> resta uguale
    @Test
    fun `livello adattivo resta uguale con accuratezza media`() {
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val nuovoLivello = AdaptiveTestEngine.calcolaLivelloAdattivo(
            progresso = progresso,
            domandeTotali = 10,
            errori = 3
        )
        assertEquals(LivelloCEFR.A1, nuovoLivello)
    }

    // 9. Configurazione personalizzata: solo grammatica
    @Test
    fun `configurazione solo grammatica`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaDomande(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            config = AdaptiveTestEngine.ConfigurazioneTest(
                numeroDomande = 10,
                includeGrammatica = true,
                includeVocabolario = false,
                includeComprensione = false,
                includeProduzione = false
            )
        )

        assertTrue("Dovrebbe includere solo grammatica", risultato.categorieCoperture.contains("Grammatica"))
        assertTrue("Non dovrebbe includere vocabolario", !risultato.categorieCoperture.contains("Vocabolario"))
    }

    // 10. Seed per riproducibilità
    @Test
    fun `seed produce risultati riproducibili`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato1 = AdaptiveTestEngine.generaDomande(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            config = AdaptiveTestEngine.ConfigurazioneTest(seed = 42L)
        )

        val risultato2 = AdaptiveTestEngine.generaDomande(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            config = AdaptiveTestEngine.ConfigurazioneTest(seed = 42L)
        )

        assertEquals(risultato1.domande.map { it.id }, risultato2.domande.map { it.id })
    }

    // 11. Livello effettivo corretto
    @Test
    fun `livello effettivo corretto`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.B1)

        val risultato = AdaptiveTestEngine.generaDomande(parole, reviews, progresso)

        assertEquals(LivelloCEFR.B1, risultato.livelloEffettivo)
    }

    // 12. Domande uniche (nessun duplicato)
    @Test
    fun `domande sono uniche`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaDomande(parole, reviews, progresso)

        val ids = risultato.domande.map { it.id }
        assertEquals("Le domande dovrebbero essere uniche", ids.size, ids.toSet().size)
    }

    // 13. Parole archiviate escluse
    @Test
    fun `parole archiviate escluse dalle domande`() {
        val parole = (1..20).map { parola(it.toString(), archived = it > 15) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 1) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaDomande(parole, reviews, progresso)

        // Le parole archiviate non dovrebbero essere coperte
        risultato.paroleCoperte.forEach { parola ->
            assertTrue("Parola archivata $parola non dovrebbe essere inclusa", parola !in listOf("w16", "w17", "w18", "w19", "w20"))
        }
    }

    // 14. Numero domande rispettato
    @Test
    fun `numero domande rispettato`() {
        val parole = (1..50).map { parola(it.toString()) }
        val reviews = (1..50).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaDomande(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            config = AdaptiveTestEngine.ConfigurazioneTest(numeroDomande = 15)
        )

        assertTrue("Dovrebbe generare almeno 10 domande", risultato.domande.size >= 10)
    }

    // 15. Categorie coperture non vuote
    @Test
    fun `categorie coperture non vuote`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaDomande(parole, reviews, progresso)

        assertTrue("Dovrebbe coprire almeno una categoria", risultato.categorieCoperture.isNotEmpty())
    }

    // 16. Generazione di test adattivi
    @Test
    fun `genera test adattivo con domande dinamiche`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaTestAdattivo(
            progresso = progresso,
            parole = parole,
            reviews = reviews
        )

        assertTrue("Dovrebbe generare almeno una domanda", risultato.isNotEmpty())
        risultato.forEach { domanda ->
            assertEquals("Ogni domanda deve avere 4 opzioni", 4, domanda.opzioni.size)
            assertTrue(
                "Risposta corretta valida per ${domanda.id}",
                domanda.rispostaCorretta in 0 until domanda.opzioni.size
            )
        }
    }

    // 17. Test adattivo con configurazione personalizzata
    @Test
    fun `test adattivo rispetta la configurazione`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato = AdaptiveTestEngine.generaTestAdattivo(
            progresso = progresso,
            parole = parole,
            reviews = reviews,
            config = AdaptiveTestEngine.ConfigurazioneTest(numeroDomande = 15, seed = 7L)
        )

        assertTrue("Dovrebbe generare domande", risultato.size >= 10)
    }

    // 18. Seed per riproducibilità del test adattivo
    @Test
    fun `test adattivo con seed è riproducibile`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)

        val risultato1 = AdaptiveTestEngine.generaTestAdattivo(
            progresso = progresso,
            parole = parole,
            reviews = reviews,
            config = AdaptiveTestEngine.ConfigurazioneTest(seed = 42L)
        )

        val risultato2 = AdaptiveTestEngine.generaTestAdattivo(
            progresso = progresso,
            parole = parole,
            reviews = reviews,
            config = AdaptiveTestEngine.ConfigurazioneTest(seed = 42L)
        )

        assertEquals(
            "Con la stessa seed i test devono essere identici",
            risultato1.map { it.id },
            risultato2.map { it.id }
        )
    }
}