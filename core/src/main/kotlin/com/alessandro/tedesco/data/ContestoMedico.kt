package com.alessandro.tedesco.data

import kotlinx.serialization.Serializable

@Serializable
data class ParolaMedica(
    val id: String,
    val german: String,
    val italian: String,
    val example: String,
    val article: String = "",
    val pronunciation: String = "",
    val categoria: String // "anatomia", "sintomi", "trattamento", "attrezzatura", "ufficio"
)

@Serializable
data class ScenarioMedico(
    val id: String,
    val titolo: String,
    val descrizione: String,
    val ruolo: String, // "fisioterapista" o "paziente"
    val frasiChiave: List<String>,
    val traduzioni: List<String>
)

// Lessico medico per fisioterapia
val LESSICO_MEDICO = listOf(
    // Anatomia
    ParolaMedica("m1", "der Körper", "il corpo", "Der Körper ist gesund.", "der", "kör-per", "anatomia"),
    ParolaMedica("m2", "der Rücken", "la schiena", "Mein Rücken tut weh.", "der", "rü-ken", "anatomia"),
    ParolaMedica("m3", "die Schulter", "la spalla", "Die Schulter ist steif.", "die", "shu-ler", "anatomia"),
    ParolaMedica("m4", "das Knie", "il ginocchio", "Das Knie ist geschwollen.", "das", "kni", "anatomia"),
    ParolaMedica("m5", "der Arm", "il braccio", "Der Arm ist stark.", "der", "arm", "anatomia"),
    ParolaMedica("m6", "die Hand", "la mano", "Die Hand ist kalt.", "die", "hant", "anatomia"),
    ParolaMedica("m7", "der Fuß", "il piede", "Der Fuß tut weh.", "der", "fus", "anatomia"),
    ParolaMedica("m8", "das Bein", "la gamba", "Das Bein ist lang.", "das", "bain", "anatomia"),
    ParolaMedica("m9", "der Hals", "il collo", "Der Hals ist steif.", "der", "hals", "anatomia"),
    ParolaMedica("m10", "der Bauch", "la pancia", "Der Bauch tut weh.", "der", "bauch", "anatomia"),
    
    // Sintomi
    ParolaMedica("m11", "die Schmerzen", "il dolore", "Ich habe Schmerzen.", "die", "schmer-tsen", "sintomi"),
    ParolaMedica("m12", "die Schwellung", "il gonfiore", "Die Schwellung ist groß.", "die", "schwel-lung", "sintomi"),
    ParolaMedica("m13", "die Steifheit", "la rigidità", "Die Steifheit ist stark.", "die", "shtai-fig-kait", "sintomi"),
    ParolaMedica("m14", "die Müdigkeit", "la stanchezza", "Die Müdigkeit ist groß.", "die", "mü-dig-kait", "sintomi"),
    ParolaMedica("m15", "der Schwindel", "il capogiro", "Ich habe Schwindel.", "der", "schvin-del", "sintomi"),
    ParolaMedica("m16", "die Übelkeit", "la nausea", "Die Übelkeit ist stark.", "die", "ü-bel-kait", "sintomi"),
    ParolaMedica("m17", "der Husten", "la tosse", "Der Husten ist stark.", "der", "hus-ten", "sintomi"),
    ParolaMedica("m18", "die Atemnot", "il respiro corto", "Die Atemnot ist stark.", "die", "a-tem-not", "sintomi"),
    ParolaMedica("m19", "die Taubheit", "l'intorpidimento", "Die Taubheit ist stark.", "die", "taub-hait", "sintomi"),
    ParolaMedica("m20", "die Rötung", "il rossore", "Die Rötung ist sichtbar.", "die", "rö-tung", "sintomi"),
    
    // Trattamento
    ParolaMedica("m21", "die Therapie", "la terapia", "Die Therapie hilft.", "die", "te-ra-pi", "trattamento"),
    ParolaMedica("m22", "die Übung", "l'esercizio", "Die Übung ist wichtig.", "die", "ü-bung", "trattamento"),
    ParolaMedica("m23", "die Massage", "il massaggio", "Die Massage ist gut.", "die", "ma-sa-sche", "trattamento"),
    ParolaMedica("m24", "die Bewegung", "il movimento", "Die Bewegung ist wichtig.", "die", "be-ve-gung", "trattamento"),
    ParolaMedica("m25", "die Dehnung", "lo stretching", "Die Dehnung ist gut.", "die", "de-nung", "trattamento"),
    ParolaMedica("m26", "die Stärkung", "il rafforzamento", "Die Stärkung ist wichtig.", "die", "shtär-kung", "trattamento"),
    ParolaMedica("m27", "die Entspannung", "il rilassamento", "Die Entspannung ist gut.", "die", "ents-pa-nung", "trattamento"),
    ParolaMedica("m28", "die Wärme", "il calore", "Die Wärme hilft.", "die", "vär-me", "trattamento"),
    ParolaMedica("m29", "die Kälte", "il freddo", "Die Kälte hilft.", "die", "kel-te", "trattamento"),
    ParolaMedica("m30", "die Elektrotherapie", "l'elettroterapia", "Die Elektrotherapie hilft.", "die", "e-lek-tro-te-ra-pi", "trattamento"),
    
    // Attrezzatura
    ParolaMedica("m31", "das Therapiebett", "il lettino terapeutico", "Das Therapiebett ist bequem.", "das", "te-ra-pi-bet", "attrezzatura"),
    ParolaMedica("m32", "der Therapieball", "la palla terapeutica", "Der Therapieball ist rund.", "der", "te-ra-pi-bal", "attrezzatura"),
    ParolaMedica("m33", "das Theraband", "l'elastico terapeutico", "Das Theraband ist elastisch.", "das", "te-ra-pi-bant", "attrezzatura"),
    ParolaMedica("m34", "die Matte", "il tappetino", "Die Matte ist weich.", "die", "mat-te", "attrezzatura"),
    ParolaMedica("m35", "das Gewicht", "il peso", "Das Gewicht ist schwer.", "das", "ge-vicht", "attrezzatura"),
    ParolaMedica("m36", "der Roller", "il rullo", "Der Roller ist weich.", "der", "rol-ler", "attrezzatura"),
    ParolaMedica("m37", "die Liege", "il lettino", "Die Liege ist bequem.", "die", "li-sche", "attrezzatura"),
    ParolaMedica("m38", "der Stuhl", "la sedia", "Der Stuhl ist stabil.", "der", "shtul", "attrezzatura"),
    ParolaMedica("m39", "das Kissen", "il cuscino", "Das Kissen ist weich.", "das", "ki-ssen", "attrezzatura"),
    ParolaMedica("m40", "die Decke", "la coperta", "Die Decke ist warm.", "die", "de-ke", "attrezzatura"),
    
    // Ufficio
    ParolaMedica("m41", "das Behandlungszimmer", "lo studio di cura", "Das Behandlungszimmer ist groß.", "das", "be-hand-lungs-tsim-mer", "ufficio"),
    ParolaMedica("m42", "das Wartezimmer", "la sala d'attesa", "Das Wartezimmer ist voll.", "das", "var-te-tsim-mer", "ufficio"),
    ParolaMedica("m43", "die Sprechstunde", "l'orario di visita", "Die Sprechstunde ist von 9 bis 17 Uhr.", "die", "shprech-stun-de", "ufficio"),
    ParolaMedica("m44", "die Anmeldung", "la registrazione", "Die Anmeldung ist wichtig.", "die", "an-mel-dung", "ufficio"),
    ParolaMedica("m45", "die Versicherung", "l'assicurazione", "Die Versicherung ist wichtig.", "die", "fer-zi-che-rung", "ufficio"),
    ParolaMedica("m46", "die Rechnung", "il conto", "Die Rechnung ist hoch.", "die", "rech-nung", "ufficio"),
    ParolaMedica("m47", "die Termin", "l'appuntamento", "Der Termin ist um 10 Uhr.", "die", "ter-min", "ufficio"),
    ParolaMedica("m48", "die Diagnose", "la diagnosi", "Die Diagnose ist klar.", "die", "di-ag-no-se", "ufficio"),
    ParolaMedica("m49", "die Verordnung", "la prescrizione", "Die Verordnung ist wichtig.", "die", "fer-ord-nung", "ufficio"),
    ParolaMedica("m50", "die Dokumentation", "la documentazione", "Die Dokumentation ist wichtig.", "die", "do-ku-men-ta-tsi-on", "ufficio")
)

