package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.WordEntity

/**
 * Allenatore articoli (der/die/das).
 *
 * Usa parole già presenti nel lessico/profilo (e opzionalmente curate) con
 * articolo determinativo singolare, filtrate per livello.
 */
object GenderTrainer {

    data class Domanda(
        val id: String,
        val parola: String,      // sostantivo senza articolo
        val articolo: String,    // der | die | das
        val traduzione: String,
        val esempio: String = "",
        val livello: String = ""
    )

    private val ARTICOLI = setOf("der", "die", "das")

    private fun livelloOrdine(livello: String?): Int =
        LivelloCEFR.daTesto(livello).ordine

    fun articoloNormalizzato(article: String?): String? {
        val a = article?.trim()?.lowercase()
        return if (a in ARTICOLI) a else null
    }

    /** Estrae articolo+sostantivo da una voce tipo "der Tisch". */
    fun daTedescoConArticolo(tedesco: String): Pair<String, String>? {
        val t = tedesco.trim()
        val art = t.substringBefore(" ").lowercase()
        if (art !in ARTICOLI) return null
        val parola = t.substringAfter(" ").trim()
        if (parola.isBlank()) return null
        return art to parola
    }

    /**
     * Costruisce il pool da parole del profilo (priorità) + eventuali fonti
     * curate, filtrato per livello selezionato (e inferiori).
     * Se [limite] è null o <= 0 usa tutte le parole disponibili.
     */
    fun generaDomande(
        paroleProfilo: List<WordEntity>,
        livelloSelezionato: LivelloCEFR,
        fontiCurate: List<LessicoLivelli.Voce> = emptyList(),
        limite: Int? = null
    ): List<Domanda> {
        val maxOrd = livelloSelezionato.ordine
        val viste = HashSet<String>()

        fun accettaLivello(lv: String?): Boolean = livelloOrdine(lv) <= maxOrd

        val daProfilo = buildList {
            for (w in paroleProfilo) {
                val art = articoloNormalizzato(w.article) ?: continue
                if (!accettaLivello(w.level)) continue
                val parola = w.german.trim()
                if (parola.isBlank()) continue
                val chiave = (art + "|" + parola.lowercase())
                if (!viste.add(chiave)) continue
                add(
                    Domanda(
                        id = "p_${w.id}",
                        parola = parola,
                        articolo = art,
                        traduzione = w.italian.trim(),
                        esempio = w.example.trim(),
                        livello = w.level
                    )
                )
            }
        }

        val daCurate = buildList {
            for (v in fontiCurate) {
                if (!accettaLivello(v.livello)) continue
                val (art, parola) = daTedescoConArticolo(v.tedesco) ?: continue
                val chiave = (art + "|" + parola.lowercase())
                if (!viste.add(chiave)) continue
                add(
                    Domanda(
                        id = "c_${v.livello}_${parola.lowercase().replace(' ', '_')}",
                        parola = parola,
                        articolo = art,
                        traduzione = v.italiano.trim(),
                        esempio = v.esempio.trim(),
                        livello = v.livello
                    )
                )
            }
        }

        // Priorità alle parole del profilo, poi completamento con curate (tutte, mescolate)
        val tutte = daProfilo.shuffled() + daCurate.shuffled()
        return if (limite != null && limite > 0) tutte.take(limite) else tutte
    }

    /** Pool predefinito curato (LessicoLivelli + Wortliste) per fallback/generico. */
    fun domandeCurate(
        livelloSelezionato: LivelloCEFR,
        limite: Int? = null
    ): List<Domanda> {
        val curate = LessicoLivelli.tutte + WortlisteB1.tutte.map {
            LessicoLivelli.Voce(it.tedesco, it.italiano, it.esempio, it.tema, it.livello)
        }
        return generaDomande(
            paroleProfilo = emptyList(),
            livelloSelezionato = livelloSelezionato,
            fontiCurate = curate.distinctBy { it.tedesco.lowercase() + "|" + it.livello },
            limite = limite
        )
    }

    /** Pool misto: profilo + curate (senza duplicati), filtrato per livello. */
    fun domandeMiste(
        paroleProfilo: List<WordEntity>,
        livelloSelezionato: LivelloCEFR,
        limite: Int? = null
    ): List<Domanda> {
        val curate = LessicoLivelli.tutte + WortlisteB1.tutte.map {
            LessicoLivelli.Voce(it.tedesco, it.italiano, it.esempio, it.tema, it.livello)
        }
        return generaDomande(
            paroleProfilo = paroleProfilo,
            livelloSelezionato = livelloSelezionato,
            fontiCurate = curate.distinctBy { it.tedesco.lowercase() + "|" + it.livello },
            limite = limite
        )
    }
}
