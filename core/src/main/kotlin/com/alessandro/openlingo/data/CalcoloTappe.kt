package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.TappaEntity

/**
 * Motore di calcolo dello stato dinamico delle tappe del piano di studio.
 *
 * Lo stato di ogni tappa non è più una stringa statica dal feed, ma viene
 * calcolato in base ai progressi reali dell'utente:
 * - "completata": l'utente ha superato tutte le lezioni della tappa
 * - "in corso": l'utente sta lavorando sulle lezioni della tappa
 * - "da iniziare": l'utente non ha ancora raggiunto le lezioni della tappa
 */
object CalcoloTappe {

    /** Stato calcolato di una tappa. */
    enum class StatoTappa {
        COMPLETATA,
        IN_CORSO,
        DA_INIZIARE
    }

    /** Progresso dell'utente necessario per calcolare gli stati. */
    data class ProgressoTappe(
        val lezioneCorrente: Int,
        val paroleApprese: Int,
        val livelloCorrente: LivelloCEFR
    )

    /**
     * Calcola lo stato dinamico di ogni tappa in base ai progressi dell'utente.
     *
     * @param tappe lista delle tappe dal piano di studio
     * @param progresso progresso corrente dell'utente
     * @return lista delle tappe con stato aggiornato
     */
    fun calcola(tappe: List<TappaEntity>, progresso: ProgressoTappe): List<TappaEntity> {
        return tappe.map { tappa ->
            val stato = when {
                isCompletata(tappa, progresso) -> StatoTappa.COMPLETATA
                isInCorso(tappa, progresso) -> StatoTappa.IN_CORSO
                else -> StatoTappa.DA_INIZIARE
            }
            tappa.copy(stato = stato.name.lowercase().replace("_", " "))
        }
    }

    /**
     * Verifica se una tappa è completata.
     * Una tappa è completata quando la lezione corrente supera la fine del range.
     */
    private fun isCompletata(tappa: TappaEntity, progresso: ProgressoTappe): Boolean {
        val range = parseRange(tappa.lezioni) ?: return false
        return progresso.lezioneCorrente > range.second
    }

    /**
     * Verifica se una tappa è in corso.
     * Una tappa è in corso quando la lezione corrente è nel range.
     */
    private fun isInCorso(tappa: TappaEntity, progresso: ProgressoTappe): Boolean {
        val range = parseRange(tappa.lezioni) ?: return false
        return progresso.lezioneCorrente in range.first..range.second
    }

    /**
     * Parsa il campo lezioni (es. "1-10") in un range di interi.
     * Supporta anche singole lezioni ("5" -> 5,5).
     */
    fun parseRange(lezioni: String): Pair<Int, Int>? {
        val trimmed = lezioni.trim()
        if (trimmed.isEmpty()) return null

        // Supporto per range "1-10"
        if (trimmed.contains("-")) {
            val parts = trimmed.split("-")
            if (parts.size != 2) return null
            val start = parts[0].trim().toIntOrNull() ?: return null
            val end = parts[1].trim().toIntOrNull() ?: return null
            return start to end
        }

        // Supporto per singola lezione "5"
        val single = trimmed.toIntOrNull() ?: return null
        return single to single
    }
}
