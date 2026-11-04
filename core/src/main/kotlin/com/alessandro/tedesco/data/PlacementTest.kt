package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR

/**
 * Test di ingresso breve (12 domande, 3 per fascia). Non sostituisce un test
 * adattivo completo, ma evita che un falso principiante passi settimane su
 * "Saluti e presentazioni".
 */
object PlacementTest {

    data class Domanda(
        val domanda: String,
        val opzioni: List<String>,
        val corretta: Int,
        val livello: String
    )

    val domande: List<Domanda> = listOf(
        Domanda("___ heißt du?", listOf("Wie", "Was", "Wo", "Wer"), 0, "A1"),
        Domanda("Ich ___ aus Italien.", listOf("komme", "kommt", "kommen", "kommst"), 0, "A1"),
        Domanda("___ Buch ist neu.", listOf("Der", "Die", "Das", "Den"), 2, "A1"),
        Domanda("Ich habe ___ Hund.", listOf("ein", "eine", "einen", "einem"), 2, "A2"),
        Domanda("Ich ___ gestern ins Kino gegangen.", listOf("bin", "habe", "war", "hatte"), 0, "A2"),
        Domanda("Ich warte ___ den Bus.", listOf("auf", "für", "mit", "zu"), 0, "A2"),
        Domanda("Das ist der Mann, ___ ich gestern getroffen habe.", listOf("den", "dem", "der", "dessen"), 0, "A2"),
        Domanda("Wenn es morgen regnet, ___ ich zu Hause.", listOf("bleibe", "bleiben", "bleibst", "geblieben"), 0, "A2"),
        Domanda("Das Buch ___ von vielen Menschen gelesen.", listOf("wird", "werden", "ist", "hat"), 0, "B1"),
        Domanda("Wenn ich mehr Zeit ___, würde ich mehr lernen.", listOf("hätte", "habe", "hatte", "hätte gehabt"), 0, "B1"),
        Domanda("Der Mann, ___ Auto gestohlen wurde, ruft die Polizei.", listOf("dessen", "deren", "dem", "den"), 0, "B1"),
        Domanda("___ es regnet, gehen wir spazieren.", listOf("Obwohl", "Weil", "Damit", "Wenn"), 0, "B1")
    )

    data class Esito(val punteggio: Int, val totale: Int, val livello: LivelloCEFR, val messaggio: String)

    fun valuta(risposteCorrette: Int): Esito {
        val livello = when {
            risposteCorrette <= 2 -> LivelloCEFR.A0
            risposteCorrette <= 5 -> LivelloCEFR.A1
            risposteCorrette <= 8 -> LivelloCEFR.A2
            else -> LivelloCEFR.B1
        }
        val messaggio = when (livello) {
            LivelloCEFR.A0 -> "Parti dalle basi: il percorso A0 è quello giusto."
            LivelloCEFR.A1 -> "Hai già qualche base: puoi partire da A1."
            LivelloCEFR.A2 -> "Buona base: parti da A2 e rinforza il Perfetto."
            else -> "Hai solide basi: puoi partire da B1."
        }
        return Esito(risposteCorrette, domande.size, livello, messaggio)
    }
}
