package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ProfiloConfig
import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.TipoProfilo
import com.alessandro.tedesco.data.local.WordSource

/**
 * Logica pura per la gestione dei preset dei profili.
 *
 * Estratta da [ProfileManager] per poterla testare senza Android/DataStore.
 * Il bug storico era: la lista profili restava vuota perché due coroutine
 * concorrenti (lettura da DataStore e creazione preset) si sovrascrivevano.
 */
object ProfiliPreset {

    const val FEED_URL =
        "https://raw.githubusercontent.com/ItsZale0/tedesco-vocab/main/vokabeln.json"
    /** Vocabolario personale di Emma: ospitato sul GitHub di Alessandro. */
    const val FEED_URL_EMMA =
        "https://raw.githubusercontent.com/ItsZale0/tedesco-vocab-emma/main/vokabeln.json"
    const val GUIDA_DOC_ID = "12yKY4Bpp6IqX7q8tgNYkFXIoAQsZR8yVd4mZhcD5I7g"
    const val SHEET_CUSTOM = "1OZ0BIbOC1wRVwWjJOLpgiBLGf6OzkRzJL7n9-5WbfD8"

    /** Canale ntfy su cui l'app pubblica le parole nuove di Emma. */
    const val NTFY_TOPIC_EMMA = "tedesco-emma-vocab-c7f3a91b"

    /**
     * Feed predefinito per tipo di profilo.
     *
     * Emma NON usa il vocabolario base: ha il suo, separate, ospitato sul
     * GitHub di Alessandro (tedesco-vocab-emma). Il default val "sempre":
     * è il punto di ritorno di migrazioni e bottone di ripristino.
     */
    fun feedPredefinito(tipo: TipoProfilo): String = when (tipo) {
        TipoProfilo.EMMA -> FEED_URL_EMMA
        else -> FEED_URL
    }

    /**
     * Chiave Groq di default per il tutor AI.
     * Sostituire con una chiave reale prima di pubblicare l'app.
     * L'utente può sovrascriverla in Profilo → Tutor AI.
     */
    const val DEFAULT_TUTOR_API_KEY = "sk-or-v1-REPLACE_WITH_REAL_KEY"

    /** Id di tutti i preset previsti, in ordine. */
    val ID_PRESET: List<String> = TipoProfilo.entries.map { it.id }

    /** Id dei preset assenti da [presenti]. */
    fun mancanti(presenti: Set<String>): Set<String> = ID_PRESET.toSet() - presenti

    /** Costruisce i profili preset. Nessuno viene attivato. */
    fun crea(now: Long = System.currentTimeMillis()): Map<String, ProfiloUtente> {
        fun preset(tipo: TipoProfilo, sheetId: String?, feedUrl: String = "", guidaDocId: String? = null): ProfiloUtente = ProfiloUtente(
            id = tipo.id,
            config = ProfiloConfig(
                tipo = tipo,
                nomeVisualizzato = tipo.nome,
                enableCustomWords = tipo.enableCustomWords,
                enableGoogleSheets = tipo.enableGoogleSheets,
                googleSheetId = sheetId,
                feedUrl = feedUrl,
                guidaDocId = guidaDocId,
                tutorApiKey = DEFAULT_TUTOR_API_KEY,
                livelloIniziale = tipo.livelloIniziale,
                mostraContestoMedico = tipo.mostraContestoMedico
            ),
            stato = ProfiloStato(
                progresso = ProgressoUtente(
                    livelloCorrente = tipo.livelloIniziale.toLivelloCEFR()
                )
            ),
            creatoIl = now,
            ultimoAccesso = now
        )

        return mapOf(
            TipoProfilo.ALESSANDRO.id to preset(TipoProfilo.ALESSANDRO, null, FEED_URL, GUIDA_DOC_ID),
            // Emma: vocabolario suo, ospitato sul GitHub di Alessandro.
            // Parte vuota e si popola con le parole che aggiunge nell'app.
            TipoProfilo.EMMA.id to preset(TipoProfilo.EMMA, SHEET_CUSTOM, FEED_URL_EMMA, GUIDA_DOC_ID)
        )
    }

    /**
     * Id del vecchio profilo "Alessandro Personalizzato", rimosso nell'1.43.3.
     * Serve solo alla migrazione dei dati già salvati.
     */
    private const val VECCHIO_ALESSANDRO_CUSTOM = "alessandro_custom"

