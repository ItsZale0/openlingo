package com.alessandro.tedesco.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.LivelloIniziale
import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProfiloConfig
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ProviderAI
import com.alessandro.tedesco.data.local.TipoProfilo
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val PROFILI_DATASTORE = "profili_data"
private val Context.profiliDataStore by preferencesDataStore(name = PROFILI_DATASTORE)

private object ProfiliKeys {
    val PROFILI_JSON = stringPreferencesKey("profili_json")
}

/** Helper usati dai test d'integrazione per pilotare direttamente il DataStore dei profili. */
internal suspend fun Context.scriviProfiliJson(json: String) {
    profiliDataStore.edit { it[ProfiliKeys.PROFILI_JSON] = json }
}

internal suspend fun Context.profiliDataStorePulito() {
    // I manager dei test precedenti possono avere un salvataggio coalescente in
    // volo (400 ms). Se azzerassimo subito, quel write in ritardo ripopolerebbe
    // il DataStore e il test successivo vedrebbe un profilo attivo "fantasma".
    // Aspettiamo che la coda si svuoti, poi puliamo davvero.
    kotlinx.coroutines.delay(SALVATAGGIO_ATTESA_MS + 100)
    profiliDataStore.edit { it.clear() }
}

/**
 * Gestisce i profili utente.
 *
 * Alla prima apertura NON viene attivato alcun profilo: l'app mostra la
 * schermata di selezione e si entra nel corso solo dopo aver scelto.
 *
 * L'inizializzazione è serializzata da un [Mutex]: lettura da DataStore e
 * creazione dei preset avvengono in un'unica sequenza. Prima due coroutine
 * concorrenti (una che legge, una che crea i preset) si sovrascrivevano a
 * vicenda lasciando la lista profili vuota — la schermata appariva ma non
 * si poteva selezionare nulla.
 */
/** Attesa della coalescenza dei salvataggi (ms). */
private const val SALVATAGGIO_ATTESA_MS = 400L

