package com.alessandro.openlingo.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import com.alessandro.openlingo.di.HttpCondiviso
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Pubblica le parole personalizzate su un canale ntfy.
 *
 * Serve a portare le parole aggiunte nell'app fino al vocabolario
 * su GitHub senza mettere un token nel telefono: un cron su questa
 * macchina legge il canale e committa il JSON aggiornato.
 */
class VocabPublishService {

    private val client = HttpCondiviso.client

    /**
     * Invia una parola al canale. Non lancia eccezioni: se la rete
     * manca, la parola resta comunque salvata in locale.
     */
    suspend fun pubblica(
        topic: String,
        german: String,
        italian: String,
        example: String = "",
        article: String? = null,
        level: String = "A1",
        lesson: Int = 1
    ): Boolean = withContext(Dispatchers.IO) {
        if (topic.isBlank()) return@withContext false
        try {
            val payload = JSONObject().apply {
                put("tipo", "parola")
                put("german", german)
                put("italian", italian)
                put("example", example)
                put("article", article ?: JSONObject.NULL)
                put("level", level)
                put("lesson", lesson)
            }.toString()

            val request = Request.Builder()
                .url("https://ntfy.sh/$topic")
                .post(payload.toRequestBody("text/plain; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            android.util.Log.w("VocabPublish", "Invio fallito: ${e.message}")
            false
        }
    }
}
