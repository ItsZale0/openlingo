package com.alessandro.tedesco.data

/**
 * Modulo Südtirol / Austria: la lingua che serve davvero a Bressanone non è
 * solo il tedesco standard "federale". Qui ci sono varianti austriache,
 * lessico amministrativo e sanitario, frasi da ambulatorio e dialoghi con
 * accento/lessico locale.
 */
object Sudtirol {

    data class Variante(
        val austriaco: String,
        val standard: String,
        val italiano: String
    )

    data class VoceLessico(
        val tedesco: String,
        val italiano: String,
        val categoria: String
    )

    data class Frase(
        val tedesco: String,
        val italiano: String,
        val nota: String = ""
    )

    data class Battuta(val parlante: String, val testo: String)

    data class DomandaDialogo(
        val domanda: String,
        val opzioni: List<String>,
        val corretta: Int
    )

    data class Dialogo(
        val titolo: String,
        val livello: String,
        val contesto: String,
        val battute: List<Battuta>,
        val domande: List<DomandaDialogo>
    )

    val varianti: List<Variante> = listOf(
        Variante("der Jänner", "der Januar", "gennaio"),
        Variante("der Erdapfel", "die Kartoffel", "patata"),
        Variante("heuer", "dieses Jahr", "quest'anno"),
        Variante("das Sackerl", "die Tüte", "sacchetto"),
        Variante("die Palatschinke", "der Pfannkuchen", "crespella"),
        Variante("die Marille", "die Aprikose", "albicocca"),
        Variante("die Ribisel", "die Johannisbeere", "ribes"),
        Variante("der Topfen", "der Quark", "ricotta / quark"),
        Variante("der Paradeiser", "die Tomate", "pomodoro"),
        Variante("der Karfiol", "der Blumenkohl", "cavolfiore"),
        Variante("die Fisolen", "die Bohnen", "fagiolini"),
        Variante("die Kohlsprossen", "der Rosenkohl", "cavolini di Bruxelles"),
        Variante("die Semmel", "das Brötchen", "panino"),
        Variante("die Grammeln", "die Grieben", "ciccioli"),
        Variante("der Leberkäse", "die Fleischwurst", "polpettone di carne")
    )

    val lessico: List<VoceLessico> = listOf(
        VoceLessico("der Befund", "referto / risultato clinico", "Ambulatorio"),
        VoceLessico("die Überweisung", "impegnativa / invio", "Ambulatorio"),
        VoceLessico("die Krankenkasse", "cassa mutua / assicurazione sanitaria", "Assicurazione"),
        VoceLessico("die Ordination", "studio medico", "Ambulatorio"),
        VoceLessico("der Termin", "appuntamento", "Ambulatorio"),
        VoceLessico("das Rezept", "ricetta", "Ambulatorio"),
        VoceLessico("die Arbeitsunfähigkeit", "inabilità al lavoro", "Assicurazione"),
        VoceLessico("die Behandlung", "trattamento", "Terapia"),
        VoceLessico("die Übung", "esercizio", "Terapia"),
        VoceLessico("der Patient / die Patientin", "paziente", "Ambulatorio"),
        VoceLessico("die Schmerzen", "dolori", "Sintomi"),
        VoceLessico("die Bewegung", "movimento", "Terapia"),
        VoceLessico("die Physiotherapie", "fisioterapia", "Terapia"),
        VoceLessico("die Anamnese", "anamnesi", "Ambulatorio"),
        VoceLessico("die Diagnose", "diagnosi", "Ambulatorio"),
        VoceLessico("der Therapieplan", "piano terapeutico", "Terapia"),
        VoceLessico("die Kontrolle", "controllo / visita di controllo", "Ambulatorio"),
        VoceLessico("die Entlassung", "dimissione", "Ospedale"),
        VoceLessico("die Zuweisung", "invio / assegnazione", "Ambulatorio"),
        VoceLessico("die Hausübung", "esercizio da fare a casa", "Terapia")
    )

