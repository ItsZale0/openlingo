package com.alessandro.tedesco.data

import androidx.test.core.app.ApplicationProvider
import com.alessandro.tedesco.data.local.TipoProfilo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test d'integrazione del [ProfileManager] con un Context Android reale
 * (Robolectric) e il vero DataStore su disco temporaneo.
 *
 * Verifica il comportamento attuale: al primo avvio il repository e' VUOTO
 * (nessun profilo predefinito), l'utente crea il proprio profilo in onboarding,
 * e i dati creati non vengono persi.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProfileManagerIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun nuovoManager(): ProfileManager = ProfileManager(
        context = ApplicationProvider.getApplicationContext(),
        json = json,
        io = Dispatchers.IO
    )

    @Before
    fun pulisci() = runBlocking {
        // Ogni test parte da uno stato pulito
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .profiliDataStorePulito()
    }

    @Test
    fun `dopo inizializza la lista profili e' vuota`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        assertTrue(manager.prontoFlow.firstValue())
        assertTrue(manager.repositoryFlow.firstValue().profili.isEmpty())
        assertEquals(0, manager.profiliDisponibili().size)
    }

    @Test
    fun `nessun profilo e' attivo dopo l'inizializzazione`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        // Requisito: la selezione avviene prima di entrare
        assertNull(manager.profiloAttivoId())
        assertNull(manager.profiloAttivo())
    }

    @Test
    fun `il flow pronto diventa true e i profili sono osservabili`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        assertTrue(manager.prontoFlow.firstValue())
        assertEquals(0, manager.repositoryFlow.firstValue().profili.size)
    }

    @Test
    fun `inizializzazioni concorrenti non svuotano la lista`() = runBlocking {
        // Il bug originale: lettura e creazione preset in parallelo si
        // sovrascrivevano, lasciando la lista profili vuota.
        val manager = nuovoManager()
        val esiti = (1..8).map { async(Dispatchers.IO) { manager.inizializza() } }.awaitAll()

        assertEquals(8, esiti.size)
        assertTrue(manager.prontoFlow.firstValue())
        assertTrue(manager.repositoryFlow.firstValue().profili.isEmpty())
    }

    @Test
    fun `creaProfilo crea e attiva un profilo con i valori di default`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        val id = manager.creaProfilo("Test Custom")

        val profili = manager.profiliDisponibili()
        assertEquals(1, profili.size)
        assertEquals(id, manager.profiloAttivoId())
        val profilo = profili[0]
        assertEquals("Test Custom", profilo.config.nomeVisualizzato)
        assertTrue(profilo.config.enableCustomWords)
        assertFalse(profilo.config.enableGoogleSheets)
        assertEquals("", manager.getFeedUrl())
        assertTrue(profilo.config.tutorApiKey.isEmpty())
    }

    @Test
    fun `selezionare un profilo lo rende attivo e persistente`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        val mario = manager.creaProfilo("Mario")
        val luigi = manager.creaProfilo("Luigi")

        manager.selezionaProfilo(luigi)
        assertEquals(luigi, manager.profiloAttivoId())
        assertEquals("Luigi", manager.nomeProfiloAttivo())

        // Un nuovo manager sullo stesso DataStore deve ritrovare la scelta
        val manager2 = nuovoManager()
        manager2.inizializza()
        assertEquals(luigi, manager2.profiloAttivoId())
    }

    @Test
    fun `uscire dal profilo riporta alla selezione senza perdere i dati`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        val id = manager.creaProfilo("Mario")
        manager.selezionaProfilo(id)
        manager.aggiornaStatoAttivo(manager.statoAttivo().copy(etag = "MIO-ETAG"), immediato = true)

        manager.esciDalProfilo()
        assertNull(manager.profiloAttivoId())
        // I profili restano disponibili per una nuova scelta
        assertEquals(1, manager.profiliDisponibili().size)

        // Rientrando, i dati sono ancora li'
        manager.selezionaProfilo(id)
        assertEquals("MIO-ETAG", manager.statoAttivo().etag)
    }

    @Test
    fun `il profilo creato abilita le parole personalizzate ma non i fogli`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        manager.creaProfilo("Mario")

        // Da 1.43.3 anche i profili personalizzati possono aggiungere parole al ripasso.
        assertTrue(manager.isCustomWordsEnabled())
        assertFalse(manager.isGoogleSheetsEnabled())
        assertEquals("", manager.getFeedUrl())
    }

    @Test
    fun `i dati di un profilo non finiscono in un altro`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        val mario = manager.creaProfilo("Mario")
        manager.selezionaProfilo(mario)
        manager.aggiornaStatoAttivo(manager.statoAttivo().copy(etag = "SOLO-MARIO"), immediato = true)

        manager.esciDalProfilo()
        val luigi = manager.creaProfilo("Luigi")
        manager.selezionaProfilo(luigi)
        assertTrue(
            "Luigi non deve vedere i dati di Mario",
            manager.statoAttivo().etag.isEmpty()
        )
    }

    @Test
    fun `un DataStore con repo vuoto resta vuoto`() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        // Simula lo stato di un primo avvio pulito: nessun profilo salvato
        ctx.scriviProfiliJson("""{"profili":{},"profiloAttivoId":null}""")

        val manager = nuovoManager()
        manager.inizializza()

        assertEquals(0, manager.profiliDisponibili().size)
    }

    @Test
    fun `flush prima del caricamento non cancella i dati salvati`() = runBlocking {
        // REGRESSIONE ("continua la app a non salvare nulla"): se onStop arriva
        // mentre inizializza() non ha ancora finito, il flush () scriveva il
        // repository VUOTO di memoria sul DataStore, cancellando i dati reali.
        val manager = nuovoManager()
        manager.inizializza()
        val id = manager.creaProfilo("Mario")
        manager.selezionaProfilo(id)
        manager.aggiornaStatoAttivo(manager.statoAttivo().copy(etag = "DATI-VETRI"), immediato = true)
        manager.flush() // persiste subito: il DataStore ora contiene "DATI-VETRI"

        // Nuovo manager che NON inizializza subito: simula onStop
        val manager2 = nuovoManager()
        manager2.flush() // senza guardia sovrascriverebbe con il repo vuoto

        val manager3 = nuovoManager()
        manager3.inizializza()
        assertEquals(id, manager3.profiloAttivoId())
        assertEquals("I dati salvati devono sopravvivere", "DATI-VETRI", manager3.statoAttivo().etag)
    }

    @Test
    fun `il writer coalescente non cancella i dati durante il caricamento`() = runBlocking {
        // Il bug di produzione: a ~400 ms dall'avvio la coroutine del writer
        // emetteva il valore iniziale (0) e scriveva _repository.value, che
        // prima di inizializza() e' il repository VUOTO: ogni avvio con dati
        // grossi (>400 ms di lettura) ripartiva da zero.
        val manager0 = nuovoManager()
        manager0.inizializza()
        val id0 = manager0.creaProfilo("Mario")
        manager0.selezionaProfilo(id0)
        manager0.aggiornaStatoAttivo(manager0.statoAttivo().copy(etag = "LUNGA-LETTURA"), immediato = true)
        manager0.flush()

        // Nuovo manager che NON inizializza subito: simula inizializza() lento
        val managerLento = nuovoManager()
        // attende oltre SALVATAGGIO_ATTESA_MS: il vecchio codice a questo punto
        // aveva gia' sovrascritto il DataStore con il repo vuoto
        kotlinx.coroutines.delay(700)
        // i dati devono essere ancora nel DataStore
        val manager3 = nuovoManager()
        manager3.inizializza()
        assertEquals("I dati devono sopravvivere al caricamento lento", "LUNGA-LETTURA", manager3.statoAttivo().etag)
    }
}

private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstValue(): T = first()
