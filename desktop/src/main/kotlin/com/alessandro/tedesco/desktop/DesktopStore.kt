package com.alessandro.tedesco.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.alessandro.tedesco.data.BackupCloud
import com.alessandro.tedesco.data.BackupMerge
import com.alessandro.tedesco.data.CalcoloCompetenze
import com.alessandro.tedesco.data.CalcoloPercorsoGiornaliero
import com.alessandro.tedesco.data.CalcoloProgresso
import com.alessandro.tedesco.data.CalcoloStatistiche
import com.alessandro.tedesco.data.FeedMerge
import com.alessandro.tedesco.data.GrammaticaSyllabus
import com.alessandro.tedesco.data.PadronanzaGrammatica
import com.alessandro.tedesco.data.ProfiliPreset
import com.alessandro.tedesco.data.SrsLessico
import com.alessandro.tedesco.data.Statistiche
import com.alessandro.tedesco.data.StatoUnita
import com.alessandro.tedesco.data.UnitaConStato
import com.alessandro.tedesco.data.WortlisteB1
import com.alessandro.tedesco.data.local.CertificazioneEntity
import com.alessandro.tedesco.data.local.FeedLogEntity
import com.alessandro.tedesco.data.local.GuidaEntity
import com.alessandro.tedesco.data.local.LessonEntity
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.LivelloIniziale
import com.alessandro.tedesco.data.local.PianoEntity
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProgressoFeedEntity
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.RisorsaEntity
import com.alessandro.tedesco.data.local.RotazioneUnita
import com.alessandro.tedesco.data.local.SessioneEntity
import com.alessandro.tedesco.data.local.SezioneEntity
import com.alessandro.tedesco.data.local.TappaEntity
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.local.WordSource
import com.alessandro.tedesco.data.remote.BackupGithub
import com.alessandro.tedesco.data.remote.FeedDto
import com.alessandro.tedesco.data.remote.FeedGithub
import com.alessandro.tedesco.data.toLivelloCEFR
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import kotlin.math.max

@Serializable
data class Preferenze(
    val temaScuro: Boolean = true,
    val profiloAttivoId: String? = null,
    /**
     * Token GitHub per il backup cloud. Salvato in locale come fa l'app
     * Android (DataStore): e' un token fine-grained limitato al solo repo di
     * backup, non la password dell'account.
     */
    val githubToken: String = "",
    val githubRepo: String = "",
    val ultimoBackup: Long = 0L,
    val ultimoHashCloud: String = "",
    val ultimaSincronizzazioneCloud: Long = 0L
)

/**
 * Stato e persistenza della versione desktop.
 *
 * I dati vivono in `~/.tedesco-desktop/`, in JSON leggibile: nessun database,
 * nessun servizio esterno. Il modello e' lo stesso dell'app Android
 * ([ProfiliRepository]), quindi i due profili (Emma e Alessandro) mantengono
 * vocabolario, ripassi, percorso e progressi separati.
 */
