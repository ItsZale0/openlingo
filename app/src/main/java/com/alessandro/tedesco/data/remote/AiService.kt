package com.alessandro.tedesco.data.remote

import com.alessandro.tedesco.data.local.ConfigurazioneAI

/**
 * Facade per le operazioni AI dell'app.
 *
 * Centralizza le chiamate a [TutorService] e [GeneratoreEsercizi] così il
 * ViewModel non conosce i dettagli dei provider. È il primo passo per
 * estrarre la logica AI da [com.alessandro.tedesco.ui.TedescoViewModel].
 */
class AiService(
    private val tutor: TutorService = TutorService(),
    private val generatoreEsercizi: GeneratoreEsercizi = GeneratoreEsercizi(),
    private val generatoreLezioni: GeneratoreLezioni = GeneratoreLezioni()
) {
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
}
