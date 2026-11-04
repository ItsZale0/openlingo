package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.DomandaTest
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.TipoDomanda
import com.alessandro.tedesco.data.local.WordEntity
import kotlin.random.Random

/**
 * Motore di test adattivi: genera domande dinamicamente basate su
 * livello, errori e progresso dell'utente.
 *
 * Le domande non sono statiche: vengono selezionate e adattate in base a:
 * - Livello CEFR dell'utente
 * - Categorie grammaticali in cui ha più errori
 * - Parole deboli (poche ripetizioni, molti errori)
 * - Prestazioni recenti nei test
 */
object AdaptiveTestEngine {

    data class DomandaAdattiva(
        val id: String,
        val tipo: TipoDomanda,
        val domanda: String,
        val opzioni: List<String>,
        val rispostaCorretta: Int,
        val spiegazione: String,
        val livelloRichiesto: LivelloCEFR,
        val categoria: String? = null,
        val parolaAssociata: String? = null
    )

    data class ConfigurazioneTest(
        val numeroDomande: Int = 10,
        val includeGrammatica: Boolean = true,
        val includeVocabolario: Boolean = true,
        val includeComprensione: Boolean = true,
        val includeProduzione: Boolean = true,
        val seed: Long? = null
    )

    data class RisultatoGenerazione(
        val domande: List<DomandaAdattiva>,
        val livelloEffettivo: LivelloCEFR,
        val categorieCoperture: List<String>,
        val paroleCoperte: List<String>
    )

    /**
     * Genera un set di domande adattive basato sullo stato dell'utente.
     *
     * @param parole tutte le parole dell'utente
     * @param reviews mappa wordId -> ReviewEntity
     * @param progresso progresso utente (errori, livello, test recenti)
     * @param config configurazione del test
     * @return domande generate dinamicamente
     */
    fun generaDomande(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        progresso: ProgressoUtente,
        config: ConfigurazioneTest = ConfigurazioneTest()
    ): RisultatoGenerazione {
        val random = config.seed?.let { Random(it) } ?: Random.Default
        val domande = mutableListOf<DomandaAdattiva>()
        val categorieCoperture = mutableListOf<String>()
        val paroleCoperte = mutableListOf<String>()

        val paroleAttive = parole.filter { !it.archived }
        val livello = progresso.livelloCorrente

        // 1. Domande di grammatica basate sugli errori
        if (config.includeGrammatica) {
            val domandeGrammatica = generaDomandeGrammatica(progresso, livello, config, random)
            domande.addAll(domandeGrammatica)
            categorieCoperture.add("Grammatica")
        }

        // 2. Domande di vocabolario basate sulle parole deboli
        if (config.includeVocabolario) {
            val (domandeVocab, paroleSel) = generaDomandeVocabolario(paroleAttive, reviews, livello, config, random)
            domande.addAll(domandeVocab)
            paroleCoperte.addAll(paroleSel)
            categorieCoperture.add("Vocabolario")
        }

        // 3. Domande di comprensione basate sul livello
        if (config.includeComprensione) {
            val domandeComp = generaDomandeComprensione(livello, config, random)
            domande.addAll(domandeComp)
            categorieCoperture.add("Comprensione")
        }

        // 4. Domande di produzione basate sul livello
        if (config.includeProduzione) {
            val domandeProd = generaDomandeProduzione(livello, config, random)
            domande.addAll(domandeProd)
            categorieCoperture.add("Produzione")
        }

        // Mescola e limita al numero richiesto
        val domandeFinali = domande.shuffled(random).take(config.numeroDomande)

        // Se non abbiamo abbastanza domande, genera domande aggiuntive dalle categorie disponibili
        if (domandeFinali.size < config.numeroDomande) {
            val domandeAggiuntive = mutableListOf<DomandaAdattiva>()
            val categorieDisponibili = mutableListOf<String>()
            if (config.includeGrammatica) categorieDisponibili.add("Grammatica")
            if (config.includeVocabolario) categorieDisponibili.add("Vocabolario")
            if (config.includeComprensione) categorieDisponibili.add("Comprensione")
            if (config.includeProduzione) categorieDisponibili.add("Produzione")

            var domandeRimanenti = config.numeroDomande - domandeFinali.size
            var roundRobinIndex = 0
            var fallbackRound = 0
            while (domandeRimanenti > 0 && categorieDisponibili.isNotEmpty()) {
                val categoria = categorieDisponibili[roundRobinIndex % categorieDisponibili.size]
                val nuoveDomande = when (categoria) {
                    "Grammatica" -> generaDomandeGrammatica(progresso, livello, config, random)
                        .filter { it.id !in domandeFinali.map { d -> d.id } }
                    "Vocabolario" -> generaDomandeVocabolario(paroleAttive, reviews, livello, config, random)
                        .first.filter { it.id !in domandeFinali.map { d -> d.id } }
                    "Comprensione" -> generaDomandeComprensione(livello, config, random, "_fb$fallbackRound")
                        .filter { it.id !in domandeFinali.map { d -> d.id } }
                    "Produzione" -> generaDomandeProduzione(livello, config, random, "_fb$fallbackRound")
                        .filter { it.id !in domandeFinali.map { d -> d.id } }
                    else -> emptyList()
                }
                if (nuoveDomande.isEmpty()) {
                    categorieDisponibili.remove(categoria)
                } else {
                    domandeAggiuntive.addAll(nuoveDomande.take(domandeRimanenti))
                    domandeRimanenti -= nuoveDomande.take(domandeRimanenti).size
                }
                roundRobinIndex++
                fallbackRound++
            }
            return RisultatoGenerazione(
                domande = (domandeFinali + domandeAggiuntive).shuffled(random),
                livelloEffettivo = livello,
                categorieCoperture = categorieCoperture,
                paroleCoperte = paroleCoperte
            )
        }

        return RisultatoGenerazione(
            domande = domandeFinali,
            livelloEffettivo = livello,
            categorieCoperture = categorieCoperture,
            paroleCoperte = paroleCoperte
        )
    }

