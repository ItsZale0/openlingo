package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.TestLivello
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Motore del percorso personalizzato del giorno.
 *
 * Il piano è pensato per una sessione breve (circa 20 minuti): non somma
 * tutte le competenze deboli, ma sceglie al massimo tre passi e riduce la
 * durata stimata per stare dentro il budget. Il primo passo è sempre quello
 * con la priorità più alta (ripasso in scadenza, poi la prossima unità del
 * percorso grammaticale, poi la competenza più debole).
 */
object CalcoloPercorsoGiornaliero {

    /** Budget massimo del piano: "circa 20 minuti" con un piccolo margine. */
    const val MINUTI_MASSIMI = 22

    /** Un piano da 20 minuti non deve frammentarsi in troppe attività. */
    const val MAX_PASSI = 3

    enum class TipoPasso {
        RIPASSO,
        GRAMMATICA,
        COMPRENSIONE,
        PRODUZIONE,
        VOCABOLARIO,
        TEST_LIVELLO,
        SESSIONE_GUIDATA
    }

    private data class Supporto(
        val passo: PassoPercorso,
        val punteggio: Float,
        val ordine: Int
    )

    data class PassoPercorso(
        val id: String,
        val titolo: String,
        val descrizione: String,
        val tipo: TipoPasso,
        val priorita: Int,
        val stimaMinuti: Int,
        val azione: String,
        val urgente: Boolean = false,
        val completato: Boolean = false
    )

    data class PercorsoGiornaliero(
        val data: String,
        val passi: List<PassoPercorso>,
        val messaggioMotivazionale: String,
        val livelloAttuale: LivelloCEFR,
        val prossimoObiettivo: String,
        val minutiTotali: Int,
        val minutiCompletati: Int,
        val obiettivoMinuti: Int = 20
    ) {
        val progressoGiornata: Float
            get() = if (minutiTotali == 0) 0f else minutiCompletati.toFloat() / minutiTotali

        val passiCompletati: Int
            get() = passi.count { it.completato }

        val passiTotali: Int
            get() = passi.size

        val percorsoCompletato: Boolean
            get() = passi.isNotEmpty() && passi.all { it.completato }

        val prossimoPasso: PassoPercorso?
            get() = passi.firstOrNull { !it.completato }
    }

