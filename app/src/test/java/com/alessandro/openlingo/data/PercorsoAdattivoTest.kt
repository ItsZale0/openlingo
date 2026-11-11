package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PercorsoAdattivoTest {

    private val NOW = 1_700_000_000_000L

    private fun parola(id: String, archived: Boolean = false) = WordEntity(
        id = id,
        german = "w$id",
        italian = "p$id",
        archived = archived
    )

    private fun review(wordId: String, repetitions: Int, lapses: Int = 0) = ReviewEntity(
        wordId = wordId,
        repetitions = repetitions,
        lapses = lapses
    )

    private fun stats(
        paroleTotali: Int = 0,
        daRipassare: Int = 0,
        paroleApprese: Int = 0,
        accuratezza: Float = 0f,
        streakGiorni: Int = 0
    ): Statistiche {
        val parole = (1..paroleTotali).map { parola(it.toString()) }
        val reviews = (1..paroleTotali).associate { it.toString() to review(it.toString(), repetitions = 4) }
        return Statistiche(
            paroleTotali = paroleTotali,
            paroleSynced = paroleTotali,
            paroleCustom = 0,
            daRipassare = daRipassare,
            nuove = paroleTotali - paroleApprese,
            inApprendimento = 0,
            mature = paroleApprese,
            lezioniTotali = 1,
            streakGiorni = streakGiorni,
            accuratezza = accuratezza,
            ripassiTotali = paroleApprese * 4,
            erroriTotali = 0,
            paroleApprese = paroleApprese,
            obiettivoB1 = 2000,
            progressoB1 = paroleApprese.toFloat() / 2000f,
            livelloStimato = LivelloCEFR.A0,
            perLezione = emptyList(),
            ultimi7giorni = emptyList(),
            punteggiCompetenze = CalcoloCompetenze.PunteggiCompetenze(
                lessico = accuratezza * 100f,
                grammatica = accuratezza * 100f,
                comprensione = accuratezza * 100f,
                produzione = accuratezza * 100f,
                livelloComplessivo = LivelloCEFR.A0
            ),
            wortlisteUfficiale = 0,
            vocabolarioAttivoUfficiale = 0,
            oreGuidaCumulate = 0..0,
            praedikatVersoObiettivo = CertificazioniUfficiali.Prädikat.NICHT_BESTANDEN,
            progresso = emptyList()
        )
    }

    // 1. Parole in scadenza -> raccomandazione ripasso con priorità massima
    @Test
    fun `parole in scadenza generano raccomandazione ripasso`() {
        val s = stats(daRipassare = 15)
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 15,
            livelloCorrente = LivelloCEFR.A1
        )
        val ripasso = percorso.raccomandazioni.first { it.categoria == CalcoloPercorsoAdattivo.CategoriaRaccomandazione.RIPASSO }
        assertEquals(1, ripasso.priorita)
        assertTrue("Con 15 parole dovrebbe essere urgente", ripasso.urgente)
    }

    // 2. Grammatica sotto 60 -> raccomandazione grammatica
    @Test
    fun `grammatica sotto 60 genera raccomandazione`() {
        val s = stats(accuratezza = 0.4f)
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        val grammatica = percorso.raccomandazioni.firstOrNull { it.categoria == CalcoloPercorsoAdattivo.CategoriaRaccomandazione.GRAMMATICA }
        assertTrue("Dovrebbe esserci una raccomandazione grammatica", grammatica != null)
        assertTrue("Con 40% dovrebbe essere urgente", grammatica!!.urgente)
    }

    // 3. Grammatica sopra 60 -> nessuna raccomandazione grammatica
    @Test
    fun `grammatica sopra 60 non genera raccomandazione`() {
        val s = stats(accuratezza = 0.8f)
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        val grammatica = percorso.raccomandazioni.firstOrNull { it.categoria == CalcoloPercorsoAdattivo.CategoriaRaccomandazione.GRAMMATICA }
        assertTrue("Non dovrebbe esserci una raccomandazione grammatica", grammatica == null)
    }

    // 4. Tutto sotto soglia -> multiple raccomandazioni
    @Test
    fun `tutto sotto soglia genera multiple raccomandazioni`() {
        val s = stats(accuratezza = 0.3f)
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 5,
            livelloCorrente = LivelloCEFR.A0
        )
        assertTrue("Dovrebbero esserci almeno 3 raccomandazioni", percorso.raccomandazioni.size >= 3)
        assertTrue("La prima dovrebbe essere ripasso", percorso.raccomandazioni[0].categoria == CalcoloPercorsoAdattivo.CategoriaRaccomandazione.RIPASSO)
    }

    // 5. Tutto sopra soglia -> complimento
    @Test
    fun `tutto sopra soglia genera messaggio complimento`() {
        val s = stats(accuratezza = 0.9f, paroleApprese = 100, daRipassare = 0)
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.B1
        )
        assertTrue("Dovrebbe esserci almeno una raccomandazione", percorso.raccomandazioni.isNotEmpty())
        assertTrue("Il messaggio dovrebbe essere positivo", percorso.messaggioMotivazionale.contains("Ottimo") || percorso.messaggioMotivazionale.contains("Prosegui"))
    }

    // 6. Prossimo obiettivo basato sul livello
    @Test
    fun `prossimo obiettivo basato su livello A0`() {
        val s = stats()
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A0
        )
        assertTrue("Obiettivo dovrebbe menzionare A1", percorso.prossimoObiettivo.contains("A1"))
    }

    // 7. Prossimo obiettivo basato su livello B1
    @Test
    fun `prossimo obiettivo basato su livello B1`() {
        val s = stats()
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.B1
        )
        assertTrue("Obiettivo dovrebbe menzionare B2", percorso.prossimoObiettivo.contains("B2"))
    }

    // 8. Vocabolario sotto 50 -> raccomandazione nuove parole
    @Test
    fun `vocabolario sotto 50 genera raccomandazione nuove parole`() {
        val s = stats(paroleTotali = 100, paroleApprese = 10)
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        val vocabolario = percorso.raccomandazioni.firstOrNull { it.categoria == CalcoloPercorsoAdattivo.CategoriaRaccomandazione.VOCABOLARIO }
        assertTrue("Dovrebbe esserci una raccomandazione vocabolario", vocabolario != null)
    }

    // 9. Streak alto -> messaggio motivazionale speciale
    @Test
    fun `streak alto genera messaggio motivazionale speciale`() {
        val s = stats(streakGiorni = 10, accuratezza = 0.8f, daRipassare = 0)
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        assertTrue("Dovrebbe menzionare lo streak", percorso.messaggioMotivazionale.contains("10"))
    }

    // 10. Priorità ordinate correttamente
    @Test
    fun `priorita sono ordinate correttamente`() {
        val s = stats(accuratezza = 0.3f, daRipassare = 20)
        val percorso = CalcoloPercorsoAdattivo.calcola(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 20,
            livelloCorrente = LivelloCEFR.A0
        )
        val priorita = percorso.raccomandazioni.map { it.priorita }
        assertEquals("Le priorità dovrebbero essere sequenziali", (1..priorita.size).toList(), priorita)
    }
}
