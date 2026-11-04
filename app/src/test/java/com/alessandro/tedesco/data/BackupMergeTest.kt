package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProfiloConfig
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProgressoLezione
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.TipoProfilo
import com.alessandro.tedesco.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test dell'unione locale ↔ backup cloud: è il cuore della sincronizzazione
 * dei progressi fra telefono e desktop. Nessun test deve poter dire "sì, ma
 * se un dato manca dall'altra parte sparisce".
 */
class BackupMergeTest {

    private val NOW = 1_700_000_000_000L
    private val GIORNO = 86_400_000L

    private fun profilo(stato: ProfiloStato) = ProfiloUtente(
        id = "ale",
        config = ProfiloConfig(tipo = TipoProfilo.ALESSANDRO, nomeVisualizzato = "Alessandro"),
        stato = stato
    )

    private fun repo(stato: ProfiloStato) = ProfiliRepository(
        profili = mapOf("ale" to profilo(stato)),
        profiloAttivoId = "ale"
    )

    private fun carta(
        wordId: String,
        repetitions: Int,
        intervalDays: Int,
        last: Long
    ) = ReviewEntity(
        wordId = wordId,
        repetitions = repetitions,
        intervalDays = intervalDays,
        dueAt = last + intervalDays * GIORNO,
        lastReviewedAt = last
    )

    // ------------------------------------------------------------- lessico

