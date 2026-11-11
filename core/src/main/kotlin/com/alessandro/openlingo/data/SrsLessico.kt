package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.ReviewEntity
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Ripetizione spaziata del lessico, estratta dal repository Android per essere
 * usata anche dalla versione desktop. Stessa identica politica dell'app:
 * intervalli 1, 3, 7, 16, 35, 75, 150, 300 giorni; una risposta sbagliata
 * riporta la carta a domani e abbassa leggermente il fattore di facilita'.
 */
object SrsLessico {

    val INTERVALS = listOf(1, 3, 7, 16, 35, 75, 150, 300)

    /** Nuova carta: in scadenza subito, cosi' entra nel primo ripasso. */
    fun nuova(wordId: String, now: Long = System.currentTimeMillis()): ReviewEntity =
        ReviewEntity(wordId = wordId, dueAt = now)

    /**
     * Applica una risposta a una carta SRS.
     *
     * @param saputa true = "lo sapevo" (avanza di un intervallo)
     */
    fun conRisposta(
        review: ReviewEntity,
        saputa: Boolean,
        now: Long = System.currentTimeMillis()
    ): ReviewEntity = if (saputa) {
        val next = INTERVALS.getOrElse(review.repetitions) { INTERVALS.last() }
        review.copy(
            repetitions = review.repetitions + 1,
            intervalDays = next,
            easeFactor = (review.easeFactor + 0.05f).coerceAtMost(3.0f),
            dueAt = now + TimeUnit.DAYS.toMillis(next.toLong()),
            lastReviewedAt = now
        )
    } else {
        review.copy(
            repetitions = 0,
            intervalDays = 1,
            lapses = review.lapses + 1,
            easeFactor = max(1.3f, review.easeFactor - 0.2f),
            dueAt = now + TimeUnit.DAYS.toMillis(1),
            lastReviewedAt = now
        )
    }
}
