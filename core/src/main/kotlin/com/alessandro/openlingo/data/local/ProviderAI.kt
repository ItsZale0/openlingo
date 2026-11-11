package com.alessandro.openlingo.data.local

import kotlinx.serialization.Serializable

/**
 * Famiglia di API usata dal provider.
 *
 * - [OPENAI_COMPATIBILE]: endpoint `/chat/completions` in stile OpenAI
 *   (Groq, ChatGPT, OpenRouter, Kilo).
 * - [ANTHROPIC]: endpoint `/messages` di Claude.
 * - [GEMINI]: endpoint `generateContent` di Google.
 */
enum class TipoApiAI {
    OPENAI_COMPATIBILE,
    ANTHROPIC,
    GEMINI
}

/**
 * Provider AI supportati dall'app.
 *
 * Ogni provider porta con sé le informazioni che servono all'interfaccia
 * (nome, esempio di chiave, link per crearla) e al client HTTP (base URL,
 * modello predefinito, modelli suggeriti). L'utente può comunque scrivere
 * un modello diverso da quelli suggeriti.
 */
@Serializable
enum class ProviderAI(
    val id: String,
    val nome: String,
    val tipoApi: TipoApiAI,
    val baseUrl: String,
    val chiaveSuggerita: String,
    val modelloPredefinito: String,
    val modelliSuggeriti: List<String>,
    val urlChiave: String
) {
    GROQ(
        id = "groq",
        nome = "Groq",
        tipoApi = TipoApiAI.OPENAI_COMPATIBILE,
        baseUrl = "https://api.groq.com/openai/v1",
        chiaveSuggerita = "gsk_...",
        modelloPredefinito = "openai/gpt-oss-120b",
        modelliSuggeriti = listOf(
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b",
            "qwen/qwen3-32b"
        ),
        urlChiave = "https://console.groq.com/keys"
    ),

    OPENAI(
        id = "openai",
        nome = "ChatGPT",
        tipoApi = TipoApiAI.OPENAI_COMPATIBILE,
        baseUrl = "https://api.openai.com/v1",
        chiaveSuggerita = "sk-...",
        modelloPredefinito = "gpt-4.1-mini",
        modelliSuggeriti = listOf(
            "gpt-4.1-mini",
            "gpt-4.1",
            "gpt-4o-mini",
            "gpt-5-mini"
        ),
        urlChiave = "https://platform.openai.com/api-keys"
    ),

    ANTHROPIC(
        id = "anthropic",
        nome = "Claude",
        tipoApi = TipoApiAI.ANTHROPIC,
        baseUrl = "https://api.anthropic.com/v1",
        chiaveSuggerita = "sk-ant-...",
        modelloPredefinito = "claude-sonnet-4-5",
        modelliSuggeriti = listOf(
            "claude-sonnet-4-5",
            "claude-haiku-4-5",
            "claude-opus-4-1"
        ),
        urlChiave = "https://console.anthropic.com/settings/keys"
    ),

    GEMINI(
        id = "gemini",
        nome = "Gemini",
        tipoApi = TipoApiAI.GEMINI,
        baseUrl = "https://generativelanguage.googleapis.com/v1beta",
        chiaveSuggerita = "AIza...",
        modelloPredefinito = "gemini-2.5-flash",
        modelliSuggeriti = listOf(
            "gemini-2.5-flash",
            "gemini-2.5-pro",
            "gemini-2.0-flash"
        ),
        urlChiave = "https://aistudio.google.com/app/apikey"
    ),

    OPENROUTER(
        id = "openrouter",
        nome = "OpenRouter",
        tipoApi = TipoApiAI.OPENAI_COMPATIBILE,
        baseUrl = "https://openrouter.ai/api/v1",
        chiaveSuggerita = "sk-or-...",
        modelloPredefinito = "openai/gpt-4.1-mini",
        modelliSuggeriti = listOf(
            "openai/gpt-4.1-mini",
            "anthropic/claude-sonnet-4.5",
            "google/gemini-2.5-flash"
        ),
        urlChiave = "https://openrouter.ai/keys"
    ),

    KILO(
        id = "kilo",
        nome = "Kilo",
        tipoApi = TipoApiAI.OPENAI_COMPATIBILE,
        baseUrl = "https://ai-gateway.kilo.ai/api/v1",
        chiaveSuggerita = "kilo-...",
        modelloPredefinito = "anthropic/claude-sonnet-4.5",
        modelliSuggeriti = listOf(
            "anthropic/claude-sonnet-4.5",
            "openai/gpt-4.1-mini",
            "google/gemini-2.5-flash"
        ),
        urlChiave = "https://app.kilo.ai"
    );

    companion object {
        /** Provider usato quando il profilo non ne ha mai scelto uno. */
        val PREDEFINITO: ProviderAI = GROQ

        /** Ritrova un provider dall'id salvato, ignorando maiuscole e spazi. */
        fun daId(id: String?): ProviderAI? =
            entries.firstOrNull { it.id.equals(id?.trim(), ignoreCase = true) }
    }
}

