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
import com.alessandro.openlingo.data.WordRepository
import com.alessandro.openlingo.ui.MainActivity
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repo: WordRepository by lazy {
        val app = applicationContext as OpenLingoApp
        app.wordRepositoryInstance
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as OpenLingoApp
        if (app.profileManagerInstance.profiloAttivoId() == null) {
            return Result.success()
        }

        // L'utente ha disattivato il promemoria: non notificare (il worker
        // resta programmato ma silenzioso, e viene rimosso alla prossima
        // programmazione).
        val notifiche = com.alessandro.openlingo.sync.NotificheSettings(applicationContext)
        if (!notifiche.ripassoAttivo()) return Result.success()

        val dueCount = repo.observeDueCount().first()
        val paroleTotali = repo.observeWords().first().size

        if (dueCount > 0) {
            sendNotification(
                title = "Ripasso giornaliero",
                message = "Hai $dueCount parole da ripassare oggi. Non perdere la serie!",
                dueCount = dueCount
            )
        } else if (paroleTotali > 0) {
            sendNotification(
                title = "Deutsch — Giornata completata",
                message = "Hai finito tutti i ripassi! Torna domani per nuove parole.",
                dueCount = 0
            )
        }

        return Result.success()
    }

    private fun sendNotification(title: String, message: String, dueCount: Int) {
        val ctx = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Promemoria Deutsch",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Promemoria giornalieri per il ripasso del tedesco"
            }
            val manager = ctx.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .apply {
                if (dueCount > 0) {
                    setCategory(NotificationCompat.CATEGORY_REMINDER)
                }
            }
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
        private const val CHANNEL_ID = "tedesco_reminder"
        private const val NOTIFICATION_ID = 1001
        private const val WORK_NAME = "reminder_giornaliero"

        fun schedule(context: Context, ora: Int = 20) {
            // Periodica ogni 24 ore, con ritardo iniziale fino all'ora scelta:
            // il promemoria arriva a orario stabile, non "un minuto dopo l'avvio".
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
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