class DesktopStore(
    private val cartella: File = File(System.getProperty("user.home"), ".tedesco-desktop")
) {
    private val fileProfili = File(cartella, "profili.json")
    private val filePreferenze = File(cartella, "preferenze.json")

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /** Json del feed: tollerante sui campi aggiunti in futuro, come l'app. */
    private val jsonFeed = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    var repo: ProfiliRepository by mutableStateOf(caricaProfili())
        private set

    var preferenze: Preferenze by mutableStateOf(caricaPreferenze())
        private set

    val percorsoDati: String get() = cartella.absolutePath

    // ---------------------------------------------------------------- profilo

    val profilo: ProfiloUtente?
        get() = repo.profiloAttivoId?.let { repo.profili[it] }

    val stato: ProfiloStato get() = profilo?.stato ?: ProfiloStato()

    val elencoProfili: List<ProfiloUtente> get() = repo.profili.values.sortedBy { it.creatoIl }

    fun attivaProfilo(id: String) {
        if (repo.profili[id] == null) return
        repo = repo.copy(profiloAttivoId = id)
        preferenze = preferenze.copy(profiloAttivoId = id)
        salva()
    }

    fun aggiornaProfilo(trasforma: (ProfiloUtente) -> ProfiloUtente) {
        val corrente = profilo ?: return
        repo = repo.copy(profili = repo.profili + (corrente.id to trasforma(corrente)))
        salva()
    }

    fun aggiornaStato(trasforma: (ProfiloStato) -> ProfiloStato) {
        aggiornaProfilo { it.copy(stato = trasforma(it.stato)) }
    }

    fun rinominaProfilo(nome: String) {
        val pulito = nome.trim()
        if (pulito.isEmpty()) return
        aggiornaProfilo { it.copy(config = it.config.copy(nomeVisualizzato = pulito)) }
    }

    fun impostaLivelloEObiettivo(iniziale: LivelloIniziale, obiettivo: LivelloCEFR) {
        aggiornaProfilo {
            it.copy(
                config = it.config.copy(livelloIniziale = iniziale),
                stato = it.stato.copy(
                    progresso = it.stato.progresso.copy(
                        livelloCorrente = iniziale.toLivelloCEFR(),
                        obiettivoLivello = obiettivo
                    )
                )
            )
        }
    }

    /** Porta le parole della Wortliste nel lessico del profilo attivo. */
    fun importaWortliste(): Int {
        // Confronto per lemma, non per id: una parola gia' arrivata dal feed
        // del corso non deve entrare una seconda volta con l'id della Wortliste.
        val esistenti = stato.parole.mapTo(HashSet()) { lemma(it) }
        val nuove = paroleWortliste().filter { lemma(it) !in esistenti }
        if (nuove.isEmpty()) return 0
        aggiornaStato { it.copy(parole = it.parole + nuove) }
        return nuove.size
    }

    /** La voce della Wortliste e' gia' nel lessico (per lemma, non per id)? */
    fun giaNelLessico(voce: WortlisteB1.Voce): Boolean {
        val chiave = lemmaVoce(voce)
        return stato.parole.any { lemma(it) == chiave }
    }

    fun aggiungiParola(voce: WortlisteB1.Voce): Boolean {
        val parola = daVoceWortliste(voce, System.currentTimeMillis())
        if (stato.parole.any { lemma(it) == lemma(parola) }) return false
        aggiornaStato { it.copy(parole = it.parole + parola) }
        return true
    }

    fun rimuoviParola(wordId: String) {
        aggiornaStato { st ->
            st.copy(
                parole = st.parole.filterNot { it.id == wordId },
                reviews = st.reviews - wordId
            )
        }
    }

    fun svuotaProfilo() {
        aggiornaStato { st ->
            st.copy(
                parole = emptyList(),
                reviews = emptyMap(),
                ripassiLog = emptyList(),
                padronanzaGrammatica = emptyMap(),
                progressiLezioni = emptyMap(),
                rotazioniEsercizi = emptyMap(),
                passiGiornoIds = emptyList(),
                streakGiorni = 0,
                ultimoGiornoStudio = null
            )
        }
    }

    // ---------------------------------------------------------------- ripasso

    /** Parole in scadenza, ordinate per urgenza (max [limite]). */
    fun paroleDaRipassare(limite: Int = 20, now: Long = System.currentTimeMillis()): List<WordEntity> {
        val attive = stato.parole.filterNot { it.archived }.associateBy { it.id }
        return stato.reviews.values
            .filter { it.dueAt <= now && attive.containsKey(it.wordId) }
            .sortedBy { it.dueAt }
            .take(limite)
            .mapNotNull { attive[it.wordId] }
    }

    /** Parole mai ripassate: servono quando la coda SRS e' vuota. */
    fun paroleNuove(limite: Int = 20): List<WordEntity> =
        stato.parole
            .filterNot { it.archived }
            .filter { stato.reviews[it.id]?.repetitions ?: 0 == 0 }
            .sortedBy { it.createdAt }
            .take(limite)

    /**
     * Registra la risposta a una flashcard. Crea la carta SRS se e' la prima
     * volta e aggiunge l'evento al log (evidenza per le statistiche).
     */
    fun rispondiRipasso(wordId: String, saputa: Boolean) {
        val now = System.currentTimeMillis()
        val esistente = stato.reviews[wordId] ?: SrsLessico.nuova(wordId, now)
        val aggiornata = SrsLessico.conRisposta(esistente, saputa, now)
        val log = (stato.ripassiLog + RipassoLogEntity(wordId, now, saputa)).takeLast(5000)
        aggiornaStato { st ->
            registraStudio(
                st.copy(
                    reviews = st.reviews + (wordId to aggiornata),
                    ripassiLog = log
                ),
                now
            )
        }
    }

    // ------------------------------------------------------------- grammatica

    fun padronanza(unitaId: String): PadronanzaGrammatica? = stato.padronanzaGrammatica[unitaId]

    fun rotazione(unitaId: String): RotazioneUnita = stato.rotazioniEsercizi[unitaId] ?: RotazioneUnita()

    /** Salva esito di una sessione di esercizi e la rotazione dei quesiti. */
    fun salvaSessione(
        unitaId: String,
        corrette: Int,
        totali: Int,
        rotazione: RotazioneUnita
    ) {
        val now = System.currentTimeMillis()
        val nuovaPadronanza = GrammaticaSyllabus.valuta(
            unitaId = unitaId,
            corrette = corrette,
            totali = totali,
            precedente = stato.padronanzaGrammatica[unitaId],
            now = now
        )
        aggiornaStato { st ->
            registraStudio(
                st.copy(
                    padronanzaGrammatica = st.padronanzaGrammatica + (unitaId to nuovaPadronanza),
                    rotazioniEsercizi = st.rotazioniEsercizi + (unitaId to rotazione)
                ),
                now
            )
        }
    }

    // ------------------------------------------------------------ piano di oggi

    fun completaPasso(passoId: String) {
        val oggi = LocalDate.now().toString()
        aggiornaStato { st ->
            val ids = if (st.passiGiornoData == oggi) st.passiGiornoIds else emptyList()
            st.copy(
                passiGiornoData = oggi,
                passiGiornoIds = (ids + passoId).distinct()
            )
        }
    }

    fun azzeraPianoOggi() {
        aggiornaStato { it.copy(passiGiornoData = LocalDate.now().toString(), passiGiornoIds = emptyList()) }
    }

    // ---------------------------------------------------------------- derivati

    fun statistiche(now: Long = System.currentTimeMillis()): Statistiche =
        CalcoloStatistiche.calcola(stato.parole, stato.reviews, stato.progresso, now, stato.ripassiLog)

    fun competenze(now: Long = System.currentTimeMillis()): CalcoloCompetenze.PunteggiCompetenze =
        CalcoloCompetenze.calcola(stato.parole, stato.reviews, stato.progresso, now, stato.ripassiLog)

    fun progressoReale(now: Long = System.currentTimeMillis()): CalcoloProgresso.ProgressoReale =
        CalcoloProgresso.calcola(
            parole = stato.parole,
            reviews = stato.reviews,
            progresso = stato.progresso,
            log = stato.ripassiLog,
            obiettivo = stato.progresso.obiettivoLivello,
            now = now
        )

    fun unita(now: Long = System.currentTimeMillis()): List<UnitaConStato> =
        GrammaticaSyllabus.percorso(
            mappa = stato.padronanzaGrammatica,
            now = now,
            livelloProfilo = stato.progresso.livelloCorrente.codiceBreve()
        )

    fun prossimaUnita(now: Long = System.currentTimeMillis()): UnitaConStato? =
        GrammaticaSyllabus.prossimaUnita(
            mappa = stato.padronanzaGrammatica,
            now = now,
            livelloProfilo = stato.progresso.livelloCorrente.codiceBreve()
        )

    /** Passi del piano di oggi gia' completati (solo se riferiti a oggi). */
    fun passiCompletatiOggi(): Set<String> =
        if (stato.passiGiornoData == LocalDate.now().toString()) stato.passiGiornoIds.toSet() else emptySet()

    fun pianoOggi(now: Long = System.currentTimeMillis()): CalcoloPercorsoGiornaliero.PercorsoGiornaliero {
        val statistiche = statistiche(now)
        val competenze = competenze(now)
        val prossima = prossimaUnita(now)
        return CalcoloPercorsoGiornaliero.genera(
            statistiche = statistiche,
            competenze = competenze,
            daRipassare = statistiche.daRipassare,
            livelloCorrente = stato.progresso.livelloCorrente,
            obiettivoLivello = stato.progresso.obiettivoLivello,
            ultimoTest = stato.progresso.ultimoTest,
            passiCompletatiOggi = passiCompletatiOggi(),
            prossimaUnitaId = prossima?.unita?.id,
            prossimaUnitaTitolo = prossima?.unita?.titolo
        )
    }

    fun unitaCompletate(): Int =
        stato.padronanzaGrammatica.values.count { it.completata }

    fun temaScuro(): Boolean = preferenze.temaScuro

    fun commutaTema() {
        preferenze = preferenze.copy(temaScuro = !preferenze.temaScuro)
        salvaPreferenze()
    }

    // ------------------------------------------------------- sincronizzazione

    /** Esito leggibile di un'operazione di rete, gia' pronto per la UI. */
    data class EsitoRete(val ok: Boolean, val messaggio: String)

    /** Backup scaricato da GitHub, in attesa che l'utente confermi. */
    sealed interface RisultatoCloud {
        data class Trovato(
            val riepilogo: BackupCloud.Riepilogo,
            val contenuto: String
        ) : RisultatoCloud

        data class Errore(val messaggio: String) : RisultatoCloud
    }

    /** Feed configurato per il profilo attivo (quello dell'app Android). */
    val feedUrl: String get() = profilo?.config?.feedUrl.orEmpty()

    fun impostaFeedUrl(url: String) {
        aggiornaProfilo { it.copy(config = it.config.copy(feedUrl = url.trim())) }
    }

    /** Timestamp dell'ultima sincronizzazione del feed. */
    val ultimaSincronizzazione: Long get() = stato.lastSync

    val tokenCloud: String get() = preferenze.githubToken

    val repoCloud: String get() = preferenze.githubRepo.ifBlank { BackupCloud.REPO_DEFAULT }

    val ultimoBackupCloud: Long get() = preferenze.ultimoBackup

    val ultimaSincronizzazioneCloud: Long get() = preferenze.ultimaSincronizzazioneCloud

    var sincronizzazioneCloudInCorso by mutableStateOf(false)
        private set

    /** Stesso nome file usato dall'app Android per il profilo attivo. */
    val nomeFileBackup: String get() = BackupCloud.nomeFilePerProfilo(repo.profiloAttivoId)

    fun salvaTokenCloud(token: String) {
        preferenze = preferenze.copy(githubToken = token.trim())
        salvaPreferenze()
    }

    fun salvaRepoCloud(repoGithub: String) {
        preferenze = preferenze.copy(githubRepo = repoGithub.trim())
        salvaPreferenze()
    }

    /**
     * Scarica il feed del corso e lo fonde con il lessico locale.
     *
     * Usa [FeedMerge], lo stesso codice dell'app Android: le parole del
     * repository entrano con gli stessi criteri, senza duplicati e senza
     * perdere quelle personalizzate. Funzione bloccante: il chiamante la
     * esegue su un thread di I/O.
     */
    fun sincronizzaFeed(): EsitoRete {
        val idProfilo = profilo?.id ?: return EsitoRete(false, "Nessun profilo attivo")
        val url = profilo?.config?.feedUrl.orEmpty().trim()
        if (url.isBlank()) return EsitoRete(false, "URL del feed non configurato")

        val statoPrima = repo.profili[idProfilo]?.stato ?: ProfiloStato()
        val risposta = FeedGithub.scarica(url, statoPrima.etag)

        if (risposta.nonModificato) {
            aggiornaStatoDi(idProfilo) { it.copy(lastSync = System.currentTimeMillis()) }
            return EsitoRete(true, "Gia' aggiornato: il feed non e' cambiato")
        }
        if (!risposta.ok) {
            return erroreFeed(idProfilo, risposta.errore ?: "Errore HTTP ${risposta.codice}")
        }
        val corpo = risposta.corpo
        if (corpo.isNullOrBlank()) return erroreFeed(idProfilo, "Risposta vuota dal server")

        val feed = try {
            jsonFeed.decodeFromString(FeedDto.serializer(), corpo)
        } catch (e: Exception) {
            return erroreFeed(idProfilo, "Feed non leggibile: ${e.message}")
        }

        val now = System.currentTimeMillis()
        val esito = FeedMerge.applica(
            parole = statoPrima.parole,
            reviews = statoPrima.reviews,
            feed = feed,
            now = now
        )

        // Il desktop parte con la Wortliste curata (id "wort-..."): quando il
        // feed porta la stessa parola con il suo id, il segnaposto viene
        // sostituito, altrimenti la stessa parola apparirebbe due volte.
        // Se il segnaposto era gia' stato ripassato, la carta SRS (ripetizioni,
        // scadenza) passa alla parola del feed: il progresso non si perde.
        val seed = statoPrima.parole
            .filter { it.tags.split(",").contains("Wortliste") }
            .associateBy { lemma(it) }
        val idPrima = statoPrima.parole.mapTo(HashSet()) { it.id }
        val segnapostoDaTogliere = mutableSetOf<String>()
        val reviewsFinali = esito.reviews.toMutableMap()
        for (nuova in esito.parole) {
            if (nuova.id in idPrima) continue
            val segnaposto = seed[lemma(nuova)] ?: continue
            if (segnaposto.id == nuova.id) continue
            statoPrima.reviews[segnaposto.id]?.let { carta ->
                reviewsFinali[nuova.id] = carta.copy(wordId = nuova.id)
            }
            segnapostoDaTogliere += segnaposto.id
        }

        val lezioni = feed.lessons.map {
            LessonEntity(it.numero, it.titolo, it.contenuto, it.audioUrl)
        }
        val guida = feed.guida?.let { g ->
            GuidaEntity(
                titolo = g.titolo,
                docUrl = g.docUrl,
                sezioni = g.sezioni.map { SezioneEntity(it.titolo, it.testo) }
            )
        }
        val piano = feed.piano?.let { p ->
            PianoEntity(
                obiettivo = p.obiettivo,
                orizzonte = p.orizzonte,
                tappe = p.tappe.map { TappaEntity(it.nome, it.descrizione, it.lezioni, it.stato) },
                certificazioni = p.certificazioni.map {
                    CertificazioneEntity(it.nome, it.ente, it.livello, it.note, it.url)
                },
                risorse = p.risorse.map { RisorsaEntity(it.nome, it.tipo, it.nota) }
            )
        }
        val progressoFeed = feed.progresso?.let { pr ->
            ProgressoFeedEntity(
                lezioneCorrente = pr.lezioneCorrente,
                streakCorrente = pr.streakCorrente,
                streakRecord = pr.streakRecord,
                totaleFatte = pr.totaleFatte,
                paroleTotali = pr.paroleTotali,
                paroleMature = pr.paroleMature
            )
        }
        val sessioni = feed.sessioni.map {
            SessioneEntity(it.tipo, it.titolo, it.descrizione, it.durata)
        }
        val contenuto = contenutoLezione(statoPrima, lezioni)
        val log = FeedLogEntity(
            syncedAt = now,
            newWords = esito.aggiunte,
            updatedWords = esito.aggiornate,
            status = "ok",
            message = "${feed.words.size} parole · guida ${feed.guida?.sezioni?.size ?: 0} sez."
        )

        aggiornaStatoDi(idProfilo) { st ->
            st.copy(
                parole = esito.parole.filterNot { it.id in segnapostoDaTogliere },
                reviews = reviewsFinali - segnapostoDaTogliere,
                guida = guida ?: st.guida,
                piano = piano ?: st.piano,
                progressoFeed = progressoFeed ?: st.progressoFeed,
                sessioni = sessioni,
                feedLog = (st.feedLog + log).takeLast(30),
                lezioneContenuto = contenuto,
                etag = risposta.etag ?: st.etag,
                lastSync = now
            )
        }

        return EsitoRete(
            true,
            "Feed sincronizzato: ${esito.aggiunte} nuove, ${esito.aggiornate} aggiornate"
        )
    }

    /** Verifica token e repo senza toccare i dati. */
    fun provaConnessioneCloud(): EsitoRete {
        val token = preferenze.githubToken.trim()
        if (token.isBlank()) return EsitoRete(false, "Inserisci prima il token GitHub")
        return try {
            val client = BackupGithub(token, repoNormalizzato(token))
            when (val esito = client.verificaAccesso()) {
                is BackupCloud.Esito.Ok -> EsitoRete(true, esito.dettaglio)
                is BackupCloud.Esito.Errore -> EsitoRete(false, esito.messaggio)
            }
        } catch (e: Exception) {
            EsitoRete(false, e.message ?: "Errore sconosciuto")
        }
    }

    /**
     * Carica tutti i profili sul repo privato (stesso file dell'app Android).
     *
     * Prima di caricare SCARICA il backup e lo unisce ai dati locali con
     * [BackupMerge]: se il telefono ha ripassato parole o avanzato nella
     * grammatica, il salvataggio dal desktop non deve cancellare quei progressi.
     * Se il cloud esiste ma non è leggibile ci si ferma: sovrascriverlo alla
     * cieca sarebbe una perdita di dati.
     */
    fun sincronizzaCloud(forza: Boolean = true): EsitoRete {
        val token = preferenze.githubToken.trim()
        if (token.isBlank()) return EsitoRete(false, "Inserisci prima il token GitHub")
        if (sincronizzazioneCloudInCorso) return EsitoRete(false, "Sincronizzazione gia' in corso")
        sincronizzazioneCloudInCorso = true
        return try {
            val client = BackupGithub(
                token = token,
                repo = repoNormalizzato(token),
                nomeFile = BackupCloud.nomeFilePerProfilo(repo.profiloAttivoId)
            )
            val remoto = client.scarica()
            val unito = if (remoto == null) {
                repo
            } else {
                val pacchetto = try {
                    BackupCloud.deserializza(remoto.json)
                } catch (e: Exception) {
                    return EsitoRete(
                        false,
                        "Backup cloud non leggibile (${e.message}). " +
                            "Sincronizzazione annullata per non sovrascriverlo."
                    )
                }
                BackupMerge.fondi(repo, pacchetto.dati)
            }
            if (unito != repo) {
                repo = unito
                salva()
            }
            val hash = BackupCloud.impronta(repo, Versione.nome)
            if (!forza && hash == preferenze.ultimoHashCloud) {
                preferenze = preferenze.copy(ultimaSincronizzazioneCloud = System.currentTimeMillis())
                salvaPreferenze()
                return EsitoRete(true, "Gia' sincronizzato")
            }
            val testo = BackupCloud.serializza(
                repo = repo,
                versioneApp = Versione.nome,
                now = System.currentTimeMillis()
            )
            client.carica(testo, "Backup Tedesco (desktop)")
            preferenze = preferenze.copy(
                ultimoBackup = System.currentTimeMillis(),
                ultimaSincronizzazioneCloud = System.currentTimeMillis(),
                ultimoHashCloud = hash
            )
            salvaPreferenze()
            EsitoRete(true, "Sincronizzato: ${repo.profili.size} profili")
        } catch (e: Exception) {
            EsitoRete(false, e.message ?: "Errore sconosciuto")
        } finally {
            sincronizzazioneCloudInCorso = false
        }
    }

    /** Pulsante manuale: forza la sincronizzazione completa. */
    fun salvaSulCloud(): EsitoRete = sincronizzaCloud(forza = true)

    /**
     * Sincronizzazione automatica, silenziosa: pensata per il timer di avvio
     * e per quello periodico. Non mostra errori: se il token manca o la rete
     * non c'e', non succede nulla.
     */
    fun sincronizzaCloudAutomatica(): Boolean {
        if (preferenze.githubToken.isBlank()) return false
        return sincronizzaCloud(forza = false).ok
    }

    val cloudConfigurato: Boolean get() = preferenze.githubToken.isNotBlank()

    /**
     * Scarica il backup e ne restituisce il riepilogo: NON tocca i dati finche'
     * l'utente non conferma con [applicaBackupCloud].
     */
    fun scaricaBackupCloud(): RisultatoCloud {
        val token = preferenze.githubToken.trim()
        if (token.isBlank()) return RisultatoCloud.Errore("Inserisci prima il token GitHub")
        return try {
            val client = BackupGithub(
                token = token,
                repo = repoNormalizzato(token),
                nomeFile = BackupCloud.nomeFilePerProfilo(repo.profiloAttivoId)
            )
            val contenuto = client.scarica()
                ?: return RisultatoCloud.Errore("Nessun backup trovato su GitHub")
            val pacchetto = BackupCloud.deserializza(contenuto.json)
            RisultatoCloud.Trovato(BackupCloud.riepilogo(pacchetto), contenuto.json)
        } catch (e: Exception) {
            RisultatoCloud.Errore(e.message ?: "Errore sconosciuto")
        }
    }

    /**
     * Applica il backup scaricato UNENDOLO ai dati locali.
     *
     * Non è più una sovrascrittura: lessico, ripassi, padronanza grammaticale e
     * progressi delle lezioni vengono confrontati voce per voce e si tiene la
     * versione più avanzata. Così ripristinare su un dispositivo non cancella
     * ciò che su quel dispositivo era stato fatto nel frattempo.
     */
    fun applicaBackupCloud(contenuto: String): EsitoRete {
        return try {
            val pacchetto = BackupCloud.deserializza(contenuto)
            val locale = ProfiliPreset.riconcilia(repo)
            val unitoCloud = ProfiliPreset.riconcilia(pacchetto.dati)
            if (unitoCloud.profili.isEmpty()) {
                return EsitoRete(false, "Il backup non contiene profili")
            }
            val unito = BackupMerge.fondi(locale, unitoCloud)
            val attivo = repo.profiloAttivoId
                ?.takeIf { unito.profili.containsKey(it) }
                ?: unito.profili.keys.first()
            repo = unito.copy(profiloAttivoId = attivo)
            preferenze = preferenze.copy(profiloAttivoId = attivo)
            salva()
            val unita = unito.profili.values.sumOf { it.stato.padronanzaGrammatica.size }
            val lezioni = unito.profili.values.sumOf { it.stato.progressiLezioni.size }
            EsitoRete(
                true,
                "Ripristino completato: ${unito.profili.size} profili, " +
                    "${unito.profili.values.sumOf { it.stato.parole.size }} parole, " +
                    "$unita unità di grammatica, $lezioni lezioni (progressi uniti)"
            )
        } catch (e: Exception) {
            EsitoRete(false, e.message ?: "Errore sconosciuto")
        }
    }

    private fun erroreFeed(idProfilo: String, motivo: String): EsitoRete {
        val log = FeedLogEntity(
            syncedAt = System.currentTimeMillis(),
            newWords = 0,
            updatedWords = 0,
            status = "errore",
            message = motivo.take(80)
        )
        aggiornaStatoDi(idProfilo) { it.copy(feedLog = (it.feedLog + log).takeLast(30)) }
        return EsitoRete(false, motivo)
    }

    private fun aggiornaStatoDi(idProfilo: String, trasforma: (ProfiloStato) -> ProfiloStato) {
        val p = repo.profili[idProfilo] ?: return
        repo = repo.copy(profili = repo.profili + (idProfilo to p.copy(stato = trasforma(p.stato))))
        salva()
    }

    /** Se l'utente scrive solo "tedesco-backup" aggiunge l'owner del token. */
    private fun repoNormalizzato(token: String): String {
        val r = preferenze.githubRepo.trim().trimEnd('/')
        if (r.isBlank()) return BackupCloud.REPO_DEFAULT
        if (r.contains("/")) return r
        val owner = runCatching {
            BackupGithub(token, BackupCloud.REPO_DEFAULT).ownerDelToken()
        }.getOrNull()
        return if (owner.isNullOrBlank()) r else "$owner/$r"
    }

    private fun contenutoLezione(st: ProfiloStato, lezioni: List<LessonEntity>): String {
        val numero = st.progresso.lezioneCorrente
        val generata = st.lezioniGenerate[numero.toString()]
        if (!generata.isNullOrBlank()) return generata
        if (lezioni.isEmpty()) return st.lezioneContenuto
        val lezione = lezioni.firstOrNull { it.numero == numero } ?: lezioni.first()
        return lezione.contenuto
    }

    // ---------------------------------------------------------------- interni

    private fun registraStudio(st: ProfiloStato, now: Long): ProfiloStato {
        val oggi = LocalDate.now().toString()
        if (st.ultimoGiornoStudio == oggi) {
            return st.copy(progresso = st.progresso.copy(ultimaAttivita = now))
        }
        val ieri = LocalDate.now().minusDays(1).toString()
        val nuovoStreak = if (st.ultimoGiornoStudio == ieri) st.streakGiorni + 1 else 1
        return st.copy(
            streakGiorni = nuovoStreak,
            streakRecord = max(st.streakRecord, nuovoStreak),
            ultimoGiornoStudio = oggi,
            progresso = st.progresso.copy(ultimaAttivita = now)
        )
    }

    private fun caricaProfili(): ProfiliRepository {
        val letto = runCatching {
            if (fileProfili.exists()) {
                json.decodeFromString(ProfiliRepository.serializer(), fileProfili.readText())
            } else {
                null
            }
        }.getOrNull()

        val base = letto?.let { ProfiliPreset.riconcilia(it) } ?: ProfiliPreset.riconcilia(
            ProfiliRepository(profili = ProfiliPreset.crea())
        )
        val conAttivo = if (base.profiloAttivoId == null) {
            base.copy(profiloAttivoId = preferenzeCaricate().profiloAttivoId ?: base.profili.keys.firstOrNull())
        } else base

        // Primo avvio: lessico iniziale dalla Wortliste, cosi' il ripasso e le
        // statistiche funzionano subito anche senza connessione. Sui salvataggi
        // successivi NON si ripopola: chi svuota il profilo deve restare vuoto.
        val primoAvvio = !fileProfili.exists()
        val inizializzato = if (primoAvvio) {
            conAttivo.copy(
                profili = conAttivo.profili.mapValues { (_, p) ->
                    if (p.stato.parole.isEmpty()) {
                        p.copy(stato = p.stato.copy(parole = paroleIniziali(p)))
                    } else p
                }
            )
        } else conAttivo

        if (primoAvvio) {
            cartella.mkdirs()
            fileProfili.writeText(json.encodeToString(inizializzato))
        }
        return inizializzato
    }

    private fun caricaPreferenze(): Preferenze = preferenzeCaricate()

    private fun preferenzeCaricate(): Preferenze = runCatching {
        if (filePreferenze.exists()) {
            json.decodeFromString(Preferenze.serializer(), filePreferenze.readText())
        } else Preferenze()
    }.getOrDefault(Preferenze())

    private fun salva() {
        runCatching {
            cartella.mkdirs()
            fileProfili.writeText(json.encodeToString(repo))
            salvaPreferenze()
        }
    }

    private fun salvaPreferenze() {
        runCatching {
            cartella.mkdirs()
            filePreferenze.writeText(json.encodeToString(preferenze))
        }
    }

    companion object {
        /**
         * Lemma normalizzato (senza articolo) per riconoscere la stessa parola
         * fra Wortliste e feed. Si confronta solo il tedesco: la traduzione
         * italiana del corso puo' differire da quella della Wortliste
         * ("la mamma" vs "madre") senza che la parola sia un'altra.
         */
        private fun lemma(w: WordEntity): String =
            w.german.trim().lowercase()
                .removePrefix("der ").removePrefix("die ").removePrefix("das ")
                .trim()

        /** Lemma di una voce della Wortliste (che include l'articolo). */
        private fun lemmaVoce(voce: WortlisteB1.Voce): String =
            voce.tedesco.trim().lowercase()
                .removePrefix("der ").removePrefix("die ").removePrefix("das ")
                .trim()

        private fun daVoceWortliste(voce: WortlisteB1.Voce, now: Long): WordEntity {
            val articolo = listOf("der ", "die ", "das ").firstOrNull { voce.tedesco.startsWith(it) }
            val parola = articolo?.let { voce.tedesco.removePrefix(it) } ?: voce.tedesco
            val id = "wort-" + voce.tedesco.lowercase()
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
            return WordEntity(
                id = id,
                german = parola,
                italian = voce.italiano,
                example = voce.esempio,
                article = articolo?.trim(),
                level = voce.livello,
                tags = "Wortliste",
                source = WordSource.SYNCED,
                createdAt = now
            )
        }

        fun paroleWortliste(): List<WordEntity> {
            val now = System.currentTimeMillis()
            return WortlisteB1.tutte.distinctBy { it.tedesco.lowercase() }
                .map { daVoceWortliste(it, now) }
        }

        /**
         * Lessico di partenza per un profilo nuovo: solo le parole del livello
         * giusto, cosi' la Wortliste resta una fonte da cui aggiungere (non un
         * elenco gia' tutto importato) e il ripasso non annega un principiante.
         */
        fun paroleIniziali(p: ProfiloUtente): List<WordEntity> {
            val livelli = when (p.config.livelloIniziale) {
                LivelloIniziale.A0, LivelloIniziale.A1 -> setOf("A1")
                LivelloIniziale.A2 -> setOf("A1", "A2")
                LivelloIniziale.B1 -> setOf("A2", "B1")
                LivelloIniziale.B2 -> setOf("B1")
            }
            return paroleWortliste().filter { it.level in livelli }
        }
    }
}

/** Etichetta con articolo, come nelle flashcard. */
fun WordEntity.etichetta(): String = article?.takeIf { it.isNotBlank() }?.let { "$it $german" } ?: german

/** Conteggio delle unita' del percorso per stato. */
fun List<UnitaConStato>.completate(): Int = count { it.stato == StatoUnita.COMPLETATA }
