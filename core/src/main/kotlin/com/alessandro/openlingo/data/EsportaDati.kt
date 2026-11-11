package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.ProfiliRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Esportazione COMPLETA dei dati dell'utente in un unico file JSON.
 *
 * Contiene:
 * - tutti i profili (parole, ripassi, risposte, grammatica, lezioni, guide,
 *   conversazioni, esercizi generati, badge, streak…);
 * - le preferenze delle notifiche;
 * - il colore del tema.
 *
 * Non contiene il token GitHub del backup: è una credenziale e va reinserita
 * a mano sul nuovo dispositivo. Le chiavi API dei profili, invece, fanno parte
 * della configurazione del profilo e sono incluse: il file va conservato in
 * un luogo sicuro.
 *
 * Il formato è lo stesso usato dal backup cloud, quindi il file può essere
 * reimportato con il normale ripristino.
 */
object EsportaDati {

    const val VERSIONE_FORMATO = 1

    @Serializable
    data class Pacchetto(
        val formato: Int = VERSIONE_FORMATO,
        val creatoIl: Long,
        val versioneApp: String = "",
        val profili: ProfiliRepository,
        val notifiche: BackupCloud.NotificheBackup? = null,
        val temaColore: String = ""
    )

    /** Riepilogo leggibile di un file esportato. */
    data class Riepilogo(
        val profili: Int,
        val parole: Int,
        val ripassi: Int,
        val risposte: Int
    )

    fun serializza(
        repository: ProfiliRepository,
        versioneApp: String,
        now: Long,
        notifiche: BackupCloud.NotificheBackup? = null,
        temaColore: String = ""
    ): String {
        val pacchetto = Pacchetto(
            formato = VERSIONE_FORMATO,
            creatoIl = now,
            versioneApp = versioneApp,
            profili = repository,
            notifiche = notifiche,
            temaColore = temaColore
        )
        return json.encodeToString(Pacchetto.serializer(), pacchetto)
    }

    /** True se il testo è un'esportazione completa leggibile da questa versione. */
    fun valida(jsonTesto: String): Boolean = runCatching {
        val p = json.decodeFromString(Pacchetto.serializer(), jsonTesto)
        p.formato <= VERSIONE_FORMATO
    }.getOrDefault(false)

    fun riepilogo(jsonTesto: String): Riepilogo? = runCatching {
        val p = json.decodeFromString(Pacchetto.serializer(), jsonTesto)
        Riepilogo(
            profili = p.profili.profili.size,
            parole = p.profili.profili.values.sumOf { it.stato.parole.size },
            ripassi = p.profili.profili.values.sumOf { it.stato.ripassiLog.size },
            risposte = p.profili.profili.values.sumOf { it.stato.risposte.size }
        )
    }.getOrNull()

    /**
     * Converte un'esportazione completa nel formato del backup cloud, così può
     * essere ripristinata con l'unione dei progressi già esistente.
     */
    fun comeBackup(jsonTesto: String): String? = runCatching {
        val p = json.decodeFromString(Pacchetto.serializer(), jsonTesto)
        BackupCloud.serializza(p.profili, p.versioneApp, p.creatoIl, p.notifiche)
    }.getOrNull()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
}
