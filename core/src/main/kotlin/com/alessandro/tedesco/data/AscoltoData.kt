package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ProfiloUtente
import kotlinx.serialization.Serializable

/**
 * Esercizi di ascolto (Hörverstehen) basati su TTS.
 * Ogni esercizio contiene una frase/dialogo da ascoltare in tedesco
 * e una domanda di comprensione con 4 opzioni.
 */
@Serializable
data class EsercizioAscolto(
    val id: String,
    val fraseTedesca: String,
    val traduzioneItaliana: String,
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    val spiegazione: String,
    val livello: String = "A1"
)

object AscoltoData {

    val esercizi: List<EsercizioAscolto> = listOf(
        // === A1: Frasi semplici ===
        EsercizioAscolto(
            id = "a1_1",
            fraseTedesca = "Ich heiße Marco und ich komme aus Italien.",
            traduzioneItaliana = "Mi chiamo Marco e vengo dall'Italia.",
            domanda = "Da dove viene Marco?",
            opzioni = listOf("Dall'Italia", "Dalla Germania", "Dall'Austria", "Dalla Svizzera"),
            rispostaCorretta = 0,
            spiegazione = "'Ich komme aus Italien' significa 'vengo dall'Italia'.",
            livello = "A1"
        ),
        EsercizioAscolto(
            id = "a1_2",
            fraseTedesca = "Es ist drei Uhr nachmittags.",
            traduzioneItaliana = "Sono le tre del pomeriggio.",
            domanda = "Che ore sono?",
            opzioni = listOf("Le tre", "Le due", "Le quattro", "Le cinque"),
            rispostaCorretta = 0,
            spiegazione = "'Es ist drei Uhr' = 'sono le tre'.",
            livello = "A1"
        ),
        EsercizioAscolto(
            id = "a1_3",
            fraseTedesca = "Ich habe zwei Brüder und eine Schwester.",
            traduzioneItaliana = "Ho due fratelli e una sorella.",
            domanda = "Quanti fratelli ha il parlante?",
            opzioni = listOf("Due", "Tre", "Uno", "Nessuno"),
            rispostaCorretta = 0,
            spiegazione = "'Zwei Brüder' = due fratelli.",
            livello = "A1"
        ),
        EsercizioAscolto(
            id = "a1_4",
            fraseTedesca = "Das Wetter ist heute sehr schön.",
            traduzioneItaliana = "Il tempo è oggi molto bello.",
            domanda = "Com'è il tempo oggi?",
            opzioni = listOf("Bello", "Brutto", "Freddo", "Piovoso"),
            rispostaCorretta = 0,
            spiegazione = "'Sehr schön' = molto bello.",
            livello = "A1"
        ),
        EsercizioAscolto(
            id = "a1_5",
            fraseTedesca = "Ich wohne in einer großen Wohnung in Berlin.",
            traduzioneItaliana = "Abito in un grande appartamento a Berlino.",
            domanda = "Dove abita il parlante?",
            opzioni = listOf("A Berlino", "A Monaco", "Amburgo", "A Vienna"),
            rispostaCorretta = 0,
            spiegazione = "'In Berlin' = a Berlino.",
            livello = "A1"
        ),
        EsercizioAscolto(
            id = "a1_6",
            fraseTedesca = "Ich trinke morgens immer Kaffee.",
            traduzioneItaliana = "Bevo sempre il caffè al mattino.",
            domanda = "Cosa beve il parlante al mattino?",
            opzioni = listOf("Caffè", "Tè", "Succo", "Latte"),
            rispostaCorretta = 0,
            spiegazione = "'Kaffee' = caffè.",
            livello = "A1"
        ),
        EsercizioAscolto(
            id = "a1_7",
            fraseTedesca = "Der Zug kommt um acht Uhr an.",
            traduzioneItaliana = "Il treno arriva alle otto.",
            domanda = "A che ora arriva il treno?",
            opzioni = listOf("Alle otto", "Alle sette", "Alle nove", "Alle dieci"),
            rispostaCorretta = 0,
            spiegazione = "'Um acht Uhr' = alle otto.",
            livello = "A1"
        ),
        EsercizioAscolto(
            id = "a1_8",
            fraseTedesca = "Ich gehe gern ins Kino.",
            traduzioneItaliana = "Mi piace andare al cinema.",
            domanda = "Cosa gli piace fare?",
            opzioni = listOf("Andare al cinema", "Andare al teatro", "Andare al museo", "Andare al ristorante"),
            rispostaCorretta = 0,
            spiegazione = "'Ins Kino gehen' = andare al cinema.",
            livello = "A1"
        ),

        // === A2: Dialoghi e frasi più complesse ===
        EsercizioAscolto(
            id = "a2_1",
            fraseTedesca = "Entschuldigung, wo ist der Bahnhof?",
            traduzioneItaliana = "Scusi, dov'è la stazione ferroviaria?",
            domanda = "Cosa sta cercando la persona?",
            opzioni = listOf("La stazione", "L'hotel", "Il ristorante", "Il supermercato"),
            rispostaCorretta = 0,
            spiegazione = "'Der Bahnhof' = la stazione ferroviaria.",
            livello = "A2"
        ),
        EsercizioAscolto(
            id = "a2_2",
            fraseTedesca = "Ich habe gestern einen Film gesehen, der war sehr spannend.",
            traduzioneItaliana = "Ieri ho visto un film che era molto emozionante.",
            domanda = "Com'era il film?",
            opzioni = listOf("Emozionante", "Noioso", "Triste", "Divertente"),
            rispostaCorretta = 0,
            spiegazione = "'Spannend' = emozionante.",
            livello = "A2"
        ),
        EsercizioAscolto(
            id = "a2_3",
            fraseTedesca = "Könnten Sie mir bitte sagen, wie spät es ist?",
            traduzioneItaliana = "Può dirmi che ore sono?",
            domanda = "Cosa chiede la persona?",
            opzioni = listOf("L'ora", "La data", "Il giorno", "Il tempo"),
            rispostaCorretta = 0,
            spiegazione = "'Wie spät es ist' = che ore sono.",
            livello = "A2"
        ),
        EsercizioAscolto(
            id = "a2_4",
            fraseTedesca = "Ich habe meinen Schlüssel verloren.",
            traduzioneItaliana = "Ho perso le mie chiavi.",
            domanda = "Cosa ha perso il parlante?",
            opzioni = listOf("Le chiavi", "Il portafoglio", "Il telefono", "La borsa"),
            rispostaCorretta = 0,
            spiegazione = "'Schlüssel' = chiavi.",
            livello = "A2"
        ),
        EsercizioAscolto(
            id = "a2_5",
            fraseTedesca = "Wir treffen uns morgen um zwanzig Uhr.",
            traduzioneItaliana = "Incontriamo domani alle venti.",
            domanda = "A che ora si incontrano?",
            opzioni = listOf("Alle vento", "Alle otto", "Alle nove", "Alle dieci"),
            rispostaCorretta = 0,
            spiegazione = "'Um zwanzig Uhr' = alle venti (8 di sera).",
            livello = "A2"
        ),
        EsercizioAscolto(
            id = "a2_6",
            fraseTedesca = "Ich habe gestern Abend Pizza gegessen.",
            traduzioneItaliana = "Ieri sera ho mangiato la pizza.",
            domanda = "Cosa ha mangiato ieri sera?",
            opzioni = listOf("Pizza", "Pasta", "Insalata", "Zuppa"),
            rispostaCorretta = 0,
            spiegazione = "'Pizza' = pizza.",
            livello = "A2"
        ),
        EsercizioAscolto(
            id = "a2_7",
            fraseTedesca = "Der Film beginnt erst in zehn Minuten.",
            traduzioneItaliana = "Il film inizia tra dieci minuti.",
            domanda = "Quando inizia il film?",
            opzioni = listOf("Tra dieci minuti", "Adesso", "Tra un'ora", "Ieri"),
            rispostaCorretta = 0,
            spiegazione = "'In zehn Minuten' = tra dieci minuten.",
            livello = "A2"
        ),
        EsercizioAscolto(
            id = "a2_8",
            fraseTedesca = "Ich habe keine Zeit, ich muss arbeiten.",
            traduzioneItaliana = "Non ho tempo, devo lavorare.",
            domanda = "Perché non ha tempo?",
            opzioni = listOf("Deve lavorare", "Deve studiare", "Deve dormire", "Deve mangiare"),
            rispostaCorretta = 0,
            spiegazione = "'Ich muss arbeiten' = devo lavorare.",
            livello = "A2"
        ),

        // === B1: Dialoghi più lunghi e situazioni complesse ===
        EsercizioAscolto(
            id = "b1_1",
            fraseTedesca = "Ich habe gehört, dass du nächste Woche nach Deutschland reist. Wann fährst du ab?",
            traduzioneItaliana = "Ho sentito che la prossima settimana vai in Germania. Quando parti?",
            domanda = "Quando parte per la Germania?",
            opzioni = listOf("La prossima settimana", "Questo mese", "Il mese prossimo", "Domani"),
            rispostaCorretta = 0,
            spiegazione = "'Nächste Woche' = la prossima settimana.",
            livello = "B1"
        ),
        EsercizioAscolto(
            id = "b1_2",
            fraseTedesca = "Leider kann ich nicht kommen, ich habe schon andere Pläne.",
            traduzioneItaliana = "Purtroppo non posso venire, ho già altri programmi.",
            domanda = "Perché non può venire?",
            opzioni = listOf("Ha altri programmi", "È malato", "Non vuole", "Non ha tempo"),
            rispostaCorretta = 0,
            spiegazione = "'Ich habe schon andere Pläne' = ho già altri programmi.",
            livello = "B1"
        ),
        EsercizioAscolto(
            id = "b1_3",
            fraseTedesca = "Das Konzert war eigentlich schon ausverkauft, aber ich habe noch ein Ticket bekommen.",
            traduzioneItaliana = "Il concerto era in realtà già esaurito, ma ho ricevuto ancora un biglietto.",
            domanda = "Come ha ottenuto il biglietto?",
            opzioni = listOf("Qualcuno gli ha dato un biglietto", "Lo ha comprato online", "Lo ha vinto", "Lo ha trovato"),
            rispostaCorretta = 0,
            spiegazione = "'Ich habe noch ein Ticket bekommen' = ho ricevuto ancora un biglietto.",
            livello = "B1"
        ),
        EsercizioAscolto(
            id = "b1_4",
            fraseTedesca = "Ich bin gestern spät nach Hause gekommen, weil ich noch arbeiten musste.",
            traduzioneItaliana = "Ieri sono tornato a casa tardi perché dovevo ancora lavorare.",
            domanda = "Perché è tornato tardi?",
            opzioni = listOf("Doveva lavorare", "Era in ritardo", "Ha perso il treno", "Era al cinema"),
            rispostaCorretta = 0,
            spiegazione = "'Weil ich noch arbeiten musste' = perché dovevo ancora lavorare.",
            livello = "B1"
        ),
        EsercizioAscolto(
            id = "b1_5",
            fraseTedesca = "Hast du schon von dem neuen Restaurant gehört? Es soll sehr gut sein.",
            traduzioneItaliana = "Hai sentito parlare del nuovo ristorante? Dicono che sia molto buono.",
            domanda = "Cosa si dice del nuovo ristorante?",
            opzioni = listOf("È molto buono", "È molto costoso", "È molto lontano", "È chiuso"),
            rispostaCorretta = 0,
            spiegazione = "'Es soll sehr gut sein' = è molto buono (si dice).",
            livello = "B1"
        ),
        EsercizioAscolto(
            id = "b1_6",
            fraseTedesca = "Ich habe mein Handy verloren, kannst du mich bitte anrufen?",
            traduzioneItaliana = "Ho perso il telefono, puoi chiamarmi per favore?",
            domanda = "Cosa chiede di fare?",
            opzioni = listOf("Chiamarlo", "Mandargli un messaggio", "Aspettarlo", "Cercarlo"),
            rispostaCorretta = 0,
            spiegazione = "'Kannst du mich bitte anrufen?' = puoi chiamarmi per favore?",
            livello = "B1"
        ),
        EsercizioAscolto(
            id = "b1_7",
            fraseTedesca = "Der Zug hat zwanzig Minuten Verspätung, wir müssen noch warten.",
            traduzioneItaliana = "Il treno ha venti minuti di ritardo, dobbiamo ancora aspettare.",
            domanda = "Quanto ritardo ha il treno?",
            opzioni = listOf("Venti minuti", "Dieci minuti", "Trenta minuti", "Un'ora"),
            rispostaCorretta = 0,
            spiegazione = "'Zwanzig Minuten Verspätung' = venti minuti di ritardo.",
            livello = "B1"
        ),
        EsercizioAscolto(
            id = "b1_8",
            fraseTedesca = "Ich habe gestern ein Buch gekauft, das sehr interessant ist.",
            traduzioneItaliana = "Ieri ho comprato un libro che è molto interessante.",
            domanda = "Cosa ha comprato ieri?",
            opzioni = listOf("Un libro", "Un film", "Una rivista", "Un giornale"),
            rispostaCorretta = 0,
            spiegazione = "'Ein Buch' = un libro.",
            livello = "B1"
        )
    )

