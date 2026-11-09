package com.alessandro.tedesco.data.remote

import kotlinx.coroutines.delay
import com.alessandro.tedesco.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import com.alessandro.tedesco.di.HttpCondiviso
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
private data class GroqModel(val id: String)

@Serializable
private data class GroqModels(val data: List<GroqModel> = emptyList())

@Serializable
private data class ChatMessage(val role: String, val content: String)

@Serializable
private data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 2048,
    val temperature: Double = 0.7,
    @SerialName("reasoning_effort") val reasoningEffort: String? = null
)

@Serializable
private data class ChatChoice(val message: ChatMessage? = null)

@Serializable
private data class ChatResponse(val choices: List<ChatChoice> = emptyList())

@Serializable
private data class GeminiPart(val text: String)

@Serializable
private data class GeminiContent(val role: String? = null, val parts: List<GeminiPart>)

@Serializable
private data class GeminiSystemInstruction(val parts: List<GeminiPart>)

@Serializable
private data class GeminiGenerationConfig(
    @SerialName("maxOutputTokens") val maxOutputTokens: Int = 2048,
    val temperature: Double = 0.7
)

@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("system_instruction") val systemInstruction: GeminiSystemInstruction? = null,
    @SerialName("generationConfig") val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

@Serializable
private data class GeminiCandidate(val content: GeminiContent? = null)

@Serializable
private data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())

/**
 * Client per il tutor AI via Groq.
 *
 * Usa un modello veloce ed economico (Llama 3.1 8B) per risposte
 * in tempo reale. La chiave API è configurata dall'utente nelle impostazioni.
 */
class TutorService {

    private val client = HttpCondiviso.client

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Invia un messaggio al tutor e ottiene la risposta.
     *
     * @param apiKey chiave Groq
     * @param cronologia conversazione precedente
     * @param livello livello CEFR dell'utente (A0-B2)
     * @param lezione contesto della lezione corrente
     */
    // Livello di ragionamento Groq (null = default). Usato solo dai modelli che lo supportano (gpt-oss).
    @Volatile
    var reasoningEffort: String? = null

