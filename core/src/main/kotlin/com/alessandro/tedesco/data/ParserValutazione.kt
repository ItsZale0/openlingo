package com.alessandro.tedesco.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Estrae la valutazione strutturata dalla risposta JSON del tutor.
 *
 * Il modello puo' sbagliare formato: il parser non deve mai far crashare l'app
 * ne' gonfiare i voti. Se un criterio manca, resta 0 (mai un default alto).
 */
object ParserValutazione {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    data class Risultato(
        val erfuellung: Float?,
        val kohaerenz: Float?,
        val wortschatz: Float?,
        val numErrori: Int,
        val correzione: String
    )

    /** Estrae i campi dal JSON; tollera ```json, testo attorno e campi mancanti. */
    fun parse(risposta: String): Risultato {
        val pulito = estraiJson(risposta)
        val obj = runCatching { json.parseToJsonElement(pulito).jsonObject }.getOrNull()
            ?: return Risultato(null, null, null, 0, risposta.trim())

        fun punteggio(chiave: String): Float? =
            obj[chiave]?.jsonPrimitive?.floatOrNull?.coerceIn(0f, 100f)

        // Conta gli errori DEDUPLICATI: il modello tende a elencare lo stesso
        // errore due volte (parola isolata + frase che la contiene).
        val errori = errori(risposta).size

        val correzione = obj["correzione"]?.jsonPrimitive?.contentOrNull
            ?: obj["commento"]?.jsonPrimitive?.contentOrNull
            ?: ""

        return Risultato(
            erfuellung = punteggio("erfuellung") ?: punteggio("erfüllung"),
            kohaerenz = punteggio("kohaerenz") ?: punteggio("kohärenz"),
            wortschatz = punteggio("wortschatz"),
            numErrori = errori,
            correzione = correzione.trim()
        )
    }

    /** Un errore strutturato, gia' deduplicato. */
    data class Errore(val estratto: String, val correzione: String, val spiegazione: String)

    /** Elenco leggibile degli errori (per mostrarli all'utente). */
    fun elencaErrori(risposta: String): List<String> =
        errori(risposta).map { e ->
            buildString {
                append("**").append(e.estratto).append("** → **").append(e.correzione).append("**")
                if (e.spiegazione.isNotBlank()) append("\n").append(e.spiegazione)
            }
        }

    /** Estrae gli errori deduplicati. */
    fun errori(risposta: String): List<Errore> {
        val pulito = estraiJson(risposta)
        val arr = runCatching {
            json.parseToJsonElement(pulito).jsonObject["errori"]?.jsonArray
        }.getOrNull() ?: return emptyList()
        val grezzi = arr.mapNotNull { e ->
            val o = runCatching { e.jsonObject }.getOrNull() ?: return@mapNotNull null
            val estratto = o["estratto"]?.jsonPrimitive?.contentOrNull?.trim()
                ?: return@mapNotNull null
            Errore(
                estratto = estratto,
                correzione = o["correzione"]?.jsonPrimitive?.contentOrNull?.trim() ?: "",
                spiegazione = o["spiegazione"]?.jsonPrimitive?.contentOrNull?.trim() ?: ""
            )
        }
        return deduplica(grezzi)
    }

    /**
     * Rimuove i DOPPI CONTEGGI: il modello spesso elenca lo stesso errore due
     * volte, una come parola isolata e una dentro la frase che la contiene
     * (es. "Deutch" e "ich lerne Deutch."). Contare entrambi gonfia la densita'
     * di errori e penalizza il punteggio in modo ingiusto.
     *
     * Regola: se l'estratto di un errore e' contenuto in quello di un altro,
     * si tiene il piu' ampio (copre entrambi) e si scarta il piu' corto.
     */
    fun deduplica(errori: List<Errore>): List<Errore> {
        if (errori.size <= 1) return errori
        val ordinati = errori.sortedByDescending { it.estratto.length }
        val tenuti = mutableListOf<Errore>()
        for (e in ordinati) {
            val contenuto = tenuti.any { piu ->
                piu.estratto.contains(e.estratto, ignoreCase = true) &&
                    !e.estratto.equals(piu.estratto, ignoreCase = true)
            }
            if (!contenuto) tenuti.add(e)
        }
        // Rimuovi anche i duplicati esatti (stesso estratto normalizzato)
        return tenuti.distinctBy { it.estratto.lowercase().replace(Regex("[\\s.,!?;:]+"), "") }
    }

    /** Isola il blocco JSON anche se il modello aggiunge testo o ```json. */
    private fun estraiJson(risposta: String): String {
        val senzaFence = risposta.replace("```json", "").replace("```", "").trim()
        val inizio = senzaFence.indexOf('{')
        val fine = senzaFence.lastIndexOf('}')
        return if (inizio >= 0 && fine > inizio) senzaFence.substring(inizio, fine + 1)
        else senzaFence
    }
}
