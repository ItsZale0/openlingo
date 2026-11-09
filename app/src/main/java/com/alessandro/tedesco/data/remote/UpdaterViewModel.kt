package com.alessandro.tedesco.data.remote

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alessandro.tedesco.TedescoApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.alessandro.tedesco.di.HttpCondiviso
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data object UpToDate : UpdateState()
    data class Available(val version: VersionInfo) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class ReadyToInstall(val file: File) : UpdateState()
    data object NeedPermission : UpdateState()
    data class Error(val message: String, val retryable: Boolean = true) : UpdateState()
}

class UpdaterViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TedescoApp

    private val updateSettings = UpdateSettings(app)

    private val client = HttpCondiviso.client

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    /** Esito dell'ultimo controllo, mostrato in Impostazioni per capire se funziona. */
    private val _ultimoControllo = MutableStateFlow("")
    val ultimoControllo: StateFlow<String> = _ultimoControllo.asStateFlow()

    private var downloadJob: Job? = null

    companion object {
        // Canale di aggiornamento attivo. Il vecchio /version.json nella root resta
        // congelato alla 1.36.33 (canale legacy): la 1.36.33 era firmata con una chiave
        // diversa, quindi Android non può installarne sopra una build firmata con la
        // chiave attuale. Tenerlo fermo evita un download che fallirebbe alla firma.
        const val URL_RAW =
            "https://raw.githubusercontent.com/ItsZale0/tedesco-app/master/updates/version.json"
        const val URL_API_CONTENTS =
            "https://api.github.com/repos/ItsZale0/tedesco-app/contents/updates/version.json"
    }

    val versioneCorrente: String = runCatching {
        val pm = getApplication<Application>().packageManager
        pm.getPackageInfo(getApplication<Application>().packageName, 0).versionName ?: "0"
    }.getOrDefault("0")

    val versioneCorrenteCode: Int = runCatching {
        val pm = getApplication<Application>().packageManager
        val info = pm.getPackageInfo(getApplication<Application>().packageName, 0)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            info.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            info.versionCode
        }
    }.getOrDefault(0)

    /** Timestamp dell'ultimo controllo automatico (0 = mai). */
    private var ultimoControlloAutomatico = 0L

    /**
     * Controllo automatico con throttling: al massimo una volta ogni
     * [intervalloMs]. Evita una richiesta di rete a ogni ritorno in primo piano
     * (consumo di batteria e dati senza alcun beneficio).
     */
    fun checkForUpdateSeNecessario(intervalloMs: Long = 6 * 60 * 60 * 1000L) {
        val adesso = System.currentTimeMillis()
        if (adesso - ultimoControlloAutomatico < intervalloMs) return
        ultimoControlloAutomatico = adesso
        checkForUpdate()
    }

    fun checkForUpdate() {
        if (_state.value is UpdateState.Checking) return
        _state.value = UpdateState.Checking
        _ultimoControllo.value = ""
        android.util.Log.d("Updater", "Controllo aggiornamenti… (versione locale: $versioneCorrenteCode)")

        viewModelScope.launch {
            try {
                val version = withContext(Dispatchers.IO) { leggiVersioneServer() }

                android.util.Log.d("Updater", "Versione server: ${version.versionCode} (${version.versionName}), locale: $versioneCorrenteCode")
                _ultimoControllo.value =
                    "Server: v${version.versionName} (${version.versionCode}) — locale: $versioneCorrente"

                _state.value = if (version.versionCode > versioneCorrenteCode &&
                    version.versionCode != updateSettings.versioneSaltata()
                ) {
                    UpdateState.Available(version)
                } else {
                    UpdateState.UpToDate
                }
            } catch (e: Exception) {
                android.util.Log.e("Updater", "Errore controllo aggiornamenti", e)
                val msg = messaggioErrore(e)
                _ultimoControllo.value = "Ultimo controllo fallito: $msg"
                _state.value = UpdateState.Error(msg, retryable = true)
            }
        }
    }

    /**
     * Legge updates/version.json da due fonti: prima raw.githubusercontent.com (veloce, senza
     * autenticazione); se non risponde (VPN, DNS, blocco Cloudflare) prova l'API
     * GitHub contents (base64). Entrambe fallite → IOException.
     */
    private fun leggiVersioneServer(): VersionInfo {
        // Legge da entrambe le fonti e prende la versione PIU' RECENTE: la cache CDN di
        // raw.githubusercontent.com può restituire per ore il file vecchio dopo un push,
        // e in quel caso l'auto-update non partirebbe mai.
        val sorgenti = listOf(
            runCatching { scaricaVersione(URL_RAW) },
            runCatching { scaricaVersioneDaApi() }
        ).mapNotNull { it.getOrNull() }

        return versionePiuRecente(sorgenti)
            ?: throw IOException("Entrambe le fonti di versione non rispondono")
    }

    private fun scaricaVersione(url: String): VersionInfo {
        val request = Request.Builder().url("$url?t=${System.currentTimeMillis()}").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            val body = response.body?.string() ?: throw IOException("Risposta vuota dal server")
            return Json.decodeFromString<VersionInfo>(body)
        }
    }

    /** Fallback: /contents/updates/version.json di GitHub restituisce il file in base64. */
    private fun scaricaVersioneDaApi(): VersionInfo {
        val request = Request.Builder().url(URL_API_CONTENTS).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            return decodificaContenutoApi(response.body?.string() ?: "{}")
        }
    }

    fun apkGiaScaricato(version: VersionInfo): Boolean {
        val dir = File(getApplication<Application>().cacheDir, "updates")
        val apk = File(dir, "Deutsch-v${version.versionName}.apk")
        return apk.exists() && VerificaApk.isApkValido(apk, version.apkSha256)
    }

    fun downloadUpdate(version: VersionInfo) {
        downloadJob?.cancel()

        // Se l'APK è già scaricato e valido, salta il download
        if (apkGiaScaricato(version)) {
            val dir = File(getApplication<Application>().cacheDir, "updates")
            val apk = File(dir, "Deutsch-v${version.versionName}.apk")
            android.util.Log.d("Updater", "APK già scaricato: ${apk.length()} byte")
            _state.value = UpdateState.ReadyToInstall(apk)
            return
        }

        _state.value = UpdateState.Downloading(0)
        android.util.Log.d("Updater", "Download v${version.versionName} da ${version.apkUrl}")

        downloadJob = viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val dir = File(getApplication<Application>().cacheDir, "updates")
                    dir.mkdirs()

                    // Cancella APK vecchi nella cartella
                    dir.listFiles()?.forEach { f ->
                        if (f.extension == "apk") {
                            f.delete()
                        }
                    }

                    val apk = File(dir, "Deutsch-v${version.versionName}.apk")

                    // Prova l'URL dichiarato, poi una variante con nome file minuscolo:
                    // l'asset su GitHub e' case-sensitive e il task può divergere.
                    val urlAlternative = version.apkUrl.replace("Deutsch-v", "deutsch-v")
                    val fonti = listOf(version.apkUrl, urlAlternative).distinct()
                    val riuscita = fonti.any { url -> scaricaApk(url, apk, version.apkSha256) }
                    if (!riuscita) throw IOException("Download fallito da tutte le fonti")

                    // Verifica integrità APK (struttura + SHA-256 del manifest)
                    if (!VerificaApk.isApkValido(apk, version.apkSha256)) {
                        apk.delete()
                        throw IOException("APK scaricato non valido o corrotto")
                    }

                    apk
                }

                android.util.Log.d("Updater", "Download completato: ${file.length()} byte")
                _state.value = UpdateState.ReadyToInstall(file)
            } catch (e: Exception) {
                android.util.Log.e("Updater", "Errore download", e)
                _state.value = UpdateState.Error(messaggioErrore(e), retryable = true)
            }
        }
    }

    /**
     * Scarica l'APK in `apk` dall'URL dato. Ritorna true se riuscito (e l'APK e'
     * valido), false in caso di errore di rete — cosi' si puo' provare un'altra fonte.
     */
    private suspend fun scaricaApk(url: String, apk: File, shaAtteso: String = ""): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching false
                val body = response.body ?: return@runCatching false
                val total = body.contentLength()
                var downloaded = 0L
                apk.outputStream().use { out ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            out.write(buffer, 0, read)
                            downloaded += read
                            if (total > 0) {
                                _state.value = UpdateState.Downloading((downloaded * 100 / total).toInt())
                            }
                        }
                    }
                }
            }
            VerificaApk.isApkValido(apk, shaAtteso)
        }.getOrDefault(false)
    }

    /** Prende la versione più alta tra le fonti disponibili (cache CDN può essere vecchia). */
    internal fun versionePiuRecente(sorgenti: List<VersionInfo>): VersionInfo? =
        sorgenti.maxByOrNull { it.versionCode }

    /**
     * Decodifica la risposta di /repos/{owner}/{repo}/contents/version.json:
     * {"content": "<base64>"}. Testata perché l'auto-update non deve dipendere
     * da una sola fonte.
     */
    internal fun decodificaContenutoApi(json: String): VersionInfo {
        val base64 = Json.parseToJsonElement(json).jsonObject["content"]
            ?.jsonPrimitive?.content ?: throw IOException("Campo content assente")
        val decoded = String(Base64.getDecoder().decode(base64.replace("\\s".toRegex(), "")))
        return Json.decodeFromString<VersionInfo>(decoded)
    }

    fun hasInstallPermission(): Boolean {
        val pm = getApplication<Application>().packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pm.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun installUpdate(file: File) {
        viewModelScope.launch {
            try {
                if (!hasInstallPermission()) {
                    android.util.Log.w("Updater", "Permesso installazione app sconosciuti NON concesso")
                    _state.value = UpdateState.NeedPermission
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    val uri = FileProvider.getUriForFile(
                        getApplication<Application>(),
                        "${getApplication<Application>().packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    getApplication<Application>().startActivity(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("Updater", "Errore installazione", e)
                _state.value = UpdateState.Error("Installazione fallita: ${e.message}", retryable = true)
            }
        }
    }

    fun apriImpostazioniInstallazione() {
        val packageName = getApplication<Application>().packageName
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:$packageName")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    fun dismiss() {
        downloadJob?.cancel()
        _state.value = UpdateState.Idle
    }

    fun skipUpdate(version: VersionInfo) {
        viewModelScope.launch {
            updateSettings.salvaVersioneSaltata(version.versionCode)
            downloadJob?.cancel()
            _state.value = UpdateState.Idle
            android.util.Log.d("Updater", "Aggiornamento v${version.versionName} saltato dall'utente")
        }
    }

    private fun messaggioErrore(e: Exception): String {
        return when (e) {
            is UnknownHostException -> "Nessuna connessione a internet. Controlla la rete e riprova."
            is SocketTimeoutException -> "Timeout della connessione. Il server non risponde, riprova più tardi."
            is IOException -> {
                val msg = e.message ?: ""
                when {
                    msg.startsWith("HTTP ") -> "Errore del server (${msg.removePrefix("HTTP ")}). Riprova più tardi."
                    msg.contains("vuota", ignoreCase = true) -> "Il server ha risposto in modo non valido. Riprova più tardi."
                    else -> "Errore di rete: $msg"
                }
            }
            is kotlinx.serialization.SerializationException -> "Il file di versione del server non è valido. Riprova più tardi."
            else -> "Errore imprevisto: ${e.message ?: e.javaClass.simpleName}"
        }
    }
}
