package com.alessandro.tedesco.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import com.alessandro.tedesco.data.ControlloGrammatica
import com.alessandro.tedesco.data.DeduplicaEsercizi
import com.alessandro.tedesco.di.HttpCondiviso
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Risposta "grezza" di un singolo esercizio generato dall'AI.
 * I campi sono tutti opzionali perché il modello può sbagliare il JSON:
 * il parser scarta gli elementi incompleti invece di far crashare.
 */
@Serializable
private data class EsercizioAi(
    val id: String? = null,
    val tipo: String? = null,
    val domanda: String? = null,
    val opzioni: List<String>? = null,
    @SerialName("rispostaCorretta") val rispostaCorretta: Int? = null,
    val spiegazione: String? = null,
    val livello: String? = null,
    val fraseTedesca: String? = null,
    val traduzioneItaliana: String? = null,
    val testoLettura: String? = null,
    /** Solo per COMPRENSIONE: parole nuove del testo + traduzione italiana. */
    val glossario: List<VoceGlossarioAi>? = null
)

@Serializable
private data class VoceGlossarioAi(
    val tedesco: String? = null,
    val italiano: String? = null
)

@Serializable
private data class RispostaEserciziAi(
    val esercizi: List<EsercizioAi> = emptyList()
)

/** Tipo di esercizio richiesto al generatore. */
enum class TipoEsercizio(val etichetta: String, val jsonTipo: String) {
    COMPRENSIONE("Comprensione", "comprensione"),
    PRODUZIONE("Produzione", "produzione"),
    ASCOLTO("Ascolto", "ascolto"),
    GRAMMATICA("Grammatica", "grammatica")
}

/** Un esercizio generato dall'AI, già normalizzato e validato. */
data class EsercizioGenerato(
    val id: String,
    val tipo: TipoEsercizio,
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    val spiegazione: String,
    val livello: String,
    /** Solo per ASCOLTO: la frase da far ascoltare via TTS. */
    val fraseTedesca: String? = null,
    val traduzioneItaliana: String? = null,
    /** Solo per COMPRENSIONE/Lesen: il testo tedesco da leggere prima delle domande. */
    val testoLettura: String? = null,
    /** Solo per COMPRENSIONE: parole nuove del testo con la traduzione italiana. */
    val glossario: List<VoceGlossario> = emptyList()
)

/**
 * Generatore di esercizi infiniti via Groq (solo modelli free).
 *
 * Gli esercizi statici delle schermate (QuizData, AscoltoData, TestB1) sono un
 * pool iniziale: quando lo studente li esaurisce, l'app ne genera di nuovi con
 * l'AI. La generazione avviene SOLO su richiesta esplicita dello schermo
 * (bottone "Genera altri") e mai in background, per non sprecare rate limit.
 *
 * Usa la stessa cascata di modelli free di TutorService: se uno è in rate
 * limit si passa al successivo.
 */
class GeneratoreEsercizi {

