package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.EsercizioGenEntity
import com.alessandro.tedesco.data.local.RotazioneUnita

/**
 * Costruisce la sessione di esercizi di una lezione.
 *
 * Il problema che risolve: rifacendo la stessa lezione lo studente ritrovava
 * quasi le stesse domande (spesso con la risposta corretta nella stessa
 * posizione), quindi bastava ripetere la serie per arrivare all'80% senza aver
 * capito la regola. Non basta "mescolare": con un banco di 12 esercizi e una
 * sessione di 8, due tentativi consecutivi condividevano 5-6 domande.
 *
 * Regole:
 * - la sessione non supera mai la metà del banco, così il tentativo successivo
 *   può essere fatto di esercizi COMPLETAMENTE diversi;
 * - una sessione non contiene MAI esercizi della sessione precedente (finché il
 *   banco lo consente): è il vincolo che elimina il "me li ricordo di 20
 *   secondi fa";
 * - la sessione in corso è persistita ([RotazioneUnita.sessione]), quindi
 *   chiudendo l'app non riparte da zero né cambia sotto le mani;
 * - le opzioni di scelta multipla sono rimescolate, ma la risposta corretta
 *   resta corretta.
 *
 * Tutto è deterministico rispetto al seme: stessa sessione = stesse domande.
 */
object SessioneLezione {

    /** Tetto massimo di esercizi per tentativo. */
    const val ESERCIZI_PER_SESSIONE = 8

    /** Sotto questa soglia una sessione sarebbe troppo corta per allenare. */
    const val MINIMO_PER_SESSIONE = 4

    /**
     * Quanti esercizi mostrare con un banco di [banco] esercizi.
     * Mai più della metà del banco: è ciò che rende possibile una sessione
     * successiva senza nessun esercizio in comune.
     */
    fun dimensione(banco: Int, massimo: Int = ESERCIZI_PER_SESSIONE): Int {
        if (banco <= 0) return 0
        val meta = banco / 2
        return minOf(massimo, maxOf(MINIMO_PER_SESSIONE, meta), banco)
    }

    /**
     * Banco completo di una unità: esercizi scritti + esercizi generati dall'AI.
     * I doppioni di contenuto vengono scartati (l'AI può riformulare una
     * domanda già presente).
     */
    fun banco(
        unita: UnitaGrammatica,
        generati: List<EsercizioUnita> = emptyList()
    ): List<EsercizioUnita> {
        if (generati.isEmpty()) return unita.esercizi
        val chiavi = unita.esercizi.mapTo(HashSet()) { chiaveEsercizio(it) }
        val ai = generati.filter { chiavi.add(chiaveEsercizio(it)) }
        return unita.esercizi + ai
    }

    /** Come [banco], partendo dagli esercizi AI ancora nel formato persistito. */
    fun bancoDaGenerati(
        unita: UnitaGrammatica,
        generati: List<EsercizioGenEntity>
    ): List<EsercizioUnita> = banco(unita, generati.mapNotNull { daGenerato(it) })

    /** Converte un esercizio generato dall'AI nel formato delle unità. */
    fun daGenerato(e: EsercizioGenEntity): EsercizioUnita? {
        if (e.domanda.isBlank()) return null
        if (e.opzioni.size < 2) return null
        if (e.rispostaCorretta !in e.opzioni.indices) return null
        // Un esercizio grammaticalmente sbagliato insegna una regola falsa:
        // meglio scartarlo e restare con il banco scritto a mano.
        if (ControlloGrammatica.problemi(e.domanda, e.opzioni, e.rispostaCorretta).isNotEmpty()) {
            return null
        }
        return EsercizioUnita(
            id = "ai-${e.id}",
            tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
            domanda = e.domanda,
            opzioni = e.opzioni,
            rispostaCorretta = e.rispostaCorretta,
            spiegazione = e.spiegazione
        )
    }

    private fun chiaveEsercizio(e: EsercizioUnita): String =
        DeduplicaEsercizi.chiave(e.domanda)

    /**
     * Sessione da mostrare: quella salvata se è ancora valida (stessi esercizi
     * presenti nel banco), altrimenti una nuova.
     */
    fun sessione(
        banco: List<EsercizioUnita>,
        rotazione: RotazioneUnita,
        seme: Int = rotazione.seme
    ): SessioneUnita {
        val perId = banco.associateBy { it.id }
        val salvata = rotazione.sessione.mapNotNull { perId[it] }
        if (salvata.size >= MINIMO_PER_SESSIONE && salvata.size == rotazione.sessione.size) {
            return SessioneUnita(
                esercizi = conOpzioniMescolate(salvata, rotazione.seme),
                rotazione = rotazione
            )
        }
        return nuovaSessione(banco, rotazione, seme)
    }

