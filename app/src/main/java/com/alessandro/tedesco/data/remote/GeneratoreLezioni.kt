package com.alessandro.tedesco.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.alessandro.tedesco.data.local.ConfigurazioneAI
import java.io.IOException

/**
 * Genera una lezione COMPLETA (contenuto + vocabolario) per il livello dello studente.
 *
 * Perche' esiste: le lezioni arrivavano solo dal feed JSON, che per Emma era VUOTO
 * (0 parole, 0 lezioni) e per Alessandro aveva un buco (lezioni 1,2,3,5 - mancava la 4)
 * e la lezione 5 senza nemmeno una parola. Risultato: "le lezioni non escono" e
 * "non aggiornano il lessico". Qui la lezione si genera al momento, sul livello reale.
 */

@Serializable
private data class ParolaLezioneAi(
    val tedesco: String? = null,
    val italiano: String? = null,
    val articolo: String? = null,
    val esempio: String? = null
)

@Serializable
private data class LezioneAi(
    val titolo: String? = null,
    val contenuto: String? = null,
    val parole: List<ParolaLezioneAi> = emptyList()
)

/** Una parola generata per una lezione. */
data class ParolaLezione(
    val tedesco: String,
    val italiano: String,
    val articolo: String?,
    val esempio: String
)

/** Lezione generata: contenuto markdown + parole nuove. */
data class LezioneGenerata(
    val titolo: String,
    val contenuto: String,
    val parole: List<ParolaLezione>
)

