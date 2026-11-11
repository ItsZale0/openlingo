package com.alessandro.openlingo.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alessandro.openlingo.R
import com.alessandro.openlingo.di.HttpCondiviso
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.Base64
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Auto-update VERO: gira anche ad app chiusa (WorkManager, ogni 24h).
 *
 * 1. legge version.json da GitHub (raw + API contents);
 * 2. confronta versionCode con quello installato;
 * 3. se c'è una versione nuova: notifica. Se l'utente ha attivato il download
 *    automatico, scarica l'APK in cache e notifica "pronto da installare".
 */
class UpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val impostazioni = UpdateSettings(applicationContext)
        if (!impostazioni.autoControllo()) return Result.success()

        val versione = runCatching { leggiVersione() }.getOrElse { return Result.retry() }
        if (versione.versionCode <= versioneInstallata()) return Result.success()

        creaCanale(applicationContext)

        val autoDownload = impostazioni.autoDownload()
        if (autoDownload) {
            val apk = runCatching { scaricaApk(versione) }.getOrNull()
            if (apk != null) {
                notificaPronto(apk, versione)
                return Result.success()
            }
        }
        notificaDisponibile(versione)
        return Result.success()
    }

    private fun versioneInstallata(): Int = runCatching {
        val pm = applicationContext.packageManager
        val info = pm.getPackageInfo(applicationContext.packageName, 0)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            info.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            info.versionCode
        }
    }.getOrDefault(0)

    private fun leggiVersione(): VersionInfo {
        val sorgenti = listOf(
            runCatching { scaricaVersione(UpdaterViewModel.URL_RAW) },
            runCatching { scaricaVersioneDaApi() }
        ).mapNotNull { it.getOrNull() }
        return sorgenti.maxByOrNull { it.versionCode }
            ?: throw IOException("Nessuna fonte di versione disponibile")
    }

    private fun scaricaVersione(url: String): VersionInfo {
        val request = Request.Builder().url("$url?t=${System.currentTimeMillis()}").build()
        HttpCondiviso.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            val body = response.body?.string() ?: throw IOException("Risposta vuota")
            return Json.decodeFromString<VersionInfo>(body)
        }
    }

    private fun scaricaVersioneDaApi(): VersionInfo {
        val request = Request.Builder().url(UpdaterViewModel.URL_API_CONTENTS).build()
        HttpCondiviso.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            val json = response.body?.string() ?: throw IOException("Risposta vuota")
            val base64 = Json.parseToJsonElement(json).jsonObject["content"]
                ?.jsonPrimitive?.content ?: throw IOException("Campo content assente")
            val decoded = String(Base64.getDecoder().decode(base64.replace("\\s".toRegex(), "")))
            return Json.decodeFromString<VersionInfo>(decoded)
        }
    }

    private fun scaricaApk(versione: VersionInfo): File {
        val dir = File(applicationContext.cacheDir, "updates").apply { mkdirs() }
        dir.listFiles()?.filter { it.extension == "apk" }?.forEach { it.delete() }
        val apk = File(dir, "Deutsch-v${versione.versionName}.apk")
        val fonti = listOf(
            versione.apkUrl,
            versione.apkUrl.replace("Deutsch-v", "deutsch-v")
        ).distinct()
        for (url in fonti) {
            val ok = runCatching {
                val request = Request.Builder().url(url).build()
                HttpCondiviso.client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use false
                    val body = response.body ?: return@use false
                    apk.outputStream().use { out ->
                        body.byteStream().use { input -> input.copyTo(out) }
                    }
                    true
                }
            }.getOrDefault(false)
            if (ok && VerificaApk.isApkValido(apk)) return apk
        }
        throw IOException("Download APK non riuscito")
    }

    private fun notificaDisponibile(versione: VersionInfo) {
        val intent = Intent(applicationContext, com.alessandro.openlingo.ui.MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            applicationContext, 1001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notifica = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Aggiornamento disponibile: v${versione.versionName}")
            .setContentText("Tocca per aprire l'app e installare la nuova versione.")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching { NotificationManagerCompat.from(applicationContext).notify(ID_DISPONIBILE, notifica) }
    }

    private fun notificaPronto(apk: File, versione: VersionInfo) {
        val puoInstallare = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.packageManager.canRequestPackageInstalls()
        } else true

        val intent = if (puoInstallare) {
            runCatching {
                val uri = FileProvider.getUriForFile(
                    applicationContext,
                    "${applicationContext.packageName}.fileprovider",
                    apk
                )
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }.getOrElse {
                Intent(applicationContext, com.alessandro.openlingo.ui.MainActivity::class.java)
            }
        } else {
            Intent(applicationContext, com.alessandro.openlingo.ui.MainActivity::class.java)
        }

        val pending = PendingIntent.getActivity(
            applicationContext, 1002, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notifica = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Aggiornamento pronto: v${versione.versionName}")
            .setContentText(
                if (puoInstallare) "Tocca per installare l'APK già scaricato."
                else "Tocca e concedi il permesso di installare app sconosciute."
            )
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        runCatching { NotificationManagerCompat.from(applicationContext).notify(ID_PRONTO, notifica) }
    }

    companion object {
        const val CHANNEL_ID = "aggiornamenti_app"
        private const val ID_DISPONIBILE = 2001
        private const val ID_PRONTO = 2002
        private const val NOME_LAVORO = "controllo_aggiornamenti"
        private const val NOME_SUBITO = "controllo_aggiornamenti_subito"

        fun creaCanale(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val canale = NotificationChannel(
                CHANNEL_ID,
                "Aggiornamenti app",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avvisi quando è disponibile una nuova versione dell'app"
            }
            manager.createNotificationChannel(canale)
        }

        /** Controllo periodico ogni 24 ore, anche ad app chiusa. */
        fun schedule(context: Context) {
            creaCanale(context)
            val richiesta = PeriodicWorkRequestBuilder<UpdateWorker>(24, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOME_LAVORO,
                ExistingPeriodicWorkPolicy.UPDATE,
                richiesta
            )
        }

        /** Controllo immediato all'avvio (una volta sola, con rete). */
        fun controllaSubito(context: Context) {
            val richiesta = OneTimeWorkRequestBuilder<UpdateWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                NOME_SUBITO,
                ExistingWorkPolicy.REPLACE,
                richiesta
            )
        }

        fun annulla(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(NOME_LAVORO)
            WorkManager.getInstance(context).cancelUniqueWork(NOME_SUBITO)
        }
    }
}
