package com.alessandro.openlingo.data

import androidx.test.core.app.ApplicationProvider
import com.alessandro.openlingo.data.local.ConversazioneEntry
import com.alessandro.openlingo.data.local.FeedbackEntry
import com.alessandro.openlingo.data.local.TipoProfilo
import com.alessandro.openlingo.data.remote.FeedService
import com.alessandro.openlingo.data.remote.ParolaLezione
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * REGRESSIONE: `WordRepository.salvaSuProfilo()` ricostruiva un `ProfiloStato` da
 * zero copiando solo 8 campi, quindi OGNI salvataggio (risposta di ripasso, parola
 * aggiunta, sync fallito) AZZERAVA conversazioni del tutor, correzioni, esercizi
 * generati, lezioni generate e streak.
 *
 * Sintomi che questo test blocca: "la chat non ha memoria", "la streak e' 0",
 * "gli esercizi spariscono", "la lezione generata sparisce".
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SalvaSuProfiloNonAzzeraTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun mockService(): FeedService {
        val response = Response.Builder()
            .request(Request.Builder().url("https://example.com/feed").build())
            .protocol(Protocol.HTTP_1_1)
            .code(304)
            .message("Not Modified")
            .body("".toResponseBody(null))
            .build()
        return object : FeedService {
            override suspend fun getRaw(url: String): Response = response
            override suspend fun head(url: String): Response = response
        }
    }

    @Before
    fun pulisci() = runBlocking {
        ApplicationProvider.getApplicationContext<android.content.Context>().profiliDataStorePulito()
    }

    @Test
    fun `salvare le parole di una lezione NON cancella chat correzioni e lezioni generate`() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val manager = ProfileManager(context = ctx, json = json, io = Dispatchers.IO)
        manager.inizializza()
        val idProfilo = manager.creaProfilo("Prova Salva")
        manager.selezionaProfilo(idProfilo)

        // Stato ricco: chat, correzioni, lezione generata
        val stato = manager.statoAttivo()
        manager.aggiornaStatoAttivo(
            stato.copy(
                conversazioni = listOf(
                    ConversazioneEntry(id = "c1", tipo = "tutor", ruolo = "utente", testo = "Ciao", timestamp = 1L),
                    ConversazioneEntry(id = "c2", tipo = "tutor", ruolo = "tutor", testo = "Hallo", timestamp = 2L)
                ),
                risposte = listOf(
                    FeedbackEntry(id = "r1", testo = "ich lerne", timestamp = 1L, correzione = "Ich lerne")
                ),
                lezioniGenerate = mapOf("5" to "# Lezione 5 generata"),
                lezioneContenuto = "# Lezione 5 generata",
                streakGiorni = 7,
                streakRecord = 12
            )
        )

        // Parole UNICHE per run: il DataStore e' condiviso tra i run e una parola
        // gia' presente renderebbe il conteggio dipendente dall'ordine/storia.
        val t = System.nanoTime()

        // Un salvataggio qualsiasi che passa da salvaSuProfilo()
        val repo = WordRepository(
            context = ctx,
            service = mockService(),
            json = json,
            io = Dispatchers.IO,
            profileManager = manager
        )
        val aggiunte = repo.aggiungiParoleLezione(
            parole = listOf(
                ParolaLezione("die Stadt$t", "la citta", "die", "Ich wohne in der Stadt."),
                ParolaLezione("das Dorf$t", "il villaggio", "das", "Das Dorf ist klein.")
            ),
            numero = 5,
            livello = "A0"
        )
        assertEquals(2, aggiunte)

        val dopo = manager.statoAttivo()
        assertEquals("chat del tutor cancellata", 2, dopo.conversazioni.size)
        assertEquals("correzioni cancellate", 1, dopo.risposte.size)
        assertEquals("lezione generata cancellata", "# Lezione 5 generata", dopo.lezioniGenerate["5"])
        assertEquals("streak azzerata", 7, dopo.streakGiorni)
        assertEquals("record streak azzerato", 12, dopo.streakRecord)
        assertTrue("parole della lezione non salvate", dopo.parole.size >= 2)
    }

    @Test
    fun `le parole della lezione entrano nel vocabolario senza il flag custom words`() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val manager = ProfileManager(context = ctx, json = json, io = Dispatchers.IO)
        manager.inizializza()
        val idProfilo = manager.creaProfilo("Prova Salva")
        manager.selezionaProfilo(idProfilo)

        val repo = WordRepository(
            context = ctx,
            service = mockService(),
            json = json,
            io = Dispatchers.IO,
            profileManager = manager
        )
        // Parola UNICA per run: una gia' presente nel DataStore condiviso farebbe
        // tornare 0 aggiunte (falso negativo) a seconda dell'ordine di esecuzione.
        val t = System.nanoTime()
        val nuova = "das Krankenhaus$t"
        val n = repo.aggiungiParoleLezione(
            parole = listOf(ParolaLezione(nuova, "l'ospedale", "das", "Er arbeitet im Krankenhaus.")),
            numero = 5,
            livello = "A0"
        )
        assertEquals(1, n)

        val parole = repo.observeWords().first()
        assertTrue("la parola della lezione non e' nel vocabolario", parole.any { it.german == nuova })
        // La lezione e' assegnata: cosi' compare nella sezione della lezione
        assertTrue(parole.any { it.lesson == 5 })
    }
}
