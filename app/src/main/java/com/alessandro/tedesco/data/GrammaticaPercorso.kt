package com.alessandro.tedesco.data

import kotlinx.serialization.Serializable
import java.util.Locale
import java.util.concurrent.TimeUnit

/*
 * Percorso grammaticale strutturato A0 -> B2.
 *
 * La grammatica non è più una raccolta di quiz casuali: ogni unità ha
 * una regola, tabelle, esempi, errori tipici, esercizi e una verifica di
 * padronanza. Lo sblocco della unità successiva dipende dai prerequisiti
 * e dalla padronanza (>= 80%).
 */

enum class TipoEsercizioGrammatica {
    SCELTA_MULTIPLA,
    COMPLETAMENTO,
    TRASFORMAZIONE,
    PRODUZIONE
}

/** Una tabella didattica (declinazioni, coniugazioni, ecc.). */
data class TabellaGrammatica(
    val titolo: String,
    val intestazioni: List<String>,
    val righe: List<List<String>>,
    val nota: String = ""
)

/** Un esempio tedesco con traduzione italiana. */
data class EsempioGrammatica(
    val tedesco: String,
    val italiano: String,
    val nota: String = ""
)

/** Errore tipico dell'italofono, con la forma corretta e il perché. */
data class ErroreTipico(
    val sbagliato: String,
    val corretto: String,
    val spiegazione: String
)

/** Teoria completa di una unità. */
data class TeoriaGrammatica(
    val regola: List<String>,
    val tabelle: List<TabellaGrammatica> = emptyList(),
    val esempi: List<EsempioGrammatica> = emptyList(),
    val erroriTipici: List<ErroreTipico> = emptyList(),
    val merksatz: String = ""
)

/** Esercizio di una unità del percorso. */
data class EsercizioUnita(
    val id: String,
    val tipo: TipoEsercizioGrammatica,
    val domanda: String,
    val opzioni: List<String> = emptyList(),
    /** Indice della risposta corretta per SCELTA_MULTIPLA. */
    val rispostaCorretta: Int = 0,
    /** Risposta attesa per gli esercizi a testo libero. */
    val rispostaTesto: String = "",
    /** Altre risposte accettate (maiuscole, varianti, ecc.). */
    val alternative: List<String> = emptyList(),
    val spiegazione: String = "",
    val suggerimento: String = ""
)

/** Una unità del percorso grammaticale. */
data class UnitaGrammatica(
    val id: String,
    val livello: String,
    val titolo: String,
    val sottotitolo: String,
    val obiettivo: String,
    val minuti: Int,
    val prerequisiti: List<String> = emptyList(),
    val teoria: TeoriaGrammatica,
    val esercizi: List<EsercizioUnita>
)

/** Stato calcolato di una unità rispetto alla padronanza dell'utente. */
enum class StatoUnita {
    BLOCCATA,
    DISPONIBILE,
    COMPLETATA,
    DA_RIPASSARE
}

/** Unità + stato + padronanza: è cio' che consuma la UI. */
data class UnitaConStato(
    val unita: UnitaGrammatica,
    val stato: StatoUnita,
    val padronanza: PadronanzaGrammatica?,
    /**
     * True se lo studente può aprirla: prerequisiti fatti OPPURE livello già
     * raggiunto. È diverso da [stato]: una unità sotto il livello del profilo
     * resta formalmente "bloccata" dai prerequisiti, ma deve essere apribile.
     */
    val accessibile: Boolean = true
)

/**
 * Padronanza persistente di una unità.
 * I campi hanno default per essere compatibili con i profili già salvati.
 */
@Serializable
data class PadronanzaGrammatica(
    val unitaId: String = "",
    val corrette: Int = 0,
    val totali: Int = 0,
    val percentuale: Int = 0,
    val completata: Boolean = false,
    val ultimaVolta: Long = 0L,
    val prossimaRipasso: Long = 0L,
    val ripassi: Int = 0
)

/** Tappa futura del syllabus, mostrata nel percorso come "in arrivo". */
data class TappaPercorso(
    val livello: String,
    val titolo: String
)

object GrammaticaSyllabus {

    const val SOGLIA_PADRONANZA = 80

    private val GIORNO_MS = TimeUnit.DAYS.toMillis(1)

