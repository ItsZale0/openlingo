package com.alessandro.openlingo

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.alessandro.openlingo.data.ProfileManager
import com.alessandro.openlingo.data.WordRepository
import com.alessandro.openlingo.data.remote.FeedService
import com.alessandro.openlingo.data.remote.GeneratoreEsercizi
import com.alessandro.openlingo.data.remote.TutorService
import com.alessandro.openlingo.data.remote.UpdateWorker
import com.alessandro.openlingo.data.remote.VocabPublishService
import com.alessandro.openlingo.di.AppModule
import com.alessandro.openlingo.sync.ReminderWorker
import com.alessandro.openlingo.sync.ImmersioneSeraleWorker
import com.alessandro.openlingo.data.BackupSettings
import kotlinx.coroutines.launch
import com.alessandro.openlingo.sync.BackupWorker
import com.alessandro.openlingo.sync.SyncWorker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class OpenLingoApp : Application(), Configuration.Provider {

    /** Handler originale: il crash resta visibile all'utente ("l'app si e' interrotta"). */
    private val handlerOriginale: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    /** Scope dell'app: per lavoro che deve sopravvivere all'Activity ma non e' un worker. */
    private val appScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
    )

    // Manual DI - singletons
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val json: Json = AppModule.provideJson()
    private val okHttp: OkHttpClient = AppModule.provideOkHttp()
    private val feedService: FeedService = AppModule.provideFeedService(okHttp)
    private val tutorService: TutorService = AppModule.provideTutorService()
    private val vocabPublishService: VocabPublishService = AppModule.provideVocabPublishService()
    private val generatoreEsercizi: GeneratoreEsercizi by lazy { AppModule.provideGeneratoreEsercizi() }

    // Gestione profili utente
    private val profileManager: ProfileManager by lazy {
        AppModule.provideProfileManager(this, json, ioDispatcher)
    }

    private val wordRepository: WordRepository by lazy {
        AppModule.provideWordRepository(this, feedService, json, ioDispatcher, profileManager)
    }

    val wordRepositoryInstance: WordRepository get() = wordRepository
    val profileManagerInstance: ProfileManager get() = profileManager
    val tutorServiceInstance: TutorService get() = tutorService
    val vocabPublishServiceInstance: VocabPublishService get() = vocabPublishService
    val generatoreEserciziInstance: GeneratoreEsercizi get() = generatoreEsercizi

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        installaCrashReporter()
        SyncWorker.schedule(this)
        // Sistema di notifiche: due promemoria locali decisi dalle impostazioni
        // (ripanno 15-20' e ascolto passivo serale). WorkManager dimentica i job
        // in alcuni casi (aggiornamento/reinstallazione), quindi qui si
        // riprogramma idempotentemente (KEEP/UPDATE sul nome univoco).
        appScope.launch {
            val notifiche = com.alessandro.openlingo.sync.NotificheSettings(this@OpenLingoApp)
            if (notifiche.ripassoAttivo()) {
                ReminderWorker.schedule(this@OpenLingoApp, notifiche.ripassoOra())
            } else {
                ReminderWorker.cancel(this@OpenLingoApp)
            }
            if (notifiche.immersioneSeraleAttiva()) {
                ImmersioneSeraleWorker.schedule(this@OpenLingoApp, notifiche.immersioneSeraleOra())
            } else {
                ImmersioneSeraleWorker.cancel(this@OpenLingoApp)
            }
        }
        // Auto-update reale: controllo periodico (24h) anche ad app chiusa + uno
        // immediato all'avvio. Se l'utente ha attivato il download automatico,
        // il worker scarica l'APK e manda la notifica "pronto da installare".
        UpdateWorker.schedule(this)
        UpdateWorker.controllaSubito(this)
        // Riprogramma il backup automatico se l'utente lo aveva attivato
        // (WorkManager perde il job se l'app viene reinstallata/aggiornata).
        appScope.launch {
            if (BackupSettings(this@OpenLingoApp).autoAbilitato()) {
                BackupWorker.schedule(this@OpenLingoApp)
            }
        }
    }

    /**
     * Il sistema chiede memoria: liberiamo tutto cio' che si puo' ricostruire.
     * Senza questo l'app arrivava a riempire l'heap (256 MB) e moriva con
     * OutOfMemoryError anche per allocazioni da 32 byte.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            liberaMemoria()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        liberaMemoria()
    }

    /** Chiude il motore di sintesi e svuota le cache derivate (si ricreano da sole). */
    private fun liberaMemoria() {
        runCatching { com.alessandro.openlingo.ui.TtsCondiviso.chiudi() }
        runCatching { com.alessandro.openlingo.data.VerificaLessico.svuotaCache() }
    }

    /**
     * Salva lo stack trace dell'ultimo crash in filesDir/ultimo_crash.txt, cosi'
     * Impostazioni lo mostra: un crash senza log e' un bug irripetibile.
     * Il handler originale viene chiamato dopo, cosi' l'app si chiude come sempre.
     */
    private fun installaCrashReporter() {
        Thread.setDefaultUncaughtExceptionHandler { thread, errore ->
            runCatching {
                val trace = buildString {
                    appendLine("Data: ${java.util.Date()}")
                    appendLine("Thread: ${thread.name}")
                    append(errore.stackTraceToString())
                    var causa: Throwable? = errore.cause
                    while (causa != null) {
                        appendLine("Causa: $causa")
                        append(causa.stackTraceToString())
                        causa = causa.cause
                    }
                }
                java.io.File(filesDir, "ultimo_crash.txt").writeText(trace)
                android.util.Log.e("CrashReporter", "Crash salvato in filesDir/ultimo_crash.txt", errore)
            }
            handlerOriginale?.uncaughtException(thread, errore)
        }
    }

    companion object {
        fun get(context: Context): OpenLingoApp = context.applicationContext as OpenLingoApp

        /** Ultimo crash salvato (null se l'app non e' mai crashata). */
        fun ultimoCrash(context: Context): String? = runCatching {
            val f = java.io.File(context.filesDir, "ultimo_crash.txt")
            if (f.exists()) f.readText() else null
        }.getOrNull()

        fun cancellaUltimoCrash(context: Context) {
            runCatching { java.io.File(context.filesDir, "ultimo_crash.txt").delete() }
        }
    }
}
