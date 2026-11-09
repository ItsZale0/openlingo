package com.alessandro.tedesco.data.local

import com.alessandro.tedesco.data.EsercizioAscolto
import com.alessandro.tedesco.data.PadronanzaGrammatica
import com.alessandro.tedesco.data.TestB1
import com.alessandro.tedesco.data.ValutazioneProduzione
import com.alessandro.tedesco.data.remote.VoceGlossario
import kotlinx.serialization.Serializable

@Serializable
data class WordEntity(
    val id: String,
    val german: String,
    val italian: String,
    val example: String = "",
    val article: String? = null,
    val pronunciation: String? = null,
    val level: String = "A1",
    val lesson: Int = 0,
    val tags: String = "",
    val archived: Boolean = false,
    val createdAt: Long = 0L,
    val source: WordSource = WordSource.SYNCED
)

enum class WordSource {
    SYNCED,     // Dal feed GitHub del corso
    CUSTOM      // Aggiunta manualmente dall'utente
}

@Serializable
data class ReviewEntity(
    val wordId: String,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val dueAt: Long = 0L,
    val lastReviewedAt: Long? = null
)

@Serializable
data class FeedLogEntity(
    val syncedAt: Long,
    val newWords: Int,
    val updatedWords: Int,
    val status: String,
    val message: String? = null
)

/** Sezione della guida di studio, sincronizzata dal Google Doc dell'utente. */
@Serializable
data class SezioneEntity(
    val titolo: String = "",
    val testo: String = ""
)

@Serializable
data class LessonEntity(
    val numero: Int,
    val titolo: String,
    val contenuto: String,
    val audioUrl: String = ""
)

@Serializable
data class GuidaEntity(
    val titolo: String = "",
    val docUrl: String = "",
    val sezioni: List<SezioneEntity> = emptyList()
)

/** Livelli CEFR supportati */
enum class LivelloCEFR(val ordine: Int, val label: String, val descrizione: String) {
    A0(0, "A0 — Principiante assoluto", "Nessuna conoscenza, si inizia da zero"),
    // Conteggi parole = Wortliste UFFICIALE Goethe (unità lessicali ricettive).
    A1(1, "A1 — Elementare", "Frasi semplici, presente, articoli · Wortliste 650 parole (attive ~325)"),
    A2(2, "A2 — Base", "Perfetto, dativo, separabili, modali · Wortliste 1300 parole (attive ~650)"),
    B1(3, "B1 — Intermedio", "Congiuntivo, passivo, frasi complesse · Wortliste 2400 parole (attive ~1200)"),
    B2(4, "B2 — Avanzato", "Lavorativo, testi complessi, sfumature · ~4000 parole"),
    C1(5, "C1 — Autonomo", "Fluente, accademico/professionale · ~8000 parole"),
    C2(6, "C2 — Madrelingua", "Nativo, tutte le sfumature");

    /** Codice breve: "A0 — Principiante assoluto" -> "A0". */
    fun codiceBreve(): String = label.substringBefore(" — ").substringBefore(" ").trim().uppercase()

    companion object {
        /**
         * Risolve un codice ("A0") o un label ("A0 — Principiante assoluto") al livello.
         *
         * Il label e' lungo e pieno di caratteri speciali: confrontarlo con i codici
         * brevi delle parole ha fatto filtrare lista vuota in due punti dell'app.
         */
        fun daTesto(testo: String?): LivelloCEFR {
            val t = testo?.trim().orEmpty()
            if (t.isEmpty()) return A0
            entries.firstOrNull { it.label == t }?.let { return it }
            val breve = t.substringBefore(" — ").substringBefore(" ").trim().uppercase()
            return entries.firstOrNull { it.codiceBreve() == breve } ?: A0
        }
    }
}

/** Risultato di un test di livello */
@Serializable
data class TestLivello(
    val id: String,
    val timestamp: Long,
    val livelloPrecedente: LivelloCEFR,
    val livelloNuovo: LivelloCEFR,
    val punteggioVocab: Int,
    val punteggioGrammatica: Int,
    val punteggioTotale: Int,
    val paroleTestate: Int,
    val corrette: Int,
    val durataSecondi: Long
)

