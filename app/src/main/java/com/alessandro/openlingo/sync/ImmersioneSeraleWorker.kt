package com.alessandro.openlingo.sync

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alessandro.openlingo.R
import com.alessandro.openlingo.OpenLingoApp
import com.alessandro.openlingo.data.DidatticaImmersione
import com.alessandro.openlingo.ui.MainActivity
import java.util.concurrent.TimeUnit

/**
 * Passo 5 del metodo (Deutsch Hub), il "bonus pre-sonno": un promemoria serale
 * per i 15-20 minuti di ascolto passivo. Notifica 100% locale: WorkManager,
 * nessun servizio di rete. Notifica solo se il profilo attivo ha la
 * Modalità Immersione attiva; il testo propone una frase del livello, così
 * l'utente già ascolta del tedesco vero dal lock screen.
 */
class ImmersioneSeraleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as OpenLingoApp
        val pm = app.profileManagerInstance
        if (pm.profiloAttivoId() == null) return Result.success()

        val profilo = pm.profiloAttivo() ?: return Result.success()
        if (!profilo.config.didatticaImmersione) return Result.success()

        val livello = profilo.stato.progresso.livelloCorrente.codiceBreve()
        val frase = DidatticaImmersione.frasiPassive(livello).randomOrNull()
        val messaggio = if (frase != null) {
            "15 minuti di ascolto da sveglio, poi dormi: il sonno consolida. Es: «$frase»"
        } else {
            "15 minuti di ascolto da sveglio, poi dormi: il sonno consolida. Apri la Modalità Immersione."
        }

        sendNotification(
            title = "Modalità Immersione — ascolto serale",
            message = messaggio
        )
        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val ctx = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ascolto passivo serale",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Promemoria del blocco pre-sonno della Modalità Immersione"
            }
            val manager = ctx.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSilent(true)
            .build()

        if (ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "tedesco_immersione"
        private const val NOTIFICATION_ID = 1002
        private const val WORK_NAME = "immersione_serale"

        /** Periodica ogni 24 ore con ritardo iniziale fino all'ora scelta. */
        fun schedule(context: Context, ora: Int = 21) {
            val request = PeriodicWorkRequestBuilder<ImmersioneSeraleWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(
                    NotificheSettings.ritardoFinoAlle(ora),
                    TimeUnit.MILLISECONDS
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
