package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.WordEntity
import com.alessandro.openlingo.data.remote.FeedDto
import com.alessandro.openlingo.data.remote.toEntity

/**
 * Fusione del feed del corso con il lessico locale.
 *
 * E' la stessa logica usata dall'app Android e dalla versione desktop: il
 * vocabolario che arriva dal repository GitHub (feed dell'insegnante) entra
 * con gli stessi criteri su tutti i dispositivi, senza duplicati e senza
 * perdere le parole personalizzate.
 */
object FeedMerge {

    data class Esito(
        val parole: List<WordEntity>,
        val reviews: Map<String, ReviewEntity>,
        val aggiunte: Int,
        val aggiornate: Int
    )

    fun applica(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        feed: FeedDto,
        now: Long = System.currentTimeMillis()
    ): Esito {
        val attuali = parole.associateBy { it.id }.toMutableMap()
        val nuove = mutableSetOf<String>()
        var aggiunte = 0
        var aggiornate = 0

        for (dto in feed.words) {
            val esistente = attuali[dto.id]
            val entita = dto.toEntity(now, esistente)
            when {
                esistente == null -> {
                    attuali[dto.id] = entita
                    nuove += dto.id
                    aggiunte++
                }

                esistente != entita -> {
                    attuali[dto.id] = entita
                    aggiornate++
                }

                else -> Unit
            }
        }

        // Le parole nuove entrano subito nella coda di ripasso (carta vergine).
        val reviewsAggiornate = reviews.toMutableMap()
        for (id in nuove) {
            reviewsAggiornate[id] = ReviewEntity(
                wordId = id,
                easeFactor = 2.5f,
                intervalDays = 0,
                repetitions = 0,
                lapses = 0,
                dueAt = now,
                lastReviewedAt = null
            )
        }

        return Esito(
            parole = attuali.values.toList(),
            reviews = reviewsAggiornate,
            aggiunte = aggiunte,
            aggiornate = aggiornate
        )
    }
}