    /**
     * Percorso completo A0 -> B2. L'ordine è didattico: prima le basi,
     * poi i casi, i tempi del passato, le strutture B1 e infine il B2.
     * Gli ID sono stabili perché sono le chiavi della padronanza salvata.
     */
    private val unitaBase: List<UnitaGrammatica> = listOf(
        unitaA0Alfabeto(),
        unitaA0PronomiSein(),
        unitaA0Articoli(),
        unitaA0Plurale(),
        unitaA0Frase(),
        unitaPraesens(),
        unitaAkkusativ(),
        unitaA1Possessivi(),
        unitaA1Modali(),
        unitaA1Separabili(),
        unitaA1PreposizioniLuogo(),
        unitaA1PreposizioniTempo(),
        unitaA1Imperativo(),
        unitaA1Fragewoerter(),
        unitaPerfekt(),
        unitaA2Dativo(),
        unitaA2Wechselpraepositionen(),
        unitaA2Praeteritum(),
        unitaA2Riflessivi(),
        unitaA2Subordinate(),
        unitaA2Relative(),
        unitaA2Passivo(),
        unitaA2Komparativ(),
        // ---- A2 aggiunte (gap audit) ----
        unitaA2Adjektivdeklination(),
        unitaA2Konjunktiv2Basis(),
        unitaA2VerbenPraepositionen(),
        unitaA2Indefinitpronomen(),
        unitaA2IndirekteFragen(),
        // ---- A2 residui (Grammatik Aktiv) ----
        unitaA2VerbenAkkDativ(),
        unitaB1Futur(),
        unitaB1Adjektivdeklination(),
        unitaB1Partizipien(),
        unitaB1Konnektoren(),
        unitaB1Doppelkonnektoren(),
        unitaB1Konjunktiv2(),
        unitaB1IndirekteRede(),
        unitaB1Nominalisierung(),
        unitaB1Genitiv(),
        // ---- B1 aggiunte (gap audit) ----
        unitaB1Plusquamperfekt(),
        unitaB1NDeklination(),
        unitaB1GenitivPraepositionen(),
        unitaB1AdjektiveAlsNomen(),
        unitaB1Praepositionaladverbien(),
        unitaB1Komparativsaetze(),
        // ---- B1 residui (Grammatik Aktiv) ----
        unitaB1PassivVergangenheit(),
        unitaB1TemporaleNebensaetze(),
        // ---- B2 ----
        unitaB2PassivModal(),
        unitaB2Konjunktiv2Vergangenheit(),
        unitaB2Partizipialkonstruktionen(),
        unitaB2VerbenPraepositionen(),
        unitaB2RelativeWasWo(),
        unitaB2Nominalstil(),
        // ---- B2 aggiunte (gap audit) ----
        unitaB2PassivErsatz(),
        unitaB2Zustandspassiv(),
        unitaB2Modalpartikeln(),
        unitaB2Referenzwoerter()
    )

    /**
     * L'unità del percorso più il suo banco extra di esercizi (fonte:
     * [EserciziExtra], syllabus rifinito sul repertorio Grammatik Aktiv):
     * più materia per lo stesso argomento, per chi vuole continuare
     * ad esercitare. Gli id degli esercizi extra usano il suffisso "-x"
     * per non toccare le chiavi di padronanza già salvate.
     */
    val unita: List<UnitaGrammatica> = unitaBase.map { u ->
        val extra = EserciziExtra[u.id].orEmpty() + EserciziExtra2[u.id].orEmpty()
        if (extra.isEmpty()) u
        else u.copy(esercizi = u.esercizi + extra)
    }

    val ordineLivelli = listOf("A0", "A1", "A2", "B1", "B2")

    /**
     * Il percorso A0 -> B2 è completo. Non ci sono più tappe "in arrivo":
     * la UI usa questa lista vuota per mostrare il messaggio di percorso completo.
     */
    val tappeFuture: List<TappaPercorso> = emptyList()

    fun perId(id: String): UnitaGrammatica? = unita.firstOrNull { it.id == id }

    /**
     * Calcola lo stato di tutte le unità a partire dalla mappa salvata.
     *
     * [livelloProfilo] serve a NON lasciare tutto bloccato quando lo studente
     * cambia livello a mano: chi è B1 deve poter aprire tutte le unità fino al
     * B1 anche senza i prerequisiti, altrimenti il cambio livello non produce
     * nessun effetto visibile.
     */
    fun percorso(
        mappa: Map<String, PadronanzaGrammatica>,
        now: Long = System.currentTimeMillis(),
        livelloProfilo: String? = null
    ): List<UnitaConStato> {
        val soglia = sogliaLivello(livelloProfilo)
        return unita.map { unita ->
            val padronanza = mappa[unita.id]
            val stato = when {
                padronanza?.completata == true &&
                    padronanza.prossimaRipasso > 0L &&
                    padronanza.prossimaRipasso <= now -> StatoUnita.DA_RIPASSARE

                padronanza?.completata == true -> StatoUnita.COMPLETATA
                sbloccata(unita, mappa) -> StatoUnita.DISPONIBILE
                else -> StatoUnita.BLOCCATA
            }
            val perLivello = soglia >= 0 && ordineLivello(unita.livello) <= soglia
            UnitaConStato(unita, stato, padronanza, stato != StatoUnita.BLOCCATA || perLivello)
        }
    }

    /** Ordine CEFR di un livello ("B1 - Intermedio" -> 3). -1 se assente. */
    private fun sogliaLivello(livello: String?): Int {
        if (livello.isNullOrBlank()) return -1
        val codice = livello.uppercase(Locale.ROOT)
            .substringBefore(" - ")
            .substringBefore(" ")
            .trim()
        return ordineLivelli.indexOf(codice)
    }

    private fun ordineLivello(livello: String?): Int {
        val indice = sogliaLivello(livello)
        return if (indice < 0) ordineLivelli.lastIndex else indice
    }

