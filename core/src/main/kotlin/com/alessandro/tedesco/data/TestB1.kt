package com.alessandro.tedesco.data

import kotlinx.serialization.Serializable

/**
 * Test di valutazione che simula il Goethe-Zertifikat B1.
 * 4 sezioni: Lesen, Hören, Schreiben, Sprechen.
 */
@Serializable
data class TestB1(
    val id: String,
    val data: Long,
    val punteggioLesen: Float,      // 0-100
    val punteggioHoeren: Float,     // 0-100
    val punteggioSchreiben: Float,  // 0-100
    val punteggioSprechen: Float,   // 0-100
    val punteggioComplessivo: Float // media
)

enum class SezioneTestB1 {
    LESEN, HOEREN, SCHREIBEN, SPRECHEN
}

@Serializable
data class DomandaTestB1(
    val id: String,
    val sezione: SezioneTestB1,
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    val spiegazione: String,
    /** Testo di lettura (tedesco) per la sezione LESEN. */
    val testoLettura: String? = null
)

object TestB1Data {
    
    /** Testo di lettura condiviso dalla sezione LESEN (formato Goethe Lesen). */
    val TESTO_LESEN_B1 = """Deutschland ist ein Land mit vielen Gesichtern. Die meisten Menschen \
        |leben in Städten, aber auch auf dem Land gibt es schöne Dörfer. Viele Deutsche \
        |sprechen Englisch, besonders die jüngere Generation. In den Schulen lernen die \
        |Kinder schon früh Fremdsprachen. Das Leben in Deutschland ist nicht billig, \
        |aber die Löhne sind im Vergleich zu vielen anderen Ländern recht hoch. \
        |Besonders beliebt sind die zahlreichen Feiertage und die Tradition, im Sommer \
        |viel Zeit draußen zu verbringen. Viele Familien machen Urlaub in den Bergen \
        |oder an der Nordsee.""".trimMargin()

    val domandeLesen: List<DomandaTestB1> = listOf(
        DomandaTestB1(
            id = "l1",
            sezione = SezioneTestB1.LESEN,
            domanda = "Was ist der Hauptgedanke des Textes?",
            opzioni = listOf(
                "Deutschland ist ein schönes Land",
                "Deutschland hat viele Probleme",
                "Deutschland ist teuer",
                "Deutschland ist klein"
            ),
            rispostaCorretta = 0,
            spiegazione = "Der Text beschreibt die positiven Aspekte Deutschlands.",
            testoLettura = TESTO_LESEN_B1
        ),
        DomandaTestB1(
            id = "l2",
            sezione = SezioneTestB1.LESEN,
            domanda = "Welche Aussage ist richtig?",
            opzioni = listOf(
                "Alle Deutschen sprechen Englisch",
                "Viele Deutsche sprechen Englisch",
                "Kein Deutscher spricht Englisch",
                "Deutsche lernen nie Englisch"
            ),
            rispostaCorretta = 1,
            spiegazione = "Nicht alle, aber viele Deutsche sprechen Englisch.",
            testoLettura = TESTO_LESEN_B1
        ),
        DomandaTestB1(
            id = "l3",
            sezione = SezioneTestB1.LESEN,
            domanda = "Was bedeutet 'Bildung'?",
            opzioni = listOf(
                "Bildung bedeutet nur Schule",
                "Bildung umfasst Schule, Studium und Weiterbildung",
                "Bildung ist nur für Kinder",
                "Bildung ist unwichtig"
            ),
            rispostaCorretta = 1,
            spiegazione = "Bildung umfasst alle Formen des Lernens.",
            testoLettura = TESTO_LESEN_B1
        ),
        DomandaTestB1(
            id = "l4",
            sezione = SezioneTestB1.LESEN,
            domanda = "Welches Wort ist ein Synonym für 'schnell'?",
            opzioni = listOf("langsam", "rasch", "groß", "klein"),
            rispostaCorretta = 1,
            spiegazione = "'Rasch' ist ein Synonym für 'schnell'.",
            testoLettura = TESTO_LESEN_B1
        ),
        DomandaTestB1(
            id = "l5",
            sezione = SezioneTestB1.LESEN,
            domanda = "Was ist der Unterschied zwischen 'können' und 'dürfen'?",
            opzioni = listOf(
                "Es gibt keinen Unterschied",
                "'können' = Fähigkeit, 'dürfen' = Erlaubnis",
                "'können' = Erlaubnis, 'dürfen' = Fähigkeit",
                "Beide bedeuten 'müssen'"
            ),
            rispostaCorretta = 1,
            spiegazione = "'können' drückt Fähigkeit aus, 'dürfen' Erlaubnis.",
            testoLettura = TESTO_LESEN_B1
        )
    )
    