    /**
     * Genera il percorso personalizzato del giorno.
     *
     * @param statistiche statistiche complete dell'app
     * @param competenze punteggi delle 4 competenze
     * @param daRipassare numero di parole in scadenza
     * @param livelloCorrente livello CEFR dell'utente
     * @param obiettivoLivello livello obiettivo (default B1)
     * @param ultimoTest ultimo test di livello effettuato (null se mai fatto)
     * @param passiCompletatiOggi IDs dei passi già completati oggi
     * @param prossimaUnitaId unità grammaticale da aprire (null se il percorso è finito)
     * @param prossimaUnitaTitolo titolo leggibile dell'unità grammaticale
     */
    fun genera(
        statistiche: Statistiche,
        competenze: CalcoloCompetenze.PunteggiCompetenze,
        daRipassare: Int,
        livelloCorrente: LivelloCEFR,
        obiettivoLivello: LivelloCEFR = LivelloCEFR.B1,
        ultimoTest: TestLivello? = null,
        passiCompletatiOggi: Set<String> = emptySet(),
        prossimaUnitaId: String? = null,
        prossimaUnitaTitolo: String? = null
    ): PercorsoGiornaliero {
        val data = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(Date())
        val candidati = mutableListOf<PassoPercorso>()
        var prioritaCounter = 1

        // 1. RIPASSO: sempre il primo passo se ci sono parole in scadenza.
        // La stima resta breve: il piano non deve diventare una maratona.
        if (daRipassare > 0) {
            val minuti = (daRipassare * 0.5f).toInt().coerceIn(4, 8)
            candidati.add(
                PassoPercorso(
                    id = "ripasso",
                    titolo = "Ripasso flashcard",
                    descrizione = "$daRipassare parole in scadenza · ~$minuti min",
                    tipo = TipoPasso.RIPASSO,
                    priorita = prioritaCounter++,
                    stimaMinuti = minuti,
                    azione = "ripasso",
                    urgente = daRipassare >= 10,
                    completato = "ripasso" in passiCompletatiOggi
                )
            )
        }

        // 2. TEST DI LIVELLO: se manca poco all'obiettivo, il test viene prima
        // della nuova unità. È il passo più utile per capire dove si è.
        val progressoObiettivo = when (obiettivoLivello) {
            LivelloCEFR.B1 -> competenze.lessico / 100f * 0.5f +
                competenze.grammatica / 100f * 0.3f +
                competenze.comprensione / 100f * 0.2f

            else -> competenze.lessico / 100f * 0.4f +
                competenze.grammatica / 100f * 0.3f +
                competenze.comprensione / 100f * 0.15f +
                competenze.produzione / 100f * 0.15f
        }
        if (progressoObiettivo > 0.7f && livelloCorrente < obiettivoLivello) {
            val minuti = 15
            candidati.add(
                PassoPercorso(
                    id = "test-livello",
                    titolo = "Verifica il tuo livello",
                    descrizione = "Test di ${obiettivoLivello.label} · ~$minuti min",
                    tipo = TipoPasso.TEST_LIVELLO,
                    priorita = prioritaCounter++,
                    stimaMinuti = minuti,
                    azione = "verifica",
                    completato = "test-livello" in passiCompletatiOggi
                )
            )
        }

        // 3. GRAMMATICA: la prossima unità del percorso A0-B2 è il cuore del
        // piano. Se non c'è un'unità successiva, resta il ripasso generico.
        val includiGrammatica = prossimaUnitaId != null || competenze.grammatica < 60f
        if (includiGrammatica) {
            val minuti = 8
            val descrizione = if (prossimaUnitaId != null) {
                val titolo = prossimaUnitaTitolo?.takeIf { it.isNotBlank() } ?: "prossima unità"
                "$titolo · ~$minuti min"
            } else {
                "Grammatica al ${competenze.grammatica.toInt()}% · ~$minuti min"
            }
            candidati.add(
                PassoPercorso(
                    id = "grammatica",
                    titolo = if (prossimaUnitaId != null) "Percorso grammaticale" else "Esercizi di grammatica",
                    descrizione = descrizione,
                    tipo = TipoPasso.GRAMMATICA,
                    priorita = prioritaCounter++,
                    stimaMinuti = minuti,
                    azione = prossimaUnitaId?.let { "unita_grammatica/$it" } ?: "grammatica",
                    urgente = competenze.grammatica < 40f,
                    completato = "grammatica" in passiCompletatiOggi
                )
            )
        }

        // 4. SUPPORTI: una sola competenza debole, scelta per punteggio più
        // basso. Così il piano resta da 20 minuti invece di sommare tutto.
        val supporti = mutableListOf<Supporto>()
        if (competenze.comprensione < 60f) {
            val minuti = if (competenze.comprensione < 30f) 6 else 4
            supporti.add(
                Supporto(
                    PassoPercorso(
                        id = "comprensione",
                        titolo = "Comprensione",
                        descrizione = "Ascolto e lettura · ~$minuti min",
                        tipo = TipoPasso.COMPRENSIONE,
                        priorita = 0,
                        stimaMinuti = minuti,
                        azione = "comprensione",
                        urgente = competenze.comprensione < 40f,
                        completato = "comprensione" in passiCompletatiOggi
                    ),
                    competenze.comprensione,
                    1
                )
            )
        }
        if (competenze.produzione < 60f) {
            val minuti = if (competenze.produzione < 30f) 6 else 4
            supporti.add(
                Supporto(
                    PassoPercorso(
                        id = "produzione",
                        titolo = "Produzione",
                        descrizione = "Scrivi e rivedi un testo · ~$minuti min",
                        tipo = TipoPasso.PRODUZIONE,
                        priorita = 0,
                        stimaMinuti = minuti,
                        azione = "produzione",
                        urgente = competenze.produzione < 40f,
                        completato = "produzione" in passiCompletatiOggi
                    ),
                    competenze.produzione,
                    2
                )
            )
        }
        if (competenze.lessico < 50f) {
            val minuti = if (competenze.lessico < 20f) 6 else 4
            supporti.add(
                Supporto(
                    PassoPercorso(
                        id = "vocabolario",
                        titolo = "Lessico",
                        descrizione = "Parole utili per il livello · ~$minuti min",
                        tipo = TipoPasso.VOCABOLARIO,
                        priorita = 0,
                        stimaMinuti = minuti,
                        azione = "wortliste",
                        urgente = competenze.lessico < 25f,
                        completato = "vocabolario" in passiCompletatiOggi
                    ),
                    competenze.lessico,
                    0
                )
            )
        }
        candidati.addAll(
            supporti
                .sortedWith(compareBy({ it.punteggio }, { it.ordine }))
                .map { it.passo }
        )

        // 5. FALLBACK: nessuna priorità -> una sessione guidata da 20 minuti.
        if (candidati.isEmpty()) {
            candidati.add(
                PassoPercorso(
                    id = "sessione",
                    titolo = "Sessione di studio",
                    descrizione = "Ripasso completo e nuovi argomenti · ~20 min",
                    tipo = TipoPasso.SESSIONE_GUIDATA,
                    priorita = 1,
                    stimaMinuti = 20,
                    azione = "esercizi",
                    completato = "sessione" in passiCompletatiOggi
                )
            )
        }

        val passi = limitaAlBudget(candidati)
        val minutiTotali = passi.sumOf { it.stimaMinuti }
        val minutiCompletati = passi.filter { it.completato }.sumOf { it.stimaMinuti }

        val messaggio = when {
            passi.any { it.urgente } -> "Oggi conviene concentrarsi sulle aree con il badge Priorità."
            daRipassare >= 20 -> "Tante parole da ripassare. Inizia dal ripasso!"
            daRipassare >= 10 -> "Buon ritmo! Mantieni la costanza."
            statistiche.streakGiorni >= 7 -> "Streak di ${statistiche.streakGiorni} giorni! Sei inarrestabile."
            competenze.grammatica >= 60f && daRipassare == 0 -> "Prosegui così, stai facendo ottimi progressi."
            competenze.lessico < 30f -> "Sei all'inizio del viaggio. Ogni parola conta!"
            else -> "Buona sessione di studio!"
        }

        val prossimoObiettivo = when {
            livelloCorrente < LivelloCEFR.A1 -> "Raggiungere A1 con 50 parole"
            livelloCorrente < LivelloCEFR.A2 -> "Raggiungere A2 con 150 parole"
            livelloCorrente < LivelloCEFR.B1 -> "Raggiungere B1 con 900 parole"
            livelloCorrente < LivelloCEFR.B2 -> "Raggiungere B2 con 1800 parole"
            else -> "Raggiungere C1 con 3500 parole"
        }

        return PercorsoGiornaliero(
            data = data,
            passi = passi,
            messaggioMotivazionale = messaggio,
            livelloAttuale = livelloCorrente,
            prossimoObiettivo = prossimoObiettivo,
            minutiTotali = minutiTotali,
            minutiCompletati = minutiCompletati
        )
    }