/**
 * Configurazione AI pronta per una chiamata: provider, chiave e modello.
 *
 * Un [modello] vuoto significa "usa il modello predefinito del provider",
 * con cascata sui modelli suggeriti in caso di errore.
 */
data class ConfigurazioneAI(
    val provider: ProviderAI,
    val chiave: String = "",
    val modello: String = ""
) {
    val chiavePulita: String get() = chiave.trim()
    val modelloScelto: String get() = modello.trim()
    val haChiave: Boolean get() = chiavePulita.isNotEmpty()
}

/**
 * Provider effettivo del profilo.
 *
 * I profili creati prima del supporto multi-provider non hanno [ProfiloConfig.providerAI]:
 * in quel caso lo deduciamo dalle vecchie chiavi (`tutorApiKey` per Groq,
 * `geminiApiKey` per Gemini), così i dati esistenti continuano a funzionare
 * senza migrazioni distruttive.
 */
fun ProfiloConfig.providerEffettivo(): ProviderAI {
    providerAI?.let { return it }

    // Una chiave salvata nella nuova mappa è già una scelta esplicita.
    val daMappa = chiaviAI.keys
        .asSequence()
        .mapNotNull(ProviderAI::daId)
        .firstOrNull()
    if (daMappa != null) return daMappa

    return when {
        tutorApiKey.trim().startsWith("gsk_") -> ProviderAI.GROQ
        geminiApiKey.isNotBlank() -> ProviderAI.GEMINI
        tutorApiKey.isNotBlank() -> ProviderAI.GROQ
        else -> ProviderAI.PREDEFINITO
    }
}

/**
 * Chiave del provider richiesto.
 *
 * La mappa [ProfiloConfig.chiaviAI] è la fonte principale; le vecchie chiavi
 * separate restano come fallback per i profili già salvati.
 */
fun ProfiloConfig.chiaveProvider(provider: ProviderAI = providerEffettivo()): String {
    val dallaMappa = chiaviAI[provider.id]?.trim().orEmpty()
    if (dallaMappa.isNotEmpty()) return dallaMappa

    return when (provider) {
        ProviderAI.GROQ -> tutorApiKey.trim()
        ProviderAI.GEMINI -> geminiApiKey.trim()
        else -> ""
    }
}

/**
 * Modello scelto esplicitamente dall'utente per il provider, oppure vuoto.
 *
 * Se il modello salvato appartiene palesemente a un altro provider (caso
 * frequente cambiando provider), meglio non usarlo: si torna al predefinito.
 */
fun ProfiloConfig.modelloEsplicito(provider: ProviderAI = providerEffettivo()): String {
    val scelto = modelloAI.trim()
    if (scelto.isEmpty()) return ""
    if (provider.modelliSuggeriti.any { it.equals(scelto, ignoreCase = true) }) return scelto

    val sembraDiAltroProvider = ProviderAI.entries.any { altro ->
        altro != provider && altro.modelliSuggeriti.any { it.equals(scelto, ignoreCase = true) }
    }
    return if (sembraDiAltroProvider) "" else scelto
}

/** Modello effettivo mostrato all'utente (predefinito se non ne ha scelto uno). */
fun ProfiloConfig.modelloProvider(provider: ProviderAI = providerEffettivo()): String =
    modelloEsplicito(provider).ifBlank { provider.modelloPredefinito }

/** Configurazione pronta per [ConfigurazioneAI] del profilo. */
fun ProfiloConfig.configurazioneAI(): ConfigurazioneAI {
    val provider = providerEffettivo()
    return ConfigurazioneAI(
        provider = provider,
        chiave = chiaveProvider(provider),
        // Vuoto = il client usa il predefinito e, se serve, la cascata.
        modello = modelloEsplicito(provider)
    )
}

/** Provider per cui il profilo ha una chiave salvata (non vuota). */
fun ProfiloConfig.providerConfigurati(): List<ProviderAI> =
    ProviderAI.entries.filter { chiaveProvider(it).isNotBlank() }
