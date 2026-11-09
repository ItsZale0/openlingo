package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloIniziale
import com.alessandro.tedesco.data.local.ProfiloConfig
import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.TipoProfilo
import com.alessandro.tedesco.data.local.WordSource

/**
 * Logica pura per la creazione e la manutenzione dei profili.
 *
 * Da questa versione l'app è pensata per QUALSIASI utente:
 * - al primo avvio non esiste nessun profilo: lo crea l'utente;
 * - non viene più creato automaticamente alcun profilo;
 * - i profili già presenti (compresi quelli storici "alessandro" ed "emma")
 *   restano intatti, con tutti i loro progressi.
 *
 * Estratta da [ProfileManager] per poterla testare senza Android/DataStore.
 */
object ProfiliPreset {

    /**
     * Feed remoto predefinito: VUOTO.
     *
     * Il lessico di base è incluso nell'app (A1-B2) e funziona offline.
     * Un feed remoto è una funzione opzionale che ogni utente configura con
     * il proprio repository: nessun URL personale è più incorporato.
     */
    const val FEED_URL = ""

    /**
     * Chiave del tutor AI predefinita: VUOTA.
     *
     * L'app funziona offline senza AI. Chi vuole il tutor inserisce la propria
     * chiave in Impostazioni; nessuna chiave condivisa viene distribuita.
     */
    const val DEFAULT_TUTOR_API_KEY = ""

    /** Vecchi URL personali: servono solo a ripulire i profili esistenti. */
    private val VECCHI_FEED_PERSONALI = setOf(
        "https://raw.githubusercontent.com/ItsZale0/tedesco-vocab/main/vokabeln.json",
        "https://raw.githubusercontent.com/ItsZale0/tedesco-vocab-emma/main/vokabeln.json"
    )

    /** Vecchio documento Google della guida personale. */
    private const val VECCHIA_GUIDA_DOC = "12yKY4Bpp6IqX7q8tgNYkFXIoAQsZR8yVd4mZhcD5I7g"

    /** Feed predefinito per tipo di profilo (nessuno). */
    fun feedPredefinito(tipo: TipoProfilo): String = FEED_URL

    /**
     * Costruisce un nuovo profilo pronto all'uso.
     *
     * @param id identificatore stabile (non cambia mai)
     * @param nome nome mostrato all'utente
     * @param livello livello CEFR di partenza
     * @param contesto contesto personale per l'AI (professione, obiettivi)
     * @param mostraContestoMedico true = contenuti con lessico sanitario
     */
    fun nuovo(
        id: String,
        nome: String,
        livello: LivelloIniziale = LivelloIniziale.A0,
        contesto: String = "",
        mostraContestoMedico: Boolean = false,
        tipo: TipoProfilo = TipoProfilo.PERSONALIZZATO,
        now: Long = System.currentTimeMillis()
    ): ProfiloUtente = ProfiloUtente(
        id = id,
        config = ProfiloConfig(
            tipo = tipo,
            nomeVisualizzato = nome,
            enableCustomWords = true,
            enableGoogleSheets = false,
            googleSheetId = null,
            feedUrl = "",
            guidaDocId = null,
            tutorApiKey = DEFAULT_TUTOR_API_KEY,
            livelloIniziale = livello,
            mostraContestoMedico = mostraContestoMedico,
            contestoUtente = contesto.trim()
        ),
        stato = ProfiloStato(
            progresso = ProgressoUtente(livelloCorrente = livello.toLivelloCEFR())
        ),
        creatoIl = now,
        ultimoAccesso = now
    )

    /**
     * Id del vecchio profilo "Alessandro Personalizzato", rimosso nell'1.43.3.
     * Serve solo alla migrazione dei dati già salvati.
     */
    private const val VECCHIO_ALESSANDRO_CUSTOM = "alessandro_custom"

    /**
     * Ripulisce quanto letto da DataStore SENZA creare profili.
     *
     * Migrazioni incluse (solo per dati già esistenti):
     * - il profilo "Alessandro Personalizzato" viene eliminato e le sue parole
     *   personalizzate passano al profilo "alessandro";
     * - le parole personalizzate vengono abilitate dove previste;
     * - i feed e i documenti Google personali vengono azzerati: restano solo
     *   gli URL scelti dall'utente.
     *
     * Se il repository è vuoto, resta vuoto: è il primo avvio e l'utente
     * crea il proprio profilo.
     */
    fun riconcilia(letto: ProfiliRepository): ProfiliRepository {
        val migrato = migraAlessandroCustom(letto)
        val paroleOk = abilitaParoleCustom(migrato)
        val pulito = pulisciRisorsePersonali(paroleOk)
        return pulito
    }

    /** Elimina il vecchio profilo custom e ne assorbe le parole in "alessandro". */
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

    /**
     * Rimuove i riferimenti a risorse personali dell'autore:
     * i vecchi feed del vocabolario e il vecchio documento Google della guida.
     * Un URL diverso (scelto dall'utente) viene mantenuto.
     */
    private fun pulisciRisorsePersonali(letto: ProfiliRepository): ProfiliRepository {
        val profili = letto.profili.mapValues { (_, profilo) ->
            val feed = profilo.config.feedUrl.trim()
            val guida = profilo.config.guidaDocId
            val nuovoFeed = if (feed in VECCHI_FEED_PERSONALI) "" else profilo.config.feedUrl
            val nuovaGuida = if (guida == VECCHIA_GUIDA_DOC) null else guida
            if (nuovoFeed != profilo.config.feedUrl || nuovaGuida != guida) {
                profilo.copy(config = profilo.config.copy(feedUrl = nuovoFeed, guidaDocId = nuovaGuida))
            } else {
                profilo
            }
        }
        return if (profili == letto.profili) letto else letto.copy(profili = profili)
    }
}
