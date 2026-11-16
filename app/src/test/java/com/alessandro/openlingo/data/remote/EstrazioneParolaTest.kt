package com.alessandro.openlingo.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test della logica di estrazione JSON usata da
 * OpenLingoViewModel.analizzaEAggiungiParola (AI -> parola da aggiungere).
 * Verifica che il JSON del modello venga interpretato correttamente.
 */
class EstrazioneParolaTest {

    private data class ParolaEstratta(val tedesco: String, val articolo: String?, val italiano: String, val esempio: String, val livello: String)

    private fun parse(rispostaAi: String): ParolaEstratta {
        val json = Json.parseToJsonElement(rispostaAi.trimIndent()).jsonObject
        val tedesco = json["tedesco"]?.jsonPrimitive?.content?.trim().orEmpty()
        val articolo = json["articolo"]?.jsonPrimitive?.content?.trim()?.takeIf { it in setOf("der", "die", "das") }
        val italiano = json["italiano"]?.jsonPrimitive?.content?.trim().orEmpty().ifEmpty { tedesco }
        val esempio = json["esempio"]?.jsonPrimitive?.content?.trim().orEmpty()
        val livello = json["livello"]?.jsonPrimitive?.content?.trim()?.takeIf { it in setOf("A1", "A2", "B1", "B2") } ?: "A1"
        return ParolaEstratta(tedesco, articolo, italiano, esempio, livello)
    }

    @Test
    fun `estrae sostantivo con articolo`() {
        val p = parse("""
            {"tedesco":"Tisch","articolo":"der","italiano":"tavolo","esempio":"Der Tisch ist groß.","livello":"A1"}
        """)
        assertEquals("Tisch", p.tedesco)
        assertEquals("der", p.articolo)
        assertEquals("tavolo", p.italiano)
        assertTrue(p.esempio.contains("Tisch"))
        assertEquals("A1", p.livello)
    }

    @Test
    fun `articolo non valido diventa null e livello di default A1`() {
        val p = parse("""
            {"tedesco":"gehen","articolo":"uber","italiano":"andare","esempio":"Ich gehe nach Hause.","livello":"C1"}
        """)
        assertEquals("gehen", p.tedesco)
        assertNull(p.articolo)
        assertEquals("A1", p.livello)
    }

    @Test
    fun `italiano vuoto fa fallback al tedesco`() {
        val p = parse("""
            {"tedesco":"Haus","articolo":"das","italiano":"","esempio":"","livello":"A2"}
        """)
        assertEquals("Haus", p.italiano)
        assertEquals("das", p.articolo)
        assertEquals("A2", p.livello)
    }
}
