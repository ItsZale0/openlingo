package com.alessandro.tedesco.data

/**
 * Consiglio dinamico del tipo di esercizio, basato sui progressi reali.
 *
 * Regole (in ordine di priorita'):
 *  1. Se ci sono parole sbagliate da ripassare -> RIPASSO prima di tutto.
 *  2. Se tutte le competenze superano la soglia ufficiale -> ASCOLTO (rinforzo).
 *  3. Altrimenti il tipo corrispondente alla competenza piu' debile.
 *
 * Puro e senza Android: testabile senza emulator.
 */
object ConsigliEsercizi {

    data class Consiglio(
        val route: String,
        val titolo: String,
        val motivo: String
    )

    fun consiglio(pr: CalcoloProgresso.ProgressoReale?): Consiglio {
        if (pr == null) {
            return Consiglio(
                route = "grammatica",
                titolo = "Grammatica",
                motivo = "Iniziamo dalle basi: la grammatica e' il fondamento di tutto il resto."
            )
        }

        if (pr.paroleFragili > 0) {
            return Consiglio(
                route = "ripasso",
                titolo = "Ripasso",
                motivo = "Hai ${pr.paroleFragili} parole da ripassare: meglio fissare quelle che conosci " +
                    "che aggiungerne di nuove."
            )
        }

        val soglia = CertificazioniUfficiali.SOGLIA_SUPERAMENTO
        val dimensioni = listOf(
            "Lessico" to pr.competenze.lessico,
            "Grammatica" to pr.competenze.grammatica,
            "Comprensione" to pr.competenze.comprensione,
            "Produzione" to pr.competenze.produzione
        )

        // Nessuna evidenza in nessuna competenza: si parte dalle basi.
        if (dimensioni.all { it.second <= 0f }) {
            return Consiglio(
                route = "grammatica",
                titolo = "Grammatica",
                motivo = "Iniziamo dalle basi: la grammatica e' il fondamento di tutto il resto."
            )
        }

        val (nome, valore) = dimensioni.minByOrNull { it.second } ?: ("Grammatica" to 0f)

        return if (valore >= soglia) {
            Consiglio(
                route = "ascolto",
                titolo = "Ascolto",
                motivo = "Tutte le competenze superano la soglia ufficiale: ora rendi l'orecchio piu' veloce."
            )
        } else {
            val pct = valore.toInt()
            when (nome) {
                "Lessico" -> Consiglio(
                    route = "verifica",
                    titolo = "Verifica il lessico",
                    motivo = "Il lessico e' la tua area piu' debole ($pct%): dimostra di sapere le parole studiate."
                )
                "Grammatica" -> Consiglio(
                    route = "grammatica",
                    titolo = "Grammatica",
                    motivo = "La grammatica e' la tua area piu' debole ($pct%): una regola alla volta."
                )
                "Comprensione" -> Consiglio(
                    route = "comprensione",
                    titolo = "Lettura",
                    motivo = "La comprensione e' la tua area piu' debole ($pct%): leggi un testo calibrato sul tuo livello."
                )
                else -> Consiglio(
                    route = "produzione",
                    titolo = "Produzione scritta",
                    motivo = "La produzione e' la tua area piu' debole ($pct%): scrivi e fai correggere."
                )
            }
        }
    }
}
