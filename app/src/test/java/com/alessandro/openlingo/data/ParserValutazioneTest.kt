package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test del parser della valutazione (JSON del tutor: robusto e anti-gonfiaggio). */
class ParserValutazioneTest {

    @Test
    fun `estrae i tre criteri e il numero di errori`() {
        val json = """{"erfuellung":80,"kohaerenz":70,"wortschatz":60,
            "errori":[{"estratto":"a","correzione":"b","spiegazione":"c"}],
            "correzione":"commento"}"""
        val r = ParserValutazione.parse(json)
        assertEquals(80f, r.erfuellung!!, 0.01f)
        assertEquals(70f, r.kohaerenz!!, 0.01f)
        assertEquals(60f, r.wortschatz!!, 0.01f)
        assertEquals(1, r.numErrori)
        assertEquals("commento", r.correzione)
    }

    @Test
    fun `tollera il blocco markdown e il testo attorno`() {
        val json = "Ecco la valutazione:\n```json\n{\"erfuellung\":50,\"kohaerenz\":50,\"wortschatz\":50,\"errori\":[],\"correzione\":\"ok\"}\n```"
        val r = ParserValutazione.parse(json)
        assertEquals(50f, r.erfuellung!!, 0.01f)
        assertEquals(0, r.numErrori)
    }

    @Test
    fun `un campo mancante resta null e non diventa un voto alto`() {
        val json = """{"kohaerenz":70,"errori":[]}"""
        val r = ParserValutazione.parse(json)
        assertEquals(null, r.erfuellung)
        assertEquals(70f, r.kohaerenz!!, 0.01f)
        assertEquals(null, r.wortschatz)
    }

    @Test
    fun `json illeggibile non fa crashare`() {
        val r = ParserValutazione.parse("non e' json")
        assertEquals(null, r.erfuellung)
        assertEquals(0, r.numErrori)
    }

    @Test
    fun `i voti fuori scala vengono limitati a 0-100`() {
        val json = """{"erfuellung":150,"kohaerenz":-20,"wortschatz":80,"errori":[]}"""
        val r = ParserValutazione.parse(json)
        assertEquals(100f, r.erfuellung!!, 0.01f)
        assertEquals(0f, r.kohaerenz!!, 0.01f)
    }

    @Test
    fun `deduplica gli errori contenuti in altri piu ampi`() {
        // Il modello elenca "Deutch" e anche la frase che lo contiene
        val json = """{"errori":[
            {"estratto":"Deutch","correzione":"Deutsch","spiegazione":"ortografia"},
            {"estratto":"ich lerne Deutch.","correzione":"ich lerne Deutsch.","spiegazione":"maiuscola"}
        ]}"""
        val errori = ParserValutazione.errori(json)
        assertEquals("il piu' corto e' assorbito dal piu' ampio", 1, errori.size)
        assertEquals("ich lerne Deutch.", errori[0].estratto)
    }

    @Test
    fun `deduplica errori esatti ripetuti`() {
        val json = """{"errori":[
            {"estratto":"jehtz","correzione":"jetzt","spiegazione":"x"},
            {"estratto":"jehtz","correzione":"jetzt","spiegazione":"x"}
        ]}"""
        assertEquals(1, ParserValutazione.errori(json).size)
    }

    @Test
    fun `errori diversi restano tutti`() {
        val json = """{"errori":[
            {"estratto":"jehtz","correzione":"jetzt","spiegazione":"x"},
            {"estratto":"student","correzione":"Student","spiegazione":"y"}
        ]}"""
        assertEquals(2, ParserValutazione.errori(json).size)
    }

    @Test
    fun `elenco leggibile formatta con grassetto`() {
        val json = """{"errori":[{"estratto":"jehtz","correzione":"jetzt","spiegazione":"ortografia"}]}"""
        val el = ParserValutazione.elencaErrori(json)
        assertEquals(1, el.size)
        assertTrue(el[0].contains("**jehtz**"))
        assertTrue(el[0].contains("**jetzt**"))
    }
}
