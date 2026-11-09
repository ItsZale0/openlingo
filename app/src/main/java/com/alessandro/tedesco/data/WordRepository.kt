package com.alessandro.tedesco.data

import com.alessandro.tedesco.i18n.t

import android.content.Context
import com.alessandro.tedesco.data.local.FeedLogEntity
import com.alessandro.tedesco.data.local.GuidaEntity
import com.alessandro.tedesco.data.local.LessonEntity
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.SezioneEntity
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.local.WordSource
import com.alessandro.tedesco.data.remote.FeedDto
import com.alessandro.tedesco.data.remote.FeedService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.comparisons.compareBy
import com.alessandro.tedesco.data.local.PianoEntity
import com.alessandro.tedesco.data.local.ProgressoFeedEntity
import com.alessandro.tedesco.data.local.SessioneEntity
import com.alessandro.tedesco.data.local.TappaEntity
import com.alessandro.tedesco.data.local.CertificazioneEntity
import com.alessandro.tedesco.data.local.RisorsaEntity

sealed class SyncResult {
    data object NotModified : SyncResult()
    data class Updated(val newWords: Int, val updatedWords: Int, val total: Int) : SyncResult()
    data class Failed(val message: String) : SyncResult()
}

/**
 * Repository delle parole del corso.
 *
 * I dati (parole, ripassi, guida) sono letti e salvati nello stato del
 * profilo attivo: ogni profilo ha il proprio vocabolario e i propri progressi.
 * Quando l'utente cambia profilo, il repository ricarica automaticamente.
 */
