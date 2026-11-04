package com.alessandro.tedesco.data

import kotlinx.serialization.Serializable

@Serializable
data class DomandaAscolto(
    val id: String,
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    val spiegazione: String
)

@Serializable
data class ListeningExercise(
    val id: String,
    val titolo: String,
    val testo: String,
    val domande: List<DomandaAscolto>,
    val livello: String
)

val LISTENING_EXERCISES = listOf(
    ListeningExercise(
        id = "le1",
        titolo = "Presentazioni",
        testo = "Ich heiße Alessandro. Ich komme aus Italien. Ich bin Physiotherapeut.",
        domande = listOf(
            DomandaAscolto("le1q1", "Come si chiama il parlante?", listOf("Alessandro", "Marco", "Luca"), 0, "Il parlante dice 'Ich heiße Alessandro'"),
            DomandaAscolto("le1q2", "Da dove viene?", listOf("Germania", "Italia", "Austria"), 1, "Il parlante dice 'Ich komme aus Italien'"),
            DomandaAscolto("le1q3", "Qual è il suo lavoro?", listOf("Medico", "Insegnante", "Fisioterapeuta"), 2, "Il parlante dice 'Ich bin Physiotherapeut'")
        ),
        livello = "A0"
    ),
    ListeningExercise(
        id = "le2",
        titolo = "Numeri",
        testo = "Eins, zwei, drei, vier, fünf, sechs, sieben, acht, neun, zehn.",
        domande = listOf(
            DomandaAscolto("le2q1", "Quanti numeri sono stati detti?", listOf("5", "10", "7"), 1, "Sono stati detti 10 numeri da 1 a 10"),
            DomandaAscolto("le2q2", "Qual è l'ultimo numero?", listOf("neun", "zehn", "acht"), 1, "L'ultimo numero è 'zehn' (10)"),
            DomandaAscolto("le2q3", "Come si dice 3 in tedesco?", listOf("zwei", "drei", "vier"), 1, "3 in tedesco è 'drei'")
        ),
        livello = "A0"
    ),
    ListeningExercise(
        id = "le3",
        titolo = "Famiglia",
        testo = "Das ist meine Mutter. Das ist mein Vater. Das ist meine Schwester. Das ist mein Bruder.",
        domande = listOf(
            DomandaAscolto("le3q1", "Quante persone sono state presentate?", listOf("2", "3", "4"), 3, "Sono state presentate 4 persone: madre, padre, sorella, fratello"),
            DomandaAscolto("le3q2", "Chi è 'die Mutter'?", listOf("la madre", "il padre", "la sorella"), 0, "'Die Mutter' significa 'la madre'"),
            DomandaAscolto("le3q3", "Chi è 'der Bruder'?", listOf("il fratello", "il padre", "il figlio"), 0, "'Der Bruder' significa 'il fratello'")
        ),
        livello = "A0"
    ),
    ListeningExercise(
        id = "le4",
        titolo = "Città",
        testo = "Ich wohne in Bozen. Bozen ist eine schöne Stadt. Es gibt viele Geschäfte und Restaurants.",
        domande = listOf(
            DomandaAscolto("le4q1", "Dove vive il parlante?", listOf("Milano", "Bozen", "Roma"), 1, "Il parlante dice 'Ich wohne in Bozen'"),
            DomandaAscolto("le4q2", "Come è la città?", listOf("brutta", "piccola", "bella"), 2, "Il parlante dice 'eine schöne Stadt'"),
            DomandaAscolto("le4q3", "Cosa c'è a Bozen?", listOf("solo case", "negozi e ristoranti", "solo uffici"), 1, "Il parlante dice 'viele Geschäfte und Restaurants'")
        ),
        livello = "A0"
    ),
    ListeningExercise(
        id = "le5",
        titolo = "Ristorante",
        testo = "Ich hätte gern ein Wasser, bitte. Und ein Stück Kuchen. Danke schön!",
        domande = listOf(
            DomandaAscolto("le5q1", "Cosa vuole bere il parlante?", listOf("caffè", "vino", "acqua"), 2, "Il parlante dice 'ein Wasser'"),
            DomandaAscolto("le5q2", "Cosa vuole mangiare?", listOf("pane", "torta", "formaggio"), 1, "Il parlante dice 'ein Stück Kuchen'"),
            DomandaAscolto("le5q3", "Come ringrazia?", listOf("Grazie mille", "Danke schön", "Prego"), 1, "Il parlante dice 'Danke schön'")
        ),
        livello = "A0"
    )
)
