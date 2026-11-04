package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.WordEntity

/**
 * Motore di generazione dinamica di sessioni adattive.
 *
 * A differenza delle sessioni statiche (caricate dal feed), questo motore
 * genera sessioni personalizzate in base a:
 * - Livello CEFR dell'utente
 * - Punteggi di competenza (aree deboli)
 * - Parole in scadenza per il ripasso
 * - Errori grammaticali recenti
 * - Progresso verso l'obiettivo
 *
 * Le sessioni sono ordinate per priorità e si adattano dinamicamente:
 * cambiano man mano che l'utente progredisce.
 */
object AdaptiveSessionEngine {

    enum class TipoSessione {
        RIPASSO,
        GRAMMATICA,
        COMPRENSIONE,
        PRODUZIONE,
        VOCABOLARIO,
        TEST_LIVELLO,
        SESSIONE_GUIDATA,
        ROLEPLAY
    }

    data class SessioneAdattiva(
        val id: String,
        val tipo: TipoSessione,
        val titolo: String,
        val descrizione: String,
        val durataMinuti: Int,
        val priorita: Int,
        val urgente: Boolean = false,
        val azione: String,
        val competenzaTarget: String? = null,
        val paroleTarget: Int = 0
    )

    data class RisultatoGenerazione(
        val sessioni: List<SessioneAdattiva>,
        val livelloAttuale: LivelloCEFR,
        val messaggioMotivazionale: String,
        val areeDeboli: List<String>,
        val tempoTotaleMinuti: Int
    )