    suspend fun rispondi(
        apiKey: String,
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int,
        modello: String? = null,
        geminiApiKey: String = "",
        contestoUtente: String = ""
    ): String = withContext(Dispatchers.IO) {
        // Free mode (no API key): uses Gemini endpoint automatically

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

        val messaggi = buildList {
            add(ChatMessage("system", systemPrompt))
            cronologia.takeLast(8).forEach { (ruolo, testo) ->
                add(ChatMessage(if (ruolo == "utente") "user" else "assistant", testo))
            }
        }

        // Provider Gemini (chiave separata): se presente, usa Gemini
        if (geminiApiKey.isNotBlank()) {
            var ultimoErroreG: String? = null
            for (tentativo in 1..2) {
                try {
                    return@withContext chiamaGemini(geminiApiKey, systemPrompt, messaggi)
                } catch (e: IOException) {
                    ultimoErroreG = e.message
                    if (e.message?.contains("non valida") == true) throw e
                    if (tentativo < 2) delay(4000)
                }
            }
            throw IOException(ultimoErroreG ?: "Nessun modello Gemini disponibile al momento")
        }

        // Se l'utente ha scelto un modello, usa quello; altrimenti prova i free in cascata
        val modelli = if (!modello.isNullOrBlank()) listOf(modello)
            else if (apiKey.isBlank()) MODELLI_NO_KEY else MODELLI_FREE

        var ultimoErrore: String? = null
        var rateLimited = false
        for (tentativo in 1..2) {
            for (m in modelli) {
                try {
                    return@withContext chiamaModello(apiKey, m, messaggi)
                } catch (e: IOException) {
                    ultimoErrore = e.message
                    // 401 = chiave non valida: inutile provare altri modelli
                    if (e.message?.contains("non valida") == true) throw e
                    if (e.message?.contains("Troppe richieste") == true) rateLimited = true
                    // altrimenti prova il prossimo modello
                }
            }
            if (!rateLimited) break
            if (tentativo < 2) delay(4000)
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
    }

    /**
     * Corregge una risposta in tedesco di uno studente.
     *
     * @param apiKey chiave Groq
     * @param testoStudente la risposta da correggere
     * @param livello livello CEFR dell'utente (A0-B2)
     * @param lezione contesto della lezione corrente
     * @param modello modello opzionale (null = automatico)
     * @return la correzione in italiano, una riga per errore
     */
    /**
     * Valutazione STRUTTURATA della produzione scritta, su criteri ufficiali Goethe.
     *
     * Il modello restituisce JSON con i tre criteri che richiedono giudizio
     * (adempimento, coerenza, lessico) piu' l'elenco degli errori. Il quarto
     * criterio (correttezza formale) NON viene chiesto al modello: si calcola
     * dalla densita' di errori, cosi' un modello generoso non puo' gonfiare.
     */
    suspend fun valutaProduzione(
        apiKey: String,
        testoStudente: String,
        livello: String,
        lezione: Int,
        modello: String? = null,
        geminiApiKey: String = ""
    ): String = withContext(Dispatchers.IO) {
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
            - Non inventare errori che non ci sono; non ometterne di evidenti.
        """.trimIndent()

        val messaggi = listOf(
            ChatMessage("system", systemPrompt),
            ChatMessage("user", testoStudente)
        )
        if (geminiApiKey.isNotBlank()) {
            var ultimoErroreG: String? = null
            for (tentativo in 1..2) {
                try {
                    return@withContext chiamaGemini(geminiApiKey, systemPrompt, messaggi)
                } catch (e: IOException) {
                    ultimoErroreG = e.message
                    if (e.message?.contains("non valida") == true) throw e
                    if (tentativo < 2) delay(4000)
                }
            }
            throw IOException(ultimoErroreG ?: "Nessun modello Gemini disponibile al momento")
        }

        val modelli = if (!modello.isNullOrBlank()) listOf(modello)
            else if (apiKey.isBlank()) MODELLI_NO_KEY else MODELLI_FREE

        var ultimoErrore: String? = null
        for (m in modelli) {
            try {
                return@withContext chiamaModello(apiKey, m, messaggi)
            } catch (e: IOException) {
                ultimoErrore = e.message
                if (e.message?.contains("non valida") == true) throw e
            }
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
    }

    suspend fun correggiRisposta(
        apiKey: String,
        testoStudente: String,
        livello: String,
        lezione: Int,
        modello: String? = null,
        geminiApiKey: String = ""
    ): String = withContext(Dispatchers.IO) {
        // Free mode (no API key): uses Gemini endpoint automatically

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

        val messaggi = listOf(
            ChatMessage("system", systemPrompt),
            ChatMessage("user", testoStudente)
        )

        if (geminiApiKey.isNotBlank()) {
            var ultimoErroreG: String? = null
            for (tentativo in 1..2) {
                try {
                    return@withContext chiamaGemini(geminiApiKey, systemPrompt, messaggi)
                } catch (e: IOException) {
                    ultimoErroreG = e.message
                    if (e.message?.contains("non valida") == true) throw e
                    if (tentativo < 2) delay(4000)
                }
            }
            throw IOException(ultimoErroreG ?: "Nessun modello Gemini disponibile al momento")
        }

        val modelli = if (!modello.isNullOrBlank()) listOf(modello)
            else if (apiKey.isBlank()) MODELLI_NO_KEY else MODELLI_FREE

        var ultimoErrore: String? = null
        for (m in modelli) {
            try {
                return@withContext chiamaModello(apiKey, m, messaggi)
            } catch (e: IOException) {
                ultimoErrore = e.message
                if (e.message?.contains("non valida") == true) throw e
            }
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
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
     *
     * @param apiKey chiave Groq
     * @param scenario lo scenario di gioco di ruolo
     * @param cronologia conversazione precedente
     * @param livello livello CEFR dell'utente (A0-B2)
     * @param lezione contesto della lezione corrente
     * @param modello modello opzionale (null = automatico)
     */
    suspend fun roleplay(
        apiKey: String,
        scenario: RoleplayScenario,
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int,
        modello: String? = null,
        geminiApiKey: String = ""
    ): String = withContext(Dispatchers.IO) {
        // Free mode (no API key): uses Gemini endpoint automatically

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
            - Non usare tabelle né HTML.
        """.trimIndent()

        val messaggi = buildList {
            add(ChatMessage("system", systemPrompt))
            cronologia.takeLast(8).forEach { (ruolo, testo) ->
                add(ChatMessage(if (ruolo == "utente") "user" else "assistant", testo))
            }
        }

        if (geminiApiKey.isNotBlank()) {
            var ultimoErroreG: String? = null
            for (tentativo in 1..2) {
                try {
                    return@withContext chiamaGemini(geminiApiKey, systemPrompt, messaggi)
                } catch (e: IOException) {
                    ultimoErroreG = e.message
                    if (e.message?.contains("non valida") == true) throw e
                    if (tentativo < 2) delay(4000)
                }
            }
            throw IOException(ultimoErroreG ?: "Nessun modello Gemini disponibile al momento")
        }

        val modelli = if (!modello.isNullOrBlank()) listOf(modello)
            else if (apiKey.isBlank()) MODELLI_NO_KEY else MODELLI_FREE

        var ultimoErrore: String? = null
        for (m in modelli) {
            try {
                return@withContext chiamaModello(apiKey, m, messaggi)
            } catch (e: IOException) {
                ultimoErrore = e.message
                if (e.message?.contains("non valida") == true) throw e
            }
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
    }

    /** Rimuove il ragionamento/thinking del modello, lasciando solo la risposta finale. */
    private fun pulisciRagionamento(text: String): String {
        var t = text
        t = Regex("(?s)\u003cthink\u003e.*?\u003c/think\u003e").replace(t, "")
        t = Regex("(?s)\u003creasoning\u003e.*?\u003c/reasoning\u003e").replace(t, "")
        t = Regex("(?si)\\[thinking\\].*?\\[/thinking\\]").replace(t, "")
        return t.trim()
    }

    private fun chiamaModello(
        apiKey: String,
        modello: String,
        messaggi: List<ChatMessage>
    ): String {
        val isFreeMode = apiKey.isBlank()
        val url = if (isFreeMode) FREE_ENDPOINT else "https://api.groq.com/openai/v1/chat/completions"
        val effectiveKey = if (apiKey.startsWith("gsk_")) apiKey else FREE_API_KEY

        val body = json.encodeToString(
            ChatRequest.serializer(),
            ChatRequest(model = modello, messages = messaggi, reasoningEffort = if (modello.contains("oss", ignoreCase = true)) reasoningEffort else null)
        )

        val builder = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $effectiveKey")
            .header("Content-Type", "application/json")

        if (!isFreeMode) {
            builder.header("HTTP-Referer", "https://github.com/ItsZale0/tedesco-app")
            builder.header("X-Title", "Deutsch")
        }

        val request = builder.post(body.toRequestBody("application/json".toMediaType())).build()

        client.newCall(request).execute().use { response ->
            val testo = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errore = runCatching {
                    json.parseToJsonElement(testo).toString()
                        .substringAfter("\"message\":\"", "")
                        .substringBefore("\"")
                }.getOrNull()
                throw IOException(
                    when {
                        response.code == 401 -> "Chiave API non valida."
                        response.code == 429 || response.code == 403 -> "Troppe richieste. Riprova tra poco."
                        errore.isNullOrBlank() -> "Errore del server (${response.code})"
                        else -> errore
                    }
                )
            }
            val parsed = json.decodeFromString(ChatResponse.serializer(), testo)
            val contenuto = parsed.choices.firstOrNull()?.message?.content
                ?: throw IOException("Risposta vuota dal tutor")
            return pulisciRagionamento(contenuto)
        }
    }

    private fun chiamaGemini(
        apiKey: String,
        systemPrompt: String,
        messaggi: List<ChatMessage>
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
        val contents = messaggi.filter { it.role != "system" }.map { m ->
            GeminiContent(
                role = if (m.role == "assistant") "model" else "user",
                parts = listOf(GeminiPart(m.content))
            )
        }
        val reqBody = json.encodeToString(
            GeminiRequest.serializer(),
            GeminiRequest(
                contents = contents,
                systemInstruction = GeminiSystemInstruction(listOf(GeminiPart(systemPrompt))),
                generationConfig = GeminiGenerationConfig()
            )
        )
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(reqBody.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val testo = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errore = runCatching {
                    json.parseToJsonElement(testo).toString()
                        .substringAfter("\"message\":\"", "")
                        .substringBefore("\"")
                }.getOrNull()
                throw IOException(
                    when {
                        response.code == 400 || response.code == 401 || response.code == 403 -> "Chiave Gemini non valida."
                        response.code == 429 -> "Troppe richieste. Riprova tra poco."
                        errore.isNullOrBlank() -> "Errore del server Gemini (${response.code})"
                        else -> errore
                    }
                )
            }
            val parsed = json.decodeFromString(GeminiResponse.serializer(), testo)
            val contenuto = parsed.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IOException("Risposta vuota da Gemini")
            return pulisciRagionamento(contenuto)
        }
    }

