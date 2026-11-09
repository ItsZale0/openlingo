package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test del backup cloud: serializzazione, validazione, riepilogo. */
class BackupCloudTest {

    @Test
    fun `il file di backup e' separato per profilo`() {
        assertEquals("tedesco-backup.json", BackupCloud.nomeFilePerProfilo("alessandro"))
        assertEquals("tedesco-backup.json", BackupCloud.nomeFilePerProfilo(null))
        assertEquals("tedesco-backup.json", BackupCloud.nomeFilePerProfilo(""))
        assertEquals("tedesco-backup.json", BackupCloud.nomeFilePerProfilo(" "))
        assertEquals("tedesco-backup-emma.json", BackupCloud.nomeFilePerProfilo("emma"))
        assertEquals("tedesco-backup-ale.json", BackupCloud.nomeFilePerProfilo("ale"))
    }

    private val NOW = 1_700_000_000_000L

    @Test
    fun `il ripristino prova prima il file del profilo poi quello condiviso`() {
        // Il backup viene caricato sul file del profilo attivo al momento del
        // salvataggio: se poi si cambia profilo, il file condiviso deve comunque
        // essere trovato.
        assertEquals(listOf("tedesco-backup.json"), BackupCloud.candidatiRipristino("alessandro"))
        assertEquals(listOf("tedesco-backup.json"), BackupCloud.candidatiRipristino(null))
        assertEquals(
            listOf("tedesco-backup-emma.json", "tedesco-backup.json"),
            BackupCloud.candidatiRipristino("emma")
        )
    }

    @Test
    fun `riconosce solo i file di backup dell'app`() {
        assertTrue(BackupCloud.eFileBackup("tedesco-backup.json"))
        assertTrue(BackupCloud.eFileBackup("tedesco-backup-emma.json"))
        assertFalse(BackupCloud.eFileBackup("README.md"))
        assertFalse(BackupCloud.eFileBackup("tedesco-backup-.json"))
        assertFalse(BackupCloud.eFileBackup("tedesco-backup-a/b.json"))
    }

    @Test
    fun `preferisce il backup del profilo attivo anche se un altro e' piu' recente`() {
        val nomi = listOf("tedesco-backup.json", "tedesco-backup-emma.json", "note.txt")
        val date = mapOf("tedesco-backup.json" to 10L, "tedesco-backup-emma.json" to 99L)
        assertEquals(
            listOf("tedesco-backup-emma.json", "tedesco-backup.json"),
            BackupCloud.ordinaBackup("emma", nomi, date)
        )
    }

    @Test
    fun `fra backup sconosciuti sceglie il piu' recente`() {
        val nomi = listOf("tedesco-backup-anna.json", "tedesco-backup-luca.json")
        val date = mapOf("tedesco-backup-anna.json" to 5L, "tedesco-backup-luca.json" to 50L)
        assertEquals(
            listOf("tedesco-backup-luca.json", "tedesco-backup-anna.json"),
            BackupCloud.ordinaBackup("profilo_nuovo", nomi, date)
        )
    }

    private fun repo(): ProfiliRepository {
        val ale = ProfiloUtente(
            id = "ale",
            config = com.alessandro.tedesco.data.local.ProfiloConfig(
                tipo = com.alessandro.tedesco.data.local.TipoProfilo.ALESSANDRO,
                nomeVisualizzato = "Alessandro"
            ),
            stato = ProfiloStato(
                parole = listOf(
                    WordEntity(id = "w1", german = "ich", italian = "io"),
                    WordEntity(id = "w116", german = "die Wand", italian = "la parete")
                ),
                ripassiLog = listOf(RipassoLogEntity("w1", NOW, true, verifica = true))
            )
        )
        val emma = ProfiloUtente(
            id = "emma",
            config = com.alessandro.tedesco.data.local.ProfiloConfig(
                tipo = com.alessandro.tedesco.data.local.TipoProfilo.EMMA,
                nomeVisualizzato = "Emma"
            ),
            stato = ProfiloStato(parole = listOf(WordEntity(id = "e1", german = "der Gipfel", italian = "la cima")))
        )
        return ProfiliRepository(profili = mapOf("ale" to ale, "emma" to emma), profiloAttivoId = "ale")
    }

    @Test
    fun `round-trip completo conserva tutti i dati`() {
        val originale = repo()
        val json = BackupCloud.serializza(originale, "1.35.50", NOW)
        val letto = BackupCloud.deserializza(json)
        assertEquals(originale.profili.size, letto.dati.profili.size)
        assertEquals(originale.profiloAttivoId, letto.dati.profiloAttivoId)
        assertEquals("io", letto.dati.profili["ale"]!!.stato.parole[0].italian)
        assertEquals("die Wand", letto.dati.profili["ale"]!!.stato.parole[1].german)
        assertEquals("la parete", letto.dati.profili["ale"]!!.stato.parole[1].italian)
        assertEquals(1, letto.dati.profili["ale"]!!.stato.ripassiLog.size)
        assertTrue(letto.dati.profili["ale"]!!.stato.ripassiLog[0].verifica)
        assertEquals("der Gipfel", letto.dati.profili["emma"]!!.stato.parole[0].german)
    }

    @Test
    fun `il pacchetto registra versione e data`() {
        val json = BackupCloud.serializza(repo(), "1.35.50", NOW)
        val p = BackupCloud.deserializza(json)
        assertEquals(BackupCloud.VERSIONE_FORMATO, p.versione)
        assertEquals(NOW, p.creatoIl)
        assertEquals("1.35.50", p.versioneApp)
    }

    @Test
    fun `un backup di formato futuro viene rifiutato`() {
        // Mai sovrascrivere dati con un formato che non capiamo
        val json = """{"versione":99,"creatoIl":1,"versioneApp":"9.9","dati":{"profili":{},"profiloAttivoId":null}}"""
        val e = runCatching { BackupCloud.deserializza(json) }.exceptionOrNull()
        assertTrue(e is BackupCloud.BackupInvalido)
        assertTrue(e!!.message!!.contains("piu' recente") || e.message!!.contains("recente"))
    }

    @Test
    fun `json non leggibile viene rifiutato con messaggio chiaro`() {
        val e = runCatching { BackupCloud.deserializza("{non json}") }.exceptionOrNull()
        assertTrue(e is BackupCloud.BackupInvalido)
        assertTrue(e!!.message!!.contains("non e' leggibile"))
    }

    @Test
    fun `il riepilogo conta profili parole ripassi risposte`() {
        val json = BackupCloud.serializza(repo(), "1.35.50", NOW)
        val r = BackupCloud.riepilogo(BackupCloud.deserializza(json))
        assertEquals(2, r.numProfili)
        assertEquals(3, r.numParole)
        assertEquals(1, r.numRipassi)
        assertEquals(0, r.numRisposte)
        assertEquals(NOW, r.creatoIl)
    }

    @Test
    fun `un repository vuoto si serializza senza errori`() {
        val json = BackupCloud.serializza(ProfiliRepository(), "1.35.50", NOW)
        val p = BackupCloud.deserializza(json)
        assertEquals(0, p.dati.profili.size)
        assertEquals(0, BackupCloud.riepilogo(p).numProfili)
    }
}
