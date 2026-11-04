package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.DomandaTest
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.TipoDomanda

/**
 * Domande per i quiz di comprensione (Lesen) e produzione (Schreiben).
 * Seguono lo stesso pattern di TestB1Data.
 */
object QuizData {

    // === COMPRENSIONE (Lesen) ===

    val domandeComprensione: List<DomandaTest> = listOf(
        DomandaTest(
            id = "comp_1",
            tipo = TipoDomanda.TRADUZIONE,
            domanda = "Was bedeutet 'der Tisch'?",
            opzioni = listOf("il tavolo", "la sedia", "la porta", "la finestra"),
            rispostaCorretta = 0,
            spiegazione = "'Der Tisch' significa 'il tavolo'.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "comp_2",
            tipo = TipoDomanda.TRADUZIONE,
            domanda = "Was bedeutet 'die Küche'?",
            opzioni = listOf("il bagno", "la cucina", "la camera", "il soggiorno"),
            rispostaCorretta = 1,
            spiegazione = "'Die Küche' significa 'la cucina'.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "comp_3",
            tipo = TipoDomanda.VOCAB_TED_ITA,
            domanda = "Quale parola tedesca significa 'lavorare'?",
            opzioni = listOf("wohnen", "arbeiten", "lernen", "spielen"),
            rispostaCorretta = 1,
            spiegazione = "'Arbeiten' significa 'lavorare'. 'Wohnen' = abitare, 'lernen' = imparare, 'spielen' = giocare.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "comp_4",
            tipo = TipoDomanda.VOCAB_ITA_TED,
            domanda = "Come si dice 'la settimana' in tedesco?",
            opzioni = listOf("der Tag", "die Woche", "der Monat", "das Jahr"),
            rispostaCorretta = 1,
            spiegazione = "'Die Woche' significa 'la settimana'. 'Der Tag' = il giorno, 'der Monat' = il mese, 'das Jahr' = l'anno.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "comp_5",
            tipo = TipoDomanda.TRADUZIONE,
            domanda = "Was bedeutet 'Ich habe zwei Brüder.'?",
            opzioni = listOf(
                "Io ho due fratelli.",
                "Io ho due sorelle.",
                "Io vado dal fratello.",
                "Io amo i miei fratelli."
            ),
            rispostaCorretta = 0,
            spiegazione = "'Zwei Brüder' = due fratelli. 'Schwestern' = sorelle.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "comp_6",
            tipo = TipoDomanda.VOCAB_TED_ITA,
            domanda = "Quale parola tedesca significa 'mangiare'?",
            opzioni = listOf("trinken", "essen", "schlafen", "lesen"),
            rispostaCorretta = 1,
            spiegazione = "'Essen' significa 'mangiare'. 'Trinken' = bere, 'schlafen' = dormire, 'lesen' = leggere.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "comp_7",
            tipo = TipoDomanda.TRADUZIONE,
            domanda = "Was bedeutet 'Das Wetter ist schön.'?",
            opzioni = listOf(
                "Il tempo è bello.",
                "Il tempo è brutto.",
                "Fa freddo.",
                "Fa caldo."
            ),
            rispostaCorretta = 0,
            spiegazione = "'Schön' = bello. 'Das Wetter' = il tempo (meteo).",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "comp_8",
            tipo = TipoDomanda.VOCAB_ITA_TED,
            domanda = "Come si dice 'domani' in tedesco?",
            opzioni = listOf("gestern", "heute", "morgen", "jetzt"),
            rispostaCorretta = 2,
            spiegazione = "'Morgen' = domani. 'Gestern' = ieri, 'heute' = oggi, 'jetzt' = adesso.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "comp_9",
            tipo = TipoDomanda.TRADUZIONE,
            domanda = "Was bedeutet 'Ich wohne in Berlin.'?",
            opzioni = listOf(
                "Abito a Berlino.",
                "Lavoro a Berlino.",
                "Vado a Berlino.",
                "Amo Berlino."
            ),
            rispostaCorretta = 0,
            spiegazione = "'Wohnen' = abitare. 'In Berlin' = a Berlino.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "comp_10",
            tipo = TipoDomanda.VOCAB_TED_ITA,
            domanda = "Quale parola tedesca significa 'parlare'?",
            opzioni = listOf("sagen", "sprechen", "hören", "sehen"),
            rispostaCorretta = 1,
            spiegazione = "'Sprechen' = parlare. 'Sagen' = dire, 'hören' = sentire, 'sehen' = vedere.",
            livelloRichiesto = LivelloCEFR.A1
        )
    )

