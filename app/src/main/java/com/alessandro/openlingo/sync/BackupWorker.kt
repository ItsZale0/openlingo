package com.alessandro.openlingo.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alessandro.openlingo.BuildConfig
import com.alessandro.openlingo.OpenLingoApp
import com.alessandro.openlingo.data.BackupEsecutore
import com.alessandro.openlingo.data.BackupSettings
import java.util.concurrent.TimeUnit

/**
 * Backup automatico periodico su GitHub.
 *
 * NOTA sui tempi: Android impone un minimo di 15 minuti per il lavoro periodico
 * in background (WorkManager rifiuta intervalli piu' brevi). Per avere un
 * intervallo di 5 minuti REALI mentre l'app e' in uso, il backup periodico e'
 * affiancato da un timer in-app (vedi [com.alessandro.openlingo.ui.AppNav]).
 * Fuori dall'app vale il minimo di sistema: 15 minuti.
 *
 * Il worker non ricarica se il contenuto non e' cambiato (hash), quindi non
 * consuma richieste GitHub inutilmente.
 */
class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as OpenLingoApp
        // Nessun profilo attivo: non c'e' nulla da salvare
        if (app.profileManagerInstance.profiloAttivoId() == null) return Result.success()

        val settings = BackupSettings(app)
        if (!settings.autoAbilitato()) return Result.success()

        return when (BackupEsecutore.esegui(app, BuildConfig.VERSION_NAME, forza = false)) {
            is BackupEsecutore.Esito.Ok,
            BackupEsecutore.Esito.NessunCambiamento -> Result.success()
            is BackupEsecutore.Esito.NonConfigurato -> Result.success()
            is BackupEsecutore.Esito.Errore -> Result.retry()  // riprova (rete, 5xx)
        }
    }

    companion object {
        private const val WORK_NAME = "backup_automatico"

        /**
         * Intervallo del backup periodico in background: il MINIMO consentito da
         * Android per il lavoro periodico. Non si puo' scendere sotto.
         */
        private const val MINUTI_BACKGROUND = 15L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BackupWorker>(
                MINUTI_BACKGROUND, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
