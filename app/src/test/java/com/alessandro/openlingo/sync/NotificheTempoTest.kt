package com.alessandro.openlingo.sync

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Test per il calcolo del ritardo iniziale dei promemoria: il worker
 *_periodico_ (24h) deve partire la prima volta all'ora scelta.
 */
class NotificheTempoTest {

    private fun adesso(giorno: Int, ora: Int, minuto: Int, secondo: Int = 0): Calendar =
        Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, giorno, ora, minuto, secondo)
            set(Calendar.MILLISECOND, 0)
        }

    private fun verifica(giorno: Int, oraOra: Int, oraTarget: Int, ore: Long) {
        val ritardo = NotificheSettings.ritardoFinoAlle(oraTarget, adesso(giorno, oraOra, 0))
        val atteso = ore * 60L * 60L * 1000L
        assertTrue(
            "ritardo $ritardo non è nell'intervallo [$atteso, ${atteso + 60_000})",
            ritardo >= atteso && ritardo < atteso + 60_000
        )
    }

    @Test
    fun `target ancora stasera`() {
        // Ore 10:00 del 10, promemoria alle 21 -> 11 ore.
        verifica(10, 10, 21, 11)
    }

    @Test
    fun `target già passato, si va a domani`() {
        // Ore 22:00 del 10, promemoria alle 21 -> 23 ore (21 di domani).
        verifica(10, 22, 21, 23)
    }

    @Test
    fun `target esattamente adesso, si va a domani`() {
        // Ore 20:00 in punto, promemoria alle 20 -> 24 ore.
        verifica(10, 20, 20, 24)
    }

    @Test
    fun `il calcolo non dipende da secondi sparsi`() {
        val ritardo1 = NotificheSettings.ritardoFinoAlle(21, adesso(10, 10, 0, 15))
        val ritardo2 = NotificheSettings.ritardoFinoAlle(21, adesso(10, 10, 0, 0))
        val differenza = ritardo2 - ritardo1
        assertTrue(
            "cambiando i secondi il ritardo muta di più di 60 s: $differenza",
            differenza in 0..60_000
        )
    }
}
