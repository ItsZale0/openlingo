package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveSessionEngineTest {

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

    private fun competenze(
        lessico: Float = 50f,
        grammatica: Float = 50f,
        comprensione: Float = 50f,
        produzione: Float = 50f
    ) = CalcoloCompetenze.PunteggiCompetenze(
        lessico = lessico,
        grammatica = grammatica,
        comprensione = comprensione,
        produzione = produzione,
        livelloComplessivo = LivelloCEFR.A1
    )

    // 1. Generazione base con configurazione di default
    @Test
    fun `genera sessioni con configurazione default`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze()

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        assertTrue("Dovrebbe generare almeno una sessione", risultato.sessioni.isNotEmpty())
        assertEquals(LivelloCEFR.A1, risultato.livelloAttuale)
        assertTrue("Tempo totale positivo", risultato.tempoTotaleMinuti > 0)
    }

    // 2. Parole in scadenza -> sessione ripasso con priorità massima
    @Test
    fun `parole in scadenza generano sessione ripasso prioritaria`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze()

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 15,
            livelloCorrente = LivelloCEFR.A1
        )

        val ripasso = risultato.sessioni.first { it.tipo == AdaptiveSessionEngine.TipoSessione.RIPASSO }
        assertEquals(1, ripasso.priorita)
        assertTrue("Con 15 parole dovrebbe essere urgente", ripasso.urgente)
    }

    // 3. Grammatica sotto 60 -> sessione grammatica
    @Test
    fun `grammatica sotto 60 genera sessione grammatica`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze(grammatica = 35f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        val grammatica = risultato.sessioni.firstOrNull { it.tipo == AdaptiveSessionEngine.TipoSessione.GRAMMATICA }
        assertTrue("Dovrebbe esserci una sessione grammatica", grammatica != null)
        assertTrue("Con 35% dovrebbe essere urgente", grammatica!!.urgente)
        assertTrue("Dovrebbe essere nelle aree deboli", risultato.areeDeboli.contains("Grammatica"))
    }

    // 4. Grammatica sopra 60 -> nessuna sessione grammatica
    @Test
    fun `grammatica sopra 60 non genera sessione grammatica`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze(grammatica = 80f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        val grammatica = risultato.sessioni.firstOrNull { it.tipo == AdaptiveSessionEngine.TipoSessione.GRAMMATICA }
        assertTrue("Non dovrebbe esserci una sessione grammatica", grammatica == null)
    }

    // 5. Comprensione sotto 60 -> sessione comprensione
    @Test
    fun `comprensione sotto 60 genera sessione comprensione`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze(comprensione = 45f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        val comprensione = risultato.sessioni.firstOrNull { it.tipo == AdaptiveSessionEngine.TipoSessione.COMPRENSIONE }
        assertTrue("Dovrebbe esserci una sessione comprensione", comprensione != null)
    }

    // 6. Produzione sotto 60 -> sessione produzione
    @Test
    fun `produzione sotto 60 genera sessione produzione`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze(produzione = 35f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        val produzione = risultato.sessioni.firstOrNull { it.tipo == AdaptiveSessionEngine.TipoSessione.PRODUZIONE }
        assertTrue("Dovrebbe esserci una sessione produzione", produzione != null)
    }

    // 7. Lessico sotto 50 -> sessione vocabolario
    @Test
    fun `lessico sotto 50 genera sessione vocabolario`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze(lessico = 30f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        val vocabolario = risultato.sessioni.firstOrNull { it.tipo == AdaptiveSessionEngine.TipoSessione.VOCABOLARIO }
        assertTrue("Dovrebbe esserci una sessione vocabolario", vocabolario != null)
    }

    // 8. Tutto sotto soglia -> multiple sessioni
    @Test
    fun `tutto sotto soglia genera multiple sessioni`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A0)
        val comp = competenze(lessico = 20f, grammatica = 30f, comprensione = 25f, produzione = 20f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 5,
            livelloCorrente = LivelloCEFR.A0
        )

        assertTrue("Dovrebbero esserci almeno 3 sessioni", risultato.sessioni.size >= 3)
        assertEquals("La prima dovrebbe essere ripasso", AdaptiveSessionEngine.TipoSessione.RIPASSO, risultato.sessioni[0].tipo)
    }

    // 9. Tutto sopra soglia -> sessione guidata
    @Test
    fun `tutto sopra soglia genera sessione guidata`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.B1)
        val comp = competenze(lessico = 80f, grammatica = 80f, comprensione = 80f, produzione = 80f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.B1
        )

        assertTrue("Dovrebbe esserci almeno una sessione", risultato.sessioni.isNotEmpty())
        assertTrue("Il messaggio dovrebbe essere positivo",
            risultato.messaggioMotivazionale.contains("Ottimo") ||
            risultato.messaggioMotivazionale.contains("Prosegui") ||
            risultato.messaggioMotivazionale.contains("Buona"))
    }

    // 10. Priorità ordinate correttamente
    @Test
    fun `priorita sono ordinate correttamente`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A0)
        val comp = competenze(lessico = 20f, grammatica = 30f, comprensione = 25f, produzione = 20f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 20,
            livelloCorrente = LivelloCEFR.A0
        )

        val priorita = risultato.sessioni.map { it.priorita }
        assertEquals("Le priorità dovrebbero essere sequenziali", (1..priorita.size).toList(), priorita)
    }

    // 11. Livello adattivo: progresso verso obiettivo genera test livello
    @Test
    fun `progresso verso obiettivo genera test livello`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A2)
        val comp = competenze(lessico = 80f, grammatica = 80f, comprensione = 80f, produzione = 80f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A2,
            obiettivoLivello = LivelloCEFR.B1
        )

        val testLivello = risultato.sessioni.firstOrNull { it.tipo == AdaptiveSessionEngine.TipoSessione.TEST_LIVELLO }
        assertTrue("Dovrebbe esserci un test di livello", testLivello != null)
    }

    // 12. Roleplay per livello sufficiente
    @Test
    fun `roleplay generato per livello A1 o superiore`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze(grammatica = 50f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        val roleplay = risultato.sessioni.firstOrNull { it.tipo == AdaptiveSessionEngine.TipoSessione.ROLEPLAY }
        assertTrue("Dovrebbe esserci un roleplay per A1", roleplay != null)
    }

    // 13. Roleplay non generato per livello A0
    @Test
    fun `roleplay non generato per livello A0`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A0)
        val comp = competenze(grammatica = 30f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A0
        )

        val roleplay = risultato.sessioni.firstOrNull { it.tipo == AdaptiveSessionEngine.TipoSessione.ROLEPLAY }
        assertTrue("Non dovrebbe esserci un roleplay per A0", roleplay == null)
    }

    // 14. Sessione ripasso parole deboli
    @Test
    fun `genera sessione ripasso parole deboli`() {
        val parole = (1..20).map { parola(it.toString()) }
        // Parole deboli: poche ripetizioni
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 1) }

        val sessione = AdaptiveSessionEngine.generaSessioneRipasso(
            parole = parole,
            reviews = reviews,
            maxParole = 10
        )

        assertEquals(AdaptiveSessionEngine.TipoSessione.RIPASSO, sessione.tipo)
        assertEquals(10, sessione.paroleTarget)
        assertTrue("Dovrebbe essere urgente con tante parole deboli", sessione.urgente)
    }

    // 15. Sessione grammatica errori
    @Test
    fun `genera sessione grammatica errori`() {
        val errori = listOf(
            "Ich ___ gestern ins Kino gegangen.",
            "Er ___ die Hausaufgaben gemacht.",
            "Wir ___ nach Deutschland reisen."
        )

        val sessione = AdaptiveSessionEngine.generaSessioneGrammatica(
            erroriGrammatica = errori,
            livello = LivelloCEFR.A1
        )

        assertEquals(AdaptiveSessionEngine.TipoSessione.GRAMMATICA, sessione.tipo)
        assertEquals(3, sessione.descrizione.count { it.isDigit() })
    }

    // 16. Parole archiviate escluse
    @Test
    fun `parole archiviate escluse dalle sessioni`() {
        val parole = (1..20).map { parola(it.toString(), archived = it > 15) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 1) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze()

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        // Le parole archiviate non dovrebbero essere coperte
        risultato.sessioni.forEach { sessione ->
            if (sessione.paroleTarget > 0) {
                assertTrue("Parole archiviate non dovrebbero essere incluse", sessione.paroleTarget <= 15)
            }
        }
    }

    // 17. Durata sessione ragionevole
    @Test
    fun `durata sessione ragionevole`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze()

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        risultato.sessioni.forEach { sessione ->
            assertTrue("Durata minima 5 min", sessione.durataMinuti >= 5)
            assertTrue("Durata massima 30 min", sessione.durataMinuti <= 30)
        }
    }

    // 18. Azione sempre presente
    @Test
    fun `azione sempre presente`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze()

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        risultato.sessioni.forEach { sessione ->
            assertTrue("Azione non vuota", sessione.azione.isNotBlank())
        }
    }

    // 19. Streak alto -> messaggio motivazionale speciale
    @Test
    fun `streak alto genera messaggio motivazionale speciale`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1, streakGiorni = 10)
        val comp = competenze(grammatica = 80f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        assertTrue("Dovrebbe menzionare lo streak", risultato.messaggioMotivazionale.contains("10"))
    }

    // 20. Aree deboli identificate correttamente
    @Test
    fun `aree deboli identificate correttamente`() {
        val parole = (1..20).map { parola(it.toString()) }
        val reviews = (1..20).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A1)
        val comp = competenze(lessico = 30f, grammatica = 40f, comprensione = 50f, produzione = 45f)

        val risultato = AdaptiveSessionEngine.generaSessioni(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            competenze = comp,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )

        assertTrue("Dovrebbe identificare Grammatica", risultato.areeDeboli.contains("Grammatica"))
        assertTrue("Dovrebbe identificare Lessico", risultato.areeDeboli.contains("Lessico"))
        assertTrue("Dovrebbe identificare Produzione", risultato.areeDeboli.contains("Produzione"))
    }
}
