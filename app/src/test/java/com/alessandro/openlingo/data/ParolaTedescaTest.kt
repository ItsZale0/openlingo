package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test del rendering unico delle parole tedesche (doppio articolo + placeholder). */
class ParolaTedescaTest {

    private fun parola(de: String, it: String, article: String? = null, archived: Boolean = false) =
        WordEntity(id = "x", german = de, italian = it, article = article, archived = archived)

    @Test
    fun `non duplica l articolo se german lo contiene gia`() {
        // Il caso segnalato: "die die Wand"
        assertEquals("die Wand", ParolaTedesca.conArticolo("die", "die Wand"))
        assertEquals("der Boden", ParolaTedesca.conArticolo("der", "der Boden"))
        assertEquals("das Haus", ParolaTedesca.conArticolo("das", "das Haus"))
    }

    @Test
    fun `antepone l articolo quando german non lo ha`() {
        assertEquals("die Wand", ParolaTedesca.conArticolo("die", "Wand"))
        assertEquals("der Hund", ParolaTedesca.conArticolo("der", "Hund"))
    }

    @Test
    fun `senza articolo lascia la parola invariata`() {
        assertEquals("ich", ParolaTedesca.conArticolo(null, "ich"))
        assertEquals("ich", ParolaTedesca.conArticolo("", "ich"))
        assertEquals("Ich bin", ParolaTedesca.conArticolo(null, "Ich bin"))
    }

    @Test
    fun `riconosce gli articoli indeterminativi`() {
        assertEquals("eine Wohnung", ParolaTedesca.conArticolo("eine", "eine Wohnung"))
        assertEquals("eine Wohnung", ParolaTedesca.conArticolo("eine", "Wohnung"))
    }

    @Test
    fun `non scambia una parola che inizia per articolo`() {
        // "Dienstag" NON inizia con l'articolo "die"
        assertEquals("der Dienstag", ParolaTedesca.conArticolo("der", "Dienstag"))
    }

    @Test
    fun `riconosce i placeholder del feed come traduzioni non valide`() {
        assertFalse(ParolaTedesca.traduzioneValida("(da completare)"))
        assertFalse(ParolaTedesca.traduzioneValida("da completare"))
        assertFalse(ParolaTedesca.traduzioneValida("-"))
        assertFalse(ParolaTedesca.traduzioneValida(""))
        assertFalse(ParolaTedesca.traduzioneValida(null))
        assertTrue(ParolaTedesca.traduzioneValida("io"))
        assertTrue(ParolaTedesca.traduzioneValida("la casa"))
    }

    @Test
    fun `una parola con placeholder non e utilizzabile`() {
        // Il caso dello screenshot: ich -> (da completare)
        assertFalse(ParolaTedesca.utilizzabile(parola("ich", "(da completare)")))
        assertTrue(ParolaTedesca.utilizzabile(parola("ich", "io")))
        assertFalse(ParolaTedesca.utilizzabile(parola("ich", "io", archived = true)))
    }
}
