package com.alessandro.tedesco.data

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Badge(
    val id: String,
    val nome: String,
    val descrizione: String,
    val icona: String,
    val sbloccato: Boolean = false,
    val dataSblocco: String? = null
)

@Serializable
data class GamificationState(
    val streakGiorni: Int = 0,
    val streakRecord: Int = 0,
    val ultimoGiornoStudio: String? = null,
    val badgeSbloccati: List<String> = emptyList(),
    val puntiTotali: Int = 0
)

val BADGE_DISPONIBILI = listOf(
    Badge("primo_passo", "Primo passo", "Completa la prima lezione", "🎯"),
    Badge("studente_costante", "Studente costante", "7 giorni di fila", "🔥"),
    Badge("vocabolario_crescita", "Vocabolario in crescita", "50 parole imparate", "📚"),
    Badge("grammatica_pro", "Grammatica pro", "10 esercizi di grammatica completati", "✏️"),
    Badge("conversatore", "Conversatore", "5 conversazioni vocali completate", "🎤"),
    Badge("ascoltatore", "Ascoltatore", "5 esercizi di ascolto completati", "🎧"),
    Badge("test_master", "Test master", "3 test completati", "📝"),
    Badge("maratoneta", "Maratoneta", "30 giorni di fila", "🏃"),
    Badge("poliglotta", "Poliglotta", "100 parole imparate", "🌟"),
    Badge("perfezionista", "Perfezionista", "50 esercizi completati senza errori", "💎")
)

fun controllaBadge(
    streakGiorni: Int,
    paroleTotali: Int,
    eserciziCompletati: Int,
    conversazioniCompletate: Int,
    ascoltiCompletati: Int,
    testCompletati: Int,
    badgeGiaSbloccati: List<String>
): List<Badge> {
    val nuoviBadge = mutableListOf<Badge>()
    val oggi = LocalDate.now().toString()
    
    if (streakGiorni >= 7 && !badgeGiaSbloccati.contains("studente_costante")) {
        nuoviBadge.add(Badge("studente_costante", "Studente costante", "7 giorni di fila", "🔥", true, oggi))
    }
    if (paroleTotali >= 50 && !badgeGiaSbloccati.contains("vocabolario_crescita")) {
        nuoviBadge.add(Badge("vocabolario_crescita", "Vocabolario in crescita", "50 parole imparate", "📚", true, oggi))
    }
    if (eserciziCompletati >= 10 && !badgeGiaSbloccati.contains("grammatica_pro")) {
        nuoviBadge.add(Badge("grammatica_pro", "Grammatica pro", "10 esercizi di grammatica completati", "✏️", true, oggi))
    }
    if (conversazioniCompletate >= 5 && !badgeGiaSbloccati.contains("conversatore")) {
        nuoviBadge.add(Badge("conversatore", "Conversatore", "5 conversazioni vocali completate", "🎤", true, oggi))
    }
    if (ascoltiCompletati >= 5 && !badgeGiaSbloccati.contains("ascoltatore")) {
        nuoviBadge.add(Badge("ascoltatore", "Ascoltatore", "5 esercizi di ascolto completati", "🎧", true, oggi))
    }
    if (testCompletati >= 3 && !badgeGiaSbloccati.contains("test_master")) {
        nuoviBadge.add(Badge("test_master", "Test master", "3 test completati", "📝", true, oggi))
    }
    if (streakGiorni >= 30 && !badgeGiaSbloccati.contains("maratoneta")) {
        nuoviBadge.add(Badge("maratoneta", "Maratoneta", "30 giorni di fila", "🏃", true, oggi))
    }
    if (paroleTotali >= 100 && !badgeGiaSbloccati.contains("poliglotta")) {
        nuoviBadge.add(Badge("poliglotta", "Poliglotta", "100 parole imparate", "🌟", true, oggi))
    }
    
    return nuoviBadge
}