/** Progresso complessivo dell'utente */
@Serializable
data class ProgressoUtente(
    val livelloCorrente: LivelloCEFR = LivelloCEFR.A0,
    // NOTA: xpTotale / paroleApprese / paroleInCorso / lezioniCompletate /
    // accuratezzaMedia / streakGiorni NON sono piu' la fonte di verita': erano campi
    // mai scritti (valori sempre 0, mostrati come "0 punti / 0 giorni").
    // I valori reali vengono calcolati da CalcoloProgresso (dati reali).
    // Restano per compatibilita' di deserializzazione dei profili salvati.
    val xpTotale: Int = 0,
    val paroleApprese: Int = 0,
    val paroleInCorso: Int = 0,
    val lezioniCompletate: Int = 0,
    val accuratezzaMedia: Float = 0f,
    val streakGiorni: Int = 0,
    val ultimoTest: TestLivello? = null,
    val storicoTest: List<TestLivello> = emptyList(),
    val ultimaAttivita: Long = 0,
    val obiettivoLivello: LivelloCEFR = LivelloCEFR.B1,
    val lezioneCorrente: Int = 1,
    val dataInizioLezione: Long = 0,
    val testGrammatica: List<TestGrammatica> = emptyList(),
    val testComprensione: List<TestComprensione> = emptyList(),
    val testProduzione: List<TestProduzione> = emptyList(),
    /** Valutazioni della PRODUZIONE SCRITTA libera (criteri ufficiali Goethe). */
    val valutazioniProduzione: List<ValutazioneProduzione> = emptyList(),
    val testB1: List<com.alessandro.tedesco.data.TestB1> = emptyList(),
    val testAdattivi: List<TestAdattivo> = emptyList(),
    val testAscolto: List<TestAscolto> = emptyList(),
    val erroriGrammatica: List<String> = emptyList(),
    /** Miglior punteggio (0-100) ottenuto in ogni lezione. Chiave = numero lezione. */
    val punteggiLezioni: Map<Int, Int> = emptyMap()
)

/**
 * Evento di ripasso: una riga per OGNI risposta data dall'utente.
 * Base delle statistiche reali (ripassi, accuratezza, grafici nel tempo).
 */
@Serializable
data class RipassoLogEntity(
    val wordId: String,
    val timestamp: Long,
    val corretta: Boolean,
    /**
     * true = evento di VERIFICA (quiz a scelta multipla, evidenza forte);
     * false = ripasso flashcard (self-report "Lo so"/"Non lo so").
     * Serve a distinguere l'evidenza oggettiva dal self-report: solo le
     * verifiche contano per il lessico dimostrato.
     */
    val verifica: Boolean = false
)

/** Risultato di un test di grammatica */
@Serializable
data class TestGrammatica(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int
)

/** Risultato di un test di comprensione */
@Serializable
data class TestComprensione(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int
)

/** Risultato di un test di produzione */
@Serializable
data class TestProduzione(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int
)

/** Risultato di un test adattivo (domande generate dinamicamente) */
@Serializable
data class TestAdattivo(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int,
    val livello: String
)

/** Risultato di un test di ascolto (Hörverstehen) */
@Serializable
data class TestAscolto(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int,
    val livello: String
)

/** Domanda per test di livello */
@Serializable
data class DomandaTest(
    val id: String,
    val tipo: TipoDomanda,
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    val spiegazione: String,
    val livelloRichiesto: LivelloCEFR,
    val tagGrammatica: String? = null,
    /** Testo di lettura (tedesco) da mostrare prima delle domande. */
    val testoLettura: String? = null,
    /** Parole nuove del testo con traduzione (glossario). */
    val glossario: List<VoceGlossario> = emptyList()
)

enum class TipoDomanda {
    VOCAB_TED_ITA,
    VOCAB_ITA_TED,
    GRAMMATICA,
    COMPLETAMENTO,
    TRADUZIONE
}

/* ==================== PROFILI UTENTE ==================== */

/** Tipologia profilo: definisce il preset di funzionalità */
enum class TipoProfilo(
    val id: String,
    val nome: String,
    val descrizione: String,
    val enableCustomWords: Boolean,
    val enableGoogleSheets: Boolean,
    val livelloIniziale: LivelloIniziale = LivelloIniziale.A0,
    val mostraContestoMedico: Boolean = false
) {
    /**
     * Profilo "professionalizzante": percorso con contesto sanitario.
     *
     * L'id resta "alessandro" per compatibilità con i dati e i backup già
     * esistenti (l'enum è serializzato per nome). Il nome mostrato è neutro:
     * chiunque lavora in ambito sanitario può usarlo.
     */
    ALESSANDRO(
        id = "alessandro",
        nome = "Sanitario",
        descrizione = "Percorso con contesto medico e sanitario",
        enableCustomWords = true,
        enableGoogleSheets = false,
        livelloIniziale = LivelloIniziale.A0,
        mostraContestoMedico = true
    ),
    PERSONALIZZATO(
        id = "personalizzato",
        nome = "Personalizzato",
        descrizione = "Profilo creato dall'utente, senza contesto predefinito",
        enableCustomWords = true,
        enableGoogleSheets = false,
        livelloIniziale = LivelloIniziale.A0,
        mostraContestoMedico = false
    ),
    /** Profilo "standard" con parole personalizzate. Id storico mantenuto. */
    EMMA(
        id = "emma",
        nome = "Standard",
        descrizione = "Percorso standard con parole personalizzate",
        enableCustomWords = true,
        enableGoogleSheets = true,
        livelloIniziale = LivelloIniziale.A0,
        mostraContestoMedico = false
    )
}

