package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.ProfiliRepository
import com.alessandro.openlingo.data.local.ProfiloConfig
import com.alessandro.openlingo.data.local.ProfiloStato
import com.alessandro.openlingo.data.local.ProfiloUtente
import com.alessandro.openlingo.data.local.TipoProfilo
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test dell'hash usato dal backup automatico per evitare ricariche inutili.
 * La logica di hash e' dentro BackupEsecutore; qui si verifica il comportamento
 * del pacchetto che ne determina il valore.
 */
class BackupEsecutoreTest {

    private val NOW = 1_700_000_000_000L

    private fun repoCon(parole: List<WordEntity>) = ProfiliRepository(
        profili = mapOf(
            "ale" to ProfiloUtente(
                id = "ale",
                config = ProfiloConfig(tipo = TipoProfilo.ALESSANDRO, nomeVisualizzato = "Alessandro"),
                stato = ProfiloStato(parole = parole),
                creatoIl = NOW,
                ultimoAccesso = NOW
            )
        ),
        profiloAttivoId = "ale"
    )

    @Test
    fun `lo stesso contenuto produce lo stesso json (hash stabile)`() {
        // Il timestamp e' passato esplicitamente: l'hash dipende SOLO dal contenuto
        val a = BackupCloud.serializza(repoCon(listOf(WordEntity(id = "w1", german = "ich", italian = "io"))), "1.35", NOW)
        val b = BackupCloud.serializza(repoCon(listOf(WordEntity(id = "w1", german = "ich", italian = "io"))), "1.35", NOW)
        assertEquals(a, b)
    }

    @Test
    fun `contenuto diverso produce json diverso (il backup parte)`() {
        val a = BackupCloud.serializza(repoCon(listOf(WordEntity(id = "w1", german = "ich", italian = "io"))), "1.35", NOW)
        val b = BackupCloud.serializza(
            repoCon(listOf(
                WordEntity(id = "w1", german = "ich", italian = "io"),
                WordEntity(id = "w2", german = "du", italian = "tu")
            )), "1.35", NOW
        )
        assertNotEquals(a, b)
    }

    @Test
    fun `il pacchetto conserva tutti i profili`() {
        val repo = ProfiliRepository(
            profili = mapOf(
                "ale" to ProfiloUtente(id = "ale", config = ProfiloConfig(TipoProfilo.ALESSANDRO, "Alessandro"), creatoIl = NOW, ultimoAccesso = NOW),
                "emma" to ProfiloUtente(id = "emma", config = ProfiloConfig(TipoProfilo.EMMA, "Emma"), creatoIl = NOW, ultimoAccesso = NOW)
            ),
            profiloAttivoId = "ale"
        )
        val letto = BackupCloud.deserializza(BackupCloud.serializza(repo, "1.35", NOW))
        assertEquals(2, letto.dati.profili.size)
        assertTrue(letto.dati.profili.containsKey("emma"))
    }

    @Test
    fun `un repo vuoto non e un errore di formato`() {
        val json = BackupCloud.serializza(ProfiliRepository(), "1.35", NOW)
        assertEquals(0, BackupCloud.deserializza(json).dati.profili.size)
    }
}
