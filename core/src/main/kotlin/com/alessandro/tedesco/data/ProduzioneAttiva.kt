package com.alessandro.tedesco.data

import java.util.Locale

/**
 * Esercizi PRODUTTIVI: lo studente deve scrivere o ricostruire la frase, non
 * scegliere tra quattro opzioni. È il pezzo che mancava per il B1: il
 * riconoscimento (scelta multipla) non allena il recupero attivo.
 *
 * - TRADUZIONE: frase italiana -> frase tedesca
 * - CLOZE: frase tedesca con un buco -> parola mancante
 * - DETTATO: si ascolta la frase (TTS) e la si scrive
 * - RIORDINO: parole mescolate -> frase corretta
 */
enum class TipoProduttivo(val etichetta: String, val descrizione: String) {
    TRADUZIONE("Traduzione", "Traduci la frase in tedesco"),
    CLOZE("Completa", "Scrivi la parola che manca"),
    DETTATO("Dettato", "Ascolta e scrivi la frase"),
    RIORDINO("Riordina", "Metti le parole nell'ordine giusto")
}

data class EsercizioProduttivo(
    val id: String,
    val tipo: TipoProduttivo,
    val consegna: String,
    /** Testo mostrato: frase italiana, frase con ___ oppure frase da ascoltare. */
    val prompt: String,
    /** Risposta canonica. */
    val risposta: String,
    /** Varianti accettate (es. "Ich heisse" senza ß). */
    val alternative: List<String> = emptyList(),
    val spiegazione: String = "",
    val livello: String = "A1",
    /** Solo RIORDINO: parole nella forma corretta (mescolate dalla UI). */
    val parole: List<String> = emptyList(),
    /** Solo DETTATO: frase completa da leggere con il TTS. */
    val testoAscolto: String? = null
) {
    /** Testo da pronunciare con il TTS, se l'esercizio è orale. */
    val daAscoltare: String? get() = testoAscolto ?: if (tipo == TipoProduttivo.DETTATO) prompt else null
}

/**
 * Confronto tollerante ma non troppo: ignora maiuscole, punteggiatura e
 * spazi; accetta le varianti ae/oe/ue/ss al posto di ä/ö/ü/ß (tastiere
 * italiane). NON accetta parole diverse o articoli sbagliati.
 */
object RisposteProduttive {

    private val punteggiatura = Regex("[.,!?;:\"'„“”’()\\[\\]{}–—-]")
    private val spazi = Regex("\\s+")

    fun normalizza(testo: String): String = testo
        .trim()
        .lowercase(Locale.GERMAN)
        .replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("ß", "ss")
        .replace(punteggiatura, " ")
        .replace(spazi, " ")
        .trim()

    /** true se [input] coincide con la risposta o con una variante accettata. */
    fun valuta(input: String, esercizio: EsercizioProduttivo): Boolean {
        val normalizzato = normalizza(input)
        if (normalizzato.isEmpty()) return false
        return (listOf(esercizio.risposta) + esercizio.alternative)
            .any { normalizza(it) == normalizzato }
    }

    /** Testo da mostrare quando la risposta è sbagliata. */
    fun rispostaMostrata(esercizio: EsercizioProduttivo): String {
        val varianti = esercizio.alternative
            .filter { normalizza(it) != normalizza(esercizio.risposta) }
        return if (varianti.isEmpty()) esercizio.risposta
        else "${esercizio.risposta}  (accettate anche: ${varianti.joinToString(" / ")})"
    }
}

/**
 * Banca curata di esercizi produttivi: funziona OFFLINE e dà subito materiale
 * corretto anche quando l'AI non risponde. La generazione AI la estende.
 */
object BancaProduttiva {

    private fun tr(
        id: String,
        livello: String,
        italiano: String,
        tedesco: String,
        vararg alternative: String
    ) = EsercizioProduttivo(
        id = id,
        tipo = TipoProduttivo.TRADUZIONE,
        consegna = "Traduci in tedesco",
        prompt = italiano,
        risposta = tedesco,
        alternative = alternative.toList(),
        spiegazione = "Confronta la tua frase con la soluzione: controlla verbo, articoli e ordine delle parole.",
        livello = livello
    )