    /** Una unità è sbloccata quando tutti i prerequisiti sono completati. */
    fun sbloccata(
        unita: UnitaGrammatica,
        mappa: Map<String, PadronanzaGrammatica>
    ): Boolean = unita.prerequisiti.all { mappa[it]?.completata == true }

    /**
     * Prossima azione consigliata: prima i ripassi scaduti, poi la prima
     * unità disponibile, infine una unità completata da ripassare a mano.
     * Con [livelloProfilo] non propone unità troppo facili per chi ha già un
     * livello alto (Emma B1 non deve ripartire dall'alfabeto).
     */
    fun prossimaUnita(
        mappa: Map<String, PadronanzaGrammatica>,
        now: Long = System.currentTimeMillis(),
        livelloProfilo: String? = null
    ): UnitaConStato? {
        val lista = percorso(mappa, now, livelloProfilo)
        val soglia = sogliaLivello(livelloProfilo)
        return lista.firstOrNull { it.stato == StatoUnita.DA_RIPASSARE }
            ?: lista.firstOrNull {
                it.accessibile && it.stato != StatoUnita.COMPLETATA &&
                    soglia >= 0 && ordineLivello(it.unita.livello) >= soglia
            }
            ?: lista.firstOrNull { it.accessibile && it.stato != StatoUnita.COMPLETATA }
            ?: lista.firstOrNull { it.stato == StatoUnita.COMPLETATA }
    }

    /**
     * Calcola la padronanza dopo un tentativo.
     *
     * - >= 80%: unità completata; il ripasso si sposta a 1, 3, 7, 16 giorni.
     * - < 80%: si resta sulla unità; se era già completata non viene
     *   "richiusa" (non si perde l'accesso), ma il ripasso torna a domani.
     */
    fun valuta(
        unitaId: String,
        corrette: Int,
        totali: Int,
        precedente: PadronanzaGrammatica? = null,
        now: Long = System.currentTimeMillis()
    ): PadronanzaGrammatica {
        val totale = totali.coerceAtLeast(1)
        val corretteValide = corrette.coerceIn(0, totale)
        val percentuale = corretteValide * 100 / totale
        val promosso = percentuale >= SOGLIA_PADRONANZA
        val giaCompletata = precedente?.completata == true
        val completata = promosso || giaCompletata
        val ripassi = if (promosso) {
            (precedente?.ripassi ?: 0) + 1
        } else {
            precedente?.ripassi ?: 0
        }
        val giorni = if (promosso) intervalloRipassoGiorni(ripassi) else 1
        return PadronanzaGrammatica(
            unitaId = unitaId,
            corrette = corretteValide,
            totali = totale,
            percentuale = percentuale,
            completata = completata,
            ultimaVolta = now,
            prossimaRipasso = now + giorni * GIORNO_MS,
            ripassi = ripassi
        )
    }

    /** Intervallo di ripasso spaziato: 1, 3, 7, 16 giorni. */
    fun intervalloRipassoGiorni(ripassi: Int): Int = when {
        ripassi <= 1 -> 1
        ripassi == 2 -> 3
        ripassi == 3 -> 7
        else -> 16
    }

    /** Normalizza una risposta libera: spazi, maiuscole, punteggiatura finale. */
    fun normalizza(testo: String): String = testo
        .trim()
        .lowercase(Locale.GERMAN)
        .replace(Regex("\\s+"), " ")
        .trim()
        .trimEnd('.', '!', '?', ',', ';', ':')
        .trim()

    /** Accetta anche la scrittura senza umlaut (ae, oe, ue, ss). */
    fun traslittera(testo: String): String = normalizza(testo)
        .replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("ß", "ss")

    fun rispostaSceltaCorretta(esercizio: EsercizioUnita, indice: Int?): Boolean =
        esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA &&
            indice == esercizio.rispostaCorretta

    fun rispostaTestoCorretta(esercizio: EsercizioUnita, risposta: String): Boolean {
        val attese = listOf(esercizio.rispostaTesto) + esercizio.alternative
        val normalizzata = normalizza(risposta)
        val traslitterata = traslittera(risposta)
        return attese.any { attesa ->
            attesa.isNotBlank() && (
                normalizza(attesa) == normalizzata ||
                    traslittera(attesa) == traslitterata
                )
        }
    }

    // =====================================================================
    // UNITA' 1 - A1 Akkusativ
    // =====================================================================

