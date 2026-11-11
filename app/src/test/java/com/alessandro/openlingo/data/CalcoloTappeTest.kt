package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.TappaEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CalcoloTappeTest {

    private fun tappa(nome: String, lezioni: String, stato: String = "") = TappaEntity(
        nome = nome,
        descrizione = "Descrizione $nome",
        lezioni = lezioni,
        stato = stato
    )

    private fun progresso(lezione: Int, parole: Int = 0, livello: LivelloCEFR = LivelloCEFR.A0) =
        CalcoloTappe.ProgressoTappe(
            lezioneCorrente = lezione,
            paroleApprese = parole,
            livelloCorrente = livello
        )

    // --- Test 1: tappa completata quando lezione corrente supera il range ---
    @Test
    fun `tappa completata quando lezione corrente supera il range`() {
        val tappe = listOf(tappa("Tappa 1", "1-10"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 11))
        assertEquals("completata", risultato[0].stato)
    }

    // --- Test 2: tappa in corso quando lezione corrente è nel range ---
    @Test
    fun `tappa in corso quando lezione corrente è nel range`() {
        val tappe = listOf(tappa("Tappa 1", "1-10"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 5))
        assertEquals("in corso", risultato[0].stato)
    }

    // --- Test 3: tappa da iniziare quando lezione corrente è prima del range ---
    @Test
    fun `tappa da iniziare quando lezione corrente è prima del range`() {
        val tappe = listOf(tappa("Tappa 1", "11-20"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 5))
        assertEquals("da iniziare", risultato[0].stato)
    }

    // --- Test 4: tappa con singola lezione ---
    @Test
    fun `tappa con singola lezione completata`() {
        val tappe = listOf(tappa("Tappa 1", "5"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 6))
        assertEquals("completata", risultato[0].stato)
    }

    // --- Test 5: tappa con singola lezione in corso ---
    @Test
    fun `tappa con singola lezione in corso`() {
        val tappe = listOf(tappa("Tappa 1", "5"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 5))
        assertEquals("in corso", risultato[0].stato)
    }

    // --- Test 6: tappa con singola lezione da iniziare ---
    @Test
    fun `tappa con singola lezione da iniziare`() {
        val tappe = listOf(tappa("Tappa 1", "5"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 4))
        assertEquals("da iniziare", risultato[0].stato)
    }

    // --- Test 7: multiple tappe con stati misti ---
    @Test
    fun `multiple tappe con stati misti`() {
        val tappe = listOf(
            tappa("Tappa 1", "1-10"),
            tappa("Tappa 2", "11-20"),
            tappa("Tappa 3", "21-30")
        )
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 15))
        assertEquals("completata", risultato[0].stato)
        assertEquals("in corso", risultato[1].stato)
        assertEquals("da iniziare", risultato[2].stato)
    }

    // --- Test 8: tappa con range vuoto ---
    @Test
    fun `tappa con range vuoto resta da iniziare`() {
        val tappe = listOf(tappa("Tappa 1", ""))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 5))
        assertEquals("da iniziare", risultato[0].stato)
    }

    // --- Test 9: tappa con range invalido ---
    @Test
    fun `tappa con range invalido resta da iniziare`() {
        val tappe = listOf(tappa("Tappa 1", "abc"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 5))
        assertEquals("da iniziare", risultato[0].stato)
    }

    // --- Test 10: tappa con range "1-10" e lezione 10 è ancora in corso ---
    @Test
    fun `tappa con lezione alla fine del range è in corso`() {
        val tappe = listOf(tappa("Tappa 1", "1-10"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 10))
        assertEquals("in corso", risultato[0].stato)
    }

    // --- Test 11: tappa con range "1-10" e lezione 1 è in corso ---
    @Test
    fun `tappa con lezione all inizio del range è in corso`() {
        val tappe = listOf(tappa("Tappa 1", "1-10"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 1))
        assertEquals("in corso", risultato[0].stato)
    }

    // --- Test 12: parseRange con range valido ---
    @Test
    fun `parseRange con range valido`() {
        val range = CalcoloTappe.parseRange("1-10")
        assertEquals(1, range?.first)
        assertEquals(10, range?.second)
    }

    // --- Test 13: parseRange con singola lezione ---
    @Test
    fun `parseRange con singola lezione`() {
        val range = CalcoloTappe.parseRange("5")
        assertEquals(5, range?.first)
        assertEquals(5, range?.second)
    }

    // --- Test 14: parseRange con stringa vuota ---
    @Test
    fun `parseRange con stringa vuota`() {
        val range = CalcoloTappe.parseRange("")
        assertEquals(null, range)
    }

    // --- Test 15: parseRange con stringa non numerica ---
    @Test
    fun `parseRange con stringa non numerica`() {
        val range = CalcoloTappe.parseRange("abc")
        assertEquals(null, range)
    }

    // --- Test 16: parseRange con range malformato ---
    @Test
    fun `parseRange con range malformato`() {
        val range = CalcoloTappe.parseRange("1-2-3")
        assertEquals(null, range)
    }

    // --- Test 17: tappe vuote ---
    @Test
    fun `lista tappe vuota`() {
        val risultato = CalcoloTappe.calcola(emptyList(), progresso(lezione = 5))
        assertEquals(0, risultato.size)
    }

    // --- Test 18: tappa con spazi nel range ---
    @Test
    fun `tappa con spazi nel range`() {
        val tappe = listOf(tappa("Tappa 1", " 1 - 10 "))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 5))
        assertEquals("in corso", risultato[0].stato)
    }

    // --- Test 19: tappa con range "0-5" ---
    @Test
    fun `tappa con range che inizia da 0`() {
        val tappe = listOf(tappa("Tappa 1", "0-5"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 3))
        assertEquals("in corso", risultato[0].stato)
    }

    // --- Test 20: tappa con range "0-5" e lezione 6 è completata ---
    @Test
    fun `tappa con range 0-5 e lezione 6 è completata`() {
        val tappe = listOf(tappa("Tappa 1", "0-5"))
        val risultato = CalcoloTappe.calcola(tappe, progresso(lezione = 6))
        assertEquals("completata", risultato[0].stato)
    }
}