/** Configurazione profilo */
/** Livello CEFR iniziale per il profilo */
@Serializable
enum class LivelloIniziale(val label: String) {
    A0("A0"), A1("A1"), A2("A2"), B1("B1"), B2("B2")
}

/** Configurazione profilo */
@Serializable
data class ProfiloConfig(
    val tipo: TipoProfilo,
    val nomeVisualizzato: String,
    val enableCustomWords: Boolean = false,
    val enableGoogleSheets: Boolean = false,
    val googleSheetId: String? = null,
    val feedUrl: String = "",
    val guidaDocId: String? = null,
    val tutorApiKey: String = "",
    val geminiApiKey: String = "",
    /**
     * Provider AI scelto dall'utente.
     *
     * null = profilo creato prima del supporto multi-provider: il provider
     * viene dedotto dalle vecchie chiavi [tutorApiKey] e [geminiApiKey].
     */
    val providerAI: ProviderAI? = null,
    /** Chiavi API per provider: id del provider -> chiave. */
    val chiaviAI: Map<String, String> = emptyMap(),
    /** Modello scelto (vuoto = modello predefinito del provider). */
    val modelloAI: String = "",
    val palette: String = "uber",
    val stileDesign: String = "pixel",
    val livelloIniziale: LivelloIniziale = LivelloIniziale.A0,
    val mostraContestoMedico: Boolean = false,
    /**
     * Contesto personale scritto dall'utente: professione, interessi, obiettivi
     * (es. "elettricista, vorrei parlare con i clienti del cantiere").
     *
     * Viene passato ai prompt AI (tutor, esercizi, lezioni) per personalizzare
     * contenuti e vocabolario. Vuoto = nessuna personalizzazione.
     */
    val contestoUtente: String = "",
    /** Modalità Immersione: percorso didattico separato (input comprensibile, shadowing, ascolto passivo). */
    val didatticaImmersione: Boolean = false,
    /** Data (YYYY-MM-DD) in cui la missione giornaliera è stata completata. Vuoto = non completata oggi. */
    val missioneGiornalieraCompletata: String = ""
)

