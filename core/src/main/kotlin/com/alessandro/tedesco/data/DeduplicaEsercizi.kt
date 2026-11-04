package com.alessandro.tedesco.data

import java.util.Locale

/**
 * Deduplica per CONTENUTO (non per id) degli esercizi.
 *
 * L'AI a volte restituisce lo stesso esercizio più volte nello stesso batch,
 * oppure lo ripropone in sessioni successive: gli id sono sempre nuovi, quindi
 * un semplice `distinctBy { it.id }` non basta. Qui si confronta il testo
 * normalizzato (minuscole, umlaut, punteggiatura, spazi) della domanda,
 * eventualmente insieme alla frase ascoltata.
 *
 * La normalizzazione è volutamente identica a quella usata per le risposte
 * (ae/oe/ue/ss) così che "schön" e "schoen" contino come la stessa domanda.
 */
object DeduplicaEsercizi {

    private val punteggiatura = Regex("[^\\p{L}\\p{N}\\s]+")
    private val spazi = Regex("\\s+")

    /** Testo normalizzato: minuscolo, senza umlaut/punteggiatura, spazi singoli. */
    fun normalizza(testo: String?): String = (testo ?: "")
        .trim()
        .lowercase(Locale.ROOT)
        .replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("ß", "ss")
        .replace(punteggiatura, " ")
        .replace(spazi, " ")
        .trim()

    /**
     * Chiave di confronto stabile. [contesto] serve agli esercizi di ascolto,
     * dove la stessa domanda generica ("Cosa dice il parlante?") può riferirsi
     * a frasi diverse: in quel caso la frase fa parte della chiave.
     */
    fun chiave(domanda: String?, contesto: String? = null): String {
        val base = normalizza(domanda)
        val extra = normalizza(contesto)
        return if (extra.isEmpty()) base else "$base || $extra"
    }

    /** Tiene la prima occorrenza di ogni contenuto, nell'ordine originale. */
    fun <T> distinti(items: List<T>, chiave: (T) -> String): List<T> {
        val visti = HashSet<String>()
        return items.filter { item ->
            val k = chiave(item)
            k.isNotEmpty() && visti.add(k)
        }
    }

    /** Come [distinti], ma scarta anche i contenuti già visti (in [evita]). */
    fun <T> senzaGiaVisti(
        items: List<T>,
        evita: Collection<String>,
        chiave: (T) -> String
    ): List<T> {
        if (evita.isEmpty()) return distinti(items, chiave)
        val vietati = evita.mapTo(HashSet()) { normalizza(it) }
        return distinti(items.filterNot { normalizza(chiave(it)) in vietati }, chiave)
    }

    /**
     * Unisce la memoria storica e i nuovi esercizi tenendo l'ULTIMA occorrenza
     * di ogni contenuto (così vince lo stato "completato" più recente) e limita
     * la lista a [max] elementi.
     */
    fun <T> unisciTenendoUltimi(
        esistenti: List<T>,
        nuovi: List<T>,
        max: Int,
        chiave: (T) -> String
    ): List<T> {
        if (max <= 0) return emptyList()
        val uniti = (esistenti + nuovi).asReversed()
            .distinctBy { chiave(it) }
            .asReversed()
        return if (uniti.size <= max) uniti else uniti.takeLast(max)
    }
}