    private fun cl(
        id: String,
        livello: String,
        frase: String,
        parola: String,
        spiegazione: String,
        vararg alternative: String
    ) = EsercizioProduttivo(
        id = id,
        tipo = TipoProduttivo.CLOZE,
        consegna = "Completa la frase",
        prompt = frase,
        risposta = parola,
        alternative = alternative.toList(),
        spiegazione = spiegazione,
        livello = livello
    )

    private fun de(id: String, livello: String, frase: String) = EsercizioProduttivo(
        id = id,
        tipo = TipoProduttivo.DETTATO,
        consegna = "Ascolta e scrivi la frase",
        prompt = "Premi Play, ascolta e scrivi esattamente quello che senti.",
        risposta = frase,
        spiegazione = "Riascolta e confronta parola per parola. Attenzione a maiuscole, umlaut e verbi separabili.",
        livello = livello,
        testoAscolto = frase
    )

    private fun ri(id: String, livello: String, frase: String, parole: List<String>) = EsercizioProduttivo(
        id = id,
        tipo = TipoProduttivo.RIORDINO,
        consegna = "Riordina le parole",
        prompt = "Tocca le parole nell'ordine giusto per formare una frase corretta.",
        risposta = parole.joinToString(" "),
        spiegazione = "In tedesco il verbo coniugato sta in seconda posizione nelle frasi principali.",
        livello = livello,
        parole = parole
    )

    private val traduzioni = listOf(
        // A1
        tr("tr_a1_1", "A1", "Mi chiamo Anna.", "Ich heiße Anna.", "Ich heisse Anna."),
        tr("tr_a1_2", "A1", "Ho 20 anni.", "Ich bin 20 Jahre alt."),
        tr("tr_a1_3", "A1", "Vivo a Bressanone.", "Ich wohne in Brixen.", "Ich wohne in Bressanone."),
        tr("tr_a1_4", "A1", "Ho mal di testa.", "Ich habe Kopfschmerzen."),
        tr("tr_a1_5", "A1", "Il libro è sul tavolo.", "Das Buch liegt auf dem Tisch."),
        tr("tr_a1_6", "A1", "Domani vado al lavoro.", "Morgen gehe ich zur Arbeit."),
        // A2
        tr("tr_a2_1", "A2", "Ieri sono andato al cinema.", "Gestern bin ich ins Kino gegangen."),
        tr("tr_a2_2", "A2", "Ho comprato un biglietto.", "Ich habe eine Fahrkarte gekauft.", "Ich habe ein Ticket gekauft."),
        tr("tr_a2_3", "A2", "Puoi aiutarmi, per favore?", "Kannst du mir bitte helfen?"),
        tr("tr_a2_4", "A2", "Mi fa male il ginocchio da tre giorni.", "Mein Knie tut seit drei Tagen weh.", "Ich habe seit drei Tagen Knieschmerzen."),
        tr("tr_a2_5", "A2", "Quando arriva il treno?", "Wann kommt der Zug an?"),
        tr("tr_a2_6", "A2", "Vorrei fissare un appuntamento.", "Ich möchte einen Termin vereinbaren."),
        // B1
        tr("tr_b1_1", "B1", "Se avessi più tempo, farei più sport.", "Wenn ich mehr Zeit hätte, würde ich mehr Sport machen."),
        tr("tr_b1_2", "B1", "Il paziente deve essere visitato dal medico.", "Der Patient muss vom Arzt untersucht werden."),
        tr("tr_b1_3", "B1", "Nonostante il dolore, è venuto al lavoro.", "Trotz der Schmerzen ist er zur Arbeit gekommen.", "Obwohl er Schmerzen hatte, ist er zur Arbeit gekommen."),
        tr("tr_b1_4", "B1", "Mi interessa lavorare in una clinica.", "Ich interessiere mich für die Arbeit in einer Klinik."),
        tr("tr_b1_5", "B1", "Mi può dire dove si trova la farmacia?", "Können Sie mir sagen, wo die Apotheke ist?"),
        tr("tr_b1_6", "B1", "È importante che tu faccia gli esercizi ogni giorno.", "Es ist wichtig, dass du die Übungen jeden Tag machst.")
    )

