package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ProfiliRepository
import kotlinx.serialization.Serializable

/**
 * BACKUP CLOUD su GitHub (repo privato).
 *
 * I dati dell'app vivono in un unico [ProfiliRepository] (tutti i profili, con
 * parole, ripassi, log, test, progressi). Il backup e' quindi una sua fotografia
 * JSON, versionata, caricata su un repo privato dell'utente.
 *
 * Perche' GitHub e non Drive: nessun progetto Google Cloud da configurare, nessun
 * SHA-1 di firma, l'utente incolla un token una volta sola come gia' fa per la
 * chiave del tutor. Funziona offline-first: il backup e' un'operazione esplicita.
 */
object BackupCloud {

    /** Versione del formato: serve a rifiutare backup di versioni future. */
    const val VERSIONE_FORMATO = 2

    /** Nome file usato nel repo di backup. */
    const val NOME_FILE = "tedesco-backup.json"

    /**
     * Nome file per profilo: il backup è separato per utente, così due telefoni
     * (Alessandro ed Emma) non si sovrascrivono sullo stesso repo.
     *
     * Alessandro resta sul nome storico (retrocompatibilità con i backup già
     * esistenti); ogni altro profilo (es. Emma) ha il proprio file.
     * Nessun setup richiesto: il nome deriva dal profilo attivo.
     */
    fun nomeFilePerProfilo(profiloId: String?): String {
        val id = profiloId?.trim().orEmpty()
        if (id.isBlank() || id == "alessandro") return NOME_FILE
        return "tedesco-backup-$id.json"
    }

    /**
     * Repo di backup di default (privato). DEVE includere l'owner:
     * l'API e' /repos/{owner}/{repo}, quindi "tedesco-backup" da solo darebbe 404.
     */
    const val REPO_DEFAULT = "ItsZale0/tedesco-backup"

    @Serializable
    data class Pacchetto(
        val versione: Int = VERSIONE_FORMATO,
        val creatoIl: Long,
        val versioneApp: String = "",
        /** I dati veri: tutti i profili. */
        val dati: ProfiliRepository,
        /**
         * Preferenze delle notifiche (promemoria ripasso e ascolto serale).
         * Opzionale per retrocompatibilita' con i backup di formato 1.
         */
        val notifiche: NotificheBackup? = null
    )

    /** Esito di un'operazione di backup, per messaggi onesti all'utente. */
    sealed interface Esito {
        data class Ok(val dettaglio: String) : Esito
        data class Errore(val messaggio: String, val ritentabile: Boolean = false) : Esito
    }

    /** Preferenze delle notifiche, serializzabili nel backup. */
    @Serializable
    data class NotificheBackup(
        val ripassoAttivo: Boolean = true,
        val ripassoOra: Int = 20,
        val immersioneSeraleAttiva: Boolean = false,
        val immersioneSeraleOra: Int = 21
    )

    /** Serializza il pacchetto di backup. */
    fun serializza(
        repo: ProfiliRepository,
        versioneApp: String,
        now: Long,
        notifiche: NotificheBackup? = null
    ): String {
        val pacchetto = Pacchetto(
            versione = VERSIONE_FORMATO,
            creatoIl = now,
            versioneApp = versioneApp,
            dati = repo,
            notifiche = notifiche
        )
        return jsonBackup.encodeToString(Pacchetto.serializer(), pacchetto)
    }

    /**
     * Deserializza un backup. Lancia [BackupInvalido] se il formato non e'
     * leggibile o se la versione e' piu' nuova di quella supportata (mai
     * sovrascrivere dati con un formato che non capiamo).
     */
    fun deserializza(json: String): Pacchetto {
        val p = try {
            jsonBackup.decodeFromString(Pacchetto.serializer(), json)
        } catch (e: Exception) {
            throw BackupInvalido("Il file di backup non e' leggibile: ${e.message}")
        }
        if (p.versione > VERSIONE_FORMATO) {
            throw BackupInvalido(
                "Backup creato da una versione piu' recente dell'app (formato ${p.versione}). " +
                    "Aggiorna l'app prima di ripristinare."
            )
        }
        return p
    }

    /** Riepilogo leggibile del contenuto, per far vedere cosa si sta ripristinando. */
    fun riepilogo(p: Pacchetto): Riepilogo {
        val profili = p.dati.profili.values
        return Riepilogo(
            numProfili = profili.size,
            numParole = profili.sumOf { it.stato.parole.size },
            numRipassi = profili.sumOf { it.stato.ripassiLog.size },
            numRisposte = profili.sumOf { it.stato.risposte.size },
            creatoIl = p.creatoIl,
            versioneApp = p.versioneApp
        )
    }

    data class Riepilogo(
        val numProfili: Int,
        val numParole: Int,
        val numRipassi: Int,
        val numRisposte: Int,
        val creatoIl: Long,
        val versioneApp: String
    )

    class BackupInvalido(message: String) : Exception(message)

    /**
     * Json dedicato al backup: NON ignora le chiavi sconosciute (un backup non
     * deve perdere silenziosamente dei campi) e non e' lenient.
     */
    private val jsonBackup = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true   // compatibilita' con campi aggiunti in futuro
        encodeDefaults = true      // salva anche i default: backup completo
        prettyPrint = false
    }
}