    /**
     * Riconcilia quanto letto da DataStore con i preset previsti:
     * i preset mancanti vengono aggiunti, quelli già presenti (con i loro
     * progressi) restano intatti.
     *
     * Migrazioni incluse:
     * - il profilo "Alessandro Personalizzato" viene eliminato e le sue parole
     *   personalizzate passano al profilo Alessandro base;
     * - le parole personalizzate vengono abilitate su Alessandro e Emma;
     * - se il profilo attivo era quello rimosso, si passa ad Alessandro.
     */
    fun riconcilia(letto: ProfiliRepository): ProfiliRepository {
        val migrato = migraAlessandroCustom(letto)
        val aggiornato = abilitaParoleCustom(migrato)
        val allineato = allineaFeedEmma(aggiornato)
        val mancanti = mancanti(allineato.profili.keys)
        if (mancanti.isEmpty()) return allineato
        val aggiunte = crea().filterKeys { it in mancanti }
        return allineato.copy(profili = allineato.profili + aggiunte)
    }

    /**
     * Allinea il feed di Emma al vocabolario separato.
     *
     * I profili creati prima del vocabolario separato portavano il feed
     * condiviso o niente: qui li si riporta a [FEED_URL_EMMA], che è il
     * default per Emma e viene riapplicato a ogni avvio.
     * Un URL scelto apposta (diverso dai default) viene rispettato:
     * potrebbe puntare a un vocabolario alternativo voluto.
     */
    private fun allineaFeedEmma(letto: ProfiliRepository): ProfiliRepository {
        val profili = letto.profili.mapValues { (id, profilo) ->
            if (id != TipoProfilo.EMMA.id) return@mapValues profilo
            val corrente = profilo.config.feedUrl.trim()
            if (corrente.isEmpty() || corrente == FEED_URL) {
                profilo.copy(config = profilo.config.copy(feedUrl = FEED_URL_EMMA))
            } else {
                profilo
            }
        }
        return if (profili == letto.profili) letto else letto.copy(profili = profili)
    }

    /** Elimina il vecchio profilo custom e ne assorbe le parole in Alessandro. */
    private fun migraAlessandroCustom(letto: ProfiliRepository): ProfiliRepository {
        val vecchio = letto.profili[VECCHIO_ALESSANDRO_CUSTOM] ?: return letto
        val base = letto.profili[TipoProfilo.ALESSANDRO.id]
        val restanti = letto.profili - VECCHIO_ALESSANDRO_CUSTOM

        if (base == null) {
            return letto.copy(
                profili = restanti,
                profiloAttivoId = if (letto.profiloAttivoId == VECCHIO_ALESSANDRO_CUSTOM) null else letto.profiloAttivoId
            )
        }

        val idEsistenti = base.stato.parole.map { it.id }.toSet()
        val paroleCustom = vecchio.stato.parole.filter {
            it.source == WordSource.CUSTOM && it.id !in idEsistenti
        }
        val idCustom = paroleCustom.map { it.id }.toSet()
        val reviewsCustom = vecchio.stato.reviews.filterKeys { it in idCustom }

        val unito = base.copy(
            config = base.config.copy(enableCustomWords = true),
            stato = base.stato.copy(
                parole = base.stato.parole + paroleCustom,
                reviews = base.stato.reviews + reviewsCustom
            )
        )

        return letto.copy(
            profili = restanti + (unito.id to unito),
            profiloAttivoId = if (letto.profiloAttivoId == VECCHIO_ALESSANDRO_CUSTOM) unito.id else letto.profiloAttivoId
        )
    }

    /** I profili che possono aggiungere parole devono poterlo fare dopo l'update. */
    private fun abilitaParoleCustom(letto: ProfiliRepository): ProfiliRepository {
        val profili = letto.profili.mapValues { (_, profilo) ->
            val tipo = profilo.config.tipo
            if (tipo.enableCustomWords && !profilo.config.enableCustomWords) {
                profilo.copy(config = profilo.config.copy(enableCustomWords = true))
            } else {
                profilo
            }
        }
        return if (profili == letto.profili) letto else letto.copy(profili = profili)
    }
}