    private fun unitaAkkusativ(): UnitaGrammatica = UnitaGrammatica(
        id = "a1-akkusativ",
        livello = "A1",
        titolo = "L'accusativo",
        sottotitolo = "Il caso del complemento oggetto",
        obiettivo = "Usare correttamente articoli, possessivi e pronomi all'accusativo.",
        minuti = 12,
        prerequisiti = listOf("a1-praesens"),
        teoria = TeoriaGrammatica(
            regola = listOf(
                "L'accusativo è il caso del complemento oggetto: risponde a **Wen?** (chi?) o **Was?** (cosa?).",
                "Per gli articoli e i possessivi cambia solo il **maschile singolare**: der -> den, ein -> einen, mein -> meinen, kein -> keinen.",
                "Femminile, neutro e plurale restano uguali al nominativo: die, das, die; eine, ein, eine.",
                "Dopo verbi come haben, sehen, kaufen, brauchen, essen, trinken, lesen, mögen, suchen e finden si usa l'accusativo.",
                "Anche le preposizioni durch, für, gegen, ohne e um reggono l'accusativo."
            ),
            tabelle = listOf(
                TabellaGrammatica(
                    titolo = "Articoli definiti",
                    intestazioni = listOf("", "Maschile", "Femminile", "Neutro", "Plurale"),
                    righe = listOf(
                        listOf("Nominativo", "der", "die", "das", "die"),
                        listOf("Accusativo", "den", "die", "das", "die")
                    ),
                    nota = "Solo il maschile cambia: der -> den."
                ),
                TabellaGrammatica(
                    titolo = "Articoli indefiniti",
                    intestazioni = listOf("", "Maschile", "Femminile", "Neutro", "Plurale"),
                    righe = listOf(
                        listOf("Nominativo", "ein", "eine", "ein", "-"),
                        listOf("Accusativo", "einen", "eine", "ein", "-")
                    ),
                    nota = "Al plurale non si usa l'articolo indefinito."
                ),
                TabellaGrammatica(
                    titolo = "Pronomi personali",
                    intestazioni = listOf("Nominativo", "Accusativo"),
                    righe = listOf(
                        listOf("ich", "mich"),
                        listOf("du", "dich"),
                        listOf("er", "ihn"),
                        listOf("sie", "sie"),
                        listOf("es", "es"),
                        listOf("wir", "uns"),
                        listOf("ihr", "euch"),
                        listOf("sie / Sie", "sie / Sie")
                    ),
                    nota = "er diventa ihn; gli altri pronomi restano uguali."
                )
            ),
            esempi = listOf(
                EsempioGrammatica("Ich sehe den Mann.", "Vedo l'uomo."),
                EsempioGrammatica("Sie kauft einen Apfel.", "Lei compra una mela."),
                EsempioGrammatica("Wir haben das Buch.", "Abbiamo il libro."),
                EsempioGrammatica("Er besucht seine Mutter.", "Lui visita sua madre."),
                EsempioGrammatica("Ich brauche keine Hilfe.", "Non ho bisogno di aiuto."),
                EsempioGrammatica("Kennst du ihn?", "Lo conosci?")
            ),
            erroriTipici = listOf(
                ErroreTipico(
                    sbagliato = "Ich sehe der Mann.",
                    corretto = "Ich sehe den Mann.",
                    spiegazione = "Mann è maschile: all'accusativo der diventa den."
                ),
                ErroreTipico(
                    sbagliato = "Ich habe ein Hund.",
                    corretto = "Ich habe einen Hund.",
                    spiegazione = "Hund è maschile: ein diventa einen."
                ),
                ErroreTipico(
                    sbagliato = "Ich kaufe die Buch.",
                    corretto = "Ich kaufe das Buch.",
                    spiegazione = "Buch è neutro: l'articolo è das, non die."
                ),
                ErroreTipico(
                    sbagliato = "Ich sehe er.",
                    corretto = "Ich sehe ihn.",
                    spiegazione = "Il pronome er diventa ihn all'accusativo."
                )
            ),
            merksatz = "Maschile all'accusativo: der -> den, ein -> einen. Femminile, neutro e plurale non cambiano."
        ),
        esercizi = listOf(
            EsercizioUnita(
                id = "akk-01",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Ich sehe ___ Mann.",
                opzioni = listOf("der", "den", "dem", "das"),
                rispostaCorretta = 1,
                spiegazione = "Mann è maschile; all'accusativo der diventa den."
            ),
            EsercizioUnita(
                id = "akk-02",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Sie kauft ___ Apfel.",
                opzioni = listOf("ein", "einen", "eine", "einem"),
                rispostaCorretta = 1,
                spiegazione = "Apfel è maschile; ein diventa einen."
            ),
            EsercizioUnita(
                id = "akk-03",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Wir haben ___ Buch.",
                opzioni = listOf("der", "die", "das", "den"),
                rispostaCorretta = 2,
                spiegazione = "Buch è neutro; all'accusativo resta das."
            ),
            EsercizioUnita(
                id = "akk-04",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Er besucht ___ Mutter.",
                opzioni = listOf("sein", "seinen", "seine", "seiner"),
                rispostaCorretta = 2,
                spiegazione = "Mutter è femminile; il possessivo resta seine."
            ),
            EsercizioUnita(
                id = "akk-05",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Ich brauche ___ Hilfe.",
                opzioni = listOf("kein", "keine", "keinen", "keiner"),
                rispostaCorretta = 1,
                spiegazione = "Hilfe è femminile; kein diventa keine."
            ),
            EsercizioUnita(
                id = "akk-06",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Ich trinke ___ Kaffee.",
                rispostaTesto = "einen",
                alternative = listOf("einen"),
                spiegazione = "Kaffee è maschile: ein diventa einen.",
                suggerimento = "Scrivi solo l'articolo mancante."
            ),
            EsercizioUnita(
                id = "akk-07",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Wir suchen ___ Schlüssel.",
                rispostaTesto = "den",
                alternative = listOf("den"),
                spiegazione = "Schlüssel è maschile: der diventa den.",
                suggerimento = "Scrivi solo l'articolo mancante."
            ),
            EsercizioUnita(
                id = "akk-08",
                tipo = TipoEsercizioGrammatica.TRASFORMAZIONE,
                domanda = "Riscrivi all'accusativo: Der Hund ist groß. -> Ich sehe ___.",
                rispostaTesto = "Ich sehe den Hund.",
                alternative = listOf("ich sehe den hund", "den Hund"),
                spiegazione = "Hund è maschile: der diventa den.",
                suggerimento = "Scrivi la frase completa."
            ),
            EsercizioUnita(
                id = "akk-09",
                tipo = TipoEsercizioGrammatica.TRASFORMAZIONE,
                domanda = "Riscrivi all'accusativo: Ein Tisch ist frei. -> Wir brauchen ___.",
                rispostaTesto = "Wir brauchen einen Tisch.",
                alternative = listOf("wir brauchen einen tisch", "einen Tisch"),
                spiegazione = "Tisch è maschile: ein diventa einen.",
                suggerimento = "Scrivi la frase completa."
            ),
            EsercizioUnita(
                id = "akk-10",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Kennst du ___? (er)",
                rispostaTesto = "ihn",
                alternative = listOf("ihn"),
                spiegazione = "er diventa ihn all'accusativo.",
                suggerimento = "Scrivi solo il pronome."
            ),
            EsercizioUnita(
                id = "akk-11",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Ich sehe ___. (sie, plurale)",
                rispostaTesto = "sie",
                alternative = listOf("sie"),
                spiegazione = "sie plurale resta sie anche all'accusativo.",
                suggerimento = "Scrivi solo il pronome."
            ),
            EsercizioUnita(
                id = "akk-12",
                tipo = TipoEsercizioGrammatica.PRODUZIONE,
                domanda = "Traduci: Compro una mela.",
                rispostaTesto = "Ich kaufe einen Apfel.",
                alternative = listOf("ich kaufe einen apfel", "ich kaufe mir einen apfel"),
                spiegazione = "Apfel è maschile: einen Apfel.",
                suggerimento = "Usa il verbo kaufen e l'accusativo."
            )
        )
    )

