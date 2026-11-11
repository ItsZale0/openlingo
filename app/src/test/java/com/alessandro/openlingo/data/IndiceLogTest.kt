package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.RipassoLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * L'indice del log si calcola UNA volta per log e da' tutti i valori derivati.
 *
 * Prima ogni ricalcolo del progresso faceva oltre 10 scansioni complete del log,
 * con una LocalDate creata per voce per 7 giorni diversi (~35.000 oggetti per
 * evento): era la spazzatura che riempiva l'heap fino all'OutOfMemoryError.
 */
class IndiceLogTest {

    private fun log(): List<RipassoLogEntity> = listOf(
        RipassoLogEntity("w1", 86_400_000L * 10, corretta = true, verifica = true),
        RipassoLogEntity("w1", 86_400_000L * 10, corretta = false, verifica = true),
        RipassoLogEntity("w2", 86_400_000L * 11, corretta = true, verifica = false),
        RipassoLogEntity("w2", 86_400_000L * 11, corretta = true, verifica = true),
        RipassoLogEntity("w3", 86_400_000L * 12, corretta = false, verifica = false)
    )

    @Test
    fun `lo stesso log non viene ricalcolato`() {
        val l = log()
        assertSame("l'indice deve essere memoizzato", IndiceLog.di(l), IndiceLog.di(l))
    }

    @Test
    fun `conta ripassi, errori e accuratezza in un passaggio`() {
        val idx = IndiceLog.di(log())
        assertEquals(5, idx.totale)
        assertEquals(3, idx.corretti)
        assertEquals(2, idx.errori)
        assertEquals(0.6f, idx.accuratezza, 0.001f)
    }

    @Test
    fun `le verifiche prendono l'ultimo evento a parita' di timestamp`() {
        val idx = IndiceLog.di(log())
        // w1: due eventi con lo stesso timestamp, l'ultimo e' corretta=false
        assertEquals(false, idx.verifiche["w1"])
        assertEquals(true, idx.verifiche["w2"])
        // w3 non ha eventi di verifica: non compare
        assertTrue(idx.verifiche["w3"] == null)
    }

    @Test
    fun `i giorni e i ripassi per giorno sono pronti per i grafici`() {
        val idx = IndiceLog.di(log())
        assertEquals(3, idx.giorni.size)
        assertEquals(2, idx.ripassiDelGiorno(java.time.LocalDate.ofEpochDay(10)))
        assertEquals(1, idx.ripassiDelGiorno(java.time.LocalDate.ofEpochDay(12)))
        assertEquals(0, idx.ripassiDelGiorno(java.time.LocalDate.ofEpochDay(99)))
    }

    @Test
    fun `la prima risposta corretta e' quella piu' vecchia`() {
        val idx = IndiceLog.di(log())
        assertEquals(86_400_000L * 11, idx.primaCorretta["w2"])
        assertEquals(86_400_000L * 10, idx.primaCorretta["w1"])
    }

    @Test
    fun `dopo lo svuotamento i valori sono identici`() {
        val l = log()
        val prima = IndiceLog.di(l)
        IndiceLog.svuota()
        val dopo = IndiceLog.di(l)
        assertEquals(prima.totale, dopo.totale)
        assertEquals(prima.verifiche, dopo.verifiche)
        assertEquals(prima.giorni, dopo.giorni)
    }

    @Test
    fun `un log vuoto non esplode`() {
        val idx = IndiceLog.di(emptyList())
        assertEquals(0, idx.totale)
        assertEquals(0f, idx.accuratezza, 0f)
        assertTrue(idx.verifiche.isEmpty())
    }
}
