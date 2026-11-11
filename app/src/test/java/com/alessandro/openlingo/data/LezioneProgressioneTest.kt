package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LessonEntity
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * La progressione delle lezioni è pura: dati lo stesso input, la stessa uscita.
 * Niente rete, nessun repository, nessun timestamp.
 */
class LezioneProgressioneTest {

    @Test
    fun `senza parole e senze lezioni si avanza di una lezione`() {
        assertEquals(2, selezionaProssimaLezione(1, emptyList(), emptyList()))
    }

    @Test
    fun `salta le lezioni vuote e va a quella che ha parole`() {
        val parole = listOf(
            WordEntity("w1", "il libro", "il libro", lesson = 1, level = "A0"),
            WordEntity("w2", "la ciotola", "la ciotola", lesson = 3, level = "A0")
        )
        assertEquals(3, selezionaProssimaLezione(1, parole, emptyList()))
    }

    @Test
    fun `la lezione attuale con parole non avanza se la prossima e' vuota`() {
        val parole = listOf(WordEntity("w1", "la parola", "la parola", lesson = 1, level = "A0"))
        assertEquals(2, selezionaProssimaLezione(1, parole, emptyList()))
    }

    @Test
    fun `le parole nella lezione 5 riportano a 5 anche da 1`() {
        // la lezione 1 non ha parole, quella 5 ne ha: la prossima lezione reale è la 5.
        val parole = listOf(WordEntity("w1", "a", "a", lesson = 5, level = "A0"))
        assertEquals(5, selezionaProssimaLezione(1, parole, emptyList()))
    }

    @Test
    fun `lezione con parole e il feed riempiono le buche`() {
        val parole = listOf(WordEntity("w1", "a", "a", lesson = 1, level = "A0"))
        val feed = listOf(LessonEntity(numero = 2, titolo = "T", contenuto = "c", audioUrl = ""))
        assertEquals(2, selezionaProssimaLezione(1, parole, feed))
    }

    @Test
    fun `lezione con parole nella lezione corrente non fa saltare a quella successiva`() {
        val parole = listOf(WordEntity("w1", "a", "a", lesson = 1, level = "A0"))
        val feed = listOf(LessonEntity(numero = 1, titolo = "T", contenuto = "c", audioUrl = ""))
        assertEquals(2, selezionaProssimaLezione(1, parole, feed))
    }
}
