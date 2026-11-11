package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test dei moduli didattici nuovi: esercizi produttivi, quaderno errori (SRS),
 * test di ingresso, task d'esame, Wortliste, dialoghi, Südtirol, pronuncia.
 * Sono tutti dati puri/offline: se cambiano, questi test lo dicono subito.
 */
class NuoviModuliTest {

    // ---------------- Produzione attiva ----------------

    @Test
    fun bancaProduttiva_copreTuttiITipiETuttiILivelli() {
        assertTrue("servono almeno 60 esercizi", BancaProduttiva.tutti.size >= 60)
        TipoProduttivo.entries.forEach { tipo ->
            assertTrue("nessun esercizio ${tipo.name}", BancaProduttiva.perTipo(tipo).isNotEmpty())
        }
        listOf("A1", "A2", "B1").forEach { livello ->
            assertTrue(
                "nessun esercizio per $livello",
                BancaProduttiva.perLivello(livello).isNotEmpty()
            )
        }
    }

    @Test
    fun bancaProduttiva_ogniEsercizioHaRispostaEConsegna() {
        assertTrue(BancaProduttiva.tutti.all { it.risposta.isNotBlank() })
        assertTrue(BancaProduttiva.tutti.all { it.prompt.isNotBlank() })
        assertTrue(BancaProduttiva.tutti.all { it.consegna.isNotBlank() })
        // Il riordino deve contenere esattamente le parole della risposta.
        BancaProduttiva.perTipo(TipoProduttivo.RIORDINO).forEach { e ->
            assertTrue("riordino senza parole: ${e.id}", e.parole.isNotEmpty())
            assertEquals(
                "parole e risposta divergono in ${e.id}",
                RisposteProduttive.normalizza(e.risposta),
                RisposteProduttive.normalizza(e.parole.joinToString(" "))
            )
        }
        // Il dettato deve avere un testo da pronunciare.
        BancaProduttiva.perTipo(TipoProduttivo.DETTATO).forEach { e ->
            assertNotNull("dettato senza audio: ${e.id}", e.daAscoltare)
        }
    }

    @Test
    fun risposteProduttive_accettaUmlautAlternativiEMaiuscole() {
        val e = EsercizioProduttivo(
            id = "x",
            tipo = TipoProduttivo.TRADUZIONE,
            consegna = "Traduci",
            prompt = "Mi chiamo Anna.",
            risposta = "Ich heiße Anna.",
            alternative = listOf("Ich heisse Anna.")
        )
        assertTrue(RisposteProduttive.valuta("ich heisse anna", e))
        assertTrue(RisposteProduttive.valuta("  ICH HEISSE ANNA!  ", e))
        assertFalse(RisposteProduttive.valuta("Ich bin Anna", e))
        assertFalse(RisposteProduttive.valuta("", e))
    }

    // ---------------- Quaderno errori (SRS) ----------------

    @Test
    fun erroriSrs_intervalliCrescentiERisoluzione() {
        var errore = ErroriSRS.nuovo(
            tipo = "grammatica",
            domanda = "Ich ___ Anna.",
            rispostaCorretta = "heiße",
            livello = "A1",
            now = 0L
        )
        assertEquals(1, errore.intervalloGiorni)
        assertFalse(errore.risolto)

        val giorni = mutableListOf<Int>()
        repeat(5) {
            errore = ErroriSRS.rispondi(errore, saputo = true, now = it.toLong())
            giorni.add(errore.intervalloGiorni)
        }
        assertEquals(listOf(1, 3, 7, 16, 35), giorni)
        assertTrue("dopo 5 richiami è risolto", errore.risolto)

        val sbagliato = ErroriSRS.rispondi(errore, saputo = false, now = 100L)
        assertFalse(sbagliato.risolto)
        assertEquals(1, sbagliato.intervalloGiorni)
        assertEquals(0, sbagliato.ripetizioni)
    }

    @Test
    fun erroriSrs_chiaveStabileEvitaDuplicati() {
        val a = ErroriSRS.nuovo("grammatica", "Das ___ ist neu.", "Buch")
        val b = ErroriSRS.nuovo("GRAMMATICA", "das ___ ist neu!", "buch")
        assertEquals(a.chiave, b.chiave)
    }

    @Test
    fun erroriSrs_daRipassareSoloAllaScadenza() {
        val errore = ErroriSRS.nuovo(
            tipo = "cloze",
            domanda = "Ich ___ 20 Jahre alt.",
            rispostaCorretta = "bin",
            now = 0L
        )
        assertFalse(ErroriSRS.daRipassare(errore, now = 0L))
        assertTrue(ErroriSRS.daRipassare(errore, now = 24L * 60L * 60L * 1000L))
        assertEquals(0, ErroriSRS.giorniAllaScadenza(errore, now = errore.prossimaRipasso))
    }

    // ---------------- Placement test ----------------