    @Test
    fun `le parole presenti solo da una parte restano tutte`() {
        val locale = repo(
            ProfiloStato(parole = listOf(WordEntity(id = "a", german = "Haus", italian = "casa")))
        )
        val remoto = repo(
            ProfiloStato(parole = listOf(WordEntity(id = "b", german = "Baum", italian = "albero")))
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato

        assertEquals(listOf("a", "b"), unito.parole.map { it.id }.sorted())
        assertEquals("casa", unito.parole.first { it.id == "a" }.italian)
        assertEquals("albero", unito.parole.first { it.id == "b" }.italian)
    }

    @Test
    fun `per la stessa parola vince la carta ripassata piu' di recente`() {
        val locale = repo(
            ProfiloStato(reviews = mapOf("a" to carta("a", repetitions = 1, intervalDays = 2, last = NOW)))
        )
        val remoto = repo(
            ProfiloStato(
                reviews = mapOf(
                    "a" to carta("a", repetitions = 6, intervalDays = 40, last = NOW + GIORNO)
                )
            )
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato

        assertEquals(6, unito.reviews["a"]!!.repetitions)
        assertEquals(40, unito.reviews["a"]!!.intervalDays)
        assertEquals(NOW + GIORNO, unito.reviews["a"]!!.lastReviewedAt)
    }

    @Test
    fun `i log di ripasso si uniscono senza duplicare lo stesso evento`() {
        val evento = RipassoLogEntity("a", NOW, corretta = true, verifica = true)
        val locale = repo(ProfiloStato(ripassiLog = listOf(evento)))
        val remoto = repo(
            ProfiloStato(
                ripassiLog = listOf(
                    evento,
                    RipassoLogEntity("b", NOW + 10, corretta = false)
                )
            )
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato

        assertEquals(2, unito.ripassiLog.size)
        assertEquals(listOf(NOW, NOW + 10), unito.ripassiLog.map { it.timestamp })
    }

    // -------------------------------------------------------- grammatica

    @Test
    fun `la padronanza grammaticale completata vince su quella parziale`() {
        val locale = repo(
            ProfiloStato(
                padronanzaGrammatica = mapOf(
                    "a1-pronomi" to PadronanzaGrammatica(
                        unitaId = "a1-pronomi",
                        corrette = 8,
                        totali = 10,
                        percentuale = 80,
                        completata = true,
                        ultimaVolta = NOW,
                        ripassi = 1
                    )
                )
            )
        )
        val remoto = repo(
            ProfiloStato(
                padronanzaGrammatica = mapOf(
                    "a1-pronomi" to PadronanzaGrammatica(
                        unitaId = "a1-pronomi",
                        corrette = 4,
                        totali = 10,
                        percentuale = 40,
                        completata = false,
                        ultimaVolta = NOW + GIORNO,
                        ripassi = 5,
                        prossimaRipasso = NOW + 3 * GIORNO
                    )
                )
            )
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato
        val p = unito.padronanzaGrammatica["a1-pronomi"]!!

        assertTrue(p.completata)
        assertEquals(80, p.percentuale)
        assertEquals(8, p.corrette)
        // I ripassi fatti sull'altro dispositivo non si buttano via.
        assertEquals(5, p.ripassi)
        assertEquals(NOW + 3 * GIORNO, p.prossimaRipasso)
    }

    @Test
    fun `le unita' di grammatica presenti solo nel backup vengono aggiunte`() {
        val locale = repo(
            ProfiloStato(
                padronanzaGrammatica = mapOf(
                    "unita-a" to PadronanzaGrammatica(unitaId = "unita-a", percentuale = 50)
                )
            )
        )
        val remoto = repo(
            ProfiloStato(
                padronanzaGrammatica = mapOf(
                    "unita-b" to PadronanzaGrammatica(
                        unitaId = "unita-b",
                        corrette = 5,
                        totali = 5,
                        percentuale = 100,
                        completata = true
                    )
                )
            )
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato

        assertEquals(setOf("unita-a", "unita-b"), unito.padronanzaGrammatica.keys)
        assertTrue(unito.padronanzaGrammatica["unita-b"]!!.completata)
    }

    // ------------------------------------------------------------ lezioni

    @Test
    fun `la lezione riprende dal punto piu' avanti`() {
        val locale = repo(
            ProfiloStato(
                progressiLezioni = mapOf(
                    "lezione-1" to ProgressoLezione(indice = 2, corrette = 2, totali = 8, aggiornato = NOW)
                )
            )
        )
        val remoto = repo(
            ProfiloStato(
                progressiLezioni = mapOf(
                    "lezione-1" to ProgressoLezione(
                        indice = 6,
                        corrette = 5,
                        totali = 8,
                        aggiornato = NOW + GIORNO
                    )
                )
            )
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato
        val l = unito.progressiLezioni["lezione-1"]!!

        assertEquals(6, l.indice)
        assertEquals(5, l.corrette)
        assertEquals(8, l.totali)
        assertEquals(NOW + GIORNO, l.aggiornato)
    }

    @Test
    fun `una lezione completata non torna indietro`() {
        val locale = repo(
            ProfiloStato(
                progressiLezioni = mapOf(
                    "lezione-2" to ProgressoLezione(
                        indice = 12, corrette = 12, totali = 12, completata = true, aggiornato = NOW
                    )
                )
            )
        )
        // Backup vecchio: stessa lezione lasciata a metà.
        val remoto = repo(
            ProfiloStato(
                progressiLezioni = mapOf(
                    "lezione-2" to ProgressoLezione(indice = 4, corrette = 3, totali = 12, aggiornato = NOW)
                )
            )
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato

        assertTrue(unito.progressiLezioni["lezione-2"]!!.completata)
        assertEquals(12, unito.progressiLezioni["lezione-2"]!!.indice)
    }

    @Test
    fun `i punteggi delle lezioni tengono il massimo`() {
        val locale = repo(ProfiloStato(progresso = ProgressoUtente(punteggiLezioni = mapOf(1 to 70, 2 to 50))))
        val remoto = repo(ProfiloStato(progresso = ProgressoUtente(punteggiLezioni = mapOf(1 to 90, 3 to 60))))

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato.progresso

        assertEquals(mapOf(1 to 90, 2 to 50, 3 to 60), unito.punteggiLezioni)
    }

    // ----------------------------------------------------------- progresso

    @Test
    fun `il livello piu' alto vince ma l'obiettivo resta quello locale`() {
        val locale = repo(
            ProfiloStato(
                progresso = ProgressoUtente(
                    livelloCorrente = LivelloCEFR.A2,
                    obiettivoLivello = LivelloCEFR.B2,
                    lezioneCorrente = 10
                )
            )
        )
        val remoto = repo(
            ProfiloStato(
                progresso = ProgressoUtente(
                    livelloCorrente = LivelloCEFR.B1,
                    obiettivoLivello = LivelloCEFR.A1,
                    lezioneCorrente = 24
                )
            )
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato.progresso

        assertEquals(LivelloCEFR.B1, unito.livelloCorrente)
        assertEquals(LivelloCEFR.B2, unito.obiettivoLivello)
        assertEquals(24, unito.lezioneCorrente)
    }

    @Test
    fun `lo streak appartiene al giorno di studio piu' recente`() {
        val locale = repo(
            ProfiloStato(
                streakGiorni = 9,
                ultimoGiornoStudio = "2026-10-20",
                progresso = ProgressoUtente(streakGiorni = 9)
            )
        )
        val remoto = repo(
            ProfiloStato(
                streakGiorni = 3,
                ultimoGiornoStudio = "2026-11-03",
                progresso = ProgressoUtente(streakGiorni = 3)
            )
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato

        assertEquals("2026-11-03", unito.ultimoGiornoStudio)
        assertEquals(3, unito.streakGiorni)
        assertEquals(9, unito.streakRecord)
    }

    @Test
    fun `i badge e i punti si sommano senza duplicati`() {
        val locale = repo(ProfiloStato(badgeSbloccati = listOf("primo-giorno"), puntiTotali = 40))
        val remoto = repo(
            ProfiloStato(badgeSbloccati = listOf("primo-giorno", "dieci-ripassi"), puntiTotali = 25)
        )

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato

        assertEquals(setOf("primo-giorno", "dieci-ripassi"), unito.badgeSbloccati.toSet())
        assertEquals(40, unito.puntiTotali)
    }

    // ------------------------------------------------------------- profili

    @Test
    fun `un profilo presente solo nel backup viene recuperato`() {
        val locale = ProfiliRepository(
            profili = mapOf("ale" to profilo(ProfiloStato())),
            profiloAttivoId = "ale"
        )
        val emma = ProfiloUtente(
            id = "emma",
            config = ProfiloConfig(tipo = TipoProfilo.EMMA, nomeVisualizzato = "Emma"),
            stato = ProfiloStato(parole = listOf(WordEntity(id = "e1", german = "Gipfel", italian = "cima")))
        )
        val remoto = ProfiliRepository(
            profili = mapOf("emma" to emma),
            profiloAttivoId = "emma"
        )

        val unito = BackupMerge.fondi(locale, remoto)

        assertEquals(setOf("ale", "emma"), unito.profili.keys)
        assertEquals("ale", unito.profiloAttivoId)
        assertEquals(1, unito.profili["emma"]!!.stato.parole.size)
    }

    // ------------------------------------------------------------ idempotenza

    @Test
    fun `unire due volte lo stesso stato non cambia i progressi`() {
        val stato = ProfiloStato(
            parole = listOf(WordEntity(id = "a", german = "Haus", italian = "casa")),
            reviews = mapOf("a" to carta("a", 4, 20, NOW)),
            padronanzaGrammatica = mapOf(
                "u1" to PadronanzaGrammatica(
                    unitaId = "u1", corrette = 9, totali = 10, percentuale = 90,
                    completata = true, ultimaVolta = NOW, ripassi = 2
                )
            ),
            progressiLezioni = mapOf("l1" to ProgressoLezione(3, 3, 10, false, NOW)),
            streakGiorni = 4,
            ultimoGiornoStudio = "2026-11-01",
            ripassiLog = listOf(RipassoLogEntity("a", NOW, true))
        )

        val una = BackupMerge.fondi(repo(stato), repo(stato)).profili["ale"]!!.stato
        val due = BackupMerge.fondi(repo(una), repo(stato)).profili["ale"]!!.stato

        assertEquals(1, due.parole.size)
        assertEquals(4, due.reviews["a"]!!.repetitions)
        assertEquals(90, due.padronanzaGrammatica["u1"]!!.percentuale)
        assertEquals(3, due.progressiLezioni["l1"]!!.indice)
        assertEquals(1, due.ripassiLog.size)
        assertEquals(4, due.streakGiorni)
        assertEquals(una, due.copy(etag = una.etag))
    }

    @Test
    fun `l'etag resta valido quando l'unione non cambia nulla`() {
        val stato = ProfiloStato(etag = "\"abc\"", lastSync = NOW, parole = listOf(WordEntity("a", "Haus", "casa")))
        val remoto = ProfiloStato(etag = "\"abc\"", lastSync = NOW, parole = listOf(WordEntity("a", "Haus", "casa")))
        val unito = BackupMerge.fondi(repo(stato), repo(remoto)).profili["ale"]!!.stato
        assertEquals("\"abc\"", unito.etag)
    }
    @Test
    fun `l'etag viene azzerato per forzare un nuovo sync del feed`() {
        val locale = repo(ProfiloStato(etag = "\"abc\"", lastSync = NOW))
        val remoto = repo(ProfiloStato(etag = "\"def\"", lastSync = NOW - GIORNO, parole = listOf(WordEntity("b", "Baum", "albero"))))

        val unito = BackupMerge.fondi(locale, remoto).profili["ale"]!!.stato

        assertEquals("", unito.etag)
        assertEquals(NOW, unito.lastSync)
    }

    @Test
    fun `un repository locale vuoto non cancella il backup`() {
        val remoto = repo(ProfiloStato(parole = listOf(WordEntity("a", "Haus", "casa"))))
        val unito = BackupMerge.fondi(ProfiliRepository(), remoto)
        assertEquals(1, unito.profili.size)
        assertEquals(1, unito.profili["ale"]!!.stato.parole.size)
    }

    @Test
    fun `i campi di configurazione vuoti vengono recuperati dal backup`() {
        val locale = ProfiloUtente(
            id = "ale",
            config = ProfiloConfig(
                tipo = TipoProfilo.ALESSANDRO,
                nomeVisualizzato = "Alessandro",
                tutorApiKey = "",
                feedUrl = ""
            ),
            stato = ProfiloStato()
        )
        val remoto = locale.copy(
            config = locale.config.copy(
                tutorApiKey = "chiave",
                feedUrl = "https://example.test/feed.json"
            )
        )

        val unito = BackupMerge.fondi(locale, remoto)

        assertEquals("chiave", unito.config.tutorApiKey)
        assertEquals("https://example.test/feed.json", unito.config.feedUrl)
    }

    // ------------------------------------------------------------ backup reale

    @Test
    fun `il backup serializzato e unito conserva grammatica e lezioni`() {
        val locale = repo(
            ProfiloStato(
                padronanzaGrammatica = mapOf(
                    "u2" to PadronanzaGrammatica(unitaId = "u2", percentuale = 60, totali = 10)
                ),
                progressiLezioni = mapOf("l9" to ProgressoLezione(indice = 2, totali = 5))
            )
        )
        val remoto = repo(
            ProfiloStato(
                padronanzaGrammatica = mapOf(
                    "u2" to PadronanzaGrammatica(
                        unitaId = "u2", corrette = 10, totali = 10,
                        percentuale = 100, completata = true, ripassi = 3
                    ),
                    "u3" to PadronanzaGrammatica(unitaId = "u3", completata = true, percentuale = 100)
                ),
                progressiLezioni = mapOf(
                    "l9" to ProgressoLezione(indice = 5, corrette = 5, totali = 5, completata = true),
                    "l10" to ProgressoLezione(indice = 1, totali = 4)
                )
            )
        )

        val json = BackupCloud.serializza(remoto, "1.49.0", NOW)
        val pacchetto = BackupCloud.deserializza(json)
        val unito = BackupMerge.fondi(locale, pacchetto.dati).profili["ale"]!!.stato

        assertEquals(2, unito.padronanzaGrammatica.size)
        assertTrue(unito.padronanzaGrammatica["u2"]!!.completata)
        assertEquals(100, unito.padronanzaGrammatica["u2"]!!.percentuale)
        assertEquals(3, unito.padronanzaGrammatica["u2"]!!.ripassi)
        assertTrue(unito.progressiLezioni["l9"]!!.completata)
        assertEquals(5, unito.progressiLezioni["l9"]!!.indice)
        assertEquals(1, unito.progressiLezioni["l10"]!!.indice)

        val riepilogo = BackupCloud.riepilogo(pacchetto)
        assertEquals(2, riepilogo.numUnitaGrammatica)
        assertEquals(2, riepilogo.numLezioni)
    }

    @Test
    fun `nessun progresso viene inventato quando entrambi i lati sono vuoti`() {
        val unito = BackupMerge.fondi(repo(ProfiloStato()), repo(ProfiloStato()))
        val stato = unito.profili["ale"]!!.stato

        assertTrue(stato.parole.isEmpty())
        assertTrue(stato.padronanzaGrammatica.isEmpty())
        assertTrue(stato.progressiLezioni.isEmpty())
        assertFalse(stato.placementFatto)
        assertNull(stato.ultimoGiornoStudio)
    }
}