    // =====================================================================
    // UNITA' 2 - A1 Praesens
    // =====================================================================

    private fun unitaPraesens(): UnitaGrammatica = UnitaGrammatica(
        id = "a1-praesens",
        livello = "A1",
        titolo = "Il presente",
        sottotitolo = "Verbi regolari, irregolari e ordine della frase",
        obiettivo = "Coniugare i verbi al presente e costruire frasi principali corrette.",
        minuti = 15,
        prerequisiti = listOf("a0-frase"),
        teoria = TeoriaGrammatica(
            regola = listOf(
                "Il presente si forma dalla radice del verbo + desinenza: **-e, -st, -t, -en, -t, -en**.",
                "Se la radice finisce in **-d** o **-t**, aggiungi una **-e-** prima della desinenza: du arbeitest, er arbeitet.",
                "Se la radice finisce in **-s, -ss, -z**, la seconda persona singolare non aggiunge la **-s**: du reist, du heißt.",
                "Alcuni verbi cambiano la vocale alla 2ª e 3ª persona singolare: a -> ä (fahren -> du fährst), e -> i/ie (sprechen -> du sprichst, sehen -> du siehst), au -> äu (laufen -> du läufst).",
                "sein e haben sono irregolari: ich bin, du bist, er ist; ich habe, du hast, er hat.",
                "Nella frase principale il verbo coniugato sta in **seconda posizione**. Nelle domande si/no sta in prima posizione."
            ),
            tabelle = listOf(
                TabellaGrammatica(
                    titolo = "Verbi regolari: wohnen",
                    intestazioni = listOf("Person", "Desinenza", "Forma"),
                    righe = listOf(
                        listOf("ich", "-e", "wohne"),
                        listOf("du", "-st", "wohnst"),
                        listOf("er / sie / es", "-t", "wohnt"),
                        listOf("wir", "-en", "wohnen"),
                        listOf("ihr", "-t", "wohnt"),
                        listOf("sie / Sie", "-en", "wohnen")
                    )
                ),
                TabellaGrammatica(
                    titolo = "sein e haben",
                    intestazioni = listOf("Person", "sein", "haben"),
                    righe = listOf(
                        listOf("ich", "bin", "habe"),
                        listOf("du", "bist", "hast"),
                        listOf("er / sie / es", "ist", "hat"),
                        listOf("wir", "sind", "haben"),
                        listOf("ihr", "seid", "habt"),
                        listOf("sie / Sie", "sind", "haben")
                    ),
                    nota = "Sono i due verbi irregolari più frequenti: vanno imparati a memoria."
                ),
                TabellaGrammatica(
                    titolo = "Verbi con cambio di vocale",
                    intestazioni = listOf("Infinito", "du", "er / sie / es"),
                    righe = listOf(
                        listOf("fahren", "fährst", "fährt"),
                        listOf("sprechen", "sprichst", "spricht"),
                        listOf("sehen", "siehst", "sieht"),
                        listOf("laufen", "läufst", "läuft")
                    ),
                    nota = "Il cambio riguarda solo tu e lui/lei."
                )
            ),
            esempi = listOf(
                EsempioGrammatica("Ich wohne in Brixen.", "Abito a Bressanone."),
                EsempioGrammatica("Du arbeitest im Krankenhaus.", "Lavori in ospedale."),
                EsempioGrammatica("Er spricht Deutsch.", "Lui parla tedesco."),
                EsempioGrammatica("Wir fahren nach Bozen.", "Andiamo a Bolzano."),
                EsempioGrammatica("Sie hat einen Termin.", "Lei ha un appuntamento."),
                EsempioGrammatica("Kommst du aus Italien?", "Vieni dall'Italia?")
            ),
            erroriTipici = listOf(
                ErroreTipico(
                    sbagliato = "Du sprecht Deutsch.",
                    corretto = "Du sprichst Deutsch.",
                    spiegazione = "La 2a persona singolare di sprechen è sprichst."
                ),
                ErroreTipico(
                    sbagliato = "Er habt einen Termin.",
                    corretto = "Er hat einen Termin.",
                    spiegazione = "haben è irregolare: er hat."
                ),
                ErroreTipico(
                    sbagliato = "Du arbeitst viel.",
                    corretto = "Du arbeitest viel.",
                    spiegazione = "Dopo una radice in -t si inserisce una -e-."
                ),
                ErroreTipico(
                    sbagliato = "Ich bin 25 Jahre.",
                    corretto = "Ich bin 25 Jahre alt.",
                    spiegazione = "In tedesco l'aggettivo alt è necessario."
                )
            ),
            merksatz = "Verbo in seconda posizione; du -> -st, er/sie/es -> -t. A, e e au possono cambiare vocale."
        ),
        esercizi = listOf(
            EsercizioUnita(
                id = "pra-01",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Ich ___ in Brixen.",
                opzioni = listOf("wohne", "wohnst", "wohnt", "wohnen"),
                rispostaCorretta = 0,
                spiegazione = "ich + radice + -e: wohne."
            ),
            EsercizioUnita(
                id = "pra-02",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Du ___ im Krankenhaus.",
                opzioni = listOf("arbeite", "arbeitest", "arbeitet", "arbeiten"),
                rispostaCorretta = 1,
                spiegazione = "Dopo radice in -t: du arbeitest."
            ),
            EsercizioUnita(
                id = "pra-03",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Er ___ Deutsch.",
                opzioni = listOf("spreche", "spricht", "sprechen", "sprecht"),
                rispostaCorretta = 1,
                spiegazione = "sprechen cambia e -> i: er spricht."
            ),
            EsercizioUnita(
                id = "pra-04",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Wir ___ nach Bozen.",
                opzioni = listOf("fahre", "fährst", "fahren", "fahrt"),
                rispostaCorretta = 2,
                spiegazione = "wir + infinito senza desinenza: fahren."
            ),
            EsercizioUnita(
                id = "pra-05",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Sie ___ einen Termin.",
                opzioni = listOf("habe", "hast", "hat", "haben"),
                rispostaCorretta = 2,
                spiegazione = "haben è irregolare: sie hat."
            ),
            EsercizioUnita(
                id = "pra-06",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "___ du aus Italien?",
                rispostaTesto = "Kommst",
                alternative = listOf("kommst"),
                spiegazione = "Domanda si/no: verbo in prima posizione. du kommen -> kommst.",
                suggerimento = "Scrivi il verbo coniugato."
            ),
            EsercizioUnita(
                id = "pra-07",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Ich ___ 25 Jahre alt.",
                rispostaTesto = "bin",
                alternative = listOf("bin"),
                spiegazione = "sein: ich bin.",
                suggerimento = "Scrivi solo il verbo."
            ),
            EsercizioUnita(
                id = "pra-08",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Er ___ Arzt.",
                rispostaTesto = "ist",
                alternative = listOf("ist"),
                spiegazione = "sein: er ist.",
                suggerimento = "Scrivi solo il verbo."
            ),
            EsercizioUnita(
                id = "pra-09",
                tipo = TipoEsercizioGrammatica.TRASFORMAZIONE,
                domanda = "Forma una frase: du / arbeiten / viel",
                rispostaTesto = "Du arbeitest viel.",
                alternative = listOf("du arbeitest viel"),
                spiegazione = "La radice arbeit- prende -e- davanti a -st: arbeitest.",
                suggerimento = "Verbo in seconda posizione."
            ),
            EsercizioUnita(
                id = "pra-10",
                tipo = TipoEsercizioGrammatica.TRASFORMAZIONE,
                domanda = "Forma una frase: er / sehen / den Film",
                rispostaTesto = "Er sieht den Film.",
                alternative = listOf("er sieht den film"),
                spiegazione = "sehen cambia e -> ie: er sieht. Film è maschile: den Film.",
                suggerimento = "Verbo in seconda posizione e accusativo."
            ),
            EsercizioUnita(
                id = "pra-11",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Correggi: Du sprecht Deutsch.",
                rispostaTesto = "Du sprichst Deutsch.",
                alternative = listOf("du sprichst deutsch", "du sprichst"),
                spiegazione = "La forma corretta è du sprichst.",
                suggerimento = "Scrivi la frase corretta."
            ),
            EsercizioUnita(
                id = "pra-12",
                tipo = TipoEsercizioGrammatica.PRODUZIONE,
                domanda = "Traduci: Lavoro in ospedale.",
                rispostaTesto = "Ich arbeite im Krankenhaus.",
                alternative = listOf(
                    "ich arbeite im krankenhaus",
                    "ich arbeite in einem krankenhaus"
                ),
                spiegazione = "arbeiten: ich arbeite. Krankenhaus è neutro: im Krankenhaus.",
                suggerimento = "Usa il presente di arbeiten."
            )
        )
    )

