package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.TestLivello
import com.alessandro.openlingo.i18n.t

/**
 * Motore adattivo: genera raccomandazioni di studio personalizzate
 * basate sui progressi dell'utente.
 *
 * Le raccomandazioni sono ordinate per priorità (1 = più urgente)
 * e si adattano dinamicamente: cambiano man mano che l'utente progredisce.
 */
object CalcoloPercorsoAdattivo {

    enum class CategoriaRaccomandazione {
        RIPASSO,
        GRAMMATICA,
        COMPRENSIONE,
        PRODUZIONE,
        TEST_LIVELLO,
        SESSIONE_GUIDATA,
        VOCABOLARIO
    }

    data class Raccomandazione(
        val priorita: Int,
        val categoria: CategoriaRaccomandazione,
        val titolo: String,
        val descrizione: String,
        val azione: String,
        val urgente: Boolean = false
    )

    data class PercorsoAdattivo(
        val raccomandazioni: List<Raccomandazione>,
        val messaggioMotivazionale: String,
        val livelloAttuale: LivelloCEFR,
        val prossimoObiettivo: String
    )

    /**
     * Calcola il percorso adattivo basato sui dati dell'utente.
     *
     * @param statistiche statistiche complete dell'app
     * @param competenze punteggi delle 4 competenze
     * @param daRipassare numero di parole in scadenza
     * @param livelloCorrente livello CEFR dell'utente
     * @param obiettivoLivello livello obiettivo (default B1)
     * @param ultimoTest ultimo test di livello effettuato (null se mai fatto)
     */
    fun calcola(
        statistiche: Statistiche,
        competenze: CalcoloCompetenze.PunteggiCompetenze,
        daRipassare: Int,
        livelloCorrente: LivelloCEFR,
        obiettivoLivello: LivelloCEFR = LivelloCEFR.B1,
        ultimoTest: TestLivello? = null
    ): PercorsoAdattivo {
        val raccomandazioni = mutableListOf<Raccomandazione>()
        var prioritaCounter = 1

        // 1. RIPASSO: se ci sono parole in scadenza, è la priorità massima
        if (daRipassare > 0) {
            raccomandazioni.add(
                Raccomandazione(
                    priorita = prioritaCounter++,
                    categoria = CategoriaRaccomandazione.RIPASSO,
                    titolo = t("Ripasso flashcard"),
                    descrizione = "$daRipassare " + t("parole in scadenza. Mantieni il ritmo!"),
                    azione = "ripasso",
                    urgente = daRipassare >= 10
                )
            )
        }

        // 2. GRAMMATICA: se il punteggio è sotto la soglia
        if (competenze.grammatica < 60f) {
            raccomandazioni.add(
                Raccomandazione(
                    priorita = prioritaCounter++,
                    categoria = CategoriaRaccomandazione.GRAMMATICA,
                    titolo = t("Esercizi di grammatica"),
                    descrizione = t("Grammatica al") + " ${competenze.grammatica.toInt()}%. " + t("Concentrati su: articoli, verbi modali e congiuntivo."),
                    azione = "grammatica",
                    urgente = competenze.grammatica < 50f
                )
            )
        }

        // 3. COMPRENSIONE: se il punteggio è sotto la soglia
        if (competenze.comprensione < 60f) {
            raccomandazioni.add(
                Raccomandazione(
                    priorita = prioritaCounter++,
                    categoria = CategoriaRaccomandazione.COMPRENSIONE,
                    titolo = t("Quiz di comprensione"),
                    descrizione = t("Comprensione al") + " ${competenze.comprensione.toInt()}%. " + t("Leggi testi e rispondi alle domande."),
                    azione = "quiz-comprensione",
                    urgente = competenze.comprensione < 40f
                )
            )
        }

        // 4. PRODUZIONE: se il punteggio è sotto la soglia
        if (competenze.produzione < 60f) {
            raccomandazioni.add(
                Raccomandazione(
                    priorita = prioritaCounter++,
                    categoria = CategoriaRaccomandazione.PRODUZIONE,
                    titolo = t("Esercizi di produzione"),
                    descrizione = t("Produzione al") + " ${competenze.produzione.toInt()}%. " + t("Scrivi frasi complete in tedesco."),
                    azione = "quiz-produzione",
                    urgente = competenze.produzione < 40f
                )
            )
        }

        // 5. TEST DI LIVELLO: se l'utente è vicino al livello obiettivo
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
            raccomandazioni.add(
                Raccomandazione(
                    priorita = prioritaCounter++,
                    categoria = CategoriaRaccomandazione.TEST_LIVELLO,
                    titolo = t("Verifica il tuo livello"),
                    descrizione = t("Sei pronto per un test di") + " ${obiettivoLivello.label}. " + t("Metti alla prova ciò che hai imparato."),
                    azione = "testb1"
                )
            )
        }