    private val cloze = listOf(
        // A1
        cl("cl_a1_1", "A1", "Ich ___ Anna.", "heiße", "Verbo sein? No: 'heißen' alla prima persona -> heiße.", "heisse"),
        cl("cl_a1_2", "A1", "___ Buch ist neu.", "Das", "'Buch' è neutro: das Buch."),
        cl("cl_a1_3", "A1", "Ich ___ 20 Jahre alt.", "bin", "'sein' alla prima persona: ich bin."),
        cl("cl_a1_4", "A1", "Wir ___ in Brixen.", "wohnen", "'wohnen' alla prima persona plurale: wir wohnen."),
        cl("cl_a1_5", "A1", "Er ___ einen Hund.", "hat", "'haben' alla terza persona singolare: er hat."),
        cl("cl_a1_6", "A1", "___ Sie bitte langsam!", "Sprechen", "Imperativo formale di 'sprechen': Sprechen Sie!"),
        // A2
        cl("cl_a2_1", "A2", "Ich ___ gestern ins Kino gegangen.", "bin", "Perfetto con 'sein' per i verbi di movimento: ich bin gegangen."),
        cl("cl_a2_2", "A2", "Sie hat eine Fahrkarte ___.", "gekauft", "Perfetto con 'haben' + participio: hat ... gekauft."),
        cl("cl_a2_3", "A2", "Ich warte ___ den Bus.", "auf", "'warten auf' + accusativo: auf den Bus."),
        cl("cl_a2_4", "A2", "Er freut sich ___ das Wochenende.", "auf", "'sich freuen auf' + accusativo: auf das Wochenende."),
        cl("cl_a2_5", "A2", "Das ist der Mann, ___ ich gestern getroffen habe.", "den", "Pronome relativo all'accusativo maschile: den."),
        cl("cl_a2_6", "A2", "Wenn es morgen regnet, ___ ich zu Hause.", "bleibe", "Frase con 'wenn': il verbo coniugato resta in seconda posizione nella principale."),
        // B1
        cl("cl_b1_1", "B1", "Das Buch ___ von vielen Menschen gelesen.", "wird", "Passivo presente: das Buch wird gelesen."),
        cl("cl_b1_2", "B1", "Die Hausaufgaben müssen morgen ___ werden.", "gemacht", "Passivo con modale: müssen ... gemacht werden."),
        cl("cl_b1_3", "B1", "Ich lerne Deutsch, ___ ich in Brixen arbeiten will.", "weil", "'weil' introduce la causa e manda il verbo in fondo."),
        cl("cl_b1_4", "B1", "___ es regnet, gehen wir spazieren.", "Obwohl", "'obwohl' introduce una concessione: nonostante piova."),
        cl("cl_b1_5", "B1", "Der Mann, ___ Auto gestohlen wurde, ruft die Polizei.", "dessen", "Genitivo del pronome relativo maschile: dessen."),
        cl("cl_b1_6", "B1", "Wenn ich mehr Zeit ___, würde ich mehr lernen.", "hätte", "Konjunktiv II di 'haben': hätte.")
    )

    private val dettati = listOf(
        de("de_a1_1", "A1", "Ich heiße Anna und wohne in Brixen."),
        de("de_a1_2", "A1", "Das Buch liegt auf dem Tisch."),
        de("de_a1_3", "A1", "Ich habe heute Kopfschmerzen."),
        de("de_a1_4", "A1", "Wir gehen morgen zur Arbeit."),
        de("de_a1_5", "A1", "Er trinkt einen Kaffee."),
        de("de_a1_6", "A1", "Die Katze ist klein und schwarz."),
        de("de_a2_1", "A2", "Gestern bin ich mit dem Zug nach Bozen gefahren."),
        de("de_a2_2", "A2", "Ich habe eine Fahrkarte für den Bus gekauft."),
        de("de_a2_3", "A2", "Mein Knie tut seit drei Tagen weh."),
        de("de_a2_4", "A2", "Kannst du mir bitte helfen?"),
        de("de_a2_5", "A2", "Der Termin ist am Montag um zehn Uhr."),
        de("de_a2_6", "A2", "Sie hat den ganzen Tag gearbeitet."),
        de("de_b1_1", "B1", "Der Patient muss vom Arzt untersucht werden."),
        de("de_b1_2", "B1", "Obwohl es geregnet hat, sind wir spazieren gegangen."),
        de("de_b1_3", "B1", "Ich interessiere mich für die Arbeit in einer Klinik."),
        de("de_b1_4", "B1", "Können Sie mir sagen, wo die Apotheke ist?"),
        de("de_b1_5", "B1", "Wenn ich mehr Zeit hätte, würde ich mehr Sport machen."),
        de("de_b1_6", "B1", "Die Übungen sollen jeden Tag gemacht werden.")
    )