    /**
     * Genera sessioni adattive basate sullo stato dell'utente.
     *
     * @param parole tutte le parole dell'utente
     * @param reviews mappa wordId -> ReviewEntity
     * @param progresso progresso utente (errori, livello, test recenti)
     * @param competenze punteggi delle 4 competenze
     * @param daRipassare numero di parole in scadenza
     * @param livelloCorrente livello CEFR dell'utente
     * @param obiettivoLivello livello obiettivo (default B1)
     * @return sessioni generate dinamicamente, ordinate per priorità
     */
    fun generaSessioni(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        progresso: ProgressoUtente,
        competenze: CalcoloCompetenze.PunteggiCompetenze,
        daRipassare: Int,
        livelloCorrente: LivelloCEFR,
        obiettivoLivello: LivelloCEFR = LivelloCEFR.B1
    ): RisultatoGenerazione {
        val sessioni = mutableListOf<SessioneAdattiva>()
        var prioritaCounter = 1
        val areeDeboli = mutableListOf<String>()

        val paroleAttive = parole.filter { !it.archived }

        // 1. RIPASSO: se ci sono parole in scadenza, è la priorità massima
        if (daRipassare > 0) {
            val minuti = (daRipassare * 0.5f).toInt().coerceIn(5, 30)
            sessioni.add(
                SessioneAdattiva(
                    id = "adapt_ripasso",
                    tipo = TipoSessione.RIPASSO,
                    titolo = "Ripasso flashcard",
                    descrizione = "$daRipassare parole in scadenza · ~$minuti min",
                    durataMinuti = minuti,
                    priorita = prioritaCounter++,
                    urgente = daRipassare >= 10,
                    azione = "ripasso",
                    paroleTarget = daRipassare
                )
            )
        }

        // 2. GRAMMATICA: se il punteggio è sotto la soglia
        if (competenze.grammatica < 60f) {
            val minuti = when {
                competenze.grammatica < 30f -> 20
                competenze.grammatica < 50f -> 15
                else -> 10
            }
            val categorieErrori = progresso.erroriGrammatica.take(3)
            val descErrori = if (categorieErrori.isNotEmpty()) {
                "Errori recenti: ${categorieErrori.joinToString("; ")}"
            } else {
                "Grammatica al ${competenze.grammatica.toInt()}%"
            }
            sessioni.add(
                SessioneAdattiva(
                    id = "adapt_grammatica",
                    tipo = TipoSessione.GRAMMATICA,
                    titolo = "Esercizi di grammatica",
                    descrizione = "$descErrori · ~$minuti min",
                    durataMinuti = minuti,
                    priorita = prioritaCounter++,
                    urgente = competenze.grammatica < 40f,
                    azione = "grammatica",
                    competenzaTarget = "Grammatica"
                )
            )
            areeDeboli.add("Grammatica")
        }

        // 3. COMPRENSIONE: se il punteggio è sotto la soglia
        if (competenze.comprensione < 60f) {
            val minuti = when {
                competenze.comprensione < 30f -> 20
                competenze.comprensione < 50f -> 15
                else -> 10
            }
            sessioni.add(
                SessioneAdattiva(
                    id = "adapt_comprensione",
                    tipo = TipoSessione.COMPRENSIONE,
                    titolo = "Quiz di comprensione",
                    descrizione = "Comprensione al ${competenze.comprensione.toInt()}% · ~$minuti min",
                    durataMinuti = minuti,
                    priorita = prioritaCounter++,
                    urgente = competenze.comprensione < 40f,
                    azione = "quiz-comprensione",
                    competenzaTarget = "Comprensione"
                )
            )
            areeDeboli.add("Comprensione")
        }

        // 4. PRODUZIONE: se il punteggio è sotto la soglia
        if (competenze.produzione < 60f) {
            val minuti = when {
                competenze.produzione < 30f -> 20
                competenze.produzione < 50f -> 15
                else -> 10
            }
            sessioni.add(
                SessioneAdattiva(
                    id = "adapt_produzione",
                    tipo = TipoSessione.PRODUZIONE,
                    titolo = "Esercizi di produzione",
                    descrizione = "Produzione al ${competenze.produzione.toInt()}% · ~$minuti min",
                    durataMinuti = minuti,
                    priorita = prioritaCounter++,
                    urgente = competenze.produzione < 40f,
                    azione = "quiz-produzione",
                    competenzaTarget = "Produzione"
                )
            )
            areeDeboli.add("Produzione")
        }

        // 5. VOCABOLARIO: se il lessico è sotto la soglia
        if (competenze.lessico < 50f) {
            val minuti = when {
                competenze.lessico < 20f -> 15
                competenze.lessico < 35f -> 10
                else -> 8
            }
            val nuoveParole = paroleAttive.count { w ->
                val r = reviews[w.id]
                r == null || r.repetitions == 0
            }
            sessioni.add(
                SessioneAdattiva(
                    id = "adapt_vocabolario",
                    tipo = TipoSessione.VOCABOLARIO,
                    titolo = "Nuove parole",
                    descrizione = "Lessico al ${competenze.lessico.toInt()}% · $nuoveParole nuove parole · ~$minuti min",
                    durataMinuti = minuti,
                    priorita = prioritaCounter++,
                    urgente = competenze.lessico < 20f,
                    azione = "nuove",
                    competenzaTarget = "Lessico",
                    paroleTarget = nuoveParole
                )
            )
            areeDeboli.add("Lessico")
        }

        // 6. TEST DI LIVELLO: se l'utente è vicino al livello obiettivo
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
            sessioni.add(
                SessioneAdattiva(
                    id = "adapt_test_livello",
                    tipo = TipoSessione.TEST_LIVELLO,
                    titolo = "Verifica il tuo livello",
                    descrizione = "Test di ${obiettivoLivello.label} · ~15 min",
                    durataMinuti = 15,
                    priorita = prioritaCounter++,
                    urgente = false,
                    azione = "testb1"
                )
            )
        }

        // 7. ROLEPLAY: se l'utente ha un livello sufficiente per conversare
        if (livelloCorrente.ordine >= LivelloCEFR.A1.ordine && competenze.grammatica >= 40f) {
            sessioni.add(
                SessioneAdattiva(
                    id = "adapt_roleplay",
                    tipo = TipoSessione.ROLEPLAY,
                    titolo = "Conversazione guidata",
                    descrizione = "Roleplay in tedesco per livello ${livelloCorrente.label} · ~10 min",
                    durataMinuti = 10,
                    priorita = prioritaCounter++,
                    urgente = false,
                    azione = "roleplay"
                )
            )
        }