/** Stato persistente per profilo — dati completamente separati per utente */
@Serializable
data class ProfiloStato(
    val parole: List<WordEntity> = emptyList(),
    val reviews: Map<String, ReviewEntity> = emptyMap(),
    val feedLog: List<FeedLogEntity> = emptyList(),
    val guida: GuidaEntity? = null,
    val progresso: ProgressoUtente = ProgressoUtente(),
    val lezioneContenuto: String = "",
    /**
     * Lezioni GENERATE dall'AI, per numero (chiave stringa per la serializzazione).
     * Persistite qui perche' il sync dal feed sovrascrive [lezioneContenuto]: senza
     * questa mappa una lezione generata sparirebbe al primo sync (ogni 15 min).
     */
    val lezioniGenerate: Map<String, String> = emptyMap(),
    val etag: String = "",
    val lastSync: Long = 0L,
    val risposte: List<FeedbackEntry> = emptyList(),
    val piano: PianoEntity? = null,
    val progressoFeed: ProgressoFeedEntity? = null,
    val sessioni: List<SessioneEntity> = emptyList(),
    val conversazioni: List<ConversazioneEntry> = emptyList(),
    val streakGiorni: Int = 0,
    val streakRecord: Int = 0,
    val ultimoGiornoStudio: String? = null,
    val badgeSbloccati: List<String> = emptyList(),
    val puntiTotali: Int = 0,
    val eserciziGeneratiComprensione: List<EsercizioGenEntity> = emptyList(),
    val eserciziGeneratiProduzione: List<EsercizioGenEntity> = emptyList(),
    val eserciziGeneratiAscolto: List<EsercizioGenEntity> = emptyList(),
    val eserciziGeneratiGrammatica: List<EsercizioGenEntity> = emptyList(),
    val ripassiLog: List<RipassoLogEntity> = emptyList(),
    /** Quaderno degli errori con ripetizione spaziata (SRS separato dal lessico). */
    val errori: List<com.alessandro.tedesco.data.ErroreEntity> = emptyList(),
    /** true dopo che il test di ingresso e' stato completato (o saltato). */
    val placementFatto: Boolean = false,
    /**
     * Padronanza del percorso grammaticale, per id di unita'.
     * Default vuoto = profili salvati prima di questa funzione restano validi.
     */
    val padronanzaGrammatica: Map<String, PadronanzaGrammatica> = emptyMap(),
    /**
     * Lezione del percorso attualmente aperta. Vuoto = il percorso sceglie la
     * lezione consigliata in base al livello del profilo.
     */
    val lezionePercorsoId: String = "",
    /**
     * Progresso di ogni lezione del percorso, per id di unità.
     * Persiste indice della domanda, risposte corrette e completamento: chiudendo
     * l'app la lezione riparte da dove si era rimasti.
     */
    val progressiLezioni: Map<String, ProgressoLezione> = emptyMap(),
    /** Giorno (yyyy-MM-dd) a cui si riferiscono i passi del piano completati. */
    val passiGiornoData: String = "",
    /** IDs dei passi del piano giornaliero completati in [passiGiornoData]. */
    val passiGiornoIds: List<String> = emptyList(),
    /**
     * Rotazione delle sessioni di esercizi, per id di unità.
     * Serve a non riproporre nel tentativo successivo gli esercizi appena
     * visti: senza questo stato il banco di una lezione è troppo piccolo per
     * garantire sessioni davvero diverse. Default vuoto = profili esistenti validi.
     */
    val rotazioniEsercizi: Map<String, RotazioneUnita> = emptyMap(),
    /**
     * Esercizi generati dall'AI specifici di una unità del percorso.
     * Restano separati dal banco scritto a mano: se l'AI non è raggiungibile
     * la lezione continua a funzionare con i suoi esercizi.
     */
    val eserciziUnitaAi: Map<String, List<EsercizioGenEntity>> = emptyMap()
)

/**
 * Stato di rotazione degli esercizi di una unità.
 *
 * - [visti]: esercizi già usciti nel ciclo corrente (per non riproporli);
 * - [ultimi]: esercizi della sessione precedente (mai riproposti subito);
 * - [sessione]: ids della sessione in corso, così chiudendo l'app si riprende
 *   esattamente la stessa sessione invece di ricominciare da capo;
 * - [seme]: seme con cui è stata costruita, per ricostruire le stesse opzioni.
 */
@Serializable
data class RotazioneUnita(
    val visti: List<String> = emptyList(),
    val ultimi: List<String> = emptyList(),
    val sessione: List<String> = emptyList(),
    val seme: Int = 0
)

/** Progresso persistente di una lezione del percorso. */
@Serializable
data class ProgressoLezione(
    /** Indice della prossima domanda da mostrare (0 = inizio). */
    val indice: Int = 0,
    val corrette: Int = 0,
    val totali: Int = 0,
    /** True quando la sessione di esercizi è stata portata a termine. */
    val completata: Boolean = false,
    val aggiornato: Long = 0L
)

/** Piano di studio verso il B2. */
@Serializable
data class PianoEntity(
    val obiettivo: String = "",
    val orizzonte: String = "",
    val tappe: List<TappaEntity> = emptyList(),
    val certificazioni: List<CertificazioneEntity> = emptyList(),
    val risorse: List<RisorsaEntity> = emptyList()
)

@Serializable
data class TappaEntity(
    val nome: String = "",
    val descrizione: String = "",
    val lezioni: String = "",
    val stato: String = ""
)

@Serializable
data class CertificazioneEntity(
    val nome: String = "",
    val ente: String = "",
    val livello: String = "",
    val note: String = "",
    val url: String = ""
)

@Serializable
data class RisorsaEntity(
    val nome: String = "",
    val tipo: String = "",
    val nota: String = ""
)

/** Sessione strutturata disponibile nell'app. */
@Serializable
data class SessioneEntity(
    val tipo: String = "",
    val titolo: String = "",
    val descrizione: String = "",
    val durata: Int = 0
)

/** Risposta dell'utente da correggere. */
@Serializable
data class FeedbackEntry(
    val id: String,
    val testo: String,
    val timestamp: Long,
    val corretto: Boolean = false,
    val correzione: String? = null
)