    private val client = HttpCondiviso.client

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Genera [quanti] esercizi nuovi del tipo richiesto.
     *
     * @param apiKey chiave Groq dell'utente
     * @param tipo quale sezione deve generare
     * @param livello livello CEFR (A0-B2)
     * @param quanti quanti esercizi (consigliato 5-10)
     * @param contestoMedico true = usa vocabolario medico/ospedaliero
     * @param evita lista di testi già mostrati, per non ripetere le domande
     * @param modello modello opzionale (null = cascata dei free)
     * @return lista di esercizi validati, senza duplicati di contenuto e
     *         al massimo [quanti] elementi (può essere più corta se il modello
     *         produce esercizi ripetuti o scadenti)
     */
    suspend fun genera(
        apiKey: String,
        tipo: TipoEsercizio,
        livello: String,
        quanti: Int = 5,
        contestoMedico: Boolean = false,
        evita: List<String> = emptyList(),
        modello: String? = null,
        tema: String? = null
    ): List<EsercizioGenerato> = withContext(Dispatchers.IO) {
        // Free mode (no API key): uses Gemini endpoint automatically
        if (quanti <= 0) return@withContext emptyList()

        val systemPrompt = costruisciPrompt(tipo, livello, quanti, contestoMedico, evita, tema)
        val messaggi = listOf(
            ChatMessageCompat("system", systemPrompt),
            ChatMessageCompat("user", "Genera gli esercizi in JSON.")
        )

        val modelli = if (!modello.isNullOrBlank()) listOf(modello)
            else if (apiKey.isBlank()) TutorService.MODELLI_NO_KEY else TutorService.MODELLI_FREE

        var ultimoErrore: String? = null
        var rateLimited = false
        // 2 tentativi: se tutti i modelli danno rate limit (429), aspetta e riprova
        for (tentativo in 1..2) {
            for (m in modelli) {
                try {
                    val risposta = chiamaModello(apiKey, m, messaggi)
                    val esercizi = parseEsercizi(risposta, tipo, livello, quanti)
                    if (esercizi.isNotEmpty()) return@withContext esercizi
                    ultimoErrore = "Risposta non valida dal modello $m"
                } catch (e: IOException) {
                    ultimoErrore = e.message
                    // 401 = chiave non valida: inutile provare altri modelli
                    if (e.message?.contains("non valida") == true) throw e
                    if (e.message?.contains("Troppe richieste") == true) rateLimited = true
                }
            }
            if (!rateLimited) break
            if (tentativo < 2) delay(4000)
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
    }

    private fun costruisciPrompt(
        tipo: TipoEsercizio,
        livello: String,
        quanti: Int,
        contestoMedico: Boolean,
        evita: List<String>,
        tema: String? = null
    ): String {
        val contesto = if (contestoMedico) {
            "\n- Contesto professionale: lo studente è un fisioterapista che lavorerà a Bolzano. " +
                "Dove ha senso, usa vocabolario medico/ospedaliero (Krankenhaus, Schmerzen, Behandlung, Patient…)."
        } else ""

        val istruzioniTipo = when (tipo) {
            TipoEsercizio.ASCOLTO -> """
                Prova di ASCOLTO (Hören), come nelle certificazioni: si ascolta UNA frase
                tedesca e si risponde a una domanda di comprensione.

                REGOLE SPECIFICHE:
                - "fraseTedesca": una frase tedesca REALISTICA e COMPLETA, calibrata sul livello:
                  * A0/A1: 5-9 parole, presente, vocabolario quotidiano (orari, prezzi, luoghi).
                  * A2: 8-14 parole, presente + Perfetto, un'informazione da cogliere.
                  * B1: 12-20 parole, tempi passati, subordinate semplici, un dettaglio da inferire.
                  * B2: 18-28 parole, strutture complesse, informazione implicita.
                - "traduzioneItaliana": la traduzione esatta della frase.
                - La domanda (in italiano) deve richiedere di AVER CAPITO la frase, non di
                  indovinare: chi/cosa/dove/quando/perché/come.
                - Distrattori PLAUSIBILI ma sbagliati: numeri vicini, luoghi simili, tempi diversi.
                  Mai opzioni assurde o facilmente eliminabili.
                - Se la frase contiene un numero/ora/prezzo, una domanda su quello è ottima.
            """.trimIndent()
            TipoEsercizio.COMPRENSIONE -> """
                È una prova di COMPRENSIONE DELLA LETTURA (Lesen), come nelle certificazioni
                Goethe/telc/ÖSD: si legge UN testo in tedesco e si rispondono domande su di esso.

                REGOLE SPECIFICHE:
                - Genera UN SOLO testo tedesco, coerente, su un tema quotidiano realistico,
                  CALIBRATO ESATTAMENTE sul livello $livello:
                  * A0/A1: 40-70 parole, presente indicativo, frasi semplicissime (soggetto-verbo-oggetto),
                    vocabolario di base (famiglia, cibo, casa, giorni, numeri). NIENTE passato,
                    niente subordinate, niente congiuntivo, niente lessico astratto.
                  * A2: 70-110 parole, presente + Perfetto, frasi coordinate semplici,
                    vocabolario quotidiano. Evita subordinate complesse e passivo.
                  * B1: 110-160 parole, tempi passati, subordinate (weil/dass/wenn), passivo semplice.
                  * B2: 160-220 parole, strutture complesse, lessico astratto, congiuntivo.
                  USA SOLO grammatica e vocabolario CHE UN LIVELLO $livello GIÀ CONOSCE.
                - Ripeti lo STESSO testo nel campo "testoLettura" di OGNI esercizio del batch.
                - Compila "glossario": un array con le 3-6 parole PIÙ DIFFICILI del testo
                  (le più utili da imparare per il livello), ognuna come
                  {"tedesco": "<parola con articolo se sostantivo>", "italiano": "<traduzione>"}.
                  Traduci in italiano ESATTO e naturale. Ripeti lo STESSO glossario in ogni esercizio.
                - Ogni esercizio è una domanda a 4 opzioni SUL testo (in italiano):
                  chi/cosa/dove/quando/perché, vero significato di una frase del testo,
                  inferenza semplice. NON domande isolate su singole parole.
                - La risposta corretta deve essere deducibile SOLO leggendo il testo.
            """.trimIndent()
            TipoEsercizio.PRODUZIONE -> """
                Prova di PRODUZIONE SCRITTA (Schreiben): si sceglie la frase tedesca CORRETTA
                per esprimere un contenuto dato (la domanda è in italiano, la risposta è tedesca).

                REGOLE SPECIFICHE:
                - La domanda chiede di ESPRIMERE qualcosa in tedesco
                  (es. "Come si dice: 'Ho mal di testa da tre giorni'?").
                - Le 4 opzioni sono TUTTE frasi tedesche complete: una corretta, tre con
                  errori REALI e tipici (articolo sbagliato, caso sbagliato, verbo non coniugato,
                  tempo sbagliato, ordine parole sbagliato) — errori che uno studente farebbe davvero.
                - Calibratura sul livello $livello:
                  * A0/A1: frasi brevi al presente, argomenti quotidiani.
                  * A2: presente + Perfetto, frasi coordinate.
                  * B1: subordinate (weil/dass/wenn), passato, casi con preposizioni.
                  * B2: strutture complesse, congiuntivo, registro formale.
                - La spiegazione (in italiano) deve dire PERCHÉ le altre sono sbagliate.
            """.trimIndent()
            TipoEsercizio.GRAMMATICA -> """
                Prova di GRAMMATICA: si applica una REGOLA precisa (articoli, casi, declinazioni,
                verbi, preposizioni, plurali, ordine delle parole).

                REGOLE SPECIFICHE:
                - Ogni esercizio testa UNA regola chiara, calibrata sul livello $livello:
                  * A0/A1: articoli der/die/das, presente dei verbi comuni, plurale, negazione (nicht/kein).
                  * A2: accusativo/dativo, Perfetto (haben/sein + participio), preposizioni di luogo,
                    verbi modali (können/müssen/wollen).
                  * B1: subordinate (weil, dass, wenn), Präteritum, passivo semplice,
                    preposizioni con caso, aggettivi possessivi.
                  * B2: Konjunktiv II, participio attributivo, verbi con preposizione fissa,
                    costruzioni complesse.
                - La domanda è in italiano e indica la regola (es. "Quale articolo? — ___ Krankenhaus ist neu.").
                - Distrattori plausibili che testano confusioni REALI (der/die/das, den/dem, mir/mich).
                - La spiegazione (in italiano) spiega la REGOLA in una riga.
            """.trimIndent()
        }

        val evitaTesto = if (evita.isNotEmpty()) {
            "\n\nNON ripetere domande simili a queste già viste:\n" + evita.take(20).joinToString("\n") { "- $it" }
        } else ""

        // TEMA OBBLIGATORIO: usato dalle lezioni del percorso, dove gli esercizi
        // devono allenare UNA regola precisa e non divagare su altre.
        val temaTesto = if (tema.isNullOrBlank()) "" else """

            TEMA OBBLIGATORIO (vincolo assoluto):
            - Tutti gli esercizi devono allenare esattamente questa regola: $tema
            - NON cambiare argomento e NON mescolare altre regole grammaticali.
            - Usa SOLO la grammatica spiegata nel tema: se un esercizio richiede una
              regola che non è nel tema, non generarlo.
            - Formato preferito: frase tedesca con un buco ___ (es. "Der ___ Hund spielt im Garten.")
              e 4 forme candidate nelle opzioni; una sola corretta.
            - Se la regola non si presta al buco, usa una domanda in italiano con 4 frasi
              tedesche tra cui scegliere quella corretta.
            - Ogni esercizio deve essere DIVERSO dagli altri dello stesso batch: cambia
              soggetto, verbo, caso o numero, non solo una parola.
            - CORRETTEZZA OBBLIGATORIA: controlla l'accordo articolo/nome PRIMA di rispondere.
              * maschile: der/ein/kein + Mann, Hund, Tisch, Apfel, Kaffee, Termin, Schlüssel
              * femminile: die/eine/keine + Frau, Stadt, Wohnung, Schule, Frage
              * neutro: das/ein/kein + Buch, Kind, Haus, Auto, Zimmer, Problem
              * all'accusativo cambia SOLO il maschile: den/einen/keinen.
              * Forme VIETATE (sbagliate): "einen Buch", "die Buch", "den Buch",
                "eine Hund", "einen Frau", "einen Kind", "einen Auto".
        """.trimIndent()

        // Con un tema (esercizi di una unità) le opzioni SONO forme tedesche:
        // la regola "tutto in italiano" qui renderebbe gli esercizi inutilizzabili.
        val linguaTesto = if (tema.isNullOrBlank()) {
            """
            - LINGUA (REGOLA FONDAMENTALE): domande, opzioni e spiegazioni sono ESCLUSIVAMENTE in italiano.
            - NON usare MAI spagnolo (vietate parole come amigo, hola, gracias, senor, por favor, muy, bueno, manana), ne inglese, ne altre lingue.
            - L'unico tedesco ammesso e la frase/esempio tedesco quando il tipo lo richiede.
            - Esempio corretto di opzioni in italiano: ["Io leggo un libro", "Tu leggi un libro", "Lui legge un libro", "Noi leggiamo un libro"]
            - Esempio SBAGLIATO da NON usare (spagnolo): ["Ich a un amigo", "Tu a un amigo", "El a mi"]
            """.trimIndent()
        } else {
            """
            - LINGUA: la domanda contiene una frase tedesca con il buco ___ e le opzioni sono le
              forme tedesche candidate. La spiegazione è in italiano, breve e concreta.
            - NON usare spagnolo, inglese o altre lingue: solo tedesco (esercizio) e italiano (spiegazione).
            """.trimIndent()
        }

        return """
            Sei un generatore di esercizi di tedesco per uno studente italiano di livello $livello.

            Genera ESATTAMENTE $quanti esercizi di tipo "${tipo.etichetta}" in formato JSON.
            $istruzioniTipo
            $temaTesto

            Regole OBBLIGATORIE:
            - Rispondi SOLO con un JSON valido, senza testo prima o dopo, senza ```json.
            - Struttura: {"esercizi": [ ... ]}
            - Ogni esercizio ha: "tipo" ("${tipo.jsonTipo}"), "domanda", "opzioni" (array di esattamente 4 stringhe),
              "rispostaCorretta" (indice 0-3 della risposta giusta), "spiegazione" (in italiano, breve),
              "livello" ("$livello")${if (tipo == TipoEsercizio.COMPRENSIONE) ""","testoLettura" (il testo tedesco da leggere, IDENTICO in tutti gli esercizi del batch), "glossario" (array di {"tedesco","italiano"} delle parole difficili, IDENTICO in tutti gli esercizi)""" else ""}.$contesto
            $linguaTesto
            - Le 4 opzioni devono essere plausibili ma con UNA sola risposta corretta.
            - DIFFICOLTÀ: genera esercizi SFIDANTI e UTILI per il livello $livello. Evita domande banali su parole singole elementari (es. il significato di "Hallo", "ciao", "grazie"). Privilegia frasi complete, vocabolario pratico e contesto realistico.
            - ANTI-INFLATIONE (REGOLA FONDAMENTALE): ogni domanda deve TESTARE una capacita' reale,
              non gonfiare il punteggio. Vietato:
              * domande con risposta ovvia o deducibile dalla forma della domanda;
              * distrattori assurdi, ridicoli o facilmente eliminabili (es. la risposta giusta e'
                l'unica in tedesco mentre le altre sono in italiano);
              * la stessa domanda riformulata;
              * opzioni dove una contiene la parola della domanda e le altre no.
              I distrattori devono essere SBAGLIATI ma PLAUSIBILI: chi non conosce la regola
              deve poter sbagliare davvero.
            - Le domande devono verificare APPLICAZIONE di una regola o COMPRENSIONE reale,
              non riconoscimento di parole elementari.
            - VARIETÀ: alterna schemi diversi (completare una frase, scegliere l'articolo/caso, coniugare un verbo, tradurre una frase utile, comprensione di una breve frase). Non proporre sempre lo stesso tipo.
            - I sostantivi tedeschi vanno sempre con l'articolo (der/die/das).
            - Non inventare esercizi con meno di 4 opzioni o senza rispostaCorretta.$evitaTesto
        """.trimIndent()
    }

    private fun chiamaModello(
        apiKey: String,
        modello: String,
        messaggi: List<ChatMessageCompat>
    ): String {
        val isFreeMode = apiKey.isBlank()
        val url = if (isFreeMode) TutorService.FREE_ENDPOINT else "https://api.groq.com/openai/v1/chat/completions"
        val effectiveKey = apiKey.trim().ifBlank { TutorService.FREE_API_KEY }

        val body = json.encodeToString(
            ChatRequestCompat.serializer(),
            ChatRequestCompat(model = modello, messages = messaggi, reasoningEffort = if (modello.contains("oss", ignoreCase = true)) "low" else null)
        )
        val builder = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $effectiveKey")
            .header("Content-Type", "application/json")

        if (!isFreeMode) {
            builder.header("HTTP-Referer", "https://github.com/ItsZale0/tedesco-app")
            builder.header("X-Title", "Tedesco App")
        }

        val request = builder.post(body.toRequestBody("application/json".toMediaType())).build()

        client.newCall(request).execute().use { response ->
            val testo = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException(
                    when (response.code) {
                        401 -> "Chiave API non valida."
                        429, 403 -> "Troppe richieste. Riprova tra poco."
                        else -> "Errore del server (${response.code})"
                    }
                )
            }
            val parsed = json.decodeFromString(ChatResponseCompat.serializer(), testo)
            return parsed.choices.firstOrNull()?.messageCompat?.content
                ?: throw IOException("Risposta vuota dal generatore")
        }
    }