    /**
     * Costruisce una sessione nuova, escludendo gli esercizi della sessione
     * precedente. Se il ciclo ha già mostrato tutto il banco si riparte, ma
     * sempre senza gli esercizi appena visti.
     */
    fun nuovaSessione(
        banco: List<EsercizioUnita>,
        rotazione: RotazioneUnita,
        seme: Int
    ): SessioneUnita {
        if (banco.isEmpty()) return SessioneUnita(emptyList(), rotazione)
        val idBanco = banco.mapTo(HashSet()) { it.id }
        // Contenuti aggiornati: gli id spariti dal banco non contano più.
        var visti = rotazione.visti.filter { it in idBanco }
        val ultimi = rotazione.ultimi.filter { it in idBanco }
        val obiettivo = dimensione(banco.size)

        var candidati = banco.filter { it.id !in visti }
        if (candidati.size < obiettivo) {
            // Ciclo esaurito: nuovo giro dal banco intero, MAI dagli esercizi
            // dell'ultima sessione (il tentativo dopo non deve essere una copia).
            visti = emptyList()
            candidati = banco.filter { it.id !in ultimi }
            if (candidati.isEmpty()) candidati = banco
        }

        val n = minOf(obiettivo, candidati.size)
        val ordine = RotoreEsercizi.sessione(
            sezione = "sessione-esercizi",
            grandezza = candidati.size,
            quanti = candidati.size,
            seme = seme
        )
        val primaSessione = rotazione.visti.isEmpty()
        val scelti = scegliPerDifficolta(candidati, ordine, n, primaSessione).map { candidati[it] }
        val ids = scelti.map { it.id }
        val nuova = RotazioneUnita(
            visti = (visti + ids).distinct(),
            ultimi = ids,
            sessione = ids,
            seme = seme
        )
        return SessioneUnita(conOpzioniMescolate(scelti, seme), nuova)
    }

    /** Le opzioni cambiano ordine, la risposta corretta resta quella giusta. */
    private fun conOpzioniMescolate(
        esercizi: List<EsercizioUnita>,
        seme: Int
    ): List<EsercizioUnita> = esercizi.map { esercizio ->
        if (esercizio.tipo != TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
            esercizio
        } else {
            val (opzioni, corretta) = RotoreEsercizi.mescolaOpzioni(
                seme = seme * 31 + esercizio.id.hashCode(),
                opzioni = esercizio.opzioni,
                rispostaCorretta = esercizio.rispostaCorretta
            )
            esercizio.copy(opzioni = opzioni, rispostaCorretta = corretta)
        }
    }

    /**
     * Sceglie gli esercizi della sessione rispettando due tetti:
     * - al massimo una PRODUZIONE;
     * - al massimo un altro esercizio "picco" ([DifficoltaEsercizi.PESO_ALTO] o più).
     *
     * Nella prima sessione di una unità la produzione non compare: chi apre
     * l'argomento per la prima volta deve prima vedere la regola in azione.
     * Alla fine la sessione è ordinata per difficoltà crescente, così il
     * tentativo ha una scala e non un salto casuale.
     */
    private fun scegliPerDifficolta(
        banco: List<EsercizioUnita>,
        mescolati: List<Int>,
        n: Int,
        primaSessione: Boolean
    ): List<Int> {
        // In prima sessione la produzione è esclusa; se il banco non ha
        // abbastanza altri esercizi si torna al banco completo, perché una
        // sessione troppo corta sarebbe peggio di una produzione in anticipo.
        val disponibili = if (primaSessione) {
            val senzaProduzione = mescolati.filter {
                banco[it].tipo != TipoEsercizioGrammatica.PRODUZIONE
            }
            if (senzaProduzione.size >= n) senzaProduzione else mescolati
        } else {
            mescolati
        }

        val scelti = LinkedHashSet<Int>()
        var produzioni = 0
        var picchi = 0

        fun ammesso(indice: Int): Boolean {
            val esercizio = banco[indice]
            if (DifficoltaEsercizi.eProduzione(esercizio)) return produzioni < 1
            if (DifficoltaEsercizi.ePicco(esercizio)) return picchi < 1
            return true
        }

        fun aggiungi(indice: Int) {
            if (!scelti.add(indice)) return
            val esercizio = banco[indice]
            if (DifficoltaEsercizi.eProduzione(esercizio)) {
                produzioni++
            } else if (DifficoltaEsercizi.ePicco(esercizio)) {
                // La produzione ha già il suo tetto: qui si contano solo gli
                // altri esercizi molto impegnativi.
                picchi++
            }
        }

        // Un esercizio per ogni tipo presente: non deve mancare la produzione
        // (quando è ammessa) né la trasformazione, altrimenti la sessione
        // allena solo il riconoscimento.
        TipoEsercizioGrammatica.entries.forEach { tipo ->
            if (scelti.size >= n) return@forEach
            disponibili.firstOrNull {
                banco[it].tipo == tipo && it !in scelti && ammesso(it)
            }?.let { aggiungi(it) }
        }
        for (indice in disponibili) {
            if (scelti.size >= n) break
            if (indice in scelti) continue
            if (ammesso(indice)) aggiungi(indice)
        }
        // Rete di sicurezza: se i tetti hanno lasciato meno del minimo, meglio
        // una sessione completa (e un picco in più) che una sessione monca.
        if (scelti.size < minOf(n, MINIMO_PER_SESSIONE)) {
            for (indice in disponibili) {
                if (scelti.size >= n) break
                aggiungi(indice)
            }
        }

        // Facile -> difficile. A parità di punteggio resta l'ordine mescolato,
        // così due sessioni con gli stessi esercizi non sono identiche.
        return scelti.sortedWith(
            compareBy(
                { DifficoltaEsercizi.punteggio(banco[it]) },
                { disponibili.indexOf(it) }
            )
        )
    }
}

/** Esercizi di una sessione + stato di rotazione da salvare. */
data class SessioneUnita(
    val esercizi: List<EsercizioUnita>,
    val rotazione: RotazioneUnita
)
