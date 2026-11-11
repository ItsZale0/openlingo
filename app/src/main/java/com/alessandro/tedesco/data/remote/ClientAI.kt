package com.alessandro.tedesco.data.remote

import com.alessandro.tedesco.data.local.ConfigurazioneAI
import com.alessandro.tedesco.data.local.ProviderAI
import com.alessandro.tedesco.data.local.TipoApiAI
import com.alessandro.tedesco.di.HttpCondiviso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/** Un messaggio della conversazione, indipendente dal provider. */
data class MessaggioAI(
    val ruolo: String,
    val testo: String
) {
    val eSistema: Boolean get() = ruolo == "system"
    val eUtente: Boolean get() = ruolo == "user" || ruolo == "utente"
}

/**
 * Client HTTP unico per tutti i provider AI supportati.
 *
 * Le differenze fra le API restano qui dentro:
 * - Groq, ChatGPT, OpenRouter e Kilo parlano il formato OpenAI (`/chat/completions`);
 * - Claude usa `/messages` con header `x-api-key`;
 * - Gemini usa `generateContent` con la chiave in query string.
 *
 * Il resto dell'app costruisce solo [MessaggioAI] e riceve testo.
 */
class ClientAI(
    private val client: OkHttpClient = HttpCondiviso.client
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    /**
     * Invia la conversazione al provider configurato e restituisce il testo.
     *
     * Se [ConfigurazioneAI.modello] è vuoto usa il modello predefinito del
     * provider e, in caso di errore, prova i modelli suggeriti in cascata.
     * I rate limit (429) vengono ritentati una volta dopo qualche secondo.
     */
    suspend fun chat(
        config: ConfigurazioneAI,
        system: String,
        cronologia: List<MessaggioAI>,
        maxTokens: Int = 2048,
        temperature: Double = 0.7,
        reasoningEffort: String? = null
    ): String = withContext(Dispatchers.IO) {
        val chiave = config.chiavePulita
        if (chiave.isEmpty()) {
            throw IOException(
                "Nessuna chiave API configurata per ${config.provider.nome}. " +
                    "Apri Impostazioni > Tutor AI e inserisci una chiave."
            )
        }

        val messaggi = cronologia.filter { !it.eSistema && it.testo.isNotBlank() }
        val modelli = modelliDaProvare(config)

        var ultimoErrore: String? = null
        var rateLimited = false
        for (tentativo in 1..2) {
            for (modello in modelli) {
                try {
                    return@withContext esegui(
                        config = config.copy(chiave = chiave, modello = modello),
                        system = system,
                        messaggi = messaggi,
                        maxTokens = maxTokens,
                        temperature = temperature,
                        reasoningEffort = reasoningEffort
                    )
                } catch (e: IOException) {
                    ultimoErrore = e.message
                    // Chiave non valida: provare altri modelli è inutile.
                    if (e.message?.contains("non valida", ignoreCase = true) == true) throw e
                    if (e.message?.contains("Troppe richieste", ignoreCase = true) == true) rateLimited = true
                }
            }
            if (!rateLimited) break
            if (tentativo < 2) delay(4000)
        }
        throw IOException(
            ultimoErrore ?: "Nessun modello disponibile per ${config.provider.nome}."
        )
    }

    private fun modelliDaProvare(config: ConfigurazioneAI): List<String> {
        if (config.modelloScelto.isNotEmpty()) return listOf(config.modelloScelto)
        return (listOf(config.provider.modelloPredefinito) + config.provider.modelliSuggeriti)
            .distinct()
    }

    private fun esegui(
        config: ConfigurazioneAI,
        system: String,
        messaggi: List<MessaggioAI>,
        maxTokens: Int,
        temperature: Double,
        reasoningEffort: String?
    ): String = when (config.provider.tipoApi) {
        TipoApiAI.OPENAI_COMPATIBILE -> chiamaOpenAiCompatibile(
            config, system, messaggi, maxTokens, temperature, reasoningEffort
        )
        TipoApiAI.ANTHROPIC -> chiamaAnthropic(config, system, messaggi, maxTokens, temperature)
        TipoApiAI.GEMINI -> chiamaGemini(config, system, messaggi, maxTokens, temperature)
    }

    // ---- OpenAI compatibile: Groq, ChatGPT, OpenRouter, Kilo ----

    private fun chiamaOpenAiCompatibile(
        config: ConfigurazioneAI,
        system: String,
        messaggi: List<MessaggioAI>,
        maxTokens: Int,
        temperature: Double,
        reasoningEffort: String?
    ): String {
        val modello = config.modello
        val ragionamento = if (modello.contains("oss", ignoreCase = true)) {
            reasoningEffort ?: "low"
        } else null
        val modelloRagionamento = Regex("^(gpt-5|o[1-9])", RegexOption.IGNORE_CASE)
            .containsMatchIn(modello)

        val richiesta = RichiestaOpenAI(
            model = modello,
            messages = buildList {
                add(MessaggioHttp("system", system))
                addAll(messaggi.map { MessaggioHttp(if (it.eUtente) "user" else "assistant", it.testo) })
            },
            maxTokens = if (modelloRagionamento) null else maxTokens,
            maxCompletionTokens = if (modelloRagionamento) maxTokens else null,
            temperature = if (modelloRagionamento) null else temperature,
            reasoningEffort = if (modelloRagionamento) reasoningEffort else ragionamento
        )

        val builder = Request.Builder()
            .url("${config.provider.baseUrl}/chat/completions")
            .header("Authorization", "Bearer ${config.chiave}")
            .header("Content-Type", "application/json")

        // Header richiesti/consigliati da OpenRouter per identificare l'app.
        if (config.provider == ProviderAI.OPENROUTER) {
            builder.header("HTTP-Referer", "https://github.com/ItsZale0/openlingo")
            builder.header("X-Title", "Deutsch")
        }

        val request = builder
            .post(json.encodeToString(RichiestaOpenAI.serializer(), richiesta)
                .toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val testo = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException(messaggioErrore(config.provider, response.code, testo))
            }
            val parsed = runCatching {
                json.decodeFromString(RispostaOpenAI.serializer(), testo)
            }.getOrElse { throw IOException("Risposta non valida da ${config.provider.nome}.") }
            val contenuto = parsed.choices.firstOrNull()?.message?.content
                ?.takeIf { it.isNotBlank() }
                ?: throw IOException("Risposta vuota da ${config.provider.nome}.")
            return pulisciRagionamento(contenuto)
        }
    }

    // ---- Anthropic (Claude) ----

    private fun chiamaAnthropic(
        config: ConfigurazioneAI,
        system: String,
        messaggi: List<MessaggioAI>,
        maxTokens: Int,
        temperature: Double
    ): String {
        val richiesta = RichiestaAnthropic(
            model = config.modello,
            maxTokens = maxTokens,
            temperature = temperature,
            system = system,
            messages = unisciRuoliConsecutivi(messaggi)
        )
        val request = Request.Builder()
            .url("${config.provider.baseUrl}/messages")
            .header("x-api-key", config.chiave)
            .header("anthropic-version", "2023-06-01")
            .header("Content-Type", "application/json")
            .post(json.encodeToString(RichiestaAnthropic.serializer(), richiesta)
                .toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val testo = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException(messaggioErrore(config.provider, response.code, testo))
            }
            val parsed = runCatching {
                json.decodeFromString(RispostaAnthropic.serializer(), testo)
            }.getOrElse { throw IOException("Risposta non valida da ${config.provider.nome}.") }
            val contenuto = parsed.content
                .mapNotNull { it.text }
                .filter { it.isNotBlank() }
                .joinToString("\n")
            if (contenuto.isBlank()) throw IOException("Risposta vuota da ${config.provider.nome}.")
            return pulisciRagionamento(contenuto)
        }
    }

    /**
     * Anthropic richiede ruoli alternati user/assistant: unisce eventuali
     * messaggi consecutivi con lo stesso ruolo (es. storico importato).
     */
    private fun unisciRuoliConsecutivi(messaggi: List<MessaggioAI>): List<MessaggioHttp> {
        val risultato = mutableListOf<MessaggioHttp>()
        for (m in messaggi) {
            val ruolo = if (m.eUtente) "user" else "assistant"
            val ultimo = risultato.lastOrNull()
            if (ultimo != null && ultimo.role == ruolo) {
                risultato[risultato.lastIndex] = ultimo.copy(content = "${ultimo.content}\n${m.testo}")
            } else {
                risultato += MessaggioHttp(ruolo, m.testo)
            }
        }
        // Claude vuole che la conversazione inizi con un messaggio utente.
        if (risultato.firstOrNull()?.role == "assistant") {
            risultato.add(0, MessaggioHttp("user", "Ciao"))
        }
        return risultato
    }

    // ---- Google Gemini ----

    private fun chiamaGemini(
        config: ConfigurazioneAI,
        system: String,
        messaggi: List<MessaggioAI>,
        maxTokens: Int,
        temperature: Double
    ): String {
        val contenuti = messaggi.map { m ->
            ContenutoGemini(
                role = if (m.eUtente) "user" else "model",
                parts = listOf(ParteGemini(m.testo))
            )
        }
        val richiesta = RichiestaGemini(
            contents = contenuti,
            systemInstruction = IstruzioneSistemaGemini(listOf(ParteGemini(system))),
            generationConfig = ConfigurazioneGenerazioneGemini(
                maxOutputTokens = maxTokens,
                temperature = temperature
            )
        )
        val url = "${config.provider.baseUrl}/models/${config.modello}:generateContent?key=${config.chiave}"
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(json.encodeToString(RichiestaGemini.serializer(), richiesta)
                .toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val testo = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException(messaggioErrore(config.provider, response.code, testo))
            }
            val parsed = runCatching {
                json.decodeFromString(RispostaGemini.serializer(), testo)
            }.getOrElse { throw IOException("Risposta non valida da ${config.provider.nome}.") }
            val contenuto = parsed.candidates.firstOrNull()?.content?.parts
                ?.mapNotNull { it.text }
                ?.filter { it.isNotBlank() }
                ?.joinToString("\n")
                .orEmpty()
            if (contenuto.isBlank()) throw IOException("Risposta vuota da ${config.provider.nome}.")
            return pulisciRagionamento(contenuto)
        }
    }

    // ---- Errori e pulizia ----

    private fun messaggioErrore(provider: ProviderAI, code: Int, body: String): String {
        val dettaglio = dettaglioErrore(body)
        return when {
            code == 401 -> "Chiave API non valida per ${provider.nome}."
            code == 403 && dettaglio?.contains("quota", ignoreCase = true) == true ->
                "Troppe richieste. Riprova tra poco."
            code == 403 -> "Accesso negato a ${provider.nome}: controlla i permessi della chiave."
            code == 429 -> "Troppe richieste. Riprova tra poco."
            code == 404 -> "Modello non disponibile per ${provider.nome}."
            code == 400 && provider.tipoApi == TipoApiAI.GEMINI &&
                dettaglio?.contains("API key", ignoreCase = true) == true ->
                "Chiave API non valida per ${provider.nome}."
            code == 400 && !dettaglio.isNullOrBlank() ->
                "Richiesta non valida per ${provider.nome}: $dettaglio"
            code == 400 -> "Richiesta non valida per ${provider.nome}."
            code >= 500 -> "Errore del server ${provider.nome} ($code). Riprova tra poco."
            !dettaglio.isNullOrBlank() -> dettaglio
            else -> "Errore ${provider.nome} ($code)."
        }
    }

    private fun dettaglioErrore(body: String): String? = runCatching {
        val radice = json.parseToJsonElement(body) as? JsonObject ?: return null
        val errore = radice["error"] as? JsonObject
        val messaggio = (errore?.get("message") ?: radice["message"]) as? JsonPrimitive
        messaggio?.contentOrNull?.takeIf { it.isNotBlank() }
    }.getOrNull()

    /** Rimuove il ragionamento/thinking del modello, lasciando solo la risposta finale. */
    private fun pulisciRagionamento(text: String): String {
        var t = text
        t = Regex("(?s)\u003cthink\u003e.*?\u003c/think\u003e").replace(t, "")
        t = Regex("(?s)\u003creasoning\u003e.*?\u003c/reasoning\u003e").replace(t, "")
        t = Regex("(?si)\\[thinking\\].*?\\[/thinking\\]").replace(t, "")
        return t.trim()
    }

    companion object {
        /**
         * Modelli mostrati nei selettori dell'interfaccia.
         * null = "Automatico": il client usa il predefinito e la cascata.
         */
        fun modelliConsigliati(provider: ProviderAI): List<Pair<String?, String>> =
            listOf(null to com.alessandro.tedesco.i18n.t("Automatico (${provider.modelloPredefinito})")) +
                provider.modelliSuggeriti.map { it to it }
    }
}

