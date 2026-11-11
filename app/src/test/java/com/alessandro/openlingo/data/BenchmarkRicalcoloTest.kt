package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.RipassoLogEntity
import com.alessandro.openlingo.data.local.WordEntity
import org.junit.Test

/**
 * BENCHMARK (non asserzione): quanto ALLOCA un singolo ricalcolo del progresso.
 *
 * Il progresso si ricalcola a ogni evento (ogni risposta data). Se un ricalcolo
 * alloca megabyte, l'heap si riempie e l'app muore di OutOfMemoryError: e' quello
 * che e' successo (heap pieno a 256 MB, allocazione fallita in verificheDaLog).
 *
 * Usa ThreadMXBean.getCurrentThreadAllocatedBytes: byte allocati REALI.
 */
class BenchmarkRicalcoloTest {

    private val parole = (1..1200).map { i ->
        WordEntity(
            id = "w$i", german = "Vokabel$i", italian = "parola $i",
            example = "Beispielsatz fuer Vokabel$i.", article = "das",
            pronunciation = null, level = "B1", lesson = (i % 12) + 1
        )
    }
    private val reviews = parole.associate { w ->
        w.id to ReviewEntity(w.id, repetitions = 3, lapses = 1, lastReviewedAt = 1_700_000_000_000L, intervalDays = 3)
    }
    private val log = (1..5000).map { i ->
        RipassoLogEntity("w${(i % 1200) + 1}", 1_700_000_000_000L + i * 60_000L, i % 3 != 0, verifica = i % 5 == 0)
    }
    private val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.B1)

    /** Tempo medio per chiamata in microsecondi (proxy del lavoro allocato). */
    private fun microPerChiamata(n: Int, blocco: () -> Unit): Double {
        val t0 = System.nanoTime()
        repeat(n) { blocco() }
        return (System.nanoTime() - t0) / 1000.0 / n
    }

    @Test
    fun `quanto alloca un ricalcolo`() {
        val now = 1_700_000_000_000L
        // riscaldamento (JIT + cache)
        repeat(20) {
            CalcoloStatistiche.calcola(parole, reviews, progresso, now, log)
            CalcoloProgresso.calcola(parole, reviews, progresso, log, LivelloCEFR.B1, now)
        }
        val n = 200
        val stat = microPerChiamata(n) { CalcoloStatistiche.calcola(parole, reviews, progresso, now, log) }
        val prog = microPerChiamata(n) { CalcoloProgresso.calcola(parole, reviews, progresso, log, LivelloCEFR.B1, now) }
        val comp = microPerChiamata(n) { CalcoloCompetenze.calcola(parole, reviews, progresso, now, log) }

        println("BENCH Statistiche.calcola  : %9.1f us".format(stat))
        println("BENCH Progresso.calcola    : %9.1f us".format(prog))
        println("BENCH Competenze.calcola   : %9.1f us".format(comp))
        println("BENCH totale per evento    : %9.1f us  (=%.2f ms)".format(stat + prog + comp, (stat + prog + comp) / 1000.0))
    }
}