class GeneratoreLezioni(
    private val clientAI: ClientAI = ClientAI()
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Genera la lezione [numero] per il livello [livello].
     *
     * @param config provider, chiave e modello scelti dal profilo
     * @param argomento tema della lezione (es. "Città e paese"); se vuoto lo sceglie il modello
     * @param paroleNote parole gia' presenti, da NON ripetere
     */
    suspend fun genera(
        config: ConfigurazioneAI,
        numero: Int,
        livello: String,
        argomento: String,
        paroleNote: List<String> = emptyList(),
        contesto: String = "",
        contestoMedico: Boolean = false
    ): LezioneGenerata = withContext(Dispatchers.IO) {
        val messaggi = listOf(
            MessaggioAI(
                "user",
                "Genera la lezione $numero per il livello $livello."
            )
        )

        var ultimoErrore: Exception? = null
        for (tentativo in 1..2) {
            try {
                val testo = clientAI.chat(
                    config = config,
                    system = promptSistema(livello, numero, argomento, paroleNote, contesto, contestoMedico),
                    cronologia = messaggi,
                    maxTokens = 4096,
                    temperature = 0.35,
                    reasoningEffort = "low"
                )
                val parsed = estrai(testo)
                if (parsed != null && parsed.parole.isNotEmpty()) return@withContext parsed
                ultimoErrore = IOException("Risposta non valida dal generatore di lezioni")
            } catch (e: IOException) {
                ultimoErrore = e
                if (e.message?.contains("non valida", ignoreCase = true) == true) throw e
            }
        }
        throw ultimoErrore ?: IOException("Generazione della lezione non riuscita")
    }

    private fun promptSistema(
        livello: String,
        numero: Int,
        argomento: String,
        paroleNote: List<String>,
        contesto: String = "",
        contestoMedico: Boolean = false
    ): String {
        val tema = argomento.ifBlank { "scegli tu un tema utile e nuovo per questo livello" }
        val daEvitare = if (paroleNote.isEmpty()) ""
            else "\n- NON ripetere queste parole, gia' studiate: " + paroleNote.take(60).joinToString(", ")
        val contestoTesto = buildString {
            if (contestoMedico) {
                append(
                    "\n- Contesto professionale: ambito sanitario. " +
                        "Dove ha senso, usa situazioni e lessico medico/ospedaliero."
                )
            }
            val personale = contesto.trim()
            if (personale.isNotEmpty()) {
                append(
                    "\n- Contesto dichiarato dallo studente: \"${personale.take(400)}\". " +
                        "Scegli esempi, dialoghi e vocabolario pertinenti a questo contesto, " +
                        "mantenendo la difficolta' del livello $livello."
                )
            }
        }

        return """
            Sei un insegnante di tedesco per studenti italiani. Genera la LEZIONE $numero.
            LIVELLO DELLO STUDENTE: $livello
            ARGOMENTO: $tema$contestoTesto

            REGOLE DI CALIBRAZIONE (obbligatorie):
            - Usa SOLO grammatica e lessico che uno studente di livello $livello conosce davvero.
            - A0/A1: presente, frasi soggetto-verbo-oggetto, articoli der/die/das, vocabolario quotidiano. NIENTE passato, subordinate, congiuntivo.
            - A2: presente + Perfetto, accusativo/dativo, modali, frasi semplici legate.
            - B1: passati, subordinate (weil/dass/wenn), passivo semplice, frasi piu' lunghe.
            - B2: congiuntivo, lessico astratto, strutture complesse.
            - Il tedesco deve essere CORRETTO al 100%: nessun errore di ortografia, articolo, caso o ordine delle parole.

            CONTENUTO (campo "contenuto", markdown in ITALIANO con esempi in TEDESCO):
            ## Obiettivo
            ## Riscaldamento (3-5 domande di ripasso a voce)
            ## Nuova lezione (spiegazione della grammatica, con tabelle)
            ## Esempi (frasi tedesche con traduzione italiana)
            ## Vocabolario (tabella: Deutsch | Italiano)
            ## Esercizi (4-6 esercizi pratici con soluzione)

            VOCABOLARIO (campo "parole"): esattamente 12 parole NUOVE, utili per l'argomento e per il livello $livello.
            - Ogni parola DEVE avere: articolo (der/die/das per i sostantivi), traduzione italiana, frase d'esempio tedesca corretta.
            - Niente parole astratte o inutili: solo vocabolario che serve a parlare davvero.$daEvitare

            Rispondi SOLO con questo JSON, senza testo attorno:
            {"titolo":"<titolo breve>","contenuto":"<markdown>","parole":[{"tedesco":"die Stadt","italiano":"la citta'","articolo":"die","esempio":"Ich wohne in einer grossen Stadt."}]}
        """.trimIndent()
    }

    /** Estrae il JSON dalla risposta (il modello a volte aggiunge ```json o testo attorno). */
    private fun estrai(testo: String): LezioneGenerata? {
        val pulito = testo.replace("```json", "").replace("```", "").trim()
        val inizio = pulito.indexOf('{')
        val fine = pulito.lastIndexOf('}')
        if (inizio < 0 || fine <= inizio) return null
        val body = pulito.substring(inizio, fine + 1)

        val ai = try {
            json.decodeFromString(LezioneAi.serializer(), body)
        } catch (e: Exception) {
            return null
        }

        val contenuto = ai.contenuto?.trim().orEmpty()
        if (contenuto.length < 200) return null

        val parole = ai.parole.mapNotNull { p ->
            val de = p.tedesco?.trim().orEmpty()
            val it = p.italiano?.trim().orEmpty()
            if (de.isBlank() || it.isBlank()) return@mapNotNull null
            if (contieneNonLatino(de) || contieneNonLatino(it)) return@mapNotNull null
            ParolaLezione(
                tedesco = de,
                italiano = it,
                articolo = p.articolo?.trim()?.ifBlank { null },
                esempio = p.esempio?.trim().orEmpty()
            )
        }.distinctBy { it.tedesco.lowercase() }

        return LezioneGenerata(
            titolo = ai.titolo?.trim()?.ifBlank { null } ?: "Lezione",
            contenuto = contenuto,
            parole = parole
        )
    }

    /** Guardia: scarta output in alfabeti non latini (arabo/cirillico/CJK). */
    private fun contieneNonLatino(t: String): Boolean =
        Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u0400-\\u04FF\\u4E00-\\u9FFF\\u3040-\\u30FF]")
            .containsMatchIn(t)
}