    val domandeHoeren: List<DomandaTestB1> = listOf(
        DomandaTestB1(
            id = "h1",
            sezione = SezioneTestB1.HOEREN,
            domanda = "Was hören Sie im Dialog?",
            opzioni = listOf(
                "Ein Termin beim Arzt",
                "Eine Party",
                "Ein Kino",
                "Ein Restaurant"
            ),
            rispostaCorretta = 0,
            spiegazione = "Der Dialog handelt von einem Arzttermin."
        ),
        DomandaTestB1(
            id = "h2",
            sezione = SezioneTestB1.HOEREN,
            domanda = "Wann trifft sich die Gruppe?",
            opzioni = listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag"),
            rispostaCorretta = 2,
            spiegazione = "Die Gruppe trifft sich am Mittwoch."
        ),
        DomandaTestB1(
            id = "h3",
            sezione = SezioneTestB1.HOEREN,
            domanda = "Was bedeutet 'sich verspäten'?",
            opzioni = listOf(
                "zu früh kommen",
                "zu spät kommen",
                "pünktlich sein",
                "sich beeilen"
            ),
            rispostaCorretta = 1,
            spiegazione = "'Sich verspäten' bedeutet zu spät kommen."
        ),
        DomandaTestB1(
            id = "h4",
            sezione = SezioneTestB1.HOEREN,
            domanda = "Welches Wort passt? 'Ich ___ dich morgen anrufen.'",
            opzioni = listOf("werde", "bin", "habe", "kann"),
            rispostaCorretta = 0,
            spiegazione = "Futur mit 'werden'."
        ),
        DomandaTestB1(
            id = "h5",
            sezione = SezioneTestB1.HOEREN,
            domanda = "Was ist das Gegenteil von 'anfangen'?",
            opzioni = listOf("aufhören", "weitermachen", "beginnen", "machen"),
            rispostaCorretta = 0,
            spiegazione = "'Aufhören' ist das Gegenteil von 'anfangen'."
        )
    )
    
    val domandeSchreiben: List<DomandaTestB1> = listOf(
        DomandaTestB1(
            id = "s1",
            sezione = SezioneTestB1.SCHREIBEN,
            domanda = "Welche Formulierung ist höflich?",
            opzioni = listOf(
                "Gib mir das Buch!",
                "Könntest du mir bitte das Buch geben?",
                "Ich will das Buch!",
                "Das Buch ist meins!"
            ),
            rispostaCorretta = 1,
            spiegazione = "Konjunktiv II + 'bitte' = höfliche Bitte."
        ),
        DomandaTestB1(
            id = "s2",
            sezione = SezioneTestB1.SCHREIBEN,
            domanda = "Welcher Satz ist grammatikalisch korrekt?",
            opzioni = listOf(
                "Ich habe gestern ins Kino gegangen",
                "Ich bin gestern ins Kino gegangen",
                "Ich habe gestern ins Kino gehen",
                "Ich bin gestern ins Kino gehen"
            ),
            rispostaCorretta = 1,
            spiegazione = "Perfekt mit 'sein' + Partizip II."
        ),
        DomandaTestB1(
            id = "s3",
            sezione = SezioneTestB1.SCHREIBEN,
            domanda = "Welche Konjunktion passt? 'Ich lerne Deutsch, ___ ich in Deutschland arbeiten will.'",
            opzioni = listOf("weil", "dass", "obwohl", "wenn"),
            rispostaCorretta = 0,
            spiegazione = "'weil' introduce una causa."
        ),
        DomandaTestB1(
            id = "s4",
            sezione = SezioneTestB1.SCHREIBEN,
            domanda = "Wie schreibt man eine formelle E-Mail?",
            opzioni = listOf(
                "Mit 'Hallo' und 'Tschüss'",
                "Mit 'Sehr geehrte Damen und Herren' und 'Mit freundlichen Grüßen'",
                "Mit 'Hey' und 'Bis bald'",
                "Mit 'Liebe' und 'Alles Liebe'"
            ),
            rispostaCorretta = 1,
            spiegazione = "Formelle E-Mails beginnen mit 'Sehr geehrte Damen und Herren'."
        ),
        DomandaTestB1(
            id = "s5",
            sezione = SezioneTestB1.SCHREIBEN,
            domanda = "Welches Wort ist ein Fehler? 'Ich habe gestern ein Buch gekauft.'",
            opzioni = listOf(
                "Ich",
                "habe",
                "gestern",
                "kein Fehler"
            ),
            rispostaCorretta = 3,
            spiegazione = "Der Satz ist korrekt."
        )
    )
    