    /**
     * Estrae il JSON dalla risposta (il modello a volte aggiunge ```json o testo),
     * poi converte in [EsercizioGenerato] scartando gli elementi invalidi.
     */
    // Solo parole inequivocabilmente spagnole, a parole intere (\b) per non matchare sottostringhe italiane
    // (es. "questa" NON deve matchare "esta"). Vetate solo parole che in italiano non esistono.
    private val regexSpagnolo = Regex(
        "\\b(amigo|amiga|hola|senor|senora|gracias|manana|perro|gato|muy|bueno|buena|por favor|de nada|que tal|como estas)\\b",
        RegexOption.IGNORE_CASE
    )

    /** true se il testo contiene parole spagnole (euristica anti-lingua-sbagliata). */
    private fun contieneSpagnolo(testo: String): Boolean = regexSpagnolo.containsMatchIn(testo)

    // Alfabeti non latini: arabo, cirillico, CJK, ecc. Le domande/opzioni devono essere in italiano (latino).
    private val regexNonLatino = Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u0400-\\u04FF\\u4E00-\\u9FFF\\u3040-\\u30FF]")

    /** true se il testo contiene caratteri di alfabeti non latini (arabo, cirillico, CJK...). */
    private fun contieneNonLatino(testo: String): Boolean = regexNonLatino.containsMatchIn(testo)

    internal fun parseEsercizi(
        risposta: String,
        tipo: TipoEsercizio,
        livelloDefault: String,
        quanti: Int = Int.MAX_VALUE
    ): List<EsercizioGenerato> {
        val jsonPulito = estraiJson(risposta) ?: return emptyList()
        val parsed = runCatching {
            json.decodeFromString(RispostaEserciziAi.serializer(), jsonPulito)
        }.getOrNull() ?: return emptyList()

        // Per la COMPRENSIONE: un solo testo condiviso da TUTTE le domande del batch
        // (formato reale Goethe/telc Lesen). Uso il primo testo non vuoto generato.
        val testoCondiviso = if (tipo == TipoEsercizio.COMPRENSIONE) {
            parsed.esercizi.firstNotNullOfOrNull { e ->
                e.testoLettura?.trim()?.takeIf { it.isNotEmpty() }
            }
        } else null

        // Glossario condiviso (parole difficili del testo + traduzione italiana).
        val glossarioCondiviso: List<VoceGlossario> = if (tipo == TipoEsercizio.COMPRENSIONE) {
            parsed.esercizi.asSequence()
                .flatMap { it.glossario?.asSequence() ?: emptySequence() }
                .mapNotNull { v ->
                    val ted = v.tedesco?.trim().orEmpty()
                    val ita = v.italiano?.trim().orEmpty()
                    if (ted.isEmpty() || ita.isEmpty()) null
                    else if (contieneNonLatino(ted) || contieneNonLatino(ita)) null
                    else VoceGlossario(ted, ita)
                }
                .distinctBy { it.tedesco.lowercase() }
                .take(8)
                .toList()
        } else emptyList()

        val validi = parsed.esercizi.mapIndexedNotNull { index, e ->
            val domanda = e.domanda?.trim().orEmpty()
            val opzioni = e.opzioni?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
            val rispostaCorretta = e.rispostaCorretta ?: return@mapIndexedNotNull null
            // Validazione: scarta esercizi incompleti o con risposta fuori range
            if (domanda.isEmpty() || opzioni.size != 4) return@mapIndexedNotNull null
            if (rispostaCorretta !in 0..3) return@mapIndexedNotNull null
            if (opzioni.toSet().size != opzioni.size) return@mapIndexedNotNull null // opzioni duplicate
            // Scarta esercizi in lingua sbagliata (spagnolo/inglese) nelle domande o opzioni
            if (contieneSpagnolo(domanda) || opzioni.any { contieneSpagnolo(it) }) return@mapIndexedNotNull null
            if (contieneNonLatino(domanda) || opzioni.any { contieneNonLatino(it) }) return@mapIndexedNotNull null
            // Scarta se la spiegazione è in tedesco invece che in italiano (modello che ignora la lingua)
            if (!e.spiegazione.isNullOrBlank() && contieneNonLatino(e.spiegazione)) return@mapIndexedNotNull null
            // Correttezza grammaticale: un esercizio con "einen Buch" (Buch è
            // neutro) insegnerebbe una regola falsa. Il controllo copre articoli,
            // possessivi e negazione per i nomi di genere noto.
            if (ControlloGrammatica.problemi(domanda, opzioni, rispostaCorretta).isNotEmpty()) {
                return@mapIndexedNotNull null
            }
            // ASCOLTO richiede la frase da ascoltare e la traduzione
            if (tipo == TipoEsercizio.ASCOLTO && (e.fraseTedesca.isNullOrBlank() || e.traduzioneItaliana.isNullOrBlank())) return@mapIndexedNotNull null
            // COMPRENSIONE (Lesen) richiede il testo di lettura, e NON deve essere in alfabeto non latino
            val testoLettura = if (tipo == TipoEsercizio.COMPRENSIONE) testoCondiviso
                else e.testoLettura?.trim()?.takeIf { it.isNotEmpty() }
            if (tipo == TipoEsercizio.COMPRENSIONE) {
                if (testoLettura == null) return@mapIndexedNotNull null
                if (contieneNonLatino(testoLettura)) return@mapIndexedNotNull null
            }

            EsercizioGenerato(
                id = "${tipo.jsonTipo}_gen_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(8)}",
                tipo = tipo,
                domanda = domanda,
                opzioni = opzioni,
                rispostaCorretta = rispostaCorretta,
                spiegazione = e.spiegazione?.trim().orEmpty().ifEmpty { "Nessuna spiegazione disponibile." },
                livello = e.livello?.trim()?.takeIf { it.isNotEmpty() } ?: livelloDefault,
                fraseTedesca = e.fraseTedesca?.trim()?.takeIf { it.isNotEmpty() },
                traduzioneItaliana = e.traduzioneItaliana?.trim()?.takeIf { it.isNotEmpty() },
                testoLettura = testoLettura,
                glossario = glossarioCondiviso
            )
        }

        // Deduplica per CONTENUTO: il modello può ripetere la stessa domanda
        // più volte nel batch (id diversi, testo uguale). Per l'ascolto la
        // frase fa parte della chiave, così due audio diversi con la stessa
        // domanda generica restano due esercizi distinti.
        fun chiaveDi(e: EsercizioGenerato): String =
            DeduplicaEsercizi.chiave(
                e.domanda,
                if (tipo == TipoEsercizio.ASCOLTO) e.fraseTedesca else null
            )

        return DeduplicaEsercizi.distinti(validi, ::chiaveDi)
            .take(quanti)
    }

    /** Trova il primo blocco JSON bilanciato nella risposta. */
    private fun estraiJson(testo: String): String? {
        val start = testo.indexOf('{')
        if (start < 0) return null
        var depth = 0
        var inString = false
        var escape = false
        for (i in start until testo.length) {
            val c = testo[i]
            when {
                escape -> escape = false
                c == '\\' && inString -> escape = true
                c == '"' -> inString = !inString
                !inString && c == '{' -> depth++
                !inString && c == '}' -> {
                    depth--
                    if (depth == 0) return testo.substring(start, i + 1)
                }
            }
        }
        return null
    }
}

// ── Modelli di compatibilità per il generatore (evitano di toccare TutorService) ──

@Serializable
private data class ChatMessageCompat(val role: String, val content: String)

@Serializable
private data class ChatRequestCompat(
    val model: String,
    val messages: List<ChatMessageCompat>,
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    val temperature: Double = 0.25,
    @SerialName("reasoning_effort") val reasoningEffort: String? = null
)

@Serializable
private data class ChatMessageRespCompat(val content: String)

@Serializable
private data class ChatChoiceCompat(@SerialName("message") val messageCompat: ChatMessageRespCompat? = null)

@Serializable
private data class ChatResponseCompat(val choices: List<ChatChoiceCompat> = emptyList())