        // 6. SESSIONE GUIDATA: se non ci sono parole in scadenza ma il vocabolario è basso
        if (daRipassare == 0 && statistiche.paroleTotali < 500) {
            raccomandazioni.add(
                Raccomandazione(
                    priorita = prioritaCounter++,
                    categoria = CategoriaRaccomandazione.SESSIONE_GUIDATA,
                    titolo = t("Sessione di studio"),
                    descrizione = t("Nessun ripasso in scadenza.") + " " + t("Approfondisci con una sessione guidata."),
                    azione = "sessioni"
                )
            )
        }

        // 7. VOCABOLARIO: se il lessico è sotto la soglia
        if (competenze.lessico < CertificazioniUfficiali.SOGLIA_SUPERAMENTO) {
            raccomandazioni.add(
                Raccomandazione(
                    priorita = prioritaCounter++,
                    categoria = CategoriaRaccomandazione.VOCABOLARIO,
                    titolo = t("Nuove parole"),
                    descrizione = t("Lessico al") + " ${competenze.lessico.toInt()}%. " + t("Impara nuove parole dalla lezione") + " ${statistiche.perLezione.size + 1}.",
                    azione = "nuove"
                )
            )
        }

        // Se non ci sono raccomandazioni, complimentati
        if (raccomandazioni.isEmpty()) {
            raccomandazioni.add(
                Raccomandazione(
                    priorita = 1,
                    categoria = CategoriaRaccomandazione.SESSIONE_GUIDATA,
                    titolo = t("Ottimo lavoro!"),
                    descrizione = t("Hai coperto tutte le aree principali.") + " " + t("Continua così con una sessione di approfondimento."),
                    azione = "sessioni"
                )
            )
        }

        val messaggio = when {
            daRipassare >= 20 -> t("Torna in carriera! Tante parole da ripassare.")
            daRipassare >= 10 -> t("Buon ritmo! Mantieni la costanza.")
            statistiche.streakGiorni >= 7 -> t("Streak di") + " ${statistiche.streakGiorni} " + t("giorni! Sei inarrestabile.")
            competenze.grammatica >= 60f && daRipassare == 0 -> t("Prosegui così, stai facendo ottimi progressi.")
            competenze.grammatica < 40f -> t("La grammatica ti sta mettendo alla prova. Forza, puoi farcela!")
            competenze.lessico < 30f -> t("Sei all'inizio del viaggio. Ogni parola conta!")
            else -> t("Prosegui così, stai facendo ottimi progressi.")
        }

        // Obiettivi con il vocabolario ATTIVO ufficiale (Wortliste Goethe / 2).
        val prossimoObiettivo = when {
            livelloCorrente < LivelloCEFR.A1 ->
                t("Raggiungere A1:") + " ${CertificazioniUfficiali.vocabolarioAttivoRichiesto(LivelloCEFR.A1)} " + t("parole attive")
            livelloCorrente < LivelloCEFR.A2 ->
                t("Raggiungere A2:") + " ${CertificazioniUfficiali.vocabolarioAttivoRichiesto(LivelloCEFR.A2)} " + t("parole attive")
            livelloCorrente < LivelloCEFR.B1 ->
                t("Raggiungere B1:") + " ${CertificazioniUfficiali.vocabolarioAttivoRichiesto(LivelloCEFR.B1)} " + t("parole attive")
            livelloCorrente < LivelloCEFR.B2 ->
                t("Raggiungere B2:") + " ${CertificazioniUfficiali.vocabolarioAttivoRichiesto(LivelloCEFR.B2)} " + t("parole attive")
            else ->
                t("Raggiungere C1:") + " ${CertificazioniUfficiali.vocabolarioAttivoRichiesto(LivelloCEFR.C1)} " + t("parole attive")
        }

        return PercorsoAdattivo(
            raccomandazioni = raccomandazioni,
            messaggioMotivazionale = messaggio,
            livelloAttuale = livelloCorrente,
            prossimoObiettivo = prossimoObiettivo
        )
    }
}