    /**
     * Genera domande di grammatica basate sulle categorie in cui l'utente ha errori.
     * Se non ci sono errori, genera domande basate sul livello.
     */
    private fun generaDomandeGrammatica(
        progresso: ProgressoUtente,
        livello: LivelloCEFR,
        config: ConfigurazioneTest,
        random: Random
    ): List<DomandaAdattiva> {
        val domande = mutableListOf<DomandaAdattiva>()

        // Categorie con errori
        val categorieConErrori = progresso.erroriGrammatica.map { errore ->
            GrammaticaB1.esercizi.firstOrNull { it.domanda == errore }?.categoria
        }.filterNotNull().toSet()

        // Seleziona esercizi dalle categorie deboli
        val eserciziDeboli = if (categorieConErrori.isNotEmpty()) {
            GrammaticaB1.esercizi.filter { it.categoria in categorieConErrori }
        } else {
            // Nessun errore: usa categorie appropriate per il livello
            val categorieLivello = CategoriaGrammatica.perLivello(livello.name)
            GrammaticaB1.esercizi.filter { it.categoria in categorieLivello }
        }

        val numDomandeGrammatica = config.numeroDomande.coerceAtLeast(2)
        val selezionati = eserciziDeboli.shuffled(random).take(numDomandeGrammatica)

        selezionati.forEach { esercizio ->
            domande.add(
                DomandaAdattiva(
                    id = "adapt_g_${esercizio.id}",
                    tipo = TipoDomanda.GRAMMATICA,
                    domanda = esercizio.domanda,
                    opzioni = esercizio.opzioni,
                    rispostaCorretta = esercizio.rispostaCorretta,
                    spiegazione = esercizio.spiegazione,
                    livelloRichiesto = livello,
                    categoria = esercizio.categoria.name
                )
            )
        }

        return domande
    }