class ProfileManager(
    private val context: Context,
    private val json: Json,
    private val io: CoroutineDispatcher
) {
    private val _repository = MutableStateFlow(ProfiliRepository())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val repositoryFlow: Flow<ProfiliRepository> = _repository

    /** true quando i profili sono stati caricati (o creati) e sono utilizzabili */
    private val _pronto = MutableStateFlow(false)
    val prontoFlow: Flow<Boolean> = _pronto

    private val mutex = Mutex()
    @Volatile
    private var inizializzato = false

    /**
     * Carica i profili da DataStore e riconcilia con i preset previsti.
     * Idempotente e sicuro se chiamata più volte o in concorrenza.
     */
    suspend fun inizializza(forza: Boolean = false) {
        mutex.withLock {
            if (inizializzato && !forza) return

            val letto = leggiDaDataStore()
            val repo = ProfiliPreset.riconcilia(letto)
            val riparato = repo != letto

            _repository.value = repo
            if (riparato) scriviSuDataStore(repo)

            inizializzato = true
            _pronto.value = true
        }
    }

    /** Ricarica i profili forzando la rilettura (pulsante "Riprova"). */
    suspend fun ricarica() {
        inizializza(forza = true)
    }

    private suspend fun leggiDaDataStore(): ProfiliRepository = withContext(io) {
        val data = context.profiliDataStore.data.first()
        val jsonStr = data[ProfiliKeys.PROFILI_JSON]
        if (jsonStr.isNullOrBlank()) {
            ProfiliRepository()
        } else {
            runCatching {
                json.decodeFromString(ProfiliRepository.serializer(), jsonStr)
            }.getOrElse {
                // Non perdere nulla: prima di ripartire da zero conserviamo il
                // JSON illeggibile in files/recupero/, così l'utente (o chi lo
                // assiste) può recuperarlo o allegarlo a una segnalazione.
                salvaCopiaRecupero(jsonStr)
                ProfiliRepository()
            }
        }
    }

    private fun salvaCopiaRecupero(jsonStr: String) {
        runCatching {
            val cartella = java.io.File(context.filesDir, "recupero")
            cartella.mkdirs()
            val nome = "profili_data_corrotto_${System.currentTimeMillis()}.json"
            java.io.File(cartella, nome).writeText(jsonStr)
        }
    }

    private suspend fun scriviSuDataStore(repo: ProfiliRepository) = withContext(io) {
        val jsonStr = json.encodeToString(ProfiliRepository.serializer(), repo)
        context.profiliDataStore.edit { it[ProfiliKeys.PROFILI_JSON] = jsonStr }
    }

    /**
     * Salvataggio COALESCENTE.
     *
     * Prima ogni modifica (ogni risposta data, ogni parola aggiunta) codificava in
     * JSON l'intero repository (~1,7 MB) e riscriveva il file DataStore: decine di
     * megabyte di allocazioni e I/O al minuto. Ora le modifiche ravvicinate si
     * fondono in UNA sola scrittura (400 ms dopo l'ultima).
     */
    private val daScrivere = MutableStateFlow(0L)

    init {
        scope.launch {
            daScrivere.collectLatest {
                // BUG CRITICO (persita dati ad ogni avvio): il flow StateFlow
                // emette subito il valore iniziale (0), e la vecchia versione
                // partiva comunque con delay+scrittura: 400 ms dopo la
                // costruzione sovrascriveva il DataStore con _repository.value,
                // che prima di inizializza() e' VUOTO. Se la lettura/decodifica
                // dei ~MB di JSON impiega piu' di 400 ms, i dati salvati venivano
                // distrutti: ogni riavvio ricreava i profili da zero.
                // Ora si scrive SOLO dopo un'aggiunta vera (it > 0) e solo dopo
                // che i dati sono stati effettivamente caricati.
                if (it > 0L && inizializzato) {
                    delay(SALVATAGGIO_ATTESA_MS)
                    if (inizializzato) scriviSuDataStore(_repository.value)
                }
            }
        }
    }

    /** Aggiornamento frequente (ogni risposta): scrive in modo coalescente. */
    private fun saveToDataStore() {
        daScrivere.value = daScrivere.value + 1
    }

    /**
     * Operazione strutturale (cambio profilo, creazione, uscita, ripristino):
     * rara e importante, si scrive SUBITO. Il coalescing serve solo a non
     * riscrivere 1,7 MB a ogni singola risposta.
     */
    private suspend fun scriviSubito() = scriviSuDataStore(_repository.value)

    /**
     * Scrive SUBITO, senza attendere la coalescenza: da chiamare quando l'app va
     * in background, cosi' non si perde l'ultima modifica.
     *
     * Se i dati non sono ancora stati caricati (inizializza() in corso oppure
     * non ancora partita), NON toccare il DataStore: il repository in memoria e'
     * vuoto e la sovrascrittura cancellerebbe i dati salvati — questo, con
     * onStop che arriva prima del caricamento, era l'altra via di perdita dati.
     */
    suspend fun flush() {
        if (!inizializzato) return
        scriviSuDataStore(_repository.value)
    }

    /** Tutti i profili disponibili, dal più vecchio al più recente. */
    fun profiliDisponibili(): List<ProfiloUtente> =
        _repository.value.profili.values.sortedBy { it.creatoIl }

    fun profiloAttivoId(): String? = _repository.value.profiloAttivoId

    fun profiloAttivo(): ProfiloUtente? {
        val id = _repository.value.profiloAttivoId ?: return null
        return _repository.value.profili[id]
    }

    /** Seleziona il profilo attivo. */
    suspend fun selezionaProfilo(profiloId: String) {
        val repo = _repository.value
        val esistente = repo.profili[profiloId]
            ?: throw IllegalArgumentException("Profilo non trovato: $profiloId")
        val aggiornato = esistente.copy(ultimoAccesso = System.currentTimeMillis())
        _repository.value = repo.copy(
            profili = repo.profili + (profiloId to aggiornato),
            profiloAttivoId = profiloId
        )
        scriviSubito()
    }

    /**
     * Crea un nuovo profilo e lo attiva subito.
     *
     * @param nome nome mostrato
     * @param livello livello CEFR di partenza
     * @param contesto descrizione personale per l'AI (professione, interessi, obiettivi):
     *   è l'unica personalizzazione richiesta, anche per il lessico specialistico
     * @param mostraContestoMedico mantenuto per compatibilità con i profili storici
     * @param providerAI provider AI scelto (null = predefinito)
     * @param chiaviAI chiavi inserite, per id di provider
     * @param modelloAI modello scelto (vuoto = predefinito del provider)
     * @return l'id del profilo creato
     */
    suspend fun creaProfilo(
        nome: String,
        livello: LivelloIniziale = LivelloIniziale.A0,
        contesto: String = "",
        mostraContestoMedico: Boolean = false,
        providerAI: ProviderAI? = null,
        chiaviAI: Map<String, String> = emptyMap(),
        modelloAI: String = ""
    ): String {
        val repo = _repository.value
        val id = "profilo_${System.currentTimeMillis()}"
        val nuovo = ProfiliPreset.nuovo(
            id = id,
            nome = nome.trim().ifBlank { "Profilo" },
            livello = livello,
            contesto = contesto,
            mostraContestoMedico = mostraContestoMedico,
            providerAI = providerAI,
            chiaviAI = chiaviAI,
            modelloAI = modelloAI
        )
        _repository.value = repo.copy(
            profili = repo.profili + (id to nuovo),
            profiloAttivoId = id
        )
        scriviSubito()
        return id
    }

    /** Torna alla schermata di selezione profilo. */
    suspend fun esciDalProfilo() {
        _repository.value = _repository.value.copy(profiloAttivoId = null)
        scriviSubito()
    }

    /**
     * Sostituisce l'intero repository (tutti i profili).
     *
     * Usato dal RIPRISTINO del backup: il chiamante passa già il risultato di
     * [BackupMerge], cioè l'unione fra dati locali e backup. Non è più una
     * sovrascrittura cieca: nessun progresso locale viene scavalcato da una
     * versione meno avanzata. Persiste subito su DataStore.
     */
    suspend fun sostituisciRepository(nuovo: ProfiliRepository) = withContext(io) {
        _repository.value = nuovo
        scriviSubito()
    }

    /** Aggiorna lo stato del profilo attivo (parole, reviews, guida, progresso). */
    /**
     * Aggiorna lo stato del profilo attivo.
     *
     * @param immediato true per le operazioni rare ma importanti (sync del feed,
     *   lezione generata, reset): scrivono subito. Gli aggiornamenti frequenti
     *   (ogni risposta data) vengono coalesciati in una sola scrittura.
     */
    suspend fun aggiornaStatoAttivo(stato: ProfiloStato, immediato: Boolean = false) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(stato = stato, ultimoAccesso = System.currentTimeMillis())
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        if (immediato) scriviSubito() else saveToDataStore()
    }

    fun statoAttivo(): ProfiloStato = profiloAttivo()?.stato ?: ProfiloStato()

    // ---- Scorciatoie sul profilo attivo ----

    fun isCustomWordsEnabled(): Boolean = profiloAttivo()?.config?.enableCustomWords == true

    fun isGoogleSheetsEnabled(): Boolean = profiloAttivo()?.config?.enableGoogleSheets == true

    fun getGoogleSheetId(): String? = profiloAttivo()?.config?.googleSheetId

    fun getGuidaDocId(): String? = profiloAttivo()?.config?.guidaDocId

    /**
     * Feed del profilo attivo.
     *
     * Il feed è facoltativo: se il profilo non ha un URL si usa il lessico
     * incluso nell'app. Nessun feed personale è incorporato.
     */
    fun getFeedUrl(): String {
        val profilo = profiloAttivo()
        val url = profilo?.config?.feedUrl?.trim().orEmpty()
        if (url.isNotEmpty()) return url
        return profilo?.let { ProfiliPreset.feedPredefinito(it.config.tipo) } ?: ProfiliPreset.FEED_URL
    }

    /** Livello CEFR iniziale del profilo attivo. */
    fun getLivelloIniziale(): LivelloIniziale = profiloAttivo()?.config?.livelloIniziale ?: LivelloIniziale.A0

    /** True se il profilo deve vedere il contesto medico (fisioterapia). */
    fun mostraContestoMedico(): Boolean = profiloAttivo()?.config?.mostraContestoMedico == true

    fun nomeProfiloAttivo(): String = profiloAttivo()?.config?.nomeVisualizzato ?: ""

    /** Contesto personale del profilo attivo (professione, obiettivi, interessi). */
    fun contestoUtente(): String = profiloAttivo()?.config?.contestoUtente.orEmpty()

    /** Ordine dei profili, esposto per l'UI. */
    fun ordinePreset(): List<String> = profiliDisponibili().map { it.id }

    /** Aggiorna il contesto personale usato dall'AI per il profilo attivo. */
    suspend fun aggiornaContestoUtente(contesto: String) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(
            config = profilo.config.copy(contestoUtente = contesto.trim())
        )
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /** Aggiorna il feed URL del profilo attivo. */
    suspend fun aggiornaFeedUrl(nuovoUrl: String) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(config = profilo.config.copy(feedUrl = nuovoUrl))
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /** Aggiorna l'ID del documento Google della guida per il profilo attivo. */
    suspend fun aggiornaGuidaDocId(nuovoDocId: String?) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(config = profilo.config.copy(guidaDocId = nuovoDocId))
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /**
     * Canale di pubblicazione delle parole nuove.
     *
     * Le parole restano locali: nessun canale ntfy condiviso è più incorporato.
     * La pubblicazione su un servizio esterno richiede una configurazione
     * esplicita dell'utente (funzione non attiva di default).
     */
    fun topicPubblicazione(): String = ""

    /** Flusso della palette colore scelta per il profilo attivo. */
    val paletteFlow: StateFlow<String> = repositoryFlow
        .map { r -> r.profiloAttivoId?.let { r.profili[it]?.config?.palette } ?: "uber" }
        .stateIn(scope, SharingStarted.Eagerly, "uber")

    /** Cambia la palette colore per il profilo attivo. */
    suspend fun cambiaPalette(nuovaPalette: String) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val nuovoProfilo = profilo.copy(config = profilo.config.copy(palette = nuovaPalette))
        val nuovoRepo = repo.copy(profili = repo.profili + (id to nuovoProfilo))
        _repository.value = nuovoRepo
        saveToDataStore()
    }

    /** Aggiorna la chiave API del tutor AI per il profilo attivo (legacy: Groq). */
    suspend fun aggiornaTutorApiKey(nuovaChiave: String) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val chiavi = profilo.config.chiaviAI.toMutableMap().apply {
            if (nuovaChiave.isBlank()) remove(ProviderAI.GROQ.id)
            else put(ProviderAI.GROQ.id, nuovaChiave.trim())
        }
        val aggiornato = profilo.copy(
            config = profilo.config.copy(
                tutorApiKey = nuovaChiave.trim(),
                chiaviAI = chiavi
            )
        )
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /** Aggiorna la chiave API Gemini per il profilo attivo (legacy). */
    suspend fun aggiornaGeminiApiKey(nuovaChiave: String) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val chiavi = profilo.config.chiaviAI.toMutableMap().apply {
            if (nuovaChiave.isBlank()) remove(ProviderAI.GEMINI.id)
            else put(ProviderAI.GEMINI.id, nuovaChiave.trim())
        }
        val aggiornato = profilo.copy(
            config = profilo.config.copy(
                geminiApiKey = nuovaChiave.trim(),
                chiaviAI = chiavi
            )
        )
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /** Provider AI scelto per il profilo attivo. */
    suspend fun aggiornaProviderAI(provider: ProviderAI) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(
            config = profilo.config.copy(providerAI = provider)
        )
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /** Modello AI scelto per il profilo attivo (vuoto = predefinito del provider). */
    suspend fun aggiornaModelloAI(modello: String) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(
            config = profilo.config.copy(modelloAI = modello.trim())
        )
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /**
     * Salva in un'unica scrittura provider, chiavi e modello del profilo attivo.
     *
     * Le chiavi vuote vengono rimosse: l'utente può cancellare una chiave
     * senza lasciare stringhe vuote nel backup. Le vecchie chiavi separate
     * restano allineate per compatibilità con le versioni precedenti.
     */
    suspend fun aggiornaConfigurazioneAI(
        provider: ProviderAI,
        chiaviAI: Map<String, String>,
        modelloAI: String
    ) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return

        val pulite = chiaviAI
            .mapValues { (_, chiave) -> chiave.trim() }
            .filterValues { it.isNotEmpty() }

        val aggiornato = profilo.copy(
            config = profilo.config.copy(
                providerAI = provider,
                chiaviAI = pulite,
                modelloAI = modelloAI.trim(),
                tutorApiKey = pulite[ProviderAI.GROQ.id].orEmpty(),
                geminiApiKey = pulite[ProviderAI.GEMINI.id].orEmpty()
            )
        )
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /** Segna la missione giornaliera come completata per oggi. */
    suspend fun segnaMissioneCompletata() {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val oggi = java.time.LocalDate.now().toString()
        val aggiornato = profilo.copy(config = profilo.config.copy(missioneGiornalieraCompletata = oggi))
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /** Attiva/disattiva la Modalità Immersione per il profilo attivo. */
    suspend fun cambiaDidatticaImmersione(attiva: Boolean) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(config = profilo.config.copy(didatticaImmersione = attiva))
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }
}
