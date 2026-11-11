package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.LivelloIniziale
import com.alessandro.openlingo.data.local.ProfiloStato
import com.alessandro.openlingo.data.local.ProfiliRepository
import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.TipoProfilo
import com.alessandro.openlingo.data.local.WordEntity
import com.alessandro.openlingo.data.local.WordSource
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica la logica di preset e riconciliazione dei profili.
 *
 * Da 1.48.72 l'app non crea piu' profili predefiniti al primo avvio: il
 * repository parte vuoto e lo l'utente crea il proprio profilo in onboarding.
 * I preset servono a costruire profili esistenti o nuovi, e a ripulire i
 * riferimenti a risorse personali rimaste dalle versioni precedenti.
 */
class ProfiliPresetTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `repo vuoto resta vuoto senza profili predefiniti`() {
        val risultato = ProfiliPreset.riconcilia(ProfiliRepository())

        assertTrue(risultato.profili.isEmpty())
        assertNull(risultato.profiloAttivoId)
    }

    @Test
    fun `riconciliazione non attiva nessun profilo`() {
        val risultato = ProfiliPreset.riconcilia(ProfiliRepository())
        // Requisito utente: la selezione avviene prima di entrare
        assertNull(risultato.profiloAttivoId)
    }

    @Test
    fun `un profilo esistente non viene sovrascritto dalla riconciliazione`() {
        val esistente = ProfiliPreset.nuovo(
            id = "profilo_123",
            nome = "Mario",
            livello = LivelloIniziale.B1,
            now = 1L
        )
        val repo = ProfiliRepository(profili = mapOf("profilo_123" to esistente), profiloAttivoId = "profilo_123")

        val risultato = ProfiliPreset.riconcilia(repo)

        assertEquals(1, risultato.profili.size)
        assertEquals(esistente, risultato.profili["profilo_123"])
        assertEquals("profilo_123", risultato.profiloAttivoId)
    }

    @Test
    fun `nuovi profili hanno feed e chiave tutor vuoti`() {
        val alessandro = ProfiliPreset.nuovo(
            id = TipoProfilo.ALESSANDRO.id,
            nome = "Sanitario",
            livello = LivelloIniziale.A0,
            mostraContestoMedico = true,
            tipo = TipoProfilo.ALESSANDRO,
            now = 1L
        )
        val emma = ProfiliPreset.nuovo(
            id = TipoProfilo.EMMA.id,
            nome = "Standard",
            livello = LivelloIniziale.B1,
            tipo = TipoProfilo.EMMA,
            now = 1L
        )

        assertEquals("", ProfiliPreset.feedPredefinito(TipoProfilo.ALESSANDRO))
        assertEquals("", ProfiliPreset.feedPredefinito(TipoProfilo.EMMA))
        assertEquals("", ProfiliPreset.feedPredefinito(TipoProfilo.PERSONALIZZATO))
        assertEquals("", ProfiliPreset.DEFAULT_TUTOR_API_KEY)
        assertEquals("", alessandro.config.feedUrl)
        assertEquals("", alessandro.config.tutorApiKey)
        assertEquals("", emma.config.feedUrl)
        assertEquals("", emma.config.tutorApiKey)
    }

    @Test
    fun `il profilo sanitario ha contesto medico e livello A0`() {
        val p = ProfiliPreset.nuovo(
            id = TipoProfilo.ALESSANDRO.id,
            nome = "Sanitario",
            livello = LivelloIniziale.A0,
            mostraContestoMedico = true,
            tipo = TipoProfilo.ALESSANDRO,
            now = 1L
        )

        assertEquals(TipoProfilo.ALESSANDRO, p.config.tipo)
        assertTrue(p.config.mostraContestoMedico)
        assertEquals(LivelloCEFR.A0, p.stato.progresso.livelloCorrente)
    }

    @Test
    fun `il profilo personalizzato usa contesto e livello forniti`() {
        val p = ProfiliPreset.nuovo(
            id = "profilo_999",
            nome = "Mario",
            livello = LivelloIniziale.B2,
            contesto = "Eletticista, obiettivo: parlare con i clienti del cantiere",
            now = 1L
        )

        assertEquals("", p.config.tutorApiKey)
        assertEquals("Eletticista, obiettivo: parlare con i clienti del cantiere", p.config.contestoUtente)
        assertEquals(LivelloCEFR.B2, p.stato.progresso.livelloCorrente)
    }

    @Test
    fun `il vecchio profilo personalizzato viene assorbito da Alessandro`() {
        val alessandro = ProfiliPreset.nuovo(
            id = TipoProfilo.ALESSANDRO.id,
            nome = "Sanitario",
            livello = LivelloIniziale.A0,
            mostraContestoMedico = true,
            tipo = TipoProfilo.ALESSANDRO,
            now = 1L
        )
        val custom = alessandro.copy(
            id = "alessandro_custom",
            stato = ProfiloStato(
                parole = listOf(
                    WordEntity(
                        id = "w-custom",
                        german = "der Kater",
                        italian = "il gatto",
                        source = WordSource.CUSTOM
                    )
                ),
                reviews = mapOf("w-custom" to ReviewEntity(wordId = "w-custom"))
            )
        )
        val repo = ProfiliRepository(
            profili = mapOf(TipoProfilo.ALESSANDRO.id to alessandro, "alessandro_custom" to custom),
            profiloAttivoId = "alessandro_custom"
        )

        val risultato = ProfiliPreset.riconcilia(repo)

        // Il profilo rimosso non esiste piu'...
        assertFalse(risultato.profili.containsKey("alessandro_custom"))
        // ...e i suoi dati non vanno persi: passano ad Alessandro.
        val ale = risultato.profili[TipoProfilo.ALESSANDRO.id]!!
        assertTrue(ale.stato.parole.any { it.id == "w-custom" })
        assertTrue(ale.stato.reviews.containsKey("w-custom"))
        assertTrue(ale.config.enableCustomWords)
        assertEquals(TipoProfilo.ALESSANDRO.id, risultato.profiloAttivoId)
    }

    @Test
    fun `riconciliazione ripulisce i vecchi feed personali`() {
        val emma = ProfiliPreset.nuovo(
            id = TipoProfilo.EMMA.id,
            nome = "Standard",
            livello = LivelloIniziale.B1,
            tipo = TipoProfilo.EMMA,
            now = 1L
        )
        val conFeedVecchio = emma.copy(
            config = emma.config.copy(feedUrl = "https://raw.githubusercontent.com/ItsZale0/tedesco-vocab/main/vokabeln.json")
        )
        val repo = ProfiliRepository(profili = mapOf(TipoProfilo.EMMA.id to conFeedVecchio))

        val risultato = ProfiliPreset.riconcilia(repo)

        assertEquals("", risultato.profili[TipoProfilo.EMMA.id]!!.config.feedUrl)
    }

    @Test
    fun `il repository serializza e deserializza senza perdere profili`() {
        val originale = ProfiliRepository(
            profili = mapOf("profilo_1" to ProfiliPreset.nuovo(id = "profilo_1", nome = "Mario", livello = LivelloIniziale.B1, now = 1L)),
            profiloAttivoId = "profilo_1"
        )
        val stringa = json.encodeToString(ProfiliRepository.serializer(), originale)
        val riletto = json.decodeFromString(ProfiliRepository.serializer(), stringa)

        assertEquals(1, riletto.profili.size)
        assertEquals(originale.profiloAttivoId, riletto.profiloAttivoId)
        originale.profili.forEach { (id, atteso) ->
            val ottenuto = riletto.profili[id]!!
            assertEquals(atteso.config, ottenuto.config)
            assertEquals(atteso.stato, ottenuto.stato)
        }
    }

    @Test
    fun `un JSON corrotto non fa crashare la lettura`() {
        val rotto = "{ questo non e' json valido"
        val risultato = runCatching {
            json.decodeFromString(ProfiliRepository.serializer(), rotto)
        }.getOrElse { ProfiliRepository() }

        // Il chiamante riparte da un repo vuoto, poi riconcilia' (che resta vuoto)
        val dopo = ProfiliPreset.riconcilia(risultato)
        assertTrue(dopo.profili.isEmpty())
        assertNull(dopo.profiloAttivoId)
    }
}
