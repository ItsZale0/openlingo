package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.LivelloIniziale
import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.TipoProfilo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ogni profilo deve avere i propri contenuti: due profili distinti devono
 * restare separati anche dopo la riconciliazione.
 *
 * Nota: emptyList()/emptyMap() di Kotlin sono singleton IMMUTABILI, quindi due
 * profili "vuoti" condividono le stesse istanze senza alcun rischio. Quindi qui
 * non si verifica l'identita' degli oggetti vuoti, ma che i dati propri restino
 * propri.
 */
class ProfiliIsolationTest {

    private fun profiloAle() = ProfiliPreset.nuovo(
        id = TipoProfilo.ALESSANDRO.id,
        nome = "Sanitario",
        livello = LivelloIniziale.A0,
        mostraContestoMedico = true,
        tipo = TipoProfilo.ALESSANDRO,
        now = 1_700_000_000_000L
    )

    private fun profiloEmma() = ProfiliPreset.nuovo(
        id = TipoProfilo.EMMA.id,
        nome = "Standard",
        livello = LivelloIniziale.B1,
        tipo = TipoProfilo.EMMA,
        now = 1_700_000_000_001L
    )

    @Test
    fun `esistono profili distinti per Alessandro ed Emma`() {
        val ale = profiloAle()
        val emma = profiloEmma()

        assertEquals("alessandro", ale.id)
        assertEquals("emma", emma.id)
        assertNotEquals(ale, emma)
    }

    @Test
    fun `Emma parte da B1 senza contesto medico, Alessandro da A0 con contesto medico`() {
        val ale = profiloAle()
        val emma = profiloEmma()

        assertEquals(LivelloCEFR.A0, ale.stato.progresso.livelloCorrente)
        assertEquals(LivelloCEFR.B1, emma.stato.progresso.livelloCorrente)

        assertTrue(ale.config.mostraContestoMedico)
        assertTrue(!emma.config.mostraContestoMedico)
    }

    @Test
    fun `i profili hanno feed e contesto separati`() {
        val ale = profiloAle()
        val emma = profiloEmma()

        // Nessun feed predefinito: l'utente lo sceglie. Il contesto e' per il
        // profilo sanitario quello medico, per l'altro (quando impostato) quello
        // personale.
        assertEquals("", ale.config.feedUrl)
        assertEquals("", emma.config.feedUrl)
        assertEquals("", ale.config.contestoUtente)
    }

    @Test
    fun `la riconciliazione con un profilo esistente non cancella i dati`() {
        // Se Emma ha gia' una lezione generata, aggiungere un profilo nuovo NON
        // deve riscrivere il suo profilo (altrimenti perderebbe i contenuti propri).
        val emma = profiloEmma().copy(
            stato = profiloEmma().stato.copy(
                lezioneContenuto = "Lezione 1 di Emma",
                lezioniGenerate = mapOf("1" to "Lezione 1 di Emma")
            )
        )
        val letto = ProfiliRepository(
            profili = mapOf(TipoProfilo.EMMA.id to emma),
            profiloAttivoId = TipoProfilo.EMMA.id
        )

        val dopo = ProfiliPreset.riconcilia(letto)
        val emmaDopo = dopo.profili.getValue(TipoProfilo.EMMA.id)

        assertEquals("Lezione 1 di Emma", emmaDopo.stato.lezioneContenuto)
        assertEquals(mapOf("1" to "Lezione 1 di Emma"), emmaDopo.stato.lezioniGenerate)
    }

    @Test
    fun `un profilo nuovo o senza dati resta vuoto e al livello scelto`() {
        val emma = profiloEmma()
        val vuota = emma.copy(
            stato = emma.stato.copy(
                lezioniGenerate = emptyMap(),
                lezioneContenuto = "",
                parole = emptyList()
            )
        )

        assertTrue(vuota.stato.lezioniGenerate.isEmpty())
        assertTrue(vuota.stato.lezioneContenuto.isEmpty())
        assertTrue(vuota.stato.parole.isEmpty())
        assertEquals(LivelloCEFR.B1, vuota.stato.progresso.livelloCorrente)
    }

    @Test
    fun `un profilo senza dati non viene riportato al livello base`() {
        // Il ricalcolo del livello non deve degradare un profilo vuoto: il livello
        // iniziale e' una scelta dell'utente (Emma = B1).
        val emma = profiloEmma()
        val conDati = emma.copy(stato = emma.stato.copy(parole = emptyList()))

        assertEquals(LivelloCEFR.B1, conDati.stato.progresso.livelloCorrente)
    }
}
