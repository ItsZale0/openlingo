package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PercorsoGiornalieroTest {

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

    // 1. Percorso con parole in scadenza -> primo passo è ripasso
    @Test
    fun `parole in scadenza generano passo ripasso come primo`() {
        val s = stats(daRipassare = 15)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 15,
            livelloCorrente = LivelloCEFR.A1
        )
        assertEquals(
            CalcoloPercorsoGiornaliero.TipoPasso.RIPASSO,
            percorso.passi.first().tipo
        )
        assertTrue("Con 15 parole dovrebbe essere urgente", percorso.passi.first().urgente)
    }

    // 2. Percorso con grammatica sotto soglia -> passo grammatica presente
    @Test
    fun `grammatica sotto 60 genera passo grammatica`() {
        val s = stats(accuratezza = 0.35f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        val grammatica = percorso.passi.firstOrNull { it.tipo == CalcoloPercorsoGiornaliero.TipoPasso.GRAMMATICA }
        assertNotNull("Dovrebbe esserci un passo grammatica", grammatica)
        assertTrue("Con 35% dovrebbe essere urgente", grammatica!!.urgente)
    }

    // 3. Percorso con tutto sotto soglia -> multiple passi
    @Test
    fun `tutto sotto soglia genera multiple passi`() {
        val s = stats(accuratezza = 0.3f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 5,
            livelloCorrente = LivelloCEFR.A0
        )
        assertTrue("Dovrebbero esserci almeno 3 passi", percorso.passi.size >= 3)
        assertEquals(
            CalcoloPercorsoGiornaliero.TipoPasso.RIPASSO,
            percorso.passi[0].tipo
        )
    }

    // 4. Percorso con tutto sopra soglia -> solo sessione o complimento
    @Test
    fun `tutto sopra soglia genera percorso con sessione`() {
        val s = stats(accuratezza = 0.9f, paroleApprese = 100, daRipassare = 0)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.B1
        )
        assertTrue("Dovrebbe esserci almeno un passo", percorso.passi.isNotEmpty())
        assertTrue(
            "Il messaggio dovrebbe essere positivo",
            percorso.messaggioMotivazionale.contains("ottimi progressi") ||
                percorso.messaggioMotivazionale.contains("Buona sessione")
        )
    }

    // 5. Percorso con vocabolario sotto 50 -> passo vocabolario
    @Test
    fun `vocabolario sotto 50 genera passo vocabolario`() {
        val s = stats(paroleTotali = 100, paroleApprese = 10)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        val vocabolario = percorso.passi.firstOrNull { it.tipo == CalcoloPercorsoGiornaliero.TipoPasso.VOCABOLARIO }
        assertNotNull("Dovrebbe esserci un passo vocabolario", vocabolario)
    }

    // 6. Percorso con streak alto -> messaggio motivazionale speciale
    @Test
    fun `streak alto genera messaggio motivazionale speciale`() {
        val s = stats(streakGiorni = 10, accuratezza = 0.8f, daRipassare = 0)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        assertTrue("Dovrebbe menzionare lo streak", percorso.messaggioMotivazionale.contains("10"))
    }

    // 7. Percorso con prossimo obiettivo basato su livello
    @Test
    fun `prossimo obiettivo basato su livello A0`() {
        val s = stats()
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A0
        )
        assertTrue("Obiettivo dovrebbe menzionare A1", percorso.prossimoObiettivo.contains("A1"))
    }

    // 8. Percorso con prossimo obiettivo basato su livello B1
    @Test
    fun `prossimo obiettivo basato su livello B1`() {
        val s = stats()
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.B1
        )
        assertTrue("Obiettivo dovrebbe menzionare B2", percorso.prossimoObiettivo.contains("B2"))
    }

    // 9. Percorso con test di livello vicino all'obiettivo
    @Test
    fun `progresso obiettivo alto genera passo test livello`() {
        val s = stats(accuratezza = 0.9f, paroleTotali = 1500, paroleApprese = 1500, daRipassare = 0)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A2,
            obiettivoLivello = LivelloCEFR.B1
        )
        val testLivello = percorso.passi.firstOrNull { it.tipo == CalcoloPercorsoGiornaliero.TipoPasso.TEST_LIVELLO }
        assertNotNull("Dovrebbe esserci un passo test livello", testLivello)
    }

    // 10. Percorso con passi completati -> tracking corretto
    @Test
    fun `passi completati vengono tracciati correttamente`() {
        val s = stats(daRipassare = 10, accuratezza = 0.4f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 10,
            livelloCorrente = LivelloCEFR.A1,
            passiCompletatiOggi = setOf("ripasso")
        )
        val ripasso = percorso.passi.first { it.tipo == CalcoloPercorsoGiornaliero.TipoPasso.RIPASSO }
        assertTrue("Il passo ripasso dovrebbe essere completato", ripasso.completato)
        assertEquals(1, percorso.passiCompletati)
        assertTrue("I minuti completati dovrebbero essere > 0", percorso.minutiCompletati > 0)
    }

    // 11. Percorso con completaPasso -> aggiornamento corretto
    @Test
    fun `completaPasso aggiorna il percorso`() {
        val s = stats(daRipassare = 10, accuratezza = 0.4f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 10,
            livelloCorrente = LivelloCEFR.A1
        )
        val aggiornato = CalcoloPercorsoGiornaliero.completaPasso(percorso, "ripasso")
        assertEquals(1, aggiornato.passiCompletati)
        assertTrue("Il progresso dovrebbe essere > 0", aggiornato.progressoGiornata > 0f)
    }

    // 12. Percorso con resetGiorno -> tutti i passi non completati
    @Test
    fun `resetGiorno resetta tutti i passi`() {
        val s = stats(daRipassare = 10, accuratezza = 0.4f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 10,
            livelloCorrente = LivelloCEFR.A1,
            passiCompletatiOggi = setOf("ripasso", "grammatica")
        )
        val resettato = CalcoloPercorsoGiornaliero.resetGiorno(percorso)
        assertEquals(0, resettato.passiCompletati)
        assertEquals(0, resettato.minutiCompletati)
        assertTrue("Tutti i passi dovrebbero essere non completati", resettato.passi.all { !it.completato })
    }

    // 13. Percorso con tutti i passi completati -> percorsoCompletato true
    @Test
    fun `percorso con tutti i passi completati`() {
        val s = stats(daRipassare = 10, accuratezza = 0.4f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 10,
            livelloCorrente = LivelloCEFR.A1,
            passiCompletatiOggi = setOf("ripasso", "grammatica", "comprensione", "produzione", "vocabolario")
        )
        assertTrue("Il percorso dovrebbe essere completato", percorso.percorsoCompletato)
        assertEquals(1f, percorso.progressoGiornata, 0.01f)
    }

    // 14. Percorso con prossimoPasso -> primo passo non completato
    @Test
    fun `prossimoPasso restituisce il primo passo non completato`() {
        val s = stats(daRipassare = 10, accuratezza = 0.4f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 10,
            livelloCorrente = LivelloCEFR.A1,
            passiCompletatiOggi = setOf("ripasso")
        )
        val prossimo = percorso.prossimoPasso
        assertNotNull("Dovrebbe esserci un prossimo passo", prossimo)
        assertFalse("Il prossimo passo non dovrebbe essere completato", prossimo!!.completato)
    }

    // 15. Percorso con minutiTotali -> somma corretta
    @Test
    fun `minutiTotali è la somma dei minuti dei passi`() {
        val s = stats(daRipassare = 10, accuratezza = 0.4f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 10,
            livelloCorrente = LivelloCEFR.A1
        )
        val somma = percorso.passi.sumOf { it.stimaMinuti }
        assertEquals(somma, percorso.minutiTotali)
    }

    // 16. Percorso con comprensione sotto soglia -> passo comprensione presente
    @Test
    fun `comprensione sotto 60 genera passo comprensione`() {
        val s = stats(accuratezza = 0.5f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        val comprensione = percorso.passi.firstOrNull { it.tipo == CalcoloPercorsoGiornaliero.TipoPasso.COMPRENSIONE }
        assertNotNull("Dovrebbe esserci un passo comprensione", comprensione)
    }

    // 17. Percorso con produzione sotto soglia -> passo produzione presente
    @Test
    fun `produzione sotto 60 genera passo produzione`() {
        val s = stats(accuratezza = 0.5f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        val produzione = percorso.passi.firstOrNull { it.tipo == CalcoloPercorsoGiornaliero.TipoPasso.PRODUZIONE }
        assertNotNull("Dovrebbe esserci un passo produzione", produzione)
    }

    // 18. Percorso con sessione guidata quando non c'è altro
    @Test
    fun `nessuna priorità genera sessione guidata`() {
        val s = stats(accuratezza = 0.9f, paroleTotali = 1000, paroleApprese = 1000, daRipassare = 0)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.B1
        )
        assertTrue(
            "Dovrebbe esserci almeno un passo sessione",
            percorso.passi.any { it.tipo == CalcoloPercorsoGiornaliero.TipoPasso.SESSIONE_GUIDATA }
        )
    }

    // 19. Percorso con data formattata correttamente
    @Test
    fun `data del percorso è formattata correttamente`() {
        val s = stats()
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1
        )
        assertTrue("La data dovrebbe essere non vuota", percorso.data.isNotBlank())
    }

    // 20. Percorso con priorità ordinate correttamente
    @Test
    fun `priorità sono ordinate correttamente`() {
        val s = stats(accuratezza = 0.3f, daRipassare = 20)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 20,
            livelloCorrente = LivelloCEFR.A0
        )
        val priorita = percorso.passi.map { it.priorita }
        assertEquals("Le priorità dovrebbero essere sequenziali", (1..priorita.size).toList(), priorita)
    }

    // 21. Il piano resta dentro il budget di circa 20 minuti
    @Test
    fun `il piano rispetta il budget di ventidue minuti`() {
        val s = stats(accuratezza = 0.2f, daRipassare = 40)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 40,
            livelloCorrente = LivelloCEFR.A0
        )
        assertTrue(
            "Il piano non deve superare ${CalcoloPercorsoGiornaliero.MINUTI_MASSIMI} minuti",
            percorso.minutiTotali <= CalcoloPercorsoGiornaliero.MINUTI_MASSIMI
        )
        assertTrue(
            "Il piano non deve avere più di ${CalcoloPercorsoGiornaliero.MAX_PASSI} passi",
            percorso.passi.size <= CalcoloPercorsoGiornaliero.MAX_PASSI
        )
    }

    @Test
    fun `le descrizioni mostrano la durata effettiva dopo il taglio del budget`() {
        val s = stats(accuratezza = 0.2f, daRipassare = 40)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 40,
            livelloCorrente = LivelloCEFR.A0
        )
        percorso.passi.forEach { passo ->
            Regex("""~(\d+) min""").find(passo.descrizione)?.let { match ->
                assertEquals(
                    "La descrizione di '${passo.id}' non corrisponde alla durata effettiva",
                    passo.stimaMinuti,
                    match.groupValues[1].toInt()
                )
            }
        }
    }

    // 22. La grammatica apre direttamente la prossima unità
    @Test
    fun `il passo grammatica apre la prossima unita del percorso`() {
        val s = stats(accuratezza = 0.5f)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.A1,
            prossimaUnitaId = "a0-alfabeto",
            prossimaUnitaTitolo = "Alfabeto, suoni e pronuncia"
        )
        val grammatica = percorso.passi.firstOrNull {
            it.tipo == CalcoloPercorsoGiornaliero.TipoPasso.GRAMMATICA
        }
        assertNotNull("Dovrebbe esserci il passo grammatica", grammatica)
        assertEquals("unita_grammatica/a0-alfabeto", grammatica!!.azione)
        assertTrue(grammatica.descrizione.contains("Alfabeto"))
    }

    // 23. La prossima unità resta nel piano anche con competenze alte
    @Test
    fun `la prossima unita entra nel piano anche con competenze alte`() {
        val s = stats(accuratezza = 0.9f, paroleTotali = 1500, paroleApprese = 1500)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 0,
            livelloCorrente = LivelloCEFR.B1,
            prossimaUnitaId = "b1-konnektoren",
            prossimaUnitaTitolo = "I connettori"
        )
        assertTrue(
            "La prossima unità deve restare nel piano",
            percorso.passi.any { it.azione == "unita_grammatica/b1-konnektoren" }
        )
        assertTrue(percorso.minutiTotali <= CalcoloPercorsoGiornaliero.MINUTI_MASSIMI)
    }

    // 24. Nessun passo duplicato e azioni sempre valorizzate
    @Test
    fun `i passi non sono duplicati e hanno sempre un azione`() {
        val s = stats(accuratezza = 0.2f, daRipassare = 12)
        val percorso = CalcoloPercorsoGiornaliero.genera(
            statistiche = s,
            competenze = s.punteggiCompetenze!!,
            daRipassare = 12,
            livelloCorrente = LivelloCEFR.A1,
            prossimaUnitaId = "a1-praesens",
            prossimaUnitaTitolo = "Il presente"
        )
        assertEquals(
            "Passi duplicati nel piano",
            percorso.passi.size,
            percorso.passi.map { it.id }.distinct().size
        )
        assertTrue(
            "Ogni passo deve avere un'azione navigabile",
            percorso.passi.all { it.azione.isNotBlank() }
        )
    }
}
