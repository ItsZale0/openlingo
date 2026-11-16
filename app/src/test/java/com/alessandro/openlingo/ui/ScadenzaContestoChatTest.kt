package com.alessandro.openlingo.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test della finestra di contesto della chat del tutor (30 minuti). */
class ScadenzaContestoChatTest {

    @Test
    fun `messaggio recente resta nel contesto`() {
        val ora = System.currentTimeMillis()
        assertTrue(messaggioNelContesto(ora - 60_000, ora))       // 1 minuto fa
        assertTrue(messaggioNelContesto(ora - 29 * 60_000, ora))  // 29 minuti fa
    }

    @Test
    fun `messaggio vecchio esce dal contesto`() {
        val ora = System.currentTimeMillis()
        assertFalse(messaggioNelContesto(ora - 31 * 60_000, ora)) // 31 minuti fa
        assertFalse(messaggioNelContesto(ora - 60 * 60_000, ora)) // 1 ora fa
        assertFalse(messaggioNelContesto(0L, ora))                // timestamp mancante
    }

    @Test
    fun `confine esatto 30 minuti`() {
        val ora = System.currentTimeMillis()
        assertTrue(messaggioNelContesto(ora - SCADEZZA_CONTESTO_CHAT_MS, ora))
        assertFalse(messaggioNelContesto(ora - SCADEZZA_CONTESTO_CHAT_MS - 1, ora))
    }
}