class WordRepository(
    context: Context,
    private val service: FeedService,
    private val json: Json,
    private val io: CoroutineDispatcher,
    private val profileManager: ProfileManager
) {

    private val _words = MutableStateFlow<List<WordEntity>>(emptyList())
    private val _reviews = MutableStateFlow<Map<String, ReviewEntity>>(emptyMap())
    private val _feedLog = MutableStateFlow<List<FeedLogEntity>>(emptyList())
    private val _guida = MutableStateFlow<GuidaEntity?>(null)
    private val _lezioneContenuto = MutableStateFlow("")
    private val _lezioni = MutableStateFlow<List<LessonEntity>>(emptyList())
    private val _piano = MutableStateFlow<PianoEntity?>(null)
    private val _progressoFeed = MutableStateFlow<ProgressoFeedEntity?>(null)
    private val _sessioni = MutableStateFlow<List<SessioneEntity>>(emptyList())
    private val _ripassiLog = MutableStateFlow<List<RipassoLogEntity>>(emptyList())

    val wordsFlow: Flow<List<WordEntity>> = _words
    val reviewsFlow: Flow<Map<String, ReviewEntity>> = _reviews
    val ripassiLogFlow: Flow<List<RipassoLogEntity>> = _ripassiLog
    val feedLogFlow: Flow<List<FeedLogEntity>> = _feedLog
    val guidaFlow: Flow<GuidaEntity?> = _guida
    val pianoFlow: Flow<PianoEntity?> = _piano
    val progressoFeedFlow: Flow<ProgressoFeedEntity?> = _progressoFeed
    val sessioniFlow: Flow<List<SessioneEntity>> = _sessioni

    init {
        // Carica subito lo stato del profilo attivo. Senza questa lettura
        // sincrona, un sync avviato appena creato il repository poteva partire
        // con piano/guida/sessioni a null e sovrascrivere quelli già salvati.
        caricaDaProfilo()

        // Ad ogni cambio di profilo (o al primo caricamento) ricarica i dati
        CoroutineScope(io).launch {
            var ultimoProfilo: String? = "__init__"
            profileManager.repositoryFlow.collect { repo ->
                val id = repo.profiloAttivoId
                if (id != ultimoProfilo) {
                    ultimoProfilo = id
                    caricaDaProfilo()
                }
            }
        }
    }

    private fun caricaDaProfilo() {
        val stato = profileManager.statoAttivo()
        _words.value = stato.parole
        _reviews.value = stato.reviews
        _feedLog.value = stato.feedLog
        _guida.value = stato.guida
        _piano.value = stato.piano
        _progressoFeed.value = stato.progressoFeed
        _sessioni.value = stato.sessioni
        _ripassiLog.value = stato.ripassiLog
        // Contenuto lezione e feed lezioni: appartengono al profilo. Senza il reset
        // il nuovo profilo vedrebbe la lezione e l'elenco delle lezioni del profilo
        // precedente (Emma vedeva i contenuti di Alessandro).
        _lezioneContenuto.value = ""
        _lezioni.value = emptyList()
    }

    /** Azzera i contenuti dipendenti dal profilo (lezione corrente e feed lezioni). */
    fun resetContenutiProfilo() {
        _lezioneContenuto.value = ""
        _lezioni.value = emptyList()
    }

    /**
     * Salva sul profilo i campi posseduti da questo repository.
     *
     * USA .copy() — NON ricostruire un ProfiloStato da zero: il costruttore
     * azzera ogni campo non elencato, e questo metodo viene chiamato a ogni
     * risposta di ripasso, parola aggiunta o sync fallito. Costruirlo da zero
     * cancellava conversazioni del tutor, correzioni, esercizi generati,
     * lezioni generate e le streak ("la chat non ha memoria", "la streak e' 0").
     */
    private suspend fun salvaSuProfilo(immediato: Boolean = false) {
        val corrente = profileManager.statoAttivo()
        profileManager.aggiornaStatoAttivo(
            corrente.copy(
                parole = _words.value,
                reviews = _reviews.value,
                feedLog = _feedLog.value,
                guida = _guida.value,
                piano = _piano.value,
                progressoFeed = _progressoFeed.value,
                sessioni = _sessioni.value,
                ripassiLog = _ripassiLog.value,
                lastSync = System.currentTimeMillis()
            ),
            immediato = immediato
        )
    }

    fun observeGuida(): Flow<GuidaEntity?> = guidaFlow
    fun observeLezioneContenuto(): Flow<String> = _lezioneContenuto
    fun observeLezioni(): Flow<List<LessonEntity>> = _lezioni

    /** Aggiorna il contenuto mostrato quando l'utente cambia lezione. */
    fun aggiornaLezioneContenuto(numero: Int) {
        // La lezione GENERATA ha priorita' su quella del feed: il feed puo' essere
        // vuoto (profilo Emma) o avere buchi (lezioni 1,2,3,5 - mancava la 4).
        val generata = profileManager.statoAttivo().lezioniGenerate[numero.toString()]
        if (!generata.isNullOrBlank()) {
            _lezioneContenuto.value = generata
            return
        }
        val lezione = _lezioni.value.firstOrNull { it.numero == numero }
            ?: _lezioni.value.firstOrNull()
        _lezioneContenuto.value = lezione?.contenuto.orEmpty()
    }

    /** Salva una lezione generata dall'AI cosi' sopravvive ai sync del feed. */
    suspend fun salvaLezioneGenerata(numero: Int, contenuto: String) = withContext(io) {
        val profilo = profileManager.profiloAttivo() ?: return@withContext
        val mappa = profilo.stato.lezioniGenerate.toMutableMap()
        mappa[numero.toString()] = contenuto
        profileManager.aggiornaStatoAttivo(
            profilo.stato.copy(lezioneContenuto = contenuto, lezioniGenerate = mappa),
            immediato = true
        )
        _lezioneContenuto.value = contenuto
    }

    /**
     * Aggiunge le parole di una lezione generata al vocabolario del profilo.
     *
     * NON passa da [addCustomWord]: quelle sono parole dell'utente e dipendono dal
     * flag enableCustomWords. Le parole di una lezione sono contenuto del corso,
     * quindi entrano sempre (altrimenti la lezione "non aggiorna il lessico").
     *
     * @return quante parole sono state effettivamente aggiunte
     */
    suspend fun aggiungiParoleLezione(
        parole: List<com.alessandro.tedesco.data.remote.ParolaLezione>,
        numero: Int,
        livello: String
    ): Int = withContext(io) {
        if (parole.isEmpty()) return@withContext 0
        val now = System.currentTimeMillis()
        val esistenti = _words.value.map { it.german.trim().lowercase() }.toSet()
        val nuove = parole
            .filter { it.tedesco.trim().lowercase() !in esistenti }
            .mapIndexed { i, p ->
                WordEntity(
                    id = "lezione_${numero}_${now}_${i}_${p.tedesco.hashCode()}",
                    german = p.tedesco.trim(),
                    italian = p.italiano.trim(),
                    example = p.esempio.trim(),
                    article = p.articolo?.trim()?.ifBlank { null },
                    pronunciation = null,
                    level = livello,
                    lesson = numero,
                    tags = "lezione",
                    archived = false,
                    createdAt = now + i,
                    source = WordSource.CUSTOM
                )
            }
        if (nuove.isEmpty()) return@withContext 0

        _words.value = _words.value + nuove

        val reviews = _reviews.value.toMutableMap()
        nuove.forEach { w ->
            reviews[w.id] = ReviewEntity(
                wordId = w.id,
                easeFactor = 2.5f,
                intervalDays = 0,
                repetitions = 0,
                lapses = 0,
                dueAt = now,
                lastReviewedAt = null
            )
        }
        _reviews.value = reviews
        salvaSuProfilo()
        nuove.size
    }

    /**
     * Aggiunge al lessico del profilo un pacchetto di parole curate.
     *
     * È il percorso della sezione Lessico: qui il livello è quello della voce
     * (A1, A2, B1, B2), non quello del profilo, altrimenti un A1 che studia
     * parole A2 se le ritroverebbe tutte segnate "A1".
     *
     * Ogni parola nuova riceve una carta SRS in scadenza subito: entra nel
     * ripasso e negli esercizi senza doverle aggiungere una per una.
     * Il confronto è per lemma **e categoria** ([LessicoLivelli.chiaveParola]):
     * "die Wohnung" e "Wohnung" sono la stessa parola, "der Morgen" e "morgen" no.
     *
     * @return quante parole sono state effettivamente aggiunte
     */
    suspend fun aggiungiParoleCurate(voci: List<LessicoLivelli.Voce>): Int = withContext(io) {
        if (voci.isEmpty()) return@withContext 0
        val now = System.currentTimeMillis()
        val esistenti = _words.value.mapTo(HashSet()) { LessicoLivelli.chiaveParola(it.german, it.article) }
        val nuove = voci
            .distinctBy { LessicoLivelli.chiaveParola(it.tedesco) }
            .filter { LessicoLivelli.chiaveParola(it.tedesco) !in esistenti }
            .mapIndexed { i, v ->
                val chiave = LessicoLivelli.chiaveParola(v.tedesco)
                val senzaArticolo = WortlisteB1.parolaSenzaArticolo(v.tedesco).trim()
                WordEntity(
                    // La categoria entra nell'id: "der Morgen" (mattina) e
                    // "morgen" (domani) hanno lo stesso lemma ma sono due id diversi.
                    id = "lessico_${v.livello.lowercase()}_${chiave.replaceFirst(':', '_')}",
                    german = senzaArticolo.ifBlank { v.tedesco.trim() },
                    italian = v.italiano.trim(),
                    example = v.esempio.trim(),
                    article = LessicoLivelli.articoloDi(v.tedesco),
                    pronunciation = null,
                    level = v.livello,
                    lesson = 0,
                    tags = if (v.tema.isBlank()) "lessico" else "lessico,${v.tema}",
                    archived = false,
                    createdAt = now + i,
                    source = WordSource.CUSTOM
                )
            }
        if (nuove.isEmpty()) return@withContext 0

        _words.value = _words.value + nuove

        val reviews = _reviews.value.toMutableMap()
        nuove.forEach { w -> reviews[w.id] = ReviewEntity(wordId = w.id, dueAt = now) }
        _reviews.value = reviews
        salvaSuProfilo()
        nuove.size
    }

    fun observeWords() = wordsFlow
        // L'ordinamento di migliaia di parole e' lavoro CPU: va fatto sul
        // dispatcher di default, mai sul thread principale della UI.
        .map { it.filter { !it.archived }.sortedWith(compareBy({ it.lesson }, { it.german })) }
        .flowOn(Dispatchers.Default)

    fun observeWordsByLesson(lesson: Int) = wordsFlow
        .map { it.filter { !it.archived && it.lesson == lesson }.sortedBy { it.german } }

    fun observeLessons(): Flow<List<LessonEntity>> = _lezioni

    fun observeWordsByLevel(level: String): Flow<List<WordEntity>> = wordsFlow
        .map { it.filter { !it.archived && it.level == LivelloCEFR.daTesto(level).codiceBreve() }.sortedBy { it.german } }

    fun observeLevels(): Flow<List<String>> = wordsFlow
        .map { it.filter { !it.archived }.map { it.level }.distinct().sorted() }

    fun observeLastSync() = feedLogFlow.map { it.maxByOrNull { log -> log.syncedAt } }

    fun observeDueCount(): Flow<Int> = reviewsFlow.map { reviews ->
        val now = System.currentTimeMillis()
        reviews.values.count { it.dueAt <= now }
    }

    suspend fun sync(): SyncResult = withContext(io) {
        val url = profileManager.getFeedUrl()
        if (url.isBlank()) {
            return@withContext SyncResult.Failed(t("URL del feed non configurato"))
        }

        try {
            val etag = profileManager.statoAttivo().etag
            val response = service.getRaw(url)

            if (response.code == 304) return@withContext SyncResult.NotModified
            if (!response.isSuccessful) {
                return@withContext SyncResult.Failed("HTTP ${response.code}")
            }

            val newEtag = response.headers["ETag"]
            val body = response.body?.string()
            if (body.isNullOrBlank()) {
                return@withContext SyncResult.Failed(t("Risposta vuota dal server"))
            }

            val feed = json.decodeFromString(FeedDto.serializer(), body)

            if (etag.isNotBlank() && newEtag != null && etag == newEtag && feed.words.isEmpty()) {
                return@withContext SyncResult.NotModified
            }

            val (added, changed) = merge(feed)

            _lezioni.value = feed.lessons.map { LessonEntity(it.numero, it.titolo, it.contenuto, it.audioUrl) }
            val lezioneCorrente = profileManager.statoAttivo().progresso.lezioneCorrente
            // Non assegnare il contenuto direttamente: aggiornaLezioneContenuto
            // preferisce la versione GENERATA e non la fa sparire al sync.
            aggiornaLezioneContenuto(lezioneCorrente)
            feed.guida?.let { g ->
                _guida.value = GuidaEntity(
                    titolo = g.titolo,
                    docUrl = g.docUrl,
                    sezioni = g.sezioni.map { SezioneEntity(it.titolo, it.testo) }
                )
            }
            feed.piano?.let { p ->
                _piano.value = PianoEntity(
                    obiettivo = p.obiettivo,
                    orizzonte = p.orizzonte,
                    tappe = p.tappe.map { TappaEntity(it.nome, it.descrizione, it.lezioni, it.stato) },
                    certificazioni = p.certificazioni.map {
                        CertificazioneEntity(it.nome, it.ente, it.livello, it.note, it.url)
                    },
                    risorse = p.risorse.map { RisorsaEntity(it.nome, it.tipo, it.nota) }
                )
            }
            feed.progresso?.let { pr ->
                _progressoFeed.value = ProgressoFeedEntity(
                    lezioneCorrente = pr.lezioneCorrente,
                    streakCorrente = pr.streakCorrente,
                    streakRecord = pr.streakRecord,
                    totaleFatte = pr.totaleFatte,
                    paroleTotali = pr.paroleTotali,
                    paroleMature = pr.paroleMature
                )
            }
            _sessioni.value = feed.sessioni.map {
                SessioneEntity(it.tipo, it.titolo, it.descrizione, it.durata)
            }

            val logEntry = FeedLogEntity(
                syncedAt = System.currentTimeMillis(),
                newWords = added,
                updatedWords = changed,
                status = "ok",
                message = "${feed.words.size} parole · guida ${feed.guida?.sezioni?.size ?: 0} sez."
            )
            _feedLog.value = (_feedLog.value + logEntry).takeLast(30)

            salvaSuProfiloConEtag(newEtag)   // sync: raro e importante, scrive subito

            SyncResult.Updated(added, changed, feed.words.size)
        } catch (e: Exception) {
            val logEntry = FeedLogEntity(
                syncedAt = System.currentTimeMillis(),
                newWords = 0,
                updatedWords = 0,
                status = "errore",
                message = e.message?.take(80)
            )
            _feedLog.value = (_feedLog.value + logEntry).takeLast(30)
            salvaSuProfilo()
            SyncResult.Failed(e.message ?: "Errore sconosciuto")
        }
    }

    private suspend fun salvaSuProfiloConEtag(nuovoEtag: String?) {
        val corrente = profileManager.statoAttivo()
        profileManager.aggiornaStatoAttivo(
            corrente.copy(
                parole = _words.value,
                reviews = _reviews.value,
                feedLog = _feedLog.value,
                guida = _guida.value,
                piano = _piano.value,
                progressoFeed = _progressoFeed.value,
                sessioni = _sessioni.value,
                etag = nuovoEtag ?: corrente.etag,
                lastSync = System.currentTimeMillis()
            ),
            immediato = true   // sync del feed: raro, ma deve essere subito su disco
        )
    }

    private suspend fun merge(feed: FeedDto): Pair<Int, Int> = withContext(io) {
        // Stessa identica fusione usata dalla versione desktop (FeedMerge, nel
        // modulo :core): una sola regola per il vocabolario del feed, cosi' i
        // due programmi non possono divergere.
        val esito = FeedMerge.applica(
            parole = _words.value,
            reviews = _reviews.value,
            feed = feed,
            now = System.currentTimeMillis()
        )
        if (esito.aggiunte > 0 || esito.aggiornate > 0) {
            _words.value = esito.parole
        }
        _reviews.value = esito.reviews
        esito.aggiunte to esito.aggiornate
    }

    suspend fun setArchived(id: String, archived: Boolean) = withContext(io) {
        val currentWords = _words.value.associateBy { it.id }.toMutableMap()
        currentWords[id]?.let { word ->
            currentWords[id] = word.copy(archived = archived)
            _words.value = currentWords.values.toList()
            salvaSuProfilo()
        }
    }

    /**
     * Parole in scadenza, al massimo [limite] per sessione.
     * Un A0 con 100 parole nuove avrebbe 100 carte in scadenza il primo giorno:
     * una sessione così non si finisce mai. Meglio poche carte al giorno.
     */
    suspend fun dueReviews(limite: Int = 20): List<ReviewEntity> = withContext(io) {
        val now = System.currentTimeMillis()
        _reviews.value.values
            .filter { it.dueAt <= now }
            .sortedBy { it.dueAt }
            .take(limite)
    }

    suspend fun answer(wordId: String, knewIt: Boolean) = withContext(io) {
        val reviews = _reviews.value.toMutableMap()
        val r = reviews[wordId] ?: return@withContext

        val updated = if (knewIt) {
            val next = INTERVALS.getOrElse(r.repetitions) { INTERVALS.last() }
            r.copy(
                repetitions = r.repetitions + 1,
                intervalDays = next,
                easeFactor = (r.easeFactor + 0.05f).coerceAtMost(3.0f),
                dueAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(next.toLong()),
                lastReviewedAt = System.currentTimeMillis()
            )
        } else {
            r.copy(
                repetitions = 0,
                intervalDays = 1,
                lapses = r.lapses + 1,
                easeFactor = max(1.3f, r.easeFactor - 0.2f),
                dueAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                lastReviewedAt = System.currentTimeMillis()
            )
        }
        reviews[wordId] = updated
        _reviews.value = reviews
        registraRipasso(wordId, knewIt)
        salvaSuProfilo()
    }

    /**
     * Registra l'esito di una VERIFICA oggettiva (quiz a scelta multipla).
     * E' l'evidenza forte: aggiorna anche la SRS come un ripasso riuscito/fallito,
     * cosi' le parole verificate non tornano subito nel ripasso.
     */
    suspend fun registraVerifica(wordId: String, corretta: Boolean) = withContext(io) {
        val reviews = _reviews.value.toMutableMap()
        val r = reviews[wordId] ?: ReviewEntity(wordId = wordId)
        reviews[wordId] = if (corretta) {
            val next = INTERVALS.getOrElse(r.repetitions) { INTERVALS.last() }
            r.copy(
                repetitions = r.repetitions + 1,
                intervalDays = next,
                easeFactor = (r.easeFactor + 0.05f).coerceAtMost(3.0f),
                dueAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(next.toLong()),
                lastReviewedAt = System.currentTimeMillis()
            )
        } else {
            r.copy(
                repetitions = 0,
                intervalDays = 1,
                lapses = r.lapses + 1,
                easeFactor = max(1.3f, r.easeFactor - 0.2f),
                dueAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                lastReviewedAt = System.currentTimeMillis()
            )
        }
        _reviews.value = reviews
        registraRipasso(wordId, corretta, verifica = true)
        salvaSuProfilo()
    }

    /** Registra un evento di ripasso reale (per statistiche accurate nel tempo). */
    private fun registraRipasso(wordId: String, corretta: Boolean, verifica: Boolean = false) {
        // Cap a 5000 eventi (~ultimi mesi) per non gonfiare il DataStore.
        _ripassiLog.value = (_ripassiLog.value + RipassoLogEntity(wordId, System.currentTimeMillis(), corretta, verifica)).takeLast(5000)
    }

    /** Svuota i dati del profilo attivo (parole, ripassi, log, guida). */
    /** Segna le parole specificate come studiate (conosciute), creando la review se manca. */
    suspend fun segnaParoleStudiate(wordIds: List<String>) = withContext(io) {
        if (wordIds.isEmpty()) return@withContext
        val reviews = _reviews.value.toMutableMap()
        for (wordId in wordIds) {
            val r = reviews[wordId] ?: ReviewEntity(wordId = wordId)
            val next = INTERVALS.getOrElse(r.repetitions) { INTERVALS.last() }
            reviews[wordId] = r.copy(
                repetitions = r.repetitions + 1,
                intervalDays = next,
                easeFactor = (r.easeFactor + 0.05f).coerceAtMost(3.0f),
                dueAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(next.toLong()),
                lastReviewedAt = System.currentTimeMillis()
            )
        }
        _reviews.value = reviews
        salvaSuProfilo()
    }

    suspend fun resetAll() = withContext(io) {
        _words.value = emptyList()
        _reviews.value = emptyMap()
        _feedLog.value = emptyList()
        _guida.value = null
        val corrente = profileManager.statoAttivo()
        profileManager.aggiornaStatoAttivo(
            corrente.copy(
                parole = emptyList(),
                reviews = emptyMap(),
                feedLog = emptyList(),
                guida = null,
                etag = ""
            ),
            immediato = true
        )
    }

    /** Aggiunge una parola personalizzata (solo se il profilo lo consente). */
    suspend fun addCustomWord(
        german: String,
        italian: String,
        example: String = "",
        article: String? = null,
        pronunciation: String? = null,
        lesson: Int = 0,
        tags: String = ""
    ): WordEntity = withContext(io) {
        if (!profileManager.isCustomWordsEnabled()) {
            throw UnsupportedOperationException(t("Parole personalizzate non abilitate per questo profilo"))
        }
        val now = System.currentTimeMillis()
        // Il livello della parola e' quello reale del profilo (Emma parte da B1,
        // Alessandro da A0): prima era sempre "A1" e falsava le statistiche.
        val livelloProfilo = profileManager.profiloAttivo()
            ?.stato?.progresso?.livelloCorrente?.codiceBreve()
            ?: "A1"
        val word = WordEntity(
            id = "custom_${now}_${german.hashCode()}",
            german = german.trim(),
            italian = italian.trim(),
            example = example.trim(),
            article = article?.trim()?.ifBlank { null },
            pronunciation = pronunciation?.trim()?.ifBlank { null },
            level = livelloProfilo,
            lesson = lesson,
            tags = tags.trim(),
            archived = false,
            createdAt = now,
            source = WordSource.CUSTOM
        )
        val currentWords = _words.value.toMutableList()
        currentWords.removeAll { it.german.equals(word.german, ignoreCase = true) }
        currentWords.add(0, word)
        _words.value = currentWords

        val currentReviews = _reviews.value.toMutableMap()
        currentReviews[word.id] = ReviewEntity(
            wordId = word.id,
            easeFactor = 2.5f,
            intervalDays = 0,
            repetitions = 0,
            lapses = 0,
            dueAt = now,
            lastReviewedAt = null
        )
        _reviews.value = currentReviews

        salvaSuProfilo()
        word
    }

    /** Elimina una parola personalizzata. */
    suspend fun deleteCustomWord(id: String) = withContext(io) {
        _words.value = _words.value.filterNot { it.id == id }
        _reviews.value = _reviews.value - id
        salvaSuProfilo()
    }

    companion object {
        val INTERVALS = listOf(1, 3, 7, 16, 35, 75, 150, 300)
    }
}
