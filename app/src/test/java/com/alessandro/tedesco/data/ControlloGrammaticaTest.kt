package com.alessandro.tedesco.data

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