    suspend fun modelliDisponibili(apiKey: String): List<Pair<String?, String>> = withContext(Dispatchers.IO) {
        val key = if (apiKey.startsWith("gsk_")) apiKey else FREE_API_KEY
        val req = Request.Builder().url("https://api.groq.com/openai/v1/models").header("Authorization", "Bearer $key").get().build()
        val ids = try {
            client.newCall(req).execute().use { r ->
                if (r.isSuccessful) json.decodeFromString<GroqModels>(r.body?.string().orEmpty()).data.map { it.id } else emptyList()
            }
        } catch (e: Exception) { emptyList() }
        val chat = ids.filter { id ->
            val l = id.lowercase()
            !l.contains("whisper") && !l.contains("orpheus") && !l.contains("guard") && !l.contains("safeguard")
        }.sorted()
        if (chat.isEmpty()) listOf(null to "Automatico (consigliato)") + MODELLI_FREE.map { it to it }
        else listOf(null to "Automatico (consigliato)") + chat.map { it to it.substringAfterLast('/').replaceFirstChar { c -> c.uppercase() } }
    }

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

        // Endpoint gratuito senza chiave API (Google Gemini free tier)
        const val FREE_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
        val FREE_API_KEY = BuildConfig.FREE_API_KEY

        /**
         * Modelli gratuiti senza chiave API (Gemini), in ordine di preferenza.
         */
        val MODELLI_NO_KEY = listOf(
            "openai/gpt-oss-120b",
            "qwen/qwen3.8-27b",
            "openai/gpt-oss-20b"
        )

        /**
         * Modelli Groq, in ordine di preferenza.
         * Usati quando l'utente inserisce la propria chiave API.
         * Se uno è in rate limit si passa al successivo.
         */
        val MODELLI_FREE = listOf(
            "openai/gpt-oss-120b",
            "qwen/qwen3.8-27b",
            "openai/gpt-oss-20b"
        )
        const val MODELLO = "qwen/qwen3.8-27b"
    }
}
