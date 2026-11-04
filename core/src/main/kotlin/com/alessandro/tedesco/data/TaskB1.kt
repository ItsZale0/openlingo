package com.alessandro.tedesco.data

/**
 * Compiti d'esame realistici (Goethe/telc/ÖSD): non "scrivi un testo", ma una
 * situazione con destinatario, tre punti da coprire e un limite di parole.
 * Servono a dare al tutor un testo confrontabile con la prova vera.
 */
object TaskB1 {

    data class TaskScrittura(
        val id: String,
        val livello: String,
        val titolo: String,
        val situazione: String,
        val punti: List<String>,
        val paroleMin: Int,
        val paroleMax: Int
    ) {
        /** Istruzioni complete da passare al tutor. */
        fun istruzioni(): String = buildString {
            append(situazione)
            append("\n\nPunti da coprire:")
            punti.forEach { append("\n- ").append(it) }
            append("\n\nScrivi ").append(paroleMin).append("-").append(paroleMax).append(" parole.")
        }
    }

    data class TaskOrale(
        val id: String,
        val livello: String,
        val titolo: String,
        val descrizione: String,
        val punti: List<String>
    )

    val scrittura: List<TaskScrittura> = listOf(
        TaskScrittura(
            id = "b1_email_informale",
            livello = "B1",
            titolo = "Email informale: invito",
            situazione = "Scrivi un'email a un amico tedesco. Lo inviti a passare un fine settimana da te a Bressanone.",
            punti = listOf(
                "racconta perché vuoi vederlo",
                "proponi due attività per il fine settimana",
                "chiedi quando arriva e se ha bisogno di un letto"
            ),
            paroleMin = 80,
            paroleMax = 120
        ),
        TaskScrittura(
            id = "b1_post_opinione",
            livello = "B1",
            titolo = "Post di opinione: sport e salute",
            situazione = "Scrivi un post in un forum: 'Fare sport è importante per la salute'. Esprimi la tua opinione.",
            punti = listOf(
                "la tua esperienza personale",
                "due argomenti a favore",
                "un possibile svantaggio o difficoltà"
            ),
            paroleMin = 80,
            paroleMax = 120
        ),
        TaskScrittura(
            id = "b1_email_formale_candidatura",
            livello = "B1",
            titolo = "Email formale: candidatura",
            situazione = "Scrivi un'email formale a una clinica di fisioterapia a Bressanone per candidarti a un tirocinio.",
            punti = listOf(
                "chi sei e cosa studi",
                "perché ti interessa proprio quella clinica",
                "quando sei disponibile per un colloquio"
            ),
            paroleMin = 80,
            paroleMax = 120
        ),
        TaskScrittura(
            id = "b1_email_formale_disdetta",
            livello = "B1",
            titolo = "Email formale: disdetta appuntamento",
            situazione = "Scrivi un'email formale allo studio medico: devi disdire l'appuntamento di giovedì.",
            punti = listOf(
                "motiva brevemente la disdetta",
                "chiedi un nuovo appuntamento",
                "scusati per il disturbo"
            ),
            paroleMin = 60,
            paroleMax = 90
        ),
        TaskScrittura(
            id = "b1_reclamo",
            livello = "B1",
            titolo = "Reclamo: corso di tedesco",
            situazione = "Scrivi un reclamo alla scuola di tedesco: il corso non corrisponde a quanto promesso.",
            punti = listOf(
                "spiega cosa non va",
                "indica cosa ti aspettavi",
                "chiedi una soluzione"
            ),
            paroleMin = 70,
            paroleMax = 100
        ),
        TaskScrittura(
            id = "b1_richiesta_info",
            livello = "B1",
            titolo = "Richiesta informazioni: corso B1",
            situazione = "Scrivi alla scuola di lingue per chiedere informazioni su un corso di tedesco B1 serale.",
            punti = listOf(
                "orari e durata",
                "costo e materiali",
                "possibilità di una lezione di prova"
            ),
            paroleMin = 60,
            paroleMax = 90
        ),
        TaskScrittura(
            id = "b1_email_paziente",
            livello = "B1",
            titolo = "Email a un paziente: cambio appuntamento",
            situazione = "Sei fisioterapista. Scrivi un'email formale a un paziente per spostare la seduta di venerdì.",
            punti = listOf(
                "scusati per il cambiamento",
                "proponi due alternative",
                "chiedi conferma"
            ),
            paroleMin = 60,
            paroleMax = 90
        ),
        TaskScrittura(
            id = "b1_resoconto_terapia",
            livello = "B1",
            titolo = "Resoconto: una seduta di fisioterapia",
            situazione = "Scrivi un breve resoconto per il tuo tutor: descrivi una seduta di fisioterapia simulata.",
            punti = listOf(
                "il problema del paziente",
                "cosa hai fatto durante la seduta",
                "quali esercizi ha ricevuto"
            ),
            paroleMin = 80,
            paroleMax = 120
        ),
        TaskScrittura(
            id = "a2_messaggio_collega",
            livello = "A2",
            titolo = "Messaggio a un collega: cambio turno",
            situazione = "Scrivi un messaggio a un collega per chiedere di cambiare turno.",
            punti = listOf("saluta", "spiega perché", "proponi un altro giorno", "ringrazia"),
            paroleMin = 40,
            paroleMax = 70
        ),
        TaskScrittura(
            id = "a2_invito_compleanno",
            livello = "A2",
            titolo = "Invito a una festa",
            situazione = "Scrivi un messaggio a un'amica per invitarla alla tua festa di compleanno.",
            punti = listOf("quando e dove", "cosa portare", "chiedi conferma"),
            paroleMin = 40,
            paroleMax = 70
        ),
        TaskScrittura(
            id = "a2_problema_casa",
            livello = "A2",
            titolo = "Email alla padrona di casa",
            situazione = "Scrivi alla padrona di casa: il riscaldamento non funziona.",
            punti = listOf("descrivi il problema", "da quando", "chiedi un intervento"),
            paroleMin = 40,
            paroleMax = 70
        ),
        TaskScrittura(
            id = "a2_fine_settimana",
            livello = "A2",
            titolo = "Il mio fine settimana",
            situazione = "Scrivi un breve testo per il tuo corso di tedesco sul tuo fine settimana.",
            punti = listOf("cosa hai fatto", "con chi", "cosa ti è piaciuto"),
            paroleMin = 50,
            paroleMax = 80
        )
    )

