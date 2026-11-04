package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR

/**
 * Report per l'insegnante: uno snapshot leggibile dei dati REALI dell'app
 * (competenze, lessico, produzioni, punteggi delle lezioni, errori SRS).
 *
 * Non contiene segreti né testi delle conversazioni con il tutor: solo ciò che
 * serve a un docente per capire dove intervenire. Funzione pura e testabile.
 */
object ReportInsegnante {

    fun genera(
        nome: String,
        livelloObiettivo: LivelloCEFR,
        pr: CalcoloProgresso.ProgressoReale,
        errori: List<ErroreEntity>,
        punteggiLezioni: Map<Int, Int>,
        data: String = ""
    ): String {
        val aperti = errori.filter { !it.risolto }
        val risolti = errori.count { it.risolto }
        val sb = StringBuilder()
        sb.appendLine("REPORT DI APPRENDIMENTO - TEDESCO")
        sb.appendLine("Studente: ${nome.ifBlank { "Profilo attivo" }}")
        if (data.isNotBlank()) sb.appendLine("Data: $data")
        sb.appendLine()
        sb.appendLine("LIVELLO")
        sb.appendLine("  Attuale: ${pr.livello.label}")
        sb.appendLine("  Obiettivo: ${livelloObiettivo.label}")
        sb.appendLine("  Verso il prossimo livello: ${(pr.versoProssimoLivello * 100).toInt()}%")
        sb.appendLine()
        sb.appendLine("COMPETENZE (0-100)")
        sb.appendLine("  Lessico: ${pr.competenze.lessico.toInt()}")
        sb.appendLine("  Grammatica: ${pr.competenze.grammatica.toInt()}")
        sb.appendLine("  Comprensione: ${pr.competenze.comprensione.toInt()}")
        sb.appendLine("  Produzione: ${pr.competenze.produzione.toInt()}")
        sb.appendLine()
        sb.appendLine("ATTIVITÀ")
        sb.appendLine("  Parole verificate: ${pr.paroleImparate} / ${pr.paroleDisponibili}")
        sb.appendLine("  Parole fragili: ${pr.paroleFragili}")
        sb.appendLine("  Ripassi totali: ${pr.ripassiTotali}")
        sb.appendLine("  Accuratezza: ${pr.accuratezza.toInt()}%")
        sb.appendLine("  Giorni di studio: ${pr.giorniStudioTotali} (streak ${pr.streakGiorni})")
        sb.appendLine("  Minuti stimati: ${pr.minutiStudiati}")
        sb.appendLine("  Testi prodotti: ${pr.produzioniScritte} (media ${pr.mediaProduzione.toInt()}%)")
        sb.appendLine()
        sb.appendLine("PUNTEGGI DELLE LEZIONI")
        if (punteggiLezioni.isEmpty()) {
            sb.appendLine("  Nessuna lezione completata.")
        } else {
            punteggiLezioni.toSortedMap().forEach { (lezione, punteggio) ->
                sb.appendLine("  Lezione $lezione: $punteggio%")
            }
        }
        sb.appendLine()
        sb.appendLine("QUADERNO ERRORI")
        sb.appendLine("  Errori aperti: ${aperti.size} (risolti: $risolti)")
        aperti.take(15).forEach { errore ->
            sb.appendLine("  - [${errore.tipo}/${errore.livello}] ${errore.domanda}")
            sb.appendLine("    corretta: ${errore.rispostaCorretta}")
            if (errore.rispostaData.isNotBlank()) {
                sb.appendLine("    ultima risposta: ${errore.rispostaData}")
            }
        }
        if (aperti.size > 15) sb.appendLine("  ... e altri ${aperti.size - 15} errori.")
        sb.appendLine()
        sb.appendLine("PROSSIMO PASSO CONSIGLIATO")
        sb.appendLine("  ${pr.azioneConsigliata}")
        return sb.toString()
    }

    /** CSV per Excel italiano (separatore ;). Intestazione stabile. */
    fun csvErrori(errori: List<ErroreEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("tipo;livello;domanda;risposta_corretta;ultima_risposta;ripetizioni;stato;prossimo_ripasso")
        errori.sortedBy { it.prossimaRipasso }.forEach { e ->
            sb.appendLine(
                listOf(
                    e.tipo,
                    e.livello,
                    e.domanda,
                    e.rispostaCorretta,
                    e.rispostaData,
                    e.ripetizioni.toString(),
                    if (e.risolto) "risolto" else "aperto",
                    e.prossimaRipasso.toString()
                ).joinToString(";") { campo -> escapeCsv(campo) }
            )
        }
        return sb.toString()
    }

    private fun escapeCsv(campo: String): String {
        val pulito = campo.replace("\r", " ").replace("\n", " ")
        return if (pulito.contains(';') || pulito.contains('"')) {
            "\"" + pulito.replace("\"", "\"\"") + "\""
        } else pulito
    }
}
