package com.alessandro.tedesco.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

class TraduttoreTest {
    @Test
    fun englishTranslatesExact() {
        Traduttore.lingua = "en"
        assertEquals("Accuracy", t("Accuratezza"))
        assertEquals("Next unit", t("Prossima unità"))
        assertEquals("Correct!", t("Corretto!"))
    }

    @Test
    fun italianPassthrough() {
        Traduttore.lingua = "it"
        assertEquals("Accuratezza", t("Accuratezza"))
        assertEquals("Prossima unità", t("Prossima unità"))
    }

    @Test
    fun interpolatedSchemi() {
        Traduttore.lingua = "en"
        assertEquals("12 words", t("12 parole"))
        assertEquals("Level A2", t("Livello A2"))
        assertEquals("Question 2 of 10", t("Domanda 2 di 10"))
        assertEquals("Score: 7/10", t("Punteggio: 7/10"))
    }

    @Test
    fun unknownReturnsInput() {
        Traduttore.lingua = "en"
        assertEquals("xyzzy", t("xyzzy"))
    }
}
