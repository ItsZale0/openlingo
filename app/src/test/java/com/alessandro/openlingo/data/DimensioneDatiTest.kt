package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.ConversazioneEntry
import com.alessandro.openlingo.data.local.EsercizioGenEntity
import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.LivelloIniziale
import com.alessandro.openlingo.data.local.ProfiloStato
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.ProfiloUtente
import com.alessandro.openlingo.data.local.ProfiliRepository
import com.alessandro.openlingo.data.local.RipassoLogEntity
import com.alessandro.openlingo.data.local.TipoProfilo
import com.alessandro.openlingo.data.local.TestGrammatica
import com.alessandro.openlingo.data.local.WordEntity
import com.alessandro.openlingo.data.remote.VoceGlossario
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * MISURA (non asserzione): quanto pesa il JSON che ProfileManager scrive in
 * DataStore a ogni modifica del profilo, con dati realistici di un corso biennale.
 */
class DimensioneDatiTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun parola(i: Int) = WordEntity(
        id = "w$i",
        german = "Vokabel$i",
        italian = "parola numero $i in italiano",
        example = "Das ist ein Beispielsatz fuer Vokabel$i im Kontext.",
        article = "das",
        pronunciation = "fokabel",
        level = "B1",
        lesson = (i % 12) + 1
    )

    private fun esercizio(i: Int) = EsercizioGenEntity(
        id = "e$i",
        tipo = "COMPRENSIONE",
        domanda = "Domanda numero $i su un testo letto in classe?",
        opzioni = listOf("Prima opzione", "Seconda opzione", "Terza opzione", "Quarta opzione"),
        rispostaCorretta = 1,
        spiegazione = "Spiegazione dettagliata del perche' la risposta e' corretta.",
        livello = "B1",
        fraseTedesca = "Ein Beispielsatz auf Deutsch fuer die Uebung Nummer $i.",
        traduzioneItaliana = "Una frase di esempio in tedesco per l'esercizio numero $i.",
        testoLettura = "Ein laengerer Lesetext auf Deutsch. ".repeat(30),
        glossario = listOf(VoceGlossario("das Wort", "la parola"), VoceGlossario("der Satz", "la frase"))
    )

    private fun profiloBase(): ProfiloUtente = ProfiliPreset.nuovo(
        id = TipoProfilo.ALESSANDRO.id,
        nome = "Sanitario",
        livello = LivelloIniziale.A0,
        mostraContestoMedico = true,
        tipo = TipoProfilo.ALESSANDRO,
        now = 1_700_000_000_000L
    )

    private fun repoCon(parole: Int, log: Int, esercizi: Int, conversazioni: Int): ProfiliRepository {
        val stato = ProfiloStato(
            parole = (1..parole).map { parola(it) },
            ripassiLog = (1..log).map { i ->
                RipassoLogEntity("w${i % parole}", 1_700_000_000_000L + i * 60_000L, i % 3 != 0, verifica = i % 5 == 0)
            },
            eserciziGeneratiComprensione = (1..esercizi).map { esercizio(it) },
            conversazioni = (1..conversazioni).map { i ->
                ConversazioneEntry(
                    id = "c$i", tipo = "tutor", ruolo = if (i % 2 == 0) "utente" else "tutor",
                    testo = "Messaggio numero $i della conversazione con il tutor di tedesco.",
                    timestamp = 1_700_000_000_000L + i
                )
            },
            lezioniGenerate = (1..12).associate { n -> "$n" to ("# Lezione $n\n\n" + "Testo della lezione in tedesco. ".repeat(40)) },
            progresso = ProgressoUtente(
                livelloCorrente = LivelloCEFR.B1,
                testGrammatica = (1..500).map { TestGrammatica(1_700_000_000_000L + it, 80f, 2, 10) },
                valutazioniProduzione = (1..500).map { i ->
                    ValutazioneProduzione(
                        erfuellung = 80f, kohaerenz = 75f, wortschatz = 78f, strukturen = 72f,
                        numErrori = 6, numParole = 150, livello = "B1",
                        correzione = "Correzione del tutor con spiegazioni. ".repeat(20),
                        data = 1_700_000_000_000L + i
                    )
                }
            )
        )
        // uso un profilo reale del preset: cosi' la config e' quella vera
        return ProfiliRepository(
            profili = mapOf(TipoProfilo.ALESSANDRO.id to profiloBase().copy(stato = stato)),
            profiloAttivoId = TipoProfilo.ALESSANDRO.id
        )
    }

    private fun misura(nome: String, repo: ProfiliRepository) {
        val s = json.encodeToString(ProfiliRepository.serializer(), repo)
        println("MISURA %-34s %7.2f MB  (%,d char)".format(nome, s.length / 1024.0 / 1024.0, s.length))
    }

    @Test
    fun `misura il peso dei dati`() {
        println("=========== PESO DEI DATI DEL PROFILO ===========")
        misura("solo parole (1200)", repoCon(parole = 1200, log = 0, esercizi = 0, conversazioni = 0))
        misura("+ log 5000", repoCon(parole = 1200, log = 5000, esercizi = 0, conversazioni = 0))
        misura("+ 320 esercizi", repoCon(parole = 1200, log = 5000, esercizi = 320, conversazioni = 0))
        misura("completo (+100 chat, 12 lezioni)", repoCon(parole = 1200, log = 5000, esercizi = 320, conversazioni = 100))
        println("================================================")
    }
}
