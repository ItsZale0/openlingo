package com.alessandro.tedesco.data

import java.time.LocalDate
import kotlin.random.Random

/**
 * ROTORE ESERCIZI.
 *
 * Le sezioni con un banco fisso di esercizi (Sprechuebungen, Dialoghi,
 * Pronuncia) sono più utili se ogni giorno ripresentano i contenuti in un
 * ordine diverso: l'utente percepisce esercizi "nuovi" anche studiando la
 * stessa lista.
 *
 * Come funziona: partiamo dal giorno corrente: la rotazione è DETERMINISTICA
 * (stesso giorno = stesso ordine, niente sfarfallio) e cambia da un giorno
 * all'altro. Non salva NIENTE: dopo la storia recente delle perdite dati,
 * le nuove funzionalità non devono aggiungere stato persistito.
 */
object RotoreEsercizi {

    /** Giorno corrente (data acquisita SENZA ora: cambia a mezzanotte). */
    private fun giornoCorrente(): Long = LocalDate.now().toEpochDay()

    /**
     * Indice di partenza per un ciclo di esercizi uno-dopo-l'altro:
     * ogni giorno si riparte da un punto diverso della lista.
     */
    fun indiceIniziale(sezione: String, grandezza: Int): Int {
        if (grandezza <= 0) return 0
        val semina = (giornoCorrente() * 1009L + sezione.hashCode()).toInt()
        return (Random(semina).nextInt(grandezza) + grandezza) % grandezza
    }

    /**
     * Permutazione degli indici della lista per il giorno corrente: usata
     * dalle schermate a lista (Dialoghi, Pronuncia) per girare l'ordine.
     * Ritorna gli indici 0..grandezza-1 mescolati in modo deterministico.
     */
    fun ordine(sezione: String, grandezza: Int): List<Int> {
        if (grandezza <= 0) return emptyList()
        val semina = (giornoCorrente() * 1009L + sezione.hashCode()).toInt()
        return mescola(semina, (0 until grandezza).toList())
    }

    /**
     * Indici degli esercizi da mostrare in una sessione.
     *
     * A differenza di [ordine] (che cambia solo a mezzanotte), qui il seme è
     * scelto da chi chiama: la sessione di una lezione cambia a ogni tentativo,
     * così rifare gli esercizi non significa ritrovare le stesse domande.
     * Lo stesso seme produce sempre la stessa sessione (niente sfarfallio tra
     * ricomposizioni).
     *
     * @param sezione identificatore stabile della lezione
     * @param grandezza quanti esercizi ha il banco
     * @param quanti quanti esercizi mostrare (limitato a [grandezza])
     * @param seme seme del tentativo (es. timestamp di apertura + numero di prove)
     */
    fun sessione(sezione: String, grandezza: Int, quanti: Int, seme: Int): List<Int> {
        if (grandezza <= 0) return emptyList()
        val n = quanti.coerceIn(1, grandezza)
        val mescolati = mescola(
            seme xor (sezione.hashCode() * 31),
            (0 until grandezza).toList()
        )
        return mescolati.take(n)
    }

    /**
     * Mescola le opzioni di una scelta multipla e restituisce anche il nuovo
     * indice della risposta corretta. Serve perché memorizzare "la risposta è
     * la B" non deve bastare: il testo corretto resta lo stesso, la posizione no.
     */
    fun mescolaOpzioni(
        seme: Int,
        opzioni: List<String>,
        rispostaCorretta: Int
    ): Pair<List<String>, Int> {
        if (opzioni.size <= 1 || rispostaCorretta !in opzioni.indices) {
            return opzioni to rispostaCorretta
        }
        val ordine = mescola(seme, opzioni.indices.toList())
        val nuove = ordine.map { opzioni[it] }
        return nuove to ordine.indexOf(rispostaCorretta)
    }

    /** Fisher-Yates deterministico: nessun duplicato, nessun buco. */
    private fun mescola(semina: Int, indici: List<Int>): List<Int> {
        val rnd = Random(semina)
        val out = indici.toMutableList()
        for (i in out.size - 1 downTo 1) {
            val j = rnd.nextInt(i + 1)
            val t = out[i]; out[i] = out[j]; out[j] = t
        }
        return out
    }
}
