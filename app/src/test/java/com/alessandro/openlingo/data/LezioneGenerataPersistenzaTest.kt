package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.ProfiloStato
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La lezione generata deve SOPRAVVIVERE alla serializzazione (e quindi al riavvio
 * e al backup): se il campo non fosse serializzato, la lezione sparirebbe al primo
 * sync del feed (che ogni 15 min riscrive lezioneContenuto dal JSON remoto).
 */
class LezioneGenerataPersistenzaTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `le lezioni generate sopravvivono a serializzazione e deserializzazione`() {
        val stato = ProfiloStato(
            lezioneContenuto = "# Lezione 5",
            lezioniGenerate = mapOf("5" to "# Lezione 5 generata", "6" to "# Lezione 6 generata")
        )

        val riletto = json.decodeFromString(ProfiloStato.serializer(), json.encodeToString(ProfiloStato.serializer(), stato))

        assertEquals(2, riletto.lezioniGenerate.size)
        assertEquals("# Lezione 5 generata", riletto.lezioniGenerate["5"])
        assertEquals("# Lezione 6 generata", riletto.lezioniGenerate["6"])
    }

    @Test
    fun `un profilo salvato prima di questa feature resta leggibile (campo assente)`() {
        // JSON senza il campo lezioniGenerate: la deserializzazione NON deve fallire
        val vecchio = """{"parole":[],"reviews":{},"lezioneContenuto":"# vecchia"}"""
        val riletto = json.decodeFromString(ProfiloStato.serializer(), vecchio)
        assertTrue(riletto.lezioniGenerate.isEmpty())
        assertEquals("# vecchia", riletto.lezioneContenuto)
    }

    @Test
    fun `il contenuto generato e preferito a quello del feed`() {
        val stato = ProfiloStato(
            lezioneContenuto = "# dal feed",
            lezioniGenerate = mapOf("5" to "# generata")
        )
        // La regola applicata in WordRepository.aggiornaLezioneContenuto
        val mostrato = stato.lezioniGenerate["5"] ?: stato.lezioneContenuto
        assertEquals("# generata", mostrato)
    }
}