    private val riordini = listOf(
        ri("ri_a1_1", "A1", "Ich heiße Anna.", listOf("Ich", "heiße", "Anna")),
        ri("ri_a1_2", "A1", "Das Buch liegt auf dem Tisch.", listOf("Das", "Buch", "liegt", "auf", "dem", "Tisch")),
        ri("ri_a1_3", "A1", "Ich wohne in Brixen.", listOf("Ich", "wohne", "in", "Brixen")),
        ri("ri_a1_4", "A1", "Er hat einen Hund.", listOf("Er", "hat", "einen", "Hund")),
        ri("ri_a1_5", "A1", "Wir lernen Deutsch.", listOf("Wir", "lernen", "Deutsch")),
        ri("ri_a1_6", "A1", "Sie trinkt einen Kaffee.", listOf("Sie", "trinkt", "einen", "Kaffee")),
        ri("ri_a2_1", "A2", "Gestern bin ich ins Kino gegangen.", listOf("Gestern", "bin", "ich", "ins", "Kino", "gegangen")),
        ri("ri_a2_2", "A2", "Ich habe eine Fahrkarte gekauft.", listOf("Ich", "habe", "eine", "Fahrkarte", "gekauft")),
        ri("ri_a2_3", "A2", "Kannst du mir bitte helfen?", listOf("Kannst", "du", "mir", "bitte", "helfen")),
        ri("ri_a2_4", "A2", "Mein Knie tut seit drei Tagen weh.", listOf("Mein", "Knie", "tut", "seit", "drei", "Tagen", "weh")),
        ri("ri_a2_5", "A2", "Der Termin ist am Montag um zehn Uhr.", listOf("Der", "Termin", "ist", "am", "Montag", "um", "zehn", "Uhr")),
        ri("ri_a2_6", "A2", "Ich warte auf den Bus.", listOf("Ich", "warte", "auf", "den", "Bus")),
        ri("ri_b1_1", "B1", "Der Patient muss vom Arzt untersucht werden.", listOf("Der", "Patient", "muss", "vom", "Arzt", "untersucht", "werden")),
        ri("ri_b1_2", "B1", "Wir sind trotz des Regens spazieren gegangen.", listOf("Wir", "sind", "trotz", "des", "Regens", "spazieren", "gegangen")),
        ri("ri_b1_3", "B1", "Ich interessiere mich für die Arbeit in einer Klinik.", listOf("Ich", "interessiere", "mich", "für", "die", "Arbeit", "in", "einer", "Klinik")),
        ri("ri_b1_4", "B1", "Können Sie mir sagen, wo die Apotheke ist?", listOf("Können", "Sie", "mir", "sagen", "wo", "die", "Apotheke", "ist")),
        ri("ri_b1_5", "B1", "Die Übungen sollen jeden Tag gemacht werden.", listOf("Die", "Übungen", "sollen", "jeden", "Tag", "gemacht", "werden")),
        ri("ri_b1_6", "B1", "Er hat sich für die Stelle beworben.", listOf("Er", "hat", "sich", "für", "die", "Stelle", "beworben"))
    )

    val tutti: List<EsercizioProduttivo> = traduzioni + cloze + dettati + riordini

    /** Esercizi del livello richiesto (A0 ricade su A1, B2+ su B1). */
    fun perLivello(livello: String, tipo: TipoProduttivo? = null): List<EsercizioProduttivo> {
        val codice = livello.substringBefore(" ").substringBefore("-").trim().uppercase()
        val target = when (codice) {
            "A0", "A1" -> "A1"
            "A2" -> "A2"
            else -> "B1"
        }
        return tutti.filter { it.livello == target && (tipo == null || it.tipo == tipo) }
    }

    fun perTipo(tipo: TipoProduttivo): List<EsercizioProduttivo> = tutti.filter { it.tipo == tipo }
}
