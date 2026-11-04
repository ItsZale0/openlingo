package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR

/**
 * Valori UFFICIALI delle certificazioni linguistiche tedesche, con fonte.
 * Applicati ai livelli dell'app e alle statistiche.
 *
 * Fonti (verificate):
 * - Soglia di superamento 60%: Goethe-Institut "Durchfuehrungsbestimmungen" B1/B2
 *   ("Ein Modul ist bestanden, wenn mindestens 60 Punkte bzw. 60 % erreicht sind"),
 *   telc (60% scritto e orale), ÖSD ZB1 ("mindestens 60 Punkte bzw. 60 %").
 * - Scala di valutazione (Prädikat) Goethe, per modulo da 100 punti.
 * - Wortliste Goethe: A1 ~650, A2 ~1300, B1 ~2400 unita lessicali (RICETTIVE).
 * - Guided learning hours (Cambridge English, cumulate da zero): A1 90-100,
 *   A2 180-200, B1 350-400, B2 500-600.
 */
object CertificazioniUfficiali {

    /** Soglia ufficiale di superamento (Goethe / telc / ÖSD): 60%. */
    const val SOGLIA_SUPERAMENTO = 60f

    /** Scala di valutazione ufficiale Goethe (Prädikat), per modulo da 100 punti. */
    enum class Prädikat(val etichetta: String) {
        SEHR_GUT("sehr gut"),
        GUT("gut"),
        BEFRIEDIGEND("befriedigend"),
        AUSREICHEND("ausreichend"),
        NICHT_BESTANDEN("nicht bestanden")
    }

    /** Converte un punteggio 0-100 nel Prädikat ufficiale Goethe. */
    fun praedikat(punteggio: Float): Prädikat = when {
        punteggio >= 90f -> Prädikat.SEHR_GUT
        punteggio >= 80f -> Prädikat.GUT
        punteggio >= 70f -> Prädikat.BEFRIEDIGEND
        punteggio >= 60f -> Prädikat.AUSREICHEND
        else -> Prädikat.NICHT_BESTANDEN
    }

    /** true se il punteggio supera la soglia ufficiale del 60%. */
    fun supera(punteggio: Float): Boolean = punteggio >= SOGLIA_SUPERAMENTO

    /**
     * Wortliste Goethe ufficiale: unita lessicali RICETTIVE che il candidato
     * deve conoscere al livello (A1 ~650, A2 ~1300, B1 ~2400).
     */
    fun wortlisteRicettiva(livello: LivelloCEFR): Int = when (livello) {
        LivelloCEFR.A0 -> 0
        LivelloCEFR.A1 -> 650
        LivelloCEFR.A2 -> 1300
        LivelloCEFR.B1 -> 2400
        LivelloCEFR.B2 -> 4000
        LivelloCEFR.C1 -> 8000
        LivelloCEFR.C2 -> 16000
    }

    /**
     * Vocabolario ATTIVO richiesto (Goethe: "Als aktiver Wortschatz sollte einem
     * Lernenden auf dieser Stufe etwa die Haelfte dieser circa 650 Eintraege zur
     * Verfuegung stehen" -> attivo = circa meta' della Wortliste ricettiva).
     * E' il valore che l'app usa per il gate "parole mature" del livello.
     */
    fun vocabolarioAttivoRichiesto(livello: LivelloCEFR): Int =
        wortlisteRicettiva(livello) / 2

    /**
     * Guided learning hours cumulate da zero (Cambridge English, ufficiale).
     * Usate come riferimento onesto del "tempo di studio" necessario.
     */
    fun oreGuidaCumulate(livello: LivelloCEFR): IntRange = when (livello) {
        LivelloCEFR.A0 -> 0..0
        LivelloCEFR.A1 -> 90..100
        LivelloCEFR.A2 -> 180..200
        LivelloCEFR.B1 -> 350..400
        LivelloCEFR.B2 -> 500..600
        LivelloCEFR.C1 -> 700..800
        LivelloCEFR.C2 -> 1000..1200
    }
}
