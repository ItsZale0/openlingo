package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LessonEntity
import com.alessandro.tedesco.data.local.WordEntity

/**
 * Scegli la prossima lezione "reale": quella che ha parole nel vocabolario o
 * contenuto nel feed. Senza parole e senze lezioni, prosegui sulla successiva.
 *
 * Logica pura: nessun repository, nessuna rete, nessun I/O. E' la parte che
 * calcola la progressione delle lezioni e va mantenuta in questo posto, lontano
 * dal ViewModel.
 */
fun selezionaProssimaLezione(lezioneCorrente: Int, parole: List<WordEntity>, lezioniFeed: List<LessonEntity>): Int {
    val reali = (parole.map { it.lesson }.distinct() + lezioniFeed.map { it.numero })
        .distinct()
        .sorted()
    return if (reali.isEmpty()) lezioneCorrente + 1
        else (lezioneCorrente + 1..reali.last()).firstOrNull { it in reali } ?: lezioneCorrente + 1
}