    fun eserciziPerLivello(livello: String, profilo: ProfiloUtente? = null): List<EsercizioAscolto> {
        val tutti = getTuttiEserciziAscolto(profilo)
        return tutti.filter { it.livello == livello }
    }

    fun livelliDisponibili(profilo: ProfiloUtente? = null): List<String> {
        val tutti = getTuttiEserciziAscolto(profilo)
        return tutti.map { it.livello }.distinct().sorted()
    }
}

// Esercizi A0 - Presentazioni (profile-aware)
fun getEserciziA0(profilo: ProfiloUtente?): List<EsercizioAscolto> {
    val base = listOf(
        EsercizioAscolto(
            id = "a0_1",
            fraseTedesca = if (profilo?.config?.mostraContestoMedico == true)
                "Ich heiße Alessandro."
            else
                "Ich heiße Emma.",
            traduzioneItaliana = if (profilo?.config?.mostraContestoMedico == true)
                "Mi chiamo Alessandro."
            else
                "Mi chiamo Emma.",
            domanda = "Come si chiama il parlante?",
            opzioni = if (profilo?.config?.mostraContestoMedico == true)
                listOf("Alessandro", "Marco", "Luca", "Paolo")
            else
                listOf("Emma", "Anna", "Julia", "Sofia"),
            rispostaCorretta = 0,
            spiegazione = if (profilo?.config?.mostraContestoMedico == true)
                "Il parlante dice 'Ich heiße Alessandro' (Mi chiamo Alessandro)"
            else
                "Il parlante dice 'Ich heiße Emma' (Mi chiamo Emma)",
            livello = "A0"
        ),
        EsercizioAscolto(
            id = "a0_2",
            fraseTedesca = "Ich komme aus Italien.",
            traduzioneItaliana = "Vengo dall'Italia.",
            domanda = "Da dove viene il parlante?",
            opzioni = listOf("Germania", "Italia", "Austria", "Svizzera"),
            rispostaCorretta = 1,
            spiegazione = "Il parlante dice 'Ich komme aus Italien' (Vengo dall'Italia)",
            livello = "A0"
        ),
        EsercizioAscolto(
            id = "a0_3",
            fraseTedesca = if (profilo?.config?.mostraContestoMedico == true)
                "Ich bin Physiotherapeut."
            else
                "Ich bin Studentin.",
            traduzioneItaliana = if (profilo?.config?.mostraContestoMedico == true)
                "Sono fisioterapeuta."
            else
                "Sono studentessa.",
            domanda = "Qual è il lavoro/studio del parlante?",
            opzioni = if (profilo?.config?.mostraContestoMedico == true)
                listOf("Medico", "Insegnante", "Fisioterapeuta", "Infermiere")
            else
                listOf("Studentessa", "Insegnante", "Medica", "Avvocata"),
            rispostaCorretta = if (profilo?.config?.mostraContestoMedico == true) 2 else 0,
            spiegazione = if (profilo?.config?.mostraContestoMedico == true)
                "Il parlante dice 'Ich bin Physiotherapeut' (Sono fisioterapeuta)"
            else
                "Il parlante dice 'Ich bin Studentin' (Sono studentessa)",
            livello = "A0"
        ),
        EsercizioAscolto(
            id = "a0_4",
            fraseTedesca = "Eins, zwei, drei, vier, fünf.",
            traduzioneItaliana = "Uno, due, tre, quattro, cinque.",
            domanda = "Quanti numeri sono stati detti?",
            opzioni = listOf("3", "5", "7", "9"),
            rispostaCorretta = 1,
            spiegazione = "Sono stati detti 5 numeri: eins, zwei, drei, vier, fünf",
            livello = "A0"
        ),
        EsercizioAscolto(
            id = "a0_5",
            fraseTedesca = "Das ist meine Mutter.",
            traduzioneItaliana = "Questa è mia madre.",
            domanda = "Chi è 'die Mutter'?",
            opzioni = listOf("la madre", "il padre", "la sorella", "il fratello"),
            rispostaCorretta = 0,
            spiegazione = "'Die Mutter' significa 'la madre'",
            livello = "A0"
        ),
        EsercizioAscolto(
            id = "a0_6",
            fraseTedesca = if (profilo?.config?.mostraContestoMedico == true)
                "Ich wohne in Bozen."
            else
                "Ich wohne in Berlin.",
            traduzioneItaliana = if (profilo?.config?.mostraContestoMedico == true)
                "Vivo a Bolzano."
            else
                "Vivo a Berlino.",
            domanda = "Dove vive il parlante?",
            opzioni = if (profilo?.config?.mostraContestoMedico == true)
                listOf("Milano", "Bozen", "Roma", "Venezia")
            else
                listOf("Monaco", "Berlino", "Amburgo", "Vienna"),
            rispostaCorretta = if (profilo?.config?.mostraContestoMedico == true) 1 else 1,
            spiegazione = if (profilo?.config?.mostraContestoMedico == true)
                "Il parlante dice 'Ich wohne in Bozen' (Vivo a Bolzano)"
            else
                "Il parlante dice 'Ich wohne in Berlin' (Vivo a Berlino)",
            livello = "A0"
        ),
        EsercizioAscolto(
            id = "a0_7",
            fraseTedesca = "Ich hätte gern ein Wasser, bitte.",
            traduzioneItaliana = "Vorrei un'acqua, per favore.",
            domanda = "Cosa vuole il parlante?",
            opzioni = listOf("caffè", "vino", "acqua", "succo"),
            rispostaCorretta = 2,
            spiegazione = "Il parlante dice 'ein Wasser' (un'acqua)",
            livello = "A0"
        ),
        EsercizioAscolto(
            id = "a0_8",
            fraseTedesca = "Guten Morgen!",
            traduzioneItaliana = "Buongiorno!",
            domanda = "Cosa dice il parlante?",
            opzioni = listOf("Buonanotte", "Buongiorno", "Arrivederci", "Buonasera"),
            rispostaCorretta = 1,
            spiegazione = "'Guten Morgen' significa 'Buongiorno'",
            livello = "A0"
        ),
        EsercizioAscolto(
            id = "a0_9",
            fraseTedesca = "Danke schön!",
            traduzioneItaliana = "Grazie mille!",
            domanda = "Cosa dice il parlante?",
            opzioni = listOf("Prego", "Grazie", "Scusa", "Per favore"),
            rispostaCorretta = 1,
            spiegazione = "'Danke schön' significa 'Grazie mille'",
            livello = "A0"
        ),
        EsercizioAscolto(
            id = "a0_10",
            fraseTedesca = "Ich lerne Deutsch.",
            traduzioneItaliana = "Imparo tedesco.",
            domanda = "Cosa fa il parlante?",
            opzioni = listOf("lavora", "impara tedesco", "dorme", "mangia"),
            rispostaCorretta = 1,
            spiegazione = "Il parlante dice 'Ich lerne Deutsch' (Imparo tedesco)",
            livello = "A0"
        )
    )
    return base
}

// Aggiungi esercizi A0 alla lista principale (profile-aware)
fun getTuttiEserciziAscolto(profilo: ProfiloUtente?): List<EsercizioAscolto> {
    // A0 dipende dal profilo (contesto medico); A1/A2/B1 sono la banca ricca
    // di AscoltoData. Prima qui c'era una seconda lista A1 ridotta (5 esercizi
    // con gli stessi id a1_1..a1_5 della banca vera): A2 e B1 non esistevano e
    // per tutti gli studenti restavano sempre le stesse 5 domande.
    return getEserciziA0(profilo) + AscoltoData.esercizi
}
