package com.alessandro.tedesco.data.remote

import com.alessandro.tedesco.data.local.ConfigurazioneAI
import kotlinx.serialization.Serializable

/**
 * Client del tutor AI.
 *
 * Non contiene più logica di rete: delega a [ClientAI], che conosce i formati
 * di Groq, ChatGPT, Claude, Gemini, OpenRouter e Kilo. Qui restano i prompt
 * didattici e la gestione della conversazione.
 */
class TutorService(
    private val clientAI: ClientAI = ClientAI()
) {

    /**
     * Livello di ragionamento per i modelli che lo supportano (gpt-oss).
     * null = default del provider.
     */
    @Volatile
    var reasoningEffort: String? = null

    /**
     * Invia un messaggio al tutor e ottiene la risposta.
     *
     * @param config provider, chiave e modello scelti dal profilo
     * @param cronologia conversazione precedente
     * @param livello livello CEFR dell'utente (A0-B2)
     * @param lezione contesto della lezione corrente
     * @param contestoUtente professione/interessi scritti dallo studente
     */
    suspend fun rispondi(
        config: ConfigurazioneAI,
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int,
        contestoUtente: String = ""
    ): String {
        val contesto = contestoUtente.trim().let {
            if (it.isEmpty()) ""
            else "\n12. Contesto dichiarato dallo studente: \"${it.take(400)}\". " +
                "Collega gli esempi a questo contesto quando e' utile, senza cambiare il livello."
        }

        val systemPrompt = """
            Sei un tutor di tedesco per uno studente italiano di livello $livello.

            REGOLE DA SEGUIRE ALLA LETTERA:
            1. Rispondi ESCLUSIVAMENTE in italiano. Spiegazioni, correzioni e domande sono in italiano.
            2. NON usare l'inglese. NON scrivere spiegazioni in tedesco. Il tedesco compare SOLO negli esempi (parole e frasi).
            3. Esempio corretto: Il sostantivo **der Tisch** (il tavolo) e maschile, quindi si dice con l'articolo **der**.
            4. Esempio SBAGLIATO da evitare: "The noun **der Tisch** is masculine." oppure "Das Nomen **der Tisch** ist maskulin."
            5. Rispondi in modo completo e dettagliato.
            6. I sostantivi tedeschi vanno sempre con l'articolo (der/die/das).
            7. Adatta la difficoltà al livello $livello: sii semplice e chiaro.
            8. La lezione corrente è la numero $lezione.
            9. Se lo studente sbaglia, correggi spiegando brevemente il perche.
            10. Usa markdown semplice: **grassetto** per i termini chiave, - per gli elenchi, ## per i titoli.
            11. Non usare tabelle ne HTML.$contesto
        """.trimIndent()

        return clientAI.chat(
            config = config,
            system = systemPrompt,
            cronologia = messaggiDa(cronologia),
            maxTokens = 2048,
            temperature = 0.7,
            reasoningEffort = reasoningEffort
        )
    }

    /**
     * Valutazione STRUTTURATA della produzione scritta, su criteri ufficiali Goethe.
     *
     * Il modello restituisce JSON con i tre criteri che richiedono giudizio
     * (adempimento, coerenza, lessico) piu' l'elenco degli errori. Il quarto
     * criterio (correttezza formale) NON viene chiesto al modello: si calcola
     * dalla densita' di errori, cosi' un modello generoso non puo' gonfiare.
     */
    suspend fun valutaProduzione(
        config: ConfigurazioneAI,
        testoStudente: String,
        livello: String,
        lezione: Int,
        contestoUtente: String = ""
    ): String {
        val systemPrompt = """
            Sei un valutatore di tedesco scritto per uno studente italiano di livello $livello.
            Valuti secondo i criteri ufficiali Goethe/telc per la prova di Schreiben.

            Restituisci SOLO un JSON valido, senza testo prima o dopo, senza ```json:
            {
              "erfuellung": <0-100, adempimento del compito: risponde a quanto chiesto?> ,
              "kohaerenz": <0-100, coerenza: il testo e' collegato e ordinato?>,
              "wortschatz": <0-100, lessico: appropriato e vario per il livello $livello?>,
              "errori": [ {"estratto": "<parte sbagliata>", "correzione": "<forma corretta>", "spiegazione": "<perche', in italiano>"} ],
              "correzione": "<commento in italiano, markdown semplice, che spiega gli errori principali>"
            }

            Regole:
            - Sii PRECISO, non generoso: un testo con molti errori non puo' avere lessico 90.
            - "errori" deve elencare TUTTI gli errori reali (grammatica, ortografia, articoli,
              casi, preposizioni, ordine parole, maiuscole dei sostantivi).
            - Se il testo e' corretto, "errori" e' un array vuoto.
            - La lezione corrente e' la numero $lezione.
            - Non inventare errori che non ci sono; non ometterne di evidenti.${if (contestoUtente.isBlank()) "" else "\n            - CONTESTO DELLO STUDENTE: \"${contestoUtente.trim().take(400)}\". Considera il lessico pertinente a questo contesto nella valutazione di wortschatz."}
        """.trimIndent()

        return clientAI.chat(
            config = config,
            system = systemPrompt,
            cronologia = listOf(MessaggioAI("user", testoStudente)),
            maxTokens = 2048,
            temperature = 0.3,
            reasoningEffort = reasoningEffort
        )
    }

    /**
     * Corregge una risposta in tedesco di uno studente.
     *
     * @return la correzione in italiano, una riga per errore
     */
    suspend fun correggiRisposta(
        config: ConfigurazioneAI,
        testoStudente: String,
        livello: String,
        lezione: Int
    ): String {
        val systemPrompt = """
            Sei un correttore di tedesco per uno studente italiano di livello $livello.
            Regole:
            - Analizza la risposta dello studente e individua TUTTI gli errori (grammatica, vocabolario, sintassi, articoli, preposizioni, coniugazioni).
            - Per ogni errore, scrivi una riga nel formato: "- **Errore**: [descrizione] → **Correzione**: [forma corretta]"
            - Se non ci sono errori, rispondi: "Corretto! Ottimo lavoro."
            - Spiega brevemente il perché di ogni correzione.
            - La lezione corrente è la numero $lezione.
            - Usa markdown semplice: **grassetto** per i termini chiave, - per gli elenchi.
            - Non usare tabelle né HTML.
        """.trimIndent()

        return clientAI.chat(
            config = config,
            system = systemPrompt,
            cronologia = listOf(MessaggioAI("user", testoStudente)),
            maxTokens = 2048,
            temperature = 0.3,
            reasoningEffort = reasoningEffort
        )
    }

    /**
     * Scenario di gioco di ruolo preimpostato.
     */
    @Serializable
    data class RoleplayScenario(
        val id: String,
        val titolo: String,
        val descrizione: String,
        val ruoloAI: String,
        val ruoloUtente: String,
        val situazione: String
    )

    /**
     * Avvia un gioco di ruolo con uno scenario preimpostato.
     */
    suspend fun roleplay(
        config: ConfigurazioneAI,
        scenario: RoleplayScenario,
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int,
        contestoUtente: String = ""
    ): String {
        val contesto = contestoUtente.trim().let {
            if (it.isEmpty()) ""
            else "\nCONTESTO DELLO STUDENTE: \"${it.take(400)}\". Adatta il ruolo e il lessico a questo contesto quando ha senso."
        }
        val systemPrompt = """
            Sei un ${scenario.ruoloAI} in un gioco di ruolo per uno studente italiano di tedesco di livello $livello.

            SCENARIO: ${scenario.titolo}
            SITUAZIONE: ${scenario.situazione}
            TUO RUOLO: ${scenario.ruoloAI}
            RUOLO DELLO STUDENTE: ${scenario.ruoloUtente}

            Regole:
            - Interagisci SEMPRE in tedesco (con traduzione in italiano tra parentesi se necessario).
            - Resti nel personaggio per tutta la conversazione.
            - Adatta la difficoltà al livello $livello: usa frasi semplici e vocabolario di base.
            - Se lo studente sbaglia, correggi gentilmente e continua la conversazione.
            - La lezione corrente è la numero $lezione.
            - Mantieni la conversazione naturale e realistica.
            - Se lo studente usa l'italiano, rispondi in tedesco incoraggiandolo a continuare.
            - Usa markdown semplice: **grassetto** per i termini chiave.
            - Non usare tabelle né HTML.$contesto
        """.trimIndent()

        return clientAI.chat(
            config = config,
            system = systemPrompt,
            cronologia = messaggiDa(cronologia),
            maxTokens = 2048,
            temperature = 0.7,
            reasoningEffort = reasoningEffort
        )
    }

    /** Converte la cronologia dell'app in messaggi per il client. */
    private fun messaggiDa(cronologia: List<Pair<String, String>>): List<MessaggioAI> =
        cronologia.takeLast(8).map { (ruolo, testo) ->
            MessaggioAI(if (ruolo == "utente" || ruolo == "user") "user" else "assistant", testo)
        }

    /**
     * Modelli suggeriti per il provider scelto.
     *
     * L'elenco è statico: i provider cambiano modelli spesso, quindi il campo
     * modello resta liberamente modificabile dall'utente.
     */
    fun modelliDisponibili(config: ConfigurazioneAI): List<Pair<String?, String>> =
        ClientAI.modelliConsigliati(config.provider)

    companion object {
        /**
         * Scenari di gioco di ruolo preimpostati.
         */
        val SCENARI = listOf(
            RoleplayScenario(
                id = "ristorante",
                titolo = "Al ristorante",
                descrizione = "Ordina un pasto in un ristorante tedesco",
                ruoloAI = "Cameriere",
                ruoloUtente = "Cliente",
                situazione = "Sei in un ristorante a Berlino. Il cameriere ti accoglie e ti porta al tavolo."
            ),
            RoleplayScenario(
                id = "stazione",
                titolo = "Alla stazione",
                descrizione = "Chiedi informazioni e biglietti alla stazione",
                ruoloAI = "Impiegato della stazione",
                ruoloUtente = "Viaggiatore",
                situazione = "Sei alla stazione centrale di Monaco e devi comprare un biglietto per Vienna."
            ),
            RoleplayScenario(
                id = "negozio",
                titolo = "Negozio",
                descrizione = "Fai shopping e chiedi informazioni sui prodotti",
                ruoloAI = "Commesso",
                ruoloUtente = "Cliente",
                situazione = "Sei in un negozio di abititi a Amburgo e cerchi un vestito per una festa."
            ),
            RoleplayScenario(
                id = "medico",
                titolo = "Al medico",
                descrizione = "Descrivi i tuoi sintomi al medico",
                ruoloAI = "Medico",
                ruoloUtente = "Paziente",
                situazione = "Sei dal medico perché hai mal di testa e febbre da due giorni."
            ),
            RoleplayScenario(
                id = "albergo",
                titolo = "In albergo",
                descrizione = "Fai il check-in e chiedi informazioni",
                ruoloAI = "Receptionist",
                ruoloUtente = "Ospite",
                situazione = "Arrivi in un albergo a Zurigo e fai il check-in. Hai una prenotazione."
            )
        )
    }
}
