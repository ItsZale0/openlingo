package com.alessandro.openlingo.data.remote

import com.alessandro.openlingo.data.local.ConfigurazioneAI
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Facade per le operazioni AI dell'app.
 *
 * Centralizza le chiamate a [TutorService] e [GeneratoreEsercizi] così il
 * ViewModel non conosce i dettagli dei provider. È il primo passo per
 * estrarre la logica AI da [com.alessandro.openlingo.ui.OpenLingoViewModel].
 */
class AiService(
    private val tutor: TutorService = TutorService(),
    private val generatoreEsercizi: GeneratoreEsercizi = GeneratoreEsercizi(),
    private val generatoreLezioni: GeneratoreLezioni = GeneratoreLezioni()
) {
    /**
     * Chiamata AI generica con system prompt arbitrario: usata per estrarre
     * dati strutturati (es. parola da aggiungere al vocabolario) senza passare
     * dal prompt didattico del tutor.
     */
    suspend fun chatRaw(
        config: ConfigurazioneAI,
        system: String,
        testoUtente: String,
        maxTokens: Int = 512,
        temperature: Double = 0.1
    ): String = tutor.chatRaw(config, system, testoUtente, maxTokens, temperature)

    suspend fun chiediAlTutor(
        config: ConfigurazioneAI,
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int,
        contestoUtente: String = ""
    ): String = tutor.rispondi(config, cronologia, livello, lezione, contestoUtente)

    suspend fun chiediAlRoleplay(
        config: ConfigurazioneAI,
        scenario: TutorService.RoleplayScenario,
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int,
        contestoUtente: String = ""
    ): String = tutor.roleplay(config, scenario, cronologia, livello, lezione, contestoUtente)

    suspend fun valutaProduzione(
        config: ConfigurazioneAI,
        testoStudente: String,
        livello: String,
        lezione: Int,
        contestoUtente: String = ""
    ): String = tutor.valutaProduzione(config, testoStudente, livello, lezione, contestoUtente)

    suspend fun generaEsercizi(
        config: ConfigurazioneAI,
        tipo: TipoEsercizio,
        livello: String,
        quanti: Int = 5,
        contestoMedico: Boolean = false,
        contestoUtente: String = "",
        evita: List<String> = emptyList(),
        tema: String? = null
    ): List<EsercizioGenerato> =
        generatoreEsercizi.genera(config, tipo, livello, quanti, contestoMedico, contestoUtente, evita, tema)

    suspend fun generaLezione(
        config: ConfigurazioneAI,
        numero: Int,
        livello: String,
        argomento: String,
        paroleNote: List<String> = emptyList(),
        contesto: String = "",
        contestoMedico: Boolean = false
    ): LezioneGenerata =
        generatoreLezioni.genera(config, numero, livello, argomento, paroleNote, contesto, contestoMedico)
}
