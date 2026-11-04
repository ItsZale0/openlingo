package com.alessandro.tedesco.data

import kotlinx.serialization.Serializable

/**
 * Un errore reale fatto dallo studente, non una parola generica.
 *
 * Il quaderno degli errori è un SRS separato dal lessico: gli errori ricorrenti
 * (articolo, caso, ordine delle parole, preposizione) sono il vero collo di
 * bottiglia verso il B1 e devono riemergere in modo mirato.
 */
@Serializable
data class ErroreEntity(
    val id: String,
    /** "grammatica", "traduzione", "dettato", "cloze", "riordino", ... */
    val tipo: String,
    val domanda: String,
    val rispostaCorretta: String,
    val rispostaData: String = "",
    val spiegazione: String = "",
    val livello: String = "A1",
    val creato: Long = 0L,
    val ultimaVolta: Long = 0L,
    val ripetizioni: Int = 0,
    val intervalloGiorni: Int = 0,
    val prossimaRipasso: Long = 0L,
    val risolto: Boolean = false
) {
    /** Chiave stabile per non duplicare lo stesso errore. */
    val chiave: String
        get() = "${tipo.trim().lowercase()}|${RisposteProduttive.normalizza(domanda)}|${RisposteProduttive.normalizza(rispostaCorretta)}"
}

/**
 * Ripetizione spaziata degli errori (più semplice dell'SM-2 del lessico, ma con
 * la stessa logica: ogni richiamo riuscito allontana la scadenza, ogni errore
 * riavvicina).
 */
object ErroriSRS {

    val INTERVALLI = listOf(1, 3, 7, 16, 35, 75, 150)
    private const val GIORNO_MS = 24L * 60L * 60L * 1000L
    private const val SOGLIA_RISOLTO = 5

    fun nuovo(
        tipo: String,
        domanda: String,
        rispostaCorretta: String,
        rispostaData: String = "",
        spiegazione: String = "",
        livello: String = "A1",
        now: Long = System.currentTimeMillis()
    ): ErroreEntity = ErroreEntity(
        id = "err_${now}_${domanda.hashCode().toUInt()}_${rispostaCorretta.hashCode().toUInt()}",
        tipo = tipo,
        domanda = domanda,
        rispostaCorretta = rispostaCorretta,
        rispostaData = rispostaData,
        spiegazione = spiegazione,
        livello = livello,
        creato = now,
        ultimaVolta = now,
        ripetizioni = 0,
        intervalloGiorni = 1,
        prossimaRipasso = now + GIORNO_MS,
        risolto = false
    )

    /** Aggiorna l'errore dopo un ripasso: saputo = richiamo riuscito. */
    fun rispondi(errore: ErroreEntity, saputo: Boolean, now: Long = System.currentTimeMillis()): ErroreEntity {
        return if (saputo) {
            val ripetizioni = errore.ripetizioni + 1
            val giorni = INTERVALLI.getOrElse(ripetizioni - 1) { INTERVALLI.last() }
            errore.copy(
                ultimaVolta = now,
                ripetizioni = ripetizioni,
                intervalloGiorni = giorni,
                prossimaRipasso = now + giorni * GIORNO_MS,
                risolto = ripetizioni >= SOGLIA_RISOLTO
            )
        } else {
            errore.copy(
                ultimaVolta = now,
                ripetizioni = 0,
                intervalloGiorni = 1,
                prossimaRipasso = now + GIORNO_MS,
                risolto = false
            )
        }
    }

    fun daRipassare(errore: ErroreEntity, now: Long = System.currentTimeMillis()): Boolean =
        !errore.risolto && errore.prossimaRipasso <= now

    fun giorniAllaScadenza(errore: ErroreEntity, now: Long = System.currentTimeMillis()): Int {
        if (errore.risolto) return -1
        val diff = errore.prossimaRipasso - now
        if (diff <= 0) return 0
        return ((diff + GIORNO_MS - 1) / GIORNO_MS).toInt()
    }
}
