package com.alessandro.tedesco.data.remote

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/**
 * Lettura del feed del corso dal repository GitHub.
 *
 * Il feed e' lo stesso file JSON che legge l'app Android: contiene le parole
 * dell'insegnante, le lezioni, la guida e il piano. Supporta ETag, cosi' se
 * nulla e' cambiato il server risponde 304 e non si riscarica tutto.
 *
 * Funzioni bloccanti di proposito: il chiamante decide su quale thread
 * eseguirle (Android: Dispatchers.IO, desktop: Dispatchers.IO).
 */
object FeedGithub {

    data class Risposta(
        val codice: Int,
        val corpo: String? = null,
        val etag: String? = null,
        val errore: String? = null
    ) {
        val ok: Boolean get() = codice in 200..299
        val nonModificato: Boolean get() = codice == 304
    }

    fun scarica(url: String, etag: String = "", timeoutMs: Int = 20_000): Risposta {
        if (url.isBlank()) return Risposta(0, errore = "URL del feed non configurato")
        return try {
            val connessione = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty(
                    "User-Agent",
                    "TedescoApp/1.0 (+https://github.com/ItsZale0/tedesco-app)"
                )
                if (etag.isNotBlank()) setRequestProperty("If-None-Match", etag)
            }
            try {
                val codice = connessione.responseCode
                val nuovoEtag = connessione.getHeaderField("ETag")
                if (codice == 304) {
                    Risposta(304, etag = nuovoEtag ?: etag)
                } else if (codice in 200..299) {
                    val corpo = connessione.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    if (corpo.isBlank()) {
                        Risposta(codice, errore = "Risposta vuota dal server")
                    } else {
                        Risposta(codice, corpo = corpo, etag = nuovoEtag)
                    }
                } else {
                    Risposta(codice, errore = "HTTP $codice")
                }
            } finally {
                connessione.disconnect()
            }
        } catch (e: IOException) {
            Risposta(0, errore = "Nessuna connessione: ${e.message}")
        } catch (e: Exception) {
            Risposta(0, errore = e.message ?: "Errore sconosciuto")
        }
    }

    /** Due URL puntano allo stesso file? Serve a capire se il feed e' cambiato. */
    fun stessoFeed(a: String, b: String): Boolean = runCatching {
        val ua = URI(a)
        val ub = URI(b)
        ua.host.equals(ub.host, ignoreCase = true) && ua.path == ub.path
    }.getOrDefault(false)
}
