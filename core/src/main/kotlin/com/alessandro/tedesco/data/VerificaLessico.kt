package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.WordEntity
import kotlin.math.abs

/**
 * VERIFICA OGGETTIVA del lessico.
 *
 * Il ripasso classico e' self-report ("Lo so" / "Non lo so"): l'utente puo' dire
 * "lo so" senza saperlo, e `repetitions >= 4` misura quante volte ha VISTO la
 * parola, non se la SA. Qui si verifica in modo oggettivo: si mostra la parola
 * tedesca e si sceglie il significato italiano corretto tra 3-4 distrattori.
 * Solo una risposta corretta al primo colpo conta come "verificata".
 *
 * Da questa evidenza nascono:
 * - `paroleVerificate`: quante parole l'utente ha DIMOSTRATO di sapere
 * - il punteggio LESSICO del progresso, che non e' piu' un contatore di ripassi
 * - la lista di parole "fragili" da riproporre nel quiz
 */
object VerificaLessico {

    /** Distrattori da mostrare oltre alla risposta corretta. */
    const val DISTRATTORI = 3

    /** Una domanda del quiz di verifica. */
    data class Domanda(
        val parola: WordEntity,
        val opzioni: List<String>,   // significati italiani, la corretta e' inclusa
        val corretta: String
    )

    /** Esito di una sessione di verifica. */
    data class Esito(
        val totale: Int,
        val corrette: Int,
        val paroleVerificate: List<String>,  // id delle parole sapute al primo colpo
        val paroleDaRivedere: List<String>   // id delle parole sbagliate
    ) {
        val punteggio: Float get() = if (totale == 0) 0f else corrette.toFloat() / totale * 100f
    }

    /**
     * Costruisce le domande mescolando i significati di altre parole come distrattori.
     * Le parole con lo stesso significato italiano non vengono usate come distrattori
     * (eviterebbe domande ambigue con due risposte giuste).
     */
    fun costruisciDomande(
        parole: List<WordEntity>,
        distrattori: Int = DISTRATTORI,
        casuale: kotlin.random.Random = kotlin.random.Random.Default
    ): List<Domanda> {
        if (parole.size < 2) return emptyList()
        return parole.map { p ->
            val candidati = parole
                .filter { it.id != p.id && it.italian.isNotBlank() }
                .filter { it.italian.trim().lowercase() != p.italian.trim().lowercase() }
                .map { it.italian.trim() }
                .distinct()
                .shuffled(casuale)
                .take(distrattori)
            val opzioni = (candidati + p.italian.trim()).distinct().shuffled(casuale)
            Domanda(parola = p, opzioni = opzioni, corretta = p.italian.trim())
        }
    }

    /**
     * Punteggio lessico (0-100): percentuale di parole VERIFICATE sulle parole
     * disponibili. Non usa `repetitions`: usa solo evidenza di risposte corrette
     * registrate nel log (esito del quiz di verifica).
     *
     * @param paroleDisponibili parole attive (non archiviate)
     * @param verifiche mappa wordId -> true se verificata (saputa al primo colpo)
     */
    fun punteggioLessico(paroleDisponibili: Int, verifiche: Map<String, Boolean>): Float {
        if (paroleDisponibili == 0) return 0f
        val verificate = verifiche.count { it.value }
        return (verificate.toFloat() / paroleDisponibili * 100f).coerceIn(0f, 100f)
    }

    /**
     * Ricostruisce quali parole risultano VERIFICATE dal log.
     * Una parola e' verificata se l'ultima VERIFICA (quiz) e' corretta.
     * Il self-report del flashcard ("Lo so"/"Non lo so") NON conta: non e' evidenza.
     */
    /**
     * Mappa parola -> esito dell'ULTIMA verifica.
     * Delegata a [IndiceLog]: un solo passaggio sul log, memoizzato.
     */
    fun verificheDaLog(log: List<RipassoLogEntity>): Map<String, Boolean> =
        IndiceLog.di(log).verifiche

    /** Giorni distinti con almeno un ripasso (per la streak). */
    fun giorniDaLog(log: List<RipassoLogEntity>): Set<java.time.LocalDate> =
        IndiceLog.di(log).giorni

    /** Libera le cache (pressione di memoria): si ricostruiscono al bisogno. */
    fun svuotaCache() {
        IndiceLog.svuota()
    }

    /**
     * Parole "fragili": sbagliate almeno una volta e non ancora verificate.
     * Sono quelle da riproporre per prime nel quiz.
     */
    fun paroleFragili(
        parole: List<WordEntity>,
        log: List<RipassoLogEntity>,
        verifiche: Map<String, Boolean>
    ): List<WordEntity> {
        // Solo le verifiche sbagliate rendono una parola "fragile": un "Non lo so"
        // nel flashcard non e' un errore dimostrato.
        val sbagliate = log.filter { it.verifica && !it.corretta }.map { it.wordId }.toSet()
        return parole.filter { w -> !w.archived && sbagliate.contains(w.id) && verifiche[w.id] != true }
    }

    /**
     * Parole da ripassare: UNICA fonte di verita' — la usano sia il contatore
     * ("N da ripassare" in home/hub) sia le carte della sessione di ripasso.
     * Con due insiemi diversi l'app mostrava "niente da ripassare" a schermo aperto
     * ma "2 da ripassare" a schermo chiuso: l'utente non capiva cosa fosse successo.
     * Unisce:
     *  - fragili: sbagliate in una verifica e non ancora verificate;
     *  - scadute: review di ripetizione spaziata con dueAt <= now.
     * Le fragili vengono per prime: sono quelle da riprendere subito.
     */
    fun paroleDaRipassare(
        parole: List<WordEntity>,
        log: List<RipassoLogEntity>,
        verifiche: Map<String, Boolean>,
        reviews: Map<String, ReviewEntity>,
        now: Long
    ): List<WordEntity> {
        val fragili = paroleFragili(parole, log, verifiche).map { it.id }.toSet()
        val scadute = reviews.values.filter { it.dueAt <= now }.map { it.wordId }.toSet()
        val daRipassare = fragili + scadute
        return parole
            .filter { !it.archived && daRipassare.contains(it.id) }
            .filter { ParolaTedesca.utilizzabile(it) }
            .sortedWith(
                compareByDescending<WordEntity> { fragili.contains(it.id) }
                    .thenBy { reviews[it.id]?.dueAt ?: Long.MAX_VALUE }
            )
    }

    /** Parole mai verificate (nuove o deboli), priorita' a quelle con piu' errori. */
    fun paroleDaVerificare(
        parole: List<WordEntity>,
        log: List<RipassoLogEntity>,
        verifiche: Map<String, Boolean>,
        limite: Int
    ): List<WordEntity> {
        val erroriPerParola = log.filter { it.verifica && !it.corretta }.groupingBy { it.wordId }.eachCount()
        return parole
            .filter { !it.archived && verifiche[it.id] != true }
            .sortedByDescending { erroriPerParola[it.id] ?: 0 }
            .take(limite)
    }
}