    /**
     * Genera domande di vocabolario basate sulle parole deboli.
     * Parole deboli = poche ripetizioni o molti errori.
     */
    private fun generaDomandeVocabolario(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        livello: LivelloCEFR,
        config: ConfigurazioneTest,
        random: Random
    ): Pair<List<DomandaAdattiva>, List<String>> {
        val domande = mutableListOf<DomandaAdattiva>()
        val paroleCoperte = mutableListOf<String>()

        // Parole deboli: poche ripetizioni o molti errori
        val paroleDeboli = parole
            .filter { w ->
                val r = reviews[w.id]
                r == null || r.repetitions < 3 || r.lapses > 2
            }
            .shuffled(random)

        // Se non ci sono parole deboli, usa tutte le parole disponibili
        val poolCandidati = if (paroleDeboli.isNotEmpty()) paroleDeboli else parole.shuffled(random)

        val numDomandeVocab = config.numeroDomande.coerceAtLeast(2)
        val paroleSelezionate = poolCandidati.take(numDomandeVocab)

        paroleSelezionate.forEach { parola ->
            val tipoDomanda = if (random.nextBoolean()) TipoDomanda.VOCAB_TED_ITA else TipoDomanda.VOCAB_ITA_TED

            val domanda = when (tipoDomanda) {
                TipoDomanda.VOCAB_TED_ITA -> {
                    // Tedesco -> Italiano
                    val opzioniErrate = parole
                        .filter { it.id != parola.id }
                        .shuffled(random)
                        .take(3)
                        .map { it.italian }
                    val opzioni = (opzioniErrate + parola.italian).shuffled(random)
                    val rispostaCorretta = opzioni.indexOf(parola.italian)

                    DomandaAdattiva(
                        id = "adapt_v_${parola.id}",
                        tipo = tipoDomanda,
                        domanda = "Was bedeutet '${parola.german}'?",
                        opzioni = opzioni,
                        rispostaCorretta = rispostaCorretta,
                        spiegazione = "'${parola.german}' significa '${parola.italian}'.",
                        livelloRichiesto = livello,
                        categoria = "Vocabolario",
                        parolaAssociata = parola.german
                    )
                }
                else -> {
                    // Italiano -> Tedesco
                    val opzioniErrate = parole
                        .filter { it.id != parola.id }
                        .shuffled(random)
                        .take(3)
                        .map { it.german }
                    val opzioni = (opzioniErrate + parola.german).shuffled(random)
                    val rispostaCorretta = opzioni.indexOf(parola.german)

                    DomandaAdattiva(
                        id = "adapt_v_${parola.id}",
                        tipo = tipoDomanda,
                        domanda = "Come si dice '${parola.italian}' in tedesco?",
                        opzioni = opzioni,
                        rispostaCorretta = rispostaCorretta,
                        spiegazione = "'${parola.italian}' in tedesco è '${parola.german}'.",
                        livelloRichiesto = livello,
                        categoria = "Vocabolario",
                        parolaAssociata = parola.german
                    )
                }
            }

            domande.add(domanda)
            paroleCoperte.add(parola.german)
        }

        return Pair(domande, paroleCoperte)
    }