    @Test
    fun placementTest_dodiciDomandeConOpzioniValide() {
        assertEquals(12, PlacementTest.domande.size)
        PlacementTest.domande.forEach { d ->
            assertTrue("opzioni insufficienti: ${d.domanda}", d.opzioni.size >= 3)
            assertTrue("indice corretto fuori range: ${d.domanda}", d.corretta in d.opzioni.indices)
        }
    }

    @Test
    fun placementTest_soglie() {
        assertEquals(LivelloCEFR.A0, PlacementTest.valuta(0).livello)
        assertEquals(LivelloCEFR.A1, PlacementTest.valuta(4).livello)
        assertEquals(LivelloCEFR.A2, PlacementTest.valuta(7).livello)
        assertEquals(LivelloCEFR.B1, PlacementTest.valuta(10).livello)
        assertEquals(12, PlacementTest.valuta(12).totale)
    }

    // ---------------- Task d'esame ----------------

    @Test
    fun taskB1_scritturaEOraleCompleti() {
        assertTrue(TaskB1.scrittura.size >= 10)
        assertTrue(TaskB1.orale.size >= 6)
        TaskB1.scrittura.forEach { task ->
            assertTrue(task.paroleMin in 20..200)
            assertTrue(task.paroleMax > task.paroleMin)
            assertTrue("punti mancanti in ${task.id}", task.punti.size >= 2)
            assertTrue(task.istruzioni().contains("Punti da coprire"))
        }
        TaskB1.orale.forEach { task ->
            assertTrue(task.descrizione.isNotBlank())
            assertTrue(task.punti.isNotEmpty())
        }
    }

    // ---------------- Wortliste ----------------

    @Test
    fun wortlisteB1_almenoCentoParoleConTema() {
        assertTrue("Wortliste troppo corta", WortlisteB1.tutte.size >= 100)
        assertTrue(WortlisteB1.tutte.all { it.italiano.isNotBlank() })
        assertTrue(WortlisteB1.tutte.all { it.tema.isNotBlank() })
        assertTrue(WortlisteB1.cerca("Termin").any { it.tedesco.contains("Termin") })
        assertTrue(WortlisteB1.cerca("appuntamento").any { it.italiano.contains("appuntamento") })
    }

    @Test
    fun wortlisteB1_separaArticolo() {
        assertEquals("der", WortlisteB1.articoloDi("der Termin"))
        assertEquals("die", WortlisteB1.articoloDi("die Bewerbung"))
        assertEquals("das", WortlisteB1.articoloDi("das Gehalt"))
        assertNull(WortlisteB1.articoloDi("Termin"))
        assertEquals("Termin", WortlisteB1.parolaSenzaArticolo("der Termin"))
    }

    // ---------------- Dialoghi ----------------

    @Test
    fun dialoghi_hannoDomandeValideETraduzioni() {
        assertTrue(Dialoghi.dialoghi.size >= 5)
        Dialoghi.dialoghi.forEach { dialogo ->
            assertTrue(dialogo.battute.size >= 4)
            assertTrue(dialogo.domande.isNotEmpty())
            assertTrue(dialogo.battute.all { it.testo.isNotBlank() && it.traduzione.isNotBlank() })
            dialogo.domande.forEach { domanda ->
                assertTrue(domanda.corretta in domanda.opzioni.indices)
            }
        }
    }

    // ---------------- Südtirol e pronuncia ----------------

    @Test
    fun sudtirol_contenutiPresenti() {
        assertTrue(Sudtirol.varianti.size >= 10)
        assertTrue(Sudtirol.lessico.size >= 10)
        assertTrue(Sudtirol.frasi.isNotEmpty())
        assertTrue(Sudtirol.frasi.all { it.tedesco.isNotBlank() && it.italiano.isNotBlank() })
    }

    @Test
    fun pronuncia_coppieEConsigliPresenti() {
        assertTrue(Pronuncia.coppie.size >= 8)
        assertTrue(Pronuncia.consigli.size >= 5)
        assertTrue(Pronuncia.scioglilingua.isNotEmpty())
        assertTrue(Pronuncia.frasiCliniche.isNotEmpty())
        Pronuncia.coppie.forEach { coppia ->
            assertTrue(coppia.a.isNotBlank() && coppia.b.isNotBlank())
            assertTrue(coppia.a != coppia.b)
        }
    }

    // ---------------- Export per l'insegnante ----------------

    @Test
    fun reportInsegnante_csvConIntestazioneEEscape() {
        val errore = ErroriSRS.nuovo(
            tipo = "grammatica",
            domanda = "Das ___ ist neu; wirklich",
            rispostaCorretta = "Buch",
            rispostaData = "ein \"Buch\"",
            now = 1000L
        )
        val csv = ReportInsegnante.csvErrori(listOf(errore))
        assertTrue(csv.startsWith("tipo;livello;domanda;risposta_corretta;"))
        assertTrue(csv.contains("\"Das ___ ist neu; wirklich\""))
        assertTrue(csv.contains("\"ein \"\"Buch\"\"\""))
        assertTrue(csv.contains(";aperto;"))
    }
}
