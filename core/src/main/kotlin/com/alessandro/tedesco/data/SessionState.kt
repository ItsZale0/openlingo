package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.WordEntity

data class SessionState(
    val cards: List<WordEntity> = emptyList(),
    val indice: Int = 0,
    val rispostaMostrata: Boolean = false,
    val sbagliate: MutableList<String> = mutableListOf()
) {
    val cartaCorrente: WordEntity? get() = cards.getOrNull(indice)
    val totale: Int get() = cards.size
    val finita: Boolean get() = indice >= cards.size
    val progresso: Float get() = if (totale == 0) 0f else indice.toFloat() / totale
}

fun createInitialSessionState(): SessionState = SessionState(cards = emptyList<WordEntity>())