    /**
     * Genera domande di comprensione basate sul livello.
     * Le domande sono generate dinamicamente in base al livello CEFR.
     */
    private fun generaDomandeComprensione(
        livello: LivelloCEFR,
        config: ConfigurazioneTest,
        random: Random,
        idSuffix: String = ""
    ): List<DomandaAdattiva> {
        val domande = mutableListOf<DomandaAdattiva>()
        val numDomandeComp = config.numeroDomande.coerceAtLeast(2)

        // Genera domande di comprensione in base al livello
        val domandeBase = when (livello) {
            LivelloCEFR.A0, LivelloCEFR.A1 -> listOf(
                DomandaAdattiva(
                    id = "adapt_c${idSuffix}_1",
                    tipo = TipoDomanda.TRADUZIONE,
                    domanda = "Was bedeutet 'der Tisch'?",
                    opzioni = listOf("il tavolo", "la sedia", "la porta", "la finestra"),
                    rispostaCorretta = 0,
                    spiegazione = "'Der Tisch' significa 'il tavolo'.",
                    livelloRichiesto = livello,
                    categoria = "Comprensione"
                ),
                DomandaAdattiva(
                    id = "adapt_c${idSuffix}_2",
                    tipo = TipoDomanda.TRADUZIONE,
                    domanda = "Was bedeutet 'die Küche'?",
                    opzioni = listOf("il bagno", "la cucina", "la camera", "il soggiorno"),
                    rispostaCorretta = 1,
                    spiegazione = "'Die Küche' significa 'la cucina'.",
                    livelloRichiesto = livello,
                    categoria = "Comprensione"
                ),
                DomandaAdattiva(
                    id = "adapt_c${idSuffix}_3",
                    tipo = TipoDomanda.TRADUZIONE,
                    domanda = "Was bedeutet 'Ich habe zwei Brüder.'?",
                    opzioni = listOf("Io ho due fratelli.", "Io ho due sorelle.", "Io vado dal fratello.", "Io amo i miei fratelli."),
                    rispostaCorretta = 0,
                    spiegazione = "'Zwei Brüder' = due fratelli.",
                    livelloRichiesto = livello,
                    categoria = "Comprensione"
                )
            )
            LivelloCEFR.A2 -> listOf(
                DomandaAdattiva(
                    id = "adapt_c${idSuffix}_4",
                    tipo = TipoDomanda.TRADUZIONE,
                    domanda = "Was bedeutet 'Ich wohne in Berlin.'?",
                    opzioni = listOf("Abito a Berlino.", "Lavoro a Berlino.", "Vado a Berlino.", "Amo Berlino."),
                    rispostaCorretta = 0,
                    spiegazione = "'Wohnen' = abitare.",
                    livelloRichiesto = livello,
                    categoria = "Comprensione"
                ),
                DomandaAdattiva(
                    id = "adapt_c${idSuffix}_5",
                    tipo = TipoDomanda.TRADUZIONE,
                    domanda = "Was bedeutet 'Das Wetter ist schön.'?",
                    opzioni = listOf("Il tempo è bello.", "Il tempo è brutto.", "Fa freddo.", "Fa caldo."),
                    rispostaCorretta = 0,
                    spiegazione = "'Schön' = bello.",
                    livelloRichiesto = livello,
                    categoria = "Comprensione"
                )
            )
            else -> listOf(
                DomandaAdattiva(
                    id = "adapt_c${idSuffix}_6",
                    tipo = TipoDomanda.TRADUZIONE,
                    domanda = "Was bedeutet 'Ich freue mich auf deinen Besuch.'?",
                    opzioni = listOf("Non vedo l'ora della tua visita.", "Sono felice della tua visita.", "Aspetto la tua visita.", "Mi piace la tua visita."),
                    rispostaCorretta = 0,
                    spiegazione = "'Sich freuen auf' = non vedere l'ora di (futuro).",
                    livelloRichiesto = livello,
                    categoria = "Comprensione"
                ),
                DomandaAdattiva(
                    id = "adapt_c${idSuffix}_7",
                    tipo = TipoDomanda.TRADUZIONE,
                    domanda = "Was bedeutet 'Er hat mir gesagt, dass er nicht kommt.'?",
                    opzioni = listOf("Mi ha detto che non viene.", "Mi ha detto che viene.", "Mi ha detto che non è venuto.", "Mi ha detto che verrà."),
                    rispostaCorretta = 0,
                    spiegazione = "'dass er nicht kommt' = che non viene.",
                    livelloRichiesto = livello,
                    categoria = "Comprensione"
                )
            )
        }

        domande.addAll(domandeBase.shuffled(random).take(numDomandeComp))
        return domande
    }

