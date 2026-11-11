package com.alessandro.openlingo.data

/**
 * Difficoltà di un singolo esercizio, su una scala da 1 a 6.
 *
 * Perché esiste: dentro la stessa unità il banco mescola tipi molto diversi
 * (una scelta multipla da una parola e una produzione di una frase intera)
 * e fino a ora la sessione li presentava in ordine casuale. Lo studente
 * passava da un esercizio da dieci secondi a uno da due minuti senza scala,
 * e la stessa unità sembrava a volte facilissima a volte impossibile.
 *
 * Il punteggio non misura la bellezza dell'esercizio: misura quanto lavoro
 * mentale richiede, con quattro segnali che si sommano e che sono gli stessi
 * che pesano davvero per chi studia:
 *
 * - il TIPO: riconoscere un'opzione è più facile che produrre una frase;
 * - la LUNGHEZZA della consegna: una frase lunga va letta e capita tutta;
 * - la forma della RISPOSTA: una parola sola è più facile di una frase;
 * - il numero di OPZIONI: più alternative da confrontare, più difficile.
 *
 * Il risultato è limitato a 1..6 perché oltre la scala perde significato: una
 * produzione lunga e una trasformazione lunga sono comunque "il massimo".
 */
object DifficoltaEsercizi {

    const val MINIMA = 1
    const val MASSIMA = 6

    /**
     * Da questo punteggio un esercizio è un "picco" della sessione: nella
     * stessa sessione ne compare al massimo uno, così il tentativo non diventa
     * una raffica di esercizi tutti al limite.
     */
    const val PESO_ALTO = 5

    /** Lunghezza (in caratteri) oltre la quale la consegna è considerata lunga. */
    private const val LUNGHEZZA_CONSEGNA = 45

    /** Punteggio dell'esercizio, sempre compreso tra [MINIMA] e [MASSIMA]. */
    fun punteggio(esercizio: EsercizioUnita): Int {
        var punti = when (esercizio.tipo) {
            TipoEsercizioGrammatica.SCELTA_MULTIPLA -> 1
            TipoEsercizioGrammatica.COMPLETAMENTO -> 2
            TipoEsercizioGrammatica.TRASFORMAZIONE -> 3
            TipoEsercizioGrammatica.PRODUZIONE -> 4
        }
        if (esercizio.domanda.length > LUNGHEZZA_CONSEGNA) punti++
        val parole = paroleRisposta(esercizio)
        if (parole >= 2) punti++
        if (parole >= 4) punti++
        if (esercizio.opzioni.size > 4) punti++
        return punti.coerceIn(MINIMA, MASSIMA)
    }

    /** Vero se l'esercizio è una produzione: il tipo più impegnativo. */
    fun eProduzione(esercizio: EsercizioUnita): Boolean =
        esercizio.tipo == TipoEsercizioGrammatica.PRODUZIONE

    /** Vero se l'esercizio è un picco di difficoltà per la sessione. */
    fun ePicco(esercizio: EsercizioUnita): Boolean =
        eProduzione(esercizio) || punteggio(esercizio) >= PESO_ALTO

    /**
     * Testo della risposta attesa, utile per misurare la difficoltà e per i
     * controlli automatici: per la scelta multipla è l'opzione corretta.
     */
    fun risposta(esercizio: EsercizioUnita): String =
        if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
            esercizio.opzioni.getOrNull(esercizio.rispostaCorretta).orEmpty()
        } else {
            esercizio.rispostaTesto
        }

    private fun paroleRisposta(esercizio: EsercizioUnita): Int =
        risposta(esercizio)
            .trim()
            .split(Regex("\\s+"))
            .count { it.isNotBlank() }
}
