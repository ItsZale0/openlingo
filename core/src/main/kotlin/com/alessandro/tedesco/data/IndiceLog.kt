package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.RipassoLogEntity
import java.time.LocalDate

/**
 * Indice del log dei ripassi: TUTTO cio' che serve a statistiche e progresso,
 * calcolato in UN SOLO passaggio e memoizzato sull'identita' della lista.
 *
 * Perche' esiste: il progresso si ricalcola a ogni evento (ogni risposta data) e
 * ogni ricalcolo faceva oltre 10 scansioni complete del log (5000 voci), con una
 * `LocalDate` creata per voce per 7 giorni diversi: ~35.000 oggetti per evento.
 * Era la spazzatura che riempiva l'heap fino all'OutOfMemoryError.
 *
 * La chiave di cache e' l'IDENTITA' della lista: il repository la sostituisce a
 * ogni modifica, quindi `===` e' una chiave valida e sicura.
 */
class IndiceLog private constructor(
    /** wordId -> esito dell'ultima verifica (solo eventi di verifica). */
    val verifiche: Map<String, Boolean>,
    /** Giorni distinti con almeno un ripasso (per la streak). */
    val giorni: Set<LocalDate>,
    /** epochDay -> numero di ripassi di quel giorno (per il grafico 7 giorni). */
    val ripassiPerGiorno: Map<Long, Int>,
    /** Numero totale di eventi nel log. */
    val totale: Int,
    /** Quanti eventi sono risposte corrette. */
    val corretti: Int,
    /** wordId -> timestamp della PRIMA risposta corretta (quando fu "imparata"). */
    val primaCorretta: Map<String, Long>
) {
    val errori: Int get() = totale - corretti

    val accuratezza: Float get() = if (totale == 0) 0f else corretti.toFloat() / totale.toFloat()

    /** Ripassi in un dato giorno (0 se nessuno). */
    fun ripassiDelGiorno(giorno: LocalDate): Int = ripassiPerGiorno[giorno.toEpochDay()] ?: 0

    companion object {
        private var cacheLog: List<RipassoLogEntity>? = null
        private var cache: IndiceLog? = null

        /** Indice del log, memoizzato: stesso log (identita') = stesso indice. */
        @Synchronized
        fun di(log: List<RipassoLogEntity>): IndiceLog {
            val cached = cache
            if (cached != null && cacheLog === log) return cached

            val verifiche = HashMap<String, Boolean>()
            val ultimoTimestamp = HashMap<String, Long>()
            val giorni = HashSet<LocalDate>()
            val perGiorno = HashMap<Long, Int>()
            val primaCorretta = HashMap<String, Long>()
            var corretti = 0

            for (evento in log) {
                val epochDay = evento.timestamp / 86_400_000L
                giorni.add(LocalDate.ofEpochDay(epochDay))
                perGiorno[epochDay] = (perGiorno[epochDay] ?: 0) + 1

                if (evento.corretta) {
                    corretti++
                    val precedente = primaCorretta[evento.wordId]
                    if (precedente == null || evento.timestamp < precedente) {
                        primaCorretta[evento.wordId] = evento.timestamp
                    }
                }

                if (evento.verifica) {
                    // In caso di timestamp identici vale l'ULTIMO evento della lista.
                    val precedente = ultimoTimestamp[evento.wordId]
                    if (precedente == null || evento.timestamp >= precedente) {
                        ultimoTimestamp[evento.wordId] = evento.timestamp
                        verifiche[evento.wordId] = evento.corretta
                    }
                }
            }

            val indice = IndiceLog(verifiche, giorni, perGiorno, log.size, corretti, primaCorretta)
            cacheLog = log
            cache = indice
            return indice
        }

        /** Libera l'indice (pressione di memoria): si ricostruisce al bisogno. */
        @Synchronized
        fun svuota() {
            cacheLog = null
            cache = null
        }
    }
}
