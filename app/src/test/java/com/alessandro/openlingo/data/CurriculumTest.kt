package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumTest {

    @Test
    fun `A1 parte da saluti e presentazioni`() {
        assertTrue(Curriculum.argomento("A1", 1).contains("Saluti"))
    }

    @Test
    fun `A0 usa il curriculum A1`() {
        assertEquals(Curriculum.argomento("A1", 3), Curriculum.argomento("A0", 3))
    }

    @Test
    fun `A2 ha grammatica progressiva`() {
        assertTrue(Curriculum.argomento("A2", 2).contains("Accusativo"))
    }

    @Test
    fun `B1 parte da Adjektivdeklination`() {
        assertTrue(Curriculum.argomento("B1", 1).contains("Adjektivdeklination"))
    }

    @Test
    fun `livello sconosciuto ricade su A1`() {
        assertEquals(Curriculum.argomento("A1", 1), Curriculum.argomento("sconosciuto", 1))
    }

    @Test
    fun `numero zero o negativo non va in crash`() {
        assertEquals(Curriculum.argomento("A1", 1), Curriculum.argomento("A1", 0))
        assertEquals(Curriculum.argomento("A1", 1), Curriculum.argomento("A1", -5))
    }

    @Test
    fun `dopo il ventesimo tema non ripete la stessa lezione`() {
        // Prima il modulo faceva ripartire da "Saluti" alla lezione 21.
        assertNotEquals(Curriculum.argomento("A1", 1), Curriculum.argomento("A1", 21))
        assertTrue(Curriculum.argomento("A1", 21).contains("approfondimento"))
    }

    @Test
    fun `il focus grammaticale e calibrato sul livello`() {
        assertTrue(Curriculum.focusGrammaticale("A0").contains("presente"))
        assertTrue(Curriculum.focusGrammaticale("B1").contains("Adjektivdeklination"))
    }
}