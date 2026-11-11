package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WortlisteTest {

    @Test
    fun laWortlisteHaUnNucleoAdeguato() {
        assertTrue(
            "La Wortliste dovrebbe avere almeno 250 parole curate, trovate ${WortlisteB1.tutte.size}",
            WortlisteB1.tutte.size >= 250
        )
    }

    @Test
    fun leParoleSonoUniche() {
        val parole = WortlisteB1.tutte.map { it.tedesco.lowercase() }
        assertEquals(
            "Parole duplicate nella Wortliste",
            parole.size,
            parole.distinct().size
        )
    }

    @Test
    fun ogniVoceHaTuttiICampi() {
        WortlisteB1.tutte.forEach { voce ->
            assertTrue("Parola tedesca vuota", voce.tedesco.isNotBlank())
            assertTrue("Traduzione vuota per ${voce.tedesco}", voce.italiano.isNotBlank())
            assertTrue("Esempio vuoto per ${voce.tedesco}", voce.esempio.isNotBlank())
            assertTrue("Tema vuoto per ${voce.tedesco}", voce.tema.isNotBlank())
            assertTrue("Livello vuoto per ${voce.tedesco}", voce.livello.isNotBlank())
        }
    }

    @Test
    fun iLivelliSonoQuelliAttesi() {
        val livelli = WortlisteB1.tutte.map { it.livello }.distinct().sorted()
        assertEquals(listOf("A1", "A2", "B1"), livelli)
    }

    @Test
    fun ogniLivelloHaUnNumeroUtileDiParole() {
        val perLivello = WortlisteB1.tutte.groupingBy { it.livello }.eachCount()
        assertTrue("Poche parole A1: ${perLivello["A1"]}", (perLivello["A1"] ?: 0) >= 40)
        assertTrue("Poche parole A2: ${perLivello["A2"]}", (perLivello["A2"] ?: 0) >= 30)
        assertTrue("Poche parole B1: ${perLivello["B1"]}", (perLivello["B1"] ?: 0) >= 100)
    }

    @Test
    fun laRicercaTrovaTedescoEItaliano() {
        assertTrue(WortlisteB1.cerca("Krankenhaus").any { it.tedesco == "das Krankenhaus" })
        assertTrue(WortlisteB1.cerca("ospedale").any { it.tedesco == "das Krankenhaus" })
        assertTrue(WortlisteB1.cerca("  ").size == WortlisteB1.tutte.size)
    }

    @Test
    fun ilFiltroPerLivelloFunziona() {
        val a1 = WortlisteB1.perLivello("A1")
        assertTrue(a1.isNotEmpty())
        assertTrue(a1.all { it.livello == "A1" })
        assertFalse(WortlisteB1.perLivello("C1").isNotEmpty())
    }
}
