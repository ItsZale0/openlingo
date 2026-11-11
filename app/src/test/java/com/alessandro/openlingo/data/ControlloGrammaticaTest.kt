package com.alessandro.openlingo.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Gli esercizi non devono insegnare forme sbagliate.
 *
 * Il caso che ha fatto partire tutto: "einen Buch" al posto di "ein Buch"
 * (Buch è neutro, quindi all'accusativo non cambia). Il test finale controlla
 * TUTTO il percorso: se un esercizio statico contiene un errore di accordo
 * riconoscibile, la build fallisce prima che arrivi allo studente.
 */
class ControlloGrammaticaTest {

    private fun problemi(frase: String) = ControlloGrammatica.problemiFrase(frase)

    @Test
    fun `einen Buch viene segnalato`() {
        assertFalse(problemi("Ich habe einen Buch.").isEmpty())
    }

    @Test
    fun `ein Buch e corretto`() {
        assertTrue(problemi("Ich habe ein Buch.").isEmpty())
    }

    @Test
    fun `gli articoli sbagliati sul neutro vengono segnalati`() {
        assertFalse(problemi("Die Buch ist neu.").isEmpty())
        assertFalse(problemi("Den Buch lese ich.").isEmpty())
        assertFalse(problemi("Einen Buch kaufe ich.").isEmpty())
    }

    @Test
    fun `le forme corrette del neutro passano`() {
        assertTrue(problemi("Das Buch ist neu.").isEmpty())
        assertTrue(problemi("Ich lese das Buch.").isEmpty())
        assertTrue(problemi("Ich helfe dem Kind.").isEmpty())
        assertTrue(problemi("Das ist mein Buch.").isEmpty())
    }

    @Test
    fun `accusativo maschile corretto`() {
        assertTrue(problemi("Ich sehe einen Hund.").isEmpty())
        assertTrue(problemi("Ich sehe den Hund.").isEmpty())
        assertTrue(problemi("Er kauft einen Apfel.").isEmpty())
    }

    @Test
    fun `accusativo maschile sbagliato`() {
        assertFalse(problemi("Ich sehe eine Hund.").isEmpty())
        // "einen" è solo maschile: con Buch (neutro) è sempre sbagliato.
        assertFalse(problemi("Ich habe einen Buch.").isEmpty())
        // Nota: "Ich sehe ein Hund." NON è rilevabile senza analisi del caso,
        // perché "ein" è valido anche al nominativo maschile.
    }

    @Test
    fun `l'aggettivo tra articolo e nome non confonde il controllo`() {
        assertTrue(problemi("Ich sehe einen kleinen Hund.").isEmpty())
        assertTrue(problemi("Ich habe ein neues Buch.").isEmpty())
        assertFalse(problemi("Ich habe einen neuen Buch.").isEmpty())
    }

    @Test
    fun `una relativa non viene scambiata per un articolo`() {
        // "die" è pronome relativo, non articolo di Garten.
        assertTrue(problemi("Die Kinder, die im Garten spielen, sind laut.").isEmpty())
    }

    @Test
    fun `il controllo su una scelta multipla usa la risposta corretta`() {
        val problemi = ControlloGrammatica.problemi(
            domanda = "Ich habe ___ Buch.",
            opzioni = listOf("einen", "eine", "ein", "einem"),
            rispostaCorretta = 0
        )
        assertTrue(problemi.joinToString().contains("Buch"))
    }

    // ── Ambiguità di numero sui nomi deboli (N-Deklination) ──

    @Test
    fun `l'esercizio ambiguo su Patienten viene segnalato`() {
        // Caso reale: "Patienten" è accusativo singolare e plurale; sia "euren"
        // sia "eure" sono corrette, ma l'app ne accettava una sola.
        val problemi = ControlloGrammatica.problemi(
            domanda = "Wir besuchen ___ Patienten im Zimmer.",
            opzioni = listOf("euer", "eurer", "euren", "eure"),
            rispostaCorretta = 3
        )
        assertFalse(problemi.isEmpty())
        assertTrue(problemi.joinToString().contains("singolare"))
        assertTrue(problemi.joinToString().contains("plurale"))
    }

    @Test
    fun `con una sola opzione valida il controllo tace`() {
        // Stessa frase, ma solo "euren" è una forma accusativa possibile:
        // "eurem"/"eures"/"euer" non reggono un accusativo maschile.
        val problemi = ControlloGrammatica.problemi(
            domanda = "Wir besuchen ___ Patienten im Zimmer.",
            opzioni = listOf("euren", "eurem", "eures", "euer"),
            rispostaCorretta = 0
        )
        assertTrue(problemi.isEmpty())
    }

    @Test
    fun `l'articolo determinativo ambiguo su un nome debole viene segnalato`() {
        val problemi = ControlloGrammatica.problemi(
            domanda = "Wir besuchen ___ Patienten.",
            opzioni = listOf("der", "den", "die", "das"),
            rispostaCorretta = 1
        )
        assertFalse(problemi.isEmpty())
    }

    @Test
    fun `il dativo ambiguo su un nome debole viene segnalato`() {
        val problemi = ControlloGrammatica.problemi(
            domanda = "Der Arzt hilft ___ Patienten.",
            opzioni = listOf("dem", "den", "der", "des"),
            rispostaCorretta = 0
        )
        assertFalse(problemi.isEmpty())
    }

    @Test
    fun `un quantificatore plurale toglie l'ambiguita`() {
        // "alle" fissa il plurale: con "euren" (plurale dativo) la frase di
        // partenza non è più ambigua come nel caso senza quantificatore.
        val problemi = ControlloGrammatica.problemi(
            domanda = "Wir besuchen alle ___ Patienten.",
            opzioni = listOf("euer", "eurer", "euren", "eure"),
            rispostaCorretta = 3
        )
        assertTrue(problemi.isEmpty())
    }

    @Test
    fun `un verbo che segue il nome fissa il numero`() {
        // "sind" rende il nome necessariamente plurale: niente ambiguità.
        val problemi = ControlloGrammatica.problemi(
            domanda = "___ Patienten sind im Zimmer.",
            opzioni = listOf("der", "den", "die", "das"),
            rispostaCorretta = 2
        )
        assertTrue(problemi.isEmpty())
    }

    @Test
    fun `senza contesto di caso il controllo non si pronuncia`() {
        // Nessun verbo/preposizione prima del buco: il controllo preferisce
        // tacere piuttosto che rischiare un falso allarme.
        val problemi = ControlloGrammatica.problemi(
            domanda = "___ Patienten im Zimmer.",
            opzioni = listOf("euer", "eurer", "euren", "eure"),
            rispostaCorretta = 3
        )
        assertTrue(problemi.isEmpty())
    }

    @Test
    fun `il buco che non è un determinante non attiva il controllo`() {
        // Il buco è il nome (n-Deklination), non l'articolo: ambito diverso.
        val problemi = ControlloGrammatica.problemi(
            domanda = "Ich sehe den ___.",
            opzioni = listOf("Student", "Studenten", "Studente", "Studentes"),
            rispostaCorretta = 1
        )
        assertTrue(problemi.isEmpty())
    }

    @Test
    fun `tutti gli esercizi del percorso sono grammaticalmente corretti`() {
        val problemi = mutableListOf<String>()
        GrammaticaSyllabus.unita.forEach { unita ->
            unita.esercizi.forEach { esercizio ->
                ControlloGrammatica.problemi(esercizio).forEach { problema ->
                    problemi += "${unita.id}/${esercizio.id}: $problema"
                }
            }
        }
        assertTrue(
            "Esercizi con accordo articolo/nome sbagliato:\n" + problemi.joinToString("\n"),
            problemi.isEmpty()
        )
    }
}