    /**
     * Prende i primi passi fino al budget, riducendo l'ultimo se serve.
     * Non scende sotto i 4 minuti per passo: meglio togliere un passo che
     * proporre un'attività troppo frammentata.
     */
    private fun limitaAlBudget(candidati: List<PassoPercorso>): List<PassoPercorso> {
        val scelti = mutableListOf<PassoPercorso>()
        var totale = 0
        for (passo in candidati) {
            if (scelti.size >= MAX_PASSI) break
            val restante = MINUTI_MASSIMI - totale
            if (restante < 4) break
            val minuti = passo.stimaMinuti.coerceAtMost(restante).coerceAtLeast(4)
            // La descrizione contiene la stima: se il budget accorcia il passo,
            // deve mostrare la durata reale, non quella teorica.
            val descrizione = passo.descrizione.replace(
                Regex("""~\d+ min"""),
                "~$minuti min"
            )
            scelti.add(passo.copy(stimaMinuti = minuti, descrizione = descrizione))
            totale += minuti
        }
        return scelti.mapIndexed { indice, passo ->
            passo.copy(priorita = indice + 1)
        }
    }

    /**
     * Marca un passo come completato e restituisce il percorso aggiornato.
     */
    fun completaPasso(percorso: PercorsoGiornaliero, passoId: String): PercorsoGiornaliero {
        val nuoviPassi = percorso.passi.map { passo ->
            if (passo.id == passoId) passo.copy(completato = true) else passo
        }
        val minutiCompletati = nuoviPassi.filter { it.completato }.sumOf { it.stimaMinuti }
        return percorso.copy(
            passi = nuoviPassi,
            minutiCompletati = minutiCompletati
        )
    }

    /**
     * Resetta il percorso per un nuovo giorno.
     */
    fun resetGiorno(percorso: PercorsoGiornaliero): PercorsoGiornaliero {
        val nuoviPassi = percorso.passi.map { it.copy(completato = false) }
        return percorso.copy(
            passi = nuoviPassi,
            minutiCompletati = 0
        )
    }
}
