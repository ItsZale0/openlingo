package com.alessandro.tedesco.data.remote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.BackupCloud
import com.alessandro.tedesco.data.BackupEsecutore
import com.alessandro.tedesco.data.BackupMerge
import com.alessandro.tedesco.data.BackupSettings
import com.alessandro.tedesco.data.EsportaDati
import com.alessandro.tedesco.sync.NotificheSettings
import com.alessandro.tedesco.sync.TemaSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Stato dell'operazione di backup, per messaggi onesti all'utente. */
sealed interface BackupStato {
    data object Idle : BackupStato
    data object InCorso : BackupStato
    data class Ok(val messaggio: String) : BackupStato
    data class Errore(val messaggio: String) : BackupStato
}

/**
 * Backup cloud su GitHub: carica una fotografia di TUTTI i profili su un repo
 * privato, e la ripristina.
 *
 * Il ripristino UNISCE il backup ai dati locali (BackupMerge): nessuna parola,
 * ripasso, unità di grammatica o lezione viene sovrascritta con una versione
 * meno avanzata. Per questo non parte mai da un tap solo: la schermata mostra
 * il riepilogo di cosa contiene il backup e chiede conferma.
 */
class BackupViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TedescoApp
    private val settings = BackupSettings(application)

    val token: StateFlow<String> = settings.tokenFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val repo: StateFlow<String> = settings.repoFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val ultimoBackup: StateFlow<Long> = settings.ultimoBackupFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    /** Nome del file di backup usato per il profilo attivo (separato per utente). */
    val nomeFileBackup: StateFlow<String> = app.profileManagerInstance.repositoryFlow
        .map { r -> BackupCloud.nomeFilePerProfilo(r.profiloAttivoId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, BackupCloud.NOME_FILE)

    private val _stato = MutableStateFlow<BackupStato>(BackupStato.Idle)
    val stato: StateFlow<BackupStato> = _stato

    /** Backup automatico attivo/disattivo. */
    val auto: StateFlow<Boolean> = settings.autoFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Timestamp dell'ultimo backup automatico. */
    val ultimoAuto: StateFlow<Long> = settings.ultimoAutoFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    /**
     * Attiva/disattiva il backup automatico.
     * Quando e' attivo: periodico in background (minimo 15 min imposto da Android)
     * + ogni 5 minuti mentre l'app e' aperta.
     */
    fun impostaAuto(abilitato: Boolean) {
        viewModelScope.launch {
            settings.salvaAuto(abilitato)
            if (abilitato) {
                com.alessandro.tedesco.sync.BackupWorker.schedule(getApplication())
            } else {
                com.alessandro.tedesco.sync.BackupWorker.cancel(getApplication())
            }
        }
    }

    /** Riepilogo del backup scaricato/importato, in attesa di conferma per il ripristino. */
    private val _daRipristinare = MutableStateFlow<BackupCloud.Riepilogo?>(null)
    val daRipristinare: StateFlow<BackupCloud.Riepilogo?> = _daRipristinare
    private var contenutoDaRipristinare: String? = null
    /** Colore tema da un file di esportazione completa (se presente). */
    private var temaDaRipristinare: String? = null

    fun salvaToken(t: String) { viewModelScope.launch { settings.salvaToken(t) } }
    fun salvaRepo(r: String) { viewModelScope.launch { settings.salvaRepo(r) } }
    fun pulisciStato() { _stato.value = BackupStato.Idle }

    private fun client(repoNormalizzato: String? = null, nomeFile: String = BackupCloud.NOME_FILE): BackupGitHubClient? {
        val t = token.value.trim()
        if (t.isBlank()) return null
        val r = repoNormalizzato ?: repo.value.trim().ifBlank { BackupCloud.REPO_DEFAULT }
        return BackupGitHubClient(t, r, nomeFile)
    }

    /**
     * Normalizza il repo: se l'utente scrive solo il nome ("tedesco-backup")
     * aggiunge l'owner ricavato dal token. L'API vuole sempre {owner}/{repo},
     * quindi "tedesco-backup" da solo darebbe 404.
     */
    private suspend fun repoNormalizzato(): String {
        val r = repo.value.trim().trimEnd('/')
        if (r.isBlank()) return BackupCloud.REPO_DEFAULT
        if (r.contains("/")) return r
        val owner = client()?.ownerDelToken()
        return if (owner.isNullOrBlank()) r else "$owner/$r"
    }

    /** Verifica token + repo senza toccare i dati. */
    fun provaConnessione() {
        if (token.value.isBlank()) {
            _stato.value = BackupStato.Errore("Inserisci prima il token GitHub"); return
        }
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            val c = client(repoNormalizzato()) ?: return@launch
            _stato.value = when (val e = c.verificaAccesso()) {
                is BackupCloud.Esito.Ok -> BackupStato.Ok(e.dettaglio)
                is BackupCloud.Esito.Errore -> BackupStato.Errore(e.messaggio)
            }
        }
    }

    /** Carica il backup (tutti i profili) sul repo privato. */
    fun eseguiBackup(versioneApp: String) {
        if (token.value.isBlank()) {
            _stato.value = BackupStato.Errore("Inserisci prima il token GitHub"); return
        }
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            _stato.value = when (val e = BackupEsecutore.esegui(getApplication(), versioneApp, forza = true)) {
                is BackupEsecutore.Esito.Ok -> {
                    val n = app.profileManagerInstance.repositoryFlow.first().profili.size
                    BackupStato.Ok("Backup completato: $n profili, ${e.byteCaricati / 1024} KB")
                }
                is BackupEsecutore.Esito.Errore -> BackupStato.Errore(e.messaggio)
                is BackupEsecutore.Esito.NonConfigurato -> BackupStato.Errore(e.motivo)
                BackupEsecutore.Esito.NessunCambiamento ->
                    BackupStato.Ok("Nessuna modifica da salvare")
            }
        }
    }

    /** Scarica il backup e mostra il riepilogo; il ripristino richiede conferma. */
    fun preparaRipristino() {
        if (token.value.isBlank()) {
            _stato.value = BackupStato.Errore("Inserisci prima il token GitHub"); return
        }
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            try {
                val repoAttivo = repoNormalizzato()
                val baseClient = client(repoAttivo) ?: return@launch
                val profiloId = app.profileManagerInstance.profiloAttivoId()
                val cercati = BackupCloud.candidatiRipristino(profiloId)

                // 1. File del profilo attivo, poi backup condiviso. Il backup
                //    viene caricato sul file del profilo attivo al momento del
                //    salvataggio: senza questo ordine, cambiando profilo il
                //    ripristino non trovava più nulla.
                var json: String? = null
                for (nome in cercati) {
                    val c = client(repoAttivo, nome) ?: break
                    json = c.scarica()
                    if (json != null) break
                }

                // 2. Nessun candidato: cerca qualunque backup nel repo e usa il
                //    più recente e leggibile (es. backup di un altro profilo).
                if (json == null) {
                    val disponibili = baseClient.elencaBackup()
                    val date = disponibili.mapNotNull { nome ->
                        baseClient.dataUltimoCommit(nome)?.let { nome to it }
                    }.toMap()
                    for (nome in BackupCloud.ordinaBackup(profiloId, disponibili, date)) {
                        if (nome in cercati) continue
                        val c = client(repoAttivo, nome) ?: continue
                        val letto = runCatching { c.scarica() }.getOrNull() ?: continue
                        if (runCatching { BackupCloud.deserializza(letto) }.isSuccess) {
                            json = letto
                            break
                        }
                    }
                }

                if (json == null) {
                    // Distingue "repo/token non accessibile" da "repo vuoto".
                    _stato.value = when (val accesso = baseClient.verificaAccesso()) {
                        is BackupCloud.Esito.Errore ->
                            BackupStato.Errore("${accesso.messaggio}. Backup non scaricato.")
                        is BackupCloud.Esito.Ok ->
                            BackupStato.Errore(
                                "Nessun backup trovato in $repoAttivo. " +
                                    "File cercati: ${cercati.joinToString(", ")}. " +
                                    "Se il backup è stato caricato da un altro profilo, " +
                                    "controlla che il repo contenga tedesco-backup.json."
                            )
                    }
                    return@launch
                }

                val contenuto = json
                val pacchetto = BackupCloud.deserializza(contenuto)
                contenutoDaRipristinare = contenuto
                _daRipristinare.value = BackupCloud.riepilogo(pacchetto)
                _stato.value = BackupStato.Idle
            } catch (e: Exception) {
                _stato.value = BackupStato.Errore(e.message ?: "Errore sconosciuto")
            }
        }
    }

    fun annullaRipristino() {
        _daRipristinare.value = null
        contenutoDaRipristinare = null
        temaDaRipristinare = null
    }

    /**
     * Carica un backup da file locale (esportazione completa o backup cloud JSON)
     * e mostra il riepilogo; il ripristino richiede conferma come per il cloud.
     */
    fun preparaRipristinoDaFile(jsonTesto: String) {
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            try {
                val (contenutoBackup, tema) = withContext(Dispatchers.Default) {
                    if (EsportaDati.valida(jsonTesto)) {
                        val comeBackup = EsportaDati.comeBackup(jsonTesto)
                            ?: error("File di esportazione non leggibile")
                        val temaColore = runCatching {
                            val p = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                                .decodeFromString(EsportaDati.Pacchetto.serializer(), jsonTesto)
                            p.temaColore.takeIf { it.isNotBlank() }
                        }.getOrNull()
                        comeBackup to temaColore
                    } else {
                        jsonTesto to null
                    }
                }
                val pacchetto = BackupCloud.deserializza(contenutoBackup)
                contenutoDaRipristinare = contenutoBackup
                temaDaRipristinare = tema
                _daRipristinare.value = BackupCloud.riepilogo(pacchetto)
                _stato.value = BackupStato.Idle
            } catch (e: Exception) {
                _stato.value = BackupStato.Errore(e.message ?: "Errore sconosciuto")
            }
        }
    }

    /**
     * Applica il ripristino UNENDO il backup ai dati locali.
     *
     * Non è una sovrascrittura: lessico, ripassi, padronanza grammaticale e
     * progressi delle lezioni vengono confrontati voce per voce e si tiene la
     * versione più avanzata (BackupMerge). Ripristinare su un dispositivo non
     * cancella più lo studio fatto su quel dispositivo dopo l'ultimo backup.
     */
    fun confermaRipristino() {
        val json = contenutoDaRipristinare ?: return
        val tema = temaDaRipristinare
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            try {
                val pacchetto = BackupCloud.deserializza(json)
                withContext(Dispatchers.IO) {
                    val locale = app.profileManagerInstance.repositoryFlow.first()
                    val unito = BackupMerge.fondi(locale, pacchetto.dati)
                    app.profileManagerInstance.sostituisciRepository(unito)
                    pacchetto.notifiche?.let { n: BackupCloud.NotificheBackup ->
                        NotificheSettings(getApplication()).applica(n)
                    }
                    if (!tema.isNullOrBlank()) {
                        TemaSettings(getApplication()).setColore(tema)
                    }
                }
                _daRipristinare.value = null
                contenutoDaRipristinare = null
                temaDaRipristinare = null
                _stato.value = BackupStato.Ok("Ripristino completato: ${pacchetto.dati.profili.size} profili (progressi uniti)")
            } catch (e: Exception) {
                _stato.value = BackupStato.Errore("Ripristino fallito: ${e.message}")
            }
        }
    }

    /**
     * Applica il ripristino SOVRASCRIVENDO i dati locali col backup.
     *
     * Nessuna unione: il telefono prende il backup così com'è. I progressi
     * fatti localmente dopo l'ultimo backup vengono persi.
     */
    fun confermaRipristinoSostituendo() {
        val json = contenutoDaRipristinare ?: return
        val tema = temaDaRipristinare
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            try {
                val pacchetto = BackupCloud.deserializza(json)
                withContext(Dispatchers.IO) {
                    app.profileManagerInstance.sostituisciRepository(pacchetto.dati)
                    pacchetto.notifiche?.let { n: BackupCloud.NotificheBackup ->
                        NotificheSettings(getApplication()).applica(n)
                    }
                    if (!tema.isNullOrBlank()) {
                        TemaSettings(getApplication()).setColore(tema)
                    }
                }
                _daRipristinare.value = null
                contenutoDaRipristinare = null
                temaDaRipristinare = null
                _stato.value = BackupStato.Ok("Ripristino completato: ${pacchetto.dati.profili.size} profili (backup sostituito)")
            } catch (e: Exception) {
                _stato.value = BackupStato.Errore("Ripristino fallito: ${e.message}")
            }
        }
    }
}
