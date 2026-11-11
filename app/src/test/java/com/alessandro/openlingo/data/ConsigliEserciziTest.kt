package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Il consiglio deve seguire le regole: parole fragili prima, poi la competenza
 * piu' debole, e ASCOLTO solo quando tutte superano la soglia ufficiale.
 */
class ConsigliEserciziTest {

    private val NOW = 1_700_000_000_000L

    private fun prog(
        lessico: Float = 0f,
        grammatica: Float = 0f,
        comprensione: Float = 0f,
        produzione: Float = 0f,
        paroleFragili: Int = 0,
        livello: LivelloCEFR = LivelloCEFR.A0,
        produzioni: Int = 0,
        mediaProduzione: Float = 0f
    ): CalcoloProgresso.ProgressoReale {
        val competenze = CalcoloCompetenze.PunteggiCompetenze(
            lessico = lessico, grammatica = grammatica,
            comprensione = comprensione, produzione = produzione,
            livelloComplessivo = livello
        )
        return CalcoloProgresso.ProgressoReale(
            livello = livello,
            livelloObiettivo = LivelloCEFR.B1,
            versoProssimoLivello = 0f,
            prossimoLivello = null,
            dimensioneDebole = "Lessico",
            dimensioneDeboleValore = lessico,
            azioneConsigliata = "Impara parole nuove",
            paroleImparate = 0,
            paroleFragili = paroleFragili,
            paroleDaVerificare = 0,
            produzioniScritte = produzioni,
            mediaProduzione = mediaProduzione,
            ultimoPraedikatProduzione = "",
            storicoProduzione = emptyList(),
            trendProduzione = 0f,
            correlazioneProduzione = 0f,
            produzioniSettimana = 0,
            obiettivoSettimanaleProduzione = 2,
            criterioDeboleProduzione = "N/A",
            criterioDeboleValore = 0f,
            evoluzioneErfuellung = emptyList(),
            evoluzioneKohaerenz = emptyList(),
            evoluzioneWortschatz = emptyList(),
            evoluzioneStrukturen = emptyList(),
            paroleDisponibili = 0,
            ripassiTotali = 0,
            accuratezza = 0f,
            streakGiorni = 0,
            streakRecord = 0,
            giorniStudioTotali = 0,
            minutiStudiati = 0,
            competenze = competenze,
            wortlisteUfficiale = 650,
            vocabolarioAttivoUfficiale = 325,
            oreGuida = 90..180,
            traguardi = emptyList()
        )
    }

    @Test
    fun `senza dati consiglia grammatica per iniziare`() {
        assertEquals("grammatica", ConsigliEsercizi.consiglio(null).route)
        assertEquals("grammatica", ConsigliEsercizi.consiglio(prog()).route)
    }

    @Test
    fun `parole fragili passano prima di tutto`() {
        val c = ConsigliEsercizi.consiglio(prog(paroleFragili = 3))
        assertEquals("ripasso", c.route)
        assertEquals("Ripasso", c.titolo)
    }

    @Test
    fun `lessico debole consiglia verifica lessico`() {
        val c = ConsigliEsercizi.consiglio(prog(lessico = 20f, grammatica = 50f, comprensione = 40f, produzione = 30f))
        assertEquals("verifica", c.route)
    }

    @Test
    fun `grammatica debole consiglia grammatica`() {
        val c = ConsigliEsercizi.consiglio(prog(lessico = 70f, grammatica = 30f, comprensione = 40f, produzione = 50f))
        assertEquals("grammatica", c.route)
    }

    @Test
    fun `comprensione debole consiglia lettura`() {
        val c = ConsigliEsercizi.consiglio(prog(lessico = 80f, grammatica = 70f, comprensione = 35f, produzione = 65f))
        assertEquals("comprensione", c.route)
    }

    @Test
    fun `produzione debole consiglia produzione`() {
        val c = ConsigliEsercizi.consiglio(prog(lessico = 90f, grammatica = 80f, comprensione = 70f, produzione = 45f))
        assertEquals("produzione", c.route)
    }

    @Test
    fun `tutte sopra soglia consiglia ascolto`() {
        val c = ConsigliEsercizi.consiglio(prog(lessico = 60f, grammatica = 70f, comprensione = 80f, produzione = 90f))
        assertEquals("ascolto", c.route)
    }

    @Test
    fun `motivo cita la percentuale della debole`() {
        val c = ConsigliEsercizi.consiglio(prog(lessico = 20f, grammatica = 50f, comprensione = 40f, produzione = 30f))
        org.junit.Assert.assertTrue(c.motivo.contains("20%"))
    }
}