    val orale: List<TaskOrale> = listOf(
        TaskOrale(
            id = "or_a1_presentazione",
            livello = "A1",
            titolo = "Presentati",
            descrizione = "Parla per 1 minuto: nome, età, città, lavoro o studi, hobby.",
            punti = listOf("nome e provenienza", "dove vivi", "cosa fai", "un hobby")
        ),
        TaskOrale(
            id = "or_a2_lavoro",
            livello = "A2",
            titolo = "Il mio lavoro",
            descrizione = "Descrivi il tuo lavoro o il lavoro che vorresti fare.",
            punti = listOf("cosa fai", "dove lavori", "cosa ti piace", "cosa è difficile")
        ),
        TaskOrale(
            id = "or_a2_pianificare",
            livello = "A2",
            titolo = "Pianifichiamo insieme",
            descrizione = "Con il tutor, organizza un'uscita: giorno, ora, luogo, come arrivare.",
            punti = listOf("proponi un giorno", "accetta o rifiuta", "fissa l'appuntamento")
        ),
        TaskOrale(
            id = "or_b1_presentazione",
            livello = "B1",
            titolo = "Presentazione B1: vivere a Bressanone",
            descrizione = "Presenta per 2 minuti il tema: vivere e lavorare a Bressanone da italofono.",
            punti = listOf("vantaggi", "difficoltà", "lingua e lavoro", "la tua opinione")
        ),
        TaskOrale(
            id = "or_b1_discussione",
            livello = "B1",
            titolo = "Discussione: sport e salute",
            descrizione = "Discuti con il tutor: lo sport dovrebbe essere obbligatorio? Argomenta pro e contro.",
            punti = listOf("prendi posizione", "due argomenti", "rispondi all'obiezione", "concludi")
        ),
        TaskOrale(
            id = "or_b1_anamnesi",
            livello = "B1",
            titolo = "Roleplay: primo colloquio con un paziente",
            descrizione = "Sei fisioterapista. Fai l'anamnesi a un paziente con mal di schiena.",
            punti = listOf("saluta e presentati", "chiedi dove e da quando fa male", "chiedi l'intensità 1-10", "spiega il prossimo passo")
        ),
        TaskOrale(
            id = "or_b1_telefono",
            livello = "B1",
            titolo = "Roleplay: appuntamento al telefono",
            descrizione = "Chiami uno studio di fisioterapia per fissare un appuntamento.",
            punti = listOf("presentati", "spiega il problema", "chiedi giorno e ora", "conferma")
        ),
        TaskOrale(
            id = "or_b1_opinione",
            livello = "B1",
            titolo = "Opinione: imparare il tedesco da adulti",
            descrizione = "È possibile imparare bene una lingua da adulti? Discuti con il tutor.",
            punti = listOf("la tua esperienza", "cosa aiuta", "cosa ostacola", "un consiglio")
        )
    )

    fun scritturaPerLivello(livello: String): List<TaskScrittura> {
        val codice = livello.substringBefore(" ").substringBefore("-").trim().uppercase()
        return if (codice == "A0" || codice == "A1") scrittura.filter { it.livello == "A2" }
        else scrittura
    }
}