    /**
     * Genera domande di produzione basate sul livello.
     */
    private fun generaDomandeProduzione(
        livello: LivelloCEFR,
        config: ConfigurazioneTest,
        random: Random,
        idSuffix: String = ""
    ): List<DomandaAdattiva> {
        val domande = mutableListOf<DomandaAdattiva>()
        val numDomandeProd = config.numeroDomande.coerceAtLeast(2)

        val domandeBase = when (livello) {
            LivelloCEFR.A0, LivelloCEFR.A1 -> listOf(
                DomandaAdattiva(
                    id = "adapt_p${idSuffix}_1",
                    tipo = TipoDomanda.COMPLETAMENTO,
                    domanda = "Ich ___ gestern ins Kino gegangen.",
                    opzioni = listOf("bin", "habe", "war", "hatte"),
                    rispostaCorretta = 0,
                    spiegazione = "Perfekt con 'sein' per verbi di movimento.",
                    livelloRichiesto = livello,
                    categoria = "Produzione"
                ),
                DomandaAdattiva(
                    id = "adapt_p${idSuffix}_2",
                    tipo = TipoDomanda.COMPLETAMENTO,
                    domanda = "Er ___ die Hausaufgaben gemacht.",
                    opzioni = listOf("hat", "ist", "war", "wird"),
                    rispostaCorretta = 0,
                    spiegazione = "Perfekt con 'haben' per la maggior parte dei verbi.",
                    livelloRichiesto = livello,
                    categoria = "Produzione"
                )
            )
            LivelloCEFR.A2 -> listOf(
                DomandaAdattiva(
                    id = "adapt_p${idSuffix}_3",
                    tipo = TipoDomanda.COMPLETAMENTO,
                    domanda = "Wir ___ nächste Woche nach Deutschland reisen.",
                    opzioni = listOf("werden", "sind", "haben", "waren"),
                    rispostaCorretta = 0,
                    spiegazione = "Futur con 'werden'.",
                    livelloRichiesto = livello,
                    categoria = "Produzione"
                ),
                DomandaAdattiva(
                    id = "adapt_p${idSuffix}_4",
                    tipo = TipoDomanda.COMPLETAMENTO,
                    domanda = "Ich freue mich ___ deinen Besuch.",
                    opzioni = listOf("auf", "über", "für", "mit"),
                    rispostaCorretta = 0,
                    spiegazione = "'Sich freuen auf' = attendere con gioia (futuro).",
                    livelloRichiesto = livello,
                    categoria = "Produzione"
                )
            )
            else -> listOf(
                DomandaAdattiva(
                    id = "adapt_p${idSuffix}_5",
                    tipo = TipoDomanda.COMPLETAMENTO,
                    domanda = "Ich ___ gern nach Deutschland reisen.",
                    opzioni = listOf("würde", "werde", "würde", "werde"),
                    rispostaCorretta = 0,
                    spiegazione = "Konjunktiv II per desideri irreali.",
                    livelloRichiesto = livello,
                    categoria = "Produzione"
                ),
                DomandaAdattiva(
                    id = "adapt_p${idSuffix}_6",
                    tipo = TipoDomanda.COMPLETAMENTO,
                    domanda = "Er hat mir gesagt, dass er ___ kommt.",
                    opzioni = listOf("nicht", "kein", "niemals", "nichts"),
                    rispostaCorretta = 0,
                    spiegazione = "'dass' introduce una frase subordinata.",
                    livelloRichiesto = livello,
                    categoria = "Produzione"
                )
            )
        }

        domande.addAll(domandeBase.shuffled(random).take(numDomandeProd))
        return domande
    }

    /**
     * Genera un test completo adattivo (grammatica, vocabolario, comprensione, produzione)
     * basato sullo stato attuale dell'utente.
     *
     * I quesiti non sono statici: vengono scelti e adattati in base a:
     * - categorie grammaticali con errori
     * - parole deboli (poche ripetizioni / molti errori)
     * - livello CEFR
     */
    fun generaTestAdattivo(
        progresso: ProgressoUtente,
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        config: ConfigurazioneTest = ConfigurazioneTest()
    ): List<DomandaTest> {
        val domandeAdattive = generaDomande(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            config = config
        )
        return domandeAdattive.domande.map { adapt ->
            DomandaTest(
                id = adapt.id,
                tipo = adapt.tipo,
                domanda = adapt.domanda,
                opzioni = adapt.opzioni,
                rispostaCorretta = adapt.rispostaCorretta,
                spiegazione = adapt.spiegazione,
                livelloRichiesto = adapt.livelloRichiesto,
                tagGrammatica = adapt.categoria
            )
        }
    }

    /**
     * Calcola il livello adattivo effettivo basato sulle prestazioni recenti.
     * Se l'utente ha fatto molti errori, abbassa il livello.
     * Se ha fatto pochi errori, alza il livello.
     */
    fun calcolaLivelloAdattivo(
        progresso: ProgressoUtente,
        domandeTotali: Int,
        errori: Int
    ): LivelloCEFR {
        val accuratezza = if (domandeTotali == 0) 0f
        else (domandeTotali - errori).toFloat() / domandeTotali

        return when {
            accuratezza >= 0.9f && progresso.livelloCorrente.ordine < LivelloCEFR.C2.ordine ->
                LivelloCEFR.entries[progresso.livelloCorrente.ordine + 1]
            accuratezza < 0.5f && progresso.livelloCorrente.ordine > LivelloCEFR.A0.ordine ->
                LivelloCEFR.entries[progresso.livelloCorrente.ordine - 1]
            else -> progresso.livelloCorrente
        }
    }
}