    // === PRODUZIONE (Schreiben) ===

    val domandeProduzione: List<DomandaTest> = listOf(
        DomandaTest(
            id = "prod_1",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Ich ___ gern Kaffee trinken.",
            opzioni = listOf("würde", "werde", "habe", "bin"),
            rispostaCorretta = 0,
            spiegazione = "Konjunktiv II per desideri irreali: 'würde' + infinito.",
            livelloRichiesto = LivelloCEFR.B1
        ),
        DomandaTest(
            id = "prod_2",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Ich ___ gestern ins Kino gegangen.",
            opzioni = listOf("bin", "habe", "war", "hatte"),
            rispostaCorretta = 0,
            spiegazione = "Perfekt con 'sein' per verbi di movimento: 'ich bin gegangen'.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "prod_3",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Er ___ die Hausaufgaben gemacht.",
            opzioni = listOf("hat", "ist", "war", "wird"),
            rispostaCorretta = 0,
            spiegazione = "Perfekt con 'haben' per la maggior parte dei verbi: 'er hat gemacht'.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "prod_4",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Wir ___ nächste Woche nach Deutschland reisen.",
            opzioni = listOf("werden", "sind", "haben", "waren"),
            rispostaCorretta = 0,
            spiegazione = "Futur con 'werden': 'wir werden reisen'.",
            livelloRichiesto = LivelloCEFR.A2
        ),
        DomandaTest(
            id = "prod_5",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Ich freue mich ___ deinen Besuch.",
            opzioni = listOf("auf", "über", "für", "mit"),
            rispostaCorretta = 0,
            spiegazione = "'Sich freuen auf' = attendere con gioia (futuro). 'Sich freuen über' = essere felice di (passato).",
            livelloRichiesto = LivelloCEFR.A2
        ),
        DomandaTest(
            id = "prod_6",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Er hat mir gesagt, dass er ___ kommt.",
            opzioni = listOf("nicht", "kein", "niemals", "nichts"),
            rispostaCorretta = 0,
            spiegazione = "'Dass' introduce una frase subordinata: 'dass er nicht viene'.",
            livelloRichiesto = LivelloCEFR.B1
        ),
        DomandaTest(
            id = "prod_7",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Ich habe ein Buch ___.",
            opzioni = listOf("gelesen", "lesen", "las", "liest"),
            rispostaCorretta = 0,
            spiegazione = "Perfekt: Partizip II 'gelesen' con 'haben'.",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "prod_8",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Sie ___ seit drei Jahren in Deutschland.",
            opzioni = listOf("wohnt", "wohnte", "wohnen", "gewohnt"),
            rispostaCorretta = 0,
            spiegazione = "Presente con 'seit' + periodo: 'sie wohnt seit...'.",
            livelloRichiesto = LivelloCEFR.A2
        ),
        DomandaTest(
            id = "prod_9",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Ich ___ dir helfen, wenn du willst.",
            opzioni = listOf("kann", "konnte", "könnte", "können"),
            rispostaCorretta = 0,
            spiegazione = "Presente indicativo: 'ich kann' (possibilità attuale).",
            livelloRichiesto = LivelloCEFR.A1
        ),
        DomandaTest(
            id = "prod_10",
            tipo = TipoDomanda.COMPLETAMENTO,
            domanda = "Er hat mir ___ erzählt.",
            opzioni = listOf("alles", "jedes", "vieles", "manches"),
            rispostaCorretta = 0,
            spiegazione = "'Alles' = tutto (neutro). 'Er hat mir alles erzählt' = mi ha raccontato tutto.",
            livelloRichiesto = LivelloCEFR.A2
        )
    )
}