// ---- Modelli di richiesta/risposta per i tre formati ----

@Serializable
private data class MessaggioHttp(val role: String, val content: String)

@Serializable
private data class RichiestaOpenAI(
    val model: String,
    val messages: List<MessaggioHttp>,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int? = null,
    val temperature: Double? = null,
    @SerialName("reasoning_effort") val reasoningEffort: String? = null
)

@Serializable
private data class SceltaOpenAI(val message: MessaggioHttp? = null)

@Serializable
private data class RispostaOpenAI(val choices: List<SceltaOpenAI> = emptyList())

@Serializable
private data class RichiestaAnthropic(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val temperature: Double,
    val system: String,
    val messages: List<MessaggioHttp>
)

@Serializable
private data class BloccoAnthropic(val type: String? = null, val text: String? = null)

@Serializable
private data class RispostaAnthropic(val content: List<BloccoAnthropic> = emptyList())

@Serializable
private data class ParteGemini(val text: String)

@Serializable
private data class ContenutoGemini(val role: String? = null, val parts: List<ParteGemini>)

@Serializable
private data class IstruzioneSistemaGemini(val parts: List<ParteGemini>)

@Serializable
private data class ConfigurazioneGenerazioneGemini(
    @SerialName("maxOutputTokens") val maxOutputTokens: Int,
    val temperature: Double
)

@Serializable
private data class RichiestaGemini(
    val contents: List<ContenutoGemini>,
    @SerialName("system_instruction") val systemInstruction: IstruzioneSistemaGemini? = null,
    val generationConfig: ConfigurazioneGenerazioneGemini
)

@Serializable
private data class CandidatoGemini(val content: ContenutoGemini? = null)

@Serializable
private data class RispostaGemini(val candidates: List<CandidatoGemini> = emptyList())
