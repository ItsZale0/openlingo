package com.alessandro.tedesco.data.remote

import com.alessandro.tedesco.data.BackupCloud
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.Base64

/**
 * Client GitHub per il backup cloud, condiviso fra app Android e desktop.
 *
 * Usa l'API Contents: PUT per caricare/aggiornare, GET per scaricare. Il file
 * e' lo stesso per entrambi (`tedesco-backup.json` per Alessandro, uno per
 * profilo per gli altri), quindi telefono e computer leggono e scrivono gli
 * stessi dati sullo stesso repo privato.
 *
 * Il token e' un fine-grained token con permesso "Contents: Read and write"
 * sul solo repo di backup.
 */
class BackupGithub(
    private val token: String,
    private val repo: String,
    private val nomeFile: String = BackupCloud.NOME_FILE,
    timeoutSecondi: Long = 25
) {
    data class Contenuto(val json: String, val sha: String?)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(timeoutSecondi))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private val timeout: Duration = Duration.ofSeconds(timeoutSecondi)

    private fun base() = "https://api.github.com/repos/$repo/contents/$nomeFile"

    private fun richiesta(url: String): HttpRequest.Builder = HttpRequest.newBuilder()
        .uri(URI(url))
        .timeout(timeout)
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/vnd.github+json")
        .header("X-GitHub-Api-Version", "2022-11-28")
        .header("User-Agent", "DeutschApp-Backup")

    private fun invia(richiesta: HttpRequest): HttpResponse<String> =
        http.send(richiesta, HttpResponse.BodyHandlers.ofString(Charsets.UTF_8))

    /** Scarica il backup; null se non esiste ancora (404). */
    fun scarica(): Contenuto? {
        val risposta = invia(richiesta(base()).GET().build())
        return when (risposta.statusCode()) {
            200 -> {
                val obj = json.parseToJsonElement(risposta.body()).jsonObject
                val b64 = obj["content"]?.jsonPrimitive?.contentOrNull
                    ?: throw IllegalStateException("Campo content mancante nel backup")
                val sha = obj["sha"]?.jsonPrimitive?.contentOrNull
                val testo = String(Base64.getMimeDecoder().decode(b64.replace("\n", "")))
                Contenuto(testo, sha)
            }

            404 -> null
            401 -> throw IllegalStateException("Token non valido o scaduto (401)")
            403 -> throw IllegalStateException("Token senza permesso Contents sul repo (403)")
            else -> throw IllegalStateException("GitHub ha risposto ${risposta.statusCode()}")
        }
    }

    /** sha attuale del file, o null se non esiste. Serve per l'aggiornamento. */
    fun shaCorrente(): String? = runCatching { scarica()?.sha }.getOrNull()

    /**
     * Nomi dei file di backup presenti nella radice del repo. Vuoto se il repo
     * non è leggibile o non ne contiene.
     */
    fun elencaBackup(): List<String> {
        return try {
            val risposta = invia(richiesta("https://api.github.com/repos/$repo/contents/").GET().build())
            if (risposta.statusCode() != 200) return emptyList()
            val elementi = json.parseToJsonElement(risposta.body()).jsonArray
            elementi.mapNotNull { elemento ->
                val obj = elemento.jsonObject
                val nome = obj["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val tipo = obj["type"]?.jsonPrimitive?.contentOrNull ?: "file"
                if (tipo == "file" && BackupCloud.eFileBackup(nome)) nome else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Data dell'ultimo commit che ha toccato [nomeFile], in millisecondi.
     * null se non disponibile. Serve a scegliere il backup più recente quando
     * il file del profilo attivo non esiste.
     */
    fun dataUltimoCommit(nomeFile: String): Long? {
        return try {
            val codificato = URLEncoder.encode(nomeFile, "UTF-8")
            val url = "https://api.github.com/repos/$repo/commits?path=$codificato&per_page=1"
            val risposta = invia(richiesta(url).GET().build())
            if (risposta.statusCode() != 200) return null
            val commit = json.parseToJsonElement(risposta.body()).jsonArray.firstOrNull()?.jsonObject
            val data = commit?.get("commit")?.jsonObject
                ?.get("committer")?.jsonObject
                ?.get("date")?.jsonPrimitive?.contentOrNull
            data?.let { Instant.parse(it).toEpochMilli() }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Carica (o aggiorna) il backup. Legge lo sha corrente: senza, GitHub
     * rifiuta la sovrascrittura con 409.
     */
    fun carica(contenutoJson: String, messaggio: String = "Backup Deutsch"): String {
        val sha = shaCorrente()
        val payload = buildJsonObject {
            put("message", messaggio)
            put("content", Base64.getEncoder().encodeToString(contenutoJson.toByteArray(Charsets.UTF_8)))
            if (sha != null) put("sha", sha)
        }
        val risposta = invia(
            richiesta(base())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(payload.toString(), Charsets.UTF_8))
                .build()
        )
        if (risposta.statusCode() !in 200..299) {
            throw IllegalStateException(erroreLeggibile(risposta.statusCode(), risposta.body().take(300)))
        }
        return risposta.body()
    }

    /** Ricava l'username a cui appartiene il token (per completare il repo). */
    fun ownerDelToken(): String? = runCatching {
        val risposta = invia(richiesta("https://api.github.com/user").GET().build())
        if (risposta.statusCode() !in 200..299) return null
        json.parseToJsonElement(risposta.body()).jsonObject["login"]?.jsonPrimitive?.contentOrNull
    }.getOrNull()

    /** Verifica che token e repo siano validi (pulsante "Prova connessione"). */
    fun verificaAccesso(): BackupCloud.Esito = try {
        val risposta = invia(richiesta("https://api.github.com/repos/$repo").GET().build())
        when (risposta.statusCode()) {
            200 -> BackupCloud.Esito.Ok("Connesso a $repo")
            401 -> BackupCloud.Esito.Errore("Token non valido o scaduto", false)
            403 -> BackupCloud.Esito.Errore("Token senza i permessi necessari", false)
            404 -> BackupCloud.Esito.Errore(
                "Repo \"$repo\" non trovato o non accessibile con questo token",
                false
            )

            else -> BackupCloud.Esito.Errore("GitHub ha risposto ${risposta.statusCode()}", true)
        }
    } catch (e: Exception) {
        BackupCloud.Esito.Errore("Nessuna connessione: ${e.message}", true)
    }

    private fun erroreLeggibile(codice: Int, corpo: String): String = when (codice) {
        401 -> "Token non valido o scaduto (401)."
        403 -> "Token senza permesso Contents sul repo (403)."
        404 -> "Repo \"$repo\" non trovato (404)."
        409 -> "Conflitto: il backup e' stato modificato altrove. Riprova."
        422 -> "Dati rifiutati da GitHub (422)."
        else -> "Errore GitHub $codice: $corpo"
    }
}