    // =====================================================================
    // UNITA' 3 - A2 Perfekt
    // =====================================================================

    private fun unitaPerfekt(): UnitaGrammatica = UnitaGrammatica(
        id = "a2-perfekt",
        livello = "A2",
        titolo = "Il Perfekt",
        sottotitolo = "Il passato prossimo del tedesco",
        obiettivo = "Raccontare azioni passate con haben/sein + Partizip II.",
        minuti = 15,
        prerequisiti = listOf("a1-praesens", "a1-akkusativ"),
        teoria = TeoriaGrammatica(
            regola = listOf(
                "Il Perfekt è il passato prossimo usato nel parlato: **haben/sein al presente + Partizip II**.",
                "La maggior parte dei verbi usa **haben**: Ich habe gearbeitet.",
                "Usano **sein** i verbi di movimento o cambiamento di stato (gehen, fahren, kommen, fliegen, aufstehen, werden, sterben) e alcuni verbi come bleiben e passieren.",
                "Partizip II regolare: **ge- + radice + -t** (machen -> gemacht). Con radici in -t/-d si aggiunge -et: arbeiten -> gearbeitet.",
                "Partizip II irregolare: **ge- + radice modificata + -en** (sehen -> gesehen, gehen -> gegangen, sprechen -> gesprochen).",
                "I verbi con prefisso inseparabile (be-, ver-, er-, ge-, ent-) non prendono ge-: besuchen -> besucht, verstehen -> verstanden.",
                "Nei verbi separabili ge- va tra prefisso e verbo: aufstehen -> aufgestanden, einkaufen -> eingekauft.",
                "Il Partizip II va in fondo alla frase: Ich habe den Termin vergessen."
            ),
            tabelle = listOf(
                TabellaGrammatica(
                    titolo = "haben e sein",
                    intestazioni = listOf("Person", "haben", "sein"),
                    righe = listOf(
                        listOf("ich", "habe", "bin"),
                        listOf("du", "hast", "bist"),
                        listOf("er / sie / es", "hat", "ist"),
                        listOf("wir", "haben", "sind"),
                        listOf("ihr", "habt", "seid"),
                        listOf("sie / Sie", "haben", "sind")
                    )
                ),
                TabellaGrammatica(
                    titolo = "Partizip II",
                    intestazioni = listOf("Infinito", "Tipo", "Partizip II"),
                    righe = listOf(
                        listOf("machen", "regolare", "gemacht"),
                        listOf("arbeiten", "regolare", "gearbeitet"),
                        listOf("sehen", "irregolare", "gesehen"),
                        listOf("gehen", "irregolare", "gegangen"),
                        listOf("sprechen", "irregolare", "gesprochen"),
                        listOf("besuchen", "inseparabile", "besucht"),
                        listOf("aufstehen", "separabile", "aufgestanden")
                    )
                ),
                TabellaGrammatica(
                    titolo = "Quale ausiliare?",
                    intestazioni = listOf("Verbo", "Ausiliare", "Perfekt"),
                    righe = listOf(
                        listOf("arbeiten", "haben", "ich habe gearbeitet"),
                        listOf("gehen", "sein", "ich bin gegangen"),
                        listOf("fahren", "sein", "ich bin gefahren"),
                        listOf("bleiben", "sein", "ich bin geblieben"),
                        listOf("besuchen", "haben", "ich habe besucht"),
                        listOf("aufstehen", "sein", "ich bin aufgestanden")
                    ),
                    nota = "Movimento o cambiamento di stato -> sein. Gli altri -> haben."
                )
            ),
            esempi = listOf(
                EsempioGrammatica("Ich habe den Termin vergessen.", "Ho dimenticato l'appuntamento."),
                EsempioGrammatica("Wir sind nach Bozen gefahren.", "Siamo andati a Bolzano."),
                EsempioGrammatica("Er ist früh aufgestanden.", "Si è alzato presto."),
                EsempioGrammatica("Sie hat im Krankenhaus gearbeitet.", "Ha lavorato in ospedale."),
                EsempioGrammatica("Hast du die Tabletten genommen?", "Hai preso le compresse?"),
                EsempioGrammatica("Ich bin zu Hause geblieben.", "Sono rimasto a casa.")
            ),
            erroriTipici = listOf(
                ErroreTipico(
                    sbagliato = "Ich habe nach Bozen gefahren.",
                    corretto = "Ich bin nach Bozen gefahren.",
                    spiegazione = "fahren esprime movimento: usa sein."
                ),
                ErroreTipico(
                    sbagliato = "Ich habe aufgestanden.",
                    corretto = "Ich bin aufgestanden.",
                    spiegazione = "aufstehen esprime cambiamento di stato: usa sein."
                ),
                ErroreTipico(
                    sbagliato = "Ich habe gebesucht.",
                    corretto = "Ich habe besucht.",
                    spiegazione = "I verbi con be- non prendono ge-."
                ),
                ErroreTipico(
                    sbagliato = "Ich bin gearbeitet.",
                    corretto = "Ich habe gearbeitet.",
                    spiegazione = "arbeiten non esprime movimento: usa haben."
                ),
                ErroreTipico(
                    sbagliato = "Ich habe den Termin vergisst.",
                    corretto = "Ich habe den Termin vergessen.",
                    spiegazione = "Il Partizip II di vergessen è vergessen."
                )
            ),
            merksatz = "haben/sein al presente + Partizip II in fondo. Movimento o cambiamento di stato -> sein."
        ),
        esercizi = listOf(
            EsercizioUnita(
                id = "per-01",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Ich ___ den Termin vergessen.",
                opzioni = listOf("habe", "bin", "hatte", "war"),
                rispostaCorretta = 0,
                spiegazione = "vergessen usa haben: ich habe vergessen."
            ),
            EsercizioUnita(
                id = "per-02",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Wir ___ nach Bozen gefahren.",
                opzioni = listOf("haben", "sind", "waren", "hatten"),
                rispostaCorretta = 1,
                spiegazione = "fahren esprime movimento: wir sind gefahren."
            ),
            EsercizioUnita(
                id = "per-03",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Er ___ früh aufgestanden.",
                opzioni = listOf("hat", "ist", "hatte", "war"),
                rispostaCorretta = 1,
                spiegazione = "aufstehen usa sein: er ist aufgestanden."
            ),
            EsercizioUnita(
                id = "per-04",
                tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
                domanda = "Sie ___ im Krankenhaus gearbeitet.",
                opzioni = listOf("hat", "ist", "hatte", "war"),
                rispostaCorretta = 0,
                spiegazione = "arbeiten usa haben: sie hat gearbeitet."
            ),
            EsercizioUnita(
                id = "per-05",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Partizip II di machen: ___",
                rispostaTesto = "gemacht",
                alternative = listOf("gemacht"),
                spiegazione = "Regolare: ge- + mach + -t = gemacht.",
                suggerimento = "Scrivi solo il Partizip II."
            ),
            EsercizioUnita(
                id = "per-06",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Partizip II di sehen: ___",
                rispostaTesto = "gesehen",
                alternative = listOf("gesehen"),
                spiegazione = "Irregolare: ge- + sehen = gesehen.",
                suggerimento = "Scrivi solo il Partizip II."
            ),
            EsercizioUnita(
                id = "per-07",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Partizip II di aufstehen: ___",
                rispostaTesto = "aufgestanden",
                alternative = listOf("aufgestanden"),
                spiegazione = "Separabile: auf + ge + standen = aufgestanden.",
                suggerimento = "Scrivi solo il Partizip II."
            ),
            EsercizioUnita(
                id = "per-08",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Partizip II di besuchen: ___",
                rispostaTesto = "besucht",
                alternative = listOf("besucht"),
                spiegazione = "Inseparabile: be- + sucht = besucht, senza ge-.",
                suggerimento = "Scrivi solo il Partizip II."
            ),
            EsercizioUnita(
                id = "per-09",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Ich habe den Film ___. (sehen)",
                rispostaTesto = "gesehen",
                alternative = listOf("gesehen"),
                spiegazione = "Il Partizip II di sehen è gesehen.",
                suggerimento = "Scrivi solo il participio."
            ),
            EsercizioUnita(
                id = "per-10",
                tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                domanda = "Er ist nach Hause ___. (gehen)",
                rispostaTesto = "gegangen",
                alternative = listOf("gegangen"),
                spiegazione = "Il Partizip II di gehen è gegangen.",
                suggerimento = "Scrivi solo il participio."
            ),
            EsercizioUnita(
                id = "per-11",
                tipo = TipoEsercizioGrammatica.TRASFORMAZIONE,
                domanda = "Correggi: Ich habe nach Bozen gefahren.",
                rispostaTesto = "Ich bin nach Bozen gefahren.",
                alternative = listOf("ich bin nach bozen gefahren"),
                spiegazione = "fahren esprime movimento: l'ausiliare è sein.",
                suggerimento = "Scrivi la frase corretta."
            ),
            EsercizioUnita(
                id = "per-12",
                tipo = TipoEsercizioGrammatica.PRODUZIONE,
                domanda = "Traduci: Ho lavorato in ospedale.",
                rispostaTesto = "Ich habe im Krankenhaus gearbeitet.",
                alternative = listOf(
                    "ich habe im krankenhaus gearbeitet",
                    "ich habe in einem krankenhaus gearbeitet"
                ),
                spiegazione = "arbeiten usa haben: ich habe gearbeitet. Im Krankenhaus.",
                suggerimento = "Usa haben + Partizip II."
            )
        )
    )
}
