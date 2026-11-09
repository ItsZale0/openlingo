package com.alessandro.tedesco.data.remote

import com.alessandro.tedesco.data.BackupCloud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import com.alessandro.tedesco.di.HttpCondiviso
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder
import java.time.Instant
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Client GitHub per il backup cloud (repo privato).
 *
 * Usa l'API Contents: PUT per caricare/aggiornare, GET per scaricare.
 * Il token dell'utente e' un fine-grained token con permesso "Contents: Read and
 * write" sul solo repo di backup.
 */
class BackupGitHubClient(
    private val token: String,
    private val repo: String,
    private val nomeFile: String = BackupCloud.NOME_FILE
) {
    private val client = HttpCondiviso.client

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val mediaJson = "application/json".toMediaType()

    private fun base() = "https://api.github.com/repos/$repo/contents/$nomeFile"

    private fun req(url: String) = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/vnd.github+json")
        .header("User-Agent", "DeutschApp-Backup")

    /** Scarica il backup; null se non esiste ancora (404). */
    suspend fun scarica(): String? = withContext(Dispatchers.IO) {
        val r = req(base()).get().build()
        client.newCall(r).execute().use { resp ->
            when (resp.code) {
                200 -> {
                    val body = resp.body?.string() ?: throw IOException("Risposta vuota da GitHub")
                    val obj = json.parseToJsonElement(body).jsonObject
                    val b64 = obj["content"]?.jsonPrimitive?.contentOrNull
                        ?: throw IOException("Campo content mancante nel backup")
                    // GitHub spezza il base64 su piu' righe
                    String(Base64.getMimeDecoder().decode(b64.replace("\n", "")))
                }
                404 -> null
                401 -> throw IOException("Token non valido o scaduto (401)")
                403 -> throw IOException("Token senza permesso Contents sul repo (403)")
                else -> throw IOException("GitHub ha risposto ${resp.code}")
            }
        }
    }

    /** sha attuale del file, o null se non esiste. Serve per l'aggiornamento. */
    suspend fun shaCorrente(): String? = withContext(Dispatchers.IO) {
        val r = req(base()).get().build()
        client.newCall(r).execute().use { resp ->
            when (resp.code) {
                200 -> {
                    val body = resp.body?.string() ?: return@use null
                    json.parseToJsonElement(body).jsonObject["sha"]?.jsonPrimitive?.contentOrNull
                }
                404 -> null
                else -> null
            }
        }
    }

    /**
     * Nomi dei file di backup presenti nella radice del repo. Vuoto se il repo
     * non è leggibile o non ne contiene.
     */
    suspend fun elencaBackup(): List<String> = withContext(Dispatchers.IO) {
        val r = req("https://api.github.com/repos/$repo/contents/").get().build()
        client.newCall(r).execute().use { resp ->
            if (resp.code != 200) return@use emptyList()
            val body = resp.body?.string() ?: return@use emptyList()
            val elementi = runCatching { json.parseToJsonElement(body).jsonArray }.getOrNull()
                ?: return@use emptyList()
            elementi.mapNotNull { elemento ->
                val obj = elemento.jsonObject
                val nome = obj["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val tipo = obj["type"]?.jsonPrimitive?.contentOrNull ?: "file"
                if (tipo == "file" && BackupCloud.eFileBackup(nome)) nome else null
            }
        }
    }

    /**
     * Data dell'ultimo commit che ha toccato [nomeFile], in millisecondi.
     * null se non disponibile. Serve a scegliere il backup più recente quando
     * il file del profilo attivo non esiste.
     */
    suspend fun dataUltimoCommit(nomeFile: String): Long? = withContext(Dispatchers.IO) {
        try {
            val codificato = URLEncoder.encode(nomeFile, "UTF-8")
            val url = "https://api.github.com/repos/$repo/commits?path=$codificato&per_page=1"
            val r = req(url).get().build()
            client.newCall(r).execute().use { resp ->
                if (resp.code != 200) return@use null
                val body = resp.body?.string() ?: return@use null
                val commit = runCatching {
                    json.parseToJsonElement(body).jsonArray.firstOrNull()?.jsonObject
                }.getOrNull() ?: return@use null
                val data = commit["commit"]?.jsonObject
                    ?.get("committer")?.jsonObject
                    ?.get("date")?.jsonPrimitive?.contentOrNull
                data?.let { Instant.parse(it).toEpochMilli() }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Carica (o aggiorna) il backup. Legge lo sha corrente: senza, GitHub rifiuta
     * la sovrascrittura con 409.
     */
    suspend fun carica(contenutoJson: String, messaggio: String = "Backup Deutsch"): String =
        withContext(Dispatchers.IO) {
            val sha = shaCorrente()
            val payload = buildJsonObject {
                put("message", messaggio)
                put("content", Base64.getEncoder().encodeToString(contenutoJson.toByteArray()))
                if (sha != null) put("sha", sha)
            }
            val r = req(base()).put(payload.toString().toRequestBody(mediaJson)).build()
            client.newCall(r).execute().use { resp ->
                if (resp.code !in 200..299) {
                    val err = resp.body?.string()?.take(300) ?: ""
                    throw IOException(erroreLeggibile(resp.code, err))
                }
                resp.body?.string() ?: ""
            }
        }

    /** Ricava l'username a cui appartiene il token (per completare il repo). */
    suspend fun ownerDelToken(): String? = withContext(Dispatchers.IO) {
        val r = Request.Builder()
            .url("https://api.github.com/user")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "DeutschApp-Backup")
            .get().build()
        try {
            client.newCall(r).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val body = resp.body?.string() ?: return@use null
                json.parseToJsonElement(body).jsonObject["login"]?.jsonPrimitive?.contentOrNull
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Verifica che token e repo siano validi (per il pulsante "Prova connessione"). */
    suspend fun verificaAccesso(): BackupCloud.Esito = withContext(Dispatchers.IO) {
        try {
            val r = Request.Builder()
                .url("https://api.github.com/repos/$repo")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "DeutschApp-Backup")
                .get().build()
            client.newCall(r).execute().use { resp ->
                when (resp.code) {
                    200 -> BackupCloud.Esito.Ok("Connesso a $repo")
                    401 -> BackupCloud.Esito.Errore("Token non valido o scaduto", false)
                    403 -> BackupCloud.Esito.Errore("Token senza i permessi necessari", false)
                    404 -> BackupCloud.Esito.Errore("Repo \"$repo\" non trovato o non accessibile con questo token", false)
                    else -> BackupCloud.Esito.Errore("GitHub ha risposto ${resp.code}", true)
                }
            }
        } catch (e: Exception) {
            BackupCloud.Esito.Errore("Nessuna connessione: ${e.message}", true)
        }
    }

    private fun erroreLeggibile(codice: Int, corpo: String): String = when (codice) {
        401 -> "Token non valido o scaduto (401)."
        403 -> "Token senza permesso Contents sul repo (403)."
        404 -> "Repo \"$repo\" non trovato (404)."
        409 -> "Conflitto: il backup e' stato modificato altrove. Riprova."
        422 -> "Dati rifiutati da GitHub (422)."
        else -> "Errore GitHub ${codice}: $corpo"
    }
}
