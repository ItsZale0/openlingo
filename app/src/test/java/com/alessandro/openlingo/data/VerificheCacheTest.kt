package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.RipassoLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La mappa delle verifiche DEVE essere calcolata una volta per log.
 *
 * Il progresso si ricalcola a ogni evento (ogni risposta data) e chiamava
 * verificheDaLog 2-3 volte ricostruendo ogni volta una HashMap da migliaia di
 * voci: era la causa dell'OutOfMemoryError con l'heap a 256 MB.
 */
class VerificheCacheTest {

    private fun log(vararg righe: Pair<String, Boolean>): List<RipassoLogEntity> =
        righe.mapIndexed { i, (id, ok) ->
            RipassoLogEntity(wordId = id, timestamp = 1_000L + i, corretta = ok, verifica = true)
        }

    @Test
    fun `lo stesso log restituisce la stessa mappa (nessun ricalcolo)`() {
        val l = log("w1" to true, "w2" to false)
        val prima = VerificaLessico.verificheDaLog(l)
        val seconda = VerificaLessico.verificheDaLog(l)

        assertSame("con lo stesso log non si deve ricalcolare", prima, seconda)
    }

    @Test
    fun `un log nuovo viene ricalcolato`() {
        val l1 = log("w1" to true)
        val l2 = log("w1" to false)
        val prima = VerificaLessico.verificheDaLog(l1)
        val seconda = VerificaLessico.verificheDaLog(l2)

        assertNotSame(prima, seconda)
        assertEquals(true, prima["w1"])
        assertEquals(false, seconda["w1"])
    }

    @Test
    fun `dopo lo svuotamento il risultato e' identico`() {
        val l = log("w1" to true, "w2" to true)
        val prima = VerificaLessico.verificheDaLog(l)
        VerificaLessico.svuotaCache()
        val dopo = VerificaLessico.verificheDaLog(l)

        assertEquals(prima, dopo)
    }

    @Test
    fun `vince l'ultimo evento a parita' di timestamp`() {
        val l = listOf(
            RipassoLogEntity("w1", 5_000L, corretta = false, verifica = true),
            RipassoLogEntity("w1", 5_000L, corretta = true, verifica = true)
        )
        VerificaLessico.svuotaCache()
        assertEquals(true, VerificaLessico.verificheDaLog(l)["w1"])
    }

    @Test
    fun `i giorni si derivano dallo stesso passaggio`() {
        val l = listOf(
            RipassoLogEntity("w1", 86_400_000L * 10, true, verifica = true),
            RipassoLogEntity("w2", 86_400_000L * 11, true, verifica = true)
        )
        VerificaLessico.svuotaCache()
        val giorni = VerificaLessico.giorniDaLog(l)

        assertEquals(2, giorni.size)
        assertTrue(giorni.contains(java.time.LocalDate.ofEpochDay(10)))
    }
}
