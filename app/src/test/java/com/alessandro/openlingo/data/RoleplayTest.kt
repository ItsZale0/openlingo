package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.remote.TutorService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TDD tests for the roleplay feature.
 *
 * Tests the pre-built roleplay scenarios and the RoleplayScenario data class.
 */
class RoleplayTest {

    // --- SCENARI list tests ---

    @Test
    fun `SCENARI contains 5 scenarios`() {
        assertEquals(5, TutorService.SCENARI.size)
    }

    @Test
    fun `SCENARI has ristorante scenario`() {
        val ristorante = TutorService.SCENARI.firstOrNull { it.id == "ristorante" }
        assertNotNull("ristorante scenario should exist", ristorante)
        assertEquals("Al ristorante", ristorante!!.titolo)
        assertEquals("Cameriere", ristorante.ruoloAI)
        assertEquals("Cliente", ristorante.ruoloUtente)
    }

    @Test
    fun `SCENARI has stazione scenario`() {
        val stazione = TutorService.SCENARI.firstOrNull { it.id == "stazione" }
        assertNotNull("stazione scenario should exist", stazione)
        assertEquals("Alla stazione", stazione!!.titolo)
        assertEquals("Impiegato della stazione", stazione.ruoloAI)
        assertEquals("Viaggiatore", stazione.ruoloUtente)
    }

    @Test
    fun `SCENARI has negozio scenario`() {
        val negozio = TutorService.SCENARI.firstOrNull { it.id == "negozio" }
        assertNotNull("negozio scenario should exist", negozio)
        assertEquals("Negozio", negozio!!.titolo)
        assertEquals("Commesso", negozio.ruoloAI)
        assertEquals("Cliente", negozio.ruoloUtente)
    }

    @Test
    fun `SCENARI has medico scenario`() {
        val medico = TutorService.SCENARI.firstOrNull { it.id == "medico" }
        assertNotNull("medico scenario should exist", medico)
        assertEquals("Al medico", medico!!.titolo)
        assertEquals("Medico", medico.ruoloAI)
        assertEquals("Paziente", medico.ruoloUtente)
    }

    @Test
    fun `SCENARI has albergo scenario`() {
        val albergo = TutorService.SCENARI.firstOrNull { it.id == "albergo" }
        assertNotNull("albergo scenario should exist", albergo)
        assertEquals("In albergo", albergo!!.titolo)
        assertEquals("Receptionist", albergo.ruoloAI)
        assertEquals("Ospite", albergo.ruoloUtente)
    }

    // --- RoleplayScenario data class tests ---

    @Test
    fun `RoleplayScenario stores all fields correctly`() {
        val scenario = TutorService.RoleplayScenario(
            id = "test",
            titolo = "Test Scenario",
            descrizione = "A test scenario",
            ruoloAI = "AI Role",
            ruoloUtente = "User Role",
            situazione = "Test situation"
        )
        assertEquals("test", scenario.id)
        assertEquals("Test Scenario", scenario.titolo)
        assertEquals("A test scenario", scenario.descrizione)
        assertEquals("AI Role", scenario.ruoloAI)
        assertEquals("User Role", scenario.ruoloUtente)
        assertEquals("Test situation", scenario.situazione)
    }

    @Test
    fun `RoleplayScenario is serializable`() {
        val scenario = TutorService.RoleplayScenario(
            id = "test",
            titolo = "Test",
            descrizione = "Desc",
            ruoloAI = "AI",
            ruoloUtente = "User",
            situazione = "Situation"
        )
        // Verify it can be serialized and deserialized
        val json = kotlinx.serialization.json.Json.encodeToString(
            TutorService.RoleplayScenario.serializer(),
            scenario
        )
        assertTrue("JSON should contain id", json.contains("\"id\""))
        assertTrue("JSON should contain titolo", json.contains("\"titolo\""))
        assertTrue("JSON should contain ruoloAI", json.contains("\"ruoloAI\""))
        assertTrue("JSON should contain ruoloUtente", json.contains("\"ruoloUtente\""))
        assertTrue("JSON should contain situazione", json.contains("\"situazione\""))
    }

    // --- Scenario content quality tests ---

    @Test
    fun `all scenarios have non-empty fields`() {
        TutorService.SCENARI.forEach { scenario ->
            assertTrue("Scenario ${scenario.id} should have non-empty titolo", scenario.titolo.isNotBlank())
            assertTrue("Scenario ${scenario.id} should have non-empty descrizione", scenario.descrizione.isNotBlank())
            assertTrue("Scenario ${scenario.id} should have non-empty ruoloAI", scenario.ruoloAI.isNotBlank())
            assertTrue("Scenario ${scenario.id} should have non-empty ruoloUtente", scenario.ruoloUtente.isNotBlank())
            assertTrue("Scenario ${scenario.id} should have non-empty situazione", scenario.situazione.isNotBlank())
        }
    }

    @Test
    fun `all scenarios have unique ids`() {
        val ids = TutorService.SCENARI.map { it.id }
        assertEquals("All scenario ids should be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun `all scenarios have different roles for AI and user`() {
        TutorService.SCENARI.forEach { scenario ->
            assertTrue(
                "Scenario ${scenario.id}: AI and user roles should be different",
                scenario.ruoloAI != scenario.ruoloUtente
            )
        }
    }

    @Test
    fun `scenarios cover diverse everyday situations`() {
        val titoli = TutorService.SCENARI.map { it.titolo }
        assertTrue("Should have ristorante", titoli.any { it.contains("ristorante", ignoreCase = true) })
        assertTrue("Should have stazione", titoli.any { it.contains("stazione", ignoreCase = true) })
        assertTrue("Should have negozio", titoli.any { it.contains("negozio", ignoreCase = true) })
        assertTrue("Should have medico", titoli.any { it.contains("medico", ignoreCase = true) })
        assertTrue("Should have albergo", titoli.any { it.contains("albergo", ignoreCase = true) })
    }
}
