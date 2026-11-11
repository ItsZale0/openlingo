package com.alessandro.openlingo.di

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Client HTTP UNICO dell'app.
 *
 * Prima ogni servizio creava il proprio [OkHttpClient]: 7 client, quindi 7 pool di
 * connessioni e 7 thread pool (il dispatcher di OkHttp tiene fino a 5 thread
 * ognuno con uno stack da ~1 MB). Era memoria sprecata in modo permanente.
 *
 * Ora esiste un solo client, con un solo pool e un solo dispatcher. I timeout
 * sono quelli massimi richiesti (le chiamate all'AI arrivano a 90s), cosi' nessun
 * servizio ha bisogno di un client separato.
 */
object HttpCondiviso {

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
}