        // 8. SESSIONE GUIDATA: se non ci sono priorità o come approfondimento
        if (sessioni.isEmpty() || (daRipassare == 0 && competenze.grammatica >= 60f)) {
            sessioni.add(
                SessioneAdattiva(
                    id = "adapt_sessione_guidata",
                    tipo = TipoSessione.SESSIONE_GUIDATA,
                    titolo = "Sessione di approfondimento",
                    descrizione = "Ripasso completo e nuovi argomenti · ~20 min",
                    durataMinuti = 20,
                    priorita = prioritaCounter++,
                    urgente = false,
                    azione = "ripasso"
                )
            )
        }

        // Se ancora vuoto (caso estremo), crea una sessione di base
        if (sessioni.isEmpty()) {
            sessioni.add(
                SessioneAdattiva(
                    id = "adapt_base",
                    tipo = TipoSessione.SESSIONE_GUIDATA,
                    titolo = "Sessione di studio",
                    descrizione = "Inizia il tuo percorso di studio · ~15 min",
                    durataMinuti = 15,
                    priorita = 1,
                    urgente = false,
                    azione = "ripasso"
                )
            )
        }

        val tempoTotale = sessioni.sumOf { it.durataMinuti }

        val messaggio = when {
            // Chi è agli inizi non ha "priorità": ha solo tanto da imparare. Messaggio incoraggiante.
            competenze.lessico < 30f && daRipassare < 10 ->
                "Sei all'inizio del viaggio. Parti dalle nuove parole: ogni parola conta!"
            sessioni.any { it.urgente } -> "Oggi conviene concentrarsi sulle aree con il badge Priorità."
            daRipassare >= 20 -> "Tante parole da ripassare. Inizia dal ripasso!"
            daRipassare >= 10 -> "Buon ritmo! Mantieni la costanza."
            progresso.streakGiorni >= 7 -> "Streak di ${progresso.streakGiorni} giorni! Sei inarrestabile."
            competenze.grammatica >= 60f && daRipassare == 0 -> "Prosegui così, stai facendo ottimi progressi."
            else -> "Buona sessione di studio!"
        }

        return RisultatoGenerazione(
            sessioni = sessioni,
            livelloAttuale = livelloCorrente,
            messaggioMotivazionale = messaggio,
            areeDeboli = areeDeboli,
            tempoTotaleMinuti = tempoTotale
        )
    }

    /**
     * Genera una sessione di ripasso basata sulle parole deboli.
     * Parole deboli = poche ripetizioni o molti errori.
     */
    fun generaSessioneRipasso(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        maxParole: Int = 20
    ): SessioneAdattiva {
        val paroleDeboli = parole
            .filter { !it.archived }
            .filter { w ->
                val r = reviews[w.id]
                r == null || r.repetitions < 3 || r.lapses > 2
            }
            .take(maxParole)

        val minuti = (paroleDeboli.size * 0.5f).toInt().coerceIn(5, 30)

        return SessioneAdattiva(
            id = "adapt_ripasso_debole",
            tipo = TipoSessione.RIPASSO,
            titolo = "Ripasso parole deboli",
            descrizione = "${paroleDeboli.size} parole deboli · ~$minuti min",
            durataMinuti = minuti,
            priorita = 1,
            urgente = paroleDeboli.size >= 10,
            azione = "ripasso",
            paroleTarget = paroleDeboli.size
        )
    }

    /**
     * Genera una sessione di grammatica basata sugli errori specifici.
     */
    fun generaSessioneGrammatica(
        erroriGrammatica: List<String>,
        livello: LivelloCEFR
    ): SessioneAdattiva {
        val minuti = when {
            erroriGrammatica.size >= 5 -> 20
            erroriGrammatica.size >= 3 -> 15
            else -> 10
        }

        return SessioneAdattiva(
            id = "adapt_grammatica_errori",
            tipo = TipoSessione.GRAMMATICA,
            titolo = "Ripasso errori di grammatica",
            descrizione = "${erroriGrammatica.size} errori da correggere · ~$minuti min",
            durataMinuti = minuti,
            priorita = 1,
            urgente = erroriGrammatica.size >= 5,
            azione = "grammatica",
            competenzaTarget = "Grammatica"
        )
    }
}
