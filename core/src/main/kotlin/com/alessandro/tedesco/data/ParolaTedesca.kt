package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.WordEntity

/**
 * Rendering UNICO delle parole tedesche: risolve il doppio articolo.
 *
 * Il feed vocabolario e le parole custom possono contenere l'articolo in DUE posti:
 * - nel campo `article` (es. article = "die", german = "Wand")
 * - gia' dentro `german` (es. german = "die Wand", article = "die" o null)
 *
 * Senza un punto unico, ogni schermata lo gestiva a modo suo (alcune con un fragile
 * `startsWith`, la verifica senza alcun controllo) e usciva "die die Wand".
 */
object ParolaTedesca {

    /** Articoli determinativi e indeterminativi tedeschi. */
    private val ARTICOLI = setOf(
        "der", "die", "das", "den", "dem", "des",
        "ein", "eine", "einen", "einem", "einer", "eines"
    )

    /**
     * Testo della parola con UN SOLO articolo, mai duplicato.
     * Se `german` contiene gia' un articolo, il campo `article` viene ignorato.
     */
    fun conArticolo(article: String?, german: String): String {
        val g = german.trim()
        val a = article?.trim()?.lowercase()
        if (g.isBlank()) return g
        if (a.isNullOrBlank()) return g
        // `german` inizia gia' con QUEL articolo -> non ripeterlo
        if (iniziaConArticolo(g)) return g
        return "$article $g".trim()
    }

    /** true se il testo inizia con un articolo tedesco (es. "die Wand"). */
    fun iniziaConArticolo(testo: String): Boolean {
        val prima = testo.trim().substringBefore(' ').lowercase()
        return prima in ARTICOLI
    }

    /** true se `italian` e' un placeholder del feed, non una traduzione reale. */
    fun traduzioneValida(italian: String?): Boolean {
        val it = italian?.trim() ?: return false
        if (it.isBlank()) return false
        val basso = it.lowercase()
        return basso != "(da completare)" && basso != "da completare" &&
            basso != "-" && basso != "?" && basso != "n/a" && basso != "todo"
    }

    /** La parola e' utilizzabile in un quiz/esercizio? */
    fun utilizzabile(w: WordEntity): Boolean =
        !w.archived && w.german.isNotBlank() && traduzioneValida(w.italian)
}