/** Conversazione persistente con il tutor AI o speaking */
@Serializable
data class ConversazioneEntry(
    val id: String,
    val tipo: String, // "tutor" o "speaking"
    val ruolo: String, // "utente" o "tutor"
    val testo: String,
    val timestamp: Long,
    val correzione: String? = null
)

/**
 * Esercizio generato dall'AI e salvato localmente (cache persistita nel profilo).
 * Equivalente serializzabile di EsercizioGenerato (data.remote).
 */
@Serializable
data class EsercizioGenEntity(
    val id: String,
    val tipo: String, // "comprensione" | "produzione" | "ascolto" | "grammatica"
    val domanda: String,
    val opzioni: List<String> = emptyList(),
    val rispostaCorretta: Int = 0,
    val spiegazione: String = "",
    val livello: String = "A1",
    val fraseTedesca: String? = null,
    val traduzioneItaliana: String? = null,
    /** Solo per COMPRENSIONE/Lesen: il testo tedesco da leggere. */
    val testoLettura: String? = null,
    /** Solo per COMPRENSIONE: parole nuove del testo con traduzione italiana. */
    val glossario: List<VoceGlossario> = emptyList(),
    /** true = lo studente ha già completato questo esercizio (non riproporlo a breve). */
    val completato: Boolean = false,
    /** timestamp completamento (per futuro cooldown/riproposta). */
    val dataCompletamento: Long = 0L
)

/** Progresso corrente pubblicato dal tutor nel feed. */
@Serializable
data class ProgressoFeedEntity(
    val lezioneCorrente: Int = 1,
    val streakCorrente: Int = 0,
    val streakRecord: Int = 0,
    val totaleFatte: Int = 0,
    val paroleTotali: Int = 0,
    val paroleMature: Int = 0
)

/** Profilo utente completo con stato */
@Serializable
data class ProfiloUtente(
    val id: String,
    val config: ProfiloConfig,
    val stato: ProfiloStato = ProfiloStato(),
    val creatoIl: Long = System.currentTimeMillis(),
    val ultimoAccesso: Long = System.currentTimeMillis()
)

/** Repository per gestione profili */
@Serializable
data class ProfiliRepository(
    val profili: Map<String, ProfiloUtente> = emptyMap(),
    val profiloAttivoId: String? = null,
    /** ID dei profili eliminati dall'utente: il merge li ignora, così una
     *  cancellazione non viene "resuscitata" da un backup cloud o da un altro
     *  dispositivo che ha ancora il profilo. */
    val profiliEliminati: Set<String> = emptySet()
)

// Stile design dell'app
enum class StileDesign {
    PIXEL,      // Google Pixel: colori vivaci, animazioni fluide
    IOS,        // iOS: glassmorphism, ombre profonde
    MATERIAL,   // Material You: tonalità dinamiche
    UBER        // Uber: dark minimal, alto contrasto
}

/** Conversione di un esercizio generato (salvato come entità) in DomandaTest. */
fun EsercizioGenEntity.toDomandaTest(
    tipoDefault: TipoDomanda = TipoDomanda.VOCAB_TED_ITA
): DomandaTest = DomandaTest(
    id = id,
    tipo = when (tipo) {
        "comprensione" -> TipoDomanda.VOCAB_TED_ITA
        "produzione" -> TipoDomanda.COMPLETAMENTO
        "ascolto" -> TipoDomanda.VOCAB_ITA_TED
        "grammatica" -> TipoDomanda.GRAMMATICA
        else -> tipoDefault
    },
    domanda = domanda,
    opzioni = opzioni,
    rispostaCorretta = rispostaCorretta.coerceIn(0, opzioni.lastIndex.coerceAtLeast(0)),
    spiegazione = spiegazione,
    livelloRichiesto = when (livello.uppercase()) {
        "A0" -> LivelloCEFR.A0
        "A1" -> LivelloCEFR.A1
        "A2" -> LivelloCEFR.A2
        "B1" -> LivelloCEFR.B1
        "B2" -> LivelloCEFR.B2
        else -> LivelloCEFR.A1
    },
    tagGrammatica = null
)

/** Conversione in EsercizioAscolto (per la sezione Ascolto). */
fun EsercizioGenEntity.toEsercizioAscolto(): EsercizioAscolto = EsercizioAscolto(
    id = id,
    fraseTedesca = fraseTedesca ?: "",
    traduzioneItaliana = traduzioneItaliana ?: "",
    domanda = domanda,
    opzioni = opzioni,
    rispostaCorretta = rispostaCorretta.coerceIn(0, opzioni.lastIndex.coerceAtLeast(0)),
    spiegazione = spiegazione,
    livello = livello
)