    val frasi: List<Frase> = listOf(
        Frase("Guten Tag, haben Sie einen Termin?", "Buongiorno, ha un appuntamento?"),
        Frase("Ich habe seit drei Tagen Schmerzen im Knie.", "Ho dolore al ginocchio da tre giorni."),
        Frase("Wie stark sind die Schmerzen auf einer Skala von 1 bis 10?", "Quanto è forte il dolore su una scala da 1 a 10?"),
        Frase("Bitte beugen Sie das Knie langsam.", "Pieghi lentamente il ginocchio.", "Imperativo formale: Sie."),
        Frase("Bitte strecken Sie das Bein.", "Distenda la gamba."),
        Frase("Atmen Sie tief ein und langsam aus.", "Inspiri profondamente ed espiri lentamente."),
        Frase("Die Übungen machen Sie bitte dreimal am Tag.", "Faccia gli esercizi tre volte al giorno."),
        Frase("Bitte bringen Sie den Befund mit.", "Porti il referto."),
        Frase("Haben Sie eine Überweisung?", "Ha un'impegnativa?"),
        Frase("Sie können sich hier anmelden.", "Può registrarsi qui."),
        Frase("Ich verstehe nicht ganz. Können Sie das langsamer sagen?", "Non capisco del tutto. Può dirlo più lentamente?"),
        Frase("Können Sie das bitte wiederholen?", "Può ripetere, per favore?"),
        Frase("Ich spreche noch nicht so gut Deutsch.", "Non parlo ancora bene il tedesco."),
        Frase("Sind Sie gesetzlich versichert?", "È assicurato con il servizio sanitario?")
    )

    val dialoghi: List<Dialogo> = listOf(
        Dialogo(
            titolo = "In der Ordination",
            livello = "A2",
            contesto = "Alla reception di uno studio medico.",
            battute = listOf(
                Battuta("Rezeption", "Guten Tag, haben Sie einen Termin?"),
                Battuta("Patient", "Ja, ich heiße Rossi. Ich habe um zehn Uhr einen Termin."),
                Battuta("Rezeption", "Einen Moment bitte. Haben Sie die Überweisung dabei?"),
                Battuta("Patient", "Ja, hier bitte. Und meinen Befund habe ich auch.")
            ),
            domande = listOf(
                DomandaDialogo("A che ora ha l'appuntamento il paziente?", listOf("Alle 9", "Alle 10", "Alle 11", "Alle 12"), 1),
                DomandaDialogo("Che cosa porta con sé il paziente?", listOf("Solo il documento", "L'impegnativa e il referto", "La ricetta", "Niente"), 1)
            )
        ),
        Dialogo(
            titolo = "Physiotherapie: Anamnese",
            livello = "B1",
            contesto = "Primo colloquio in fisioterapia.",
            battute = listOf(
                Battuta("Physiotherapeutin", "Guten Morgen, ich bin Frau Huber. Wo tut es weh?"),
                Battuta("Patient", "Mein Knie, seit drei Tagen. Besonders beim Treppensteigen."),
                Battuta("Physiotherapeutin", "Wie stark sind die Schmerzen von eins bis zehn?"),
                Battuta("Patient", "Etwa sieben. In der Nacht wird es besser.")
            ),
            domande = listOf(
                DomandaDialogo("Da quanto tempo ha dolore il paziente?", listOf("Da un giorno", "Da tre giorni", "Da una settimana", "Da un mese"), 1),
                DomandaDialogo("Quando peggiora il dolore?", listOf("Di notte", "Salendo le scale", "Da seduto", "Mangiando"), 1)
            )
        ),
        Dialogo(
            titolo = "Kollegengespräch: Jänner und Erdäpfel",
            livello = "B1",
            contesto = "Due colleghi parlano in pausa pranzo, con lessico austriaco.",
            battute = listOf(
                Battuta("Anna", "Hast du heuer schon Urlaub gemacht?"),
                Battuta("Lukas", "Ja, im Jänner. Ich war in Südtirol."),
                Battuta("Anna", "Und? Gab es dort gute Erdäpfel?"),
                Battuta("Lukas", "Ja, und einen tollen Topfenstrudel. Die Semmeln waren auch frisch.")
            ),
            domande = listOf(
                DomandaDialogo("Quando è andato in vacanza Lukas?", listOf("A gennaio", "A luglio", "In autunno", "A Natale"), 0),
                DomandaDialogo("Che cosa ha mangiato di buono?", listOf("Una pizza", "Uno strudel di ricotta", "Un panino", "Una torta"), 1)
            )
        )
    )

    /** Traduce una variante austriaca nello standard (per il quiz). */
    fun standardDi(austriaco: String): String? =
        varianti.firstOrNull { it.austriaco.equals(austriaco, ignoreCase = true) }?.standard
}
