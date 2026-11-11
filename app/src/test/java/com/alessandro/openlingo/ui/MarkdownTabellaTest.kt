package com.alessandro.openlingo.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regressione markdown: le tabelle delle lezioni generate dall'AI devono essere
 * riconosciute (con e senza pipe esterni) e mai scambiate per testo normale.
 */
class MarkdownTabellaTest {

    @Test
    fun `riconosce tabella con pipe esterni e conta le righe`() {
        val righe = listOf(
            "| Tedesco | Italiano |",
            "|---------|----------|",
            "| der Hund | il cane |",
            "| die Katze | il gatto |",
            "Testo dopo la tabella"
        )
        val (tabella, prossimo) = rilevaTabella(righe, 0)!!

        assertEquals(2, tabella.intestazioni.size)
        assertEquals(2, tabella.righe.size)
        assertEquals("der Hund", tabella.righe[0][0].text)
        assertEquals("il gatto", tabella.righe[1][1].text)
        assertEquals(4, prossimo)
    }

    @Test
    fun `riconosce tabella senza pipe esterni`() {
        val righe = listOf("Wort | Bedeutung", "--- | ---", "der Hund | il cane")

        val (tabella, _) = rilevaTabella(righe, 0)!!

        assertEquals(2, tabella.intestazioni.size)
        assertEquals("il cane", tabella.righe[0][1].text)
    }

    @Test
    fun `allineamenti letti dai due punti`() {
        val righe = listOf("| a | b | c |", "|:--|:-:|--:|", "| 1 | 2 | 3 |")

        val (tabella, _) = rilevaTabella(righe, 0)!!

        assertEquals(Allineamento.SX, tabella.allineamenti[0])
        assertEquals(Allineamento.CENTRO, tabella.allineamenti[1])
        assertEquals(Allineamento.DX, tabella.allineamenti[2])
    }

    @Test
    fun `riga con meno celle viene riempita`() {
        val righe = listOf("| a | b | c |", "|---|---|---|", "| 1 |")

        val (tabella, _) = rilevaTabella(righe, 0)!!

        assertEquals(3, tabella.righe[0].size)
        assertEquals("", tabella.righe[0][2].text)
    }

    @Test
    fun `pipe senza riga separatrice non e una tabella`() {
        assertNull(rilevaTabella(listOf("a | b", "testo normale"), 0))
    }

    @Test
    fun `separatore senza pipe non e una tabella`() {
        assertFalse(eSeparatoreTabella("---"))
        assertNull(rilevaTabella(listOf("a | b", "---"), 0))
    }

    @Test
    fun `grassetto e corsivo funzionano dentro le celle`() {
        val righe = listOf("| **Wort** | x |", "|---|---|", "| *der* Hund | y |")

        val (tabella, _) = rilevaTabella(righe, 0)!!

        assertTrue(tabella.intestazioni[0].spanStyles.isNotEmpty())
        assertTrue(tabella.righe[0][0].spanStyles.isNotEmpty())
    }

    @Test
    fun `celleTabella toglie i pipe esterni`() {
        assertEquals(listOf("a", "b"), celleTabella("| a | b |"))
        assertEquals(listOf("a", "b"), celleTabella("a | b"))
    }
}