// Scenari di roleplay per fisioterapia
val SCENARI_MEDICI = listOf(
    ScenarioMedico(
        id = "s1",
        titolo = "Primo colloquio",
        descrizione = "Un paziente entra nel tuo studio per la prima volta. Presentati e chiedi come sta.",
        ruolo = "fisioterapista",
        frasiChiave = listOf(
            "Guten Tag, ich bin die Physiotherapeutin.",
            "Wie kann ich Ihnen helfen?",
            "Wo haben Sie Schmerzen?",
            "Seit wann haben Sie diese Schmerzen?",
            "Ich werde Sie jetzt untersuchen."
        ),
        traduzioni = listOf(
            "Buongiorno, sono la fisioterapista.",
            "Come posso aiutarla?",
            "Dove ha dolore?",
            "Da quando ha questo dolore?",
            "Ora la esaminerò."
        )
    ),
    ScenarioMedico(
        id = "s2",
        titolo = "Valutazione del dolore",
        descrizione = "Il paziente ha dolori alla schiena. Chiedi dettagli e spiega cosa farai.",
        ruolo = "fisioterapista",
        frasiChiave = listOf(
            "Können Sie den Schmerz beschreiben?",
            "Ist der Schmerz stechend oder dumpf?",
            "Wann ist der Schmerz am schlimmsten?",
            "Ich werde jetzt Ihren Rücken untersuchen.",
            "Bitte atmen Sie tief ein."
        ),
        traduzioni = listOf(
            "Può descrivere il dolore?",
            "Il dolore è acuto o sordo?",
            "Quando è peggiore?",
            "Ora esaminerò la sua schiena.",
            "Per favore respiri profondamente."
        )
    ),
    ScenarioMedico(
        id = "s3",
        titolo = "Esercizi di rilassamento",
        descrizione = "Il paziente è teso. Guida alcuni esercizi di rilassamento.",
        ruolo = "fisioterapista",
        frasiChiave = listOf(
            "Bitte entspannen Sie sich.",
            "Atmen Sie tief ein und aus.",
            "Spüren Sie die Entspannung.",
            "Die Schultern nach unten.",
            "Gut gemacht!"
        ),
        traduzioni = listOf(
            "Per favore si rilassi.",
            "Respiri profondamente dentro e fuori.",
            "Senta il rilassamento.",
            "Le spalle giù.",
            "Ben fatto!"
        )
    ),
    ScenarioMedico(
        id = "s4",
        titolo = "Spiegazione del trattamento",
        descrizione = "Spiega al paziente il piano di trattamento.",
        ruolo = "fisioterapista",
        frasiChiave = listOf(
            "Ich werde Ihnen eine Übung zeigen.",
            "Bitte machen Sie die Übung langsam.",
            "Wiederholen Sie die Übung zehnmal.",
            "Haben Sie Schmerzen?",
            "Das war gut!"
        ),
        traduzioni = listOf(
            "Le mostrerò un esercizio.",
            "Per favore faccia l'esercizio lentamente.",
            "Ripeta l'esercizio dieci volte.",
            "Ha dolore?",
            "Era buono!"
        )
    ),
    ScenarioMedico(
        id = "s5",
        titolo = "Fine della sessione",
        descrizione = "La sessione è finita. Saluta il paziente e dai istruzioni per casa.",
        ruolo = "fisioterapista",
        frasiChiave = listOf(
            "Das war alles für heute.",
            "Bitte machen Sie die Übungen zu Hause.",
            "Kommen Sie nächste Woche wieder.",
            "Gute Besserung!",
            "Auf Wiedersehen!"
        ),
        traduzioni = listOf(
            "Questo è tutto per oggi.",
            "Per favore faccia gli esercizi a casa.",
            "Torni la prossima settimana.",
            "Guarisca bene!",
            "Arrivederci!"
        )
    )
)