    val domandeSprechen: List<DomandaTestB1> = listOf(
        DomandaTestB1(
            id = "sp1",
            sezione = SezioneTestB1.SPRECHEN,
            domanda = "Wie beschreibt man ein Bild?",
            opzioni = listOf(
                "Mit 'Ich sehe...' und 'Da ist...'",
                "Mit 'Ich denke...' und 'Ich glaube...'",
                "Mit 'Ich will...' und 'Ich muss...'",
                "Mit 'Ich habe...' und 'Ich bin...'"
            ),
            rispostaCorretta = 0,
            spiegazione = "Beschreibungen verwenden 'Ich sehe...' und 'Da ist...'."
        ),
        DomandaTestB1(
            id = "sp2",
            sezione = SezioneTestB1.SPRECHEN,
            domanda = "Wie äußert man eine Meinung?",
            opzioni = listOf(
                "Mit 'Ich finde...' und 'Meiner Meinung nach...'",
                "Mit 'Ich weiß...' und 'Ich glaube...'",
                "Mit 'Ich will...' und 'Ich muss...'",
                "Mit 'Ich habe...' und 'Ich bin...'"
            ),
            rispostaCorretta = 0,
            spiegazione = "Meinungen äußert man mit 'Ich finde...'."
        ),
        DomandaTestB1(
            id = "sp3",
            sezione = SezioneTestB1.SPRECHEN,
            domanda = "Wie bittet man um etwas?",
            opzioni = listOf(
                "Mit 'Könnten Sie...' und 'Würden Sie...'",
                "Mit 'Gib mir...' und 'Ich will...'",
                "Mit 'Ich muss...' und 'Ich soll...'",
                "Mit 'Ich habe...' und 'Ich bin...'"
            ),
            rispostaCorretta = 0,
            spiegazione = "Höfliche Bitten verwenden Konjunktiv II."
        ),
        DomandaTestB1(
            id = "sp4",
            sezione = SezioneTestB1.SPRECHEN,
            domanda = "Wie verabschiedet man sich?",
            opzioni = listOf(
                "Mit 'Auf Wiedersehen' und 'Tschüss'",
                "Mit 'Hallo' und 'Hi'",
                "Mit 'Guten Morgen' und 'Gute Nacht'",
                "Mit 'Bis bald' und 'Mach's gut'"
            ),
            rispostaCorretta = 0,
            spiegazione = "Verabschiedungen: 'Auf Wiedersehen', 'Tschüss'."
        ),
        DomandaTestB1(
            id = "sp5",
            sezione = SezioneTestB1.SPRECHEN,
            domanda = "Welches Wort passt? 'Ich ___ gern nach Deutschland reisen.'",
            opzioni = listOf("würde", "werde", "würde", "werde"),
            rispostaCorretta = 0,
            spiegazione = "Konjunktiv II per desideri irreali."
        )
    )
    
    fun tutteLeDomande(): List<DomandaTestB1> {
        return domandeLesen + domandeHoeren + domandeSchreiben + domandeSprechen
    }
    
    fun domandePerSezione(sezione: SezioneTestB1): List<DomandaTestB1> {
        return when (sezione) {
            SezioneTestB1.LESEN -> domandeLesen
            SezioneTestB1.HOEREN -> domandeHoeren
            SezioneTestB1.SCHREIBEN -> domandeSchreiben
            SezioneTestB1.SPRECHEN -> domandeSprechen
        }
    }
}
