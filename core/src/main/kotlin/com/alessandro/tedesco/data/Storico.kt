package com.alessandro.tedesco.data

/**
 * Tetto alle liste storiche del profilo (test, valutazioni, errori).
 *
 * Crescevano a ogni esercizio senza limite: ogni salvataggio riscrive tutto il
 * repository in DataStore e ogni backup lo serializza, quindi liste infinite
 * rendono l'app sempre piu' lenta e pesante. 500 voci coprono abbondantemente i
 * grafici e le statistiche di un corso biennale.
 */
const val MAX_STORICO = 500

/** Tiene solo le [max] voci piu' recenti. */
fun <T> List<T>.limita(max: Int = MAX_STORICO): List<T> =
    if (size > max) takeLast(max) else this
